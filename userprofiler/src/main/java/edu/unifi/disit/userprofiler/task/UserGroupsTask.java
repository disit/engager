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

import java.util.Date;
import java.util.List;
import java.util.Set;

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

import edu.unifi.disit.commons.datamodel.userprofiler.Device;
import edu.unifi.disit.engager_utils.SampleDataSource;
import edu.unifi.disit.userprofiler.datamodel.DeviceDAO;
import edu.unifi.disit.userprofiler.ppois.GetPropertyValues;
import edu.unifi.disit.userprofiler.ppois.PopulateComplex;
import edu.unifi.disit.userprofiler.ppois.markov.MovemementLearning;
import edu.unifi.disit.userprofiler.service.IDeviceService;

@EnableScheduling
@Component
public class UserGroupsTask implements SchedulingConfigurer {

	DBinterface dbi_wallet = DBinterface.getInstance();

	@Autowired
	GetPropertyValues properties;

	@Autowired
	PopulateComplex pe;

	@Autowired
	MovemementLearning ml;

	private static final Logger logger = LoggerFactory.getLogger(PpoisTask.class);

	@Value("${usergroups.task.refresh.cron}")
	private String cronrefresh;

	@Value("${usergroups.task.refresh.duration.minutes}")
	private Long durationrefresh;

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

				logger.debug("next execution for USER GROUPS will be fired at:{}", nextExec);

				return nextExec;
			}
		});
	}

	private void myTask() {

		logger.debug("updating usergroups");

		// retrieve the cached device that need to update (i.e. the device that has updatedtime>currenttime-duration)
		List<Device> devices = devicedao.findAllByLastUpdateGreaterThan(new Date(System.currentTimeMillis() - durationrefresh * 60 * 1000));

		for (Device d : devices) {

			logger.debug("updating usergroups, device {}", d.getDeviceId());

			Set<String> groups = d.getGroups();

			// GROUP_SIIMOBILITY_PILOTA -- if the user got some point in the wallet, add it (never remove)
			if ((!groups.contains(SampleDataSource.GROUP_SIIMOBILITY_PILOTA)) && dbi_wallet.getPointsFromWallet(d.getDeviceId())) {
				groups.add(SampleDataSource.GROUP_SIIMOBILITY_PILOTA);
			}

			// GROUP_WALLET_REGISTERED -- if the user has been registered in the wallet, add it (never remove)
			if ((!groups.contains(SampleDataSource.GROUP_WALLET_REGISTERED)) && dbi_wallet.isRegistered(d.getDeviceId())) {
				groups.add(SampleDataSource.GROUP_WALLET_REGISTERED);
			}

			d.setGroups(groups);
			devicedao.save(d);// update and save
		}
	}

}
