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
package edu.unifi.disit.executerchecker_surveys;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.unifi.disit.commons.datamodel.MobilityUserLocation;
import edu.unifi.disit.commons.utils.NetClientGet;
import edu.unifi.disit.commons.utils.Utils;
import edu.unifi.disit.engager_utils.SampleDataSource;
import edu.unifi.disit.engagerapi.GetPropertyValues;
import edu.unifi.disit.engagerapi.datamodel.Response;

public class DBinterfaceUserProfiler {

	private static final GetPropertyValues properties = GetPropertyValues.getInstance();

	NetClientGet ncg = new NetClientGet();

	private static final Logger logger = LogManager.getLogger("DBinterface-userprofiler");
	private SessionFactory sessionFactoryUserprofiler;
	private static DBinterfaceUserProfiler instance = null;

	private static DBinterfaceSensors db_sensors = DBinterfaceSensors.getInstance();

	public static DBinterfaceUserProfiler getInstance() {
		if (instance == null) {
			synchronized (DBinterfaceUserProfiler.class) {
				if (instance == null) {
					instance = new DBinterfaceUserProfiler();
				}
			}
		}
		return instance;
	}

	private DBinterfaceUserProfiler() {
		this.sessionFactoryUserprofiler = new Configuration().configure("hibernate-userprofiler.cfg.xml").buildSessionFactory();

	}

	public void close() throws Throwable {
		if (instance != null) {
			sessionFactoryUserprofiler.close();
			sessionFactoryUserprofiler = null;
			instance = null;
			super.finalize();
		}
	}

	@SuppressWarnings("unchecked")
	public Date retrievePT(Response r, int howmanytime) {

		Date toreturn = null;

		Session session = sessionFactoryUserprofiler.openSession();
		Transaction tx = null;

		try {

			String from_s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(r.getTimeSend());
			String to_s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(r.getTimeElapsed());

			logger.debug("timelines from {}", from_s);
			logger.debug("timleines to {}", to_s);

			String sqlquery = "SELECT timeline.date, timeline.seconds FROM userprofiler.timeline where device_device_id='" + r.getUserId() + "' "
					+ "and status='" + SampleDataSource.EVENTID_PUBLIC_TRANSPORT + "' "
					+ "and date>='" + from_s + "'"
					+ "and date<='" + to_s + "'"
					+ "and seconds>='360';";// TODO set from properties

			tx = session.beginTransaction();
			SQLQuery query = session.createSQLQuery(sqlquery);
			List<Object[]> returned = query.list();
			tx.commit();

			if ((returned != null)) {
				for (Object[] o : returned) {
					logger.debug("date is {}", o[0]);
					logger.debug("duration is {}", o[1]);
				}

				toreturn = checkBusThisTimeline(returned, r, howmanytime);
			} else {
				logger.debug("no date found");
			}

		} catch (Exception ex) {
			logger.error("cannot retrieve due:", ex);
			if (tx != null)
				tx.rollback();

		} finally {
			try {
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
			}
		}
		return toreturn;
	}

	private Date checkBusThisTimeline(List<Object[]> returned, Response r, int howmanytimetarget) {

		Date toreturn = null;
		int howmanytime = 0;

		// extract bus line from r
		List<String> busLinesSuggested = extractBusLinesSuggested(r.getAction_msg());

		for (Object[] timeline : returned) {

			logger.debug("checkin this timeline: {} {}", timeline[0], timeline[1]);

			// retrieve the gps on this timeline
			Date start = (Date) timeline[0];
			Date end = new Date(((Date) timeline[0]).getTime() + ((java.math.BigInteger) timeline[1]).longValue() * 1000l);

			List<MobilityUserLocation> locations = db_sensors.retrieveMobilityLocations(r.getUserId(), start, end);

			// check if bus around of this location are == any inside r.action_msg
			for (MobilityUserLocation location : locations) {

				logger.debug("lat {} long {} date {}", location.getLatitude(), location.getLongitude(), location.getData());

				List<String> busLineAround = extractBusLinesAround(location.getLatitude(), location.getLongitude());

				if (contains(busLineAround, busLinesSuggested)) {

					toreturn = location.getData();
					howmanytime = howmanytime + 1;
					logger.debug("gotcha one time. total is {}", howmanytime);
					break;
				}
			}

		}

		if (howmanytime >= howmanytimetarget)
			return toreturn;
		else
			return null;
	}

