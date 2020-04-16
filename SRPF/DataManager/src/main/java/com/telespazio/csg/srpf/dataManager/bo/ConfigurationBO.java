/**
*
* MODULE FILE NAME:	ConfigurationBO.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Define the Businnes Object for the Configuration model stored on DB
*
* PURPOSE:			Used for DB data
*
* CREATION DATE:	11-01-2016
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
* 20-04-2018|  Amedeo Bancone  |2.0 | Added method updateSensorModesConfiguration
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.dataManager.bo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.naming.NamingException;

import com.telespazio.csg.srpf.dataManager.bean.BeamBean;
import com.telespazio.csg.srpf.dataManager.bean.SatelliteBean;
import com.telespazio.csg.srpf.dataManager.bean.SensorModeBean;
import com.telespazio.csg.srpf.dataManager.dao.ConfigurationDao;
import com.telespazio.csg.srpf.dataManager.util.DataManagerConstants;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;

/**
 * Define the Businnes Object for the Configuration model stored on DB
 * 
 * @author Abed Alissa
 * @version 2.0
 *
 */
public class ConfigurationBO {

	// log class
	TraceManager tm = new TraceManager();

	/**
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @throws Exception
	 */
	public void exportConfiguration(int idMission, FileWriter fw) throws Exception {

		ConfigurationDao dao = null;

		SatelliteBO satelliteBO;
		ArrayList satellitesPerSensorModeList = new ArrayList();
		ArrayList beamsSatelliteList = new ArrayList();
		ArrayList allDataList = null;

		try {
			dao = new ConfigurationDao();

			satelliteBO = new SatelliteBO();

			ArrayList listSensorsMode = satelliteBO.getSensorsMode();

			Map<Integer, Object[]> data = new TreeMap<>();
			int x = 0;
			for (int i = 0; i < listSensorsMode.size(); i++) {
				x++;

				satellitesPerSensorModeList = satelliteBO.getSatellitesPerSensorModeConfiguration(
						((SensorModeBean) listSensorsMode.get(i)).getIdsensorMode(), idMission);

				for (int k1 = 0; k1 < satellitesPerSensorModeList.size(); k1++) {
					allDataList = new ArrayList();

					allDataList.add(((SatelliteBean) satellitesPerSensorModeList.get(k1)).getMissionName());
					allDataList.add((((SensorModeBean) listSensorsMode.get(i)).getSensorModeName()));
					allDataList.add(((SatelliteBean) satellitesPerSensorModeList.get(k1)).getSatelliteName());
					allDataList.add(((SatelliteBean) satellitesPerSensorModeList.get(k1)).getTrackOffset());
					if (((SatelliteBean) satellitesPerSensorModeList.get(k1))
							.getIsEnabled() == DataManagerConstants.ENABLED) {
						allDataList.add("Yes");
					} else {
						allDataList.add("No");
					}
					if (((SatelliteBean) satellitesPerSensorModeList.get(k1))
							.getIdAllowedLookSide() == DataManagerConstants.ID_ALLOWED_lOOK_RIGHT) {
						allDataList.add("Right");
					} else if (((SatelliteBean) satellitesPerSensorModeList.get(k1))
							.getIdAllowedLookSide() == DataManagerConstants.ID_ALLOWED_lOOK_LEFT) {
						allDataList.add("Left");
					} else {
						allDataList.add("Both");
					}
					beamsSatelliteList = satelliteBO.getBeamsExportConfiguration(
							((SensorModeBean) listSensorsMode.get(i)).getIdsensorMode(),
							((SatelliteBean) satellitesPerSensorModeList.get(k1)).getIdSatellite());
					for (int kAll = 0; kAll < beamsSatelliteList.size(); kAll++) {

						if (allDataList.size() > 8) {
							allDataList.remove(17);

							allDataList.remove(16);
							allDataList.remove(15);
							allDataList.remove(14);
							allDataList.remove(13);
							allDataList.remove(12);
							allDataList.remove(11);
							allDataList.remove(10);
							allDataList.remove(9);
							allDataList.remove(8);
							allDataList.remove(7);
							allDataList.remove(6);
						} // end if
						allDataList.add(((BeamBean) beamsSatelliteList.get(kAll)).getNearOffNadir());
						allDataList.add(((BeamBean) beamsSatelliteList.get(kAll)).getFarOffNadir());
						allDataList.add(((BeamBean) beamsSatelliteList.get(kAll)).getBeamName());
						allDataList.add(((BeamBean) beamsSatelliteList.get(kAll)).getNearOffNadirBeam());
						allDataList.add(((BeamBean) beamsSatelliteList.get(kAll)).getFarOffNadirBeam());
						// allDataList.add(((BeamBean)
						// beamsSatelliteList.get(kAll)).getIsEnabled());

						if (((BeamBean) beamsSatelliteList.get(kAll))
								.getIsEnabled() == DataManagerConstants.ID_ALLOWED_lOOK_RIGHT) {
							allDataList.add("Yes");
						} // end if
						else {
							allDataList.add("No");
						} // end else

						allDataList.add(((BeamBean) beamsSatelliteList.get(kAll)).getSwDim1());
						allDataList.add(((BeamBean) beamsSatelliteList.get(kAll)).getSwDim2());
						if (((BeamBean) beamsSatelliteList.get(kAll)).isSpotLight()) {
							allDataList.add("Yes");
						} // end if
						else {
							allDataList.add("No");
						} // end else

						allDataList.add(((BeamBean) beamsSatelliteList.get(kAll)).getDtoMinDuration());
						allDataList.add(((BeamBean) beamsSatelliteList.get(kAll)).getDtoMaxDuration());
						allDataList.add(((BeamBean) beamsSatelliteList.get(kAll)).getResTime());
						allDataList.add(((BeamBean) beamsSatelliteList.get(kAll)).getDtoDurationSquared());

						data.put(x++, allDataList.toArray());

					} // end for list beams
						// data.put(x++, allDataList.toArray());
				} // end for list sat

			} // end for list sensors

			// Iterate over data and write to sheet
			Set<Integer> keyset = data.keySet();

			// String exportfileName = "e:\\dataManagerConfiguration.csv";

			// FileWriter fw = new FileWriter(exportfileName);
			/*
			 * Object[] objArr1 = { "Mission", "Sensor Mode",
			 * "Satellite Name","Track Offset", "Is Enabled Sat", "Allowed Look Side",
			 * "Near Off Nadir SAT", "Far Off Nadir SAT", "Beam Name",
			 * "Near Off Nadir Beam", "Far Off Nadir Beam", "Is Enabled Beam", "Sw Dim1",
			 * "Sw Dim2", "Is Spot Light", "Dto Min duration", "Dto Max Duration",
			 * "Res Time" }; for (Object obj1 : objArr1) {
			 * 
			 * fw.append((String) obj1); fw.append("|"); } // end for
			 * 
			 * // fw.flush(); fw.append(System.getProperty("line.separator"));
			 */
			for (Integer key : keyset) {

				Object[] objArr = data.get(key);

				for (Object obj : objArr) {

					fw.append(obj.toString());
					// fw.append(" | ");
					fw.append("|");

				} // end for
				fw.append(System.getProperty("line.separator"));

			} // end for

		} // end try
		finally {
			dao.closeConnection();

			this.tm.debug("Configuration  written successfully on disk for mission ");
		} // end finally

	}// end method

