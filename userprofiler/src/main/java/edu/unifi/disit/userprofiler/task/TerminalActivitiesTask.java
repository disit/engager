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
import edu.unifi.disit.userprofiler.datamodel.DeviceDAO;
import edu.unifi.disit.userprofiler.service.IDeviceService;

@EnableScheduling
@Component
public class TerminalActivitiesTask implements SchedulingConfigurer {

	private static final Logger logger = LoggerFactory.getLogger(TerminalActivitiesTask.class);

	@Value("${terminal.task.refresh.cron}")
	private String cronrefresh;

	@Value("${terminal.task.refresh.duration.minutes}")
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

				logger.debug("next execution for TERMINAL will be fired at:{}", nextExec);

				return nextExec;
			}
		});
	}

	private void myTask() {

		logger.debug("updating terminal info");

		// retrieve the cached device that need to update (i.e. the device that has updatedtime>currenttime-duration)
		List<Device> devices = devicedao.findAllByLastUpdateGreaterThan(new Date(System.currentTimeMillis() - durationrefresh * 60 * 1000));

		for (Device d : devices) {

			logger.debug("updating terminal, device {}", d.getDeviceId());

			devicedao.save(deviceService.updateTerminalInfo(d));// update and save
		}
	}
}
