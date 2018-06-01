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
package edu.unifi.disit.commons.datamodel;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.unifi.disit.commons.utils.DeviceSpecs;
import edu.unifi.disit.commons.utils.NetClientGet;
import edu.unifi.disit.commons.utils.Utils;

public class MobilityTrainingData {

	NetClientGet ncg = new NetClientGet();

	// null mean NA
	private static final Logger logger = LogManager.getLogger();

	Double accuracy = null;// USER_EVAL accuracy
	String modalityClass4 = null;// OUTPUT
	Integer provider = null;// USER_EVAL provider
	Double speed = null;// ---> USER_EVAL speed
	Double mean_speed = null;// ---> USER_EVAL meanspeed
	Integer bds = 0;// terminal info TODO
	Double avg_lin_acc_magn = null;// ---> USER_EVAL accel
	Double lin_acc_x = null;// ---> USER_EVAL accel
	Double lin_acc_y = null;// ---> USER_EVAL accel
	Double lin_acc_z = null;// ---> USER_EVAL accel
	Integer weekEnd = null;// from date
	Integer timeZone = null;// from date
	Integer phoneYear = null;// terminal info
	Integer profile = null;// ---> USER_EVAL profile
	Double prevSpeed = null;
	Double meanPrevSpeed = null;
	Double medianPrevSpeed = null;
	Double speedDistance = null;
	Integer sportsFacility = null;// oo servicemap
	Integer touristTrail = null;// oo servicemap
	Integer cyclePaths = null;// oo servicemap
	Integer tramBusLine = null;// oo servicemap
	Integer railLine = null;// oo servicemap
	Integer greenAreas = null;// oo servicemap

	// String deviceId, Double latitude, Double longitude, Long when, String mobility_mode, String terminal_lang

	public MobilityTrainingData(Double accuracy, String provider, Double speed, Double mean_speed, Double avg_lin_acc_magn, Double lin_acc_x, Double lin_acc_y, Double lin_acc_z, String profile) {
		super();
		this.accuracy = accuracy;
		this.provider = parseProvider(provider);
		if ((speed != null) && (speed != 0.0001) && (speed != 0.0002))
			this.speed = speed;
		if ((mean_speed != null) && (mean_speed != 0.0001) && (mean_speed != 0.0002))
			this.mean_speed = mean_speed;
		else
			this.mean_speed = speed;
		if ((avg_lin_acc_magn != null) && (avg_lin_acc_magn != 0.0001) && (avg_lin_acc_magn != 0.0002))
			this.avg_lin_acc_magn = avg_lin_acc_magn;
		if ((lin_acc_x != null) && (lin_acc_x != 0.0001) && (lin_acc_x != 0.0002))
			this.lin_acc_x = lin_acc_x;
		if ((lin_acc_y != null) && (lin_acc_y != 0.0001) && (lin_acc_y != 0.0002))
			this.lin_acc_y = lin_acc_y;
		if ((lin_acc_z != null) && (lin_acc_z != 0.0001) && (lin_acc_z != 0.0002))
			this.lin_acc_z = lin_acc_z;
		this.profile = parseProfile(profile);
	}

	public MobilityTrainingData(Double accuracy, String modalityClass4, String provider, Double speed, Double mean_speed, Integer bds, Double avg_lin_acc_magn, Double lin_acc_x, Double lin_acc_y, Double lin_acc_z, Integer weekEnd,
			Integer timeZone, Integer phoneYear, Integer profile, Double prevSpeed, Double meanPrevSpeed, Double medianPrevSpeed, Double speedDistance, Integer sportsFacility, Integer touristTrail, Integer cyclePaths, Integer tramBusLine,
			Integer railLine,
			Integer greenAreas) {
		super();
		this.accuracy = accuracy;
		this.modalityClass4 = modalityClass4;
		this.provider = parseProvider(provider);
		this.speed = speed;
		this.mean_speed = mean_speed;
		this.bds = bds;
		this.avg_lin_acc_magn = avg_lin_acc_magn;
		this.lin_acc_x = lin_acc_x;
		this.lin_acc_y = lin_acc_y;
		this.lin_acc_z = lin_acc_z;
		this.weekEnd = weekEnd;
		this.timeZone = timeZone;
		this.phoneYear = phoneYear;
		this.profile = profile;
		this.prevSpeed = prevSpeed;
		this.meanPrevSpeed = meanPrevSpeed;
		this.medianPrevSpeed = medianPrevSpeed;
		this.speedDistance = speedDistance;
		this.sportsFacility = sportsFacility;
		this.touristTrail = touristTrail;
		this.cyclePaths = cyclePaths;
		this.tramBusLine = tramBusLine;
		this.railLine = railLine;
		this.greenAreas = greenAreas;
	}

