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
package edu.unifi.disit.userprofiler.controller;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.unifi.disit.commons.datamodel.userprofiler.Log;
import edu.unifi.disit.userprofiler.exception.LogAlreadyExistsException;
import edu.unifi.disit.userprofiler.exception.LogNotExistsException;
import edu.unifi.disit.userprofiler.exception.OperationNotPermittedException;
import edu.unifi.disit.userprofiler.service.IDeviceLogService;

@SuppressWarnings({ "rawtypes", "unchecked" })
@RestController
public class DeviceLogController {

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	IDeviceLogService logService;

	// -------------------GET Single Log------------------------------------------
	@RequestMapping(value = "/api/v1/device/{deviceId}/logs/{logId}", method = RequestMethod.GET)
	public ResponseEntity<Log> getLogV1(
			@PathVariable("deviceId") String deviceId,
			@PathVariable("logId") Long logId,
			@RequestParam(value = "lang", defaultValue = "en") String lang) {

		logger.debug("Requested GET log for {}, logId {}, lang {}", deviceId, logId, lang);

		Log log = null;

		try {
			log = logService.getLog(deviceId, logId, lang);
		} catch (LogNotExistsException e) {
			return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		if (log == null)
			return new ResponseEntity(HttpStatus.NO_CONTENT);

		return new ResponseEntity<Log>(log, HttpStatus.OK);
	}

	// -------------------GET All Logs------------------------------------------
	@RequestMapping(value = "/api/v1/device/{deviceId}/logs", method = RequestMethod.GET)
	public ResponseEntity<List<Log>> getLogsV1(
			@PathVariable("deviceId") String deviceId,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "size", required = false) Integer size,
			@RequestParam(value = "dataset", required = false) String dataset,
			@RequestParam(value = "valueType", required = false) String valueType,
			@RequestParam(value = "from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date from,
			@RequestParam(value = "to", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date to,
			@RequestParam(value = "lang", defaultValue = "en") String lang) {

		logger.debug("Requested GET logs for {}, lang {}", deviceId, lang);

		// first scenario
		if (page != null)
			logger.debug("page {}", page);
		if (size != null)
			logger.debug("size {}", size);

		// second scenario
		if (dataset != null)
			logger.debug("dataset {}", dataset);
		if (valueType != null)
			logger.debug("valueType {}", valueType);
		if (from != null)
			logger.debug("from {}", from);
		if (to != null)
			logger.debug("to {}", to);

		List<Log> logs = logService.getLogs(deviceId, lang, page, size, dataset, valueType, from, to);

		if (logs == null)
			return new ResponseEntity(HttpStatus.NO_CONTENT);

		return new ResponseEntity<List<Log>>(logs, HttpStatus.OK);
	}

	@RequestMapping(value = "/api/v1/device/{deviceId}/logs/count", method = RequestMethod.GET)
	public ResponseEntity<Integer> countLogsV1(
			@PathVariable("deviceId") String deviceId) {

		logger.debug("Requested GET logs count for {}", deviceId);

		return new ResponseEntity<Integer>(logService.countLogs(deviceId), HttpStatus.OK);
	}

	// -------------------POST Single Log------------------------------------------
	@RequestMapping(value = "/api/v1/device/{deviceId}/logs", method = RequestMethod.POST)
	// create
	public ResponseEntity<Log> postLogV1(
			@PathVariable("deviceId") String deviceId,
			@RequestBody Log log,
			@RequestParam(value = "lang", defaultValue = "en") String lang) {

		logger.debug("Requested POST Log for {}, log {}, lang {}", deviceId, log, lang);

		Log newLog = null;

		try {
			newLog = logService.addLog(deviceId, log, lang);

		} catch (LogAlreadyExistsException | OperationNotPermittedException e) {
			return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<Log>(newLog, HttpStatus.OK);
	}

	// -------------------PUT Single Log------------------------------------------
	// update
	@RequestMapping(value = "/api/v1/device/{deviceId}/logs/{logId}", method = RequestMethod.PUT)
	public ResponseEntity<Log> putLogV1(
			@PathVariable("deviceId") String deviceId,
			@PathVariable("logId") Long logId,
			@RequestBody Log log,
			@RequestParam(value = "lang", defaultValue = "en") String lang) {

		logger.debug("Requested PUT Log for {}, logId {}, log {}, lang {}", deviceId, logId, lang);

		Log newLog = null;

		try {

			if (logId.longValue() != log.getId().longValue()) {
				return new ResponseEntity("the specified id is different from the entity name", HttpStatus.BAD_REQUEST);
			}

			newLog = logService.updateLog(deviceId, log, lang);

		} catch (LogNotExistsException e) {
			return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<Log>(newLog, HttpStatus.OK);
	}

	// -------------------DELETE Single Log------------------------------------------
	@RequestMapping(value = "/api/v1/device/{deviceId}/logs/{logId}", method = RequestMethod.DELETE)
	public ResponseEntity deleteLogV1(
			@PathVariable("deviceId") String deviceId,
			@PathVariable("logId") Long logId,
			@RequestParam(value = "force", defaultValue = "false") Boolean force,
			@RequestParam(value = "lang", defaultValue = "en") String lang) {

		logger.debug("Requested DELETE for {}, logId {}, , lang {}", deviceId, logId, lang);

		try {

			logService.deleteLog(deviceId, logId, force, lang);

		} catch (LogNotExistsException e) {
			return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity(HttpStatus.OK);
	}

	// -------------------DELETE All Logs from same deviceId------------------------------------------
	@RequestMapping(value = "/api/v1/device/{deviceId}/logs", method = RequestMethod.DELETE)
	public ResponseEntity deleteLogsV1(
			@PathVariable("deviceId") String deviceId,
			@RequestParam(value = "dataset", required = false) String dataset,
			@RequestParam(value = "valueType", required = false) String valueType,
			@RequestParam(value = "from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date from,
			@RequestParam(value = "to", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date to,
			@RequestParam(value = "force", defaultValue = "false") Boolean force,
			@RequestParam(value = "lang", defaultValue = "en") String lang) {

		logger.debug("Requested DELETE for {},  lang {}", deviceId, force, lang);

		if (dataset != null)
			logger.debug("dataset {}", dataset);
		if (valueType != null)
			logger.debug("valueType {}", valueType);
		if (from != null)
			logger.debug("from {}", from);
		if (to != null)
			logger.debug("to {}", to);

		logService.deleteLogs(deviceId, lang, dataset, valueType, from, to, force);

		return new ResponseEntity(HttpStatus.OK);
	}
}
