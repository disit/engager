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
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.unifi.disit.commons.datamodel.userprofiler.Location;
import edu.unifi.disit.commons.utils.NetClientGet;
import edu.unifi.disit.wallet_user_mngt.datamodel.Device;
import edu.unifi.disit.wallet_user_mngt.object.SettingsForm;
import edu.unifi.disit.wallet_user_mngt.service.IDeviceService;
import edu.unifi.disit.wallet_user_mngt.service.ISecurityService;
import edu.unifi.disit.wallet_user_mngt.service.ISettingsService;
import edu.unifi.disit.wallet_user_mngt.service.ISocialService;
import edu.unifi.disit.wallet_user_mngt.service.IUserService;

@Controller
public class ProfileController {

	@Value("${userprofiler.url}")
	private String upuri;

	@Value("${userprofiler.timeout}")
	private Integer timeout;

	NetClientGet ncg = new NetClientGet();

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	private ISettingsService settingsService;

	@Autowired
	private IUserService userService;

	@Autowired
	private ISocialService socialService;

	@Autowired
	private ISecurityService sec;

	@Autowired
	private IDeviceService deviceService;

	@RequestMapping(value = "/profile", method = RequestMethod.GET)
	public ModelAndView profile(String message, String error, @ModelAttribute SettingsForm settingsForm) throws ParseException, IOException {
		logger.debug("/profile GET invoked");
		return createSettingModel(message, error, settingsForm);
	}

	// @RequestMapping(value = "/profile", method = RequestMethod.POST, params = { "updatemunicipality" })
	// public ModelAndView updatemunicipalitydevice(String message, String error, @ModelAttribute SettingsForm settingsForm) throws ParseException, IOException {
	//
	// // update municipality contacting up
	// logger.debug("settings form municipality {}", settingsForm.getMunicipality());
	//
	// // postMunicipality(settingsForm.getActivedeviceid(), settingsForm.getMunicipality());
	//
	// return createSettingModel(message, error, settingsForm);
	// }

	@RequestMapping(value = "/profile", method = RequestMethod.POST, params = { "refresh" })
	public ModelAndView refreshdevice(String message, String error, @ModelAttribute SettingsForm settingsForm) throws ParseException, IOException {

		return createSettingModel(message, error, settingsForm);
	}

	private ModelAndView createSettingModel(String message, String error, SettingsForm settingsForm) throws ParseException, IOException {
		ModelAndView mav = new ModelAndView("profile");

		// ------------------------devices + settings form
		Set<Device> devices = userService.getConnectedDevicesForLoggedUser();

		Map<String, String> inputsettings = settingsService.loadSettings();
		mav.addObject("inputsettings", inputsettings);

		if (settingsForm.getActivedeviceid() == null) {
			settingsForm = new SettingsForm(); // output
			Map<String, String> outputsettings = new HashMap<String, String>();
			outputsettings.putAll(inputsettings);
			settingsForm.setProperties(outputsettings);

			if ((devices != null) && (devices.size() > 0))
				settingsForm.setActivedeviceid(getLastIterator(devices).getDeviceId());// active device

			// timelinedate
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Calendar cal = Calendar.getInstance();
			String timelinedate = dateFormat.format(cal.getTime());
			settingsForm.setTimelinedate(timelinedate);

		}

		mav.addObject("connectedsocialservice", socialService.getConnectedSocialLoggedUser());

		// ppois etc-----------------------
		if ((devices != null) && (devices.size() > 0)) {

			mav.addObject("connecteddevices", deviceService.getDeviceModelNameForLoggedUser());

			edu.unifi.disit.commons.datamodel.userprofiler.Device d = getDevice(settingsForm.getActivedeviceid());

			Location l = getCurrentLocation(settingsForm.getActivedeviceid());

			if (d != null) {
				String serialized_ppoi = new ObjectMapper().writeValueAsString(d.getPpois());
				mav.addObject("ppois", serialized_ppoi);
				mav.addObject("reg_date", d.getTerminalInstallationDate());
				mav.addObject("last_update", d.getLastUpdate());
				mav.addObject("last_login", sec.findLoggedInUser().getLastLogin());
				// TOD Ocheck the provincia is one of the available one
				// if (d.getTerminal_municipality() == null) // TODO Check it is one of the contains!!!!
				// settingsForm.setMunicipality("--ALTRO--"); // TODO popolate externally (also below!)
				// else
				// settingsForm.setMunicipality(l.getMunicipality());

				if (l != null)
					mav.addObject("municipality", l.getMunicipality());
				// mav.addObject("allmunicipalities", getAllMunicipalities());
			}
		}

		mav.addObject("settingsForm", settingsForm);

		// -------------------------error
		if (message != null)
			mav.addObject("message", message);

		if (error != null)
			mav.addObject("error", error);

		return mav;
	}

