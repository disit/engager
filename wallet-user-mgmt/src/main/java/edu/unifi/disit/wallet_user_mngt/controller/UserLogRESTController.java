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
package edu.unifi.disit.wallet_user_mngt.controller;

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
import edu.unifi.disit.wallet_user_mngt.exception.OperationNotPermittedException;
import edu.unifi.disit.wallet_user_mngt.object.Response;
import edu.unifi.disit.wallet_user_mngt.service.IDeviceService;

@RestController
public class UserLogRESTController {

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	IDeviceService deviceService;

	// -------------------POST Single Data------------------------------------------
	@RequestMapping(value = "/api/v1/device/{deviceId}/data", method = RequestMethod.POST)
	// create
	public ResponseEntity<Object> postDataV1(
			@PathVariable("deviceId") String deviceId,
			@RequestBody Log log,
			@RequestParam(value = "lang", defaultValue = "en") String lang) {

		logger.debug("Requested POST Log for {}, log {}, lang {}", deviceId, log, lang);

		Log newLog;
		try {
			newLog = deviceService.addLog(deviceId, log, lang);
		} catch (OperationNotPermittedException e) {
			Response r = new Response();
			r.setResult(false);
			r.setMessage(e.getMessage());
			return new ResponseEntity<Object>(r, HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<Object>(newLog, HttpStatus.OK);
	}

	// -------------------Get Data------------------------------------------
	@RequestMapping(value = "/api/v1/device/{deviceId}/data", method = RequestMethod.GET)
	// create
	public ResponseEntity<Object> getDataV1(
			@PathVariable("deviceId") String deviceId,
			@RequestParam(value = "dataset", required = true) String dataset,
			@RequestParam(value = "valueType", required = false) String valueType,
			@RequestParam(value = "from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date from,
			@RequestParam(value = "to", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date to,
			@RequestParam(value = "lang", defaultValue = "en") String lang) {

		logger.debug("Requested GET Log for {}, log {}, lang {}", deviceId, dataset, lang);

		if (valueType != null)
			logger.debug("valueType {}", valueType);

		if (from != null)
			logger.debug("from {}", from);

		if (to != null)
			logger.debug("to {}", to);

		List<Log> logs;
		try {
			logs = deviceService.getLogs(deviceId, dataset, valueType, from, to, lang);
		} catch (OperationNotPermittedException e) {
			Response r = new Response();
			r.setResult(false);
			r.setMessage(e.getMessage());
			return new ResponseEntity<Object>(r, HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<Object>(logs, HttpStatus.OK);
	}

}
