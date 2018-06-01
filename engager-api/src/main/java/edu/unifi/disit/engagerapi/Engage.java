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
import org.kie.api.KieServices;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import disit.engager_base.ACTIONS;
import disit.engager_siimobility.EVENT;
import disit.engager_siimobility.POI;
import disit.engager_siimobility.PPOI;
import disit.engager_siimobility.TIMELINE;
import disit.engager_siimobility.TRANSPORT;
import edu.unifi.disit.engagerapi.datamodel.CONTEXT;

public class Engage {

	private static final Logger logger = LogManager.getLogger("Engage");

	KieContainer kContainer;
	KieScanner kScanner;

	public Engage() {
		KieServices ks = KieServices.Factory.get();
		// Dynamic container
		ReleaseId releaseId = ks.newReleaseId("disit", "engager-siimobility", "6.0.0-SNAPSHOT");
		kContainer = ks.newKieContainer(releaseId);
		kScanner = ks.newKieScanner(kContainer);
		// Start the KieScanner polling the Maven repository every 10 minutes -> 600sec * 1000
		kScanner.start(600000L);
	}

	public void stopit() {
		kScanner.shutdown();
	}

	public ACTIONS engage(CONTEXT context) {

		ACTIONS actions_to_return = new ACTIONS();

		try {
			KieSession kSession = kContainer.newKieSession("siimobilitysession");

			kSession.setGlobal("actions", actions_to_return);

			// inseriamo il fatto USER
			logger.debug("Adding user:{}", context.getUser());
			kSession.insert(context.getUser());
			// inseriamo il fatto PPOI
			if (context.getUser().getPpois() != null)
				for (PPOI p : context.getUser().getPpois()) {
					logger.debug("adding ppoi:{}", p.toString());
					kSession.insert(p);
				}
			// inseriamo i fatti TIMELINEs for previous
			if (context.getUser().getPreviousTimelines() != null)
				for (TIMELINE t : context.getUser().getPreviousTimelines()) {
					logger.debug("adding timeline:{}", t.toString());
					kSession.insert(t);
				}
			// inseriamo il fatto TIMELINE for last
			if (context.getUser().getPreviousTimeline() != null) {
				kSession.insert(context.getUser().getPreviousTimeline());
			}

			// inseriamo il fatto LOCATION
			logger.debug("Adding location:{}", context.getLocation());
			kSession.insert(context.getLocation());

			// inseriamo il fatto ENVIROMENT
			logger.debug("Adding enviroment:{}", context.getEnv());
			kSession.insert(context.getEnv());
			// inseriamo il fatto EVENT
			if (context.getEnv().getEvents() != null)
				for (EVENT ev : context.getEnv().getEvents()) {
					logger.debug("adding event:{}", ev.toString());
					kSession.insert(ev);
				}
			// inseriamo il fatto POI
			if (context.getEnv().getClosePois() != null)
				for (POI p : context.getEnv().getClosePois()) {
					logger.debug("adding poi:{}", p.toString());
					kSession.insert(p);
				}
			// inseriamo il fatto TRANSPORT previous
			if (context.getEnv().getPreviousTransports() != null)
				for (TRANSPORT t : context.getEnv().getPreviousTransports()) {
					logger.debug("adding transport:{}", t.toString());
					kSession.insert(t);
				}
			// inseriamo il fatto TRANSPORT next
			if (context.getEnv().getNextTransports() != null)
				for (TRANSPORT t : context.getEnv().getNextTransports()) {
					logger.debug("adding transport:{}", t.toString());
					kSession.insert(t);
				}

			// inseriamo il fatto TIME
			logger.debug("Adding time:{}", context.getTime());
			kSession.insert(context.getTime());

			kSession.fireAllRules();

			kSession.dispose();

		} catch (Exception e) {
			logger.error("Exception in engage:", e);
		}
		return actions_to_return;
	}
}
