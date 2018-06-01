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
package edu.unifi.disit.userprofiler.ppois.markov;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.unifi.disit.commons.utils.Utils;
import edu.unifi.disit.userprofiler.ppois.GetPropertyValues;
import edu.unifi.disit.commons.datamodel.Prediction;

@Component
public class Markowinterface {

	@Autowired
	GetPropertyValues properties;

	private static final Logger logger = LogManager.getLogger();

	HashMap<String, MarkovNetwork> m = new HashMap<String, MarkovNetwork>();

	@PostConstruct
	void init() {
		try {
			load();
		} catch (ClassNotFoundException | IOException e) {
			logger.error("BIG PROBLEM HERE");
			e.printStackTrace();
		}
	}

	public synchronized void delete(String userId) throws IOException, ClassNotFoundException {
		File userFile = new File(properties.getMarkovPath().concat(userId).concat(".mkv"));
		userFile.delete();
		logger.debug("Serialized data is deleted from {}", properties.getMarkovPath().concat(userId).concat(".mkv"));
	}

	// load all user in the folder /markov
	private void load() throws ClassNotFoundException, IOException {
		File folder = new File(properties.getMarkovPath());
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				logger.debug("....loading File {}", listOfFiles[i].getName());
				String userId = listOfFiles[i].getName().substring(0, listOfFiles[i].getName().length() - 4);
				load(userId);
			}
		}
	}

	public synchronized void load(String userId) throws IOException, ClassNotFoundException {
		FileInputStream fileIn = new FileInputStream(properties.getMarkovPath().concat(userId).concat(".mkv"));
		ObjectInputStream in = new ObjectInputStream(fileIn);
		MarkovNetwork mn = (MarkovNetwork) in.readObject();
		in.close();
		fileIn.close();
		m.put(userId, mn);
		logger.debug("Serialized data is loaded from {}", properties.getMarkovPath().concat(userId).concat(".mkv"));
	}

	public synchronized void write(String userId, MarkovNetwork mn) throws IOException {
		FileOutputStream fileOut = new FileOutputStream(properties.getMarkovPath().concat(userId).concat(".mkv"));
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(mn);
		out.close();
		fileOut.close();
		logger.debug("Serialized data is saved to {}", properties.getMarkovPath().concat(userId).concat(".mkv"));
	}

	public Prediction predict(String userId, long data, String ppoi) throws Exception {
		if (ppoi == null)
			return null;

		List<Integer> condizioni = new ArrayList<Integer>();
		condizioni.add(Utils.retrieveDaySlotInteger(data));
		// condizioni.add(0);// dont' predict on dayslot
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(data);
		condizioni.add(c.get(Calendar.DAY_OF_WEEK) - 1);
		// condizioni.add(0);// don't predict on weekday

		MarkovNetwork mn = m.get(userId);
		if (mn != null)
			return mn.predictMost(ppoi, condizioni);
		else
			return null;
	}

	public MarkovNetwork get(String userId) {
		return m.get(userId);
	}
}
