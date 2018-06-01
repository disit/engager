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
package edu.unifi.disit.commons.datamodel.engager;

import java.util.Date;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "engage_executed")
public class EngageExecuted {

	@JsonIgnore
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Long id;

	@JsonIgnore
	@Column(name = "response_id")
	public Long responseId;

	@Column(name = "time")
	@Type(type = "timestamp")
	public Date time;

	@Column(name = "time_insert")
	@Type(type = "timestamp")
	public Date timeInsert;

	@JsonIgnore
	@Column(name = "user_id")
	public String userId;

	@Column(name = "rule_name")
	public String ruleName;

	// 0- responseid
	// 1- date
	// 2- userid
	// 3- rulename
	// 4- time inserted
	public EngageExecuted(Object[] o) {
		this(((BigInteger) o[0]).longValue(), (Date) o[1], (String) o[2], (String) o[3], (Date) o[4]);
	}

	
	public EngageExecuted() {
	}
	//
	// public EngageExecuted(Long responseId, String userId, String ruleName) {
	// this(responseId, new Date(), userId, ruleName);
	// }

	public EngageExecuted(Long responseId, Date time, String userId, String ruleName) {
		this(responseId, time, userId, ruleName, new Date());

	}

	public EngageExecuted(Long responseId, Date time, String userId, String ruleName, Date timeInsert) {
		super();
		this.responseId = responseId;
		this.time = time;
		this.userId = userId;
		this.ruleName = ruleName;
		this.timeInsert = timeInsert;
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

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public Date getTimeInsert() {
		return timeInsert;
	}

	public void setTimeInsert(Date timeInsert) {
		this.timeInsert = timeInsert;
	}

	@Override
	public String toString() {
		return "EngageExecuted [id=" + id + ", responseId=" + responseId + ", time=" + time + ", timeInsert=" + timeInsert + ", userId=" + userId + ", ruleName=" + ruleName + "]";
	}

}
