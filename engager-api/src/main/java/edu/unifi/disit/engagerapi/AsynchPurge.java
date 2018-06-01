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
package edu.unifi.disit.engagerapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AsynchPurge extends Thread {

	private static final Logger logger = LogManager.getLogger("AsynchPurge");
	private static final edu.unifi.disit.engagerapi.DBinterface dbi = edu.unifi.disit.engagerapi.DBinterface.getInstance();
	private static final GetPropertyValues properties = GetPropertyValues.getInstance();
	private boolean isAlive = true;

	public void run() {

		while (isAlive) {
			// purge the ACTION created but already elapsed or older than the value specified in getTimeoutEngagementMilliseconds
			dbi.purge(properties.getTimeoutEngagementMilliseconds());

			try {
				logger.trace("going sleeping...");
				sleep(properties.getSleepMillisecondsAccessLog());
				logger.trace("...woke up");
			} catch (InterruptedException e) {
				logger.error("InterruptedException in sleep:", e);
				isAlive = false;
			}
		}

		// release here all the resource
		try {
			this.finalize();
		} catch (Throwable e) {
			logger.error("Throwable in finalize:", e);
		}

		logger.info("...current thread stopped");
	}

	public void stopit() throws Throwable {
		isAlive = false;
		logger.info("Try to stop current thread...");
	}
}
