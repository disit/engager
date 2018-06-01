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
package edu.unifi.disit.userprofiler.externaldb;

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

import edu.unifi.disit.commons.datamodel.Location;
import edu.unifi.disit.commons.datamodel.MobilitySpeed;
import edu.unifi.disit.commons.datamodel.MobilityUserLocation;
import edu.unifi.disit.commons.datamodel.Position;
import edu.unifi.disit.commons.datamodel.Submitted;
import edu.unifi.disit.commons.datamodel.engager.EngageExecuted;

public class DBinterface {

	private static final Logger logger = LogManager.getLogger();

	private SessionFactory sessionFactorySensors; // sensors DB
	private SessionFactory sessionFactoryRemotePHOE; // DB phoenix
	private SessionFactory sessionFactoryAccesslog; // accesslog DB
	private SessionFactory sessionFactoryEngager; // engager DB

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
		this.sessionFactorySensors = new Configuration().configure("hibernate-remote-sensors.cfg.xml").buildSessionFactory();
		// this.sessionFactoryRemotePHOE = new Configuration().configure("hibernate-remote-sensors-phoenix.cfg.xml").buildSessionFactory();
		this.sessionFactoryAccesslog = new Configuration().configure("hibernate-remote-accesslog.cfg.xml").buildSessionFactory();
		this.sessionFactoryEngager = new Configuration().configure("hibernate-remote-engager.cfg.xml").buildSessionFactory();
	}

	public void close() throws Throwable {
		if (instance != null) {

			sessionFactorySensors.close();
			sessionFactorySensors = null;

			sessionFactoryRemotePHOE.close();
			sessionFactoryRemotePHOE = null;

			sessionFactoryAccesslog.close();
			sessionFactoryAccesslog = null;

			sessionFactoryEngager.close();
			sessionFactoryEngager = null;

			instance = null;
			super.finalize();
		}
	}

	@SuppressWarnings("unchecked")
	public Object[] getTerminalInfo(String device_id) {

		logger.debug("Retrieving Terminal info of device:{}", device_id);

		Session session = sessionFactorySensors.openSession();
		// Session session = sessionFactoryRemotePHOE.openSession();
		Transaction tx = null;

		Object[] toreturn = null;

		try {

			String sqlquery = "SELECT appID, device_model, version "
					+ "FROM sensors "
					+ "WHERE (device_id='" + device_id + "') "
					+ "ORDER BY idmeasure DESC LIMIT 1";

			SQLQuery query = session.createSQLQuery(sqlquery);

			tx = session.beginTransaction();

			List<Object[]> entities = query.list();

			tx.commit();

			if ((entities != null) && (entities.size() > 0) && (entities.get(0) != null))
				toreturn = entities.get(0);
			else
				logger.warn("Terminal INFO not found");

		} catch (Exception ex) {
			logger.error("cannot retrieve Terminal INFO due:", ex);
			if (tx != null)
				tx.rollback();
		} finally {
			try {
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
			}
		}

		if (toreturn != null)
			for (int i = 0; i < toreturn.length; i++)
				logger.debug("Retrieved {}", toreturn[i]);

		return toreturn;
	}

	// ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------USER ACTIVITES

	public List<EngageExecuted> getExecutedEngagements(String device_id) {
		return getExecutedEngagements(device_id, null, null);
	}

	@SuppressWarnings("unchecked")
	public List<EngageExecuted> getExecutedEngagements(String device_id, Integer from, Integer howmany) {

		logger.debug("Retrieving engagement executed of device:{}", device_id);

		List<EngageExecuted> toreturn = null;

		Session session = sessionFactoryEngager.openSession();
		Transaction tx = null;

		try {

			String sqlquery = "SELECT response_id, time, user_id, rule_name, time_insert "
					+ "FROM engager.engage_executed "
					+ "WHERE user_id='" + device_id + "';";

			SQLQuery query = session.createSQLQuery(sqlquery);

			tx = session.beginTransaction();

			// toreturn = session.createCriteria(EngageExecuted.class)
			// .add(Restrictions.like("userId", device_id))
			// .list();

			List<Object[]> entities = query.list();

			tx.commit();

			if (from == null)
				from = 1;
			if (howmany == null)
				howmany = entities.size();

			if ((entities != null) && (entities.size() > 0)) {
				toreturn = new ArrayList<EngageExecuted>();
				for (int i = 0; i < entities.size(); i++) {
					// for (Object[] o : entities) {
					if ((i >= (from - 1)) && (i < (from + howmany - 1))) {
						Object[] o = entities.get(i);
						toreturn.add(new EngageExecuted(o));
					}
				}
			}

		} catch (Exception ex) {
			logger.error("cannot retrieve engagement executed due:", ex);
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

		if (toreturn != null)
			for (EngageExecuted s : toreturn)
				logger.debug("Retrieved {}", s);

		return toreturn;
	}

	public List<Submitted> getSubmittedStars(String device_id) {
		return getSubmittedStars(device_id, null, null);
	}

	@SuppressWarnings("unchecked")
	public List<Submitted> getSubmittedStars(String device_id, Integer from, Integer howmany) {

		logger.debug("Retrieving submitted stars of device:{}", device_id);

		List<Submitted> toreturn = new ArrayList<Submitted>();

		Session session = sessionFactoryAccesslog.openSession();
		Transaction tx = null;

		try {

			String sqlquery = "SELECT timestamp, serviceUri, text "
					+ "FROM ServiceMap.AccessLog "
					+ "WHERE uid='" + device_id + "' "
					+ "AND (mode='api-service-stars');";

			SQLQuery query = session.createSQLQuery(sqlquery);

			tx = session.beginTransaction();

			List<Object[]> entities = query.list();

			tx.commit();

			if (from == null)
				from = 1;
			if (howmany == null)
				howmany = entities.size();

			if ((entities != null) && (entities.size() > 0)) {
				toreturn = new ArrayList<Submitted>();
				for (int i = 0; i < entities.size(); i++) {
					// for (Object[] o : entities) {
					if ((i >= (from - 1)) && (i < (from + howmany - 1))) {
						Object[] o = entities.get(i);
						toreturn.add(new Submitted(o));
					}
				}
			}

		} catch (Exception ex) {
			logger.error("cannot retrieve submitted stars due:", ex);
			if (tx != null)
				tx.rollback();
		} finally {
			try {
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
			}
		}

		if (toreturn != null)
			for (Submitted s : toreturn)
				logger.debug("Retrieved {}", s);

		return toreturn;
	}

	public List<Submitted> getSubmittedComments(String device_id) {
		return getSubmittedComments(device_id, null, null);
	}

	@SuppressWarnings("unchecked")
	public List<Submitted> getSubmittedComments(String device_id, Integer from, Integer howmany) {
		logger.debug("Retrieving submitted comments of device:{}", device_id);

		List<Submitted> toreturn = new ArrayList<Submitted>();

		// we separate the procedure in two steps, to enable scenario where the first part is executed on phoenix and the second part is executed on mysql

		List<Object[]> entities_accesslog = new ArrayList<Object[]>();

		Session session = sessionFactoryAccesslog.openSession();
		Transaction tx = null;

		try {

			String sqlquery = "SELECT timestamp, serviceUri "
					+ "FROM ServiceMap.AccessLog "
					+ "WHERE uid='" + device_id + "' "
					+ "AND mode='api-service-comment';";

			SQLQuery query = session.createSQLQuery(sqlquery);

			tx = session.beginTransaction();

			entities_accesslog = query.list();

			tx.commit();

		} catch (Exception ex) {
			logger.error("cannot retrieve1 submitted stars due:", ex);
			if (tx != null)
				tx.rollback();
		} finally {
			try {
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
			}
		}

		if (from == null)
			from = 1;
		if (howmany == null)
			howmany = Integer.MAX_VALUE;

		for (Object[] entity_accesslog : entities_accesslog) {

			List<String> entities_servicecomment = new ArrayList<String>();

			session = sessionFactoryAccesslog.openSession();
			tx = null;

			try {

				String sqlquery = "SELECT comment "
						+ "FROM ServiceMap.ServiceComment "
						+ "WHERE '" + device_id + "'=uid "
						+ "AND '" + ((String) entity_accesslog[1]) + "'=serviceUri "
						+ "AND status='validated';";

				SQLQuery query = session.createSQLQuery(sqlquery);

				tx = session.beginTransaction();

				entities_servicecomment = query.list();

				tx.commit();

				if ((entities_servicecomment != null) && (entities_servicecomment.size() > 0)) {
					toreturn = new ArrayList<Submitted>();
					for (int i = 0; i < entities_servicecomment.size(); i++) {
						// for (Object[] o : entities) {
						if ((i >= (from - 1)) && (i < (from + howmany - 1))) {
							String o = entities_servicecomment.get(i);
							toreturn.add(new Submitted(entity_accesslog, o));
						}
					}
				}

			} catch (Exception ex) {
				logger.error("cannot retrieve2submitted stars due:", ex);
				if (tx != null)
					tx.rollback();
			} finally {
				try {
					session.close();
				} catch (Exception e) {
					logger.error("cannot flush due:", e);
				}
			}
		}

		if (toreturn != null)
			for (Submitted s : toreturn)
				logger.debug("Retrieved {}", s);

		return toreturn;
	}

	public List<Submitted> getSubmittedPhotos(String device_id) {
		return getSubmittedPhotos(device_id, null, null);
	}

	@SuppressWarnings("unchecked")
	public List<Submitted> getSubmittedPhotos(String device_id, Integer from, Integer howmany) {

		logger.debug("Retrieving submitted photo of device: {}", device_id);

		List<Submitted> toreturn = new ArrayList<Submitted>();

		// we separate the procedure in two steps, to enable scenario where the first part is executed on phoenix and the second part is executed on mysql

		List<Object[]> entities_accesslog = new ArrayList<Object[]>();

		Session session = sessionFactoryAccesslog.openSession();
		Transaction tx = null;

		try {

			String sqlquery = "SELECT AccessLog.timestamp, AccessLog.serviceUri " +
					"FROM ServiceMap.AccessLog " +
					"WHERE AccessLog.uid='" + device_id + "' and AccessLog.mode='api-service-photo';";

			SQLQuery query = session.createSQLQuery(sqlquery);

			tx = session.beginTransaction();

			entities_accesslog = query.list();

			tx.commit();

		} catch (Exception ex) {
			logger.error("cannot retrieve1 submitted photo due:", ex);
			if (tx != null)
				tx.rollback();
		} finally {
			try {
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
			}
		}

		if (from == null)
			from = 1;
		if (howmany == null)
			howmany = Integer.MAX_VALUE;

		for (Object[] entity_accesslog : entities_accesslog) {

			List<String> entities_servicecomment = new ArrayList<String>();

			session = sessionFactoryAccesslog.openSession();
			tx = null;

			try {

				String sqlquery = "SELECT file "
						+ "FROM ServiceMap.ServicePhoto "
						+ "WHERE '" + device_id + "'=uid "
						+ "AND '" + ((String) entity_accesslog[1]) + "'=serviceUri "
						+ "AND status='validated';";

				SQLQuery query = session.createSQLQuery(sqlquery);

				tx = session.beginTransaction();

				entities_servicecomment = query.list();

				tx.commit();

			} catch (Exception ex) {
				logger.error("cannot retrieve2 submitted photo due:", ex);
				if (tx != null)
					tx.rollback();
			} finally {
				try {
					session.close();
				} catch (Exception e) {
					logger.error("cannot flush due:", e);
				}
			}

			if ((entities_servicecomment != null) && (entities_servicecomment.size() > 0)) {
				toreturn = new ArrayList<Submitted>();
				for (int i = 0; i < entities_servicecomment.size(); i++) {
					// for (Object[] o : entities) {
					if ((i >= (from - 1)) && (i < (from + howmany - 1))) {
						String o = entities_servicecomment.get(i);
						toreturn.add(new Submitted(entity_accesslog, o));
					}
				}
			}

		}

		if (toreturn != null)
			for (Submitted s : toreturn)
				logger.debug("Retrieved {}", s);

		return toreturn;
	}

	// ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- PPOIS
	// retrieve from the remote DB the list of
	// 1-last_weeks
	// 2-accuracy
	// 3-from/to
	// 4-weekend constrains
	// 5-soglia
	// 6-refuse category, accept category
	// +is not moving
	@SuppressWarnings("unchecked")
	public List<String> retrieveUsers(Date now, int hoursbefore, int accuracy, String from, String to, boolean justweekdays, List<String> refuseList, List<String> acceptList, String status) {
		List<String> toreturn = new ArrayList<String>();

		Session session = sessionFactorySensors.openSession();
		// Session session = sessionFactoryRemotePHOE.openSession();
		Transaction tx = null;

		try {

			// 604800 seconds in a week
			Date before_d = new Date(now.getTime() - (3600000 * hoursbefore));
			String before_s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(before_d);
			String now_s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now);

			logger.debug("Retrieving users from:{} to:{}", before_s, now_s);
			logger.debug("Retrieving users refusing:{}", refuseList);
			logger.debug("Retrieving users accepting:{}", acceptList);

			String sqlquery = "SELECT DISTINCT device_id FROM sensors.user_eval where " +
					"(user_eval.date between '" + before_s + "' and '" + now_s + "') and " +
					"(user_eval.accuracy < " + accuracy + ") and " +
					"(user_eval.version>='3.0.0') and " +
					"((CONVERT(user_eval.date,TIME) between '" + from + "' and '" + to + "')) and " +
					weekday(justweekdays) +
					createList(refuseList, "<>", "and") +
					createList(acceptList, "=", " or") +
					"(user_eval.curr_status_new='" + status + "')and"
					+ "(user_eval.curr_status_time_new IS NOT NULL)";

			// String sqlquery = "SELECT DISTINCT device_id "
			// + "FROM user_eval "
			// + "WHERE (date between TO_TIME('" + before_s + "') and TO_TIME('" + now_s + "')) and " // sulle ultime 24 ore
			// + "(accuracy < " + accuracy + ") and "
			// + "(TO_CHAR(date,'HH:mm:ss') between '" + from + "' and '" + to + "') and "
			// + weekdayPHOE(justweekdays)
			// + "(version>='3.0.0') and "
			// + createList(refuseList, "<>", "and")
			// + createList(acceptList, "=", " or")
			// + "(curr_status_new='" + status + "') and "
			// + "(curr_status_time_new IS NOT NULL)";

			logger.debug(sqlquery);

			SQLQuery query = session.createSQLQuery(sqlquery);

			tx = session.beginTransaction();

			toreturn = query.list();

			tx.commit();

			logger.debug("Retrieved n={} users", toreturn.size());

		} catch (Exception ex) {
			logger.error("cannot retrieveUsers due:", ex);
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

	// retrieve from the remote DB the list of the location that:
	// 1-userid
	// 2-last_weeks
	// 3-accuracy
	// 4-from/to
	// 5-weekend constrains
	// +is not moving
	@SuppressWarnings("unchecked")
	public List<Location> retrieveLocations(Date now, String userId, int weeksbefore, int accuracy, String from, String to, boolean justweekdays, boolean juststay, boolean usecentroid, String status) {
		List<Location> toreturn = new ArrayList<Location>();

		Session session = sessionFactorySensors.openSession();
		// Session session = sessionFactoryRemotePHOE.openSession();
		Transaction tx = null;

		try {

			// 604800 seconds in a week
			Date before_d = new Date(now.getTime() - (604800000L * weeksbefore));
			String before_s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(before_d);
			String now_s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now);

			logger.debug("Retrieving locations of user:{}", userId);
			logger.debug("Retrieving locations from:{} to:{}", before_s, now_s);
			logger.debug("Retrieving locations with time constrains from:{} to:{}", from, to);
			logger.debug("Retrieving locations for weekday:{} ", justweekdays);

			String gpsString = null;

			if (usecentroid)
				gpsString = "lat_centroid, lon_centroid";
			else
				gpsString = "latitude, longitude";

			String sqlquery;

			if (juststay) {
				sqlquery = "SELECT user_eval.date, user_eval.curr_status_new, user_eval.curr_status_time_new, " + gpsString + " FROM sensors.user_eval WHERE " +
						"(user_eval.device_id='" + userId + "') and " +
						"(user_eval.date between '" + before_s + "' and '" + now_s + "') and " +
						"(user_eval.accuracy < " + accuracy + ") and " +
						"(user_eval.version>='3.0.0') and " +
						weekday(justweekdays) +
						"((CONVERT(user_eval.date,TIME) between '" + from + "' and '" + to + "')) and " +
						"(user_eval.curr_status_new='" + status + "')and"
						+ "(user_eval.curr_status_time_new IS NOT NULL)";

				// sqlquery = "SELECT date, curr_status_new, curr_status_time_new, " + gpsString + " "
				// + "FROM user_eval "
				// + "WHERE (device_id='" + userId + "') and "
				// + "(date between TO_TIME('" + before_s + "') and TO_TIME('" + now_s + "')) and " // sulle ultime 8 settimane
				// + "(accuracy < " + accuracy + ") and "
				// + "(TO_CHAR(date,'HH:mm:ss') between '" + from + "' and '" + to + "') and "
				// + weekdayPHOE(justweekdays)
				// + "(version>='3.0.0') and "
				// + "(curr_status_new='" + status + "') and"
				// + "(curr_status_time_new IS NOT NULL)";

			} else {
				sqlquery = "SELECT user_eval.date, user_eval.curr_status_new, user_eval.curr_status_time_new, " + gpsString + " FROM sensors.user_eval WHERE " +
						"(user_eval.device_id='" + userId + "') and " +
						"(user_eval.date between '" + before_s + "' and '" + now_s + "') and " +
						"(user_eval.accuracy < " + accuracy + ") and " +
						"(user_eval.version>='3.0.0') and " +
						weekday(justweekdays) +
						"((CONVERT(user_eval.date,TIME) between '" + from + "' and '" + to + "'));";

				// sqlquery = "SELECT date, curr_status_new, curr_status_time_new, " + gpsString + " "
				// + "FROM user_eval "
				// + "WHERE (device_id='" + userId + "') and "
				// + "(date between TO_TIME('" + before_s + "') and TO_TIME('" + now_s + "')) and " // sulle ultime 8 settimane
				// + "(accuracy < " + accuracy + ") and "
				// + "(TO_CHAR(date,'HH:mm:ss') between '" + from + "' and '" + to + "') and "
				// + weekdayPHOE(justweekdays)
				// + "(version>='3.0.0')";

			}

			logger.debug(sqlquery);

			SQLQuery query = session.createSQLQuery(sqlquery);

			tx = session.beginTransaction();

			List<Object[]> entities = query.list();

			tx.commit();

			for (Object[] entity : entities) {
				toreturn.add(new Location(entity));
			}

			logger.debug("Retrieved n={} locations", entities.size());

		} catch (Exception ex) {
			logger.error("cannot retrieveLocations due:", ex);
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

	// subset of above, rewrote to use the same index
	// needed to retreive the position at exacly time
	@SuppressWarnings("unchecked")
	public List<Location> retrieveLocations(String userId, Date start_d, Date end_d) {
		List<Location> toreturn = new ArrayList<Location>();

		Session session = sessionFactorySensors.openSession();
		// Session session = sessionFactoryRemotePHOE.openSession();

		Transaction tx = null;

		String start_s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(start_d);
		String end_s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(end_d);

		try {

			logger.debug("Retrieving locations of user:{}", userId);
			logger.debug("Retrieving locations between:{} {}", start_s, end_s);

			String sqlquery = "SELECT user_eval.date, user_eval.curr_status_new, user_eval.curr_status_time_new, latitude, longitude FROM sensors.user_eval WHERE " +
					"(user_eval.device_id='" + userId + "') and " +
					"(user_eval.date between '" + start_s + "' and '" + end_s + "');";
			// String sqlquery = "SELECT date, curr_status_new, curr_status_time_new, latitude, longitude "
			// + "FROM user_eval "
			// + "WHERE (device_id='" + userId + "') and "
			// + "(date between TO_TIME('" + start_s + "') and TO_TIME('" + end_s + "'))";

			SQLQuery query = session.createSQLQuery(sqlquery);

			tx = session.beginTransaction();

			List<Object[]> entities = query.list();

			tx.commit();

			for (Object[] entity : entities) {
				toreturn.add(new Location(entity));
			}

			logger.debug("Retrieved n={} locations", entities.size());

		} catch (Exception ex) {
			logger.error("cannot retrieveLocations due:", ex);
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

	private String createList(List<String> classlist, String operator, String andor) {
		String toreturn = "";

		if ((classlist != null) && (classlist.size() > 0)) {
			toreturn = toreturn.concat("(");
			for (String classe : classlist) {
				toreturn = toreturn.concat("(profile" + operator + "'" + classe + "')" + andor);
			}
			toreturn = toreturn.substring(0, toreturn.length() - andor.length());
			toreturn = toreturn.concat(")");
			toreturn = toreturn.concat("and");
		}

		return toreturn;
	}

	private String weekday(boolean justweekdays) {
		String toreturn = "";
		if (justweekdays) {
			// has to be <> from saturday and <> from sunday
			toreturn = toreturn.concat("(WEEKDAY (user_eval.date)<>5)and(WEEKDAY (user_eval.date)<>6)and");
		}
		return toreturn;
	}

	// private String weekdayPHOE(boolean justweekdays) {
	// String toreturn = "";
	// if (justweekdays) {
	// // has to be <> from saturday and <> from sunday
	// toreturn = toreturn.concat("(DAYOFWEEK(date)!=6) and (DAYOFWEEK(date)!=7) and ");
	// }
	// return toreturn;
	// }

	// --------------
	public Position retrievePosition(String userId, Date at) {

		List<Location> loc = retrieveLocations(userId, at, at);
		if ((loc != null) && (loc.size() > 0)) {
			return loc.get(0);
		} else
			return null;

	}

	// ------------------------------------------------aggregated mobility

	public int getMaxId() {
		return getMaxIdFromLast(null);
	}

	// get maxid
	// from lastid (if null, constrain is not included in the query)
	// where curr_status_time_new !=null
	@SuppressWarnings("unchecked")
	public int getMaxIdFromLast(Integer lastId) {

		logger.trace("requested MaxID from {}", lastId);

		int toreturn = 0;

		Session session = sessionFactorySensors.openSession();
		// Session session = sessionFactoryRemotePHOE.openSession();
		Transaction tx = null;

		try {

			String sqlquery = "SELECT max(user_eval.user_eval_id) FROM sensors.user_eval WHERE ";
			if (lastId != null)
				sqlquery = sqlquery + " user_eval.user_eval_id>" + lastId + " and ";
			sqlquery = sqlquery + " user_eval.curr_status_new IS NOT NULL;";

			tx = session.beginTransaction();

			SQLQuery query = session.createSQLQuery(sqlquery);

			// List<java.math.BigInteger> returned = query.list();
			List<java.lang.Integer> returned = query.list();

			tx.commit();

			if (returned.size() > 0) {
				toreturn = returned.get(0).intValue();
				logger.trace("MaxID is:{}", toreturn);
			} else {
				logger.error("MaxID not found");
			}

		} catch (Exception ex) {
			logger.error("cannot getMaxId due:", ex);
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
	public List<String> retrieveUsersForStatusAggregation(Integer from, Integer to) {

		logger.debug("retrieve from {} to {}", from, to);

		List<String> toreturn = new ArrayList<String>();

		// Session session = sessionFactoryRemotePHOE.openSession();
		Session session = sessionFactorySensors.openSession();
		Transaction tx = null;

		try {

			String sqlquery = "SELECT DISTINCT user_eval.device_id FROM sensors.user_eval where " +
					"(user_eval.user_eval_id between " + from + " and " + to + ") and " +
					"(user_eval.curr_status_time_new IS NOT NULL)";

			SQLQuery query = session.createSQLQuery(sqlquery);

			tx = session.beginTransaction();

			toreturn = query.list();

			tx.commit();

			logger.debug("Retrieved n={} users", toreturn.size());

		} catch (Exception ex) {
			logger.error("cannot retrieveUsers due:", ex);
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
	public List<Location> retrieveLocationForStatusAggregation(String userId, Integer from, Integer to) {
		List<Location> toreturn = new ArrayList<Location>();

		Session session = sessionFactorySensors.openSession();
		// Session session = sessionFactoryRemotePHOE.openSession();
		Transaction tx = null;

		logger.debug("searching entry for user {}", userId);

		try {

			String sqlquery = "SELECT user_eval.date, user_eval.curr_status_new, user_eval.curr_status_time_new, user_eval.latitude, user_eval.longitude FROM sensors.user_eval WHERE " +
					"(user_eval.device_id='" + userId + "') and " +
					"(user_eval.user_eval_id between " + from + " and " + to + ") and " +
					"(user_eval.curr_status_time_new IS NOT NULL)";

			SQLQuery query = session.createSQLQuery(sqlquery);

			tx = session.beginTransaction();

			List<Object[]> entities = query.list();

			tx.commit();

			for (Object[] entity : entities) {
				// toreturn.add(new Location(entity, true));
				toreturn.add(new Location(entity));
			}

			logger.debug("Retrieved n={} locations", entities.size());

		} catch (Exception ex) {
			logger.error("cannot retrieveLocations due:", ex);
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

	// -----------------------------------------for mobility R learning

	public List<MobilityUserLocation> retrieveMobilityLocations(String userId, Long start, Long end) {
		return retrieveMobilityLocations(userId, new Date(start), new Date(end));
	}

	public List<MobilityUserLocation> retrieveMobilityLocations(String userId, Date start_d, Date end_d) {
		String start_s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(start_d);
		String end_s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(end_d);
		return retrieveMobilityLocations(userId, start_s, end_s);
	}

	@SuppressWarnings("unchecked")
	public List<MobilityUserLocation> retrieveMobilityLocations(String userId, String before_s, String end_s) {
		List<MobilityUserLocation> toreturn = new ArrayList<MobilityUserLocation>();

		Session session = sessionFactorySensors.openSession();
		Transaction tx = null;

		try {

			logger.debug("Retrieving locations of user:{}", userId);
			logger.debug("Retrieving locations from:{} to:{}", before_s, end_s);

			String sqlquery = "SELECT user_eval.user_eval_id, user_eval.date, user_eval.device_id, user_eval.latitude, user_eval.longitude, user_eval.curr_status, user_eval.lang, user_eval.accuracy, user_eval.speed, user_eval.profile, user_eval.provider, user_eval.avg_speed, user_eval.avg_lin_acc_magn, user_eval.lin_acc_x,user_eval.lin_acc_y, user_eval.lin_acc_z FROM sensors.user_eval WHERE "
					+ "(user_eval.device_id='" + userId + "') and " +
					"(user_eval.latitude!='0') and " +
					"(user_eval.date between '" + before_s + "' and '" + end_s + "') order by date asc;";

			SQLQuery query = session.createSQLQuery(sqlquery);

			tx = session.beginTransaction();

			List<Object[]> entities = query.list();

			tx.commit();

			for (Object[] entity : entities) {
				toreturn.add(new MobilityUserLocation(entity));
			}

			logger.debug("Retrieved n={} locations", entities.size());

		} catch (Exception ex) {
			logger.error("cannot retrieveLocations due:", ex);
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
	public List<MobilitySpeed> retrieveMobilityLocationsMS(String userId, Long from, Long to) {
		List<MobilitySpeed> toreturn = new ArrayList<MobilitySpeed>();

		Session session = sessionFactorySensors.openSession();
		Transaction tx = null;

		try {

			String froms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(from);
			String tos = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(to);

			logger.debug("Retrieving locations of user:{}", userId);
			logger.debug("Retrieving locations from:{} to:{}", from, to);

			String sqlquery = "SELECT user_eval.user_eval_id, user_eval.device_id, user_eval.date, user_eval.speed , user_eval.latitude, user_eval.longitude FROM sensors.user_eval WHERE "
					+ "(user_eval.device_id='" + userId + "') and " +
					"(user_eval.latitude!='0') and " +
					"(user_eval.speed!='0.0001') and " +
					"(user_eval.speed!='0.0002') and " +
					"(user_eval.date between '" + froms + "' and '" + tos + "') order by date asc;";

			SQLQuery query = session.createSQLQuery(sqlquery);

			tx = session.beginTransaction();

			List<Object[]> entities = query.list();

			tx.commit();

			for (Object[] entity : entities) {
				toreturn.add(new MobilitySpeed(entity));
			}

			logger.debug("Retrieved n={} locations", entities.size());

		} catch (Exception ex) {
			logger.error("cannot retrieveLocations due:", ex);
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
