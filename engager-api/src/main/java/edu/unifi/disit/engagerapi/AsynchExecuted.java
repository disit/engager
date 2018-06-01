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

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.unifi.disit.engagerapi.datamodel.Response;
import edu.unifi.disit.engagerapi.executed.RuleChecker;

public class AsynchExecuted extends Thread {

	private static final Logger logger = LogManager.getLogger("AsynchExecuted");
	private static final edu.unifi.disit.engagerapi.DBinterface dbi = edu.unifi.disit.engagerapi.DBinterface.getInstance();
	private static final GetPropertyValues properties = GetPropertyValues.getInstance();
	private boolean isAlive = true;
	private static final RuleChecker rc = RuleChecker.getInstance();

	public void run() {

		// wait until ec is started
		while (rc.isStarted() == false) {
			try {
				logger.trace("startup going sleeping...");
				sleep(10000);// 10 seconds sleep startup
				logger.trace("...woke up startup");
			} catch (InterruptedException e) {
				logger.error("InterruptedException in startup sleep:", e);
			}
		}

		// check in the db the last requested entry
		// if there is still no entry OR the entry is too old, request just the getStartingRequestMilliseconds previous
		Date last = dbi.getLastExecuted();
		if ((last == null) || (last.getTime() < (System.currentTimeMillis() - properties.getStartingWindowMillisecondsExecuted()))) {
			last = new Date(System.currentTimeMillis() - properties.getStartingWindowMillisecondsExecuted());
			logger.debug("Last EXECUTED is too old, set to:{}", last);
			dbi.setLastExecuted(last);
		}

		while (isAlive) {

			try {
				// search the engagement SENT from last to now, ordered by sendTime
				List<Response> engagementSent = dbi.retrieveEngagementForExecuted(last);

				// TODO remove from engagementViewed the engagement that has been already labelled as executed

				logger.debug("response is {}", engagementSent.size());

				boolean isfirst = true; // use FIRST one that is NOT executed and NOT elapsed for next execution

				for (Response r : engagementSent) {
					logger.debug("check on {}", r.toString());
					Date executionTime = null;
					if ((executionTime = rc.checkExecuted(r)) != null) {
						logger.debug("this engagement is executed, insert and move on");
						dbi.insertEngagementExecuted(r, executionTime);
					} else {
						if (r.getTimeElapsed().getTime() < System.currentTimeMillis()) {
							logger.debug("this engagement is NOT executed and elapsed. move on");
						} else {
							logger.debug("this engagement is NOT executed and NOT elapsed. No more increment");
							if (isfirst) {
								last = new Date(r.getTimeSend().getTime());
								logger.debug("isfirst, updating last to {}", last);
								isfirst = false;
							}
						}
					}
				}

				// if all of them passed use the last entryies + 1sec for calcolate next iteraction
				if ((isfirst) && (engagementSent.size() > 0)) {
					logger.debug("isfirst still true, using last entry to last");
					last = new Date(engagementSent.get(engagementSent.size() - 1).getTimeSend().getTime() + 1000);// get last entryes + 1 second
				}

				// update last and save to disk
				if (engagementSent.size() > 0) {
					dbi.setLastExecuted(last);
				} else
					logger.debug("No new event");

			} catch (Exception e) {
				logger.error("exeception got in executer: {}", e);
			}
			try {
				logger.debug("going sleeping...");
				sleep(properties.getSleepMillisecondsExecuted());
				logger.debug("...woke up");
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
		// rc.stopit();
		isAlive = false;
		logger.info("Try to stop current thread...");
	}
}
