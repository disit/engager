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

import edu.unifi.disit.userprofiler.ppois.markov.MarkovNetwork;
import edu.unifi.disit.userprofiler.ppois.markov.Markowinterface;

@SuppressWarnings({ "rawtypes", "unchecked" })
@RestController
public class DeviceTripsController {

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	Markowinterface mi;

	// -------------------GET trips for ------------------------------------------
	@RequestMapping(value = "/api/v1/device/{deviceId}/trips/aggregated", method = RequestMethod.GET)
	public ResponseEntity<MarkovNetwork> getCurrentLocationsV1(
			@PathVariable("deviceId") String deviceId,
			@RequestParam(value = "lang", defaultValue = "en") String lang) {

		logger.debug("Requested GET past aggregated trips for {}, lang {}", deviceId, lang);

		MarkovNetwork mn = mi.get(deviceId);

		if (mn == null)
			return new ResponseEntity(HttpStatus.NO_CONTENT);

		return new ResponseEntity<MarkovNetwork>(mn, HttpStatus.OK);
	}

}
