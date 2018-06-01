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

import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.unifi.disit.userprofiler.service.IDeviceTerminalService;

@RestController
public class DeviceTerminalController {

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	IDeviceTerminalService terminalService;

	// --------------------GET ALL Terminal data ------------------------------------------
	@RequestMapping(value = "/api/v1/device/{deviceId}/terminal", method = RequestMethod.GET)
	public ResponseEntity<String> getTerminla(@PathVariable("deviceId") String deviceId,
			@RequestParam(value = "lang", required = false, defaultValue = "en") String lang) {

		logger.debug("Requested all terminal data for {}, lang {}", deviceId, lang);

		return new ResponseEntity<String>("TODO", HttpStatus.NOT_IMPLEMENTED);
	}

	// -------------------POST
	// if we send a terminal_municipality EMPTY, it reset
	// @RequestMapping(value = "/api/v1/device/{deviceId}/terminal", method = RequestMethod.POST)
	// public ResponseEntity<Device> postTerminal(@PathVariable("deviceId") String deviceId,
	// @RequestParam(value = "municipality", required = false) String terminal_municipality,
	// @RequestParam(value = "lang", required = false, defaultValue = "en") String lang) {
	//
	// Device d = null;
	//
	// if (terminal_municipality != null) {
	// logger.debug("Requested POST municipality {} {}, lang {}", terminal_municipality, deviceId, lang);
	// d = terminalService.postTerminalMunicipality(deviceId, terminal_municipality);
	// return new ResponseEntity<Device>(d, HttpStatus.OK);
	// }
	//
	// return new ResponseEntity<Device>(d, HttpStatus.NOT_IMPLEMENTED);
	//
	// }

	// terminal model is
	// String id (hero2lte)
	// String label (Motorola Moto G)
	// -------------------GET Terminal DATA, TERMINAL MODEL ------------------------------------------
	@RequestMapping(value = "/api/v1/device/{deviceId}/terminal/terminalmodel", method = RequestMethod.GET)
	public ResponseEntity<Hashtable<String, String>> getTerminalModelV1(@PathVariable("deviceId") String deviceId,
			@RequestParam(value = "lang", required = false, defaultValue = "en") String lang) {

		logger.debug("Requested terminalmodel data for {}, lang {}", deviceId, lang);

		return new ResponseEntity<Hashtable<String, String>>(terminalService.getTerminalModel(deviceId), HttpStatus.OK);
	}

}
