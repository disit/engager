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

import java.util.List;
import java.util.Locale;

import org.springframework.context.ApplicationEvent;

public class OnPreparationEmailCompleteEvent extends ApplicationEvent {

	private static final long serialVersionUID = 3527333443293159677L;

	private final String to;
	private final String subject;
	private final List<String> text;
	private final Locale lang;
	private EmailScenarioType scenario;

	// simple scenario
	public OnPreparationEmailCompleteEvent(String to, String subject, List<String> text, Locale lang) {
		super(to);

		this.to = to;
		this.subject = subject;
		this.text = text;
		this.lang = lang;

		this.scenario = EmailScenarioType.SIMPLE;
	}

	public OnPreparationEmailCompleteEvent(String to, String subject, List<String> text, Locale lang, EmailScenarioType scenario) {
		super(to);

		this.to = to;
		this.subject = subject;
		this.text = text;
		this.lang = lang;

		this.scenario = scenario;
	}

	public String getTo() {
		return to;
	}

	public String getSubject() {
		return subject;
	}

	public List<String> getText() {
		return text;
	}

	public Locale getLang() {
		return lang;
	}

	public EmailScenarioType getEmailScenarioType() {
		return scenario;
	}
}
