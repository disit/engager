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
package edu.unifi.disit.commons.datamodel.userprofiler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import edu.unifi.disit.commons.utils.DeviceSpecs;

@Component
public class TerminalModel {

	private static final Logger logger = LogManager.getLogger();

	private static final String DEFAULT_SEPARATOR = ",";

	String devicespecsFN;
	String androidFile;
	String microsoft;

	Hashtable<String, String> ht_android = new Hashtable<String, String>();
	Hashtable<String, DeviceSpecs> ht_details = new Hashtable<String, DeviceSpecs>();
	Hashtable<String, String> ht_microsoft = null;

	@Autowired
	public TerminalModel(@Value("${device_specs_folder}") String propFolder) {

		devicespecsFN = propFolder + "devices_specs.csv";
		androidFile = propFolder + "supported_devices.csv";
		microsoft = propFolder + "LumiaFirmware.htm";

		try {
			// -------------------------------------------------------------------------------------------------------first step

			String line = "";
			BufferedReader br = new BufferedReader(new FileReader(androidFile));

			// skip first line
			line = br.readLine();

			int i = 0;

			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] linez = line.split(DEFAULT_SEPARATOR);
				i++;

				if (linez[2].length() == 0) {
					logger.warn("{}-esimo empty code", i);
					continue;
				}

				if (linez[0].length() == 0) {
					logger.warn("{}-esimo empty brand", i);
					continue;
				}

				if (linez[1].length() == 0) {
					logger.warn("{}-esimo empty model", i);
					continue;
				}

				if (linez[1].indexOf("\\xe8\\x8d\\xa3\\xe8\\x80\\x80") != -1)
					linez[1] = "Honor " + linez[1].substring(linez[1].indexOf("\\xe8\\x8d\\xa3\\xe8\\x80\\x80"));

				logger.debug(i + " MODEL [code= " + linez[2] + " , brand=" + linez[0] + " , name=" + linez[1] + "]");

				ht_android.put(linez[2], linez[0] + " " + linez[1]);

			}
			br.close();

			// -------------------------------------------------------------------------------------------------------second step

			BufferedReader br_details = new BufferedReader(new FileReader(devicespecsFN));

			i = 0;

			while ((line = br_details.readLine()) != null) {

				// use comma as separator
				String[] linez = line.split(DEFAULT_SEPARATOR);

				logger.debug(i + " MODEL [name=" + linez[0] + " , year=" + linez[1] + ", gps=" + linez[2] + "]");

				ht_details.put(linez[0], new DeviceSpecs(linez[1], linez[2]));

				i++;

			}
			br_details.close();

			// ----------------------microsoft

			BufferedReader brmicro = new BufferedReader(new FileReader(microsoft));
			while ((line = brmicro.readLine()) != null) {

				// search for line contains <b>HD-500</b> - </div>
				if ((line.indexOf("<b>HD-500</b> - Display Dock") != -1)) {
					ht_microsoft = extractmicro(line);
					break;
				}
			}

			if (ht_microsoft.keySet().size() == 0)
				logger.error("microsoft trouble");