	public Double getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(Double accuracy) {
		this.accuracy = accuracy;
	}

	public String getModalityClass4() {
		return modalityClass4;
	}

	public void setModalityClass4(String modalityClass4) {
		this.modalityClass4 = modalityClass4;
	}

	public Integer getProvider() {
		return provider;
	}

	public void setProvider(Integer provider) {
		this.provider = provider;
	}

	public Double getSpeed() {
		return this.speed;
	}

	public void setSpeed(Double speed) {
		if ((speed != 0.0001) && (speed != 0.0002))
			this.speed = speed;
		else
			this.speed = null;
	}

	public Double getMean_speed() {
		return mean_speed;
	}

	public void setMean_speed(Double mean_speed) {
		if ((mean_speed != null) && (mean_speed != 0.0001) && (mean_speed != 0.0002))
			this.mean_speed = mean_speed;
		else
			this.mean_speed = null;
	}

	public Integer getBds() {
		return bds;
	}

	public void setBds(Integer bds) {
		this.bds = bds;
	}

	public Double getAvg_lin_acc_magn() {
		return this.avg_lin_acc_magn;
	}

	public void setAvg_lin_acc_magn(Double avg_lin_acc_magn) {
		if ((avg_lin_acc_magn != null) && (avg_lin_acc_magn != 0.0001) && (avg_lin_acc_magn != 0.0002))
			this.avg_lin_acc_magn = avg_lin_acc_magn;
		else
			this.avg_lin_acc_magn = null;
	}

	public Double getLin_acc_x() {
		return lin_acc_x;
	}

	public void setLin_acc_x(Double lin_acc_x) {
		if ((lin_acc_x != null) && (lin_acc_x != 0.0001) && (lin_acc_x != 0.0002))
			this.lin_acc_x = lin_acc_x;
		else
			this.lin_acc_x = null;
	}

	public Double getLin_acc_y() {
		return lin_acc_y;
	}

	public void setLin_acc_y(Double lin_acc_y) {
		if ((lin_acc_y != null) && (lin_acc_y != 0.0001) && (lin_acc_y != 0.0002))
			this.lin_acc_y = lin_acc_x;
		else
			this.lin_acc_y = null;
	}

	public Double getLin_acc_z() {
		return lin_acc_z;
	}

	public void setLin_acc_z(Double lin_acc_z) {
		if ((lin_acc_z != null) && (lin_acc_z != 0.0001) && (lin_acc_z != 0.0002))
			this.lin_acc_z = lin_acc_x;
		else
			this.lin_acc_z = null;
	}

	public Integer getWeekEnd() {
		return weekEnd;
	}

	public void setWeekEnd(Integer weekEnd) {
		this.weekEnd = weekEnd;
	}

	public Integer getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(Integer timeZone) {
		this.timeZone = timeZone;
	}

	public Integer getPhoneYear() {
		return phoneYear;
	}

	public void setPhoneYear(Integer phoneYear) {
		this.phoneYear = phoneYear;
	}

	public Integer getProfile() {
		return profile;
	}

	public void setProfile(Integer profile) {
		this.profile = profile;
	}

	public void setProfile(String profile) {
		this.profile = parseProfile(profile);
	}

	public Double getPrevSpeed() {
		return prevSpeed;
	}

	public void setPrevSpeed(Double prevSpeed) {
		this.prevSpeed = prevSpeed;
	}

	public Double getMeanPrevSpeed() {
		return meanPrevSpeed;
	}

	public void setMeanPrevSpeed(Double meanPrevSpeed) {
		this.meanPrevSpeed = meanPrevSpeed;
	}

	public Double getMedianPrevSpeed() {
		return medianPrevSpeed;
	}

	public void setMedianPrevSpeed(Double medianPrevSpeed) {
		this.medianPrevSpeed = medianPrevSpeed;
	}

	public Double getSpeedDistance() {
		return speedDistance;
	}

	public void setSpeedDistance(Double speedDistance) {
		this.speedDistance = speedDistance;
	}

	public Integer getSportsFacility() {
		return sportsFacility;
	}

	public void setSportsFacility(Integer sportsFacility) {
		this.sportsFacility = sportsFacility;
	}

	public Integer getTouristTrail() {
		return touristTrail;
	}

	public void setTouristTrail(Integer touristTrail) {
		this.touristTrail = touristTrail;
	}

	public Integer getCyclePaths() {
		return cyclePaths;
	}

	public void setCyclePaths(Integer cyclePaths) {
		this.cyclePaths = cyclePaths;
	}

