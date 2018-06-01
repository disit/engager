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
package edu.unifi.disit.wallet_user_mngt.scheduled;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import edu.unifi.disit.commons.datamodel.engager.EngageExecuted;
import edu.unifi.disit.wallet_user_mngt.datamodel.Campaign;
import edu.unifi.disit.wallet_user_mngt.datamodel.CampaignDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.LastExecutedAnalyzed;
import edu.unifi.disit.wallet_user_mngt.datamodel.LastExecutedAnalyzedDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.Rule;
import edu.unifi.disit.wallet_user_mngt.datamodel.RuleDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.TraceIn;
import edu.unifi.disit.wallet_user_mngt.datamodel.TraceInDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.User;
import edu.unifi.disit.wallet_user_mngt.datamodel.UserDAO;
import edu.unifi.disit.wallet_user_mngt.externaldb.DBinterfaceEngager;

@EnableScheduling
@Component
public class TraceInTask implements SchedulingConfigurer {

	@Autowired
	LastExecutedAnalyzedDAO lastexecutedRepo;

	private static final DBinterfaceEngager dbi_engager = DBinterfaceEngager.getInstance();

	// @Autowired
	// GetPropertyValues properties;
	//
	// @Autowired
	// PopulateComplex pe;
	//
	// @Autowired
	// MovemementLearning ml;

	private static final String CAMPAIGN_PILOTA = "PILOTA";

	private static final Logger logger = LogManager.getLogger();

	@Value("${lastexecuted.task.refresh.duration.cron}")
	private String cronrefresh;

	@Autowired
	UserDAO userdao;

	@Autowired
	RuleDAO ruledao;

	@Autowired
	CampaignDAO campaigndao;
	@Autowired
	TraceInDAO traceInDAO;

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

				logger.debug("next execution for LAST EXECUTED will be fired at:{}", nextExec);

				return nextExec;
			}
		});
	}

	private void myTask() {
		LastExecutedAnalyzed last = lastexecutedRepo.findOne(1l);// .getLastExecuted();
		if (last == null) {
			last = new LastExecutedAnalyzed();
			last.setId(1l);
			last.setLastExecuted(0l);
		}

		List<EngageExecuted> executed = dbi_engager.getEngagementExecuted(last.getLastExecuted());

		if (executed.size() > 0) {
			updateTraceIn(executed);

			last.setLastExecuted((Long) executed.get(executed.size() - 1).getId());
			last.setId(1l);
			lastexecutedRepo.save(last);
		}
	}

	private void updateTraceIn(List<EngageExecuted> executed) {
		for (EngageExecuted e : executed) {
			logger.debug("updating {}", e);

			Rule r = ruledao.findByName(e.getRuleName());

			logger.debug("rule is {}", r);

			// get the ecosysytem -> campaing that own this rule_name
			List<Campaign> cAll = campaigndao.getCampaignIdFromRulename(e.getRuleName());

			Campaign c = extractCampaign(cAll);

			// get the user own this terminal
			List<User> users = userdao.findByDevicesDeviceId(e.getUserId());

			logger.debug("user size is {}", users.size());

			if ((users != null) && (users.size() > 0) && (c != null) && (r != null)) {

				for (User u : users) {

					logger.debug("Inserting traceIn with {} {} {}", r, c, u);

					TraceIn t = new TraceIn();

					// insert the tracein with:
					// 1-time
					// 2-rulename
					// 3-username
					// 4-campaign

					t.setCampaign(c);
					t.setUser(u);
					t.setRule(r);
					t.setTime(e.getTime());
					traceInDAO.save(t);
				}
			} else {
				logger.debug("NOT INSERED");
			}
		}
	}

	private Campaign extractCampaign(List<Campaign> cAll) {
		for (Campaign c : cAll) {
			logger.debug("check if compaign is PILOTA {}", c);
			if (c.getName().equals(CAMPAIGN_PILOTA))
				return c;

		}
		return null;
	}
}
