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

import static org.junit.Assert.assertTrue;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.servlet.http.Cookie;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.unifi.disit.wallet_user_mngt.Application;
import edu.unifi.disit.wallet_user_mngt.object.Response;
import edu.unifi.disit.wallet_user_mngt.security.MultiHttpSecurityConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@WebAppConfiguration
@ContextConfiguration(classes = { Application.class, MultiHttpSecurityConfig.class }, loader = SpringBootContextLoader.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)

public class ApiDocumentationTest {

	ObjectMapper mapper = new ObjectMapper();

	@Rule
	public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext context;

	@Before
	public void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
				.apply(springSecurity())
				.apply(documentationConfiguration(this.restDocumentation))
				.build();
	}

	// TODO include JSESSIONID information

	// TODO test all web pages
	// @Test
	// public void indexExample() throws Exception {
	// this.mockMvc.perform(get("/home/1"))
	// .andExpect(status().isOk())
	// .andDo(document("index"));
	// }

	@Test
	public void testA1registration() throws Exception {
		MvcResult result = this.mockMvc.perform(post("/api/v1/registration").param("username", "angelo.difino@unifi.it").param("password", "test"))
				.andExpect(status().isOk())
				.andDo(document("registration"))
				.andReturn();

		Response r = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<Response>() {
		});

		assertTrue(r.getResult());
	}

	@Test
	public void testA2registration() throws Exception {
		MvcResult result = this.mockMvc.perform(post("/api/v1/registration").param("username", "angelo.difino@unifi.it").param("password", "test"))
				.andExpect(status().isOk())// TODO check json if response=false
				.andDo(document("registrationko"))
				.andReturn();

		Response r = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<Response>() {
		});

		assertTrue(!r.getResult());
	}

	// @Test NO automatically generated since need user authentication on terminal side
	// public void testB1socialsignin() throws Exception {
	// MvcResult result = this.mockMvc
	// .perform(post("/api/v1/signinsocial").param("accesstoken",
	// "facebook:<facebook_terminal_code>")
	// .param(
	// "remember-me",
	// "on"))
	// .andExpect(status().isUnauthorized())
	// .andDo(document("socialsignin"))
	// .andReturn();
	//
	// updateCookie(result);
	// }

	@Test
	public void testB2signin() throws Exception {
		MvcResult result = this.mockMvc.perform(post("/api/v1/signin").param("username", "angelo.difino@unifi.it").param("password", "test").param("remember-me", "on"))
				.andExpect(status().isOk())
				.andDo(document("signin"))
				.andReturn();

		updateCookie(result);
	}

	@Test
	public void testB3signin() throws Exception {
		MvcResult result = this.mockMvc.perform(post("/api/v1/signin").param("username", "angelo.difino@unifi.it").param("password", "test1").param("remember-me", "on"))
				.andExpect(status().isUnauthorized())
				.andDo(document("signinko"))
				.andReturn();

		updateCookie(result);
	}

	@Test
	public void testCadddeviceid() throws Exception {
		MvcResult result = this.mockMvc.perform(post("/api/v1/deviceid").cookie(readCookie()).param("deviceid", "test_device_id"))
				.andExpect(status().isOk())
				.andDo(document("adddeviceid")).andReturn();

		updateCookie(result);
	}

	@Test
	public void testDlastlogin() throws Exception {
		MvcResult result = this.mockMvc.perform(get("/api/v1/lastlogin").cookie(readCookie()))
				.andExpect(status().isOk())
				.andDo(document("getlastlogin")).andReturn();
		updateCookie(result);
	}

	@Test
	public void testEforgotpwd() throws Exception {

		// no need for cookie here
		this.mockMvc.perform(post("/api/v1/forgotpwd").param("username", "angelo.difino@unifi.it").param("appid", "fdck-a").param("lang", "it"))
				.andExpect(status().isOk())
				.andDo(document("forgotpwd")).andReturn();
	}

	@Test
	public void testFsavepwd() throws Exception {

		FileInputStream fis = new FileInputStream("forgotpwd.dat");
		ObjectInputStream ois = new ObjectInputStream(fis);
		Long id = (Long) ois.readObject();
		String token = (String) ois.readObject();
		ois.close();

		MvcResult result = this.mockMvc.perform(post("/api/v1/savepwd").param("id", String.valueOf(id)).param("token", token).param("password", "new_pwd"))
				.andExpect(status().isOk())
				.andDo(document("savepwd")).andReturn();

		updateCookie(result);
	}

	// ------------------------------LOGOUT

	@Test
	public void testWlogout() throws Exception {
		this.mockMvc.perform(post("/api/v1/logout").cookie(readCookie()))// logout actually does not need cookie
				.andExpect(status().isOk())
				.andDo(document("logout"));
	}

	@Test
	public void testXlastlogin() throws Exception {
		MvcResult result = this.mockMvc.perform(get("/api/v1/lastlogin").cookie(readCookie()))
				.andExpect(status().isUnauthorized())
				.andReturn();
		updateCookie(result);
	}

	@Test
	public void testYsignin() throws Exception {
		MvcResult result = this.mockMvc.perform(post("/api/v1/signin").param("username", "angelo.difino@unifi.it").param("password", "new_pwd").param("remember-me", "on"))
				.andExpect(status().isOk())
				.andReturn();

		updateCookie(result);
	}

	// ------------------------------DELETE
	@Test
	public void testZdelete() throws Exception {
		this.mockMvc.perform(post("/api/v1/delete").cookie(readCookie()))
				.andExpect(status().isOk())
				.andDo(document("delete"));
	}

	private Cookie readCookie() throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream("cookie.ser");
		ObjectInputStream ois = new ObjectInputStream(fis);
		Cookie c = (Cookie) ois.readObject();
		ois.close();
		return c;
	}

	private void updateCookie(MvcResult result) throws IOException {
		Cookie c = result.getResponse().getCookie("remember-me");
		if ((c != null) && (c.getValue() != null)) {
			assertTrue(c.getValue().length() > 10);
			FileOutputStream fos = new FileOutputStream("cookie.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(c);
			oos.close();
		}
	}
}
