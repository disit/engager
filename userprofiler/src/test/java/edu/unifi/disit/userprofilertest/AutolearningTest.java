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
package edu.unifi.disit.userprofilertest;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.unifi.disit.commons.datamodel.userprofiler.Timeline;
import edu.unifi.disit.userprofiler.Application;
import edu.unifi.disit.userprofiler.ppois.GetPropertyValues;
import edu.unifi.disit.userprofiler.security.MultiHttpSecurityConfig;
import edu.unifi.disit.userprofiler.service.IDeviceTimelineService;
import edu.unifi.disit.userprofiler.service.RPredictionServiceImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@WebAppConfiguration
@ContextConfiguration(classes = { Application.class, MultiHttpSecurityConfig.class }, loader = SpringBootContextLoader.class)
public class AutolearningTest {

	@Autowired
	RPredictionServiceImpl rp;

	@Autowired
	GetPropertyValues properties;

	@Autowired
	IDeviceTimelineService timelineservice;

	@Test
	public void callingPrepareDataset() throws ParseException, JsonProcessingException, IOException {

		// String deviceId = "2bef2df9920c6334074a8081852896f9047184119592205a14ecf808ff2ec93a";// irene
		String deviceId = "af97e11488be5af2408ab27ddd90d52ad763b5d4ff1e5f89e6fc378fdf785f75";// ange
		// DeviceSpecs ds = new DeviceSpecs("2015", "BDS");// TODO retrieve from model ANGE
		// DeviceSpecs ds = new DeviceSpecs("2016", "BDS");// TODO retrieve from model IRENE
		// String fromS = "2018-1-17 07:00:00";
		// Long from = 1516143600000l;
		// String toS = "2018-1-18 07:00:00";
		// Long to = 1516230000000l;

		// String fromS = "2018-1-17 00:00:00";
		Long from = 1516143600000l;
		// String toS = "2018-1-18 00:00:00";
		Long to = 1516230000000l;

		String mobility_status = "Stay";// TODO to convert

		timelineservice.prepareDataset(deviceId, from, to, mobility_status);
	}

	@Test
	public void callingtimelineprevious() throws ParseException, JsonProcessingException, IOException {

		// String deviceId = "2bef2df9920c6334074a8081852896f9047184119592205a14ecf808ff2ec93a";// irene
		String deviceId = "af97e11488be5af2408ab27ddd90d52ad763b5d4ff1e5f89e6fc378fdf785f75";// ange
		// DeviceSpecs ds = new DeviceSpecs("2015", "BDS");// TODO retrieve from model ANGE
		// DeviceSpecs ds = new DeviceSpecs("2016", "BDS");// TODO retrieve from model IRENE
		// String fromS = "2018-1-17 07:00:00";
		// Long from = 1516143600000l;
		// String toS = "2018-1-18 07:00:00";
		// Long to = 1516230000000l;

		// String fromS = "2018-1-17 00:00:00";
		Long from = 1515692700000l;// 11gen 18.45.00
		// String toS = "2018-1-18 00:00:00";
		// Long to = 1515750035000l;// 12gen 10.40.35
		Long to = 1515743820000l;// 8.57

		List<Timeline> ts = timelineservice.getTimeline(deviceId, from, to, "Stay", null, "en");
		for (Timeline t : ts) {
			System.out.println(t);
		}
	}

}
