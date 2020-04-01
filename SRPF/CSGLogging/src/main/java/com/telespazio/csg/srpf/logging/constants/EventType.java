/**
*
* MODULE FILE NAME:	EventType.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Define the log event
*
* PURPOSE:			Used for logging purposes 
*
* CREATION DATE:	17-01-2016
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
 * Define the log event
 * @author Girolamo Castaldo
 * @version 1.0
 *
 */
public class EventType 
{
	public static final String COMMUNICATION_EVENT = "001"; // Communication event
	public static final String APPLICATION_EVENT = "100"; // Application event
	public static final String SECURITY_EVENT = "200"; // Security event
	public static final String RESOURCE_EVENT = "300"; // Generic resource event
	public static final String HW_RESOURCE_EVENT = "310"; // Hardware resource event
	public static final String SW_RESOURCE_EVENT = "320"; // Software resource event
	public static final String STATE_CHANGE_EVENT = "400"; // State change event
	public static final String SYSTEM_EVENT = "500"; // System event
	public static final String SOFTWARE_EVENT = "501"; // Software event
	public static final String MIB_EVENT = "502"; // Mib event
	public static final String LOG_EVENT = "503"; // Log event
} // end class