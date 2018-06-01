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
@Table(name = "Timeline")
public class Timeline extends TimelineAbs {

	public TimelineCurrent toTimelineCurrent() {
		TimelineCurrent tlc = new TimelineCurrent();
		// no update id
		tlc.setDevice(this.device);
		tlc.setDate(this.date);
		tlc.setStatus(this.status);
		tlc.setSeconds(this.seconds);
		tlc.setMeters(this.meters);
		tlc.setLatitude(this.latitude);
		tlc.setLongitude(this.longitude);
		return tlc;
	}
}
