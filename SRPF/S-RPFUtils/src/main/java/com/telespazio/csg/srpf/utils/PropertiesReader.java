/**
*
* MODULE FILE NAME:	PropertiesReader.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Singleton class which wraps a Properties object to read configuration parameters
*
* PURPOSE:			Configuration
*
* CREATION DATE:	18-11-2015
*
* AUTHORS:			Girolamo Castaldo
*
* DESIGN ISSUE:		2.0
*
* INTERFACES:
*
* SUBORDINATES:
*
* MODIFICATION HISTORY:
*
*
* --------------------------+------------+----------------+-------------------------------
*    20-06-2017 | Amedeo Bancone  |2.0 |insert trim in the property reader
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton class which wraps a Properties object to read configuration
 * parameters
 * 
 * @author Girolamo Castaldo
 * @version 2.0
 * 
 */
public class PropertiesReader
{

    private Properties properties; // Properties object to read configuration
                                   // parameters
    private final String CONFIG_FILE = System.getenv("SRPF_CONF_FILE"); // Path
                                                                        // to
                                                                        // the
                                                                        // config
                                                                        // file

    /**
     * Creates a singleton wrapper for the Properties object
     * 
     * @param empty
     *            If true, an empty Properties object is created
     * @throws IOException
     *             If an error occurs while reading the config file
     */
    private PropertiesReader(boolean empty) throws IOException
    {
        this.properties = new Properties();

        if (empty == true)
        {
            return;
        } // end if

        InputStream input = null; // Input stream for config file
        try
        {
            if (this.CONFIG_FILE != null)
            {
                input = new FileInputStream(this.CONFIG_FILE);
                this.properties.load(input);
            } // end if
        } // end try
        catch (IOException ioe)
        {
            // do nothing
            // just throw
            throw ioe;
        } // end catch
        finally
        {
            if (input != null)
            {
                try
                {
                    // close file
                    input.close();
                } // end try
                catch (IOException ioe)
                {
                }
            } // end if
        } // end finally
    }// end method

    /**
     * Inner helper class to create the singleton
     * 
     * @author Girolamo Castaldo
     * @version 1.0
     */
    private static class PropertiesReaderHelper
    {

        private static PropertiesReader INSTANCE; // The single instance of
                                                  // PropertiesReader
        static
        {
            boolean empty = false; // If true, an empty Properties is created
            try
            {
                INSTANCE = new PropertiesReader(empty);
            } // end try
            catch (IOException ioe)
            {
                // throw new ExceptionInInitializerError(ioe);
                empty = true;
                try
                {
                    INSTANCE = new PropertiesReader(empty);
                } // end try
                catch (IOException ioe2)
                {
                    ioe.printStackTrace();
                }
            } // end catch
        }// end static
    } // end class

    /**
     * Entry point to get the single instance of the class
     * 
     * @return The single instance of the class
     */
    public static PropertiesReader getInstance()
    {
        return PropertiesReaderHelper.INSTANCE;
    } // end method

    /**
     * Retrieves the requested key value
     * 
     * @param key
     *            Parameter to lookup
     * @return Value of the requested key
     */
    public String getProperty(String key)
    {
        String retval = null;
        // getting value
        String value = this.properties.getProperty(key);
        if (value != null)
        {
            // trimming
            retval = value.trim();
        } // end if

        return retval;
    } // end method

    /**
     * Retrieves the requested key value
     * 
     * @param key
     *            Parameter to lookup
     * @param defaultValue
     *            Default value for input parameter
     * @return Value of the requested key or default in case it's not found
     */
    public String getProperty(String key, String defaultValue)
    {
        return this.properties.getProperty(key, defaultValue).trim();
    } // end method

    /**
     * Returns the path of the underlying config file
     * 
     * @return Config file used
     */
    public String getConfigFile()
    {
        return this.CONFIG_FILE;
    }// end method
} // end class