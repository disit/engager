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
package edu.unifi.disit.userprofiler.ppois.markov;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.unifi.disit.commons.datamodel.AggregatedMobility;
import edu.unifi.disit.commons.datamodel.Location;
import edu.unifi.disit.commons.datamodel.PPOI;
import edu.unifi.disit.commons.utils.Utils;
import edu.unifi.disit.userprofiler.ppois.DBinterface;
import edu.unifi.disit.userprofiler.ppois.GetPropertyValues;
import edu.unifi.disit.userprofiler.service.IDeviceTimelineService;

@Component
public class MovemementLearning {

	@Autowired
	IDeviceTimelineService tl;

	@Autowired
	GetPropertyValues properties;

	@Autowired
	Markowinterface mi;

	private static final Logger logger = LogManager.getLogger();

	private static final DBinterface db = DBinterface.getInstance();
	private static final edu.unifi.disit.userprofiler.externaldb.DBinterface db_remote = edu.unifi.disit.userprofiler.externaldb.DBinterface.getInstance();

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------main
	// retrieve the user that has at least one ppoi
	public List<String> retrieveUsers() {
		return db.getUsersHasPPOI();
	}

	public boolean learning(String userId) throws Exception {
		return learning(userId, new Date());
	}

	public boolean learning(String userId, Date now) throws Exception {

		logger.debug("Learning for user: {}", userId);

		// retrieve the PPOIs related to the userId
		List<PPOI> ppois = db.getAllPPOIs(userId, null);

		if (ppois.size() > 1) {

			MarkovNetwork mn = new MarkovNetwork(createConditions(), ppoisName(ppois));

			// retrieve the story of the user in the last 8 weeks
			List<Location> locations = db_remote.retrieveLocations(now, userId, properties.getNWeekBefore(), properties.getCutAccuracy(), properties.getALLFrom(), properties.getALLTo(), false, false, false, null);

			logger.info("n ppois {} n location {}", ppois.size(), locations.size());

			String last_mov = null;
			String last_valid_poi = "null";
			Date last_valid_date = null;
			Date previous_valid_date = null;
			String current_ppoi = null;

			Hashtable<String, Double> modality = new Hashtable<String, Double>();

			for (Location l : locations) {

				if (l.getLatitude() != 0) {

					if (previous_valid_date != null)
						updateModality(modality, l.getStatus(), l.getData().getTime() - previous_valid_date.getTime());

					// retrieve current poi, can be "null"
					current_ppoi = db.retrievePoiName(userId, l, ppois, properties.getDistanceCluster());
					if (current_ppoi == null)
						current_ppoi = "null";

					if (last_mov != null) {// wait at least two data
						if (!last_mov.equals(current_ppoi)) {
							// different status
							if (!current_ppoi.equals("null")) {
								if (!last_mov.equals("null")) {
									// exit from last
									exitingFrom(l, last_mov, current_ppoi);
									// entering in current
									enteringTo(l, last_mov, current_ppoi);
									transition(userId, mn, l, previous_valid_date, last_mov, current_ppoi, ppois, modality);
									modality = new Hashtable<String, Double>();
								} else {
									// entering in current
									enteringTo(l, last_mov, current_ppoi);
									transition(userId, mn, l, last_valid_date, last_valid_poi, current_ppoi, ppois, modality);
									modality = new Hashtable<String, Double>();
								}
							} else if (!last_mov.equals("null")) {
								// exit from last
								exitingFrom(l, last_mov, current_ppoi);
								last_valid_poi = last_mov;
								last_valid_date = previous_valid_date;// has to refer the last observed
							}
						} else {
							// logger.debug("{} ... we stay in {}", l.getData().toString(), current_ppoi);
						}
					}

					// update last poi
					last_mov = current_ppoi;

					previous_valid_date = l.getData();
				}
			}
			mi.write(userId, mn);
			mi.load(userId);
			return true; // learning done
		} else {
			// if there are not enough ppois, the markov should not exist anyway. maybe is obsolete?
			// we try to delete it anyway
			logger.debug("not enough ppois, deleting {}", userId);

			mi.delete(userId);

			return false;// learning not done
		}
	}

	private void updateModality(Hashtable<String, Double> modality, String status, Long seconds) {

		if ((status != null) && (!status.isEmpty()) && (seconds != null)) {
			Double d = modality.get(status);
			if (d == null)
				d = 0d;

			modality.put(status, d + seconds);
		}
	}

	private List<Integer> createConditions() {
		List<Integer> toreturn = new ArrayList<Integer>();
		toreturn.add(8);// slots
		toreturn.add(7);// days
		return toreturn;
	}

