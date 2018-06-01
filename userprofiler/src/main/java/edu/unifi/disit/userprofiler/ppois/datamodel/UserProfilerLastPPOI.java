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
package edu.unifi.disit.userprofiler.ppois.datamodel;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "users_last_ppoi")
public class UserProfilerLastPPOI {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	Long id;

	@Column(name = "userid")
	String userid;

	@Column(name = "ppoi")
	String ppoi;

	@Column
	@Type(type = "timestamp")
	private Date time;

	@Column(name = "distance")
	Double distance = 0d;

	public UserProfilerLastPPOI() {
	}

	public UserProfilerLastPPOI(Long id, String userid, String ppoi, Date time, Double distance) {
		this.id = id;
		this.userid = userid;
		this.ppoi = ppoi;
		this.time = time;
		this.distance = distance;
	}

	public UserProfilerLastPPOI(String userid, String ppoi, Date time, Double distance) {
		this.userid = userid;
		this.ppoi = ppoi;
		this.time = time;
		this.distance = distance;
	}

	public UserProfilerLastPPOI(String userid, String ppoi, Double distance) {
		this.userid = userid;
		this.ppoi = ppoi;
		this.time = new Date();
		this.distance = distance;
	}

	public Long getUserProfilerLastPPOIId() {
		return id;
	}

	public void setUserProfilerLastPPOIId(Long id) {
		this.id = id;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getPpoi() {
		return ppoi;
	}

	public void setPpoi(String ppoi) {
		this.ppoi = ppoi;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	public void addDistance(Double distance) {
		this.distance += distance;
	}

	@Override
	public String toString() {
		return "UserProfilerLastPPOI [id=" + id + ", userid=" + userid + ", ppoi=" + ppoi + ", time=" + time + ", distance=" + distance + "]";
	}
}
