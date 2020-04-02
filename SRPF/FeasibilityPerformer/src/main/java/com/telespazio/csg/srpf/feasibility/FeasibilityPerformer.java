/**
*
* MODULE FILE NAME:	FeasibilityPerformer.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Class used modelize / perform opration on Aqcuisition Request
*
* PURPOSE:			Perform Feasibility
*
* CREATION DATE:	17-12-2015
*
* AUTHORS:			Amedeo Bancone
*
* DESIGN ISSUE:		2.1
*
* INTERFACES:
*
* SUBORDINATES:
*
* MODIFICATION HISTORY:
*
*
* --------------------------+------------+----------------+-------------------------------
* 16-05-2016 | Amedeo Bancone  |1.1|changed message on PRSTATUS
* --------------------------+------------+----------------+-------------------------------
* 01-10-2018 | Amedeo Bancone  |2.0| Modified for:
* 									 use in memory DB
* 									 Add C4 funtionalities
* --------------------------+------------+----------------+-------------------------------
* 06-03-2018 | Amedeo Bancone  |2.1| Modified for:
* 									 Adding cgs SUF Calculator
* 									 Stero Algo
* 									 Di2s algo
* --------------------------+------------+----------------+-------------------------------
* PROCESSING
*/

package com.telespazio.csg.srpf.feasibility;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathExpressionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.referencing.GeodeticCalculator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.telespazio.csg.srpf.dataManager.bean.BeamBean;
import com.telespazio.csg.srpf.dataManager.bean.EpochBean;
import com.telespazio.csg.srpf.dataManager.bean.PlatformActivityWindowBean;
import com.telespazio.csg.srpf.dataManager.bean.SatelliteBean;
import com.telespazio.csg.srpf.dataManager.bean.SatellitePassBean;
import com.telespazio.csg.srpf.dataManager.bo.PlatformActivityWindowBO;
import com.telespazio.csg.srpf.dataManager.bo.SatelliteBO;
import com.telespazio.csg.srpf.dataManager.bo.SatellitePassBO;
import com.telespazio.csg.srpf.dataManager.inMemoryOrbitalData.EphemeridInMemoryDB;
import com.telespazio.csg.srpf.dem.DEMManager;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.suf.SUFCalculator;
import com.telespazio.csg.srpf.utils.DateUtils;
import com.telespazio.csg.srpf.utils.PropertiesReader;
import com.telespazio.csg.srpf.utils.XMLUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * This Class perform the main tasks related to the feasibility analysis
 *
 * @author Amedeo Bancone
 * @version 2.1
 */
public class FeasibilityPerformer

{

	static final Logger logger = LogManager.getLogger(FeasibilityPerformer.class.getName());

	/**
	 * Log
	 */
	private TraceManager tracer = new TraceManager();
	// static final Logger logger =
	// LogManager.getLogger(FeasibilityPerformer.class.getName());

	// This flag states that in case of spotlight on circle or polygon
	// the check on sinngle acquisibility must be performerd on the centroid of
	// the enelope
	// of the area of interest
	protected boolean haveCheckForCentroidIfSpotLightAndArea = true;

	/**
	 * Feasibility XSD
	 */
	protected String xsdPath = "";

	/**
	 * Working dir file
	 */
	protected String prListWorkingDir;

	/**
	 * Grid point list
	 */
	protected List<GridPoint> gridPointList = new ArrayList<>();

	/**
	 * DEM
	 */
	protected DEMManager dem;

	/**
	 * Gridder
	 */
	protected Gridder gridder;

	/**
	 * XML DOC
	 */
	protected Document doc = null;

	/**
	 * Satellite list
	 */
	protected List<Satellite> satList = new ArrayList<>();

	/**
	 * Default values for grid spacing
	 */
	protected double gridSpacing = FeasibilityConstants.StripMapGridSpacing;

	/**
	 * Strimap default
	 */
	protected double stripMapGridSpacing = FeasibilityConstants.StripMapGridSpacing;

	/**
	 * Spotlight default
	 */
	protected double spotlightGridSpacing = FeasibilityConstants.SpotLightGridSpacing;

	/*
	 * Max area of interest taht can be acquired in m2. This is the default value,
	 * to be used if not found in configuration
	 */
	protected double maxAreaOfInterest = FeasibilityConstants.MaxAreaOfInterest;

	/*
	 * min coverage requested to consider at least partial
	 */
	protected double minCoverage = FeasibilityConstants.MinCoverage;

	/*
	 * if true feasibility in the past are allowed (To be read by the configuration
	 * file)
	 */
	protected boolean performFeasibilityInThePast = false;

	/*
	 * number of outer iteration to be performed. Default is 5 if not specified in
	 * configuration file
	 */
	protected int numberOfOuterIteration = 5;

	/**
	 * min pr duration
	 */
	protected double minPRValidityDuration = FeasibilityConstants.MinimalPRValidityDuration;

	/**
	 * max pr duration
	 */
	protected double maxPRValidityDuration = FeasibilityConstants.MaximalPRvalidityDuration;

	/*
	 * if true use a number of days less than the validity
	 */
	protected boolean haveOptimizeTimeLine = false;

	/*
	 * if true resolve conflict in expanding DTO
	 */
	protected boolean haveCheckForConflict = false;

	/*
	 * polar limit
	 */
	protected double polarLimit = 84.0;

	// interferometric parameters
	/**
	 * True if interferometric
	 */
	protected boolean isInterferometric = false;
	/**
	 * First satellite
	 */
	protected Satellite firstInterferometricSatellite = null;
	/**
	 * Second satellite
	 */
	protected Satellite secondInterferometricSatellite = null;
	/**
	 * Decorrelation tolerance
	 */
	protected double decorrelationTolerance = DateUtils.secondsToJulian(1);
	/**
	 * decorrelation time
	 */
	protected double decorrelationTime = 0;

	// paramerters for serching holes
	/**
	 * True if have check
	 */
	protected boolean haveCheckForHoles = false;

	// flag for enabling disabling sparc
	/**
	 * True to enable sparc in feasibility
	 */
	protected boolean haveUseSparc = true;

	/**
	 * 0 optimal 1 suboptimal
	 */
	protected int configuredSparcMode = 0;

	/**
	 * Di2SConfirmation Flag
	 */
	protected boolean di2SAvailabilityConfirmationFlag = false;

	/**
	 * NamespaceURI prefix map
	 */

	Map<String, String> namespaceMap = new TreeMap<>();

	/**
	 * minimum coverage required between stereo dto
	 *
	 */
	protected double minCoverageBetweenStereoPairDto = 0.9;

	// if true we have perform a check on single acquisition in building the AR
	// element ion response
	protected boolean haveCheckForSingleAcquisitionInBuildingARElement = true;

	/**
	 * Constructor
	 *
	 * @param xsdPath
	 * @param DEMBaseDir, base directory of DEM DATA
	 * @throws IOException
	 */
	public FeasibilityPerformer(final String xsdPath, final String DEMBaseDir) throws IOException

	{
		this.xsdPath = xsdPath;
		this.dem = new DEMManager(DEMBaseDir);

		/**
		 * Initialize configuration
		 */
		initConfiguration();
	}// end method

	/**
	 * Default constructor: Uses the value specified in SPRPF properties to
	 * inizialize the xsd e dem
	 *
	 * @throws IOException
	 */
	public FeasibilityPerformer() throws IOException {
		/**
		 * Deafult constructor Init XSD
		 */
		String value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.XSD_PATH_CONF_KEY);
		if (value != null) {

			this.xsdPath = value;
		} else {
			// logger.fatal("Unable to found " +
			// FeasibilityConstants.XSD_PATH_CONF_KEY + " in conffiguration");
			/**
			 * No value found in Configuration
			 */
			this.tracer.critical(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.XSD_PATH_CONF_KEY + " in configuration");
			throw new IOException("Unable to found " + FeasibilityConstants.XSD_PATH_CONF_KEY + " in configuration");
		}

