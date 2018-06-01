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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.unifi.disit.commons.datamodel.AggregatedMobility;
import edu.unifi.disit.commons.datamodel.Location;
import edu.unifi.disit.commons.datamodel.MobilitySpeed;
import edu.unifi.disit.commons.datamodel.MobilityTrainingData;
import edu.unifi.disit.commons.datamodel.MobilityUserLocation;
import edu.unifi.disit.commons.datamodel.Position;
import edu.unifi.disit.commons.datamodel.userprofiler.Device;
import edu.unifi.disit.commons.datamodel.userprofiler.TerminalModel;
import edu.unifi.disit.commons.datamodel.userprofiler.Timeline;
import edu.unifi.disit.commons.utils.DeviceSpecs;
import edu.unifi.disit.userprofiler.datamodel.DeviceDAO;
import edu.unifi.disit.userprofiler.datamodel.TimelineCurrentDAO;
import edu.unifi.disit.userprofiler.datamodel.TimelineDAO;
import edu.unifi.disit.userprofiler.ppois.GetPropertyValues;

@Component
public class DeviceTimelineServiceImpl implements IDeviceTimelineService {

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	TerminalModel tm;

	@Autowired
	GetPropertyValues properties;

	@Autowired
	TimelineDAO timelineRepo;

	@Autowired
	TimelineCurrentDAO timelineCurrentRepo;

	@Autowired
	DeviceDAO deviceRepo;

	private static final edu.unifi.disit.userprofiler.externaldb.DBinterface dbi_remote = edu.unifi.disit.userprofiler.externaldb.DBinterface.getInstance();

	@Override // mintime in minutes valid just for lastStatys
	public List<Timeline> getTimeline(String deviceId, Long from, Long to, String lastStatus, Long minTime, String lang) {
		List<Timeline> timelines = getTimeline(deviceId, from, to, lang);
		if (lastStatus == null)// if lastStatus is not specified, return all timelines
			return timelines;
		else

			// go back to the last previous lastStatus (not consider current status)
			return extractlastPrevious(timelines, lastStatus, minTime);

	}

	private List<Timeline> extractlastPrevious(List<Timeline> timelines, String lastStatus, Long minTime) {

		logger.debug("looking {} in", lastStatus);
		for (int i = 0; i < timelines.size(); i++) {
			logger.debug("{} {}", i, timelines.get(i));
		}

		int index = timelines.size() - 2;// not consider current status
		int foundindex = -1;
		while ((foundindex == -1) && (index >= 0)) {
			if (timelines.get(index).getStatus().equals(lastStatus) && (timelines.get(index).getSeconds() > minTime * 60))
				foundindex = index;
			index--;
		}

		if (foundindex == -1) {
			logger.debug("not found");
			return null;
		}

		else {
			logger.debug("found {}", foundindex);
			return timelines.subList(foundindex, timelines.size());
		}

	}

	private List<Timeline> getTimeline(String deviceId, Long from, Long to, String lang) {

		if (from != null)
			logger.debug("from:{}", new Date(from));
		if (to != null)
			logger.debug("to:{}", new Date(to));

		List<Timeline> toreturn = new ArrayList<Timeline>();

		Device device = deviceRepo.findByDeviceId(deviceId);

		// first border
		if (from == null)// always specify from
			from = device.getTerminalInstallationDate().getTime();// TODO what if terminal installation is not available
		else {
			Timeline first = timelineRepo.findTopByDeviceDeviceIdAndDateBeforeOrderByDateDesc(deviceId, new Date(from));
			if (first != null) {
				logger.debug("first is {}", first);
				Timeline prima = adjustFirst(first, from);
				if (prima != null)
					toreturn.add(prima);
			}
		}

		if (to == null) // always specify to
			to = System.currentTimeMillis();

		// central border
		toreturn.addAll(timelineRepo.findByDeviceDeviceIdAndDateBetween(deviceId, new Date(from), new Date(to)));

		// last border
		if (toreturn.size() > 1) {// check if there are data
			Timeline last = toreturn.get(toreturn.size() - 1);

			if (to > last.getDate().getTime() + last.getSeconds() * 1000) {
				logger.debug("no need for current");
				toreturn.add(device.getTimelineCurrent().toTimeline());
			}

			adjustLast(toreturn, to);
		} else
			logger.debug("nodata");

		return toreturn;
	}

