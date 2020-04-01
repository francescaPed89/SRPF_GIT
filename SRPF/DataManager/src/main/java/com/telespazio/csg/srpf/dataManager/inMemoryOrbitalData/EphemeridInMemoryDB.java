/**
*
* MODULE FILE NAME:	EphemeridInMemoryDB.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Singleton Class to manage the full set of orbital data
*
* PURPOSE:			Manage orbital data
*
* CREATION DATE:	09-07-2016
*
* AUTHORS:			Amedeo Bancone
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

package com.telespazio.csg.srpf.dataManager.inMemoryOrbitalData;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.telespazio.csg.srpf.dataManager.bean.EpochBean;
import com.telespazio.csg.srpf.dataManager.bo.SatelliteBO;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;

/**
 * Singleton Class to manage the full set of orbital data
 * 
 * @author Amedeo Bancone
 * @version 1.0
 *
 */
public class EphemeridInMemoryDB

{
	static final Logger logger = LogManager.getLogger(EphemeridInMemoryDB.class.getName());


    // log
    TraceManager tm = new TraceManager();
    // private boolean isInitialized =false;

    // Satellite ephem map
    private Map<String, SatelliteEphemerids> dbMap = new TreeMap<>();

    /*
     * public Map<String, SatelliteEphemerids> getDbMap() { return dbMap; }
     */
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /**
     * Default constructor (private)
     */
    private EphemeridInMemoryDB()
    {
        // //System.out.println("==============================================
        // init EphemeridInMemoryDB");
        // initialize();

    }// end method

    /**
     * Initialize the In memory DB
     */
    private void initialize()
    {
        // //System.out.println("==============================================
        // initialize");
        // clearing the map
        this.dbMap.clear();
        SatelliteBO satBo = new SatelliteBO();

        this.tm.log("Initializing In memory db for ephemerid");

        try
        {
            // List<String> satList = satBo.getSatellites();
            // map
            Map<Integer, String> satIsNameMap = satBo.getSatellitesIdNameMap();
            // loopyng in the map
            for (Entry<Integer, String> e : satIsNameMap.entrySet())
            {
                // putting data in mao
                this.dbMap.put(e.getValue(), new SatelliteEphemerids(e.getKey().intValue(), e.getValue()));
            } // end for

            // refreshDB();

            // this.isInitialized=true;

        } // end try
        catch (Exception e)
        {
            // clear map
            this.dbMap.clear();
            // log
            this.tm.critical(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_ERROR, "Unable to retrieve satellite list. In memory db empty " + e.getMessage());
        } // end catch

    } // End method

    /**
     * 
     * Helper class used for the INSTANCE GENERATION
     *
     */
    private static class EphemeridDBHelper
    {

        private static EphemeridInMemoryDB INSTANCE; // The single instance of
                                                     // PropertiesReader

        static
        {
            INSTANCE = new EphemeridInMemoryDB();
        }// end static block

    } // end class

    /**
     * Entry point to get the single instance of the class
     * 
     * @return The single instance of the class
     */
    public static EphemeridInMemoryDB getInstance()
    {

        return EphemeridDBHelper.INSTANCE;
    } // end method

    /**
     * synchronyze db with files
     */
    public void refreshDB()

    {
        // acquiring lock
        this.readWriteLock.writeLock().lock();
        try
        {
            if (this.dbMap.size() == 0)
            {
                initialize();
            } // end if

            SatelliteBO bo = new SatelliteBO();
            // getting filenames from DB
            Map<String, ArrayList<String>> retMap = bo.getOBdataFileName();

            SatelliteEphemerids eph;
            // looping in the map
            for (Entry<String, ArrayList<String>> e : retMap.entrySet())
            {
                eph = this.dbMap.get(e.getKey());
                if (eph != null)
                {
                    // rephreshing data
                    eph.refreshAllData(e.getValue());
                } // end if
                else
                {
                    // log
                    this.tm.critical(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_ERROR, "Unable to retrieve Ephemerids for satellite  " + e.getKey());
                } // end else
            } // end for

        }
        catch (Exception e)
        {
            // log
            this.tm.critical(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_ERROR, "Unable to retrieve obdata map  " + e.getMessage());

        } // end catch
        finally
        {
            // release lock
            this.readWriteLock.writeLock().unlock();
        } // end finally
    }// end refreshDB

    /**
     *
     * @param sat
     * @param startTime
     * @param stopTime
     * @return a list holding the requested time line
     */
    public ArrayList<EpochBean> selectEpochs(String sat, double startTime, double stopTime)
    {
        this.readWriteLock.readLock().lock();
        try
        {
            ArrayList<EpochBean> list = new ArrayList<>();
            /*
             * if(!this.isInitialized) //somethings has gone wrong before so not
             * initialized { return list; }
             */

            SatelliteEphemerids s = this.dbMap.get(sat);

            if (s != null)
            {
                list = s.selectEpochData(startTime, stopTime);
            }


            return list;
        }
        finally
        {
            this.readWriteLock.readLock().unlock();
        }

    }// selectEpochs

}// end class
