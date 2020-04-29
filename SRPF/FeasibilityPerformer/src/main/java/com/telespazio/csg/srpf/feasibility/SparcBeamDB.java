/**
*
* MODULE FILE NAME:	SparcBeamDB.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Singleton class holding the parameters used in generating input for SPARC
*
* PURPOSE:			Used to interact with SPARC
*
* CREATION DATE:	03-03-2017
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

package com.telespazio.csg.srpf.feasibility;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.PropertiesReader;

/**
 * Singleton class used to held the parameters used in generating input for
 * SPARC, The first call should be performed by the controller
 *
 * @author Amedeo Bancone
 * @version 1.0
 */
public class SparcBeamDB

{

    // log
    TraceManager tm = new TraceManager();

    // filke path
    String dbFilePath = null;

    // Name of the configuration parameters holding the path of the file holding
    // the DB
    private static String SPARC_BEAM_DB_FILE_CONF_KEY = "SPARC_BEAM_DB_FILE";

    private static String DB_FIELD_DELIM = ":";
    private static String DB_COMMENT_LINE = "#";

    // This map holds the number of cut in azimuth for standard product
    private Map<String, Integer> numberOfSampleInAzimuthStandardMaps = new TreeMap<>();

    // This map holds the number of cut in azimuth for extended product
    private Map<String, Integer> numberOfSampleInAzimuthExtentedMaps = new TreeMap<>();

    // This map holds the time treshold defining the separating the standard
    // from extended product
    private Map<String, Integer> standardProductTimeLimitMap = new TreeMap<>();

    // This map holds the number of sample for in range scene
    private Map<String, Integer> numberOfsampleInRangeForSceneMap = new TreeMap<>();

    // This map holds lenght for each sample at near offnadir
    private Map<String, Integer> nearOffNadirStepLenghtMap = new TreeMap<>();

    // This map number of step at near (half interval)
    private Map<String, Integer> nearOfNadirNumberOfStepMap = new TreeMap<>();

    // This map holds lenght for each sample at far offnadir
    private Map<String, Integer> farOffNadirStepLenghtMap = new TreeMap<>();

    // This map number of step at far (half interval)
    private Map<String, Integer> farOfNadirNumberOfStepMap = new TreeMap<>();

    // This map holds lenght for each sample at subSat
    private Map<String, Integer> subSatelliteStepLenghtMap = new TreeMap<>();

    // This map number of step at sub sat (half interval)
    private Map<String, Integer> subSatelliteNumberOfStepMap = new TreeMap<>();

    /**
     *
     * @param beamName
     * @return the value of of number of steo at subSatellite (half interval) -1
     *         otherwhise
     */
    public int getSubSatelliteNumberOfStep(String beamName)
    {
        // retrieving from map
        Integer val = this.subSatelliteNumberOfStepMap.getOrDefault(beamName, new Integer(-1));

        return val.intValue();
    }// end method

    /**
     *
     * @param beamName
     * @return the value of of steo lenght at subSatellite -1 otherwhise
     */
    public int getSubSatelliteStepLengh(String beamName)
    {
        // retrieving from map
        Integer val = this.subSatelliteStepLenghtMap.getOrDefault(beamName, new Integer(-1));

        return val.intValue();
    }// end method

    /**
     *
     * @param beamName
     * @return the value of of number of steo at far (half interval) -1
     *         otherwhise
     */
    public int getFarOffNadirNumberOfStep(String beamName)
    {
        // retrieving from map
        Integer val = this.farOfNadirNumberOfStepMap.getOrDefault(beamName, new Integer(-1));

        return val.intValue();
    }// end method

    /**
     *
     * @param beamName
     * @return the value of of steo lenght at far -1 otherwhise
     */
    public int getFarOffNadirStepLenght(String beamName)
    {
        // retrieving from map
        Integer val = this.farOffNadirStepLenghtMap.getOrDefault(beamName, new Integer(-1));

        return val.intValue();
    }// end method

    /**
     *
     * @param beamName
     * @return the value of of number of steo at near (half interval) -1
     *         otherwhise
     */
    public int getNearOfNadirNumberOfStep(String beamName)
    {
        // retrieving from map
        Integer val = this.nearOfNadirNumberOfStepMap.getOrDefault(beamName, new Integer(-1));

        return val.intValue();
    }// end method

    /**
     *
     * @param beamName
     * @return the value of of steo lenght at near -1 otherwhise
     */
    public int getNearOffNadirStepLenght(String beamName)
    {
        // retrieving from map
        Integer val = this.nearOffNadirStepLenghtMap.getOrDefault(beamName, new Integer(-1));

        return val.intValue();
    }// end method

    /**
     *
     * @param beamName
     * @return the value of the threshold for standard product product -1
     *         otherwhise
     */
    public int getNumberOfsampleInRangeForScene(String beamName)
    {
        // retrieving from map
        Integer val = this.numberOfsampleInRangeForSceneMap.getOrDefault(beamName, new Integer(-1));

        return val.intValue();
    }// end method

    /**
     *
     * @param beamName
     * @return the value of the threshold for standard product product -1
     *         otherwhise
     */
    public int getStandardProductTimeLimit(String beamName)
    {
        // retrieving from map
        Integer val = this.standardProductTimeLimitMap.getOrDefault(beamName, new Integer(-1));

        return val.intValue();
    }// end method