	private void adjustLast(List<Timeline> t, Long d) {

		Long tot_seconds = t.get(t.size() - 1).getSeconds();

		Long adjusted_seconds = Math.min((d - t.get(t.size() - 1).getDate().getTime()) / 1000, t.get(t.size() - 1).getSeconds());

		if (adjusted_seconds <= 0) {
			t.remove(t.size() - 1);
			return;
		}

		// adjust seconds
		t.get(t.size() - 1).setSeconds(adjusted_seconds);

		if (tot_seconds != 0) {
			Long percentage = (long) (adjusted_seconds / tot_seconds);

			// adjust meter basics on percentage
			t.get(t.size() - 1).setMeters(t.get(t.size() - 1).getMeters() * percentage);
		}
	}

	private Timeline adjustFirst(Timeline t, Long d) {

		Long tot_seconds = t.getSeconds();

		logger.debug("adj first {} {} {}", t.getDate().getTime(), t.getSeconds(), d);

		Long adjusted_seconds = ((t.getDate().getTime() + t.getSeconds() * 1000 - d) / 1000);
		if (adjusted_seconds <= 0) {
			// no adjustement was found (previous timeline is BEFORE d)
			return null;
		}

		// adjust seconds
		t.setSeconds(adjusted_seconds);

		if (tot_seconds != 0) {
			Long percentage = (long) (adjusted_seconds / tot_seconds);
			// adjust meter basics on percentage
			t.setMeters(t.getMeters() * percentage);
		}

		// adjust start date
		t.setDate(new Timestamp(d));

		return t;
	}

	@Override
	public List<AggregatedMobility> getAggregatedMobility(String deviceId, Long from, Long to, String status, String lang) {
		List<Timeline> ts = getTimeline(deviceId, from, to, lang);

		return aggregate(ts, status);
	}

	private List<AggregatedMobility> aggregate(List<Timeline> ts, String status) {

		Hashtable<String, AggregatedMobility> toreturn = new Hashtable<String, AggregatedMobility>();

		for (Timeline t : ts) {

			if ((status == null) || (status.equals(t.getStatus()))) {

				logger.debug(t);

				AggregatedMobility current = null;
				if ((current = toreturn.get(t.getStatus())) == null) {

					toreturn.put(t.getStatus(), new AggregatedMobility(t.getStatus(), t.getSeconds(), t.getMeters()));
				} else
					current.add(t);
			}
		}

		List<AggregatedMobility> l = new ArrayList<AggregatedMobility>(toreturn.values());
		for (AggregatedMobility am : l) {
			logger.debug("aggregated {}", am);
		}

		return l;
	}

	@Override
	public List<Position> getPositions(String deviceId, Long from, Long to, String lang) {

		Device device = deviceRepo.findByDeviceId(deviceId);

		if (from == null)// always specify from
			from = device.getTerminalInstallationDate().getTime();

		if (to == null) // always specify to
			to = System.currentTimeMillis();

		List<Location> ls = dbi_remote.retrieveLocations(deviceId, new Date(from), new Date(to));

		List<Position> ps = new ArrayList<Position>();

		for (Location l : ls) {
			if (l.getLatitude() != 0)
				ps.add(l);
		}

		return ps;
	}

	@Override
	public List<Timeline> postTimeline(String deviceId, Long from, Long to, String mobility, String lang) throws JsonProcessingException, ParseException, IOException {

		Date to_d = new Date(to);

		// update any timeline in this range to new mobility
		List<Timeline> tls = timelineRepo.findByDeviceDeviceIdAndDateBetween(deviceId, new Date(from), to_d);

		if ((tls.size() > 0) && (tls.get(tls.size() - 1).getDate().getTime() == to)) {
			logger.debug("reached edge");
			tls.remove(tls.size() - 1);
		}

		for (Timeline tl : tls) {
			logger.debug("updating {}", tl);
			tl.setStatus(convert(mobility));
			logger.debug("updated {}", tl);
			timelineRepo.save(tl);
		}

		// create file for updating
		if (!mobility.equals("unknown"))// TODO use constant
			prepareDataset(deviceId, from, to, mobility);// TODO fill dataset

		return getTimeline(deviceId, from, to, lang);
	}

	private String convert(String mobility) {

		// # mttm$modalityClass9[mttm$modality=="1"]="Stay"
		// # mttm$modalityClass9[mttm$modality=="2"]="Walk"
		// # mttm$modalityClass9[mttm$modality=="4"]="Bike"
		// # mttm$modalityClass9[mttm$modality=="5"]="Motorbike"
		// # mttm$modalityClass9[mttm$modality=="6"]="Bus"
		// # mttm$modalityClass9[mttm$modality=="7"]="Tram"
		// # mttm$modalityClass9[mttm$modality=="8"]="Car"
		// # mttm$modalityClass9[mttm$modality=="9"]="Train"

		switch (mobility) {
		case "Stay":
			return "Stay";
		case "Walk":
			return "Walk";
		case "Run":
			return "Walk";
		case "Bike":
			return "PrivateTransport";
		case "Motorbike":
			return "PrivateTransport";
		case "Bus":
			return "PublicTransport";
		case "Tram":
			return "PublicTransport";
		case "Car":
			return "PrivateTransport";
		case "Train":
			return "PublicTransport";
		case "unknown":
			return "unknown";
		default:
			return "unknown";
		}
	}

