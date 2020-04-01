/**
*
* MODULE FILE NAME:	SatelliteBO.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Define the Businnes Object for the Satellite model stored on DB
*
* PURPOSE:			Used for DB data
*
* CREATION DATE:	21-01-2016
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

package com.telespazio.csg.srpf.dataManager.bo;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.telespazio.csg.srpf.dataManager.bean.BeamBean;
import com.telespazio.csg.srpf.dataManager.bean.SatelliteBean;
import com.telespazio.csg.srpf.dataManager.dao.SatelliteDao;
import com.telespazio.csg.srpf.dataManager.util.DataManagerConstants;
import com.telespazio.csg.srpf.logging.EventManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.utils.DateUtils;

/**
 * Define the Businnes Object for the Satellite model stored on DB
 * 
 * @author Abed Alissa
 * @version 1.0
 *
 */
public class SatelliteBO
{
     private EventManager eventMgr = new EventManager(); // Handles event logging

    static final Logger logger = LogManager.getLogger(SatelliteBO.class.getName());


    /**
     * @author Abed Alissa
     * @version 1.0
     * @since 2016-1-20
     * @param missionName
     * @return mission id
     * @throws Exception
     */
    public int getIdMission(String missionName) throws Exception
    {

        SatelliteDao dao = null;
        // Mission id
        int idMission = 0;
        SatelliteBO.logger.debug("Inside method getIdMission ");
        try
        {
            dao = new SatelliteDao();
            // retrieve mission
            idMission = dao.getIdMission(missionName);

        } // end try
        catch (Exception e)
        {
            throw e; // rethrow
        } // end catch
        finally
        {
            if (dao != null)
            {
                dao.closeConnection(); // close connection
            }
        } // end finally
        return idMission;
    }// end method

    /**
     * @author Abed Alissa
     * @version 1.0
     * @since 2016-1-20
     * @param sensorMode
     * @param idSatellite
     * @return List of beams
     * @throws Exception
     */
    public ArrayList<BeamBean> getBeamsSatellite(int sensorMode, int idSatellite) throws Exception
    {

        SatelliteDao dao = null;
        // list to be returned
        ArrayList<BeamBean> beamsSatelliteList = new ArrayList<>();
        SatelliteBO.logger.trace("Inside method getBeamsSatellite ArrayList<BeamBean> of SatelliteBO ");
        try
        {

            dao = new SatelliteDao(); // initialize DAO
            // fill list
            beamsSatelliteList = dao.getBeamsSatellite(sensorMode, idSatellite);

        } // end try
        catch (Exception e)
        {
            // rethorw
            throw e;
        } // end catch
        finally
        {
            // close connection
            if (dao != null)
            {
                dao.closeConnection();
            }
        } // end finally
        return beamsSatelliteList;
    }// end method

    /**
     * Return a list of beams for export configuration
     * 
     * @author Abed Alissa
     * @version 1.0
     * @since 2016-1-20
     * @param sensorMode
     * @param idSatellite
     * @return List of meams
     * @throws Exception
     */
    public ArrayList getBeamsExportConfiguration(int sensorMode, int idSatellite) throws Exception
    {
        // DAO
        SatelliteDao dao = null;
        // list of beam to be returned
        ArrayList beamsSatelliteListCon = new ArrayList();
        SatelliteBO.logger.debug("Inside method getBeamsExportConfiguration ");
        try
        {
            dao = new SatelliteDao();
            // fill list
            beamsSatelliteListCon = dao.getBeamsExportConfiguration(sensorMode, idSatellite);

        } // end try
        catch (Exception e)
        {
            // log
            SatelliteBO.logger.error(EventType.SOFTWARE_EVENT, "---", e.getMessage());
            // rethrow
            throw e;
        } // end catch
        finally
        {
            // close connection
            if (dao != null)
            {
                dao.closeConnection();
            }
        } // end finally
        return beamsSatelliteListCon;
    } // end method

