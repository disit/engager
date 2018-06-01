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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import edu.unifi.disit.wallet_user_mngt.datamodel.Campaign;
import edu.unifi.disit.wallet_user_mngt.datamodel.CampaignDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.Ecosystem;
import edu.unifi.disit.wallet_user_mngt.datamodel.EcosystemDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.Prize;
import edu.unifi.disit.wallet_user_mngt.datamodel.PrizeDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.TraceInDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.TraceOutDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.User;
import edu.unifi.disit.wallet_user_mngt.datamodel.UserDAO;
import edu.unifi.disit.wallet_user_mngt.object.CampaignStatus;
import edu.unifi.disit.wallet_user_mngt.object.PrizeListForm;
import edu.unifi.disit.wallet_user_mngt.object.ScoreboardEntry;

@Service
public class ManagerServiceImpl implements IManagerService {

	private static final String CAMPAIGN_PILOTA = "PILOTA";

	@Autowired
	private CampaignDAO campaignDAO;

	@Autowired
	private TraceOutDAO traceOutDAO;

	@Autowired
	private TraceInDAO traceInDAO;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private PrizeDAO prizeDAO;

	@Autowired
	private EcosystemDAO ecosystemDAO;

	@Autowired
	private SecurityServiceImpl securityService;

	@Override
	public Page<Campaign> getCampaignsForLoggedUser(PageRequest pageRequest) {
		return campaignDAO.getCampaigns(securityService.findLoggedInUsername(), pageRequest);
	}

	@Override
	public Page<Object[]> getTraceInForEcosystem(Long id, PageRequest pageRequest) {
		return traceInDAO.findAllForEcosytem(id, pageRequest);
	}

	@Override
	public Page<Object[]> getTraceOutForEcosystem(Long id, PageRequest pageRequest) {
		return traceOutDAO.findAllForEcosytem(id, pageRequest);
	}

	@Override
	public String getCampaignName(Long id) {
		return campaignDAO.findById(id).getName();
	}

	@Override
	public String getEcosystemNameByCampaign(Long id) {
		return campaignDAO.findById(id).getEcosystem().getName();
	}

	@Override
	public Campaign saveCampaign(Campaign campaign, PrizeListForm prizeList) {
		// update selected ecosystem, since on the VIEW we just set the id
		campaign.setEcosystem(ecosystemDAO.findById(campaign.getEcosystem().getId()));
		String username = securityService.findLoggedInUsername();
		User u = userDAO.findByUsername(username);
		if (campaign.getUser() == null)
			campaign.setUser(u);
		Campaign toreturn = campaignDAO.save(campaign);
		List<Prize> prizes = prizeList.getPrizeList();
		for (Prize prize : prizes) {
			prize.setCampaign(campaign);
		}
		prizeDAO.save(prizes);
		return toreturn;
	}

	@Override
	public List<Ecosystem> getEcosystems() {
		return ecosystemDAO.findByUsersUsername(securityService.findLoggedInUsername());
	}

	@Override
	public Campaign getCampaign(Long id) {
		return campaignDAO.findById(id);
	}

	@Override
	public List<CampaignStatus> getStatus(List<Campaign> campaigns) {
		List<CampaignStatus> css = new ArrayList<CampaignStatus>();
		for (Campaign c : campaigns) {
			css.add(c.getCampaignStatus());
		}
		return css;
	}

	@Override
	public List<ScoreboardEntry> getScoreboard7daysPilota() {

		List<User> users = userDAO.findAll();

		Calendar ca = Calendar.getInstance();
		ca.setTimeInMillis(System.currentTimeMillis());
		ca.add(Calendar.HOUR, -24 * 7);

		List<ScoreboardEntry> scoreboard7days = new ArrayList<ScoreboardEntry>();
		for (User u : users) {
			ScoreboardEntry o = new ScoreboardEntry(u.getUsername(), traceInDAO.countPointsForUserForCampaingFromDate(u.getUsername(), CAMPAIGN_PILOTA, ca.getTime()));
			scoreboard7days.add(o);
		}
		Collections.sort(scoreboard7days);
		return scoreboard7days;
	}

	@Override
	public List<ScoreboardEntry> getScoreboardTotalPilota() {
		List<User> users = userDAO.findAll();
		List<ScoreboardEntry> scoreboardTotal = new ArrayList<ScoreboardEntry>();
		for (User u : users) {
			ScoreboardEntry o = new ScoreboardEntry(u.getUsername(), traceInDAO.countAllPointsForUserForCampaing(u.getUsername(), CAMPAIGN_PILOTA));
			scoreboardTotal.add(o);
		}
		Collections.sort(scoreboardTotal);

		return scoreboardTotal;
	}
}
