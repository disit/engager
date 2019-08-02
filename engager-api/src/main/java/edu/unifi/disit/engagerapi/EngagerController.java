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
package edu.unifi.disit.engagerapi;

import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.unifi.disit.commons.utils.Utils;
import edu.unifi.disit.engagerapi.datamodel.Result;

@RestController
public class EngagerController {

	private static final Logger logger = LogManager.getLogger("EngagerController");
	private static final DBinterface dbi = DBinterface.getInstance();
	private static final GetPropertyValues properties = GetPropertyValues.getInstance();
	private Engage e;
	private AsynchLayer al;
	private AsynchAccesslog aal;
	private AsynchPurge ap;
	private AsynchExecuted ae;

	EngagerController() {
		// setting logger level
		Utils.setLoggerLevel(properties.getLogLevel());

		this.e = new Engage();
		this.al = new AsynchLayer(e);
		this.al.start();
		this.aal = new AsynchAccesslog();
		this.aal.start();
		this.ap = new AsynchPurge();
		this.ap.start();
		this.ae = new AsynchExecuted();
		this.ae.start();
	}

	@RequestMapping(value = "/test", method = RequestMethod.GET)
	public ResponseEntity<String> engagerTest() {
		return new ResponseEntity<String>("alive", HttpStatus.OK);
	}

	@RequestMapping("/engager")
	public Result engageAS(
			@RequestParam("uid") String user_id,
			@RequestParam(value = "selection", required = false) String selection,
			@RequestParam(value = "update", required = false, defaultValue = "true") Boolean update) {// this specify if an update has to be committed (moving from CREATED to SENT)

		long start_time = System.currentTimeMillis();

		logger.debug("received request from user {} start time is {}", user_id, start_time);

		Result toreturn = new Result();

		if ((user_id == null) || (user_id.length() == 0) || (user_id.equalsIgnoreCase(" "))) {
			logger.info("username not valid, skipping");
		} else {
			toreturn = dbi.getResult(user_id, selection, start_time, properties.getDBSensorTimeoutMilliseconds(), update);
		}

		return toreturn;
	}

	@RequestMapping("/cancel-engagement")
	public edu.unifi.disit.commons.datamodel.Result cancelEngagement(
			@RequestParam("id") Long id) {

		logger.info("----Received a request for cancel engagement id={}", id);

		edu.unifi.disit.commons.datamodel.Result result = new edu.unifi.disit.commons.datamodel.Result("OK");

		if (id == null) {
			logger.info("id not valid, skipping");
			result.setResult("KO");
			result.setMessage("id not valid");
		} else {
			try {
				dbi.insertEngageCancelled(id);
			} catch (Exception e) {
				result.setResult("KO");
				result.setMessage(e.getMessage());
			}
		}

		return result;
	}

	@PreDestroy
	public void cleanUp() throws Exception {
		try {
			e.stopit();
			e = null;
			al.stopit();
			aal.stopit();
			ap.stopit();
			ae.stopit();
			properties.stopit();
			dbi.close();
			super.finalize();
		} catch (Throwable e) {
			logger.error("Error in cleanup:", e);
		}
	}
}