    /**
     *
     * @param beamName
     * @return the value of number cut in azimuth for extended product -1
     *         otherwhise
     */
    public int getNumberOfSampleInAzimuthExtended(String beamName)
    {
        // retrieving from map
        Integer val = this.numberOfSampleInAzimuthExtentedMaps.getOrDefault(beamName, new Integer(-1));

        return val.intValue();
    }// end method

    /**
     *
     * @param beamName
     * @return the value of number cut in azimuth for standard product -1
     *         otherwhise
     */
    public int getNumberOfSampleInAzimuthStandard(String beamName)
    {
        // retrieving from map
        Integer val = this.numberOfSampleInAzimuthStandardMaps.getOrDefault(beamName, new Integer(-1));

        return val.intValue();

    }// end method

    /**
     * Private constructor
     */
    private SparcBeamDB()
    {
        this.dbFilePath = PropertiesReader.getInstance().getProperty(SPARC_BEAM_DB_FILE_CONF_KEY);
        
        System.out.println("SPARC_BEAM_DB_FILE_CONF_KEY : "+this.dbFilePath);
        if (this.dbFilePath == null)
        {
            // logger.warn("Unable to found " +
            // FeasibilityConstants.MAX_AREA_OF_INTEREST_CONF_KEY + " in
            // conffiguration");
            // just log
            System.out.println("ERROR ! SPARC_BEAM_DB_FILE_CONF is null");

            this.tm.critical(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + SPARC_BEAM_DB_FILE_CONF_KEY + " in configuration");
        } // end if
        else
        {
            // initializing
            initializeDB();
        } // end else

    }// end method

    /***
     * fill the maps with the value found in the file
     */
    private void initializeDB()
    {
        this.tm.log("Initializing In memory db for SPARC Beam");
        // reader
        BufferedReader br = null;
        try
        {
            InputStream fis = new FileInputStream(this.dbFilePath);
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            br = new BufferedReader(isr);
            String line;
            // looping on line of file
            while ((line = br.readLine()) != null)
            {
                if (line.startsWith(DB_COMMENT_LINE) || line.isEmpty())
                {
                    // have continue
                    continue;
                } // end if
                StringTokenizer tokens = new StringTokenizer(line, DB_FIELD_DELIM);

                try
                {
                    // tokenizing
                    String beamId = tokens.nextToken().trim();
                    // get token
                    Integer numberOfSampleInAzimuthStandard = new Integer(tokens.nextToken().trim());
                    this.numberOfSampleInAzimuthStandardMaps.put(beamId, numberOfSampleInAzimuthStandard);
                    // get token
                    Integer numberOfSampleInAzimuthExetnded = new Integer(tokens.nextToken().trim());
                    this.numberOfSampleInAzimuthExtentedMaps.put(beamId, numberOfSampleInAzimuthExetnded);
                    // get token
                    Integer standardProductTimeLimit = new Integer(tokens.nextToken().trim());
                    this.standardProductTimeLimitMap.put(beamId, standardProductTimeLimit);
                    // get token
                    Integer numberOfsampleInRangeForScene = new Integer(tokens.nextToken().trim());
                    this.numberOfsampleInRangeForSceneMap.put(beamId, numberOfsampleInRangeForScene);

                    // get token
                    Integer nearOffNadirStepLenght = new Integer(tokens.nextToken().trim());
                    this.nearOffNadirStepLenghtMap.put(beamId, nearOffNadirStepLenght);
                    // get token
                    Integer nearOfNadirNumberOfStep = new Integer(tokens.nextToken().trim());
                    this.nearOfNadirNumberOfStepMap.put(beamId, nearOfNadirNumberOfStep);
                    // get token
                    Integer farOffNadirStepLenght = new Integer(tokens.nextToken().trim());
                    this.farOffNadirStepLenghtMap.put(beamId, farOffNadirStepLenght);
                    // get token
                    Integer farOfNadirNumberOfStep = new Integer(tokens.nextToken().trim());
                    this.farOfNadirNumberOfStepMap.put(beamId, farOfNadirNumberOfStep);
                    // get token
                    Integer subSatelliteStepLenght = new Integer(tokens.nextToken().trim());
                    this.subSatelliteStepLenghtMap.put(beamId, subSatelliteStepLenght);
                    // get token
                    Integer subSatelliteNumberOfStep = new Integer(tokens.nextToken().trim());
                    this.subSatelliteNumberOfStepMap.put(beamId, subSatelliteNumberOfStep);

                } // end try
                catch (Exception e)
                {
                    // just log
                    this.tm.critical(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Malformed line in SPARC BEAM DB " + e.getMessage());

                } // end log

            } // END WHILE

        } // end try
        catch (Exception e)
        {
            // just log
            this.tm.critical(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Error in initializing SPRC BEAM DB " + e.getMessage());

        } // end catch
        finally
        {
            if (br != null)
            {
                try
                {
                    // clong reader
                    br.close();
                }
                catch (Exception e)
                {
                    // do nothing
                } // end cacth
            } // end if
        } // end finally
    }// end method

    /*
     * Helper class used for the INSTANCE GENERATION
     *
     */
    private static class SparcBeamDBHelper
    {

        private static SparcBeamDB INSTANCE; // The single instance of
                                             // PropertiesReader
        static
        {
            INSTANCE = new SparcBeamDB();
        }
    } // end class

    /**
     * Entry point to get the single instance of the class
     *
     * @return The single instance of the class
     */
    public static SparcBeamDB getInstance()
    {

        return SparcBeamDBHelper.INSTANCE;
    } // end method

}// end class
