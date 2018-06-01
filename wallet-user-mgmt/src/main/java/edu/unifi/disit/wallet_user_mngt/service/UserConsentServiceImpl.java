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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import edu.unifi.disit.commons.datamodel.DatasetType;
import edu.unifi.disit.wallet_user_mngt.datamodel.Consent;
import edu.unifi.disit.wallet_user_mngt.datamodel.ConsentDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.Device;
import edu.unifi.disit.wallet_user_mngt.datamodel.DeviceDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.User;
import edu.unifi.disit.wallet_user_mngt.datamodel.UserDAO;
import edu.unifi.disit.wallet_user_mngt.exception.OperationNotPermittedException;
import edu.unifi.disit.wallet_user_mngt.externaldb.DBinterfaceDrupal;
import edu.unifi.disit.wallet_user_mngt.externaldb.DBinterfaceIOTApps;
import edu.unifi.disit.wallet_user_mngt.object.IOTApp;
import edu.unifi.disit.wallet_user_mngt.object.MobileApp;

@Service
public class UserConsentServiceImpl implements IUserConsentService {

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	ConsentDAO consentRepo;

	@Autowired
	ISecurityService securityService;

	@Autowired
	UserDAO userRepo;

	@Autowired
	DeviceDAO deviceRepo;

	@Autowired
	IDeviceService deviceService;

	DBinterfaceIOTApps dbIOTApps = DBinterfaceIOTApps.getInstance();
	DBinterfaceDrupal dbDrupal = DBinterfaceDrupal.getInstance();

	@Override
	public Consent updateConsentForLoggedUser(Consent consent, String lang) {

		User loggedUser = securityService.findLoggedInUser();

		logger.debug("updating consent {} for uid {}", consent, loggedUser.getUsername());

		Consent toreturn = consent;

		// load all the consent available for this uid
		List<Consent> consents = consentRepo.findByUserUsernameAndDataset(loggedUser.getUsername(), consent.getDataset());

		if ((consents == null) || (consents.size() == 0)) { // empty there
			if (!consent.getConsent()) {// add if the passed contents is false
				Consent c = new Consent(false, consent.getDataset(), loggedUser);
				consentRepo.save(c);
			}
		} else { // something there
			if (consents.size() > 1) {
				logger.warn("seems strange there is more than one, anyway return the first one");
			}
			if (consents.get(0).getConsent() != consent.getConsent()) {// if different
				if (consent.getConsent()) {// remove if the passed is false
					consentRepo.delete(consents.get(0));
				} else {// add if the passed contents is true
					Consent c = new Consent(false, consent.getDataset(), loggedUser);
					consentRepo.save(c);
				}
			}
		}

		return toreturn;
	}

	@Override
	// THIS NOT POPULATE the user
	public List<Consent> getConsentsForLoggedUser(DatasetType dataset, String lang) {

		User loggedUser = securityService.findLoggedInUser();

		return getConsentsForUser(loggedUser.getUsername(), dataset, lang);
	}

	@Override
	// THIS IS NOT POPULATE the user
	public List<Consent> getConsentsForDevice(String deviceId, DatasetType dataset, String lang) {

		String username = getUsernameForDevice(deviceId, dataset);

		return getConsentsForUser(username, dataset, lang);
	}

	private List<Consent> getConsentsForUser(String username, DatasetType dataset, String lang) {

		if ((dataset == null)) {
			return getAllConsents(username);

		} else {
			List<Consent> consents = consentRepo.findByUserUsernameAndDataset(username, dataset);
			if ((consents != null) && (consents.size() > 0)) {
				logger.warn("retrieved consent should be ==1 for useranme {} and dataset {}", username, dataset);
			}
			if ((consents == null) || (consents.size() == 0)) {
				consents.add(new Consent(true, dataset, userRepo.findByUsername(username)));
			}

			return consents;
		}
	}

	private List<Consent> getAllConsents(String username) {
		List<Consent> toreturn = new ArrayList<Consent>();

		for (DatasetType dataset : DatasetType.values()) {
			List<Consent> consents = consentRepo.findByUserUsernameAndDataset(username, dataset);
			if ((consents != null) && (consents.size() > 0)) {
				if (consents.size() > 1) {
					logger.warn("occhio we retrieved more than one consent for the current deviceid and dataset. NOT POSSIBLE. Anyway we consider just the first one");
				}
				toreturn.add(consents.get(0));
			} else {
				Consent c = new Consent(dataset);
				toreturn.add(c);
			}
		}

		return toreturn;
	}

