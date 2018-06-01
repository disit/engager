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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.unifi.disit.commons.datamodel.MobilitySpeed;
import edu.unifi.disit.commons.datamodel.MobilityTrainingData;
import edu.unifi.disit.commons.datamodel.MobilityUserLocation;
import edu.unifi.disit.commons.datamodel.PPOI;
import edu.unifi.disit.commons.datamodel.Position;
import edu.unifi.disit.commons.datamodel.Prediction;
import edu.unifi.disit.commons.datamodel.SMLocation;
import edu.unifi.disit.commons.datamodel.userprofiler.Device;
import edu.unifi.disit.commons.datamodel.userprofiler.Location;
import edu.unifi.disit.commons.datamodel.userprofiler.TerminalModel;
import edu.unifi.disit.commons.datamodel.userprofiler.Timeline;
import edu.unifi.disit.commons.datamodel.userprofiler.TimelineCurrent;
import edu.unifi.disit.commons.utils.Utils;
import edu.unifi.disit.engager_utils.SampleDataSource;
import edu.unifi.disit.userprofiler.datamodel.DeviceDAO;
import edu.unifi.disit.userprofiler.datamodel.LocationDAO;
import edu.unifi.disit.userprofiler.datamodel.TimelineCurrentDAO;
import edu.unifi.disit.userprofiler.datamodel.TimelineDAO;
import edu.unifi.disit.userprofiler.ppois.GetPropertyValues;
import edu.unifi.disit.userprofiler.ppois.datamodel.UserProfilerLastPPOI;
import edu.unifi.disit.userprofiler.ppois.datamodel.UserProfilerPPOI;
import edu.unifi.disit.userprofiler.ppois.markov.Markowinterface;

@Component
public class DeviceServiceImpl implements IDeviceService {

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	IRPredictionService mobPred;

	@Autowired
	DeviceDAO devicedao;

	@Autowired
	TimelineCurrentDAO tlcRepo;

	@Autowired
	LocationDAO locationRepo;

	@Autowired
	TimelineDAO tRepo;

	@Autowired
	Markowinterface mi;

	@Autowired
	GetPropertyValues properties;

	@Autowired
	TerminalModel tm;

	@Autowired
	IDevicePPOIService ppoiService;

	@Autowired
	IDeviceUserActivitiesService deviceUAservice;

	private static final edu.unifi.disit.userprofiler.externaldb.DBinterface dbi_remote = edu.unifi.disit.userprofiler.externaldb.DBinterface.getInstance();
	private static final edu.unifi.disit.userprofiler.ppois.DBinterface dbi_local = edu.unifi.disit.userprofiler.ppois.DBinterface.getInstance();

	private static final String STAY = "Stay";
	private static final boolean USE_R = true;

	@PostConstruct
	public void init() {
	}

	@Override
	public List<Device> getAllDevices(String lang) {
		return devicedao.findAll();
	}

	// always return a valid instance... if the cache was not found, it return a new one!!!
	@Override
	public Device getCachedDevice(String deviceId, String lang) {

		Device d = devicedao.findByDeviceId(deviceId);

		if (d != null) {
			logger.debug("using cache for {}", deviceId);
			// ---------------PPOIS external DB
			d.setPpois(ppoiService.getAllPPOIs(deviceId, null, lang));
			d.setPpoiNext(getNextPpoi(deviceId, System.currentTimeMillis(), d));
		} else {
			logger.debug("NOT using cache for {}", deviceId);
			d = getNewDevice(deviceId, lang);
		}

		return d;
	}

	@Override
	public Device getDevice(String deviceId, String lang) {
		Device d = devicedao.findByDeviceId(deviceId);
		if (d == null) {
			return getNewDevice(deviceId, lang);
		} else
			return getDevice(deviceId, d, lang);
	}

	private Device getNewDevice(String deviceId, String lang) {

		// TODO before creating a new deviceID, be sure this device really exist in the ecosystem, otherwise any silly request generate a new entry!!!

		Device d = new Device();
		d.setDeviceId(deviceId);
		d.setTerminalInstallationDate(new Date());
		return getDevice(deviceId, d, lang);
	}

