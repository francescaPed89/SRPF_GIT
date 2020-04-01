
/**
*
* MODULE FILE NAME:	ImportEpochs.java
*
* MODULE TYPE:		Main program
*
* FUNCTION:			Define the main program to import Orbital data on DB
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
*            12-07-2016                |  Amedeo Bancone      |   2.0    | Modified to use In memory Ephemerid DB

*
* PROCESSING
*/

package com.telespazio.csg.srpf.importTools;

import java.io.File;

import com.telespazio.csg.srpf.dataManager.bo.SatelliteBO;

/**
 * Define the main program to import Orbital data on DB Abed Alissa
 * 
 * @author amedeo
 * @version 2.0
 *
 */
public class ImportEpochs
{

    /**
     * Main
     * 
     * @param args
     */
    public static void main(String[] args)
    {

        String filePath = args[0];

        File file = new File(filePath);

        SatelliteBO bo = new SatelliteBO();

        try
        {
            if (file.isDirectory())
            {
                // updatuing scabnning the dir
                bo.updateObdataFileNameByDir(filePath);
            } // end if
            else
            {
                // single file
                bo.updateObdataFileName(filePath);
            } // end else
        } // end try
        catch (Exception e)
        {
            // do nothing
            // just print
            System.err.println("Unable to update db: " + e.getMessage());
        } // end catch

    }// end method

}// end class
