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
package edu.unifi.disit.userprofiler.ppois;

import java.util.Enumeration;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.unifi.disit.userprofiler.datamodel.PropertiesDAO;

@Component
public class GetPropertyValues {

	@Autowired
	PropertiesDAO propertiesdao;

	private static final Logger logger = LogManager.getLogger();

	protected Properties properties;

	public void update() {
		for (edu.unifi.disit.userprofiler.datamodel.Properties prop : propertiesdao.findAll()) {
			properties.setProperty(prop.getProperties_name(), prop.getProperties_value());
		}
	}

	@PostConstruct
	void init() {
		try {
			properties = new Properties();
			for (edu.unifi.disit.userprofiler.datamodel.Properties prop : propertiesdao.findAll()) {
				properties.setProperty(prop.getProperties_name(), prop.getProperties_value());
			}
		} catch (Exception e) {
			logger.error("Exception in parsing:", e);
		}

	}

	public org.apache.logging.log4j.Level getLogLevel() {
		String level = properties.getProperty("log_level");
		if (level != null) {
			logger.debug("logger level is:{}", level);
			org.apache.logging.log4j.Level loglevel = Level.getLevel(level);
			if (loglevel != null)
				return loglevel;
		}
		logger.warn("beware the properties log_level is not defined");
		return (org.apache.logging.log4j.Level.WARN);// WARN is the default log level
	}

	public String getProperty(String property_name) {
		return properties.getProperty(property_name);
	}

	public Enumeration<Object> getPropertyKeys() {
		return properties.keys();
	}

	// populate ppoi
	public Float getDistanceCluster() {
		return Float.parseFloat(properties.getProperty("distance_cluster"));
	}

	public Integer getNWeekBefore() {
		return Integer.parseInt(properties.getProperty("n_week_before"));
	}

	public Integer getCutAccuracy() {
		return Integer.parseInt(properties.getProperty("cut_accuracy"));
	}

	public Integer getMinSoglia() {
		return Integer.parseInt(properties.getProperty("min_soglia_entry"));
	}

	public Float getSogliaAccuracyHome() {
		return Float.parseFloat(properties.getProperty("soglia_accuracy_home"));
	}

	public String getHomeFrom() {
		return properties.getProperty("home_from");
	}

	public String getHomeTo() {
		return properties.getProperty("home_to");
	}

	public Float getSogliaAccuracyWork() {
		return Float.parseFloat(properties.getProperty("soglia_accuracy_work"));
	}

	public String getWorkFrom() {
		return properties.getProperty("work_from");
	}

	public String getWorkTo() {
		return properties.getProperty("work_to");
	}

	public Float getSogliaAccuracySchool() {
		return Float.parseFloat(properties.getProperty("soglia_accuracy_school"));
	}

	public String getSchoolFrom() {
		return properties.getProperty("school_from");
	}

	public String getSchoolTo() {
		return properties.getProperty("school_to");
	}

	public Float getSogliaAccuracyALL() {
		return Float.parseFloat(properties.getProperty("soglia_accuracy_all"));
	}

	public String getALLFrom() {
		return properties.getProperty("all_from");
	}

	public String getALLTo() {
		return properties.getProperty("all_to");
	}

	public Integer getNHoursBefore() {
		return Integer.parseInt(properties.getProperty("n_hours_before"));
	}

	// servicemap
	public String getServicemapURL() {
		return properties.getProperty("servicemap_url");
	}

	public int getReadTimeoutMillisecond() {
		return Integer.parseInt(properties.getProperty("read_timeout"));
	}

	// markov
	public int getMinSecondsForPPOI() {
		return Integer.parseInt(properties.getProperty("min_hours_for_ppoi")) * 3600;
	}

	public String getMarkovPath() {
		return properties.getProperty("markov_path");
	}

	// suspicious
	public Long getAlertThresholdStatusSeconds() {
		return Long.parseLong(properties.getProperty("alert_threshold_status_minutes")) * 60;
	}

	public Long getAlertRefreshStatusSeconds() {
		return Long.parseLong(properties.getProperty("alert_refresh_status_minutes")) * 60;
	}

	public String getRDatasetPath() {
		return properties.getProperty("rdataset_path");
	}

	public Float getKMetersForRefreshLocation() {
		return Float.parseFloat(properties.getProperty("meters_refresh_location")) / 1000;
	}

	public long getSecondsForRefreshLocation() {
		return Long.parseLong(properties.getProperty("minutes_refresh_location")) * 60;
	}
}
