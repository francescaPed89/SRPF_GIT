/**
*
* MODULE FILE NAME:	FeasibilityException.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			This Class define the exception for feasibility
*
* PURPOSE:			Feasibility
*
* CREATION DATE:	19-11-2015
*
* AUTHORS:			Amedeo Bancone
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

package com.telespazio.csg.srpf.feasibility;

/**
 * This Class define the exception for feasibility
 *
 * @author Amedeo Bancone
 * @version 1.0
 *
 */
public class FeasibilityException extends Exception
{

    /**
     *
     * @param msg
     */
    public FeasibilityException(String msg)
    {
        super(msg);
    }// end method
}// End Class
