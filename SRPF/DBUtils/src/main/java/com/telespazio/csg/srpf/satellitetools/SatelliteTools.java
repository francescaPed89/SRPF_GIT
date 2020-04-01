/**
*
* MODULE FILE NAME:	SatelliteTools.java
*
* MODULE TYPE:		Main Program
*
* FUNCTION:			Utitility class too manage some charachteristic of satellite on DB
* PURPOSE:			Used to change allowed look side for the satellite
* CREATION DATE:	05-04-2017
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

package com.telespazio.csg.srpf.satellitetools;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.telespazio.csg.srpf.dataManager.bo.SatelliteBO;

/**
 *
 * Main Class Utitility class too manage some charachteristic of satellite on DB
 * 
 * @author Amedeo Bancone
 * @version 1.0
 *
 */
public class SatelliteTools

{
    
    static final Logger logger = LogManager.getLogger(SatelliteTools.class.getName());


    /**
     * Main
     * 
     * @param args
     */
    public static void main(String[] args)

    {
        Options options = new Options();
        // add option
        options.addOption("h", "help", false, "print this message");
        // add option
        options.addOption("L", "change-llok-side", false, "this switch tells that the operation required is a change in the look side");
        // add option
        options.addOption("S", "satellite", true, "Name of the satellite involved in the change");
        // add option
        options.addOption("A", "angle", true, "allowed angle: 1 fot right 2 for left 3 for both ");

        CommandLineParser parser = new DefaultParser();

        try
        {
            // parsing line
            CommandLine line = parser.parse(options, args);
            if (line.hasOption('L'))
            {
                // change side
                changeLookSide(line);
            } // end if

        } // end try
        catch (Exception e)
        {
            // do nothing
            // just log
            e.printStackTrace();
        } // end catch

    }// end method

    /**
     * Change look side using parameters givene in the line string
     * 
     * @param line
     * @throws Exception
     */
    public static void changeLookSide(CommandLine line) throws Exception
    {
        String satelliteName = null;
        int lookSide = 0;
        if (line.hasOption('S'))
        {
            // getting sat name
            satelliteName = line.getOptionValue('S');
        } // end if
        else
        {
            // do nothing
            // just log
            throw new ParseException("Unspecified satellite name");

        } // end else
        

        if (line.hasOption('A'))
        {
            // getting look side
            lookSide = Integer.parseInt(line.getOptionValue('A'));
        } // end if
        else
        {
            // do nothing
            // just log
            throw new ParseException("Unspecified angle");

        } // end else
          // access satellite
        SatelliteBO bo = new SatelliteBO();
        // updatng
        bo.updateAllowedLookSide(satelliteName, lookSide);
    }// end methods

}// end class