    /**
     * 
     * @author Abed Alissa
     * @version 1.0
     * @since 2016-1-20
     * @param sensorMode
     * @param idMission
     * @return sensor mode list
     * @throws Exception
     */
    public ArrayList getBeamsSensorMode(int sensorMode, int idMission) throws Exception
    {
        // TODO controllare se serve davvero. Mi sa che lo ha scritto per essere
        // usato solo in un test!!!!!!
        //// Inoltre minchiata delle minchiate ritorna una lista di liste
        SatelliteDao dao = null;
        ArrayList idSatellitesList = new ArrayList();// list of id
        ArrayList beamsSatelliteList = null;
        ArrayList beamsSatelliteListAll = new ArrayList(); // sat list
        int idSatellite = 0;
        SatelliteBO.logger.debug("Inside method getBeamsSensorMode ");
        try
        {
            dao = new SatelliteDao(); // init dao

            idSatellitesList = dao.getIdSatellitesMission(idMission);
            for (int i = 0; i < idSatellitesList.size(); i++)
            {
                beamsSatelliteList = new ArrayList();
                idSatellite = ((int) idSatellitesList.get(i));
                if (dao.existSatelliteBeam(idSatellite))
                {
                    beamsSatelliteList = dao.getBeamsSatellite(sensorMode, idSatellite);
                } // end
                beamsSatelliteListAll.add(beamsSatelliteList);
            } // end

        } // end try
        catch (Exception e)
        {
            throw e; // rethrow
        } // end catch
        finally
        {
            if (dao != null)
            {
                dao.closeConnection(); // close connection
            }
        } // end finally
        return beamsSatelliteListAll;
    } // end method

    /**
     * @author Abed Alissa
     * @version 1.0
     * @since 2016-1-20
     * @param SatelliteName
     * @return Satellite data
     * @throws Exception
     */
    public SatelliteBean getDatiSatellite(String SatelliteName) throws Exception
    {
        SatelliteDao dao = null;
        SatelliteBean satelliteDati = new SatelliteBean(); // sat bean
        SatelliteBO.logger.debug("Inside method getDatiSatellite ");
        try
        {
            dao = new SatelliteDao();
            // retrieve data
            satelliteDati = dao.getDatiSatellite(SatelliteName);

        } // end try
        catch (Exception e)
        {
            throw e; // rethrow
        } // end catch
        finally
        {
            if (dao != null)
            {
                dao.closeConnection(); // close connection
            }
        } // end finally
        return satelliteDati;
    } // end method

    /**
     * @author Abed Alissa
     * @version 1.0
     * @since 2016-1-20
     * @param sensoreModeName
     * @return ID Sensor mode
     * @throws Exception
     */
    public int getIdSensorMode(String sensoreModeName) throws Exception
    {

        SatelliteDao dao = null;
        int idSensorMode = 0;
        SatelliteBO.logger.debug("Inside method getIdSensorMode ");

        try
        {
            dao = new SatelliteDao();
            // fill sensor mode
            idSensorMode = dao.getIdSensorMode(sensoreModeName);
        } // end try
        catch (Exception e)
        {
            // Rethrow
            throw e;
        } // end catch
        finally
        {
            if (dao != null)
            {
                dao.closeConnection(); // close connection
            }
        } // end finally
        return idSensorMode;
    } // end method

    /**
     * @author Abed Alissa
     * @version 1.0
     * @since 2016-1-20
     * @param idSensorMode
     * @param idMission
     * @return SatelliteList
     * @throws Exception
     */
    public ArrayList<SatelliteBean> getSatellitesPerSensorMode(int idSensorMode, int idMission) throws Exception
    {
        SatelliteDao dao = null;
        // list to be returbed
        ArrayList<SatelliteBean> satelliteForSensorModeList = new ArrayList<>();
        SatelliteBO.logger.debug("Inside method getSatellitesPerSensorMode ");
        try
        {
            dao = new SatelliteDao();
            // fill list
            satelliteForSensorModeList = dao.getSatellitesPerSensorMode(idSensorMode, idMission);

        } // end try
        catch (Exception e)
        {
            throw e; // rethrow
        } // end catch
        finally
        {
            // close connection
            if (dao != null)
            {
                dao.closeConnection();
            }
        } // end finally
        return satelliteForSensorModeList;

    } // end method

