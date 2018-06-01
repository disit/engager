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
package edu.unifi.disit.wallet_user_mngt.object;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

public class SettingsForm {

	private Map<String, String> properties;

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	@Override
	public String toString() {
		return "SettingsForm [properties=" + properties + "]";
	}

	@Valid
	public String activedeviceid = null;

	public String getActivedeviceid() {
		return this.activedeviceid;
	}

	public void setActivedeviceid(String activedeviceid) {
		this.activedeviceid = activedeviceid;
	}

	@Valid
	public String timelinedate = new String();

	public String getTimelinedate() {
		return this.timelinedate;
	}

	public void setTimelinedate(String timelinedate) {
		this.timelinedate = timelinedate;
	}

	private List<String> mobilities = new ArrayList<String>();

	public List<String> getMobilities() {
		return mobilities;
	}

	public void setMobilities(List<String> mobilities) {
		this.mobilities = mobilities;
	}

	@Valid
	public String municipality = null;

	public String getMunicipality() {
		return municipality;
	}

	public void setMunicipality(String municipality) {
		this.municipality = municipality;
	}

	@Valid
	public String dataset = null;

	public String getDataset() {
		return dataset;
	}

	public void setDataset(String dataset) {
		this.dataset = dataset;
	}

}
