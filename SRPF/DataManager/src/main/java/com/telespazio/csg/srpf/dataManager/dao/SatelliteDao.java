/**
*
* MODULE FILE NAME:	SatelliteDao.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Define the Satellite  Data Abstract Model for low level DB access
*
* PURPOSE:			Used for DB data
*
* CREATION DATE:	01-02-2016
*
* AUTHORS:			Abed Alissa
*
* DESIGN ISSUE:		2.0
*
* INTERFACES:
*
* SUBORDINATES:
*
* MODIFICATION HISTORY:
*
*             Date                |  Name      |   New ver.    | Description
* --------------------------+------------+----------------+-------------------------------
* 10-10-2017 | Amedeo Bancone  |2.0 | added getBeamsSatellite one shot query
* 									  getFileNameForOrbitData
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.dataManager.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.telespazio.csg.srpf.dataManager.GenericDAO;
import com.telespazio.csg.srpf.dataManager.bean.BeamBean;
import com.telespazio.csg.srpf.dataManager.bean.SatelliteBean;
import com.telespazio.csg.srpf.dataManager.bean.SensorModeBean;
import com.telespazio.csg.srpf.dataManager.util.DataManagerConstants;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.utils.DateUtils;

/**
 * Define the Satellite Data Abstract Model for low level DB access
 * 
 * @author Abed Alissa
 * @version 2.0
 *
 */
public class SatelliteDao extends GenericDAO {

	TraceManager tm = new TraceManager();
	static final Logger logger = LogManager.getLogger(SatelliteDao.class.getName());

	/**
	 * default constructor
	 * 
	 * @throws NamingException
	 * @throws Exception
	 */
	public SatelliteDao() throws NamingException, Exception {
		super();
		// TODO Auto-generated constructor stub
	}// end method

	/**
	 * @param d
	 * @throws NamingException
	 * @throws Exception
	 */
	public SatelliteDao(GenericDAO d) throws NamingException, Exception {
		super(d);
		// TODO Auto-generated constructor stub
	}// end method

	/**
	 * Retrun a sat bean given its name
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @since 2016-1-20
	 * @param SatelliteName
	 * @return satellite bean
	 * @throws Exception
	 */
	public SatelliteBean getDatiSatellite(String SatelliteName) throws Exception {
		SatelliteBean p = null;
		String query = "SELECT ID_SATELLITE, SATELLITE_NAME, MISSION, IS_ENABLED  , ID_ALLOWED_LOOK_SIDE,TRACK_OFFSET,  MISSION_NAME  from SATELLITE s, MISSION m where   SATELLITE_NAME = "
				+ "'" + SatelliteName + "'" + " and s.MISSION=m.ID_MISSION ";

		try {

			// ManagerLogger.logDebug(this, query);
			this.tm.debug("Inside method getDatiSatellite ");
			PreparedStatement st = null;
			ResultSet rs = null;

			try {
				if (con == null || con.isClosed()) {

					con = initConnection();
					
					}
				st =con.prepareStatement(query);
				
				// exeute query
				rs = st.executeQuery();
				// for each row rertieve data
				while (rs.next()) {
					p = new SatelliteBean();
					p.setIdSatellite(rs.getInt("ID_SATELLITE"));
					p.setSatelliteName(rs.getString("SATELLITE_NAME"));
					p.setIsEnabled(rs.getInt("IS_ENABLED"));
					p.setIdAllowedLookSide(rs.getInt("ID_ALLOWED_LOOK_SIDE"));
					p.setMissionName(rs.getString("MISSION_NAME"));
					p.setIdMission(rs.getInt("MISSION"));
					p.setTrackOffset(rs.getInt("TRACK_OFFSET"));
				} // end while

			} // end try
			finally {
				// close rs set
				if (rs != null) {
					rs.close();
				}
				// close statement
				st.close();
				con.close();
			} // end if
		} // end finally
		catch (Exception e)

		{
			// log
			logger.error(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
					e.getMessage());
			// rethow
			throw e;
		} // end catch
		return p;
	}// end method

