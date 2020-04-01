/**
*
* MODULE FILE NAME:	GridException.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			This Class define the exception raised by a gridder
*
* PURPOSE:			Feasibility
*
* CREATION DATE:	19-01-2015
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
 * This Class define the exception raised by a gridder
 *
 * @author Amedeo Bancone
 * @version 1.0
 *
 */
public class GridException extends Exception
{

    /**
     *
     * @param msg
     */
    public GridException(String msg)
    {
        /**
         * super
         */
        super(msg);
    }// end method
}// end class
