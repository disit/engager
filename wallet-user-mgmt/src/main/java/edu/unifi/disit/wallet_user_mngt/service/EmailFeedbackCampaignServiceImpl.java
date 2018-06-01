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

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.unifi.disit.wallet_user_mngt.datamodel.User;
import edu.unifi.disit.wallet_user_mngt.datamodel.UserDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.feedback.EmailFeedbackCampaignDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.feedback.EmailFeedbackCampaignToken;
import edu.unifi.disit.wallet_user_mngt.datamodel.feedback.EmailFeedbackCampaignTokenDAO;
import edu.unifi.disit.wallet_user_mngt.exception.FeedbackTokenNotExistException;

@Service
public class EmailFeedbackCampaignServiceImpl implements IEmailFeedbackCampaignService {

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	EmailFeedbackCampaignTokenDAO emailFCTDAO;

	@Autowired
	EmailFeedbackCampaignDAO emailFCDAO;

	@Autowired
	UserDAO userDAO;

	@Autowired
	private ISecurityService securityService;

	@Override
	public Set<String> retrieveUsers(Long emailFeedbackCampaignID) {

		// retrieve all the user that cancelled for this emailFeedbackCampaignID
		List<EmailFeedbackCampaignToken> efct = emailFCTDAO.findByConfirmedRejectedAndEmailfeedbackcampaign_Id(Boolean.FALSE, emailFeedbackCampaignID);

		List<User> allUsers = userDAO.findAll();

		Set<String> toreturn = new HashSet<String>();

		for (User user : allUsers) {
			// check if in the list
			logger.debug("checking user {}", user.getUsername());

			if (!contiene(efct, user)) {
				logger.debug("passed {}", user.getUsername());
				toreturn.add(user.getUsername());
			}
		}

		return toreturn;

	}

	private boolean contiene(List<EmailFeedbackCampaignToken> efcts, User user) {
		for (EmailFeedbackCampaignToken efct : efcts) {
			if (efct.getUser().getUsername().equals(user.getUsername()))
				return true;
		}
		return false;
	}

	@Override
	public void removeUserFrom(String emailFeedbackCampaignToken) {

		// retrieve user from this token
		EmailFeedbackCampaignToken efct = emailFCTDAO.findByToken(emailFeedbackCampaignToken);

		efct.setConfirmedRejected(Boolean.FALSE);
		efct.setFeedbackTime(new Date());

		emailFCTDAO.save(efct);

	}

	@Override
	public void feedbackUserFrom(String emailFeedbackCampaignToken) throws FeedbackTokenNotExistException {

		// retrieve user from this token
		EmailFeedbackCampaignToken efct = emailFCTDAO.findByToken(emailFeedbackCampaignToken);

		if (efct == null)
			throw new FeedbackTokenNotExistException();

		efct.setConfirmedRejected(Boolean.TRUE);
		efct.setFeedbackTime(new Date());

		emailFCTDAO.save(efct);

	}

	@Override
	public List<EmailFeedbackCampaignToken> getMyCancelledFeedbackCampaign() {
		// List<String> cancelled = new ArrayList<String>();

		// List<EmailFeedbackCampaignToken> mycancelledtokens =

		// for (EmailFeedbackCampaignToken efct : mycancelledtokens)
		// cancelled.add(efct.getEmailfeedbackcampaign().getName());
		// in theory here there are no repetition, because when you cancel your subscription
		// you don't cannot cancel again since you're not in the list anymore

		return emailFCTDAO.findByConfirmedRejectedAndUser(Boolean.FALSE, securityService.findLoggedInUser());
	}

	@Override
	public void removeCancellation(String token) {
		EmailFeedbackCampaignToken e = emailFCTDAO.findByToken(token);
		e.setConfirmedRejected(null);
		emailFCTDAO.save(e);

	}
}
