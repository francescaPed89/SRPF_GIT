/**
*
* MODULE FILE NAME:	PlatformActivityWindowDAO.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Define the PAW Data Abstract Model for low level DB access
*
* PURPOSE:			Used for DB data
*
* CREATION DATE:	29-01-2016
*
* AUTHORS:			Abed Alissa
*
* DESIGN ISSUE:		1.0
*
* INTERFACES:
*
* SUBORDINATES:
*
* MODIFICATION HISTORY:
*
*             Date                |  Name      |   New ver.    | Description
* --------------------------+------------+----------------+-------------------------------
* <DD-MMM-YYYY> | <name>  |<Ver>.<Rel> | <reasons of changes>
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.dataManager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.telespazio.csg.srpf.dataManager.GenericDAO;
import com.telespazio.csg.srpf.dataManager.bean.PlatformActivityWindowBean;
import com.telespazio.csg.srpf.dataManager.util.DataManagerConstants;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.DateUtils;

/**
 * Define the PAW Data Abstract Model for low level DB access
 * 
 * @author Abed Alissa
 * @version 1.0
 *
 */
public class PlatformActivityWindowDAO extends GenericDAO {
	static final Logger logger = LogManager.getLogger(PlatformActivityWindowDAO.class.getName());

	/**
	 * Insert statement
	 */
	private static final String PawInsertQuery = "INSERT INTO GSIF_PAW (ID, SATELLITE, ACTIVITY_TYPE, ACTIVITY_ID, ACTIVITY_START_TIME, ACTIVITY_STOP_TIME, DEFERRABLE_FLAG) "

			+ "VALUES (GSIF_PAW_SEQ.nextval,?,?,?,?,?,?)";
	/**
	 * Logger
	 */
	TraceManager tm = new TraceManager();

	/**
	 * Default constructor
	 * 
	 * @throws NamingException
	 * @throws Exception
	 */
	public PlatformActivityWindowDAO() throws NamingException, Exception {
		super();
		// TODO Auto-generated constructor stub
	}// end method

	/**
	 * Delete a specific activity from PAW table
	 * 
	 * @param activityId
	 * @throws Exception
	 */
	public void deleteActivity(String satName, long activityId) throws Exception {

		// statement
		Statement deleteStatement = null;

		try {
			int satId = getIdSatellite(satName);
			// delete string
			if (con == null || con.isClosed()) {
				con = super.initConnection();
			}
			String deleteString = "delete from GSIF_PAW where ACTIVITY_ID=" + activityId + " and SATELLITE=" + satId;
			this.con.setAutoCommit(false);
			deleteStatement = this.con.createStatement();
			// execute statement
			deleteStatement.executeUpdate(deleteString);
		} // end try
		catch (Exception e) {
			// log
			this.tm.warning(EventType.APPLICATION_EVENT, ProbableCause.APPLICATION_SUBSYSTEM_FAILURE, e.getMessage());
			// rollback
			this.con.rollback();
			// close
			closeStatement(deleteStatement);
			// rethrow
			throw e;
		} // end catch
		finally {
			// close statement
			closeStatement(deleteStatement);
			con.close();
		} // end finally

	}// end method

