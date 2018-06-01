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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import edu.unifi.disit.wallet_user_mngt.datamodel.CampaignDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.Device;
import edu.unifi.disit.wallet_user_mngt.datamodel.Roletype;
import edu.unifi.disit.wallet_user_mngt.datamodel.RuleDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.TraceInDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.User;
import edu.unifi.disit.wallet_user_mngt.datamodel.UserDAO;
import edu.unifi.disit.wallet_user_mngt.event.EmailScenarioType;
import edu.unifi.disit.wallet_user_mngt.event.OnPreparationEmailCompleteEvent;
import edu.unifi.disit.wallet_user_mngt.object.ScoreboardEntry;
import edu.unifi.disit.wallet_user_mngt.service.IDeviceService;
import edu.unifi.disit.wallet_user_mngt.service.IManagerService;

@EnableScheduling
@Component
public class DailyWinnerTask implements SchedulingConfigurer {

	private static final Logger logger = LogManager.getLogger();

	@Value("${dailywinner.task.refresh.duration.cron}")
	private String cronrefresh;

	@Autowired
	ApplicationEventPublisher eventPublisher;

	@Autowired
	UserDAO userRepo;

	@Autowired
	RuleDAO ruledao;

	@Autowired
	CampaignDAO campaigndao;
	@Autowired
	TraceInDAO traceInDAO;

	@Autowired
	IManagerService managerService;

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

				logger.debug("next execution for daily winner will be fired at:{}", nextExec);

				return nextExec;
			}
		});
	}

	private void myTask() {

		List<ScoreboardEntry> scoreboard7days = managerService.getScoreboard7daysPilota();

		List<ScoreboardEntry> scoreboard = managerService.getScoreboardTotalPilota();
		List<User> managers = userRepo.findByRoletype(Roletype.ROLE_MANAGER);

		for (User user : managers) {

			eventPublisher.publishEvent(prepareEventForRegistration(user, scoreboard7days, scoreboard));
		}
	}

	private OnPreparationEmailCompleteEvent prepareEventForRegistration(User user, List<ScoreboardEntry> scoreboards7days, List<ScoreboardEntry> scoreboard) {

		String recipientAddress = user.getUsername();
		String subject = "Daily winner PILOTA Sii-mobility";

		String testo = "";
		for (ScoreboardEntry se : scoreboards7days) {

			String totalpoint = getTotalPoint(scoreboard, se.getUsername());

			if (!totalpoint.equals("null")) {

				if (getAssessment(se.getUsername()))
					testo = testo + "(assessor) ";

				testo = testo + se.getUsername() + "\t" + se.getPoints() + "[" + totalpoint + "]" + "\n";
			}
		}

		List<String> testos = new ArrayList<String>();
		testos.add(testo);

		return new OnPreparationEmailCompleteEvent(recipientAddress, subject, testos, new Locale("en"), EmailScenarioType.SIMPLE);
	}

	private String getTotalPoint(List<ScoreboardEntry> scoreboard, String username) {

		for (ScoreboardEntry se : scoreboard) {
			if (se.getUsername().equals(username) && (se.getPoints() != null))
				return se.getPoints().toString();
		}

		return "null";
	}

	private boolean getAssessment(String username) {
		User user = userRepo.findByUsername(username);
		for (Device d : user.getDevices()) {
			if (deviceService.getDevice(d.getDeviceId()).getIsAssessor())
				return true;
		}
		return false;
	}

}
