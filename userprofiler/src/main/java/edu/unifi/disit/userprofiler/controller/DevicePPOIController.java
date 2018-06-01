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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.unifi.disit.commons.datamodel.PPOI;
import edu.unifi.disit.userprofiler.exception.OperationNotPermittedException;
import edu.unifi.disit.userprofiler.exception.PPOIAlreadyExistsException;
import edu.unifi.disit.userprofiler.exception.PPOINotExistsException;
import edu.unifi.disit.userprofiler.service.IDevicePPOIService;

@SuppressWarnings({ "rawtypes", "unchecked" })
@RestController
public class DevicePPOIController {

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	IDevicePPOIService ppoiService;

	// -------------------GET All PPOI---------------------------------------------
	@RequestMapping(value = "/api/v1/device/{deviceId}/ppoi", method = RequestMethod.GET)
	public ResponseEntity<List<PPOI>> getAllPPOIsV1(
			@PathVariable("deviceId") String deviceId,
			@RequestParam(value = "confirmation", required = false) Boolean confirmation,
			@RequestParam(value = "lang", defaultValue = "en") String lang) {

		logger.debug("Requested GET all ppois for {}, confirmed {}, , lang {}", deviceId, confirmation, lang);

		List<PPOI> ppois = ppoiService.getAllPPOIs(deviceId, confirmation, lang);

		if ((ppois == null) || (ppois.isEmpty()))
			return new ResponseEntity(HttpStatus.NO_CONTENT);

		return new ResponseEntity<List<PPOI>>(ppois, HttpStatus.OK);
	}

	// -------------------GET Single PPOI------------------------------------------
	@RequestMapping(value = "/api/v1/device/{deviceId}/ppoi/{ppoiName}", method = RequestMethod.GET)
	public ResponseEntity<List<PPOI>> getPPOIV1(
			@PathVariable("deviceId") String deviceId,
			@PathVariable("ppoiName") String ppoiName,
			@RequestParam(value = "wildcard", defaultValue = "false") Boolean wildcard,
			@RequestParam(value = "confirmation", required = false) Boolean confirmation,
			@RequestParam(value = "lang", defaultValue = "en") String lang) {

		logger.debug("Requested GET ppois for {}, ppoiName {}, wildcard {}, confirmation {}, lang {}", deviceId, ppoiName, wildcard, confirmation, lang);

		List<PPOI> ppois = ppoiService.getPPOIs(deviceId, ppoiName, wildcard, confirmation, lang);

		if (ppois == null)
			return new ResponseEntity(HttpStatus.NO_CONTENT);

		return new ResponseEntity<List<PPOI>>(ppois, HttpStatus.OK);
	}

	// -------------------POST Single PPOI------------------------------------------
	// this change the ppoiname if it's USERGENERATED
	// this change the id
	@RequestMapping(value = "/api/v1/device/{deviceId}/ppois", method = RequestMethod.POST)
	public ResponseEntity<PPOI> postPPOIV1(
			@PathVariable("deviceId") String deviceId,
			@RequestBody PPOI ppoi,
			@RequestParam(value = "lang", defaultValue = "en") String lang) {

		logger.debug("Requested POST for {}, ppoi {}, lang {}", deviceId, ppoi, lang);

		PPOI newppoi = null;

		try {

			newppoi = ppoiService.addPPOI(deviceId, ppoi, lang);

		} catch (PPOIAlreadyExistsException e) {
			return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<PPOI>(newppoi, HttpStatus.OK);
	}

	// -------------------PUT Single PPOI------------------------------------------
	// put single id
	@RequestMapping(value = "/api/v1/device/{deviceId}/ppoi/{id}", method = RequestMethod.PUT)
	public ResponseEntity putPPOIV1(
			@PathVariable("deviceId") String deviceId,
			@PathVariable("id") Long id,
			@RequestBody(required = false) PPOI ppoi,
			@RequestParam(value = "confirmation", required = false) Boolean confirmation,
			@RequestParam(value = "label", required = false) String label,
			@RequestParam(value = "lang", defaultValue = "en") String lang) {

		logger.debug("Requested PUT ppoi-confirmation for {}, id {}, ppoi {}, confirmation {}, label {}, lang {}", deviceId, id, ppoi, confirmation, label, lang);

		try {

			if (ppoi != null) {

				if (id.longValue() != ppoi.getId().longValue()) {
					return new ResponseEntity("the specified id is different from the entity name", HttpStatus.BAD_REQUEST);
				}

				ppoiService.updatePPOI(deviceId, ppoi, lang);

			} else {
				if (confirmation != null) {
					ppoiService.updateConfirmationPPOI(deviceId, id, confirmation, lang);
				}
				if (label != null) {
					ppoiService.updateLabelPPOI(deviceId, id, label, lang);
				}
			}

		} catch (PPOINotExistsException e) {
			return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity(HttpStatus.OK);
	}

	// -------------------DELETE Single PPOI------------------------------------------
	@RequestMapping(value = "/api/v1/device/{deviceId}/ppoi/{id}", method = RequestMethod.DELETE)
	public ResponseEntity deletePPOIV1(
			@PathVariable("deviceId") String deviceId,
			@PathVariable("id") Long id,
			@RequestParam(value = "lang", defaultValue = "en") String lang) {

		logger.debug("Requested DELETE for {}, id {}, , lang {}", deviceId, id, lang);

		try {

			ppoiService.deletePPOI(deviceId, id, lang);

		} catch (PPOINotExistsException | OperationNotPermittedException e) {
			return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity(HttpStatus.OK);
	}
}
