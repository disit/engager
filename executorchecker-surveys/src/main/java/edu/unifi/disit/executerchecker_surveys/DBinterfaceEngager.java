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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import disit.engager_base.ACTION;

public class DBinterfaceEngager {

	private static final Logger logger = LogManager.getLogger("DBinterface-engager");
	private SessionFactory sessionFactoryEngager;
	private static DBinterfaceEngager instance = null;

	public static DBinterfaceEngager getInstance() {
		if (instance == null) {
			synchronized (DBinterfaceEngager.class) {
				if (instance == null) {
					instance = new DBinterfaceEngager();
				}
			}
		}
		return instance;
	}

	private DBinterfaceEngager() {
		this.sessionFactoryEngager = new Configuration().configure("hibernate-engager.cfg.xml").buildSessionFactory();

	}

	public void close() throws Throwable {
		if (instance != null) {
			sessionFactoryEngager.close();
			sessionFactoryEngager = null;
			instance = null;
			super.finalize();
		}
	}

	// check if this user had already completed this survey
	// used from engager

	// this has to specialize basing on the type of rulename:
	// A -- if rule_name .startsWith("confirm_ppoi_")
	// if survey_response contains at least one "yes"
	// B -- if rule_name .startsWith("spent_time_")
	// if survey_response same message

	// retrieve true if in the interval [now-bannettime, now], the userId cancelled the uri
	public boolean retrieveCancelledUri(String userId, String uri, long bannedtime) {
		logger.debug("retrieveCancelledUri: userId:{} uri:{} bannedtime:{}", userId, uri, bannedtime);

		String sqlquery = "SELECT a.time " +
				"FROM engage_cancelled a " +
				"LEFT JOIN response b " +
				"ON a.response_id=b.id " +
				"WHERE (a.user_id='" + userId + "' AND b.action_uri='" + uri + "') " +
				"ORDER BY time DESC;";

		return retrieveCancelled(sqlquery, bannedtime);
	}

	// retrieve true if in the interval [now-bannettime, now], the userId cancelled the rule
	public boolean retrieveCancelledRule(String userId, String rule_name, Long bannedtime) {
		logger.debug("retrieveCancelledRule: userId:{} rule:{} bannedtime:{}", userId, rule_name, bannedtime);

		String sqlquery = "SELECT a.time " +
				"FROM engage_cancelled a " +
				"LEFT JOIN response b " +
				"ON a.response_id=b.id " +
				"WHERE (a.user_id='" + userId + "' AND b.rule_name='" + rule_name + "') " +
				"ORDER BY time DESC;";

		return retrieveCancelled(sqlquery, bannedtime);

	}

	// retrieve true if in the interval [now-bannettime, now], the time retrieved from the query is found
	@SuppressWarnings("unchecked")
	private boolean retrieveCancelled(String sqlquery, long bannedtime) {
		logger.debug("retrieveCancelledUri: sql_query{} bannedtime:{}", sqlquery, bannedtime);

		Session session = sessionFactoryEngager.openSession();
		Transaction tx = null;

		boolean toreturn = false;

		try {

			tx = session.beginTransaction();

			SQLQuery query = session.createSQLQuery(sqlquery);

			List<Object> entities = query.list();

			tx.commit();

			if (entities.size() > 0) {
				// consider just the last cancelled on
				if (((new Date()).getTime() - bannedtime - ((Timestamp) entities.get(0)).getTime()) < 0)
					toreturn = true;
			}

		} catch (Exception ex) {
			logger.error("cannot retrieve due:", ex);
			if (tx != null)
				tx.rollback();
		} finally {
			try {
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);

			}
		}

