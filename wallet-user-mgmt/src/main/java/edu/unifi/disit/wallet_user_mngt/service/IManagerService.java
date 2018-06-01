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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import edu.unifi.disit.wallet_user_mngt.datamodel.Campaign;
import edu.unifi.disit.wallet_user_mngt.datamodel.Ecosystem;
import edu.unifi.disit.wallet_user_mngt.object.CampaignStatus;
import edu.unifi.disit.wallet_user_mngt.object.PrizeListForm;
import edu.unifi.disit.wallet_user_mngt.object.ScoreboardEntry;

public interface IManagerService {

	Page<Campaign> getCampaignsForLoggedUser(PageRequest pageRequest);

	Page<Object[]> getTraceInForEcosystem(Long id, PageRequest pageRequest);

	Page<Object[]> getTraceOutForEcosystem(Long id, PageRequest pageRequest);

	String getCampaignName(Long id);

	String getEcosystemNameByCampaign(Long id);

	Campaign saveCampaign(Campaign campaign, PrizeListForm prizeList);

	List<Ecosystem> getEcosystems();

	Campaign getCampaign(Long id);

	List<CampaignStatus> getStatus(List<Campaign> campaigns);

	List<ScoreboardEntry> getScoreboardTotalPilota();

	List<ScoreboardEntry> getScoreboard7daysPilota();
}