	public Integer getTramBusLine() {
		return tramBusLine;
	}

	public void setTramBusLine(Integer tramBusLine) {
		this.tramBusLine = tramBusLine;
	}

	public Integer getRailLine() {
		return railLine;
	}

	public void setRailLine(Integer railLine) {
		this.railLine = railLine;
	}

	public Integer getGreenAreas() {
		return greenAreas;
	}

	public void setGreenAreas(Integer greenAreas) {
		this.greenAreas = greenAreas;
	}

	@Override
	public String toString() {

		String toreturn = "MobilityTrainingData [";

		if (accuracy != null)
			toreturn = toreturn + "accuracy=" + accuracy;
		else
			toreturn = toreturn + "accuracy=" + "NA";
		toreturn = toreturn + " , ";

		if (modalityClass4 != null)
			toreturn = toreturn + "modalityClass4=" + parsaModality(modalityClass4);
		else
			toreturn = toreturn + "modalityClass4=" + "NA";
		toreturn = toreturn + " , ";

		if (provider != null)
			toreturn = toreturn + "provider=" + provider;
		else
			toreturn = toreturn + "provider=" + "NA";
		toreturn = toreturn + " , ";

		if (speed != null)
			toreturn = toreturn + "speed=" + speed;
		else
			toreturn = toreturn + "speed=" + "NA";
		toreturn = toreturn + " , ";

		if (mean_speed != null)
			toreturn = toreturn + "mean_speed=" + mean_speed;
		else
			toreturn = toreturn + "mean_speed=" + "NA";
		toreturn = toreturn + " , ";

		if (bds != null)
			toreturn = toreturn + "bds=" + bds;
		else
			toreturn = toreturn + "bds=" + "NA";
		toreturn = toreturn + " , ";

		if (avg_lin_acc_magn != null)
			toreturn = toreturn + "acc-mag" + avg_lin_acc_magn;
		else
			toreturn = toreturn + "acc-mag" + "NA";
		toreturn = toreturn + " , ";

		if (lin_acc_x != null)
			toreturn = toreturn + lin_acc_x;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + " , ";

		if (lin_acc_y != null)
			toreturn = toreturn + lin_acc_y;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + " , ";

		if (lin_acc_z != null)
			toreturn = toreturn + lin_acc_z;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + " , ";

		if (weekEnd != null)
			toreturn = toreturn + "we" + weekEnd;
		else
			toreturn = toreturn + "we" + "NA";
		toreturn = toreturn + " , ";

		if (timeZone != null)
			toreturn = toreturn + "slot" + timeZone;
		else
			toreturn = toreturn + "slot" + "NA";
		toreturn = toreturn + " , ";

		if (phoneYear != null)
			toreturn = toreturn + "year" + phoneYear;
		else
			toreturn = toreturn + "year" + "NA";
		toreturn = toreturn + " , ";

		if (profile != null)
			toreturn = toreturn + "profile" + profile;
		else
			toreturn = toreturn + "profile" + "NA";
		toreturn = toreturn + " , ";

		if (prevSpeed != null)
			toreturn = toreturn + prevSpeed;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + " , ";

		if (meanPrevSpeed != null)
			toreturn = toreturn + meanPrevSpeed;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + " , ";

		if (medianPrevSpeed != null)
			toreturn = toreturn + medianPrevSpeed;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + " , ";

		if (speedDistance != null)
			toreturn = toreturn + speedDistance;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + " , ";

		if (sportsFacility != null)
			toreturn = toreturn + sportsFacility;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + " , ";

		if (touristTrail != null)
			toreturn = toreturn + touristTrail;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + " , ";

		if (cyclePaths != null)
			toreturn = toreturn + cyclePaths;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + " , ";

		if (tramBusLine != null)
			toreturn = toreturn + tramBusLine;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + " , ";

		if (railLine != null)
			toreturn = toreturn + railLine;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + " , ";

		if (greenAreas != null)
			toreturn = toreturn + greenAreas;
		else
			toreturn = toreturn + "NA";

		return toreturn + "]";
	}

	public void fillDeviceSpecs(DeviceSpecs ds) {

		// default
		this.bds = 0;

		if (ds != null) {
			if (ds.getGps().indexOf("BDS") != -1)
				this.bds = 1;

			this.phoneYear = Integer.parseInt(ds.getYear());
		}
	}

