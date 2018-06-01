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

import java.util.List;
import java.util.Set;

import edu.unifi.disit.wallet_user_mngt.datamodel.feedback.EmailFeedbackCampaignToken;
import edu.unifi.disit.wallet_user_mngt.exception.FeedbackTokenNotExistException;

public interface IEmailFeedbackCampaignService {

	// retrieve the user NOT cancelled to this emailFeedbackCampaignID
	// TODO manage cancellation from any kind of notification
	public Set<String> retrieveUsers(Long emailFeedbackCampaignID);

	public void removeUserFrom(String emailFeedbackCampaignToken);

	public void feedbackUserFrom(String emailFeedbackCampaignToken) throws FeedbackTokenNotExistException;

	public List<EmailFeedbackCampaignToken> getMyCancelledFeedbackCampaign();

	public void removeCancellation(String removeid);

	// public List<EmailFeedbackCampaignToken> getEmailFeedbackCampaignToken(int i);

}
