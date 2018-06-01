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

import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import edu.unifi.disit.wallet_user_mngt.datamodel.Prize;
import edu.unifi.disit.wallet_user_mngt.object.PrizeListForm;

public class PrizeListFormValidator implements Validator {

	MessageSource messages;

	public PrizeListFormValidator(MessageSource messages) {
		this.messages = messages;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return PrizeListForm.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		PrizeListForm prizeListForm = (PrizeListForm) target;
		if ((prizeListForm.getPrizeList() != null) && (checkPrizeListRuleNameNotNull(prizeListForm.getPrizeList()))) {
			errors.rejectValue("prizeList", "error.prizeList", messages.getMessage("validator.notnull", null, LocaleContextHolder.getLocale()));
		}
	}

	private boolean checkPrizeListRuleNameNotNull(List<Prize> prizeList) {
		for (Prize p : prizeList) {
			if ((p.getName() == null) || (p.getName().equals("")))
				return true;
		}
		return false;
	}
}
