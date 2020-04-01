/**
*
* MODULE FILE NAME:	LogMisc.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Define log constants 
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
 * Define log constants 
 * @author Girolamo Castaldo
 * @version 1.0
 *
 */
public class LogMisc 
{
    public static final String LOG4J_CONF_FILE_PARAM = "log4j.configurationFile"; // Log4j system property name
    //public static final String LOG4J_CONF_FILE_PATH = "/opt/SRPF/log4j2.xml";
    public static final String LOG4J_CONF_FILE_PATH_PARAM = "Log4jConfFilePath"; // Log4j config file path param name

    public static final String ORIGINATOR_PARAM = "ComponentName"; // Log originator param name
    public static final String DEFAULT_HOSTNAME_PARAM = "DefaultHostname"; // Default hostname param name

    public static final String SERVICE_ID_DEFAULT = ""; // Service ID default value
    public static final String PRODUCTION_ID_DEFAULT = ""; // Production ID default value

    public static final String EVENT_LOGGER = "Syslog"; // Event logger name
    public static final String TRACE_LOGGER = "RollingFile"; // Trace logger name

    public static final String SEPARATOR = "|"; // Log fields separator
    public static final String DATE_TIME_FORMAT = "yyyyMMddHHmmss"; // Log date format
} // end class