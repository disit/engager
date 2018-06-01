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

import edu.unifi.disit.commons.datamodel.userprofiler.Timeline;
import edu.unifi.disit.commons.utils.Utils;

public class AggregatedMobility {

	String status;
	Long seconds;
	Long meters;

	public AggregatedMobility() {
		super();
	}

	public AggregatedMobility(String status, Long seconds, Long meters) {
		this.status = status;
		this.seconds = seconds;
		this.meters = meters;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getSeconds() {
		return seconds;
	}

	public void setSeconds(Long seconds) {
		this.seconds = seconds;
	}

	public Long getMeters() {
		return meters;
	}

	public void setMeters(Long meters) {
		this.meters = meters;
	}

	@Override
	public String toString() {
		return "AggregatedMobility [status=" + status + ", seconds=" + seconds + ", meters=" + meters + "]";
	}

	public void add(Timeline am) {
		this.seconds = this.seconds + am.getSeconds();
		this.meters = this.meters + am.getMeters();

	}

	public String getSecondsLabel() {
		return Utils.convertSecondsToString(this.seconds);
	}

	public void setSecondsLabel(String sLabel) {
		// TODO convert from string to seconds
	}

	public String getMetersLabel() {
		return Utils.convertMetersToString(this.meters);
	}

	public void setMetersLabel(String sLabel) {
		// TODO convert from string to seconds
	}

}