	public void fillTimeData(Long when) {

		if (when != null) {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(when);
			if (c.get(Calendar.DAY_OF_WEEK) == 0 || c.get(Calendar.DAY_OF_WEEK) == 6)
				this.weekEnd = 1;
			else
				this.weekEnd = 0;

			if (c.get(Calendar.HOUR_OF_DAY) < 8)
				this.timeZone = 1;
			if (c.get(Calendar.HOUR_OF_DAY) >= 8 & c.get(Calendar.HOUR_OF_DAY) < 12)
				this.timeZone = 2;
			if (c.get(Calendar.HOUR_OF_DAY) == 12 && c.get(Calendar.MINUTE) < 29)
				this.timeZone = 2;
			if (c.get(Calendar.HOUR_OF_DAY) == 12 && c.get(Calendar.MINUTE) >= 29)
				this.timeZone = 3;
			if (c.get(Calendar.HOUR_OF_DAY) >= 13 & c.get(Calendar.HOUR_OF_DAY) < 19)
				this.timeZone = 3;
			if (c.get(Calendar.HOUR_OF_DAY) >= 19)
				this.timeZone = 4;
		}
	}

	public void fillServiceMapData(Double latitude, Double longitude, String serviceMapUri, Integer timeout) {

		if ((latitude != null) && (longitude != null)) {

			try {
				String around = retrieveAround(latitude, longitude, serviceMapUri, timeout);

				if ((around != null) && (around.length() != 0))
					parsaAround(around);
				else {
					logger.error("servicemap not reachable");
				}
			} catch (Exception e) {
				logger.error(e);
				logger.error("problem occurred retrieveing data from servicemap {}", e);
			}
		}
	}

	private String retrieveAround(Double latitude, Double longitude, String serviceMapUri, Integer timeout) {
		String toreturn = new String();
		try {
			toreturn = ncg.get(new URL(serviceMapUri + "location/?position=" + Utils.approx(latitude) + ";" + Utils.approx(longitude) + "&intersectGeom=true&maxDists=0.001"), timeout);

		} catch (MalformedURLException e) {
			logger.error("MalformedURLException on retrieveCPZ:", e);
		}
		return toreturn;
	}

	private Integer parseProvider(String provider) {
		if (provider != null) {
			if (provider.equals("gps")) {
				return 1;
			} else if (provider.equals("fused")) {
				return 2;
			} else if (provider.equals("network")) {
				return 3;
			}
		}
		return null;
	}

	private String parsaModality(String modalityClass4) {

		// # mttm$modalityClass9[mttm$modality=="1"]="Stay"
		// # mttm$modalityClass9[mttm$modality=="2"]="Walk"
		// # mttm$modalityClass9[mttm$modality=="4"]="Bike"
		// # mttm$modalityClass9[mttm$modality=="5"]="Motorbike"
		// # mttm$modalityClass9[mttm$modality=="6"]="Bus"
		// # mttm$modalityClass9[mttm$modality=="7"]="Tram"
		// # mttm$modalityClass9[mttm$modality=="8"]="Car"
		// # mttm$modalityClass9[mttm$modality=="9"]="Train"

		if (modalityClass4 != null) {
			if (modalityClass4.equals("Stay")) {
				return "1";
			} else if (modalityClass4.equals("Walk")) {
				return "2";
			} else if (modalityClass4.equals("Run")) {
				return "3";
			} else if (modalityClass4.equals("Bike")) {
				return "4";
			} else if (modalityClass4.equals("Motorbike")) {
				return "5";
			} else if (modalityClass4.equals("Bus")) {
				return "6";
			} else if (modalityClass4.equals("Tram")) {
				return "7";
			} else if (modalityClass4.equals("Car")) {
				return "8";
			} else if (modalityClass4.equals("Train")) {
				return "9";
			}
		}
		return null;
	}

	private void parsaAround(String around) throws JsonProcessingException, IOException {
		this.greenAreas = 0;
		this.cyclePaths = 0;
		this.touristTrail = 0;
		this.sportsFacility = 0;
		this.tramBusLine = 0;
		this.railLine = 0;

		// create ObjectMapper instance
		ObjectMapper objectMapper = new ObjectMapper();

		// read JSON like DOM Parser
		JsonNode rootNode = objectMapper.readTree(around.getBytes());
		JsonNode intNode = rootNode.path("intersect");

		Iterator<JsonNode> elements = intNode.elements();
		while (elements.hasNext()) {
			JsonNode intersect = elements.next();
			if (intersect.get("class") != null) {
				if (intersect.get("class").textValue().equals("http://www.disit.org/km4city/schema#Green_areas")) {
					this.greenAreas = 1;
				} else if (intersect.get("class").textValue().equals("http://www.disit.org/km4city/schema#Cycle_paths")) {
					this.cyclePaths = 1;
				} else if (intersect.get("class").textValue().equals("http://www.disit.org/km4city/schema#Tourist_trail")) {
					this.touristTrail = 1;
				} else if (intersect.get("class").textValue().equals("http://www.disit.org/km4city/schema#Sports_facility")) {
					this.sportsFacility = 1;
				}
			}
			if (intersect.get("routeType") != null) {
				if (intersect.get("routeType").textValue().equals("Bus")) {
					this.tramBusLine = 1;
				} else if (intersect.get("routeType").textValue().equals("Train")) {
					this.railLine = 1;
				} else if (intersect.get("routeType").textValue().equals("Tram")) {
					this.tramBusLine = 1;
				}
			}
		}
	}

