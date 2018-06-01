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
@Table(name = "user_service_interest")
public class UserServiceInterest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	Long id;

	@Column(name = "last_time")
	@Type(type = "timestamp")
	private Date lastTime;

	@Column(name = "user_id")
	private String userId;

	@Column(name = "service_uri")
	private String serviceUri;

	@Column(name = "rating")
	private Integer rating;

	@Column(name = "ppoi_type")
	private String ppoiType;

	public UserServiceInterest() {
	}

	public UserServiceInterest(String userId, String serviceUri, String ppoiType, Integer rating) {
		super();
		this.lastTime = new Date();
		this.userId = userId;
		this.serviceUri = serviceUri;
		this.rating = rating;
		this.ppoiType = ppoiType;
	}

	public UserServiceInterest(Long id, Date lastTime, String userId, String serviceUri, Integer rating, String ppoiType) {
		super();
		this.id = id;
		this.lastTime = lastTime;
		this.userId = userId;
		this.serviceUri = serviceUri;
		this.rating = rating;
		this.ppoiType = ppoiType;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getLastTime() {
		return lastTime;
	}

	public void setLastTime(Date lastTime) {
		this.lastTime = lastTime;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getServiceUri() {
		return serviceUri;
	}

	public void setServiceUri(String serviceUri) {
		this.serviceUri = serviceUri;
	}

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	public String getPpoiType() {
		return ppoiType;
	}

	public void setPpoiType(String ppoiType) {
		this.ppoiType = ppoiType;
	}

	@Override
	public String toString() {
		return "UserServiceInterest [id=" + id + ", lastTime=" + lastTime + ", userId=" + userId + ", serviceUri=" + serviceUri + ", rating=" + rating + ", ppoiType=" + ppoiType + "]";
	}
}
