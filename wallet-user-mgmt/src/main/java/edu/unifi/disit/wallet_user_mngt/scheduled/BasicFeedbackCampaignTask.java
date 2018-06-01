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

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import edu.unifi.disit.wallet_user_mngt.datamodel.feedback.EmailFeedbackCampaign;
import edu.unifi.disit.wallet_user_mngt.datamodel.feedback.EmailFeedbackCampaignDAO;

public abstract class BasicFeedbackCampaignTask implements SchedulingConfigurer {

	private static final Logger logger = LoggerFactory.getLogger(BasicFeedbackCampaignTask.class);

	public abstract EmailFeedbackCampaign getDefaultFeedbackCampaign();

	public abstract void doFedbackCampaign();

	public EmailFeedbackCampaign currentFC;

	@Autowired
	public EmailFeedbackCampaignDAO emailFCDAO;

	@PostConstruct
	public void init() {
		EmailFeedbackCampaign defaultFC = getDefaultFeedbackCampaign();// get default to retrieve the id

		currentFC = emailFCDAO.findOne(defaultFC.getId());// use id here

		if (currentFC == null) {// if still not exist, creat a default one
			emailFCDAO.save(defaultFC);
			currentFC = defaultFC;
		}
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.addTriggerTask(new Runnable() {
			@Override
			public void run() {
				init();// init the list here everytime

				doFedbackCampaign();
			}
		}, new Trigger() {
			@Override
			public Date nextExecutionTime(TriggerContext triggerContext) {

				CronTrigger trigger = new CronTrigger(currentFC.getRate());
				Date nextExec = trigger.nextExecutionTime(triggerContext);

				logger.debug("next execution will be fired at:{}", nextExec);

				return nextExec;
			}
		});
	}
}
