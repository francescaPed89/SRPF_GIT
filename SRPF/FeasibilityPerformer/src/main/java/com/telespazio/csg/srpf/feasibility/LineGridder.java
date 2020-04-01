/**
*
* MODULE FILE NAME:	LineGridder.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Implementation of a Gridder interface in case of LineString
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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.telespazio.csg.srpf.dem.DEMManager;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.PropertiesReader;
import com.vividsolutions.jts.densify.Densifier;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.operation.union.UnaryUnionOp;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;

/**
 * This class is a specializaion of gridder for the case of lineString
 *
 * @author Amedeo Bancone
 * @version 2.0
 *
 */
public class LineGridder implements Gridder

{
	static final Logger logger = LogManager.getLogger(LineGridder.class.getName());

    // static final Logger logger =
    // LogManager.getLogger(LineGridder.class.getName());

    /**
     * logger
     */
    protected TraceManager tracer = new TraceManager();
    /**
     * DEM
     */
    protected DEMManager dem;

    /**
     * Line string
     */
    protected String lineString;
    /**
     * true if we have densify number of points
     */
    protected boolean haveDensify = false;
    /**
     * densify tolerance to be read from configuration
     */
    protected double densifierTolerance = 0.1;

    /**
     * gridpoing list
     */
    protected List<GridPoint> gridPointList;

    /**
     * line as geometry jts
     */
    protected LineString line;

    /**
     * factory used in building geometries
     */
    protected GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * treu if across line of date
     */
    protected boolean isOverDateLine = false;

    /**
     * this is a value to considert tarh a change of sign in longitude means an
     * overcome of the line of date
     */
    protected double longitudeLimitToUnderstandForLineDate = FeasibilityConstants.LongitudeLimitToUnderstandForLineDate;

    /**
     * Value of the ratio hole/target area to consider the hole
     */
    protected double notAllowedHoleAreaRatio;

    /**
     * Used during extension to perform overlap and coverage
     */
    protected Geometry multiGeometry = null;

    /**
     * Defaut constructor
     */
    public LineGridder()
    {

    }// end method

    /**
     * Constructor
     *
     * @param lineString
     * @param dem
     * @throws GridException
     */
    public LineGridder(final String lineString, final DEMManager dem) throws GridException

    {
        this.lineString = lineString;
        this.dem = dem;
        /**
         * initializing configuration
         */
        initConf();

        if ((lineString != null) && !lineString.equals(""))
        {
            /**
             * buildi the line string
             */
            this.line = this.buildLineString();
        }
    }// end method

    /**
     * Build the multigeometry from an ar list used in extension
     *
     * @param arlist
     */
    @Override
    public void buildMultiGeometryForExtension(List<AcqReq> arlist)
    {

        List<Geometry> polyList = new ArrayList<>();
        Geometry p;
        for (AcqReq a : arlist)
        {
            /**
             * For each acq build the relevant polygon
             */
            p = getPolygonFromCorners(a.getDTOList().get(0).getCorners());

            /**
             * finding the intersection between ar polygon and the line string
             */
            Geometry currentLine = this.line.intersection(GeometryPrecisionReducer.reduce(p, new PrecisionModel()));

            /**
             * adding intersection to list
             */
            polyList.add(GeometryPrecisionReducer.reduce(currentLine, new PrecisionModel()));
            // polyList.add(p);

        } // end For

        // logger.debug("Unioning");
        /**
         * creating multigeometry
         */
        this.multiGeometry = UnaryUnionOp.union(polyList);
    }// end method