	/**
	 * Insert the header to configuration file
	 * 
	 * @param fw
	 * @throws IOException
	 */
	private void insertConfigurationHeader(FileWriter fw) throws IOException {

		// header of configuration file
		Object[] objArr1 = { "Mission", "Sensor Mode", "Satellite Name", "Track Offset", "Is Enabled Sat",
				"Allowed Look Side", "Near Off Nadir SAT", "Far Off Nadir SAT", "Beam Name", "Near Off Nadir Beam",
				"Far Off Nadir Beam", "Is Enabled Beam", "Sw Dim1", "Sw Dim2", "Is Spot Light", "Dto Min duration",
				"Dto Max Duration", "Res Time" };
		// for each element
		for (Object obj1 : objArr1) {
			// insert header for column
			fw.append((String) obj1);
			// insert separator
			fw.append("|");
		} // end for
		fw.append(System.getProperty("line.separator"));

	}// end method

	/**
	 * Fill table with configuration file
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @since
	 * @param path of configuration file
	 * @throws NamingException
	 * @throws Exception
	 */
	public void importConfiguration(String path)

	{
		// fill the tables starting from a configuration file
		// create dao
		ConfigurationDao dao = null;

		tm.debug("uploading csv data " + path);
		// retrieve file
		File csvFile = new File(path);
		try {
			dao = new ConfigurationDao();
			Date date = new Date();
			System.out.println("dataControlCsv start");

			// check if file is well formed
			dao.dataControlCsv(csvFile);
			Date date1 = new Date();
			long diff = date1.getTime() - date.getTime();
			System.out.println("time for dataControlCsv : " + diff);
			// delete tables
			date1 = new Date();
			System.out.println("deleteTablesConfiguration start");

			dao.deleteTablesConfiguration();
			date = new Date();
			diff = date.getTime() - date1.getTime();
			System.out.println("time for deleteTablesConfiguration : " + diff);

			// fill mission
			System.out.println("importConfigurationMissionCsv start");

			dao.importConfigurationMissionCsv(csvFile);
			date1 = new Date();
			diff = date1.getTime() - date.getTime();
			System.out.println("time for importConfigurationMissionCsv : " + diff);

			System.out.println("importConfigurationSensoreModeCsv start");
			// fill sensor modes
			dao.importConfigurationSensoreModeCsv(csvFile);
			date = new Date();
			diff = date.getTime() - date1.getTime();

			System.out.println("time for importConfigurationSensoreModeCsv : " + diff);

			System.out.println("importConfigurationSatelliteCsv start");
			// fill satellite
			dao.importConfigurationSatelliteCsv(csvFile);
			date1 = new Date();
			diff = date1.getTime() - date.getTime();
			System.out.println("time for importConfigurationSatelliteCsv : " + diff);

			System.out.println("importConfigurationSatelliteCsv start");
			// fill beams
			dao.importConfigurationBeamCsv(csvFile);
			date = new Date();
			diff = date.getTime() - date1.getTime();
			System.out.println("time for importConfigurationSatelliteCsv : " + diff);

			// create beam satellite association
			dao = new ConfigurationDao();
			System.out.println("importConfigurationSatelliteBeamAssCsv start");

			dao.importConfigurationSatelliteBeamAssCsv(csvFile);
			date1 = new Date();
			diff = date1.getTime() - date.getTime();
			System.out.println("time for importConfigurationSatelliteBeamAssCsv : " + diff);

		} // end try

		catch (Exception ex) {
			// ManagerLogger.logError(this, "Connection error: " + ex);
			// log error
			tm.critical(EventType.SOFTWARE_EVENT, "Connection error: ", ex.getMessage());
			// rollback
			if (dao != null) {
				dao.rollback();
			}
			// rethrow exception
		} finally {
			// close transaction
			if (dao != null) {
				dao.closeTransaction();
				// close connection
				dao.closeConnection();
			}
		} // end finaally

	}// end method

