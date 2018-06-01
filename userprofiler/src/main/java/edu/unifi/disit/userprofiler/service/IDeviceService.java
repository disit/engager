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
package edu.unifi.disit.userprofiler.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.unifi.disit.commons.datamodel.userprofiler.Device;
import edu.unifi.disit.commons.datamodel.userprofiler.Timeline;

@Service
public interface IDeviceService {

	List<Device> getAllDevices(String lang);

	Device getCachedDevice(String deviceId, String lang);

	Device getDevice(String deviceId, String lang);

	Device updateTerminalInfo(Device d);

	Device updateLocationDevice(String deviceId, Double latitude, Double longitude, Long when, String mobility_mode, Double speed, String profile,
			String terminal_lang, Double accuracy, String provider, Double mean_speed, Double acc_magn, Double acc_x, Double acc_y, Double acc_z);

	void updateInterestDevice(String deviceId, String serviceUri, Integer rate, String type);

	void updateTime(String deviceId, String ppoiName, Boolean confirmation, Long time);

	void scaricoTimeline(Timeline t);// TO USED TEST
}
