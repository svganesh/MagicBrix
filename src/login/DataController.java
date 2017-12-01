package login;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

/**
 * Servlet implementation class DataController
 */
public class DataController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String  API_KEY = "AIzaSyCabY3IHKk5BpGgiUHuJumC-jLThblTWHQ";
       


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		String location = request.getParameter("location");
		String userInfo = (String)request.getSession().getAttribute("userinfo");
		String profession = "Student";
		String works = "Arizona%20State%20University";
		String[] urls = {
				"https://www.zillow.com/webservice/GetDeepSearchResults.htm?zws-id=X1-ZWz1g3fx99xekr_5m9vn&address=3255+S+Dorsey+Ln&citystatezip=Tempe%2C+AZ"+"&key="+API_KEY,
				"https://www.zillow.com/webservice/GetDeepSearchResults.htm?zws-id=X1-ZWz1g3fx99xekr_5m9vn&address=401+S+Mill+Ave&citystatezip=Tempe%2C+AZ"+"&key="+API_KEY,
				"https://www.zillow.com/webservice/GetDeepSearchResults.htm?zws-id=X1-ZWz1g3fx99xekr_5m9vn&address=114+E+7th+St&citystatezip=Tempe%2C+AZ"+"&key="+API_KEY,
				"https://www.zillow.com/webservice/GetDeepSearchResults.htm?zws-id=X1-ZWz1g3fx99xekr_5m9vn&address=202+E+University+Dr&citystatezip=Tempe%2C+AZ"+"&key="+API_KEY,
				"https://www.zillow.com/webservice/GetDeepSearchResults.htm?zws-id=X1-ZWz1g3fx99xekr_5m9vn&address=149+S+Farmer+Ave&citystatezip=Tempe%2C+AZ"+"&key="+API_KEY,
				"https://www.zillow.com/webservice/GetDeepSearchResults.htm?zws-id=X1-ZWz1g3fx99xekr_5m9vn&address=250+E+University+Dr&citystatezip=Tempe%2C+AZ"+"&key="+API_KEY
				
				
		};
		JSONObject result = null;

		try {
			JSONArray jsonArray = new JSONArray();
			for(int i=0; i<urls.length; i++) {
				String responseobj=TwitterLogin.getUrlObj(urls[i],null,null,"GET");
				result = new JSONObject();
				JSONObject json = XML.toJSONObject(responseobj);
				JSONObject jArray = (JSONObject) json.opt("SearchResults:searchresults");
				jArray = (JSONObject) jArray.get("response");
				jArray = (JSONObject) jArray.get("results");
				JSONObject entry = null;
				try {
				JSONArray results = (JSONArray) jArray.opt("result");
				entry = (JSONObject) results.opt(0);
				}catch(Exception e) {
					entry = (JSONObject) jArray.opt("result");
				}
				long bedrooms =  entry.optLong("bedrooms");
				double bathrooms =  entry.optDouble("bathrooms");
				long yearBuilt =  entry.optLong("yearBuilt");
				long finishedSqFt =   entry.optLong("finishedSqFt");
				JSONObject links = (JSONObject) entry.opt("links");
				JSONObject localRealEstate = (JSONObject) entry.opt("localRealEstate");
				localRealEstate = (JSONObject) localRealEstate.opt("region");
				String name = localRealEstate.optString("name");
				JSONObject address = (JSONObject) entry.opt("address");

				result.put("name", name);
				result.put("address", address.optString("street")+","+address.optString("city")+","+ address.optString("state"));
				result.put("latitude", address.optString("latitude"));
				result.put("longitude", address.optString("longitude"));
				result.put("finishedSqFt", finishedSqFt == 0?  "Not Available": finishedSqFt );
				Random rnd = new Random();
				if(finishedSqFt>500) {
					result.put("bedrooms", bedrooms == 0.0 ? rnd.nextInt(2)+1: bedrooms);
				}else {
					result.put("bedrooms", 1);
				}
				if(Double.isFinite(bathrooms) && finishedSqFt>500) {
					result.put("bathrooms", (int)Math.ceil(bathrooms));
				}else {
					result.put("bathrooms", 1);
				}
				result.put("yearBuilt", yearBuilt == 0? "Not Available": yearBuilt);
				result.put("homedetails", links.optString("homedetails"));
				String walkScore=TwitterLogin.getUrlObj("http://api.walkscore.com/score?format=json&transit=1&bike=1&wsapikey=cb704c0c5a54ed7cdd8ee5e9e1cd0d92&lat="+address.optString("latitude")+"&lon="+address.optString("longitude"),null,null,"GET");
				JSONObject jsonObj = new JSONObject(walkScore);
				JSONObject transit = new JSONObject(jsonObj.optString("transit"));
				JSONObject bike = new JSONObject(jsonObj.optString("bike"));
				JSONObject score = new JSONObject();
				score.put("Walk", jsonObj.optString("walkscore"));
				score.put("Bike", bike.optString("score"));
				score.put("Transit", transit.optString("score"));
				result.put("score", score.toString());
				if(jsonObj.optInt("walkscore") > transit.optInt("score")) {
					if(jsonObj.optInt("walkscore") > bike.optInt("score")) {
						result.put("comments", jsonObj.optString("description"));
					}else {
						result.put("comments", bike.optString("description"));
					}
				} else if(transit.optInt("score") > bike.optInt("score")) {
					result.put("comments", transit.optString("description"));
				}else {
					result.put("comments", bike.optString("description"));
				}
				ArrayList<String> comments = new ArrayList<String>();
				comments.add(jsonObj.optString("description"));
				comments.add(bike.optString("description"));
				comments.add(jsonObj.optString("description"));
				result.put("comments", comments.toString());
				jsonArray.put(result);
			}
			
			request.getSession().setAttribute("details", jsonArray);
			response.getWriter().print(jsonArray.toString());


		} catch (JSONException e) {
			// TODO Auto-generated catch block
			response.getWriter().print(result);
			e.printStackTrace();
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