	/**
	 * Return a map of SatelliteBean vs BeamBean, #@author Amedeo Bancone
	 * 
	 * @param missionNameOrFirstSensorMode or first sensor mode in case of combined
	 *                                     request
	 * @param sensorModeName
	 * @param isCombined
	 * @return map holding the satellite vs beams
	 * @throws Exception
	 */
	public Map<SatelliteBean, List<BeamBean>> getBeamsSatellite(String missionNameOrFirstSensorMode,
			String sensorModeName, boolean isCombined) throws Exception {
		Map<SatelliteBean, List<BeamBean>> beamMap = new TreeMap<>();
		SatelliteDao.logger
				.debug("Inside method getBeamsSatellite Map<SatelliteBean, List<BeamBean>> of satelliteDao ");

		String querona;
		if (!isCombined) {
			// SatelliteDao.logger.debug("getBeamsSatellite : not a combined request, input
			// parameters are : " + missionNameOrFirstSensorMode + " as missionName and " +
			// sensorModeName + " as sensor mode name");

			// Query to be used if comnined
			// request
			querona = "SELECT SATELLITE.SATELLITE_NAME, MISSION.MISSION_NAME, SATELLITE.IS_ENABLED, SATELLITE.ID_ALLOWED_LOOK_SIDE, SATELLITE.ID_SATELLITE ,MISSION.ID_MISSION,SATELLITE.TRACK_OFFSET, SENSOR_MODE.ID_SENSOR_MODE, BEAM.ID_BEAM, BEAM.BEAM_NAME, BEAM.IS_ENABLED, BEAM.NEAR_OFF_NADIR, BEAM.FAR_OFF_NADIR, BEAM.SW_DIM1, BEAM.SW_DIM2, SENSOR_MODE.IS_SPOT_LIGHT, SENSOR_MODE.SENSOR_MODE_NAME, BEAM.DTO_MIN_DURATION, BEAM.DTO_MAX_DURATION, BEAM.RES_TIME, BEAM.DTO_DURATION_SQUARED from SATELLITE,"
					+ " MISSION," + " SAT_BEAM_ASSOCIATION, " + " SENSOR_MODE," + " BEAM "
					+ " WHERE SATELLITE.MISSION = MISSION.ID_MISSION" + " AND MISSION.MISSION_NAME= " + "'"
					+ missionNameOrFirstSensorMode + "'" + " AND "
					+ " BEAM.SENSOR_MODE=SENSOR_MODE.ID_SENSOR_MODE AND SENSOR_MODE.SENSOR_MODE_NAME=" + "'"
					+ sensorModeName + "'"
					+ " AND BEAM.ID_BEAM=SAT_BEAM_ASSOCIATION.BEAM AND SATELLITE.ID_SATELLITE=SAT_BEAM_ASSOCIATION.SATELLITE order by SATELLITE.SATELLITE_NAME, BEAM.BEAM_NAME ";

		} // end if

		/*
		 * FRANCESCA : questa parte di codice dell'else non viene mai utilizzata,
		 * rimuovibile
		 */
		else {

			SatelliteDao.logger.debug(
					"getBeamsSatellite : combined request, input parameters are : " + missionNameOrFirstSensorMode
							+ " as first sensor mode name and " + sensorModeName + " as second sensor mode name");

			// query
			// used for single mission
			querona = "SELECT SAT_NAME, MISSION_NAME, ENABLED,  ALLOWED_SIDE, SAT_ID, MISSION_ID, TOFF, SMODE_ID,"
					+ " BEAM_ID, BNAME, IS_ENABLED_BEAM,NEAR_OFF_NADIR,FAR_OFF_NADIR,"
					+ " SW_DIM1, SW_DIM2, IS_SPOT_LIGHT, SENSOR_MODE_NAME, DTO_MIN_DURATION, DTO_MAX_DURATION, REST_TIME "
					+ " from (SELECT SATELLITE.SATELLITE_NAME as SAT_NAME, MISSION.MISSION_NAME as MISSION_NAME,SATELLITE.IS_ENABLED as ENABLED,"
					+ " SATELLITE.ID_ALLOWED_LOOK_SIDE AS ALLOWED_SIDE, SATELLITE.ID_SATELLITE as SAT_ID, MISSION.ID_MISSION as MISSION_ID, SATELLITE.TRACK_OFFSET as TOFF "
					+ "from SATELLITE INNER join MISSION on SATELLITE.MISSION = MISSION.ID_MISSION  " + ") inner join"
					+ "( SELECT SMODE_ID, BEAM_ID,BNAME,"
					+ " SAT_BEAM_ASSOCIATION.IS_ENABLED as IS_ENABLED_BEAM, BEAM.NEAR_OFF_NADIR as NEAR_OFF_NADIR, "
					+ "BEAM.FAR_OFF_NADIR as FAR_OFF_NADIR, "
					+ " SW_DIM1, SW_DIM2, IS_SPOT_LIGHT, SENSOR_MODE_NAME, DTO_MIN_DURATION,DTO_MAX_DURATION, REST_TIME, "
					+ " SAT_BEAM_ASSOCIATION.SATELLITE as ASSOCIATED_SAT " + "from( SELECT " + " "
					+ "SENSOR_MODE.ID_SENSOR_MODE as SMODE_ID, " + "BEAM.ID_BEAM as BEAM_ID, "
					+ "BEAM.BEAM_NAME as BNAME, " + "BEAM.SW_DIM1 as SW_DIM1," + " " + "BEAM.SW_DIM2 as SW_DIM2, "
					+ "SENSOR_MODE.IS_SPOT_LIGHT as IS_SPOT_LIGHT, "
					+ "SENSOR_MODE.SENSOR_MODE_NAME as SENSOR_MODE_NAME," + " "
					+ "BEAM.DTO_MIN_DURATION as DTO_MIN_DURATION, " + "BEAM.DTO_MAX_DURATION as DTO_MAX_DURATION, "
					+ " " + "BEAM.RES_TIME as REST_TIME , " + "BEAM.DTO_DURATION_SQUARED AS DTO_DURATION_SQUARED "
					+ "from BEAM INNER join SENSOR_MODE on BEAM.SENSOR_MODE=SENSOR_MODE.ID_SENSOR_MODE "
					+ " where SENSOR_MODE.SENSOR_MODE_NAME=" + "'" + missionNameOrFirstSensorMode
					+ "' OR SENSOR_MODE.SENSOR_MODE_NAME=" + "'" + sensorModeName + "'"
					+ " ) Inner join SAT_BEAM_ASSOCIATION on BEAM_ID=SAT_BEAM_ASSOCIATION.BEAM ) "
					+ " on SAT_ID=ASSOCIATED_SAT order by SAT_NAME, BNAME";

			// SatelliteDao.logger.debug("querona : " +querona);
		} // end else
			// the above queries refer to:
			// rhe first unique query for both mission
			// the second on single mission
			// query
		try {
			if (con == null || con.isClosed()) {

				con = initConnection();
				
				}
			// ManagerLogger.logDebug(this, query);
			PreparedStatement st = con.prepareStatement(querona);
			ResultSet rs = null;

			try {
				// exeuting query
				rs = st.executeQuery();

				SatelliteBean satBean;

				String currentSatName = "";
				String newSatName;
				ArrayList<BeamBean> beamList = new ArrayList<>();
				BeamBean currentBeam;
				// retrievinbg data
				while (rs.next()) {

					newSatName = rs.getString(1);

					if (!currentSatName.equals(newSatName)) { // filling satellite bean
						currentSatName = newSatName;
						beamList = new ArrayList<>();
						satBean = new SatelliteBean();
						satBean.setSatelliteName(rs.getString(1));
						satBean.setMissionName(rs.getString(2));
						satBean.setIsEnabled(rs.getInt(3));
						satBean.setIdAllowedLookSide(rs.getInt(4));
						satBean.setIdSatellite(rs.getInt(5));
						satBean.setIdMission(rs.getInt(6));
						satBean.setTrackOffset(rs.getInt(7));
						if (satBean.getIdAllowedLookSide() == DataManagerConstants.ID_ALLOWED_lOOK_RIGHT) {// checking
																											// for
																											// allowed
																											// angles
							satBean.setAllowedLookSide(DataManagerConstants.ALLOWED_lOOK_RIGHT);
						} // end if
						else if (satBean.getIdAllowedLookSide() == DataManagerConstants.ID_ALLOWED_lOOK_LEFT) {
							satBean.setAllowedLookSide(DataManagerConstants.ALLOWED_lOOK_LEFT);
						} else if (satBean.getIdAllowedLookSide() == DataManagerConstants.ID_ALLOWED_lOOK_BOTH) {
							satBean.setAllowedLookSide(DataManagerConstants.ALLOWED_lOOK_BOTH);
						} // end else

						if (satBean.getIsEnabled() == DataManagerConstants.ENABLED) {
							// ad satellite onbly if enabled
							beamMap.put(satBean, beamList);
						} // end if

					} // end if

					/*
					 * "SAT_NAME," + " MISSION_NAME, " + "ENABLED,  " + "ALLOWED_SIDE, " +
					 * "SAT_ID, " + "MISSION_ID, " + "TOFF, "
					 */

					/*
					 * + "SMODE_ID," + " " + "BEAM_ID, " + "BNAME, " + "IS_ENABLED_BEAM," +
					 * "NEAR_OFF_NADIR," + "FAR_OFF_NADIR," + " " + "SW_DIM1, " + "SW_DIM2, " +
					 * "IS_SPOT_LIGHT, " + "SENSOR_MODE_NAME, " + "DTO_MIN_DURATION, " +
					 * "DTO_MAX_DURATION, " + "REST_TIME "
					 * 
					 */
					// filling beams
					currentBeam = new BeamBean();
					// set sensor mode
					currentBeam.setSensorMode(rs.getInt(8));
					// set satname
					currentBeam.setSatName(currentSatName);
					// set beam id
					currentBeam.setIdBeam(rs.getInt(9));
					// set beam name
					currentBeam.setBeamName(rs.getString(10));
					// set enabled
					currentBeam.setIsEnabled(rs.getInt(11));
					// set off nasir
					currentBeam.setNearOffNadirBeam(rs.getDouble(12));
					currentBeam.setFarOffNadirBeam(rs.getDouble(13));
					currentBeam.setNearOffNadir(currentBeam.getNearOffNadirBeam());
					currentBeam.setFarOffNadir(currentBeam.getFarOffNadirBeam());
					// set scene dimension
					currentBeam.setSwDim1(rs.getDouble(14));
					currentBeam.setSwDim2(rs.getDouble(15));
					// set if spot
					currentBeam.setSpotLight(rs.getBoolean(16));

					// setting snsor mode charachteristic
					currentBeam.setSensorModeName(rs.getString(17));

					currentBeam.setDtoMinDuration(rs.getInt(18));
					currentBeam.setDtoMaxDuration(rs.getInt(19));
					currentBeam.setResTime(rs.getInt(20));
					currentBeam.setDtoDurationSquared(rs.getInt(21));
					// SatelliteDao.logger.debug("beamList : "+currentBeam.toString());
					beamList.add(currentBeam);
					// at the end
					// info on sat
					// sat
					// and sensor mode are filled
				} // end while

			} // end try
			catch (Exception e) {
				DateUtils.getLogInfo(e, SatelliteDao.logger);
			} finally {
				// closing rs set
				if (rs != null) {
					rs.close();
				}
				// closing stm
				st.close();
				con.close();
			} // end if
		} // end finally
		catch (Exception e)

		{
			// log
			SatelliteDao.logger.error(EventType.SOFTWARE_EVENT,
					"Execution error of the query by prepared statement: " + querona, e.getMessage());

			DateUtils.getLogInfo(e, SatelliteDao.logger);
			// thorow
			throw e;
		} // end catch

		// retruning map
		return beamMap;
	}// end method

