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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import edu.unifi.disit.wallet_user_mngt.object.IOTApp;

public class DBinterfaceIOTApps {

	private static final Logger logger = LogManager.getLogger();

	private SessionFactory sessionFactoryIOTApps; // drupal DB

	private static DBinterfaceIOTApps instance = null;

	public static DBinterfaceIOTApps getInstance() {
		if (instance == null) {
			synchronized (DBinterfaceIOTApps.class) {
				if (instance == null) {
					instance = new DBinterfaceIOTApps();
				}
			}
		}
		return instance;
	}

	private DBinterfaceIOTApps() {
		this.sessionFactoryIOTApps = new Configuration().configure("hibernate-remote-iotapps.cfg.xml").buildSessionFactory();
	}

	public void close() throws Throwable {
		if (instance != null) {

			sessionFactoryIOTApps.close();
			sessionFactoryIOTApps = null;

			instance = null;
			super.finalize();
		}
	}

	// ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------USER ACTIVITES
	@SuppressWarnings("unchecked")
	public List<IOTApp> getIOTApps(Integer drupalUid) {
		List<IOTApp> toreturn = null;

		logger.debug("getIOTAppsId for {} ", drupalUid);

		Session session = sessionFactoryIOTApps.openSession();
		Transaction tx = null;

		try {

			String sqlquery = "SELECT id, name, created, container_id "
					+ "FROM application "
					+ "WHERE (uid='" + drupalUid + "');";

			SQLQuery query = session.createSQLQuery(sqlquery);

			tx = session.beginTransaction();

			List<Object[]> entities = query.list();

			tx.commit();

			if ((entities != null) && (entities.size() > 0)) {
				toreturn = new ArrayList<IOTApp>();
				for (Object[] entity : entities) {
					toreturn.add(new IOTApp(entity));
				}
				logger.debug("getIOTAppsId size : {}", toreturn.size());
			} else
				logger.debug("getIOTAppsId not found");

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
	public Integer getDrupalUid(String containerId) {
		Integer toreturn = null;

		logger.debug("getDrupalUid for {} ", containerId);

		Session session = sessionFactoryIOTApps.openSession();
		Transaction tx = null;

		try {

			String sqlquery = "SELECT uid "
					+ "FROM application "
					+ "WHERE (container_id='" + containerId + "');";

			SQLQuery query = session.createSQLQuery(sqlquery);

			tx = session.beginTransaction();

			List<Integer> entities = query.list();

			tx.commit();

			if ((entities != null) && (entities.size() > 0) && (entities.get(0) != null)) {
				if (entities.size() > 1)
					logger.warn("returned more than one uid. IGNORING, we get just the first result");
				toreturn = entities.get(0);
				logger.debug("getDrupalUid: {}", toreturn);
			} else
				logger.debug("DrupalUid not found");

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
