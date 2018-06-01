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

import java.sql.Timestamp;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserLocation extends Location {

	private static final Logger logger = LogManager.getLogger("UserLocation");

	String userName;
	String deviceLanguage = Locale.ITALIAN.toString();
	int id;
	Double accuracy;
	Double speed;
	String profile;

	public UserLocation(String userName, Double latitude, Double longitude, String name, Timestamp data, Double seconds, String status, String deviceLanguage, Double accuracy, Double speed, String profile) {
		super(latitude, longitude, name, data, seconds, status);
		this.userName = userName;
		setDeviceLanguage(deviceLanguage);
		this.accuracy = accuracy;
		this.speed = speed;
		this.profile = profile;
	}

	// from db
	// 0 is id
	// 1 is date
	// 2 is userName
	// 3 is latitude
	// 4 is longitude
	// 5 status is the old support, not the aggregated one from daniele
	// 6 device language
	// 7 accuracy
	// 8 speed
	// 9 profile
	public UserLocation(Object[] fromDB) {
		super((Double) fromDB[3], (Double) fromDB[4], "unknown", (Timestamp) fromDB[1], 0d, (String) fromDB[5]);
		this.userName = (String) fromDB[2];
		setDeviceLanguage((String) fromDB[6]);
		// this.id = ((java.math.BigInteger) fromDB[0]).intValue();
		this.id = ((Integer) fromDB[0]).intValue();
		this.accuracy = ((Double) fromDB[7]);
		this.speed = ((Double) fromDB[8]);
		this.profile = ((String) fromDB[9]);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getDeviceLanguage() {
		return deviceLanguage;
	}

	public Double getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(Double accuracy) {
		this.accuracy = accuracy;
	}

	public Double getSpeed() {
		return speed;
	}

	public void setSpeed(Double speed) {
		this.speed = speed;
	}

	public void setDeviceLanguage(String deviceLang) {
		if ((deviceLang != null) && (!deviceLang.isEmpty())) {
			Locale l = new Locale(deviceLang);
			if (l.toString() != null) {
				this.deviceLanguage = l.toString();
			} else {
				logger.warn("No valid language code for user:{} was ({}) ... set default: italian", userName, deviceLang);
			}
		} else {
			logger.debug("No defined language for user:{} (set default italian)", userName);
		}
	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	@Override
	public String toString() {
		return "UserLocation [userName=" + userName + ", deviceLanguage=" + deviceLanguage + ", id=" + id + ", accuracy=" + accuracy + ", speed=" + speed + ", profile=" + profile + ", data=" + data + ", seconds=" + seconds + ", status="
				+ status + ", latitude=" + latitude + ", longitude=" + longitude + ", name=" + name + "]";
	}

}
