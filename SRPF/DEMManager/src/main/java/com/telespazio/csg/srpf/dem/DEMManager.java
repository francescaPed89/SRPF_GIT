/**
*
* MODULE FILE NAME:	DEMManager.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Class used to manage the Data Elevation Model
*
* PURPOSE:			Manage DEM
*
* CREATION DATE:	18-12-2015
*
* AUTHORS:			Amedeo Bancone
*
* DESIGN ISSUE:		1.1
*
* INTERFACES:
*
* SUBORDINATES:
*
* MODIFICATION HISTORY:
*
*             Date                |  Name      |   New ver.    | Description
* --------------------------+------------+----------------+-------------------------------
* 18-05-2018| Amedeo Bancone  |1.1 | modified getElevation in order to take into account the possibility of an ArrayIndexOutOfBoundsException
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.dem;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.DateUtils;

import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;

/**
 *
 * @author Amedeo Bancone
 * @version 1.1
 *
 */
public class DEMManager
{
	//static final Logger logger = LogManager.getLogger(DEMManager.class.getName());

    // static final Logger logger =
    // LogManager.getLogger(DEMManager.class.getName());
    // log
    private TraceManager tracer; // Handles trace logging

    // base parh dir
    private String demBasePathDir;

    // map holding data
    private Map<String, DEMRect> demMap = new TreeMap<>();

    // Max number of rect in map
    private static final int defaultNumeberOfinMemoryRect = 100;
    private int maxNumberofRectInMemory = defaultNumeberOfinMemoryRect;

    // Since 90° is a singular point for the dem used, we subctrat an eps in
    // case of 90° of latitude
    private final double latituteEps = 0.000000001;

    /**
     * Constructor
     *
     * @param demBasePathDir
     */
    public DEMManager(final String demBasePathDir)
    {
        // initializing
        this.demBasePathDir = demBasePathDir;
        this.tracer = new TraceManager();

    }// end method

    /**
     * Constructor
     *
     * @param demBasePathDir
     * @param maxNumerOfRectangleInMemory
     */
    public DEMManager(final String demBasePathDir, int maxNumerOfRectangleInMemory)
    {
        // setting base dir
        this.demBasePathDir = demBasePathDir;
        // setting number of rectangle
        this.maxNumberofRectInMemory = maxNumerOfRectangleInMemory;
        this.tracer = new TraceManager();
    }// end method

    /**
     *
     * @param _latitude
     * @param _longitude
     * @return the elevation
     * @throws ArrayIndexOutOfBoundsException
     */
    public int getElevation(final double _latitude, final double _longitude) throws ArrayIndexOutOfBoundsException
    {
        int elevation = 0;

        double latitude = _latitude;
        double longitude = _longitude;

        // necessari beacuse of naming convention.
        if (longitude == 180)
        {
            longitude = -180;
        } // end if

        if (latitude == 90)
        {
            latitude = latitude - this.latituteEps; // 90° is a singular point
                                                    // for
            // the files
        } // end if

//        logger.debug("latitude for getELEVATION "+latitude);
//        logger.debug("longitude for getELEVATION "+longitude);

        String hdf5FilePath = findRectanglePath(latitude, longitude);
//        logger.debug("hdf5FilePath for getELEVATION "+hdf5FilePath);

        /*
         * File file = new File(hdf5FilePath); if(!file.exists()) {
         *
         * this.tracer.warning(EventType.RESOURCE_EVENT,
         * ProbableCause.FILE_ERROR,"File " + hdf5FilePath +
         * " not found: 0 will be returned as elevation ");
         *
         * return 0; }
         */
       // this.demMap.clear();
        DEMRect rectangle = null;
        if(!this.demMap.isEmpty())
        {
            rectangle = this.demMap.get(hdf5FilePath);
//          logger.debug("rectangle for getELEVATION "+rectangle);

        }
  
        if (rectangle == null)
        {
            // logger.info("Adding new rectangle to dem map");
            try
            {
                rectangle = new DEMRect(hdf5FilePath);
            }
            catch (Exception e)
            {
                // logger.error("Unable to create rectangle for file: " +
                // hdf5FilePath + " fake rectangle returning zeros will be added
                // to DEMManager" );
            	//logger.debug(EventType.RESOURCE_EVENT, ProbableCause.OUT_OF_MEMORY, "Unable to create rectangle for file: " + hdf5FilePath + " fake rectangle returning zeros will be added to DEMManager");
                // logger.error(e.getMessage());
                // rectangle = new DEMRect();
                // rectangle.setFilePath(hdf5FilePath);
                rectangle = null;
                elevation = 0;
            //	DateUtils.getLogInfo(e, logger);

            } // end catch

            if ((this.demMap.size() >= this.maxNumberofRectInMemory) && (rectangle != null))
            {
               // logger.warn("too many rectangle in memory: empting the memory map");
                this.demMap.clear();
            } // end if

            if (rectangle != null)
            {
                this.demMap.put(hdf5FilePath, rectangle);
            }
        } // end if

        try
        {
            // rectangle initialized return elevation
            if (rectangle != null)
            {
                elevation = rectangle.getElevation(latitude, longitude);
            }
        } // end try
        catch (Exception e)
        {
//            logger.error("elevation Exception e "+e);
//        	DateUtils.getLogInfo(e, logger);


            // this should never happen
            // if yes it means that the hdf5 file
            // has been built with errors
            // this is a big issue
            // we decided to return 0 elevation
            // and log the problem
            elevation = 0;
            // log
            this.tracer.warning(EventType.RESOURCE_EVENT, ProbableCause.CORRUPT_DATA, "Error in retrieving elevation for point : " + latitude + " " + longitude + " in " + hdf5FilePath + ". 0 elevation will be returned. Check if the hdf5 file is correct");

        } // end catch

        return elevation;
    }// end method

