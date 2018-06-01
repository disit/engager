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

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import edu.unifi.disit.commons.datamodel.userprofiler.Log;
import edu.unifi.disit.userprofiler.exception.LogAlreadyExistsException;
import edu.unifi.disit.userprofiler.exception.LogNotExistsException;
import edu.unifi.disit.userprofiler.exception.OperationNotPermittedException;

@Service
public interface IDeviceLogService {

	Log getLog(String deviceId, Long logId, String lang) throws LogNotExistsException;

	List<Log> getLogs(String deviceId, String lang, Integer startId, Integer endId, String dataset, String valueType, Date from, Date to);

	Log addLog(String deviceId, Log log, String lang) throws LogAlreadyExistsException, OperationNotPermittedException;

	Log updateLog(String deviceId, Log log, String lang) throws LogNotExistsException;

	void deleteLog(String deviceId, Long logId, Boolean force, String lang) throws LogNotExistsException;

	void deleteLogs(String deviceId, String lang, String dataset, String valueType, Date from, Date to, Boolean force);

	Integer countLogs(String deviceId);
}
