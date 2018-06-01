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
@Table(name = "response")
public class Response {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	Long id;

	@Column(name = "userId")
	String userId;

	@Column(name = "action_class")
	String action_class;

	@Column(name = "action_type")
	String action_type;

	@Column(name = "action_title")
	String action_title;

	@Column(name = "action_msg")
	String action_msg;

	@Column(name = "action_uri")
	String action_uri;

	@Column(name = "gps_lat")
	Double gps_lat;

	@Column(name = "gps_long")
	Double gps_long;

	@Column(name = "status")
	String status;

	@Column(name = "timeCreated")
	@Type(type = "timestamp")
	private Date timeCreated;

	@Column(name = "timeElapsed")
	@Type(type = "timestamp")
	private Date timeElapsed;

	@Column(name = "service_type")
	String service_type;

	@Column(name = "service_label")
	String service_label;

	@Column(name = "service_name")
	String service_name;

	@Column(name = "send_rate")
	Long send_rate;

	@Column(name = "how_many")
	Long how_many;

	@Column(name = "rule_name")
	String rule_name;

	@Column(name = "timeSend")
	@Type(type = "timestamp")
	private Date timeSend;

	@Column(name = "timeViewed")
	@Type(type = "timestamp")
	private Date timeViewed;

	@Column(name = "isAssessor")
	private Boolean isAssessor;

	@Column(name = "points")
	private Integer points;

	public Response() {
	}

	public Response(Long id, String userId, String action_class, String action_type, String action_title, String action_msg, String action_uri, Double gps_lat, Double gps_long, String status, Date timeCreated, Date timeElapsed,
			String service_type, String service_label, String service_name, Long send_rate, Long how_many, String rule_name, Date timeSend, Date timeViewed, Boolean isAssessor, Integer points) {
		this.id = id;
		this.userId = userId;
		this.action_class = action_class;
		this.action_type = action_type;
		this.action_title = action_title;
		this.action_msg = action_msg;
		this.action_uri = action_uri;
		this.gps_lat = gps_lat;
		this.gps_long = gps_long;
		this.status = status;
		this.timeCreated = timeCreated;
		this.timeElapsed = timeElapsed;
		this.service_type = service_type;
		this.service_label = service_label;
		this.service_name = service_name;
		this.send_rate = send_rate;
		this.how_many = how_many;
		this.rule_name = rule_name;
		this.timeSend = timeSend;
		this.timeViewed = timeViewed;
		this.isAssessor = isAssessor;
		this.points = points;
	}

	public Response(String userId, String action_class, String action_type, String action_title, String action_msg, String action_uri, Double gps_lat, Double gps_long, String status, Date timeCreated, Date timeElapsed, String service_type,
			String service_label, String service_name, Long send_rate, Long how_many, String rule_name, Date timeSend, Date timeViewed, Boolean isAssessor, Integer points) {
		this.userId = userId;
		this.action_class = action_class;
		this.action_type = action_type;
		this.action_title = action_title;
		this.action_msg = action_msg;
		this.action_uri = action_uri;
		this.gps_lat = gps_lat;
		this.gps_long = gps_long;
		this.status = status;
		this.timeCreated = timeCreated;
		this.timeElapsed = timeElapsed;
		this.service_type = service_type;
		this.service_label = service_label;
		this.service_name = service_name;
		this.send_rate = send_rate;
		this.how_many = how_many;
		this.rule_name = rule_name;
		this.timeSend = timeSend;
		this.timeViewed = timeViewed;
		this.isAssessor = isAssessor;
		this.points = points;
	}

	public String getService_name() {
		return service_name;
	}

	public void setService_name(String service_name) {
		this.service_name = service_name;
	}

	public String getAction_title() {
		return action_title;
	}

	public void setAction_title(String action_title) {
		this.action_title = action_title;
	}

	public String getAction_uri() {
		return action_uri;
	}

	public void setAction_uri(String action_uri) {
		this.action_uri = action_uri;
	}

	public Double getGps_lat() {
		return gps_lat;
	}

	public void setGps_lat(Double gps_lat) {
		this.gps_lat = gps_lat;
	}

	public Double getGps_long() {
		return gps_long;
	}

	public void setGps_long(Double gps_long) {
		this.gps_long = gps_long;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAction_class() {
		return action_class;
	}

	public void setAction_class(String action_class) {
		this.action_class = action_class;
	}

	public String getAction_type() {
		return action_type;
	}

	public void setAction_type(String action_type) {
		this.action_type = action_type;
	}

	public String getAction_msg() {
		return action_msg;
	}

	public void setAction_msg(String action_msg) {
		this.action_msg = action_msg;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(Date timeCreated) {
		this.timeCreated = timeCreated;
	}

	public Date getTimeElapsed() {
		return timeElapsed;
	}

	public void setTimeElapsed(Date timeElapsed) {
		this.timeElapsed = timeElapsed;
	}

	public String getService_type() {
		return service_type;
	}

	public void setService_type(String service_type) {
		this.service_type = service_type;
	}

	public String getService_label() {
		return service_label;
	}

	public void setService_label(String service_label) {
		this.service_label = service_label;
	}

	public Long getSend_rate() {
		return send_rate;
	}

	public void setSend_rate(Long send_rate) {
		this.send_rate = send_rate;
	}

	public Long getHow_many() {
		return how_many;
	}

	public void setHow_many(Long how_many) {
		this.how_many = how_many;
	}

	public String getRule_name() {
		return rule_name;
	}

	public void setRule_name(String rule_name) {
		this.rule_name = rule_name;
	}

	public Date getTimeSend() {
		return timeSend;
	}

	public void setTimeSend(Date timeSend) {
		this.timeSend = timeSend;
	}

	public Date getTimeViewed() {
		return timeViewed;
	}

	public void setTimeViewed(Date timeViewed) {
		this.timeViewed = timeViewed;
	}

	public Boolean getIsAssessor() {
		return isAssessor;
	}

	public void setIsAssessor(Boolean isAssessor) {
		this.isAssessor = isAssessor;
	}

	public Integer getPoints() {
		return points;
	}

	public void setPoints(Integer points) {
		this.points = points;
	}

	@Override
	public String toString() {
		return "Response [id=" + id + ", userId=" + userId + ", action_class=" + action_class + ", action_type=" + action_type + ", action_title=" + action_title + ", action_msg=" + action_msg + ", action_uri=" + action_uri + ", gps_lat="
				+ gps_lat + ", gps_long=" + gps_long + ", status=" + status + ", timeCreated=" + timeCreated + ", timeElapsed=" + timeElapsed + ", service_type=" + service_type + ", service_label=" + service_label + ", service_name="
				+ service_name + ", send_rate=" + send_rate + ", how_many=" + how_many + ", rule_name=" + rule_name + ", timeSend=" + timeSend + ", timeViewed=" + timeViewed + ", isAssessor=" + isAssessor + ", points=" + points + "]";
	}
}
