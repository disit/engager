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
package edu.unifi.disit.wallet_user_mngt.service;

import java.util.Date;
import java.util.List;
import java.util.Set;

import edu.unifi.disit.commons.datamodel.userprofiler.Log;
import edu.unifi.disit.wallet_user_mngt.datamodel.Device;
import edu.unifi.disit.wallet_user_mngt.exception.OperationNotPermittedException;
import edu.unifi.disit.wallet_user_mngt.object.MobileApp;

public interface IDeviceService {

	List<MobileApp> getDeviceModelNameForLoggedUser();

	List<Log> getLogs(String deviceId, String dataset, String lang);

	List<Log> getLogs(String deviceId, String dataset, String lang, Integer page, Integer size);

	Log addLog(String deviceId, Log log, String lang) throws OperationNotPermittedException;

	Integer countLogs(String deviceId);

	List<MobileApp> extractModelName(Set<Device> devices);

	List<Log> getLogs(String deviceId, String dataset, String valueType, Date from, Date to, String lang) throws OperationNotPermittedException;

	void deleteLogs(String device, String lang);

	void deleteLogs(String device, String dataset, String lang);

	edu.unifi.disit.commons.datamodel.userprofiler.Device getDevice(String deviceId);

}
