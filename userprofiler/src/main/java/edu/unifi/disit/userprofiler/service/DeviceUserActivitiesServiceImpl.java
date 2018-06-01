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
package edu.unifi.disit.userprofiler.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.unifi.disit.commons.datamodel.Submitted;
import edu.unifi.disit.commons.datamodel.engager.EngageExecuted;
import edu.unifi.disit.commons.datamodel.userprofiler.Device;
import edu.unifi.disit.commons.datamodel.userprofiler.UserActivities;
import edu.unifi.disit.commons.utils.NetClientGet;
import edu.unifi.disit.userprofiler.datamodel.DeviceDAO;
import edu.unifi.disit.userprofiler.exception.OperationNotPermittedException;
import edu.unifi.disit.userprofiler.ppois.GetPropertyValues;

@Component
public class DeviceUserActivitiesServiceImpl implements IDeviceUserActivitiesService {

	private static final NetClientGet ncg = new NetClientGet();

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	GetPropertyValues properties;

	@Autowired
	IDeviceService deviceService;

	@Autowired
	DeviceDAO deviceRepo;

	private static final edu.unifi.disit.userprofiler.externaldb.DBinterface dbi_remote = edu.unifi.disit.userprofiler.externaldb.DBinterface.getInstance();

	// if the cached version if null (migration scenario), retrieve a live user activities
	@Override
	public UserActivities getCachedUserActivies(String deviceId, String lang) throws OperationNotPermittedException {

		logger.debug("retrieve a cached user activities for {}" + deviceId);

		Device d = deviceService.getCachedDevice(deviceId, lang);
		if (d != null) {
			UserActivities ua = d.getUserActivities();
			if (ua == null) {
				logger.debug("seems strange the user activities is null. try to refresh {}" + deviceId);
				ua = refreshUserActivies(deviceId, lang);

				storeRefreshedUserActivities(deviceId, ua, lang);
			}
			return ua;
		} else
			throw new OperationNotPermittedException("issue found trying to retrieve a cached version of the device " + deviceId);
	}

	// TODO default use user prefered language
	@Override
	public UserActivities refreshUserActivies(String deviceId) {
		return refreshUserActivies(deviceId, "en");
	}

	@Override
	public UserActivities refreshUserActivies(String deviceId, String lang) {

		logger.debug("retrieve a refresh user activities for {}", deviceId);

		UserActivities ua = new UserActivities(getSubmittedStars(deviceId), getSubmittedComments(deviceId), getSubmittedPhotos(deviceId), getExecutedEngagements(deviceId));

		return ua;
	}

	@Override
	public void storeRefreshedUserActivities(String deviceId, UserActivities ua, String lang) {
		// save in the storage this new version
		Device d = deviceService.getCachedDevice(deviceId, lang);// always return a good device. if the cached was not found, create a new one
		d.setUserActivities(ua);
		deviceRepo.save(d);
	}

	public Integer getSubmittedComments(String deviceId) {
		List<Submitted> o = dbi_remote.getSubmittedComments(deviceId);

		if (o != null)
			return o.size();
		else
			return 0;
	}

	public Integer getSubmittedPhotos(String deviceId) {
		List<Submitted> o = dbi_remote.getSubmittedPhotos(deviceId);

		if (o != null)
			return o.size();
		else
			return 0;
	}

	public Integer getSubmittedStars(String deviceId) {
		List<Submitted> o = dbi_remote.getSubmittedStars(deviceId);

		if (o != null)
			return o.size();
		else
			return 0;
	}

	public Integer getExecutedEngagements(String deviceId) {
		List<EngageExecuted> e = dbi_remote.getExecutedEngagements(deviceId);

		if (e != null)
			return e.size();
		else
			return 0;
	}

	@Override
	public List<Submitted> enrichSubmitted(List<Submitted> o) {
		List<Submitted> toreturn = new ArrayList<Submitted>();
		for (Submitted s : o) {
			toreturn.add(enrichSubmitted(s));
		}
		return toreturn;

	}

	private Submitted enrichSubmitted(Submitted o) {

		String name = null;
		String serviceType = null;

		try {

			// servicemap.km4city.org/WebAppGrafo/api/v1/?serviceUri=http://www.disit.org/km4city/resource/CarParkCareggi
			String response = ncg.get(new URL(properties.getServicemapURL() + "?serviceUri=" + o.getServiceUri()), properties.getReadTimeoutMillisecond());

			if ((response != null) && (response.length() != 0)) {
				// create ObjectMapper instance
				ObjectMapper objectMapper = new ObjectMapper();

				// read JSON like DOM Parser
				JsonNode rootNode = objectMapper.readTree(response.getBytes());

				// ----------------------------------------------------------Services
				JsonNode serviceNode = rootNode.path("Service");
				JsonNode featNode = serviceNode.path("features");

				Iterator<JsonNode> elements = featNode.elements();
				while (elements.hasNext()) {
					JsonNode feature = elements.next();

					JsonNode propertiesJ = feature.path("properties");

					name = propertiesJ.get("name").textValue();

					serviceType = propertiesJ.get("serviceType").textValue();
				}

			}
		} catch (MalformedURLException e) {
			logger.error("MalformedURLException on retrieveCPZ:", e);
		} catch (JsonProcessingException e) {
			logger.error("JsonProcessingException on retrieveCPZ:", e);
		} catch (IOException e) {
			logger.error("IOException on retrieveCPZ:", e);
		} catch (java.util.NoSuchElementException e) {
			logger.error("NoSuch on retrieveCPZ:", e);
		}

		return new Submitted(o.getTime(), o.getServiceUri(), o.getText(), name, serviceType);

	}
}
