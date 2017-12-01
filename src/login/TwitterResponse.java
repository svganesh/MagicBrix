package login;

import java.net.URLEncoder;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;



public class TwitterResponse extends HttpServlet {
	private final static String accessTokenURL = "https://api.twitter.com/oauth/access_token";

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException { 
		HashMap<String,String> details=new HashMap<String,String>();
		String requestToken=request.getParameter("oauth_token");
		//tokensecret=request.getParameter("oauth_token_secret");
		String nonce=TwitterLogin.getNonce();
		String timestamp=TwitterLogin.getTimestamp();
		String signature=TwitterLogin.getSign(accessTokenURL,nonce,timestamp,requestToken,"", "POST");
		
		//HttpsURLConnection connection = null;
		String oauth_callback_encoded = URLEncoder.encode(TwitterLogin.callback, "UTF-8");
		String header = "oauth_callback=\"" + oauth_callback_encoded + "\", " +
		        "oauth_consumer_key=\"" + TwitterLogin.CONSUMER_KEY + "\", " +
		        "oauth_nonce=\"" + nonce + "\", " +
		        "oauth_signature=\"" + URLEncoder.encode(signature, "UTF-8") + "\", " +
		        "oauth_signature_method=\"" + "HMAC-SHA1" + "\", " +
		        "oauth_timestamp=\"" + timestamp + "\", " +
		        "oauth_token=\"" +(requestToken!=null?requestToken:"")+ "\", " +
		        "oauth_version=\"1.0\"";
		String responseobj=TwitterLogin.getUrlObj(accessTokenURL,header,request.getParameter("oauth_verifier"),"POST");
	    HttpSession session = request.getSession(true);//to be removed
		//session.setAttribute("code", (String)str);
		session.setAttribute("responseobj",responseobj); 
		response.sendRedirect("http://mywebsite.com:8080/SWProject/userLogged.jsp"); //logged-in page 

	}
	


}