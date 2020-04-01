 /**
*
* MODULE FILE NAME:	TraceType.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Define the exception raised by the log system
*
* PURPOSE:			Used for logging purposes 
*
* CREATION DATE:	13-01-2016
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

package com.telespazio.csg.srpf.logging.exceptions;

/**
 * Define the exception raised by the log systemls
 * 
 * @author Girolamo Castaldo
 * @version 1.0
 *
 */
public class LoggerConfigurationException extends Exception
{
	private static final long serialVersionUID = 601583114395312274L; // For (de)serialization checks

	/*
     *  Do nothing
     */
    public LoggerConfigurationException() {}

    /*
     * Customize the exception with a message
     * 
     * @param message The message accompanying the exception
     */
    public LoggerConfigurationException(String message)
    {
       super(message);
    } // end method
} // end class