	private Integer parseProfile(String profile) {

		if (profile != null) {
			switch (profile) {
			case "citizen":
				return 1;
			case "student":
				return 2;
			case "commuter":
				return 3;
			case "tourist":
				return 4;
			case "all":
				return 5;
			case "operator":
				return 6;
			case "disabled":
				return 7;
			}
		}
		return null;

	}

	public void fillHistoricalData(List<MobilitySpeed> lastMS, Long when) {
		for (MobilitySpeed ms : lastMS) {
			logger.debug("calcolate historical data:{}", ms);
		}

		if (lastMS.size() <= 1)
			return;

		// here is always size>=2 (current included)
		// we remove it
		MobilitySpeed current = lastMS.remove(lastMS.size() - 1);

		// here size is always >=1

		this.prevSpeed = calcolate12minPreviousSpeed(lastMS, when);

		// here remove if the first one is between 12.5 and 12 we remove
		if ((when - lastMS.get(0).getTime().getTime()) > 720000) {
			lastMS.remove(0);
			if (lastMS.size() == 0)
				return;
		}

		// here size is always >=1

		this.meanPrevSpeed = calcolateMeanSpeed(lastMS);
		this.medianPrevSpeed = calcolateMedianSpeed(lastMS);
		this.speedDistance = calcolateServerSpeed(lastMS, current);
	}

	private Double calcolate12minPreviousSpeed(List<MobilitySpeed> lastMS, Long when) {

		if ((when - lastMS.get(0).getTime().getTime()) < 330000) {// 6 minutes -30 sec
			logger.debug("too young, ignoring");
			return null;
		}

		return lastMS.get(0).getSpeed();
	}

	// don't use the last entry (the current one)
	private Double calcolateMeanSpeed(List<MobilitySpeed> lastMS) {// used by R

		Double average = 0d;
		for (int i = 0; i < lastMS.size(); i++) {
			average = average + (Double) lastMS.get(i).getSpeed();
		}

		return (double) average / lastMS.size();

	}

	// don't use the last entry (the current one)
	@SuppressWarnings("unchecked")
	private Double calcolateMedianSpeed(List<MobilitySpeed> originallastMS) {// used by R

		ArrayList<MobilitySpeed> lastMS = (ArrayList<MobilitySpeed>) ((ArrayList<MobilitySpeed>) originallastMS).clone();

		Collections.sort(lastMS); // order the lastMS
		int middle = lastMS.size() / 2;
		if (lastMS.size() % 2 == 1) {
			return lastMS.get(middle).getSpeed();
		} else {
			return (lastMS.get(middle - 1).getSpeed() + lastMS.get(middle).getSpeed()) / 2.0;
		}
	}

	private Double calcolateServerSpeed(List<MobilitySpeed> lastMS, MobilitySpeed current) {

		if (lastMS.size() == 1) {
			Double distance = 1000 * Position.distance(lastMS.get(0).getLatitude(), lastMS.get(0).getLongitude(), current.getLatitude(), current.getLongitude(), "K");
			Long time = (current.getTime().getTime() - lastMS.get(0).getTime().getTime()) / 1000;
			return distance / time;
		} else {

			List<Double> speeds = new ArrayList<Double>();

			for (int k = 1; k < lastMS.size(); k++) {
				Double distance = 1000 * Position.distance(lastMS.get(k).getLatitude(), lastMS.get(k).getLongitude(), lastMS.get(k - 1).getLatitude(), lastMS.get(k - 1).getLongitude(), "K");
				Long time = (lastMS.get(k).getTime().getTime() - lastMS.get(k - 1).getTime().getTime()) / 1000;
				speeds.add(distance / (double) time);
			}

			return calcolateMedian(speeds);
		}
	}

	private Double calcolateMedian(List<Double> lastMS) {// used by R
		Collections.sort(lastMS); // order
		int middle = lastMS.size() / 2;
		if (lastMS.size() % 2 == 1) {
			return lastMS.get(middle);
		} else {
			return (lastMS.get(middle - 1) + lastMS.get(middle)) / 2.0;
		}
	}

