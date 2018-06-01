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

import java.util.Hashtable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.unifi.disit.commons.datamodel.userprofiler.Device;
import edu.unifi.disit.commons.datamodel.userprofiler.TerminalModel;
import edu.unifi.disit.userprofiler.datamodel.DeviceDAO;

@Component
public class DeviceTerminalServiceImpl implements IDeviceTerminalService {

	@Autowired
	TerminalModel tm;

	@Autowired
	DeviceDAO devicedao;

	@Override
	public Hashtable<String, String> getTerminalModel(String deviceId) {
		Hashtable<String, String> toreturn = null;

		Device d = devicedao.findByDeviceId(deviceId);

		if ((d != null) && (d.getTerminalModel() != null)) {
			toreturn = new Hashtable<String, String>();
			toreturn.put("id", d.getTerminalModel());
			if (tm.getLabel(d.getTerminalModel()) != null)
				toreturn.put("label", tm.getLabel(d.getTerminalModel()));
		}

		return toreturn;
	}

	// @Override
	// public Device postTerminalMunicipality(String deviceId, String terminal_municipality) {
	// Device toreturn = devicedao.findByDeviceId(deviceId);
	//
	// if ((toreturn != null)) {
	// toreturn.setTerminal_municipality(terminal_municipality);
	// devicedao.save(toreturn);
	// }
	//
	// return toreturn;
	// }
}