	@Override
	public void prepareDataset(String deviceId, Long fromS, Long toS, String mobility_status) throws ParseException, JsonProcessingException, IOException {

		// retrieve deviceSpecs
		DeviceSpecs ds = tm.get(deviceRepo.findByDeviceId(deviceId).getTerminalModel());

		logger.debug("..............preparedataset for {} from {} to {} on {}", deviceId, fromS, toS, mobility_status);

		// remove gps null
		// order by data
		List<MobilityUserLocation> uls = dbi_remote.retrieveMobilityLocations(deviceId, fromS, toS);

		logger.debug("data are # {}", uls.size());

		MobilityUserLocation old_mob = null;
		int progress = 0;

		List<MobilityTrainingData> toreturn = new ArrayList<MobilityTrainingData>();

		for (int i = 0; i < uls.size() - 1; i++) {
			MobilityUserLocation ul = uls.get(i);

			if ((old_mob != null) && (old_mob.getData().equals(ul.getData()))) {
				System.out.println("same that before");
				continue;
			}

			progress++;

			logger.debug(progress + " on " + uls.size() + " current is:" + ul.toString() + "\r ");

			if ((ul.getSpeed() != 0.0001) && (ul.getSpeed() != 0.0002)) {
				toreturn.add(filling(deviceId, ul, old_mob, ds, uls, mobility_status));
			}

			old_mob = ul;

		}

		logger.debug("data ripulita are # {}", toreturn.size());

		if (toreturn.size() > 0)
			writeTO(deviceId + uls.get(0).getData().getTime() + ".csv", toreturn);
	}

	private void writeTO(String string, List<MobilityTrainingData> mtds) throws IOException {

		File f = new File(properties.getRDatasetPath() + string);

		BufferedWriter writer = new BufferedWriter(new FileWriter(f));

		if (mtds.size() > 0) {
			writer.append(mtds.get(0).getScenarioColumnsNameAll());
			writer.newLine();
		}

		int i = 1;

		for (MobilityTrainingData mtd : mtds) {
			writer.append("\"" + i + "\",");
			writer.append(mtd.getScenarioColumnsValueAll());
			writer.newLine();
			i++;
		}

		writer.flush();
		writer.close();
	}

	private MobilityTrainingData filling(String deviceId, MobilityUserLocation ul, MobilityUserLocation old, DeviceSpecs ds, List<MobilityUserLocation> muls, String mobility_status) {

		MobilityTrainingData mtd = new MobilityTrainingData(ul.getAccuracy(), ul.getProvider(), ul.getSpeed(), ul.getMean_speed(), ul.getAcc_magni(), ul.getAcc_x(), ul.getAcc_y(), ul.getAcc_z(), ul.getProfile());

		mtd.fillServiceMapData(ul.getLatitude(), ul.getLongitude(), properties.getServicemapURL(), properties.getReadTimeoutMillisecond());

		mtd.fillTimeData(ul.getData().getTime());

		mtd.fillDeviceSpecs(ds);

		Long from = ul.getData().getTime() - 750000;// 12min 30 sec are= 30+12*60=750 sec =
		Long to = ul.getData().getTime();

		// lastMS does not contains 0.0001, 0.0002
		List<MobilitySpeed> lastMS = retrieveMobilityLocationsMS(muls, from, to, deviceId);// TODO avoid it
		mtd.fillHistoricalData(lastMS, ul.getData().getTime());

		mtd.setModalityClass4(mobility_status);

		return mtd;
	}

	private List<MobilitySpeed> retrieveMobilityLocationsMS(List<MobilityUserLocation> muls, Long from, Long to, String deviceId) {

		if ((muls.size() == 0) || from <= (muls.get(0).getData().getTime()))
			return dbi_remote.retrieveMobilityLocationsMS(deviceId, from, to);

		List<MobilitySpeed> toreturn = new ArrayList<MobilitySpeed>();

		for (MobilityUserLocation mul : muls) {
			if ((mul.getData().getTime() >= from) &&
					(mul.getData().getTime() <= to) &&
					(mul.getSpeed() != 0.0001) &&
					(mul.getSpeed() != 0.0002)) {
				toreturn.add(new MobilitySpeed(mul, deviceId));
			}
		}

		return toreturn;
	}

}