	/**
	 * Delete all activities inside the startValidity stopValidity
	 * 
	 * @param startValidity
	 * @param stopValidity
	 * @param mission
	 * @throws SQLException
	 */
	public void deleteActivityInsideWindow(double startValidity, double stopValidity, boolean isOcculted,
			String mission) throws Exception {
		Statement deleteStatement = null;
		try {

			// String deleteString = "delete from GSIF_PAW where
			// ACTIVITY_START_TIME>=" + startValidity +" and
			// ACTIVITY_STOP_TIME<=" + stopValidity;
			// String deleteString="delete from GSIF_PAW where
			// ((ACTIVITY_START_TIME>=" + startValidity +" and
			// ACTIVITY_START_TIME<=" + stopValidity+")"+
			// " or (ACTIVITY_STOP_TIME>=" + startValidity +" and
			// ACTIVITY_STOP_TIME<="+stopValidity+"))";

			String deleteString = "delete from GSIF_PAW where ACTIVITY_START_TIME>=" + startValidity
					+ " and ACTIVITY_STOP_TIME<=" + stopValidity;

			if (isOcculted) {
				// occulkted paw
				deleteString = deleteString + " and ACTIVITY_TYPE='" + DataManagerConstants.OCCULTED_PAW_TYPE + "'";
			} // end if
			else {
				// not occultration paw
				deleteString = deleteString + " and ACTIVITY_TYPE!='" + DataManagerConstants.OCCULTED_PAW_TYPE + "'";
			} // end else

			// taking into account mission
			// only paw relate the requested mission will be deleted
			deleteString = deleteString + " AND SATELLITE in "
					+ "(SELECT SATELLITE.ID_SATELLITE from SATELLITE join MISSION on SATELLITE.MISSION=MISSION.ID_MISSION AND MISSION.MISSION_NAME='"
					+ mission + "')";
			if (con == null || con.isClosed()) {
				con = super.initConnection();
			}
			// no autocommit
			this.con.setAutoCommit(false);
			// executing
			deleteStatement = this.con.createStatement();
			deleteStatement.executeUpdate(deleteString);
		} // end try
		catch (Exception e) {
			// log
			// rollback
			// close stm
			// throw
			this.tm.warning(EventType.APPLICATION_EVENT, ProbableCause.APPLICATION_SUBSYSTEM_FAILURE, e.getMessage());
			this.con.rollback();
			closeStatement(deleteStatement);
			throw e;
		} // end catch
		finally {
			// close stm
			closeStatement(deleteStatement);
			con.close();
		} // end finally

	}// end method

	/**
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param satelliteName
	 * @param activityType
	 * @param activityId
	 * @param activityStartTime
	 * @param activityStopTime
	 * @param deferrableFlag
	 * @throws NamingException
	 * @throws Exception
	 */
	public void uploadPaw(String satelliteName, String activityType, long activityId, double activityStartTime,
			double activityStopTime, boolean deferrableFlag) throws NamingException, Exception

	{

		// System.out.println("inside PlatformActivityWindowDAO.uploadPaw");
		int satId = 0;
		// satName = tokens.nextToken().toString();
		// prepared stm
		// System.out.println("Sat Name: " + satelliteName);
		PreparedStatement insertStatement = null;

		try

		{
			satId = getIdSatellite(satelliteName);
			// System.out.println("show connection status "+this.con.toString());
			if (con == null || con.isClosed()) {
				con = super.initConnection();
			}
			// autocommit false
			con.setAutoCommit(false);
			// building statement
			insertStatement = this.con.prepareStatement(PawInsertQuery);
			// System.out.println("trying to insert data into DB " + PawInsertQuery);
			// inserting values
			insertStatement.setInt(1, satId);
			insertStatement.setString(2, activityType);
			insertStatement.setLong(3, activityId);
			insertStatement.setDouble(4, activityStartTime);
			insertStatement.setDouble(5, activityStopTime);
			// check for deferreabvìle
			if (deferrableFlag == true) {
				insertStatement.setInt(6, 1);
			} // end if
			else {
				insertStatement.setInt(6, 0);
			} // end else
				// executing inster
			insertStatement.executeUpdate();
			// -
			// -
			// -

		} // end try

		catch (NoSuchElementException e)

		{
			// rollback
			con.rollback();
			// close stm
			closeStatement(insertStatement);

		} // end catch

		finally {
			// close stm
			closeStatement(insertStatement);
			con.close();
		} // end finally

		// System.out.println("Data inserted");
	}// end method

