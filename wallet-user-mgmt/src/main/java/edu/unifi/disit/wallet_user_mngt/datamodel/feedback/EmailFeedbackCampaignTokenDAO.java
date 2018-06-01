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
package edu.unifi.disit.wallet_user_mngt.datamodel.feedback;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.unifi.disit.wallet_user_mngt.datamodel.User;

public interface EmailFeedbackCampaignTokenDAO extends JpaRepository<EmailFeedbackCampaignToken, Long> {

	EmailFeedbackCampaignToken findByToken(String token);

	List<EmailFeedbackCampaignToken> findByConfirmedRejectedAndEmailfeedbackcampaign_Id(Boolean confirmedRejected, Long id);

	List<EmailFeedbackCampaignToken> findByConfirmedRejectedNotNullAndEmailfeedbackcampaign_Id(Long id);

	List<EmailFeedbackCampaignToken> findByConfirmedRejectedAndUser(Boolean confirmedRejected, User u);
}
