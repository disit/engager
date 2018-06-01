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

import java.sql.Timestamp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import edu.unifi.disit.commons.datamodel.MobilityTrainingData;
import edu.unifi.disit.commons.datamodel.userprofiler.Device;
import edu.unifi.disit.commons.datamodel.userprofiler.Timeline;
import edu.unifi.disit.userprofiler.Application;
import edu.unifi.disit.userprofiler.security.MultiHttpSecurityConfig;
import edu.unifi.disit.userprofiler.service.IDeviceService;
import edu.unifi.disit.userprofiler.service.RPredictionServiceImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@WebAppConfiguration
@ContextConfiguration(classes = { Application.class, MultiHttpSecurityConfig.class }, loader = SpringBootContextLoader.class)
public class RTest {

	@Autowired
	RPredictionServiceImpl rp;

	@Autowired
	IDeviceService devserv;

	// @Before
	// public void setUp() {
	//
	// // rp = new RPredictionServiceImpl();
	// // rp.init("C://Programs//R//means//Rscript//");
	//
	// }

	@Test
	public void callingR() {

		// gps=1
		// fused=2

		// TRUE_TRUE_TRUE
		MobilityTrainingData mtd = new MobilityTrainingData(24d, "NA", "gps", 0d, 0d, 0, 0.22551d, 0.01317d, -0.00748d, 0.26321d, 0, 2, 2014, 5, 0.13333d, 0.066665d, 0.066665d, 0.0983317603484576d, 0, 0, 0, 0, 0, 0);
		System.out.println("Prediction invoked on: " + mtd);
		System.out.println("Cols: " + mtd.getScenarioColumnsName());
		System.out.println("Values: " + mtd.getScenarioColumnsValue());
		System.out.println(rp.predict(mtd));
		assert (rp.predict(mtd).equals("Stay"));

		// TRUE_TRUE_FALSE
		mtd = new MobilityTrainingData(24d, "NA", "gps", 0d, 0d, 0, 0.22551d, 0.01317d, -0.00748d, 0.26321d, 0, 2, 2014, 5, 0.13333d, 0.066665d, 0.066665d, 0.0983317603484576d, 0, 0, 0, 0, 0, 0);
		mtd.setPhoneYear(null);
		System.out.println("Prediction invoked on: " + mtd);
		System.out.println("Cols: " + mtd.getScenarioColumnsName());
		System.out.println("Values: " + mtd.getScenarioColumnsValue());
		System.out.println(rp.predict(mtd));
		assert (rp.predict(mtd).equals("Stay"));

		// TRUE_FALSE_TRUE
		mtd = new MobilityTrainingData(24d, "NA", "gps", 0d, 0d, 0, 0.22551d, 0.01317d, -0.00748d, 0.26321d, 0, 2, 2014, 5, 0.13333d, 0.066665d, 0.066665d, 0.0983317603484576d, 0, 0, 0, 0, 0, 0);
		mtd.setPrevSpeed(null);
		System.out.println("Prediction invoked on: " + mtd);
		System.out.println("Cols: " + mtd.getScenarioColumnsName());
		System.out.println("Values: " + mtd.getScenarioColumnsValue());
		System.out.println(rp.predict(mtd));
		assert (rp.predict(mtd).equals("Stay"));

		// TRUE_FALSE_FALSE
		mtd = new MobilityTrainingData(24d, "NA", "gps", 0d, 0d, 0, 0.22551d, 0.01317d, -0.00748d, 0.26321d, 0, 2, 2014, 5, 0.13333d, 0.066665d, 0.066665d, 0.0983317603484576d, 0, 0, 0, 0, 0, 0);
		mtd.setPhoneYear(null);
		mtd.setPrevSpeed(null);
		System.out.println("Prediction invoked on: " + mtd);
		System.out.println("Cols: " + mtd.getScenarioColumnsName());
		System.out.println("Values: " + mtd.getScenarioColumnsValue());
		System.out.println(rp.predict(mtd));
		assert (rp.predict(mtd).equals("Stay"));

		// FALSE_TRUE_TRUE
		mtd = new MobilityTrainingData(24d, "NA", "gps", 0d, 0d, 0, 0.22551d, 0.01317d, -0.00748d, 0.26321d, 0, 2, 2014, 5, 0.13333d, 0.066665d, 0.066665d, 0.0983317603484576d, 0, 0, 0, 0, 0, 0);
		mtd.setAvg_lin_acc_magn(null);
		System.out.println("Prediction invoked on: " + mtd);
		System.out.println("Cols: " + mtd.getScenarioColumnsName());
		System.out.println("Values: " + mtd.getScenarioColumnsValue());
		System.out.println(rp.predict(mtd));
		assert (rp.predict(mtd).equals("Stay"));

		// FALSE_TRUE_FALSE
		mtd = new MobilityTrainingData(24d, "NA", "gps", 0d, 0d, 0, 0.22551d, 0.01317d, -0.00748d, 0.26321d, 0, 2, 2014, 5, 0.13333d, 0.066665d, 0.066665d, 0.0983317603484576d, 0, 0, 0, 0, 0, 0);
		mtd.setPhoneYear(null);
		mtd.setAvg_lin_acc_magn(null);
		System.out.println("Prediction invoked on: " + mtd);
		System.out.println("Cols: " + mtd.getScenarioColumnsName());
		System.out.println("Values: " + mtd.getScenarioColumnsValue());
		System.out.println(rp.predict(mtd));
		assert (rp.predict(mtd).equals("Stay"));

		// FALSE_FALSE_TRUE
		mtd = new MobilityTrainingData(24d, "NA", "gps", 0d, 0d, 0, 0.22551d, 0.01317d, -0.00748d, 0.26321d, 0, 2, 2014, 5, 0.13333d, 0.066665d, 0.066665d, 0.0983317603484576d, 0, 0, 0, 0, 0, 0);
		mtd.setPrevSpeed(null);
		mtd.setAvg_lin_acc_magn(null);
		System.out.println("Prediction invoked on: " + mtd);
		System.out.println("Cols: " + mtd.getScenarioColumnsName());
		System.out.println("Values: " + mtd.getScenarioColumnsValue());
		System.out.println(rp.predict(mtd));
		assert (rp.predict(mtd).equals("Stay"));

		// FALSE_FALSE_FALSE
		mtd = new MobilityTrainingData(24d, "NA", "gps", 0d, 0d, 0, 0.22551d, 0.01317d, -0.00748d, 0.26321d, 0, 2, 2014, 5, 0.13333d, 0.066665d, 0.066665d, 0.0983317603484576d, 0, 0, 0, 0, 0, 0);
		mtd.setPhoneYear(null);
		mtd.setPrevSpeed(null);
		mtd.setAvg_lin_acc_magn(null);
		System.out.println("Prediction invoked on: " + mtd);
		System.out.println("Cols: " + mtd.getScenarioColumnsName());
		System.out.println("Values: " + mtd.getScenarioColumnsValue());
		System.out.println("Cols: " + mtd.getScenarioColumnsName());
		System.out.println("Values: " + mtd.getScenarioColumnsValue());
		System.out.println(rp.predict(mtd));
		assert (rp.predict(mtd).equals("Stay"));

	}

	@Test
	public void callingScaricoimeline() {
		Timeline t = new Timeline();

		// t.setDate(new Timestamp(1516913700000l));21.55.00 del 25
		t.setDate(new Timestamp(1516870500000l));// 9.55.00 del 25
		t.setStatus("Stay");
		t.setSeconds(180400l);
		t.setMeters(10000l);
		t.setLatitude(14d);
		t.setLongitude(14d);
		Device d = new Device();
		d.setDeviceId("af97e11488be5af2408ab27ddd90d52ad763b5d4ff1e5f89e6fc378fdf785f75");
		t.setDevice(d);

		devserv.scaricoTimeline(t);

	}

}
