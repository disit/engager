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

@Entity
@Table(name = "time_last_sent")
public class TimeLastSent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	@Column(name = "userId")
	private String userId;

	@Column(name = "ruleName")
	private String ruleName;

	@Column(name = "timeLastSent")
	private Date timeLastSent;

	public TimeLastSent() {
		super();
	}

	public TimeLastSent(String userId, String ruleName, Date timeLastSent) {
		this.userId = userId;
		this.ruleName = ruleName;
		this.timeLastSent = timeLastSent;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Date getTimeLastSent() {
		return timeLastSent;
	}

	public void setTimeLastSent(Date timeLastSent) {
		this.timeLastSent = timeLastSent;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	@Override
	public String toString() {
		return "TimeLastSent [id=" + id + ", userId=" + userId + ", timeLastSent=" + timeLastSent + ", ruleName=" + ruleName + "]";
	}
}
