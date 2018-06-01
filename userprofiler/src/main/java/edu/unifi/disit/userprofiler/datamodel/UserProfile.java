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
package edu.unifi.disit.userprofiler.datamodel;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import edu.unifi.disit.commons.datamodel.userprofiler.Device;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfile {

	String email;

	List<Device> devices;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<Device> getDevices() {
		return devices;
	}

	public void setDevices(List<Device> devices) {
		this.devices = devices;
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		return "UserProfile [email=" + email + ", devices=" + (devices != null ? devices.subList(0, Math.min(devices.size(), maxLen)) : null) + "]";
	}
}
