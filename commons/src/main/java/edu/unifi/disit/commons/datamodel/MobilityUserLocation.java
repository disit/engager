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

import edu.unifi.disit.commons.datamodel.UserLocation;

public class MobilityUserLocation extends UserLocation {

	String provider;

	Double mean_speed;

	Double acc_magni;
	Double acc_x;
	Double acc_y;
	Double acc_z;

	// from db
	// 0 -> 9 super class
	// 10 provider
	// 11 mean speed
	// 12 magnitude acce
	// 13, 14, 15 acce
	public MobilityUserLocation(Object[] entity) {
		super(entity);
		this.provider = (String) entity[10];
		this.mean_speed = (Double) entity[11];
		this.acc_magni = (Double) entity[12];
		this.acc_x = (Double) entity[13];
		this.acc_y = (Double) entity[14];
		this.acc_z = (Double) entity[15];
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public Double getMean_speed() {
		return mean_speed;
	}

	public void setMean_speed(Double mean_speed) {
		this.mean_speed = mean_speed;
	}

	public Double getAcc_magni() {
		return acc_magni;
	}

	public void setAcc_magni(Double acc_magni) {
		this.acc_magni = acc_magni;
	}

	public Double getAcc_x() {
		return acc_x;
	}

	public void setAcc_x(Double acc_x) {
		this.acc_x = acc_x;
	}

	public Double getAcc_y() {
		return acc_y;
	}

	public void setAcc_y(Double acc_y) {
		this.acc_y = acc_y;
	}

	public Double getAcc_z() {
		return acc_z;
	}

	public void setAcc_z(Double acc_z) {
		this.acc_z = acc_z;
	}

	@Override
	public String toString() {
		return "MobilityUserLocation [provider=" + provider + ", mean_speed=" + mean_speed + ", acc_magni=" + acc_magni + ", acc_x=" + acc_x + ", acc_y=" + acc_y + ", acc_z=" + acc_z + ", userName=" + userName + ", deviceLanguage="
				+ deviceLanguage + ", id=" + id + ", accuracy=" + accuracy + ", speed=" + speed + ", profile=" + profile + ", data=" + data + ", seconds=" + seconds + ", status=" + status + ", latitude=" + latitude + ", longitude="
				+ longitude + ", name=" + name + "]";
	}
}
