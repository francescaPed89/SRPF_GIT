/**
*
* MODULE FILE NAME:	GridPoint.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			This Class modelize a gridpoint
*
* PURPOSE:			Feasibility
*
* CREATION DATE:	17-11-2015
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

import java.util.Arrays;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.telespazio.csg.srpf.utils.ReferenceFrameUtils;

/**
 *
 * Class describing a point in the Area Grid The point coordinate arem stored in
 * ECEF format
 *
 * @author Amedeo Bancone
 * @version 1.0
 */
public class GridPoint
{

    /**
     * ID
     */
    private int pointId;

    /**
     * ECEF Coords
     */
    private Vector3D ecefCoords;

    /**
     * llh coords
     */
    private double[] llh;

    /**
     * Flag stating if the point is the centroid
     */
    private boolean isCentroid = false;

    /**
     * Flag stating if the point is internal to the polygon (Case of area). In
     * case of centroid it could be extenal
     */
    private boolean isInternal = true;

    /**
     * true if belongs to MinAoI
     */
    private boolean belongsToMinimumAoI = false;

    /**
     *
     * @return true if the point belongs the MinimumAoI
     */
    public boolean isBelongsToMinimumAoI()
    {
        return this.belongsToMinimumAoI;
    }// end method

    /**
     * Set the flag of the belonging to Minumum AoI
     *
     * @param belongsToMinimumAoI
     */
    public void setBelongsToMinimumAoI(boolean belongsToMinimumAoI)
    {
        this.belongsToMinimumAoI = belongsToMinimumAoI;
    }// end method

    /**
     * Return true if the point is centroid
     *
     * @return
     */
    public boolean isCentroid()
    {
        return this.isCentroid;
    }// end method

    /**
     * Set the centroid flag
     *
     * @param isCentroid
     */
    public void setCentroid(boolean isCentroid)
    {
        this.isCentroid = isCentroid;
    }// end method

    
    public String getUnivocalKey()
    {
        return pointId+"_"+this.ecefCoords.toString();
    }
    
    @Override
    public String toString()
    {
        return "GridPoint [pointId=" + pointId + ", ecefCoords=" + ecefCoords + ", llh=" + Arrays.toString(llh) + ", isCentroid=" + isCentroid + ", isInternal=" + isInternal + ", belongsToMinimumAoI=" + belongsToMinimumAoI + "]";
    }

    /**
     * return true if the point belongs to the area of interest
     *
     * @return
     */
    public boolean isInternal()
    {
        return this.isInternal;
    }// end method

    /**
     * set the flag isInternal
     *
     * @param isInternal
     */
    public void setInternal(boolean isInternal)
    {
        this.isInternal = isInternal;
    }// end method

    /**
     * Constructor
     *
     * @param id
     * @param llh
     *            lat lon (degree), height
     */
    public GridPoint(final int pointId, final double[] llh)

    {
        initialize(pointId, llh);

    }// end method

    private void initialize(final int pointId, final double[] llh)
    {
        this.pointId = pointId;
        this.llh = llh;
        // evaluating ECEF coords
        this.ecefCoords = new Vector3D(ReferenceFrameUtils.llh2ecef(llh, true));
    }// end method

    /**
     *
     * @param pointId
     * @param latitude
     * @param longitude
     * @param height
     */
    public GridPoint(final int pointId, final double latitude, final double longitude, final double height)
    {
        // llh
        double[] latLonHeight = new double[3];
        latLonHeight[0] = latitude;
        latLonHeight[1] = longitude;
        latLonHeight[2] = height;
        // initializing
        initialize(pointId, latLonHeight);
    }// end method

    /**
     * return the ECEF vector of coordinate
     *
     * @return
     */
    public Vector3D getEcef()

    {
        return this.ecefCoords;
    }// end method

    /**
     * retrun the position in LLH
     *
     * @return
     */
    public double[] getLLH()

    {
        return this.llh;
    }// end method

    /**
     * return the point id
     *
     * @return
     */
    public int getId()
    {
        return this.pointId;

    }// end method

    /**
     * Set the grid point id
     *
     * @param id
     */
    public void setId(int id)
    {
        this.pointId = id;
    }// end method

    /**
     * @return true if same id
     */
    @Override
    public boolean equals(Object obj)
    {

        GridPoint p = (GridPoint) obj;
        return this.pointId == p.pointId;
    }// end method

}// end class
