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
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;

import edu.unifi.disit.commons.datamodel.engager.EngageExecuted;

public class DBinterfaceEngager {

	private static final Logger logger = LogManager.getLogger();

	private SessionFactory sessionFactoryEngager; // engager DB

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
		this.sessionFactoryEngager = new Configuration().configure("hibernate-remote-engager.cfg.xml").buildSessionFactory();
	}

	public void close() throws Throwable {
		if (instance != null) {

			sessionFactoryEngager.close();
			sessionFactoryEngager = null;

			instance = null;
			super.finalize();
		}
	}

	// ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------USER ACTIVITES
	@SuppressWarnings("unchecked")
	public List<EngageExecuted> getEngagementExecuted(Long lastExecutedId) {
		List<EngageExecuted> toreturn = null;

		logger.debug("getEngageExecuted from {} ", lastExecutedId);

		Session session = sessionFactoryEngager.openSession();
		Transaction tx = null;

		try {

			tx = session.beginTransaction();

			toreturn = session.createCriteria(EngageExecuted.class)
					.add(Restrictions.gt("id", lastExecutedId))
					.list();

			tx.commit();

			logger.debug("getEngageExecuted:  size{}", toreturn.size());

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