	/**
	 * Fill table with configuration file
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @since
	 * @param path of configuration file
	 * @throws NamingException
	 * @throws Exception
	 */
	public void updateBeamTable(String path) throws NamingException, Exception

	{
		// fill the tables starting from a configuration file
		// create dao
		ConfigurationDao dao = new ConfigurationDao();

		this.tm.debug("uploading csv data " + path);
		// retrieve file
		File csvFile = new File(path);
		try {
			Date date = new Date();
			System.out.println("dataControlCsv start");

			// check if file is well formed
			dao.dataControlCsv(csvFile);
			Date date1 = new Date();
			long diff = date1.getTime() - date.getTime();
			System.out.println("time for dataControlCsv : " + diff);
			// delete tables
			date1 = new Date();
			System.out.println("deleteTablesConfiguration start");

			dao.deleteTablesConfiguration();
			date = new Date();
			diff = date.getTime() - date1.getTime();
			System.out.println("time for deleteTablesConfiguration : " + diff);

			// fill mission
			System.out.println("importConfigurationMissionCsv start");

			dao.importConfigurationMissionCsv(csvFile);
			date1 = new Date();
			diff = date1.getTime() - date.getTime();
			System.out.println("time for importConfigurationMissionCsv : " + diff);

			System.out.println("importConfigurationSensoreModeCsv start");
			// fill sensor modes
			dao.importConfigurationSensoreModeCsv(csvFile);
			date = new Date();
			diff = date.getTime() - date1.getTime();

			System.out.println("time for importConfigurationSensoreModeCsv : " + diff);

			System.out.println("importConfigurationSatelliteCsv start");
			// fill satellite
			dao.importConfigurationSatelliteCsv(csvFile);
			date1 = new Date();
			diff = date1.getTime() - date.getTime();
			System.out.println("time for importConfigurationSatelliteCsv : " + diff);

			System.out.println("importConfigurationSatelliteCsv start");
			// fill beams
			dao.importConfigurationBeamCsv(csvFile);
			date = new Date();
			diff = date.getTime() - date1.getTime();
			System.out.println("time for importConfigurationSatelliteCsv : " + diff);

			// create beam satellite association
			dao = new ConfigurationDao();
			System.out.println("importConfigurationSatelliteBeamAssCsv start");

			dao.importConfigurationSatelliteBeamAssCsv(csvFile);
			date1 = new Date();
			diff = date1.getTime() - date.getTime();
			System.out.println("time for importConfigurationSatelliteBeamAssCsv : " + diff);

		} // end try

		catch (SQLException ex) {
			// ManagerLogger.logError(this, "Connection error: " + ex);
			// log error
			this.tm.critical(EventType.SOFTWARE_EVENT, "Connection error: ", ex.getMessage());
			// rollback
			dao.rollback();
			// rethrow exception
			throw ex;
		} // end catch
		finally {
			// close transaction
			dao.closeTransaction();
			// close connection
			dao.closeConnection();
		} // end finaally

	}// end method

