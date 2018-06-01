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
package edu.unifi.disit.userprofiler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan({ "edu.unifi.disit.commons.datamodel.userprofiler", "edu.unifi.disit.userprofiler.service", "edu.unifi.disit.userprofiler.ppois.markov", "edu.unifi.disit.userprofiler.ppois", "edu.unifi.disit.userprofiler.security",
		"edu.unifi.disit.userprofiler.task", "edu.unifi.disit.userprofiler.controller" })
@EnableJpaRepositories({ "edu.unifi.disit.commons.datamodel.userprofiler", "edu.unifi.disit.userprofiler.datamodel", "edu.unifi.disit.userprofiler.service" })
@EntityScan({ "edu.unifi.disit.commons.datamodel.userprofiler", "edu.unifi.disit.userprofiler.datamodel" })
public class Application extends SpringBootServletInitializer {

	public static void main(String[] args) throws Throwable {
		SpringApplication.run(Application.class, args);
	}

	// to enable scenario with my external tomcat
	private static Class<Application> applicationClass = Application.class;

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(applicationClass);
	}
}
