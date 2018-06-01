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

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import edu.unifi.disit.commons.datamodel.DatasetType;
import edu.unifi.disit.wallet_user_mngt.datamodel.Consent;
import edu.unifi.disit.wallet_user_mngt.object.Response;
import edu.unifi.disit.wallet_user_mngt.service.IUserConsentService;

@Controller
public class UserConsentRESTController {

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	IUserConsentService userConsentService;

	// -------------------GET Consent---------------------------------------------
	@RequestMapping(value = "/api/v1/device/{deviceId}/consent", method = RequestMethod.GET)
	public ResponseEntity<Object> getConsentV1(@PathVariable("deviceId") String deviceId,
			@RequestParam("dataset") String dataset, // TODO ENABLE dataset==null--> now we need the specification of the dataset since we need to understand howto retrieve the username from the deviceId
			@RequestParam(value = "lang", required = false, defaultValue = "en") String lang) {

		logger.debug("Requested GET Consent for {}, lang {}", deviceId, lang);
		if (dataset != null) {
			logger.debug("Dataset {}", dataset);
		}

		// search a datasettype matching the passed dataset
		DatasetType d = DatasetType.findBy(dataset);
		if (d == null) {
			Response r = new Response();
			r.setResult(false);
			r.setMessage("specified dataset not valid");
			return new ResponseEntity<Object>(r, HttpStatus.BAD_REQUEST);
		}

		List<Consent> consents = null;

		try {
			consents = userConsentService.getConsentsForDevice(deviceId, d, lang);
		} catch (UsernameNotFoundException unfe) {
			Response r = new Response();
			r.setResult(false);
			r.setMessage(unfe.getMessage());
			return new ResponseEntity<Object>(r, HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<Object>(consents, HttpStatus.OK);
	}

	// -------------------POST Consent---------------------------------------------
	// @RequestMapping(value = "/api/v1/device/{deviceId}/consent", method = RequestMethod.PUT)
	// public ResponseEntity<Consent> putConsentV1(@PathVariable("deviceId") String deviceId,
	// @RequestBody Consent consent,
	// @RequestParam(value = "lang", required = false, defaultValue = "en") String lang) JsonProcessingException, ParseException, IOException {
	//
	// logger.debug("Requested PUT Consent for {}, consent {}, lang {}", deviceId, consent, lang);
	//
	// consent = deviceConsentService.updateConsent(deviceId, consent, lang);
	//
	// return new ResponseEntity<Consent>(consent, HttpStatus.OK);
	// }
}
