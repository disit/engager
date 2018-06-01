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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.unifi.disit.commons.datamodel.userprofiler.Device;
import edu.unifi.disit.engager_utils.SampleDataSource;
import edu.unifi.disit.userprofiler.service.IDeviceService;

@SuppressWarnings({ "rawtypes", "unchecked" })
@RestController
public class DeviceController {

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	IDeviceService deviceService;

	// -------------------GET All Devices---------------------------------------------
	@RequestMapping(value = "/api/v1/device/", method = RequestMethod.GET)
	public ResponseEntity<List<Device>> getAllDevicesV1(
			@RequestParam(value = "lang", defaultValue = "en") String lang) {

		logger.debug("Requested GET all devices, lang {}", lang);

		List<Device> devices = deviceService.getAllDevices(lang);

		if ((devices == null) || (devices.isEmpty())) {
			return new ResponseEntity(HttpStatus.NO_CONTENT);
		}

		return new ResponseEntity<List<Device>>(devices, HttpStatus.OK);
	}

	// -------------------GET Single Device------------------------------------------
	@RequestMapping(value = "/api/v1/device/{deviceId}", method = RequestMethod.GET)
	public ResponseEntity<Device> getDeviceV1(
			@PathVariable("deviceId") String deviceId,
			@RequestParam(value = "lang", defaultValue = "en") String lang,
			@RequestParam(value = "refresh", defaultValue = "false") Boolean refresh) {

		logger.debug("Requested GET device for {}, lang {}, refresh {}", deviceId, lang, refresh);

		Device device = null;

		if (refresh) {
			device = deviceService.getDevice(deviceId, lang);
		} else {
			device = deviceService.getCachedDevice(deviceId, lang);
		}

		return new ResponseEntity<Device>(device, HttpStatus.OK);
	}

	// -------------------POST location Device------------------------------------------
	@RequestMapping(value = "/api/v1/device/{deviceId}/location", method = RequestMethod.POST)
	public ResponseEntity<Device> postLocationV1(@PathVariable("deviceId") String deviceId,
			@RequestParam("when") Long when,
			@RequestParam("profile") String profile,
			@RequestParam("terminal_lang") String terminal_lang,
			@RequestParam(value = "lang", defaultValue = "en") String lang,
			@RequestParam(value = "latitude", required = false) Double latitude,
			@RequestParam(value = "longitude", required = false) Double longitude,
			@RequestParam(value = "mobility_mode", required = false) String mobility_mode,
			@RequestParam(value = "speed", required = false) Double speed,
			@RequestParam(value = "accuracy", required = false) Double accuracy,
			@RequestParam(value = "provider", required = false) String provider,
			@RequestParam(value = "meanspeed", required = false) Double mean_speed,
			@RequestParam(value = "accmagn", required = false) Double acc_magn,
			@RequestParam(value = "accx", required = false) Double acc_x,
			@RequestParam(value = "accy", required = false) Double acc_y,
			@RequestParam(value = "accz", required = false) Double acc_z) {

		logger.debug("Requested POST location for {}, lang {}, latitude {}, longitude {}, when {}, mobility_mode {}, speed {}, profile {}, terminal_lang {} accuracy {} provider {} meanspeed {} accmagn {} accx {} accy {} accz {}", deviceId,
				lang, latitude, longitude, when, mobility_mode, speed, profile, terminal_lang, accuracy, provider, mean_speed, acc_magn, acc_x, acc_y, acc_z);

		Device d = null;
		try {
			d = deviceService.updateLocationDevice(deviceId, latitude, longitude, when, mobility_mode, speed, profile,
					terminal_lang, accuracy, provider, mean_speed, acc_magn, acc_x, acc_y, acc_z);
		} catch (Exception e) {
			logger.error(e);
			logger.error("{}", e);
		}

		return new ResponseEntity<Device>(d, HttpStatus.OK);
	}

	@ExceptionHandler(Exception.class)
	public String handleException(final Exception e) {
		logger.error("handle exception {}", e);
		logger.error(e);

		return "forward:/serverError";
	}

	// -------------------POST interest Device------------------------------------------
	// serviceUri has to be URL encoded
	@RequestMapping(value = "/api/v1/device/{deviceId}/interest", method = RequestMethod.POST)
	public ResponseEntity postInterestV1(@PathVariable("deviceId") String deviceId,
			@RequestParam("serviceuri") String serviceUri,
			@RequestParam(value = "lang", defaultValue = "en") String lang,
			@RequestParam("rate") Integer rate,
			@RequestParam("type") String type) throws UnsupportedEncodingException {

		logger.debug("Requested POST interest for {}, lang {}, serviceUri {}, rate {}, type {}", deviceId, lang, URLDecoder.decode(serviceUri, "UTF-8"), rate, type);

		deviceService.updateInterestDevice(deviceId, serviceUri, rate, type);

		return new ResponseEntity(HttpStatus.OK);
	}

	// -------------------POST confirmation PPOI Device------------------------------------------
	@RequestMapping(value = "/api/v1/device/{deviceId}/time/{time}", method = RequestMethod.POST)
	public ResponseEntity postTimeV1(@PathVariable("deviceId") String deviceId,
			@PathVariable("time") Long time,
			@RequestParam(value = "lang", defaultValue = "en") String lang,
			@RequestParam("confirmation") Boolean confirmation,
			@RequestParam(value = "ppoi_name", required = false) String ppoiName) {

		if ((ppoiName == null) || (ppoiName == SampleDataSource.PPOI))
			logger.debug("Requested POST time-confirmation for {}, lang {}, confirmation {}, ppoiName not specified", deviceId, lang, ppoiName, confirmation);
		else
			logger.debug("Requested POST time-confirmation for {}, lang {}, confirmation {}, ppoiName {}", deviceId, lang, ppoiName, confirmation, ppoiName);

		deviceService.updateTime(deviceId, ppoiName, confirmation, time);

		return new ResponseEntity(HttpStatus.OK);
	}
}
