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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.unifi.disit.commons.datamodel.AggregatedMobility;
import edu.unifi.disit.commons.datamodel.Submitted;
import edu.unifi.disit.commons.datamodel.engager.EngageExecuted;
import edu.unifi.disit.commons.datamodel.userprofiler.UserActivities;
import edu.unifi.disit.commons.datamodel.userprofiler.UserActivityEntry;
import edu.unifi.disit.commons.utils.Constants;
import edu.unifi.disit.commons.utils.NetClientGet;
import edu.unifi.disit.wallet_user_mngt.datamodel.Device;
import edu.unifi.disit.wallet_user_mngt.datamodel.ExtendedEngagementExecuted;
import edu.unifi.disit.wallet_user_mngt.datamodel.Rule;
import edu.unifi.disit.wallet_user_mngt.datamodel.RuleDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.User;
import edu.unifi.disit.wallet_user_mngt.service.SecurityServiceImpl;

@RestController
public class UserActivitiesRESTController {

	private static final Logger logger = LogManager.getLogger();

	@Value("${userprofiler.url}")
	private String upuri;

	@Value("${userprofiler.timeout}")
	private Integer timeout;

	@Autowired
	private SecurityServiceImpl secService;

	@Autowired
	private RuleDAO rulerepo;

	NetClientGet ncg = new NetClientGet();

	@RequestMapping(value = "/api/v1/useractivites", method = RequestMethod.GET)
	public List<UserActivityEntry> getUserActivitesV1(@RequestParam(value = "refresh", required = false, defaultValue = "false") Boolean refresh,
			@RequestParam(value = "lang", defaultValue = "en") String lang) throws JsonParseException, JsonMappingException, IOException {

		logger.debug("get user activites, lang {} refresh {}", lang, refresh);

		return common(refresh).toUserActivityObject();

	}

	@RequestMapping(value = "/api/v1/useractivites/{activity}", method = RequestMethod.GET)
	public List<Object> getUserActivitesDetailedV1(@RequestParam(value = "lang", defaultValue = "en") String lang,
			@PathVariable("activity") String activity,
			@RequestParam(value = "from", required = false) Integer from,
			@RequestParam(value = "howmany", required = false) Integer howmany,
			@RequestParam(value = "terminalID", required = false) String terminalID) throws JsonParseException, JsonMappingException, IOException {

		logger.debug("get detailed user activites, lang {}", lang);

		if (terminalID != null)
			logger.debug("terminalID {}", terminalID);

		User u = secService.findLoggedInUser();

		logger.debug(" user is {}", u.getUsername());

		List<Object> toreturn = new ArrayList<Object>();

		Set<Device> devices = u.getDevices();

		if (devices.size() == 0) {
			logger.warn("be ware the user {} has not attached devices", u.getUsername());
		} else {

			if (terminalID != null)// get specified terminalID
			{
				// TODO check this terminalID belong here to devices
				toreturn.add(getUAD(terminalID, activity, from, howmany));
			} else// get all terminalID
			{

				for (Iterator<Device> it = devices.iterator(); it.hasNext();) {
					Device device = it.next();
					logger.debug("using {}", device.getDeviceId());

					toreturn.addAll(getUAD(device.getDeviceId(), activity, from, howmany));

				}
			}
		}

		logger.debug("response {}", toreturn);

		return toreturn;

	}

	@SuppressWarnings("unchecked")
	private List<Object> getUAD(String terminalID, String activity, Integer from, Integer howmany) throws JsonParseException, JsonMappingException, IOException {

		String params = "";
		if (from != null) {
			if (howmany != null) {
				params = "?from=" + from + "&howmany=" + howmany;
			} else {
				params = "?from=" + from;
			}
		} else {
			if (howmany != null) {
				params = "?howmany=" + howmany;
			}
		}

		String request = upuri + "/api/v1/device/" + terminalID + "/useractivities/" + activity + params;

		String response_ua = ncg.get(new URL(request), timeout);

		ObjectMapper mapper = new ObjectMapper();

		if (activity.equals(Constants.DEVICE_USERACTIVITIES_EXECUTED)) {

			List<Object> toreturn = new ArrayList<Object>();
			if ((response_ua != null) && (response_ua.length() > 0)) {

				List<Object> ret = (((List<Object>) mapper.readValue(response_ua, new TypeReference<List<EngageExecuted>>() {
				})));

				for (Object ee : ret) {
					Rule r = rulerepo.findByName(((EngageExecuted) ee).getRuleName());

					String label = null;

					if ((r != null) && (r.getLabel() != null) && (r.getLabel().length() != 0))
						label = r.getLabel();

					Long points = null;

					if ((r != null) && (r.getValue() != null) && (r.getValue() != 0))
						points = r.getValue();

					// enrich points

					toreturn.add(new ExtendedEngagementExecuted((EngageExecuted) ee, label, points));
				}
			}
			return toreturn;

		} else
			return ((List<Object>) mapper.readValue(response_ua, new TypeReference<List<Submitted>>() {
			}));

	}