	// acc,storic,year
	public String getScenario() {
		String toReturn;

		if ((lin_acc_x == null) || (lin_acc_y == null) || (lin_acc_z == null) || (avg_lin_acc_magn == null))
			toReturn = "FALSE_";
		else
			toReturn = "TRUE_";

		if ((prevSpeed == null) || (meanPrevSpeed == null) || (medianPrevSpeed == null) || (speedDistance == null))
			toReturn = toReturn + "FALSE_";
		else
			toReturn = toReturn + "TRUE_";

		if ((bds == null) || (phoneYear == null))
			toReturn = toReturn + "FALSE";
		else
			toReturn = toReturn + "TRUE";

		return toReturn;
	}

	public String getScenarioColumnsNameAll() {
		return "\"\",\"accuracy\",\"modality\",\"provider\",\"speed\",\"mean_speed\",\"bds\",\"avg_lin_acc_magn\",\"lin_acc_x\",\"lin_acc_y\",\"lin_acc_z\",\"weekEnd\",\"timeZone\",\"phoneYear\",\"profile\",\"prevSpeed\",\"meanPrevSpeed\",\"medianPrevSpeed\",\"SpeedDistance\",\"sportsFacility\",\"touristTrail\",\"cyclePaths\",\"tramBusLine\",\"railLine\",\"greenAreas\",\"acc\",\"modalityClass4\"";
	}

	public String getScenarioColumnsName() {
		return getScenarioColumnsName(getScenario());
	}

	// acc,storic,year
	public String getScenarioColumnsName(String scenario) {

		switch (scenario) {
		case "TRUE_TRUE_TRUE":
			return "\"accuracy\", \"modalityClass4\", \"provider\", \"speed\", \"mean_speed\", \"bds\", \"avg_lin_acc_magn\", \"lin_acc_x\", \"lin_acc_y\", \"lin_acc_z\", \"weekEnd\", \"timeZone\", \"phoneYear\", \"profile\", \"prevSpeed\", \"meanPrevSpeed\", \"medianPrevSpeed\", \"SpeedDistance\", \"sportsFacility\", \"touristTrail\", \"cyclePaths\", \"tramBusLine\", \"railLine\", \"greenAreas\"";
		case "TRUE_TRUE_FALSE":
			return "\"accuracy\", \"modalityClass4\", \"provider\", \"speed\", \"mean_speed\", \"bds\", \"avg_lin_acc_magn\", \"lin_acc_x\", \"lin_acc_y\", \"lin_acc_z\", \"weekEnd\", \"timeZone\", \"profile\", \"prevSpeed\", \"meanPrevSpeed\", \"medianPrevSpeed\", \"SpeedDistance\", \"sportsFacility\", \"touristTrail\", \"cyclePaths\", \"tramBusLine\", \"railLine\", \"greenAreas\"";
		case "TRUE_FALSE_TRUE":
			return "\"accuracy\", \"modalityClass4\", \"provider\", \"speed\", \"mean_speed\", \"bds\", \"avg_lin_acc_magn\", \"lin_acc_x\", \"lin_acc_y\", \"lin_acc_z\", \"weekEnd\", \"timeZone\", \"phoneYear\", \"profile\", \"sportsFacility\", \"touristTrail\", \"cyclePaths\", \"tramBusLine\", \"railLine\", \"greenAreas\"";
		case "TRUE_FALSE_FALSE":
			return "\"accuracy\", \"modalityClass4\", \"provider\", \"speed\", \"mean_speed\", \"bds\", \"avg_lin_acc_magn\", \"lin_acc_x\", \"lin_acc_y\", \"lin_acc_z\", \"weekEnd\", \"timeZone\", \"profile\", \"sportsFacility\", \"touristTrail\", \"cyclePaths\", \"tramBusLine\", \"railLine\", \"greenAreas\"";
		case "FALSE_TRUE_TRUE":
			return "\"accuracy\", \"modalityClass4\", \"provider\", \"speed\", \"mean_speed\", \"bds\", \"weekEnd\", \"timeZone\", \"phoneYear\", \"profile\", \"prevSpeed\", \"meanPrevSpeed\", \"medianPrevSpeed\", \"SpeedDistance\", \"sportsFacility\", \"touristTrail\", \"cyclePaths\", \"tramBusLine\", \"railLine\", \"greenAreas\"";
		case "FALSE_TRUE_FALSE":
			return "\"accuracy\", \"modalityClass4\", \"provider\", \"speed\", \"mean_speed\", \"bds\", \"weekEnd\", \"timeZone\", \"profile\", \"prevSpeed\", \"meanPrevSpeed\", \"medianPrevSpeed\", \"SpeedDistance\", \"sportsFacility\", \"touristTrail\", \"cyclePaths\", \"tramBusLine\", \"railLine\", \"greenAreas\"";
		case "FALSE_FALSE_TRUE":
			return "\"accuracy\", \"modalityClass4\", \"provider\", \"speed\", \"mean_speed\", \"bds\", \"weekEnd\", \"timeZone\", \"phoneYear\", \"profile\", \"sportsFacility\", \"touristTrail\", \"cyclePaths\", \"tramBusLine\", \"railLine\", \"greenAreas\"";
		case "FALSE_FALSE_FALSE":
			return "\"accuracy\", \"modalityClass4\", \"provider\", \"speed\", \"mean_speed\", \"bds\", \"weekEnd\", \"timeZone\", \"profile\", \"sportsFacility\", \"touristTrail\", \"cyclePaths\", \"tramBusLine\", \"railLine\", \"greenAreas\"";
		default:
			return null;
		}
	}

