/**
*
* MODULE FILE NAME:	PolygonGridder.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Implementation of a Gridder interface in case of PolygonGridder
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
* 									 added method for centroid
*
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.feasibility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.math3.analysis.function.Minus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.referencing.GeodeticCalculator;

import com.telespazio.csg.srpf.dem.DEMManager;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.PropertiesReader;
import com.vividsolutions.jts.densify.Densifier;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.operation.union.UnaryUnionOp;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;

/**
 * Build a Grid for an area enclosed by a polygon
 *
 * @author Amedeo Bancone
 * @version 2.0
 *
 */
public class PolygonGridder implements Gridder

{
	static final Logger logger = LogManager.getLogger(PolygonGridder.class.getName());

	// static final Logger logger =
	// LogManager.getLogger(PolygonGridder.class.getName());
	/**
	 * Logger
	 */
	protected TraceManager tracer = new TraceManager();

	/**
	 * List of position as string
	 */
	protected String polyList;

	/**
	 * true if we have densify point on polygon
	 */
	protected boolean haveDensify = false;

	/**
	 * Tolerance in densify assigned default value
	 */
	protected double densifierTolerance = 0.1;

	/**
	 * grid point list
	 */
	protected List<GridPoint> gridPointList;

	/**
	 * Data Elevation Model
	 */
	protected DEMManager dem;

	/**
	 * Geometry Fracory used to build geometries
	 */
	protected GeometryFactory geometryFactory = new GeometryFactory();

	/**
	 * Grid spacing
	 */
	protected double gridSpacing;

	/**
	 * polygon
	 */
	protected Polygon polygon = null;

	/**
	 * target center to be added to grid if not null
	 */
	protected double[] targetCenter = null;

	/**
	 * true if across the line of date
	 */
	protected boolean isOverDateLine = false;

	/**
	 * this is a value to consider tarh a change of sign in longitude means an
	 * overcome of the line of date Now assigned the default
	 */
	protected double longitudeLimitToUnderstandForLineDate = FeasibilityConstants.LongitudeLimitToUnderstandForLineDate;

	/**
	 * Value of the ratio hole/target area to consider the hole now defaut
	 */
	protected double notAllowedHoleAreaRatio = 0.0001;

	/**
	 * Used during extension to perform overlap and coverage
	 */
	protected Geometry multiGeometry = null;

	/**
	 * Constructor
	 *
	 * @param polyList
	 * @param dem
	 * @param gridSpacing
	 * @throws GridException
	 */
	public PolygonGridder(final String polyList, final DEMManager dem, double gridSpacing) throws GridException

	{
		/**
		 * Setting base attributes
		 */
		this.polyList = polyList;
		this.dem = dem;
		this.gridSpacing = gridSpacing;

		/**
		 * Initializing data
		 */
		initConf();

		if ((polyList != null) && (!polyList.equals(""))) {
			this.polygon = getPolygonEnclosingArea(polyList);
		}
	}

