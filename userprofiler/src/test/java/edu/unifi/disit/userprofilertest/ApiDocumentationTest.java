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

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Hashtable;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.unifi.disit.commons.datamodel.Location;
import edu.unifi.disit.userprofiler.Application;
import edu.unifi.disit.userprofiler.security.MultiHttpSecurityConfig;
import edu.unifi.disit.userprofiler.service.IDeviceTerminalService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@WebAppConfiguration
@ContextConfiguration(classes = { Application.class, MultiHttpSecurityConfig.class }, loader = SpringBootContextLoader.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)

public class ApiDocumentationTest {

	@Autowired
	IDeviceTerminalService terminalService;

	private static final Logger logger = LogManager.getLogger();

	private static final edu.unifi.disit.userprofiler.externaldb.DBinterface dbi_remote = edu.unifi.disit.userprofiler.externaldb.DBinterface.getInstance();

	ObjectMapper mapper = new ObjectMapper();

	@Rule
	public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext context;

	private String mydeviceid = "af97e11488be5af2408ab27ddd90d52ad763b5d4ff1e5f89e6fc378fdf785f75";
	private String altrodeviceid = "911c62dc12d8ad8aebda0dd7c1675be0d32f0edb90e29b6e9e71c77b45626b96";

	@Before
	public void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
				.apply(springSecurity())
				.apply(documentationConfiguration(this.restDocumentation))
				.build();
	}

	@Test
	public void testA1getAllDevices() throws Exception {
		this.mockMvc.perform(get("/api/v1/device/").param("lang", "en"))
				.andExpect(status().isOk())
				.andDo(document("getalldevices"))
				.andReturn();
	}

	@Test
	public void testA1getDevice() throws Exception {
		this.mockMvc.perform(get("/api/v1/device/{deviceid}", mydeviceid).param("lang", "en").param("refresh", "true"))
				.andExpect(status().isOk())
				.andDo(document("getdevice", pathParameters(parameterWithName("deviceid")
						.description("The device's id"))))
				.andReturn();
	}

	@Test
	public void testRetrievalDBforAggregation() throws Exception {

		List<Location> ls = dbi_remote.retrieveLocationForStatusAggregation("af97e11488be5af2408ab27ddd90d52ad763b5d4ff1e5f89e6fc378fdf785f75", 2303940, 2304467);

		for (Location l : ls) {
			logger.debug(l);
		}
	}

	@Test
	public void testModelAndLabel() throws Exception {

		Hashtable<String, String> ht = terminalService.getTerminalModel(mydeviceid);
		logger.debug(ht);

		ht = terminalService.getTerminalModel(altrodeviceid);
		logger.debug(ht);

	}
}
