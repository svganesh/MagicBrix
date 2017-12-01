package login;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/** * Servlet implementation class LoginServlet */ 
public class TwitterLogin extends HttpServlet {
	 
    private final static String getTokenURL = "https://api.twitter.com/oauth/request_token";
    private static String bearerToken;
    public static final String callback="http://mywebsite.com:8080/SWProject/twitter/redirect";
    public static final String CONSUMER_KEY = "Wn6cC0zxfyUgfFYp68UgVw";
    public static final String CONSUMER_SECRET= "PgYFYtHLeJFJoV1awZpGCpp75CmE0gOoHnzOvkd7Lg";
   // static String secretcode;
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException { 
		  HashMap<String,String> bearer=new HashMap<String,String>();

	                try {

	                    bearerToken = requestBearerToken();
	            		String[] parts=bearerToken.split("&");	            		
	            		String[] val=null;
	            		for(int i=0;i<parts.length;i++)
	            		{
	            			val=parts[i].split("=");
	            			bearer.put(val[0],val[1]);
	            		}
	                    response.sendRedirect("https://api.twitter.com/oauth/authenticate?"+"oauth_token="+bearer.get("oauth_token"));
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	   
	}
	     
	        

	    

	    public static String getNonce(){
	        char[] chars = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
	        StringBuilder sb = new StringBuilder();
	        Random random = new Random();
	        for (int i = 0; i < 32; i++) {
	            char c = chars[random.nextInt(chars.length)];
	            sb.append(c);
	        }
	        String nonce = sb.toString();

	        return nonce;
	    }


	    public static String getTimestamp(){
	        long time = System.currentTimeMillis();
	        return String.valueOf(time/1000);
	    }
	    
	    public static String getSign(String request_uri,String nonce, String timestamp,String oauthtoken,String secrettoken, String method) throws UnsupportedEncodingException{
	    	return getSign(request_uri, nonce, timestamp, oauthtoken, secrettoken, method, null);
	    }

	    
	    
	    public static String getSign(String request_uri,String nonce, String timestamp,String oauthtoken,String secrettoken, String method, String count) throws UnsupportedEncodingException
	    {

	        String sign_method ="HMAC-SHA1";
	       // request_uri=URLEncoder.encode(request_uri, "UTF-8"); 
	        String callbackurl=URLEncoder.encode(callback, "UTF-8"); 
	        String countStirng="";
	        if(count!=null) {
	        	countStirng = "&count="+count;
	        }
	        String paramstring="oauth_consumer_key="+CONSUMER_KEY+"&oauth_nonce="+nonce+"&oauth_signature_method="+sign_method+"&oauth_timestamp="+timestamp+"&oauth_token="+oauthtoken+"&oauth_version=1.0"+countStirng;
	        paramstring = "oauth_callback=" + callbackurl + "&" + paramstring;
	        String baseString=method + "&"+URLEncoder.encode(request_uri , "UTF-8")+"&"+URLEncoder.encode(paramstring , "UTF-8");
	        String signKey = URLEncoder.encode(CONSUMER_SECRET, "UTF-8")+"&"+URLEncoder.encode(secrettoken, "UTF-8");           //REQUEST TOKEN NOT KNOWN

	        try {
	            SecretKeySpec signingKey = new SecretKeySpec(signKey.getBytes(),"HmacSHA1");
	            Mac mac = Mac.getInstance(signingKey.getAlgorithm());
	            mac.init(signingKey);
	            byte[] rawHmac = mac.doFinal(baseString.getBytes());
	            String result = new String(java.util.Base64.getEncoder().encode(rawHmac));
	            return result;

	        } catch (GeneralSecurityException e) {
	            return "";
	        }
	    }

	    public static String requestBearerToken()
	            throws IOException {
	        
	        String nonce = getNonce();
	        String timestamp = getTimestamp();
	        String signature = getSign(getTokenURL,nonce, timestamp,"","", "POST");
	        String oauth_callback_encoded = URLEncoder.encode(callback, "UTF-8");
	        String header = "oauth_callback=\"" + oauth_callback_encoded + "\", " +
	                "oauth_consumer_key=\"" + CONSUMER_KEY + "\", " +
	                "oauth_nonce=\"" + nonce + "\", " +
	                "oauth_signature=\"" + URLEncoder.encode(signature, "UTF-8") + "\", " +
	                "oauth_signature_method=\"" + "HMAC-SHA1" + "\", " +
	                "oauth_timestamp=\"" + timestamp + "\", " +
	                "oauth_token=\"" + "\", " +
	                "oauth_version=\"1.0\"";
	        return(getUrlObj(getTokenURL,header, null,"POST"));
	        
	    }
	        public static String getUrlObj(String address,String header,String verifier,String method) throws IOException{
	        	HttpURLConnection connection = null;
	        try {
	            
	            URL url = new URL(address);
	            connection = (HttpURLConnection) url.openConnection();
	            connection.setDoOutput(true);
	            connection.setDoInput(true);
	            connection.setRequestMethod(method);
	            connection.setRequestProperty("Host", "api.twitter.com");
	            connection.setRequestProperty("User-Agent", "oauthApplication");
	            connection.setRequestProperty("Authorization", "OAuth "+header);
	            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
	            connection.setUseCaches(false);
	            if(verifier!=null)
	            {
	            	OutputStreamWriter output = new OutputStreamWriter(connection.getOutputStream());
	        		output.write("oauth_verifier="+verifier);
	        		output.close();
	            }
	            String response = readResponse(connection);
	            return response;
	        } catch (MalformedURLException e) {
	            throw new IOException("Invalid endpoint URL specified.", e);
	        } finally {
	            if (connection != null) {
	                connection.disconnect();
	            }
	        }
	     
	    }
	    
	    private static String readResponse(HttpURLConnection connection) {
	        try {
	            StringBuilder str = new StringBuilder();

	            BufferedReader br = new BufferedReader(new InputStreamReader(
	                    connection.getInputStream()));
	            String line = "";
	            while ((line = br.readLine()) != null) {
	                str.append(line + System.getProperty("line.separator"));
	            }
	            return str.toString();
	        } catch (IOException e) {
	            return new String(e.getMessage());
	        }
	    }

}