	/**
	 * return a list of beams given sat id and sensor mode id
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @since 2016-1-20
	 * @param idSensorMode
	 * @param idSatellite
	 * @return list of beam
	 * @throws Exception
	 */
	public ArrayList<BeamBean> getBeamsSatellite(int idSensorMode, int idSatellite) throws Exception

	{
		// Mega query
		String query = "SELECT SENSOR_MODE as \"Sensor Mode\", " + "s.SATELLITE_NAME AS \"Satellite Name\", "
				+ "b.ID_BEAM AS \"ID BEAM\", " + "b.BEAM_NAME AS \"Beam Name\"," + "b.IS_ENABLED AS \"Is Enabled\", "
				+ "b.NEAR_OFF_NADIR AS \"Near Off Nadir\" ," + "b.FAR_OFF_NADIR AS\" Far Off Nadir\"" + " , "
				+ "b.SW_DIM1 AS\" Sw Dim1\", " + "b.SW_DIM2 AS\" Sw Dim2\", " + "sm.IS_SPOT_LIGHT AS\"Spot Light\", "
				+ "sm.SENSOR_MODE_NAME AS\" Sensor Name\"  " + " , " + "b.DTO_MIN_DURATION AS\" Dto Min\", "
				+ "b.DTO_MAX_DURATION AS\" Dto Max\" , " + "b.RES_TIME AS\" Res Time\" , "
				+ "b.DTO_DURATION_SQUARED AS\" Duration Squared\" " + " from " + "BEAM b," + "SAT_BEAM_ASSOCIATION sb, "
				+ "satellite s, " + "SENSOR_MODE sm  " + ""
				+ "where b.ID_BEAM(+)=sb.BEAM  and sb.SATELLITE=s.ID_SATELLITE "
				+ " and b.SENSOR_MODE=sm.ID_SENSOR_MODE and s.ID_SATELLITE= " + idSatellite + " and b.SENSOR_MODE= "
				+ idSensorMode;
		//
		// log
		//
		// SatelliteDao.logger.debug("Inside method getBeamsSatellite " + query);
		PreparedStatement st = null;
		ResultSet rs = null;
		ArrayList<BeamBean> listaBeams = new ArrayList<>();

		BeamBean beamBean = null;
		try {
			if (con == null || con.isClosed()) {

				con = initConnection();
				
				}
			st = con.prepareStatement(query);
			// execute query
			rs = st.executeQuery();
			// for each row
			while (rs.next()) {
				// retrieve info
				beamBean = new BeamBean();
				beamBean.setSensorMode(rs.getInt(1));
				beamBean.setSatName(rs.getString(2));
				beamBean.setIdBeam(rs.getInt(3));
				beamBean.setBeamName(rs.getString(4));
				beamBean.setIsEnabled(rs.getInt(5));
				beamBean.setNearOffNadirBeam(rs.getDouble(6));
				beamBean.setFarOffNadirBeam(rs.getDouble(7));
				beamBean.setNearOffNadir(beamBean.getNearOffNadirBeam());
				beamBean.setFarOffNadir(beamBean.getFarOffNadirBeam());

				// dimension info
				beamBean.setSwDim1(rs.getDouble(8));
				beamBean.setSwDim2(rs.getDouble(9));
				beamBean.setSpotLight(rs.getBoolean(10));
				beamBean.setSensorModeName(rs.getString(11));
				// timong info
				beamBean.setDtoMinDuration(rs.getInt(12));
				beamBean.setDtoMaxDuration(rs.getInt(13));
				beamBean.setResTime(rs.getInt(14));
				beamBean.setDtoDurationSquared(rs.getInt(15));

				listaBeams.add(beamBean);
			} // end while
		} // end try
		catch (SQLException e) {
			// log
			logger.error(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
					e.getMessage());
			// rethrow
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
		} // end finally
		return listaBeams;
	}// end method

	/**
	 * List of satellites given the related sensor mode
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param idSensorMode
	 * @param idMission
	 * @return Satellite Bean list
	 * @throws Exception
	 */
	public ArrayList<SatelliteBean> getSatellitesPerSensorMode(int idSensorMode, int idMission) throws Exception {

		/*
		 * String query =
		 * "SELECT  distinct(s.SATELLITE_NAME) AS \"Satellite Name\", MISSION_NAME as \"Mission Name\",  s.IS_ENABLED AS \"Is Enabled\", s.ID_ALLOWED_LOOK_SIDE AS \"Id Allowed Look Side\", s.ID_SATELLITE AS \"Id Satellite\"   from BEAM b,SAT_BEAM_ASSOCIATION sb, SATELLITE s, MISSION m  where b.ID_BEAM(+)=sb.BEAM  and sb.SATELLITE=s.ID_SATELLITE  "
		 * + " and b.SENSOR_MODE= " + idSensorMode + " and m.ID_MISSION= s.MISSION" +
		 * " and  m.ID_MISSION= " + idMission + " order by s.SATELLITE_NAME";
		 */
		String query = "SELECT  distinct(s.SATELLITE_NAME), " + "MISSION_NAME,  " + "s.IS_ENABLED, "
				+ "s.ID_ALLOWED_LOOK_SIDE, " + "s.ID_SATELLITE, " + "s.TRACK_OFFSET  " + "" + "from BEAM b,"
				+ "SAT_BEAM_ASSOCIATION sb, " + "SATELLITE s, " + "MISSION m  " + ""
				+ "where b.ID_BEAM(+)=sb.BEAM  and " + "sb.SATELLITE=s.ID_SATELLITE  " + " and " + "b.SENSOR_MODE= "
				+ idSensorMode + " and " + "m.ID_MISSION= s.MISSION" + " and  " + "m.ID_MISSION= " + idMission
				+ " order by s.SATELLITE_NAME";

		// ManagerLogger.logDebug(this, query);
		// this.tm.debug("Inside method getSatellitesPerSensorMode " + query);
		PreparedStatement st = null;
		ResultSet rs = null;
		ArrayList<SatelliteBean> satsList = new ArrayList<>();

		SatelliteBean satBean = null;
		try {
			if (con == null || con.isClosed()) {

				con = initConnection();
				
				}
			st = con.prepareStatement(query);
			// executing query
			rs = st.executeQuery();
			// getting data
			while (rs.next()) {
				satBean = new SatelliteBean();

				satBean.setSatelliteName(rs.getString(1));
				satBean.setMissionName(rs.getString(2));
				satBean.setIsEnabled(rs.getInt(3));
				satBean.setIdAllowedLookSide(rs.getInt(4));
				satBean.setIdSatellite(rs.getInt(5));
				satBean.setTrackOffset(rs.getInt(6));
				if (satBean.getIdAllowedLookSide() == DataManagerConstants.ID_ALLOWED_lOOK_RIGHT) {
					satBean.setAllowedLookSide(DataManagerConstants.ALLOWED_lOOK_RIGHT);
				} // end if
				else if (satBean.getIdAllowedLookSide() == DataManagerConstants.ID_ALLOWED_lOOK_LEFT) {
					satBean.setAllowedLookSide(DataManagerConstants.ALLOWED_lOOK_LEFT);
				} // end else if
				else if (satBean.getIdAllowedLookSide() == DataManagerConstants.ID_ALLOWED_lOOK_BOTH) {
					satBean.setAllowedLookSide(DataManagerConstants.ALLOWED_lOOK_BOTH);
				} // end else

				if (satBean.getIsEnabled() == DataManagerConstants.ENABLED) {
					satsList.add(satBean);
				} // end if

			} // end while
		} // end try
		catch (SQLException e) {

			logger.error(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
					e.getMessage());
			// retheow
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
		} // end finally
		return satsList;
	}// end method

