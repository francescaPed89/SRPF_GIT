/**
*
* MODULE FILE NAME:	PolarLineGridder.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Implementation of a Gridder interface in case of LineString at poles
*
* PURPOSE:			Perform Feasibility
*
* CREATION DATE:	17-05-2015
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
*--------------------------+------------+----------------+-------------------------------
* 19-12-2017 | Amedeo Bancone  |2.0| added method isAcrossDateLine();
* 									 added method needed for extension : buildMultiGeometryForExtension and getGridpointInsdeAR
* 									 added method for centroid
*
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.feasibility;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.geotools.referencing.GeodeticCalculator;

import com.telespazio.csg.srpf.dem.DEMManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.ReferenceFrameUtils;
import com.vividsolutions.jts.densify.Densifier;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;

/**
 *
 * This class is a specializaion of gridder for the case of lineString over
 * poles The JTS line is built on the Streographic projection plan
 *
 * @author Amedeo Bancone
 * @version 2.0
 *
 *
 */
public class PolarLineGridder extends LineGridder

{

    /**
     * true if south pole
     */
    private boolean isSouthPole = false;

    /**
     * Contructor
     *
     * @param lineString
     * @param dem
     * @throws GridException
     */
    public PolarLineGridder(String lineString, DEMManager dem) throws GridException

    {
        /**
         * Build a the gridder
         */
        super("", dem);
        this.lineString = lineString;

        /**
         * build if allowed
         */
        if ((lineString != null) && !lineString.equals(""))
        {
            /**
             * Building linestring JTS
             */
            this.line = this.buildLineString();
        }
        // //System.out.println("++++++++++++++++++++COSTRUTTORE POLO");

    }// end method

    /**
     * Return the centroid of the envelope
     */
    @Override
    public GridPoint getCentroid()
    {

        GridPoint centroid = null;

        try
        {
            if (this.line != null)
            {
                /**
                 * Retreiving centroid as center of the envelop of JTS geometry
                 */
                Point p = this.line.getEnvelope().getCentroid();
                double x = p.getX();
                double y = p.getY();

                /**
                 * Transforming by inverse stereographic projection
                 */
                double[] latLon = ReferenceFrameUtils.fromStereoToLatLon(x, y, this.isSouthPole);

                double height = this.dem.getElevation(latLon[0], latLon[1]);

                centroid = new GridPoint(-1, latLon[0], latLon[1], height);

                /**
                 * Set centroid true
                 */
                centroid.setCentroid(true);
                /**
                 * true if centroid belogns the line
                 */
                centroid.setInternal(p.within(this.line));

            } // end if
        } // end try
        catch (Exception e)
        {
            /**
             * just log error
             */
            this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, e.getMessage());

        } // end catch

