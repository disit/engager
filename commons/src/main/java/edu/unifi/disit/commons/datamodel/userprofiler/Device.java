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

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import edu.unifi.disit.commons.datamodel.PPOI;
import edu.unifi.disit.commons.datamodel.Prediction;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
public class Device {

	@Id
	// @GeneratedValue(strategy = GenerationType.IDENTITY)
	String deviceId;

	// -----------------------------------------TERMINAL INFO
	Date lastUpdate = null;
	Double currentPositionLat = null;// updated live
	Double currentPositionLong = null;// updated live
	Double currentPositionAccuracy = null;// updated live
	String terminalAppID = null;
	String terminalModel = null;
	String terminalVersion = null;
	Date terminalInstallationDate = null;
	String terminal_profile = null;// TODO TO MOVE in user profile
	Locale terminal_language = null;// TODO TO MOVE in user profile
	// String terminal_municipality = null;
	// String terminal_province = null;

	Boolean isAssessor = false;

	// -----------------------------------------USER ACTIVITIES
	// default is FetchType.EAGER
	@OneToOne(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
	UserActivities userActivities;

	// ----------------------------------------------------PPOIS
	@Transient
	List<PPOI> ppois = null;
	String ppoiPrevious;
	String ppoiCurrent;
	@Transient
	Prediction ppoiNext = null;
	Long ppoiPreviousHowlong = null;
	Double ppoiPreviousDistance = null;
	Double averageSpeed = null;

	// ----------------------------------- current timeline
	// default is FetchType.EAGER
	@OneToOne(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
	TimelineCurrent timelineCurrent = null;

	@Transient
	Timeline timelinePrevious = null;

	// OTHER
	@ElementCollection(fetch = FetchType.EAGER)
	Set<String> groups = null;

	@JsonIgnore
	@OneToMany(mappedBy = "device", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, orphanRemoval = true)
	Set<Log> logs = null;

	@JsonIgnore
	@OneToMany(mappedBy = "device", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, orphanRemoval = true)
	Set<Location> location = null;

	public Timeline getTimelinePrevious() {
		return timelinePrevious;
	}

	public void setTimelinePrevious(Timeline timelinePrevious) {
		this.timelinePrevious = timelinePrevious;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Double getCurrentPositionLat() {
		return currentPositionLat;
	}

	public void setCurrentPositionLat(Double currentPositionLat) {
		this.currentPositionLat = currentPositionLat;
	}

	public Double getCurrentPositionLong() {
		return currentPositionLong;
	}

	public void setCurrentPositionLong(Double currentPositionLong) {
		this.currentPositionLong = currentPositionLong;
	}

	public Double getCurrentPositionAccuracy() {
		return currentPositionAccuracy;
	}

	public void setCurrentPositionAccuracy(Double currentPositionAccuracy) {
		this.currentPositionAccuracy = currentPositionAccuracy;
	}

	public String getTerminalModel() {
		return terminalModel;
	}

	public void setTerminalModel(String terminalModel) {
		this.terminalModel = terminalModel;
	}

	public String getTerminalAppID() {
		return terminalAppID;
	}

	public void setTerminalAppID(String terminalAppID) {
		this.terminalAppID = terminalAppID;
	}

	public String getTerminalVersion() {
		return terminalVersion;
	}

	public void setTerminalVersion(String terminalVersion) {
		this.terminalVersion = terminalVersion;
	}

	public Date getTerminalInstallationDate() {
		return terminalInstallationDate;
	}

	public void setTerminalInstallationDate(Date terminalInstallationDate) {
		this.terminalInstallationDate = terminalInstallationDate;
	}

	public String getTerminal_profile() {
		return terminal_profile;
	}

	public void setTerminal_profile(String terminal_profile) {
		this.terminal_profile = terminal_profile;
	}

	public Locale getTerminal_language() {
		return terminal_language;
	}

	public void setTerminal_language(Locale terminal_language) {
		this.terminal_language = terminal_language;
	}

	public UserActivities getUserActivities() {
		return userActivities;
	}

	public void setUserActivities(UserActivities userActivities) {
		this.userActivities = userActivities;

		userActivities.setDevice(this);
	}

	public List<PPOI> getPpois() {
		return ppois;
	}

	public void setPpois(List<PPOI> ppois) {
		this.ppois = ppois;
	}

	public String getPpoiPrevious() {
		return ppoiPrevious;
	}

	public void setPpoiPrevious(String ppoiPrevious) {
		this.ppoiPrevious = ppoiPrevious;
	}

	public String getPpoiCurrent() {
		return ppoiCurrent;
	}

	public void setPpoiCurrent(String ppoiCurrent) {
		this.ppoiCurrent = ppoiCurrent;
	}

	public Prediction getPpoiNext() {
		return ppoiNext;
	}

	public void setPpoiNext(Prediction ppoiNext) {
		this.ppoiNext = ppoiNext;
	}

	public Long getPpoiPreviousHowlong() {
		return ppoiPreviousHowlong;
	}

	public void setPpoiPreviousHowlong(Long ppoiPreviousHowlong) {
		this.ppoiPreviousHowlong = ppoiPreviousHowlong;
	}

	public Double getPpoiPreviousDistance() {
		return ppoiPreviousDistance;
	}

	public void setPpoiPreviousDistance(Double ppoiPreviousDistance) {
		this.ppoiPreviousDistance = ppoiPreviousDistance;
	}

	public Double getAverageSpeed() {
		return averageSpeed;
	}

	public void setAverageSpeed(Double averageSpeed) {
		this.averageSpeed = averageSpeed;
	}

	public TimelineCurrent getTimelineCurrent() {
		return timelineCurrent;
	}

	public void setTimelineCurrent(TimelineCurrent timelineCurrent) {
		this.timelineCurrent = timelineCurrent;
	}

	public Boolean getIsAssessor() {
		return isAssessor;
	}

	public void setIsAssessor(Boolean isAssessor) {
		this.isAssessor = isAssessor;
	}

	public Set<String> getGroups() {
		return groups;
	}

	public void setGroups(Set<String> groups) {
		this.groups = groups;
	}

	// public String getTerminal_municipality() {
	// return terminal_municipality;
	// }
	//
	// public void setTerminal_municipality(String terminal_municipality) {
	// this.terminal_municipality = terminal_municipality;
	// }
	//
	// public String getTerminal_province() {
	// return terminal_province;
	// }
	//
	// public void setTerminal_province(String terminal_province) {
	// this.terminal_province = terminal_province;
	// }

	public Set<Log> getLogs() {
		return logs;
	}

	public void setLogs(Set<Log> logs) {
		this.logs = logs;
	}

	@Override
	public String toString() {

		String buf = "Device [deviceId=" + deviceId;

		if (lastUpdate != null)
			buf = buf + ", lastUpdate=" + lastUpdate;

		if (currentPositionLat != null)
			buf = buf + ", currentPositionLat=" + currentPositionLat;

		if (currentPositionLong != null)
			buf = buf + ", currentPositionLong=" + currentPositionLong;

		if (currentPositionAccuracy != null)
			buf = buf + ", currentPositionAccuracy=" + currentPositionAccuracy;

		if (terminalAppID != null)
			buf = buf + ", terminalAppID=" + terminalAppID;

		if (terminalModel != null)
			buf = buf + ", terminalModel=" + terminalModel;

		if (terminalVersion != null)
			buf = buf + ", terminalVersion=" + terminalVersion;

		if (terminalInstallationDate != null)
			buf = buf + ", terminalInstallationDate=" + terminalInstallationDate;

		if (terminal_profile != null)
			buf = buf + ", terminal_profile=" + terminal_profile;

		if (terminal_language != null)
			buf = buf + ", terminal_language=" + terminal_language;

		// if (terminal_municipality != null)
		// buf = buf + ", terminal_municipality=" + terminal_municipality;
		//
		// if (terminal_province != null)
		// buf = buf + ", terminal_province=" + terminal_province;

		if (ppois != null)
			buf = buf + ", ppois=" + ppois;

		if (ppoiPrevious != null)
			buf = buf + ", ppoiPrevious=" + ppoiPrevious;

		if (ppoiCurrent != null)
			buf = buf + ", ppoiCurrent=" + ppoiCurrent;

		if (ppoiNext != null)
			buf = buf + ", ppoiNext=" + ppoiNext;

		if (ppoiPreviousHowlong != null)
			buf = buf + ", ppoiPreviousHowlong=" + ppoiPreviousHowlong;

		if (ppoiPreviousDistance != null)
			buf = buf + ", ppoiPreviousDistance=" + ppoiPreviousDistance;

		if (averageSpeed != null)
			buf = buf + ", averageSpeed=" + averageSpeed;

		if (isAssessor != null)
			buf = buf + ", isAssessor=" + isAssessor;

		if (userActivities != null)
			buf = buf + ", userActivities=" + userActivities;

		if (timelineCurrent != null)
			buf = buf + ", timelineCurrent=" + timelineCurrent;

		if (timelinePrevious != null)
			buf = buf + ", timelinePrevious=" + timelinePrevious;

		if ((groups != null) && (groups.size() > 0)) {
			buf = buf + ", groups=";
			for (String s : groups) {
				buf = buf + s + ",";
			}
			buf = buf.substring(0, buf.length() - 1);
		}

		return buf + "]";
	}
}
