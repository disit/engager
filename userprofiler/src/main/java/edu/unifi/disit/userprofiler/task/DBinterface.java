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
package edu.unifi.disit.userprofiler.task;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class DBinterface {

	private static final Logger logger = LogManager.getLogger();

	private SessionFactory sessionFactoryWallet; // wallet DB

	private static DBinterface instance = null;

	private static final String CAMPAIGN_PILOTA = "PILOTA";

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
		this.sessionFactoryWallet = new Configuration().configure("hibernate-remote-wallet.cfg.xml").buildSessionFactory();

	}

	public void close() throws Throwable {
		if (instance != null) {

			sessionFactoryWallet.close();
			sessionFactoryWallet = null;

			instance = null;
			super.finalize();
		}
	}

	@SuppressWarnings("unchecked")
	public boolean getPointsFromWallet(String deviceId) {
		logger.debug("getPointsFromWallet from {} ", deviceId);

		Session session = sessionFactoryWallet.openSession();
		Transaction tx = null;

		try {

			String sqlquery = "select " +
					"sum(rule2_.value) as col_0_0_ " +
					"from " +
					"tracein tracein0_ " +
					"inner join " +
					"user user1_ " +
					"on tracein0_.user_id=user1_.id " +
					"inner join " +
					"rule rule2_ " +
					"on tracein0_.rule_id=rule2_.id " +
					"inner join " +
					"campaign campaign3_ " +
					"on tracein0_.campaign_id=campaign3_.id " +
					"inner join " +
					"device device4_ " +
					"on user1_.id=device4_.user_id " +
					"where " +
					"device4_.device_id='" + deviceId + "' " +
					"and campaign3_.name='" + CAMPAIGN_PILOTA + "';";

			SQLQuery query = session.createSQLQuery(sqlquery);

			tx = session.beginTransaction();

			List<java.math.BigDecimal> entities = query.list();

			tx.commit();

			for (java.math.BigDecimal o : entities) {
				if ((o != null)) {
					logger.debug("got {}", o);
					return true;
				} else
					logger.debug("is null");
			}

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

		return false;
	}

	@SuppressWarnings("unchecked")
	public boolean isRegistered(String deviceId) {
		logger.debug("isRegistered  {} ", deviceId);

		Session session = sessionFactoryWallet.openSession();
		Transaction tx = null;

		try {

			String sqlquery = "select " +
					"*" +
					"from " +
					"device " +
					"where " +
					"device_id='" + deviceId + "';";

			SQLQuery query = session.createSQLQuery(sqlquery);

			tx = session.beginTransaction();

			List<Object[]> entities = query.list();

			tx.commit();

			if (entities.size() > 0) {
				logger.debug("user is registered");
				return true;
			}

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

		logger.debug("user is NOT registered");
		return false;
	}
}
