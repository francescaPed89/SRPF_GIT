/**
*
* MODULE FILE NAME:	SrpfBackendException.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Wrapper exception for all errors related to SRPF-backend
*
* PURPOSE:			Feasibility 
*
* CREATION DATE:	19-11-2015
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
package com.telespazio.csg.srpf.backend;

public class SrpfBackendException extends Exception 
{
    private static final long serialVersionUID = -6182724621291235611L;  // For serialization purposes

    /**
     * Creates a new exception with a given message
     * @param msg The error message
     */
    public SrpfBackendException(String msg)
    {
        super(msg);
    } // end method
} // end class