	private Device getDevice(String deviceId, Device d, String lang) {

		// ---------------TERMINAL
		d = updateTerminalInfo(d);

		// ---------------USER ACTIVITIES
		d.setUserActivities(deviceUAservice.refreshUserActivies(deviceId, lang));

		// ---------------PPOIS
		d.setPpois(ppoiService.getAllPPOIs(deviceId, null, lang));// not rejected
		d.setPpoiNext(getNextPpoi(deviceId, System.currentTimeMillis(), d));

		d = devicedao.save(d);

		return d;
	}

	@Override
	public Device updateTerminalInfo(Device device) {
		Object[] o1 = dbi_remote.getTerminalInfo(device.getDeviceId());
		if ((o1 != null) && (o1[0] != null))
			device.setTerminalAppID((String) o1[0]);
		if ((o1 != null) && (o1[1] != null))
			device.setTerminalModel((String) o1[1]);
		if ((o1 != null) && (o1[2] != null))
			device.setTerminalVersion((String) o1[2]);
		return device;
	}

	public Prediction getNextPpoi(String deviceId, Long when, Device d) {

		String search_ppoi = null; // next prediction depend on current (if not null) or last (if not null) --> otherwise we cannot prediction
		if (d.getPpoiCurrent() != null) {
			search_ppoi = d.getPpoiCurrent();
		} else if (d.getPpoiPrevious() != null) {
			search_ppoi = d.getPpoiPrevious();
		}

		Prediction toreturn = null;

		try {
			toreturn = mi.predict(deviceId, when, search_ppoi);
		} catch (Exception e) {
			logger.warn("Catched exception for user {}, trying to predict next step", deviceId, e);
		}

		return toreturn;
	}

