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
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Location extends Position implements Comparable<Location> {

	protected Date data;
	@JsonIgnore
	protected Double seconds;
	@JsonIgnore
	protected String status = null;

	public Location(Double latitude, Double longitude, String name, Timestamp data, Double seconds, String status) {
		super(latitude, longitude, name);
		this.data = new Date(data.getTime());
		this.seconds = seconds;
		if ((status != null) && (status.length() != 0))
			this.status = status;
	}

	public Location(Position p, Timestamp data, Double seconds) {
		super(p);
		this.data = new Date(data.getTime());
		this.seconds = seconds;
	}

	public Location(Position p, Date data, Double seconds) {
		super(p);
		this.data = data;
		this.seconds = seconds;
	}

	// from db
	// 0 is date
	// 1 is status
	// 2 is seconds
	// 3 is latitude
	// 4 is longitude
	public Location(Object[] fromDB) {
		super((Double) fromDB[3], (Double) fromDB[4], "unknown");
		this.data = new Date(((Timestamp) fromDB[0]).getTime());
		this.seconds = (Double) fromDB[2];
		if (((String) fromDB[1] != null) && (((String) fromDB[1]).length() != 0))
			this.status = (String) fromDB[1];
	}

	public Location(Object[] fromDB, boolean GMT) {
		super((Double) fromDB[3], (Double) fromDB[4], "unknown");
		long localTime = ((Timestamp) fromDB[0]).getTime();

		this.data = new Date(localTime - TimeZone.getDefault().getOffset(localTime));

		this.seconds = (Double) fromDB[2];
		if (((String) fromDB[1] != null) && (((String) fromDB[1]).length() != 0))
			this.status = (String) fromDB[1];
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	public Double getSeconds() {
		return seconds;
	}

	public void setSeconds(Double seconds) {
		this.seconds = seconds;
	}

	public void addSeconds(Double seconds) {
		this.seconds = this.seconds + seconds;
	}

	@Override
	public String toString() {
		if (status != null)
			return "Location [data=" + data + ", seconds=" + seconds + "\t, status=" + status + "\t, latitude=" + latitude + ", longitude=" + longitude + ", name=" + name + "]";
		else
			return "Location [data=" + data + ", seconds=" + seconds + "\t, status=NULL\t, latitude=" + latitude + ", longitude=" + longitude + ", name=" + name + "]";
	}

	public int compareTo(Location o) {
		return (Double.compare(o.getSeconds(), this.seconds));
	}
}
