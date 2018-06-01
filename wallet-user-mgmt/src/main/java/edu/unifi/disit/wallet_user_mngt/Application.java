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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.web.ProviderSignInController;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.social.google.config.boot.GoogleAutoConfiguration;
import org.springframework.web.client.RestTemplate;

import edu.unifi.disit.wallet_user_mngt.security.AccessTokenAuthenticationFilter;
import edu.unifi.disit.wallet_user_mngt.signin.MyProviderSignInController;
import edu.unifi.disit.wallet_user_mngt.signin.SimpleSignInAdapter;

@SpringBootApplication
@Import(GoogleAutoConfiguration.class)
@ComponentScan({ "edu.unifi.disit.wallet_user_mngt" })

public class Application extends SpringBootServletInitializer {

	@Value("${application.url}")
	private String myappurl;

	public static void main(String[] args) throws Throwable {
		SpringApplication.run(Application.class, args);
	}

	// to enable scenario with my external tomcat
	private static Class<Application> applicationClass = Application.class;

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(applicationClass);
	}

	// to enable a common crypt encoder
	@Bean
	PasswordEncoder bcryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// to avoid accesstoken filter to be executed in all the filterchains
	@Autowired
	private AccessTokenAuthenticationFilter accessTokenAuthenticationFilter;

	@Bean
	public FilterRegistrationBean filterRegistrationBean() {
		FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
		filterRegistrationBean.setEnabled(false);
		filterRegistrationBean.setFilter(accessTokenAuthenticationFilter);
		return filterRegistrationBean;
	}

	// to enable remember-me scenario for social
	@Bean
	public SignInAdapter signInAdapter() {
		return new SimpleSignInAdapter();
	}

	@Bean
	public ProviderSignInController signInController(
			ConnectionFactoryLocator factoryLocator,
			UsersConnectionRepository usersRepository, SignInAdapter signInAdapter) {
		MyProviderSignInController controller = new MyProviderSignInController(
				factoryLocator, usersRepository, signInAdapter);
		controller.setApplicationUrl(myappurl);

		return controller;
	}

	// to enable captcha
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		super.onStartup(servletContext);
		servletContext.addListener(new SessionListener());
	}

	// to avoid annoying DEBUG message about missing executor (Could not find default ScheduledExecutorService bean)
	@Bean
	public TaskScheduler taskScheduler() {
		return new ConcurrentTaskScheduler(); // single threaded by default
	}
}
