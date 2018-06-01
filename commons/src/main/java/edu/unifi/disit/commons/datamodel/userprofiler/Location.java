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

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import edu.unifi.disit.commons.datamodel.SMLocation;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "Location")
public class Location extends SMLocation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	Date time;
	Double latitude;
	Double longitude;

	@JsonIgnore
	@ManyToOne
	private Device device;

	public Location() {
		super();
	}

	public Location(Long id, Date time, Double latitude, Double longitude, Device device) {
		super();
		this.id = id;
		this.time = time;
		this.latitude = latitude;
		this.longitude = longitude;
		this.device = device;
	}

	public Location(Date time, Double latitude, Double longitude, Device device) {
		super();
		this.time = time;
		this.latitude = latitude;
		this.longitude = longitude;
		this.device = device;
	}

	public Location(SMLocation sml, Date time, Double latitude, Double longitude, Device device) {
		super(sml);
		this.time = time;
		this.latitude = latitude;
		this.longitude = longitude;
		this.device = device;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
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

	@Override
	public String toString() {
		return "Location [id=" + id + ", time=" + time + ", latitude=" + latitude + ", longitude=" + longitude + ", device=" + device + ", address=" + address + ", number=" + number + ", municipality=" + municipality + ", province="
				+ province + ", cpz=" + cpz + "]";
	}

}
