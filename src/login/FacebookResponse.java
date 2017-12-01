package login;

import java.net.URLEncoder;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;








public class FacebookResponse extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException { 
		HashMap<String,String> details=new HashMap<String,String>();
		String requestToken=request.getParameter("oauth_token");
		//tokensecret=request.getParameter("oauth_token_secret");
		
		String oauthResponseUrl = FacebookLogin.callback; 
		String accessTokenUrl = "https://www.linkedin.com/uas/oauth2/accessToken";
		String requestParams = "";
		requestParams += "code=" + URLEncoder.encode(request.getParameter("code"), "UTF-8"); //No I18N
		requestParams += "&client_id=" + URLEncoder.encode(FacebookLogin.CONSUMER_KEY, "UTF-8");
		requestParams += "&client_secret=" + URLEncoder.encode(FacebookLogin.CONSUMER_SECRET, "UTF-8");
		requestParams += "&redirect_uri=" +URLEncoder.encode(FacebookLogin.callback, "UTF-8");
		requestParams += "&grant_type=authorization_code";  //No I18N
		int connectionTimeOut = 3000;
		accessTokenUrl += "?"+requestParams;
		String accesstoken;
		try {
			accesstoken = new JSONObject(FacebookLogin.getUrlObj(accessTokenUrl, "GET")).getString("access_token");
			
			String dataFetchUrl = "https://api.linkedin.com/v1/people/~:";
			dataFetchUrl += "(id,first-name,last-name,industry,public-profile-url,picture-url,headline,skills,positions)"; //No I18N
			dataFetchUrl += "?" + "oauth2_access_token=" + accesstoken; //No I18N
			String res = FacebookLogin.getUrlObj(dataFetchUrl, "GET");
			response.getWriter().println(res);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	


}