    /***
     * Clear the maps of rectangke
     */
    public void clear()
    {
        // looping in map
        for (Map.Entry<String, DEMRect> entry : this.demMap.entrySet())
        {
            try
            {
                // clearing
                entry.getValue().clear();
            }
            catch (HDF5Exception e)
            {
                // do nothing
                // just log
                this.tracer.warning(EventType.APPLICATION_EVENT, "Error clearing map", e.getMessage());
            } // end catch
        } // end for

    }// end method

    /**
     * Find the file holding the elevation for the given latitude longitude
     *
     * @param latitude
     * @param longitude
     * @return the path of rectnagle holding data
     */
    private String findRectanglePath(final double latitude, final double longitude)
    {
        // path to be returned
        String path = null;

        // build corners
        int topLeftCornerLatitude = (int) Math.floor(latitude + 1);
        int topLeftCornerLongitude = (int) Math.floor(longitude);

        // String latitudePostfix = topLeftCornerLatitude >= 0 ?"N" : "S";
        // String longitudePostfix = topLeftCornerLongitude >= 0 ?"E" : "W";

        String trailingLatitudeZero = Math.abs(topLeftCornerLatitude) >= 10 ? "" : "0";
        String trailingLongitudeZero = "";

        int absLongitude = Math.abs(topLeftCornerLongitude);
        // evaluarte longitude
        if (absLongitude >= 100)
        {
            trailingLongitudeZero = "";
        }
        else if (absLongitude < 10)
        {
            trailingLongitudeZero = "00";
        }
        else
        {
            trailingLongitudeZero = "0";
        } // end else
          // postfix
        String zeroPostFixLatitude = "000";
        String zeroPostFixLongitude = "000";

        /*
         * String fileName = "NDEM_GMTE_" +
         * trailingLatitudeZero+Math.abs(topLeftCornerLatitude) +
         * zeroPostFixLatitude + latitudePostfix +"_"+ trailingLongitudeZero +
         * Math.abs(topLeftCornerLongitude)+zeroPostFixLongitude
         * +longitudePostfix+"_";
         */

        // Evaluate second corner
        int bottomRightCornerLatitude = topLeftCornerLatitude - 1;
        int bottomRightCornerLongitude = topLeftCornerLongitude + 1;

        // evaluate if north
        String latitudePostfix = bottomRightCornerLatitude >= 0 ? "N" : "S";
        // String longitudePostfix = bottomRightCornerLongitude >= 0 ?"E" : "W";
        // evaluate is east
        String longitudePostfix = topLeftCornerLongitude >= 0 ? "E" : "W";

        // building file name
        String fileName = "NDEM_GMTE_" + trailingLatitudeZero + Math.abs(topLeftCornerLatitude) + zeroPostFixLatitude + latitudePostfix + "_" + trailingLongitudeZero + Math.abs(topLeftCornerLongitude) + zeroPostFixLongitude + longitudePostfix + "_";

        // trailing edge
        trailingLatitudeZero = Math.abs(bottomRightCornerLatitude) >= 10 ? "" : "0";

        absLongitude = Math.abs(bottomRightCornerLongitude);

        if (absLongitude >= 100)
        {
            trailingLongitudeZero = "";
        }
        else if (absLongitude < 10)
        {
            trailingLongitudeZero = "00";
        }
        else
        {
            trailingLongitudeZero = "0";
        } // end else

        String intermediatePath = latitudePostfix + trailingLatitudeZero + Math.abs(bottomRightCornerLatitude);

        // logger.debug("Intermediatepath: " + intermediatePath );
        // buuilding file name
        fileName = fileName + trailingLatitudeZero + Math.abs(bottomRightCornerLatitude) + zeroPostFixLatitude + latitudePostfix + "_" + trailingLongitudeZero + Math.abs(bottomRightCornerLongitude) + zeroPostFixLongitude + longitudePostfix + ".h5";

        // building path
        path = this.demBasePathDir + File.separator + intermediatePath + File.separator + fileName;

        // returning
        return path;
    }// end method

    /**
     * Destructor
     */
    @Override
    protected void finalize() throws Throwable
    {
        // clear
        this.clear();
        super.finalize();
    }// end method

}