    /**
     * Return a map of SatelliteBean vs BeamBean,
     * 
     * @param missionNameOrFirstSensorMode
     *            or first sensor mode in case of combined request
     * @param sensorModeName
     * @param isCombined
     * @return map holding the satellite vs beams
     * @throws Exception
     */
    public Map<SatelliteBean, List<BeamBean>> getBeamsSatellite(String missionNameOrFirstSensorMode, String sensorModeName, boolean isCombined) throws Exception
    {
        // map to be returned
        Map<SatelliteBean, List<BeamBean>> beamMap = new TreeMap<>();
        SatelliteDao dao = null;

        SatelliteBO.logger.trace("Inside method getBeamsSatellite Map<SatelliteBean, List<BeamBean>> os SatelliteBO missionNameOrFirstSensorMode");
        try
        {
            dao = new SatelliteDao();

            // fill map
            beamMap = dao.getBeamsSatellite(missionNameOrFirstSensorMode, sensorModeName, isCombined);

        } // end try
        catch (Exception e)
        {
        	DateUtils.getLogInfo(e, SatelliteBO.logger);
            // rethor
            throw e;
        } // end catch
        finally
        {
            // close connection
            if (dao != null)
            {
                dao.closeConnection();
            }
        } // end finally
        return beamMap;

    } // end method

    /**
     * 
     * @return the satellite bean list
     * @throws Exception
     */
    public ArrayList<SatelliteBean> getSatelliteBeanList() throws Exception
    {
        SatelliteDao dao = null;
        // list to be returned
        ArrayList<SatelliteBean> satelliteForSensorModeList = new ArrayList<>();
        try
        {
            dao = new SatelliteDao();
            // fill list
            satelliteForSensorModeList = dao.getSatelliteBeanList();

        } // end try
        catch (Exception e)
        {
            // rethrow
            throw e;
        } // end catch
        finally
        {
            // close connection
            if (dao != null)
            {
                dao.closeConnection();
            }
        } // end finally
        return satelliteForSensorModeList;

    } // end method

    /**
     * Return a list of satellite bean used for export
     * 
     * @param idSensorMode
     * @param idMission
     * @return SatelliteBean for sensor mode
     * @throws Exception
     */
    public ArrayList<SatelliteBean> getSatellitesPerSensorModeConfiguration(int idSensorMode, int idMission) throws Exception
    {
        SatelliteDao dao = null;
        // list to be returned
        ArrayList<SatelliteBean> satelliteForSensorModeList = new ArrayList<>();

        try
        {
            dao = new SatelliteDao();
            SatelliteBO.logger.debug("Start: Get List Satellites Per SensorMode");
            // fill list
            satelliteForSensorModeList = dao.getSatellitesPerSensorModeConfiguration(idSensorMode, idMission);
            SatelliteBO.logger.debug("End: Get List Satellites PerSensor Mode");
        } // end try
        catch (Exception e)
        {
            // rethrow
            throw e;
        } // end catch
        finally
        {
            // close connection
            if (dao != null)
            {
                dao.closeConnection();
            }
        } // end finally
        return satelliteForSensorModeList;

    } // end method

    /**
     * @author Abed Alissa
     * @version 1.0
     * @since 2016-1-20
     * @return List of id
     * @throws Exception
     */
    public ArrayList getIdSensorsMode() throws Exception
    {

        SatelliteDao dao = null;
        // list to be returned
        ArrayList listaIdSensorsMode = new ArrayList();
        SatelliteBO.logger.debug("Start: Get Id Satellites PerSensor Mode");
        try
        {
            dao = new SatelliteDao();
            // fill list
            listaIdSensorsMode = dao.getIdSensorsMode();
        } // end try
        catch (Exception e)
        {
            // Rethow
            throw e;
        } // end catch
        finally
        {
            // close connection
            if (dao != null)
            {
                dao.closeConnection();
            }
        } // end finally
        return listaIdSensorsMode;
    } // end method

