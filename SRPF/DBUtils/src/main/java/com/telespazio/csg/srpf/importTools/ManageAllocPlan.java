/**
*
* MODULE FILE NAME:	ManageAllocPlan.java
*
* MODULE TYPE:		Main Program
*
* FUNCTION:			Utitility class too import / delete allocation plan
*
* PURPOSE:			Used to import / delete allocation plan
*
* CREATION DATE:	16-12-2016
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

package com.telespazio.csg.srpf.importTools;

import javax.naming.NamingException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.telespazio.csg.srpf.dataManager.bo.SatellitePassBO;
import com.telespazio.csg.srpf.utils.DateUtils;

/**
 * Utitility class too import / delete allocation plan
 * 
 * @author Amedeo Bancone
 * @version 1.0
 *
 */
public class ManageAllocPlan
{
    /**
     * Delete all allocation plan older than current time
     * 
     * @throws NamingException
     * @throws Exception
     */
    static void deleteAllFromNow() throws NamingException, Exception
    {
        SatellitePassBO bo = new SatellitePassBO();
        // get current time as julian
        double now = DateUtils.cskDateTimeNow();
        // delete
        bo.deleteSatPassOlderThan(now);

    }// end method

    /**
     * Delete pass plan in the interval given in the opt vector opt[0] start
     * time given in iso format opt[1] stop time given in iso format
     * 
     * @param opt
     *            vector holding start and stop time interval
     * @throws NamingException
     * @throws Exception
     */
    static void deleteInterval(String[] opt) throws NamingException, Exception
    {
        // start time
        String startTime = opt[0];
        // stop time
        String stopTime = opt[1];

        System.out.println("Deleting passes  from " + startTime + " - " + stopTime);
        // tranform to Julian
        double initialEpoch = DateUtils.fromISOToCSKDate(startTime);
        // tranform to Julian
        double finalEpoch = DateUtils.fromISOToCSKDate(stopTime);

        SatellitePassBO bo = new SatellitePassBO();
        // deleting
        bo.deletePassInTheInterval(initialEpoch, finalEpoch);

    }// end method

    /**
     * Delete pass plan in the interval given in the opt vector and given
     * satellite opts[0] Satellite name opts[1] start time given in iso format
     * opts[2] stop time given in iso format
     * 
     * @param opts
     *            ector holding sar name start and stop time interval
     * @throws NamingException
     * @throws Exception
     */
    static void deleteIntervalForSatellite(String[] opts) throws NamingException, Exception
    {
        // satellite
        String sarName = opts[0];
        // start
        String startTime = opts[1];
        // stop
        String stopTime = opts[2];

        // to julian date
        double initialEpoch = DateUtils.fromISOToCSKDate(startTime);
        // to julina
        double finalEpoch = DateUtils.fromISOToCSKDate(stopTime);

        System.out.println("Deleting passes for " + sarName + "   from " + startTime + " - " + stopTime);
        SatellitePassBO bo = new SatellitePassBO();
        // deleting
        bo.deletePassInTheIntervalForSatellite(sarName, initialEpoch, finalEpoch);
    }// end method

    /**
     * Import Allocation plan xml file
     * 
     * @param fileName
     * @throws NamingException
     * @throws Exception
     */
    static void importAllocationPlan(String fileName) throws NamingException, Exception
    {
        System.out.println("Trying to import: " + fileName);
        // accessing db
        SatellitePassBO bo = new SatellitePassBO();
        // importing
        bo.importSatellitePass(fileName);
    }// end method

    /**
     * Import Satellite pass from SOE xml file
     * 
     * @param fileName
     * @throws Exception
     */
    static void importSoe(String fileName) throws Exception
    {
        System.out.println("Trying to import SOE: " + fileName);
        // accessing db
        SatellitePassBO bo = new SatellitePassBO();
        // importing
        bo.importSatellitePassFromSOE(fileName);
    }// end method

    /**
     * Main Methods
     * 
     * @param args
     */
    public static void main(String[] args)

    {
        Options options = new Options();
        // Add option
        options.addOption("h", "help", false, "print this message");
        // Add option
        options.addOption("D", "delete-all-from-now", false, "delete all the Satellite plan whose visibility stop  is older than now");
        Option option = new Option("d", "delete", true, "delete the satellite passes for all satellite from start to stop. Date should be specified in the ISO format: 2016-01-12T04:30:00Z");
        option.setArgs(2);
        options.addOption(option);
        Option option1 = new Option("s", "delete-pass-for-satellite", true, "delete-pass-for-satellite the satellitepasses for the specified satellite from start to stop. Date should be specified in the ISO format: 2016-01-12T04:30:00Z");
        option1.setArgs(3);
        options.addOption(option1);
        // Add option
        options.addOption("i", "import-all-plan", true, "import the specified allocation plan file");
        // Add option
        options.addOption("e", "import-soe", true, "import the specified soe plan file");

        CommandLineParser parser = new DefaultParser();

        try
        {
            // parsing command line
            CommandLine line = parser.parse(options, args);

            if ((line.hasOption('D') && line.hasOption('d')) || (line.hasOption('D') && line.hasOption('i')) || (line.hasOption('i') && line.hasOption('d')) || (line.hasOption('i') && line.hasOption('s')) || (line.hasOption('D') && line.hasOption('s')) || (line.hasOption('d') && line.hasOption('s')))
            {
                // do nothing
                // just log
                throw new ParseException("incompatible switch combination in command line");
            } // end if

            if (line.hasOption('h') || (line.getOptions().length == 0))
            {
                // build help string
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("tool", options);
                // exit
                return;
            } // end if
            else if (line.hasOption('D'))
            {
                // delete all from now
                deleteAllFromNow();
                System.out.println("Satellite passes successfully deleted");
            } // end else if
            else if (line.hasOption('d'))
            {
                // delete from intervals
                deleteInterval(line.getOptionValues('d'));
                System.out.println("Satellite passes successfully deleted");
            } // end else if
            else if (line.hasOption('s'))
            {
                // delete from intervals and satellite
                deleteIntervalForSatellite(line.getOptionValues('s'));
                System.out.println("Satellite passes successfully deleted");
            } // end else if
            else if (line.hasOption('i'))
            {
                // import allocation plan
                importAllocationPlan(line.getOptionValue('i'));
                System.out.println("File successfully imported");
            } // end else if
            else if (line.hasOption('e'))
            {
                // import soe
                importSoe(line.getOptionValue('e'));
                System.out.println("File successfully imported");
            } // end else if

        }
        catch (Exception e)
        {
            // do nothing
            // just log
            e.printStackTrace();
        } // end cacth

    }// end main

}// end class
