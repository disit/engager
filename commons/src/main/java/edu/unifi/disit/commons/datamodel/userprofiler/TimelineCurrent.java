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

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "TimelineCurent")
public class TimelineCurrent extends TimelineAbs {

	public TimelineCurrent(Device device, String mobility_mode, Long when) {
		super();
		setDevice(device);
		setDateTimestamp(when);
		setStatus(mobility_mode);
	}

	public TimelineCurrent() {
		super();
	}

	public Timeline toTimeline() {
		Timeline tl = new Timeline();
		// no update id
		tl.setDevice(this.device);
		tl.setDate(this.date);
		tl.setStatus(this.status);
		tl.setSeconds(this.seconds);
		tl.setMeters(this.meters);
		tl.setLatitude(this.latitude);
		tl.setLongitude(this.longitude);
		return tl;
	}
}