	public String getScenarioColumnsValueAll() {
		String toreturn = "";

		NumberFormat nf = NumberFormat.getInstance(Locale.ROOT);
		nf.setMaximumFractionDigits(16);
		nf.setGroupingUsed(false);

		if (accuracy != null)
			toreturn = toreturn + accuracy;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + ",";

		// report the modalityClass, as a index
		if ((modalityClass4 != null) && (parsaModality(modalityClass4) != null))
			toreturn = toreturn + "\"" + parsaModality(modalityClass4) + "\"";
		else
			toreturn = toreturn + "\"NA\"";
		toreturn = toreturn + ",";

		if (provider != null)
			toreturn = toreturn + provider;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + ",";

		if (speed != null)
			toreturn = toreturn + nf.format(speed);
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + ",";

		if (mean_speed != null)
			toreturn = toreturn + nf.format(mean_speed);
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + ",";

		if (bds != null)
			toreturn = toreturn + bds;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + ",";

		if (avg_lin_acc_magn != null)
			toreturn = toreturn + nf.format(avg_lin_acc_magn);
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + ",";

		if (lin_acc_x != null)
			toreturn = toreturn + nf.format(lin_acc_x);
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + ",";

		if (lin_acc_y != null)
			toreturn = toreturn + nf.format(lin_acc_y);
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + ",";

		if (lin_acc_z != null)
			toreturn = toreturn + nf.format(lin_acc_z);
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + ",";

		if (weekEnd != null)
			toreturn = toreturn + weekEnd;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + ",";

		if (timeZone != null)
			toreturn = toreturn + timeZone;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + ",";

		if (phoneYear != null)
			toreturn = toreturn + phoneYear;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + ",";

		if (profile != null)
			toreturn = toreturn + profile;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + ",";

		if (prevSpeed != null)
			toreturn = toreturn + nf.format(prevSpeed);
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + ",";

		if (meanPrevSpeed != null)
			toreturn = toreturn + nf.format(meanPrevSpeed);
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + ",";

		if (medianPrevSpeed != null)
			toreturn = toreturn + nf.format(medianPrevSpeed);
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + ",";

		if (speedDistance != null)
			toreturn = toreturn + nf.format(speedDistance);
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + ",";

		if (sportsFacility != null)
			toreturn = toreturn + sportsFacility;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + ",";

		if (touristTrail != null)
			toreturn = toreturn + touristTrail;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + ",";

		if (cyclePaths != null)
			toreturn = toreturn + cyclePaths;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + ",";

		if (tramBusLine != null)
			toreturn = toreturn + tramBusLine;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + ",";

		if (railLine != null)
			toreturn = toreturn + railLine;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + ",";

		if (greenAreas != null)
			toreturn = toreturn + greenAreas;
		else
			toreturn = toreturn + "NA";
		toreturn = toreturn + ",";

		// specify if accelerometor is included or not
		if ((avg_lin_acc_magn != null) && (lin_acc_x != null) && (lin_acc_y != null) && (lin_acc_z != null))
			toreturn = toreturn + "\"1\"";
		else
			toreturn = toreturn + "\"0\"";
		toreturn = toreturn + ",";

		// report the modalityClass, as a string
		if (modalityClass4 != null)
			toreturn = toreturn + "\"" + modalityClass4 + "\"";
		else
			toreturn = toreturn + "\"NA\"";

		return toreturn;
	}

