package knockout;
import java.util.*;
import javax.servlet.http.*;
import org.apache.http.util.EntityUtils;

public class Helper{

    /** Achraf Derdak */
    public static void insertBackToHomeButton(HttpServletRequest request, HttpServletResponse response) {
        try {
            String homeURL = request.getContextPath();
            String backButtonHtml = "<input type=\"button\" onclick=\"location.href=\'REPLACEME\';\" value=\"HOME\" />";
            backButtonHtml = backButtonHtml.replace("REPLACEME", homeURL);
            response.getWriter().println(backButtonHtml);
        } catch (Exception e) {

        }
    }

    /** Thao Tran */
    public static void printArrayToResponse(Enumeration<?> data, HttpServletResponse response) {
        try {
            ArrayList<String> list = new ArrayList<String>();
            while(data.hasMoreElements()){
                list.add(data.nextElement().toString());
            }
            printArrayToResponse(list, response);
        } catch (Exception e) {

        }
    }

    /** Thao Tran */
    public static void printArrayToResponse(String[] data, HttpServletResponse response){
        if ( data == null || data.length == 0 ){
            return;
        }
        ArrayList<String> list = new ArrayList<String>();
        for( int i = 0; i < data.length; i++){
            list.add(data[i]);
        }
        printArrayToResponse(list, response);
    }

    /** Thao Tran */
    public static void printArrayToResponse(ArrayList<String> data, HttpServletResponse response){
        try {
            for(int i = 0; i < data.size(); i++){
                response.getWriter().println(String.format("<br>%s<br>", data.get(i)));
            }
        } catch (Exception e) {
       
        }
    }
    
    /** Thao Tran */
    public static void printStringToReponse(String data, HttpServletResponse response) {
        try {
                response.getWriter().println(String.format("<br>%s<br>", data));
        } catch (Exception e) {

        }
    }

    /** Thao Tran */
    public static String getJSONStringFromResponse(org.apache.http.HttpResponse response){
        try {            
            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            return "Exception thrown at getJSONStringFromResponse() " + e.getLocalizedMessage();
        }
    }


}
