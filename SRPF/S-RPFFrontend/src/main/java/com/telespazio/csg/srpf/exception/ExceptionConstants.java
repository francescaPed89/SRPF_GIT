/**
*
* MODULE FILE NAME:	ShutDownHandler.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Constants used to build exceptions thrown by S-RPF Frontend
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
 * Constants used to build exceptions thrown by S-RPF Frontend
 * 
 * @author Girolamo Castaldo
 * @version 1.0
 */
public class ExceptionConstants
{

    public static final String EXCEPTION_XML_START = "<?xml"; // First
                                                              // characters of
                                                              // an error XML
    public static final String EXCEPTION_LOCATOR = "S-RPF"; // The exception
                                                            // generator

    public static final String MISSING_ATTACHMENT_EXCEPTION_CODE = "01"; // Request
                                                                         // message
                                                                         // without
                                                                         // attachment
                                                                         // code
    public static final String MISSING_ATTACHMENT_EXCEPTION_TEXT = "Missing attachment in request message"; // Request
                                                                                                            // message
                                                                                                            // without
                                                                                                            // attachment
                                                                                                            // text

    public static final String PERFORMER_ERROR_EXCEPTION_CODE = "02"; // Error
                                                                      // during
                                                                      // feasibility
                                                                      // analysis
                                                                      // code
    public static final String PERFORMER_ERROR_EXCEPTION_TEXT = "Performer error during analysis"; // Error
                                                                                                   // during
                                                                                                   // feasibility
                                                                                                   // analysis
                                                                                                   // text

    public static final String INPUT_OUTPUT_EXCEPTION_CODE = "03"; // I/O error
                                                                   // during
                                                                   // feasibility
                                                                   // analysis
                                                                   // code
    public static final String INPUT_OUTPUT_EXCEPTION_TEXT = "I/O error during analysis"; // I/O
                                                                                          // error
                                                                                          // during
                                                                                          // feasibility
                                                                                          // analysis
                                                                                          // text
} // end class