	public String getScenarioColumnsValue() {
		return getScenarioColumnsValue(getScenario());
	}

	// acc,storic,year
	public String getScenarioColumnsValue(String scenario) {

		switch (scenario) {
		case "TRUE_TRUE_TRUE":
			return accuracy + " , " + "NA" + " , " + provider + " , " + speed + " , " + mean_speed + " , " + bds + " , " + avg_lin_acc_magn + " , " + lin_acc_x + " , " + lin_acc_y + " , " + lin_acc_z + " , " + weekEnd + " , " + timeZone
					+ " , " + phoneYear + " , " + profile + " , " + prevSpeed + " , " + meanPrevSpeed
					+ " , " + medianPrevSpeed + " , " + speedDistance + " , " + sportsFacility + " , " + touristTrail + " , " + cyclePaths + " , " + tramBusLine + " , " + railLine + " , " + greenAreas;
		case "TRUE_TRUE_FALSE":
			return accuracy + " , " + "NA" + " , " + provider + " , " + speed + " , " + mean_speed + " , " + bds + " , " + avg_lin_acc_magn + " , " + lin_acc_x + " , " + lin_acc_y + " , " + lin_acc_z + " , " + weekEnd + " , " + timeZone
					+ " , " + profile
					+ " , " + prevSpeed + " , " + meanPrevSpeed + " , " + medianPrevSpeed + " , " + speedDistance + " , " + sportsFacility
					+ " , " + touristTrail + " , " + cyclePaths + " , " + tramBusLine + " , " + railLine + " , " + greenAreas;
		case "TRUE_FALSE_TRUE":
			return accuracy + " , " + "NA" + " , " + provider + " , " + speed + " , " + mean_speed + " , " + bds + " , " + avg_lin_acc_magn + " , " + lin_acc_x + " , " + lin_acc_y + " , " + lin_acc_z + " , " + weekEnd + " , " + timeZone
					+ " , " + phoneYear + " , " + profile + " , " + sportsFacility + " , " + touristTrail + " , " + cyclePaths + " , " + tramBusLine + " , " + railLine
					+ " , " + greenAreas;
		case "TRUE_FALSE_FALSE":
			return accuracy + " , " + "NA" + " , " + provider + " , " + speed + " , " + mean_speed + " , " + bds + " , " + avg_lin_acc_magn + " , " + lin_acc_x + " , " + lin_acc_y + " , " + lin_acc_z + " , " + weekEnd + " , " + timeZone
					+ " , " + profile
					+ " , " + sportsFacility + " , " + touristTrail + " , " + cyclePaths + " , " + tramBusLine + " , " + railLine + " , " + greenAreas;
		case "FALSE_TRUE_TRUE":
			return accuracy + " , " + "NA" + " , " + provider + " , " + speed + " , " + mean_speed + " , " + bds + " , " + weekEnd + " , " + timeZone + " , " + phoneYear + " , " + profile + " , " + prevSpeed + " , " + meanPrevSpeed
					+ " , "
					+ medianPrevSpeed + " , " + speedDistance + " , " + sportsFacility + " , " + touristTrail + " , " + cyclePaths + " , " + tramBusLine
					+ " , " + railLine + " , " + greenAreas;
		case "FALSE_TRUE_FALSE":
			return accuracy + " , " + "NA" + " , " + provider + " , " + speed + " , " + mean_speed + " , " + bds + " , " + weekEnd + " , " + timeZone + " , " + profile + " , " + prevSpeed + " , " + meanPrevSpeed + " , "
					+ medianPrevSpeed
					+ " , "
					+ speedDistance + " , " + sportsFacility + " , " + touristTrail + " , " + cyclePaths + " , " + tramBusLine + " , " + railLine
					+ " , " + greenAreas;
		case "FALSE_FALSE_TRUE":
			return accuracy + " , " + "NA" + " , " + provider + " , " + speed + " , " + mean_speed + " , " + bds + " , " + weekEnd + " , " + timeZone + " , " + phoneYear + " , " + profile + " , " + sportsFacility + " , " + touristTrail
					+ " , " + cyclePaths + " , " + tramBusLine + " , " + railLine + " , " + greenAreas;
		case "FALSE_FALSE_FALSE":
			return accuracy + " , " + "NA" + " , " + provider + " , " + speed + " , " + mean_speed + " , " + bds + " , " + weekEnd + " , " + timeZone + " , " + profile + " , " + sportsFacility + " , " + touristTrail + " , " + cyclePaths
					+ " , "
					+ tramBusLine + " , " + railLine + " , " + greenAreas;
		default:
			return null;
		}
	}
}
