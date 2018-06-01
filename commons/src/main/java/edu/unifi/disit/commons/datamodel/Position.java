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

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Position {

	protected Double latitude;
	protected Double longitude;
	@JsonIgnore
	protected String name = "unknown";

	Position() {
	}

	public Position(Position p) {
		this(p.getLatitude(), p.getLongitude(), p.getName());
	}

	public Position(Double latitude, Double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Position(Double latitude, Double longitude, String name) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
		return "Position [latitude=" + latitude + ", longitude=" + longitude + "]";
	}

	public Double distance(Position p) {
		return distance(latitude, longitude, p.getLatitude(), p.getLongitude(), "K");
	}

	public static final boolean isClose(Double lat1, Double lon1, Double lat2, Double lon2, Float distance) {
		return distance(lat1, lon1, lat2, lon2, "K") < distance ? true : false;
	}

	public boolean isClose(Position p, Float distance) {
		return distance(latitude, longitude, p.getLatitude(), p.getLongitude(), "K") < distance ? true : false;
	}

	public static final Double distance(Double lat1, Double lon1, Double lat2, Double lon2, String unit) {
		if ((lat1.doubleValue() == lat2.doubleValue()) && (lon1.doubleValue() == lon2.doubleValue()))
			return 0d;

		Double theta = lon1 - lon2;
		Double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == "K") {
			dist = dist * 1.609344;
		} else if (unit == "N") {
			dist = dist * 0.8684;
		}

		if (Double.isNaN(dist))
			dist = 0d;

		return (dist);
	}

	private static final Double deg2rad(Double deg) {
		return (deg * Math.PI / 180.0);
	}

	private static final Double rad2deg(Double rad) {
		return (rad * 180 / Math.PI);
	}
}
