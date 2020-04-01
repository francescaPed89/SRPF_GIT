/**
*
* MODULE FILE NAME:	Gridder.java
*
* MODULE TYPE:		Interface definition
*
* FUNCTION:			Interface to specify a generic grid point
*
* PURPOSE:			Perform Feasibility
*
* CREATION DATE:	17-12-2015
*
* AUTHORS:			Amedeo Bancone
*
* DESIGN ISSUE:		2.0
*
* INTERFACES:
*
* SUBORDINATES:
*
* MODIFICATION HISTORY:
*
*
*
* --------------------------+------------+----------------+-------------------------------
* 19-12-2017 | Amedeo Bancone  |2.0| added method isAcrossDateLine();
* 									 added method needed for extension : buildMultiGeometryForExtension and getGridpointInsdeAR
*
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.feasibility;

import java.util.List;

/**
 * Generic interface for fill a grid
 *
 * @author Amedeo Bancone
 * @version 2.0
 *
 */
public interface Gridder
{

    /**
     * Fille the gridPointList
     *
     * @param gridPointList
     * @throws GridException
     */
    public void fillGrid(List<GridPoint> gridPointList) throws GridException;

    /**
     * get the coverage related to the given ARLIST
     *
     * @param arList
     * @return
     * @throws GridException
     */
    public double getCoverage(List<AcqReq> arList) throws GridException;
    
    public List<AcqReq> processOverlap(List<AcqReq> arList) throws GridException;

    /**
     * get the overlap factor for the given ARLIST
     *
     * @param arList
     * @return
     * @throws GridException
     */
    public double getOverlapFactor(List<AcqReq> arList) throws GridException;

    /**
     * Return the list of centroids of holes in the coverage area, empty list if
     * no holes are found
     *
     * @param arList
     * @return the list of holes centers, null if no holes are found
     * @throws GridException
     */
    public List<GridPoint> getHolesCenter(List<AcqReq> arList) throws GridException;

    public double getAream2();

    /**
     *
     * @return true if the grid is across the line of date
     */
    public boolean isAcrossDateLine();

    /**
     * Build the multigeometry
     *
     * @param polyList
     */
    public void buildMultiGeometryForExtension(List<AcqReq> arlist);

    /**
     *
     *
     * @param list
     * @param corners
     * @return
     */
    public List<GridPoint> getGridpointInsdeAR(List<GridPoint> list, double[][] corners);

    /**
     * In case of single acqiuisition return the poslist of the polygon to be
     * used in AR poslist
     *
     * @return poslist
     */
    public String retunPolyListForSingleAcq();
}// end interface
