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
import java.text.SimpleDateFormat;
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

import edu.unifi.disit.engagerapi.datamodel.Response;

public class DBinterfaceAccessLog {

	private static final Logger logger = LogManager.getLogger("DBinterface-accesslog");
	private SessionFactory sessionFactoryAccessLog;
	private static DBinterfaceAccessLog instance = null;

	public static DBinterfaceAccessLog getInstance() {
		if (instance == null) {
			synchronized (DBinterfaceAccessLog.class) {
				if (instance == null) {
					instance = new DBinterfaceAccessLog();
				}
			}
		}
		return instance;
	}

	private DBinterfaceAccessLog() {
		this.sessionFactoryAccessLog = new Configuration().configure("hibernate-accesslog.cfg.xml").buildSessionFactory();
	}

	public void close() throws Throwable {
		if (instance != null) {
			sessionFactoryAccessLog.close();
			sessionFactoryAccessLog = null;
			instance = null;
			super.finalize();
		}
	}

	// return Date if there is an entry related to this user and this serviceUri and mode= to:
	// api-service-stars
	// api-service-comment
	// api-service-photo
	// --->with time contrain
	@SuppressWarnings("unchecked")
	public Date retrieveAccessLogServiceUriClick(Response r) {

		String userId = r.getUserId();
		String serviceUri = r.getAction_uri();
		Date sentTime = r.getTimeSend();
		Date elapsingTime = r.getTimeElapsed();

		String sentTime_s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(sentTime);
		String elapsingTime_s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(elapsingTime);

		Date toreturn = null;
		logger.debug("RetrieveAccessLogServiceUriClick for user:{} and serviceUri:{} sendTime {}, elapsing {}", userId, serviceUri, sentTime, elapsingTime);
		List<Object> entities = new ArrayList<Object>(0);
		Session session = sessionFactoryAccessLog.openSession();
		Transaction tx = null;

		try {

			String sqlquery = "SELECT AccessLog.timestamp FROM ServiceMap.AccessLog where "
					+ "(serviceUri='" + serviceUri + "') "
					+ "and (uid='" + userId + "') "
					+ "and (mode='api-service-stars' or mode='api-service-comment' or mode='api-service-photo') "
					+ "and (timestamp between '" + sentTime_s + "' and '" + elapsingTime_s + "');";
			tx = session.beginTransaction();
			SQLQuery query = session.createSQLQuery(sqlquery);
			entities = query.list();
			tx.commit();
			if (entities != null) {
				logger.debug("Returned n={} clicks", entities.size());
				if (entities.size() > 0)
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
		return (toreturn);
	}

	// return Date if there is at least an entry related to this user and this serviceUri and mode= to:
	// api-service-stars
	// api-service-comment
	// api-service-photo
	// ----------> without time constrin
	@SuppressWarnings("unchecked")
	public Date retrieveAccessLogServiceUriClick(String userId, String serviceUri) {

		Date toreturn = null;
		logger.debug("RetrieveAccessLogServiceUriClick for user:{} and serviceUri:{}", userId, serviceUri);
		List<Object> entities = new ArrayList<Object>(0);
		Session session = sessionFactoryAccessLog.openSession();
		Transaction tx = null;

		try {

			String sqlquery = "SELECT AccessLog.timestamp FROM ServiceMap.AccessLog where serviceUri='" + serviceUri
					+ "' and uid='" + userId + "' and (mode='api-service-stars' or mode='api-service-comment' or mode='api-service-photo');";
			tx = session.beginTransaction();
			SQLQuery query = session.createSQLQuery(sqlquery);
			entities = query.list();
			tx.commit();
			if (entities != null) {
				logger.debug("Returned n={} clicks", entities.size());
				if (entities.size() > 0)
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
		return (toreturn);
	}
}