	/**
	 * initialize configuration
	 */
	protected void initConf() {
		/**
		 * Reading property
		 */
		String value = PropertiesReader.getInstance()
				.getProperty(FeasibilityConstants.LONGITUDE_LIMIT_TO_UNDERSTAND_FOR_LINE_DATE_CONF_KEY);
		if (value != null) {
			try {
				double dValue = Double.valueOf(value);
				this.longitudeLimitToUnderstandForLineDate = dValue;
			} catch (Exception e) {
				/**
				 * misconfigured parameter using default
				 */
				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Unable to found " + FeasibilityConstants.LONGITUDE_LIMIT_TO_UNDERSTAND_FOR_LINE_DATE_CONF_KEY
								+ " in configuration");
			}

		} else {
			/**
			 * missing parameter using default
			 */
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.LONGITUDE_LIMIT_TO_UNDERSTAND_FOR_LINE_DATE_CONF_KEY
							+ " in configuration");
		}

		/**
		 * Reading property
		 */
		value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.PERIMETER_DENSIFIER_TOLERANCE_CONF_KEY);
		if (value != null) {
			try {
				double dValue = Double.valueOf(value);
				this.densifierTolerance = dValue;
			} catch (Exception e) {
				/**
				 * misconfigured parameter using default
				 */
				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Unable to found " + FeasibilityConstants.PERIMETER_DENSIFIER_TOLERANCE_CONF_KEY
								+ " in configuration");
			}

		} else {
			/**
			 * missing parameter using default
			 */
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.PERIMETER_DENSIFIER_TOLERANCE_CONF_KEY
							+ " in configuration");
		}

		/**
		 * Reading property
		 */
		value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.HAVE_DENSIFY_AREA_PERIMETER_CONF_KEY);
		if (value != null) {
			try {
				int iValue = Integer.valueOf(value);
				if (iValue == 1) {
					this.haveDensify = true;
				}
			} catch (Exception e) {
				/**
				 * misconfigured parameter using default
				 */
				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Unable to found " + FeasibilityConstants.HAVE_DENSIFY_AREA_PERIMETER_CONF_KEY
								+ " in configuration");
			}

		} else {
			/**
			 * missing parameter using default
			 */
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.HAVE_DENSIFY_AREA_PERIMETER_CONF_KEY
							+ " in configuration");
		}

		// HOLES PARAMETERS
		/**
		 * Reading property
		 */
		value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.NOT_ALLOWED_HOLE_AREA_RATIO_CONF_KEY);
		if (value != null) {
			try {
				double dValue = Double.valueOf(value);
				this.notAllowedHoleAreaRatio = dValue;
			} catch (Exception e) {
				/**
				 * misconfigured parameter using default
				 */
				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Unable to found " + FeasibilityConstants.NOT_ALLOWED_HOLE_AREA_RATIO_CONF_KEY
								+ " in configuration");
			}

		} else {
			/**
			 * missing parameter using default
			 */
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.NOT_ALLOWED_HOLE_AREA_RATIO_CONF_KEY
							+ " in configuration");
		}

	}// end method

	/**
	 * Fill the grid point list
	 *
	 * @param gridPointList
	 */
	@Override
	public void fillGrid(List<GridPoint> gridPointList) throws GridException

	{
		// logger.debug("Building grid");
		this.gridPointList = gridPointList;
		/**
		 * Building grid polygon
		 */
		buildGridFromPolyListString();
		if (this.targetCenter != null) {
			/**
			 * If target center has been set add it to grid point list
			 */
			// int pointId = this.gridPointList.size()+1;
			int pointId = this.gridPointList.size();
			GridPoint center = new GridPoint(pointId, this.targetCenter);
			this.gridPointList.add(center);
		} // end if

	}// end method

	/**
	 * Check if the minimmum AoI is inside the poligon and if true insert the point
	 * in the gridpointlist
	 *
	 * @param poslist
	 * @param gridPointList
	 * @return true if the min area of interest is inside target area
	 * @throws GridException
	 */
	public boolean insertCheckMinimumAoI(String minimumAoiPoslist, List<GridPoint> gridPointList) throws GridException {
		this.tracer.log("Checking for the minumum Area of interest");
		/**
		 * retval true if min Aoi is inside area of interest
		 */
		boolean retval = false;

		/**
		 * Build polygon enclosing the min area of interest
		 */
		Polygon minimumAoiPolygon = this.getPolygonEnclosingArea(minimumAoiPoslist);

		/**
		 * Check if this polygon is inside the polygon
		 */
		retval = (minimumAoiPolygon.within(this.polygon) ||  minimumAoiPolygon.equals(this.polygon));
		logger.debug("the minimumAoiPolygon in inside the poligon? "+retval);
		/**
		 * Min Area inside so we have to insert point belonging the enclosing polygon in
		 * the grid list point
		 */
		if (retval) {
			/**
			 * JTS Coordinates
			 */
			Coordinate[] coords = minimumAoiPolygon.getCoordinates();

			/**
			 * Current point to be inserted
			 */
			GridPoint p;
			double latitude;
			double longitude;
			double height;
			/**
			 * First and last point in the polygon are the same
			 */
			int numberOfPointToBeInserted = coords.length - 1;

			for (int i = 0; i < numberOfPointToBeInserted; i++) {

				/**
				 * For each JTS point coordinate point evaluate lat and lon
				 */

				latitude = coords[i].x;
				longitude = coords[i].y;

				/**
				 * Check if across line date
				 */
				if (this.isOverDateLine && (longitude > 180)) {
					longitude = longitude - 360;

				} // end if

				/**
				 * eavluation elevation
				 */
				height = this.dem.getElevation(latitude, longitude);

				/**
				 * create the relate grid point
				 */
				p = new GridPoint(gridPointList.size(), latitude, longitude, height);
				/**
				 * set the flag stating that belongs the minm aoi
				 */
				p.setBelongsToMinimumAoI(true);

				/**
				 * add to grid list
				 */
				gridPointList.add(p);
			} // end for
		} // end if

		return retval;
	} // end method

	/**
	 * Build the multigeometry used in case of extension
	 *
	 * @param arlist list of AR building the multi geometry
	 */
	@Override
	public void buildMultiGeometryForExtension(List<AcqReq> arlist) {
		/**
		 * List of polygon building the multy geometry
		 */
		List<Geometry> polyList = new ArrayList<>();
		Geometry p;
		for (AcqReq a : arlist) {
			/**
			 * For Each AR Build polygon Add to polygon list
			 */
			p = getPolygonFromCorners(a.getDTOList().get(0).getCorners());
			polyList.add(GeometryPrecisionReducer.reduce(p, new PrecisionModel()));

		} // end For

		// logger.debug("Unioning");
		/**
		 * Multi geometry as Union of single polygon
		 */
		this.multiGeometry = UnaryUnionOp.union(polyList);
	}// end method

	/**
	 * Set the center area target. It shall be passed as a string of type : "lat
	 * lon"
	 *
	 * @param center
	 * @throws GridException
	 */
	public void setTargeTCenter(String center) throws GridException {
		/**
		 * Yokenize string
		 */
		StringTokenizer tokens = new StringTokenizer(center);

		if (tokens.countTokens() != 2) // No elements or spare elements
		{
			/**
			 * Just throw an exception
			 */
			throw new GridException("Malformed TargetCenteredpoint");
		}

		try {
			/**
			 * Building LLH coordinate
			 */
			double lat = Double.valueOf(tokens.nextToken());
			double longitude = Double.valueOf(tokens.nextToken());
			this.targetCenter = new double[3];
			this.targetCenter[0] = lat;
			this.targetCenter[1] = longitude;
			this.targetCenter[2] = 0;
		} catch (NumberFormatException e) {
			/**
			 * Malformed number just throw exception
			 */
			throw new GridException(e.getMessage());
		}

	}// end setTargeTCenter

	/**
	 * Return the centroid of the envelope of the polygon as GridPoint type
	 *
	 * @return centroid or null in case of error
	 */
	public GridPoint getCentroid() {

		/**
		 * Returned grid point
		 */
		GridPoint centroid = null;
		try {
			if (this.polygon != null) {
				// Point p = this.polygon.getCentroid();
				/**
				 * Evaluate Centrid as JTS Point
				 */
				Point p = this.polygon.getEnvelope().getCentroid();

				/**
				 * Extracting lat and lon
				 */
				double latitude = p.getX();
				double longitude = p.getY();

				/**
				 * Check for line date
				 */
				if ((longitude > 180) && this.isOverDateLine) {
					longitude = longitude - 360;
				}

				/**
				 * Evaluating elevation
				 */
				double height = this.dem.getElevation(latitude, longitude);

				/**
				 * Build grid point and set relevant centroid flags
				 */
				centroid = new GridPoint(-1, latitude, longitude, height);
				centroid.setCentroid(true);
				centroid.setInternal(p.within(this.polygon));

			} // end if

		} // end try
		catch (Exception e) {
			/**
			 * Error JUST log
			 */
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					e.getMessage());

		} // end catch

		return centroid;
	}// end method

	/**
	 * Build the grid form the position list of the polygon retrieved by the PRList
	 *
	 * @param polyList
	 * @throws IOException
	 */
	protected void buildGridFromPolyListString() throws GridException

	{
		/**
		 * Polyon enclosing traget area
		 */
		this.polygon = getPolygonEnclosingArea(this.polyList);

		// logger.info("Poligon corners: " +
		// this.polygon.getCoordinates().length);

		// Adding point in polygon to Grid

		/**
		 * JTS Coordinate
		 */
		Coordinate[] coords = this.polygon.getCoordinates();
		/**
		 * Currend cgrid point
		 */
		GridPoint p;
		double latitude;
		double longitude;
		double height;

		/**
		 * Number of point First and last point in the polygon are the same
		 */
		int numberOfPointToBeInserted = coords.length - 1;

		/**
		 * Inesrting in the grid list points belonging the polygon For each point
		 * belonging the polygon
		 */
		for (int i = 0; i < numberOfPointToBeInserted; i++) {
			/**
			 * Evaluate lat lon
			 */
			latitude = coords[i].x;
			longitude = coords[i].y;

			/**
			 * Check for line date
			 */
			if (this.isOverDateLine && (longitude > 180)) {
				longitude = longitude - 360;

			}

			/**
			 * Evaluate elevation
			 */
			height = this.dem.getElevation(latitude, longitude);

			/**
			 * Create grid point
			 */
			p = new GridPoint(i, latitude, longitude, height);

			/**
			 * add to list
			 */
			this.gridPointList.add(p);
		}

		/**
		 * Filling the grid list with point inside the polygon
		 */
		buildGridInsidePolygon(this.polygon);

		// logger.debug("Grid built");
		this.tracer.log("Grid built");

	}// end buildGridFromPolyListString

	public static List<AcqReq> cloneList(List<AcqReq> list) {
		List<AcqReq> clone = new ArrayList<AcqReq>(list.size());
		for (AcqReq item : list)
			clone.add(item.clone());
		return clone;
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

						localCoverage = getCoverageDtoInsideAR(localAcqReq);

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

						localCoverage = getCoverageDtoInsideAR(localAcqReq);

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

	/**
	 * Evaluate the overlap factor for an AR List
	 *
	 * @param arList
	 * @return overlap factor
	 */
	@Override
	public double getOverlapFactor(List<AcqReq> arList) throws GridException {
		/**
		 * Value to be returned
		 */
		double overlapFactor = 0;

		if (arList != null && !arList.isEmpty()) {
			try {
				/**
				 * List of polygon overlapping target area
				 */
				List<Polygon> polyognsList = new ArrayList<>();
				Polygon p;
				/**
				 * Polygon enclosing target areaarea
				 */
				Geometry reducedPolygon;

				if (this.multiGeometry != null) {
					/**
					 * Case extension Reducing precision geometry to avoid problem in intersection
					 * operation
					 *
					 */
					reducedPolygon = GeometryPrecisionReducer.reduce(this.multiGeometry, new PrecisionModel());
				} else {
					/**
					 * Feasibility Reducing precision geometry to avoid problem in intersection
					 * operation
					 */
					reducedPolygon = GeometryPrecisionReducer.reduce(this.polygon, new PrecisionModel());
				}

				/**
				 * Area of DTOs intersection with Target area
				 */
				double sumOfDtoIntersectionArea = 0;

				for (AcqReq a : arList) {
					/**
					 * For each AR Build polygon enclosing AREA Add polygon to a list
					 */
					p = getPolygonFromAR(a);
					/**
					 * Reducing precision geometry to avoid problem in intersection operation
					 */
					polyognsList.add((Polygon) GeometryPrecisionReducer.reduce(p, new PrecisionModel()));

					/**
					 * Evaluate area of intersection with target area and add to sum
					 */
					sumOfDtoIntersectionArea = sumOfDtoIntersectionArea + p.intersection(reducedPolygon).getArea();

				} // end for

				/**
				 * Union of AR polygon
				 */
				Geometry dtoUnion = UnaryUnionOp.union(polyognsList);

				/**
				 * Evaluating the area of intersection of union with traget Area
				 */
				Geometry intersection = dtoUnion.intersection(reducedPolygon);
				double intecectedArea = intersection.getArea();
				/**
				 * Evaluating overlap
				 */
				overlapFactor = (1 - (intecectedArea / sumOfDtoIntersectionArea)) * 100;

			} // end try
			catch (Exception e) {
				/**
				 * Error just throw
				 */
				throw new GridException(e.getMessage());
			}
		}
		return overlapFactor;
	}// end method

	/**
	 * return the coverage of a given DTO
	 *
	 * @param dto
	 * @return the cooverage
	 * @throws GridException
	 */
	public double getDTOCoverage(DTO dto) throws GridException {
		/**
		 * Area value
		 */
		double retval = 0;
		try {
			double area;
			/**
			 * Corners of DTO
			 */
			double[][] corners = dto.getCorners();
			/**
			 * Buildin DTO Polygon reducing to avoid intersection problems
			 */
			Geometry dtoPolygon = getPolygonFromCorners(corners);
			Geometry reducedDTOPolygon = GeometryPrecisionReducer.reduce(dtoPolygon, new PrecisionModel());

			Geometry reducedPolygon;
			if (this.multiGeometry != null) {
				/**
				 * Extension reducing to avoid intersection problems Area of traget
				 */
				reducedPolygon = GeometryPrecisionReducer.reduce(this.multiGeometry, new PrecisionModel());
				area = this.multiGeometry.getArea();
			} else {
				/**
				 * Feasibilty reducing to avoid intersection problems area of target
				 */
				reducedPolygon = GeometryPrecisionReducer.reduce(this.polygon, new PrecisionModel());
				area = this.polygon.getArea();
			}

			Geometry intersection;
			/**
			 * Evaluating coverage as: intesected area/target area
			 */
			intersection = reducedDTOPolygon.intersection(reducedPolygon);
			double intecectedArea = intersection.getArea();
			retval = intecectedArea / area;

		} // end try
		catch (Exception e) {
			/**
			 * Just Throw
			 */
			throw new GridException(e.getMessage());
		} // end catch

		return retval;
	}// end getDTOCoverage

	/**
	 * return the coveragle give a list of AR
	 *
	 * @param arList
	 * @return coverage
	 *
	 */
	@Override
	public double getCoverage(List<AcqReq> arList) throws GridException {
		// this.tracer.debug("Evaluating coverage");
		/**
		 * value to be returned
		 */
		double retval = 0;
		try {
			double area;
			List<Geometry> polygonsList = new ArrayList<>();
			Geometry p;
			for (AcqReq a : arList) {
				/**
				 * For each AR evaluate polygon and add to list of polygon reducing to avoid
				 * intersection problems
				 */
				p = getPolygonFromAR(a);
				polygonsList.add(GeometryPrecisionReducer.reduce(p, new PrecisionModel()));
			} // end For

			
			if(!polygonsList.isEmpty())
			{
				// logger.debug("Unioning");
				/**
				 * Buiding union of AR Polygon
				 */
				Geometry dtoUnion = UnaryUnionOp.union(polygonsList);
				// logger.debug("Intersecting");

				Geometry reducedPolygon;
				if (this.multiGeometry != null) {
					/**
					 * Area of target Case extension reducing to avoid intersection problems
					 */
					reducedPolygon = GeometryPrecisionReducer.reduce(this.multiGeometry, new PrecisionModel());
					area = this.multiGeometry.getArea();
				} else {
					/**
					 * Area of target Case Feasibility reducing to avoid intersection problems
					 */
					reducedPolygon = GeometryPrecisionReducer.reduce(this.polygon, new PrecisionModel());
					area = this.polygon.getArea();
				}

				Geometry intersection;

				/**
				 * Intersect AR Polygon Union with target Area
				 */
				intersection = dtoUnion.intersection(reducedPolygon);
				/**
				 * Evaluating area of intersection
				 */
				double intecectedArea = intersection.getArea();

				/**
				 * Evaluating coverage
				 */

				retval = intecectedArea / area;
			}


		} // end try
		catch (Exception e) {
			// System.err.println(e.getMessage());
			/**
			 * Error just throw
			 */
			throw new GridException(e.getMessage());
		} // end catch

		return retval;
	}// end method

	
	
	/*
	 * 
	 * MODIFICA 03092020
	 * 
	 * 
	 */
	
	
	public double getCoverageDtoInsideAR(List<AcqReq> arList) throws GridException {
		// this.tracer.debug("Evaluating coverage");
		/**
		 * value to be returned
		 */
		double retval = 0;
		try {
			double area;
			List<Geometry> polygonsList = new ArrayList<>();
			Geometry p;
			for (AcqReq a : arList) {
				for(int i=0;i<a.getDTOList().size();i++)
				{
					/**
					 * For each AR evaluate polygon and add to list of polygon reducing to avoid
					 * intersection problems
					 */
					p = getPolygonFromDtoInsideAR(a.getDTOList().get(i));
					
					polygonsList.add(GeometryPrecisionReducer.reduce(p, new PrecisionModel()));
				}

			} // end For

			// logger.debug("Unioning");
			/**
			 * Buiding union of AR Polygon
			 */
			Geometry dtoUnion = UnaryUnionOp.union(polygonsList);
			// logger.debug("Intersecting");

			Geometry reducedPolygon;
			if (this.multiGeometry != null) {
				/**
				 * Area of target Case extension reducing to avoid intersection problems
				 */
				reducedPolygon = GeometryPrecisionReducer.reduce(this.multiGeometry, new PrecisionModel());
				area = this.multiGeometry.getArea();
			} else {
				/**
				 * Area of target Case Feasibility reducing to avoid intersection problems
				 */
				reducedPolygon = GeometryPrecisionReducer.reduce(this.polygon, new PrecisionModel());
				area = this.polygon.getArea();
			}

			Geometry intersection;

			/**
			 * Intersect AR Polygon Union with target Area
			 */
			intersection = dtoUnion.intersection(reducedPolygon);
			/**
			 * Evaluating area of intersection
			 */
			double intecectedArea = intersection.getArea();

			/**
			 * Evaluating coverage
			 */

			retval = intecectedArea / area;

		} // end try
		catch (Exception e) {
			// System.err.println(e.getMessage());
			/**
			 * Error just throw
			 */
			throw new GridException(e.getMessage());
		} // end catch

		return retval;
	}// end method


	/**
	 * Return a polygon from the first DTO of the AR
	 *
	 * @param ar
	 * @return polygon
	 */
	protected Polygon getPolygonFromDtoInsideAR(DTO dto) {
		/**
		 * return value
		 */
		Polygon retval = null;
		
		//MODIFICA 31/08 iterare sulla lista di DTO dell'ar e creare un poligono con l'unione di tutti i corner delle dto dell'ar.
		double[][] corners = dto.getCorners();
		/**
		 * buiding polygon
		 */
		retval = getPolygonFromCorners(corners);
		return retval;
	}// end getPolygonFromAR

	
	
	/**
	 * Return the area in m2 of the enclosing target area used to check against
	 * Maxarea allowed
	 *
	 * @return area as m2
	 */
	@Override
	public double getAream2() {
		/**
		 * Envelop of the target area Polygon
		 */
		Polygon envelope = (Polygon) this.polygon.getEnvelope();

		/**
		 * Coordinate of envelope
		 */
		Coordinate[] coord = envelope.getCoordinates();

		/**
		 * Area of envelope as for JTS (being the polygon n lat and long the area is
		 * deg^2)
		 */
		double envelopeAreaDG2 = envelope.getArea();
		/**
		 * Area of traget area as for JTS
		 */
		double polyAreaDG2 = this.polygon.getArea();
		/**
		 * Evaluating area of envelope in m^2 using a geodetic calculator
		 */
		GeodeticCalculator calc = new GeodeticCalculator();
		calc.setStartingGeographicPoint(coord[0].y, coord[0].x);
		calc.setDestinationGeographicPoint(coord[1].y, coord[1].x);

		/**
		 * orthodromic lenght of the first side of envelope rectangle
		 */
		double len1 = calc.getOrthodromicDistance();
		calc.setStartingGeographicPoint(coord[1].y, coord[1].x);
		calc.setDestinationGeographicPoint(coord[2].y, coord[2].x);
		/**
		 * orthodromic lenght of the second side of envelope rectangle
		 */
		double len2 = calc.getOrthodromicDistance();
		/**
		 * envelop area
		 */
		double enevlopeAream2 = len1 * len2;

		/**
		 * Evaluating target area as proportion
		 */
		double polyAreaM2 = (enevlopeAream2 * polyAreaDG2) / envelopeAreaDG2;

		// logger.warn("Area in km2 target : " + polyAreaM2/1000000.0);
		return polyAreaM2;
	}// end method

	/**
	 * Return a polygon from the first DTO of the AR
	 *
	 * @param ar
	 * @return polygon
	 */
	protected Polygon getPolygonFromAR(AcqReq ar) {
		/**
		 * return value
		 */
		Polygon retval = null;
		
		//MODIFICA 31/08 iterare sulla lista di DTO dell'ar e creare un poligono con l'unione di tutti i corner delle dto dell'ar.
		double[][] corners = ar.getDTOList().get(0).getCorners();
		/**
		 * buiding polygon
		 */
		retval = getPolygonFromCorners(corners);
		return retval;
	}// end getPolygonFromAR

	/**
	 * Return a polygon from corners of a DTO
	 *
	 * @param ar
	 * @return Polygon
	 */
	protected Polygon getPolygonFromCorners(double[][] corners) {
		/**
		 * Retval
		 */
		Polygon retval = null;

		int cornerNumber = corners.length;
		/**
		 * JTS Cooordinate first and last point in polygon must coincide
		 */
		Coordinate[] coords = new Coordinate[cornerNumber + 1];

		double longitude;

		for (int i = 0; i < cornerNumber; i++) {
			/**
			 * for each corner check for line date
			 */
			longitude = corners[i][1];
			if (this.isOverDateLine && (longitude < 0)) {
				longitude = longitude + 360;
			}
			/**
			 * Create JTS coordinate
			 */
			coords[i] = new Coordinate(corners[i][0], longitude);
		} // end for
		/**
		 * First and last shall coincide
		 */
		coords[cornerNumber] = new Coordinate(coords[0].x, coords[0].y);

		/**
		 * build polygon
		 */
		retval = this.geometryFactory.createPolygon(coords);

		return retval;
	}// end getPolygonFromAR

	/**
	 * Generate a gripoint inside the polygon
	 *
	 * @param poly
	 */
	private void buildGridInsidePolygon(Polygon poly)

	{
		// logger.debug("buildGridInsidePolygon");
		/**
		 * Envelop of target area
		 */
		Polygon envelope = (Polygon) poly.getEnvelope();

		/**
		 * Envelop coordinates
		 */
		Coordinate[] coord = envelope.getCoordinates();

		/**
		 * Max e min lat lon of envelope in building grid we cicle along the envelope
		 */
		double minLatitude = coord[0].x;
		double minLongitude = coord[0].y;

		double maxLatitude = coord[2].x;
		double maxLongitude = coord[2].y;

		// logger.info("Minlong: " + minLongitude + " max long: " +maxLongitude
		// );

		/*
		 * System.err.println("min lat: " + minLatitude + " min long: " + minLongitude);
		 * System.err.println("max lat: " + maxLatitude + " max long: " + maxLongitude);
		 * for(int i =0; i< coord.length;i++) System.err.println("lat: " + coord[i].x +
		 * "  long: " + coord[i].y);
		 */

		/**
		 * Current values of point
		 */
		double currentLatitude = minLatitude;
		Coordinate currentCoord;
		Point currentPoint;

		// logger.error("Gridspacing: " + gridSpacing);

		double gridLatitudeSpacing = evaluareGridSpacing();

		for (int i = 1; currentLatitude < maxLatitude; i++) // loop on latitude

		{
			/**
			 * Iterating on latitude
			 */
			double currentLongitude = minLongitude;
			currentLatitude = minLatitude + (i * this.gridSpacing);

			for (int j = 1; currentLongitude < maxLongitude; j++)

			{
				/**
				 * Iterating on longitude
				 */
				currentLongitude = minLongitude + (j * gridLatitudeSpacing);
				currentCoord = new Coordinate(currentLatitude, currentLongitude);
				/**
				 * JTS point
				 */
				currentPoint = this.geometryFactory.createPoint(currentCoord);

				// System.err.println();
				// System.err.print(currentLatitude + " " + currentLongitude);

				/**
				 * Evaluated point is inside target
				 */
				if (currentPoint.within(poly)) {
					// TODO controllare se il punto è duplicato?
					// System.err.print(" inside" );
					/**
					 * check for line date
					 */
					if (this.isOverDateLine && (currentLongitude > 180)) {
						currentLongitude = currentLongitude - 360;
					}
					/**
					 * add point to list
					 */
					this.gridPointList.add(new GridPoint(this.gridPointList.size(), currentLatitude, currentLongitude,
							this.dem.getElevation(currentLatitude, currentLongitude)));

				} // end if
			} // end for

		} // end for

	}// end gridInsidepolygon

	/**
	 * Evaluate longitude grid spacing Given an angle the distance along longitude
	 * of two poing with longitude difference equals that angle is max at equator
	 * and min at poles So we have to adapt the spacing grid along longitude taking
	 * in to account the latitude
	 *
	 * @return longitude grid spacing
	 */
	private double evaluareGridSpacing() {
		/**
		 * initially longitude spacing equals latitude spacing
		 */
		double longitudeGrid = this.gridSpacing;
		// Point p = this.polygon.getCentroid();
		/**
		 * Latitude of centroid We evaluate the longitude grid spacing using as
		 * reference the latitude of centoid target area
		 */
		double latitude = this.polygon.getCentroid().getX();
		GeodeticCalculator calc = new GeodeticCalculator();
		// double dist ;
		double equatorialDistance;

		/**
		 * Evaluating distace of an arc on this.gridpsacing to equatorial zone remember
		 * that the geodetic Calculator works using first the longitude!!!!
		 */
		calc.setStartingGeographicPoint(0, 0);
		calc.setDestinationGeographicPoint(this.gridSpacing, 0);
		equatorialDistance = calc.getOrthodromicDistance();

		/**
		 * evaluating longitude grid
		 */
		calc.setStartingGeographicPoint(0, latitude);
		calc.setDirection(90, equatorialDistance);
		// remember that the geodetic Calculator works using first the
		// longitude!!!!
		/**
		 * Geodetic inversion problem
		 *
		 */
		longitudeGrid = calc.getDestinationGeographicPoint().getX();
		return longitudeGrid;
	}// end method

	/**
	 * Generate a polygon enclosing the area of interest
	 *
	 * @param polyList
	 * @return Polygon
	 * @throws IOException
	 */
	private Polygon getPolygonEnclosingArea(String polyList) throws GridException {
		// logger.debug("getPolygonEnclosingArea");
		/**
		 * Tokenizing pos list
		 */
		StringTokenizer tokens = new StringTokenizer(polyList);

		if ((tokens.countTokens() == 0) || (((tokens.countTokens()) % 2) != 0)) // No
																				// elements
																				// or
																				// spare
																				// elements
		{
			/**
			 * Error on poslist as for request JUST throw
			 */
			throw new GridException("Wrong Polygon");
		} // end if

		/**
		 * Numbero fo point
		 */
		int numberOfPoint = tokens.countTokens() / 2;

		// logger.info("Number of point " + numberOfPoint);
		/**
		 * JTS coordinates
		 */
		Coordinate[] coordLLH = new Coordinate[numberOfPoint];

		int currentCoord = 0;
		double latitude;
		double longitude;
		// double lastLongitude=0;

		boolean isFirst = true;

		/**
		 * Cycling on tokens
		 *
		 */
		while (tokens.hasMoreElements()) {
			try {
				latitude = Double.valueOf(tokens.nextToken());
				longitude = Double.valueOf(tokens.nextToken());
				coordLLH[currentCoord] = new Coordinate(latitude, longitude);
				currentCoord++;
				/**
				 * Check for line date
				 */
				if (Math.abs(longitude) > this.longitudeLimitToUnderstandForLineDate) {
					this.isOverDateLine = true;
				}
				/*
				 * if(isFirst) { isFirst=false; lastLongitude=longitude; } else { if(((longitude
				 * > 0) != (lastLongitude >0)) && Math.abs(longitude)>this.
				 * longitudeLimitToUnderstandForLineDate) { //over date line
				 * this.isOverDateLine=true; } lastLongitude=longitude; }
				 */
			} // end try
			catch (NumberFormatException e) {
				/**
				 * Wrong tokens Just throw
				 */
				throw new GridException(e.getMessage());
			}

		} // end while

		/**
		 * Have modify negative longitude if we are across line of date
		 */
		if (this.isOverDateLine) {
			double currLong;
			for (int i = 0; i < coordLLH.length; i++) {
				currLong = coordLLH[i].y;
				if (currLong < 0) {
					currLong = currLong + 360;
					coordLLH[i].y = currLong;
				} // end if

			} // end for
		} // end if

		Polygon llhPoly;

		Polygon returnedPoligon;
		// Polygon desifiedpolygon;

		// Polygon bufferedPolygon;

		try {
			/**
			 * Polygon as llh coordinate
			 */
			llhPoly = this.geometryFactory.createPolygon(coordLLH);
			if (this.haveDensify) {

				this.tracer.debug("Have densify polygon");
				try {
					/**
					 * Densifying polygon (Adding extra point)
					 */
					returnedPoligon = (Polygon) Densifier.densify(llhPoly, this.densifierTolerance);
				} catch (Exception e) {
					/**
					 * Error jus log
					 */
					this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.APPLICATION_SUBSYSTEM_FAILURE,
							e.getMessage());
					this.tracer.log("Unable to densify polygon using non densified polygon");
					returnedPoligon = llhPoly;
				}

			} // end if
			else {
				/**
				 * we have no densify
				 */
				returnedPoligon = llhPoly;
			}

			this.tracer.debug("Frontier point: " + returnedPoligon.getCoordinates().length);

		} // end try
		catch (IllegalArgumentException e) {
			/**
			 * Error just log
			 */
			throw new GridException(e.getMessage());
		} // end catch

		return returnedPoligon;
		// return desifiedpolygon;
		// return bufferedPolygon;
	} // end getPolygonEnclosingArea

	/**
	 * @return true is across line of date
	 */
	@Override
	public boolean isAcrossDateLine()

	{
		return this.isOverDateLine;
	}// end method

	/**
	 * Return the centroid of holes in the coverage area, empty list if no holes are
	 * found
	 *
	 * @param arList
	 * @return the list of holes centers, null if no holes are found
	 * @throws GridException
	 */
	@Override
	public List<GridPoint> getHolesCenter(List<AcqReq> arList) throws GridException {
		/**
		 * Retval
		 */
		List<GridPoint> gridpointList = new ArrayList<>();

		/**
		 * List of centroid as JTS point
		 */
		List<Point> centroidList = getHolesCentroid(arList);

		double latitude;
		double longitude;
		GridPoint gridPoint;
		for (Point p : centroidList) {
			/**
			 * For each JTS centroid evaluate lat lon check for line date create grid point
			 * add it to ret list
			 */
			latitude = p.getX();
			longitude = p.getY();
			if (this.isOverDateLine && (longitude > 180)) {
				longitude = longitude - 360;

			} // end if

			gridPoint = new GridPoint(gridpointList.size(), latitude, longitude,
					this.dem.getElevation(latitude, longitude));
			gridpointList.add(gridPoint);
		} // end for

		return gridpointList;
	}// end method

	/**
	 * Return the centroid of holes in the coverage area, empty list if no holes are
	 * found
	 *
	 * @param arList
	 * @return the list of holes centers, null if no holes are found
	 * @throws GridException
	 */
	protected List<Point> getHolesCentroid(List<AcqReq> arList) throws GridException {
		/**
		 * Ret list
		 */
		List<Point> gridpointList = new ArrayList<>();

		double targetArea = this.polygon.getArea();

		try {
			/**
			 * List of AR polygon
			 */
			List<Polygon> polyList = new ArrayList<>();
			Polygon p;
			for (AcqReq a : arList) {
				/**
				 * For each AR build polygon and add to list of AR polygon
				 */
				p = getPolygonFromAR(a);
				polyList.add((Polygon) GeometryPrecisionReducer.reduce(p, new PrecisionModel()));
			} // end ForgetHolesCenter

			// logger.debug("Unioning");
			/**
			 * Evaluating union of AR polygon
			 */
			Geometry dtoUnion = UnaryUnionOp.union(polyList);
			// logger.debug("Intersecting");

			/**
			 * Intersect Ar polygon union with target area polygon
			 */
			Polygon reducedPolygon = (Polygon) GeometryPrecisionReducer.reduce(this.polygon, new PrecisionModel());
			Geometry intersection = dtoUnion.intersection(reducedPolygon);

			/**
			 * Evaluating holes as synmmetric difference of intersection with target area
			 * polygon
			 */
			Geometry closure = intersection.symDifference(reducedPolygon);

			if (!closure.isEmpty()) {
				/**
				 * Found holes
				 */
				Point holeCenter;
				Polygon currentPoygon;
				double currentArea;
				if (closure instanceof MultiPolygon) {
					/**
					 * Multi hole number of holes
					 */
					int numOfPolygon = closure.getNumGeometries();

					for (int i = 0; i < numOfPolygon; i++) {
						/**
						 * For each hole evaluate polygon evaluting area check if the hole is so big to
						 * require a patch in this case evaluate centroid and add to ret list
						 */
						currentPoygon = (Polygon) closure.getGeometryN(i);
						currentArea = currentPoygon.getArea();
						if ((currentArea / targetArea) > this.notAllowedHoleAreaRatio) {

							holeCenter = currentPoygon.getCentroid();
							gridpointList.add(holeCenter);
						} // end if
					} // end for
				} // end if
				else if (closure instanceof Polygon) {
					/**
					 * single hole evaluate polygon evaluting area check if the hole is so big to
					 * require a patch in this case evaluate centroid and add to ret list
					 */
					currentPoygon = (Polygon) closure;
					currentArea = currentPoygon.getArea();
					if ((currentArea / targetArea) > this.notAllowedHoleAreaRatio) {
						holeCenter = currentPoygon.getCentroid();
						gridpointList.add(holeCenter);
					} // end if
				} // end else if
			} // end if

		} // end try
		catch (Exception e) {
			/**
			 * Error just throw
			 */
			throw new GridException(e.getMessage());
		}

		return gridpointList;
	}// end method

	/**
	 * Retrieve the posList of the intesection
	 *
	 * @return
	 *
	 *         public String getIntersectionPosList(double [][]corners) throws
	 *         GridException { String retval=""; Polygon
	 *         arPol=getPolygonFromCorners(corners);
	 *
	 *
	 *         Polygon reducedArPolygon = (Polygon)
	 *         GeometryPrecisionReducer.reduce(arPol,new PrecisionModel()); Polygon
	 *         reducedPolygon = (Polygon)
	 *         GeometryPrecisionReducer.reduce(this.polygon,new PrecisionModel());
	 *
	 *         Geometry intersection =
	 *         reducedArPolygon.intersection(reducedPolygon);
	 *
	 *         //Geometry intersection = arPol.intersection(this.polygon);
	 *
	 *         if(intersection instanceof Polygon) { Coordinate [] coordinate =
	 *         intersection.getCoordinates(); double latitude; double longitude;
	 *         for(int i =0;i<coordinate.length;i++) { latitude=coordinate[i].x;
	 *         longitude=coordinate[i].y; retval=retval +" "+latitude+" "+longitude
	 *         ; }//end for }//end if return retval; }//end method
	 */

	/**
	 * Retuirn the list of point falling inside an AR given a list of point
	 *
	 * @param list
	 * @param corners
	 * @return list of points inside AR
	 */
	@Override
	public List<GridPoint> getGridpointInsdeAR(List<GridPoint> list, double[][] corners) {
		/**
		 * list to be returned
		 */
		List<GridPoint> ppoinList = new ArrayList<>();

		/**
		 * polygong enclosing AR
		 */
		Polygon arPol = getPolygonFromCorners(corners);

		// Polygon reducedArPolygon = (Polygon)
		// GeometryPrecisionReducer.reduce(arPol,new PrecisionModel());

		Point currentPoint;
		double latitude;
		double longitude;
		for (GridPoint p : list) {
			/**
			 * For each point in input list evaluate lat and lon check for line of date
			 * create JTS point check if inside AR if yes add to retval list
			 */
			latitude = p.getLLH()[0];
			longitude = p.getLLH()[1];
			if (this.isOverDateLine && (longitude < 0)) {
				longitude = longitude + 360;
			}

			currentPoint = this.geometryFactory.createPoint(new Coordinate(latitude, longitude));
			if (currentPoint.within(arPol)) {
				ppoinList.add(p);
			} // end if
		} // end for

		// //System.out.println("=============================ritorno : " +
		// ppoinList.size() + " punti su "+ list.size());

		return ppoinList;
	}// end method

	/**
	 * In case of single acquisition return the poslist of the polygon to be used in
	 * AR poslist In this case is the poslist used in constructor
	 *
	 * @return poslist
	 */
	@Override
	public String retunPolyListForSingleAcq() {
		return this.polyList;
	}// end retunPolyListForSingleAcq

}// end Class
