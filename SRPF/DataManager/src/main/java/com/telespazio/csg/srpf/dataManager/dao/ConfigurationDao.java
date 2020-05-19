/**
*
* MODULE FILE NAME:	SatellitePassBO.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Define the configuration Data Abstract Model for low level DB access
*
* PURPOSE:			for configuration purposes
*
* CREATION DATE:	22-01-2016
*
* AUTHORS:			Abed Alissa
*
* DESIGN ISSUE:		2.1
*
* INTERFACES:
*
* SUBORDINATES:
*
* MODIFICATION HISTORY:
*
*
* --------------------------+------------+----------------+-------------------------------
* 01-12-2016 |Amedeo Bancone  |2.0     | align to the modiication on DB structure
* 20-04-2018 |Amedeo Bancone  |2.1	   | added contol to dataControlCsv. It generates exception and give information about malformed line
* 									   | added method to update configuration modifying sensor mode beam and satbeamass tables	:
* 									   | deleteSensorModes
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.dataManager.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.telespazio.csg.srpf.dataManager.GenericDAO;
import com.telespazio.csg.srpf.dataManager.bean.BeamBean;
import com.telespazio.csg.srpf.dataManager.bean.SatelliteBean;
import com.telespazio.csg.srpf.dataManager.util.DataManagerConstants;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;

/**
 *
 * @author Abed Alissa
 * @version 2.1
 *
 */

public class ConfigurationDao extends GenericDAO {
	static final Logger logger = LogManager.getLogger(ConfigurationDao.class.getName());

	// comment
	private static String StartComment = "M";
	// number of expected values
	private static int NumberOfExpectedEntryInLine = 18;
	//static final Logger logger = LogManager.getLogger(ConfigurationDao.class.getName());

	// logger
	TraceManager tm = new TraceManager();

	/**
	 * Default Constructor
	 * 
	 * @throws NamingException
	 * @throws Exception
	 */
	public ConfigurationDao() throws NamingException, Exception {
		super();
		// TODO Auto-generated constructor stub
	}// end method

	/**
	 * Copy constructor
	 * 
	 * @param d
	 * @throws NamingException
	 * @throws Exception
	 */
	public ConfigurationDao(GenericDAO d) throws NamingException, Exception {
		super(d);
		// TODO Auto-generated constructor stub
	}// end method