	@Override
	public Device updateLocationDevice(String deviceId, Double latitude, Double longitude, Long when, String mobility_mode, Double speed, String profile,
			String terminal_lang, Double accuracy, String provider, Double mean_speed, Double avg_lin_acc_magn, Double lin_acc_x, Double lin_acc_y, Double lin_acc_z) {

		// retrieve cached version of device
		Device device = getCachedDevice(deviceId, terminal_lang);// TODO shall we use terminal_lang as lang?

		// check if this message is in the correct order (i mean, i can learn just if the current time is > timelinecurrent.date+timelinecurrent.seconds
		if ((device.getTimelineCurrent() != null) && (when <= (device.getTimelineCurrent().getDate().getTime() + device.getTimelineCurrent().getSeconds() * 1000))) {
			logger.warn("this entry is older, probably a repost {}", deviceId);
			return device;
		}

		// override some basic info
		device.setTerminal_profile(profile);
		device.setTerminal_language(new Locale(terminal_lang));

		device.setPpoiCurrent(null);
		device.setPpoiPrevious(null);
		device.setPpoiPreviousHowlong(null);
		device.setPpoiPreviousDistance(null);
		if ((latitude != null) && (longitude != null)) {

			// retrieve: PpoiCurrent, PpoiPrevious, PpoiPreviousHowlong, PpoiPreviousDistance---------------------------------------------------------------------------------------------------------------------------------------------
			// retrieve ppois of user, we need later on //this not include null
			List<PPOI> ppois = ppoiService.getAllPPOIs(deviceId, null, terminal_lang);

			if ((ppois != null) && (ppois.size() != 0)) {

				UserProfilerLastPPOI last_poi = dbi_local.getLast(deviceId, false);
				UserProfilerLastPPOI last_transition = dbi_local.getLast(deviceId, true);

				if ((last_poi != null) && (dbi_local.retrievePosition(ppois, last_poi.getPpoi()) == null)) {
					// if last_poi is valid, BUT in the meantime this ppoi is not valid anymore, set this ppoi as null
					last_poi = null;
				}

				boolean last_was_transtion = true;
				boolean first_run = false;
				if ((last_poi == null) && (last_transition != null)) {
					last_was_transtion = true;
				} else if ((last_poi != null) && (last_transition == null)) {
					last_was_transtion = false;
				} else if ((last_poi == null) && (last_transition == null)) {
					first_run = true;
				} else if (last_poi.getTime().after(last_transition.getTime()))
					last_was_transtion = false;

				double distance_tot = 0;
				if (!first_run) {
					if (last_was_transtion) {
						logger.debug("check distance in progress");
						PPOI poi_null = dbi_local.getPPOI(deviceId, "null", false, null);
						if (poi_null != null)
							distance_tot = poi_null.distance(new Position(latitude, longitude)) + last_transition.getDistance();
						else
							logger.warn("seems strange since we're in transition but there is no null entry");

					} else {
						logger.debug("check distance from last poi");

						distance_tot = dbi_local.retrievePosition(ppois, last_poi.getPpoi()).distance(new Position(latitude, longitude)) + last_poi.getDistance();

					}
				}

				logger.debug("so far tot distance is:{}", distance_tot);

				// retrieve the current result, can be null
				String current = dbi_local.retrievePoiName(deviceId, latitude, longitude, ppois, properties.getDistanceCluster());

				// if current==null, we're in transiction
				if (current == null) {// update position of transaction, so later on i can check the distance
					PPOI poi_null = dbi_local.getPPOI(deviceId, "null", false, null);
					if (poi_null == null) {
						poi_null = new PPOI();
						poi_null.setName("null");
					}
					poi_null.setLatitude(latitude);
					poi_null.setLongitude(longitude);

					dbi_local.updatePPOI(new UserProfilerPPOI(deviceId, poi_null)); // in transition the cpz can be set to null, the address can be null
				}

				// always update the last transaction with new distance new time ...
				double newdistance = dbi_local.updateLast(deviceId, current, when, distance_tot);

				device.setPpoiCurrent(current);
				if (last_poi != null) {
					device.setPpoiPrevious(last_poi.getPpoi());
					device.setPpoiPreviousHowlong((long) (when - last_poi.getTime().getTime()) / 1000);
				}
				device.setPpoiPreviousDistance(newdistance);
			}

			// retrieve averageSpeed----------------------------------------------------------------------------------------------------------------------------------------------------------------------
			device.setAverageSpeed(null);

			// insert just if valid entry
			if ((speed != null) && (speed != 0.0001) && (speed != 0.0002)) {

				// to return a valid speed, we check if
				// -in the last 11 minutes
				// -we need at least TWO entries
				// -has to be a valid data (speed!=0.001)
				// -continusly moving (not two consecutive entry close)

				// insert corrent
				MobilitySpeed ms = new MobilitySpeed();
				ms.setDeviceId(deviceId);
				ms.setTime(new Date(when));
				ms.setLatitude(latitude);
				ms.setLongitude(longitude);
				ms.setSpeed(speed);
				dbi_local.addMobilitySpeed(ms);// lo scrivo su disco
			}

			// 12 minuti e 30 sec are: 12*60*1000 + 30*1000
			Date limite = new Date(when - 750000);

			// remove old
			dbi_local.deleteMobilitySpeed(deviceId, limite);
			// check remains
			List<MobilitySpeed> lastMS = dbi_local.getMobilitySpeed(deviceId);

			if (lastMS.size() < 2) {// at least 2 observation
				logger.debug("no enough observation in last 11 minutes to mobspeed");
			} else {// good entry
				logger.debug("good entry mobspeed");
				device.setAverageSpeed(calcolateMean(lastMS));
			}

			Boolean mobileoff = false;
			if ((device.getLastUpdate() != null) && ((when - device.getLastUpdate().getTime()) / 1000) > properties.getAlertRefreshStatusSeconds()) {// ----------------------------------------------------------------------regola C
				logger.debug("regola D, mobile off");
				mobileoff = true;
			}

			// --------------------------check if i-m moving-----------------------------------------------------------------------------------------------------------------------------------------------
			if ((device.getCurrentPositionLat() != null) && (device.getCurrentPositionLong() != null)// -------------------------------------------------------------------regola B
					&& (Position.isClose(device.getCurrentPositionLat(), device.getCurrentPositionLong(), latitude, longitude, properties.getDistanceCluster() * 2))) {
				logger.debug("regola A, close to before");
				mobility_mode = STAY;
			} else if ((speed == null) || /* (mean_speed == null) || */ (speed == 0.0001) || (speed == 0.0002) /* || (mean_speed == 0.0001) || (mean_speed == 0.0002) */) {// -----------------------regola A
				logger.debug("regola B, speeds not valid");
				mobility_mode = "unknown";
			} else {

				if (USE_R) {
					logger.debug("using R prediction");
					MobilityTrainingData mtd = new MobilityTrainingData(accuracy, provider, speed, mean_speed, avg_lin_acc_magn, lin_acc_x, lin_acc_y, lin_acc_z, profile);

					mtd.fillServiceMapData(latitude, longitude, properties.getServicemapURL(), properties.getReadTimeoutMillisecond());

					mtd.fillTimeData(when);

					mtd.fillDeviceSpecs(tm.get(device.getTerminalModel()));

					mtd.fillHistoricalData(lastMS, when);

					logger.debug("Prediction on {}", mtd);
					logger.debug("Prediction Scenario on {}", mtd.getScenario());
					logger.debug("Prediction Colums on {}", mtd.getScenarioColumnsName());
					logger.debug("Prediction Value on {}", mtd.getScenarioColumnsValue());

					mobility_mode = mobPred.predict(mtd);

					logger.debug("Prediction return {}", mobility_mode);
				} else {
					logger.debug("not using R prediction");
				}
			}

			// correct the situation, if they are distance and still Stay --> problem
			if ((device.getCurrentPositionLat() != null) && (device.getCurrentPositionLong() != null) &&
					(!Position.isClose(device.getCurrentPositionLat(), device.getCurrentPositionLong(), latitude, longitude, properties.getDistanceCluster() * 2))// -------------regola D
					&& (mobility_mode.equals(STAY))) {
				logger.debug("regola C, predicted stays but we're distant");
				mobility_mode = "unknown";
			}

			// the status has to be always different to null or stringa vuota //BACKWORK compatibility
			if ((mobility_mode == null) || (mobility_mode.length() == 0)) {
				mobility_mode = "unknown";// TODO use constants
			}

			// update position
			device.setCurrentPositionLat(latitude);
			device.setCurrentPositionLong(longitude);
			device.setCurrentPositionAccuracy(accuracy);

			// update last update
			device.setLastUpdate(new Date(when));

			// ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
			if (mobility_mode != null) {

				TimelineCurrent tlc = device.getTimelineCurrent();

				if (tlc == null) {
					// first run, create new timelinecurrent
					logger.debug("new timeline");
					tlc = new TimelineCurrent(device, mobility_mode, when);
				} else {
					// at least second run
					if (mobileoff) {
						logger.debug("regola D, mobile off");
						tlc.setStatus("unknown");
					}
					if ((tlc.getStatus() != null) && (tlc.getStatus().equals(mobility_mode))) {
						// same status, update always last
						logger.debug("continue timeline");
						tlc.setSeconds((when - tlc.getDate().getTime()) / 1000);
						tlc.addMeters(Position.distance(tlc.getLatitude(), tlc.getLongitude(), latitude, longitude, "K") * 1000);

					} else {

						// if in timeline current we have stay<4 minute, ignore timeline current (assimilate to next)
						if ((tlc.getStatus().equals(STAY)) && (((when - tlc.getDate().getTime()) / 1000) < 240)) {// ---------------------------------------------------regola E
							logger.debug("regola E, stay too short");
							// non scarico

							// update current
							tlc.setStatus(mobility_mode);
							// date of start is the previous one
							tlc.setSeconds((when - tlc.getDate().getTime()) / 1000);// as above
							tlc.addMeters(Position.distance(tlc.getLatitude(), tlc.getLongitude(), latitude, longitude, "K") * 1000);// as above

						} else {
							// update previous e scarica
							Timeline t = tlc.toTimeline();
							t.setSeconds((when - tlc.getDate().getTime()) / 1000);
							t.addMeters(Position.distance(tlc.getLatitude(), tlc.getLongitude(), latitude, longitude, "K") * 1000);
							// t.setLatitude(latitude);//don't need to update timeline to last entry! this belong to current TIMELINE
							// t.setLongitude(longitude);//don't need to update timeline to last entry! this belong to current TIMELINE
							t.resetId();

							scaricoTimeline(t);
							device.setTimelinePrevious(t);// wherever SCARICO, add to previous (this timeline is finished)

							// update current
							tlc.setStatus(mobility_mode);
							tlc.setDateTimestamp(when);
							tlc.resetSeconds();
							tlc.resetMeters();
						}

					}
				}

				if (mobility_mode.equals(STAY) && (tlc.getLatitude() != null) && (tlc.getLongitude() != null)) {// if we keep in stay, we memorize the centroide
					// simple approx to middle point
					tlc.setLatitude((tlc.getLatitude() + latitude) / 2);
					tlc.setLongitude((tlc.getLongitude() + longitude) / 2);
				} else {
					tlc.setLatitude(latitude);
					tlc.setLongitude(longitude);
				}
				logger.debug("update timeline {}", tlc);
				device.setTimelineCurrent(tlc);
			}

			List<Location> locations = locationRepo.findByDeviceDeviceIdOrderByTimeDesc(deviceId);

			// update LOCATION if last entry was
			// 1( first time
			// OR
			// 2) far from last gps
			// AND
			// 3) passed time from last time

			if ((locations == null) || (locations.size() == 0) ||
					((!Position.isClose(latitude, longitude, locations.get(0).getLatitude(), locations.get(0).getLongitude(), properties.getKMetersForRefreshLocation()))
							&&
							((when - locations.get(0).getTime().getTime()) > (properties.getSecondsForRefreshLocation() * 1000)))) {

				logger.debug("updating terminal municipility and province");
				updateTerminalInfoMP(device, when);
			}
		}

		else

		{
			logger.debug("gps nullo");
		}

		if (deviceId.equals("af97e11488be5af2408ab27ddd90d52ad763b5d4ff1e5f89e6fc378fdf785f75") && (device.getGroups() != null) && (device.getGroups().size() == 0)) {
			logger.debug("debugg adding uno here");
			List<String> groups = new ArrayList<String>();
			groups.add(SampleDataSource.GROUP_WINNER_USB);
			device.setGroups(new HashSet<String>(groups));
		}

		// update the cached version
		device = devicedao.save(device);

		return device;
	}

