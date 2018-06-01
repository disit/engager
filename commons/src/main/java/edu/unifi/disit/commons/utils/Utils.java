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
package edu.unifi.disit.commons.utils;

import java.io.IOException;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.unifi.disit.commons.datamodel.SMLocation;
import edu.unifi.disit.commons.datamodel.SOType;

public final class Utils {

	private static final Logger logger = LogManager.getLogger("Utils");

	private static final NetClientGet ncg = new NetClientGet();

	public static final void setLoggerLevel(org.apache.logging.log4j.Level level) {
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();
		LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
		loggerConfig.setLevel(level);
		ctx.updateLoggers();
	}

	static final DecimalFormat df = initDF();// to speed up i put here

	public static final String approx(Double number) {
		return df.format(number);
	}

	private static final DecimalFormat initDF() {
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat("#.####"); // approximate to 10 meter
		df.setRoundingMode(RoundingMode.HALF_DOWN);
		df.setDecimalFormatSymbols(dfs);
		return df;
	}

	public static final SMLocation retrieveLocation(Double latitude, Double longitude, String cpzUrl, int timeout) {
		SMLocation toreturn = new SMLocation();

		try {
			// retrieve current location info: cpz, adress, ...
			// http://192.168.0.206:8080/WebAppGrafo/api/v1/location/?position=43.7979;11.2538&intersectGeom=true
			String response = ncg.get(new URL(cpzUrl + "location/?position=" + Utils.approx(latitude) + ";" + Utils.approx(longitude) + "&intersectGeom=true"), timeout);
			toreturn = parsaLOCATION(response);
		} catch (MalformedURLException e) {
			logger.warn("MalformedURLException on retrieveCPZ:", e);
		} catch (JsonProcessingException e) {
			logger.warn("JsonProcessingException on retrieveCPZ:", e);
		} catch (IOException e) {
			logger.warn("IOException on retrieveCPZ:", e);
		} catch (NoSuchElementException e) {
			logger.warn("NoSuchElementException on retrieveCPZ:", e);
		}
		return toreturn;
	}

	// here we return just cpz+address, but we can retrieve all the information regarding this location, i.e. municipality, number, ...
	// 0 - cpz
	// 1 - address
	// 2 - municipality
	// 3 - number
	// 4 - provincia
	private static final SMLocation parsaLOCATION(String response) throws JsonProcessingException, IOException {

		SMLocation toreturn = new SMLocation();

		if ((response != null) && (response.length() != 0)) {

			// create ObjectMapper instance
			ObjectMapper objectMapper = new ObjectMapper();

			// read JSON like DOM Parser
			JsonNode rootNode = objectMapper.readTree(response.getBytes());

			JsonNode intNode = rootNode.path("intersect");

			Iterator<JsonNode> elements = intNode.elements();

			double min_dist = Double.MAX_VALUE;

			while (elements.hasNext()) {
				JsonNode intersect = elements.next();

				if ((intersect.get("class") != null) && (intersect.get("class").textValue().equals("http://www.disit.org/km4city/schema#Controlled_parking_zone"))) {

					double current_distance = 0;

					JsonNode d = intersect.get("distance");

					if (d != null) {
						current_distance = d.doubleValue();
					} else {
						logger.debug("distance not avalable");
					}

					if (current_distance < min_dist) {
						toreturn.setCpz((intersect.get("name").textValue()));
						logger.debug("setting new CPZ:{} since distance is:{}", intersect.get("name").textValue(), current_distance);
						min_dist = current_distance;
					} else
						logger.debug("distance is higher:{} respect:{}", current_distance, min_dist);
				}

			}

			JsonNode addNode = rootNode.path("address");
			toreturn.setAddress(addNode.asText(null));

			JsonNode municipalityNode = rootNode.path("municipality");
			toreturn.setMunicipality(municipalityNode.asText(null));

			JsonNode numberNode = rootNode.path("number");
			toreturn.setNumber(numberNode.asText(null));

			JsonNode provinceNode = rootNode.path("province");
			toreturn.setProvince(provinceNode.asText(null));

		} else
			logger.debug("Null value to PARSE or EMPTY to parsaLOCATION, return empty");
		return toreturn;
	}

	public static String cluster(int hours, int minutes, int cluster_size) {
		// cluster the input basing on the cluster size.
		// if cluster is 5, output has to be 0, 5, 10, .... etc
		Integer div = minutes / cluster_size;

		return String.format("%1$02d:%2$02d", hours, div * cluster_size);

	}

	public static SOType extractSOType(String string) {

		if (string.length() == 0) {
			logger.debug("empty string");
			return SOType.OTHER;
		}

		if (string.charAt(string.length() - 1) == 'a')
			return SOType.ANDROID;
		else if (string.charAt(string.length() - 1) == 'w')
			return SOType.WINDOWS;
		else if (string.charAt(string.length() - 1) == 'i')
			return SOType.IOS;
		else if (string.charAt(string.length() - 1) == 'b')
			return SOType.BROWSER;
		else
			return SOType.OTHER;
	}

	public static final Integer retrieveDaySlotInteger(Long milliseconds) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(milliseconds);
		return retrieveDaySlotInteger(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
	}

	public static final Integer retrieveDaySlotInteger(Integer hours, Integer minutes) {
		if (hours < 5)
			return 0;
		else if ((hours >= 5) && (hours < 8))
			return 1;
		else if ((hours >= 8) && (hours < 10))
			return 1;
		else if (((hours >= 10) && (hours < 12)) ||
				((minutes < 30) && (hours == 12)))
			return 2;
		else if (((hours >= 13) && (hours < 14)) ||
				((minutes >= 30) && (hours == 12)))
			return 2;
		else if (((hours >= 14) && (hours < 18)) ||
				((minutes < 30) && (hours == 18)))
			return 3;
		else if (((hours >= 19) && (hours < 21)) ||
				((minutes >= 30) && (hours == 18)))
			return 3;
		else if (hours >= 21)
			return 4;
		else
			return -1;
	}

	public static String convertSecondsToString(Long seconds) {
		NumberFormat nf = DecimalFormat.getInstance();
		nf.setMaximumFractionDigits(0);
		String toreturn = "";

		Double h = Math.floor(seconds / 3600);

		if (h != 0)
			toreturn = toreturn.concat(nf.format(h) + " hour ");

		Double m = Math.floor((seconds % 3600) / 60);

		if (m != 0)
			toreturn = toreturn.concat(nf.format(m) + " min ");

		Long s = (seconds % 3600) % 60;

		if (s != 0)
			toreturn = toreturn.concat(s + " sec ");

		return toreturn;
	}

	public static String convertMetersToString(Long meters) {
		NumberFormat nf = DecimalFormat.getInstance();
		nf.setMaximumFractionDigits(0);
		String toreturn = "";

		Double km = Math.floor(meters / 1000);

		if (km != 0)
			toreturn = toreturn.concat(nf.format(km) + " km ");

		Integer m = (int) (meters % 1000);

		if (m != 0)
			toreturn = toreturn.concat(m + " m ");

		return toreturn;
	}
}
