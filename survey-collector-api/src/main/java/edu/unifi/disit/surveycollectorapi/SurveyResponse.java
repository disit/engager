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
package edu.unifi.disit.surveycollectorapi;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "surveyresponse")
public class SurveyResponse {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	Long id;

	@Column(name = "user_id")
	String user_id;

	@Column(name = "survey_id")
	String survey_id;

	@Column(name = "completed_time")
	@Type(type = "timestamp")
	Date completed_time;

	@Column(name = "survey_response", columnDefinition = "TEXT")
	String survey_response;

	@Column(name = "engagement_id")
	Long engagement_id;

	public SurveyResponse() {
	}

	public SurveyResponse(Long id, String user_id, String survey_id, Date completed_time, String survey_response, Long engagement_id) {
		super();
		this.id = id;
		this.user_id = user_id;
		this.survey_id = survey_id;
		this.completed_time = completed_time;
		this.survey_response = survey_response;
		this.engagement_id = engagement_id;
	}

	public SurveyResponse(String user_id, String survey_id, Date completed_time, String survey_response, Long engagement_id) {
		super();
		this.user_id = user_id;
		this.survey_id = survey_id;
		this.completed_time = completed_time;
		this.survey_response = survey_response;
		this.engagement_id = engagement_id;
	}

	public SurveyResponse(Long id, String user_id, String survey_id, Long completed_time, String survey_response, Long engagement_id) {
		super();
		this.id = id;
		this.user_id = user_id;
		this.survey_id = survey_id;
		this.completed_time = new Date(completed_time);
		this.survey_response = survey_response;
		this.engagement_id = engagement_id;
	}

	public SurveyResponse(String user_id, String survey_id, Long completed_time, String survey_response, Long engagement_id) {
		super();
		this.user_id = user_id;
		this.survey_id = survey_id;
		this.completed_time = new Date(completed_time);
		this.survey_response = survey_response;
		this.engagement_id = engagement_id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getSurvey_id() {
		return survey_id;
	}

	public void setSurvey_id(String survey_id) {
		this.survey_id = survey_id;
	}

	public Date getCompleted_time() {
		return completed_time;
	}

	public void setCompleted_time(Date completed_time) {
		this.completed_time = completed_time;
	}

	public String getSurvey_response() {
		return survey_response;
	}

	public void setSurvey_response(String survey_response) {
		this.survey_response = survey_response;
	}

	public Long getEngagement_id() {
		return engagement_id;
	}

	public void setEngagement_id(Long engagement_id) {
		this.engagement_id = engagement_id;
	}

	@Override
	public String toString() {
		return "SurveyResponse [id=" + id + ", user_id=" + user_id + ", survey_id=" + survey_id + ", completed_time=" + completed_time + ", survey_response=" + survey_response + ", engagement_id=" + engagement_id + "]";
	}
}