	/**
	 * Update SENSOR_MODE BEAM and SAT_BEAM:ASSOCIATON only by using the
	 * Configuration file
	 * 
	 * @param path
	 * @throws NamingException
	 * @throws Exception
	 */
	public void updateSensorModesConfiguration2(String path) throws NamingException, Exception

	{
		// fill the tables starting from a configuration file
		// create dao
		ConfigurationDao dao = new ConfigurationDao();

		this.tm.debug("uploading csv data " + path);
		File csvFile = new File(path);
		try {
			// check file
			dao.dataControlCsv(csvFile);

			this.tm.debug("Trying to  import csv data to DataManager DataBase table: " + "BEAM");
			// import beams

			List<BeamBean> allBeams = dao.getBeamsSatellite();
			dao.updateBeamCsv(allBeams, csvFile);
			this.tm.debug("Success import csv to DataManager DataBase table: " + "BEAM");

		} // end try

		catch (SQLException ex) {
			// ManagerLogger.logError(this, "Connection error: " + ex);
			this.tm.critical(EventType.SOFTWARE_EVENT, "Connection error: ", ex.getMessage());
			// rollback
			dao.rollback();
			// rethrow exception
			throw ex;
		} // end catch
		finally {
			// close transaction
			dao.closeTransaction();
			// close connection
			dao.closeConnection();
		} // end finally

	}// end method

	/**
	 * Update SENSOR_MODE BEAM and SAT_BEAM:ASSOCIATON only by using the
	 * Configuration file
	 * 
	 * @param path
	 * @throws NamingException
	 * @throws Exception
	 */
	public void updateSensorModesConfiguration(String path) throws NamingException, Exception