        return centroid;
    }// end method

    /**
     * Fill the gridPointList
     *
     * @param gridPointList
     * @throws GridException
     */
    @Override
    public void fillGrid(List<GridPoint> gridPointList) throws GridException
    {
        /**
         * Building JTS line string
         */
        this.line = buildLineString();
        Coordinate[] coords = this.line.getCoordinates();
        GridPoint p;
        double latitude;
        double longitude;
        double height;
        for (int i = 0; i < coords.length; i++)
        {
            /**
             * Trasnforimg form stereographic plan to llh
             */
            double[] latLon = ReferenceFrameUtils.fromStereoToLatLon(coords[i].x, coords[i].y, this.isSouthPole);

            latitude = latLon[0];
            longitude = latLon[1];
            /**
             * Evaluating elevation Creating grid point add to list
             */
            height = this.dem.getElevation(latitude, longitude);
            p = new GridPoint(i, latitude, longitude, height);
            gridPointList.add(p);
        } // end for;

    }// end method

    /**
     * Build the lineString object in stereographic plan
     *
     * @throws GridException
     */
    @Override
    protected LineString buildLineString() throws GridException

    {
        // //System.out.println("++++++++++++++++++++buildLineString POLO");
        LineString line;
        /**
         * Tokenizing linestring
         */
        StringTokenizer tokens = new StringTokenizer(this.lineString);

        if ((tokens.countTokens() == 0) || (((tokens.countTokens()) % 2) != 0)) // No
                                                                                // elements
                                                                                // or
                                                                                // spare
                                                                                // elements
        {
            /**
             * wrong linestring
             */
            throw new GridException("Wrong Line String");
        } // end if

        int numberOfPoint = tokens.countTokens() / 2;
        Coordinate[] stereoCoords = new Coordinate[numberOfPoint];

        int currentCoord = 0;
        double latitude;
        double longitude;

        /**
         * iterating on tokens
         */
        while (tokens.hasMoreElements())
        {
            try
            {
                latitude = Double.valueOf(tokens.nextToken());
                /**
                 * South pole
                 */
                if (latitude < 0)
                {
                    this.isSouthPole = true;
                }

                longitude = Double.valueOf(tokens.nextToken());

                /**
                 * Stereographic tranformation
                 */
                double[] stereo = ReferenceFrameUtils.fromLatLongToStereo(latitude, longitude);
                stereoCoords[currentCoord] = new Coordinate(stereo[0], stereo[1]);
                currentCoord++;

            } // en try
            catch (NumberFormatException e)
            {
                /**
                 * error in trasorming to number
                 */
                throw new GridException(e.getMessage());
            }

        } // end while

        LineString returnedLine;

        try
        {
            /**
             * Building line
             */
            line = this.geometryFactory.createLineString(stereoCoords);

            /**
             * have to densify
             */
            if (this.haveDensify)
            {
                this.tracer.debug("Have densify line");

                try
                {
                    /**
                     * Trying to dendify line
                     */

                    returnedLine = (LineString) Densifier.densify(line, evaluateGridSpacing(this.densifierTolerance));
                } // end
                catch (Exception e)
                {
                    this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.APPLICATION_SUBSYSTEM_FAILURE, "Unable to densify line using non densified line" + e.getMessage());
                    returnedLine = line;
                }

            } // end if
            else
            {
                /**
                 * Not have to densify
                 */
                returnedLine = line;
            }

            this.tracer.debug("Frontier point: " + returnedLine.getCoordinates().length);

        }
        catch (IllegalArgumentException e)
        {
            /**
             * Just throw excetpion
             */
            throw new GridException(e.getMessage());
        }
        // return denseLine;
        return returnedLine;

    }// end buildGridFromPolyListString

    /**
     * Build a polyn from an AR
     *
     * @param ar
     * @return Polygon
     */
    @Override
	public Polygon getPolygonFromAR(AcqReq ar)
    {
        // this.tracer.debug("getPolygonFromAR polar");

        Polygon retval = null;
        /**
         * getting corners
         */
        double[][] corners = ar.getDTOList().get(0).getCorners();
        /**
         * building polygon
         */
        retval = getPolygonFromCorners(corners);
        return retval;

    }// end getPolygonFromAR

    /**
     * Return a polygon from corners
     *
     * @param corners
     * @return polygon
     */
    @Override
    protected Polygon getPolygonFromCorners(double[][] corners)
    {
        // this.tracer.debug("getPolygonFromAR polar");
        /**
         * retval
         */
        Polygon retval = null;
        int cornerNumber = corners.length;
        Coordinate[] coords = new Coordinate[cornerNumber + 1];
        for (int i = 0; i < cornerNumber; i++)
        {
            /**
             * Transforming to stereo plan
             */
            double[] stereo = ReferenceFrameUtils.fromLatLongToStereo(corners[i][0], corners[i][1]);

            coords[i] = new Coordinate(stereo[0], stereo[1]);
        } // end for
        coords[cornerNumber] = new Coordinate(coords[0].x, coords[0].y);
        /**
         * Building polygon
         */
        retval = this.geometryFactory.createPolygon(coords);

        /**
         * returning
         */
        return retval;

    }// end getPolygonFromAR

    /**
     * Return the centroid of holes in the coverage area, empty list if no holes
     * are found
     *
     * @param arList
     * @return the list of holes centers, null if no holes are found
     * @throws GridException
     */
    @Override
    public List<GridPoint> getHolesCenter(List<AcqReq> arList) throws GridException
    {
        /**
         * return list
         */
        List<GridPoint> gridpointList = new ArrayList<>();

        /**
         * Retrieve centroid as JTS points
         */
        List<Point> centroidList = getHolesCentroid(arList);

        double latitude;
        double longitude;
        GridPoint gridPoint;
        double[] llh;
        for (Point p : centroidList)
        {
            /**
             * For each JTS point transform in llh then create the relevant grid
             * point and add it to retval list
             */
            latitude = p.getX();
            longitude = p.getY();

            llh = ReferenceFrameUtils.fromStereoToLatLon(p.getX(), p.getY(), this.isSouthPole);

            latitude = llh[0];
            longitude = llh[1];

            gridPoint = new GridPoint(gridpointList.size(), latitude, longitude, this.dem.getElevation(latitude, longitude));
            gridpointList.add(gridPoint);
        }

        return gridpointList;
    }// end method

    /**
     * translate gridspacing from deg to meters Since grid spacing is in deg,
     * but to poles we work on sterographic plan, so in a plan using linear
     * coordinate, we have translate the spacing in meters
     *
     * @return grid dpacing
     */
    private double evaluateGridSpacing(double spacing)
    {

        /**
         * Evaluating the distance between two point llh0 and llh1 the first
         * having: llh0 =0,0,0 llh1 = 0,spacing,0
         */
        GeodeticCalculator calc = new GeodeticCalculator();
        calc.setStartingGeographicPoint(0, 0);
        calc.setDestinationGeographicPoint(spacing, 0);
        return calc.getOrthodromicDistance();

    }// end method

    /**
     * Evaluate the list of grid point insed an AR
     *
     * @param list
     *            of points to be evaluated
     * @param corners
     * @return list of gridpoint insed AR
     */
    @Override
    public List<GridPoint> getGridpointInsdeAR(List<GridPoint> list, double[][] corners)
    {
        /**
         * retval list
         */
        List<GridPoint> ppoinList = new ArrayList<>();

        /**
         * AR Polygon
         */
        Polygon arPol = getPolygonFromCorners(corners);

        Polygon reducedArPolygon = (Polygon) GeometryPrecisionReducer.reduce(arPol, new PrecisionModel());

        Point currentPoint;
        double latitude;
        double longitude;
        double[] stereoCoords;
        for (GridPoint p : list)
        {
            /**
             * for each point in the list to be evaluated 1) transform in stereo
             * plan 2)check if the transformed point is inside polygon 3)if yes
             * add the original point to retval list
             */
            latitude = p.getLLH()[0];
            longitude = p.getLLH()[1];

            stereoCoords = ReferenceFrameUtils.fromLatLongToStereo(latitude, longitude);

            currentPoint = this.geometryFactory.createPoint(new Coordinate(stereoCoords[0], stereoCoords[1]));
            if (currentPoint.within(arPol))
            {
                ppoinList.add(p);
            } // end if
        } // end for

        return ppoinList;
    }// end method

}// end class
