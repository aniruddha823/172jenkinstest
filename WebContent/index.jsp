<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<center>
<head>
<div>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Team Knockout HW#2</title>
<h1>Team Knockout HW#2 Using Twitter API</h1>
</div>
</head>
</center>
<body bgcolor="#0082E7">
    <div style="padding-left:350px">
   <% if( request.getParameter("buttonName") != null) { session.setAttribute("status", "guest"); } %>

    <FORM action="IndexServlet" NAME="form1" METHOD="GET">
        <input type="hidden" name="button">
       
        <table>
            <tr> 
                <td>  <h3>Search Tweets</h3> </td> 
            </tr>
            <tr>
                <td> Enter keyword: </td>
                <td> <input type="text" name="field" value=""/> </td>
                <td> <input type="button" value="Search Tweets" onclick="button1(this.value, field.value)"/> </td>
            </tr>
            <tr> 
                <td> <h3>List followers of user</h3> </td> 
            </tr>
            <tr>
                <td> Enter username: </td>
                <td> <input type="text" name="user" value=""/> </td>
                <td> <input type="button" value="List Followers" onclick="button1(this.value, user.value)"/> </td>
            </tr>
            <tr> 
                <td> <h3>Search users</h3> </td> 
            </tr>
            <tr> 
                <td> Enter username: </td> 
                <td> <input type="text" name="username" value=""/> </td>
                <td> <input type="button" value="Search Users" onclick="button1(this.value, username.value)"/> </td>
            </tr>
            <tr> 
                <td><h3>Search for trends</h3></td> 
            </tr>
            <tr> 
                <td> Enter location: </td> 
                <td> <input type="text" name="location" value=""/> </td>
                <td> <input type="button" value="Search Trends" onclick="button1(this.value, location.value)"/>
                <input type="button" value="Global Trends" onclick="button1(this.value, '')"/> </td>
            </tr>
            <tr> 
                <td>  <h3>Get Statuses</h3> </td> 
            </tr>
            <tr>
                <td> Enter a username: </td>
                <td> <input type="text" name="handle" value=""/> </td>
                <td> <input type="button" value="Get Statuses" onclick="button1(this.value, handle.value)"/> </td>
            </tr>
    </FORM>

    <form action="IndexServlet" name="form2" METHOD="POST">
        <input type="hidden" name="button">
        
            <tr> 
                <td> <h3>Follow a user</h3> </td> 
            </tr>
            <tr> 
                <td> Enter username:</td> 
                <td> <input type="text" name="username" value=""/> </td>
                <td> <input type="button" value="Follow" onclick="button2(this.value, username.value)"/> </td>
            </tr>
            <tr> 
                <td> <h3>Unfollow a user</h3> </td> 
            </tr>
            <tr> 
                <td> Enter username: </td> 
                <td> <input type="text" name="username1" value=""/> </td>
                <td> <input type="button" value="Unfollow" onclick="button2(this.value, username1.value)"/> </td>
            </tr>
            <tr> 
                <td> <h3>Direct Message</h3> </td> 
            </tr>
            <tr> 
                <td> Enter username: </td> 
                <td> <input type="text" name="userName" value=""/> </td>  
            </tr>
            <tr> 
                <td> Enter message: </td> 
                <td> <input type="text" name="msg" value=""/> </td> 
                <td> <input type="button" value="Send Direct Message" onclick="button3(this.value, userName.value, msg.value)"/> </td> 
            </tr>
            </table>
    </form>
    </div>

    <SCRIPT LANGUAGE="JavaScript">
        function button1(val, val2)
        {
            if(val == "Search Tweets"){
                document.form1.field.value = val2;
            }
            else if(val == "List Followers"){
                document.form1.user.value = val2;
            }
            else if(val == "Search Users"){
                document.form1.username.value = val2;
            }
            if(val == "Search Trends"){
                document.form1.location.value = val2;
            }
            if(val == "get Global Trends"){
                document.form1.location.value = val2;
            }
            document.form1.button.value = val;
            form1.submit();
        }
        function button2(val, val2)
        {
            if(val == "Follow"){
                document.form2.username.value = val2;
            }
            else if(val == "Unfollow"){
                document.form2.username1.value = val2;
            }
            document.form2.button.value = val;
            form2.submit();
        }
        function button3(val, val2, val3){
            document.form2.userName.value = val2;
            document.form2.msg.value = val3;
            document.form2.button.value = val;
            form2.submit();
        }
    </SCRIPT>
</body>
</html>
