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
package edu.unifi.disit.userprofiler.ppois;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.unifi.disit.commons.datamodel.Location;
import edu.unifi.disit.commons.datamodel.PPOI;
import edu.unifi.disit.commons.datamodel.Position;
import edu.unifi.disit.commons.datamodel.SMLocation;
import edu.unifi.disit.commons.utils.Utils;
import edu.unifi.disit.engager_utils.SampleDataSource;
import edu.unifi.disit.userprofiler.ppois.datamodel.UserProfilerPPOI;

@Component
public class PopulateComplex {

	@Autowired
	GetPropertyValues properties;

	private static final Logger logger = LogManager.getLogger();

	private static final DBinterface db = DBinterface.getInstance();
	private static final edu.unifi.disit.userprofiler.externaldb.DBinterface db_remote = edu.unifi.disit.userprofiler.externaldb.DBinterface.getInstance();

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------main
	public List<String> retrieveUsersHome() {
		return retrieveUsersHome(new Date());
	}

	public List<String> retrieveUsersWork() {
		return retrieveUsersWork(new Date());
	}

	public List<String> retrieveUsersSchool() {
		return retrieveUsersSchool(new Date());
	}

	public List<String> retrieveUsersALL() {
		return retrieveUsersALL(new Date());
	}

	public List<String> retrieveUsersHome(Date now) {
		List<String> refuse = new ArrayList<String>();
		refuse.add("tourist");
		return db_remote.retrieveUsers(now, properties.getNHoursBefore(), properties.getCutAccuracy(), properties.getHomeFrom(), properties.getHomeTo(), true, refuse, null, "stay");
	}

	public List<String> retrieveUsersWork(Date now) {
		List<String> accept = new ArrayList<String>();
		accept.add("commuter");
		accept.add("citizen");
		accept.add("all");
		return db_remote.retrieveUsers(now, properties.getNHoursBefore(), properties.getCutAccuracy(), properties.getWorkFrom(), properties.getWorkTo(), true, null, accept, "stay");
	}

	public List<String> retrieveUsersSchool(Date now) {
		List<String> accept = new ArrayList<String>();
		accept.add("student");
		return db_remote.retrieveUsers(now, properties.getNHoursBefore(), properties.getCutAccuracy(), properties.getSchoolFrom(), properties.getSchoolTo(), true, null, accept, "stay");
	}

	public List<String> retrieveUsersALL(Date now) {
		return db_remote.retrieveUsers(now, properties.getNHoursBefore(), properties.getCutAccuracy(), properties.getALLFrom(), properties.getALLTo(), false, null, null, "stay");
	}

	public boolean retrieveHomePOI(String userId) {
		return retrieveHomePOI(userId, new Date());
	}

	public boolean retrieveWorkPOI(String userId) {
		return retrieveWorkPOI(userId, new Date());
	}

	public boolean retrieveSchoolPOI(String userId) {
		return retrieveSchoolPOI(userId, new Date());
	}

	public int retrieveALLPOI(String userId) {
		return retrieveALLPOI(userId, new Date());
	}

	public boolean retrieveHomePOI(String userId, Date now) {
		return retrievePOI(now, userId, "HOME", properties.getSogliaAccuracyHome(), properties.getHomeFrom(), properties.getHomeTo(), true);
	}

	public boolean retrieveWorkPOI(String userId, Date now) {
		return retrievePOI(now, userId, "WORK", properties.getSogliaAccuracyWork(), properties.getWorkFrom(), properties.getWorkTo(), true);
	}

	public boolean retrieveSchoolPOI(String userId, Date now) {
		return retrievePOI(now, userId, "SCHOOL", properties.getSogliaAccuracySchool(), properties.getSchoolFrom(), properties.getSchoolTo(), true);
	}

	public int retrieveALLPOI(String userId, Date now) {
		return retrieveAlternativePOI(now, userId, "PPOI", properties.getSogliaAccuracyALL(), properties.getALLFrom(), properties.getALLTo(), false);
	}

	// ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------private
	// return true if a poi was found (i.e. there is at least one valid)
	// location, and the accuracy of the best location is greater than soglia
	private boolean retrievePOI(Date now, String userId, String poiname, float soglia, String from, String to, boolean weekday) {
		boolean toreturn = false;

		// retrieve confirmation for this user. don't need any elaboration, we just check if we have at least one
		PPOI confppoi = db.getPPOI(userId, poiname, true, true);
		if (confppoi != null) {
			logger.debug("this {} for user {} is already confirmed, skipping", poiname, userId);
			toreturn = true;
		} else {

			// retrieve rejected for this user
			// put in ban list
			PPOI ppoi_to_remove = db.getPPOI(userId, poiname, true, false);
			if (ppoi_to_remove != null)
				db.deletePPOI(new UserProfilerPPOI(userId, ppoi_to_remove));

			// retrieve all the location from this user
			List<Location> locations = db_remote.retrieveLocations(now, userId, properties.getNWeekBefore(), properties.getCutAccuracy(), from, to, weekday, true, true, "stay");

			if (locations.size() < properties.getMinSoglia()) {
				logger.debug("user has still not enough entries, skipping");
				return false;
			}

			// aggregate all common locations - return in order
			List<Location> places = analyse(locations, userId);

			double total_seconds = count_total_sec(places);

			if (places.size() > 0) {
				float best_acc = (float) (places.get(0).getSeconds() / total_seconds);

				if (best_acc > soglia) {

					// if there is some pppoix with same gps already confirmed/rejected, use this infomration in the confirmation
					PPOI confirmedFromPreviousPPOI = getConfiremedFromPreviousPPOI(userId, places.get(0));
					Boolean confirmedFromPrevious = false;
					if (confirmedFromPreviousPPOI != null) {
						logger.warn("there qas a prvious confirmed here, get its info");
						confirmedFromPrevious = confirmedFromPreviousPPOI.getConfirmation();
					}

					SMLocation loc_data = Utils.retrieveLocation(places.get(0).getLatitude(), places.get(0).getLongitude(), properties.getServicemapURL(), properties.getReadTimeoutMillisecond());

					UserProfilerPPOI up = new UserProfilerPPOI(userId, poiname, places.get(0).getLatitude(), places.get(0).getLongitude(), best_acc, loc_data, confirmedFromPrevious, poiname);// create a new entry -> confirmation is
																																																// null

					db.updatePPOI(up);
					toreturn = true;
					logger.info("{} - OK:{}", userId, best_acc);

					if (confirmedFromPreviousPPOI != null) {
						logger.debug("we found an PPOIx close to {} that was already reject/confirm, cleaning", places.get(0));
						db.deletePPOI(new UserProfilerPPOI(userId, confirmedFromPreviousPPOI));
					}

				} else {
					// db.clean(userId, poiname);//TODO ... do we need it????????????
					logger.info("{} - KO:{}", userId, best_acc);
				}
			} else {
				logger.warn("Seems very strange. For user:{} poiname ({}) we noticed that there are no places found", userId, poiname);
			}

			logger.debug("size is:{}", places.size());
		}

		return toreturn;
	}

	private PPOI getConfiremedFromPreviousPPOI(String userId, Position p) {
		List<PPOI> ppois = db.getPPOIs(userId, "PPOI", true, true);

		if (ppois != null)
			for (PPOI ppoi : ppois) {
				if (ppoi.isClose(p, properties.getDistanceCluster()))
					return ppoi;
			}
		return null;
	}

	// insert in DB the PPOI that are not commonPPOI(WORK/SHOOL/...)
	// with at least 4 hours
	// return the number of insert PPOI
	private int retrieveAlternativePOI(Date now, String userId, String poiname, float soglia, String from, String to, boolean weekday) {
		int toreturn = 0;

		List<PPOI> ppois_toremove = db.getPPOIs(userId, poiname, true, false);

		if (ppois_toremove != null)
			for (PPOI ppoi : ppois_toremove) {
				// remove old PPOIx
				db.deletePPOI(new UserProfilerPPOI(userId, ppoi)); // do not remove if confirmed
			}

		List<PPOI> commonPPOIs = retrieveCommonPPOI(userId);

		List<PPOI> bannedPPOI_name = db.getPPOIs(userId, poiname, true, null);

		// retrieve all the location from this user, in stay status
		List<Location> locations = db_remote.retrieveLocations(now, userId, properties.getNWeekBefore(), properties.getCutAccuracy(), from, to, weekday, true, true, "stay");

		// aggregate all common locations - return in order
		List<Location> places = analyse(locations, userId);

		double total_seconds = count_total_sec(places);

		if (places.size() > 0) {
			for (Location l : places) {
				if (l.getSeconds() < properties.getMinSecondsForPPOI()) {
					logger.debug("vado sotto soglia, exit.");
					break;
				} else {
					logger.debug("check this {}", l);
				}
				if (!isCommonPPOI(l, commonPPOIs)) {
					SMLocation loc_data = Utils.retrieveLocation(l.getLatitude(), l.getLongitude(), properties.getServicemapURL(), properties.getReadTimeoutMillisecond());
					String myppoiname = db.getNewName(poiname, bannedPPOI_name);// calcolare the ppoiname, based on the first available not banned

					float acc = (float) (l.getSeconds() / total_seconds);

					UserProfilerPPOI up = new UserProfilerPPOI(userId, myppoiname, l.getLatitude(), l.getLongitude(), acc, loc_data, false, myppoiname);// create a new entry -> confirmation is null

					db.updatePPOI(up);
					if (bannedPPOI_name == null)
						bannedPPOI_name = new ArrayList<PPOI>();
					bannedPPOI_name.add(up.toPPOI());// add this to the list of name not avaialbe
					// new insert
					toreturn = toreturn + 1;
					logger.info("{} - added :{} with accuracy", userId, myppoiname, acc);
				}
			}
		} else {
			logger.warn("Seems very strange. For user:{} poiname ({}) we noticed that there are no places found", userId, poiname);
		}

		logger.debug("size was:{}", places.size());
		logger.debug("size is:{}", toreturn);

		return toreturn;
	}

