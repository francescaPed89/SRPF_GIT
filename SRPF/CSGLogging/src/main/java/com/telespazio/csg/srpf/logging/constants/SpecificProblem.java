/**
*
* MODULE FILE NAME:	SpecificProblem.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Define constants to specify  problem  in log
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
 * Define constants to specify  problem  in log
 * @author Girolamo Castaldo
 * @version 1.0
 *
 */
public class SpecificProblem 
{
	// Operation Type
	public static final String OPERATION_PRINT = "040"; // Print operation
	public static final String OPERATION_EXPORT = "110"; // Export operation
	public static final String OPERATION_IMPORT = "120"; // Import operation
	public static final String OPERATION_UNSUCCESSFUL_ACCESS = "140"; // Unsuccessful access operation
	public static final String OPERATION_DECLASSIFICATION_OF_DATA = "150"; // Declassification of data operation
	public static final String OPERATION_DEGRADATION_OF_DATA = "155"; // Degradation of data operation
	public static final String OPERATION_TRANSMISSION_OF_INFORMATION = "160"; // Transmission of information operation
	public static final String OPERATION_RECEIPT_OF_INFORMATION = "165"; // Receipt of information operation
	public static final String OPERATION_CLASSIFIED_FILE_CREATION = "170"; // Classified file creation operation
	public static final String OPERATION_CLASSIFIED_FILE_MODIFICATION = "172"; // Classified file modification operation
	public static final String OPERATION_CLASSIFIED_FILE_DELETION = "174"; // Classified file deletion operation
	public static final String OPERATION_UNSUCCESSFUL_CHECKSUM_FOR_ARCHIVED_DATA = "180"; // Unsuccessful cheksum for archived data operation
	public static final String OPERATION_UNSUCCESSFUL_CHECKSUM_FOR_EXTERNAL_DATA = "181"; // Unsuccessful cheksum for external data operation
	public static final String OPERATION_UNSUCCESSFUL_CHECKSUM_FOR_DELIVERABLE_DATA = "182"; // Unsuccessful cheksum for deliverable data operation
	public static final String OPERATION_OTHER = "999"; // Other operation
	// Success or Failure
	public static final String SUCCESS_RESULT = "OK"; // Success result
	public static final String FAILURE_RESULT = "KO"; // Failure result
	// Return Code
	public static final int FILE_NOT_FOUND = 2; // File not found
} // end class
