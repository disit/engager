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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import disit.engager_base.ACTION;
import disit.engager_base.ACTIONS;
import edu.unifi.disit.commons.datamodel.MobilityUserLocation;
import edu.unifi.disit.commons.datamodel.UserLocation;
import edu.unifi.disit.engagerapi.datamodel.CONTEXT;
import edu.unifi.disit.engagerapi.datamodel.Result;

public class AsynchLayer extends Thread {

	private static final Logger logger = LogManager.getLogger();
	private static final edu.unifi.disit.engagerapi.DBinterface dbi = edu.unifi.disit.engagerapi.DBinterface.getInstance();
	private static final GetPropertyValues properties = GetPropertyValues.getInstance();
	private Engage engager;
	private boolean isAlive = true;
	private boolean isSimulation = false;
	private String simulationPath;

	public AsynchLayer(Engage engager) {
		this.engager = engager;

		if (!(simulationPath = properties.getSimulationPath()).equalsIgnoreCase("null")) {
			logger.debug("simulation is on");
			isSimulation = true;
		}
	}

	public void run() {

		Long lastEngaged = (long) dbi.getMaxId();// lastEngaged always point to last entry in id USER_EVAL

		Long lastUP = dbi.getLastEngaged();// lastUP always point to entry saved (last user-profiled)
		if ((lastUP == null) || (lastUP == 0))
			lastUP = lastEngaged;// if up was never contacted, use the last entry in USER_EVAL

		while (isAlive) {

			try {

				List<MobilityUserLocation> entries = dbi.retrieveLastLocations(lastUP);

				try {
					// live scenario
					for (int index = 0; index < entries.size(); index++) {

						MobilityUserLocation entry = entries.get(index);

						CONTEXT context = new CONTEXT();

						context.retrieveDynamicUSER(entry);// call up
						lastUP = (long) entry.getId();// update lastUP

						// if we're ONLINE, engage!
						if (entries.size() != edu.unifi.disit.engagerapi.DBinterface.THRESHOLD_ENGAGER_ONLINE) {
							logger.debug("engaging live");

							engage(context, entries, index, lastEngaged);// call engage
							lastEngaged = (long) entry.getId();// update lastEngaged
						}

					}
				} catch (NoSuchElementException nsee) {
					logger.error("USERPROFILER is down... MAX_ID is stopped to:" + lastUP);
				}

				if (entries.size() > 0) {
					dbi.setLastEngaged(lastUP);
				} else
					logger.debug("no data");

				// recovery scenario
				if (entries.size() == edu.unifi.disit.engagerapi.DBinterface.THRESHOLD_ENGAGER_ONLINE) {
					logger.debug("engaging offline (recovery mode)");

					List<MobilityUserLocation> engageOnline = dbi.retrieveLastLocations(lastEngaged);

					for (int index = 0; index < engageOnline.size(); index++) {

						MobilityUserLocation entry2 = engageOnline.get(index);

						CONTEXT context = new CONTEXT();

						context.retrieveStaticUSER(entry2);

						engage(context, engageOnline, index, lastEngaged);
						lastEngaged = (long) entry2.getId();// update lastEngaged

					}
				}

			} catch (Exception e) {
				logger.error("Exception in run:", e);// general error
				lastUP++;// increment by one to avoid problem on next cycle
			}
		}

		// release here all the resource
		try {
			this.finalize();
		} catch (Throwable e) {
			logger.error("Throwable in finalize:", e);
		}

		logger.info("...current thread stopped");
	}

