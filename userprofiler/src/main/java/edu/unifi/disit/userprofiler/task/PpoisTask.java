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
package edu.unifi.disit.userprofiler.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import edu.unifi.disit.userprofiler.datamodel.DeviceDAO;
import edu.unifi.disit.userprofiler.ppois.GetPropertyValues;
import edu.unifi.disit.userprofiler.ppois.PopulateComplex;
import edu.unifi.disit.userprofiler.ppois.markov.MovemementLearning;
import edu.unifi.disit.userprofiler.service.IDeviceService;

@EnableScheduling
@Component
public class PpoisTask implements SchedulingConfigurer {

	@Autowired
	GetPropertyValues properties;

	@Autowired
	PopulateComplex pe;

	@Autowired
	MovemementLearning ml;

	private static final Logger logger = LoggerFactory.getLogger(PpoisTask.class);

	@Value("${ppoi.task.refresh.duration.cron}")
	private String cronrefresh;

	@Autowired
	DeviceDAO devicedao;

	@Autowired
	IDeviceService deviceService;

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.addTriggerTask(new Runnable() {
			@Override
			public void run() {
				myTask();
			}
		}, new Trigger() {

			@Override
			public Date nextExecutionTime(TriggerContext triggerContext) {

				CronTrigger trigger = new CronTrigger(cronrefresh);
				Date nextExec = trigger.nextExecutionTime(triggerContext);

				logger.debug("next execution for PPOI will be fired at:{}", nextExec);

				return nextExec;
			}
		});
	}

	private void myTask() {

		int passed = 0;
		int totali = 0;
		logger.info("Scenario HOME");
		List<String> users_home = pe.retrieveUsersHome();
		for (String user : users_home) {
			totali++;
			if (pe.retrieveHomePOI(user))
				passed++;
		}
		logger.info("Totali:{}", totali);
		logger.info("Passed:{}", passed);
		logger.info("Passed/Totali:{}", ((double) passed / totali));

		// -------------------------------------------------------------------------------------------------work scenario

		passed = 0;
		totali = 0;
		logger.info("Scenario WORK");
		List<String> users_work = pe.retrieveUsersWork();
		for (String user : users_work) {
			totali++;
			if (pe.retrieveWorkPOI(user))
				passed++;
		}
		logger.info("Totali:{}", totali);
		logger.info("Passed:{}", passed);
		logger.info("Passed/Totali:{}", ((double) passed / totali));

		// --------------------------------------------------------------------------------------------------school scenario

		passed = 0;
		totali = 0;
		logger.info("Scenario SCHOOL");
		List<String> users_school = pe.retrieveUsersSchool();
		for (String user : users_school) {
			totali++;
			if (pe.retrieveSchoolPOI(user))
				passed++;
		}
		logger.info("Totali:{}", totali);
		logger.info("Passed:{}", passed);
		logger.info("Passed/Totali:{}" + ((double) passed / totali));

		// ---------------------------------------------------------------------------------------------------all scenaio
		passed = 0;
		totali = 0;
		logger.info("Scenario ALL");
		List<String> users_all = pe.retrieveUsersALL();
		for (String user : users_all) {
			totali++;
			int returned = pe.retrieveALLPOI(user);
			if (returned > 0)
				passed++;

			logger.info("Added:{}", returned);
		}
		logger.info("Totali:{}", totali);
		logger.info("Passed:{}", passed);
		logger.info("Passed/Totali:{}" + ((double) passed / totali));

		// ---------------------------------------------------------------------------------------------------markov learning
		logger.info("Scenario MARKOV");
		int learning_done = 0;
		List<String> usersMov = interseca(ml.retrieveUsers(), users_all);// update markov net if the user has ppoi and has activities
		for (String user : usersMov) {
			try {
				if (ml.learning(user))
					learning_done++;
			} catch (Exception e) {
				logger.error("Catched exception for user {}, trying to predict next step", user, e);
			}
		}
		logger.debug("totale learned={}", learning_done);
	}

	private List<String> interseca(List<String> retrieveUsers, List<String> users_all) {
		List<String> toReturn = new ArrayList<String>();
		for (String userAll : users_all) {
			if (retrieveUsers.contains(userAll))
				toReturn.add(userAll);
		}
		logger.debug("Updating markov users n={} users", toReturn.size());
		return toReturn;
	}
}
