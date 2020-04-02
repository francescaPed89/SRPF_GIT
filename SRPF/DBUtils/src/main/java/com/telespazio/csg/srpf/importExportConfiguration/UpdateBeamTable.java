package com.telespazio.csg.srpf.importExportConfiguration;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.telespazio.csg.srpf.dataManager.bo.ConfigurationBO;
import com.telespazio.csg.srpf.dataManager.util.DataManagerConstants;

public class UpdateBeamTable {

    static final Logger logger = LogManager.getLogger(UpdateBeamTable.class.getName());

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

            if (typeConfiguration.equalsIgnoreCase(DataManagerConstants.CONFIGURATION_UPDATE))
            {
                // update
                configurationBO.alterTableBeam(filePath);

            } // end else if

            
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

}
