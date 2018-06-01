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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "user_activities")
public class UserActivities {

	@JsonIgnore
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	Long id;

	@Column(name = "SUBMITTED_STARS")
	private int SUBMITTED_STARS = 0;

	@Column(name = "SUBMITTED_COMMENTS")
	private int SUBMITTED_COMMENTS = 0;

	@Column(name = "SUBMITTED_PHOTOS")
	private int SUBMITTED_PHOTOS = 0;

	@Column(name = "EXECUTED_ENGAGEMENTS")
	private int EXECUTED_ENGAGEMENTS = 0;

	@JsonIgnore
	@OneToOne
	private Device device;

	public UserActivities() {
		super();
	}

	public UserActivities(int SUBMITTED_STARS, int SUBMITTED_COMMENTS, int SUBMITTED_PHOTOS, int EXECUTED_ENGAGEMENTS) {
		super();
		this.SUBMITTED_STARS = SUBMITTED_STARS;
		this.SUBMITTED_COMMENTS = SUBMITTED_COMMENTS;
		this.SUBMITTED_PHOTOS = SUBMITTED_PHOTOS;
		this.EXECUTED_ENGAGEMENTS = EXECUTED_ENGAGEMENTS;
	}

	public UserActivities(Long id, int SUBMITTED_STARS, int SUBMITTED_COMMENTS, int SUBMITTED_PHOTOS, int EXECUTED_ENGAGEMENTS) {
		super();
		this.id = id;
		// this.device = device;
		this.SUBMITTED_STARS = SUBMITTED_STARS;
		this.SUBMITTED_COMMENTS = SUBMITTED_COMMENTS;
		this.SUBMITTED_PHOTOS = SUBMITTED_PHOTOS;
		this.EXECUTED_ENGAGEMENTS = EXECUTED_ENGAGEMENTS;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getSUBMITTED_STARS() {
		return SUBMITTED_STARS;
	}

	public void setSUBMITTED_STARS(int sUBMITTED_STARS) {
		SUBMITTED_STARS = sUBMITTED_STARS;
	}

	public int getSUBMITTED_COMMENTS() {
		return SUBMITTED_COMMENTS;
	}

	public void setSUBMITTED_COMMENTS(int sUBMITTED_COMMENTS) {
		SUBMITTED_COMMENTS = sUBMITTED_COMMENTS;
	}

	public int getSUBMITTED_PHOTOS() {
		return SUBMITTED_PHOTOS;
	}

	public void setSUBMITTED_PHOTOS(int sUBMITTED_PHOTOS) {
		SUBMITTED_PHOTOS = sUBMITTED_PHOTOS;
	}

	public int getEXECUTED_ENGAGEMENTS() {
		return EXECUTED_ENGAGEMENTS;
	}

	public void setEXECUTED_ENGAGEMENTS(int eXECUTED_ENGAGEMENTS) {
		EXECUTED_ENGAGEMENTS = eXECUTED_ENGAGEMENTS;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	@Override
	public String toString() {

		if (device != null)
			return "UserActivities [id=" + id + ", SUBMITTED_STARS=" + SUBMITTED_STARS + ", SUBMITTED_COMMENTS=" + SUBMITTED_COMMENTS + ", SUBMITTED_PHOTOS=" + SUBMITTED_PHOTOS + ", EXECUTED_ENGAGEMENTS=" + EXECUTED_ENGAGEMENTS
					+ ", deviceId="
					+ device.getDeviceId() + "]";
		else
			return "UserActivities [id=" + id + ", SUBMITTED_STARS=" + SUBMITTED_STARS + ", SUBMITTED_COMMENTS=" + SUBMITTED_COMMENTS + ", SUBMITTED_PHOTOS=" + SUBMITTED_PHOTOS + ", EXECUTED_ENGAGEMENTS=" + EXECUTED_ENGAGEMENTS + "]";
	}

	public void add(UserActivities ua) {
		this.EXECUTED_ENGAGEMENTS += ua.getEXECUTED_ENGAGEMENTS();
		this.SUBMITTED_COMMENTS += ua.getSUBMITTED_COMMENTS();
		this.SUBMITTED_PHOTOS += ua.getSUBMITTED_PHOTOS();
		this.SUBMITTED_STARS += ua.getSUBMITTED_STARS();
	}

	public List<UserActivityEntry> toUserActivityObject() {

		List<UserActivityEntry> toreturn = new ArrayList<UserActivityEntry>();

		toreturn.add(new UserActivityEntry("SUBMITTED_STARS", this.SUBMITTED_STARS));
		toreturn.add(new UserActivityEntry("SUBMITTED_PHOTOS", this.SUBMITTED_PHOTOS));
		toreturn.add(new UserActivityEntry("SUBMITTED_COMMENTS", this.SUBMITTED_COMMENTS));
		toreturn.add(new UserActivityEntry("EXECUTED_ENGAGEMENTS", this.EXECUTED_ENGAGEMENTS));
		return toreturn;

	}
}
