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
package edu.unifi.disit.wallet_user_mngt.event;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import edu.unifi.disit.wallet_user_mngt.service.MailFeedbackOpenBuilder;
import edu.unifi.disit.wallet_user_mngt.service.MailForgotPwdBuilder;

@Component
public class EmailListener implements ApplicationListener<OnPreparationEmailCompleteEvent> {

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	MailForgotPwdBuilder mfpb;

	@Autowired
	MailFeedbackOpenBuilder mfob;

	@Autowired
	private JavaMailSender mailSender;

	@Value("${spring.mail.from}")
	private String from;

	@Override
	public void onApplicationEvent(OnPreparationEmailCompleteEvent event) {
		this.confirmRegistration(event);
	}

	private void confirmRegistration(OnPreparationEmailCompleteEvent event) {

		if (event.getEmailScenarioType().equals(EmailScenarioType.SIMPLE)) {
			// simple scanario --> simple email in txt
			SimpleMailMessage email = new SimpleMailMessage();
			email.setFrom(from);
			email.setTo(event.getTo());
			email.setSubject(event.getSubject());
			if (event.getText().size() > 0)
				email.setText(event.getText().get(0));
			else
				email.setText("no text specified");
			mailSender.send(email);
		} else if (event.getEmailScenarioType().equals(EmailScenarioType.FORGOT_PWD)) {
			// forgot scanario --> email in html + template
			MimeMessage mm = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mm);
			try {
				helper.setFrom(from);
				helper.setTo(event.getTo());
				helper.setSubject(event.getSubject());
				helper.setText(mfpb.build(event.getText().get(0), event.getLang()), true);

			} catch (MessagingException e) {
				logger.error("error {}", e);
			}
			mailSender.send(mm);
		} else if (event.getEmailScenarioType().equals(EmailScenarioType.FEEDBACK_OPEN)) {
			// feedback open --> email in html + template
			MimeMessage mm = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mm);
			try {
				helper.setFrom(from);
				helper.setTo(event.getTo());
				helper.setSubject(event.getSubject());
				helper.setText(mfob.build(event.getText().get(0), event.getText().get(1), event.getLang()), true);

			} catch (MessagingException e) {
				logger.error("error {}", e);
			}
			mailSender.send(mm);
		}

		logger.debug("sending email to {}", event.getTo());
	}
}
