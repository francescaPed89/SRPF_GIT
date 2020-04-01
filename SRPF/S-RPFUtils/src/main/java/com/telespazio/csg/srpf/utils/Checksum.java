
/**
*
* MODULE FILE NAME:	Checksum.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Calculates the checksum of input file in a chosen format
*
* PURPOSE:			Used for logging purposes
*
* CREATION DATE:	18-01-2016
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

package com.telespazio.csg.srpf.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

/**
 * Calculates the checksum of a file for any given format (MD5, SHA-1, etc.)
 * 
 * @author Girolamo Castaldo
 * @version 1.0
 */
public class Checksum
{

    /**
     * Calculates the checksum of input file in a chosen format
     * 
     * @param digest
     *            Checksum format
     * @param file
     *            File whose checksum is to be calculated
     * @return File checksum in the chosen format
     * @throws IOException
     *             If there's a problem reading input file
     */
    public static String getFileChecksum(MessageDigest digest, File file) throws IOException
    {
        FileInputStream fis = new FileInputStream(file); // Get file input
                                                         // stream for reading
                                                         // the file content

        byte[] byteArray = new byte[1024]; // Create byte array to read data in
                                           // chunks
        int bytesCount = 0;

        // Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1)
        {
            digest.update(byteArray, 0, bytesCount);
        } // end while

        // close the stream; We don't need it now.
        fis.close();

        byte[] bytes = digest.digest(); // Get the hash's bytes

        // This bytes[] has bytes in decimal format;
        // Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        } // end for

        // return complete hash
        return sb.toString();
    } // end method
} // end class