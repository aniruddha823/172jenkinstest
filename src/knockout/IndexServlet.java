package knockout;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

@WebServlet("/IndexServlet")
public class IndexServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public IndexServlet() {
		super();
	}
	
	/** Thao Tran 
	 * @return HttpResponse*/
	public int searchForTweets(String tweet, HttpServletResponse response) {
		int statusCode = 0;
		try {
			// 1. Create the OAuthConsumer object
			//		First parameter is : ConsumerKey: "MHWyn8J9wdgdA13SCleFp3aqF";
			//		Second parameter is : ConsumerSecrete: "yp7tHUEctEzQRqa1r99Cu8jKQlx54wuSW66pxe4H4xNpdYMbxQ"
			//	 These keys were issued to me at https://apps.twitter.com/app/14870247/show
			// TODO store this OAuthConsumer object globally.
			OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(Constants.Key, Constants.Secret);

			// 2. Set the access tokens so that I can use
			oAuthConsumer.setTokenWithSecret(Constants.Token, Constants.TokenSecret);

			// 3a. The API url provided from twitter: https://api.twitter.com/1.1/search/tweets
			// 3b. The required query key that twitter needs: q=%23
			// 3c. Apend the tweet to the end of the query.
			// TODO: Use a smarter way to build the Query String.
			String urlQuery = "https://api.twitter.com/1.1/search/tweets.json?q=%23" + tweet;

			// Create the GET object
			HttpGet gett = new HttpGet(urlQuery);

			// Create the TRANSPORT to execute the HttpGet
			HttpClient httpClient = new DefaultHttpClient();

			// Sign it with the crediential.
			// This oAuthCOnsumer object can be reused to sign new HttpGet objects.
			oAuthConsumer.sign(gett);

			// Execute the TRANSPORT and get back the Repsonse
			HttpResponse httpResponse = httpClient.execute(gett);

			// Reponse code, see https://developer.twitter.com/en/docs/basics/response-codes
			statusCode = httpResponse.getStatusLine().getStatusCode();

			// The httpResponse object has all the data in JSON in its getEntity()
			String json = EntityUtils.toString(httpResponse.getEntity());
			JSONParser simpleParser = new JSONParser();
			JSONObject obj = (JSONObject) simpleParser.parse(json);

			if (obj.containsKey("statuses")) {
				JSONArray vals = (JSONArray) obj.get("statuses");
				ArrayList<String> list = new ArrayList<String>();
				for (int i = 0; i < vals.size(); i++) {
					JSONObject jObj = (JSONObject) vals.get(i);
					if (jObj.containsKey("text"))
						list.add(jObj.get("text").toString());
				}
				Helper.printArrayToResponse(list, response);
			}
		} catch (Exception e) {
			Helper.printStringToReponse("Exception thrown " + e.getLocalizedMessage() + e.getStackTrace().toString(),
					response);
		}
		return statusCode;
	}

	/** Thao Tran */
	public int sendMessage(String userName, String msg, HttpServletResponse response) {
		String query = "https://api.twitter.com/1.1/direct_messages/events/new.json";
		String userID = lookUpUserID(userName, response);
		int statusCode = 0;
		if (userName.isEmpty() || userID.isEmpty()) {
			Helper.printStringToReponse("Failed to send message, cannot find the user.", response);
			return statusCode;
		}

		String jsonQuery = String.format(
				"{ \"event\": { \"type\": \"message_create\", \"message_create\": { \"target\": { \"recipient_id\": \"%s\" }, \"message_data\": { \"text\": \"%s\" } } } }",
				userID, msg);

		OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(Constants.Key, Constants.Secret);
		oAuthConsumer.setTokenWithSecret(Constants.Token, Constants.TokenSecret);
		try {

			HttpPost post = new HttpPost(query);
			post.addHeader("Content-Type", "application/json");
			post.addHeader("event", jsonQuery);
			StringEntity jsonEntity = new StringEntity(jsonQuery);
			post.setEntity(jsonEntity);
			oAuthConsumer.sign(post);

			HttpClient transport = new DefaultHttpClient();
			HttpResponse r = transport.execute(post);
			if (r.getStatusLine().getStatusCode() == 200) {
				Helper.printStringToReponse("Successfully sent message.", response);
			} else {
				Helper.printStringToReponse("Failed to send message, reason: " + r.getStatusLine().getReasonPhrase(),
						response);
			}
		} catch (Exception e) {
			Helper.printStringToReponse(e.getMessage(), response);
		}
		return statusCode;
	}

	/** Gurvir Gill *
	* @return HttpResponse*/
	public int getFollowerList(String screen_name, HttpServletResponse response) {
		int statusCode = 0;
		try {
			// Create the OAuthConsumer object
			OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(Constants.Key, Constants.Secret);

			// Set the access tokens
			oAuthConsumer.setTokenWithSecret(Constants.Token, Constants.TokenSecret);

			// The API url provided from twitter: https://api.twitter.com/1.1/followers/list
			String urlQuery = "https://api.twitter.com/1.1/followers/list.json?cursor=-1&screen_name=" + screen_name;

			// Create the GET object
			HttpGet followers_get = new HttpGet(urlQuery);

			// Create the TRANSPORT to execute the HttpGet
			HttpClient httpClient = new DefaultHttpClient();

			// Sign get with the oAuthCOnsumer object credentials
			oAuthConsumer.sign(followers_get);

			// Execute the TRANSPORT and get back the Repsonse
			HttpResponse httpResponse = httpClient.execute(followers_get);

			statusCode = httpResponse.getStatusLine().getStatusCode();

			String json = EntityUtils.toString(httpResponse.getEntity());

			JSONParser simpleParser = new JSONParser();
			JSONObject obj = (JSONObject) simpleParser.parse(json);

			if (obj.containsKey("users")) {
				JSONArray vals = (JSONArray) obj.get("users");
				ArrayList<String> list = new ArrayList<String>();

				int count = 0;

				for (int i = 0; i < vals.size(); i++) {
					JSONObject jObj = (JSONObject) vals.get(i);

					if (jObj.containsKey("name"))
						list.add("Follower " + ++count + ": " + jObj.get("name").toString());

				}
				Helper.printArrayToResponse(list, response);
			}
		} catch (Exception e) {
			Helper.printStringToReponse("Exception thrown " + e.getLocalizedMessage() + e.getStackTrace().toString(),
					response);
		}
		return statusCode;
	}

	/** Gurvir Gill 
	 * @return HttpResponse*/
	public int getUsers(String username, HttpServletResponse response) {
		int statusCode = 0;
		try {
			OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(Constants.Key, Constants.Secret);
			oAuthConsumer.setTokenWithSecret(Constants.Token, Constants.TokenSecret);

			String urlQuery = "https://api.twitter.com/1.1/users/show.json?screen_name=" + username;

			HttpGet user_get = new HttpGet(urlQuery);

			HttpClient httpClient = new DefaultHttpClient();

			oAuthConsumer.sign(user_get);

			HttpResponse httpResponse = httpClient.execute(user_get);
			
			statusCode = httpResponse.getStatusLine().getStatusCode();

			String json = EntityUtils.toString(httpResponse.getEntity());

			JSONParser simpleParser = new JSONParser();
			JSONObject obj = (JSONObject) simpleParser.parse(json);
			ArrayList<String> list = new ArrayList<String>();

			if (obj.containsKey("screen_name"))
				list.add("Username: " + obj.get("screen_name").toString());

			if (obj.containsKey("followers_count"))
				list.add("Followers count: " + obj.get("followers_count").toString());

			if (obj.containsKey("friends_count"))
				list.add("Friends count: " + obj.get("friends_count").toString());

			if (obj.containsKey("status")) {
				JSONObject jObj = (JSONObject) obj.get("status");
				if (jObj.containsKey("text"))
					list.add("Status: " + jObj.get("text").toString());
			}
			Helper.printArrayToResponse(list, response);

		} catch (Exception e) {
			Helper.printStringToReponse("Exception thrown " + e.getLocalizedMessage() + e.getStackTrace().toString(),
					response);
		}
		return statusCode;
	}

	/** Gurvir Gill 	
	* @return HttpResponse*/
	public int follow(String username, HttpServletResponse response) {
		int statusCode = 0;
		try {
			OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(Constants.Key, Constants.Secret);
			oAuthConsumer.setTokenWithSecret(Constants.Token, Constants.TokenSecret);

			String urlQuery = "https://api.twitter.com/1.1/friendships/create.json?screen_name=" + username
					+ "&follow=true";

			HttpPost follow_Post = new HttpPost(urlQuery);

			HttpClient httpClient = new DefaultHttpClient();

			oAuthConsumer.sign(follow_Post);

			HttpResponse httpResponse = httpClient.execute(follow_Post);

			statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == 200)
				response.getWriter().println("Follow request has been sent to " + username);

		} catch (Exception e) {
			Helper.printStringToReponse("Exception thrown " + e.getLocalizedMessage() + e.getStackTrace().toString(),
					response);
		}
		return statusCode;
	}

	/** Gurvir Gill 
	 * @return HttpResponse*/
	public int unfollow(String username, HttpServletResponse response) {
		int statusCode = 0;
		try {
			OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(Constants.Key, Constants.Secret);
			oAuthConsumer.setTokenWithSecret(Constants.Token, Constants.TokenSecret);

			String urlQuery = "https://api.twitter.com/1.1/friendships/destroy.json?screen_name=" + username;

			HttpPost follow_Post = new HttpPost(urlQuery);

			HttpClient httpClient = new DefaultHttpClient();

			oAuthConsumer.sign(follow_Post);

			HttpResponse httpResponse = httpClient.execute(follow_Post);

			statusCode = httpResponse.getStatusLine().getStatusCode();

			if (statusCode == 200)
				response.getWriter().println("You have successfully unfollowed " + username);

		} catch (Exception e) {
			Helper.printStringToReponse("Exception thrown " + e.getLocalizedMessage() + e.getStackTrace().toString(),
					response);
		}
		return statusCode;
	}

	/** Farhan Mohmad Tamam 
	 * @return HttpResponse*/
	public int getTrendsNear(String location, HttpServletResponse response) {//throws IOException {
		int woeidStatusCode  = 0;
		try {
			// Create the TRANSPORT to execute the HttpGet
			HttpClient httpClient = new DefaultHttpClient();
			String trendQuery = "";

			//if no location is provided, treat it as Global with woeid=1
			if (!location.isEmpty()) {
				String woeidQwuery = "https://query.yahooapis.com/v1/public/yql?q=select%20woeid%20from%20geo.places%20where%20text%3D%22"
						+ URLEncoder.encode(location, "UTF-8") + "%22%20limit%201&diagnostics=false&format=json";
				//				System.out.println(woeidQwuery);
				HttpGet gettWoeid = new HttpGet(woeidQwuery);
				HttpResponse woeidHttpResponse = httpClient.execute(gettWoeid);
				woeidStatusCode = woeidHttpResponse.getStatusLine().getStatusCode();
				response.getWriter().println("<br>Yahoo woeid API Status Code: " + woeidStatusCode);
				String woeidStr = EntityUtils.toString(woeidHttpResponse.getEntity());
				//				Helper.printStringToReponse(woeidStr, response);

				JSONParser simpleParser = new JSONParser();
				JSONObject woeidObj = (JSONObject) simpleParser.parse(woeidStr);
				String woeid = "";

				//getting the woeid by travering 4 levels down
				//there is probably a better way to do this
				//sample json: {"query":{"count":1,"created":"2018-03-09T00:38:25Z","lang":"en-US","results":{"place":{"woeid":"12589342"}}}}
				if (woeidObj.containsKey("query")) {
					JSONObject qObj = (JSONObject) woeidObj.get("query");

					Long count = (Long) qObj.get("count");
					//check if a woeid for this location is found
					if (count > 0) {
						if (qObj.containsKey("results")) {
							JSONObject rObj = (JSONObject) qObj.get("results");

							if (rObj.containsKey("place")) {
								JSONObject pObj = (JSONObject) rObj.get("place");

								if (pObj.containsKey("woeid")) {
									woeid = (String) pObj.get("woeid");
								} //end if woeid
							} //end if place
						} //end if results
					} else {
						Helper.printStringToReponse("<br>Sorry, location: <em>" + location
								+ "</em> not found!<br />Try different location!", response);
						return woeidStatusCode;
					}
				} //end if query

				Helper.printStringToReponse(
						"<br><h3>Finding Trends near <em>'" + location + "'</em> with woeid: " + woeid + "</h3>",
						response);
				//				System.out.println("woeid: "+woeid);

				//api call to get twitter trends near woeid (where on earth id)
				trendQuery = "https://api.twitter.com/1.1/trends/place.json?id=" + woeid;
				//				System.out.println(trendQuery);
			} else {
				Helper.printStringToReponse("<br><h3>Finding <em>Global</em> Trends with woeid: " + 1 + "</h3>",
						response);
				System.out.println("woeid: " + 1);

				trendQuery = "https://api.twitter.com/1.1/trends/place.json?id=" + 1;
				//				System.out.println(trendQuery);
			}

			HttpGet getTrends = new HttpGet(trendQuery);

			OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(Constants.Key, Constants.Secret);
			oAuthConsumer.setTokenWithSecret(Constants.Token, Constants.TokenSecret);
			oAuthConsumer.sign(getTrends);
			HttpResponse httpResponse = httpClient.execute(getTrends);

			// Reponse code, see https://developer.twitter.com/en/docs/basics/response-codes
			int trendsStatusCode = httpResponse.getStatusLine().getStatusCode();
			response.getWriter().println("<br>twitter trendsNear API Status Code: " + trendsStatusCode + "<br>");

			String jsonStr = EntityUtils.toString(httpResponse.getEntity());
			//			Helper.printStringToReponse(jsonStr, response);

			if (trendsStatusCode == 200) {
				JSONArray jsonArray = (JSONArray) JSONValue.parse(jsonStr);
				for (Object o : jsonArray) {
					JSONObject obj = (JSONObject) o;

					if (obj.containsKey("trends")) {
						JSONArray vals = (JSONArray) obj.get("trends");
						ArrayList<String> list = new ArrayList<String>();
						for (int j = 0; j < vals.size(); j++) {
							JSONObject jObj = (JSONObject) vals.get(j);

							if (jObj.containsKey("name"))
								list.add(jObj.get("name").toString());
						}

						Helper.printArrayToResponse(list, response);
						//Helper.printStringToReponse(jsonStr, response);
					}
				}
			} else {
				//				Helper.printStringToReponse(jsonStr, response);
				Helper.printStringToReponse("<br>Sorry, no trends found!<br />Try different location!", response);
			}

		} catch (Exception e) {
			Helper.printStringToReponse(
					"Exception thrown " + e.getLocalizedMessage() + "<br />" + e.getStackTrace().toString(), response);
			//			response.getWriter().println("<br>stack trace: <br>" + e.getStackTrace().toString());
			//			e.printStackTrace();
		}
		return woeidStatusCode;
	}

	/** Achraf Derdak */
	public String lookUpUserID(String userName, HttpServletResponse response) {
		//https://api.twitter.com/1.1/users/lookup.json?screen_name=twitterapi,twitter
		String query = "https://api.twitter.com/1.1/users/lookup.json?screen_name=" + userName;
		try {
			OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(Constants.Key, Constants.Secret);
			oAuthConsumer.setTokenWithSecret(Constants.Token, Constants.TokenSecret);
			HttpGet get = new HttpGet(query);
			oAuthConsumer.sign(get);

			HttpClient transport = new DefaultHttpClient();
			HttpResponse reply = transport.execute(get);

			String json = EntityUtils.toString(reply.getEntity());

			JSONParser simpleParser = new JSONParser();
			JSONArray arr = (JSONArray) simpleParser.parse(json);

			for (int i = 0; i < arr.size(); i++) {
				JSONObject obj = (JSONObject) arr.get(i);
				if (obj.containsKey("id_str"))
					return obj.get("id_str").toString();

			}
		} catch (Exception e) {
			Helper.printStringToReponse("Exception throw lookupuserid " + e.getMessage(), response);
		}
		return "";
	}

	/** Aniruddha Prabhu 
	 * @return HttpResponse*/
	public int getTimeline(String username, HttpServletResponse response){
		int statusCode = 0;
		try {
			OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(Constants.Key, Constants.Secret); // create oauth consumer
			oAuthConsumer.setTokenWithSecret(Constants.Token, Constants.TokenSecret); //set access tokens
			
			// twitter api query to get tweets from a timeline, only includes last 10 tweets
			String query = "https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=" + username + "&count=10";
			
			HttpGet tltweets = new HttpGet(query); // creates a GET object
			HttpClient hc = new DefaultHttpClient(); //creates a transport to execute GET
			oAuthConsumer.sign(tltweets); // sign the query with the tokens/credentials provided earlier
			
			HttpResponse hr = hc.execute(tltweets); //execute the http get and get a response
			
			statusCode = hr.getStatusLine().getStatusCode();
			response.getWriter().println("<br>Status Code: " + statusCode);
			response.getWriter().println("</br>");
			response.getWriter().println("<br>Here are the last 10 tweets from @" + username + ":");
			response.getWriter().println("</br>");
			
			String json = EntityUtils.toString(hr.getEntity());
			JSONParser simpleParser = new JSONParser(); // json parser to filter tweet data

			JSONArray x = (JSONArray) simpleParser.parse(json);
			ArrayList<String> list = new ArrayList<String>(); //list to store the 10 tweets from a timeline
			for (int i = 0; i < x.size(); i++){
				JSONObject jObj = (JSONObject) x.get(i);

				if (jObj.containsKey("text"))
					list.add(jObj.get("text").toString()); //get the text of the tweet and add it to the list

			}

			Helper.printArrayToResponse(list, response);
		}
		catch(Exception e) {
			Helper.printStringToReponse("Exception thrown " + e.getLocalizedMessage() + e.getStackTrace().toString(),
					response);
		}
		return statusCode;
	}


	/** Everyone */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println("<h1>CMPE172 TEAMKNOCKOUT</h1>");
        Helper.insertBackToHomeButton(request, response);
		String button = request.getParameter("button");

		if (button.equals("Search Tweets")) {
			String field = request.getParameter("field");
			int code = searchForTweets(field, response);
			Assert.assertEquals(code,200);
		} else if (button.equals("List Followers")) {
			String screen_name = request.getParameter("user");
			int code = getFollowerList(screen_name, response);
			Assert.assertEquals(code,200);
		} else if (button.equals("Search Users")) {
			String username = request.getParameter("username");
			int code = getUsers(username, response);
			Assert.assertEquals(code,200);
		} else if (button.equals("Search Trends")) {
			String location = request.getParameter("location");
			int code = getTrendsNear(location, response);
			Assert.assertEquals(code,200);
		} else if (button.equals("get Global Trends")) {
			String globalLocation = request.getParameter("location");
			int code = getTrendsNear(globalLocation, response);
			Assert.assertEquals(code,200);
		} else if (button.equals("Get Statuses")) {
			String handle = request.getParameter("handle");
			int code = getTimeline(handle, response);
			Assert.assertEquals(code,200);
		}

		response.getWriter().println("<h1>Done!</h1>");
	}

	/** Everyone */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println("<h1>CMPE172 TEAMKNOCKOUT</h1>");

		String button = request.getParameter("button");
		if (button.equals("Follow")) {
			String username = request.getParameter("username");
			int code = follow(username, response);
			Assert.assertEquals(code,200);
		} else if (button.equals("Unfollow")) {
			String username = request.getParameter("username1");
			int code = unfollow(username, response);
			Assert.assertEquals(code,200);
		} else if (button.equals("Send Direct Message")) {
			String userName = request.getParameter("userName");
			String msg = request.getParameter("msg");
			int code = sendMessage(userName, msg, response);
			Assert.assertEquals(code,200);
		}

		response.getWriter().println("<h1>Done!</h1>");
	}

}
