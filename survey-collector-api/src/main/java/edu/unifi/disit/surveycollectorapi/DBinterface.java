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
package edu.unifi.disit.surveycollectorapi;

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
import org.hibernate.criterion.Restrictions;

import edu.unifi.disit.commons.datamodel.Properties;

public class DBinterface {

	private static final Logger logger = LogManager.getLogger("DBinterface-surveycollector");
	private SessionFactory sessionFactoryLocal; // engager DB
	private static DBinterface instance = null;

	public static DBinterface getInstance() {
		if (instance == null) {
			synchronized (DBinterface.class) {
				if (instance == null) {
					instance = new DBinterface();
				}
			}
		}
		return instance;
	}

	private DBinterface() {
		this.sessionFactoryLocal = new Configuration().configure("hibernate-local-sv.cfg.xml").buildSessionFactory();
	}

	public void close() throws Throwable {
		if (instance != null) {
			sessionFactoryLocal.close();
			sessionFactoryLocal = null;
			instance = null;
			super.finalize();
		}
	}

	@SuppressWarnings("unchecked")
	public List<Properties> getProperties() {

		List<Properties> toreturn = new ArrayList<Properties>();

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			tx = session.beginTransaction();

			toreturn = session.createCriteria(Properties.class)
					.list();

			tx.commit();

		} catch (Exception ex) {
			logger.error("cannot getProperties due:", ex);
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

	public void add(SurveyResponse response) {

		if (response == null) {
			logger.debug("survey response is NULL, skipping");
			return;
		}

		logger.debug("ADD surveyResponse: {}", response);

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			tx = session.beginTransaction();

			session.save(response);

			tx.commit();

		} catch (Exception ex) {
			if (tx != null)
				tx.rollback();
			logger.error("cannot add due:", ex);
		} finally {
			try {
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
			}
		}

		logger.debug("ADD done");
	}

	@SuppressWarnings("unchecked")
	public List<SurveyResponse> get(String survey_id) {

		logger.debug("requested get for survey:{}", survey_id);

		List<SurveyResponse> responses = new ArrayList<SurveyResponse>();

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			tx = session.beginTransaction();

			responses = session.createCriteria(SurveyResponse.class)
					.add(Restrictions.like("survey_id", survey_id))
					.list();

			tx.commit();

		} catch (Exception ex) {
			if (tx != null)
				tx.rollback();
			logger.error("cannot get due:", ex);
		} finally {
			try {
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
			}
		}

		logger.debug("return size={}", responses.size());

		return responses;
	}

	// this rely on response table, so we cannot use hibernate since otherwise we need to add ciclic dependencies
	@SuppressWarnings("unchecked")
	public Date getEngagementDate(Long id) {

		logger.debug("searching engagement date for id {}", id);

		Date toreturn = null;

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			String sqlquery = "SELECT timeCreated FROM engager.response where id='" + id + "';";

			SQLQuery query = session.createSQLQuery(sqlquery);

			tx = session.beginTransaction();

			List<Object> entities = query.list();

			tx.commit();

			if ((entities != null) && (entities.size() > 0)) {
				logger.debug("date is {}", entities);
				toreturn = new Date(((Timestamp) entities.get(0)).getTime());
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

		logger.debug("engagement date is {}", toreturn);

		return toreturn;
	}

	// this rely on response table, so we cannot use hibernate since otherwise we need to add ciclic dependencies
	@SuppressWarnings("unchecked")
	public String getEngagementServiceUri(Long id) {

		logger.debug("searching engagement service uri for id {}", id);

		String toreturn = null;

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			String sqlquery = "SELECT action_uri FROM engager.response where id='" + id + "';";

			SQLQuery query = session.createSQLQuery(sqlquery);

			tx = session.beginTransaction();

			List<Object> entities = query.list();

			tx.commit();

			if ((entities != null) && (entities.size() > 0)) {
				logger.debug("serviceUri is {}", entities);
				toreturn = (String) entities.get(0);
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

		logger.debug("engagement service uri is {}", toreturn);

		return toreturn;
	}

	// -----------------used by mychecker
	@SuppressWarnings("unchecked")
	public Date getSurveyDate(Long id) {

		Date toreturn = null;

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			tx = session.beginTransaction();

			List<SurveyResponse> returned = session.createCriteria(SurveyResponse.class)
					.add(Restrictions.like("engagement_id", id))
					.list();

			tx.commit();

			if ((returned != null) && (returned.size() != 0)) {
				logger.debug("date is {}", returned.get(0).getCompleted_time());
				toreturn = returned.get(0).getCompleted_time();
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
