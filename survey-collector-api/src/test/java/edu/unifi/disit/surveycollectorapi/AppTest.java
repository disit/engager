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
package edu.unifi.disit.surveycollectorapi;

import java.net.URL;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.unifi.disit.commons.datamodel.PPOI;
import edu.unifi.disit.commons.utils.NetClientGet;

public class AppTest {

	NetClientGet ncg = new NetClientGet();

	private static final GetPropertyValues properties = GetPropertyValues.getInstance();

	@Test
	public void test1() {

		try {
			ObjectMapper mapper = new ObjectMapper();

			String user_id = "3b554b438ad5d31a6c849872d07e25b93216911f3fb2f87867e108bd9ef5dbbf";
			String ppoi_name = "HOME";

			String ppois_string = ncg.get(new URL(properties.getUserProfilerURL() + "device/" + user_id + "/ppoi/" + ppoi_name + "?confirmation=false"), properties.getReadTimeoutMillisecond(), Level.ERROR);

			System.out.println(ppois_string);

			if (ppois_string.length() > 0) {

				List<PPOI> ppois = mapper.readValue(ppois_string, new TypeReference<List<PPOI>>() {
				});

				// set the confirmation to true
				if (ppois != null)
					for (PPOI ppoi : ppois) {

						System.out.println("confirm the ppoi: " + ppoi);

						ncg.put(new URL(properties.getUserProfilerURL() + "device/" + user_id + "/ppoi/" + ppoi.getId() + "?confirmation=true"), properties.getReadTimeoutMillisecond(), Level.ERROR);
					}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
