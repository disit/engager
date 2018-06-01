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
package edu.unifi.disit.engagerapi.executed;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;
import java.util.HashMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import disit.engager_base.ACTION;
import edu.unifi.disit.engagerapi.GetPropertyValues;

import edu.unifi.disit.engagerapi.datamodel.Response;
import edu.unifi.disit.engagerapi.executed.RuleChecker;

public class RuleChecker {

	private static final GetPropertyValues properties = GetPropertyValues.getInstance();
	private static final Logger logger = LogManager.getLogger("ExecutionChecker");

	HashMap<String, IRuleChecker> table = new HashMap<String, IRuleChecker>();// TABLE is syncronized, BE WARE!!!!

	ReadPackage rp = new ReadPackage();

	boolean isStarted = false;

	private static RuleChecker instance = null;

	public static RuleChecker getInstance() {
		if (instance == null) {
			synchronized (RuleChecker.class) {
				if (instance == null) {
					instance = new RuleChecker();
				}
			}
		}
		return instance;
	}

	private RuleChecker() {
		rp.start();
	}

	// can return null
	public Date checkExecuted(Response r) {
		if (r.getRule_name() != null) {
			synchronized (table) {
				for (String key : table.keySet()) {
					if (r.getRule_name().startsWith(key)) {

						logger.debug("checking {}", r.toString());

						IRuleChecker exec = table.get(key);

						if (exec != null) {
							logger.debug("passed");
							return exec.checkExecuted(r);
						}
					}
				}
			}
		}
		return null;
	}

	// cannot return null
	public Boolean checkBanned(String userId, ACTION a) {
		if (a.getAction_rulename() != null)

		{
			synchronized (table) {
				for (String key : table.keySet()) {
					if (a.getAction_rulename().startsWith(key)) {

						logger.debug("checking {}", a.toString());

						IRuleChecker exec = table.get(key);

						if (exec != null) {
							logger.debug("passed");
							return exec.checkBanned(userId, a);
						}
					}
				}
			}
		}
		return false;
	}

	// cannot return null
	public Boolean checkCancelled(String userId, ACTION a) {
		if (a.getAction_rulename() != null) {
			synchronized (table) {
				for (String key : table.keySet()) {
					if (a.getAction_rulename().startsWith(key)) {

						logger.debug("checking {}", a.toString());

						IRuleChecker exec = table.get(key);

						if (exec != null) {
							logger.debug("passed");
							return exec.checkCancelled(userId, a);
						}
					}
				}
			}
		}
		return false;
	}

	public boolean isStarted() {
		return isStarted;
	}

	protected class ReadPackage extends Thread {

		@SuppressWarnings("resource")
		public void run() {

			logger.debug("properties is {}", properties);
			logger.debug("path is {}", properties.getExecutionCheckerPath());

			File folder = new File(properties.getExecutionCheckerPath());

			File[] listOfFiles = folder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					logger.debug("....loading File! {}", listOfFiles[i].getName());

					try {
						URLClassLoader child = new URLClassLoader(new URL[] { listOfFiles[i].toURI().toURL() }, this.getClass().getClassLoader());

						Thread.currentThread().setContextClassLoader(child);

						Manifest m = new JarFile(listOfFiles[i]).getManifest();

						Attributes a = m.getMainAttributes();

						if (a.getValue("executor_main_class") != null) {

							@SuppressWarnings("rawtypes")
							Class classToLoad = Class.forName(a.getValue("executor_main_class"), true, child);
							IRuleChecker instance = (IRuleChecker) classToLoad.newInstance();

							for (String rulename : instance.getRuleNames()) {
								synchronized (table) {
									table.put(rulename, instance);
									logger.info("loaded {} for {}", instance.toString(), rulename);
								}
							}
						} else
							logger.error("this jar doesn't contains the definition of executor_main_class");

					} catch (Exception e) {
						logger.error("Exception catched:", e);
					}
				}
			}

			isStarted = true;

		}
	}
}
