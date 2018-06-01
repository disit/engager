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
package edu.unifi.disit.wallet_user_mngt_test;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
//import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.unifi.disit.wallet_user_mngt.Application;
import edu.unifi.disit.wallet_user_mngt.datamodel.Campaign;
import edu.unifi.disit.wallet_user_mngt.datamodel.CampaignDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.Ecosystem;
import edu.unifi.disit.wallet_user_mngt.datamodel.EcosystemDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.Prize;
import edu.unifi.disit.wallet_user_mngt.datamodel.PrizeDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.Roletype;
import edu.unifi.disit.wallet_user_mngt.datamodel.Rule;
import edu.unifi.disit.wallet_user_mngt.datamodel.RuleDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.TraceIn;
import edu.unifi.disit.wallet_user_mngt.datamodel.TraceInDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.TraceOut;
import edu.unifi.disit.wallet_user_mngt.datamodel.TraceOutDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.User;
import edu.unifi.disit.wallet_user_mngt.datamodel.UserDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.VerificationTokenDAO;
import edu.unifi.disit.wallet_user_mngt.service.IAdminService;

@RunWith(SpringJUnit4ClassRunner.class)
// @ContextConfiguration(classes = Application.class, loader = SpringApplicationContextLoader.class)
@ContextConfiguration(classes = Application.class, loader = SpringBootContextLoader.class)
@SpringBootTest
public class TestDB {

	@Autowired
	TraceInDAO traceInDAO;

	@Autowired
	TraceOutDAO traceOutDAO;

	@Autowired
	UserDAO userDAO;

	@Autowired
	RuleDAO ruleDAO;

	@Autowired
	CampaignDAO campaignDAO;

	@Autowired
	EcosystemDAO ecosystemDAO;

	@Autowired
	PrizeDAO prizeDAO;

	@Autowired
	VerificationTokenDAO verificationTokenDAO;

	@Autowired
	private IAdminService adminService;

	@Test
	public void testFindManagers() {
		Page<User> managers = adminService.getAllManagers(new PageRequest(0, 10));
		for (User u : managers) {
			System.out.println(u.getUsername());
		}
	}

	@Test
	public void testFindCampaignByManagerId() {
		List<Campaign> campaigns = adminService.getCampaignByManager(3l);
		for (Campaign c : campaigns) {
			System.out.println(c.getName());
		}
	}