    /**
     * Return the centroid of the envelope of the polygon
     *
     * @return centroid as grid point null if none
     */
    public GridPoint getCentroid()
    {

        GridPoint centroid = null;
        try
        {
            if (this.line != null)
            {
                Point p = this.line.getEnvelope().getCentroid();

                double latitude = p.getX();
                double longitude = p.getY();

                /**
                 * check if across line date
                 */
                if ((longitude > 180) && this.isOverDateLine)
                {
                    longitude = longitude - 360;
                }

                double height = this.dem.getElevation(latitude, longitude);
                /**
                 * create point
                 */
                centroid = new GridPoint(-1, latitude, longitude, height);
                /**
                 * is centroid
                 */
                centroid.setCentroid(true);
                /**
                 * true if centroid is in line
                 */
                centroid.setInternal(p.within(this.line));

            }

        } // end try
        catch (Exception e)
        {
            this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, e.getMessage());

        } // end cathc
          // //System.out.println("Returning centroid");
        return centroid;
    }// end method

    /**
     * Initialize the configuration
     */
    protected void initConf()
    {
        /**
         * Reading property
         */
        String value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.LONGITUDE_LIMIT_TO_UNDERSTAND_FOR_LINE_DATE_CONF_KEY);
        if (value != null)
        {
            try
            {
                double dValue = Double.valueOf(value);
                this.longitudeLimitToUnderstandForLineDate = dValue;
            }
            catch (Exception e)
            {
                /**
                 * Misconfigured property using default
                 */
                // logger.warn("Unable to found " +
                // FeasibilityConstants.LONGITUDE_LIMIT_TO_UNDERSTAND_FOR_LINE_DATE_CONF_KEY
                // + " in configuration");
                this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.LONGITUDE_LIMIT_TO_UNDERSTAND_FOR_LINE_DATE_CONF_KEY + " in configuration");

            }

        }
        else
        {
            /**
             * not configured using default
             */
            // logger.warn("Unable to found " +
            // FeasibilityConstants.LONGITUDE_LIMIT_TO_UNDERSTAND_FOR_LINE_DATE_CONF_KEY
            // + " in configuration");
            this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.LONGITUDE_LIMIT_TO_UNDERSTAND_FOR_LINE_DATE_CONF_KEY + " in configuration");

        }
        /**
         * Reading property
         */
        value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.LINE_DENSIFIER_TOLERANCE_CONF_KEY);
        if (value != null)
        {
            try
            {
                double dValue = Double.valueOf(value);
                this.densifierTolerance = dValue;
            }
            catch (Exception e)
            {
                /**
                 * Misconfigured property using default
                 */

                this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.LINE_DENSIFIER_TOLERANCE_CONF_KEY + " in configuration");
            }

        }
        else
        {
            /**
             * not configured using default
             */
            this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.LINE_DENSIFIER_TOLERANCE_CONF_KEY + " in configuration");
        }

        /**
         * Reading property
         */
        value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.HAVE_DENSIFY_LINE_CONF_KEY);
        if (value != null)
        {
            try
            {
                int iValue = Integer.valueOf(value);
                if (iValue == 1)
                {
                    this.haveDensify = true;
                }
            }
            catch (Exception e)
            {
                /**
                 * Misconfigured property using default
                 */

                this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.HAVE_DENSIFY_LINE_CONF_KEY + " in configuration");
            }

        }
        else
        {
            /**
             * not configured using default
             */
            this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.HAVE_DENSIFY_LINE_CONF_KEY + " in configuration");
        }

        // HOLES PARAMETERS
        /**
         * Reading property
         */
        value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.NOT_ALLOWED_HOLE_AREA_RATIO_CONF_KEY);
        if (value != null)
        {
            try
            {
                double dValue = Double.valueOf(value);
                this.notAllowedHoleAreaRatio = dValue;
            }
            catch (Exception e)
            {
                /**
                 * Misconfigured property using default
                 */

                this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.NOT_ALLOWED_HOLE_AREA_RATIO_CONF_KEY + " in configuration");
            }

        }
        else
        {
            /**
             * not configured using default
             */
            this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.NOT_ALLOWED_HOLE_AREA_RATIO_CONF_KEY + " in configuration");
        }

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
         * build the JTS geometry
         */
        this.line = buildLineString();
        /**
         * retriev coordinate
         */
        Coordinate[] coords = this.line.getCoordinates();
        GridPoint p;
        double latitude;
        double longitude;
        double height;
        for (int i = 0; i < coords.length; i++)
        {
            latitude = coords[i].x;
            longitude = coords[i].y;
            /**
             * check if across line of date
             */
            if (this.isOverDateLine && (longitude > 180))
            {
                longitude = longitude - 360;
            }

            /**
             * evaluate elevation
             */
            height = this.dem.getElevation(latitude, longitude);
            /**
             * crete grid point add to list
             */
            p = new GridPoint(i, latitude, longitude, height);
            gridPointList.add(p);
        } // end for;

    }// end method

    /**
     * get the coverage related to the given ARLIST
     *
     * @param arList
     * @return the coverage
     * @throws GridException
     */
    @Override
    public double getCoverage(List<AcqReq> arList) throws GridException
    {
        /**
         * return value
         */
        double retval = 0;

        try
        {
            double lenght;
            /**
             * list od polygon
             */
            List<Polygon> polyList = new ArrayList<>();

            Polygon p;
            for (AcqReq a : arList)
            {
                /**
                 * for each ar build polygon and add to list
                 */
                p = getPolygonFromAR(a);
                polyList.add((Polygon) GeometryPrecisionReducer.reduce(p, new PrecisionModel()));
            } // end For

            /**
             * Performing poligon union
             */
            Geometry dtoUnion = UnaryUnionOp.union(polyList);
         
            Geometry reducedLine;

            if (this.multiGeometry != null)
            {
                /**
                 * case of multigeometry (Extension)
                 */
                reducedLine = GeometryPrecisionReducer.reduce(this.multiGeometry, new PrecisionModel());
                lenght = this.multiGeometry.getLength();
            }
            else
            {
                reducedLine = GeometryPrecisionReducer.reduce(this.line, new PrecisionModel());
                lenght = this.line.getLength();
            }

            /**
             * intersection
             */
            Geometry intersection = dtoUnion.intersection(reducedLine);
            double intecectedLenght = intersection.getLength();
            /**
             * coverage = intecetopn / lenght
             */
            retval = intecectedLenght / lenght;
        } // end try
        catch (Exception e)
        {
            throw new GridException(e.getMessage());
        }
        return retval;
    }// end method

    /**
     * get the overlap factor for the given ARLIST
     *
     * @param arList
     * @return overlap factor
     * @throws GridException
     */
    @Override
    public double getOverlapFactor(List<AcqReq> arList) throws GridException
    {
        double retval = 0;

        try
        {
            double lenght = this.line.getLength();
            
            
            /**
             * list of polygon
             */
            List<Polygon> polyList = new ArrayList<>();

            double summOfIntesectedLenght = 0;

            Geometry reducedLine;
            if (this.multiGeometry != null)
            {
                /**
                 * case extension
                 */
                reducedLine = GeometryPrecisionReducer.reduce(this.multiGeometry, new PrecisionModel());
            }
            else
            {
                /**
                 * feasibility
                 */
                reducedLine = GeometryPrecisionReducer.reduce(this.line, new PrecisionModel());
            }

            Polygon p;
            for (AcqReq a : arList)
            {
                /**
                 * for each ar build polygon add to list end add its
                 * intersection with line to intersection summ
                 */
                p = getPolygonFromAR(a);
                polyList.add((Polygon) GeometryPrecisionReducer.reduce(p, new PrecisionModel()));

                summOfIntesectedLenght = summOfIntesectedLenght + p.intersection(reducedLine).getLength();
            } // end For

            /**
             * perform union of AR polygon
             */
            Geometry dtoUnion = UnaryUnionOp.union(polyList);
            ;

            /**
             * perform intersection between union and line / multine
             */
            Geometry intersection = dtoUnion.intersection(reducedLine);

            /**
             * evaluate lenght of interception
             */
            double intecectedLenght = intersection.getLength();
            /**
             * overlap = (1-(intercepted/sumofinterception))
             */
            retval = (1 - (intecectedLenght / summOfIntesectedLenght)) * 100;

        } // end try
        catch (Exception e)
        {
            throw new GridException(e.getMessage());
        }
        return retval;
    }// end method

    /**
     * Build the lineString object
     *
     * @throws GridException
     */
    protected LineString buildLineString() throws GridException

    {
        LineString jtsLine;
        /**
         * tokenize line string
         */
        StringTokenizer tokens = new StringTokenizer(this.lineString);

        /**
         * check if the number of tokens is right
         */
        if ((tokens.countTokens() == 0) || (((tokens.countTokens()) % 2) != 0)) // No
                                                                                // elements
                                                                                // or
                                                                                // spare
                                                                                // elements
        {
            throw new GridException("Wrong Line String");
        }

        int numberOfPoint = tokens.countTokens() / 2;
        Coordinate[] coordLLH = new Coordinate[numberOfPoint];

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
                /**
                 * evaluating longitude latitude
                 */
                latitude = Double.valueOf(tokens.nextToken());
                longitude = Double.valueOf(tokens.nextToken());
                /**
                 * building coordinate
                 */
                coordLLH[currentCoord] = new Coordinate(latitude, longitude);
                currentCoord++;

                if (Math.abs(longitude) > this.longitudeLimitToUnderstandForLineDate)
                {
                    /**
                     * across line of date mode
                     */
                    this.isOverDateLine = true;
                }
                /*
                 * if(isFirst) { isFirst=false; lastLongitude=longitude; } else
                 * { if(((longitude > 0) != (lastLongitude >0)) &&
                 * Math.abs(longitude)>this.
                 * longitudeLimitToUnderstandForLineDate) { //over date line
                 * this.isOverDateLine=true; } lastLongitude=longitude; }
                 */
            } // end try
            catch (NumberFormatException e)
            {
                // error on tokens format
                throw new GridException(e.getMessage());
            }

        } // end while

        // Have modify negative longitude
        if (this.isOverDateLine)
        {
            /**
             * case of line of date recalculating coordinate
             */
            double currLong;
            for (int i = 0; i < coordLLH.length; i++)
            {
                currLong = coordLLH[i].y;
                if (currLong < 0)
                {
                    currLong = currLong + 360;
                    coordLLH[i].y = currLong;
                }

            } // end for
        } // end if

        LineString returnedLine;

        try
        {
            /**
             * building geometry
             */
            jtsLine = this.geometryFactory.createLineString(coordLLH);

            if (this.haveDensify)
            {
                this.tracer.debug("Have densify line");

                try
                {
                    /**
                     * densifying line string
                     */
                    returnedLine = (LineString) Densifier.densify(jtsLine, this.densifierTolerance);
                }
                catch (Exception e)
                {
                    this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.APPLICATION_SUBSYSTEM_FAILURE, e.getMessage());
                    this.tracer.log("Unable to densify line using non densified line");
                    returnedLine = jtsLine;
                } // end try

            } // end if
            else
            {
                /**
                 * we have no densify line string
                 */
                returnedLine = jtsLine;
            }

            this.tracer.debug("Frontier point: " + returnedLine.getCoordinates().length);

        } // end try
        catch (IllegalArgumentException e)
        {
            throw new GridException(e.getMessage());
        }
        // return denseLine;
        return returnedLine;

    }// end buildGridFromPolyListString

    /**
     * Return a polygon from the first DTO of the AR
     *
     * @param ar
     * @return Polygon
     */
    public Polygon getPolygonFromAR(AcqReq ar)
    {
        Polygon retval = null;

        /**
         * retrieving corners
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
    protected Polygon getPolygonFromCorners(double[][] corners)
    {
        // return value
        Polygon retval = null;

        int cornerNumber = corners.length;
        // building coordinates vector
        Coordinate[] coords = new Coordinate[cornerNumber + 1];

        double longitude;

        /**
         * for each corner evaluating latitude and longitude
         */
        for (int i = 0; i < cornerNumber; i++)
        {
            longitude = corners[i][1];

            if (this.isOverDateLine && (longitude < 0))
            {
                longitude = longitude + 360;
            }
            coords[i] = new Coordinate(corners[i][0], longitude);
        } // end for
        coords[cornerNumber] = new Coordinate(coords[0].x, coords[0].y);

        /**
         * buidilding polygon
         */
        retval = this.geometryFactory.createPolygon(coords);

        return retval;
    }// end getPolygonFromAR

    @Override
    public double getAream2()
    {
        // TODO Auto-generated method stub
        return 0;
    }// end method

    @Override
    public boolean isAcrossDateLine()
    {
        return this.isOverDateLine;
    }// end method

    /**
     * Fake ovverride of interface method
     */
    /*
     * @Override public List<GridPoint> getHolesCenter(List<AcqReq> arList)
     * throws GridException {
     *
     * return new ArrayList<GridPoint>(); }
     */

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
        List<GridPoint> gridpointList = new ArrayList<>();

        /**
         * get centroid
         */
        List<Point> centroidList = getHolesCentroid(arList);

        double latitude;
        double longitude;
        GridPoint gridPoint;
        /**
         * for each point
         */
        for (Point p : centroidList)
        {
            /**
             * evaluate latitude and longitude check for line date
             */
            latitude = p.getX();
            longitude = p.getY();
            if (this.isOverDateLine && (longitude > 180))
            {
                longitude = longitude - 360;

            }

            /**
             * build grid point add pouint to list
             */
            gridPoint = new GridPoint(gridpointList.size(), latitude, longitude, this.dem.getElevation(latitude, longitude));
            gridpointList.add(gridPoint);
        }

        return gridpointList;
    }// end method

    /**
     * Return the centroid of holes in the coverage area, empty list if no holes
     * are found
     *
     * @param arList
     * @return the list of holes centers, null if no holes are found
     * @throws GridException
     */
    protected List<Point> getHolesCentroid(List<AcqReq> arList) throws GridException
    {
        /**
         * List of JTS Point to be returned
         */
        List<Point> gridpointList = new ArrayList<>();

        /**
         * lenght of linestring of target area
         */
        double targetlenght = this.line.getLength();

        try
        {
            List<Polygon> polyList = new ArrayList<>();
            Polygon p;
            for (AcqReq a : arList)
            {
                /**
                 * For each AR build polygon and add to Plolygonlist
                 */
                p = getPolygonFromAR(a);
                polyList.add((Polygon) GeometryPrecisionReducer.reduce(p, new PrecisionModel()));
            } // end ForgetHolesCenter

            // logger.debug("Unioning");
            /**
             * building AR polygon union
             */
            Geometry dtoUnion = UnaryUnionOp.union(polyList);
            // logger.debug("Intersecting");

            /**
             * intersecting union withj line
             */
            LineString reducedLine = (LineString) GeometryPrecisionReducer.reduce(this.line, new PrecisionModel());
            Geometry intersection = dtoUnion.intersection(reducedLine);

            /**
             * Evaluating holes polygon
             */
            Geometry closure = intersection.symDifference(reducedLine);

            if (!closure.isEmpty())
            {

                Point holeCenter;
                LineString currentLine;
                double currentLenght;
                if (closure instanceof MultiLineString)
                {
                    /**
                     * in case of multiline
                     */
                    int numOfLine = closure.getNumGeometries();

                    for (int i = 0; i < numOfLine; i++)
                    {
                        currentLine = (LineString) closure.getGeometryN(i);
                        currentLenght = currentLine.getLength();
                        if ((currentLenght / targetlenght) > this.notAllowedHoleAreaRatio)
                        {
                            /**
                             * evalute lenghtofcurrentline / targetlenght ratio
                             * if the ratio > limit evaluate centroid
                             */
                            holeCenter = currentLine.getCentroid();
                            gridpointList.add(holeCenter);
                        } // end if
                    } // end for
                } // end if
                else if (closure instanceof LineString)
                {
                    /**
                     * onli one hole
                     *
                     */
                    currentLine = (LineString) closure;
                    currentLenght = currentLine.getLength();
                    if ((currentLenght / targetlenght) > this.notAllowedHoleAreaRatio)
                    {
                        /**
                         * evalute lenghtofcurrentline / targetlenght ratio if
                         * the ratio > limit evaluate centroid
                         */
                        holeCenter = currentLine.getCentroid();
                        gridpointList.add(holeCenter);
                    } // end if
                } // end else if

            } // end if

        } // end try
        catch (Exception e)
        {
            throw new GridException(e.getMessage());
        }

        return gridpointList;
    }// end method

    /**
     * Return the list of grid point inside a AR
     *
     * @param list
     *            full GridPoint List
     * @param corners
     *            of AR
     * @return list of point
     */
    @Override
    public List<GridPoint> getGridpointInsdeAR(List<GridPoint> list, double[][] corners)
    {
        /**
         * retval list
         */
        List<GridPoint> ppoinList = new ArrayList<>();

        /**
         * Building ar polygon
         */
        Polygon arPol = getPolygonFromCorners(corners);

        Polygon reducedArPolygon = (Polygon) GeometryPrecisionReducer.reduce(arPol, new PrecisionModel());

        Point currentPoint;
        double latitude;
        double longitude;
        /**
         * for each point in list
         */
        for (GridPoint p : list)
        {
            /**
             * Create llh check for line of date
             */
            latitude = p.getLLH()[0];
            longitude = p.getLLH()[1];
            if (this.isOverDateLine && (longitude < 0))
            {
                longitude = longitude + 360;
            }

            /**
             * Create JTS point
             */
            currentPoint = this.geometryFactory.createPoint(new Coordinate(latitude, longitude));
            if (currentPoint.within(arPol))
            {
                /**
                 * If JTS point inside polygon add grid point to reval list
                 */
                ppoinList.add(p);
            }
        } // end for

        return ppoinList;
    }// end method

    /**
     * In case of single acqiuisition return the poslist of the polygon to be
     * used in AR poslist In this case return an empty string
     *
     * @return ""
     */
    @Override
    public String retunPolyListForSingleAcq()
    {
        // do nothing
        String retval = "";
        return retval;
    }// end retunPolyListForSingleAcq
    private List<AcqReq> overlapAscending(List<AcqReq> arList, double totalCoverage, boolean ascending) {

		List<AcqReq> returnedArList = new ArrayList<AcqReq>();

		// compute the coverage without the j-esim acq
		double localCoverage = 0;

		try {

			for (AcqReq acq : arList) {
				returnedArList.add(acq.clone());
			}
			
			if(ascending)
			{
				// from the last element ordered by time ascending back to the first element
				for (int j = 0; j <returnedArList.size() ; j++) {

					// create a list with all the acqReq
					List<AcqReq> localAcqReq = cloneList(returnedArList);
					if (localAcqReq.size() > 1) {
						// remove the j-esim acqReq
						localAcqReq.remove(returnedArList.get(j));
						for (int i = 0; i < returnedArList.size(); i++)

						{
//							logger.debug("arList_: " + returnedArList.get(i));

						}
//						logger.debug("arList size : " + returnedArList.size());
//
//						logger.debug("localAcqReq size : " + localAcqReq);

						localCoverage = getCoverage(localAcqReq);

//						logger.debug("localCoverage : " + localCoverage);

						// if the j-esim element doesn't increase the total coverage
						if (localCoverage == totalCoverage) {
//							logger.debug("remove acqReq : " + returnedArList.get(j));

							// remove it
							returnedArList.remove(returnedArList.get(j));
							j--;
//							logger.debug("residual returnedArList: " + returnedArList);
						}
					}

				} // end for return null;
			}
			else
			{
				// from the last element ordered by time ascending back to the first element
				for (int j = returnedArList.size() - 1; j >= 0; j--) {

					// create a list with all the acqReq
					List<AcqReq> localAcqReq = cloneList(returnedArList);
					if (localAcqReq.size() > 1) {
						// remove the j-esim acqReq
						localAcqReq.remove(returnedArList.get(j));
//						for (int i = 0; i < returnedArList.size(); i++)
//
//						{
//							logger.debug("arList_: " + returnedArList.get(i));
//
//						}
//						logger.debug("arList size : " + returnedArList.size());
//
//						logger.debug("localAcqReq size : " + localAcqReq);

						localCoverage = getCoverage(localAcqReq);

//						logger.debug("localCoverage : " + localCoverage);

						// if the j-esim element doesn't increase the total coverage
						if (localCoverage == totalCoverage) {
//							logger.debug("remove acqReq : " + returnedArList.get(j));

							// remove it
							returnedArList.remove(returnedArList.get(j));
							j--;
//							logger.debug("residual returnedArList: " + returnedArList);
						}
					}

				} // end for return null;
			}

		} catch (GridException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		logger.debug("residual arList: " + arList);

		return returnedArList;

	}
	
	@Override
	public List<AcqReq> processOverlap(List<AcqReq> arList) throws GridException {
		double overlap = getOverlapFactor(arList);
//		logger.debug("overlap BEFORE changes: " + overlap);

		if (overlap > 0) {
			double totalCoverage = getCoverage(arList);
			logger.debug("totalCoverage: " + totalCoverage);

			try {

				List<AcqReq> ascendingOrder = overlapAscending(arList, totalCoverage, true);
				List<AcqReq> descendingOrder = overlapAscending(arList, totalCoverage, false);
				if(descendingOrder.size()<ascendingOrder.size())
				{
					arList = descendingOrder;
				}
				else
				{
					arList = ascendingOrder;
				}
				overlap = getOverlapFactor(arList);
//				logger.debug("overlap AFTER changes: " + overlap);
			} // end try
			catch (Exception e) {
				/**
				 * Error just throw
				 */
				throw new GridException(e.getMessage());
			}
		}
		return arList;
	}
	public static List<AcqReq> cloneList(List<AcqReq> list) {
	    List<AcqReq> clone = new ArrayList<AcqReq>(list.size());
	    for (AcqReq item : list) clone.add(item.clone());
	    return clone;
	}
}// end class
