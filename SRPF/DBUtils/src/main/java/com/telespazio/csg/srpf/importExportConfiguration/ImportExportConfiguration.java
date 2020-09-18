/**
*
* MODULE FILE NAME:	ImportExportConfiguration.java
*
* MODULE TYPE:		Main program
*
* FUNCTION:			Define the main program to import export and update configuration data from DB
*
* PURPOSE:			for configuration purposes
*
* CREATION DATE:	22-01-2016
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
* 20-04-2018 | Amedeo Bancone  |2.0 | Added update of configuration
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.importExportConfiguration;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.telespazio.csg.srpf.dataManager.bo.ConfigurationBO;
import com.telespazio.csg.srpf.dataManager.bo.SatelliteBO;
import com.telespazio.csg.srpf.dataManager.util.DataManagerConstants;

/**
 * Define the main program to import export and update configuration data from
 * DB
 * 
 * @author Abed Alissa
 * @version 2.0
 *
 */
public class ImportExportConfiguration
{

    static final Logger logger = LogManager.getLogger(ImportExportConfiguration.class.getName());

    /**
     * main
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        ConfigurationBO configurationBO = null;
        try
        {
            String typeConfiguration = args[0];
            String filePath = args[1];

            configurationBO = new ConfigurationBO();

            if (typeConfiguration.equalsIgnoreCase(DataManagerConstants.CONFIGURATION_IMPORT))
            {
                configurationBO.importExportConfiguration(typeConfiguration, filePath);
                logger.debug("Success import csv file from DataManager tables");

            } // end if
            else if (typeConfiguration.equalsIgnoreCase(DataManagerConstants.CONFIGURATION_UPDATE))
            {
                // update
            	/*
            	 * //MODIFICA FRA 2.2.13p: prima era 
            	 *                 configurationBO.updateSensorModesConfiguration(filePath);
					ora è updateSensorModesConfiguration2
            	 */
                configurationBO.updateSensorModesConfiguration2(filePath);

                logger.debug("Success update csv file from DataManager tables");

            } // end else if

            else
            {
                configurationBO.importExportConfiguration(typeConfiguration, filePath);
                logger.debug("Success export csv file from DataManager tables");
            } // end else

        } // end try
        catch (SQLException e)
        {
            // do nothing
            // just print
        	logger.error("Database connection error: " + e);
            e.printStackTrace();
        } // end catch
        catch (ClassNotFoundException e)
        {
            // do nothing
            // just print
        	logger.error("Class not found error: " + e);
            e.printStackTrace();
        } // end catch
        catch (IOException e)
        {
            // do nothing
            // just print
        	logger.error("Unable to read file: " + e);
            e.printStackTrace();
        } // end catch
        catch (Exception e)
        {
            // do nothing
            // just print
        	logger.error("Error: " + e);
        } // end catch
    }// end method

}// end class
