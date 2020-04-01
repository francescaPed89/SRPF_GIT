/**
*
* MODULE FILE NAME:	WSControllerConstants.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Constants used by WSController
*
* PURPOSE:			Web Service
*
* CREATION DATE:	19-2-2016
*
* AUTHORS:			Girolamo Castldo
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
*             Date                |  Name      |   New ver.    | Description
* --------------------------+------------+----------------+-------------------------------
* <DD-MMM-YYYY> | <name>  |<Ver>.<Rel> | <reasons of changes>
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.backend;

/**
 *  Constants used by WSController
 *  @author Girolamo Castaldo
 *  @version 1.0
 */
public class WSControllerConstants
{
    public static final String FEASIBILITY_XSD_PATH_PARAM = "FeasibilityXSDPath"; // Feasibility xsd files path param name
    public static final String FEASIBILITY_LOOKUP_TABLE_PATH_PARAM = "FeasibilityLookupTablePath"; // Feasibility lookup table file path param name
    
    public static final String DB_PATH_PARAM = "DBPath"; // DB path param name
    
    //public static final String ORIGINATOR_PARAM = "ComponentNameBackend"; // Log originator param name
    public static final String ORIGINATOR_PARAM = "ComponentName"; // Log originator param name
} // end class