	private boolean contains(List<String> busLineAround, List<String> busLinesSuggested) {
		for (String around : busLineAround)
			for (String suggested : busLinesSuggested)
				if (around.equalsIgnoreCase(suggested))
					return true;
		return false;
	}

	private List<String> extractBusLinesSuggested(String action_msg) {

		logger.debug("extracting from:{}", action_msg);

		List<String> toreturn = new ArrayList<String>();

		try {

			int index_start = 0;
			int index_found = 0;
			while ((index_found = action_msg.indexOf("(", index_start)) != -1) {

				int terminal = action_msg.indexOf(")", index_found);

				String provider = action_msg.substring(index_found + 1, terminal);

				logger.debug("provider was:{}", provider);

				String linea = action_msg.substring(terminal + 2, action_msg.indexOf(":", terminal) - 1);

				logger.debug("linea was:{}", linea);

				if (!toreturn.contains(provider + linea))
					toreturn.add(provider + linea);

				index_start = index_found + 1;

			}
		} catch (Exception e) {
			logger.warn("error tracking action_msg {}", e.getMessage());
			// logger.warn(e);
		}

		return toreturn;
	}

	private List<String> extractBusLinesAround(Double gps_lat, Double gps_long) {

		List<String> toreturn = new ArrayList<String>();

		try {
			String response = ncg.get(new URL(properties.getServicemapURL() + "location/?position=" + Utils.approx(gps_lat) + ";" + Utils.approx(gps_long) + "&intersectGeom=true&maxDists=0.001"), properties.getReadTimeoutMillisecond());
			toreturn = parsaAround(response);

		} catch (MalformedURLException e) {
			logger.error("MalformedURLException on retrieve bus around:", e);
		} catch (JsonProcessingException e) {
			logger.error("JSONException on retrieve bus around:", e);
		} catch (IOException e) {
			logger.error("IOEException on on retrieve bus around:", e);
		}

		return toreturn;
	}

	private List<String> parsaAround(String around) throws JsonProcessingException, IOException {

		List<String> toreturn = new ArrayList<String>();

		// create ObjectMapper instance
		ObjectMapper objectMapper = new ObjectMapper();

		// read JSON like DOM Parser
		JsonNode rootNode = objectMapper.readTree(around.getBytes());
		JsonNode intNode = rootNode.path("intersect");

		Iterator<JsonNode> elements = intNode.elements();
		while (elements.hasNext()) {
			JsonNode intersect = elements.next();

			if (intersect.get("routeType") != null) {
				if (intersect.get("routeType").textValue().equals("Bus")) {
					String toadd = intersect.get("agency").textValue() + intersect.get("name").textValue();
					logger.debug("adding bus with:{}", toadd);
					if (!toreturn.contains(toadd))
						toreturn.add(toadd);
				} else if (intersect.get("routeType").textValue().equals("Train")) {
					String toadd = intersect.get("agency").textValue() + intersect.get("name").textValue();
					logger.debug("adding bus with:{}", toadd);
					if (!toreturn.contains(toadd))
						toreturn.add(toadd);
				} else if (intersect.get("routeType").textValue().equals("Tram")) {
					String toadd = intersect.get("agency").textValue() + intersect.get("name").textValue();
					logger.debug("adding bus with:{}", toadd);
					if (!toreturn.contains(toadd))
						toreturn.add(toadd);
				}
			}
		}
		return toreturn;
	}
}