	/**
	 * return sat id given its name
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @date 2016-1-20
	 * @param sensoreModeName
	 * @return sat id
	 * @throws Exception
	 */
	public int getIdSatellite(String satelliteName) throws Exception {

		int idSatellite = 0;
		// query string
		String query = "SELECT ID_SATELLITE  from SATELLITE where   SATELLITE_NAME = " + "'" + satelliteName + "'";
		if (con == null || con.isClosed()) {
			con = super.initConnection();
		}
		PreparedStatement st = this.con.prepareStatement(query);
		ResultSet rs = null;
		try {

			PlatformActivityWindowDAO.logger.debug("Inside method getIdSatellite: " + query);
			rs = st.executeQuery();

			while (rs.next()) {
				// getting id
				idSatellite = rs.getInt("ID_SATELLITE");

			} // end while

		} // en try
		catch (Exception e)

		{
			// log
			// throw
			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
					e.getMessage());

			throw e;

		} // end catch
		finally {

			// cleaning

			if (rs != null) {
				rs.close();
			}

			st.close();
			con.close();
		} // end finally

		return idSatellite;
	}// end method

	/**
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param idSatellite
	 * @param initialEpoch
	 * @param finalEpoch
	 * @return list of paw
	 * @throws Exception
	 */
	public ArrayList<PlatformActivityWindowBean> selectPawDataAll(int idSatellite, double initialEpoch,
			double finalEpoch) throws Exception {

		// francescapedrola : questo metodo non viene mai invocato!!!!!!!!!!!!

		// String query = "SELECT SATELLITE, SATELLITE_NAME, ACTIVITY_TYPE,
		// ACTIVITY_ID, ACTIVITY_START_TIME, ACTIVITY_STOP_TIME, DEFERRABLE_FLAG
		// FROM GSIF_PAW gp, SATELLITE s "
		// +"where gp.SATELLITE =s.ID_SATELLITE and SATELLITE= " + idSatellite
		// + " AND () order by gp.SATELLITE";

		String query = "SELECT  SATELLITE, SATELLITE_NAME, ACTIVITY_TYPE, ACTIVITY_ID, ACTIVITY_START_TIME, ACTIVITY_STOP_TIME, DEFERRABLE_FLAG FROM GSIF_PAW gp, SATELLITE s "
				+ "where  gp.SATELLITE =s.ID_SATELLITE  and SATELLITE= " + idSatellite + " AND ("
				+ "(gp.ACTIVITY_START_TIME <=" + finalEpoch + " and gp.ACTIVITY_STOP_TIME >=" + initialEpoch + ")"
				+ ") order by gp.SATELLITE";

		PlatformActivityWindowDAO.logger.debug(" selectPawDataAll:  " + query);
		PlatformActivityWindowDAO.logger.debug(" with initial epoch:  " + initialEpoch);
		PlatformActivityWindowDAO.logger.debug(" and final epoch :  " + finalEpoch);
		PlatformActivityWindowDAO.logger
				.debug(" with initial epoch DATE:  " + DateUtils.fromCSKDateToDateTime(initialEpoch));
		PlatformActivityWindowDAO.logger
				.debug(" and final epoch DATE:  " + DateUtils.fromCSKDateToDateTime(finalEpoch));
		if (con == null || con.isClosed()) {
			con = super.initConnection();
		}
		this.con.setAutoCommit(false);

		PreparedStatement st = this.con.prepareStatement(query);
		ResultSet rs = null;
		ArrayList<PlatformActivityWindowBean> pawList = new ArrayList<>();
		PlatformActivityWindowDAO.logger.debug(" FRANCESCA : codice aggiornato al 03/02/2020:  ");

		PlatformActivityWindowBean pawData = null;
		try {
			// execute query
			rs = st.executeQuery();
			// foreach row
			// retrieve data
			while (rs.next()) {
				pawData = new PlatformActivityWindowBean();

				pawData.setSatId(rs.getInt(1));
				pawData.setSatName(rs.getString(2));

				pawData.setActivityType(rs.getString(3));
				pawData.setActivityId(rs.getLong(4));
				pawData.setActivityStartTime(rs.getDouble(5));
				PlatformActivityWindowDAO.logger.debug(" for paw :  " + pawData);

				pawData.setActivityStopTime(rs.getDouble(6));
				PlatformActivityWindowDAO.logger.debug(" for paw startTime :  " + pawData.getActivityStartTime());
				PlatformActivityWindowDAO.logger.debug(" for paw startTime as Date :  "
						+ DateUtils.fromCSKDateToDateTime(pawData.getActivityStartTime()));

				PlatformActivityWindowDAO.logger.debug(" for paw stopTime :  " + pawData.getActivityStopTime());
				PlatformActivityWindowDAO.logger.debug(" for paw stopTime as Date :  "
						+ DateUtils.fromCSKDateToDateTime(pawData.getActivityStopTime()));

				if (pawData.getActivityStartTime() < initialEpoch && pawData.getActivityStopTime() > finalEpoch) {
					PlatformActivityWindowDAO.logger.debug(" visibility period totally included inside the paw ");

				} else if (pawData.getActivityStopTime() > finalEpoch
						&& pawData.getActivityStartTime() > initialEpoch) {
					PlatformActivityWindowDAO.logger.debug(" paw overlap partially in stop ");
				} else if (pawData.getActivityStartTime() >= initialEpoch
						&& pawData.getActivityStopTime() <= finalEpoch) {
					PlatformActivityWindowDAO.logger.debug(" paw totally included inside the visibility period");

				} else {
					PlatformActivityWindowDAO.logger.debug(" paw overlap partially in start ");
				}
				PlatformActivityWindowDAO.logger.debug(" added pawData:  " + pawData);

				// check if defereeable
				if (rs.getDouble(7) == DataManagerConstants.DEFERRABLE_FLAG_TRUE) {
					pawData.setDeferrableFlag(true);
				} // end if
				else {
					pawData.setDeferrableFlag(false);
				} // end else
				PlatformActivityWindowDAO.logger.debug(" added pawData:  " + pawData);

				pawList.add(pawData);
			} // end while
		} // end try
		catch (SQLException e) {
			// log
			this.tm.critical(EventType.SOFTWARE_EVENT, "Errore in SELECT from GSIF PAW: ", e.getMessage());
			// rethow
			throw e;

		} // end catch
		finally {
			// close rs set
			if (rs != null) {
				rs.close();
			}

			// close stm
			st.close();
			con.close();
			// con.close();
		} // end finally
		return pawList;
	}// end method

	/**
	 * Return a map holding the paw list for each satellite
	 * 
	 * @param start
	 * @param stop
	 * @return map holding the paw list for each satellite
	 * @throws SQLException
	 */
	public Map<String, ArrayList<PlatformActivityWindowBean>> getPaws(double start, double stop) throws SQLException {
		// map to be returned
		Map<String, ArrayList<PlatformActivityWindowBean>> pawMap = new TreeMap<>();

		// maxy query

		/*
		 * String query =
		 * "SELECT SATELLITE.ID_SATELLITE, SATELLITE.SATELLITE_NAME, GSIF_PAW.ACTIVITY_TYPE, "
		 * +
		 * "GSIF_PAW.ACTIVITY_ID, GSIF_PAW.ACTIVITY_START_TIME, GSIF_PAW.ACTIVITY_STOP_TIME, GSIF_PAW.DEFERRABLE_FLAG "
		 * +
		 * " from GSIF_PAW inner join SATELLITE on GSIF_PAW.SATELLITE = SATELLITE.ID_SATELLITE where "
		 * + " (GSIF_PAW.ACTIVITY_START_TIME <= " + start +
		 * " and GSIF_PAW.ACTIVITY_STOP_TIME >=" + stop + ") OR" +
		 * " (GSIF_PAW.ACTIVITY_START_TIME >= " + start +
		 * " and GSIF_PAW.ACTIVITY_START_TIME <=" + stop + ")" +
		 * " order by SATELLITE.SATELLITE_NAME, GSIF_PAW.ACTIVITY_START_TIME  ";
		 * 
		 * 
		 */
		String query = "SELECT SATELLITE.ID_SATELLITE, SATELLITE.SATELLITE_NAME, GSIF_PAW.ACTIVITY_TYPE, "
				+ "GSIF_PAW.ACTIVITY_ID, GSIF_PAW.ACTIVITY_START_TIME, GSIF_PAW.ACTIVITY_STOP_TIME, GSIF_PAW.DEFERRABLE_FLAG "
				+ " from GSIF_PAW inner join SATELLITE on GSIF_PAW.SATELLITE = SATELLITE.ID_SATELLITE where "
				+ " (GSIF_PAW.ACTIVITY_START_TIME <= " + stop + " and GSIF_PAW.ACTIVITY_STOP_TIME >=" + start + ")"
				+ " order by SATELLITE.SATELLITE_NAME, GSIF_PAW.ACTIVITY_START_TIME  ";

		PlatformActivityWindowDAO.logger.debug(" getPaws:  " + query);
		PlatformActivityWindowDAO.logger.debug(" with initial epoch:  " + DateUtils.fromCSKDateToDateTime(start));
		PlatformActivityWindowDAO.logger.debug(" and final epoch :  " + DateUtils.fromCSKDateToDateTime(stop));
		PlatformActivityWindowDAO.logger.debug(" with initial epoch (double):  " + start);
		PlatformActivityWindowDAO.logger.debug(" and final epoch (double):  " + stop);

		PreparedStatement st = null;
		ResultSet rs = null;

		String newSatelliteName = "";
		PlatformActivityWindowBean pawData;

		//ArrayList<PlatformActivityWindowBean> currentSatellitePawList = new ArrayList<>();

		try {
			if (con == null || con.isClosed()) {
				con = super.initConnection();
			}
			st = this.con.prepareStatement(query);
			// exeuting query
			rs = st.executeQuery();

			// for each row in result set
			while (rs.next()) {
				// retrieve info on paw
				newSatelliteName = rs.getString(2);

				pawData = new PlatformActivityWindowBean();

				pawData.setSatId(rs.getInt(1));
				pawData.setSatName(rs.getString(2));

				pawData.setActivityType(rs.getString(3));
				pawData.setActivityId(rs.getLong(4));
				pawData.setActivityStartTime(rs.getDouble(5));
				pawData.setActivityStopTime(rs.getDouble(6));
				// check if dererreable
				if (rs.getInt(7) == DataManagerConstants.DEFERRABLE_FLAG_TRUE) {
					pawData.setDeferrableFlag(true);
				} // end if
				else {
					pawData.setDeferrableFlag(false);
				} // end else
				PlatformActivityWindowDAO.logger.debug("for paw :  " + pawData);

				PlatformActivityWindowDAO.logger.debug(" for paw startTime :  " + pawData.getActivityStartTime());
				PlatformActivityWindowDAO.logger.debug(" for paw startTime as Date :  "
						+ DateUtils.fromCSKDateToDateTime(pawData.getActivityStartTime()));

				PlatformActivityWindowDAO.logger.debug(" for paw stopTime :  " + pawData.getActivityStopTime());
				PlatformActivityWindowDAO.logger.debug(" for paw stopTime as Date :  "
						+ DateUtils.fromCSKDateToDateTime(pawData.getActivityStopTime()));

				if (pawData.getActivityStartTime() < start && pawData.getActivityStopTime() > stop) {
					PlatformActivityWindowDAO.logger.debug(" visibility period totally included inside the paw ");

				} else if (pawData.getActivityStopTime() > stop && pawData.getActivityStartTime() > start) {
					PlatformActivityWindowDAO.logger.debug(" paw overlap partially in stop ");
				} else if (pawData.getActivityStartTime() >= start && pawData.getActivityStopTime() <= stop) {
					PlatformActivityWindowDAO.logger.debug(" paw totally included inside the visibility period");

				} else {
					PlatformActivityWindowDAO.logger.debug(" paw overlap partially in start ");
				}
				PlatformActivityWindowDAO.logger.debug("pawMap :"+pawMap);
				if (pawMap.get(newSatelliteName) != null) {
					PlatformActivityWindowDAO.logger.debug("pawMap not empty for  newSatelliteName:"+newSatelliteName);
					PlatformActivityWindowDAO.logger.debug("pawMap not empty for  newSatelliteName:"+pawMap.get(newSatelliteName));

					pawMap.get(newSatelliteName).add(pawData);
					PlatformActivityWindowDAO.logger.debug("pawMap not empty for  newSatelliteName after insert:"+pawMap.get(newSatelliteName));

				} else {
					PlatformActivityWindowDAO.logger.debug("pawMap is empty for  newSatelliteName:"+newSatelliteName);
					PlatformActivityWindowDAO.logger.debug("pawMap is empty for  newSatelliteName:"+pawMap.get(newSatelliteName));
					ArrayList<PlatformActivityWindowBean> currentSatellitePawList = new ArrayList<>();

					currentSatellitePawList.add(pawData);
					pawMap.put(newSatelliteName, currentSatellitePawList);
					PlatformActivityWindowDAO.logger.debug("pawMap is empty for  newSatelliteName:"+pawMap.get(newSatelliteName));

				}
				PlatformActivityWindowDAO.logger.debug("pawMap :"+pawMap);

//					currentSatelliteName = newSatelliteName;
//					pawMap.put(currentSatelliteName, currentSatellitePawList);
//					currentSatellitePawList = new ArrayList<>();
			}
		} // end try
		catch (Exception e) {
			this.tm.critical(EventType.SOFTWARE_EVENT, "Errore in SELECT from GSIF PAW: ", e.getMessage());
			// rethrow
			try {
				throw e;
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} // end catch
		finally {
			// close rs set
			// close stm
			if (rs != null) {
				rs.close();
			}
			st.close();
			con.close();
		} // end finally
			// return map
		return pawMap;
	}// end method

	/**
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param finalEpoch
	 * @throws Exception
	 */
	public void deletePawData(double finalEpoch) throws Exception {
		if (con == null || con.isClosed()) {
			con = super.initConnection();
		}
		StringBuffer sql = new StringBuffer();
		sql.append("DELETE FROM GSIF_PAW");
		sql.append(" WHERE");
		sql.append(" ACTIVITY_STOP_TIME <= ?");

		PreparedStatement st = con.prepareStatement(sql.toString());

		try {
			// execute stm
			st.setDouble(1, finalEpoch);
			st.executeUpdate();
		} // end try
		catch (SQLException e) {
			// log
			// throw
			this.tm.critical(EventType.SOFTWARE_EVENT, "Errore in SELECT from GSIF PAW: ", e.getMessage());

			throw e;

		} // end catch
		finally {
			// stm close
			st.close();
			con.close();
		} // end finally
	}// end method

	public List<PlatformActivityWindowBean> getPawsList(int satId, double start, double stop) {
		// map to be returned
		List<PlatformActivityWindowBean> pawList = new ArrayList<PlatformActivityWindowBean>();

				// maxy query

				/*
				 * String query =
				 * "SELECT SATELLITE.ID_SATELLITE, SATELLITE.SATELLITE_NAME, GSIF_PAW.ACTIVITY_TYPE, "
				 * +
				 * "GSIF_PAW.ACTIVITY_ID, GSIF_PAW.ACTIVITY_START_TIME, GSIF_PAW.ACTIVITY_STOP_TIME, GSIF_PAW.DEFERRABLE_FLAG "
				 * +
				 * " from GSIF_PAW inner join SATELLITE on GSIF_PAW.SATELLITE = SATELLITE.ID_SATELLITE where "
				 * + " (GSIF_PAW.ACTIVITY_START_TIME <= " + start +
				 * " and GSIF_PAW.ACTIVITY_STOP_TIME >=" + stop + ") OR" +
				 * " (GSIF_PAW.ACTIVITY_START_TIME >= " + start +
				 * " and GSIF_PAW.ACTIVITY_START_TIME <=" + stop + ")" +
				 * " order by SATELLITE.SATELLITE_NAME, GSIF_PAW.ACTIVITY_START_TIME  ";
				 * 
				 * 
				 */
				String query = "SELECT SATELLITE.ID_SATELLITE, SATELLITE.SATELLITE_NAME, GSIF_PAW.ACTIVITY_TYPE, "
						+ "GSIF_PAW.ACTIVITY_ID, GSIF_PAW.ACTIVITY_START_TIME, GSIF_PAW.ACTIVITY_STOP_TIME, GSIF_PAW.DEFERRABLE_FLAG "
						+ " from GSIF_PAW inner join SATELLITE on GSIF_PAW.SATELLITE = SATELLITE.ID_SATELLITE where "
						+ " (GSIF_PAW.ACTIVITY_START_TIME <= " + stop + " and GSIF_PAW.ACTIVITY_STOP_TIME >=" + start + ")"
						+ " order by SATELLITE.SATELLITE_NAME, GSIF_PAW.ACTIVITY_START_TIME  ";

				PlatformActivityWindowDAO.logger.debug(" getPaws:  " + query);
				PlatformActivityWindowDAO.logger.debug(" with initial epoch:  " + DateUtils.fromCSKDateToDateTime(start));
				PlatformActivityWindowDAO.logger.debug(" and final epoch :  " + DateUtils.fromCSKDateToDateTime(stop));

				PreparedStatement st = null;
				ResultSet rs = null;

				String newSatelliteName = "";
				PlatformActivityWindowBean pawData;

				ArrayList<PlatformActivityWindowBean> currentSatellitePawList = new ArrayList<>();

				try {
					if (con == null || con.isClosed()) {
						con = super.initConnection();
					}
					st = this.con.prepareStatement(query);
					// exeuting query
					rs = st.executeQuery();

					// for each row in result set
					while (rs.next()) {
						// retrieve info on paw
						newSatelliteName = rs.getString(2);

						pawData = new PlatformActivityWindowBean();

						pawData.setSatId(rs.getInt(1));
						pawData.setSatName(rs.getString(2));

						pawData.setActivityType(rs.getString(3));
						pawData.setActivityId(rs.getLong(4));
						pawData.setActivityStartTime(rs.getDouble(5));
						pawData.setActivityStopTime(rs.getDouble(6));
						// check if dererreable
						if (rs.getInt(7) == DataManagerConstants.DEFERRABLE_FLAG_TRUE) {
							pawData.setDeferrableFlag(true);
						} // end if
						else {
							pawData.setDeferrableFlag(false);
						} // end else
						PlatformActivityWindowDAO.logger.debug("for paw :  " + pawData);

						PlatformActivityWindowDAO.logger.debug(" for paw startTime :  " + pawData.getActivityStartTime());
						PlatformActivityWindowDAO.logger.debug(" for paw startTime as Date :  "
								+ DateUtils.fromCSKDateToDateTime(pawData.getActivityStartTime()));

						PlatformActivityWindowDAO.logger.debug(" for paw stopTime :  " + pawData.getActivityStopTime());
						PlatformActivityWindowDAO.logger.debug(" for paw stopTime as Date :  "
								+ DateUtils.fromCSKDateToDateTime(pawData.getActivityStopTime()));

						if (pawData.getActivityStartTime() < start && pawData.getActivityStopTime() > stop) {
							PlatformActivityWindowDAO.logger.debug(" visibility period totally included inside the paw ");

						} else if (pawData.getActivityStopTime() > stop && pawData.getActivityStartTime() > start) {
							PlatformActivityWindowDAO.logger.debug(" paw overlap partially in stop ");
						} else if (pawData.getActivityStartTime() >= start && pawData.getActivityStopTime() <= stop) {
							PlatformActivityWindowDAO.logger.debug(" paw totally included inside the visibility period");

						} else {
							PlatformActivityWindowDAO.logger.debug(" paw overlap partially in start ");
						}

							pawList.add(pawData);
						}
				} // end try
				catch (Exception e) {
					this.tm.critical(EventType.SOFTWARE_EVENT, "Errore in SELECT from GSIF PAW: ", e.getMessage());
					// rethrow
					try {
						throw e;
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				} // end catch
				finally {
					try {
					// close rs set
					// close stm
					if (rs != null) {
					
							rs.close();

					}
					st.close();
					con.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} // end finally
					// return map
				return pawList;
	}

}// end class
