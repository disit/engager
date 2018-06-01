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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.unifi.disit.commons.datamodel.userprofiler.Location;
import edu.unifi.disit.userprofiler.service.IDeviceLocationService;

@SuppressWarnings({ "rawtypes", "unchecked" })
@RestController
public class DeviceLocationController {

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	IDeviceLocationService locationService;

	// -------------------GET Current(last) Locations------------------------------------------
	@RequestMapping(value = "/api/v1/device/{deviceId}/locations/current", method = RequestMethod.GET)
	public ResponseEntity<Location> getCurrentLocationsV1(
			@PathVariable("deviceId") String deviceId,
			@RequestParam(value = "lang", defaultValue = "en") String lang) {

		logger.debug("Requested GET current locations for {}, lang {}", deviceId, lang);

		Location location = locationService.getCurrentLocations(deviceId, lang);

		if (location == null)
			return new ResponseEntity(HttpStatus.NO_CONTENT);

		return new ResponseEntity<Location>(location, HttpStatus.OK);
	}

	// -------------------GET All Locations------------------------------------------
	@RequestMapping(value = "/api/v1/device/{deviceId}/locations", method = RequestMethod.GET)
	public ResponseEntity<List<Location>> getLocationsV1(
			@PathVariable("deviceId") String deviceId,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "size", required = false) Integer size,
			@RequestParam(value = "from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date from,
			@RequestParam(value = "to", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date to,
			@RequestParam(value = "lang", defaultValue = "en") String lang) {

		logger.debug("Requested GET locations for {}, lang {}", deviceId, lang);

		// first scenario
		if (page != null)
			logger.debug("page {}", page);
		if (size != null)
			logger.debug("size {}", size);

		// second scenario
		if (from != null)
			logger.debug("from {}", from);
		if (to != null)
			logger.debug("to {}", to);

		List<Location> locations = locationService.getLocations(deviceId, lang, page, size, from, to);

		if (locations == null)
			return new ResponseEntity(HttpStatus.NO_CONTENT);

		return new ResponseEntity<List<Location>>(locations, HttpStatus.OK);
	}

	@RequestMapping(value = "/api/v1/device/{deviceId}/locations/count", method = RequestMethod.GET)
	public ResponseEntity<Integer> countLogsV1(
			@PathVariable("deviceId") String deviceId) {

		logger.debug("Requested GET location count for {}", deviceId);

		return new ResponseEntity<Integer>(locationService.countLogs(deviceId), HttpStatus.OK);
	}
}