	{
		// fill the tables starting from a configuration file
		// create dao
		ConfigurationDao dao = new ConfigurationDao();

		this.tm.debug("uploading csv data " + path);
		File csvFile = new File(path);
		try {
			// check file
			dao.dataControlCsv(csvFile);

			// dao.deleteTablesConfiguration();
			// delete sensor modes and related tables
			dao.deleteSensorModes();

			this.tm.debug("Trying to  import csv data to DataManager DataBase table: " + "SENSOR_MODE");
			// import sensor modes
			dao.importConfigurationSensoreModeCsv(csvFile);
			this.tm.debug("Success import csv to DataManager DataBase table: " + "SENSOR_MODE");

			this.tm.debug("Trying to  import csv data to DataManager DataBase table: " + "BEAM");
			// import beams
			dao.importConfigurationBeamCsv(csvFile);
			this.tm.debug("Success import csv to DataManager DataBase table: " + "BEAM");

			this.tm.debug("Trying to  import csv data to DataManager DataBase table: " + "SAT_BEAM_ASSOCIATION");
			// create satellites beams association
			dao = new ConfigurationDao();
			dao.importConfigurationSatelliteBeamAssCsv(csvFile);
			this.tm.debug("Success import csv to DataBase table: " + "SAT_BEAM_ASSOCIATION");

		} // end try

		catch (SQLException ex) {
			// ManagerLogger.logError(this, "Connection error: " + ex);
			this.tm.critical(EventType.SOFTWARE_EVENT, "Connection error: ", ex.getMessage());
			// rollback
			dao.rollback();
			// rethrow exception
			throw ex;
		} // end catch
		finally {
			// close transaction
			dao.closeTransaction();
			// close connection
			dao.closeConnection();
		} // end finally

	}// end method

	/**
	 * Update SENSOR_MODE BEAM and SAT_BEAM:ASSOCIATON only by using the
	 * Configuration file
	 * 
	 * @param path
	 * @throws NamingException
	 * @throws Exception
	 */
	public void alterTableBeam() throws NamingException, Exception

	{
		// fill the tables starting from a configuration file
		// create dao
		System.out.println("alterTableBeam before  new ConfigurationDao " + new Date());
		ConfigurationDao dao = new ConfigurationDao();
		System.out.println("alterTableBeam after  new ConfigurationDao " + new Date());

		try {
			System.out.println("alterTableBeam before  alterTableBeam " + new Date());

			dao.alterTableBeam();
			System.out.println("alterTableBeam after alterTableBeam " + new Date());

		} // end try

		catch (SQLException ex) {
			// ManagerLogger.logError(this, "Connection error: " + ex);
			this.tm.critical(EventType.SOFTWARE_EVENT, "Connection error: ", ex.getMessage());
			// rollback
			dao.rollback();
			// rethrow exception
			throw ex;
		} // end catch
		finally {
			// close transaction
			dao.closeTransaction();
			// close connection
			dao.closeConnection();
		} // end finally

	}// end method

	/**
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @return
	 * @throws Exception
	 */
	public ArrayList getListIdMission() throws Exception {

		// DAO
		ConfigurationDao dao = null;

		// LIST to be returnd
		ArrayList listIdMissions = new ArrayList();

		this.tm.debug("Start: Get List IdMission");

		try {
			dao = new ConfigurationDao();
			// retirve list
			listIdMissions = dao.getListIdMission();

		} // end try
		catch (Exception e) {
			// log error
			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error : ", e.getMessage());
			// rethrow
			throw e;
		} // end catch
		finally {
			// always close connection
			if (dao != null) {
				dao.closeConnection();
			}
		} // end finally

		this.tm.debug("End: Get List IdMission");
		// return list
		return listIdMissions;
	} // end method

	/**
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param typeConfiguration
	 * @param filePath
	 * @throws NamingException
	 * @throws Exception
	 */
	public void importExportConfiguration(String typeConfiguration, String filePath) 

	{

		try {
			if (typeConfiguration.equalsIgnoreCase(DataManagerConstants.CONFIGURATION_EXPORT)) {

				ArrayList allMissions = new ArrayList();
				allMissions = this.getListIdMission();

				FileWriter fw = new FileWriter(filePath);

				// insert header in configuration file
				this.insertConfigurationHeader(fw);

				// for each missione dump the configuration
				for (int i = 0; i < allMissions.size(); i++) {
					int idMission = (int) allMissions.get(i);
					if (idMission > 0) {
						this.exportConfiguration(idMission, fw);
					} // end if

				} // end for
				fw.flush();
				fw.close();

			} // end if
			else if (typeConfiguration.equalsIgnoreCase(DataManagerConstants.CONFIGURATION_IMPORT)) {
				this.importConfiguration(filePath);

				SatelliteBO bo = new SatelliteBO();

				bo.initializeOBDATAFileNameTable();

			} // end else if
			else {
				this.tm.debug("Failed to find Parameters: ");
			}

		} // end try
		catch (Exception e) {
			// log
			this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error : ", e.getMessage());
		} // end catch

	} // end method

}// end class
