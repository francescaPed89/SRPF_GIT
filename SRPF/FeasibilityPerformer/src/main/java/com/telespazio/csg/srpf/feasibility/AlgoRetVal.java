/**
*
* MODULE FILE NAME:	AlgoRetVal.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			This class is an helper class holding the list of Acquisition request and the algo info of the optimization
*
* PURPOSE:			Feasibility
*
* CREATION DATE:	23-01-2017
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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * This class is an helper class holding result of the algorithm and the algo
 * itself
 *
 * @author Amedeo Bancone
 * @version 1.0
 *
 */
public class AlgoRetVal
{

    /**
     * List of Acquisition
     */
    List<AcqReq> acquisitionRequestList= new ArrayList<AcqReq>();

    /**
     * Error message string
     */
    private String errorMessage = "";

    /**
     * Optimization algo
     */
    OptimizationAlgoInterface algo;

    /**
     * Return an error message
     *
     * @return the error message
     */
    public String getErrorMessage()
    {
        return this.errorMessage;
    }// end method

    /**
     * set the error message
     *
     * @param errorMessage
     */
    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }// end method

    /**
     *
     * @return Acquisiition list
     */
    public List<AcqReq> getAcquisitionRequestList()
    {
        return this.acquisitionRequestList;
    }// end method

    /**
     *
     * @return a reference to algo
     */
    public OptimizationAlgoInterface getAlgo()
    {
        return this.algo;
    }// end method

    /**
     * Constructor
     *
     * @param acquisitionRequestList
     * @param algo
     */
    public AlgoRetVal(List<AcqReq> acquisitionRequestList, OptimizationAlgoInterface algo)
    {
        /**
         *
         */
        this.acquisitionRequestList = acquisitionRequestList;
        this.algo = algo;
    }// end method

}
