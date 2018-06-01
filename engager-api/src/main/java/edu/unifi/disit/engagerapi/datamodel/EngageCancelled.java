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
package edu.unifi.disit.engagerapi.datamodel;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "engage_cancelled")
public class EngageCancelled {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	Long id;

	@Column(name = "response_id")
	private Long responseId;

	@Column(name = "time")
	@Type(type = "timestamp")
	private Date time;

	@Column(name = "user_id")
	private String userId;

	public EngageCancelled() {
	}

	public EngageCancelled(Long responseId, String userId) {
		this(responseId, new Date(), userId);
	}

	public EngageCancelled(Long responseId, Date time, String userId) {
		super();
		this.responseId = responseId;
		this.time = time;
		this.userId = userId;
	}

	public EngageCancelled(Long id, Long responseId, Date time, String userId) {
		super();
		this.id = id;
		this.responseId = responseId;
		this.time = time;
		this.userId = userId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getResponseId() {
		return responseId;
	}

	public void setResponseId(Long responseId) {
		this.responseId = responseId;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "EngageCancelled [id=" + id + ", responseId=" + responseId + ", time=" + time + ", userId=" + userId + "]";
	}
}
