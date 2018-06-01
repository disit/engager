/*******************************************************************************
 * Engager
 *    Copyright (C) 2016-2018 DISIT Lab http://www.disit.org - University of Florence
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Affero General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package edu.unifi.disit.engagerapi.datamodel;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import disit.engager_siimobility.ENVIROMENT;
import disit.engager_siimobility.EVENT;
import disit.engager_siimobility.LOCATION;
import disit.engager_siimobility.POI;
import disit.engager_siimobility.PPOI;
import disit.engager_siimobility.TIME;
import disit.engager_siimobility.TIMELINE;
import disit.engager_siimobility.TRANSPORT;
import disit.engager_siimobility.USER;
import edu.unifi.disit.commons.datamodel.MobilityUserLocation;
import edu.unifi.disit.commons.datamodel.Position;
import edu.unifi.disit.commons.datamodel.SMLocation;
import edu.unifi.disit.commons.datamodel.UserLocation;
import edu.unifi.disit.commons.datamodel.userprofiler.Device;
import edu.unifi.disit.commons.datamodel.userprofiler.Timeline;
import edu.unifi.disit.commons.utils.NetClientGet;
import edu.unifi.disit.commons.utils.Utils;
import edu.unifi.disit.engager_utils.SampleDataSource;
import edu.unifi.disit.engagerapi.GetPropertyValues;
import edu.unifi.disit.engagerapi.exception.ContextNotValidException;

public class CONTEXT {

	private static final GetPropertyValues properties = GetPropertyValues.getInstance();
	private static final Logger logger = LogManager.getLogger();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	NetClientGet ncg = new NetClientGet();

	USER user = null;
	LOCATION location = null;
	ENVIROMENT env = null;
	TIME time = null;

	public USER getUser() {
		return user;
	}

	public void setUser(USER user) {
		this.user = user;
	}

	public LOCATION getLocation() {
		return location;
	}

	public void setLocation(LOCATION location) {
		this.location = location;
	}

	public ENVIROMENT getEnv() {
		return env;
	}

	public void setEnv(ENVIROMENT env) {
		this.env = env;
	}

	public TIME getTime() {
		return time;
	}

	public void setTime(TIME time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return "CONTEXT [user=" + user + ", location=" + location + ", env=" + env + ", time=" + time + "]";
	}

	public void retrieveDynamicUSER(MobilityUserLocation userContext) {
		if (this.user == null)// TODO manage rpopertly
			this.user = new USER(); // there is always a USER to return

		try {

			String params_request = "when=" + userContext.getData().getTime() + "&profile=" + userContext.getProfile() + "&terminal_lang=" + userContext.getDeviceLanguage();

			if (userContext.getLatitude() != 0)
				params_request = params_request.concat("&latitude=" + userContext.getLatitude());
			if (userContext.getLongitude() != 0)
				params_request = params_request.concat("&longitude=" + userContext.getLongitude());
			if (userContext.getStatus() != null)
				params_request = params_request.concat("&mobility_mode=" + userContext.getStatus());
			if (userContext.getSpeed() != null)
				params_request = params_request.concat("&speed=" + userContext.getSpeed());
			if (userContext.getAccuracy() != null)
				params_request = params_request.concat("&accuracy=" + userContext.getAccuracy());

			// mobility
			if (userContext.getProvider() != null)
				params_request = params_request.concat("&provider=" + userContext.getProvider());
			if (userContext.getMean_speed() != null)
				params_request = params_request.concat("&meanspeed=" + userContext.getMean_speed());
			if (userContext.getAcc_magni() != null)
				params_request = params_request.concat("&accmagn=" + userContext.getAcc_magni());
			if (userContext.getAcc_x() != null)
				params_request = params_request.concat("&accx=" + userContext.getAcc_x());
			if (userContext.getAcc_y() != null)
				params_request = params_request.concat("&accy=" + userContext.getAcc_y());
			if (userContext.getAcc_z() != null)
				params_request = params_request.concat("&accz=" + userContext.getAcc_z());

			// retrieve user info
			String response = ncg.post(new URL(properties.getUserProfilerURL() + "device/" + userContext.getUserName() + "/location"), properties.getReadTimeoutMillisecond(), Level.ERROR, params_request);

			this.user = parsaUSER(response, userContext);

		} catch (MalformedURLException e) {
			logger.error("MalformedURLException on retrievePPOIs:", e);
		} catch (JsonProcessingException e) {
			logger.error("JsonProcessingException on retrievePPOIs:", e);
		} catch (IOException e) {
			logger.error("IOException on retrievePPOIs:", e);
		}
	}

	public void retrieveTRANSPORT(UserLocation userContext) throws NoSuchElementException, JsonParseException, JsonMappingException, IOException {

		// retrieve previous transport----------------------------------------------------------------------------------------------
		// retrieve timelines from last stay (in 12 hours)
		List<Timeline> timelines_from_last_stay = getLast12hoursTimelinesFromStay(userContext.getData().getTime(), userContext.getUserName());

		List<TIMELINE> last12hoursTimelines = convertTimelines(timelines_from_last_stay);

		// add this previous timeline to user previous timeline
		this.user.setPreviousTimelines(last12hoursTimelines);

		if ((timelines_from_last_stay != null) && (timelines_from_last_stay.size() > 0) && (this.user.getMobilityMode() != null) && (this.user.getMobilityMode().equals(SampleDataSource.EVENTID_STAY))) {// TODO beware, we retreive this info
																																																			// just if we're in stay
			if (this.env == null)
				this.env = new ENVIROMENT();// there is always an ENVIROMENT to return

			// set previous transport
			this.env.setPreviousTransports(retrieveTransports(timelines_from_last_stay.get(0).getLatitude(), timelines_from_last_stay.get(0).getLongitude(), userContext.getLatitude(), userContext.getLongitude(),
					timelines_from_last_stay.get(0).getDate()));

			if (this.user == null)
				this.user = new USER();// there is always an ENVIROMENT to return

			// set previous stay
			// TODO -> move this routine in the user profile and popolate ALWAYS
			LOCATION lastStay = convertTimeline(timelines_from_last_stay.get(0));
			if (lastStay != null)
				this.user.setPreviousStay(lastStay);
		}
	}

	private List<Timeline> getLast12hoursTimelinesFromStay(Long time, String deviceId) throws NoSuchElementException, JsonParseException, JsonMappingException, IOException {
		Long from = time - (12 * 3600 * 1000);
		String response_timelines = ncg.get(new URL(properties.getUserProfilerURL() + "device/" + deviceId + "/timeline?from=" + from + "&last_status=" + SampleDataSource.EVENTID_STAY + "&min_time=8"),
				properties.getReadTimeoutMillisecond());
		return parseTimelines(response_timelines);
	}

	private LOCATION convertTimeline(Timeline timeline) {
		LOCATION l = null;
		if (timeline != null) {
			SMLocation info = Utils.retrieveLocation(timeline.getLatitude(), timeline.getLongitude(), properties.getServicemapURL(), properties.getReadTimeoutMillisecond());
			l = new LOCATION();
			l.setAddress(info.getAddress());
			l.setCpz(info.getCpz());
			l.setMunicipality(info.getMunicipality());
			l.setNumber(info.getNumber());
			l.setProvince(info.getProvince());
			l.setGpsLatitude(timeline.getLatitude());
			l.setGpsLongitude(timeline.getLongitude());
		}
		return l;
	}

	private List<TIMELINE> convertTimelines(List<Timeline> timelines_from_last_stay) {

		List<TIMELINE> toreturn = new ArrayList<TIMELINE>();

		if (timelines_from_last_stay != null)
			for (Timeline timeline : timelines_from_last_stay) {
				TIMELINE t = new TIMELINE();
				t.setStatus(timeline.getStatus());
				t.setMeters(timeline.getMeters());
				t.setSeconds(new Long(timeline.getSeconds()));
				t.setStartDate(timeline.getDate());
				toreturn.add(t);
			}

		return toreturn;

	}

	public void retrieveStaticUSER(MobilityUserLocation userContext) throws ContextNotValidException {

		if (this.user == null)
			this.user = new USER();

		this.user.setTerminalAccuracy(userContext.getAccuracy());
		this.user.setProfile(userContext.getProfile());
		List<String> languages = new ArrayList<String>();
		if (userContext.getDeviceLanguage() != null)
			languages.add(userContext.getDeviceLanguage());
		this.user.setLanguages(languages);
		this.user.setIsAssessor(false);

	}

	public void retrieveTIME(long when) {
		this.time = new TIME(); // there is always a TIME to return

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(when);

		// popolate time characteristics
		this.time.setMilliseconds(calendar.getTimeInMillis());

		this.time.setYear(calendar.get(Calendar.YEAR));// 2016
		this.time.setMonth(calendar.get(Calendar.MONTH) + 1);// 1->12
		this.time.setDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));// 1->31

		this.time.setDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK));// 1(sunday) -> 7(saturday)
		this.time.setWeekOfMonth(((calendar.get(Calendar.DAY_OF_MONTH) - 1) / 7) + 1);// 1->5

		this.time.setHours(calendar.get(Calendar.HOUR_OF_DAY));// 0->23
		this.time.setMinutes(calendar.get(Calendar.MINUTE));// 0->59

		this.time.setTimeClustered(Utils.cluster(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 5));

		this.time.setDaySlot(SampleDataSource.retrieveDaySlot(this.time.getHours(), this.time.getMinutes()));
	}

	public void retrieveLOCATION(Double latitude, Double longitude, String deviceLanguage) {
		this.location = new LOCATION();// there is always a LOCATION to return

		try {
			// retrieve current location info: cpz, adress, ...
			// http://192.168.0.206:8080/WebAppGrafo/api/v1/location/?position=43.7979;11.2538&intersectGeom=true
			// String response = ncg.get(new URL(properties.getServicemapURL() + "location/?position=" + Utils.approx(latitude) + ";" + Utils.approx(longitude) + "&intersectGeom=true"), properties.getReadTimeoutMillisecond(), Level.WARN);

			SMLocation locations = Utils.retrieveLocation(latitude, longitude, properties.getServicemapURL(), properties.getReadTimeoutMillisecond());

			this.location = parsaLOCATION(locations, latitude, longitude);

		} catch (MalformedURLException e) {
			logger.error("MalformedURLException on retrieveCPZ:", e);
		} catch (JsonProcessingException e) {
			logger.error("JsonProcessingException on retrieveCPZ:", e);
		} catch (IOException e) {
			logger.error("IOException on retrieveCPZ:", e);
		} catch (NoSuchElementException e) {
			logger.warn("NoSuchElementException on retrieveCPZ:", e);
		}
	}

	public void retrieveENVIROMENT(Double latitude, Double longitude, String deviceLanguage) {
		if (this.env == null)
			this.env = new ENVIROMENT();// there is always an ENVIROMENT to return
		List<EVENT> events = new ArrayList<EVENT>();// there is always a list EVENT to return (event empty)
		List<POI> pois = new ArrayList<POI>();// there is always a list POI to return (event empty)

		try {
			// retrieve events
			// http://192.168.0.206:8080/WebAppGrafo/api/v1/events/?day&timestamp=20160921
			String response = ncg.get(new URL(properties.getServicemapURL() + "events/?day&timestamp=" + sdf.format(new Date())), properties.getReadTimeoutMillisecond());
			events = parsaEvent(response);
			// retrieve POI close to current location
			response = ncg.get(new URL(properties.getServicemapURL() + "?selection=" + Utils.approx(latitude) + "%3B" + Utils.approx(longitude) + "&categories=" + properties.getCategoriesServiceMap() + "&maxResults=" +
					properties.getServiceMapMaxResult() + "&maxDists=" + properties.getServiceMapRange() + "&lang=" + deviceLanguage + "&format=json"), properties.getReadTimeoutMillisecond(), Level.WARN);
			pois = parsaPOI(response, this.env, latitude, longitude);

			// popolate location characteristics
			this.env.setClosePois(pois);
			// popolate enviroment characteristics
			this.env.setEvents(events);

		} catch (MalformedURLException ex) {
			logger.error("MalformedURLException on retrieveEvent:", ex);
		} catch (JsonProcessingException ex) {
			logger.error("JsonProcessingException on retrieveEvent:", ex);
		} catch (IOException ex) {
			logger.error("IOException on retrieveEvent:", ex);
		} catch (NoSuchElementException e) {
			logger.error("NoSuchElementException on retrieveEvent:", e);
		}
	}

	private List<TRANSPORT> retrieveTransports(Double lat1, Double lon1, Double lat2, Double lon2, Date time) {

		List<TRANSPORT> transports = new ArrayList<TRANSPORT>();

		if (!Position.isClose(lat1, lon1, lat2, lon2, properties.getClosePublicTransportDistance()) &&
				(lat1 > 42.363) && (lat1 < 44.530) && (lat2 > 42.363) && (lat2 < 44.530) &&
				(lon1 > 9.684) && (lon1 < 12.397) && (lon2 > 9.684) && (lon2 < 12.397)) {

			String time_s = new SimpleDateFormat("yyyy-MM-dd").format(time) + "T" + new SimpleDateFormat("HH:mm:ss").format(time);

			try {

				// retrieve bus routing
				// http://servicemap.disit.org/WebAppGrafo/api/v1/shortestpath
				String response = ncg
						.get(new URL(properties.getServicemapURL() + "shortestpath/?source=" + Utils.approx(lat1) + ";" + Utils.approx(lon1) + "&destination=" + Utils.approx(lat2) + ";" + Utils.approx(lon2) + "&routeType=public_transport"
								+ "&maxFeetKM=1" + "&startDateTime=" + time_s), properties.getReadTimeoutMillisecond(), Level.WARN);

				transports = parsaTRANSPORTS(response);

			} catch (MalformedURLException e) {
				logger.error("MalformedURLException on retrieve_prev_trans:", e);
			} catch (JsonProcessingException e) {
				logger.error("JsonProcessingException on retrieve_prev_trans:", e);
			} catch (IOException e) {
				logger.error("IOException on retrieve_prev_trans:", e);
			} catch (NoSuchElementException e) {
				logger.warn("NoSuchElementException on retrieve_prev_trans:", e);
			}

		}

		return transports;

	}

	// ---------------------------------------------------------------------------------------------------------------------------------------------------parsa
	private List<PPOI> parsaPPOIs(List<edu.unifi.disit.commons.datamodel.PPOI> ppois) throws JsonParseException, JsonMappingException, IOException {
		List<PPOI> toreturn = new ArrayList<PPOI>();

		for (edu.unifi.disit.commons.datamodel.PPOI p : ppois) {
			PPOI ppoi = new PPOI();
			LOCATION l = new LOCATION();
			ppoi.setName(p.getName());
			ppoi.setAccuracy(p.getAccuracy());
			ppoi.setConfirmation(p.getConfirmation());
			l.setGpsLatitude(p.getLatitude());
			l.setGpsLongitude(p.getLongitude());
			l.setCpz(p.getCpz());
			l.setAddress(p.getAddress());
			l.setMunicipality(p.getMunicipality());
			l.setNumber(p.getNumber());
			ppoi.setLocation(l);
			toreturn.add(ppoi);
		}

		return toreturn;
	}

	public List<TRANSPORT> parsaTRANSPORTS(String response) throws JsonProcessingException, IOException {
		List<TRANSPORT> toreturn = new ArrayList<TRANSPORT>();

		if ((response != null) && (response.length() != 0)) {
			// create ObjectMapper instance
			ObjectMapper objectMapper = new ObjectMapper();

			// read JSON like DOM Parser
			JsonNode rootNode = objectMapper.readTree(response.getBytes());
			JsonNode journeyNode = rootNode.path("journey");
			JsonNode routesNode = journeyNode.path("routes");

			Iterator<JsonNode> routes = routesNode.elements();
			while (routes.hasNext()) {
				JsonNode route = routes.next();

				JsonNode arcsNode = route.path("arc");
				Iterator<JsonNode> arcs = arcsNode.elements();

				String descriptions = "";

				while (arcs.hasNext()) {
					JsonNode arc = arcs.next();

					// take just public_transport
					JsonNode typeN = arc.path("transport_service_type");
					String type = typeN.asText();
					if (type.equals("public transport")) {
						// TODO manage separatly the transports
						// TRANSPORT t = new TRANSPORT();
						// t.setTransport_service_type(type);
						// t.setStart_datetime(arc.path("start_datetime").asText());
						// t.setTransport_provider_name(arc.path("transport_provider_name").asText());
						// t.setDesc(arc.path("desc").asText());
						// toreturn.add(t);
						descriptions = descriptions + "(" + arc.path("transport_provider_name").asText() + ") " + arc.path("desc").asText() + " -> ";
					}

				}

				// TODO manage separatly the transports
				if (descriptions.length() != 0) {
					TRANSPORT t = new TRANSPORT();
					t.setDesc(descriptions.substring(0, descriptions.length() - 4));
					toreturn.add(t);
				}

				// take just the first ROUTE
				break;
			}

		}

		return toreturn;
	}

	public USER parsaUSER(String response, MobilityUserLocation userContext) throws JsonParseException, JsonMappingException, IOException {
		USER u = new USER();
		ObjectMapper mapper = new ObjectMapper();
		String previous_ppoi = null;
		String current_ppoi = null;
		Long howlong = 0L;
		Double speedAverage = 0d;
		double speed = 0;
		Device d = null;

		if ((response != null) && (response.length() != 0)) {

			if ((response != null) && (response.length() != 0)) {
				d = mapper.readValue(response, new TypeReference<Device>() {
				});
				logger.debug("device parsed is:{}", d);

			} else {
				logger.debug("Null value to PARSE to device, return null");
				return u;
			}

			u.setIsAssessor(d.getIsAssessor());

			u.setTerminalAccuracy(d.getCurrentPositionAccuracy());

			if (d.getTimelineCurrent() != null) {
				u.setMobilityMode(d.getTimelineCurrent().getStatus());
				howlong = d.getTimelineCurrent().getSeconds();
			}

			if (d.getTimelinePrevious() != null) {
				TIMELINE t = new TIMELINE();
				t.setMeters(d.getTimelinePrevious().getMeters());
				t.setSeconds(d.getTimelinePrevious().getSeconds());
				t.setStartDate(d.getTimelinePrevious().getDate());
				t.setStatus(d.getTimelinePrevious().getStatus());
				u.setPreviousTimeline(t);
			}

			List<PPOI> ppois = new ArrayList<PPOI>();
			if (d.getPpois() != null)
				ppois = parsaPPOIs(d.getPpois());
			u.setPpois(ppois);

			previous_ppoi = d.getPpoiPrevious();
			current_ppoi = d.getPpoiCurrent();

			if ((d.getPpoiPreviousDistance() != null) && (d.getPpoiPreviousHowlong() != null) && (d.getPpoiPreviousHowlong() != 0)) {
				speed = (double) (d.getPpoiPreviousDistance() * 3600) / d.getPpoiPreviousHowlong();
				logger.debug("speed is:{}", speed);
			}

			speedAverage = d.getAverageSpeed();

			if (howlong != null)
				u.setHowLongMobilityMode(howlong.doubleValue());
			else
				u.setHowLongMobilityMode(0d);

			if (d.getTerminal_profile() != null)
				u.setProfile(d.getTerminal_profile());

			List<String> languages = new ArrayList<String>();
			if (d.getTerminal_language() != null)
				languages.add(d.getTerminal_language().toString());
			u.setLanguages(languages);

			PPOI prev = retrievePPOI(u.getPpois(), previous_ppoi);
			if (prev != null)
				u.setLastPpoi(prev);
			PPOI curr = retrievePPOI(u.getPpois(), current_ppoi);
			if (curr != null)
				u.setCurrentPpoi(curr);

			// if (d.getPpoiNext() != null) {

			// shall we consider a valid PREDICTION?
			// TODO manage meanDistance, meanDuration, meanModality
			if ((d.getPpoiNext() != null) &&
					(d.getPpoiNext().getAccuracy() > properties.getPreditionAccuracy()) &&
					(d.getPpoiNext().getHowmany() > properties.getPredictionHowmany())) {
				logger.debug("passed");
				u.setNextPpoi(retrievePPOI(u.getPpois(), d.getPpoiNext().getName()));// CAN BE NULL SINCE THE PPOI maybe not exist, but transaction is still not updated!!! (fire scenario)

				// if there is a NEXT and prediction !contains publicTransport, populate alternative publictransport NEXT
				if ((!contains(d.getPpoiNext().getModality(), SampleDataSource.EVENTID_PUBLIC_TRANSPORT)) && (u.getNextPpoi() != null) && (u.getLastPpoi() != null)) {
					List<Timeline> timelines_from_last_stay = getLast12hoursTimelinesFromStay(userContext.getData().getTime(), userContext.getUserName());// we use getlast12hour timeline just to specify the moment we left stay
					if ((timelines_from_last_stay != null) && (timelines_from_last_stay.size() > 0)) {

						if (this.env == null)
							this.env = new ENVIROMENT();// there is always an ENVIROMENT to return

						env.setNextTransports(retrieveTransports(u.getLastPpoi().getLocation().getGpsLatitude(), u.getLastPpoi().getLocation().getGpsLongitude(),
								u.getNextPpoi().getLocation().getGpsLatitude(), u.getNextPpoi().getLocation().getGpsLongitude(),
								timelines_from_last_stay.get(0).getDate()));
					}
				}

			} else
				logger.debug("NOT passed");

			// }

			// rule here about SwitchMobilityMode
			// if current!=null AND speed > 7 km/h
			if ((current_ppoi != null) && (!current_ppoi.equals("null")) && (speed > 7.0d))
				u.setSwitchMobilityMode(SampleDataSource.PARKING);
			// if speedAvarage > 5 Km/h
			else if ((speedAverage != null) && (speedAverage > properties.getSpeedAverageThreshold())) {
				u.setSwitchMobilityMode(SampleDataSource.IN_MOBILITY);
			} else
				u.setSwitchMobilityMode(SampleDataSource.NULL);
		}

		// populate extra charactreistics msg back
		if (d.getCurrentPositionAccuracy() != null) {
			u.setTerminalAccuracy(d.getCurrentPositionAccuracy());
		}

		Set<String> groups_s = d.getGroups();

		if ((groups_s != null) && (groups_s.size() > 0)) {
			List<String> groups = new ArrayList<String>();
			groups.addAll(groups_s);
			u.setGroups(groups);
		}

		return u;
	}

	private boolean contains(List<String> modality, String id) {
		if (modality != null)
			for (String s : modality) {
				if ((s != null) && (s.equalsIgnoreCase(id)))
					return true;
			}
		return false;
	}

	private PPOI retrievePPOI(List<PPOI> ppois, String search_ppoi) {
		for (PPOI ppoi : ppois) {
			if (ppoi.getName().equals(search_ppoi))
				return ppoi;
		}
		return null;
	}

	private List<POI> parsaPOI(String response, ENVIROMENT e, Double latitude, Double longitude) throws JsonProcessingException, IOException {
		List<POI> toreturn = new ArrayList<POI>();
		List<POI> toreturn2 = new ArrayList<POI>();

		if ((response != null) && (response.length() != 0)) {
			// create ObjectMapper instance
			ObjectMapper objectMapper = new ObjectMapper();

			// read JSON like DOM Parser
			JsonNode rootNode = objectMapper.readTree(response.getBytes());

			// ----------------------------------------------------------Services
			JsonNode serviceNode = rootNode.path("Services");
			JsonNode featNode = serviceNode.path("features");

			Iterator<JsonNode> elements = featNode.elements();
			while (elements.hasNext()) {
				JsonNode feature = elements.next();

				POI poi = new POI();
				LOCATION l = new LOCATION();

				JsonNode geometries = feature.path("geometry");
				Iterator<JsonNode> GPS_element = geometries.get("coordinates").elements();
				int gps_index = 0;
				while (GPS_element.hasNext()) {
					JsonNode GPS = GPS_element.next();
					if (gps_index == 0) {
						l.setGpsLongitude(GPS.doubleValue());
					} else {
						l.setGpsLatitude(GPS.doubleValue());
					}
					gps_index++;
				}

				JsonNode propertiesJ = feature.path("properties");

				poi.setName(propertiesJ.get("name").textValue());
				poi.setTipo(propertiesJ.get("tipo").textValue());
				poi.setTypeLabel(propertiesJ.get("typeLabel").textValue());
				poi.setServiceType(propertiesJ.get("serviceType").textValue());
				poi.setServiceUri(propertiesJ.get("serviceUri").textValue());

				poi.setLocation(l);

				toreturn.add(poi);

				// check if veryclose, in case add to closetPOI
				if (e.getClosestPoi() == null) {

					if ((propertiesJ.get("distance") != null) && (!propertiesJ.get("distance").isNull())) {
						if (propertiesJ.get("distance").floatValue() < properties.getCloseDistance())// specified distance
						{
							e.setClosestPoi(poi);
						}
					} else if (Position.isClose(latitude, longitude, l.getGpsLatitude(), l.getGpsLongitude(), properties.getCloseDistance()))// mydistance
						e.setClosestPoi(poi);
				}
			}

			// randomize, so we always take a new one//TOREMOVE
			Collections.shuffle(toreturn, new Random(System.nanoTime()));

			if (toreturn.size() > 1) {
				logger.debug("got at least 1 service");
				toreturn = toreturn.subList(0, 1);
			} else
				logger.debug("SERVICE Null value to PARSE or EMPTY to parsaPOI, return empty");

			// ----------------------------------------------------------BusStops
			JsonNode busStopsNode = rootNode.path("BusStops");
			JsonNode feat2Node = busStopsNode.path("features");

			Iterator<JsonNode> elements2 = feat2Node.elements();
			while (elements2.hasNext()) {
				JsonNode feature = elements2.next();

				POI poi = new POI();
				LOCATION l = new LOCATION();

				JsonNode geometries = feature.path("geometry");
				Iterator<JsonNode> GPS_element = geometries.get("coordinates").elements();
				int gps_index = 0;
				while (GPS_element.hasNext()) {
					JsonNode GPS = GPS_element.next();
					if (gps_index == 0) {
						l.setGpsLongitude(GPS.doubleValue());
					} else {
						l.setGpsLatitude(GPS.doubleValue());
					}
					gps_index++;
				}

				JsonNode propertiesJ = feature.path("properties");

				poi.setName(propertiesJ.get("typeLabel").textValue() + " " + propertiesJ.get("name").textValue());// this change only
				poi.setTipo(propertiesJ.get("tipo").textValue());
				poi.setTypeLabel(propertiesJ.get("typeLabel").textValue());
				poi.setServiceType(propertiesJ.get("serviceType").textValue());
				poi.setServiceUri(propertiesJ.get("serviceUri").textValue());

				poi.setLocation(l);

				toreturn2.add(poi);

				// don't add this poi to closest uri
			}

			// randomize, so we always take a new one//TOREMOVE
			Collections.shuffle(toreturn2, new Random(System.nanoTime()));

			if (toreturn2.size() > 1) {
				logger.debug("got at least 1 busstop");
				toreturn2 = toreturn2.subList(0, 1);
			} else
				logger.debug("BUSSTOP Null value to PARSE or EMPTY to parsaPOI, return empty");

		}

		toreturn.addAll(toreturn2);
		return toreturn;

	}

	// return just one, randomly
	private List<EVENT> parsaEvent(String response) throws JsonProcessingException, IOException {
		List<EVENT> toreturn = new ArrayList<EVENT>();

		if ((response != null) && (response.length() != 0)) {
			// create ObjectMapper instance
			ObjectMapper objectMapper = new ObjectMapper();

			// read JSON like DOM Parser
			JsonNode rootNode = objectMapper.readTree(response.getBytes());
			JsonNode serviceNode = rootNode.path("Event");
			JsonNode featNode = serviceNode.path("features");

			Iterator<JsonNode> elements = featNode.elements();
			while (elements.hasNext()) {
				JsonNode feature = elements.next();

				EVENT event = new EVENT();
				POI p = new POI();
				LOCATION l = new LOCATION();

				JsonNode geometries = feature.path("geometry");
				Iterator<JsonNode> GPS_element = geometries.get("coordinates").elements();
				int gps_index = 0;
				while (GPS_element.hasNext()) {
					JsonNode GPS = GPS_element.next();
					if (gps_index == 0) {
						l.setGpsLongitude(GPS.doubleValue());
					} else {
						l.setGpsLatitude(GPS.doubleValue());
					}
					gps_index++;
				}

				JsonNode properties = feature.path("properties");

				p.setName(properties.get("name").textValue());
				p.setServiceUri(properties.get("serviceUri").textValue());
				p.setTypeLabel(properties.get("categoryIT").textValue()); // beware, this is specific for Italian
				p.setServiceType(properties.get("serviceType").textValue());
				event.setPlace(properties.get("place").textValue());
				event.setEndData(properties.get("endDate").textValue());

				if (properties.get("price").isNull())
					event.setPrice(0f);
				else
					event.setPrice(properties.get("price").floatValue());

				p.setLocation(l);
				event.setPoi(p);

				toreturn.add(event);
			}

			// randomize, so we always take a new one
			Collections.shuffle(toreturn, new Random(System.nanoTime()));

			if (toreturn.size() > 1)
				toreturn = toreturn.subList(0, 1);
		} else
			logger.warn("Null value to PARSE or EMPTY to parsaEvent, return empty");

		return toreturn;
	}

	private List<Timeline> parseTimelines(String response) throws JsonParseException, JsonMappingException, IOException {

		ObjectMapper mapper = new ObjectMapper();
		List<Timeline> toreturn = null;

		if ((response != null) && (response.length() != 0)) {
			toreturn = mapper.readValue(response, new TypeReference<List<Timeline>>() {
			});
			logger.debug("timeline parsed is:{}", toreturn);
		}

		return toreturn;
	}

	private LOCATION parsaLOCATION(SMLocation o, Double latitude, Double longitude) throws JsonProcessingException, IOException {
		LOCATION toreturn = new LOCATION();
		// Object[] o = Utils.parsaLOCATION(response);
		toreturn.setCpz(o.getCpz());
		toreturn.setAddress(o.getAddress());
		toreturn.setMunicipality(o.getMunicipality());
		toreturn.setNumber(o.getNumber());
		toreturn.setProvince(o.getProvince());
		toreturn.setGpsLatitude(latitude);
		toreturn.setGpsLongitude(longitude);

		return toreturn;
	}
}
