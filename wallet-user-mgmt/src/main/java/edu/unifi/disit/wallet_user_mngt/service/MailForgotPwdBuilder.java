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
package edu.unifi.disit.wallet_user_mngt.service;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailForgotPwdBuilder {

	@Autowired
	private MessageSource messages;

	@Value("${application.url}")
	private String myappuri;

	private TemplateEngine templateEngine;

	@Autowired
	public MailForgotPwdBuilder(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public String build(String link, Locale lang) {

		Context context = new Context();

		context.setVariable("imageurl", myappuri + "/image/km4city-small.png");
		context.setVariable("body", messages.getMessage("resetpwd.mail.body", null, lang));
		context.setVariable("here", messages.getMessage("resetpwd.mail.here", null, lang));
		context.setVariable("duration", messages.getMessage("resetpwd.mail.duration", null, lang));
		context.setVariable("link", link);

		return templateEngine.process("mail_forgotpwd", context);
	}
}
