/**
*
* MODULE FILE NAME:	TraceType.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Define the trace levels
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

package com.telespazio.csg.srpf.logging.constants;

/**
 * Define the trace levels
 * @author Girolamo Castaldo
 * @version 1.0
 *
 */
public class TraceType 
{
	public static final String TRACE_TYPE_PARAM = "TraceType"; // Trace type parameter
	public static final int LOG_LEVEL_1 = 1; // Log level 1
	public static final int LOG_LEVEL_2 = 2; // Log level 2
	public static final int EVENT = 3; // Log level 3 (event)
} // end class
