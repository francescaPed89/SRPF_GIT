/**
*
* MODULE FILE NAME:	DTOComparatorByTime.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			This class is an helper used to order a list  of dto according to their start time
*
* PURPOSE:			Feasibility
*
* CREATION DATE:	23-01-2018
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

import java.util.Comparator;

/**
 * Compare two dto on time basis
 *
 * @author Amedeo Bancone
 * @version 1.0
 *
 */
public class DTOComparatorByTime implements Comparator<DTO>
{

    /**
     * Return : - 0 if dto have same start time - 1 is dto has start time > of
     * dto1 - -1if dto 0 has start time < of dto1
     *
     * @param dto0
     *            first dto
     * @param dto1
     *            second dto
     * @return Path to the response xml
     *
     */
    @Override
    public int compare(DTO dto0, DTO dto1)
    {

        /**
         * Value
         */
        int retval = 1;

        /**
         * Equals
         */
        if (dto0.getStartTime() == dto1.getStartTime())
        {
            retval = 0;
        }

        /**
         * Less
         */
        if (dto0.getStartTime() < dto1.getStartTime())
        {
            retval = -1;
        }

        return retval;
    }// end compare

}// end class