	private boolean isCommonPPOI(Location l, List<PPOI> commonPPOIs) {
		for (PPOI commonPPOI : commonPPOIs) {
			if (l.isClose(commonPPOI, properties.getDistanceCluster()))
				return true;
		}

		return false;
	}

	private List<PPOI> retrieveCommonPPOI(String userId) {
		List<PPOI> commonPPOIs = new ArrayList<PPOI>();
		List<PPOI> commonPPOI = null;

		if (((commonPPOI = db.getPPOIs(userId, SampleDataSource.HOME, false, null)) != null)) {
			commonPPOIs.addAll(commonPPOI);
		}
		if (((commonPPOI = db.getPPOIs(userId, SampleDataSource.WORK, false, null)) != null)) {
			commonPPOIs.addAll(commonPPOI);
		}
		if (((commonPPOI = db.getPPOIs(userId, SampleDataSource.SCHOOL, false, null)) != null)) {
			commonPPOIs.addAll(commonPPOI);
		}
		if (((commonPPOI = db.getPPOIs(userId, SampleDataSource.PPOI, true, true)) != null)) {
			commonPPOIs.addAll(commonPPOI);
		}
		if (((commonPPOI = db.getPPOIs(userId, SampleDataSource.SPENT_TIME, true, null)) != null)) {
			commonPPOIs.addAll(commonPPOI);
		}
		if (((commonPPOI = db.getPPOIs(userId, SampleDataSource.USERGENERATED, true, null)) != null)) {
			commonPPOIs.addAll(commonPPOI);
		}

		return commonPPOIs;
	}

	private double count_total_sec(List<Location> places) {

		double toreturn = 0;

		// count total second, needed to calcultate later accurancy
		for (Location curr_location : places) {
			if (curr_location.getSeconds() > 0)
				toreturn += curr_location.getSeconds();
			logger.info(curr_location.toString());
		}
		return toreturn;
	}

	private List<Location> analyse(List<Location> locations, String userId) {

		List<Location> clusters = new ArrayList<Location>();

		Location curr_location = null;

		int i = 0;

		while ((curr_location = retrieveNOTclusterized(locations, clusters)) != null) {
			boolean foundmean = false;
			do {
				List<Location> closeto = retrieveCloseToMeAndNotClusteredAndNotBanned(curr_location, locations, clusters);

				if (closeto.size() == 0)
					logger.warn("seems there are no closed position. very strange");

				Location media = media(closeto, userId);
				if (media.isClose(curr_location, properties.getDistanceCluster() / 100)) { // means the mean didn't change so much
					foundmean = true;
					clusters.add(media);
					logger.trace("{} {}", i, media);
					logger.trace("size was {}", closeto.size());
					i++;
				} else {
					logger.trace("media was not close {}", closeto.size());
					curr_location = media;
				}
			} while (foundmean == false);
		}

		Collections.sort(clusters);

		return clusters;
	}

	private Location media(List<Location> closeto, String userId) {

		double latid = 0;
		double longit = 0;
		long seconds = 0l;

		for (int i = 0; i < closeto.size(); i++) {
			latid += closeto.get(i).getLatitude();
			longit += closeto.get(i).getLongitude();
			if (closeto.get(i).getSeconds().longValue() > 0) {

				if (closeto.get(i).getSeconds().longValue() > properties.getAlertThresholdStatusSeconds()) {
					logger.debug("suspicious duration {}, checking", closeto.get(i).getSeconds().longValue());

					seconds += properties.getAlertThresholdStatusSeconds();
				} else
					seconds += (long) closeto.get(i).getSeconds().longValue();
			}
		}

		return new Location(new Position(latid / closeto.size(), longit / closeto.size(), "claster"), new Date(), (double) seconds);
	}

	private List<Location> retrieveCloseToMeAndNotClusteredAndNotBanned(Location curr_location, List<Location> locations, List<Location> toreturn) {

		List<Location> close = new ArrayList<Location>();

		for (Location location : locations) {
			boolean isclose = location.isClose(curr_location, properties.getDistanceCluster());
			boolean iscluster = (isClusterized(location, toreturn) != null);
			if (isclose && !iscluster) {
				close.add(location);
			}

		}
		return close;
	}

	private Location retrieveNOTclusterized(List<Location> locations, List<Location> clusters) {

		for (Location location : locations) {

			if ((isClusterized(location, clusters) == null)) {
				return location;
			}
		}
		return null;

	}

	private Location isClusterized(Location location, List<Location> clusters) {
		for (Location cluster : clusters) {
			if (location.isClose(cluster, properties.getDistanceCluster())) {
				return location;
			}
		}
		return null;
	}
}
