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

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import edu.unifi.disit.wallet_user_mngt.datamodel.User;

@Entity
public class EmailFeedbackCampaignToken {

	@Id
	// @GeneratedValue(strategy = GenerationType.IDENTITY)
	private String token;

	Date sentTime;

	Date feedbackTime;

	Boolean confirmedRejected;// confirmed==TRUE, means he follows the link
	// rejected ==FALSE mean he clicked he doens't wanna receive anymore this info

	@ManyToOne
	private User user;

	@ManyToOne
	private EmailFeedbackCampaign emailfeedbackcampaign;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Date getSentTime() {
		return sentTime;
	}

	public void setSentTime(Date sentTime) {
		this.sentTime = sentTime;
	}

	public Date getFeedbackTime() {
		return feedbackTime;
	}

	public void setFeedbackTime(Date feedbackTime) {
		this.feedbackTime = feedbackTime;
	}

	public Boolean getConfirmedRejected() {
		return confirmedRejected;
	}

	public void setConfirmedRejected(Boolean confirmedRejected) {
		this.confirmedRejected = confirmedRejected;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public EmailFeedbackCampaign getEmailfeedbackcampaign() {
		return emailfeedbackcampaign;
	}

	public void setEmailfeedbackcampaign(EmailFeedbackCampaign emailfeedbackcampaign) {
		this.emailfeedbackcampaign = emailfeedbackcampaign;
	}

	@Override
	public String toString() {
		return "EmaillFeedbackCampaignToken [token=" + token + ", sentTime=" + sentTime + ", feedbackTime=" + feedbackTime + ", confirmedRejected=" + confirmedRejected + ", user=" + user + ", emailfeedbackcampaign=" + emailfeedbackcampaign
				+ "]";
	}

}
