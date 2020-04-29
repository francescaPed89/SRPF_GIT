package com.telespazio.csg.srpf.importExportConfiguration;

import java.io.IOException;
import java.sql.SQLException;

import com.telespazio.csg.srpf.dataManager.bo.ConfigurationBO;

public class UpdateDatabase {
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

            configurationBO = new ConfigurationBO();

                // update
                configurationBO.updateDatabaseDAO();

            
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