	// it just substitude get with refresh
	private UserActivities common(Boolean refresh) throws JsonParseException, JsonMappingException, IOException {

		User u = secService.findLoggedInUser();

		logger.debug(" user is {}", u.getUsername());

		UserActivities toreturn = new UserActivities();

		Set<Device> devices = u.getDevices();

		if (devices.size() == 0) {
			logger.warn("be ware the user {} has not attached devices", u.getUsername());
		} else {

			for (Iterator<Device> it = devices.iterator(); it.hasNext();) {
				Device device = it.next();
				logger.debug("using {}", device.getDeviceId());

				String request = upuri + "/api/v1/device/" + device.getDeviceId() + "/useractivities";

				if (refresh)
					request = request.concat("?refresh=true");

				String response_ua = ncg.get(new URL(request), timeout);

				ObjectMapper mapper = new ObjectMapper();
				toreturn.add((UserActivities) mapper.readValue(response_ua, new TypeReference<UserActivities>() {
				}));

			}
		}

		logger.debug("response {}", toreturn);

		return toreturn;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/api/v1/getusermobility", method = RequestMethod.GET)
	public List<AggregatedMobility> getAggregatedMobilityV1(@RequestParam(value = "lang", defaultValue = "en") String lang, @RequestParam(value = "from", required = false) Long from, @RequestParam(value = "to", required = false) Long to,
			@RequestParam(value = "status", required = false) String status) throws JsonParseException, JsonMappingException, IOException {

		User u = secService.findLoggedInUser();

		logger.debug("get user mobility, lang {}, from {}, to {}, status {}", lang, from, to, status);

		logger.debug("user is {}", u.getUsername());

		List<AggregatedMobility> toreturn = new ArrayList<AggregatedMobility>();

		Set<Device> devices = u.getDevices();

		if (devices.size() == 0) {
			logger.warn("be ware the user {} has not attached devices", u.getUsername());
		} else {

			Device mydevice;

			if (devices.size() > 1)
				logger.warn("be ware the user {} has more than one attached devices. anyway we consider just the first one", u.getUsername());

			// TODO which device we use? shall we mix it or what? --> we use just the first one

			Iterator<Device> id = devices.iterator();
			mydevice = id.next();
			logger.warn("using {}", mydevice);

			try {

				String request = upuri + "/api/v1/device/" + mydevice.getDeviceId() + "/aggregatedmobility" + getParam(status, from, to);

				String response_ag = ncg.get(new URL(request), timeout);

				if (response_ag.length() != 0) {
					ObjectMapper mapper = new ObjectMapper();
					toreturn = ((List<AggregatedMobility>) mapper.readValue(response_ag, new TypeReference<List<AggregatedMobility>>() {
					}));
				}

			} catch (MalformedURLException e) {
				logger.error("ai ai {}", e);
			}

		}

		logger.debug("response {}", toreturn);

		return toreturn;

	}

	private String getParam(String status, Long from, Long to) {
		String s = "";
		boolean first = true;

		if (status != null) {
			first = false;
			s = s.concat("?status=" + status);
		}

		if (to != null) {
			if (first) {
				first = false;
				s = s.concat("?to=" + to);
			} else {
				s = s.concat("&to=" + to);
			}
		}

		if (from != null) {
			if (first) {
				first = false;
				s = s.concat("?from=" + from);
			} else {
				s = s.concat("&from=" + from);
			}
		}

		return s;
	}
}
