package org.eclipse.smarthome.binding.yahooweather.discover;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.URI;
import org.eclipse.smarthome.binding.yahooweather.YahooWeatherBindingConstants;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


public class DiscoverHome extends AbstractDiscoveryService {

	private static final Logger logger = LoggerFactory.getLogger(DiscoverHome.class);

	public DiscoverHome() {
		super(YahooWeatherBindingConstants.SUPPORTED_THING_TYPES_UIDS,5,false);
	}

	public Set<ThingTypeUID> getSupportedThingTypes() {
		return YahooWeatherBindingConstants.SUPPORTED_THING_TYPES_UIDS;
	}


	private static String readAll(Reader rd) throws IOException {

		BufferedReader reader = new BufferedReader(rd);
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}

	private static JsonElement getAtPath(JsonElement e, String path) {
		JsonElement current = e;
		String ss[] = path.split("/");
		for (int i = 0; i < ss.length; i++) {
			current = current.getAsJsonObject().get(ss[i]);
		}
		return current;
	}

	/**
	 * Download Json Element from url
	 * @param url
	 * @return String with body
	 */
	private String downloadData(String url) {
		InputStream is = null;
		String jsonText  = null;
		try {
			is = new URL(url).openStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			jsonText = readAll(rd);
		} catch (Exception e) {
			logger.debug("Error while getting data: {}", e.getMessage());
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				//
			}
		}   

		return jsonText;
	}

	private String getWoeidData (String coordinate) {
		String query=  "SELECT * FROM geo.placefinder WHERE text='" + coordinate + "' and gflags='R'";
		String url =null;
		try{
			URI uri = new URI ("https://query.yahooapis.com/v1/public/yql",false);
			uri.setQuery ("q=" + query + "&format=json");
			url = uri.toString();
		} catch (Exception e) {
			logger.debug("Error while getting location ID: {}", e.getMessage());}
		return downloadData(url);
	}

	private String getGeoFromIpData() {
		String url = "http://freegeoip.net/json/";
		return downloadData(url);
	}
	private void AddLocation(){
		String locationName = null;
		String locationWoeid = null;	
		try{
			JsonElement locationData =  new JsonParser().parse(getGeoFromIpData());
			locationName = getAtPath(locationData, "city").getAsString()+ ", " + getAtPath(locationData, "country_name").getAsString();
			String locationCoords = getAtPath(locationData, "latitude").getAsString()+ ", " + getAtPath(locationData, "longitude").getAsString();
			logger.debug ("Location from IP {} coords {}", locationName,locationCoords);

			JsonElement woeidData = new JsonParser().parse(getWoeidData (locationCoords));
			locationName = getAtPath(woeidData, "query/results/Result/city").getAsString() + ", " + getAtPath(woeidData, "query/results/Result/country").getAsString();
			locationWoeid = getAtPath(woeidData, "query/results/Result/woeid").getAsString();
			logger.debug ("Location from locationID {} : {}", locationWoeid,locationName);
		} catch (Exception e) {
			logger.debug("Error while getting location ID: {}", e.getMessage());}

		if (locationWoeid !=null){
			ThingUID uid = new ThingUID(YahooWeatherBindingConstants.THING_TYPE_WEATHER, locationWoeid );
			if(uid!=null) {
				Map<String, Object> properties = new HashMap<>(1);
				properties.put("location" ,locationWoeid);
				DiscoveryResult result = DiscoveryResultBuilder.create(uid)
						.withProperties(properties)
						.withLabel("Yahoo weather " + locationName)
						.build();
				thingDiscovered(result);
			}}}

	@Override
	protected void activate() {
		super.activate();
		AddLocation();
	}

	@Override
	protected void startScan() {
		AddLocation();
	}



}    