	private void updateTerminalInfoMP(Device device, Long when) {

		SMLocation MP = Utils.retrieveLocation(device.getCurrentPositionLat(), device.getCurrentPositionLong(), properties.getServicemapURL(), properties.getReadTimeoutMillisecond());

		if (MP.valid()) {

			Location l = new Location(MP, new Date(when), device.getCurrentPositionLat(), device.getCurrentPositionLong(), device);

			locationRepo.save(l);
		}

	}

	public void scaricoTimeline(Timeline t) {

		logger.debug("scarico timeline {}", t);

		// if timeline is "a cavallo" of 24.00, split in two
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(t.getDate().getTime() + new Long(t.getSeconds()) * 1000);
		Integer endDate = c.get(Calendar.DAY_OF_YEAR);
		c.setTime(t.getDate());
		Integer startDate = c.get(Calendar.DAY_OF_YEAR);

		if (startDate.intValue() != endDate.intValue()) {

			logger.debug("splitting timeline");

			logger.debug("start date {}", startDate);
			logger.debug("end date {}", endDate);

			c.add(Calendar.DAY_OF_YEAR, 1);
			// first part
			Long mezzanotte = trim(c.getTime());

			Timeline t1 = new Timeline();
			t1.setDevice(t.getDevice());
			t1.setDate(t.getDate());
			t1.setSeconds(((mezzanotte - t.getDate().getTime()) / 1000));
			t1.setMeters((long) (t.getMeters() * ((double) t1.getSeconds() / t.getSeconds())));
			t1.setStatus(t.getStatus());

			List<MobilityUserLocation> l = dbi_remote.retrieveMobilityLocations(t.getDevice().getDeviceId(), t.getDate(), new Date(mezzanotte));
			if (l.size() != 0) {
				t1.setLatitude(l.get(l.size() - 1).getLatitude());
				t1.setLongitude(l.get(l.size() - 1).getLongitude());
			} else {
				logger.warn("cannot find a split gps");
				t1.setLatitude(t.getLatitude());
				t1.setLongitude(t.getLongitude());
			}
			tRepo.save(t1);
			logger.debug("first {}", t1);

			int number_of_iteration = endDate - startDate - 1;
			if (number_of_iteration < 0) {
				number_of_iteration = number_of_iteration + c.getActualMaximum(Calendar.DAY_OF_YEAR);
			}

			for (int i = 0; i < number_of_iteration; i++) {

				c.add(Calendar.DAY_OF_YEAR, 1);

				Timeline tx = new Timeline();
				tx.setDevice(t.getDevice());
				tx.setDate(new Timestamp(mezzanotte));
				tx.setSeconds(86400L);
				tx.setMeters((long) (t.getMeters() * ((double) tx.getSeconds() / t.getSeconds())));
				tx.setStatus(t.getStatus());
				l = dbi_remote.retrieveMobilityLocations(t.getDevice().getDeviceId(), t.getDate(), new Date(mezzanotte));
				if (l.size() != 0) {
					tx.setLatitude(l.get(l.size() - 1).getLatitude());
					tx.setLongitude(l.get(l.size() - 1).getLongitude());
				} else {
					logger.warn("cannot find a split gps");
					tx.setLatitude(t.getLatitude());
					tx.setLongitude(t.getLongitude());
				}
				logger.debug("middle {}", tx);

				mezzanotte = trim(c.getTime());

				tRepo.save(tx);

			}

			// last part
			Timeline t2 = new Timeline();
			t2.setDevice(t.getDevice());
			t2.setDate(new Timestamp(mezzanotte));
			t2.setSeconds(t.getSeconds() - t1.getSeconds() - 86400 * (number_of_iteration));
			t2.setMeters((long) (t.getMeters() * ((double) t2.getSeconds() / t.getSeconds())));
			t2.setStatus(t.getStatus());
			t2.setLatitude(t.getLatitude());
			t2.setLongitude(t.getLongitude());

			logger.debug("last {}", t2);
			tRepo.save(t2);

		} else {
			tRepo.save(t);
		}

	}

