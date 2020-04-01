/**
*
* MODULE FILE NAME:	PolarPolygonGridder.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Implementation of a Gridder interface in case of Polygon at poles
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
*
* PROCESSING
*/

package com.telespazio.csg.srpf.feasibility;

import java.io.IOException;
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
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;

/**
 * Build a Grid for an area enclosed by a polygon over polar region
 * Stereographic transformation are used
 *
 * @author Amedeo Bancone
 * @version 2.0
 *
 */
public class PolarPolygonGridder extends PolygonGridder

{

    /**
     * true if south pole
     */
    private boolean isSouthPole = false;

    /**
     * true if building from a circle
     */
    private boolean isCircle = false;

    /**
     * if circle center lat
     */
    private double centerLat = 0;
    /**
     * if circle center longitude
     */
    private double centerLong = 0;
    /**
     * if circle radius
     */
    private double radius = 0;

    /**
     *
     * Build polygon from a polyList
     *
     * @param polyList
     * @param dem
     * @param gridSpacing
     * @throws GridException
     */
    public PolarPolygonGridder(String polyList, DEMManager dem, double gridSpacing) throws GridException

    {
        /**
         * Calling super class constructor
         */
        super("", dem, gridSpacing);
        this.polyList = polyList;
        /**
         * Building polygon
         */
        if ((polyList != null) && (!polyList.equals("")))
        {
            this.polygon = getPolygonEnclosingArea(polyList);
        }

    }

    /**
     * Build a poligon from circle
     *
     * @param targetCenter,
     *            as for xml request
     * @param radius
     * @param dem
     * @param gridSpacing
     * @throws GridException
     */
    public PolarPolygonGridder(String targetCenter, String radius, DEMManager dem, double gridSpacing) throws GridException
    {
        /**
         * build superclass
         */
        super("", dem, gridSpacing);

        /**
         * Evaluating latitude longitude of center end radiud of circle
         */
        StringTokenizer tokens = new StringTokenizer(targetCenter);

        /**
         * No elements or spare elements
         */
        if (tokens.countTokens() != 2)
        {
            /**
             * Just throw exception
             */
            throw new GridException("Malformed circle center point");
        }

        try
        {
            /**
             * Transforming string to number
             */
            double lat = Double.valueOf(tokens.nextToken());
            double longitude = Double.valueOf(tokens.nextToken());

            this.centerLat = lat;
            this.centerLong = longitude;
            /**
             * Transforming radius in meters
             */
            this.radius = Double.valueOf(radius) * FeasibilityConstants.Kilo;

        } // end try
        catch (NumberFormatException e)
        {
            /**
             * Error in tranfformin string to numeber just throw an exception
             */
            throw new GridException(e.getMessage());
        }

        /**
         * South pole
         */
        if (this.centerLat < 0)
        {
            this.isSouthPole = true;
        }

        /**
         * building polygon
         */
        this.polygon = toCircleToPolygon(FeasibilityConstants.CircleToPolygonAngularStep);
        /**
         * setting circle flag
         */
        this.isCircle = true;
    }// end method