	/**
	 * Reurn satellite bean given id
	 * 
	 * @author Abed
	 * @param SatelliteName
	 * @return Satellite bean
	 * @throws Exception
	 */
	public SatelliteBean getDatiSatellite(int idSatellite) throws Exception {
		if (con == null || con.isClosed()) {
			con = super.initConnection();
		}
		// Bean to be returned
		SatelliteBean p = null;
		// Query string
		String query = "SELECT ID_SATELLITE, SATELLITE_NAME, IS_ENABLED  ,ALLOWED_LOOK_SIDE, ID_ALLOWED_LOOK_SIDE  from SATELLITE where   ID_SATELLITE = "
				+ "'" + idSatellite + "'";
		PreparedStatement st = null;
		ResultSet rs = null;

		try {
			// execute query
			this.tm.debug(query);
			st = con.prepareStatement(query);

			rs = st.executeQuery();
			// retieve values on records
			while (rs.next()) {
				p = new SatelliteBean();
				p.setIdSatellite(rs.getInt("ID_SATELLITE"));
				p.setSatelliteName(rs.getString("SATELLITE_NAME"));
				p.setIsEnabled(rs.getInt("IS_ENABLED"));
				p.setAllowedLookSide(rs.getString("ALLOWED_LOOK_SIDE"));
				p.setIdAllowedLookSide(rs.getInt("ID_ALLOWED_LOOK_SIDE"));

			} // end while

		} // end try
		catch (Exception e) {
			// rethrow
			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: " + query,
					e.getMessage());
			throw e;
		} // end catch
		finally {
			// close result set
			if (rs != null) {
				rs.close();
			}
			// close statement
			st.close();
			con.close();
		} // end finally
		return p;
	}// end method

	/**
	 * Reurn satellite bean given id
	 * 
	 * @author Abed
	 * @param SatelliteName
	 * @return Satellite bean
	 * @throws Exception
	 */
	public List<BeamBean> getBeamsSatellite() throws Exception {

		List<BeamBean> beams = new ArrayList<BeamBean>();
		// Bean to be returned
		// Query string
		String query = "SELECT * from BEAM ";
		// String query = "SELECT * from BEAM where ID_BEAM = (SELECT * FROM
		// SAT_BEAM_ASSOCIATION where SATELLITE =" + "'" + idSatellite + "'";

		PreparedStatement st = null;
		ResultSet rs = null;

		try {
			// execute query
			this.tm.debug(query);
			if (con == null || con.isClosed()) {
				con = super.initConnection();
			}
			st = con.prepareStatement(query);

			rs = st.executeQuery();

			// retieve values on records
			while (rs.next()) {
				BeamBean beam = new BeamBean();
				beam.setIdBeam(rs.getInt(1));
				beam.setBeamName(rs.getString(2));
				beam.setNearOffNadir(rs.getDouble(3));
				beam.setFarOffNadir(rs.getDouble(4));
				beam.setSensorMode(rs.getInt(5));
				beam.setIsEnabled(rs.getInt(6));
				beam.setSwDim1(rs.getDouble(7));
				beam.setSwDim2(rs.getDouble(8));
				beam.setDtoMinDuration(rs.getInt(9));
				beam.setDtoMaxDuration(rs.getInt(10));
				beam.setResTime(rs.getInt(11));
				beam.setDtoDurationSquared(rs.getInt(12));
				beams.add(beam);
			} // end while

		} // end try
		catch (Exception e) {
			// rethrow
			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: " + query,
					e.getMessage());
			throw e;
		} // end catch
		finally {
			// close result set
			if (rs != null) {
				rs.close();
			}
			// close statement
			st.close();
			con.close();
		} // end finally
		return beams;
	}// end method

	
	/**
	 * Reurn satellite bean given id
	 * 
	 * @author Abed
	 * @param satID 
	 * @param SatelliteName
	 * @return Satellite bean
	 * @throws Exception
	 */
	public BeamBean getBeamsFromID(String idBeam, int satID) throws Exception {

		BeamBean beamReturned = null;
		// Bean to be returned
		// Query string
		String query = "SELECT * from BEAM where BEAM_NAME =" + "'" + idBeam + "' AND ID_BEAM IN (SELECT BEAM \r\n" + 
				"		                 FROM SAT_BEAM_ASSOCIATION " + 
				"		                WHERE SATELLITE ="+ satID + ")";

		PreparedStatement st = null;
		ResultSet rs = null;

		try {
			// execute query
			logger.debug(query);
			if (con == null || con.isClosed()) {
				con = super.initConnection();
			}
			st = con.prepareStatement(query);

			rs = st.executeQuery();

			// retieve values on records
			while (rs.next()) {
				beamReturned = new BeamBean();
				beamReturned.setIdBeam(rs.getInt(1));
				beamReturned.setBeamName(rs.getString(2));
				beamReturned.setNearOffNadir(rs.getDouble(3));
				beamReturned.setFarOffNadir(rs.getDouble(4));
				beamReturned.setSensorMode(rs.getInt(5));
				beamReturned.setIsEnabled(rs.getInt(6));
				beamReturned.setSwDim1(rs.getDouble(7));
				beamReturned.setSwDim2(rs.getDouble(8));
				beamReturned.setDtoMinDuration(rs.getInt(9));
				beamReturned.setDtoMaxDuration(rs.getInt(10));
				beamReturned.setResTime(rs.getInt(11));
				beamReturned.setDtoDurationSquared(rs.getInt(12));
				
			} // end while

		} // end try
		catch (Exception e) {
			// rethrow
			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: " + query,
					e.getMessage());
			throw e;
		} // end catch
		finally {
			// close result set
			if (rs != null) {
				rs.close();
			}
			// close statement
			st.close();
			con.close();
		} // end finally
		return beamReturned;
	}// end method

	
	/**
	 * Reurn satellite bean given id
	 * 
	 * @author Abed
	 * @param offNadirAngle 
	 * @param SatelliteName
	 * @return Satellite bean
	 * @throws Exception
	 */
	public List<BeamBean> getBeamsSatelliteRefined(double nearOffNadir, double farOffNadir, int idSatellite) throws Exception {

		List<BeamBean> beams = getBeamsSatellite();

		List<Integer> allSatBeamId= new ArrayList<Integer>();
		// Bean to be returned
		// Query string
		String query = "SELECT BEAM FROM SAT_BEAM_ASSOCIATION where SATELLITE =" + "'" + idSatellite + "'";
		logger.debug("query beam  :"+query);

		PreparedStatement st = null;
		ResultSet rs = null;

		try {
			// execute query
			this.tm.debug(query);
			if (con == null || con.isClosed()) {
				con = super.initConnection();
			}
			st = con.prepareStatement(query);

			rs = st.executeQuery();
			BeamBean returnedBeam = null;
			// retieve values on records
			while (rs.next()) {
				allSatBeamId.add(rs.getInt(1));
			} // end while

			
			if(allSatBeamId.size()>0 && beams.size()>0)
			{        
				logger.debug("allSatBeamId :"+allSatBeamId);

				beams = filterBeamsForSatelliteAndNearOffAngle(allSatBeamId,beams,nearOffNadir,farOffNadir);
				
			}
		} // end try
		catch (Exception e) {
			// rethrow
			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: " + query,
					e.getMessage());
			throw e;
		} // end catch
		finally {
			// close result set
			if (rs != null) {
				rs.close();
			}
			// close statement
			st.close();
			con.close();
		} // end finally
		return beams;
	}// end method

	private List<BeamBean> filterBeamsForSatelliteAndNearOffAngle(List<Integer> allSatBeamId, List<BeamBean> beams,
			double nearOff, double farOffNadir) {
		List<BeamBean> onlyValid = new ArrayList<BeamBean>();
		logger.debug(" beam  id:"+onlyValid);
		logger.debug(" nearOff :"+nearOff);
		logger.debug(" farOffNadir :"+farOffNadir);

		for(int i=0;i<beams.size();i++)
		{
			for(int j=0;j<allSatBeamId.size();j++)
			{
				BeamBean currentBeam = beams.get(i);
				if(currentBeam.getIdBeam()==allSatBeamId.get(j))
				{
					
					if(currentBeam.getNearOffNadir()<=farOffNadir && currentBeam.getFarOffNadir()>=nearOff)
					{
						logger.debug("valid beam  :"+currentBeam);

						onlyValid.add(currentBeam);
					}
				}
			}
		}
		return onlyValid;
	}

	/**
	 * Retrurns the ID of satellites belonging the given mission
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param idMission
	 * @return list of id
	 * @throws Exception
	 */
	public ArrayList<Integer> getIdSatellites(int idMission) throws Exception {
		// query string
		String query = "SELECT ID_SATELLITE FROM SATELLITE where MISSION = " + "'" + idMission + "'";
		if (con == null || con.isClosed()) {
			con = super.initConnection();
		}
		this.tm.debug(query);
		PreparedStatement st = con.prepareStatement(query);
		ResultSet rs = null;
		ArrayList<Integer> listaIdSatellites = new ArrayList<Integer>();
		int idSatellite = 0;

		try {
			// executing query
			rs = st.executeQuery();

			while (rs.next()) {
				// retrieving ID
				idSatellite = rs.getInt(1);
				// adding to list
				listaIdSatellites.add(idSatellite);
			} // end while
		} // end try
		catch (SQLException e) {
			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: " + query,
					e.getMessage());

		} // end catch
		finally {
			// close result set
			if (rs != null) {
				rs.close();
			}
			// close statement
			st.close();
			con.close();
		} // end finally
			// returning list
		return listaIdSatellites;
	}

	/**
	 * String Buffer holding DB configuration
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param tableName
	 * @return String buffer holding configuration
	 * @throws Exception
	 */
	public StringBuffer exportConfiguration(String tableName) throws Exception {

		StringBuilder commandColl = new StringBuilder();
		StringBuilder command = null;
		StringBuffer sbOutputAll = new StringBuffer();
		PreparedStatement stmt = null;
		ResultSet rset = null;
		try {
			stmt = con.prepareStatement("SELECT * FROM " + tableName);
			if (con == null || con.isClosed()) {
				con = super.initConnection();
			}
			stmt.executeQuery();
			ResultSetMetaData meta = rset.getMetaData();
			int columns = meta.getColumnCount();
			// command.append("INSERT into " + tableName + " (");
			for (int i = 1; i <= columns; i++) {
				// if (i > 1) {
				// command.append(' ');
				// }
				// command.append('|');
				if (i == 1) {
					commandColl.append(meta.getColumnName(i));
				} else {
					commandColl.append("," + meta.getColumnName(i));
					// command.append(" ");
				}

			}
			sbOutputAll.append(commandColl.toString());
			// command.append("\n");
			// command.append(") VALUES (");
			// int head = command.length();
			while (rset.next()) {
				command = new StringBuilder();
				for (int i = 1; i <= columns; i++) {

					if (i > 1) {
						command.append(',');
					}
					String value = rset.getString(i);
					if (value != null) {
						// command.append("\n");
						command.append(value);

					} else {
						command.append("NULL");
					}
				}

				sbOutputAll.append("\n" + command.toString());
				// command.setLength(head);

			}
		} finally {
			// stm close
			stmt.close();
			rset.close();
			con.close();
		}
		return sbOutputAll;
	}

	/**
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @throws IOException
	 * @throws SQLException
	 */
	public void deleteTablesConfiguration() throws IOException, SQLException

	{
		Statement deleteStatement = null;

		try {

			// list of table to be deleted
			String[] tables = { "SATELLITE_PASS", "GSIF_PAW", "OBDATA_FILES", "SAT_BEAM_ASSOCIATION", "BEAM",
					"SENSOR_MODE", "SATELLITE", "MISSION" };
			// ManagerLogger.logDebug(this, "Reading file heading");

			// ManagerLogger.logDebug(this, "trying to insert data into DB");
			this.tm.debug("Trying to clean data into DB");

			// to do

			// ManagerLogger.logDebug(this, "deleted old epoch");

			// for each table in list
			// delete entries
			for (String tableName : tables) {

				// building delete statement
				String deleteTable = "delete  from  " + tableName;
				this.tm.debug("Trying to delete data from table:" + tableName);
				if (con == null || con.isClosed()) {
					con = super.initConnection();
				}
				con.setAutoCommit(false); // disabling autocommit
				deleteStatement = con.createStatement();

				deleteStatement.executeUpdate(deleteTable);

				this.tm.debug("Success deleting data from table: " + tableName);

			}
		} catch (Exception e) {
			this.tm.debug("ERROR " + e.getMessage());

			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// close statement
			if (deleteStatement != null) {
				closeStatement(deleteStatement);
			} // end if
			con.close();
		} // end finally

	}// end method

	/**
	 * Populate mission table
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param csvFile
	 * @throws Exception
	 */
	public void importConfigurationMissionCsv(File csvFile) throws Exception {
		/**
		 * Populate mission table staring for CSV file
		 */

		// reader
		BufferedReader br = null;
		String line = "";
		// split char
		String cvsSplitBy = "|";
		// mission list
		String[] missionSatData;
		br = new BufferedReader(new FileReader(csvFile));
		LinkedHashSet<String> allMission = new LinkedHashSet<>();

		// prepared statement
		PreparedStatement pst = null;
		// int idMission = 0;
		int j = 0;
		try {

			while ((line = br.readLine()) != null) {
				// not comment
				if (!line.startsWith(StartComment)) {

					// use | as separator
					missionSatData = line.split("\\" + cvsSplitBy);

					String missionName = missionSatData[0].trim();

					if (!missionName.equalsIgnoreCase("")) {

						if (!allMission.contains(missionName)) {
							j++;
							// query string
							String uploadMission = "INSERT into MISSION (ID_MISSION, MISSION_NAME) VALUES ('" + j
									+ "','" + missionName + "' )";

							if (con == null || con.isClosed()) {
								con = super.initConnection();
							}
							con.setAutoCommit(false);

							pst = con.prepareStatement(uploadMission);
							// execting query
							pst.execute();
							allMission.add(missionName);
							pst.close();
							pst = null;

							// idMission = getIdMission(missionName);
						} // end if
					} // end if
					else {
						// throwing
						throw new Exception("Error import csv to MISSION table: Empty field");
					} // end else
						// br.close();

				} // end if
			} // end while

		} // end try
		catch (Exception ex) {
			this.tm.critical(EventType.SOFTWARE_EVENT, "Error import csv to MISSION table: ", ex.getMessage());
			throw ex;
		} // end catch
		finally {
			// close statement
			if (pst != null) {
				pst.close();
			}
			// close reader
			if (br != null) {
				br.close();
			}
			con.close();
		} // enf finally

	}// end method

	/**
	 * Populate sensor modes
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param csvFile
	 * @throws Exception
	 */
	public void importConfigurationSensoreModeCsv(File csvFile) throws Exception {
		// reader
		BufferedReader br1 = null;
		String line = "";
		String cvsSplitBy = "|"; // token separator
		PreparedStatement pstm = null;

		try {
			LinkedHashSet<String> allSensorModes = new LinkedHashSet<>();
			// disable autocommit
			String[] sensorModeNames;
			br1 = new BufferedReader(new FileReader(csvFile));
			int j = 0;
			while ((line = br1.readLine()) != null) {
				if (!line.startsWith(StartComment)) {
					// use comma as separator
					sensorModeNames = line.split("\\" + cvsSplitBy);

					if (sensorModeNames.length > 10) {
						// sennsormode name
						String sensorModeName = sensorModeNames[1].trim();

						int isSpotLight = 0;
						while (!allSensorModes.contains(sensorModeName)) {
							j++;
							// check if spotlight
							if (sensorModeNames[14].trim().equalsIgnoreCase("Yes")) {
								isSpotLight = 1;
							} else {
								isSpotLight = 0;
							} // end if

							// Integer.parseInt(sensorModeNames[16].trim());
							// query string
							String uploadSensorMode = "INSERT into SENSOR_MODE (ID_SENSOR_MODE, SENSOR_MODE_NAME,IS_SPOT_LIGHT) VALUES ('"
									+ j + "','" + sensorModeName + "'" + "," + isSpotLight + " )";

							if (con == null || con.isClosed()) {
								con = super.initConnection();
							}
							con.setAutoCommit(false);

							pstm = con.prepareStatement(uploadSensorMode);
							// executing query
							pstm.execute();

							allSensorModes.add(sensorModeName);
							// closing
							pstm.close();
							pstm = null;
						} // end while

					} // end if

				} // end if
			} // end while

		} // end try
		catch (SQLException | IOException ex) {
			this.tm.critical(EventType.SOFTWARE_EVENT, "Error import csv to SENSOR_MODE table: ", ex.getMessage());
			// rethrow
			throw ex;

		} // end catch
		finally {
			// close statement
			if (pstm != null) {
				pstm.close();
			}
			// close reader
			if (br1 != null) {
				br1.close();
			}
			con.close();
		} // end finally

	}// end method

	/**
	 * Populate satellite table from CSV file
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param csvFile
	 * @throws Exception
	 */
	public void importConfigurationSatelliteCsv(File csvFile) throws Exception {

		// reader
		BufferedReader br = null;
		String line = "";
		// token separator
		String cvsSplitBy = "|";
		// tokens list
		String[] missionSatData;
		br = new BufferedReader(new FileReader(csvFile));

		LinkedHashSet<String> allSat = new LinkedHashSet<>();
		int i = 0;
		PreparedStatement pst = null;

		try {

			while ((line = br.readLine()) != null) {
				if (!line.startsWith(StartComment)) {

					// use | as separator
					missionSatData = line.split("\\" + cvsSplitBy);
					if (missionSatData.length > 10) {
						int isEnabled = 0;
						int idAllowedLookSide = 0;
						// getting mission
						String missionName = missionSatData[0].trim();
						// getting satellite
						String satelliteName = missionSatData[2].trim();

						if (!allSat.contains(satelliteName)) {
							// getting mission id
							int idMission = getIdMission(missionName);
							i++;

							// Track offset
							String offset = missionSatData[3].trim();
							// check if enabled
							if (missionSatData[4].trim().equalsIgnoreCase("Yes")) {
								isEnabled = 1;
							} else {
								isEnabled = 0;
							}
							// check for allowed angles
							if (missionSatData[5].trim().equalsIgnoreCase("right")) {
								idAllowedLookSide = DataManagerConstants.ID_ALLOWED_lOOK_RIGHT;
							} else if (missionSatData[5].trim().equalsIgnoreCase("left")) {
								idAllowedLookSide = DataManagerConstants.ID_ALLOWED_lOOK_LEFT;
							} else if (missionSatData[5].trim().equalsIgnoreCase("both")) {
								idAllowedLookSide = DataManagerConstants.ID_ALLOWED_lOOK_BOTH;
							} else {
								idAllowedLookSide = DataManagerConstants.ID_ALLOWED_lOOK_NONE;
							}
							// query string
							String uploadSat = "INSERT into SATELLITE (ID_SATELLITE, SATELLITE_NAME,MISSION,IS_ENABLED,ID_ALLOWED_LOOK_SIDE, TRACK_OFFSET) VALUES ("
									+ i + ",'" + satelliteName + "'," + idMission + "," + isEnabled + ","
									+ idAllowedLookSide + "," + offset + " )";
							if (con == null || con.isClosed()) {
								con = super.initConnection();
							}
							// set autocommit to false
							con.setAutoCommit(false);
							// //System.out.println(uploadSat);
							pst = con.prepareStatement(uploadSat);
							// executing query
							pst.execute();
							allSat.add(satelliteName);
							pst.close();
							pst = null;
						} // end if

					} // end if
				} // end if
			} // end while
		} // end try
		catch (Exception ex) {
			this.tm.critical(EventType.SOFTWARE_EVENT, "Error import csv to SATELLITE table: ", ex.getMessage());
			throw ex;
		} // end catch
		finally {
			// close statement
			if (pst != null) {
				pst.close();
			}

			// close reader
			if (br != null) {
				br.close();
			}
			con.close();
		} // end finally

	}// end method

	/**
	 * Import beam form file
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param csvFile
	 * @throws Exception
	 */
	public void importConfigurationBeamCsv(File csvFile) throws Exception {
		// populate beam table
		// staring from files
		// reader
		BufferedReader br = null;
		String line = "";
		// split char
		String cvsSplitBy = "|";
		PreparedStatement pstm = null;
		try {
			LinkedHashSet<String> allBeams = new LinkedHashSet<>();

			String[] sensorModeNames;
			br = new BufferedReader(new FileReader(csvFile));
			int i = 0;
			// for each line
			while ((line = br.readLine()) != null) {
				if (!line.startsWith(StartComment)) {
					// use comma as separator
					// list tokens
					sensorModeNames = line.split("\\" + cvsSplitBy);

					if (sensorModeNames.length > 10) {
						// extract sensormode
						String sensorModeName = sensorModeNames[1].trim();
						// extract beam
						String beamName = sensorModeNames[8].trim();

						// get sensormode id
						int idSensorMode = getIdSensorMode(sensorModeName);
						while (!allBeams.contains(beamName)) {
							i++;
							// getting angles
							double nearOffNadir = Double.parseDouble(sensorModeNames[9].trim());
							double farOffNadir = Double.parseDouble(sensorModeNames[10].trim());

//                            double nearOffNadirSat = Double.parseDouble(sensorModeNames[6].trim());
//                            // extracting off nadir angles
//                            double farOffNadirSat = Double.parseDouble(sensorModeNames[7].trim());

							String isEnabledString = sensorModeNames[11].trim();
							int isEnabled = 0;

							// evaluating foe enables
							if (isEnabledString.equalsIgnoreCase("Yes")) {
								isEnabled = 1;
							} else {
								isEnabled = 0;
							} // end else
								// extracting dimensions
							double swDim1 = Double.parseDouble(sensorModeNames[12].trim());
							double swDim2 = Double.parseDouble(sensorModeNames[13].trim());
							// extracting durations
							int dtoMinDuration = Integer.parseInt(sensorModeNames[15].trim());
							int dtoMaxDuration = Integer.parseInt(sensorModeNames[16].trim());
							int resTime = Integer.parseInt(sensorModeNames[17].trim());
							int dtoDurationSquared = 0;
							if (sensorModeNames.length > 18) {
								try {
									dtoDurationSquared = Integer.parseInt(sensorModeNames[18].trim());
								} catch (Exception e) {
									dtoDurationSquared = 0;
									continue;
								}
							}

							// SW_DIM1,SW_DIM2, DTO_MIN_DURATION,
							// DTO_MAX_DURATION,RES_TIME) VALUES ('" + j + "','"
							// + sensorModeName + "'" + "," + isSpotLight + ", "
							// + swDim1 + ", " + swDim2 + ", " + dtoMinDuration
							// + ", " + dtoMaxDuration + ", " + resTime + " )";
							// query string
							String uploadBeam = "INSERT into BEAM (" + "ID_BEAM, " + "BEAM_NAME," + "NEAR_OFF_NADIR,"
									+ "FAR_OFF_NADIR, " + "SENSOR_MODE," + "IS_ENABLED," + "SW_DIM1," + "SW_DIM2,"
									+ "DTO_MIN_DURATION," + "DTO_MAX_DURATION, " + "RES_TIME, "
									+ "DTO_DURATION_SQUARED) " + "" + "VALUES (" + i + ",'" + beamName + "',"
									+ nearOffNadir + "," + farOffNadir + "," + idSensorMode + "," + isEnabled + ","
									+ swDim1 + "," + swDim2 + "," + dtoMinDuration + "," + dtoMaxDuration + ","
									+ resTime + "," + dtoDurationSquared + ")";

							if (con == null || con.isClosed()) {
								con = super.initConnection();
							}
							// set autocommit to false
							con.setAutoCommit(false);

							// //System.out.println(uploadBeam);
							pstm = con.prepareStatement(uploadBeam);
							// executing query
							pstm.execute();
							allBeams.add(beamName);
							pstm.close();
							pstm = null;

						} // end while

					}
				} // end if
			} // end while

		} // end try
		catch (SQLException | IOException ex) {
			// rethrow
			this.tm.critical(EventType.SOFTWARE_EVENT, "Error import csv to BEAM table: ", ex.getMessage());
			throw ex;
		} // end catch
		finally {
			// clse statement
			if (pstm != null) {
				pstm.close();
			}
			// close reader
			if (br != null) {
				br.close();
			}
			con.close();
		} // end finally
	}// end method

	/**
	 * Import beam form file
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param csvFile
	 * @throws Exception
	 */
	public void updateConfigurationBeamCsv(File csvFile) throws Exception {
		// populate beam table
		// staring from files
		// reader
		BufferedReader br = null;
		String line = "";
		// split char
		String cvsSplitBy = "|";
		PreparedStatement pstm = null;
		try {
			if (con == null || con.isClosed()) {
				con = super.initConnection();
			}
			LinkedHashSet<String> allBeams = new LinkedHashSet<>();
			// set autocommit to false
			con.setAutoCommit(false);
			String[] sensorModeNames;
			br = new BufferedReader(new FileReader(csvFile));
			int i = 0;
			// for each line
			while ((line = br.readLine()) != null) {
				if (!line.startsWith(StartComment)) {
					// use comma as separator
					// list tokens
					sensorModeNames = line.split("\\" + cvsSplitBy);

					if (sensorModeNames.length > 10) {
						// extract sensormode
						String sensorModeName = sensorModeNames[1].trim();
						// extract beam
						String beamName = sensorModeNames[8].trim();

						// get sensormode id
						int idSensorMode = getIdSensorMode(sensorModeName);
						while (!allBeams.contains(beamName)) {
							i++;
							// getting angles
							double nearOffNadir = Double.parseDouble(sensorModeNames[9].trim());
							double farOffNadir = Double.parseDouble(sensorModeNames[10].trim());

//                            double nearOffNadirSat = Double.parseDouble(sensorModeNames[6].trim());
//                            // extracting off nadir angles
//                            double farOffNadirSat = Double.parseDouble(sensorModeNames[7].trim());

							String isEnabledString = sensorModeNames[11].trim();
							int isEnabled = 0;

							// evaluating foe enables
							if (isEnabledString.equalsIgnoreCase("Yes")) {
								isEnabled = 1;
							} else {
								isEnabled = 0;
							} // end else
								// extracting dimensions
							double swDim1 = Double.parseDouble(sensorModeNames[12].trim());
							double swDim2 = Double.parseDouble(sensorModeNames[13].trim());
							// extracting durations
							int dtoMinDuration = Integer.parseInt(sensorModeNames[15].trim());
							int dtoMaxDuration = Integer.parseInt(sensorModeNames[16].trim());
							int resTime = Integer.parseInt(sensorModeNames[17].trim());
							int dtoDurationSquared = 0;
							if (sensorModeNames.length > 18) {
								try {
									dtoDurationSquared = Integer.parseInt(sensorModeNames[18].trim());
								} catch (Exception e) {
									dtoDurationSquared = 0;
									continue;
								}
							}
							String alterTable = "ALTER TABLE BEAM "
									+ "    ADD DTO_DURATION_SQUARED NUMBER(11) NOT NULL "
									+ "    CONSTRAINT duration_Squared DEFAULT 0;";
							// SW_DIM1,SW_DIM2, DTO_MIN_DURATION,
							// DTO_MAX_DURATION,RES_TIME) VALUES ('" + j + "','"
							// + sensorModeName + "'" + "," + isSpotLight + ", "
							// + swDim1 + ", " + swDim2 + ", " + dtoMinDuration
							// + ", " + dtoMaxDuration + ", " + resTime + " )";
							// query string
							String uploadBeam = "INSERT into BEAM (" + "ID_BEAM, " + "BEAM_NAME," + "NEAR_OFF_NADIR,"
									+ "FAR_OFF_NADIR, " + "SENSOR_MODE," + "IS_ENABLED," + "SW_DIM1," + "SW_DIM2,"
									+ "DTO_MIN_DURATION," + "DTO_MAX_DURATION, " + "RES_TIME, "
									+ "DTO_DURATION_SQUARED) " + "" + "VALUES (" + i + ",'" + beamName + "',"
									+ nearOffNadir + "," + farOffNadir + "," + idSensorMode + "," + isEnabled + ","
									+ swDim1 + "," + swDim2 + "," + dtoMinDuration + "," + dtoMaxDuration + ","
									+ resTime + "," + dtoDurationSquared + ")";
							if (con == null || con.isClosed()) {
								con = super.initConnection();
							}
							// //System.out.println(uploadBeam);
							pstm = con.prepareStatement(uploadBeam);
							// executing query
							pstm.execute();
							allBeams.add(beamName);
							pstm.close();
							pstm = null;

						} // end while

					}
				} // end if
			} // end while

		} // end try
		catch (SQLException | IOException ex) {
			// rethrow
			this.tm.critical(EventType.SOFTWARE_EVENT, "Error import csv to BEAM table: ", ex.getMessage());
			throw ex;
		} // end catch
		finally {
			// clse statement
			if (pstm != null) {
				pstm.close();
			}
			// close reader
			if (br != null) {
				br.close();
			}
			con.close();
		} // end finally
	}// end method

	/**
	 * Import beam form file
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param csvFile
	 * @throws Exception
	 */
	public void updateBeamCsv(List<BeamBean> allBeamsList, File csvFile) throws Exception {
		// populate beam table
		// staring from files
		// reader
		BufferedReader br = null;
		String line = "";
		// split char
		String cvsSplitBy = "|";
		PreparedStatement pstm = null;
		System.out.println(" all beams as list : " + allBeamsList);

		try {

			LinkedHashSet<String> allBeams = new LinkedHashSet<>();

			String[] sensorModeNames;
			br = new BufferedReader(new FileReader(csvFile));
			int i = 0;
			// for each line
			while ((line = br.readLine()) != null) {
				if (!line.startsWith(StartComment)) {
					// use comma as separator
					// list tokens
					sensorModeNames = line.split("\\" + cvsSplitBy);

					if (sensorModeNames.length > 10) {
						System.out.println("reading line :" + line);

						// extract sensormode
						String sensorModeName = sensorModeNames[1].trim();
						
						String satelliteName = sensorModeNames[2].trim();

						// extract beam
						String beamName = sensorModeNames[8].trim();

						// get sensormode id
						int idSensorMode = getIdSensorMode(sensorModeName);
						String isSpotlight = sensorModeNames[14].trim();
						if (idSensorMode < 0) {
							idSensorMode = insertSensorMode(sensorModeName, isSpotlight);
						}
						while (!allBeams.contains(beamName)) {
							i++;
							// getting angles
							double nearOffNadir = Double.parseDouble(sensorModeNames[9].trim());
							double farOffNadir = Double.parseDouble(sensorModeNames[10].trim());

//                            double nearOffNadirSat = Double.parseDouble(sensorModeNames[6].trim());
//                            // extracting off nadir angles
//                            double farOffNadirSat = Double.parseDouble(sensorModeNames[7].trim());

							String isEnabledString = sensorModeNames[11].trim();
							int isEnabled = 0;

							// evaluating foe enables
							if (isEnabledString.equalsIgnoreCase("Yes")) {
								isEnabled = 1;
							} else {
								isEnabled = 0;
							} // end else
								// extracting dimensions
							double swDim1 = Double.parseDouble(sensorModeNames[12].trim());
							double swDim2 = Double.parseDouble(sensorModeNames[13].trim());
							// extracting durations
							int dtoMinDuration = Integer.parseInt(sensorModeNames[15].trim());
							int dtoMaxDuration = Integer.parseInt(sensorModeNames[16].trim());
							int resTime = Integer.parseInt(sensorModeNames[17].trim());
							int dtoDurationSquared = 0;
							String query = null;
							if (sensorModeNames.length > 18) {
								try {
									dtoDurationSquared = Integer.parseInt(sensorModeNames[18].trim());
								} catch (Exception e) {
									dtoDurationSquared = 0;
									continue;
								}
							}
							boolean insert = false;
							BeamBean dummyBean = createBeam(i, beamName, nearOffNadir, farOffNadir, idSensorMode,
									isEnabled, swDim1, swDim2, dtoMinDuration, dtoMaxDuration, resTime,
									dtoDurationSquared);

							System.out.println(" create a dummy beam for element " + dummyBean);

							BeamBean existBeam = existBeam(allBeamsList, dummyBean);
							System.out.println(
									" check if exist another beam with same beam name and same beamId -> returned : "
											+ existBeam);
							int beamId= -1;
							if (existBeam != null) {
								// Beam exist, check if there is something different
								boolean isChanged = checkIfIsDifferent(existBeam.toString(), dummyBean.toString());

								System.out.println("Check if there are changes between these beams -> " + isChanged);
								if (isChanged) {
									// update it
									System.out.println("go in update!");
									// beam doesn't exist, add it !

									query = "UPDATE BEAM " + "SET NEAR_OFF_NADIR = " + nearOffNadir + ""
											+ ", FAR_OFF_NADIR = " + farOffNadir + "" + ", SENSOR_MODE = "
											+ idSensorMode + "" + ",IS_ENABLED = " + isEnabled + "" + ",SW_DIM1 ="
											+ swDim1 + "" + ",SW_DIM2 = " + swDim2 + "" + ",DTO_MIN_DURATION ="
											+ dtoMinDuration + "" + ",DTO_MAX_DURATION = " + dtoMaxDuration + ""
											+ ",RES_TIME = " + resTime + "" + ",DTO_DURATION_SQUARED = "
											+ dtoDurationSquared + "" + " WHERE BEAM_NAME = " + "'" + beamName + "'";
									beamId = existBeam.getIdBeam();
								} else {
									// nothing to do
									System.out.println("nothing to do");
								}

							} else {
								// beam doesn't exist, add it !
								int maxId = 1000;
								query = "SELECT MAX (ID_BEAM) AS MAX_ID FROM BEAM";
								System.out.println("query " + query);

								try {
									con = checkConn();
									PreparedStatement st = con.prepareStatement(query);
									ResultSet rs = st.executeQuery();
									System.out.println("rs " + rs);

									if (rs.next()) {
										maxId = rs.getInt("MAX_ID") + 1;
									}

									// close result set
									if (rs != null) {
										rs.close();
									}
									// close statement
									st.close();
								} catch (SQLException e)

								{
									this.tm.critical(EventType.SOFTWARE_EVENT, "Error import csv to BEAM table: ",
											e.getMessage());

								}
								beamId = maxId;
								System.out.println("maxId " + maxId);

								System.out.println("go in insert!");

								insert = true;
								query = "INSERT into BEAM (" + "ID_BEAM, " + "BEAM_NAME," + "NEAR_OFF_NADIR,"
										+ "FAR_OFF_NADIR, " + "SENSOR_MODE," + "IS_ENABLED," + "SW_DIM1," + "SW_DIM2,"
										+ "DTO_MIN_DURATION," + "DTO_MAX_DURATION, " + "RES_TIME, "
										+ "DTO_DURATION_SQUARED) " + "" + " VALUES (" + maxId + ",'" + beamName + "',"
										+ nearOffNadir + "," + farOffNadir + "," + idSensorMode + "," + isEnabled + ","
										+ swDim1 + "," + swDim2 + "," + dtoMinDuration + "," + dtoMaxDuration + ","
										+ resTime + "," + dtoDurationSquared + ")";

							}
							if (query != null) {
								System.out.println("PRINT QUERY :" + query);

								if (con == null || con.isClosed()) {
									con = super.initConnection();
								}
								// set autocommit to false
								con.setAutoCommit(false);

								// //System.out.println(uploadBeam);
								pstm = con.prepareStatement(query);
								// executing query
								pstm.execute();

								pstm.close();
								
								
								if(insert)
								{
									System.out.println("insert also in satBeam assoc!");
									int satId = getIdSatellite(satelliteName);
									
									insertSatBeamAssoc(satId, beamId);
//							        SATELLITE NUMBER(10),
//							        BEAM NUMBER(11),
								}
							}
							if (!allBeams.contains(beamName)) {
								allBeams.add(beamName);
							}
							pstm = null;

						} // end while

					}
				} // end if
			} // end while

		} // end try
		catch (SQLException | IOException ex) {
			// rethrow
			this.tm.critical(EventType.SOFTWARE_EVENT, "Error import csv to BEAM table: ", ex.getMessage());
			throw ex;
		} // end catch
		finally {
			// clse statement
			if (pstm != null) {
				pstm.close();
			}
			// close reader
			if (br != null) {
				br.close();
			}
			con.close();
		} // end finally
	}// end method

	private int insertSensorMode(String sensorModeName, String isSpotlightAsString) throws Exception {
		int isSpotlight = 0;
		if (isSpotlightAsString.equalsIgnoreCase("Yes")) {
			isSpotlight = 1;
		} else {
			isSpotlight = 0;
		} // end if
		PreparedStatement st = null;

		// beam doesn't exist, add it !
		int maxId = 1000;
		String query = "SELECT MAX (ID_SENSOR_MODE) AS MAX_ID FROM SENSOR_MODE";
		System.out.println("query " + query);

		try {
			con = checkConn();
			st = con.prepareStatement(query);
			ResultSet rs = st.executeQuery();
			System.out.println("rs " + rs);

			if (rs.next()) {
				maxId = rs.getInt("MAX_ID") + 1;
			}

			// close result set
			if (rs != null) {
				rs.close();
			}
			// close statement
			st.close();

			// Integer.parseInt(sensorModeNames[16].trim());
			// query string
			String uploadSensorMode = "INSERT into SENSOR_MODE (ID_SENSOR_MODE, SENSOR_MODE_NAME,IS_SPOT_LIGHT) VALUES ('"
					+ maxId + "','" + sensorModeName + "'" + "," + isSpotlight + " )";

			con = checkConn();
			con.setAutoCommit(false);

			st = con.prepareStatement(uploadSensorMode);
			// executing query
			st.execute();

			// closing
			st.close();
			st = null;

		} catch (Exception ex)

		{
		// rethrow
		this.tm.critical(EventType.SOFTWARE_EVENT, "Error import csv to BEAM table: ", ex.getMessage());
		throw ex;
	} // end catch
	finally {
		// clse statement
		if (st != null) {
			st.close();
		}
		con.close();
	} // end finally
		
		return maxId;
	}

	private int insertSatBeamAssoc(int satId, int beamId) throws Exception {
		PreparedStatement st = null;

		// beam doesn't exist, add it !
		int maxId = 1000;
		String query = "SELECT MAX (ID_BEAM_ASSOCIATION) AS MAX_ID FROM SAT_BEAM_ASSOCIATION";
		System.out.println("query " + query);

		try {
			con = checkConn();
			st = con.prepareStatement(query);
			ResultSet rs = st.executeQuery();
			System.out.println("rs " + rs);

			if (rs.next()) {
				maxId = rs.getInt("MAX_ID") + 1;
			}

			// close result set
			if (rs != null) {
				rs.close();
			}
			// close statement
			st.close();

			// Integer.parseInt(sensorModeNames[16].trim());
			// query string
			
			/*
			 * 
			 *         ID_BEAM_ASSOCIATION NUMBER(11) NOT NULL,
        SATELLITE NUMBER(10),
        BEAM NUMBER(11),
		
			 */
			String uploadSensorMode = "INSERT into SAT_BEAM_ASSOCIATION (ID_BEAM_ASSOCIATION, SATELLITE,BEAM) VALUES ('"
					+ maxId + "','" + satId + "'" + "," + beamId + " )";

			con = checkConn();
			con.setAutoCommit(false);

			st = con.prepareStatement(uploadSensorMode);
			// executing query
			st.execute();

			// closing
			st.close();
			st = null;

		} catch (Exception ex)

		{
		// rethrow
		this.tm.critical(EventType.SOFTWARE_EVENT, "Error import csv to BEAM table: ", ex.getMessage());
		throw ex;
	} // end catch
	finally {
		// clse statement
		if (st != null) {
			st.close();
		}
		con.close();
	} // end finally
		
		return maxId;
	}
	public static boolean checkIfIsDifferent(String existBeam, String dummyBeam) {
		// TODO Auto-generated method stub
		boolean changed = false;
//		System.out.println("check between exist beam : " + existBeam);
//		System.out.println("and dummyBean : " + dummyBean);

		if (!existBeam.equalsIgnoreCase(dummyBeam)) {
			changed = true;
		}
		return changed;
	}

	private BeamBean existBeam(List<BeamBean> allBeamsList, BeamBean dummyBean) {
		BeamBean beamReturned = null;
		for (int i = 0; i < allBeamsList.size(); i++) {
			BeamBean currentBeam = allBeamsList.get(i);
			if (currentBeam.getBeamName().equalsIgnoreCase(dummyBean.getBeamName())) {
				beamReturned = currentBeam;
				break;
			}
		}
		return beamReturned;
	}

	private BeamBean createBeam(int idBeam, String beamName, double nearOffNadir, double farOffNadir, int idSensorMode,
			int isEnabled, double swDim1, double swDim2, int dtoMinDuration, int dtoMaxDuration, int resTime,
			int dtoDurationSquared) {
		BeamBean beam = new BeamBean();
		beam.setIdBeam(idBeam);
		beam.setBeamName(beamName);
		beam.setNearOffNadir(nearOffNadir);
		beam.setFarOffNadir(farOffNadir);
		beam.setSensorMode(idSensorMode);
		beam.setIsEnabled(isEnabled);
		beam.setSwDim1(swDim1);
		beam.setSwDim2(swDim2);
		beam.setDtoMinDuration(dtoMinDuration);
		beam.setDtoMaxDuration(dtoMaxDuration);
		beam.setResTime(resTime);
		beam.setDtoDurationSquared(dtoDurationSquared);
		return beam;
	}

	/**
	 * Import beam form file
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param csvFile
	 * @throws Exception
	 */
	public void alterTableBeam() throws Exception {
		PreparedStatement pstm = null;
		try {
			if (con == null || con.isClosed()) {
				con = super.initConnection();
			}
			// set autocommit to false
			con.setAutoCommit(false);

			String alterTable = "ALTER TABLE BEAM ADD DTO_DURATION_SQUARED NUMBER(11) DEFAULT 0";

			System.out.println(alterTable);
			pstm = con.prepareStatement(alterTable);
			// executing query
			pstm.execute();
			pstm.close();
			pstm = null;

		} // end try
		catch (SQLException ex) {
			// rethrow
			this.tm.critical(EventType.SOFTWARE_EVENT, "Error alter table BEAM : ", ex.getMessage());
			throw ex;
		} // end catch
		finally {
			// clse statement
			if (pstm != null) {
				pstm.close();
			}
			con.close();
		} // end finally
	}// end method

	public void updateDatabase() throws Exception {
		// ALTER TABLE ADD COLUMN TO BEAM
		alterTableAddColumnBeam();

		// get all beamId
		List<BeamBean> allBeams = getBeamsSatellite();
		// for every beam_id

		for (int i = 0; i < allBeams.size(); i++) {
			int sensorModeId = allBeams.get(i).getSensorMode();
			// SELECT SW_DIM1, SW_DIM2, DTO_MIN_DURATION, DTO_MAX_DURATION, RES_TIME FROM
			// SENSOR_MODE WHERE ID_SENSOR_MODE =
			int SW_DIM1 = 0;
			int SW_DIM2 = 0;
			int DTO_MIN_DURATION = 0;
			int DTO_MAX_DURATION = 0;
			int RES_TIME = 0;
			int IS_ENABLED = 0;
//			int NEAR_OFF_NADIR_SAT = 0;
//			int FAR_OFF_NADIR_SAT = 0;
			String query = "SELECT SW_DIM1, SW_DIM2, DTO_MIN_DURATION, DTO_MAX_DURATION, RES_TIME FROM SENSOR_MODE WHERE ID_SENSOR_MODE = "
					+ sensorModeId + ";";
			System.out.println(query);

			try {

				con = checkConn();
				PreparedStatement st = con.prepareStatement(query);
				ResultSet rs = st.executeQuery();

				while (rs.next()) {
					// getting id
					SW_DIM1 = rs.getInt("SW_DIM1");
					SW_DIM2 = rs.getInt("SW_DIM2");
					DTO_MIN_DURATION = rs.getInt("DTO_MIN_DURATION");
					DTO_MAX_DURATION = rs.getInt("DTO_MAX_DURATION");
					RES_TIME = rs.getInt("RES_TIME");
				} // end while

				// close result set
				if (rs != null) {
					rs.close();
				}
				// close statement
				st.close();

				int beamId = allBeams.get(i).getIdBeam();
				// SELECT IS_ENABLED, NEAR_OFF_NADIR_SAT, FAR_OFF_NADIR_SAT FROM
				// SAT_BEAM_ASSOCIATION WHERE BEAM ==

				// query = "SELECT IS_ENABLED, NEAR_OFF_NADIR_SAT, FAR_OFF_NADIR_SAT FROM
				// SAT_BEAM_ASSOCIATION WHERE BEAM == " + beamId+";";

				query = "SELECT IS_ENABLED FROM SAT_BEAM_ASSOCIATION WHERE BEAM == " + beamId + ";";
				System.out.println(query);

				con = checkConn();
				st = con.prepareStatement(query);
				rs = st.executeQuery();

				while (rs.next()) {
					// getting id
					IS_ENABLED = rs.getInt("IS_ENABLED");
//								NEAR_OFF_NADIR_SAT = rs.getInt("NEAR_OFF_NADIR_SAT");
//								FAR_OFF_NADIR_SAT = rs.getInt("FAR_OFF_NADIR_SAT");
				} // end while

				// close result set
				if (rs != null) {
					rs.close();
				}
				// close statement
				st.close();

				// update BEAM set SW_DIM1, SW_DIM2...
				query = "UPDATE BEAM SET IS_ENABLED = " + IS_ENABLED + ", SW_DIM1=" + SW_DIM1 + ", SW_DIM2 =" + SW_DIM2
						+ ", DTO_MIN_DURATION=" + DTO_MIN_DURATION + ",DTO_MAX_DURATION=" + DTO_MAX_DURATION
						+ ",RES_TIME=" + RES_TIME + " WHERE ID_BEAM == " + beamId + " AND SENSOR_MODE ==" + sensorModeId
						+ ";";
				System.out.println(query);
				st = con.prepareStatement(query);
				st.execute();
				st.close();
				st = null;
			} // end try
			catch (Exception e) {
				// rethrow
				this.tm.critical(EventType.SOFTWARE_EVENT,
						"Execution error of the query by prepared statement: " + query, e.getMessage());
				throw e;
			} // end catch
			finally {
				con.close();
			}

		}
		alterTableDropColumn();

	}

	private void alterTableDropColumn() throws Exception {
		PreparedStatement pstm = null;
		try {
			// ALTER TABLE DROP COLUMN FROM SENSOR_MODE
			// ALTER TABLE DROP COLUMN FROM SAT_BEAM_ASSOCIATION
			con = checkConn();
			String alterTable = "ALTER TABLE SENSOR_MODE DROP SW_DIM1, DROP SW_DIM2, DROP DTO_MIN_DURATION,DROP DTO_MAX_DURATION,DROP RES_TIME;";
			System.out.println(alterTable);
			pstm = con.prepareStatement(alterTable);
			pstm.execute();
			pstm.close();
			pstm = null;

			con = checkConn();
			alterTable = "ALTER TABLE SAT_BEAM_ASSOCIATION DROP IS_ENABLED, DROP NEAR_OFF_NADIR_SAT,DROP FAR_OFF_NADIR_SAT;";
			System.out.println(alterTable);
			pstm = con.prepareStatement(alterTable);
			pstm.execute();
			pstm.close();
			pstm = null;

		} // end try
		catch (SQLException ex) {
			// rethrow
			this.tm.critical(EventType.SOFTWARE_EVENT, "Error alter table drop column : ", ex.getMessage());
			throw ex;
		} // end catch
		finally {
			// clse statement
			if (pstm != null) {
				pstm.close();
			}
			con.close();
		} // end finally
	}

	private void alterTableAddColumnBeam() throws Exception {
		PreparedStatement pstm = null;
		try {
			con = checkConn();
			String alterTable = "ALTER TABLE BEAM ADD IS_ENABLED NUMBER(1) DEFAULT 0";
			System.out.println(alterTable);
			pstm = con.prepareStatement(alterTable);
			pstm.execute();
			pstm.close();
			pstm = null;

			con = checkConn();
			alterTable = "ALTER TABLE BEAM ADD SW_DIM1 NUMBER(11,4) DEFAULT 0";
			System.out.println(alterTable);
			pstm = con.prepareStatement(alterTable);
			pstm.execute();
			pstm.close();
			pstm = null;

			con = checkConn();
			alterTable = "ALTER TABLE BEAM ADD SW_DIM2 NUMBER(11,4) DEFAULT 0";
			System.out.println(alterTable);
			pstm = con.prepareStatement(alterTable);
			pstm.execute();
			pstm.close();
			pstm = null;

			con = checkConn();
			alterTable = "ALTER TABLE BEAM ADD DTO_MIN_DURATION NUMBER(11) DEFAULT 0";
			System.out.println(alterTable);
			pstm = con.prepareStatement(alterTable);
			pstm.execute();
			pstm.close();
			pstm = null;

			con = checkConn();
			alterTable = "ALTER TABLE BEAM ADD DTO_MAX_DURATION NUMBER(11) DEFAULT 0";
			System.out.println(alterTable);
			pstm = con.prepareStatement(alterTable);
			pstm.execute();
			pstm.close();
			pstm = null;

			con = checkConn();
			alterTable = "ALTER TABLE BEAM ADD RES_TIME NUMBER(11) DEFAULT 0";
			System.out.println(alterTable);
			pstm = con.prepareStatement(alterTable);
			pstm.execute();
			pstm.close();
			pstm = null;

			con = checkConn();
			alterTable = "ALTER TABLE BEAM ADD DTO_DURATION_SQUARED NUMBER(11) DEFAULT 0";
			System.out.println(alterTable);
			pstm = con.prepareStatement(alterTable);
			pstm.execute();
			pstm.close();
			pstm = null;

		} // end try
		catch (SQLException ex) {
			// rethrow
			this.tm.critical(EventType.SOFTWARE_EVENT, "Error alter table BEAM : ", ex.getMessage());
			throw ex;
		} // end catch
		finally {
			// clse statement
			if (pstm != null) {
				pstm.close();
			}
			con.close();
		} // end finally
	}

	private Connection checkConn() throws Exception {
		if (con == null || con.isClosed()) {
			con = super.initConnection();
		}
		// set autocommit to false
		con.setAutoCommit(false);
		return con;
	}

	/**
	 * Populate satellite beam association table
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param csvFile
	 * @throws Exception
	 */
	public void importConfigurationSatelliteBeamAssCsv(File csvFile) throws Exception {
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = "|";
		PreparedStatement pstmt = null;
		int i = 0;
		try {

			String[] satBeamAssociation;
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
				if (!line.startsWith(StartComment)) {
					i++;
					// use comma as separator
					satBeamAssociation = line.split("\\" + cvsSplitBy);

					if (satBeamAssociation.length > 10) {
						// extracint assiciation
						String satelliteName = satBeamAssociation[2].trim();
						// extracting beam
						String beamName = satBeamAssociation[8].trim();

						// get beam id
						int idBeam = getIdBeam(beamName);

						int idSatellite = getIdSatellite(satelliteName);

						// Query String
						String uploadAss = "INSERT into SAT_BEAM_ASSOCIATION (ID_BEAM_ASSOCIATION, SATELLITE,BEAM) VALUES ("
								+ i + "," + idSatellite + "," + idBeam + ")";
						// building statement

						if (con == null || con.isClosed()) {
							// System.out.println(" connection was closed");

							con = super.initConnection();
						}

						con.setAutoCommit(false);
						// System.out.println("after autocommit");

						// Statement statement = con.createStatement();

						// Esecuzione di un comando sul DB
						// ResultSet rs = statement.executeQuery(uploadAss);

						//

						// //System.out.println(uploadBeam);
						pstmt = con.prepareStatement(uploadAss);

						// executing query
						pstmt.execute();
						pstmt.close();
						pstmt = null;

					} // end if
				} // end if

			} // end while
		} // end try
		catch (SQLException | IOException ex) {
			// rethrowing
			this.tm.critical(EventType.SOFTWARE_EVENT, "Error import csv to SAT BEAM ASSOCIATION table: ",
					ex.getMessage());
			throw ex;
		} // clase catch
		finally {
			// clse statement
			if (pstmt != null) {
				pstmt.close();
			}
			// close reader
			if (br != null) {
				br.close();
			}
			con.close();
		} // end finally

	}// end method

	/**
	 * Return Mission ID given its name
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param missionName
	 * @return ID
	 * @throws Exception
	 */
	public int getIdMission(String missionName) throws Exception {
		// value to be returned
		int idMission = 0;
		// Query string
		String query = "SELECT ID_MISSION  from MISSION  where MISSION_NAME = " + "'" + missionName + "'";
		try {

			// ManagerLogger.logDebug(this, query);
			if (con == null || con.isClosed()) {
				con = super.initConnection();
			}
			PreparedStatement st = con.prepareStatement(query);
			ResultSet rs = null;

			try {
				// executinhg query
				rs = st.executeQuery();

				while (rs.next()) {
					// retrieving ID
					idMission = rs.getInt("ID_MISSION");

				} // end while

			} // end try
			finally {
				// close result set
				if (rs != null) {
					rs.close();
				}
				// close statement
				st.close();
				con.close();
			} // end finally
		} // end try
		catch (Exception e) {
			// rethrowing
			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: " + query,
					e.getMessage());
			throw e;

		} // end catch
		return idMission;
	}// end method

	/**
	 * Return ID satelllite given its name
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param satelliteName
	 * @return
	 * @throws Exception
	 */
	public int getIdSatellite(String satelliteName) throws Exception {
		// value to be returned
		int idSatellite = 0;
		// query string
		String query = "SELECT ID_SATELLITE  from SATELLITE  where   SATELLITE_NAME = " + "'" + satelliteName + "'";
		try {

			// ManagerLogger.logDebug(this, query);
			if (con == null || con.isClosed()) {
				con = super.initConnection();
			}
			PreparedStatement st = con.prepareStatement(query);
			ResultSet rs = null;

			try {
				// execute query
				rs = st.executeQuery();

				while (rs.next()) {
					// getting id
					idSatellite = rs.getInt("ID_SATELLITE");

				} // end while

			} // end try
			finally {
				// close result set
				if (rs != null) {
					rs.close();
				}
				// close statement
				st.close();
				con.close();
			} // end finally
		} catch (Exception e)

		{
			// rethrowing
			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: " + query,
					e.getMessage());
			throw e;

		} // end catch
		return idSatellite;
	}// end method

	/**
	 * Return beam id given its name
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param beamName
	 * @return ID
	 * @throws Exception
	 */
	public int getIdBeam(String beamName) throws Exception {

		int idBeam = 0;
		// query string
		String query = "SELECT ID_BEAM  from BEAM  where   BEAM_NAME = " + "'" + beamName + "'";
		try {

			// ManagerLogger.logDebug(this, query);
			if (con == null || con.isClosed()) {
				con = super.initConnection();
			}
			PreparedStatement st = con.prepareStatement(query);
			ResultSet rs = null;

			// execting query
			rs = st.executeQuery();

			while (rs.next()) {
				// getting id
				idBeam = rs.getInt("ID_BEAM");

			} // end while

			// close result set
			if (rs != null) {
				rs.close();
			}
			// close statement
			st.close();

		} // end try
		catch (Exception e) {
			// rethrow
			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: " + query,
					e.getMessage());
			throw e;
		} // end catch
			// returning
		finally {
			con.close();

		}
		return idBeam;
	}// end method

	/**
	 * Delete all sensor modes . On cascade table BEAM and SAT_BEAM_ASSOCIATION are
	 * deleted
	 * 
	 * @throws Exception
	 */
	public void deleteSensorModes() throws Exception {

		// delete statement
		String query = "delete from SENSOR_MODE";
		try {
			if (con == null || con.isClosed()) {
				con = super.initConnection();
			}
			// ManagerLogger.logDebug(this, query);
			// managed query
			PreparedStatement st = con.prepareStatement(query);
			ResultSet rs = null;

			try {
				// deleting tables
				rs = st.executeQuery();
			} // end try
			finally {
				// close statement
				if (rs != null) {
					rs.close();
				}

				st.close();

			} // end finally
		} // end try
		catch (Exception e) {
			// rethrow
			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: " + query,
					e.getMessage());
			throw e;

		} // end catch
		finally {
			con.close();
		}
	}// end method

	/**
	 * Retirn the sensormode id given its name
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param sensorModeName
	 * @return sensor mode id
	 * @throws Exception
	 */
	public int getIdSensorMode(String sensorModeName) throws Exception {
		// value to be returned
		int idSensorMode = -1;
		// query string
		String query = "SELECT ID_SENSOR_MODE from SENSOR_MODE where SENSOR_MODE_NAME = " + "'" + sensorModeName + "'";
		try {

			// ManagerLogger.logDebug(this, query);
			if (con == null || con.isClosed()) {
				con = super.initConnection();
			}
			PreparedStatement st = con.prepareStatement(query);
			ResultSet rs = null;

			try {
				// executing query
				rs = st.executeQuery();

				while (rs.next()) {
					// getting sensor mode
					idSensorMode = rs.getInt("ID_SENSOR_MODE");

				} // end while

			} // end try
			finally {
				// close result set
				if (rs != null) {
					rs.close();
				}
				// close statement
				st.close();

			} // end finally
		} catch (Exception e)

		{
			// rethrow
			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: " + query,
					e.getMessage());
			throw e;

		} // end catch
			// returning
		finally {
			con.close();
		}
		return idSensorMode;
	}// end method

	
	
	/**
	 * Retirn the sensormode id given its name
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param sensorModeName
	 * @return sensor mode id
	 * @throws Exception
	 */
	public String getSensorModeName(int sensorModeId) throws Exception {
		// value to be returned
		String idSensorMode = null;
		// query string
		String query = "SELECT SENSOR_MODE_NAME from SENSOR_MODE where ID_SENSOR_MODE = " + "'" + sensorModeId + "'";
		try {

			// ManagerLogger.logDebug(this, query);
			if (con == null || con.isClosed()) {
				con = super.initConnection();
			}
			PreparedStatement st = con.prepareStatement(query);
			ResultSet rs = null;

			try {
				// executing query
				rs = st.executeQuery();

				while (rs.next()) {
					// getting sensor mode
					idSensorMode = rs.getString("SENSOR_MODE_NAME");

				} // end while

			} // end try
			finally {
				// close result set
				if (rs != null) {
					rs.close();
				}
				// close statement
				st.close();

			} // end finally
		} catch (Exception e)

		{
			// rethrow
			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: " + query,
					e.getMessage());
			throw e;

		} // end catch
			// returning
		finally {
			con.close();
		}
		return idSensorMode;
	}// end method

	/**
	 * Return the list of mission ID
	 * 
	 * @return the list of id
	 * @throws Exception
	 */
	public ArrayList<Integer> getListIdMission() throws Exception {

		int idMission = 0;
		// query string
		String query = "SELECT ID_MISSION  from MISSION  ";
		// list to be returned
		ArrayList<Integer> listIdMissions = new ArrayList<Integer>();

		try {

			// ManagerLogger.logDebug(this, query);
			if (con == null || con.isClosed()) {
				con = super.initConnection();
			}
			PreparedStatement st = con.prepareStatement(query);
			ResultSet rs = null;

			try {
				// executing query
				rs = st.executeQuery();

				while (rs.next()) {
					// get mission id
					idMission = rs.getInt("ID_MISSION");
					// add to list
					listIdMissions.add(idMission);
				} // end while

			} // end try
			finally {
				// closing result set
				if (rs != null) {
					rs.close();
				}
				// closing statement
				st.close();
				con.close();
			} // end finally
		} catch (Exception e)

		{
			con.close();
			// rethrowing
			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: " + query,
					e.getMessage());
			throw e;

		} // end catch
			// returning
		return listIdMissions;
	}// end method

	/**
	 * Check if configuration file is well formed
	 * 
	 * @param csvFile
	 * @throws Exception
	 */
	public void dataControlCsv(File csvFile) throws Exception {
		// reader
		BufferedReader br1 = null;
		String line = "";
		// split line
		String cvsSplitBy = "|";
		// mission data
		String[] missionSatData;
		br1 = new BufferedReader(new FileReader(csvFile));
		// LinkedHashSet<String> allMission = new LinkedHashSet<String>();
		//
		// int idMission = 0;
		// current row index
		int currentrow = 0;
		try { // reading file
				// for each line
			while ((line = br1.readLine()) != null) {
				// update current row index
				currentrow++;
				if (!line.startsWith(StartComment)) {
					// not line of comment
					// use | as separator and split line
					missionSatData = line.split("\\" + cvsSplitBy);

					// iterate on tokens field
					for (int i = 0; i < NumberOfExpectedEntryInLine; i++) {
						// retrieving field
						String fieldName = missionSatData[i].trim();

						if (fieldName.equalsIgnoreCase("")) {
							// found empty field
							// log and throw exception
							this.tm.critical(EventType.SOFTWARE_EVENT, "Error import csv to DataManager tables: ",
									"There is Empty field");
							throw new Exception(
									"Error import csv to DataManager tables: There is Empty field.Check row "
											+ currentrow);
						}
						// this field have YES/NO possible values
						if ((i == 4) || (i == 11) || (i == 14)) {
							if (!(fieldName.equalsIgnoreCase("YES") || fieldName.equalsIgnoreCase("NO"))) {
								// Values not allowed thowing exception
								this.tm.critical(EventType.SOFTWARE_EVENT, "Error import csv to DataManager tables: ",
										"Data Malformed at Is Enabled field");
								throw new Exception(
										"Error import csv to DataManager tables: Data Malformed at Is Enabled field.Check row "
												+ currentrow);
							} // end if
						} // end if
							// filed 5 can have values : right /left/both
						if (i == 5) {
							if (!(fieldName.equalsIgnoreCase("right") || fieldName.equalsIgnoreCase("left")
									|| fieldName.equalsIgnoreCase("both"))) {
								// values not allowed
								// log then throwing exception
								this.tm.critical(EventType.SOFTWARE_EVENT, "Error import csv to DataManager tables: ",
										"Data Malformed at Allowed Look Side field");
								throw new Exception(
										"Error import csv to DataManager tables: Data Malformed at Allowed Look Side field. Check row "
												+ currentrow);
							} // end if
						} // end if

					} // end for

				} // end if
			} // end while
		} // end try
		finally {
			// close file
			if (br1 != null) {
				br1.close();
			}
		} // end finally

	}// end method

}// end class