	public Long trim(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		return calendar.getTimeInMillis();
	}

	// -------------------------------------------------------------------------------------------------------private
	private Double calcolateMean(List<MobilitySpeed> lastMS) {// used for average speed in movement
		Double average = 0d;
		Position current = null, previous = null;
		for (MobilitySpeed entity : lastMS) {
			logger.debug("analize for calcolate mean: {}", lastMS);
			current = new Position(entity.getLatitude(), entity.getLongitude());
			if ((previous != null) && (previous.isClose(current, properties.getDistanceCluster()))) {// if position are close, return zero
				logger.debug("too close no good mobspeed");
				return 0d;
			}
			average = average + (Double) entity.getSpeed();
			previous = current;
		}
		return (double) average / (double) lastMS.size();
	}

	@Override
	public void updateInterestDevice(String deviceId, String serviceUri, Integer rate, String type) {
		dbi_local.addUserServiceInterest(deviceId, serviceUri, type, rate);
	}

	@Override
	public void updateTime(String deviceId, String ppoiName, Boolean confirmation, Long time) {

		// retrieve the position of the user in this moment
		Position coordinate = dbi_remote.retrievePosition(deviceId, new Date(time));

		if (coordinate != null) {

			// if ppoi is specified,
			if (ppoiName != null) {
				SMLocation loc_data = Utils.retrieveLocation(coordinate.getLatitude(), coordinate.getLongitude(), properties.getServicemapURL(), properties.getReadTimeoutMillisecond());

				PPOI myppoi = dbi_local.getPPOI(deviceId, ppoiName, true, false);

				if ((ppoiName.equals(SampleDataSource.SPENT_TIME)) || // spent time scenario
						((myppoi == null))) {// home/work/shool scenario AND not already confirmed

					if (myppoi == null) {
						myppoi = new PPOI();
						myppoi.setName(ppoiName);
					}
					myppoi.setLatitude(coordinate.getLatitude());
					myppoi.setLongitude(coordinate.getLongitude());
					myppoi.setAccuracy(1f);
					myppoi.setLocationData(loc_data);
					myppoi.setConfirmation(true);
					dbi_local.updatePPOI(new UserProfilerPPOI(deviceId, myppoi));
				} else
					logger.error("it's not spent time and this ppoi was already confirmed");
			}
			// if ppoiName is not specified, we need to retrieve
			else {
				PPOI closePPOI = dbi_local.getClosePPOI(deviceId, SampleDataSource.PPOI, coordinate, properties.getDistanceCluster());
				if (closePPOI == null) {
					logger.warn("that's strange since we didn't found any PPOI close to the confirmed position");
				} else if (closePPOI.getConfirmation() == false) {
					closePPOI.setConfirmation(confirmation);
					dbi_local.updatePPOI(deviceId, closePPOI);
				} else {
					logger.warn("already confirmed/rejected {}");
				}
			}
		}
	}
}
