/**
*
* MODULE FILE NAME:	ImportPlatformActivityWindow.java
*
* MODULE TYPE:		Main program
*
* FUNCTION:			Define the main program to PAW on DB
*
* PURPOSE:			for configuration purposes
*
* CREATION DATE:	20-02-2016
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

package com.telespazio.csg.srpf.importTools;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.telespazio.csg.srpf.dataManager.bo.PlatformActivityWindowBO;
import com.telespazio.csg.srpf.dataManager.util.DataManagerConstants;
import com.telespazio.csg.srpf.utils.DateUtils;

/**
 * Define the main program to PAW on DB
 * 
 * @author Abed Alissa
 * @version 1.0
 *
 */
public class ImportPlatformActivityWindow
{
	
	static Logger logger = LogManager.getLogger(ImportPlatformActivityWindow.class.getName());


    // private static final String xmlFilePath =
    // "E:\\PAW\\GSIF_PlatformActivityWindow.xml";

    /**
     * Main
     * 
     * @param args
     */
    public static void main(String[] args)
    {

        PlatformActivityWindowBO platformActivityWindowBO = null;

        boolean success = false;
        String action = "";
        String xmlFilePath = "";
        try
        {
            if (args.length == 1)
            {
                action = args[0];

            } // end if
            else
            {
                action = args[0];
                xmlFilePath = args[1];

            } // end else

            platformActivityWindowBO = new PlatformActivityWindowBO();

            if (action.equalsIgnoreCase(DataManagerConstants.CONFIGURATION_IMPORT))
            {
            	logger.debug("Try to import xml " + xmlFilePath + " file into DataManager GSIF_PAW table");
            	//System.out.println("Try to import xml " + xmlFilePath + " file into DataManager GSIF_PAW table");

                success = platformActivityWindowBO.updatePaw(xmlFilePath);

                if (success)
                {
                    // do nothing
                    // just log
                    logger.debug("Success import xml " + xmlFilePath + " file into DataManager GSIF_PAW table");

                } // end if
                else
                {
                    // do nothing
                    // just log
                    logger.error("Fail to import xml " + xmlFilePath + " file into DataManager GSIF_PAW table");

                } // end else

            } // end if
            else if (action.equalsIgnoreCase(DataManagerConstants.CONFIGURATION_UPDATE))
            {

                logger.debug("Try to update xml " + xmlFilePath + " file into DataManager GSIF_PAW table");
                //System.out.println("Try to update xml " + xmlFilePath + " file into DataManager GSIF_PAW table");

                success = platformActivityWindowBO.updatePaw(xmlFilePath);
                if (success)
                {
                    // do nothing
                    // just log
                    logger.debug("Success import xml " + xmlFilePath + " file into DataManager GSIF_PAW table");

                } // end if
                else
                {
                    // do nothing
                    // just log
                    logger.error("Fail to import xml " + xmlFilePath + " file into DataManager GSIF_PAW table");

                } // end else

            } // end else

            else if (action.equalsIgnoreCase(DataManagerConstants.CONFIGURATION_DELETE))
            {
                Date in = new Date();
                LocalDateTime ldt = LocalDateTime.ofInstant(in.toInstant(), ZoneId.systemDefault());
                platformActivityWindowBO.deletePawData(DateUtils.fromDateTimeTOCSKDate(ldt));
                //System.out.println("Success deleting PAW in GSIF_PAW table");

            } // end else

        } // end try
        catch (SQLException e)
        {
            // do nothing
            // just log
            logger.error("Database connection error: " + e);

            e.printStackTrace();
        } // end catch
        catch (ClassNotFoundException e)
        {
            // do nothing
            // just log
            logger.error("Class not found error: " + e);
            e.printStackTrace();
        } // end catch
        catch (IOException e)
        {
            // do nothing
            // just log
            logger.error("Unable to read file: " + e);
            e.printStackTrace();
        } // end catch

        catch (Exception e)
        {
            // do nothing
            // just log
            logger.error("Unable to read file, XML file malformed: " + e);
        } // end catch
    }

}