    /**
     * @author Abed Alissa
     * @version 1.0
     * @since 2016-1-20
     * @return list of sensor mode
     * @throws Exception
     */
    public ArrayList getSensorsMode() throws Exception
    {
        SatelliteDao dao = null;
        ArrayList listSensorsModeName = new ArrayList();
        SatelliteBO.logger.debug("Start: Get List Sensors Mode");

        try
        {
            dao = new SatelliteDao();
            // fill list
            listSensorsModeName = dao.getSensorsMode();

        } // end try
        catch (Exception e)
        {
            // rethrow
            throw e;
        } // end catch
        finally
        {
            // close connection
            if (dao != null)
            {
                dao.closeConnection();
            }
        } // end finally
        SatelliteBO.logger.debug("End: Get List Sensors Mode");
        return listSensorsModeName;
    } // end method

    /**
     * return the file name of related to the Satellite
     * 
     * @param satName
     * @return a list of obdata
     * @throws Exception
     */
    public ArrayList<String> getOBdataFileName(String satName) throws Exception
    {
        // list to be returned
        ArrayList<String> fileName = new ArrayList<>();
        SatelliteDao dao = null;
        try
        {
            // init dao
            dao = new SatelliteDao();
            // list = dao.selectAllEpochDates(satName, dataType);
            // fill list
            fileName = dao.getFileNameForOrbitData(satName);

        } // end try
        catch (Exception e)
        {
            // rethrow
            throw e;
        } // end catch
        finally
        {
            // close connetion
            if (dao != null)
            {
                dao.closeConnection();
            }
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
    public Map<String, ArrayList<String>> getOBdataFileName() throws Exception
    {
        // Map to be returned
        Map<String, ArrayList<String>> retMap;

        SatelliteDao dao = null;
        try
        {
            // init dao
            dao = new SatelliteDao();
            // fill map
            retMap = dao.getFileNameForOrbitData();

        } // end try
        catch (Exception e)
        {
            // rethrow
            throw e;
        } // end catch
        finally
        {
            // close connecttion
            if (dao != null)
            {
                dao.closeConnection();
            }
        } // end finally

        return retMap;
    } // end method

    /**
     * 
     * @return the list of satellite names
     * @throws Exception
     */
    public List<String> getSatellites() throws Exception
    {
        // list to be returned
        List<String> satList = new ArrayList<>();

        SatelliteDao dao = null;

        try
        {
            dao = new SatelliteDao();
            // list = dao.selectAllEpochDates(satName, dataType);
            // fill list
            satList = dao.getSatellites();

        } // end try
        catch (Exception e)
        {
            throw e;
        } // end catch
        finally
        {
            // Close connection
            if (dao != null)
            {
                dao.closeConnection();
            }
        } // end finally

        return satList;
    } // end method

    /**
     * 
     * @return the list of satellite names
     * @throws Exception
     */
    public Map<Integer, String> getSatellitesIdNameMap() throws Exception
    {
        // map to be returned
        Map<Integer, String> satMap = new TreeMap<>();

        SatelliteDao dao = null;

        try
        {
            dao = new SatelliteDao();
            // list = dao.selectAllEpochDates(satName, dataType);
            // fill map
            satMap = dao.getSatellitesIdNameMap();

        } // end try
        catch (Exception e)
        {
            // Rethorw
            throw e;
        } // end catch
        finally
        {
            // close connection
            if (dao != null)
            {
                dao.closeConnection();
            }
        } // end finally

        return satMap;
    } // end method

    /**
     * Return the satellite id as for DB
     * 
     * @param satellite
     * @return satellite id given its name
     * @throws Exception
     */
    public int getSatelliteId(String satellite) throws Exception
    {
        // Return value
        int retval = 0;

        SatelliteDao dao = null;

        try
        {
            dao = new SatelliteDao();
            // list = dao.selectAllEpochDates(satName, dataType);
            // fill satellite
            retval = dao.getIdSatellite(satellite);

        } // end try
        catch (Exception e)
        {
            // Rethrow
            throw e;
        } // end catch
        finally
        {
            // close connection
            if (dao != null)
            {
                dao.closeConnection();
            }
        } // end finally

        return retval;
    }// end method

    /**
     * Update the obdata filenames with the ones holded in the given directory
     * 
     * @param path
     *            directory holding files
     * @throws Exception
     */
    public void updateObdataFileNameByDir(String path) throws Exception
    {
        File dir = new File(path);
        // Files in directory
        File[] files = dir.listFiles();
        for (File f : files)
        {
            // updating
            updateObdataFileName(f.getAbsolutePath());
        } // end for
    }// end method

    /**
     * update on DB the name of the file
     * 
     * @param path
     * @throws Exception
     */
    public void updateObdataFileName(String path) throws Exception
    {
        File obdataFile = new File(path);

        String fileName = obdataFile.getName();
        /**
         * StringTokenizer tokens = new StringTokenizer(fileName, "_");
         * 
         * String satName = tokens.nextToken();
         */

        String satName = getSatNameFromHeaderFile(obdataFile).trim();

        // //System.out.println("Satname: " + satName);
        // Data type
        int dataType = 0;

        if (fileName.toLowerCase().contains("odstp"))
        {
            dataType = DataManagerConstants.TYPE_ODSTP;
        } // end if
        else if (fileName.toLowerCase().contains("odmtp"))
        {
            dataType = DataManagerConstants.TYPE_ODMTP;
        } // end else if
        else if (fileName.toLowerCase().contains("odnom"))
        {
            dataType = DataManagerConstants.TYPE_ODNOM;
        } // end else if
        else if (fileName.toLowerCase().contains("odref"))
        {
            dataType = DataManagerConstants.TYPE_ODREF;
        } // end else

        updateObdataFileName(satName, path, dataType);
    }// end method

    /**
     * Update odbata file path in DB
     * 
     * @param path
     * @param type
     * @throws Exception
     */
    public void updateObdataFileName(String path, int type) throws Exception
    {
        File obdataFile = new File(path);

        // String fileName = obdataFile.getName();
        /**
         * StringTokenizer tokens = new StringTokenizer(fileName, "_");
         * 
         * String satName = tokens.nextToken();
         */

        String satName = getSatNameFromHeaderFile(obdataFile).trim();

        // //System.out.println("Satname: " + satName);
        // Data type
        int dataType = type;

        updateObdataFileName(satName, path, dataType);
    }// end method

    /**
     * Retrieve Sat name form header
     * 
     * @param file
     * @return Sat name
     * @throws IOException
     */
    private String getSatNameFromHeaderFile(File file) throws Exception
    {

        String retString = "";

        StringTokenizer tokens = new StringTokenizer(file.getName(), "_");

        retString = tokens.nextToken();

        if (retString.equalsIgnoreCase("CSG") || retString.equalsIgnoreCase("CSK"))
        {
            retString = tokens.nextToken();
        }

        /*
         * InputStream fis = new FileInputStream(file); InputStreamReader isr =
         * new InputStreamReader(fis,Charset.forName("UTF-8")); BufferedReader
         * br = new BufferedReader(isr);
         * 
         * try {
         * 
         * String line=br.readLine();
         * 
         * ////System.out.println("Header: " + line); StringTokenizer tokenize =
         * new StringTokenizer(line); tokenize.nextToken();
         * tokenize.nextToken(); retString=tokenize.nextToken();
         * 
         * } finally { br.close(); }
         * 
         * 
         * 
         * 
         * // Reading file heading
         * 
         */
        return retString;
    }

    /**
     * update the table of orbital data name
     * 
     * @param satName
     * @param path
     * @param obdataType
     * @throws Exception
     */
    void updateObdataFileName(String satName, String path, int obdataType) throws Exception
    {
        SatelliteDao dao = null;

        try
        {
            dao = new SatelliteDao();
            dao.updateObdataFileName(satName, path, obdataType);
        } // end try
        catch (Exception e)
        {
            // rollback
            dao.rollback();
            // rethorw
            throw e;
        } // end catch
        finally
        {
            // close connection
            if (dao != null)
            {
                dao.closeConnection();
            }
        } // end finally
    }// end method

    /**
     * Initialize the table on DB
     * 
     * @throws Exception
     */
    public void initializeOBDATAFileNameTable() throws Exception
    {
        SatelliteDao dao = null;
        try
        {
            dao = new SatelliteDao();
            dao.initializeOBDATAFileNameTable();
        } // end try7
        catch (Exception e)
        {
            // rolloback
            dao.rollback();
            // rethrow
            throw e;
        } // end catch
        finally
        {
            // close connection
            if (dao != null)
            {
                dao.closeConnection();
            }
        } // end finally
    }// end method

    /**
     * 
     * @return the list name of beam configured as spotlight
     * @throws Exception
     */
    public List<String> getSpotLightBeamsList() throws Exception
    {
        // list to be returned
        List<String> beamList = new ArrayList<>();
        // dao
        SatelliteDao dao = null;
        try
        {
            dao = new SatelliteDao();
            // list = dao.selectAllEpochDates(satName, dataType);
            // fill list
            beamList = dao.getSpotLightBeamsList();
        } // end try
        catch (Exception e)
        {
            throw e;
        } // end catch
        finally
        {
            // close connection
            if (dao != null)
            {
                dao.closeConnection();
            }
        } // end finally

        return beamList;
    } // end method

    /**
     * 
     * @return the list of name of sensormode configured as spotlight
     * @throws Exception
     */
    public List<String> getSpotLightSensorModeList() throws Exception
    {
        // list to be returned
        List<String> sensorModeList = new ArrayList<>();

        SatelliteDao dao = null;
        try
        {
            dao = new SatelliteDao();
            // list = dao.selectAllEpochDates(satName, dataType);
            // fill sensor mode
            sensorModeList = dao.getSpotLightSensorModeList();

        } // end try
        catch (Exception e)
        {
            throw e;
        } // end catch
        finally
        {
            // close connectionm
            if (dao != null)
            {
                dao.closeConnection();
            }
        } // end finally

        return sensorModeList;
    } // end method

    
    /**
     *  COME ERA 
     * 
    public void updateAllowedLookSide(String satName, int allowedSide) throws Exception
    {

        SatelliteDao dao = null;
        // switch on input
        switch (allowedSide)
        {
            case 0:
            case 1:
            case 2:
            case 3:
                break;

            default:
                throw new Exception("Only 0 1 2 3 are allowed values for look side");

        }// end switch

        try
        {
            dao = new SatelliteDao();
            // list = dao.selectAllEpochDates(satName, dataType);
            // update
            dao.updateAllowedLookSide(satName, allowedSide);
        } // end try
        catch (Exception e)
        {
            // rethrow
            throw e;
        } // end catch
        finally
        {
            // close connection
            if (dao != null)
            {
                dao.closeConnection();
            }
        } // end finally
    } // end method
     * 
     */
    
    /**
     * Update the allowed lookside for satellite. Allowed values are: 0: none 1:
     * right 2: left 3: both
     * 
     * @param satName
     * @param allowedSide
     * @throws SQLException
     */
    
    
    
    
    /**
     * Update the allowed lookside for satellite. Allowed values are: 0: none 1:
     * right 2: left 3: both
     * 
     * @param satName
     * @param allowedSide
     * @throws SQLException
     */
    public void updateAllowedLookSide(String satName, int allowedSide) throws Exception
    {

        SatelliteDao dao = null;
        
        if(allowedSide<0 || allowedSide>3)
        {
            SatelliteBO.logger.error("Only 0 1 2 3 are allowed values for look side");
            throw new Exception("Only 0 1 2 3 are allowed values for look side");
        }
        try
        {
            dao = new SatelliteDao();
            // list = dao.selectAllEpochDates(satName, dataType);
            // update
            dao.updateAllowedLookSide(satName, allowedSide);
        } // end try
        catch (Exception e)
        {
            // rethrow
            throw e;
        } // end catch
        finally
        {
            // close connection
            if (dao != null)
            {
                dao.closeConnection();
            }
        } // end finally
    } // end method

} // end class