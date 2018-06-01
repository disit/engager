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
package edu.unifi.disit.engagerapi;

import java.util.Enumeration;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GetPropertyValues extends Thread {

	private static final Logger logger = LogManager.getLogger();
	private static GetPropertyValues instance = null;
	protected Properties properties;
	private static final DBinterface db = DBinterface.getInstance();
	private boolean isAlive = true;

	public void run() {
		while (isAlive) {
			for (edu.unifi.disit.commons.datamodel.Properties prop : db.getProperties()) {
				properties.setProperty(prop.getProperties_name(), prop.getProperties_value());
			}
			try {
				logger.trace("properties is going sleeping...");
				sleep(getSleepPropertiesMilliseconds());
				logger.trace("...properties is woking up");
			} catch (InterruptedException e) {
				logger.error("Catch error in sleep:", e);
				isAlive = false;
			}
		}
		try {
			this.finalize();
		} catch (Throwable e) {
			logger.error("Throwable in finalize:", e);
		}

		logger.info("...current thread stopped");
	}

	public void stopit() {
		isAlive = false;
		logger.info("Try to stop current thread...");
	}

	private GetPropertyValues() {
		try {
			properties = new Properties();
			for (edu.unifi.disit.commons.datamodel.Properties prop : db.getProperties()) {
				properties.setProperty(prop.getProperties_name(), prop.getProperties_value());
			}
		} catch (Exception e) {
			logger.error("Exception in parsing:", e);
		}
		start();
	}

	public static GetPropertyValues getInstance() {
		if (instance == null) {
			synchronized (GetPropertyValues.class) {// thread safe
				if (instance == null) {
					instance = new GetPropertyValues();
				}
			}
		}
		return instance;
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

	// properties
	public String getProperty(String property_name) {
		return properties.getProperty(property_name);
	}

	public Enumeration<Object> getPropertyKeys() {
		return properties.keys();
	}

	// common
	public String getUserProfilerURL() {
		return properties.getProperty("userprofiler_url");
	}

	public Long getSleepPropertiesMilliseconds() {
		return Long.parseLong(properties.getProperty("sleep_properties_seconds")) * 1000;
	}

	public int getReadTimeoutMillisecond() {
		return Integer.parseInt(properties.getProperty("read_timeout"));
	}

	// engager
	public Integer getCutOutAccuracy() {
		return Integer.parseInt(properties.getProperty("cutout_accuracy"));
	}

	public Long getSleepMillisecondsAccessLog() {
		return Long.parseLong(properties.getProperty("sleep_accesslog")) * 1000;
	}

	public Long getEngagementElapsingMilliseconds() {
		return Long.parseLong(properties.getProperty("default_elapsing_minutes")) * 60 * 1000;
	}

	public Long getStartingWindowMillisecondsAccesslog() {
		return Long.parseLong(properties.getProperty("startup_window_seconds_accesslog")) * 1000;
	}

	public Long getTooOldUserLocationMilliseconds() {
		return Long.parseLong(properties.getProperty("too_old_user_location_minutes")) * 60 * 1000;
	}

	public String getSimulationPath() {
		return properties.getProperty("simulation_path");
	}

	public Long getDBSensorTimeoutMilliseconds() {
		return Long.parseLong(properties.getProperty("DBsensor_timeout_seconds")) * 1000;
	}

	public Long getTimeoutEngagementMilliseconds() {
		return Long.parseLong(properties.getProperty("timeout_engagement_minutes")) * 60 * 1000;
	}

	public long getStartingWindowMillisecondsExecuted() {
		return Long.parseLong(properties.getProperty("startup_window_hours_executed")) * 60 * 60 * 1000;
	}

	public long getSleepMillisecondsExecuted() {
		return Long.parseLong(properties.getProperty("sleep_executed_minutes")) * 60 * 1000;
	}

	public Long getThresholdLiveMilliseconds() {
		return Long.parseLong(properties.getProperty("threshold_live_minutes")) * 60 * 1000;
	}

	public long getSleepMilliSecondsEngager() {
		return Long.parseLong(properties.getProperty("sleep_engager_seconds")) * 1000;
	}

	public String getServicemapURL() {
		return properties.getProperty("servicemap_url");
	}

	public Integer getServiceMapMaxResult() {
		return Integer.valueOf(properties.getProperty("servicemap_maxresult"));
	}

	public Double getServiceMapRange() {
		return Double.valueOf(properties.getProperty("servicemap_range"));
	}

	public String getCategoriesServiceMap() {
		return properties.getProperty("categories_service_map");
	}

	public Float getCloseDistance() {
		return Float.parseFloat(properties.getProperty("close_distance"));
	}

	public String getExecutionCheckerPath() {
		return properties.getProperty("executionchecker_path");
	}

	public long getSleepMillisecondsExecuterLibrary() {
		return Long.parseLong(properties.getProperty("sleep_executer_libraries_minutes")) * 60 * 1000;
	}

	public Float getPreditionAccuracy() {
		return Float.parseFloat(properties.getProperty("prediction_accuracy"));
	}

	public int getPredictionHowmany() {
		return Integer.valueOf(properties.getProperty("prediction_howmany"));
	}

	public Double getSpeedAverageThreshold() {
		return Double.valueOf(properties.getProperty("speed_average_threshold"));// metri al secondo
	}

	public Float getClosePublicTransportDistance() {
		return Float.parseFloat(properties.getProperty("close_public_transport_distance"));
	}
}
