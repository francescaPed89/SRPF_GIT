/**
*
* MODULE FILE NAME:	EventSeverity.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Define the log levels
*
* PURPOSE:			Used for logging purposes 
*
* CREATION DATE:	21-01-2016
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

package com.telespazio.csg.srpf.logging.constants;

/**
 * Define the log levels
 * @author Girolamo Castaldo
 * @version 1.0
 *
 */
public class EventSeverity
{
	public static final char INFORMATION = '0'; // Information severity
	public static final char CRITICAL = '1'; // Critical severity
	public static final char MAJOR = '2'; // Major severity
	public static final char MINOR = '3'; // Minor severity
	public static final char WARNING = '4'; // Warning severity
	public static final char CLEARED = '5'; // Cleared severity
	
	public static final String MAJOR_LEVEL = "Major"; // Major severity parameter
	public static final String MINOR_LEVEL = "Minor"; // Minor severity parameter
	public static final String CLEARED_LEVEL = "Cleared"; // Cleared severity parameter
} // end class
