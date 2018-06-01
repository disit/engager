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
package edu.unifi.disit.wallet_user_mngt.service;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.unifi.disit.commons.datamodel.DatasetType;
import edu.unifi.disit.commons.datamodel.userprofiler.Log;
import edu.unifi.disit.commons.utils.NetClientGet;
import edu.unifi.disit.wallet_user_mngt.datamodel.Device;
import edu.unifi.disit.wallet_user_mngt.exception.OperationNotPermittedException;
import edu.unifi.disit.wallet_user_mngt.object.MobileApp;

@Service
public class DeviceServiceImpl implements IDeviceService {

	@Value("${userprofiler.url}")
	private String upuri;

	@Value("${userprofiler.timeout}")
	private Integer timeout;

	NetClientGet ncg = new NetClientGet();

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	IUserService userService;

	@Autowired
	IUserConsentService userConsentService;

	@Override
	public List<MobileApp> getDeviceModelNameForLoggedUser() {
		Set<Device> devices = userService.getConnectedDevicesForLoggedUser();
		return extractModelName(devices);
	}

	@Override
	public List<MobileApp> extractModelName(Set<Device> devices) {

		Iterator<Device> i = devices.iterator();

		List<MobileApp> l = new ArrayList<MobileApp>();

		while (i.hasNext()) {
			Device d = i.next();

			Hashtable<String, String> devicespecs = getModel(d.getDeviceId());
			String label = null;

			if (devicespecs != null) {
				label = devicespecs.get("label");
				if (label == null)
					label = devicespecs.get("id");
			}
			if (label == null)
				label = d.getDeviceId();

			l.add(new MobileApp(d.getDeviceId(), label));

		}
		return l;
	}

	private Hashtable<String, String> getModel(String id) {
		ObjectMapper mapper = new ObjectMapper();

		Hashtable<String, String> toreturn = null;

		try {

			String response = ncg.get(new URL(upuri + "/api/v1/device/" + id + "/terminal/terminalmodel"), timeout);
			toreturn = mapper.readValue(response, new TypeReference<Hashtable<String, String>>() {
			});

		} catch (Exception e) {
			logger.error("ai ai {}", e);
			e.printStackTrace();
		}

		logger.debug("returning these ppois");

		return toreturn;
	}

	@Override
	public List<Log> getLogs(String deviceId, String dataset, String valueType, Date from, Date to, String lang) throws OperationNotPermittedException {

		SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

		DatasetType d = DatasetType.valueOf(dataset);
		if (d == null)
			throw new OperationNotPermittedException("not recognized dataset");

		userConsentService.checkConsent(deviceId, d, lang);// if not consent it throw an exception

		String params = "?dataset=" + dataset;

		if (valueType != null) {
			params = params + "&valueType=" + valueType;
		}

		if (from != null) {
			params = params + "&from=" + sd.format(from);
		}
		if (to != null) {
			params = params + "&to=" + sd.format(to);
		}

		ObjectMapper mapper = new ObjectMapper();

		List<Log> toreturn = null;

		try {

			String response = ncg.get(new URL(upuri + "/api/v1/device/" + deviceId + "/logs" + params), timeout);
			toreturn = mapper.readValue(response, new TypeReference<List<Log>>() {
			});

		} catch (Exception e) {
			logger.error("ai ai {}", e);
			e.printStackTrace();
		}

		return toreturn;

	}

	@Override
	public List<Log> getLogs(String deviceId, String dataset, String lang) {

		return getLogs(deviceId, dataset, lang, null, null);
	}

	@Override
	public List<Log> getLogs(String deviceId, String dataset, String lang, Integer page, Integer size) {

		String params = "?dataset=" + dataset;
		if ((page != null) && (size != null))
			params = params + "&page=" + page + "&size=" + size;

		ObjectMapper mapper = new ObjectMapper();

		List<Log> toreturn = null;

		try {

			String response = ncg.get(new URL(upuri + "/api/v1/device/" + deviceId + "/logs" + params), timeout);
			toreturn = mapper.readValue(response, new TypeReference<List<Log>>() {
			});

		} catch (Exception e) {
			logger.error("ai ai {}", e);
			e.printStackTrace();
		}

		return toreturn;
	}

	@Override
	public Integer countLogs(String deviceId) {

		Integer toreturn = null;

		try {

			toreturn = new Integer(ncg.get(new URL(upuri + "/api/v1/device/" + deviceId + "/logs/count"), timeout));

		} catch (Exception e) {
			logger.error("ai ai {}", e);
			e.printStackTrace();
		}

		return toreturn;
	}

	@Override
	public Log addLog(String deviceId, Log log, String lang) throws OperationNotPermittedException {

		userConsentService.checkConsent(deviceId, log.getDataset(), lang);// if not consent it throw an exception

		ObjectMapper mapper = new ObjectMapper();

		Log toreturn = null;

		try {

			String response = ncg.post(new URL(upuri + "/api/v1/device/" + deviceId + "/logs"), timeout, mapper.writeValueAsString(log), "application/json");
			toreturn = mapper.readValue(response, new TypeReference<Log>() {
			});

		} catch (Exception e) {
			logger.error("ai ai {}", e);
			e.printStackTrace();
		}

		return toreturn;
	}

	@Override
	public void deleteLogs(String deviceId, String lang) {
		deleteLogs(deviceId, null, lang);
	}

	@Override
	public void deleteLogs(String deviceId, String dataset, String lang) {
		String params = "";
		if (dataset != null)
			params = params + "?dataset=" + dataset;

		try {

			ncg.delete(new URL(upuri + "/api/v1/device/" + deviceId + "/logs/" + params), timeout);

		} catch (Exception e) {
			logger.error("ai ai {}", e);
			e.printStackTrace();
		}
	}

	@Override
	public edu.unifi.disit.commons.datamodel.userprofiler.Device getDevice(String deviceId) {

		ObjectMapper mapper = new ObjectMapper();

		edu.unifi.disit.commons.datamodel.userprofiler.Device toreturn = null;

		try {

			String response = ncg.get(new URL(upuri + "/api/v1/device/" + deviceId), timeout);
			toreturn = mapper.readValue(response, new TypeReference<edu.unifi.disit.commons.datamodel.userprofiler.Device>() {
			});

		} catch (Exception e) {
			logger.error("ai ai {}", e);
			e.printStackTrace();
		}

		return toreturn;

	}

}