	/**
	 * 
	 * @return The list of satellite list bean
	 * @throws Exception
	 */
	public ArrayList<SatelliteBean> getSatelliteBeanList() throws Exception

	{
		// query
		String query = "SELECT SATELLITE.SATELLITE_NAME , MISSION.MISSION_NAME, SATELLITE.IS_ENABLED, "
				+ "SATELLITE.ID_ALLOWED_LOOK_SIDE, SATELLITE.ID_SATELLITE, SATELLITE.TRACK_OFFSET "
				+ " from  SATELLITE join MISSION on SATELLITE.MISSION=MISSION.ID_MISSION";

		// "SELECT BEAM.BEAM_NAME from BEAM join SENSOR_MODE on
		// BEAM.SENSOR_MODE=SENSOR_MODE.ID_SENSOR_MODE and
		// SENSOR_MODE.IS_SPOT_LIGHT=1";

		// ManagerLogger.logDebug(this, query);
		// this.tm.debug("Inside method getSatelliteBeanList " + query);
		PreparedStatement st = null;
		ResultSet rs = null;
		ArrayList<SatelliteBean> satsList = new ArrayList<>();

		SatelliteBean satBean = null;
		try {
			if (con == null || con.isClosed()) {

				con = initConnection();
				
				}
			st = con.prepareStatement(query);
			// executing query
			rs = st.executeQuery();
			// getting results
			while (rs.next()) {
				satBean = new SatelliteBean();

				satBean.setSatelliteName(rs.getString(1));
				satBean.setMissionName(rs.getString(2));
				satBean.setIsEnabled(rs.getInt(3));
				satBean.setIdAllowedLookSide(rs.getInt(4));
				satBean.setIdSatellite(rs.getInt(5));
				satBean.setTrackOffset(rs.getInt(6));
				if (satBean.getIdAllowedLookSide() == DataManagerConstants.ID_ALLOWED_lOOK_RIGHT) {// check angles
					satBean.setAllowedLookSide(DataManagerConstants.ALLOWED_lOOK_RIGHT);
				} // end if
				else if (satBean.getIdAllowedLookSide() == DataManagerConstants.ID_ALLOWED_lOOK_LEFT) {
					satBean.setAllowedLookSide(DataManagerConstants.ALLOWED_lOOK_LEFT);
				} // end else if
				else if (satBean.getIdAllowedLookSide() == DataManagerConstants.ID_ALLOWED_lOOK_BOTH) {
					satBean.setAllowedLookSide(DataManagerConstants.ALLOWED_lOOK_BOTH);
				} // end else

				if (satBean.getIsEnabled() == DataManagerConstants.ENABLED) {// chech if enables
					satsList.add(satBean);
				} // end if

			} // end while
		} // end try
		catch (SQLException e) {

			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
					e.getMessage());
			// rethrow
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
		} // end finally
		return satsList;

	}// end method

	/**
	 * list of satellites
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param idSensorMode
	 * @param idMission
	 * @return list of satellite
	 * @throws Exception
	 */
	public ArrayList<SatelliteBean> getSatellitesPerSensorModeConfiguration(int idSensorMode, int idMission)
			throws Exception {

		/*
		 * String query =
		 * "SELECT  distinct(s.SATELLITE_NAME) AS \"Satellite Name\", MISSION_NAME as \"Mission Name\",  s.IS_ENABLED AS \"Is Enabled\", s.ID_ALLOWED_LOOK_SIDE AS \"Id Allowed Look Side\", s.ID_SATELLITE AS \"Id Satellite\"   from BEAM b,SAT_BEAM_ASSOCIATION sb, SATELLITE s, MISSION m  where b.ID_BEAM(+)=sb.BEAM  and sb.SATELLITE=s.ID_SATELLITE  "
		 * + " and b.SENSOR_MODE= " + idSensorMode + " and m.ID_MISSION= s.MISSION" +
		 * " and  m.ID_MISSION= " + idMission + " order by s.SATELLITE_NAME";
		 */
		String query = "SELECT  distinct(s.SATELLITE_NAME) , MISSION_NAME ,  s.IS_ENABLED , s.ID_ALLOWED_LOOK_SIDE , s.ID_SATELLITE,s.TRACK_OFFSET  "
				+ " from BEAM b,SAT_BEAM_ASSOCIATION sb, SATELLITE s, MISSION m  where b.ID_BEAM(+)=sb.BEAM  and sb.SATELLITE=s.ID_SATELLITE  "
				+ " and b.SENSOR_MODE= " + idSensorMode + " and m.ID_MISSION= s.MISSION" + " and  m.ID_MISSION= "
				+ idMission + " order by s.SATELLITE_NAME";

		// ManagerLogger.logDebug(this, query);
		// this.tm.debug("Inside method getSatellitesPerSensorModeConfiguration " +
		// query);
		PreparedStatement st = null;
		ResultSet rs = null;
		ArrayList<SatelliteBean> satsList = new ArrayList<>();

		SatelliteBean satBean = null;
		try {
			if (con == null || con.isClosed()) {

				con = initConnection();
				
				}
			st =con.prepareStatement(query);
			// executing query
			rs = st.executeQuery();
			// getting data
			while (rs.next()) {
				satBean = new SatelliteBean();

				satBean.setSatelliteName(rs.getString(1));
				satBean.setMissionName(rs.getString(2));
				satBean.setIsEnabled(rs.getInt(3));
				satBean.setIdAllowedLookSide(rs.getInt(4));
				satBean.setIdSatellite(rs.getInt(5));
				satBean.setTrackOffset(rs.getInt(6));
				if (satBean.getIdAllowedLookSide() == DataManagerConstants.ID_ALLOWED_lOOK_RIGHT) {// check angle
					satBean.setAllowedLookSide(DataManagerConstants.ALLOWED_lOOK_RIGHT);
				} // end if
				else if (satBean.getIdAllowedLookSide() == DataManagerConstants.ID_ALLOWED_lOOK_LEFT) {// check angle
					satBean.setAllowedLookSide(DataManagerConstants.ALLOWED_lOOK_LEFT);
				} // else if
				else if (satBean.getIdAllowedLookSide() == DataManagerConstants.ID_ALLOWED_lOOK_BOTH) { // check angle
					satBean.setAllowedLookSide(DataManagerConstants.ALLOWED_lOOK_BOTH);
				} // end else

				satsList.add(satBean);

			} // end while
		} // end try
		catch (SQLException e) {

			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
					e.getMessage());
			// rethrow
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
		} // end finally
		return satsList;
	}// end method

	/**
	 * id of sensor mode given its name
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @since 2016-1-20
	 * @param sensoreModeName
	 * @return id sensor mode
	 * @throws Exception
	 */
	public int getIdSensorMode(String sensoreModeName) throws Exception {

		int idSensorMode = 0;
		// query string
		String query = "SELECT ID_SENSOR_MODE  from SENSOR_MODE where   SENSOR_MODE_NAME = " + "'" + sensoreModeName
				+ "'";
		// this.tm.debug("Inside method getIdSensorMode " + query);
		PreparedStatement st =null;
		ResultSet rs = null;
		try {
			if (con == null || con.isClosed()) {

				con = initConnection();
				
				}
			st =con.prepareStatement(query);
			// ManagerLogger.logDebug(this, query);
			// executing query
			rs = st.executeQuery();
			// getting data
			while (rs.next()) {

				idSensorMode = rs.getInt("ID_SENSOR_MODE");

			} // end

		} catch (Exception e)

		{

			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
					e.getMessage());
			// rethrow
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
		} // end finally

		return idSensorMode;
	}// end method

	/**
	 * Returns a list of sat id belonging the given mission
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @since 2016-1-20
	 * @param idMission
	 * @return list of id
	 * @throws Exception
	 */
	public ArrayList<Integer> getIdSatellitesMission(int idMission) throws Exception {
		// query
		String query = "SELECT ID_SATELLITE FROM SATELLITE where MISSION = " + "'" + idMission + "'";
		//

		// this.tm.debug("Inside method getIdSatellitesMission " + query);
		PreparedStatement st = null;
		ResultSet rs = null;
		ArrayList<Integer> listaIdSatellites = new ArrayList<Integer>();
		int idSatellite = 0;
		// try
		try {
			if (con == null || con.isClosed()) {

				con = initConnection();
				
				}
			st =con.prepareStatement(query);
			// exeuting query
			rs = st.executeQuery();
			// getting result
			while (rs.next()) {

				idSatellite = rs.getInt(1);

				listaIdSatellites.add(idSatellite);
			} // end while
		} // end try
		catch (SQLException e) {
			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
					e.getMessage());

		} // end catch
		finally {
			// close rs set
			if (rs != null) {
				rs.close();
			}
			// close stm
			st.close();
			con.close();
		} // end
		return listaIdSatellites;
	}// end method

	/**
	 * list of sensor mode
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @date 2016-1-20
	 * @return a list of sensor mode
	 * @throws Exception
	 */
	public ArrayList<Integer> getIdSensorsMode() throws Exception {

		ArrayList<Integer> listaIdSensorsMode = new ArrayList<Integer>();
		String query = "SELECT ID_SENSOR_MODE  from SENSOR_MODE  ";
		int idSensorMode = 0;
		try {
			if (con == null || con.isClosed()) {

				con = initConnection();
				
				}
			this.tm.debug("Inside method getIdSensorsMode " + query);
			PreparedStatement st = con.prepareStatement(query);
			ResultSet rs = null;
			// try
			try {
				// execute query
				rs = st.executeQuery();
				// getting result
				while (rs.next()) {

					idSensorMode = rs.getInt("ID_SENSOR_MODE");
					listaIdSensorsMode.add(idSensorMode);
				} // end

			} // end try
			finally {
				// close rs set
				if (rs != null) {
					rs.close();
				}
				// close stm
				st.close();
				con.close();
			} // end finally
		} // end try
		catch (Exception e)

		{

			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
					e.getMessage());
			// rethrow
			throw e;

		} // end catch
		return listaIdSensorsMode;
	}// end method

	/**
	 * List of sensor mode
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @date 2016-1-20
	 * @return sensor mode list
	 * @throws Exception
	 */
	public ArrayList<SensorModeBean> getSensorsMode() throws Exception {

		ArrayList<SensorModeBean> listSensorsMode = null;
		// query string
		String query = "SELECT ID_SENSOR_MODE, SENSOR_MODE_NAME,IS_SPOT_LIGHT   from SENSOR_MODE  ";
		// String sensorModeName = null;
		PreparedStatement st = null;
		try {
			if (con == null || con.isClosed()) {

				con = initConnection();
				
				}
			this.tm.debug("Inside method getSensorsMode " + query);
			st = con.prepareStatement(query);
			ResultSet rs = null;
			SensorModeBean sensorModeBean = null;
			try {
				// executing query
				rs = st.executeQuery();
				listSensorsMode = new ArrayList<SensorModeBean>();
				// getting data
				while (rs.next()) {
					sensorModeBean = new SensorModeBean();

					sensorModeBean.setIdsensorMode(rs.getInt("ID_SENSOR_MODE"));
					sensorModeBean.setSensorModeName(rs.getString("SENSOR_MODE_NAME"));

					if (rs.getInt("IS_SPOT_LIGHT") == 1) {
						sensorModeBean.setSpotLight(true);
					} // end if
					else {
						sensorModeBean.setSpotLight(false);
					} // end else

					listSensorsMode.add(sensorModeBean);
				} // end

			}
			// end try
			catch (Exception e)

			{

				this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
						e.getMessage());
				// rethrow
				throw e;

			} // end try
			finally {
				// close rs set
				if (rs != null) {
					rs.close();
				}
				// close stm
				st.close();
				con.close();
			} // end finally
		} // end try
		catch (Exception e)

		{
			// log
			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
					e.getMessage());
			// rethrow
			throw e;

		} // end catch
		return listSensorsMode;
	}// end method

	/**
	 * check if a satellite is in DB
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @date 2016-1-20
	 * @param idSatellite
	 * @return true if sat exist
	 * @throws Exception
	 */
	public boolean existSatelliteBeam(int idSatellite) throws Exception {
		boolean exist = false;
		ArrayList<Integer> values = new ArrayList<Integer>();
		StringBuffer sql = new StringBuffer();
		// building statement
		sql.append(" SELECT");
		sql.append(" *");
		sql.append(" FROM");
		sql.append(" SAT_BEAM_ASSOCIATION");
		sql.append(" WHERE");
		sql.append(" SATELLITE =?");
		this.tm.debug("Inside method existSatelliteBeam " + sql);
		PreparedStatement st = null;
		ResultSet rs = null;
		// try
		try {
			if (con == null || con.isClosed()) {

				con = initConnection();
				
				}
			st = con.prepareStatement(sql.toString());
			
			values.add(idSatellite);
			// executing query
			rs = findByPrepStmtSQL(sql.toString(), values);
			// retrievinbg result
			if (rs.next()) {
				exist = true;
			}
			// returning result
			return exist;
		}
		// end try
		// end try
		catch (Exception e)

		{

			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
					e.getMessage());
			// rethrow
			throw e;

		} finally {
			// close resut set
			if (rs != null) {
				rs.close();
			}

			// close statement
			st.close();
			con.close();
		} // end finally
	}// end method

	/**
	 * Return result set
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @date 2016-1-20
	 * @param inputQuery
	 * @param valori
	 * @return resuly set
	 * @throws SQLException
	 */
	public ResultSet findByPrepStmtSQL(String inputQuery, ArrayList<Integer> values) throws SQLException {
		PreparedStatement pst = null;
		ResultSet rs = null;

		try {
		if (con == null || con.isClosed()) {

			con = initConnection();
			
			}
		pst = con.prepareStatement(inputQuery);

		int puntiInterrogativi = new StringTokenizer(inputQuery, "?").countTokens() - 1;
		if (inputQuery.endsWith("?")) {
			puntiInterrogativi++;
		} // end
		for (int i = 0; i < puntiInterrogativi; i++) {
			if (values.get(i) != null) {
				pst.setObject(i + 1, values.get(i));
			} else {
				pst.setString(i + 1, null);
			} // end if
		} // end for

	
			// exeute query
			rs = pst.executeQuery();
		} // end try
		catch (Exception e) {
			// log
			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
					e.getMessage());
			// rethrow
			try {
				throw e;
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} // end catch

		con.close();
		// return result set
		return rs;
	}// end method

	/**
	 * return list of beams for exoprt DB purposes
	 * 
	 * @author Abed Alissa
	 * @param sensorMode
	 * @return list of beams
	 * @throws Exception
	 */
	public ArrayList<BeamBean> getBeamsExportConfiguration(int idSensorMode, int idSatellite) throws Exception {
		// mega query
		String query = "SELECT SENSOR_MODE as \"Sensor Mode\", " + "s.SATELLITE_NAME AS \"Satellite Name\", "
				+ "b.ID_BEAM AS \"ID BEAM\", " + "b.BEAM_NAME AS \"Beam Name\"" + ",  "
				+ "b.IS_ENABLED AS \"Is Enabled\", " + "b.NEAR_OFF_NADIR AS \"Near Off Nadir Beam\" ,"
				+ "b.FAR_OFF_NADIR AS\" Far Off Nadir Beam\" " + ", " + "b.SW_DIM1 AS\" Sw Dim1\", "
				+ "b.SW_DIM2 AS\" Sw Dim2\", " + "sm.IS_SPOT_LIGHT AS\"Spot Light\"  " + " , "
				+ "b.DTO_MIN_DURATION AS\" Dto Min\", " + "b.DTO_MAX_DURATION AS\" Dto Max\" , "
				+ "b.RES_TIME AS\" Res Time\", " + "b.DTO_DURATION_SQUARED AS\" Duration Squared\" " + " from "
				+ "BEAM b," + "SAT_BEAM_ASSOCIATION sb, " + "satellite s, " + "SENSOR_MODE sm  "
				+ "where b.ID_BEAM(+)=sb.BEAM  " + " and b.SENSOR_MODE=sm.ID_SENSOR_MODE"
				+ " and sb.SATELLITE=s.ID_SATELLITE and s.ID_SATELLITE= " + idSatellite + " and b.SENSOR_MODE= "
				+ idSensorMode;

		// ManagerLogger.logDebug(this, query);
		PreparedStatement st = null;
		ResultSet rs = null;
		ArrayList<BeamBean> listaBeams = new ArrayList<BeamBean>();
		this.tm.debug("Inside method getBeamsExportConfiguration " + query);
		BeamBean beamBean = null;
		try {
			
			if (con == null || con.isClosed()) {

				con = initConnection();
				
				}
			st = con.prepareStatement(query);
			// executing query
			rs = st.executeQuery();
			// for each row get
			// results
			while (rs.next()) {
				beamBean = new BeamBean();
				beamBean.setSensorMode(rs.getInt(1));
				beamBean.setSatName(rs.getString(2));
				beamBean.setIdBeam(rs.getInt(3));
				beamBean.setBeamName(rs.getString(4));
				beamBean.setIsEnabled(rs.getInt(5));
				beamBean.setNearOffNadirBeam(rs.getDouble(6));
				beamBean.setFarOffNadirBeam(rs.getDouble(7));
				beamBean.setSwDim1(rs.getDouble(10));
				beamBean.setSwDim2(rs.getDouble(11));
				// beamBean.setSpotLight(rs.getBoolean(12));
				// check if spotlight
				if (rs.getInt(12) == 1) {
					beamBean.setSpotLight(true);
				} // end if
				else {
					beamBean.setSpotLight(false);

				} // end else
					// get timing info
				beamBean.setDtoMinDuration(rs.getInt(13));
				beamBean.setDtoMaxDuration(rs.getInt(14));
				beamBean.setResTime(rs.getInt(15));
				beamBean.setDtoDurationSquared(rs.getInt(16));
				listaBeams.add(beamBean);
			} // end while
		} // end try
		catch (SQLException e) {
			// log
			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
					e.getMessage());
			// rethrow
			//
			// -
			throw e;

		} // end catch
		finally {
			// close result set
			if (rs != null) {
				rs.close();
			}
			// close stm
			st.close();
			con.close();
		} // end
			// returnin list
		return listaBeams;
	}// end method

	/**
	 * Mission id given its name
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @since 2016-1-20
	 * @param MissionName
	 * @return mission id
	 * @throws Exception
	 */
	public int getIdMission(String MissionName) throws Exception {
		if (con == null || con.isClosed()) {
//			System.out.println("connection was closed!");
			con = super.initConnection();
		}
		int idMission = 0;
		String query = "SELECT ID_MISSION  from MISSION where   MISSION_NAME = " + "'" + MissionName + "'";
		try {

			// ManagerLogger.logDebug(this, query);
			this.tm.debug("Inside method getIdMission " + query);
			PreparedStatement st = null;
			ResultSet rs = null;

			try {
				if (con == null || con.isClosed()) {

					con = initConnection();
					
					}
				st = con.prepareStatement(query);
				rs = st.executeQuery();

				while (rs.next()) {

					idMission = rs.getInt("ID_MISSION");

				} // end

			} // end
				// end try
			catch (Exception e)

			{

				this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
						e.getMessage());
				// rethrow
				throw e;

			} finally {
				// close rs set
				if (rs != null) {
					rs.close();
				}
				// close stm
				st.close();
				con.close();
			} // end
		} // end
		catch (Exception e)

		{
			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
					e.getMessage());
			// ManagerLogger.logError(this, "Execution error of the query by
			// prepared statement: " + query, e);
			throw e;

		} // end
		return idMission;
	}// end catch

	// Added for in memory DB management
	/**
	 * Return a list holding the path of orbital data
	 * 
	 * @author Amedeo Bancone
	 * @param satellite
	 * @return the obdata file name
	 * @throws Exception
	 */
	public ArrayList<String> getFileNameForOrbitData(String satellite) throws Exception {
		ArrayList<String> fileName = new ArrayList<>();
		// query string
		String queryString = "SELECT SATELLITE.SATELLITE_NAME,OBDATA_FILES.ODSTP, OBDATA_FILES.ODMTP, OBDATA_FILES.ODNOM,OBDATA_FILES.ODREF "
				+ "from SATELLITE  join OBDATA_FILES on  SATELLITE.ID_SATELLITE = OBDATA_FILES.ID_SATELLITE "
				+ "and SATELLITE.SATELLITE_NAME = '" + satellite + "'";
		con.setAutoCommit(false);
		// stm
		PreparedStatement st = null;
		ResultSet rs = null;
		// try
		try {
			if (con == null || con.isClosed()) {

				con = initConnection();
				
				}
			st = con.prepareStatement(queryString);
			this.tm.debug(st.toString());

			// executing query
			rs = st.executeQuery();

			// For each row
			// getting data
			while (rs.next()) {
				fileName.add(rs.getString(2));
				fileName.add(rs.getString(3));
				fileName.add(rs.getString(4));
				fileName.add(rs.getString(5));

			} // end while
		}
		// end try
		catch (Exception e)

		{

			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
					e.getMessage());
			// rethrow
			throw e;

		} finally {
			// close rs set
			if (rs != null) {
				rs.close();
			}
			// close stm
			st.close();
			con.close();
		} // end finally
		return fileName;
	} // end method

	/**
	 * 
	 * Return a a map holding the list of obdata path for each satellite
	 * 
	 * @author Amedeo Bancone
	 * @return a map holding the list of obdata path for each satellite
	 * @throws Exception
	 */
	public Map<String, ArrayList<String>> getFileNameForOrbitData() throws Exception {
		// list
		ArrayList<String> fileNameList;
		String satelliteName;
		// map to be returned
		TreeMap<String, ArrayList<String>> retMap = new TreeMap<>();
		// query string
		String queryString = "SELECT SATELLITE.SATELLITE_NAME,OBDATA_FILES.ODSTP, OBDATA_FILES.ODMTP, OBDATA_FILES.ODNOM,OBDATA_FILES.ODREF "
				+ "from SATELLITE  join OBDATA_FILES on  SATELLITE.ID_SATELLITE = OBDATA_FILES.ID_SATELLITE ";
		con.setAutoCommit(false);
		// statement
		PreparedStatement st = null;
		ResultSet rs = null;
		// try
		try {
			
			if (con == null || con.isClosed()) {

				con = initConnection();
				
				}
			st = con.prepareStatement(queryString);
			// executing query
			rs = st.executeQuery();
			// for each row
			while (rs.next()) {
				fileNameList = new ArrayList<>();
				satelliteName = rs.getString(1);
				fileNameList.add(rs.getString(2));
				fileNameList.add(rs.getString(3));
				fileNameList.add(rs.getString(4));
				fileNameList.add(rs.getString(5));
				// add to map
				retMap.put(satelliteName, fileNameList);

			} // end while
		} // end try
		catch (Exception e)

		{

			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
					e.getMessage());
			// rethrow
			throw e;

		} finally {
			// close rs set
			if (rs != null) {
				rs.close();
			}
			// close stm
			st.close();
			con.close();
		} // end finally
		return retMap;
	} // end method

	/**
	 * Return a list holding the name of satellites
	 * 
	 * @return ist holding the name of satellites
	 * @throws Exception 
	 */
	public List<String> getSatellites() throws Exception {
		List<String> satList = new ArrayList<>();
		String query = "SELECT SATELLITE_NAME  from SATELLITE"; // query string
		PreparedStatement st =null;
		ResultSet rs = null;
		try {
			
			if (con == null || con.isClosed()) {

				con = initConnection();
				
				}
			st = con.prepareStatement(query);
			// executing query
			this.tm.debug("Inside method getIdSatellite: " + query);
			rs = st.executeQuery();
			// getting data
			while (rs.next()) {

				String sat = rs.getString(1);
				satList.add(sat);

			} // end

		} // end try
		catch (Exception e)

		{

			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
					e.getMessage());
			// rethrow
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
		} // end finally
			// returning
		return satList;
	}// end method

	/**
	 * Return a mapping of id satellite name
	 * 
	 * @return mapping of id satellite name
	 * @throws Exception 
	 */
	public Map<Integer, String> getSatellitesIdNameMap() throws Exception {
		// map to be returned
		TreeMap<Integer, String> satMap = new TreeMap<>();
		String query = "SELECT ID_SATELLITE, SATELLITE_NAME  from SATELLITE";
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			if (con == null || con.isClosed()) {

			con = initConnection();
			
			}
			st = con.prepareStatement(query);
			// executing query
			this.tm.debug("Inside method getIdSatellite: " + query);
			rs = st.executeQuery();
			// getting data
			while (rs.next()) {
				Integer id = new Integer(rs.getInt(1));
				String sat = rs.getString(2);
				satMap.put(id, sat);

			} // end

		} // end try
		catch (Exception e)

		{

			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
					e.getMessage());
			// rethorw
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
		} // end finally
			// returning map
		return satMap;
	}// end method

	/**
	 * Return the id of a satellite
	 * 
	 * @param satelliteName
	 * @return sat id
	 * @throws Exception
	 */
	public int getIdSatellite(String satelliteName) throws Exception {

		int idSatellite = 0;
		String query = "SELECT ID_SATELLITE  from SATELLITE where   SATELLITE_NAME = " + "'" + satelliteName + "'";
		PreparedStatement st=null;
		ResultSet rs = null;
		try {
			if (con == null || con.isClosed()) {

			con = initConnection();
			
			}
			st = con.prepareStatement(query);
			// executing query
			this.tm.debug("Inside method getIdSatellite: " + query);
			rs = st.executeQuery();
			// getting data
			while (rs.next()) {

				idSatellite = rs.getInt("ID_SATELLITE");

			} // end

		} // end try
		catch (Exception e)

		{

			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
					e.getMessage());
			// rethrow
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
		} // end finally
			// id satellite
		return idSatellite;
	}// end method

	/**
	 * Return a list holding the id of satellites
	 * 
	 * @return
	 * @throws SQLException
	 */
	List<Integer> getSatellitesIDList() throws SQLException {
		// list to be returnened
		List<Integer> list = new ArrayList<>();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			if (con == null || con.isClosed()) {

			con = initConnection();
			
			}
			String query = "SELECT ID_SATELLITE  from SATELLITE ";
			st = con.prepareStatement(query);
			// executing query
			this.tm.debug("Inside method getIdSatellite: " + query);
			rs = st.executeQuery();

			int idSatellite;
			// getting data
			while (rs.next()) {

				idSatellite = rs.getInt("ID_SATELLITE");
				list.add(new Integer(idSatellite));

			} // end

		} // end try
			// end try
		catch (Exception e)

		{

			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
					e.getMessage());
			// rethrow
			try {
				throw e;
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} finally {
			// close rs set
			if (rs != null) {
				rs.close();
			}

			// close stm
			st.close();
			con.close();
		} // end finally
			// returning
		return list;
	}// end method

	/**
	 * Update the table of obdata filename with the new one
	 * 
	 * @param satName
	 * @param path
	 * @param obdataType
	 * @throws SQLException
	 */
	public void updateObdataFileName(String satName, String path, int obdataType) throws SQLException {
		PreparedStatement st = null;
		try {
			if (con == null || con.isClosed()) {
				con = initConnection();
			}
			con.setAutoCommit(false);

			String obdata = "ODSTP";
			// switching on data type file
			switch (obdataType) {
			case DataManagerConstants.TYPE_ODSTP:
				obdata = "ODSTP";
				break;
			case DataManagerConstants.TYPE_ODMTP:
				obdata = "ODMTP";
				break;
			case DataManagerConstants.TYPE_ODNOM:
				obdata = "ODNOM";
				break;
			case DataManagerConstants.TYPE_ODREF:
				obdata = "ODREF";
				break;
			default:
				throw new SQLException("Wrong Data Type");

			}// end switch

			// insetrt statement string
			String insertStatement = "update OBDATA_FILES f set f.";
			insertStatement = insertStatement + obdata + "='" + path + "' where EXISTS";
			// substastemt do complete insert statement
			String substatement = "SELECT s.ID_SATELLITE from SATELLITE s where s.SATELLITE_NAME='" + satName
					+ "' and s.ID_SATELLITE = f.ID_SATELLITE";

			insertStatement = insertStatement + "(" + substatement + ")";

			st = con.prepareStatement(insertStatement);
			this.tm.debug(" PreparedStatement :  " + st);
logger.debug("insertStatement OBDATA "+insertStatement);
			// exeuting query
			st.executeQuery();
		} catch (Exception e) {
			// log
			this.tm.critical(EventType.SOFTWARE_EVENT, "Errore in update OBDATA_FILES: ", e.getMessage());
			// rethow
			try {
				throw e;
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} // end try
		finally {

			// stm close
			st.close();
			con.close();
		} // end finally

	}// end method

	/**
	 * Insert the initial cvalue in the table holding the obdata filename
	 * 
	 * @throws SQLException
	 */
	public void initializeOBDATAFileNameTable() throws SQLException {
		
			try {
			
				// query string
				String baseInsertStatement = "insert into OBDATA_FILES (ODSTP, ODMTP, ODNOM, ODREF, ID_SATELLITE) values ('ODSTP','ODMTP','ODNOM','ODREF',";
				List<Integer> list = getSatellitesIDList();
				for (Integer i : list) {
					if (con == null || con.isClosed()) {

						con = initConnection();
						
						}
						con.setAutoCommit(false);
					String insertStatement = baseInsertStatement + i.intValue() + ")";
					// //System.out.println(insertStatement);
					PreparedStatement st = con.prepareStatement(insertStatement);
					// execute
					st.executeQuery();
					// close
					st.close();

				} // end for
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally
			{
				con.close();

			}

	}// end method

	/**
	 * 
	 * @return the list of beam configured as spotlight
	 * @throws Exception
	 */
	public List<String> getSpotLightBeamsList() throws Exception {

		/*
		 * String query =
		 * "SELECT  distinct(s.SATELLITE_NAME) AS \"Satellite Name\", MISSION_NAME as \"Mission Name\",  s.IS_ENABLED AS \"Is Enabled\", s.ID_ALLOWED_LOOK_SIDE AS \"Id Allowed Look Side\", s.ID_SATELLITE AS \"Id Satellite\"   from BEAM b,SAT_BEAM_ASSOCIATION sb, SATELLITE s, MISSION m  where b.ID_BEAM(+)=sb.BEAM  and sb.SATELLITE=s.ID_SATELLITE  "
		 * + " and b.SENSOR_MODE= " + idSensorMode + " and m.ID_MISSION= s.MISSION" +
		 * " and  m.ID_MISSION= " + idMission + " order by s.SATELLITE_NAME";
		 */

		String query = "SELECT BEAM.BEAM_NAME from BEAM join SENSOR_MODE on BEAM.SENSOR_MODE=SENSOR_MODE.ID_SENSOR_MODE and SENSOR_MODE.IS_SPOT_LIGHT=1";

		// ManagerLogger.logDebug(this, query);
		this.tm.debug("Inside method getSpotLightBeamsList " + query);
		PreparedStatement st = null;
		ResultSet rs = null;
		ArrayList<String> beamList = new ArrayList<>();

		try {
			if (con == null || con.isClosed()) {
				con = super.initConnection();
			}
			st = con.prepareStatement(query);
			// execute
			rs = st.executeQuery();
			// getting data
			while (rs.next()) {

				beamList.add(rs.getString(1));

			} // end while
		} // end try
		catch (SQLException e) {

			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
					e.getMessage());
			// rethrow
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
		} // end finally
		return beamList;
	}// end method

	/**
	 * 
	 * @return the list of name of sensormode configured as spotlight
	 * @throws Exception
	 */
	public List<String> getSpotLightSensorModeList() throws Exception {

		/*
		 * String query =
		 * "SELECT  distinct(s.SATELLITE_NAME) AS \"Satellite Name\", MISSION_NAME as \"Mission Name\",  s.IS_ENABLED AS \"Is Enabled\", s.ID_ALLOWED_LOOK_SIDE AS \"Id Allowed Look Side\", s.ID_SATELLITE AS \"Id Satellite\"   from BEAM b,SAT_BEAM_ASSOCIATION sb, SATELLITE s, MISSION m  where b.ID_BEAM(+)=sb.BEAM  and sb.SATELLITE=s.ID_SATELLITE  "
		 * + " and b.SENSOR_MODE= " + idSensorMode + " and m.ID_MISSION= s.MISSION" +
		 * " and  m.ID_MISSION= " + idMission + " order by s.SATELLITE_NAME";
		 */

		String query = "SELECT SENSOR_MODE.SENSOR_MODE_NAME from SENSOR_MODE WHERE SENSOR_MODE.IS_SPOT_LIGHT=1";

		// ManagerLogger.logDebug(this, query);
		this.tm.debug("Inside method getSpotLightSensorModeList " + query);
		PreparedStatement st = null;
		ResultSet rs = null;
		ArrayList<String> senormodeList = new ArrayList<>();

		try {
			if (con == null || con.isClosed()) {
				con = super.initConnection();
			}
			st = con.prepareStatement(query);
			// executing
			rs = st.executeQuery();
			// getting data
			while (rs.next()) {

				senormodeList.add(rs.getString(1));

			} // end while
		} // end try
		catch (SQLException e) {

			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
					e.getMessage());
			// rethrow
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
		} // end finally
		return senormodeList;
	}// end method

	/**
	 * Update the allowed lookside for satellite. Allowed values are: 0: none 1:
	 * right 2: left 3: both
	 * 
	 * @param satName
	 * @param allowedSide
	 * @throws SQLException
	 */
	public void updateAllowedLookSide(String satName, int allowedSide) throws SQLException {
		// statement string
		String updateString = "update SATELLITE set ID_ALLOWED_LOOK_SIDE=" + allowedSide + " where SATELLITE_NAME='"
				+ satName + "'";
		this.tm.debug("Inside method updateAllowedLookSide " + updateString);
		// prepared stm

		PreparedStatement st = null;

		try {
			if (con == null || con.isClosed()) {
				con = super.initConnection();
			}

			st = con.prepareStatement(updateString);
			// exeute
			st.executeQuery();

		} // end try
		catch (Exception e) {

			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ",
					e.getMessage());

		} // end catch
		finally {
			// close stm
			st.close();
			con.close();
		} // end finally

	}// end method

}// end method
