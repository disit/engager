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
package edu.unifi.disit.surveycollectorapi;

import java.util.Enumeration;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.unifi.disit.surveycollectorapi.DBinterface;

public class GetPropertyValues extends Thread {

	private static final Logger logger = LogManager.getLogger("GetPropertyValues");
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
}