    /**
     * Check if the minimmum AoI is inside the poligon and if true insert the
     * point in the gridpointlist
     *
     * @param poslist
     * @param gridPointList
     * @return true if MinAoi inside polygon
     * @throws GridException
     */
    @Override
    public boolean insertCheckMinimumAoI(String aoiPoslist, List<GridPoint> gridPointList) throws GridException
    {
        this.tracer.log("Checking for the minumum Area of interest");
        /**
         * return value
         */
        boolean retval = false;

        /**
         * polygon enveloping minimum area of interest
         */
        Polygon aoiPolygon = this.getPolygonEnclosingArea(aoiPoslist);

        /**
         * check if inside
         */
        retval = aoiPolygon.within(this.polygon);

        /**
         * if true insert min AOI point in the gridpoint list
         */
        if (retval)
        {
            /**
             * Coordinates of MinAoi polygon
             */
            Coordinate[] coords = aoiPolygon.getCoordinates();

            GridPoint p;

            double latitude;
            double longitude;

            double height;
            /**
             * First and last point in the polygon are the same
             */
            int numberOfPointToBeInserted = coords.length - 1;

            for (int i = 0; i < numberOfPointToBeInserted; i++)
            {
                /**
                 * For each JTS point belonging the MinAoi polygon 1)Transform
                 * in LLH 2)evaluate elevation 3)Create the related gridpoint 4)
                 * add to grid point list
                 */
                double[] latLon = ReferenceFrameUtils.fromStereoToLatLon(coords[i].x, coords[i].y, this.isSouthPole);

                latitude = latLon[0];
                longitude = latLon[1];

                height = this.dem.getElevation(latitude, longitude);

                p = new GridPoint(gridPointList.size(), latitude, longitude, height);
                p.setBelongsToMinimumAoI(true);
                gridPointList.add(p);
            } // end for

            // logger.debug("Grid built");
        } // end if

        /**
         * returning
         */
        return retval;
    } // end method