	@Override
	public void checkConsent(String deviceId, DatasetType dataset, String lang) throws OperationNotPermittedException {
		// check if the user belong to this device gave consent for this DATASET
		List<Consent> consent = getConsentsForDevice(deviceId, dataset, lang);
		if ((consent == null) || consent.size() == 0) {
			logger.warn("no given consenso?");
			throw new OperationNotPermittedException("Strange there is not consent for this ");
		}
		if (consent.size() > 1)
			logger.warn("seems strange there is more than a consent, anyway we take just the first one");
		if (!consent.get(0).getConsent()) {
			logger.warn("no given consent");
			throw new OperationNotPermittedException("Strange there is not consent for this ");
		}
	}

	@SuppressWarnings("unused")
	@Override
	public String getUsernameForDevice(String deviceId, DatasetType dataset) {
		// retrieve the username who belogs this deviceId
		String username = null;

		if (dataset == DatasetType.IOTAPP) {// TODO manage datasets how type
			Integer drupalUid = 66;// dbIOTApps.getDrupalUid(deviceId);
			if (drupalUid == null)
				throw new UsernameNotFoundException("no user has been found connected to the specified deviceId");

			username = dbDrupal.getUsername(drupalUid);

			if (username == null)
				throw new UsernameNotFoundException("no user has been found connected to the specified deviceId");
		} else {

			List<User> users = userRepo.findByDevicesDeviceId(deviceId);
			if ((users == null) || (users.size() == 0))
				throw new UsernameNotFoundException("no user has been found connected to the specified deviceId");
			if (users.size() > 1)
				logger.warn("There are more than one user connected to the specified device {} Anyway we take the first one", deviceId);
			username = users.get(0).getUsername();
		}
		return username;
	}

	@SuppressWarnings("unused")
	@Override
	public List<String> getDeviceForLoggedUser(DatasetType dataset) {
		// retrieve the username who belogs this deviceId
		List<String> toreturn = new ArrayList<String>();

		String loggedUserName = securityService.findLoggedInUsername();

		if (dataset == DatasetType.IOTAPP) {// TODO manage datasets how type
			// find from drupal DB the uid of the loggedUser
			Integer drupalUid = 1;// dbDrupal.getUID(loggedUserName);

			if (drupalUid == null) {
				logger.warn("the logged user {} is not found in the DRUPAL DB" + loggedUserName);
				return null;
			}

			// retrieve from 207 DB the list of IOTApp of the drupalUid

			List<IOTApp> IOTApps = dbIOTApps.getIOTApps(drupalUid);
			for (IOTApp IOTApp : IOTApps) {
				toreturn.add(IOTApp.getContainerId());
			}
		} else {
			List<Device> devices = new ArrayList<Device>();
			devices.addAll(deviceRepo.findByUserUsername(loggedUserName));
			for (Device device : devices) {
				toreturn.add(device.getDeviceId());
			}
		}

		return toreturn;
	}

	@SuppressWarnings("unused")
	@Override
	public List<MobileApp> getDeviceForLoggedUserDetailed(DatasetType dataset) {
		// retrieve the username who belogs this deviceId
		List<MobileApp> toreturn = new ArrayList<MobileApp>();

		String loggedUserName = securityService.findLoggedInUsername();

		if (dataset == DatasetType.IOTAPP) {// TODO manage datasets how type
			// find from drupal DB the uid of the loggedUser
			Integer drupalUid = 1;// dbDrupal.getUID(loggedUserName);

			if (drupalUid == null) {
				logger.warn("the logged user {} is not found in the DRUPAL DB" + loggedUserName);
				return null;
			}

			// retrieve from 207 DB the list of IOTApp of the drupalUid

			List<IOTApp> IOTApps = dbIOTApps.getIOTApps(drupalUid);
			for (IOTApp IOTApp : IOTApps) {
				toreturn.add(new MobileApp(IOTApp.getContainerId(), IOTApp.getName()));
			}
		} else {
			List<MobileApp> devices = deviceService.extractModelName(deviceRepo.findByUserUsername(loggedUserName));

			for (MobileApp device : devices) {
				toreturn.add(device);
			}
		}

		return toreturn;
	}

	@Override
	public void checkConsentForLoggedUser(DatasetType dataset, String lang) throws OperationNotPermittedException {
		// check if the user belong to this device gave consent for this DATASET
		List<Consent> consent = getConsentsForLoggedUser(dataset, lang);
		if ((consent == null) || consent.size() == 0) {
			logger.warn("no given consenso?");
			throw new OperationNotPermittedException("Strange there is not consent for this ");
		}
		if (consent.size() > 1)
			logger.warn("seems strange there is more than a consent, anyway we take just the first one");
		if (!consent.get(0).getConsent()) {
			logger.warn("no given consent");
			throw new OperationNotPermittedException("no given consent");
		}
	}
}
