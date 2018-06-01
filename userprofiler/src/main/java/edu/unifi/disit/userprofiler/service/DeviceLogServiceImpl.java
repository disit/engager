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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import edu.unifi.disit.commons.datamodel.DatasetType;
import edu.unifi.disit.commons.datamodel.userprofiler.Device;
import edu.unifi.disit.commons.datamodel.userprofiler.Log;
import edu.unifi.disit.userprofiler.datamodel.DeviceDAO;
import edu.unifi.disit.userprofiler.datamodel.LogDAO;
import edu.unifi.disit.userprofiler.exception.LogAlreadyExistsException;
import edu.unifi.disit.userprofiler.exception.LogNotExistsException;
import edu.unifi.disit.userprofiler.exception.OperationNotPermittedException;

@Component
public class DeviceLogServiceImpl implements IDeviceLogService {

	@Autowired
	LogDAO logRepo;

	@Autowired
	DeviceDAO deviceRepo;

	@Autowired
	IDeviceService deviceService;

	@Value("${log.timetodelete.days}")
	private Integer daysToDelete;

	@Override
	public Log getLog(String deviceId, Long logId, String lang) throws LogNotExistsException {
		Log log = logRepo.findByIdAndDeleteTimeIsNull(logId);

		if ((log != null) && (log.getDevice() != null) && (log.getDevice().getDeviceId() != deviceId)) {
			throw new LogNotExistsException("no logId exist for the passed deviceId");
		}
		return log;

	}

	@Override
	public List<Log> getLogs(String deviceId, String lang, Integer page, Integer size, String dataset, String valueType, Date from, Date to) {

		DatasetType d = DatasetType.findBy(dataset);

		if ((page == null) || (size == null)) {
			page = 0;
			size = countLogs(deviceId);
		}
		if (from == null) {
			from = new Date(0);
		}
		if (to == null) {
			to = new Date(4102448400000l);// to return the logs we check the logTime. Since the logTime can be in the future, we need to put an higher upper bound (2100)
		}

		if (d != null) {
			if (valueType != null) {
				return logRepo.findByDeviceDeviceIdAndValueTypeAndDatasetAndDataTimeAfterAndDataTimeBeforeAndDeleteTimeIsNull(deviceId, valueType, d, from, to, new PageRequest(page, size)).getContent();
			} else {
				return logRepo.findByDeviceDeviceIdAndDatasetAndDataTimeAfterAndDataTimeBeforeAndDeleteTimeIsNull(deviceId, d, from, to, new PageRequest(page, size)).getContent();
			}
		} else {
			if (valueType != null) {
				return logRepo.findByDeviceDeviceIdAndValueTypeAndDataTimeAfterAndDataTimeBeforeAndDeleteTimeIsNull(deviceId, valueType, from, to, new PageRequest(page, size)).getContent();
			} else {
				return logRepo.findByDeviceDeviceIdAndDataTimeAfterAndDataTimeBeforeAndDeleteTimeIsNull(deviceId, from, to, new PageRequest(page, size)).getContent();
			}
		}
	}

	@Override
	public Integer countLogs(String deviceId) {

		return logRepo.countByDeviceDeviceIdAndDeleteTimeIsNull(deviceId);
	}

	// we cannot add a new Log if another one is already present (even if is in DELETING phase)
	@Override
	public Log addLog(String deviceId, Log log, String lang) throws LogAlreadyExistsException, OperationNotPermittedException {

		if (logRepo.findById(log.getId()) != null)
			throw new LogAlreadyExistsException("there is already a log with the specified id. please set the id to null for a fresh insert");

		// retrieve the deviceId
		Device device = deviceService.getDevice(deviceId, lang);// deviceRepo.findByDeviceId(deviceId);
		if (device == null)
			throw new OperationNotPermittedException("thespecified deviceId does not exist");

		log.setDevice(device);

		log.setInsertTime(new Date());

		return logRepo.save(log);
	}

	// we update a Log if (a) exist AND (b) is in Not DELETING phase
	@Override
	public Log updateLog(String deviceId, Log log, String lang) throws LogNotExistsException {

		if ((logRepo.findByIdAndDeleteTimeIsNull(log.getId()) != null))
			return logRepo.save(log);
		else
			throw new LogNotExistsException("there is no log with the specified id");

	}

	@Override
	public void deleteLog(String deviceId, Long logId, Boolean force, String lang) throws LogNotExistsException {
		Log log = null;
		if ((log = logRepo.findByIdAndDeleteTimeIsNull(logId)) == null)
			throw new LogNotExistsException("there is no log with the specified id");
		if (force)
			logRepo.delete(log);
		else {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, daysToDelete);
			log.setDeleteTime(new Date(c.getTimeInMillis()));
			logRepo.save(log);
		}
	}

	@Override
	public void deleteLogs(String deviceId, String lang, String dataset, String valueType, Date from, Date to, Boolean force) {
		List<Log> logs = getLogs(deviceId, lang, null, null, dataset, valueType, from, to);
		if (force)
			logRepo.delete(logs);
		else {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, daysToDelete);
			for (Log log : logs) {
				log.setDeleteTime(new Date(c.getTimeInMillis()));
			}
			logRepo.save(logs);
		}
	}

}
