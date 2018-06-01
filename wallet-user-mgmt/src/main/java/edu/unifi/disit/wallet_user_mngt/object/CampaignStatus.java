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
package edu.unifi.disit.wallet_user_mngt.object;

import edu.unifi.disit.wallet_user_mngt.datamodel.CampaignStatustype;

public class CampaignStatus {

	// not started yet (how many days to start) ------ WAITING, howmany days
	// started (how many days started? day to end?) -- ACTIVE, how many days to end + how many days in total
	// finished -------------------------------------- FINISHED

	CampaignStatustype status;
	Integer progress;
	Integer total;

	public CampaignStatus() {
		this(CampaignStatustype.CAMPAIGNSTATUS_TERMINATED, 0, 0);
	}

	public CampaignStatus(CampaignStatustype status, Integer progress, Integer total) {
		super();
		this.status = status;
		this.progress = progress;
		this.total = total;
	}

	public CampaignStatustype getStatus() {
		return status;
	}

	public void setStatus(CampaignStatustype status) {
		this.status = status;
	}

	public Integer getProgress() {
		return progress;
	}

	public void setProgress(Integer progress) {
		this.progress = progress;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	@Override
	public String toString() {
		return "CampaignStatus [status=" + status + ", progress=" + progress + ", total=" + total + "]";
	}
}