		value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.DEM_BASE_DIR_PATH_CONF_KEY);
		if (value != null) {

			this.dem = new DEMManager(value);
		} else {
			/**
			 * No value found in Configuration
			 */
			// logger.warn("Unable to found " +
			// FeasibilityConstants.DEM_BASE_DIR_PATH_CONF_KEY + " in
			// conffiguration. DEM will return always 0");
			this.dem = new DEMManager(".");
			this.tracer.critical(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.DEM_BASE_DIR_PATH_CONF_KEY + " in conffiguration");

		}

		initConfiguration();
	}// end method

	/**
	 * Initialize the paramenter under configuration
	 */
	protected void initConfiguration() {
		/**
		 * Max area
		 */
		String value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.MAX_AREA_OF_INTEREST_CONF_KEY);
		if (value != null) {
			try {
				double dValue = Double.valueOf(value);
				this.maxAreaOfInterest = dValue * FeasibilityConstants.Mega;
			} catch (Exception e) {
				/**
				 * No value found in Configuration using default
				 */

				// logger.warn("Unable to found " +
				// FeasibilityConstants.MAX_AREA_OF_INTEREST_CONF_KEY + " in
				// conffiguration");
				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Malformed " + FeasibilityConstants.MAX_AREA_OF_INTEREST_CONF_KEY + " in configuration");

			}

		} else {

			/**
			 * No value found in Configuration using default
			 */

			// logger.warn("Unable to found " +
			// FeasibilityConstants.MAX_AREA_OF_INTEREST_CONF_KEY + " in
			// conffiguration");
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.MAX_AREA_OF_INTEREST_CONF_KEY + " in configuration");

		}

		// Minimal PR validity duration
		value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.MINIMAL_PR_VALIDITY_DURATION_CONF_KEY);
		if (value != null) {
			try {
				double dValue = Double.valueOf(value);
				this.minPRValidityDuration = dValue;
			} catch (Exception e) {
				/**
				 * No value found in Configuration using default
				 */

				// logger.warn("Unable to found " +
				// FeasibilityConstants.MINIMAL_PR_VALIDITY_DURATION_CONF_KEY +
				// " in configuration");
				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Malformed " + FeasibilityConstants.MINIMAL_PR_VALIDITY_DURATION_CONF_KEY
								+ " in configuration");

			}

		} else {

			/**
			 * No value found in Configuration using default
			 */

			// logger.warn("Unable to found " +
			// FeasibilityConstants.MINIMAL_PR_VALIDITY_DURATION_CONF_KEY + " in
			// configuration");
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.MINIMAL_PR_VALIDITY_DURATION_CONF_KEY
							+ " in configuration");

		}

		// Max PR validity duration
		value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.MAX_PR_VALIDITY_DURATION_CONF_KEY);
		if (value != null) {
			try {
				double dValue = Double.valueOf(value);
				this.maxPRValidityDuration = dValue;
			} catch (Exception e) {
				/**
				 * No value found in Configuration using default
				 */

				// logger.warn("Unable to found " +
				// FeasibilityConstants.MAX_PR_VALIDITY_DURATION_CONF_KEY + " in
				// configuration");
				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Malformed " + FeasibilityConstants.MAX_PR_VALIDITY_DURATION_CONF_KEY + " in configuration");

			}

		} else {

			/**
			 * No value found in Configuration using default
			 */

			// logger.warn("Unable to found " +
			// FeasibilityConstants.MAX_PR_VALIDITY_DURATION_CONF_KEY + " in
			// configuration");
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.MAX_PR_VALIDITY_DURATION_CONF_KEY + " in configuration");

		}

		// Min coverage
		value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.MIN_COVERAGE_CONF_KEY);
		if (value != null) {
			try {
				double dValue = Double.valueOf(value);
				this.minCoverage = dValue;
			} catch (Exception e) {
				/**
				 * No value found in Configuration using default
				 */

				// logger.warn("Unable to found " +
				// FeasibilityConstants.MIN_COVERAGE_CONF_KEY + " in
				// configuration");
				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Unable to found " + FeasibilityConstants.MIN_COVERAGE_CONF_KEY + " in configuration");

			}

		} else {

			/**
			 * No value found in Configuration using default
			 */

			// logger.warn("Unable to found " +
			// FeasibilityConstants.MIN_COVERAGE_CONF_KEY + " in
			// configuration");
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.MIN_COVERAGE_CONF_KEY + " in configuration");

		}

		// Stripmap grid
		value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.STRIPMAP_GRID_SPACING_CONF_KEY);
		if (value != null) {
			try {
				double dValue = Double.valueOf(value);
				this.stripMapGridSpacing = dValue;
			} catch (Exception e) {
				// logger.warn("Unable to found " +
				// FeasibilityConstants.STRIPMAP_GRID_SPACING_CONF_KEY + " in
				// configuration");
				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Unable to found " + FeasibilityConstants.STRIPMAP_GRID_SPACING_CONF_KEY + " in configuration");
			}

		} else {

			/**
			 * No value found in Configuration using default
			 */

			// logger.warn("Unable to found " +
			// FeasibilityConstants.STRIPMAP_GRID_SPACING_CONF_KEY + " in
			// configuration");
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.STRIPMAP_GRID_SPACING_CONF_KEY + " in configuration");
		}

		// Spotlight grid
		value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.SPOTLIGHT_GRID_SPACING_CONF_KEY);
		if (value != null) {
			try {
				double dValue = Double.valueOf(value);
				this.spotlightGridSpacing = dValue;
			} catch (Exception e) {
				// logger.warn("Unable to found " +
				// FeasibilityConstants.SPOTLIGHT_GRID_SPACING_CONF_KEY + " in
				// configuration");
				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Unable to found " + FeasibilityConstants.SPOTLIGHT_GRID_SPACING_CONF_KEY
								+ " in configuration");
			}

		} else {

			/**
			 * No value found in Configuration using default
			 */

			// logger.warn("Unable to found " +
			// FeasibilityConstants.SPOTLIGHT_GRID_SPACING_CONF_KEY + " in
			// configuration");
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.SPOTLIGHT_GRID_SPACING_CONF_KEY + " in configuration");
		}

		// perform feasibility in the past
		value = PropertiesReader.getInstance()
				.getProperty(FeasibilityConstants.PERFORM_FEASIBILITY_IN_THE_PAST_CONF_KEY);
		if (value != null) {
			try {
				int iValue = Integer.valueOf(value);
				if (iValue == 1) {
					this.performFeasibilityInThePast = true;
				}
			} catch (Exception e) {
				// logger.warn("Unable to found " +
				// FeasibilityConstants.PERFORM_FEASIBILITY_IN_THE_PAST_CONF_KEY
				// + " in configuration");
				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Unable to found " + FeasibilityConstants.PERFORM_FEASIBILITY_IN_THE_PAST_CONF_KEY
								+ " in configuration");
			}

		} else {

			/**
			 * No value found in Configuration using default
			 */

			// logger.warn("Unable to found " +
			// FeasibilityConstants.PERFORM_FEASIBILITY_IN_THE_PAST_CONF_KEY + "
			// in configuration");
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.PERFORM_FEASIBILITY_IN_THE_PAST_CONF_KEY
							+ " in configuration");
		}

		// Number of outer iteration
		value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.NUMBER_OF_OUTER_ITERATION_CONF_KEY);
		if (value != null) {
			try {
				int iValue = Integer.valueOf(value);
				this.numberOfOuterIteration = iValue;

			} catch (Exception e) {
				// logger.warn("Unable to found " +
				// FeasibilityConstants.NUMBER_OF_OUTER_ITERATION_CONF_KEY + "
				// in configuration");
				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Malformed " + FeasibilityConstants.NUMBER_OF_OUTER_ITERATION_CONF_KEY + " in configuration");
			}

		} else {

			/**
			 * No value found in Configuration using default
			 */

			// logger.warn("Unable to found " +
			// FeasibilityConstants.NUMBER_OF_OUTER_ITERATION_CONF_KEY + " in
			// configuration");
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.NUMBER_OF_OUTER_ITERATION_CONF_KEY + " in configuration");
		}

		// have optimize time line
		value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.HAVE_OPTIMIZE_TIMELINE_CONF_KEY);
		if (value != null) {
			try {
				int iValue = Integer.valueOf(value);
				if (iValue == 1) {
					this.haveOptimizeTimeLine = true;
				}

			} catch (Exception e) {
				// logger.warn("Unable to found " +
				// FeasibilityConstants.NUMBER_OF_OUTER_ITERATION_CONF_KEY + "
				// in configuration");
				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Unable to found " + FeasibilityConstants.HAVE_OPTIMIZE_TIMELINE_CONF_KEY
								+ " in configuration");
			}

		} else {

			/**
			 * No value found in Configuration using default
			 */

			// logger.warn("Unable to found " +
			// FeasibilityConstants.NUMBER_OF_OUTER_ITERATION_CONF_KEY + " in
			// configuration");
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.HAVE_OPTIMIZE_TIMELINE_CONF_KEY + " in configuration");
		}

		// Check for conflict in expanding DTO
		value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.HAVE_CHECK_CONFLICT_CONF_KEY);
		if (value != null) {
			try {
				int iValue = Integer.valueOf(value);
				if (iValue == 1) {
					this.haveCheckForConflict = true;
				}

			} catch (Exception e) {
				// logger.warn("Unable to found " +
				// FeasibilityConstants.NUMBER_OF_OUTER_ITERATION_CONF_KEY + "
				// in configuration");
				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Unable to found " + FeasibilityConstants.HAVE_CHECK_CONFLICT_CONF_KEY + " in configuration");
			}

		} else {

			/**
			 * No value found in Configuration using default
			 */

			// logger.warn("Unable to found " +
			// FeasibilityConstants.NUMBER_OF_OUTER_ITERATION_CONF_KEY + " in
			// configuration");
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.HAVE_CHECK_CONFLICT_CONF_KEY + " in configuration");
		}

		// Polar limit
		value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.POLAR_LIMIT_CONF_KEY);
		if (value != null) {
			try {
				double dValue = Double.valueOf(value);
				this.polarLimit = dValue;
			} catch (Exception e) {
				// logger.warn("Unable to found " +
				// FeasibilityConstants.MINIMAL_PR_VALIDITY_DURATION_CONF_KEY +
				// " in configuration");
				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Unable to found " + FeasibilityConstants.POLAR_LIMIT_CONF_KEY + " in configuration");

			}

		} else {

			/**
			 * No value found in Configuration using default
			 */

			// logger.warn("Unable to found " +
			// FeasibilityConstants.MINIMAL_PR_VALIDITY_DURATION_CONF_KEY + " in
			// configuration");
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.POLAR_LIMIT_CONF_KEY + " in configuration");

		}

		// search for hole parameters
		value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.HAVE_CHECK_FOR_HOLES_CONF_KEY);
		if (value != null) {
			try {
				int iValue = Integer.valueOf(value);
				if (iValue == 1) {
					this.haveCheckForHoles = true;
				}

			} catch (Exception e) {
				// logger.warn("Unable to found " +
				// FeasibilityConstants.NUMBER_OF_OUTER_ITERATION_CONF_KEY + "
				// in configuration");
				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Unable to found " + FeasibilityConstants.HAVE_CHECK_FOR_HOLES_CONF_KEY + " in configuration");
			}

		} else {

			/**
			 * No value found in Configuration using default
			 */

			// logger.warn("Unable to found " +
			// FeasibilityConstants.NUMBER_OF_OUTER_ITERATION_CONF_KEY + " in
			// configuration");
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.HAVE_CHECK_FOR_HOLES_CONF_KEY + " in configuration");
		}

		// Check for conflict in expanding DTO
		value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.HAVE_USE_SPARC_CONF_KEY);
		if (value != null) {
			try {
				int iValue = Integer.valueOf(value);
				if (iValue == 0) {
					this.haveUseSparc = false;
				}

			} catch (Exception e) {
				// logger.warn("Unable to found " +
				// FeasibilityConstants.NUMBER_OF_OUTER_ITERATION_CONF_KEY + "
				// in configuration");
				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Malformed " + FeasibilityConstants.HAVE_USE_SPARC_CONF_KEY + " in configuration");
			}

		} else {

			/**
			 * No value found in Configuration using default
			 */

			// logger.warn("Unable to found " +
			// FeasibilityConstants.NUMBER_OF_OUTER_ITERATION_CONF_KEY + " in
			// configuration");
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.HAVE_USE_SPARC_CONF_KEY + " in configuration");
		}

		// Minimum percentage required between stereo DTO
		value = PropertiesReader.getInstance()
				.getProperty(FeasibilityConstants.MIN_STEREO_COVERAGE_BETWEEN_STEREO_CONF_KEY);
		if (value != null) {
			try {
				double dValue = Double.valueOf(value);

				this.minCoverageBetweenStereoPairDto = dValue;
			} catch (Exception e) {
				// logger.warn("Unable to found " +
				// FeasibilityConstants.NUMBER_OF_OUTER_ITERATION_CONF_KEY + "
				// in configuration");
				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Malformed " + FeasibilityConstants.MIN_STEREO_COVERAGE_BETWEEN_STEREO_CONF_KEY
								+ " in configuration");
			}

		} else {

			/**
			 * No value found in Configuration using default
			 */

			// logger.warn("Unable to found " +
			// FeasibilityConstants.NUMBER_OF_OUTER_ITERATION_CONF_KEY + " in
			// configuration");
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.MIN_STEREO_COVERAGE_BETWEEN_STEREO_CONF_KEY
							+ " in configuration");
		}

		// sparc mode
		value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.SPARC_MODE_CONF_KEY);
		if (value != null) {
			try {
				int iValue = Integer.valueOf(value);
				if (iValue == 1) {
					this.configuredSparcMode = 1;
				}
			} catch (Exception e) {
				// logger.warn("Unable to found " +
				// FeasibilityConstants.PERFORM_FEASIBILITY_IN_THE_PAST_CONF_KEY
				// + " in configuration");
				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Unable to found " + FeasibilityConstants.SPARC_MODE_CONF_KEY + " in configuration");
			}

		} else {

			/**
			 * No value found in Configuration using default
			 */

			// logger.warn("Unable to found " +
			// FeasibilityConstants.PERFORM_FEASIBILITY_IN_THE_PAST_CONF_KEY + "
			// in configuration");
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.SPARC_MODE_CONF_KEY + " in configuration");
		}

	}// end method

	/**
	 * Set the grid spacing (degree)
	 *
	 * @param gridSpacing
	 */
	void setGridSpacing(double gridSpacing) {
		this.gridSpacing = gridSpacing;
	}// end method

	/**
	 * Set the dem manager
	 *
	 * @param dem
	 */
	public void setDEM(DEMManager dem) {
		this.dem = dem;
	}// end method

	/**
	 * Perform the feasibility
	 *
	 * @param prlistPath
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws XPathExpressionException
	 * @throws GridException
	 * @throws TransformerException
	 * @throws DateTimeParseException
	 * @throws Exception
	 */
	public String performFeasibility(final String prlistPath)
			throws SAXException, IOException, ParserConfigurationException, XPathExpressionException, GridException,
			TransformerException, DateTimeParseException, Exception

	{
		/**
		 * Factory instance
		 */
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db;
		db = dbf.newDocumentBuilder();
		this.doc = db.parse(prlistPath);

		logger.debug("Loaded document " + prlistPath);
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		/**
		 * validating doc against schema
		 */
		Schema schema = factory.newSchema(new File(this.xsdPath));
		schema.newValidator().validate(new DOMSource(this.doc));
		FeasibilityPerformer.logger.debug("Parsed valid document");

		/**
		 * Performing feasibility
		 */
		return performFeasibility(prlistPath, this.doc);

	} // end performFeasibility

	/**
	 * Perform the feasibility
	 *
	 * @param prlistPath
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	public String performFeasibility(final String prlistPath, Document doc) throws Exception {
		String responsePath = "";

		this.doc = doc;
		/**
		 * Evaluating working dir
		 */
		this.prListWorkingDir = new File(prlistPath).getParent();

		/**
		 * Evaluating response name
		 */
		responsePath = this.evaluateResponseName(prlistPath);
		Node root = this.doc.getFirstChild();

		this.namespaceMap = XMLUtils.createNSPrefixMap(this.doc);

		/**
		 * Building request
		 */
		PRRequestParameter request = new PRRequestParameter(doc);

		/**
		 * evaluating timing info
		 */
		double startValidityTime = DateUtils.fromISOToCSKDate(request.getStartTime());
		double stopValidityTime = DateUtils.fromISOToCSKDate(request.getStopTime());
		double validityDuration = stopValidityTime - startValidityTime;
		String validityTruncated = new DecimalFormat("#.###").format(validityDuration);		/**
		 * True if the request is good
		 */
		boolean isGoodrequest = true;

		/**
		 * Renaming DOM rootnode
		 */
		this.doc.renameNode(root, root.getNamespaceURI(), FeasibilityConstants.FeasibilityAnalysisResponseTagName);
		// logger.debug("DUMP XML "+doc);

		FeasibilityPerformer.logger.debug("Validity " + validityTruncated + " days");

		/**
		 * Check if validity is inside limits
		 */
		if ((validityDuration < this.minPRValidityDuration) || (validityDuration > this.maxPRValidityDuration))

		{
			fillPRStatusWithError("PR validity outside the interval " + this.minPRValidityDuration + "-"
					+ this.maxPRValidityDuration + " days");
			isGoodrequest = false;
		}

		double upperTimeTorequest = startValidityTime;
		if (isGoodrequest) {
			/**
			 * Trying to perform timeline optimization in order to reduce calculation
			 */
			upperTimeTorequest = getUpperTimeLimit(startValidityTime, stopValidityTime, request);
			if (upperTimeTorequest < 0) // no feasibility in the past are
										// allowed: check conf file
			{
				fillPRStatusWithError(
						"PR validity is in the past. Actually the system is not configured to perform feasibility in the past.");
				isGoodrequest = false;
			}

		} // end if

		/**
		 * Stereo non allowed on both
		 */
		if (request.isStereo() && (request.getRequestedOrbitDirection() == FeasibilityConstants.BothOrbitDirection)) {
			fillPRStatusWithError("Orbit direction Both is not allowed for StereoPair request");
			isGoodrequest = false;
		} // end if

		/**
		 * Stereo allowed only on CSG
		 */
		if (request.isStereo() && (!request.getMission().equals(FeasibilityConstants.CSG_NAME))) {
			fillPRStatusWithError("StereoPaire request are only allowed for CSG request");
			isGoodrequest = false;
		} // end if

		if (isGoodrequest) {
			/**
			 * We pass all intermediate test so we can perform feadibility
			 */
			doFeasibility(request, startValidityTime, upperTimeTorequest, stopValidityTime);
		}

		FeasibilityPerformer.logger.debug("Dumping feasibility response in file: " + responsePath);

		/**
		 * Dumping response on a file
		 */

		XMLUtils.dumpResponseToFile(this.doc, responsePath);
		return responsePath;
	}// end method

	/**
	 * In case list aim is not allowed it return a failed response
	 *
	 * @param prlistPath
	 * @param doc
	 * @return
	 * @throws FeasibilityException
	 * @throws TransformerException
	 */
	String dumpWrongListAim(final String prlistPath, Document doc) throws FeasibilityException, TransformerException {
		/**
		 * List aim not allowed
		 *
		 */
		String responsePath = "";

		this.doc = doc;

		this.prListWorkingDir = new File(prlistPath).getParent();

		responsePath = this.evaluateResponseName(prlistPath);

		fillPRStatusWithError(
				"Check the listAim field of the request. Only the following are managed by SRPF: Feasibility and FeasibilityExtension");
		XMLUtils.dumpResponseToFile(doc, responsePath);
		return responsePath;
	}// end method

	/**
	 * Since teh ODREF are repetible after 16 days it doesn't make sense retrieve
	 * from DB more than 32 days in the worst case. More over if startValidity > now
	 * + 16, in the worst case only 16 day from odref are needed Use carefully,
	 * basically on test, it can produce trouble with paw and pass trough. Disable
	 * it by configuration.
	 *
	 * @param startValidityTime
	 * @param stopValidityTime
	 * @return the upper util time for feasibility
	 */
	protected double getUpperTimeLimit(double startValidityTime, double stopValidityTime, PRRequestParameter pr) {
		/**
		 * By default uppertime: retval coincide with stopValidityTime
		 */
		double upperTime = stopValidityTime;
		double now = DateUtils.cskDateTimeNow();

		if ((startValidityTime < now) && !this.performFeasibilityInThePast) {
			/**
			 * Past not allowed
			 */
			return -1;
		}

		if (!this.haveOptimizeTimeLine || pr.isPassThrough()
				|| !pr.getMission().equalsIgnoreCase(FeasibilityConstants.CSK_NAME) || pr.isInterferometric()) {
			/**
			 * Optimization not allowed : uppertime coincide with stopValidity time
			 */
			this.haveOptimizeTimeLine = false;
			FeasibilityPerformer.logger.debug("timeline optimization disabled");
			return upperTime;
		}

		FeasibilityPerformer.logger.debug("Trying to optimize timeline");
		/**
		 *
		 */

		double duration = stopValidityTime - startValidityTime;

		double maxUsefullDuartion;
		if (startValidityTime < now) {
			/**
			 * In the past we the maximun optimization in 32 days
			 */
			maxUsefullDuartion = FeasibilityConstants.forTwo * FeasibilityConstants.RepetitionODREFPeriod;
		} else {
			double minValue = FeasibilityConstants.RepetitionODREFPeriod;
			/**
			 * difftime
			 */
			double difftime = startValidityTime - now;
			if (difftime < FeasibilityConstants.RepetitionODREFPeriod) {
				minValue = difftime;
			}

			maxUsefullDuartion = (FeasibilityConstants.forTwo * FeasibilityConstants.RepetitionODREFPeriod) - minValue;
		}

		if (duration > maxUsefullDuartion) {
			/**
			 * We change uppertime
			 */
			upperTime = startValidityTime + maxUsefullDuartion;
		}
		return upperTime;

	}// end getUpperTimeLimit

	/**
	 * Create a PRststus section in xml response having description filled with the
	 * provided message
	 *
	 * @param doc
	 * @param msg
	 * @throws GridException
	 * @throws FeasibilityException
	 */
	protected void fillPRStatusWithError(String msg) throws FeasibilityException {
		FeasibilityPerformer.logger.debug("Failed Feasibility: " + msg);
		if (this.doc == null) {
			/**
			 * Null doc : never happens
			 */
			throw new FeasibilityException("Not initialized DOM in feasibility");
		}

		/**
		 * Retrieving node
		 */
		NodeList nl = this.doc.getElementsByTagNameNS(FeasibilityConstants.ProgReqTagNameNS,
				FeasibilityConstants.ProgReqTagName); // retrieve
														// the
														// PR
														// NODE
		if (nl.getLength() == 0) {
			throw new FeasibilityException("Unable to found: " + FeasibilityConstants.ProgReqTagName);
		}
		Element programmingRequestNode = (Element) nl.item(0);

		/**
		 * Creating prstatus element
		 */
		Element prStatus = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.PRStatusTagName,
				FeasibilityConstants.PRStatusTagNameNS);

		programmingRequestNode.appendChild(prStatus);

		Element status = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.statusTagName,
				FeasibilityConstants.statusTagNameNS);
		prStatus.appendChild(status);

		String statusString = FeasibilityConstants.FailedStatusString;
		/**
		 * Settin status string
		 */
		status.setTextContent(statusString);
		Element description = XMLUtils.createElement(this.doc, this.namespaceMap,
				FeasibilityConstants.descriptionTagName, FeasibilityConstants.descriptionTagNameNS);

		prStatus.appendChild(description);
		description.setTextContent(msg);
		/**
		 * creating coverage element
		 */
		Element coverageElement = XMLUtils.createElement(this.doc, this.namespaceMap,
				FeasibilityConstants.PRCoverageTagName, FeasibilityConstants.PRCoverageTagNameNS);
		programmingRequestNode.appendChild(coverageElement);
		/**
		 * Setting coverage to zero
		 */
		coverageElement.setTextContent(String.format(Locale.US, "%.3f", 0.0));

	}// fillPRStatudWithError

	/**
	 * Fill the satellite list and check it against request constraint
	 *
	 * @param request
	 * @param startValidityTime
	 * @param upperTimeTorequest
	 * @param stopValidityTime
	 * @return sat list
	 * @throws Exception
	 */
	List<Satellite> fillsatelliteList(PRRequestParameter request, double startValidityTime, double upperTimeTorequest,
			double stopValidityTime) throws Exception {
		SatelliteBO satelliteBO;

		satelliteBO = new SatelliteBO();

		FeasibilityPerformer.logger.debug("Filling Satellite and beams");
		Map<SatelliteBean, List<BeamBean>> satBeamMap;
		/**
		 * By default sensormdeIsCSK
		 */
		String sensorMode = request.getSensorModeCSK();
		String mission = request.getMission();
		if (!request.isCombined()) {
			/**
			 * Not combined request
			 */
			if (mission.equalsIgnoreCase(FeasibilityConstants.CSG_NAME)) {
				/**
				 * CSG MIssion we have change sensor mode
				 */

				sensorMode = request.getSensorModeCSG();
			}
			/**
			 * Retrieving beam map from DB
			 */
			satBeamMap = satelliteBO.getBeamsSatellite(mission, sensorMode, false);
		} // end if
		else {
			/**
			 * Combined request
			 */
			this.tracer.log("Retrieving data for CSK in combined request");
			Map<SatelliteBean, List<BeamBean>> satBeamMap2;
			// satBeamMap=satelliteBO.getBeamsSatellite(request.getSensorModeCSG(),
			// request.getSensorModeCSK(),true);
			/**
			 * Filling beam map for CSK
			 */
			satBeamMap = satelliteBO.getBeamsSatellite(FeasibilityConstants.CSK_NAME, request.getSensorModeCSK(),
					false);
			this.tracer.log("Retrieving data for CSG in combined request");
			/**
			 * Filling beam map for CSG
			 */
			satBeamMap2 = satelliteBO.getBeamsSatellite(FeasibilityConstants.CSG_NAME, request.getSensorModeCSG(),
					false);
			for (Entry<SatelliteBean, List<BeamBean>> entry : satBeamMap2.entrySet()) {
				/**
				 * merging maps
				 */
				satBeamMap.put(entry.getKey(), entry.getValue());
			} // end for
		} // end else

		if (satBeamMap.entrySet().size() != 0) {
			//logger.trace("beam found " + satBeamMap);

			/**
			 * If map not empty
			 */

			for (Entry<SatelliteBean, List<BeamBean>> entry : satBeamMap.entrySet()) {
				/**
				 * List of active beam
				 */
				ArrayList<BeamBean> activeBeam = new ArrayList<>();

				for (BeamBean currentBeam : entry.getValue()) {
					if (currentBeam.getIsEnabled() == 1) {
						/**
						 * Add beam to active beam
						 */
						activeBeam.add(currentBeam);
					}
				} // end for

				if (activeBeam.size() != 0) {
					/**
					 * Create a satellite and add it to satellite list
					 */
					Satellite s = new Satellite(entry.getKey());
					s.setBeams(activeBeam);
					this.satList.add(s);
				} // end if

			} // end for

			/**
			 * Checking satellites against request constraints
			 *
			 */
			this.satList = request.chekSatelliteList(this.satList);
			/**
			 * Build for each satellite a epochs
			 */
			filleSatListWithEphemerid(request, startValidityTime, upperTimeTorequest, stopValidityTime);
		}
		// end if

		else {
			logger.debug("no beam found ");
		}

		return this.satList;
	}// end method

	/**
	 *
	 * @param request
	 * @param startValidityTime
	 * @param upperTimeTorequest
	 * @param stopValidityTime
	 * @return
	 * @throws Exception
	 */
	/*
	 * List<Satellite> fillsatelliteListOLD(PRRequestParameter request,double
	 * startValidityTime, double upperTimeTorequest, double stopValidityTime) throws
	 * Exception { //TODO TO BE DELETED? SatelliteBO satelliteBO; //EpochBO epochBO;
	 * //PlatformActivityWindowBO pawBO; satelliteBO = new SatelliteBO();
	 *
	 * this.logger.debug("Seraching for sensor mode"); int idSensorMode =
	 * satelliteBO.getIdSensorMode(request.getSensorMode()); int idMission =
	 * satelliteBO.getIdMission(request.getMission()); this.logger.debug("Mission  "
	 * + request.getMission());
	 *
	 * ArrayList<SatelliteBean>
	 * satellitesPerSensorModeList=satelliteBO.getSatellitesPerSensorMode(
	 * idSensorMode,idMission);
	 *
	 *
	 * if(satellitesPerSensorModeList.size()!=0) {
	 *
	 * for(int i=0; i< satellitesPerSensorModeList.size();i++) { ArrayList<BeamBean>
	 * beamsSatelliteList=satelliteBO.getBeamsSatellite(idSensorMode,
	 * satellitesPerSensorModeList.get(i).getIdSatellite());
	 *
	 *
	 *
	 *
	 *
	 * ArrayList<BeamBean> activeBeam = new ArrayList<BeamBean>(); BeamBean beam;
	 * for(int k =0; k< beamsSatelliteList.size(); k++ ) { beam =
	 * beamsSatelliteList.get(k); //Only active beam can be used in feasibility
	 * if(beam.getIsEnabled()==1) activeBeam.add(beam);
	 *
	 * }
	 *
	 * if(activeBeam.size()==0) continue;
	 *
	 * Satellite s = new
	 * Satellite((SatelliteBean)satellitesPerSensorModeList.get(i));
	 * s.setBeams(activeBeam); this.satList.add(s); }
	 *
	 * //Checking satellitre this.satList = request.chekSatelliteList(this.satList);
	 * filleSatListWithEphemerid(request,startValidityTime,upperTimeTorequest,
	 * stopValidityTime); }
	 *
	 * return this.satList; }
	 */

	/**
	 * Fille the sa6tellite list epochs
	 *
	 * @param request
	 * @param startValidityTime
	 * @param upperTimeTorequest
	 * @param stopValidityTime
	 * @throws Exception
	 */
	protected void filleSatListWithEphemerid(PRRequestParameter request, double startValidityTime,
			double upperTimeTorequest, double stopValidityTime) throws Exception {
		PlatformActivityWindowBO pawBO;
		pawBO = new PlatformActivityWindowBO();
		
		logger.debug("FROM METHOD filleSatListWithEphemerid");
		/**
		 * Paw map
		 */
		Map<String, ArrayList<PlatformActivityWindowBean>> satPawMap = pawBO.getPaws(startValidityTime,
				stopValidityTime);

		/**
		 * Pass Plan map
		 */
		Map<String, ArrayList<SatellitePassBean>> satPassMap = null;

		if (request.isPassThrough()) {
			/**
			 * In case of passtrough fill the pass plan map
			 */
			SatellitePassBO satPassBO = new SatellitePassBO();
			satPassMap = satPassBO.selectSatellitePass(request.getAcquisitionStationList(), startValidityTime,
					stopValidityTime);

		}

		for (Satellite s : this.satList) {
			/**
			 * For each satellite retrieve the epochs belonging the pr validity
			 */

			ArrayList<EpochBean> epochList = EphemeridInMemoryDB.getInstance().selectEpochs(s.getName(),
					startValidityTime, upperTimeTorequest);
			
//			logger.debug("set epoch FROM filleSatListWithEphemerid");
//    		logger.debug("upperTimeTorequest "+DateUtils.fromCSKDateToDateTime(upperTimeTorequest));

    		
			s.setEpochs(epochList);

			if (epochList.size() == 0) {
				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Unable to found valid orbital data for " + s.getName()
								+ " check that at least ODREF are present on DB");
			}

			/**
			 * Retrieve PAW
			 */
			ArrayList<PlatformActivityWindowBean> pawList = satPawMap.get(s.getName());

			if (pawList == null) {
				pawList = new ArrayList<>();
			}

			s.setPawList(pawList);

			if (request.isPassThrough()) {
				/*
				 * SatellitePassBO satPassBO = new SatellitePassBO();
				 * ArrayList<SatellitePassBean> satPassList =
				 * satPassBO.selectSatellitePass(request. getAcquisitionStationList(),
				 * s.getSatID(), startValidityTime, stopValidityTime);
				 * s.setSatellitePassList(satPassList);
				 */

				/**
				 * St pass plan
				 */
				ArrayList<SatellitePassBean> satPassList = satPassMap.get(s.getName());
				if (satPassList == null) {
					satPassList = new ArrayList<>();
				}
				s.setSatellitePassList(satPassList);

			} // end if

		} // end for
	}// end filleSatListWithEphemerid

	/**
	 * Fille the sa6tellite list epochs
	 *
	 * @param request
	 * @param startValidityTime
	 * @param upperTimeTorequest
	 * @param stopValidityTime
	 * @throws Exception
	 *
	 *                   protected void
	 *                   filleSatListWithEphemeridOLD(PRRequestParameter
	 *                   request,double startValidityTime, double
	 *                   upperTimeTorequest, double stopValidityTime) throws
	 *                   Exception {
	 *
	 *
	 *                   for(Satellite s: this.satList) {
	 *
	 *
	 *                   ArrayList<EpochBean> epochList =
	 *                   EphemeridInMemoryDB.getInstance().selectEpochs(s.getName(),
	 *                   startValidityTime, upperTimeTorequest);
	 *                   s.setEpochs(epochList);
	 *
	 *                   if(epochList.size()==0) {
	 *                   this.tracer.warning(EventType.SOFTWARE_EVENT,
	 *                   ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable
	 *                   to found valid orbital data for " + s.getName() + " check
	 *                   that at least ODREF are present on DB"); }
	 *                   PlatformActivityWindowBO pawBO; pawBO=new
	 *                   PlatformActivityWindowBO();
	 *
	 *                   ArrayList<PlatformActivityWindowBean> pawList =
	 *                   pawBO.selectPawData(s.getSatID(), startValidityTime,
	 *                   stopValidityTime);
	 *
	 *                   s.setPawList(pawList);
	 *
	 *                   if(request.isPassThrough()) { SatellitePassBO satPassBO =
	 *                   new SatellitePassBO(); ArrayList<SatellitePassBean>
	 *                   satPassList =
	 *                   satPassBO.selectSatellitePass(request.getAcquisitionStationList(),
	 *                   s.getSatID(), startValidityTime, stopValidityTime);
	 *                   s.setSatellitePassList(satPassList);
	 *
	 *                   }
	 *
	 *
	 *                   }//end for }//end filleSatListWithEphemerid
	 */

	/**
	 * Return the max area of interest for the selected sensor mode
	 *
	 * @param sensorMode
	 * @return max area of interest
	 */
	protected double getSpecificArea(String sensorMode) {
		/**
		 * setting default as retval
		 */
		double area = this.maxAreaOfInterest;

		/**
		 * Building the key to be used to retrieve value from configuration
		 */
		String maxAreaForSensorConfigurationKey = sensorMode + FeasibilityConstants.MaxAreaOfInterestPostFix;

		/**
		 * Retrieving value from configuration
		 */
		String areaAsString = PropertiesReader.getInstance().getProperty(maxAreaForSensorConfigurationKey);

		if (areaAsString != null) {
			try {
				double dValue = Double.valueOf(areaAsString);
				area = dValue * FeasibilityConstants.Mega;

				FeasibilityPerformer.logger.debug("Max Area for Sensor " + sensorMode + " " + areaAsString + " km2");
			} catch (Exception e) {
				/**
				 * The value is misconfigured default used
				 */
				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Malformed value for " + maxAreaForSensorConfigurationKey + " in configuration");
			} // end catch
		} // end if
		else {
			/**
			 * The value is not configured default used
			 */
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found Max area of interest for sensor: " + sensorMode + " in configuration");
		} // end else

		return area;
	}// end method

	/**
	 * Check if the area of interest doesn't exeed the max area of interest
	 *
	 * @param request
	 * @return true if the area doesn't exeed the max area of interest
	 */
	protected boolean checkAreaOfInterest(PRRequestParameter request) {
		/**
		 * Default good
		 */
		boolean retval = true;

		/**
		 * Retrieve the area from gridder
		 */
		double area = this.gridder.getAream2();
		
		String validityTruncated = new DecimalFormat("#.###").format(area / FeasibilityConstants.Mega);	

		FeasibilityPerformer.logger.debug("Area of interest of : " + validityTruncated + " Km2");
		double cskMaxArea = this.maxAreaOfInterest;
		double csgMaxArea = this.maxAreaOfInterest;

		if (request.isCombined()) {
			/**
			 * IN case of combined the less is used
			 */
			cskMaxArea = this.getSpecificArea(request.getSensorModeCSK());
			csgMaxArea = this.getSpecificArea(request.getSensorModeCSG());
			this.maxAreaOfInterest = cskMaxArea;
			if (csgMaxArea < cskMaxArea) {
				this.maxAreaOfInterest = csgMaxArea;
			}
		} else if (request.getMission().equalsIgnoreCase(FeasibilityConstants.CSK_NAME)) {
			/**
			 * Case CSk
			 */
			this.maxAreaOfInterest = this.getSpecificArea(request.getSensorModeCSK());
		} else {
			/**
			 * case CSG
			 */
			this.maxAreaOfInterest = this.getSpecificArea(request.getSensorModeCSG());
		}

		retval = this.maxAreaOfInterest >= area;

		return retval;
	}// end method

	/**
	 * elaborate the request
	 *
	 * @param request
	 * @param startValidityTime
	 * @param stopValidityTime
	 * @throws Exception
	 */
	protected void doFeasibility(PRRequestParameter request, double startValidityTime, double upperTimeTorequest,
			double stopValidityTime) throws Exception {
		// long start;
		// long stop;

		FeasibilityPerformer.logger.debug("Elaborating request and retrieving data from DB");
		FeasibilityPerformer.logger.debug("start validity : " + DateUtils.fromCSKDateToDateTime(startValidityTime));
		FeasibilityPerformer.logger.debug("stop validity : " + DateUtils.fromCSKDateToDateTime(stopValidityTime));

		/**
		 * Fille the satellite list
		 */
		fillsatelliteList(request, startValidityTime, upperTimeTorequest, stopValidityTime);

		if (this.satList.size() == 0) {
			// logger.error("No satellite found");
			// throw new FeasibilityException("Unable to found satellite with
			// configured beam in DB ");
			/**
			 * If satellite list empty have exit
			 */
			fillPRStatusWithError("Unable to found satellite in DB satifying the request");
			this.tracer.log("Unable to found satellite in DB satifying the request");
			return;
		}

		BeamBean beam = this.satList.get(0).getBeams().get(0);

		/**
		 * Set spotlight flag
		 */
		boolean isSpotLight = beam.isSpotLight();
		this.gridSpacing = this.stripMapGridSpacing;
		if (isSpotLight) {
			// logger.info("-------------------------SPOT-LIGHT-----------------------");
			/**
			 * Use default grid spacing if not configured for sensor mode
			 */
			this.gridSpacing = this.spotlightGridSpacing;
		}

		/**
		 * Evaluating grid spacing for sendor mode
		 */
		getGridSpcingPerSensorMode(beam.getSensorModeName());

		FeasibilityPerformer.logger.debug("grid spacing: " + this.gridSpacing);

		/**
		 * Building gridder
		 */
		this.gridder = buildGrid(request);

		/**
		 * Filling grid
		 */
		this.gridder.fillGrid(this.gridPointList);

		// Checking for Di2S and Minumum AoI
		if (isSpotLight && request.isDi2sAvailabilityFlag()) {
			/**
			 * if the minumum AoI does not coincide with AoI you have to check if the
			 * minimum AoI is inside AoI
			 */
			if (!request.isMinimumAoICoincidentWithAoI()) {
				logger.debug("isMinimumAoICoincidentWithAoI = false");

				//se ho inserito l'area minima -> fai check con minimum AOI, che se è interna o coincidente con area target restituisce true
				this.di2SAvailabilityConfirmationFlag = ((PolygonGridder) this.gridder)
						.insertCheckMinimumAoI(request.getMinimumAoI(), this.gridPointList);
				
			} else {
				logger.debug("isMinimumAoICoincidentWithAoI = true");

				//se non ho inserito l'area minima = coincidente con area target
				this.di2SAvailabilityConfirmationFlag = true;
			}
			logger.debug("di2SAvailabilityConfirmationFlag : "+this.di2SAvailabilityConfirmationFlag);

		} // end if

		/**
		 * inserting centroid in case if spotlght on poligon / circle and if configured
		 *
		 */
		if (isSpotLight && this.haveCheckForCentroidIfSpotLightAndArea
				&& ((request.getRequestProgrammingAreaType() == PRRequestParameter.CircleRequestType)
						|| (request.getRequestProgrammingAreaType() == PRRequestParameter.PolygonRequestType))) {
			FeasibilityPerformer.logger.debug("Inserting centroid on grid");
			insertCenterInGridPointList();

		}

		// Chheck for area dimension
		int requestType = request.getRequestProgrammingAreaType();
		FeasibilityPerformer.logger.debug("Request type: " + requestType);
		/**
		 * Check for area of interest in case of circle or polygon
		 */
		if ((requestType == PRRequestParameter.PolygonRequestType)
				|| (requestType == PRRequestParameter.CircleRequestType)) {
			/*
			 * //PolygonGridder polyGrid= (PolygonGridder) this.gridder; double area =
			 * this.gridder.getAream2(); this.logger.debug("Area of interest of : " +
			 * area/1e6 + "Km2"); double cskMaxArea=maxAreaOfInterest; double
			 * csgMaxArea=maxAreaOfInterest; if(request.isCombined()) {
			 * cskMaxArea=this.getSpecificArea(request.getSensorModeCSK());
			 * csgMaxArea=this.getSpecificArea(request.getSensorModeCSG());
			 * maxAreaOfInterest = cskMaxArea; if(csgMaxArea>cskMaxArea) {
			 * maxAreaOfInterest=csgMaxArea; } } else
			 * if(request.getMission().equalsIgnoreCase(FeasibilityConstants. CSK_NAME)) {
			 * maxAreaOfInterest=this.getSpecificArea(request.getSensorModeCSK() ); } else {
			 * maxAreaOfInterest=this.getSpecificArea(request.getSensorModeCSG() ); }
			 *
			 * if(area > maxAreaOfInterest)
			 */
			if (!this.checkAreaOfInterest(request)) {
				// logger.warn("Area exeed ");
				/**
				 * In case area exeeds fill error and dump
				 */
				fillPRStatusWithError("PR area of interest exeed max dimension: "
						+ (this.maxAreaOfInterest / FeasibilityConstants.Mega) + " Km2");
				return;
			}
		}

		/**
		 * Requestes of type Point with duration are not allowed in case of spotlight
		 */
		if ((requestType == PRRequestParameter.pointRequestWithDuration) && isSpotLight) {
			FeasibilityPerformer.logger
					.debug("Request of type Point with duration is not allowed for spotlight sensor mode");
			fillPRStatusWithError("Request of type Point with duration is not allowed for spotlight sensor mode");
			return;
		}

		// Calcolo accessi
		/**
		 * Evaluating accesses
		 */
		FeasibilityPerformer.logger.debug("Evaluating accesses");
		Date dateBeforeEvaluatingAccesses = new Date();
		AccessesEvaluator eval = new AccessesEvaluator();
		eval.evaluateSatelliteAccesses(this.satList, this.gridPointList, request);
		FeasibilityPerformer.logger.debug("Accesses evaluated");
		Date dateAfterEvaluatingAccesses = new Date();
		long gap = dateAfterEvaluatingAccesses.getTime() - dateBeforeEvaluatingAccesses.getTime();
		FeasibilityPerformer.logger.debug("Accesses evaluated in "+gap/1000+" milliseconds");

		int numberOfAccess = 0;
		for (Satellite sat : this.satList) {
			/**
			 * Evaluating hoe many accesses have been found
			 */
			numberOfAccess = numberOfAccess + sat.getAccessList().size();
		}

		FeasibilityPerformer.logger.debug("Found " + numberOfAccess + " accesses");
 
		FeasibilityPerformer.logger.debug("number of iterations : "+this.numberOfOuterIteration);
		
		if(numberOfAccess<5000)
		{
			this.numberOfOuterIteration = this.numberOfOuterIteration*20;
			FeasibilityPerformer.logger.debug("number of iterations * 20 : "+this.numberOfOuterIteration);

		}else if(numberOfAccess<10000)
		{
			this.numberOfOuterIteration = this.numberOfOuterIteration*10;
			FeasibilityPerformer.logger.debug("number of iterations * 10 : "+this.numberOfOuterIteration);

		}
		else if (numberOfAccess<15000)
		{
			this.numberOfOuterIteration = this.numberOfOuterIteration*2;
			FeasibilityPerformer.logger.debug("number of iterations * 2 : "+this.numberOfOuterIteration);

		}
		FeasibilityPerformer.logger.debug("number of iterations updated : "+this.numberOfOuterIteration);

		/**
		 * Evaluating base solution and invoking sparc, if is enabled
		 */
		AlgoRetVal retVal = optimalAcqReqList(request, stopValidityTime, isSpotLight, false);
		OptimizationAlgoInterface algo = retVal.getAlgo();

		/**
		 * Retrieving optimal ar list
		 */
		List<AcqReq> optimalAcqList = retVal.getAcquisitionRequestList();
		
		logger.debug("MODIFICA doFeasibility optimalAcqList returned as optimal " + optimalAcqList.size());



//		List<Polygon> optimalAcqListPoly = createPolygonList(optimalAcqList);
//		boolean pRDateLineBool = false;
//		// modifica per filtrare le dto in overlap, attivabile con parametro booleano in
//		// file di configurazione
//		intersectARs(optimalAcqListPoly, pRDateLineBool);

		logger.debug("processOverlap ACTIVE");
		
		optimalAcqList = this.gridder.processOverlap(optimalAcqList);
		
		

		logger.debug("MODIFICA doFeasibility optimalAcqList returned as optimal after processOverlapSIZE :" + optimalAcqList.size());

		/**
		 * performing repetivitve periodic request
		 *
		 */
		if (request.isRepetitive() || request.isPeriodic()) {
			/**
			 * Checking if base solution is empty or doesn't meet coverage requirements
			 */
			if ((optimalAcqList.size() == 0) || (!algo.isSingleAcquired()
					&& getPrStatusString(getUsedCoverage(optimalAcqList), request.getRequiredPercentageOfCoverage())
							.equals(FeasibilityConstants.FailedStatusString))) {
				/**
				 * Empty list no task performed
				 */
				fillPRStatusWithError(FeasibilityConstants.FullErrorStringForPeriodicRepetitive);
			} // end optimalAcqList.size()==0
			else {
				/**
				 * Performing task
				 */
				boolean success = performRepetitivePeriodicTasks(request, startValidityTime, upperTimeTorequest,
						stopValidityTime, isSpotLight);
				if (success) {
					/**
					 * Success dumping base solution
					 */
					FeasibilityPerformer.logger.debug("Adding response to XML");
					addResponseToXML(optimalAcqList, request, algo.isSingleAcquired());
					logOverlapFactor(optimalAcqList, algo.isSingleAcquired());
				} else {
					/**
					 * Failed
					 */
					fillPRStatusWithError(FeasibilityConstants.ErrorOnNextRanges);
				} // end else success
			} // end else optimalAcqList.size()==0
		} // end on repetitive
		else // standard request
		{
			/**
			 * No periodic solution
			 */
			/**
			 * If solution is not empty
			 *
			 */
			if (optimalAcqList.size() != 0) {
				FeasibilityPerformer.logger.debug("Adding response to XML");
				// Check foer coverage
				if (!algo.isSingleAcquired()
						&& getPrStatusString(getUsedCoverage(optimalAcqList), request.getRequiredPercentageOfCoverage())
								.equals(FeasibilityConstants.FailedStatusString)) {
					/**
					 * If control on coverage fails
					 */
					fillPRStatusWithError("The solution does not meet the coverage requirements");
				} // end if
				else {
					addResponseToXML(optimalAcqList, request, algo.isSingleAcquired());
					logOverlapFactor(optimalAcqList, algo.isSingleAcquired());
				}

			} // end if optimalAcqList.size()!=0
			else {
				/**
				 * The solution is empty
				 *
				 */
				fillPRStatusWithError("No AR found");
			}
		} // end else on periodic

	}// end doFeasibility

	/**
	 * Creates the polygon list.
	 *
	 * @param optimalAcqList the optimal acq list
	 * @return the list
	 */
	private List<Polygon> createPolygonList(List<AcqReq> optimalAcqList) {
		List<Polygon> polyList = new ArrayList<Polygon>();
		LineGridder lineGrid = new LineGridder();
		for (int i = 0; i < optimalAcqList.size(); i++) {
			polyList.add(lineGrid.getPolygonFromAR(optimalAcqList.get(i)));
		}
		return polyList;
	}

