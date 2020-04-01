/**
*
* MODULE FILE NAME:	InvalidNameException.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Exception to be Thrown when an invalid name format is found
*
* PURPOSE:
*
* CREATION DATE:	08-12-2015
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

package com.telespazio.csg.srpf.exception;

/**
 * Thrown when an invalid name format is found
 *
 * @author Girolamo Castaldo
 * @version 1.0
 */
public class InvalidNameException extends Exception
{

    private static final long serialVersionUID = -1124864243962730682L; // For
                                                                        // (de)serialization
                                                                        // checks

    /**
     * Do nothing
     */
    public InvalidNameException()
    {
    }

    /*
     * Customize the exception with a message
     * 
     * @param message The message accompanying the exception
     */
    public InvalidNameException(String message)
    {
        super(message);
    } // end method
} // end class