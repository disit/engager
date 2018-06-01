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
package edu.unifi.disit.wallet_user_mngt.externaldb;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class DBinterfaceDrupal {

	private static final Logger logger = LogManager.getLogger();

	private SessionFactory sessionFactoryDrupal; // drupal DB

	private static DBinterfaceDrupal instance = null;

	public static DBinterfaceDrupal getInstance() {
		if (instance == null) {
			synchronized (DBinterfaceDrupal.class) {
				if (instance == null) {
					instance = new DBinterfaceDrupal();
				}
			}
		}
		return instance;
	}

	private DBinterfaceDrupal() {
		this.sessionFactoryDrupal = new Configuration().configure("hibernate-remote-drupal.cfg.xml").buildSessionFactory();
	}

	public void close() throws Throwable {
		if (instance != null) {

			sessionFactoryDrupal.close();
			sessionFactoryDrupal = null;

			instance = null;
			super.finalize();
		}
	}

	// ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------USER ACTIVITES
	@SuppressWarnings("unchecked")
	public Integer getUID(String loggedUserName) {
		Integer toreturn = null;

		logger.debug("getUID for {} ", loggedUserName);

		Session session = sessionFactoryDrupal.openSession();
		Transaction tx = null;

		try {

			String sqlquery = "SELECT uid "
					+ "FROM users "
					+ "WHERE (mail='" + loggedUserName + "');";

			SQLQuery query = session.createSQLQuery(sqlquery);

			tx = session.beginTransaction();

			List<Integer> entities = query.list();

			tx.commit();

			if ((entities != null) && (entities.size() > 0) && (entities.get(0) != null)) {
				if (entities.size() > 1)
					logger.warn("returned more than one uid. IGNORING, we get just the first result");
				toreturn = entities.get(0);
				logger.debug("getUID: {}", toreturn);
			} else
				logger.debug("UID not found");

		} catch (Exception ex) {
			logger.error("cannot retrieve due:", ex);
			if (tx != null)
				tx.rollback();
			;
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
	public String getUsername(Integer drupalUid) {
		String toreturn = null;

		logger.debug("getUsername for {} ", drupalUid);

		Session session = sessionFactoryDrupal.openSession();
		Transaction tx = null;

		try {

			String sqlquery = "SELECT mail "
					+ "FROM users "
					+ "WHERE (uid='" + drupalUid + "');";

			SQLQuery query = session.createSQLQuery(sqlquery);

			tx = session.beginTransaction();

			List<String> entities = query.list();

			tx.commit();

			if ((entities != null) && (entities.size() > 0) && (entities.get(0) != null)) {
				if (entities.size() > 1)
					logger.warn("returned more than one username. IGNORING, we get just the first result");
				toreturn = entities.get(0);
				logger.debug("getUsername: {}", toreturn);
			} else
				logger.debug("Username not found");

		} catch (Exception ex) {
			logger.error("cannot retrieve due:", ex);
			if (tx != null)
				tx.rollback();
			;
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
