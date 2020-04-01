/**
*
* MODULE FILE NAME:	CommonFieldsManager.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Used to manage common fields  on log activities 
*
* PURPOSE:			Used for logging purposes 
*
* CREATION DATE:	18-01-2016
*
* AUTHORS:			Girolamo Castaldo
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

package com.telespazio.csg.srpf.logging;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.telespazio.csg.srpf.logging.constants.LogMisc;
import com.telespazio.csg.srpf.logging.exceptions.LoggerConfigurationException;
import com.telespazio.csg.srpf.utils.PropertiesReader;;

/**
 * Used to manage common fields  on log activities 
 * @author Girolamo Castaldo
 * @version 1.0
 *
 */
public class CommonFieldsManager 
{
    private int traceType = -1; // Trace level
    private String hostNameOriginator = null; // System using log facilities
    private String logOriginator = null; // Component using log facilities

    /**
     * Sets trace level and system using log facilities
     * @param traceType Trace level
     * @param logOriginator Component using log facilities
     * @throws UnknownHostException If there's a problem retrieving system hostname
     */
    public CommonFieldsManager (int traceType, String logOriginator) throws UnknownHostException
    {
    	//INIT
        this.traceType = traceType;
        this.logOriginator = logOriginator;
        this.hostNameOriginator = InetAddress.getLocalHost().getHostName();
	} // end method
	
    /**
     * Sets trace level and system using log facilities
     * @param traceType Trace level
     * @param logOriginator Component using log facilities
     * @param hostNameOriginator System using log facilities
     */
    public CommonFieldsManager (int traceType, String logOriginator, String hostNameOriginator)
    {
    	//init
        this.traceType = traceType;
        this.logOriginator = logOriginator;
        this.hostNameOriginator = hostNameOriginator;
	} // end method

    /**
     * Gets the trace level
     * @return Trace level
     */
	public int getTraceType() 
	{
		return this.traceType;
	} // end method

    /**
     * Gets the hostname of the system using log facilities
     * @return Hostname of the system using log facilities
     */
	public String getHostNameOriginator() 
	{
		return this.hostNameOriginator;
	} // end method
	
    /**
     * Gets the component using log facilities
     * @return Component using log facilities
     */
	public String getLogOriginator() 
	{
		return this.logOriginator;
	} // end method

    /**
     * Calculates the current time
     * @return Current time
     */
	public String getLogGenerationTime() 
	{
		LocalDateTime dateTime = LocalDateTime.now(); // Current date
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(LogMisc.DATE_TIME_FORMAT); // Date formatter
		return dateTime.format(formatter);
	} // end method

    /**
     * Sets the hostname of the system using log facilities
     * @param Hostname of the system using log facilities
     */
	public void setHostNameOriginator(String hostNameOriginator) 
	{
		this.hostNameOriginator = hostNameOriginator;
	} // end method
	
    /**
     * Sets the component using log facilities
     * @param Component using log facilities
     */
	public void setLogOriginator(String logOriginator) 
	{
		this.logOriginator = logOriginator;
	} // end method
} // end class