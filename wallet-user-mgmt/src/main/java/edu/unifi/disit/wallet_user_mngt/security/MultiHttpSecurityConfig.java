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
package edu.unifi.disit.wallet_user_mngt.security;

import java.util.Arrays;
import java.util.Collection;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import edu.unifi.disit.wallet_user_mngt.datamodel.Socialusertype;
import edu.unifi.disit.wallet_user_mngt.exception.SocialNotRecognizedException;
import edu.unifi.disit.wallet_user_mngt.service.ISocialService;
import edu.unifi.disit.wallet_user_mngt.service.IUserService;
import edu.unifi.disit.wallet_user_mngt.service.SecurityServiceImpl;
import edu.unifi.disit.wallet_user_mngt.service.UserDetailsServiceImpl;

@EnableWebSecurity
public class MultiHttpSecurityConfig {

	@Autowired
	private MessageSource messages;

	@Autowired
	private SecurityServiceImpl securityService;

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	@Autowired
	private IUserService userService;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	private ISocialService socialservice;

	@Value("${spring.ldap.url}")
	private String ldapUrl;

	@Value("${spring.ldap.basicdn}")
	private String ldapBasicDN;

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
		auth.eraseCredentials(false)
				.ldapAuthentication()
				.userDetailsContextMapper(userDetailsContextMapper())
				.userDnPatterns("cn={0}")
				.contextSource(contextSource());
	}

	@Bean
	public UserDetailsContextMapper userDetailsContextMapper() {
		return new LdapUserDetailsMapper() {
			@Override
			public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
				// register here the new user
				String email = null;
				try {
					socialservice.registerOrLogin(ctx, Socialusertype.LDAP, null);// TODO popolate language
					email = (String) ctx.getAttributes().get("mail").get();
				} catch (SocialNotRecognizedException | NamingException e) {
					e.printStackTrace();
				}

				return userDetailsService.loadUserByUsername(email);// super.mapUserFromContext(ctx, username, auth);
			}
		};
	}

	@Bean
	public DefaultSpringSecurityContextSource contextSource() {
		return new DefaultSpringSecurityContextSource(Arrays.asList(ldapUrl), ldapBasicDN);
	}

	// if you wanna introduce a new release version, please just copy the configuration in order and implement the new logic in the
	// new implemented class

	@Configuration
	@Order(1)
	public class AccessTokenSecurityConfigV1 extends WebSecurityConfigurerAdapter {

		@Autowired
		private AccessTokenAuthenticationFilter accessTokenAuthenticationFilter;

		@Override
		protected void configure(HttpSecurity http) throws Exception {

			http.addFilterBefore(myCorsFilter, ChannelProcessingFilter.class)
					.csrf().disable()
					/**/.antMatcher("/api/v1/signinsocial")
					/**/.authorizeRequests().anyRequest().authenticated()
					.and()
					/**/.addFilterBefore(accessTokenAuthenticationFilter, BasicAuthenticationFilter.class);
		}
	}

	// if you wanna introduce a new release version, please just copy the configuration in order and implement the new logic in the
	// new implemented class

	@Configuration
	@Order(2)
	public class RestSecurityConfigV1 extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity http) throws Exception {

			http.addFilterBefore(myCorsFilter, ChannelProcessingFilter.class)
					.csrf().disable()
					/**/.exceptionHandling()
					/**/.authenticationEntryPoint(new RestAuthenticationEntryPoint())
					.and()
					/**/.authorizeRequests()
					/**/.antMatchers("/api/v1/registration", "/api/v1/forgotpwd", "/api/v1/savepwd").permitAll()
					/**/.antMatchers("/api/v1/feedback").permitAll()
					.and()
					/**/.antMatcher("/api/v1/**") // any apis under /api is provided with the cookie generated with the signin
					/**/.authorizeRequests().anyRequest().authenticated()
					.and()
					/**/.formLogin()
					/**/.loginPage("/api/v1/signin")
					/**/.successHandler(new MyRestSuccessHandler(userService))
					/**/.failureHandler(new MyRestFailureHandler())
					.and()
					/**/.rememberMe().tokenRepository(persistentTokenRepository()).tokenValiditySeconds(1209600)
					.and()
					/**/.logout().logoutRequestMatcher(new AntPathRequestMatcher("/api/v1/logout"))
					/**/.addLogoutHandler(persistentTokenBasedRememberMeServices())
					/**/.logoutSuccessHandler((new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)))
					.and()
					.addFilterBefore(new JWTAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
		}
	}

	@Configuration
	@Order(3)
	public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
					.authorizeRequests()
					/**/.antMatchers("/api/test").permitAll()
					/**/.antMatchers("/howto").permitAll()
					/**/.antMatchers("/vendor/**", "/js/**", "/css/**", "/image/**", "/social/**").permitAll()
					/**/.antMatchers("/", "/home", "/home/**", "/registration", "/feedback", "/signin**", "/signin/**", "/signup/**").permitAll()// OTHER
					/**/.antMatchers("/facebook", "/linkedin", "/twitter", "/google", "/connect/**").permitAll()// SOCIAL
					/**/.antMatchers("/user/changepwd", "/user/resetpwd", "/forgotpwd/**", "/confirm").permitAll()// RESETPWD
					/**/.antMatchers("/feedback/**").permitAll()// FEEDBACK
					/**/.antMatchers("/download/**", "/browse/**").permitAll()
					/**/.antMatchers("/user", "prize").access("hasRole('USER') or hasRole('MANAGER')")
					/**/.antMatchers("/manager", "/manager/**", "/campaign", "/campaign/**").hasRole("MANAGER")
					/**/.antMatchers("/admin", "/admin/**", "/ecosystem", "/ecosystem/**", "/rules", "/editusers", "/reportcampaign", "/reportcampaign/**", "/emailfeedback/**").hasRole("ADMIN")
					/**/.antMatchers("/showcampaign", "/showcampaign/{\\d+}/**").access("hasRole('ADMIN') or hasRole('MANAGER')")
					/**/.antMatchers("/settings", "/user/savepwd").authenticated()// anyroles
					/**/.anyRequest().authenticated()
					.and()
					/**/.formLogin()
					/**/.loginPage("/signin")
					/**/.loginProcessingUrl("/signin/authenticate")
					/**/.successHandler(new MyWebSuccessHandler(securityService))
					/**/.failureHandler(new MyWebFailureHandler("/signin?error", messages))
					/**/.permitAll()
					.and()
					/**/.logout()
					/**/.logoutSuccessUrl("/home?logout")
					/**/.permitAll()
					.and()
					/**/.rememberMe().tokenRepository(persistentTokenRepository()).tokenValiditySeconds(1209600)
					.and()
					.addFilterBefore(new JWTAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
		}
	}

	@Autowired
	DataSource dataSource;

	@Bean
	public PersistentTokenRepository persistentTokenRepository() {
		JdbcTokenRepositoryImpl db = new JdbcTokenRepositoryImpl();
		db.setDataSource(dataSource);
		return db;
	}

	@Bean
	public MyPersistentTokenBasedRememberMeServices persistentTokenBasedRememberMeServices() {
		MyPersistentTokenBasedRememberMeServices ptbrms = new MyPersistentTokenBasedRememberMeServices("remember-me", userDetailsService, persistentTokenRepository());
		return ptbrms;
	}

	@Autowired
	private MyCorsFilter myCorsFilter;
}