	@Test
	public void addall() {

		// rule
		Rule r1 = new Rule();
		r1.setName("survey turista");
		r1.setValue(100l);
		ruleDAO.save(r1);

		Rule r2 = new Rule();
		r2.setName("survey mobility");
		r2.setValue(10l);
		ruleDAO.save(r2);

		Rule r3 = new Rule();
		r3.setName("shoot a photo");
		r3.setValue(50l);
		ruleDAO.save(r3);

		// ecosystems
		// +rules
		Ecosystem e1 = new Ecosystem();
		e1.setName("sii mobility");
		e1.getRules().add(r1);
		e1.getRules().add(r2);
		e1.getRules().add(r3);
		ecosystemDAO.save(e1);

		Ecosystem e2 = new Ecosystem();
		e2.setName("coop");
		e2.getRules().add(r3);
		ecosystemDAO.save(e2);

		// users
		User u1 = new User();
		u1.setNickname("ADMIN");
		u1.setPassword("$2a$10$t2xTnPWpQzTzgsuTunD2Gew/J85tLWMgJo1yzFfOl1WIoPFLvqhWK");
		u1.setRegistrationDate(new Date());
		u1.setRoletype(Roletype.ROLE_ADMIN);
		u1.setUsername("ADMIN@ADMIN.it");
		u1.setVisible(true);
		userDAO.save(u1);

		// +ecosystem
		User u2 = new User();
		u2.setNickname("MANAGER_tutto");
		u2.setPassword("$2a$10$t2xTnPWpQzTzgsuTunD2Gew/J85tLWMgJo1yzFfOl1WIoPFLvqhWK");
		u2.setRegistrationDate(new Date());
		u2.setRoletype(Roletype.ROLE_MANAGER);
		u2.setUsername("MANAGER1@MANAGER.it");
		u2.setVisible(true);
		u2.getEcosystems().add(e1);
		u2.getEcosystems().add(e2);
		userDAO.save(u2);

		User u3 = new User();
		u3.setNickname("MANAGER_COOP");
		u3.setPassword("$2a$10$t2xTnPWpQzTzgsuTunD2Gew/J85tLWMgJo1yzFfOl1WIoPFLvqhWK");
		u3.setRegistrationDate(new Date());
		u3.setRoletype(Roletype.ROLE_MANAGER);
		u3.setUsername("MANAGER2@MANAGER.it");
		u3.setVisible(true);
		u3.getEcosystems().add(e2);
		userDAO.save(u3);

		User u4 = new User();
		u4.setNickname("user1");
		u4.setPassword("$2a$10$t2xTnPWpQzTzgsuTunD2Gew/J85tLWMgJo1yzFfOl1WIoPFLvqhWK");
		u4.setRegistrationDate(new Date());
		u4.setRoletype(Roletype.ROLE_USER);
		u4.setUsername("user1@user.it");
		u4.setVisible(true);
		userDAO.save(u4);

		User u5 = new User();
		u5.setNickname("user2");
		u5.setPassword("$2a$10$t2xTnPWpQzTzgsuTunD2Gew/J85tLWMgJo1yzFfOl1WIoPFLvqhWK");// hash of ange
		u5.setRegistrationDate(new Date());
		u5.setRoletype(Roletype.ROLE_USER);
		u5.setUsername("user2@user.it");
		u5.setVisible(true);
		userDAO.save(u5);

		User u6 = new User();
		u6.setNickname("user3");
		u6.setPassword("$2a$10$t2xTnPWpQzTzgsuTunD2Gew/J85tLWMgJo1yzFfOl1WIoPFLvqhWK");
		u6.setRegistrationDate(new Date());
		u6.setRoletype(Roletype.ROLE_USER);
		u6.setUsername("user3@user.it");
		u6.setVisible(true);
		userDAO.save(u6);

		User u7 = new User();
		u7.setNickname("user4");
		u7.setPassword("$2a$10$t2xTnPWpQzTzgsuTunD2Gew/J85tLWMgJo1yzFfOl1WIoPFLvqhWK");// hash of ange
		u7.setRegistrationDate(new Date());
		u7.setRoletype(Roletype.ROLE_USER);
		u7.setUsername("user4@user.it");
		u7.setVisible(true);
		userDAO.save(u7);

		User u8 = new User();
		u8.setNickname("user5");
		u8.setPassword("$2a$10$t2xTnPWpQzTzgsuTunD2Gew/J85tLWMgJo1yzFfOl1WIoPFLvqhWK");
		u8.setRegistrationDate(new Date());
		u8.setRoletype(Roletype.ROLE_USER);
		u8.setUsername("user5@user.it");
		u8.setVisible(true);
		userDAO.save(u8);

		User u9 = new User();
		u9.setNickname("user6");
		u9.setPassword("$2a$10$t2xTnPWpQzTzgsuTunD2Gew/J85tLWMgJo1yzFfOl1WIoPFLvqhWK");// hash of ange
		u9.setRegistrationDate(new Date());
		u9.setRoletype(Roletype.ROLE_USER);
		u9.setUsername("user6@user.it");
		u9.setVisible(true);
		userDAO.save(u9);

		User u10 = new User();
		u10.setNickname("user7");
		u10.setPassword("$2a$10$t2xTnPWpQzTzgsuTunD2Gew/J85tLWMgJo1yzFfOl1WIoPFLvqhWK");// hash of ange
		u10.setRegistrationDate(new Date());
		u10.setRoletype(Roletype.ROLE_USER);
		u10.setUsername("user7@user.it");
		u10.setVisible(true);
		userDAO.save(u10);

		User u11 = new User();
		u11.setNickname("user8");
		u11.setPassword("$2a$10$t2xTnPWpQzTzgsuTunD2Gew/J85tLWMgJo1yzFfOl1WIoPFLvqhWK");// hash of ange
		u11.setRegistrationDate(new Date());
		u11.setRoletype(Roletype.ROLE_USER);
		u11.setUsername("user8@user.it");
		u11.setVisible(true);
		userDAO.save(u11);

		// campaign
		// +premio
		Campaign c1 = new Campaign();
		c1.setName("ataf ticket");
		c1.setRate(10l);
		c1.setHowmany(100l);
		c1.setStartDate(new Date());
		c1.setEcosystem(e1);
		c1.setUser(u2);
		campaignDAO.save(c1);

		Campaign c2 = new Campaign();
		c2.setName("turismo");
		c2.setRate(10l);
		c2.setHowmany(30l);
		c2.setStartDate(new Date());
		c2.setEcosystem(e1);
		c2.setUser(u2);
		campaignDAO.save(c2);

		Campaign c3 = new Campaign();
		c3.setName("coop.fi tessera");
		c3.setRate(2l);
		c3.setHowmany(10l);
		c3.setStartDate(new Date());
		c3.setEcosystem(e2);
		c3.setUser(u3);
		campaignDAO.save(c3);

		// premio
		// +compaign
		Prize p1 = new Prize();
		p1.setName("biglietto del bus");
		p1.setValue(130l);
		p1.setCampaign(c1);
		prizeDAO.save(p1);

		Prize p2 = new Prize();
		p2.setName("abbonamento del bus");
		p2.setValue(400l);
		p2.setCampaign(c1);
		prizeDAO.save(p2);

		Prize p3 = new Prize();
		p3.setName("book di firenze");
		p3.setValue(1500l);
		p3.setCampaign(c2);
		prizeDAO.save(p3);

		Prize p4 = new Prize();
		p4.setName("sconto coop");
		p4.setValue(200l);
		p4.setCampaign(c3);
		prizeDAO.save(p4);

		// storico, trace
		// user3, su e1
		TraceIn t1 = new TraceIn();
		t1.setCampaign(c1);
		t1.setUser(u5);
		t1.setRule(r1);
		t1.setTime(new Date());
		traceInDAO.save(t1);
		TraceIn t2 = new TraceIn();
		t2.setCampaign(c1);
		t2.setUser(u5);
		t2.setRule(r1);
		t2.setTime(new Date());
		traceInDAO.save(t2);
		TraceIn t3 = new TraceIn();
		t3.setCampaign(c1);
		t3.setUser(u5);
		t3.setRule(r1);
		t3.setTime(new Date());
		traceInDAO.save(t3);
		TraceIn t4 = new TraceIn();
		t4.setCampaign(c1);
		t4.setUser(u5);
		t4.setRule(r2);
		t4.setTime(new Date());
		traceInDAO.save(t4);
		TraceIn t5 = new TraceIn();
		t5.setCampaign(c1);
		t5.setUser(u5);
		t5.setRule(r3);
		t5.setTime(new Date());
		traceInDAO.save(t5);
		// user3, su e2
		TraceIn t6 = new TraceIn();
		t6.setCampaign(c3);
		t6.setUser(u5);
		t6.setRule(r3);
		t6.setTime(new Date());
		traceInDAO.save(t6);
		// user4, su e1
		TraceIn t7 = new TraceIn();
		t7.setCampaign(c1);
		t7.setUser(u4);
		t7.setRule(r1);
		t7.setTime(new Date());
		traceInDAO.save(t7);
		TraceIn t8 = new TraceIn();
		t8.setCampaign(c1);
		t8.setUser(u4);
		t8.setRule(r2);
		t8.setTime(new Date());
		traceInDAO.save(t8);
		// user5, su e1
		TraceIn t9 = new TraceIn();
		t9.setCampaign(c1);
		t9.setUser(u5);
		t9.setRule(r1);
		t9.setTime(new Date());
		traceInDAO.save(t9);
		// user6, su e1
		TraceIn t10 = new TraceIn();
		t10.setCampaign(c1);
		t10.setUser(u6);
		t10.setRule(r1);
		t10.setTime(new Date());
		traceInDAO.save(t10);
		// user7, su e1
		TraceIn t11 = new TraceIn();
		t11.setCampaign(c1);
		t11.setUser(u7);
		t11.setRule(r1);
		t11.setTime(new Date());
		traceInDAO.save(t11);
		// user8, su e1
		TraceIn t12 = new TraceIn();
		t12.setCampaign(c1);
		t12.setUser(u8);
		t12.setRule(r1);
		t12.setTime(new Date());
		traceInDAO.save(t12);
		// user9, su e1
		TraceIn t13 = new TraceIn();
		t13.setCampaign(c1);
		t13.setUser(u9);
		t13.setRule(r1);
		t13.setTime(new Date());
		traceInDAO.save(t13);
		// user10, su e1
		TraceIn t14 = new TraceIn();
		t14.setCampaign(c1);
		t14.setUser(u10);
		t14.setRule(r1);
		t14.setTime(new Date());
		traceInDAO.save(t14);
		// user11, su e1
		TraceIn t15 = new TraceIn();
		t15.setCampaign(c1);
		t15.setUser(u11);
		t15.setRule(r1);
		t15.setTime(new Date());
		traceInDAO.save(t15);

		// traceout
		TraceOut to1 = new TraceOut();
		to1.setUser(u11);
		to1.setPrize(p1);
		to1.setTime(new Date());
		traceOutDAO.save(to1);
		TraceOut to2 = new TraceOut();
		to2.setUser(u11);
		to2.setPrize(p2);
		to2.setTime(new Date());
		traceOutDAO.save(to2);
		TraceOut to3 = new TraceOut();
		to3.setUser(u11);
		to3.setPrize(p3);
		to3.setTime(new Date());
		traceOutDAO.save(to3);
		TraceOut to4 = new TraceOut();
		to4.setUser(u11);
		to4.setPrize(p4);
		to4.setTime(new Date());
		traceOutDAO.save(to4);
		TraceOut to5 = new TraceOut();
		to5.setUser(u11);
		to5.setPrize(p1);
		to5.setTime(new Date());
		traceOutDAO.save(to5);
		TraceOut to6 = new TraceOut();
		to6.setUser(u11);
		to6.setPrize(p1);
		to6.setTime(new Date());
		traceOutDAO.save(to6);

	}
}
