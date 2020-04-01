/**
*
* MODULE FILE NAME:	OptimizationAlgoIntef.java
*
* MODULE TYPE:		Interface definition
*
* FUNCTION:			Define the interface of the optimization algo
*
* PURPOSE:			Used to perform the feasibility in case of point duration requestes
*
* CREATION DATE:	29-11-2016
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

import java.util.List;

/**
 * Define the interface of the optimization algo
 *
 * @author Amedeo Bancone
 * @version 1.0
 *
 */
public interface OptimizationAlgoInterface

{

    /**
     * Return the optimal Acquisition Request List. If no AR has been found
     * return an empty list
     *
     * @return The optimized AcqReq
     */
    public List<AcqReq> getOptimalAcqReqList();

    /**
     * Return true if the PR as only one AcqReq
     *
     * @return true if the request has only one AcqReq
     */
    public boolean isSingleAcquired();

    /**
     * perform numberOfIteration for the outer optimization loop.
     *
     * @param numberOfIteration
     */
    public void performOptimizationLoop(final int numberOfIteration);

    /**
     *
     * @return the number of grid point not covered
     */
    public int getUncoveredNumberOfPoints();
}
