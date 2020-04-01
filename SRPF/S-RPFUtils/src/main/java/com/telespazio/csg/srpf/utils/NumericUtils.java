/**
*
* MODULE FILE NAME:	NumericUtils.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Utilities on numeric
*
* PURPOSE:
*
* CREATION DATE:	18-11-2015
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

package com.telespazio.csg.srpf.utils;

/**
 * Class collecting some generic utilities function for numeric
 * 
 * @author Amedeo Bancone
 * @version 1.0
 *
 *
 */
public class NumericUtils

{

    /**
     * Return a random integer in interval [min,max]
     * 
     * @param min
     * @param max
     * @return random integer in [min,max]
     */
    public static int randomInteger(int min, int max)

    {
        return (min + (int) (Math.random() * ((max - min) + 1)));
    } // end randomInteger

    /**
     * Return a random integer in interval [0,max]
     * 
     * @param max
     * @return random integer in [0,max]
     */
    public static int randomInteger(int max)
    {
        return (int) (Math.random() * (max + 1));

    }// end method

    /**
     * Evalute the normalize value in terms of :
     * 
     * Xi - min(x) zi = ------------------- max(x) -moin(x)
     * 
     * @param value
     * @param max
     * @param min
     * @return normalized value
     */
    public static double normalize(double value, double max, double min)
    {
        double retval = 1.0;
        // min not equals max, to avoid division by zero
        if (min != max)
        {
            retval = (value - min) / (max - min);
        } // end if

        // returning
        return retval;
    }// end method

} // end class