    /**
     * Return the centroid of the envelope
     */
    @Override
    public GridPoint getCentroid()
    {
        /**
         * retval
         */
        GridPoint centroid = null;

        try
        {
            if (this.polygon != null)
            {
                /**
                 * eavluating centroid from envelope tranform in llh create grid
                 * point
                 */
                Point p = this.polygon.getEnvelope().getCentroid();
                double x = p.getX();
                double y = p.getY();
                double[] latLon = ReferenceFrameUtils.fromStereoToLatLon(x, y, this.isSouthPole);
                double height = this.dem.getElevation(latLon[0], latLon[1]);

                centroid = new GridPoint(-1, latLon[0], latLon[1], height);

                centroid.setCentroid(true);
                centroid.setInternal(p.within(this.polygon));

            } // end if
        } // end try
        catch (Exception e)
        {
            /**
             * just log
             */
            this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, e.getMessage());

        }
        /**
         * returning
         */
        return centroid;
    }// en method

    /**
     * Build the grid form the position list of the polygon retrieved by the
     * PRList
     *
     * @param polyList
     * @throws IOException
     */
    @Override
    protected void buildGridFromPolyListString() throws GridException

    {
        this.tracer.debug("Building polar grid");
        if (!this.isCircle)
        {
            /**
             * Case polygon
             */
            this.polygon = getPolygonEnclosingArea(this.polyList);
        }
        else
        {
            /**
             * case circle
             */
            this.polygon = toCircleToPolygon(FeasibilityConstants.CircleToPolygonAngularStep);
        }

        // logger.info("Poligon corners: " +
        // this.polygon.getCoordinates().length);

        // Adding point in polygon to Grid

        /**
         * retrieving coordinates
         */
        Coordinate[] coords = this.polygon.getCoordinates();

        GridPoint p;

        double latitude;
        double longitude;

        double height;
        /**
         * First and last point in the polygon are the same
         */
        int numberOfPointToBeInserted = coords.length - 1;

        for (int i = 0; i < numberOfPointToBeInserted; i++)
        {
            /**
             * for each point : transform JTS to llh evaluate dem add to grid
             */
            double[] latLon = ReferenceFrameUtils.fromStereoToLatLon(coords[i].x, coords[i].y, this.isSouthPole);

            latitude = latLon[0];
            longitude = latLon[1];

            height = this.dem.getElevation(latitude, longitude);

            p = new GridPoint(i, latitude, longitude, height);

            this.gridPointList.add(p);
        } // end for

        /**
         * builing polygon
         */
        buildGridInsidePolygon(this.polygon);

        // logger.debug("Grid built");
        this.tracer.log("Grid built");

    }// end buildGridFromPolyListString

    /**
     * Generate a polygon encosing the area of interest
     *
     * @param polyList
     * @return Polygon
     * @throws IOException
     */
    private Polygon getPolygonEnclosingArea(String polyList) throws GridException
    {
        // logger.debug("getPolygonEncl

        this.tracer.debug("Building polar grid getPolygonEnclosingArea");

        /**
         * Tokemize pos list
         */
        StringTokenizer tokens = new StringTokenizer(polyList);

        if ((tokens.countTokens() == 0) || (((tokens.countTokens()) % 2) != 0)) // No
                                                                                // elements
                                                                                // or
                                                                                // spare
                                                                                // elements
        {
            /**
             * just throw excpetion
             */
            throw new GridException("Wrong Polygon");
        }

        int numberOfPoint = tokens.countTokens() / 2;

        // logger.info("Number of point " + numberOfPoint);

        Coordinate[] stereoCoord = new Coordinate[numberOfPoint];

        int currentCoord = 0;
        double latitude;
        double longitude;

        while (tokens.hasMoreElements())
        {
            /**
             * Iterating on tokens
             */
            try
            {
                /**
                 * Fro each couple lat lon tranform to strereo add to stereo
                 * coordinate array
                 */
                latitude = Double.valueOf(tokens.nextToken());
                if (latitude < 0)
                {
                    this.isSouthPole = true;
                }
                longitude = Double.valueOf(tokens.nextToken());
                double[] stereo = ReferenceFrameUtils.fromLatLongToStereo(latitude, longitude);
                stereoCoord[currentCoord] = new Coordinate(stereo[0], stereo[1]);
                currentCoord++;

            } // end try
            catch (NumberFormatException e)
            {
                /**
                 * rethrow
                 */
                throw new GridException(e.getMessage());
            }

        } // end while

        /**
         * return polygon
         */
        return getPolygon(stereoCoord);

    } // end getPolygonEnclosingArea

    /**
     * transorm a circle to polygon
     *
     * @return polygon
     * @throws GridException
     *
     **/
    private Polygon toCircleToPolygon(double angularStep) throws GridException
    {
        // //System.out.println("=====================================================Building
        // poligon");

        /**
         * Stereo coordinate sof polygon
         */
        ArrayList<Coordinate> stereoCoordList = new ArrayList<>();

        /**
         * circle center in stereo plan
         */
        double[] stereoCenter = ReferenceFrameUtils.fromLatLongToStereo(this.centerLat, this.centerLong);
        double currentAngle = 0;
        double x;
        double y;

        for (int i = 0; i < (360 / angularStep); i++)
        {
            /**
             * For each step evaluate stereo coords
             */
            currentAngle = i * angularStep;
            x = (this.radius * Math.cos(currentAngle)) + stereoCenter[0];
            y = (this.radius * Math.sin(currentAngle)) + stereoCenter[1];
            stereoCoordList.add(new Coordinate(x, y));

        }

        stereoCoordList.add(new Coordinate(stereoCoordList.get(0)));

        Coordinate[] stereoCoord = new Coordinate[stereoCoordList.size()];

        int i = 0;
        for (Coordinate c : stereoCoordList)
        {
            /**
             * Adding coordinate to stero vector
             */
            stereoCoord[i] = c;
            i++;
        }

        /// DA VEDERE SE TENERE////
        // Build the polylist
        String appo = "";
        boolean first = true;
        for (int j = 0; j < stereoCoord.length; j++)
        {
            if (first)
            {
                first = false;
            }
            else
            {
                appo = appo + " ";
            }
            double[] appoLLH = ReferenceFrameUtils.fromStereoToLatLon(stereoCoord[j].x, stereoCoord[j].y, this.isSouthPole);
            appo = appo + appoLLH[0] + " " + appoLLH[1];
        }
        this.polyList = appo;
        /// Fine Da vedere se tenere////

        /**
         * return
         */
        return getPolygon(stereoCoord);
    }// end toCircleToPolygon

    /**
     * Build polygon starting from coordinates
     *
     * @param stereoCoord
     * @return polygon
     * @throws GridException
     */
    private Polygon getPolygon(Coordinate[] stereoCoord) throws GridException

    {
        this.tracer.debug("Building polar grid getPolygon");

        /**
         * Stero polygon
         */
        Polygon stereoPoly;

        /**
         * retuirned polygon
         */
        Polygon returnedPoligon;

        try
        {
            /**
             * create stereo polygon
             */
            stereoPoly = this.geometryFactory.createPolygon(stereoCoord);
            if (this.haveDensify)
            {
                this.tracer.debug("Have densify polygon");
                try
                {
                    /**
                     * Trying to densify polygon
                     */
                    returnedPoligon = (Polygon) Densifier.densify(stereoPoly, evaluateGridSpacing(this.densifierTolerance));
                    // returnedPoligon= (Polygon) Densifier.densify(stereoPoly,
                    // );
                    // returnedPoligon=stereoPoly;
                }
                catch (Exception e)
                {
                    /**
                     * just log
                     */
                    this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.APPLICATION_SUBSYSTEM_FAILURE, e.getMessage());
                    this.tracer.log("Unable to densify polygon using non densified polygon");
                    returnedPoligon = stereoPoly;
                }

            }
            else
            {
                /**
                 * no densify
                 */
                returnedPoligon = stereoPoly;
            }

            this.tracer.debug("Frontier point: " + returnedPoligon.getCoordinates().length);

        }
        catch (IllegalArgumentException e)
        {
            /**
             * Just throw
             */
            throw new GridException(e.getMessage());
        }

        /**
         *
         * return
         */
        return returnedPoligon;

    }// end getPolygon

    /**
     * Generate a gripoint inside the polygon
     *
     * @param poly
     */
    private void buildGridInsidePolygon(Polygon poly)

    {
        // logger.debug("buildGridInsidePolygon");

        /**
         * Envelop to buoild grid
         */
        Polygon envelope = (Polygon) poly.getEnvelope();

        /**
         * envelope coordinates
         */
        Coordinate[] coord = envelope.getCoordinates();

        double minX = coord[0].x;
        double minY = coord[0].y;

        double maxX = coord[2].x;
        double maxY = coord[2].y;

        // logger.info("Minlong: " + minLongitude + " max long: " +maxLongitude
        // );

        /*
         * System.err.println("min lat: " + minLatitude + " min long: " +
         * minLongitude); System.err.println("max lat: " + maxLatitude +
         * " max long: " + maxLongitude); for(int i =0; i< coord.length;i++)
         * System.err.println("lat: " + coord[i].x + "  long: " + coord[i].y);
         */

        double currentX = minX;
        Coordinate currentCoord;
        Point currentPoint;

        // logger.error("Gridspacing: " + gridSpacing);

        double gridSpacingMeters = evaluateGridSpacing(this.gridSpacing);

        for (int i = 1; currentX < maxX; i++) // loop on latitude

        {

            /**
             * iterating on X
             */
            double currentY = minY;
            currentX = minX + (i * gridSpacingMeters);

            for (int j = 1; currentY < maxY; j++)

            {
                /**
                 * iyerting on y
                 */
                currentY = minY + (j * gridSpacingMeters);

                /**
                 * creating JTS point
                 */
                currentCoord = new Coordinate(currentX, currentY);

                currentPoint = this.geometryFactory.createPoint(currentCoord);

                // System.err.println();
                // System.err.print(currentLatitude + " " + currentLongitude);

                if (currentPoint.within(poly))
                {
                    // TODO controllare se il punto è duplicato?
                    // System.err.print(" inside" );
                    /**
                     * point inside area Retranform in llh create Grid Point add
                     * to list
                     */
                    double[] latLong = ReferenceFrameUtils.fromStereoToLatLon(currentX, currentY, this.isSouthPole);

                    this.gridPointList.add(new GridPoint(this.gridPointList.size(), latLong[0], latLong[1], this.dem.getElevation(latLong[0], latLong[1])));

                } // end if
            }

        }

    }// end gridInsidepolygon

    /**
     * translate gridspacing from deg to meters
     *
     * @return
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

    }

    /**
     * Evaluate the list of grid point insed an AR
     *
     * @param list
     *            of points to be evaluated
     * @param corners
     * @return list of gridpoint insed AR
     */
    @Override
    protected Polygon getPolygonFromAR(AcqReq ar)
    {
        // this.tracer.debug("getPolygonFromAR polar");

        Polygon retval = null;

        /**
         * corners from DTO
         */
        double[][] corners = ar.getDTOList().get(0).getCorners();

        /**
         * Retrieving polygon
         */
        retval = getPolygonFromCorners(corners);

        return retval;

    }// end getPolygonFromAR

    /**
     * Return a polygon from the first DTO of the AR
     *
     * @param ar
     * @return
     */
    @Override
    protected Polygon getPolygonFromCorners(double[][] corners)
    {
        Polygon retval = null;

        int cornerNumber = corners.length;

        /**
         * Number of stereo coords
         */
        Coordinate[] coords = new Coordinate[cornerNumber + 1];

        for (int i = 0; i < cornerNumber; i++)
        {
            /**
             * For each corner 1)Streo tranform 2)create JTS coordinate
             */

            double[] stereo = ReferenceFrameUtils.fromLatLongToStereo(corners[i][0], corners[i][1]);

            coords[i] = new Coordinate(stereo[0], stereo[1]);
        } // end for
        coords[cornerNumber] = new Coordinate(coords[0].x, coords[0].y);

        /**
         * Build polygon
         */
        retval = this.geometryFactory.createPolygon(coords);

        return retval;
    }

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
        // //System.out.println("=====================Getting holes
        // center=========================");
        /**
         * retval
         */
        List<GridPoint> gridpointList = new ArrayList<>();

        /**
         * List of JTS centroids
         */
        List<Point> centroidList = getHolesCentroid(arList);

        double latitude;
        double longitude;
        GridPoint gridPoint;
        double[] llh;
        /**
         * for each JTS centroid 1) Tranform 2)build gridpoint 3)add to retval
         * list
         */
        for (Point p : centroidList)
        {
            latitude = p.getX();
            longitude = p.getY();

            llh = ReferenceFrameUtils.fromStereoToLatLon(p.getX(), p.getY(), this.isSouthPole);

            latitude = llh[0];
            longitude = llh[1];

            gridPoint = new GridPoint(gridpointList.size(), latitude, longitude, this.dem.getElevation(latitude, longitude));
            gridpointList.add(gridPoint);
        }

        /**
         * returning
         */
        return gridpointList;
    }

    /**
     *
     * @param list
     * @param corners
     * @return grigpoomy list
     */
    @Override
    public List<GridPoint> getGridpointInsdeAR(List<GridPoint> list, double[][] corners)
    {
        /**
         * retval list
         */
        List<GridPoint> ppoinList = new ArrayList<>();
        /**
         * polygon from AR
         */
        Polygon arPol = getPolygonFromCorners(corners);

        Polygon reducedArPolygon = (Polygon) GeometryPrecisionReducer.reduce(arPol, new PrecisionModel());

        Point currentPoint;
        double latitude;
        double longitude;
        double[] stereoCoords;
        /**
         * for each point 1) tranform in stereo plan 2) check if tranfromed
         * inside AR polygon 3) if yes add to ret list
         */
        for (GridPoint p : list)
        {
            latitude = p.getLLH()[0];
            longitude = p.getLLH()[1];

            stereoCoords = ReferenceFrameUtils.fromLatLongToStereo(latitude, longitude);

            currentPoint = this.geometryFactory.createPoint(new Coordinate(stereoCoords[0], stereoCoords[1]));
            if (currentPoint.within(arPol))
            {
                ppoinList.add(p);
            }
        }

        /**
         * retrurn
         */
        return ppoinList;
    }// end method

    /**
     * return Area
     */
    @Override
    public double getAream2()
    {
        return this.polygon.getArea();
    }
} // end class