	private void transition(String userId, MarkovNetwork mn, Location l, Date data, String last_mov, String current_ppoi, List<PPOI> ppois, Hashtable<String, Double> modality) throws Exception {
		if (data != null) {

			Long durata = (l.getData().getTime() - data.getTime()) / 1000;

			if (durata > properties.getAlertThresholdStatusSeconds()) {
				// if (durata != db.getSuspiciousDuration(userId, data, durata, properties.getAlertRefreshStatusSeconds())) {
				// logger.debug("-----------------------------------------------> durata is wrong, return");
				// return;
				// }

				logger.debug("-----------------------------------------------> durata too long?");
				return;
			}

			List<Integer> condizioni = new ArrayList<>();
			condizioni.add(Utils.retrieveDaySlotInteger(data.getTime()));
			// condizioni.add(0);// not using condizioni on dayslot
			Calendar c = Calendar.getInstance();
			c.setTime(data);
			condizioni.add(c.get(Calendar.DAY_OF_WEEK) - 1);
			// condizioni.add(0);// not using condizioni on weekday

			/*
			 * Double distance = calcolateDistance(ppois, last_mov, current_ppoi);
			 * 
			 * if (Double.isNaN(distance)) distance = 9999d;
			 */

			// retrieve distance and modality from timeline
			List<AggregatedMobility> tls = tl.getAggregatedMobility(userId, data.getTime(), data.getTime() + durata * 1000, null, "en");

			if (durata != 0) {

				logger.debug("-----------------------------------------------> from {} to {} durata {} distance {} mobility {} slot {} day {}", last_mov, current_ppoi, durata, getDistance(tls), getHighest(tls),
						Utils.retrieveDaySlotInteger(data.getTime()),
						c.get(Calendar.DAY_OF_WEEK) - 1);

				// Trajectory t = new Trajectory(userId, SampleDataSource.retrieveDaySlotInteger(data.getTime()), c.get(Calendar.DAY_OF_WEEK) - 1, durata, last_mov, current_ppoi, distance, bestKey, accBest, data);

				// db.insertTrajector(t);

				mn.observe(current_ppoi, last_mov, condizioni, durata, getDistance(tls), getHighest(tls));
			} else {
				logger.debug("-----------------------------------------------> durata ==0 probably switch beetween gps and fused");
			}
		} else {
			logger.debug("-----------------------------------------------> there was a transition from {} to {} . WE DON'T KNOW DURATION", last_mov, current_ppoi, l.getData().getTime());
		}
	}

	private Double getDistance(List<AggregatedMobility> tls) {
		Double d = 0d;

		for (AggregatedMobility tl : tls) {
			d += tl.getMeters();
		}
		return d;
	}

	private String getHighest(List<AggregatedMobility> tls) {
		double max = 0;
		String maxS = null;
		for (AggregatedMobility tl : tls) {
			if (tl.getSeconds() >= max) {
				max = tl.getSeconds();
				maxS = tl.getStatus();
			}
		}
		return maxS;
	}

	// private String getHighest(Hashtable<String, Double> modality) {
	// Enumeration<String> i = modality.keys();
	// double max = 0;
	// String maxS = null;
	// while (i.hasMoreElements()) {
	// String s = i.nextElement();
	// double d = modality.get(s);
	// if (d >= max) {
	// max = d;
	// maxS = s;
	// }
	// }
	// return maxS;
	// }

	// private Double calcolateDistance(List<PPOI> ppois, String last_mov, String current_ppoi) {
	//
	// PPOI from = retrievePoi(last_mov, ppois);
	// PPOI to = retrievePoi(current_ppoi, ppois);
	//
	// return from.distance(to);
	// }

	// private PPOI retrievePoi(String last_mov, List<PPOI> ppois) {
	// for (PPOI ppoi : ppois) {
	// if (ppoi.getName().equals(last_mov))
	// return ppoi;
	// }
	// return null;
	// }

	private List<String> ppoisName(List<PPOI> ppois) {
		List<String> toreturn = new ArrayList<String>();
		for (PPOI ppoi : ppois) {
			toreturn.add(ppoi.getName());
		}
		return toreturn;
	}

	private void enteringTo(Location l, String last_ppoi, String current_ppoi) {
		// logger.debug("{} ENTERING. current {} last {}", l.getData().toString(), current_ppoi, last_ppoi);
	}

	private void exitingFrom(Location l, String last_ppoi, String current_ppoi) {
		// logger.debug("{} EXITING. current {} last {}", l.getData().toString(), current_ppoi, last_ppoi);
	}
}
