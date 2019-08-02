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
package edu.unifi.disit.executerchecker_surveys;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import disit.engager_base.ACTION;
import edu.unifi.disit.engagerapi.datamodel.Response;
import edu.unifi.disit.engagerapi.executed.IRuleChecker;

public class MyChecker implements IRuleChecker {

	private static final DBinterfaceAccessLog dbi_accesslog = DBinterfaceAccessLog.getInstance();
	private static final DBinterfaceEngager dbi_engager = DBinterfaceEngager.getInstance();
	private static final DBinterfaceUserProfiler dbi_userprofiler = DBinterfaceUserProfiler.getInstance();

	@Override
	public List<String> getRuleNames() {
		List<String> toreturn = new ArrayList<String>();

		toreturn.add("shoot_a_photo_");// R1
		// R2, parking
		toreturn.add("daily_event_");// R3 just for banned/cancelled
		toreturn.add("survey_turist_");// R4
		toreturn.add("confirm_ppoi_home_");// R5
		toreturn.add("confirm_ppoi_school_");// R6
		toreturn.add("confirm_ppoi_work_");// R7
		toreturn.add("spent_time_");// R8
		toreturn.add("mobility_feedback_");// R9
		toreturn.add("confirm_extrappoi_");// R10

		toreturn.add("transport_previous_pisa_");// R11
		toreturn.add("transport_previous_prato_");// R12

		toreturn.add("transport_previous_firenze_");// R13
		toreturn.add("transport_next_firenze_");// R14
		toreturn.add("current_publictransport_firenze_");// R15
		toreturn.add("review_publictransport_firenze_");// R16

		toreturn.add("survey_publictransport1_firenze_");// R17
		toreturn.add("survey_publictransport2_firenze_");// R18

		return toreturn;
	}

	@Override
	public Date checkExecuted(Response r) {
		// if ACTION is a SURVEY
		if (r.getRule_name().startsWith("survey_turist_") ||
				r.getRule_name().startsWith("confirm_ppoi_home_") ||
				r.getRule_name().startsWith("confirm_ppoi_school_") ||
				r.getRule_name().startsWith("confirm_ppoi_work_") ||
				r.getRule_name().startsWith("spent_time_") ||
				r.getRule_name().startsWith("mobility_feedback_") ||
				r.getRule_name().startsWith("survey_publictransport1_firenze_") ||
				r.getRule_name().startsWith("survey_publictransport2_firenze_") ||
				r.getRule_name().startsWith("confirm_extrappoi_"))
			// a survey has been sent (not depend on the result of the survey)
			return dbi_engager.getSurveyDate(r.getId());
		// if ACTION is --JUST-- shoot a photo
		else if (r.getRule_name().startsWith("shoot_a_photo_") ||
				r.getRule_name().startsWith("review_publictransport_firenze_"))
			// serviceUri was already cliccked
			return dbi_accesslog.retrieveAccessLogServiceUriClick(r);
		// if action is transport next/previous
		else if (r.getRule_name().startsWith("transport_previous_firenze_"))
			return dbi_userprofiler.retrievePT(r, 1);
		else if (r.getRule_name().startsWith("transport_next_firenze_"))
			return dbi_userprofiler.retrievePT(r, 3);
		// always executed
		else if (r.getRule_name().startsWith("current_publictransport_firenze_"))// ALWAYS EXECUTED x firenze
			return r.getTimeCreated();

		return null;
	}

	@Override
	public Boolean checkBanned(String userId, ACTION a) {

		// if ACTION is TAKE-A-PHOTO
		if (a.getAction_rulename().startsWith("shoot_a_photo_") ||
				a.getAction_rulename().startsWith("daily_event_") ||
				a.getAction_rulename().startsWith("review_publictransport_firenze_"))
			// serviceUri was already cliccked
			return (dbi_accesslog.retrieveAccessLogServiceUriClick(userId, a.getUri()) != null);
		// if ACTION is SURVEY
		else if (a.getAction_rulename().startsWith("survey_turist_") ||
				a.getAction_rulename().startsWith("confirm_ppoi_home_") ||
				a.getAction_rulename().startsWith("confirm_ppoi_school_") ||
				a.getAction_rulename().startsWith("confirm_ppoi_work_") ||
				a.getAction_rulename().startsWith("spent_time_") ||
				a.getAction_rulename().startsWith("mobility_feedback_") ||
				a.getAction_rulename().startsWith("survey_publictransport1_firenze_") ||
				a.getAction_rulename().startsWith("survey_publictransport2_firenze_") ||
				a.getAction_rulename().startsWith("confirm_extrappoi_"))
			// a survey has been sent (depend on the result of the survey)
			return (dbi_engager.retrieveSurvey(userId, a));
		return false;
	}

	@Override
	public Boolean checkCancelled(String userId, ACTION a) {

		// if ACTION is TAKE-A-PHOTO
		if (a.getAction_rulename().startsWith("shoot_a_photo_") ||
				a.getAction_rulename().startsWith("daily_event_") ||
				a.getAction_rulename().startsWith("review_publictransport_firenze_"))
			return (dbi_engager.retrieveCancelledUri(userId, a.getUri(), (a.getAction_bannedfor() * 60 * 1000)));
		// if ACTION is SURVEY
		else if (a.getAction_rulename().startsWith("survey_turist_") ||
				a.getAction_rulename().startsWith("confirm_ppoi_home_") ||
				a.getAction_rulename().startsWith("confirm_ppoi_school_") ||
				a.getAction_rulename().startsWith("confirm_ppoi_work_") ||
				a.getAction_rulename().startsWith("spent_time_") ||
				a.getAction_rulename().startsWith("mobility_feedback_") ||
				a.getAction_rulename().startsWith("survey_publictransport1_firenze_") ||
				a.getAction_rulename().startsWith("survey_publictransport2_firenze_") ||
				a.getAction_rulename().startsWith("confirm_extrappoi_"))
			return (dbi_engager.retrieveCancelledRule(userId, a.getAction_rulename(), (a.getAction_bannedfor() * 60 * 1000)));
		return false;
	}
}