			brmicro.close();

		} catch (Exception e) {
			logger.error("problem parsing terminal info {}", e);
			logger.error(e);
		}
	}

	// TODO unif with below
	public String getLabel(String deviceModel) {

		if ((deviceModel == null) || (deviceModel.length() == 0))
			return null;

		if (deviceModel.indexOf("iPhone") != -1) {
			return "Apple iPhone " + deviceModel.substring(7, deviceModel.indexOf(","));// extract iphone name, always same format: "iPhone8,2"
		} else if (deviceModel.indexOf("iPad") != -1) {
			return "Apple iPad " + deviceModel.substring(5, deviceModel.indexOf(","));// extract ipad name, always same format: "iPad2,2"
		} else if (deviceModel.indexOf("RM-") != -1) {
			return search_in_ht(deviceModel, ht_microsoft);
		}
		return (ht_android.get(deviceModel));

	}

	public DeviceSpecs get(String deviceModel) {

		if ((deviceModel == null) || (deviceModel.length() == 0))
			return null;

		logger.warn("looking for:{}", deviceModel);

		// ----------------------------------apple iphone + ipad
		if (deviceModel.indexOf("iPhone") != -1) {
			String name = "Apple iPhone " + deviceModel.substring(7, deviceModel.indexOf(","));// extract iphone name, always same format: "iPhone8,2"
			if (ht_details.get(name) != null) {
				logger.debug("apple iphone found");
				return ht_details.get(name);
			} else {
				logger.warn("apple not found {} -- {}", deviceModel, name);
				return null;
			}
		} else if (deviceModel.indexOf("iPad") != -1) {
			String name = "Apple iPad " + deviceModel.substring(5, deviceModel.indexOf(","));// extract ipad name, always same format: "iPad2,2"
			if (ht_details.get(name) != null) {
				logger.debug("apple ipad found");
				return ht_details.get(name);
			} else {
				logger.warn("apple not found {} -- {}", deviceModel, name);
				return null;
			}
			// ----------------------------------microsoft
		} else if (deviceModel.indexOf("RM-") != -1) {
			String name = search_in_ht(deviceModel, ht_microsoft);
			if (name != null) {
				logger.debug("microsoft found: {}", name);
				if (ht_details.get("Microsoft " + name) != null) {// try first from hashtable exactly same name (microsoft)
					logger.debug("microsoft found match exactly");
					return ht_details.get("Microsoft " + name);
				} else if (ht_details.get("Nokia " + name) != null) {// try first from hashtable exactly same name (nokia)
					logger.debug("microsoft found match exactly");
					return ht_details.get("Nokia " + name);
				} else {
					DeviceSpecs result = search_in_ds(name, ht_details);// try secondly from hashtable indexOf
					if (result != null) {
						logger.debug("microsoft found match indexof");
						return result;
					} else {
						logger.warn("microsoft not found match {} -- {}", deviceModel, name);
						return null;
					}
				}
			} else {
				logger.debug("microsoft not found");
				return null;
			}
			// ----------------------------------android (others)
		} else if (ht_android.get(deviceModel) != null) {
			logger.debug("android found: {}", ht_android.get(deviceModel));
			if (ht_details.get(ht_android.get(deviceModel)) != null) {// try first from hashtable exactly same name
				logger.debug("android found match exactly");
				return ht_details.get(ht_android.get(deviceModel));
			}
			// else {
			// DeviceSpecs result = search_in_ds(ht_android.get(deviceModel), ht_details);// try secondly from hashtable indexOf
			// if (result != null) {
			// logger.debug("android found match indexof");
			// return result;
			// }
			else {
				logger.warn("android not found match {} -- {}", deviceModel, ht_android.get(deviceModel));
				return null;
			}

		} else {
			logger.warn("not found {}", deviceModel);
			return null;
		}
	}

	private DeviceSpecs search_in_ds(String line, Hashtable<String, DeviceSpecs> ht_micro) {
		for (String chiave : ht_micro.keySet()) {
			if (line.toLowerCase(Locale.US).indexOf(chiave.toLowerCase(Locale.US)) != -1)
				return ht_micro.get(chiave);
		}
		return null;
	}

	private String search_in_ht(String line, Hashtable<String, String> ht_micro) {
		for (String chiave : ht_micro.keySet()) {
			if (line.toLowerCase(Locale.US).indexOf(chiave.toLowerCase(Locale.US)) != -1)
				return ht_micro.get(chiave);
		}
		return null;
	}

	private Hashtable<String, String> extractmicro(String line) {
		Hashtable<String, String> toreturn = new Hashtable<String, String>();

		int indexstart = 0;

		while ((indexstart = line.indexOf("<b>", indexstart)) != -1) {
			String code = line.substring(indexstart + 3, line.indexOf("</b>", indexstart));

			indexstart = line.indexOf("<i>", indexstart);

			String name = line.substring(indexstart + 3, line.indexOf("</i>", indexstart));

			logger.debug("micro: {} --> {}", code, name);

			toreturn.put(code, name);

		}

		return toreturn;

	}

}
