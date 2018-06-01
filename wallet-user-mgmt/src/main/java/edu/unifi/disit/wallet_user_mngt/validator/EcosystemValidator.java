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

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import edu.unifi.disit.wallet_user_mngt.datamodel.Ecosystem;

public class EcosystemValidator implements Validator {

	MessageSource messages;

	public EcosystemValidator(MessageSource messages) {
		this.messages = messages;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return Ecosystem.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		Ecosystem ecosystem = (Ecosystem) target;
		if (ecosystem.getName() == null) {
			errors.rejectValue("name", "error.ecosystem", messages.getMessage("validator.notnull", null, LocaleContextHolder.getLocale()));
		} else if (ecosystem.getName().equals("")) {
			errors.rejectValue("name", "error.ecosystem", messages.getMessage("validator.notnull", null, LocaleContextHolder.getLocale()));
		}
	}
}
