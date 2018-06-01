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
package edu.unifi.disit.engagerapi;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import disit.engager_base.ACTION;
import disit.engager_base.ACTIONS;
import edu.unifi.disit.commons.datamodel.MobilityUserLocation;
import edu.unifi.disit.commons.datamodel.Properties;
import edu.unifi.disit.commons.datamodel.engager.EngageExecuted;
import edu.unifi.disit.commons.utils.Constants;
import edu.unifi.disit.engagerapi.datamodel.EngageCancelled;
import edu.unifi.disit.engagerapi.datamodel.LastAccesslog;
import edu.unifi.disit.engagerapi.datamodel.LastEngaged;
import edu.unifi.disit.engagerapi.datamodel.LastExecuted;
import edu.unifi.disit.engagerapi.datamodel.Response;
import edu.unifi.disit.engagerapi.datamodel.Result;
import edu.unifi.disit.engagerapi.datamodel.TimeLastSent;
import edu.unifi.disit.engagerapi.datamodel.UserClick;
import edu.unifi.disit.engagerapi.executed.RuleChecker;

public class DBinterface {

	public static final Integer THRESHOLD_ENGAGER_ONLINE = 100;

	private static final Logger logger = LogManager.getLogger("DBinterface-engagerapi");
	private SessionFactory sessionFactoryRemoteSensors; // sensor DB
	private SessionFactory sessionFactoryLocal; // engager DB
	private SessionFactory sessionFactoryRemoteAccessLog; // accesslogdb
	// private SessionFactory sessionFactoryRemotePHOE; // DB phoenix
	private static DBinterface instance = null;
	private static final RuleChecker rc = RuleChecker.getInstance();

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
		this.sessionFactoryLocal = new Configuration().configure("hibernate-local.cfg.xml").buildSessionFactory();
		this.sessionFactoryRemoteAccessLog = new Configuration().configure("hibernate-remote-accesslog.cfg.xml").buildSessionFactory();
		this.sessionFactoryRemoteSensors = new Configuration().configure("hibernate-remote-sensors.cfg.xml").buildSessionFactory();
		// this.sessionFactoryRemotePHOE = new Configuration().configure("hibernate-remote-sensors-phoenix.cfg.xml").buildSessionFactory();
	}

	public void close() throws Throwable {
		if (instance != null) {
			sessionFactoryLocal.close();
			sessionFactoryLocal = null;
			sessionFactoryRemoteAccessLog.close();
			sessionFactoryRemoteAccessLog = null;
			sessionFactoryRemoteSensors.close();
			sessionFactoryRemoteSensors = null;
			// sessionFactoryRemotePHOE.close();
			// sessionFactoryRemotePHOE = null;
			instance = null;
			super.finalize();
		}
	}

	// ----------------------------------------------------------------------------------------------------------------------------------------------------ENGAGER pooling
	// date=now (for timeCreated) is the original terminal data
	// date=elapsed (for timeElapsed) is the local data
	public void add(String userId, ACTIONS actions, String status, Date now, Date default_elapsed, Long thresholdLiveMillisec, Boolean isAssessor) {

		Date current_elapsed = null;

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {
			for (ACTION action : actions.getActions()) {
				// calcolate elapsing time: if was described in ACTION, use it; otherwise use the default value
				if ((action.getTime_elapse() != null) && (action.getTime_elapse() != 0l)) {
					current_elapsed = new Date(System.currentTimeMillis() + (action.getTime_elapse().longValue() * 60 * 1000));
					logger.debug("action elapsing time is specified. now is {}", current_elapsed);
				} else {
					current_elapsed = default_elapsed;
					logger.debug("action elapsing time is NOT specified, using default .is {}", current_elapsed);
				}

				// if we're NOT in scenario live (send_rate>thresholdLive), check if there are a SENT engagement for this user that elapse later than now-thresholdLive
				if (action.getAction_sendrate() * 60 * 1000 > thresholdLiveMillisec) {
					logger.debug("we're NOT live scenario");
					if (getPrevious(userId, action.getAction_rulename(), new Date(System.currentTimeMillis() + thresholdLiveMillisec))) {
						logger.debug("there are an SENT/VIEWED engagement still to elapse, skipping now");
						continue;
					}
				} else
					logger.debug("we're live scenario");

				Long bannedTime = (action.getAction_bannedfor() * 60 * 1000);
				logger.info("banned time is:{}", bannedTime);

				// SPECIAL SITUATION FOR BANNED FOREVER, has to contains banned DIVERSO da ZERO
				if (bannedTime != 0) {
					if (rc.checkBanned(userId, action)) {
						logger.debug("this action is banned");
						continue;
					}
					if (rc.checkCancelled(userId, action)) {
						logger.debug("this action was cancelled");
						continue;
					}
				} else
					logger.debug("bannedtime == zero, no need to check banned/cancelled");

				Response rs = null;

				if (bannedTime != 0) {
					logger.info("get previous VIEWED:{}", System.currentTimeMillis() - bannedTime);
					rs = getPrevious(userId, action, "VIEWED", System.currentTimeMillis() - bannedTime);
				}

				if (rs != null) {
					logger.debug("this action is BANNED/VIEWED, skipping");
				} else {
					if (bannedTime != 0)
						rs = getPrevious(userId, action, "SENT", System.currentTimeMillis());
					if (rs != null) {
						logger.debug("this action was already SENT and still NOT elapsed, skipping");
					} else {
						logger.debug("this action was not already viewed/sent");
						// if CREATED update the time
						rs = getPrevious(userId, action, "CREATED");

						tx = session.beginTransaction();
						if (rs == null) {
							logger.debug("this action is new -> inserting!");
							rs = new Response(userId, action.getClasse(), action.getType(), action.getTitle(), action.getMsg(), action.getUri(), action.getGps_lat(), action.getGps_long(), status, now, current_elapsed,
									action.getServiceType(),
									action.getServiceLabel(), action.getServiceName(), action.getAction_sendrate(), action.getAction_howmany(), action.getAction_rulename(), null, null, isAssessor, action.getPoints());
							session.save(rs);
						} else {
							logger.debug("this action is old -> updating!");
							rs.setTimeCreated(now);
							rs.setTimeElapsed(current_elapsed);
							// this data can change since a same service can have more than one service type, i.e. the POI "GIARDINO DI BOBOLI" can have two different service uris
							// rs.setAction_uri(action.getUri());
							// rs.setService_type(action.getServiceType());
							// rs.setService_label(action.getServiceLabel());
							// rs.setService_name(action.getServiceName());
							// this data should not change
							// rs.setSend_rate(action.getAction_sendrate());
							// rs.setHow_many(action.getAction_howmany());
							// rs.setRule_name(action.getAction_rulename());
							session.update(rs);
						}
						tx.commit();

					}
				}
			}
			logger.debug("done");
		} catch (org.hibernate.NonUniqueObjectException o) {
			logger.warn("not unique obj in add:", o);
			if (tx != null)
				tx.rollback();
		} catch (Exception ex) {
			logger.error("cannot add due:", ex);
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

	// return true if a rule_name from the same userID elapseAFTER date was found
	@SuppressWarnings("unchecked")
	private boolean getPrevious(String userId, String action_rulename, Date date) {

		logger.debug("getting for user {} with rule_name {} from {}", userId, action_rulename, date);

		boolean toreturn = false;

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		List<Response> responses = null; // needed to update the final status to SENT

		try {

			tx = session.beginTransaction();

			responses = session.createCriteria(Response.class)
					.add(Restrictions.like("userId", userId))
					.add(Restrictions.like("rule_name", action_rulename))
					.add(Restrictions.or(Restrictions.like("status", "SENT"), Restrictions.like("status", "VIEWED")))
					.add(Restrictions.ge("timeElapsed", date))
					.list();

			tx.commit();

			if (responses.size() != 0) {
				logger.debug("at least one entry");
				toreturn = true;
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

	// return all the previous action with same ACTION MSG, with specified status
	private Response getPrevious(String userId, ACTION action, String status) {
		return getPrevious(userId, action, status, 0);
	}

	// return all the previous action with same ACTION MSG, with specified status, con timeElapsed piu' grande della data specificata
	@SuppressWarnings("unchecked")
	private Response getPrevious(String userId, ACTION action, String status, long date) {

		Response toreturn = null;

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		List<Response> responses = null; // needed to update the final status to SENT

		try {

			tx = session.beginTransaction();

			if (date == 0) {
				responses = session.createCriteria(Response.class)
						.add(Restrictions.like("userId", userId))
						.add(Restrictions.like("status", status))
						.add(Restrictions.like("action_class", action.getClasse()))
						.add(Restrictions.like("action_type", action.getType()))
						.add(Restrictions.like("action_msg", action.getMsg()))
						.list();
			} else
				responses = session.createCriteria(Response.class)
						.add(Restrictions.like("userId", userId))
						.add(Restrictions.like("status", status))
						.add(Restrictions.like("action_class", action.getClasse()))
						.add(Restrictions.like("action_type", action.getType()))
						.add(Restrictions.like("action_msg", action.getMsg()))
						.add(Restrictions.ge("timeElapsed", new Date(date)))
						.list();

			tx.commit();

			if (responses.size() == 0) {
				logger.debug("new entry for status:{} - userId:{}", status, userId);
			} else {
				if (responses.size() > 1) {
					logger.debug("more than one actions like this for status:{} - userId:{} . anyway, we return the first one:{}", status, userId, responses.get(0));
				} else {
					logger.debug("old entry for status:{} is {}", status, responses.get(0));
				}
				toreturn = responses.get(0);
			}

		} catch (Exception ex) {
			logger.error("cannot getPrevious due:", ex);
			if (tx != null)
				tx.rollback();
		} finally {
			try {
				// session.flush();
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
			}
		}

		return toreturn;
	}

	// simply return the response with the specified id: used in the cancel scenario
	@SuppressWarnings("unchecked")
	private Response getResponse(Long id) {

		Response toreturn = null;

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		List<Response> responses = null; // needed to update the final status to SENT

		try {

			tx = session.beginTransaction();

			responses = session.createCriteria(Response.class)
					.add(Restrictions.like("id", id))
					.list();

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

	// the timeelapsed returned is recalcolate back with terminal data now
	@SuppressWarnings("unchecked")
	public Object[] get(String userId, String selection) {

		logger.debug("requested get for user:{}, selection is:{}", userId, selection);

		Session session = sessionFactoryLocal.openSession();
		logger.trace("opened session");
		Transaction tx = null;

		List<ACTION> toreturn = new ArrayList<ACTION>();
		List<Response> responses = null; // needed to update the final status to SENT

		Date last_sent_or_viewed = null;
		Map<String, Date> last_sent_or_viewed_map = getLastTimeSent(userId);
		Integer current_howmany = null;
		Map<String, Integer> howmany_map = new HashMap<String, Integer>();

		try {

			logger.trace("begin transact session");
			tx = session.beginTransaction();
			logger.trace("begin transact session //DONE");

			if (selection == null) {
				responses = session.createCriteria(Response.class)
						.add(Restrictions.like("userId", userId))
						.add(Restrictions.like("status", "CREATED"))
						.add(Restrictions.ge("timeElapsed", new Date()))
						.addOrder(Order.desc("timeCreated"))
						.list();
			} else {
				responses = session.createCriteria(Response.class)
						.add(Restrictions.like("userId", userId))
						.add(Restrictions.like("status", "CREATED"))
						.add(Restrictions.like("action_class", selection))
						.add(Restrictions.ge("timeElapsed", new Date()))
						.addOrder(Order.desc("timeCreated"))
						.list();
			}

			logger.trace("begin commit");
			tx.commit();
			logger.trace("begin commit //DONE");
		} catch (Exception ex) {
			logger.error("cannot get due:", ex);
			if (tx != null)
				tx.rollback();
		} finally {
			try {
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
			}
		}

		try {
			logger.debug("Size of response before:{} for user:{}", responses.size(), userId);

			for (Iterator<Response> iterator = responses.iterator(); iterator.hasNext();) {
				Response response = iterator.next();

				logger.debug("analise {}", response.getAction_msg());

				// check howmany
				if ((current_howmany = howmany_map.get(response.getRule_name())) == null)
					current_howmany = 0;

				if ((current_howmany < response.getHow_many()) || (response.getHow_many() == 0)) {

					// check how many minutes before now you SENT or VIEWED a same rule name
					// if feaseable, return it
					if ((last_sent_or_viewed = last_sent_or_viewed_map.get(response.getRule_name())) == null) {
						logger.debug("last_sent_or_viewed NOT  from cache for {}", response.getRule_name());
						// last_sent_or_viewed_map.put(response.getRule_name(), last_sent_or_viewed = lastSentOrViewed(userId, response.getRule_name()));
						last_sent_or_viewed = new Date(0);
						last_sent_or_viewed_map.put(response.getRule_name(), last_sent_or_viewed);// adding date(0) if the user never recveived any rule of this
					} else {
						logger.debug("last_sent_or_viewed {} from cache for {}", last_sent_or_viewed, response.getRule_name());
					}

					// has to be created after last sent or view (BEWARE, consider a time-shift of 10 seconds, since LAST SENT can be on same date of LAST CREATED
					// AND
					// has to be rate==0 or now-last>rate

					if ((response.getTimeCreated().getTime() > (last_sent_or_viewed.getTime() - 10000))
							&& ((response.getSend_rate() == 0) || (((new Date()).getTime() - last_sent_or_viewed.getTime()) > response.getSend_rate() * 60 * 1000))) {

						ACTION action = new ACTION();
						action.setClasse(response.getAction_class());
						action.setType(response.getAction_type());
						action.setTitle(response.getAction_title());
						// action.setMsg(convertMessage(response.getAction_msg()));
						action.setMsg(response.getAction_msg());
						action.setUri(response.getAction_uri());
						action.setGps_lat(response.getGps_lat());
						action.setGps_long(response.getGps_long());
						action.setId(response.getId());
						long delta_elapsed = response.getTimeElapsed().getTime() - System.currentTimeMillis();
						if (delta_elapsed < 0) {
							logger.warn("beware, the delta_elapsed is negative, change to default: 1 hour, 3600 sec");
							delta_elapsed = 3600000;
						} else
							logger.debug("delta time is:{}", delta_elapsed);
						action.setTime_elapse(delta_elapsed); // calcolate delta time from now
						action.setTime_created(response.getTimeCreated().getTime());
						action.setServiceType(response.getService_type());
						action.setServiceLabel(response.getService_label());
						action.setServiceName(response.getService_name());
						action.setAction_rulename(response.getRule_name()); // needed for survey
						action.setPoints(response.getPoints());

						// add to the list
						howmany_map.put(response.getRule_name(), current_howmany + 1);
						toreturn.add(action);

						logger.debug("insert");

					} else {
						iterator.remove(); // and not update to SENT
						logger.debug("remove because constrain: sendrate ");
					}
				} else {
					iterator.remove(); // and not update to SENT
					logger.debug("remove because constrain: howmany ");
				}

			}

			logger.debug("Size of response after:{}", responses.size());
			int i = 0;
			for (ACTION a : toreturn) {
				logger.debug("{}-{}", i, a.getMsg());
				i++;
			}

		} catch (Exception e) {
			logger.error("TROUBLE in adding:", e);
		}

		// return [0] toreturn -> List<ACTION>
		// return [1] responses -> List<Responses>

		Object[] o = new Object[2];
		o[0] = toreturn;
		o[1] = responses;

		return o;
	}

	public Result getResult(String user_id, String selection, Long start_time, Long timeout) {
		return getResult(user_id, selection, start_time, timeout, true);
	}

	// update=false specify that the scenario is just in read (not updating to CREATED to SENT) (default = true)

	@SuppressWarnings("unchecked")
	public Result getResult(String user_id, String selection, Long start_time, Long timeout, Boolean update) {

		logger.info("===============================================Received a request for user_id={}", user_id);

		Result toreturn = new Result();

		Object[] get = get(user_id, selection);

		if (selection == null) {
			logger.debug("selection is ALL");

			for (ACTION action : (List<ACTION>) get[0])
				toreturn.get(action.getClasse()).add(action);

		} else {
			logger.debug("selection is:{}", selection);

			toreturn.set((List<ACTION>) get[0], selection);
		}

		logger.info("===============================================Returning a response for user_id={}", user_id);
		for (ACTION a : toreturn.getAssistance()) {
			logger.debug("{}", a.getMsg());
		}
		for (ACTION a : toreturn.getEngagement()) {
			logger.debug("{}", a.getMsg());
		}
		logger.info("===============================================");

		long duration = System.currentTimeMillis() - start_time;

		// if the response is NOT in time - 500 ms (used to be super sure the response is arrived)
		// return empty result
		if ((duration) > (timeout - 500)) {
			logger.error("ELAPSED TIME for response to: {} - time: {}", user_id, duration);
			toreturn = new Result();
		} else {
			// set in background to SENT the delivered data (just if read!=true)
			if ((get[1] != null) && (((List<Response>) get[1]).size() > 0) && (update)) {

				// update is Assessor from the first avaiable
				toreturn.setAssessor(((List<Response>) get[1]).get(0).getIsAssessor());

				UpdateBackground ub = new UpdateBackground((List<Response>) get[1], "SENT");
				ub.start();
			}
		}

		return toreturn;
	}

	protected class UpdateBackground extends Thread {

		List<Response> toupdate;
		String status;
		boolean simulazione;

		public UpdateBackground(List<Response> toupdate, String status) {
			this.toupdate = toupdate;
			this.status = status;

		}

		public void run() {
			logger.debug("Starting updating in background to SENT...");
			update(toupdate, status);
			logger.debug("... ended updating in background to SENT");
		}
	}

	// if we're in simulazione, the timeSend is the one speciifed in the action, otherwise is now!

	// save or update
	private void update(List<Response> responses, String string) {
		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			logger.trace("start here");

			tx = session.beginTransaction();

			// first of all set to SENT
			for (Response response : responses) {
				logger.trace("saving here");
				response.setStatus("SENT");
				response.setTimeSend(new Date());
				session.saveOrUpdate(response);
			}

			tx.commit();

			logger.trace("committed here");

		} catch (Exception ex) {
			logger.error("cannot update due:", ex);
			if (tx != null)
				tx.rollback();
		} finally {
			try {
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
			}
		}

		// secondly set timeSent
		for (Response response : responses) {
			logger.trace("for any");

			// response.setTimeSend(new Date());
			updateLastTimeSent(response.getUserId(), response.getRule_name(), new Date());
		}

		logger.trace("done");
	}

	// ----------------------------------------------------------------------------------------------------------------------------------------------------LAST TIME SENT
	// last time sent, specify rulename
	@SuppressWarnings("unchecked")
	private TimeLastSent getLastTimeSent(String userId, String ruleName) {
		TimeLastSent toreturn = null;

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			tx = session.beginTransaction();

			List<TimeLastSent> responses = session.createCriteria(TimeLastSent.class)
					.add(Restrictions.like("userId", userId))
					.add(Restrictions.like("ruleName", ruleName))
					.list();

			tx.commit();

			if (responses.size() > 1) {
				logger.warn("Seems very strange, there are more than one timesent for user {} for rule {}. We get first one anyway", userId, ruleName);
			}
			if (responses.size() == 0)
				logger.debug("Still there are no timeSent related to the rulename ({}) for the user:{}", ruleName, userId);
			else {
				logger.debug("There is a timeSEnt related to the ruleName ({}) for the user:{} -->{}", ruleName, userId, responses.get(0));
				toreturn = responses.get(0);
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

	// get ALL last time sent for specified user
	@SuppressWarnings("unchecked")
	private Map<String, Date> getLastTimeSent(String userId) {
		Map<String, Date> toreturn = new HashMap<String, Date>();

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			tx = session.beginTransaction();

			List<TimeLastSent> responses = session.createCriteria(TimeLastSent.class)
					.add(Restrictions.like("userId", userId))
					.list();

			tx.commit();

			for (TimeLastSent tls : responses) {
				if (toreturn.containsKey(tls.getRuleName()))
					logger.warn("there are more than two timesent related to the rule {} for user {}. Anyway, we override", tls.getRuleName(), userId);
				toreturn.put(tls.getRuleName(), tls.getTimeLastSent());
				logger.debug("added to timesent {}", tls.toString());
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

	// save or update
	private void updateLastTimeSent(String userId, String ruleName, Date sentTime) {
		TimeLastSent tls = getLastTimeSent(userId, ruleName);

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			tx = session.beginTransaction();

			if (tls == null) {

				tls = new TimeLastSent(userId, ruleName, sentTime);
				logger.debug("this lastsent new -> inserting! {}", tls);
				session.save(tls);
			} else {

				tls.setTimeLastSent(sentTime);
				logger.debug("this lastsent is old -> updating! {}", tls);
				session.update(tls);
			}

			tx.commit();

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
	}

	// ----------------------------------------------------------------------------------------------------------------------------------------------------ACCESS LOG, set viewed
	public void setLastAccesslog(Date last_accesslog) {
		logger.debug("Set LAST ACCESS LOG to:{}", last_accesslog);

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			LastAccesslog rs = new LastAccesslog(1L, last_accesslog);

			tx = session.beginTransaction();

			session.saveOrUpdate(rs);

			tx.commit();
		} catch (Exception ex) {
			logger.error("cannot setLastAccessLog due:", ex);
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

	public Date getLastAccesslog() {
		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		Date toreturn = new Date(0);

		try {

			tx = session.beginTransaction();

			LastAccesslog rs = session.get(LastAccesslog.class, 1L);

			tx.commit();

			if (rs == null) {
				logger.warn("LAST ACCESS still not present, returning null");
			} else {
				logger.debug("Get LAST ACCESS is:{}", rs.getLast_accesslog());
				toreturn = rs.getLast_accesslog();
			}

		} catch (Exception ex) {
			logger.error("cannot getLastAccessLog due:", ex);
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

	// saveorupdate
	public void setViewed(UserClick uc) {
		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			tx = session.beginTransaction();

			@SuppressWarnings("unchecked")
			List<Response> responses = session.createCriteria(Response.class)
					.add(Restrictions.like("userId", uc.getUserName()))
					.add(Restrictions.like("status", "SENT"))
					.add(Restrictions.ge("timeElapsed", new Timestamp(uc.getTimestamp().getTime() + 10 * 1000L)))// add TEN seconds since the terminal sometimes notify the user with little delay
					.list();

			if ((responses == null) || (responses.size() == 0)) {
				logger.debug("nothing pending for this user in this moment");
			} else {

				for (Response response : responses) {
					logger.debug("set to viewed this result: {}", response);
					response.setStatus("VIEWED");
					response.setTimeViewed(new Date());// settimeviewed now, and not consider uc.getTimeStemp, since i wanna order the viewed
					session.saveOrUpdate(response);
				}
			}

			tx.commit();

		} catch (Exception ex) {
			logger.error("cannot setViewed due:", ex);
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

	// saveorupdate
	public void updateFeedback(List<UserClick> ucs) {
		for (UserClick uc : ucs) {
			logger.debug("analise:{}", uc);
			setViewed(uc);
		}
	}

	// ----------------------------------------------------------------------------------------------------------------------------------------------------PURGE
	public void purge(long timeoutEngagement) {

		logger.debug("**purge**");

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			Date now = new Date();
			Date timeoutEngagementDate = new Date(now.getTime() - timeoutEngagement);

			String now_s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now);
			String timeout_s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timeoutEngagementDate);

			String sqlquery = "DELETE FROM engager.response where status='CREATED' and (timeElapsed<'" + now_s + "' or timeCreated<'" + timeout_s + "') ;";

			logger.debug("**starting** {}", sqlquery);

			tx = session.beginTransaction();

			SQLQuery query = session.createSQLQuery(sqlquery);

			query.executeUpdate();

			tx.commit();

			logger.debug("**ended**");

		} catch (Exception ex) {
			logger.error("cannot purge due:", ex);
			if (tx != null)
				tx.rollback();
		} finally {
			try {
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
			}
		}

		logger.debug("**done purge**");

	}

	// ----------------------------------------------------------------------------------------------------------------------------------------------------common:getMAXID, retrievelast, isAccessor
	@SuppressWarnings("unchecked")
	// replica, to uniform
	public int getMaxId() {

		int toreturn = 0;

		Session session = sessionFactoryRemoteSensors.openSession();
		// Session session = sessionFactoryRemotePHOE.openSession();
		Transaction tx = null;

		try {

			String sqlquery = "SELECT max(user_eval_id) FROM sensors.user_eval;";
			// String sqlquery = "SELECT user_eval_id FROM user_eval order by user_eval_id DESC limit 1";

			logger.debug("requested MaxID");

			tx = session.beginTransaction();

			SQLQuery query = session.createSQLQuery(sqlquery);

			// List<java.math.BigInteger> returned = query.list();
			List<Integer> returned = query.list();

			tx.commit();

			logger.debug("returned MaxID");

			if (returned.size() > 0) {
				// toreturn = ((java.math.BigInteger) returned.get(0)).intValue();
				toreturn = ((Integer) returned.get(0)).intValue();
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

	// retrieve from the remote DB the list of the userlocation that:
	// 1-from
	// if the timestamp is equal to 0000-00-00 00:00:00 or gps is not available, it does not return the item
	@SuppressWarnings("unchecked")
	public List<MobilityUserLocation> retrieveLastLocations(Long fromID) {

		List<MobilityUserLocation> toreturn = new ArrayList<MobilityUserLocation>();

		Session session = sessionFactoryRemoteSensors.openSession();
		// Session session = sessionFactoryRemotePHOE.openSession();
		Transaction tx = null;

		try {

			// String sqlquery = "SELECT user_eval.user_eval_id, user_eval.date, user_eval.device_id, user_eval.latitude, user_eval.longitude, user_eval.curr_status, user_eval.lang, user_eval.accuracy, user_eval.speed "
			// + "FROM sensors.user_eval WHERE "
			// + "(user_eval.user_eval_id>'" + fromID + "') limit 100;"; // limit 100 data any time
			String sqlquery = "SELECT user_eval_id, date, device_id, latitude, longitude, curr_status, lang, accuracy, speed, profile, provider, avg_speed, avg_lin_acc_magn, lin_acc_x, lin_acc_y, lin_acc_z "
					+ "FROM user_eval WHERE "
					+ "(user_eval_id>" + fromID + ") limit " + THRESHOLD_ENGAGER_ONLINE;// limit 100 data any time

			logger.debug("requested retrieveLastLocations from {}", fromID);

			tx = session.beginTransaction();

			SQLQuery query = session.createSQLQuery(sqlquery);

			List<Object[]> entities = query.list();

			tx.commit();

			logger.debug("returned retrieveLastLocations");

			for (Object[] entity : entities) {
				// TODO this check depending on the query since return an Object[]

				// not consider: device == null
				// not consider: timestamp null or 0000-00-00
				if ((entity[0] != null) && (entity[1] != null) && (!entity[1].toString().equals("0000-00-00 00:00:00")))
					toreturn.add(new MobilityUserLocation(entity));
				else
					logger.debug("NULL VALUE from timestamp!!! ignore it!");
			}

			logger.debug("Retrieved n={} locations", entities.size());

		} catch (Exception ex) {
			logger.error("cannot retrieveLastLocations due:", ex);
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

	// ----------------------------------------------------------------------------------------------------------------------------------------------------CANCEL ENGAGEMENT
	// check if this user had already completed this survey
	// used from engager
	public void insertEngageCancelled(Long engageId) throws Exception {
		logger.debug("insertEngageCancelled: engageId:{}", engageId);

		Response r = getResponse(engageId);

		if (r == null) {
			logger.error("specified id is not found");
			throw new Exception("specified id is not found");
		}

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			EngageCancelled ec = new EngageCancelled(engageId, r.getUserId());

			tx = session.beginTransaction();

			session.save(ec);

			tx.commit();

		} catch (Exception ex) {
			logger.error("cannot retrieve due:", ex);
			if (tx != null)
				tx.rollback();
			throw new Exception(ex.getMessage());
		} finally {
			try {
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
				throw new Exception(e.getMessage());
			}
		}

		logger.debug("added EngageCancelled: engageId:{}", engageId);
	}

	// -----------------------------------------------------------------------------------------executed
	public void setLastExecuted(Date last_executed) {
		logger.debug("Set LAST EXECUTED to:{}", last_executed);

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			LastExecuted rs = new LastExecuted(1L, last_executed);

			tx = session.beginTransaction();

			session.saveOrUpdate(rs);

			tx.commit();
		} catch (Exception ex) {
			logger.error("cannot setLastExecuted due:", ex);
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

	public Date getLastExecuted() {
		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		Date toreturn = new Date(0);

		try {

			tx = session.beginTransaction();

			LastExecuted rs = session.get(LastExecuted.class, 1L);

			tx.commit();

			if (rs == null) {
				logger.warn("LAST EXECUTED still not present, returning null");
			} else {
				logger.debug("Get LAST ACCESS is:{}", rs.getLast_executed());
				toreturn = rs.getLast_executed();
			}

		} catch (Exception ex) {
			logger.error("cannot getLastExecuted due:", ex);
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

	// search the engagement SENT or VIEWED from last to now
	@SuppressWarnings("unchecked")
	public List<Response> retrieveEngagementForExecuted(Date last) {
		List<Response> toreturn = new ArrayList<Response>();
		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			tx = session.beginTransaction();

			toreturn = session.createCriteria(Response.class)
					.add(Restrictions.or(Restrictions.like("status", "SENT"), Restrictions.like("status", "VIEWED")))
					.add(Restrictions.ge("timeSend", last))
					.addOrder(Order.asc("timeSend"))

					.list();

			tx.commit();

			logger.debug("Get engagement for executed size>{}", toreturn.size());

		} catch (Exception ex) {
			logger.error("cannot retrieveEngagementForExecution due:", ex);
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

	public void insertEngagementExecuted(Response executed, Date executionTime) {
		logger.debug("insertEngageExecuted: engageId:{}", executed.getId());

		Response re = getResponse(executed.getId());

		if (re == null) {
			logger.error("specified id is not found");
			return;
		}

		List<EngageExecuted> ees = getEngagementExecuted(executed.getUserId(), executed.getId(), executed.getRule_name());
		if ((ees != null) && (ees.size() != 0)) {
			logger.debug("already here, ignore");
			return;
		}

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			// the time of execution is the current time
			// in same case, if the execution time is in the past, this execution is not inserted in the dashboard!!!)
			EngageExecuted ee = new EngageExecuted(executed.getId(), executionTime, executed.getUserId(), executed.getRule_name());

			tx = session.beginTransaction();

			session.save(ee);

			tx.commit();

			logger.debug("added EngageExecuted: engageId:{}", executed.getId());

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
	}

	@SuppressWarnings("unchecked")
	public List<EngageExecuted> getEngagementExecuted(String userId, Long responseId, String ruleName) {
		List<EngageExecuted> toreturn = null;

		logger.debug("getEngageExecuted: {} {} {}", userId, responseId, ruleName);

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			tx = session.beginTransaction();

			toreturn = session.createCriteria(EngageExecuted.class)
					.add(Restrictions.like("userId", userId))
					.add(Restrictions.like("ruleName", ruleName))
					.add(Restrictions.like("responseId", responseId))
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

	// ------------------------------------------------------------------------------------------
	public List<UserClick> retrieveAccessLogUserClick(Date last, Date now) {
		logger.info("test access log start");
		List<UserClick> toreturn = new ArrayList<UserClick>();

		Session session = sessionFactoryRemoteAccessLog.openSession();
		// Session session = sessionFactoryRemotePHOE.openSession();
		Transaction tx = null;

		try {

			String before_s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(last);
			String now_s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now);

			String sqlquery = "SELECT AccessLog.timestamp, AccessLog.uid FROM ServiceMap.AccessLog WHERE (mode='api-notification') and "
					+ "(timestamp between '" + before_s + "' and '" + now_s + "') and "
					+ "(selection='" + Constants.PERSONAL_ASSISTANCE_BUTTON + "' or selection='" + Constants.PERSONAL_ASSISTANCE_NOTIFICATION_BUTTON + "');";
			// String sqlquery = "SELECT timestamp, uid "
			// + "FROM AccessLog "
			// + "WHERE (mode='api-notification') "
			// + "and (timestamp between TO_TIME('" + before_s + "','yyyy-MM-dd HH:mm:ss','GMT+2') and TO_TIME('" + now_s + "','yyyy-MM-dd HH:mm:ss','GMT+2')) "
			// + "and (selection='" + Constants.PERSONAL_ASSISTANCE_BUTTON + "' or selection='" + Constants.PERSONAL_ASSISTANCE_NOTIFICATION_BUTTON + "')";

			logger.info("test access log begin");
			tx = session.beginTransaction();

			SQLQuery query = session.createSQLQuery(sqlquery);

			@SuppressWarnings("unchecked")
			List<Object[]> entities = query.list();

			tx.commit();
			logger.info("test access log committed");

			for (Object[] entity : entities) {
				toreturn.add(new UserClick(entity));
			}

			logger.debug("Retrieved n={} locations", entities.size());

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

		logger.info("test access log start");
		return toreturn;
	}

	// --------------used by checker
	// return true if there is at least an entry related to this user and this serviceUri and mode= to:
	// api-service-stars
	// api-service-comment
	// api-service-photo
	@SuppressWarnings("unchecked")
	public Date retrieveAccessLogServiceUriClick(String userId, String serviceUri) {

		Date toreturn = null;

		logger.debug("RetrieveAccessLogServiceUriClick for user:{} and serviceUri:{}", userId, serviceUri);

		List<Object> entities = new ArrayList<Object>(0);

		Session session = sessionFactoryRemoteAccessLog.openSession();
		// Session session = sessionFactoryRemotePHOE.openSession();
		Transaction tx = null;

		try {

			String sqlquery = "SELECT AccessLog.timestamp FROM ServiceMap.AccessLog where serviceUri='" + serviceUri
					+ "' and uid='" + userId + "' and (mode='api-service-stars' or mode='api-service-comment' or mode='api-service-photo');";
			// String sqlquery = "SELECT timestamp "
			// + "FROM AccessLog "
			// + "WHERE serviceUri='" + serviceUri + "' "
			// + "and uid='" + userId + "' "
			// + "and (mode='api-service-stars' or mode='api-service-comment' or mode='api-service-photo')";

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

		Session session = sessionFactoryLocal.openSession();
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

		Session session = sessionFactoryLocal.openSession();
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
					Response r = getResponse((((java.math.BigInteger) o[0]).longValue()));

					if ((r != null) && (action.getMsg().equals(r.getAction_msg()))) {
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
					Response r = getResponse((((java.math.BigInteger) o[0]).longValue()));

					if ((r != null) && (action.getMsg().equals(r.getAction_msg()))) {
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
					Response r = getResponse((((java.math.BigInteger) o[0]).longValue()));

					if ((r != null) && (action.getMsg().equals(r.getAction_msg()))) {
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

	// -------------------------------------------------------------------last engaged
	public void setLastEngaged(Long last_engaged) {
		logger.debug("Set LAST ENGAGED to:{}", last_engaged);

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			LastEngaged rs = new LastEngaged(1L, last_engaged);

			tx = session.beginTransaction();

			session.saveOrUpdate(rs);

			tx.commit();
		} catch (Exception ex) {
			logger.error("cannot setLastEngaged due:", ex);
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

	public Long getLastEngaged() {
		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		Long toreturn = null;

		try {

			tx = session.beginTransaction();

			LastEngaged rs = session.get(LastEngaged.class, 1L);

			tx.commit();

			if (rs == null) {
				logger.warn("LAST ENGAGED still not present, returning null");
			} else {
				logger.debug("Get LAST ENGAGED is:{}", rs.getLast_engaged());
				toreturn = rs.getLast_engaged();
			}

		} catch (Exception ex) {
			logger.error("cannot getLastEngaged due:", ex);
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