//    /**
//     * Get the polygonal AOI
//     * 
//     * @param pRAoI
//     *            - the PR Area Of Interest
//     * @return
//     */
//    private Geometry getPolygon(AreaOfInterestType pRAoI) {
//
//        /**
//         * Get the GML Polygon AOI
//         */
//        polygon = pRAoI.getPolygon();
//
//        /**
//         * Check of the PR AoI GML attributes: - ID (mandatory in ProgReq
//         * validation); - SRS_NAME: Reference Frame (EPSG:4326); - SRS_DIM:
//         * WGS-84 LonLat parameters ("2");
//         */
//        if (!polygon.isSetSrsDimension()) {
//
//            LOG.error("SRS Dimension Attribute of the PR AoI is not set " + //$NON-NLS-1$
//                    "- SRS_DIM shall mandatorily be equal to " + SRS_DIM); //$NON-NLS-1$
//
//            pRFeaBool = false;
//
//        } else if (polygon.getSrsDimension().intValue() != SRS_DIM) {
//
//            LOG.error("SRS Dimension Attribute of the PR AoI is wrong " + //$NON-NLS-1$
//                    "- SRS_DIM shall mandatorily be equal to " + SRS_DIM); //$NON-NLS-1$
//
//            pRFeaBool = false;
//        }
//
//        if (!polygon.isSetSrsName()) {
//
//            LOG.error("SRS Name Attribute of the PR AoI is not set " + //$NON-NLS-1$
//                    "- SRS_NAME shall be mandatorily: " + SRS_NAME); //$NON-NLS-1$
//
//            pRFeaBool = false;
//
//        } else if (!polygon.getSrsName().contains(SRS_NAME)) {
//
//            LOG.error("SRS Name Attribute of the PR AoI is wrong " + //$NON-NLS-1$
//                    "- SRS_NAME shall be mandatorily: " + SRS_NAME); //$NON-NLS-1$
//
//            pRFeaBool = false;
//        }
//
//        /**
//         * If Feasibility is allowed
//         */
//        if (pRFeaBool) {
//
//            abRingProp = polygon.getExterior();
//
//            linRing = (LinearRingType) abRingProp.getAbstractRing();
//
//            dirPosList = linRing.getPosList();
//
//            /**
//             * The number of elements of the posList and the coordinates array
//             */
//            pRPosListElemNumber = dirPosList.getListValue().size();
//
//            pRPosListNumber = pRPosListElemNumber / SRS_DIM;
//
//            if (pRPosListNumber == pRPosListElemNumber / (double) SRS_DIM) {
//
//                /**
//                 * List of JTS PR coordinates wrt the posList properties
//                 * (SRS_DIM, SRS_NAME)
//                 */
//                pRCoordElems = new double[SRS_DIM];
//
//                pRCoords = new Coordinate[pRPosListNumber];
//
//                /**
//                 * PosList to Coordinate[] cycle: posList is yet an array with
//                 * pos[0] = pos[pRPosListNumber]
//                 */
//                for (int i = 0; i < pRPosListNumber; i++) {
//
//                    for (int j = 0; j < SRS_DIM; j++) {
//
//                        coordInd = SRS_DIM * i + j;
//
//                        pRCoordElems[j] = (double) dirPosList.getListValue()
//                                .get(coordInd);
//                    }
//
//                    /**
//                     * Read data as [Lon, Lat] from [Lat, Lon] - for external
//                     * interfaces messages
//                     */
//                    pRCoords[i] = new Coordinate(pRCoordElems[1], pRCoordElems[0]);
//
//                }
//                /**
//                 * Equalize first and last coordinates
//                 */
//                if (pRCoords[0].equals(pRCoords[pRPosListNumber - 1])) {
//
//                    aoIGeometry = pRGeomFactory.createPolygon(pRCoords);
//
//                } else {
//
//                    /**
//                     * Wrong coordinates implementation
//                     */
//                    LOG.error("Bad input of the PR AoI GML coordinates."); //$NON-NLS-1$
//
//                    pRFeaBool = false;
//
//                }
//
//            } else {
//
//                /**
//                 * Wrong coordinates implementation
//                 */
//                LOG.error("Bad input of the PR AoI GML coordinates."); //$NON-NLS-1$
//
//                pRFeaBool = false;
//            }
//
//        } else {
//
//            /**
//             * Wrong attributes implementation
//             */
//            LOG.error("Bad input of the PR AoI GML attributes."); //$NON-NLS-1$
//
//            pRFeaBool = false;
//        }
//
//        return aoIGeometry;
//
//    }

	/**
	 * Compute the intersection of ARs basic polygons with the PR AOI [from 3D (NWW)
	 * to 2D (JTS) analysis] where the PR AOI is the first polygon of the polygon
	 * input array.
	 * 
	 * Output: ARs polygons intersecting the PR AOI in LonLat frame.
	 * 
	 * 
	 * @param polygonList    - the list of AcqReqs polygons to be intersected
	 * @param aRLoxoAng      - the loxodromic angles of the AcqReq
	 * @param pRDateLineBool - the dateline crossing boolean
	 * @return the AcqReqs intersection polygons with the PR aOI
	 * @throws FeasibilityException
	 */
	public List<Polygon> intersectARs(List<Polygon> polygonList, boolean pRDateLineBool) {

		List<Polygon> intPolygonList = new ArrayList<Polygon>();
		PrecisionModel precMod = new PrecisionModel(PrecisionModel.maximumPreciseValue);
		GeometryFactory pRGeomFactory = new GeometryFactory(precMod, 2);
		try {

			/**
			 * Instance the lists
			 */

			List<Geometry> intPointGeomList = new ArrayList<Geometry>();

			/**
			 * set the input PR AOI geometry
			 */
			Geometry aOIGeom = pRGeomFactory.createGeometry(polygonList.get(0));

			/**
			 * Set output ARs polygons arrays
			 */
			int aRNum = polygonList.size() - 1;

			for (int i = 0; i < aRNum; i++) {

				/**
				 * In the 2D LoxoFrame for limiting the ARs length, the following operations are
				 * needed: Intersection between aOIGeometry and reqLatLonPolygons
				 */
				/**
				 * Set input ARs Polygonal geometries
				 */
				Geometry intGeom = pRGeomFactory.createGeometry(polygonList.get(i + 1));

				/**
				 * The resulting intersection represents the geometry comprehending all the
				 * vertices of the PR polygon by intersection of the i-th AR polygon.
				 * Multypolygons intersections are splitted
				 */
				Geometry tempIntGeom = intGeom.intersection(aOIGeom);

				if (!(tempIntGeom.isEmpty()) && (tempIntGeom.toText().contains("POLYGON"))) {

					intPointGeomList.add(tempIntGeom);

					/**
					 * Create and collect the resultant ARs Polygons
					 */
					for (int j = 0; j < intPointGeomList.get(i).getNumGeometries(); j++) {

						if (intPointGeomList.get(i).getGeometryN(j).toText().contains("POLYGON")
								&& !intPointGeomList.get(i).getGeometryN(j).isEmpty()) {

							intPolygonList.add(pRGeomFactory
									.createPolygon(intPointGeomList.get(i).getGeometryN(j).getCoordinates()));
						}
					}
				}
			}

		} catch (Exception ex) {

			DateUtils.getLogInfo(ex, logger);
		}

		return intPolygonList;
	}

	/**
	 * Perform trepetitive periodic
	 *
	 * @param request
	 * @param startValidityTime
	 * @param upperTimeTorequest
	 * @param stopValidityTime
	 * @return true if the solutions exist for all the ranges
	 * @throws Exception
	 */
	private boolean performRepetitivePeriodicTasks(PRRequestParameter request, double startValidityTime,
			double upperTimeTorequest, double stopValidityTime, boolean isSpotLight) throws Exception {
		boolean boolretval = true;
		// maybe ????
		// We do not search for optimal solution, but just for a solution, so we
		// do not need to perform
		// more than one outer loop and
		// search for holes
		this.numberOfOuterIteration = 1;
		this.haveCheckForHoles = false;

		this.tracer.information(EventType.LOG_EVENT, ProbableCause.INFORMATION_INFO,
				"Performing Repetitive Periodic tasks");

		/**
		 * retrieving repetitive periodic information
		 */
		int numberOfPeriod = request.getPeriodicIteration();
		int numberOfRepetition = request.getRepetitiveIteration();
		int periodicGranularity = request.getPeriodicGranularity();
		int repetitiveGranularity = request.getRepetitiveGranularity();

		/**
		 * we can consider the periodic as the base case for repetitive
		 */
		if (numberOfRepetition == 0) {
			numberOfRepetition = 1;
		}

		// this flag is set to trough if the timeline is fully based on odref
		// boolean isTimelineFullyODREF = false;

		/**
		 * Iterating on repetitive
		 */
		for (int k = 0; k < numberOfRepetition; k++) {
			this.tracer.log("Performing Repetition " + k);
			/**
			 * Necessary beacse at the first repetition already include the base solution
			 */
			int statringIndexPeriod = 0;
			int stopIndexPeriod = numberOfPeriod;
			if (k == 0) {
				// The base solution is already evaluated so in the first
				// repetoitive interval
				// we have start from the second period
				statringIndexPeriod++;
				// stopIndexPeriod++;

			} // end if

			/**
			 * Iterating on period
			 */
			for (int i = statringIndexPeriod; i < stopIndexPeriod; i++) {

				// poòòpo

				int newdelta = (i * periodicGranularity) + (k * repetitiveGranularity);

				this.tracer.log("Performing periodic feasibility starting at: "
						+ DateUtils.fromCSKDateToISOFMTDateTime(startValidityTime + newdelta));
				// isTimelineFullyODREF = rebuildSatList(request, startValidityTime + newdelta,
				// upperTimeTorequest + newdelta, stopValidityTime + newdelta);

				AccessesEvaluator eval = new AccessesEvaluator();
				eval.evaluateSatelliteAccesses(this.satList, this.gridPointList, request);
				AlgoRetVal retVal = optimalAcqReqList(request, stopValidityTime + newdelta, isSpotLight, true);
				OptimizationAlgoInterface algo = retVal.getAlgo();
				List<AcqReq> optimalAcqList = retVal.getAcquisitionRequestList();

				/**
				 * if single acquired we have a solution
				 */
				if (!algo.isSingleAcquired()) {
					if ((optimalAcqList.size() == 0) || getPrStatusString(getUsedCoverage(optimalAcqList),
							request.getRequiredPercentageOfCoverage())
									.equals(FeasibilityConstants.FailedStatusString)) {
						/**
						 * Failed
						 *
						 */
						boolretval = false;
						this.tracer.log("current periodic step failed");
						/**
						 * we have exit
						 */
						break;

					} // end if
					/*
					 * THis should not be performed because off paw and pass plan. So we do not
					 * assume that a solution exists else { //check if solution is based on ODREF
					 * and if the granularity equals the ODREF repetition //time all the rest of
					 * periodic solution are the same of the one just found //so we can exit the
					 * cycle positively if(isTimelineFullyODREF &&
					 * (periodicGranularity==FeasibilityConstants. RepetitionODREFPeriod)) { break;
					 * } }//end else
					 */
				} // end if

			} // end for (periodic)
				// Failed
			if (!boolretval) {

				break;
			}
			/*
			 * THis should not be performed because off paw and pass plan. So we do not
			 * assume that a solution exists else { //check if solution is based on ODREF
			 * and if the granularity equals the ODREF repetition //time all the rest of
			 * repetition solution are the same of the one just found //so we can exit the
			 * cycle positively if(isTimelineFullyODREF &&
			 * (repetitiveGranularity==FeasibilityConstants. RepetitionODREFPeriod)) {
			 * break; } }//end else
			 */

		} // end for (repetitive)

		return boolretval;
	}// end performRepetitivePeriodicTasks

	/**
	 * In periodic request is necessary to duplicate the satellite list in order to
	 * not modify the base solution. More over it returns true if the satellite
	 * timeline is based on ODREF
	 *
	 * @param request
	 * @param startValidityTime
	 * @param upperTimeTorequest
	 * @param stopValidityTime
	 * @return true if the timeline is based on ODREF
	 * @throws Exception
	 */
	private boolean rebuildSatList(PRRequestParameter request, double startValidityTime, double upperTimeTorequest,
			double stopValidityTime) throws Exception {
		/**
		 * By default odref based
		 */
		boolean retval = true;

		FeasibilityPerformer.logger.debug("Rebuilding sat list for periodic run");
		ArrayList<Satellite> newList = new ArrayList<>();
		/**
		 * Filling the new satellite list
		 */
		for (Satellite s : this.satList) {
			Satellite newSat = new Satellite(s.getUnderlyingSatelliteBean());
			newSat.setBeams(s.getBeams());
			newList.add(newSat);

		}

		this.satList = newList;

		/**
		 * Fill satellite epochs paw and passplan
		 */
		filleSatListWithEphemerid(request, startValidityTime, upperTimeTorequest, stopValidityTime);

		/**
		 * check if the full timeline is based on ODREF
		 */
		for (Satellite s : this.satList) {
			if (s.getEpochs().size() != 0) {
				if (s.getEpochs().get(0).getDataType() != FeasibilityConstants.OdrefType) {
					/**
					 * in this case at least one satellite has not odref epochs
					 */
					retval = false;
					/**
					 * We haev exit
					 */
					break;
				}
			} // end if
		} // end for

		return retval;

	}// end rebuildSatList

	/**
	 * Log the overlap factor (is required!!!!!!)
	 *
	 * @param optimalAcqList
	 * @param isSingle
	 */
	private void logOverlapFactor(List<AcqReq> optimalAcqList, boolean isSingle) {
		try {
			/**
			 * By default is 0
			 */
			double overlapFactor = 0.0;
			if (!isSingle) {
				/**
				 * IN case of not single acquired we evaluate overlap
				 */
				overlapFactor = this.gridder.getOverlapFactor(optimalAcqList);
			}
			this.tracer.log("Overlap factor: " + String.format(Locale.US, "%.3f", overlapFactor));
		} catch (Exception e) {
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.SOFTWARE_ERROR, e.getMessage());
		}
	}// end logOverlapFactor

	/**
	 * Try to remove AR that can cause conflict in Interferometric missions with
	 * only one satellite
	 *
	 * @param optmalList
	 * @return free conflict list
	 */
	protected List<AcqReq> removeConflictInInteferometricWithSingleSatellite(List<AcqReq> optmalList) {
		FeasibilityPerformer.logger.debug("Removing conflicting AR for interferometry");
		List<AcqReq> retList = new ArrayList<>();

		List<AcqReq> appoList = new ArrayList<>();

		for (AcqReq a : optmalList) {
			appoList.add(a);
		} // end for

		for (AcqReq a : optmalList) {
			if (a.getDTOList().size() != 0) {
				retList.add(a);
				for (AcqReq aref : appoList) {
					if (!a.getId().equals(aref.getId())) // not the same AR
					{
						removeConflictingDTOInInterferometric(a, aref);
					} // end if
				} //
			} // end if
		} // end for

		return retList;
	}// end removeConflictInInteferometricWithSingleSatellite

	/**
	 * Eliminates possible conflict between two AR in case of interferomtric with
	 * one satellite
	 *
	 * @param base
	 * @param second
	 */
	protected void removeConflictingDTOInInterferometric(AcqReq base, AcqReq second) {
		FeasibilityPerformer.logger.debug("Removing conflicting DTO for interferometry");
		ArrayList<DTO> dtoList = new ArrayList<>();

		DTO baseDTO = base.getDTOList().get(0);
		double decorrelationLimits = this.decorrelationTime + this.decorrelationTolerance;
		// we have check on the possible conflict pervoius and after dto base
		// validity
		double leftBttomLimit = baseDTO.getStopTime() - decorrelationLimits;
		double leftUpperLimit = baseDTO.getStartTime() - decorrelationLimits;
		double rightBottomLimit = baseDTO.getStartTime() + decorrelationLimits;
		double rightUpperLimit = baseDTO.getStopTime() + decorrelationLimits;

		double start = 0;
		double stop = 0;

		// boolean freeconflictLeft=false;
		// boolean freeconflictRight =false;

		for (DTO d : second.getDTOList()) {
			start = d.getStartTime();
			stop = d.getStopTime();

			if (((stop < rightBottomLimit) || (start > rightUpperLimit))
					&& ((stop < leftBttomLimit) || (start > leftUpperLimit))) {
				dtoList.add(d);
			} // end uif

		} // end for

		second.setDTOList(dtoList);

	}// end removeConflictingDTOInInterferometric

	/**
	 * check on paw search for deferreable paw and eliminate dto falling on not
	 * deferreable paw
	 *
	 * @param list
	 * @return
	 */
	protected List<AcqReq> checkPaw(List<AcqReq> list) {
		List<AcqReq> newList = new ArrayList<>();

		for (AcqReq a : list) {
			a.cheCheckForPaw();
			if (a.getDTOList().size() != 0) {
				newList.add(a);
			}

		} // end for

		return newList;
	}// end method

	/**
	 * Retrieve the list of AR
	 *
	 * @param request
	 * @param stopValidityTime
	 * @param isSpotLight
	 * @param isPerioicRun
	 * @return
	 * @throws GridException
	 * @throws FeasibilityException
	 * @throws IOException
	 */
	protected AlgoRetVal optimalAcqReqList(PRRequestParameter request, double stopValidityTime, boolean isSpotLight,
			boolean isPerioicRun) throws GridException, FeasibilityException, IOException {

		AlgoRetVal retval = null;

		try {
			/*
			 * eventual error message
			 */
			String errorMessage = null;
			/*
			 * Checking for interferoteric
			 */
			this.isInterferometric = request.isInterferometric();

			/*
			 * build the structure for interferometric request
			 */
			if (this.isInterferometric) {
				manageInterferometricRequest(request);
			} // end if

			OptimizationAlgoInterface algo = null;
			/**
			 * Perform optimization algo
			 */
			if (request.getRequestedOrbitDirection() == FeasibilityConstants.AnyOrbitDirection) {
				/**
				 * Case any direction
				 */
				// logger.info("Any orbit");
				algo = performOptimizationForAnyOrbitDirection(this.satList, this.gridPointList, isSpotLight, request);
			} else {
				/**
				 * Not Any direction
				 */
				algo = performOptimization(this.satList, this.gridPointList, isSpotLight, request);
			}

			List<AcqReq> optimalAcqList = algo.getOptimalAcqReqList();

			logger.debug("RETURNED ACQlIST " + optimalAcqList);
			// check if some dtos fall inside paw
			optimalAcqList = checkPaw(optimalAcqList);

			if (optimalAcqList.size() == 0) {
				/**
				 * exit in case of empty AR List
				 */
				return new AlgoRetVal(optimalAcqList, algo);
			}

			boolean needExpansion = (!algo.isSingleAcquired()
					|| (algo.isSingleAcquired() && this.haveOptimizeTimeLine));
			/**
			 * Building Interferometric
			 *
			 */
			if (this.isInterferometric) {
				logger.debug("isInterferometric! optimalAcqList before " + optimalAcqList);

				// Espando
				if (needExpansion) {
					logger.debug("Expanding DTO case interferometric");
					for (AcqReq a : optimalAcqList) {
						a.expandDTOList(stopValidityTime, algo.isSingleAcquired());
						a.cheCheckForPaw();
					} // end for
				} // end if

				// in case of single satellite have to eliminate conflict
				if (this.firstInterferometricSatellite.getName()
						.equals(this.secondInterferometricSatellite.getName())) {
					optimalAcqList = removeConflictInInteferometricWithSingleSatellite(optimalAcqList);
				} // end if
				else {
					// reordering accesses against access time
					this.secondInterferometricSatellite.getAccessList().sort(new AccessComparatorByTime());
				} // end else
				addingLinkedDTO(optimalAcqList, stopValidityTime);
				if (optimalAcqList.size() == 0) {
					/**
					 * Empty have exit
					 */
					return new AlgoRetVal(optimalAcqList, algo);
				} // ennd if

				logger.debug("isInterferometric! optimalAcqList after " + optimalAcqList);

			}

			// if(this.haveCheckForHoles &&!isSpotLight &&
			if (this.haveCheckForHoles &&
			// algo.getUncoveredNumberOfPoints()==0 &&
					!this.isInterferometric && !algo.isSingleAcquired()
					&& ((this.gridder.getCoverage(optimalAcqList) * 100) < request.getRequiredPercentageOfCoverage())) {
				logger.debug("haveCheckForHoles! optimalAcqList before " + optimalAcqList);

				/**
				 * Check for holes
				 */
				HolesPatcher holePatcher = new HolesPatcher(optimalAcqList, request, this.gridder, this.satList);
				optimalAcqList = holePatcher.checkForHoles();
				optimalAcqList = checkPaw(optimalAcqList);

				logger.debug("haveCheckForHoles! optimalAcqList after " + optimalAcqList);

			} // end if

			/**
			 * Evaluating suf
			 */
			evaluateSUF(optimalAcqList);

			/**
			 * expand DTO and perform paw check
			 */
			/**
			 *
			 * In case of single acq it doesn't make sense exapnd DTO cause all the DTO are
			 * already evaluated. Anyway if the optimization of timeline has been performed
			 * then more DTO are likely to be evaluated
			 */
			// boolean needExpansion=(!algo.isSingleAcquired() ||
			// (algo.isSingleAcquired() && this.haveOptimizeTimeLine));
			if (!this.isInterferometric && !isPerioicRun && needExpansion) {

				for (AcqReq a : optimalAcqList) {
					logger.debug("Trying to expand DTO for ar: " + a.getId());
					a.expandDTOList(stopValidityTime, algo.isSingleAcquired());
					a.cheCheckForPaw();
					/*
					 * Pass through allowed only for CSG request, and the check is possible only
					 * after sparc invovcation if(request.isPassThrough()) { a.checkForPassPlan(); }
					 */
				} // end for

				// Check tath after paw check the ar list are still valid

				List<AcqReq> usabelAcqList = new ArrayList<>();

				/**
				 * use only AR with not empty DTOLIST
				 */
				for (AcqReq a : optimalAcqList) {
					if (a.getDTOList().size() != 0) {
						usabelAcqList.add(a);

					} else {
						logger.debug("IMPOSSIBLE CASE, NO DTO FOUND FOR ACQrEQ " + a.getId());
					}
				}

				optimalAcqList = usabelAcqList;

				logger.debug("optimal after no dto acqReq removed " + optimalAcqList);

				/**
				 * it should never happen
				 */
				if (optimalAcqList.size() == 0) {

					return new AlgoRetVal(optimalAcqList, algo);
				}

				// Resolve conflict on expanded DTO
				if (this.haveCheckForConflict) {
					logger.debug("RESOLVE CONFLICT !");
					resolveConflict(optimalAcqList);
				}

			} // end if for exansion

			/**
			 * Using sparc
			 */
			logger.debug("BEFORE SPARC! optimalAcqList before " + optimalAcqList);
			for(int i=0;i<optimalAcqList.size();i++)
			{
				ArrayList<DTO> listDto = optimalAcqList.get(i).getDTOList();
				for(int j=0;j<listDto.size();j++)
				{
					DTO dto = listDto.get(j);
					double gap = dto.getStopTime()-dto.getStartTime();
					logger.debug("BEFORE SPARC!  DTO BEAM "+dto.getBeam().getBeamName());
					logger.debug("BEFORE SPARC!  DTO DURATION :"+DateUtils.fromCSKDurationToMilliSeconds(gap));
				}
			}

			int sparcMode = this.configuredSparcMode;
			/**
			 * SPARC not used in case of CSK mission or configuration (For test only)
			 */
			if (this.haveUseSparc && !request.getMission().equals(FeasibilityConstants.CSK_NAME)) {
				logger.debug("INVOKING SPARC! optimalAcqList before SIZE " + optimalAcqList.size());

				logger.debug("INVOKING SPARC! optimalAcqList before " + optimalAcqList);
				double coverage = getUsedCoverage(optimalAcqList);

				logger.debug("INVOKING SPARC! coverage before sparc " + coverage);

				/**
				 * Perform SPARC Evaluation
				 */
				SPARCManager sparc = new SPARCManager(sparcMode, request, optimalAcqList, this.prListWorkingDir,
						this.dem, algo.isSingleAcquired(), this.gridder.isAcrossDateLine(),
						this.di2SAvailabilityConfirmationFlag);
				
				optimalAcqList = sparc.getAcqList();

				if (request.isPassThrough()) {
					optimalAcqList = checkForPassThrough(optimalAcqList);
				} // end if

				logger.debug("INVOKING SPARC! optimalAcqList after SIZE" + optimalAcqList.size());
				logger.debug("INVOKING SPARC! optimalAcqList after " + optimalAcqList);

				coverage = getUsedCoverage(optimalAcqList);
				logger.debug("INVOKING SPARC! coverage after sparc " + coverage);

				for(int i=0;i<optimalAcqList.size();i++)
				{
					ArrayList<DTO> listDto = optimalAcqList.get(i).getDTOList();
					for(int j=0;j<listDto.size();j++)
					{
						DTO dto = listDto.get(j);
						double gap = dto.getStopTime()-dto.getStartTime();
						logger.debug("AFTER SPARC!  DTO BEAM "+dto.getBeam().getBeamName());
						logger.debug("AFTER SPARC!  DTO DURATION :"+DateUtils.fromCSKDurationToMilliSeconds(gap));
					}
				}
			} // end if

			/**
			 * Performing stereo post processing
			 */
			if (request.isStereo()) {
				logger.debug("perform stereopair! optimalAcqList before " + optimalAcqList);

				if (!algo.isSingleAcquired() && (optimalAcqList.size() != 0)) {
					/**
					 * Stereo allowed only on single acquired
					 */
					optimalAcqList.clear();
					this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO,
							"StereoPair Request not single acquirable");
					errorMessage = "StereoPair Request not single acquirable";

				} // end if
				else if (optimalAcqList.size() != 0) {
					/**
					 * Evaluating stereo
					 */
					optimalAcqList = performStereoPairPostProcessing(optimalAcqList, request);
					if (optimalAcqList.size() == 0) {
						/**
						 * No stereo found
						 */
						errorMessage = "Unable to find coupled DTO for steropair request";
					}
				} // end else
				logger.debug("perform stereopair! optimalAcqList after " + optimalAcqList);

			} // end if

			retval = new AlgoRetVal(optimalAcqList, algo);
			if (errorMessage != null) {
				/**
				 * Setting error messages
				 */
				retval.setErrorMessage(errorMessage);
			} // end if
		} // end try
		catch (Exception e) {

			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.SOFTWARE_ERROR,
					" anormal ending of SPRC " + e.getMessage());
			throw new FeasibilityException("Anormal ending of SPRC " + e.getMessage());

		}

		return retval;

	}// end optimalAcqReqList

	/**
	 * Perform check on passThrough
	 *
	 * @param arList
	 */
	private ArrayList<AcqReq> checkForPassThrough(List<AcqReq> arList) {
		/**
		 * List to be retuned
		 */
		ArrayList<AcqReq> newOptimalList = new ArrayList<>();

		for (AcqReq acq : arList) {
			/**
			 * Check on each DTO and discard DTO if not inside a pass plan visibility
			 */
			acq.checkForPassPlan();
			if (acq.getDTOList().size() != 0) {
				newOptimalList.add(acq);
			}
		} // end for

		return newOptimalList;
	}// end method

	/**
	 * Perform the stereo post processing
	 *
	 * @param arList
	 * @param pr
	 * @return
	 * @throws GridException
	 */
	private List<AcqReq> performStereoPairPostProcessing(List<AcqReq> arList, PRRequestParameter pr)
			throws GridException {
		ArrayList<AcqReq> optimalList = new ArrayList<>();

		this.tracer.debug("Performing stereo pair post processing");

		if (arList.size() != 0) {
			/**
			 * Sorting DTO
			 */
			AcqReq arMaster = arList.get(0);
			arMaster.getDTOList().sort(new DTOComparatorByTime());

			// reindex
			// ArrayList<DTO> dtoList = new ArrayList<>();

			/**
			 * reindexing dto
			 */
			int i = 1;
			for (DTO d : arMaster.getDTOList()) {
				d.setId("" + i);
				i++;
			}

			/**
			 * Setting true the AR flag master
			 */
			arMaster.setStereoMaster(true);
			/**
			 * Set 1 the id of AR Master
			 */
			arMaster.setId("1");

			/**
			 * Cloning AR Master
			 */
			AcqReq arSlave = arMaster.clone();

			/**
			 * Set two the AR SLave
			 */
			arSlave.setId("2");

			/**
			 * Linking AR
			 */
			arMaster.setLinkedAR(arSlave);
			arSlave.setLinkedAR(arMaster);

			/**
			 * Coupling DTO
			 */
			coupleStereoDTOs(arMaster, arSlave, pr);
			if (arMaster.isStereoLink() && arSlave.isStereoLink()) {
				optimalList.add(arMaster);
				optimalList.add(arSlave);
			} // end if

		}

		return optimalList;
	}// end performStereoPairPostProcessing

	/**
	 * Couple the DTOs of the master and slave Ar according to the specief
	 * constraintcoupleStereoDTOs
	 *
	 * @param arMaster
	 * @param arSlave
	 * @param request
	 * @throws GridException
	 */
	private void coupleStereoDTOs(AcqReq arMaster, AcqReq arSlave, PRRequestParameter request) throws GridException {

		// search couple

		// for(DTO master : arMaster.getDTOList())

		ArrayList<DTO> dtoMasteList = arMaster.getDTOList();
		ArrayList<DTO> dtoSlaveList = arSlave.getDTOList();

		/**
		 * For each dto in master
		 */
		for (int i = 0; i < (dtoMasteList.size() - 1); i++) {
			DTO master = dtoMasteList.get(i);
			/**
			 * Used to evaluate check on stereo coverage
			 */
			PolygonGridder masterDTOgridder; // used to check coverage
			if (request.isPolarRequest()) {

				masterDTOgridder = new PolarPolygonGridder(master.getPosListString(), this.dem, this.gridSpacing);
			} else {
				masterDTOgridder = new PolygonGridder(master.getPosListString(), this.dem, this.gridSpacing);
			}

			// for(DTO slave : arSlave.getDTOList())
			/**
			 * Iterating on slaves
			 */
			for (int j = (i + 1); j < dtoSlaveList.size(); j++) {
				DTO slave = dtoSlaveList.get(j);
				/**
				 * Check if the slave is stereo with the current master
				 */
				if (areDTOStereoPair(master, slave, request, masterDTOgridder)) {
					/**
					 * a couple has been find
					 *
					 */
					master.setStereoLincked(true);
					slave.setStereoLincked(true);
					master.addDTOToStereoSlaveList(slave);
					slave.addDTOToStereoMasterList(master);
					arMaster.setStereoLink(true);
					arSlave.setStereoLink(true);

				} // end if
			} // end for

		} // end for

		/**
		 * delete DTO not linked in master
		 */
		ArrayList<DTO> masterList = new ArrayList<>();
		for (DTO d : arMaster.getDTOList()) {
			if (d.isStereoLincked()) {
				masterList.add(d);
			}
		} // end for

		arMaster.setDTOList(masterList);

		/**
		 * delete not linked dto in slave
		 */
		ArrayList<DTO> slaveList = new ArrayList<>();

		for (DTO d : arSlave.getDTOList()) {
			if (d.isStereoLincked()) {
				slaveList.add(d);
			}
		} // end for

		arSlave.setDTOList(slaveList);
		/**
		 *
		 *
		 *
		 */
		// fa qulcosa
	}// end coupleStereoDTO

	/**
	 * check if two dto have a master slave relation return true if so
	 *
	 * @param master
	 * @param slave
	 * @param request
	 * @param masterDTOgridder
	 * @return
	 * @throws GridException
	 */
	boolean areDTOStereoPair(DTO master, DTO slave, PRRequestParameter request, PolygonGridder masterDTOgridder)
			throws GridException {
		double deltaAngle = request.getStereoDeltaAngle();
		double alfaMin = request.getStereoMinAngle();
		double alfaMax = request.getStereoMaxAngle();
		int requestedLookSide = request.getRequestedLookSideCSG();
		boolean coupabale = true;

		/**
		 * check if the two dto are the same:
		 */
		if (master.getId().equals(slave.getId())) {
			coupabale = false;
		}

		/**
		 * check for look side
		 *
		 */
		if (coupabale) {
			/**
			 * If look side of request is both stereo must have different side
			 */
			if ((requestedLookSide == FeasibilityConstants.BothLookSide)
					&& (master.getLookSide() == slave.getLookSide())) {
				coupabale = false;
			} // end if
		}

		// check orbit
		if (coupabale) {
			/**
			 * Stereo must have same ordit direction
			 */
			if (master.getOrditDirection() != slave.getOrditDirection()) {
				coupabale = false;
			}
		} // end if

		/**
		 * check angle
		 */
		if (coupabale) {
			if (deltaAngle != 0) {
				/**
				 * Delta angle
				 */
				double deltaLookSide = Math.abs(master.getLookAngle() - slave.getLookAngle());
				if (deltaLookSide > deltaAngle) {
					coupabale = false;
				}
			} else {
				/**
				 * Slave look angle between alfamin and alfa max
				 */
				double angle = slave.getLookAngle();
				if (!((angle >= alfaMin) && (angle <= alfaMax))) {
					coupabale = false;
				}
			}

		} // end if

		/**
		 * check if dto belongs to same satellite and are not overlapped
		 */
		if (coupabale) {
			if (master.getSatName().equals(slave.getSatName())) {
				double masterStartTime = master.getStartTime();
				double masterStopTime = master.getStopTime();
				double slaveStartTime = slave.getStartTime();
				double slaveStopTime = slave.getStopTime();

				if ((masterStartTime > (slaveStopTime + FeasibilityConstants.ManouvreTolerance))
						|| (masterStopTime < (slaveStartTime - FeasibilityConstants.ManouvreTolerance))) {
					/**
					 * Not overlap
					 */
					coupabale = true;
				} else {
					/**
					 * Overlap
					 */
					coupabale = false;
				}

			} // end if
		} // end if

		/**
		 * check for coverage
		 */
		if (coupabale) {
			double coverage = masterDTOgridder.getDTOCoverage(slave);
			if (coverage < this.minCoverageBetweenStereoPairDto) {
				coupabale = false;
			}

		} // end if

		return coupabale;
	}

	/**
	 * insert the centroid in the gripoint list
	 */
	protected void insertCenterInGridPointList() {
		int size = this.gridPointList.size();
		/**
		 * Only for polygon gridder
		 */
		if (this.gridder instanceof PolygonGridder) {
			GridPoint centroid = ((PolygonGridder) this.gridder).getCentroid();
			if (centroid != null) {
				/**
				 * Assigning id
				 */
				centroid.setId(size);
				this.gridPointList.add(centroid);

			}

		} // end if

	}// end insertCenterInGridPointList

	/**
	 * In interferometric mission link the DTO of second satellite to the first one
	 *
	 * @param optimalAcqList
	 */
	protected void addingLinkedDTO(List<AcqReq> optimalAcqList, double stopValidityTime) {
		FeasibilityPerformer.logger.debug("Searching for interferometric DTO");

		/**
		 * New lists
		 */
		List<AcqReq> toBeRemovedAcq = new ArrayList<>();
		List<AcqReq> toBeAddedAcq = new ArrayList<>();
		// //System.out.println("OPTiSIZE: " +optimalAcqList.size());

		for (AcqReq a : optimalAcqList) {
			/**
			 * Searchinng ACQ interfeometric
			 */
			AcqReq interferometricAcq = a.getinterferometricAcqReq(this.secondInterferometricSatellite,
					this.decorrelationTime, this.decorrelationTolerance, stopValidityTime);

			if (interferometricAcq != null) {
				toBeAddedAcq.add(interferometricAcq);
			} else {
				toBeRemovedAcq.add(a);
			}

		} // end for

		// //System.out.println("TO BE ADDE ACQ: " +toBeAddedAcq.size());

		/**
		 * Removing ACQ to be removed
		 */
		for (AcqReq a : toBeRemovedAcq) {
			optimalAcqList.remove(a);
		}

		/**
		 * Adding acq to be added
		 */
		for (AcqReq a : toBeAddedAcq) {
			optimalAcqList.add(a);
		}

		int i = 1;
		for (AcqReq a : optimalAcqList) {
			/**
			 * Reindexing ACQ
			 */
			a.setId(Integer.toString(i));
			i++;
		}
		// //System.out.println("OPTiSIZE: " +optimalAcqList.size() + " i vale: "
		// +i);
	}// addingLinkedDTO

	/**
	 * Perform the activities requested before the run of an optimization for an
	 * interferometric request
	 *
	 * @throws FeasibilityException
	 */
	protected void manageInterferometricRequest(PRRequestParameter request) throws FeasibilityException {
		// check that the satellite in the list matches teh configured
		// interferometric mission
		// leaves only a satellite in the list of satellite to be used in the
		// optimization algorithm

		this.tracer.log("Managing interferometric request");

		/**
		 * Retrieving interferometric parameters
		 */
		String interferometricMission = getInterferometricMissionString();
		// this.decorrelationTolerance=getDecorrelationTolerance();

		FeasibilityPerformer.logger.debug("Found interferometric configuration: " + interferometricMission);
		/**
		 * Tokeinzing interferometric string
		 */
		StringTokenizer tokens = new StringTokenizer(interferometricMission, ",");

		if (tokens.countTokens() != 2) {
			/**
			 * Wrong string
			 */
			throw new FeasibilityException("Wrong interferometric mission: " + interferometricMission);
		}

		String decorrelation = tokens.nextToken();

		try {
			long dValue = Long.valueOf(decorrelation);
			this.decorrelationTime = DateUtils.secondsToJulian(dValue);
		} catch (Exception e) {
			/**
			 * Malformed decorrelation time
			 */
			// logger.warn("Unable to found " +
			// FeasibilityConstants.MAX_AREA_OF_INTEREST_CONF_KEY + " in
			// conffiguration");
			this.tracer.critical(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Malformed  " + FeasibilityConstants.INTERFEROMETRIC_MISSIONS_CONF_KEY + " in configuration");
			throw new FeasibilityException("Malformed decorrelation time in configuration ");
		}

		// retrieving tolerance
		String tolerance = tokens.nextToken();
		try {
			long dValue = Long.valueOf(tolerance);
			this.decorrelationTolerance = DateUtils.secondsToJulian(dValue);
		} // end try
		catch (Exception e) {
			/**
			 * Malformed decorrelation time
			 */
			// logger.warn("Unable to found " +
			// FeasibilityConstants.MAX_AREA_OF_INTEREST_CONF_KEY + " in
			// conffiguration");
			this.tracer.critical(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Malformed  " + FeasibilityConstants.INTERFEROMETRIC_MISSIONS_CONF_KEY + " in configuration");
			throw new FeasibilityException("Malformed decorrelation time in configuration ");
		} // end catch

		/**
		 * Setting satellite
		 */

	}// end manageInterferometricRequest

	/**
	 * Set the satellite couple for interferometric mission
	 *
	 * @param firstSatName
	 * @param secondSatName
	 * @throws FeasibilityException
	 */
	/*
	 * protected void setSatellitesForInterometricMission(String firstSatName,
	 * String secondSatName) throws FeasibilityException {
	 * this.firstInterferometricSatellite=null;
	 * this.secondInterferometricSatellite=null;
	 *
	 *//**
		 * Searching for interferometric satellites in list
		 */
	/*
	 * for(Satellite s : this.satList) { if(s.getName().equals(firstSatName)) {
	 * this.firstInterferometricSatellite=s; }//end if else
	 * if(s.getName().equals(secondSatName)) {
	 * this.secondInterferometricSatellite=s; }//end else }//end for
	 *
	 * if(firstSatName.equals(secondSatName)) {
	 * this.secondInterferometricSatellite=this.firstInterferometricSatellite;
	 * }//end if
	 *
	 * if(this.firstInterferometricSatellite==null ||
	 * this.secondInterferometricSatellite==null) { throw new
	 * FeasibilityException("Unable to found a couple of satellite for the interferometric request"
	 * ); }//end if
	 *
	 *//***
		 * We pperform feasibility on the first satellite, so w leave only the first
		 * satellite in satlist. The Ar on secondo satelite will be builded in post
		 *//*
			 * this.satList.clear(); this.satList.add(this.firstInterferometricSatellite);
			 *
			 * }//end method
			 */

	/**
	 * Search the interferometric mission in the configuration
	 *
	 * @return the string holding decorrelation time e tolerance
	 * @throws FeasibilityException
	 */
	protected String getInterferometricMissionString() throws FeasibilityException {
		String retval = "";
		/**
		 * Reading interf from configuration
		 */

		this.firstInterferometricSatellite = null;
		this.secondInterferometricSatellite = null;

		if ((this.satList.size() > 2) || (this.satList.size() == 0)) {
			throw new FeasibilityException(
					"In case of interferometric mission you have to specify one or two satellite only");
		} // end if

		// Satellite configuration keys
		String firstInterferometricConf;
		String seconInteferometricConf;

		// Satellites names
		String sat1;
		String sat2;

		// single satellite
		if (this.satList.size() == 1) {
			this.firstInterferometricSatellite = this.satList.get(0);
			this.secondInterferometricSatellite = this.firstInterferometricSatellite;

		} // end if
		else {
			// interferometric with two satellite
			this.firstInterferometricSatellite = this.satList.get(0);
			this.secondInterferometricSatellite = this.satList.get(1);

		} // end else

		this.satList.clear();
		this.satList.add(this.firstInterferometricSatellite);

		sat1 = this.firstInterferometricSatellite.getName();

		sat2 = this.secondInterferometricSatellite.getName();

		firstInterferometricConf = sat1 + "_" + sat2 + "_" + FeasibilityConstants.INTERFEROMETRIC_MISSIONS_CONF_KEY;
		seconInteferometricConf = sat2 + "_" + sat1 + "_" + FeasibilityConstants.INTERFEROMETRIC_MISSIONS_CONF_KEY;

		FeasibilityPerformer.logger.debug("Searching interferometric for: " + firstInterferometricConf);
		String value = PropertiesReader.getInstance().getProperty(firstInterferometricConf);

		if (value != null) {

			retval = value;
		} // end if
		else {
			FeasibilityPerformer.logger.debug("Searching interferometric for: " + seconInteferometricConf);
			value = PropertiesReader.getInstance().getProperty(seconInteferometricConf);
			if (value != null) {
				retval = value;
			} // end if
			else {
				/**
				 * Unabble to find interferometric string in configuration
				 */
				// logger.warn("Unable to found " +
				// FeasibilityConstants.MAX_AREA_OF_INTEREST_CONF_KEY + " in
				// conffiguration");
				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Unable to found " + firstInterferometricConf + " or " + seconInteferometricConf
								+ " in configuration");
				throw new FeasibilityException("Unable to find interferometric mission for interferometric request");
			} // emd else

		} // end else

		// setSatellitesForInterometricMission(sat1, sat2);

		return retval;
	}// end getInterferometricMissionString

	/**
	 * search the decorelation time in the configuration
	 *
	 * @return
	 */
	/*
	 * protected double getDecorrelationTolerance() { double retval =
	 * this.decorrelationTolerance;
	 *//**
		 * Reading decorrelation trolerance from configuration
		 */
	/*
	 * String value
	 * =PropertiesReader.getInstance().getProperty(FeasibilityConstants.
	 * DECORRELATION_TOLERANCE_CONF_KEY); if(value!=null) { try{ long dValue =
	 * Long.valueOf(value); retval=DateUtils.secondsToJulian(dValue); }
	 * catch(Exception e) {
	 *//**
		 * Misconfigured decorrelation tolerance
		 */
	/*
	 * //logger.warn("Unable to found " +
	 * FeasibilityConstants.MAX_AREA_OF_INTEREST_CONF_KEY + " in conffiguration");
	 * this.tracer.warning(EventType.SOFTWARE_EVENT,
	 * ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " +
	 * FeasibilityConstants.DECORRELATION_TOLERANCE_CONF_KEY + " in configuration");
	 *
	 * }
	 *
	 * } else {
	 *//**
		 * Not decorrelation tolerance configured using default
		 *//*
			 * //logger.warn("Unable to found " +
			 * FeasibilityConstants.MAX_AREA_OF_INTEREST_CONF_KEY + " in conffiguration");
			 * this.tracer.warning(EventType.SOFTWARE_EVENT,
			 * ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " +
			 * FeasibilityConstants.DECORRELATION_TOLERANCE_CONF_KEY + " in configuration");
			 *
			 * } return retval; }//end getDecorrelationTolerance
			 */

	/**
	 * Resolve conflict on optimal acqList
	 *
	 * @param acqList
	 */
	protected void resolveConflict(List<AcqReq> acqList) {
		FeasibilityPerformer.logger.debug("Resolving conflict");
		/**
		 * Cicling on AR
		 */
		for (AcqReq req : acqList) {
			if (req.getDTOList().size() > 1) {
				/**
				 * Resolving conflit on a AR
				 */
				resolveConflictForAR(req, acqList);
			}

		}
	}// end resolveConflict

	/**
	 * resolve conflict on single AR
	 *
	 * @param req
	 * @param acqList
	 */
	protected void resolveConflictForAR(AcqReq req, List<AcqReq> acqList) {

		List<AcqReq> tempList = new ArrayList<>();
		/**
		 * Filling templist
		 */
		for (AcqReq acq : acqList) {
			if (req.getId() != acq.getId()) {
				tempList.add(acq);
			}
		}

		for (AcqReq refAcq : tempList) {
			/**
			 * For each ar resolve conflict
			 */
			resolveConflictBetweenTwoAR(req, refAcq);
			;
		}

	}// end resolveConflictForAR

	/**
	 * remove form DTO list of req the DTO conflicting with the refAcq
	 *
	 * @param req
	 * @param refAcq
	 */
	protected void resolveConflictBetweenTwoAR(AcqReq req, AcqReq refAcq) {

		ArrayList<DTO> dtoList = req.getDTOList();
		ArrayList<DTO> validDTOList = new ArrayList<>();
		validDTOList.add(dtoList.get(0));
		/**
		 * Cicling on DTO
		 */
		for (int i = 1; i < dtoList.size(); i++) {
			DTO dto = dtoList.get(i);
			if (!refAcq.dtoConflictWithAR(dto)) {
				/**
				 * If DTO not conflict is valid
				 */
				validDTOList.add(dto);
			}
		}

		/**
		 * Reindexing DTO
		 */
		for (int i = 0; i < validDTOList.size(); i++) {
			validDTOList.get(i).setId("" + (i + 1));
		}

		/**
		 * Setting DTO LIST
		 */
		req.setDTOList(validDTOList);

	}// end resolveConflictBetweenTwoAR

	/**
	 * Check if a specic grid spacing is configured for that sensor mode
	 *
	 * @param sensorMode
	 */
	protected void getGridSpcingPerSensorMode(String sensorMode) {
		/**
		 * Reading grid spacing for sensor mode from configuration
		 */
		String value = PropertiesReader.getInstance().getProperty(sensorMode);
		if (value != null) {
			try {
				double dValue = Double.valueOf(value);
				this.gridSpacing = dValue;
			} catch (Exception e) {
				/**
				 * Malformed Default used
				 */
				// logger.warn("Unable to found " +
				// FeasibilityConstants.MINIMAL_PR_VALIDITY_DURATION_CONF_KEY +
				// " in configuration");
				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Unable to found grid spacing for " + sensorMode + " in configuration, the default value "
								+ this.gridSpacing + " shall be used");

			}

		} else {
			/**
			 * Not configured default used
			 *
			 *
			 */
			// logger.warn("Unable to found " +
			// FeasibilityConstants.MINIMAL_PR_VALIDITY_DURATION_CONF_KEY + " in
			// configuration");
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found grid spacing for " + sensorMode + " in configuration, the default value "
							+ this.gridSpacing + " shall be used");

		}
	}// end getGridSpcingPerSensorMode

	/**
	 * Evaluate the suf of each DTO and AR
	 *
	 * @param optimalAcqList
	 * @throws IOException
	 * @throws FeasibilityException
	 */
	protected void evaluateSUF(List<AcqReq> optimalAcqList) throws IOException, FeasibilityException {
		// calculateSUF(String sensorMode, double startTime, double stopTime,
		// String lookSide, String beam)
		FeasibilityPerformer.logger.debug("Evaluating SUF");

		/**
		 * The AR SUF is the greather of single DTO suf
		 */
		double maxSuf;
		double currentSuf;

		// SUFCalculator sufCalc = new SUFCalculator();
		SUFCalculator sufCalc = SUFCalculator.getInstance();

		/**
		 * For each ACQ
		 */
		for (AcqReq a : optimalAcqList) {
			maxSuf = 0;

			/**
			 * For each DTO
			 */
			for (DTO d : a.getDTOList()) {
				String mission = d.getMissionName();
				try {
					if (mission.equals(FeasibilityConstants.CSK_NAME)) {
						/**
						 * DTO Belongs CSK
						 */
						currentSuf = sufCalc.calculateSUF(d.getBeam().getSensorModeName(), d.getStartTime(),
								d.getStopTime(), FeasibilityConstants.getLookSideString(d.getLookSide()),
								d.getBeamId());
					} else {
						/**
						 * DTO belongs CSG
						 */
						boolean isLeft = false;
						if (d.getLookSide() == FeasibilityConstants.LeftLookSide) // left
																					// look
																					// side
						{
							isLeft = true;// left look side extra cost will be
											// added
						} // end if
						currentSuf = sufCalc.calculateSUF(d.getBeam().getSensorModeName(), d.getStartTime(),
								d.getStopTime(), d.getBeamId(), isLeft);
					}

				} // end try
				catch (Exception e) {
					if (mission.equals(FeasibilityConstants.CSK_NAME)) {
						throw new FeasibilityException("Unable to evaluate BIC for CSK DTO: check configuration files");
					} else {
						throw new FeasibilityException(e.getMessage());
					}

				} // end catch

				d.setSuf(currentSuf);
				if (currentSuf > maxSuf) {
					/**
					 * The SUF of AR is the max of suf of the DTO
					 */
					maxSuf = currentSuf;
				}
			} // end inner for

			/**
			 * Setting SUF
			 */
			a.setSuf(maxSuf);

		} // end outer for

	}// end evaluateSUF

	/**
	 * Perform the activity of optimization and return an OptimizationAlgo object at
	 * the end of the activities
	 *
	 * @param accessList
	 * @param gridPointList
	 * @param isSpotLight
	 * @param requestType
	 * @return ObtimizationAlgo
	 */
	protected OptimizationAlgoInterface performOptimization(List<Satellite> satelliteList,
			List<GridPoint> gridPointList, boolean isSpotLight, PRRequestParameter request) {
		FeasibilityPerformer.logger.debug("Performing optimization algo");
		OptimizationAlgoInterface algo;
		if (request.getRequestProgrammingAreaType() != PRRequestParameter.pointRequestWithDuration) {
			/**
			 * Generic Case
			 */
			OptimizationAlgo algo1 = new OptimizationAlgo(satelliteList, gridPointList, this.gridder, isSpotLight,
					FeasibilityConstants.OP1, FeasibilityConstants.OP2, FeasibilityConstants.OP3);

			/**
			 * Generating strips
			 */
			algo1.generateStripsList();
			algo = algo1;
		} else {
			/**
			 * PointREquest Case
			 *
			 */
			OptimizationAlgoPointDuration algo2 = new OptimizationAlgoPointDuration(satelliteList,
					DateUtils.secondsToJulian(request.getDurationForPuntualPR()),
					DateUtils.fromISOToCSKDate(request.getStopTime()));
			algo = algo2;
		}
		
		/*
		 * modifica dinamica number of iterations
		 */
		
		/**
		 * Performing the optimization loop
		 */
		algo.performOptimizationLoop(this.numberOfOuterIteration);
		return algo;
	}// end method

	/**
	 * Perform two optimization one for orbit only ascanding and one for orbit
	 * descending and return the one with major coverage
	 *
	 * @param accessList
	 * @param gridPointList
	 * @param isSpotLight
	 * @return
	 */
	protected OptimizationAlgoInterface performOptimizationForAnyOrbitDirection(List<Satellite> satelliteList,
			List<GridPoint> gridPointList, boolean isSpotLight, PRRequestParameter request) {
		FeasibilityPerformer.logger.debug("Performing optimization case ANY orbit");

		Map<Satellite, List<Access>> satMapAccess = new TreeMap<>();

		/**
		 * For each satellite
		 */
		for (Satellite s : satelliteList) {
			List<Access> acscendingAccessList = new ArrayList<>();
			List<Access> descendingAccessList = new ArrayList<>();
			for (Access a : s.getAccessList()) {
				/**
				 * Splitting accesses on Orbit direction basis
				 */
				if (a.getOrbitDirection() == FeasibilityConstants.AscendingOrbit) {
					acscendingAccessList.add(a);
				} else {
					descendingAccessList.add(a);
				}
			} // end for

			/**
			 * Using only acsendoing list
			 */
			s.setAccessList(acscendingAccessList);

			/**
			 * Adding descending list to map
			 */
			satMapAccess.put(s, descendingAccessList);
		}

		// Perfroming optimization on acsnding orbit
		// logger.info("Performing ascending opti orbit");
		/**
		 * Perform optimization
		 */
		OptimizationAlgoInterface ascendingAlgo = performOptimization(satelliteList, gridPointList, isSpotLight,
				request);

		if (ascendingAlgo.isSingleAcquired()) {
			/**
			 * In case of single acquired no more evaluation are needed
			 */
			return ascendingAlgo;
		}

		double ascendingCoverage = 0;
		if (ascendingAlgo.getOptimalAcqReqList().size() != 0) {
			try {
				/**
				 * Evaluating ascending coverage
				 */
				ascendingCoverage = this.gridder.getCoverage(ascendingAlgo.getOptimalAcqReqList());
			} catch (GridException e) {
				this.tracer.warning(EventType.APPLICATION_EVENT, "Error on evaluating ascendingCoverage",
						e.getMessage());
			}
		}

		// perform descending optimization
		// logger.info("Performing descending opti orbit");
		/**
		 * Retrieving descending access lists
		 */
		for (Satellite s : satelliteList) {
			s.setAccessList(satMapAccess.get(s));
		}

		/**
		 * Performin optimization in descending case
		 */
		OptimizationAlgoInterface descendingAlgo = performOptimization(satelliteList, gridPointList, isSpotLight,
				request);

		if (descendingAlgo.isSingleAcquired()) {
			/**
			 * if single no more action required
			 */
			return descendingAlgo;
		}

		double descendingCoverage = 0;
		if (descendingAlgo.getOptimalAcqReqList().size() != 0) {
			try {
				/**
				 * Evaluating descending coverare
				 */
				descendingCoverage = this.gridder.getCoverage(descendingAlgo.getOptimalAcqReqList());
			} catch (GridException e) {
				// logger.warn(e.getMessage());
				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.UNEXPECTED_INFORMATION, e.getMessage());
			}
		}

		OptimizationAlgoInterface algo;

		/**
		 * the case with greater coverage is returned
		 */

		if (descendingCoverage > ascendingCoverage) {
			algo = descendingAlgo;
		} else {
			algo = ascendingAlgo;
		}

		// TODO
		// Passaggi solo per test da cancellare a regime
		/*
		 * try{ logger.info("Dumping data for debug"); String BaseOutFile =
		 * System.getProperty("user.home"); String outFile = BaseOutFile+ File.separator
		 * + "TESTDATA/accesses.txt"; dumpAccessToFile(outFile);
		 * logger.info("Dumped acceseses"); outFile = BaseOutFile+ File.separator +
		 * "TESTDATA/iteration.txt"; int optimalOuterIterationIndex =
		 * algo.getOptimalIndex();
		 * algo.getOuterItetionList().get(optimalOuterIterationIndex).dumpToFile
		 * (outFile); logger.info("Dumped optimalOuterIterationIndex");
		 * algo.getOuterItetionList().get(optimalOuterIterationIndex).
		 * dumpDTOsToFile(BaseOutFile+ File.separator + "INTESECTIONTEST/");
		 * logger.info("Dumped DTO");
		 * algo.getOuterItetionList().get(optimalOuterIterationIndex).
		 * dumpSolutionToGPX(BaseOutFile+ File.separator + "INTESECTIONTEST/out.gpx");
		 * logger.info("Dumped to gpx"); outFile = BaseOutFile+ File.separator +
		 * "TESTDATA/strips.txt"; algo.dumpStripToFile(outFile);
		 * logger.info("Dumped to strip"); } catch(Exception e) {
		 * logger.warn("Dumping data for debug"); }
		 */
		// Fine sezione passaggi da cancellare a regime

		return algo;
	}// end method

	/**
	 * Build and return an gridder object according to PRRequest
	 *
	 * @param request
	 * @return
	 * @throws GridException
	 * @throws XPathExpressionException
	 */
	protected Gridder buildGrid(PRRequestParameter request) throws GridException, XPathExpressionException {

		/**
		 * Setting the polar limit
		 */
		request.setPolarLimit(this.polarLimit);
		Gridder returnedGridder = null;
		/**
		 * Retrieving request tyoe
		 */
		int requestAreaType = request.getRequestProgrammingAreaType();

		switch (requestAreaType) {
		case PRRequestParameter.PolygonRequestType:
			String posList = request.getPosList();
			PolygonGridder polygrid;
			if (request.isPolarRequest()) {
				/**
				 * polar polygin
				 */
				FeasibilityPerformer.logger.debug("Polar polygon request");
				polygrid = new PolarPolygonGridder(posList, this.dem, this.gridSpacing);
			} else {
				/**
				 * Pomygin
				 */
				polygrid = new PolygonGridder(posList, this.dem, this.gridSpacing);
			}

			if (request.getHasTargetCenter()) {
				/**
				 * Inserting target point
				 */
				polygrid.setTargeTCenter(request.getTargetCenter());
			} else {
				/**
				 * If not target center has been specified we'll force the target center in the
				 * request to be the centroid of the area of interest
				 */
				GridPoint targetCenterPoint = polygrid.getCentroid();
				double[] targetLLH = null;
				if (targetCenterPoint != null) {
					targetLLH = targetCenterPoint.getLLH();
					targetLLH[2] = 0;

				}
				request.setTargetCenter(targetLLH);

			}

			returnedGridder = polygrid;
			break;
		case PRRequestParameter.CircleRequestType:
			PolygonGridder polygrid1;

			String targetCenterPoint = request.getTargetCenter();
			String radius = request.getCircleRadius();

			if (request.isPolarRequest()) {
				/**
				 * Polar circle
				 */
				FeasibilityPerformer.logger.debug("Polar circle request");
				String posList1 = fromCircleToPolygon(targetCenterPoint, radius);
				// polygrid1 = new PolarPolygonGridder(targetCenterPoint,
				// radius, this.dem, this.gridSpacing);
				polygrid1 = new PolarPolygonGridder(posList1, this.dem, this.gridSpacing);

			} else {
				/**
				 * Transform circle to polygon
				 */
				String posList1 = fromCircleToPolygon(targetCenterPoint, radius);
				polygrid1 = new PolygonGridder(posList1, this.dem, this.gridSpacing);
				/**
				 * Setting target center to circle center
				 */
				polygrid1.setTargeTCenter(targetCenterPoint);
			}

			returnedGridder = polygrid1;
			break;

		case PRRequestParameter.lineRequestType:
			LineGridder lineGrid;
			if (request.isPolarRequest()) {
				/**
				 * Polar Line
				 *
				 *
				 *
				 */
				FeasibilityPerformer.logger.debug("Building polar linestring");
				lineGrid = new PolarLineGridder(request.getPosList(), this.dem);
			} else {
				/**
				 * Linestring
				 *
				 *
				 */
				lineGrid = new LineGridder(request.getPosList(), this.dem);
			}
			if (!request.getHasTargetCenter()) {

				/**
				 * If not target center has been specified we'll force the target center in the
				 * request to be the centroid of the area of interest
				 */
				GridPoint centroid = lineGrid.getCentroid();
				double[] targetLLH = null;
				if (centroid != null) {
					targetLLH = centroid.getLLH();
					targetLLH[2] = 0;
				}
				request.setTargetCenter(targetLLH);
			}
			returnedGridder = lineGrid;
			break;
		case PRRequestParameter.pointRequestType:
			/**
			 * Point
			 */
			PuntualGridder pGrid = new PuntualGridder(request.getPosList(), this.dem);
			returnedGridder = pGrid;
			break;
		case PRRequestParameter.pointRequestWithDuration:
			/**
			 * Point with duration
			 */
			PuntualGridder pGridW = new PuntualGridder(request.getPosList(), this.dem);
			returnedGridder = pGridW;
			break;
		default:
			throw new XPathExpressionException("unknown programming area type");// It
																				// should
																				// not
																				// happen

		}// end switch

		return returnedGridder;
	}// end method

	/**
	 *
	 * @param acqList
	 * @param prParam
	 * @param isSingleAcquired, true if the request is single acuisition
	 * @throws FeasibilityException
	 * @throws GridException
	 */
	protected void addResponseToXML(List<AcqReq> acqList, PRRequestParameter prParam, boolean isSingleAcquired)
			throws FeasibilityException, GridException {
		/**
		 * Adding response
		 */

		NodeList nl = this.doc.getElementsByTagNameNS(FeasibilityConstants.ProgReqTagNameNS,
				FeasibilityConstants.ProgReqTagName); // retrieve
														// the
														// PR
														// NODE
		if (nl.getLength() == 0) {
			// TODO non dovrebbe capitare
			throw new FeasibilityException("Unable to found: " + FeasibilityConstants.ProgReqTagName);
		}
		Element programmingRequestNode = (Element) nl.item(0);

		/**
		 * Evaluating and dumping coverage
		 */
		double requiredCoverage = prParam.getRequiredPercentageOfCoverage();
		double coverage = 100.0;
		//if (!isSingleAcquired) {
			coverage = getUsedCoverage(acqList);
			if(coverage==100.0)
			{
				isSingleAcquired=true;
			}
		//}
		
		for (AcqReq a : acqList) {
			/**
			 * Dumpig AR
			 */
			dumpAcqReqInXML(programmingRequestNode, a, prParam, isSingleAcquired);
		}

		// TODO Evaluate SUF:May be it should be commented
		/**
		 * Dumping FA section
		 */
		addPR_FASection(this.doc, programmingRequestNode, acqList);

		/**
		 * Dumping PR STatus
		 *
		 */
		Element prStatus = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.PRStatusTagName,
				FeasibilityConstants.PRStatusTagNameNS);
		programmingRequestNode.appendChild(prStatus);

		Element status = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.statusTagName,
				FeasibilityConstants.statusTagNameNS);
		prStatus.appendChild(status);



		// String statusString = FeasibilityConstants.FailedStatusString;

		String statusString = getPrStatusString(coverage, requiredCoverage);
		/*
		 * if(coverage >= requiredCoverage) { statusString =
		 * FeasibilityConstants.CompleteStatusString; } else if(coverage
		 * >this.minCoverage && coverage < requiredCoverage) { statusString =
		 * FeasibilityConstants.PartialStatusString; } else { statusString =
		 * FeasibilityConstants.FailedStatusString; }
		 */
		status.setTextContent(statusString);
		Element description = XMLUtils.createElement(this.doc, this.namespaceMap,
				FeasibilityConstants.descriptionTagName, FeasibilityConstants.descriptionTagNameNS);

		/**
		 *
		 *
		 */
		prStatus.appendChild(description);
		description.setTextContent("Operation performed with " + acqList.size() + " AR");

		Element coverageElement = XMLUtils.createElement(this.doc, this.namespaceMap,
				FeasibilityConstants.PRCoverageTagName, FeasibilityConstants.PRCoverageTagNameNS);

		/**
		 * rounding coverage
		 */
		double coverageRounded = new BigDecimal(coverage).setScale(3, RoundingMode.FLOOR).doubleValue();

		// BigDecimal bcoverage = new BigDecimal(coverage).setScale(3,
		// BigDecimal.ROUND_FLOOR);

		/**
		 * Appending coverage
		 */
		programmingRequestNode.appendChild(coverageElement);
		logger.debug("Coverage:" + coverageRounded);
		logger.debug("acqList size:" + acqList.size());

		coverageElement.setTextContent(String.format(Locale.US, "%.3f", coverageRounded));

		// this.tracer.log(String.format(Locale.US, "Coverage:
		// %.3f",bcoverage));
		// coverageElement.setTextContent(String.format(Locale.US,"%.3f",
		// bcoverage));

	}// end addResponseToXML

	/**
	 * Return the coverage to be used in response
	 *
	 * @param acqList
	 * @return
	 * @throws GridException
	 */
	protected double getUsedCoverage(List<AcqReq> acqList) throws GridException {
		return this.gridder.getCoverage(acqList) * 100;
	}

	/**
	 * Check the coverage agiunst required coverage and return the relavant stus
	 * string
	 *
	 * @param coverage
	 * @param requiredCoverage
	 * @return status string
	 */
	protected String getPrStatusString(double coverage, double requiredCoverage) {
		/**
		 * Failed status string
		 */
		String statusString = FeasibilityConstants.FailedStatusString;

		/**
		 * evaluating status string
		 */
		if (coverage >= requiredCoverage) {
			statusString = FeasibilityConstants.CompleteStatusString;
		} else if ((coverage > this.minCoverage) && (coverage < requiredCoverage)) {
			statusString = FeasibilityConstants.PartialStatusString;
		} else {
			statusString = FeasibilityConstants.FailedStatusString;
		}

		/**
		 *
		 *
		 */
		return statusString;
	} // end getPrStatusString

	/**
	 * Add the PR_FA section to the xml response
	 *
	 * @param doc                    xml doc
	 * @param programmingRequestNode element node
	 * @param acqList                list of acquisition
	 * @param suf
	 */
	protected void addPR_FASection(Document doc, Element programmingRequestNode, List<AcqReq> acqList) {
		double suf = 0;

		/**
		 * Evaluating PR SUF
		 */
		for (AcqReq a : acqList) {
			suf = suf + a.getSuf();
		} // end for

		/**
		 * Creating element
		 */
		Element PR_FA = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.PR_FA_TagName,
				FeasibilityConstants.PR_FA_TagNameNS);
		programmingRequestNode.appendChild(PR_FA);

		/**
		 * Creating element
		 */
		Element PRSUF = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.PRSUFTagName,
				FeasibilityConstants.PRSUFTagNameNS);
		PR_FA.appendChild(PRSUF);
		PRSUF.setTextContent(String.format(Locale.US, "%.3f", suf));

		String minDTOStartTime = evaluateMinDTODate(acqList);

		/**
		 * Creating element
		 */
		Element earliestExecutionDate = XMLUtils.createElement(this.doc, this.namespaceMap,
				FeasibilityConstants.EarliestExecutionDateTagName, FeasibilityConstants.EarliestExecutionDateTagNameNS);
		PR_FA.appendChild(earliestExecutionDate);
		earliestExecutionDate.setTextContent(minDTOStartTime);

		/**
		 * Creating element
		 */
		Element likelyExecutionDate = XMLUtils.createElement(this.doc, this.namespaceMap,
				FeasibilityConstants.LikelyExecutionDateTagName, FeasibilityConstants.LikelyExecutionDateTagNameNS);
		PR_FA.appendChild(likelyExecutionDate);
		likelyExecutionDate.setTextContent(minDTOStartTime);

		/**
		 * Creating element
		 */
		Element remainingAttempt = XMLUtils.createElement(this.doc, this.namespaceMap,
				FeasibilityConstants.RemainingExecutionAttemptsTagName,
				FeasibilityConstants.RemainingExecutionAttemptsTagNameNS);
		PR_FA.appendChild(remainingAttempt);
		remainingAttempt.setTextContent("" + acqList.size());

	}// end addPR_FASection

	/**
	 * Evaluate the DTO Having StartTime minimum
	 *
	 * @param acqList
	 * @return
	 */
	protected String evaluateMinDTODate(List<AcqReq> acqList) {

		String retVal = "";
		double minDtoStartTime = 0;
		double currentStartDate;

		/**
		 * Searching info inside arlist
		 */
		for (AcqReq a : acqList) {
			/**
			 * searching info inside dtolist
			 */
			for (DTO d : a.getDTOList()) {
				currentStartDate = d.getStartTime();
				if (currentStartDate > minDtoStartTime) {
					minDtoStartTime = currentStartDate;
				}
			} // end inner for

		} // end outer for

		/**
		 * Converting to string
		 */
		retVal = DateUtils.fromCSKDateToISOFMTDate(minDtoStartTime);
		return retVal;
	}// end miethod

	/**
	 * Dump the single AR in xml response
	 *
	 * @param pr
	 * @param acq
	 */
	protected void dumpAcqReqInXML(Element pr, AcqReq acq, PRRequestParameter prParam, boolean isSingleAcquired) {
		logger.debug("Dumping AR " + acq.getId());
		logger.debug("Dumping AR " + acq);

		/**
		 * Creating element
		 */
		Element AR = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.AcquisitionRequestTagName,
				FeasibilityConstants.AcquisitionRequestTagNameNS);
		/**
		 * Creating element
		 */
		Element ARID = XMLUtils.createElement(this.doc, this.namespaceMap,
				FeasibilityConstants.AcquisitionRequestIDTagName, FeasibilityConstants.AcquisitionRequestIDTagNameNS);
		ARID.setTextContent("" + acq.getId());
		AR.appendChild(ARID);
		/**
		 * Creating element
		 */
		for (String ms : acq.getMission()) {
			Element mission = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.MissionTagName,
					FeasibilityConstants.MissionTagNameNS);
			mission.setTextContent(ms);
			AR.appendChild(mission);
		}

		if (prParam.isCombined()) {
			/**
			 * Creating element
			 */
			Element combinedReqFlag = XMLUtils.createElement(this.doc, this.namespaceMap,
					FeasibilityConstants.combinedReqFlagTagName, FeasibilityConstants.combinedReqFlagTagNameNS);
			combinedReqFlag.setTextContent(FeasibilityConstants.combinedReqFlagTrueValue);
			AR.appendChild(combinedReqFlag);
		}

		/*
		 * Element rank = XMLUtils.createElement(this.doc,
		 * this.namespaceMap,FeasibilityConstants.AcquisitionRankTangName);
		 * rank.setTextContent("1"); AR.appendChild(rank);
		 */
		/**
		 * Creating element
		 */
		Element polygon = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.PolygonTagName,
				FeasibilityConstants.PolygonTagNameNS);
		// polygon.setAttribute(FeasibilityConstants.PolygonIDAttributeName,
		// "ARPolygon"+acq.getId());
		String attrPrefix = this.namespaceMap.get(FeasibilityConstants.PolygonIDAttributeNameNS);
		if (attrPrefix != null) {
			polygon.setAttribute(attrPrefix + ":" + FeasibilityConstants.PolygonIDAttributeName,
					"ARPolygon-PR-" + prParam.getProgReqId() + "-AR-" + acq.getId());

		} else {
			polygon.setAttributeNS(FeasibilityConstants.PolygonIDAttributeNameNS,
					FeasibilityConstants.PolygonIDAttributeName,
					"ARPolygon-PR-" + prParam.getProgReqId() + "-AR-" + acq.getId());

		}
		AR.appendChild(polygon);
		/**
		 * Creating element
		 */
		Element exterior = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.ExteriorTagName,
				FeasibilityConstants.ExteriorTagNameNS);

		polygon.appendChild(exterior);
		/**
		 * Creating element
		 */
		Element linearRing = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.LinearRingTagName,
				FeasibilityConstants.LinearRingTagNameNS);
		exterior.appendChild(linearRing);
		/**
		 * Creating element
		 */
		Element posList = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.PosListTagName,
				FeasibilityConstants.PosListTagNameNS);
		linearRing.appendChild(posList);

		// logger.info("Setting polygon for AR");
		// logger.info("DTO LIST SIZE "+acq.getDTOList().size());

		int requestType = prParam.getRequestProgrammingAreaType();
		if (this.haveCheckForSingleAcquisitionInBuildingARElement && isSingleAcquired
				&& ((requestType == PRRequestParameter.pointRequestType)
						|| (requestType == PRRequestParameter.CircleRequestType)
						|| (requestType == PRRequestParameter.PolygonRequestType))) {
			// in case of single acquisition for area, circle or point,
			// we can not use the first DTO's polyline as line of AR
			posList.setTextContent(this.gridder.retunPolyListForSingleAcq());
		} // end if
		else {
			// using the first DTO polygon
			posList.setTextContent(acq.getDTOList().get(0).getPosListString());
		} // end else

		// logger.info("DTO LIST SIZE "+acq.getDTOList().size());

		/**
		 * Dumping DTOs
		 */
		for (DTO d : acq.getDTOList()) {
			AR.appendChild(createDTOXMLElement(d, acq.getId(), prParam, acq, isSingleAcquired));
		} // end for

		/**
		 * check for Di2SFlag
		 */
		if (prParam.isDi2sAvailabilityFlag()) {
			if (this.di2SAvailabilityConfirmationFlag) {
				
				logger.debug("isSingleAcquired ???"+isSingleAcquired);

				// The confirmation flag holds true only in case of single
				// acquired
				this.di2SAvailabilityConfirmationFlag = isSingleAcquired;
				
				logger.debug("so this.di2SAvailabilityConfirmationFlag "+this.di2SAvailabilityConfirmationFlag );

			}
			/**
			 * Creating element
			 */
			Element disConfirmationFlagElement = XMLUtils.createElement(this.doc, this.namespaceMap,
					FeasibilityConstants.DI2SAvailabilityConfirmationTagName,
					FeasibilityConstants.DI2SAvailabilityConfirmationTagNameNS);
			if (this.di2SAvailabilityConfirmationFlag) {
				logger.debug("set DI2SAvailabilityConfirmationTrueValue " );

				disConfirmationFlagElement.setTextContent(FeasibilityConstants.DI2SAvailabilityConfirmationTrueValue);
			} else {
				logger.debug("set DI2SAvailabilityConfirmationFalseValue " );

				disConfirmationFlagElement.setTextContent(FeasibilityConstants.DI2SAvailabilityConfirmationFalseValue);
			}
			AR.appendChild(disConfirmationFlagElement);
		} // end if

		/**
		 * Appending AR to prnode
		 */
		pr.appendChild(AR);
		// logger.info("AR Dumped");
	}// end method

	/**
	 * Create a DTO xml node
	 *
	 * @param dto
	 * @return dto xml node
	 */
	protected Element createDTOXMLElement(DTO dto, String arId, PRRequestParameter prParam, AcqReq ar,
			boolean isSingleAcquired) {
		// logger.info("Dumping DTO");
		/**
		 * Creating element
		 */
		Element dtoElement = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.DTOTagName,
				FeasibilityConstants.DTOTagNameNS);
		/**
		 * Creating element
		 */
		Element dtoId = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.DTOIdTagName,
				FeasibilityConstants.DTOIdTagNameNS);
		dtoId.setTextContent("" + dto.getId());
		dtoElement.appendChild(dtoId);
		/**
		 * Creating element
		 */
		Element platform = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.PlatformTagName,
				FeasibilityConstants.PlatformTagNameNS);
		dtoElement.appendChild(platform);

		// logger.info("Access List lenght : " + dto.getDtoAccessList().size());
		/**
		 * Creating element
		 */
		Element mission = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.MissionTagName,
				FeasibilityConstants.MissionTagNameNS);
		mission.setTextContent(dto.getDtoAccessList().get(0).getMissionName());
		platform.appendChild(mission);

		/**
		 * Creating element
		 */
		Element satellite = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.SatelliteTagName,
				FeasibilityConstants.SatelliteTagNameNS);
		satellite.setTextContent(dto.getSatName());
		platform.appendChild(satellite);
		/**
		 * Creating element
		 */
		Element sensor = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.SensorTagName,
				FeasibilityConstants.SensorTagNameNS);
		sensor.setTextContent("SAR");
		platform.appendChild(sensor);
		/**
		 * Creating element
		 */
		Element sensorMode = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.SensorModeTagName,
				FeasibilityConstants.SensorModeTagNameNS);
		sensorMode.setTextContent(dto.getDtoAccessList().get(0).getBeam().getSensorModeName());
		platform.appendChild(sensorMode);

		/**
		 * Add interferometric info
		 */
		if (this.isInterferometric) {
			/**
			 * Creating element
			 */
			Element linked = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.LinkedDTOTagNAme,
					FeasibilityConstants.LinkedDTOTagNAmeNS);
			dtoElement.appendChild(linked);
			/**
			 * Creating element
			 */
			Element linkType = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.linkTypeTagName,
					FeasibilityConstants.linkTypeTagNameNS);
			linkType.setTextContent(FeasibilityConstants.INTERFERMETRIC_LINK_TYPE);
			linked.appendChild(linkType);
			/**
			 * Creating element
			 */
			Element acqReqId = XMLUtils.createElement(this.doc, this.namespaceMap,
					FeasibilityConstants.AcquisitionRequestIDTagName,
					FeasibilityConstants.AcquisitionRequestIDTagNameNS);
			acqReqId.setTextContent("" + ar.getLinkedAR().getId());
			linked.appendChild(acqReqId);
			/**
			 * Creating element
			 */
			Element linkedDtoId = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.DTOIdTagName,
					FeasibilityConstants.DTOIdTagNameNS);
			linkedDtoId.setTextContent("" + dto.getId());
			linked.appendChild(linkedDtoId);
		} // end if

		if (prParam.isStereo()) {
			/**
			 * Adding stereo info
			 */
			addStereoPairLink(dtoElement, dto, ar);
		} // end if

		/**
		 * Creating element
		 */
		Element dtoSensing = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.DTOSensingTagName,
				FeasibilityConstants.DTOSensingTagNameNS);
		dtoElement.appendChild(dtoSensing);
		/**
		 * Creating element
		 */
		Element dtoStartTime = XMLUtils.createElement(this.doc, this.namespaceMap,
				FeasibilityConstants.timeStartTagName, FeasibilityConstants.timeStartTagNameNS);
		dtoStartTime.setTextContent(DateUtils.fromCSKDateToISOFMTDateTime(dto.getStartTime()));
		dtoSensing.appendChild(dtoStartTime);

		/**
		 * Creating element
		 */
		Element dtoStopTime = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.timeStopTagName,
				FeasibilityConstants.timeStopTagNameNS);
		dtoStopTime.setTextContent(DateUtils.fromCSKDateToISOFMTDateTime(dto.getStopTime()));
		dtoSensing.appendChild(dtoStopTime);

		/**
		 * Creating element
		 */
		Element polygon = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.PolygonTagName,
				FeasibilityConstants.PolygonTagNameNS);
		// polygon.setAttribute(FeasibilityConstants.PolygonIDAttributeName,
		// "DTOPolygon-AR-"+arId+"-DTO-"+dto.getId());
		String attrPrefix = this.namespaceMap.get(FeasibilityConstants.PolygonIDAttributeNameNS);
		/**
		 * Setting attribute
		 */
		if (attrPrefix != null) {
			polygon.setAttribute(attrPrefix + ":" + FeasibilityConstants.PolygonIDAttributeName,
					"DTOPolygon-PR-" + prParam.getProgReqId() + "-AR-" + arId + "-DTO-" + dto.getId());
		} else {
			polygon.setAttributeNS(FeasibilityConstants.PolygonIDAttributeNameNS,
					FeasibilityConstants.PolygonIDAttributeName,
					"DTOPolygon-PR-" + prParam.getProgReqId() + "-AR-" + arId + "-DTO-" + dto.getId());
		}

		dtoElement.appendChild(polygon);
		/**
		 * Creating element
		 */
		Element exterior = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.ExteriorTagName,
				FeasibilityConstants.ExteriorTagNameNS);

		polygon.appendChild(exterior);

		/**
		 * Creating element
		 */
		Element linearRing = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.LinearRingTagName,
				FeasibilityConstants.LinearRingTagNameNS);
		exterior.appendChild(linearRing);
		/**
		 * Creating element
		 */
		Element posList = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.PosListTagName,
				FeasibilityConstants.PosListTagNameNS);
		linearRing.appendChild(posList);

		posList.setTextContent(dto.getPosListString());

		/**
		 * Creating element
		 */
		Element orbitNumber = XMLUtils.createElement(this.doc, this.namespaceMap,
				FeasibilityConstants.orbitNumberTagName, FeasibilityConstants.orbitNumberTagNameNS);
		dtoElement.appendChild(orbitNumber);
		orbitNumber.setTextContent("" + dto.getOrbitId());
		/**
		 * Creating element
		 */
		Element orbitDir = XMLUtils.createElement(this.doc, this.namespaceMap,
				FeasibilityConstants.orbitDirectionTagName, FeasibilityConstants.orbitDirectionTagNameNS);
		dtoElement.appendChild(orbitDir);
		orbitDir.setTextContent(FeasibilityConstants.getOrbitDirectionAsString(dto.getOrditDirection()));
		/**
		 * Creating element
		 */
		Element trackNumber = XMLUtils.createElement(this.doc, this.namespaceMap,
				FeasibilityConstants.trackNumberTagName, FeasibilityConstants.trackNumberTagNameNS);
		dtoElement.appendChild(trackNumber);
		trackNumber.setTextContent("" + dto.getTrackNumber());
		/**
		 * Creating element
		 */
		Element dtoInfo = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.DTOInfoTagName,
				FeasibilityConstants.DTOInfoTagNameNS);
		dtoElement.appendChild(dtoInfo);
		/**
		 * Creating element
		 */
		Element sar = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.SarTagName,
				FeasibilityConstants.SarTagNameNS);
		dtoInfo.appendChild(sar);

		/**
		 * retrieving polarization
		 */
		String polarizationString = prParam.getPolarizationCSK();
		if (dto.getSat().getMissionName().equalsIgnoreCase(FeasibilityConstants.CSG_NAME)) {
			polarizationString = prParam.getPolarizationCSG();
		}

		if (!polarizationString.equals("")) {
			/**
			 * Adding polarization element
			 */
			Element polarization = XMLUtils.createElement(this.doc, this.namespaceMap,
					FeasibilityConstants.PolarizationTagName, FeasibilityConstants.PolarizationTagNameNS);
			sar.appendChild(polarization);
			polarization.setTextContent(polarizationString);
		}
		/**
		 * Creating element
		 */
		Element lookSide = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.LookSideTagName,
				FeasibilityConstants.LookSideTagNameNS);
		sar.appendChild(lookSide);
		lookSide.setTextContent(FeasibilityConstants.getLookSideString(dto.getLookSide()));

		/**
		 * Creating element
		 */
		Element beamId = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.BeamIdTagName,
				FeasibilityConstants.BeamIdTagNameNS);
		sar.appendChild(beamId);
		beamId.setTextContent(dto.getBeamId());
		/**
		 * Creating element
		 */
		Element lookAngle = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.LookAngleTagName,
				FeasibilityConstants.LookAngleTagNameNS);
		sar.appendChild(lookAngle);
		lookAngle.setTextContent(String.format(Locale.US, "%.2f", dto.getLookAngle()));

		/**
		 * inserting target distance
		 */
		if (prParam.getHasTargetCenter()) {
			double[] llhTargetedPoint = prParam.getTargetedPointLLH();
			double distance = 0;
			if (llhTargetedPoint != null) {
				// In case of not single acquisition zero will be set

				if (isSingleAcquired) {
					distance = dto.getTargetDistance(llhTargetedPoint);
				}
				// TODO
				// PEZZA A COLORE PER CSK da rivedere meglio
				/*
				 * if(distance<0.1){ distance=0.1; }
				 */
				// FINE PEZZA A COLORE
			}
			Element targetDistance = XMLUtils.createElement(this.doc, this.namespaceMap,
					FeasibilityConstants.TargetDistanceTagName, FeasibilityConstants.TargetDistanceTagNameNS);
			targetDistance.setTextContent(String.format(Locale.US, "%.3f", distance));
			dtoElement.appendChild(targetDistance);
		}

		/**
		 * Creating suf element
		 */
		Element dtoSUF = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.DTOSufTagName,
				FeasibilityConstants.DTOSufTagNameNS);
		dtoSUF.setTextContent(String.format(Locale.US, "%.3f", dto.getSuf()));
		dtoElement.appendChild(dtoSUF);

		/**
		 * check form paw (the dto is prresent onli if deferreble paw or not paw at all
		 * are in dto)
		 */
		if (dto.getPaw() != null) {

			String pawType = dto.getPaw().getActivityType();
			Element alertId = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.AlertIDTagName,
					FeasibilityConstants.AlertIDTagNameNS);
			alertId.setTextContent(pawType);
			dtoElement.appendChild(alertId);

			Element alertDesc = XMLUtils.createElement(this.doc, this.namespaceMap,
					FeasibilityConstants.AlertDescriptionTagName, FeasibilityConstants.AlertDescriptionTagNameNS);
			alertDesc.setTextContent("A deferrable paw overlaps the DTO");
			dtoElement.appendChild(alertDesc);

		}

		/**
		 * SPARCINFO only in case of CSG
		 */
		if (!dto.getSparcInfo().equals("")) {
			Element sparcInfoElement = XMLUtils.createElement(this.doc, this.namespaceMap,
					FeasibilityConstants.SPARCInfoTagName, FeasibilityConstants.SPARCInfoTagNameNS);
			sparcInfoElement.setTextContent(dto.getSparcInfo());
			dtoElement.appendChild(sparcInfoElement);
		}

		// logger.info("DTO Dumped");

		return dtoElement;

	} // end method

	/**
	 * Add stereo link to dom
	 *
	 * @param dtoElement
	 * @param dto
	 * @param ar
	 */
	private void addStereoPairLink(Element dtoElement, DTO dto, AcqReq ar) {
		List<DTO> linkedDTOList;
		/**
		 * We have use the right list
		 */
		if (ar.isStereoMaster()) {
			linkedDTOList = dto.getSlaveDTOList();
		} else {
			linkedDTOList = dto.getMasterDTOList();
		}

		/**
		 * Retrieving linked AR
		 */
		String linkedARID = ar.getLinkedAR().getId();

		for (DTO d : linkedDTOList) {
			/**
			 * Creating element
			 */
			Element linked = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.LinkedDTOTagNAme,
					FeasibilityConstants.LinkedDTOTagNAmeNS);
			dtoElement.appendChild(linked);
			/**
			 * Creating element
			 */
			Element linkType = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.linkTypeTagName,
					FeasibilityConstants.linkTypeTagNameNS);
			linkType.setTextContent(FeasibilityConstants.STEREO_PAIR_LINK_TYPE);
			linked.appendChild(linkType);
			/**
			 * Creating element
			 */
			Element acqReqId = XMLUtils.createElement(this.doc, this.namespaceMap,
					FeasibilityConstants.AcquisitionRequestIDTagName,
					FeasibilityConstants.AcquisitionRequestIDTagNameNS);
			acqReqId.setTextContent(linkedARID);
			linked.appendChild(acqReqId);
			/**
			 * Creating element
			 */
			Element linkedDtoId = XMLUtils.createElement(this.doc, this.namespaceMap, FeasibilityConstants.DTOIdTagName,
					FeasibilityConstants.DTOIdTagNameNS);
			linkedDtoId.setTextContent(d.getId());
			linked.appendChild(linkedDtoId);
		} // end for

	}// addStereoPairLink

	/**
	 * Write the xml to tehe outfile
	 *
	 * @param doc
	 * @param outfile
	 * @throws TransformerException
	 */
	/*
	 * protected void dumpResponseToFile(Document doc,String outfile) throws
	 * TransformerException
	 *
	 * { //logger.debug("Riempio risposta vuota"); File out = new File(outfile);
	 *
	 * TransformerFactory tfactory = TransformerFactory.newInstance(); Transformer
	 * transformer; transformer = tfactory.newTransformer();
	 * transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	 * transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount",
	 * "4");
	 *
	 * DOMSource source = new DOMSource(this.doc); StreamResult result = new
	 * StreamResult(out); transformer.transform(source, result);
	 *
	 * try{ out.setReadable(true,false); } catch(SecurityException ex) {
	 *
	 * } }
	 */

	/**
	 * Evaluate the response name according to the naming convention
	 *
	 * @param requestPath
	 * @return response path string
	 */
	protected String evaluateResponseName(final String requestPath)

	{
		String response = "";

		File file = new File(requestPath);

		/**
		 * Working dir
		 */
		String dirPath = file.getParent();

		// logger.debug("input dir: " + dirPath);

		String fileName = file.getName();
		// logger.debug("input fileName: " + fileName);

		/**
		 * Evaluating timestamp
		 *
		 */
		java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		java.time.LocalDateTime dateTime = LocalDateTime.now();
		String timestamp = dateTime.format(fmt);

		StringTokenizer tokens = new StringTokenizer(fileName, "_");

		/**
		 * Building response bame
		 */
		response = dirPath + File.separator + tokens.nextToken() + "_" + timestamp + ".GET_FEASIBILITY_RES.xml";

		return response;
	} // end method

	/**
	 * Tranform a circle to a polygon. Return a string holding the point of the
	 * polygon in form of : "lat1 lon1 lat2 lon2 ........"
	 *
	 * @param center      of the circcle expresses has a string holgibg lat e lon
	 * @param radius      expresses as a string
	 * @param angularStep angular spacing for the point on the circonference in
	 *                    degree
	 * @return a string holding the point of the polygon in form of : "lat1 lon1
	 *         lat2 lon2 ........"
	 * @throws GridException
	 */
	String fromCircleToPolygon(String center, String radius) throws GridException {
		/**
		 * Tokeninzing center strinhg
		 */
		StringTokenizer tokens = new StringTokenizer(center);
		double[] targetCenter = new double[3];
		double circleRadius = 0;
		/**
		 * Check for well formed center
		 */
		if (tokens.countTokens() != 2) // No elements or spare elements
		{
			throw new GridException("Malformed circle center point");
		}

		try {
			/**
			 * Evaluating center
			 */
			double lat = Double.valueOf(tokens.nextToken());
			double longitude = Double.valueOf(tokens.nextToken());

			targetCenter[0] = lat;
			targetCenter[1] = longitude;
			targetCenter[2] = 0;
			/**
			 * Evaluating radius
			 */
			circleRadius = Double.valueOf(radius) * 1000;

		} catch (NumberFormatException e) {
			throw new GridException(e.getMessage());
		}
		// TODO rendere configurabile
		return fromCircleToPolygon(targetCenter, circleRadius, FeasibilityConstants.CircleToPolygonAngularStep);
	}// end method

	/**
	 * Tranform a circle to a polygon. Return a string holding the point of the
	 * polygon in form of : "lat1 lon1 lat2 lon2 ........"
	 *
	 * @param llhCenter,  center of the circle
	 * @param radius      of the circle expressed in meters
	 * @param angularStep angular spacing for the point on the circonference in
	 *                    degree
	 * @return a string holding the point of the polygon in form of : "lat1 lon1
	 *         lat2 lon2 ........"
	 */
	String fromCircleToPolygon(double[] llhCenter, double radius, double angularStep) {
		String retval = "";
		String firstCouple = "";

		double currentAngle = 0;

		GeodeticCalculator calc = new GeodeticCalculator();
		/**
		 * Poles are singulararity point so we have to move a little
		 *
		 */
		if (llhCenter[0] == 90.0) {
			llhCenter[0] = 89.99999;
		}
		if (llhCenter[0] == -90.0) {
			llhCenter[0] = -89.99999;
		}

		calc.setStartingGeographicPoint(llhCenter[1], llhCenter[0]);

		StringWriter out = new StringWriter();

		Point2D currentPoint;

		/**
		 * Sampling the circle
		 */
		for (int i = 0; i < (360 / angularStep); i++) {
			calc.setStartingGeographicPoint(llhCenter[1], llhCenter[0]);
			currentAngle = i * angularStep;

			calc.setDirection(currentAngle, radius);

			currentPoint = calc.getDestinationGeographicPoint();

			if (i == 0) {
				firstCouple = " " + currentPoint.getY() + " " + currentPoint.getX();
			}

			// out.write(String.format(" %f %f",
			// currentPoint.getY(),currentPoint.getX()));
			/**
			 * Writing inside pos list string
			 */
			out.write("" + currentPoint.getY() + " " + currentPoint.getX() + " ");

			// //System.out.println("Evaluated point" + i+": \t" +
			// currentPoint.getY() + "\t" + currentPoint.getX());

		} // end for;

		// //System.out.println("Evaluated Center: \t" + llhCenter[0] + "\t" +
		// llhCenter[1]);
		/**
		 * First and last point must coincide
		 */
		out.write(firstCouple);

		retval = out.toString();

		return retval;
	}// end method

	/**
	 * For test purposes only
	 *
	 * @return the grid point
	 *
	 *         List<GridPoint> getGridPointList() { return this.gridPointList; }
	 */

	/**
	 * to be deleted
	 *
	 * @param l
	 */
	/*
	 * public void setGridList(List<GridPoint> l) { this.gridPointList=l; }
	 */
	/**
	 * For test purposes only
	 *
	 * @param path
	 */
	/*
	 * public void dumpGridToFile(String outFilePath)
	 *
	 * { try{ BufferedWriter out = new BufferedWriter(new FileWriter(outFilePath));
	 *
	 * double [] llh; for(GridPoint p: gridPointList) { llh = p.getLLH();
	 * out.write(String.format("%d\t%14.8f\t%14.8f\t%14.8f\n",p.getId(),llh[0],
	 * llh[1],llh[2])); }
	 *
	 * out.close();
	 *
	 * }catch (IOException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } }
	 */

	/**
	 * public Gridder getGridder() { return this.gridder; }
	 */

	/**
	 * For test purposes only. Tobe deleted
	 *
	 * @param list
	 */
	/*
	 * private void dumpAccessList(List<Satellite> list,String outFilePath) {
	 * BufferedWriter out=null; try { out = new BufferedWriter(new
	 * FileWriter(outFilePath));
	 *
	 * for(Satellite s: list) { for(Access a: s.getAccessList()){
	 * out.write(a.getSatelliteId()+"|"+"|"+a.getAccessTime()+"|"+DateUtils.
	 * fromCSKDateToISOFMTDateTime(a.getAccessTime())+"|"+a.getBeamId());
	 * out.write("\n");
	 *
	 * } }
	 *
	 *
	 * } catch (IOException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } finally { try { if(out!=null) out.close(); } catch
	 * (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); } }
	 * }
	 */
}// end class
