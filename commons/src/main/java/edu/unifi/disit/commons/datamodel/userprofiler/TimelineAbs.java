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
package edu.unifi.disit.commons.datamodel.userprofiler;

import java.sql.Timestamp;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.unifi.disit.commons.utils.Utils;

@MappedSuperclass
public class TimelineAbs {

	@JsonIgnore
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Integer id;

	Timestamp date;// starting date

	String status;

	Long seconds = 0L;

	Long meters = 0L;

	Double latitude;// update to last entry

	Double longitude;// update to last entry

	@JsonIgnore
	@OneToOne
	Device device;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void resetId() {
		this.id = null;
	}

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}

	public void setDateTimestamp(Long date) {
		this.date = new Timestamp(date);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getSeconds() {
		return seconds;
	}

	public void setSeconds(Long seconds) {
		this.seconds = seconds;
	}

	// public void setSecondsLong(Long seconds) {
	// this.seconds = new Long(seconds).intValue();
	// }

	public void resetSeconds() {
		this.seconds = 0L;
	}

	public Long getMeters() {
		return meters;
	}

	public void setMeters(Long meters) {
		this.meters = meters;
	}

	public void addMeters(Double meters) {
		addMeters(meters.longValue());
	}

	public void addMeters(Long meters) {
		this.meters += meters;
	}

	public void resetMeters() {
		this.meters = 0L;
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

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public String getSecondsLabel() {
		return Utils.convertSecondsToString(this.seconds);
	}

	public void setSecondsLabel(String sLabel) {
		// TODO convert from string to seconds
	}

	public String getMetersLabel() {
		return Utils.convertMetersToString(this.meters);
	}

	public void setMetersLabel(String sLabel) {
		// TODO convert from string to seconds
	}

	@Override
	public String toString() {
		String toReturn = "TimelineAbs [";

		if (id != null)
			toReturn = toReturn + "id=" + id + ",";
		if (date != null)
			toReturn = toReturn + "date=" + date + ",";
		if (status != null)
			toReturn = toReturn + "status=" + status + ",";
		if (seconds != null)
			toReturn = toReturn + "seconds=" + seconds + ",";
		if (meters != null)
			toReturn = toReturn + "meters=" + meters + ",";
		if (latitude != null)
			toReturn = toReturn + "latitude=" + latitude + ",";
		if (longitude != null)
			toReturn = toReturn + "longitude=" + longitude + ",";
		if ((device != null) && (device.getDeviceId() != null))
			toReturn = toReturn + "deviceId=" + device.getDeviceId() + ",";

		// return "TimelineAbs [id=" + id + ", date=" + date + ", status=" + status + ", seconds=" + seconds + ", meters=" + meters + ", latitude=" + latitude + ", longitude=" + longitude + ", device_id=" + device.getDeviceId() + "]";

		return toReturn + "]";
	}
}