	// // TODO popolate externally (also above!)
	// private List<String> getAllMunicipalities() {
	// List<String> all = new ArrayList<String>();
	// all.add("AREZZO");
	// all.add("FIRENZE");
	// all.add("GROSSETO");
	// all.add("LIVORNO");
	// all.add("LUCCA");
	// all.add("MASSA-CARRARA");
	// all.add("PISA");
	// all.add("PISTOIA");
	// all.add("PRATO");
	// all.add("SIENA");
	// all.add("--ALTRO--");
	// return all;
	// }

	// private List<DeviceModelForm> extractModelName(Set<Device> devices) {
	// Iterator<Device> i = devices.iterator();
	//
	// List<DeviceModelForm> l = new ArrayList<DeviceModelForm>();
	//
	// while (i.hasNext()) {
	// Device d = i.next();
	//
	// Hashtable<String, String> devicespecs = getModel(d.getDeviceId());
	// String label = null;
	//
	// if (devicespecs != null) {
	// label = devicespecs.get("label");
	// if (label == null)
	// label = devicespecs.get("id");
	// }
	// if (label == null)
	// label = d.getDeviceId();
	//
	// l.add(new DeviceModelForm(d.getDeviceId(), label));
	//
	// }
	// return l;
	// }

	// private Hashtable<String, String> getModel(String id) {
	// ObjectMapper mapper = new ObjectMapper();
	//
	// Hashtable<String, String> toreturn = null;
	//
	// try {
	//
	// String response = ncg.get(new URL(upuri + "/api/v1/device/" + id + "/terminal/terminalmodel"), timeout);
	// toreturn = mapper.readValue(response, new TypeReference<Hashtable<String, String>>() {
	// });
	//
	// } catch (Exception e) {
	// logger.error("ai ai {}", e);
	// e.printStackTrace();
	// }
	//
	// logger.debug("returning these ppois");
	//
	// return toreturn;
	// }

	private Location getCurrentLocation(String id) {
		ObjectMapper mapper = new ObjectMapper();

		Location toreturn = null;

		try {

			String response = ncg.get(new URL(upuri + "/api/v1/device/" + id + "/locations/current"), timeout);
			toreturn = mapper.readValue(response, new TypeReference<Location>() {
			});

		} catch (Exception e) {
			logger.error("ai ai {}", e);
			e.printStackTrace();
		}

		logger.debug("returning these ppois");

		return toreturn;
	}

	private edu.unifi.disit.commons.datamodel.userprofiler.Device getDevice(String deviceId) {

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

	// private void postMunicipality(String deviceId, String municipality) {
	//
	// String params = "";
	//
	// if (!municipality.equals("--ALTRO--"))
	// params = "municipality=" + municipality;
	// try {
	//
	// ncg.post(new URL(upuri + "/api/v1/device/" + deviceId + "/terminal"), timeout, Level.ERROR, params);
	//
	// } catch (Exception e) {
	// logger.error("set municialty failed", e);
	// e.printStackTrace();
	// }
	//
	// }

	private Device getLastIterator(Set<Device> devices) {
		Iterator<Device> i = devices.iterator();
		Device toreturn = null;

		while (i.hasNext()) {
			toreturn = i.next();
		}
		return toreturn;
	}

}
