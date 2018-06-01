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
package edu.unifi.disit.wallet_user_mngt.validator;

import java.util.Date;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import edu.unifi.disit.wallet_user_mngt.datamodel.Campaign;

public class CampaignValidator implements Validator {

	MessageSource messages;

	public CampaignValidator(MessageSource messages) {
		this.messages = messages;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return Campaign.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		Campaign campaign = (Campaign) target;

		if (campaign.getName() == null) {
			errors.rejectValue("name", "error.campaign", messages.getMessage("campaign.validator.notnull", null, LocaleContextHolder.getLocale()));
		} else if (campaign.getName().length() < 2) {
			errors.rejectValue("name", "error.campaign", messages.getMessage("campaign.validator.length", new Object[] { 2 }, LocaleContextHolder.getLocale()));
		}

		if (campaign.getHowmany() == null) {
			errors.rejectValue("howmany", "error.campaign", messages.getMessage("campaign.validator.notnull", null, LocaleContextHolder.getLocale()));
		} else if (campaign.getHowmany() < 1) {
			errors.rejectValue("howmany", "error.campaign", messages.getMessage("campaign.validator.length", new Object[] { 1 }, LocaleContextHolder.getLocale()));
		}

		if (campaign.getRate() == null) {
			errors.rejectValue("rate", "error.campaign", messages.getMessage("campaign.validator.notnull", null, LocaleContextHolder.getLocale()));
		} else if (campaign.getRate() < 1) {
			errors.rejectValue("rate", "error.campaign", messages.getMessage("campaign.validator.length", new Object[] { 21 }, LocaleContextHolder.getLocale()));
		}

		if (campaign.getStartDate() == null) {
			errors.rejectValue("startDate", "error.campaign", messages.getMessage("campaign.validator.notnull", null, LocaleContextHolder.getLocale()));
		} else if (campaign.getStartDate().before(new Date())) {
			errors.rejectValue("startDate", "error.campaign", messages.getMessage("campaign.validator.startdate", null, LocaleContextHolder.getLocale()));
		}

		if (campaign.getEcosystem() == null) {
			errors.rejectValue("ecosystem", "error.campaign", messages.getMessage("campaign.validator.notnull", null, LocaleContextHolder.getLocale()));
		} else if (campaign.getEcosystem().getId() == -1) {
			errors.rejectValue("ecosystem.id", "error.campaign", messages.getMessage("campaign.validator.notnull", null, LocaleContextHolder.getLocale()));
		}
	}
}
