package com.telespazio.csg.srpf.importExportConfiguration;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.telespazio.csg.srpf.dataManager.bo.ConfigurationBO;
import com.telespazio.csg.srpf.dataManager.util.DataManagerConstants;

public class UpdateBeamTable {

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

            configurationBO = new ConfigurationBO();

            if (typeConfiguration.equalsIgnoreCase(DataManagerConstants.CONFIGURATION_UPDATE))
            {
                // update
                configurationBO.alterTableBeam();

            } // end else if

            
        } // end try
        catch (SQLException e)
        {
            // do nothing
            // just print
        	System.out.println("Database connection error: " + e);
            e.printStackTrace();
        } // end catch
        catch (ClassNotFoundException e)
        {
            // do nothing
            // just print
        	System.out.println("Class not found error: " + e);
            e.printStackTrace();
        } // end catch
        catch (IOException e)
        {
            // do nothing
            // just print
        	System.out.println("Unable to read file: " + e);
            e.printStackTrace();
        } // end catch
        catch (Exception e)
        {
            // do nothing
            // just print
        	System.out.println("Error: " + e);
        } // end catch
    }// end method

}
