/**
*
* MODULE FILE NAME:	AccessComparatorByTime.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Class used to Compare two access on time
*
* PURPOSE:			Perform Feasibility
*
* CREATION DATE:	15-12-2015
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
 * Compare two access on time basis
 *
 * @author Amedeo Bancone
 * @version 1.0
 *
 */
public class AccessComparatorByTime implements Comparator<Access>

{

    /**
     * Return : - 0 if access have same time - 1 is acc0 has time > of acc1 - -1
     * if acc0 has time < of acc1
     *
     * @param acc0
     *            first dto
     * @param acc1
     *            second dto
     * @return Path to the response xml
     *
     */
    @Override
    public int compare(Access acc0, Access acc1)
    {
        /**
         * Default >
         */
        int retval = 1;

        /**
         * equals
         */
        if (acc0.getAccessTime() == acc1.getAccessTime())
        {
            retval = 0;
        }

        /**
         * less
         */
        if (acc0.getAccessTime() < acc1.getAccessTime())
        {
            retval = -1;
        }

        return retval;
    }// compare

}// end AccessComparatorByTime
