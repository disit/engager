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
package edu.unifi.disit.wallet_user_mngt;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/home").setViewName("home");
		registry.addViewController("/signin").setViewName("signin");
		registry.addViewController("/registration").setViewName("registration");
		registry.addViewController("/feedback").setViewName("feedback");
		registry.addViewController("/user").setViewName("user");

		registry.addViewController("/prize").setViewName("requestprize");
		registry.addViewController("/manager").setViewName("manager");
		registry.addViewController("/showcampaign").setViewName("showcampaign");
		registry.addViewController("/campaign").setViewName("campaign");
		registry.addViewController("/admin").setViewName("admin");
		registry.addViewController("/ecosystem").setViewName("ecosystem");
		registry.addViewController("/editusers").setViewName("editusers");
		registry.addViewController("/rules").setViewName("rules");
		registry.addViewController("/reportcampaign").setViewName("reportcampaign");
		registry.addViewController("/emailfeedback").setViewName("emailfeedback");
		registry.addViewController("/forgotpwd").setViewName("forgotpwd");
		registry.addViewController("/updatepwd").setViewName("updatepwd");

		registry.addViewController("/settings").setViewName("settings");
		registry.addViewController("/profile").setViewName("profile");

		registry.addViewController("/browse").setViewName("browse");

		registry.addViewController("/howto").setViewName("howto");
	}
}
