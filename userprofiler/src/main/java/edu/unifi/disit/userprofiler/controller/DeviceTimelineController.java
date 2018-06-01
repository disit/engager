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

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

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

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.unifi.disit.commons.datamodel.AggregatedMobility;
import edu.unifi.disit.commons.datamodel.Position;
import edu.unifi.disit.commons.datamodel.userprofiler.Timeline;
import edu.unifi.disit.userprofiler.exception.OperationNotPermittedException;
import edu.unifi.disit.userprofiler.service.IDeviceTimelineService;

@RestController
public class DeviceTimelineController {

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	IDeviceTimelineService deviceTService;

	// -------------------GET Timeline---------------------------------------------
	@RequestMapping(value = "/api/v1/device/{deviceId}/timeline", method = RequestMethod.GET)
	public ResponseEntity<List<Timeline>> getTimelineV1(@PathVariable("deviceId") String deviceId,
			@RequestParam(value = "from", required = false) Long from,
			@RequestParam(value = "to", required = false) Long to,
			@RequestParam(value = "last_status", required = false) String lastStatus,
			@RequestParam(value = "min_time", required = false) Long minTime, // minutes
			@RequestParam(value = "lang", required = false, defaultValue = "en") String lang) throws OperationNotPermittedException {

		logger.debug("Requested Timelines for {}, from {}, to {},  lang {}", deviceId, from, to, lang);
		if (lastStatus != null) {
			logger.debug("Last status is {}", lastStatus);
		}

		if (minTime != null) {
			logger.debug("Min time is {}", minTime);
		}

		List<Timeline> ams = deviceTService.getTimeline(deviceId, from, to, lastStatus, minTime, lang);

		return new ResponseEntity<List<Timeline>>(ams, HttpStatus.OK);
	}

	// -------------------POST Timeline---------------------------------------------
	@RequestMapping(value = "/api/v1/device/{deviceId}/timeline", method = RequestMethod.POST)
	public ResponseEntity<List<Timeline>> postTimelineV1(@PathVariable("deviceId") String deviceId,
			@RequestParam("from") Long from,
			@RequestParam("to") Long to,
			@RequestParam("mobility_status") String mobility,
			@RequestParam(value = "lang", required = false, defaultValue = "en") String lang) throws OperationNotPermittedException, JsonProcessingException, ParseException, IOException {

		logger.debug("Requested POST Timelines for {}, from {}, to {},  mob_status {}, lang {}", deviceId, from, to, mobility, lang);

		List<Timeline> ams = deviceTService.postTimeline(deviceId, from, to, mobility, lang);

		return new ResponseEntity<List<Timeline>>(ams, HttpStatus.OK);
	}

	// -------------------GET AggregatedMobility---------------------------------------------
	@RequestMapping(value = "/api/v1/device/{deviceId}/aggregatedmobility", method = RequestMethod.GET)
	public ResponseEntity<List<AggregatedMobility>> getAddregatedMobilityV1(@PathVariable("deviceId") String deviceId,
			@RequestParam(value = "from", required = false) Long from,
			@RequestParam(value = "to", required = false) Long to,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "lang", required = false, defaultValue = "en") String lang) throws OperationNotPermittedException {

		logger.debug("Requested Aggregated Mobilities for {}, from {}, to {}, status {}, lang {}", deviceId, from, to, status, lang);

		List<AggregatedMobility> ams = deviceTService.getAggregatedMobility(deviceId, from, to, status, lang);

		return new ResponseEntity<List<AggregatedMobility>>(ams, HttpStatus.OK);
	}

	// -------------------GET Positions---------------------------------------------
	@RequestMapping(value = "/api/v1/device/{deviceId}/positions", method = RequestMethod.GET)
	public ResponseEntity<List<Position>> getPositionsV1(@PathVariable("deviceId") String deviceId,
			@RequestParam(value = "from", required = false) Long from,
			@RequestParam(value = "to", required = false) Long to,
			@RequestParam(value = "lang", required = false, defaultValue = "en") String lang) throws OperationNotPermittedException {

		logger.debug("Requested Positions for {}, from {}, to {},  lang {}", deviceId, from, to, lang);

		List<Position> ps = deviceTService.getPositions(deviceId, from, to, lang);

		return new ResponseEntity<List<Position>>(ps, HttpStatus.OK);
	}
}
