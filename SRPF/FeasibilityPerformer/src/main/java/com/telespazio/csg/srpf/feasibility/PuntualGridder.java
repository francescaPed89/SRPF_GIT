/**
*
* MODULE FILE NAME:	PuntualGridder.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			It implements a degenered grid for puntual acquisition areas
*
* PURPOSE:			Modellize a degenereted grip with only a point
*
* CREATION DATE:	28-11-2016
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
import java.util.StringTokenizer;

import com.telespazio.csg.srpf.dem.DEMManager;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.PropertiesReader;
import com.telespazio.csg.srpf.utils.ReferenceFrameUtils;

/**
 * It implements a degenered grid for puntual acquisition areas
 *
 * @author Amedeo Bancone
 * @version 1.0
 *
 */
public class PuntualGridder implements Gridder

{

    private TraceManager tracer = new TraceManager();
    /**
     * polylist
     */
    private String polyList;

    // Point in the grid
    private GridPoint point = null;

    // dimension of the side of the square enclosing the point in the AR poslist
    // meters
    private double squareSideDimension = 60;

    /**
     * Data Elevation Model
     */
    DEMManager dem;

    /**
     *
     * @param polyList,
     *            the list of point enclosing the programming area (in this case
     *            onli one poin)
     * @param dem,
     *            the instane of dem
     * @throws GridException
     */
    public PuntualGridder(final String polyList, final DEMManager dem) throws GridException

    {
        /**
         * initialize
         */

        String value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.POINT_TO_SQUARE_DIMENSION_CONF_KEY);
        if (value != null)
        {
            try
            {
                double dValue = Double.valueOf(value);
                this.squareSideDimension = FeasibilityConstants.half * dValue;
            }
            catch (Exception e)
            {
                /**
                 * No value found in Configuration using default
                 */

                // logger.warn("Unable to found " +
                // FeasibilityConstants.MAX_AREA_OF_INTEREST_CONF_KEY + " in
                // conffiguration");
                this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Malformed " + FeasibilityConstants.POINT_TO_SQUARE_DIMENSION_CONF_KEY + " in configuration");

            }

        }
        else
        {

            /**
             * No value found in Configuration using default
             */

            // logger.warn("Unable to found " +
            // FeasibilityConstants.MAX_AREA_OF_INTEREST_CONF_KEY + " in
            // conffiguration");
            this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.POINT_TO_SQUARE_DIMENSION_CONF_KEY + " in configuration");

        }

        this.polyList = polyList;
        this.dem = dem;

        // filling point
        evaluatePoint();

    }// end class

    /**
     * Evaluate the grid point
     *
     * @throws GridException
     */
    private void evaluatePoint() throws GridException
    {
        StringTokenizer tokens = new StringTokenizer(this.polyList);
        if (tokens.countTokens() != 2)
        {
            /**
             * Just throw
             */
            throw new GridException("Wrong Pos list");
        }

        try
        {
            /**
             * parsing the point latitude and longitude
             */
            double latitude = Double.valueOf(tokens.nextToken());
            double longitude = Double.valueOf(tokens.nextToken());
            /**
             * evalluating elevation
             */
            double elevation = this.dem.getElevation(latitude, longitude);
            GridPoint p = new GridPoint(1, latitude, longitude, elevation);

            this.point = p;
            /**
             * Adding point
             */

        } // end try
        catch (NumberFormatException e)
        {
            /**
             * Just throw
             */
            throw new GridException(e.getMessage());
        } // end catch
    }// end evaluatePoint

    /**
     * Fill the dridPointList with the point in the grid
     */
    @Override
    public void fillGrid(List<GridPoint> gridPointList) throws GridException

    {
        // adding point
        if (this.point != null)
        {
            gridPointList.add(this.point);
        } // end if
        else
        {
            throw new GridException("Wrong Pos list");
        } // end catch

    }// end class

    /**
     * @param arList
     * @return coverage
     * @throws GridException
     */
    @Override
    public double getCoverage(List<AcqReq> arList) throws GridException
    {
        double val = 0;
        if (arList.size() != 0)
        {
            /**
             * on point coverage is 0 or 1;
             */
            // val=(double)FeasibilityConstants.hundred;
            val = 1.0;
        } // end if
        return val;
    }// end class

    /**
     * Being a degered grid it will returns always 0
     *
     * @return the overlap factor
     */
    @Override
    public double getOverlapFactor(List<AcqReq> arList) throws GridException
    {
        // TODO Auto-generated method stub
        return 0;
    }// end class

    /**
     * Being a degered grid it will returns always 0
     *
     * @return the area of the programming area
     */
    @Override
    public double getAream2()
    {
        // TODO Auto-generated method stub
        return 0;
    }// end class

    /**
     * Being a degered grid it will returns always false
     *
     * @return true if the programming area is acorss the line of date
     */
    @Override
    public boolean isAcrossDateLine()
    {
        // TODO Auto-generated method stub
        return false;
    }// end class

    /**
     * Fake ovverride of interface method
     */
    @Override
    public List<GridPoint> getHolesCenter(List<AcqReq> arList) throws GridException
    {
        // TODO Auto-generated method stub
        return new ArrayList<>();
    }// end class

    /**
     * Fake ovverride of interface method
     */
    @Override
    public void buildMultiGeometryForExtension(List<AcqReq> arlist)
    {
        // TODO Auto-generated method stub

    }// end class

    /**
     * Fake ovverride of interface method
     */
    @Override
    public List<GridPoint> getGridpointInsdeAR(List<GridPoint> list, double[][] corners)
    {
        // TODO Auto-generated method stub
        return new ArrayList<>();
    }// end class

    /**
     * In case of single acqiuisition return the poslist of the polygon to be
     * used in AR poslist
     *
     * @return poslist
     */
    @Override
    public String retunPolyListForSingleAcq()
    {

        // Building poligon in the enu plane
        // centered on Grid point
        double enu0[] =
        { -this.squareSideDimension, -this.squareSideDimension, 0 };
        double enu1[] =
        { -this.squareSideDimension, this.squareSideDimension, 0 };
        double enu2[] =
        { this.squareSideDimension, this.squareSideDimension, 0 };
        double enu3[] =
        { this.squareSideDimension, -this.squareSideDimension, 0 };

        // Returning corners in the llh frame
        double[] llh0 = ReferenceFrameUtils.ecef2llh(ReferenceFrameUtils.enu2ecef(this.point.getEcef().toArray(), enu0), true); // first
        double[] llh1 = ReferenceFrameUtils.ecef2llh(ReferenceFrameUtils.enu2ecef(this.point.getEcef().toArray(), enu1), true); // second
        double[] llh2 = ReferenceFrameUtils.ecef2llh(ReferenceFrameUtils.enu2ecef(this.point.getEcef().toArray(), enu2), true); // third
        double[] llh3 = ReferenceFrameUtils.ecef2llh(ReferenceFrameUtils.enu2ecef(this.point.getEcef().toArray(), enu3), true); // fourth

        // building the poslist
        String retval = "" + llh0[0] + " " + llh0[1] + " ";
        retval = retval + llh1[0] + " " + llh1[1] + " ";
        retval = retval + llh2[0] + " " + llh2[1] + " ";
        retval = retval + llh3[0] + " " + llh3[1] + " ";
        retval = retval + llh0[0] + " " + llh0[1];

        // returning the poslist
        return retval;
    }// end retunPolyListForSingleAcq

	@Override
	public List<AcqReq> processOverlap(List<AcqReq> arList) throws GridException {
		// TODO Auto-generated method stub
		return arList;
	}

}// end class