		logger.debug("retrieveCancelledUri return:{}", toreturn);
		return toreturn;
	}

	@SuppressWarnings("unchecked")
	public boolean retrieveSurvey(String userId, ACTION action) {

		String action_rulename = action.getAction_rulename();
		action_rulename = action_rulename.substring(0, action_rulename.length() - 3) + "%";

		logger.debug("action_rulename {}", action_rulename);

		logger.debug("RetrieveSurveyDone user:{} and action_rulename:{}", userId, action_rulename);

		List<Object[]> entities = new ArrayList<Object[]>(0);

		Session session = sessionFactoryEngager.openSession();
		Transaction tx = null;

		try {

			String sqlquery = "SELECT engagement_id, survey_response FROM engager.surveyresponse where user_id='" + userId + "' and survey_id LIKE :rulename";

			tx = session.beginTransaction();

			SQLQuery query = (SQLQuery) session.createSQLQuery(sqlquery).setParameter("rulename", action_rulename);

			entities = query.list();

			tx.commit();

			logger.debug("!!retrieve survey is {}", query);

		} catch (Exception ex) {
			logger.error("cannot retrieve due:", ex);
			if (tx != null)
				tx.rollback();

		} finally {
			try {
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
			}
		}

		logger.debug("There are n={} survey with rulename={}", entities.size(), action_rulename);

		// if no survey made with this rule name, return false
		if (entities.size() == 0) {
			logger.debug("return false");
			return false;
		}

		// special situation
		if (action_rulename.startsWith("confirm_ppoi_")) {
			logger.debug("special case, confirm ppoi");
			// if there is at least one yes, return true
			for (Object[] o : entities) {
				if (((String) o[1]).contains("\"yes\"")) {
					logger.debug("there is at least one yes, we return true");
					return true;
				}
			}
			logger.debug("none of the survey is confirmed, all of them are rejected");
			// check if the passed action.getmessage is different from any of the responseid
			for (Object[] o : entities) {

				if (o[0] == null) {
					logger.error("no engagement id related to this survey was found, skipping current");
				} else {

					// getresponse_id
					String r = getResponseActionMsg((((java.math.BigInteger) o[0]).longValue()));

					if ((r != null) && (action.getMsg().equals(r))) {
						logger.debug("we already requested the same survey, return true");
						return true;
					}
				}
			}
			logger.debug("no constrains found, return false");
			return false;
		} else if (action_rulename.startsWith("spent_time_")) {
			logger.debug("special case, spent_time");
			// check if the passed action.getmessage is different from any of the responseid
			for (Object[] o : entities) {
				if (o[0] == null) {
					logger.error("no engagement id related to this survey was found, skipping current");
				} else {
					// getresponse_id
					String r = getResponseActionMsg((((java.math.BigInteger) o[0]).longValue()));

					if ((r != null) && (action.getMsg().equals(r))) {
						logger.debug("we already requested the same survey, return true");
						return true;
					}
				}
			}
			logger.debug("no constrains found, return false");
			return false;
		} else if (action_rulename.startsWith("confirm_extrappoi_")) {
			logger.debug("special case, confirm extrappoi");
			// check if the passed action.getmessage is different from any of the responseid
			for (Object[] o : entities) {

				if (o[0] == null) {
					logger.error("no engagement id related to this survey was found, skipping current");
				} else {

					// getresponse_id
					String r = getResponseActionMsg((((java.math.BigInteger) o[0]).longValue()));

					if ((r != null) && (action.getMsg().equals(r))) {
						logger.debug("we already requested the same survey, return true");
						return true;
					}
				}
			}
			logger.debug("no constrains found, return false");
			return false;
		}

		logger.debug("no special case and size>0, return true");
		return true;
	}

	// simply return the responseActionMsg with the specified id
	@SuppressWarnings("unchecked")
	private String getResponseActionMsg(Long id) {

		String toreturn = null;

		Session session = sessionFactoryEngager.openSession();
		Transaction tx = null;

		List<String> responses = null; // needed to update the final status to SENT

		try {

			String sqlquery = "SELECT response.action_msg FROM engager.response where id='" + id + "';";
			tx = session.beginTransaction();
			SQLQuery query = session.createSQLQuery(sqlquery);
			responses = query.list();
			tx.commit();

			if (responses.size() == 0) {
				logger.debug("no entry for id:{}", id);
			} else {
				if (responses.size() > 1) {
					logger.warn("more than one response for id:{} . anyway, we return the first one:{}", id, responses.get(0));
				} else {
					logger.debug("response for id>{} is {}", id, responses.get(0));
				}
				toreturn = responses.get(0);
			}

		} catch (Exception ex) {
			logger.error("cannot getPrevious due:", ex);
			if (tx != null)
				tx.rollback();
		} finally {
			try {
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
			}
		}

		return toreturn;
	}

	@SuppressWarnings("unchecked")
	public Date getSurveyDate(Long id) {

		Date toreturn = null;

		Session session = sessionFactoryEngager.openSession();
		Transaction tx = null;

		try {

			String sqlquery = "SELECT surveyresponse.completed_time FROM engager.surveyresponse where engagement_id='" + id + "';";
			tx = session.beginTransaction();
			SQLQuery query = session.createSQLQuery(sqlquery);
			List<Date> returned = query.list();
			tx.commit();

			if ((returned != null) && (returned.size() != 0)) {
				logger.debug("date is {}", returned.get(0));
				toreturn = returned.get(0);
			}

		} catch (Exception ex) {
			logger.error("cannot retrieve due:", ex);
			if (tx != null)
				tx.rollback();

		} finally {
			try {
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
			}
		}

		return toreturn;
	}

}
