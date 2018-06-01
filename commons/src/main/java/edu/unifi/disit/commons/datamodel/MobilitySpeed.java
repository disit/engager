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
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "mobility_speed")
public class MobilitySpeed implements Comparable<MobilitySpeed>, Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	String deviceId;

	Date time;

	Double speed;

	Double latitude;

	Double longitude;

	public MobilitySpeed(MobilityUserLocation mul, String deviceId) {
		this.id = new Long(mul.getId()).longValue();
		this.deviceId = deviceId;
		this.time = mul.getData();
		this.speed = mul.getSpeed();
		this.latitude = mul.getLatitude();
		this.longitude = mul.getLongitude();
	}

	// 0 id
	// 1 deviceid
	// 2 time
	// 3 speed
	// 4 lat
	// 5 long
	public MobilitySpeed(Object[] fromDB) {
		this.id = ((Integer) fromDB[0]).longValue();
		this.deviceId = (String) fromDB[1];
		this.time = new Date(((Timestamp) fromDB[2]).getTime());
		this.speed = ((Double) fromDB[3]);
		this.latitude = ((Double) fromDB[4]);
		this.longitude = ((Double) fromDB[5]);
	}

	public MobilitySpeed(Long id, String deviceId, Date time, Double speed, Double latitude, Double longitude) {
		this.id = id;
		this.deviceId = deviceId;
		this.time = time;
		this.speed = speed;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public MobilitySpeed() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public Double getSpeed() {
		return speed;
	}

	public void setSpeed(Double speed) {
		this.speed = speed;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	@Override
	public String toString() {
		return "MobilitySpeed [id=" + id + ", deviceId=" + deviceId + ", time=" + time + ", speed=" + speed + ", latitude=" + latitude + ", longitude=" + longitude + "]";
	}

	@Override
	public int compareTo(MobilitySpeed o) {// compare basing the speed!!!! and not the date!!!!
		if (this.speed < o.getSpeed())
			return -1;
		else if (this.speed == o.getSpeed())
			return 0;
		else
			return 1;
	}
}
