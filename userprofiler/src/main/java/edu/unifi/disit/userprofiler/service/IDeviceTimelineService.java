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
import java.text.ParseException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.unifi.disit.commons.datamodel.AggregatedMobility;
import edu.unifi.disit.commons.datamodel.Position;
import edu.unifi.disit.commons.datamodel.userprofiler.Timeline;

@Service
public interface IDeviceTimelineService {

	List<Timeline> getTimeline(String deviceId, Long from, Long to, String lastStatus, Long minTime, String lang);

	List<AggregatedMobility> getAggregatedMobility(String deviceId, Long from, Long to, String status, String lang);

	List<Position> getPositions(String deviceId, Long from, Long to, String lang);

	List<Timeline> postTimeline(String deviceId, Long from, Long to, String mobility, String lang) throws JsonProcessingException, ParseException, IOException;

	void prepareDataset(String deviceId, Long fromS, Long toS, String mobility_status) throws ParseException, JsonProcessingException, IOException; // TODO used for test
}