	private void engage(CONTEXT context, List<MobilityUserLocation> entries, int index, Long lastEngagedOnline) throws JsonParseException, JsonMappingException, NoSuchElementException, IOException {

		UserLocation entry = entries.get(index);

		if (entry.getId() < lastEngagedOnline) {// already engaged
			logger.warn("already engaged {} last was {}", entry, lastEngagedOnline);
			// } else if ((index < (entries.size() - 1)) && (entry.getUserName().equals(entries.get(index + 1).getUserName())) && (entries.get(index + 1).getLatitude() != 0) && (entries.get(index + 1).getLongitude() != 0)) {// avoid burst
			// logger.debug("skipping, there is a newer position for this {}", entry);
		} else if (Math.abs(entry.getData().getTime() - System.currentTimeMillis()) > 3600 * 1000) {// if this context it's too old (mean the device still sending old cached data)! error prone for timezone TODO
			logger.debug("this context is cached and too old, ignore for engagement {}", entry);
		} else if ((entry.getLatitude() == 0) && (entry.getLongitude() == 0)) {// gps null
			logger.debug("gps is null {}", entry);
			// } else if ((entry.getAccuracy() > 100)) {// low accuracy
			// logger.debug("accuracy too high {}", entry);
		} else {

			logger.info("------------------------------------------------------------------------Recevieved an ENGAGE request");
			logger.info("Device_id is      : {}", entry.getUserName());
			logger.info("Gps latitude is   : {}", entry.getLatitude());
			logger.info("Gps longitude is  : {}", entry.getLongitude());
			if (entry.getStatus() != null)
				logger.info("MobilityStatus is : {}", entry.getStatus());
			logger.info("When is           : {}", entry.getData());
			logger.info("DeviceLanguage is : {}", entry.getDeviceLanguage());
			if (entry.getAccuracy() != null)
				logger.info("Accuracy is       : {}", entry.getAccuracy());
			if (entry.getSpeed() != null)
				logger.info("Speed is          : {}", entry.getSpeed());
			logger.info("----------------------------------------------------------------------------------------------------");

			context.retrieveLOCATION(entry.getLatitude(), entry.getLongitude(), entry.getDeviceLanguage());
			context.retrieveENVIROMENT(entry.getLatitude(), entry.getLongitude(), entry.getDeviceLanguage());
			context.retrieveTIME(entry.getData().getTime());
			context.retrieveTRANSPORT(entry);

			ACTIONS actions_to_return = this.engager.engage(context);

			int i = 0;
			logger.info("----------------------------------------------------------------------Returning ENGAGEMENTS response");
			for (ACTION action : actions_to_return.getActions()) {
				logger.info("{}-esima", (i++));
				logger.info("Action is  : {}", action.getType());
				logger.info("Message is : {}", action.getMsg());
				logger.info("Class is   : {}", action.getClass());
			}
			logger.info("----------------------------------------------------------------------------------------------------");

			dbi.add(entry.getUserName(), actions_to_return, "CREATED", entry.getData(), new Date(System.currentTimeMillis() + properties.getEngagementElapsingMilliseconds()), properties.getThresholdLiveMilliseconds(),
					context.getUser().getIsAssessor());

			if (isSimulation)
				simulazione(entry.getUserName());
		}
	}

	public void stopit() throws Throwable {
		isAlive = false;
		logger.info("Try to stop current thread...");
	}

	private void simulazione(String userName) {

		long start_time = System.currentTimeMillis();

		Result toreturn = new Result();

		if ((userName == null) || (userName.length() == 0) || (userName.equalsIgnoreCase(" "))) {
			logger.info("username not valid, skipping");
		} else {
			toreturn = dbi.getResult(userName, null, start_time, properties.getDBSensorTimeoutMilliseconds());
		}

		printOnFile(userName, (List<ACTION>) toreturn.getEngagement());
		printOnFile(userName, (List<ACTION>) toreturn.getAssistance());
	}

	private void printOnFile(String userName, List<ACTION> actions) {
		if (actions.size() > 0) {
			try {

				FileWriter fw = new FileWriter(new File(simulationPath.concat(userName).concat(".txt")), true);// true means append

				for (ACTION a : actions) {
					fw.write("------------------------------------\n");
					fw.write("now is:" + (new Date()).toString() + "\n");
					fw.write("action date creation is:" + (new Date(a.getTime_created())).toString() + "\n");
					fw.write("presumed date elapsing is:" + (new Date(a.getTime_created() + a.getTime_elapse())).toString() + "\n");// elapsing is remotly...
					fw.write("action classe:" + a.getClasse() + "\n");
					fw.write("action type:" + a.getType() + "\n");
					fw.write("action title:" + a.getTitle() + "\n");
					fw.write("action msg:" + a.getMsg() + "\n");
					fw.write("------------------------------------\n");
				}

				fw.flush();
				fw.close();
			} catch (IOException e) {
				logger.error("error in print in simulation:", e);
			}
		}
	}
}
