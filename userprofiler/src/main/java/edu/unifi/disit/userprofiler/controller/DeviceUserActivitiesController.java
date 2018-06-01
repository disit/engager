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

import edu.unifi.disit.commons.datamodel.Submitted;
import edu.unifi.disit.commons.datamodel.engager.EngageExecuted;
import edu.unifi.disit.commons.datamodel.userprofiler.UserActivities;
import edu.unifi.disit.userprofiler.exception.OperationNotPermittedException;
import edu.unifi.disit.userprofiler.service.IDeviceUserActivitiesService;

@RestController
public class DeviceUserActivitiesController {

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	IDeviceUserActivitiesService deviceUAService;

	private static final edu.unifi.disit.userprofiler.externaldb.DBinterface dbi_remote = edu.unifi.disit.userprofiler.externaldb.DBinterface.getInstance();

	// --------------------GET ALL Single Activities ------------------------------------------
	// it also refresh
	@RequestMapping(value = "/api/v1/device/{deviceId}/useractivities", method = RequestMethod.GET)
	public ResponseEntity<UserActivities> getUserActivitiesV1(@PathVariable("deviceId") String deviceId,
			@RequestParam(value = "refresh", required = false, defaultValue = "false") Boolean refresh,
			@RequestParam(value = "lang", required = false, defaultValue = "en") String lang) throws OperationNotPermittedException {

		logger.debug("Requested userActivities for {}, lang {}, refresh {}", deviceId, lang, refresh);

		if (refresh) {

			UserActivities ua = deviceUAService.refreshUserActivies(deviceId, lang);

			deviceUAService.storeRefreshedUserActivities(deviceId, ua, lang);

			return new ResponseEntity<UserActivities>(ua, HttpStatus.OK);
		} else {
			return new ResponseEntity<UserActivities>(deviceUAService.getCachedUserActivies(deviceId, lang), HttpStatus.OK);
		}
	}

	// -------------------GET Single User Activities------------------------------------------
	@RequestMapping(value = "/api/v1/device/{deviceId}/useractivities/submittedcommentsdetailed", method = RequestMethod.GET)
	public ResponseEntity<List<Submitted>> getSubmittedCommentsDetailedV1(@PathVariable("deviceId") String deviceId,
			@RequestParam(value = "from", required = false) Integer from,
			@RequestParam(value = "howmany", required = false) Integer howmany,
			@RequestParam(value = "lang", required = false, defaultValue = "en") String lang) {

		logger.debug("Requested submittedcommentsdetailed for {}, lang {}, from {}, howmany {}", deviceId, lang, from, howmany);

		return new ResponseEntity<List<Submitted>>(deviceUAService.enrichSubmitted(dbi_remote.getSubmittedComments(deviceId, from, howmany)), HttpStatus.OK);
	}

	@RequestMapping(value = "/api/v1/device/{deviceId}/useractivities/submittedphotosdetailed", method = RequestMethod.GET)
	public ResponseEntity<List<Submitted>> getSubmittedPhotosDetailedV1(@PathVariable("deviceId") String deviceId,
			@RequestParam(value = "from", required = false) Integer from,
			@RequestParam(value = "howmany", required = false) Integer howmany,
			@RequestParam(value = "lang", required = false, defaultValue = "en") String lang) {

		logger.debug("Requested submittedphotosdetailed for {}, lang {}, from {}, howmany {}", deviceId, lang, from, howmany);

		return new ResponseEntity<List<Submitted>>(deviceUAService.enrichSubmitted(dbi_remote.getSubmittedPhotos(deviceId, from, howmany)), HttpStatus.OK);
	}

	@RequestMapping(value = "/api/v1/device/{deviceId}/useractivities/submittedstarsdetailed", method = RequestMethod.GET)
	public ResponseEntity<List<Submitted>> getSubmittedStarsDetailedV1(@PathVariable("deviceId") String deviceId,
			@RequestParam(value = "from", required = false) Integer from,
			@RequestParam(value = "howmany", required = false) Integer howmany,
			@RequestParam(value = "lang", required = false, defaultValue = "en") String lang) {

		logger.debug("Requested submittedstarsdetailed for {}, lang {}, from {}, howmany {}", deviceId, lang, from, howmany);

		return new ResponseEntity<List<Submitted>>(deviceUAService.enrichSubmitted(dbi_remote.getSubmittedStars(deviceId, from, howmany)), HttpStatus.OK);
	}

	@RequestMapping(value = "/api/v1/device/{deviceId}/useractivities/executedengagementsdetailed", method = RequestMethod.GET)
	public ResponseEntity<List<EngageExecuted>> getExecutedEngagementsDetailedV1(@PathVariable("deviceId") String deviceId,
			@RequestParam(value = "from", required = false) Integer from,
			@RequestParam(value = "howmany", required = false) Integer howmany,
			@RequestParam(value = "lang", required = false, defaultValue = "en") String lang) {

		logger.debug("Requested executedengagementsdetailed for {}, lang {}, from {}, howmany {}", deviceId, lang, from, howmany);

		return new ResponseEntity<List<EngageExecuted>>(dbi_remote.getExecutedEngagements(deviceId, from, howmany), HttpStatus.OK);
	}

}
