/**
*
* MODULE FILE NAME:	PRRequestParameter.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			This Class a programming request
*
* PURPOSE:			Feasibility
*
* CREATION DATE:	17-11-2015
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
* --------------------------+------------+----------------+-------------------------------
* 11-05-2016 | Amedeo Bancone  |1.1| Fixed bug on getChildElementListText method
* 									  Fixed bug on max mean angle ckeck
* --------------------------+------------+----------------+-------------------------------
*  --------------------------+------------+----------------+-------------------------------
* 28-09-2017 | Amedeo Bancone  |2.1| Modified to take into account: XSD modification as for C4
* --------------------------+------------+----------------+-------------------------------
* --------------------------+------------+----------------+-------------------------------
* 01-03-2018 | Amedeo Bancone  |2.1| Modified to take into account:
* 																	Combined request
* 																	periodic request
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.feasibility;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.telespazio.csg.srpf.dataManager.bean.BeamBean;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.DateUtils;
import com.telespazio.csg.srpf.utils.PropertiesReader;
import com.telespazio.csg.srpf.utils.XMLUtils;

/**
 * This class holds the features related to the request
 *
 * @author Amedeo Bancone
 * @varsion 2.1
 *
 */
public class PRRequestParameter implements Cloneable

{
	static final Logger logger = LogManager.getLogger(PRRequestParameter.class.getName());

	/**
	 * Logger
	 */
	private TraceManager tracer = new TraceManager();

	// static final Logger logger =
	// LogManager.getLogger(PRRequestParameter.class.getName());
	/**
	 * Prog Req ID
	 */
	private String progReqId = "0";

	private String serviceRequestId = "0";
	/**
	 * Validity start
	 */
	private String startTime; // ok
	/**
	 * Validity stop
	 */
	private String stopTime; // ok
	/**
	 * Mission
	 */
	private String mission; // ok
	/**
	 * true if combined
	 */
	private boolean isCombined = false; // true if combined request
	/**
	 * XML doc
	 */
	private Document doc;
	/**
	 * look side for CSK
	 */
	private int requestedLookSideCSK = FeasibilityConstants.BothLookSide;
	/**
	 * look side for CSG
	 */
	private int requestedLookSideCSG = FeasibilityConstants.BothLookSide;
	/**
	 * Orbit direction
	 */
	private int requestedOrbitDirection = FeasibilityConstants.BothOrbitDirection;
	/**
	 * Required coverage (100) default
	 */
	private double requiredPercentageOfCoverage = 100.0;

	/**
	 * CSK sensormode
	 */
	private String sensorModeCSK; // ok
	private String polarizationCSK = "";// =FeasibilityConstants.DefaultPolarization;
	/**
	 * CSG sensor Mode
	 */
	private String sensorModeCSG; // ok
	private String polarizationCSG = "";// FeasibilityConstants.DefaultPolarization;

	/**
	 * CSK min look angle
	 */
	private double MinLookAngleCSK = 0;
	/**
	 * CSK max look angle
	 */
	private double MaxLookAngleCSK = 0;
	/**
	 * CSG min look angle
	 */
	private double MinLookAngleCSG = 0;
	/**
	 * CSG max look angle
	 */
	private double MaxLookAngleCSG = 0;

	/**
	 * True if have check
	 */
	private boolean HaveCheckMinLookAngleCSK = false;
	/**
	 * True if have check
	 */
	private boolean HaveCheckMaxLookAngleCSK = false;
	/**
	 * True if have check
	 */
	private boolean HaveCheckMinLookAngleCSG = false;
	/**
	 * True if have check
	 */
	private boolean HaveCheckMaxLookAngleCSG = false;

	/**
	 * Polar limit zone default = 84.0
	 */
	private double polarLimit = 84.0;
	/**
	 * UGS id
	 */
	private int ugsId = 0;
	/**
	 * Duration for puntual duration request
	 */
	private int durationForPuntualPR = 0;
	/**
	 * List of allowed satllite CSK
	 */
	private List<String> allowedSatelliteListCSK;
	/**
	 * List of allowed beam CSK
	 */
	private List<String> allowedBeamListCSK;

	/**
	 * List of allowed satllite CSG
	 */
	private List<String> allowedSatelliteListCSG;
	/**
	 * List of allowed beams CSG
	 */
	private List<String> allowedBeamListCSG;

	/**
	 * Snesor
	 */
	private String sensor = "SAR";

	/**
	 * Type of requests
	 */
	/**
	 * Polygon
	 */
	public final static int PolygonRequestType = 0;
	/**
	 * Circle
	 */
	public final static int CircleRequestType = 1;
	/**
	 * Line
	 */
	public final static int lineRequestType = 2;
	/**
	 * Point
	 */
	public final static int pointRequestType = 3;
	/**
	 * Point duration
	 */
	public final static int pointRequestWithDuration = 4;

	/**
	 * Current request type
	 *
	 */
	private int requestType = -1;
	/*
	 * held the list of position defining the polygon of programming area in case of
	 * poligon
	 */
	private String posList = "";

	/**
	 * Target center as String
	 */
	private String targetCenter = ""; // ok
	/**
	 * Target center as llh
	 */
	private double[] targetCenterLLH = null;

	/**
	 * Circle radius
	 */
	private String circleRadius = ""; // ok
	/**
	 * LineStrin in case of line request
	 */
	private String lineRequestString = "";// ok
	/**
	 * Target cencer flag
	 */
	private boolean hasTargetCenter = false;

	/**
	 * Interferometric flag
	 */
	private boolean interferometric = false;

	/**
	 * True if the DI2SAvailability is true
	 */
	private boolean di2sAvailabilityFlag = false;
	/**
	 * min area of interest in case of Di2S request
	 */
	private String minimumAoI = null;
	/**
	 * True if Di2S min AOI coincide with target area
	 */
	private boolean minimumAoICoincidentWithAoI = false;

	// stereo pair
	/**
	 * Stero pair flag
	 */
	private boolean stereo = false;
	/**
	 * stereo pair delta angle
	 */
	private double stereoDeltaAngle = 0;
	/**
	 * stereo pair min angle
	 */
	private double stereoMinAngle = 0;
	/**
	 * Stereo pair max angle
	 */
	private double stereoMaxAngle = 0;

	/**
	 * Passthrough flag
	 */
	private boolean isPassThrough = false;

	/**
	 * List aim
	 */
	private String listAim;

	/**
	 * acquisition stations list
	 */
	private List<String> acquisitionStationList = new ArrayList<>();

	// Priodic / repetitive
	/**
	 * periodic flag
	 */
	private boolean repetitive = false;
	/**
	 * repetitive flag
	 */
	private boolean periodic = false;

	/**
	 * Number of day between two PR in priodic
	 */
	private int periodicGranularity = 0;
	/**
	 * number of iteration for periodic
	 */
	private int periodicIteration = 0;

	/**
	 * Number of days between two repetition
	 */
	private int repetitiveGranularity = 0;

	/**
	 * number of repetition
	 */
	private int repetitiveIteration = 0;

	/**
	 * Set reoetetive flag
	 *
	 * @param repetitive
	 */
	public void setRepetitive(boolean repetitive) {
		this.repetitive = repetitive;
	}// end method

	/**
	 * set periodic flag
	 *
	 * @param periodic
	 */
	public void setPeriodic(boolean periodic) {
		this.periodic = periodic;
	}// end method

	/**
	 *
	 * @return true for repetitive request
	 */
	public boolean isRepetitive() {
		return this.repetitive;
	}// end method

	/**
	 *
	 * @return true for perioc request
	 */
	public boolean isPeriodic() {
		return this.periodic;
	}// end method

	/**
	 * Return the periof in days
	 *
	 * @return periodic granularity
	 */
	public int getPeriodicGranularity() {
		return this.periodicGranularity;
	}// end method

	/**
	 *
	 * @return the number of repetion in periodic
	 */
	public int getPeriodicIteration() {
		return this.periodicIteration;
	}// end method

	/**
	 * Retunr the number of days for granularity in repetition
	 *
	 * @return the repetitive granularity
	 */
	public int getRepetitiveGranularity() {
		return this.repetitiveGranularity;
	}// end method

	/**
	 *
	 * @return the number of repetition
	 */
	public int getRepetitiveIteration() {
		return this.repetitiveIteration;
	}// end method

	/**
	 *
	 * @return true if stereopair request
	 */
	public boolean isStereo() {
		return this.stereo;
	}// end method

	/**
	 *
	 * @return deltaangle for stereo pairs
	 */
	public double getStereoDeltaAngle() {
		return this.stereoDeltaAngle;
	}// end method

	/**
	 *
	 * @return min intervalk for stereo
	 */
	public double getStereoMinAngle() {
		return this.stereoMinAngle;
	}// end method

	/**
	 *
	 * @return max look per stereo
	 */
	public double getStereoMaxAngle() {
		return this.stereoMaxAngle;
	}// end method

	/**
	 *
	 * rerturn the listAim of the request. Only Feasibility and
	 * FeasinbilityExetnsion are allowe
	 *
	 * @return listAim
	 */
	public String getListAim() {
		return this.listAim;
	}// end method

	/**
	 * return true if the mission is a combined mission
	 *
	 * @return
	 */
	public boolean isCombined() {
		return this.isCombined;
	}// end method

	/**
	 * Return the minimum Aoi
	 *
	 * @return the minimum AoI
	 */
	public String getMinimumAoI() {
		/**
		 * If minimumAoI==null we return AOI
		 */
		String retval;
		if (this.minimumAoI != null) {
			retval = this.minimumAoI;
		} else {
			retval = this.posList;
		}

		return retval;
	}// end method

	/**
	 * return true if the minumum AoI coicide with the AOI
	 *
	 * @return
	 */
	public boolean isMinimumAoICoincidentWithAoI() {
		return this.minimumAoICoincidentWithAoI;
	}

	/**
	 * Set the listAim of the request. Only Feasibility and FeasinbilityExetnsion
	 * are allowe
	 *
	 * @param listAim
	 */
	public void setListAim(String listAim) {
		this.listAim = listAim;
	}// end method

	/**
	 *
	 * @return true if the request ia a passthrough request
	 */
	public boolean isPassThrough() {
		return this.isPassThrough;
	}// end method

	/**
	 * Set the passthrough flag
	 *
	 * @param isPassThrough
	 */
	public void setPassThrough(boolean isPassThrough) {
		this.isPassThrough = isPassThrough;
	}// end method

	/**
	 *
	 * @return list of acquisition station involved in the request
	 */
	public List<String> getAcquisitionStationList() {
		return this.acquisitionStationList;
	}// end method

	/**
	 * Set the list of acquisition station involved in the request
	 *
	 * @param acquisitionStationList
	 */
	public void setAcquisitionStationList(List<String> acquisitionStationList) {
		this.acquisitionStationList = acquisitionStationList;
	}// end method

	/**
	 * Return the value of the interferometric flag
	 *
	 * @return
	 */
	public boolean isInterferometric() {
		return this.interferometric;
	}// end method

	/**
	 * Set polar limit
	 *
	 * @param polarLimit
	 */
	public void setPolarLimit(double polarLimit) {
		this.polarLimit = polarLimit;
	}// end method

	/**
	 * Return the list of satellite matching the request parameters
	 *
	 * @param satList
	 * @return the new list of satellite
	 */
	public List<Satellite> chekSatelliteList(List<Satellite> satList) {
		/**
		 * retrun list
		 */
		List<Satellite> validSatList = new ArrayList<>();

		for (Satellite s : satList) {
			/**
			 * For each sat check if: sat is in the allowe list of request has valid beams
			 * Check if the satellite can look at the side requested by the PR Check if
			 * satellite has beam belonging the min / max look angle required in The PR.
			 */
			if (isSatInAllowedSatList(s) && checkIfSatHasValidBeams(s) && checkForSatLookSide(s)
					&& checkForMinMaxLookAngle(s)) {
				validSatList.add(s);
			} // end if
		} // end for

		return validSatList;
	}// end method

	/**
	 * Check if the satellite can look at the side requested by the PR
	 *
	 * @param s
	 * @return true if the satellite can look at side requested by the PR
	 */
	private boolean checkForSatLookSide(Satellite s) {
		/**
		 * return value
		 */
		boolean retval = false;

		int requestedLookSide;
		/**
		 * retrieve the requested look side
		 */
		if (s.getMissionName().equalsIgnoreCase(FeasibilityConstants.CSK_NAME)) {
			/**
			 * CSK
			 */
			requestedLookSide = this.requestedLookSideCSK;
		} else {
			/**
			 * CSG
			 */
			requestedLookSide = this.requestedLookSideCSG;
		}

		/**
		 * true if requsted lookside == BOTH true if satellite allowed look side == BOTH
		 * (satellite can llook both right and left) true if requested look side ==
		 * satellite allowed look side
		 */
		if ((requestedLookSide == FeasibilityConstants.BothLookSide)
				|| (s.getAllowedLookside() == FeasibilityConstants.BothLookSide)
				|| (requestedLookSide == s.getAllowedLookside())) {
			retval = true;
		}

		return retval;
	}// end method

	/**
	 * return true if the satellite belongs the list of satellite specified in the
	 * request
	 *
	 * @param s
	 * @return true if satellite is in the list
	 */
	private boolean isSatInAllowedSatList(Satellite s) {
		String satName = s.getName();
		/**
		 * default we consider CSK case
		 */
		List<String> allowedSatelliteList = this.allowedSatelliteListCSK;
		if (s.getMissionName().equalsIgnoreCase(FeasibilityConstants.CSG_NAME)) {
			/**
			 * if CSG consider CSG list
			 */
			allowedSatelliteList = this.allowedSatelliteListCSG;
		}

		/**
		 * Check if the name of satellite is inside the list
		 */
		return checkIfStringIsInlist(satName, allowedSatelliteList, true);

	}// end isSatInAllowedSatList

	/**
	 * Return true if satellite has beam in the list of beams specified in the
	 * request. Moreover the list of beams in satellite is updated leaving only the
	 * valid beam
	 *
	 * @param s
	 * @return true if satellite has beam in the list of beams specified in the
	 *         request
	 */
	private boolean checkIfSatHasValidBeams(Satellite s) {
		/**
		 * Return param
		 */
		boolean retval = true;
		/**
		 * Default CSK list
		 */
		List<String> allowedBeamList = this.allowedBeamListCSK;

		if (s.getMissionName().equalsIgnoreCase(FeasibilityConstants.CSG_NAME)) {
			/**
			 * if CSG use CSG list
			 */
			allowedBeamList = this.allowedBeamListCSG;
		}

		/**
		 * List of valid beams
		 */
		ArrayList<BeamBean> beams = new ArrayList<>();

		/**
		 * List of beams on satellite for requested sensor mode
		 */
		List<BeamBean> satBeams = s.getBeams();
		BeamBean beam;
		for (int i = 0; i < satBeams.size(); i++) {
			beam = satBeams.get(i);
			/**
			 * For each beam in satlist beam check if the name is in the requested beam and
			 * if so add to the list of valid beam
			 */
			if (checkIfStringIsInlist(beam.getBeamName(), allowedBeamList, true)) {
				beams.add(beam);
			}

		} // end for

		/**
		 * set the satellite list beam to valid list beam
		 */
		s.setBeams(beams);

		/**
		 * if valid beam list empty return false
		 */
		if (beams.size() == 0) {
			retval = false;
		}

		return retval;

	}// end method

	/**
	 * Check if satellite has beam belonging the min / max look angle required in
	 * The PR. If so it returns true and leaves in the list of beams only the ones
	 * matching the request
	 *
	 * @param s
	 * @return true if satellite has beam belonging the min / max look angle
	 *         required in The PR.
	 */
	private boolean checkForMinMaxLookAngle(Satellite s) {
		/**
		 * Flag to be reruned
		 */
		boolean retval = true;

		/**
		 * Flags stating if checks must be performed defaults CSK
		 */
		boolean HaveCheckMinLookAngle = this.HaveCheckMinLookAngleCSK;
		boolean HaveCheckMaxLookAngle = this.HaveCheckMaxLookAngleCSK;
		double MinLookAngle = this.MinLookAngleCSK;
		double MaxLookAngle = this.MaxLookAngleCSK;
		if (s.getMissionName().equalsIgnoreCase(FeasibilityConstants.CSG_NAME)) {
			/**
			 * setting defaults for CSG
			 */
			HaveCheckMinLookAngle = this.HaveCheckMinLookAngleCSG;
			HaveCheckMaxLookAngle = this.HaveCheckMaxLookAngleCSG;
			MinLookAngle = this.MinLookAngleCSG;
			MaxLookAngle = this.MaxLookAngleCSG;
		}

		if (!HaveCheckMinLookAngle) {
			/**
			 * No check required so true must be returned
			 */
			return true;
		}

		BeamBean b;

		// using raw list beacase of teh DataManager returns raw list!!!!!!
		/**
		 * List of allowed beams
		 */
		ArrayList<BeamBean> alloweBeamList = new ArrayList<>();

		/**
		 * Satellite list beams
		 */
		List<BeamBean> beams = s.getBeams();

		double nearAngle;
		double farAngle;

		for (int i = 0; i < beams.size(); i++) {
			/**
			 * For each beam
			 */
			b = beams.get(i);

			/**
			 * get near and far offnadir angle
			 */
			nearAngle = b.getNearOffNadir();
			farAngle = b.getFarOffNadir();

			/*
			 * if(farAngle < this.MinLookAngle) { //the beam has angle range below the
			 * requested value; continue; }
			 */

			if (HaveCheckMaxLookAngle) {
				/**
				 * checking for min and max angle if true add to allowed list
				 */
				// if(nearAngle >= this.MinLookAngle && farAngle <=
				// this.MinLookAngle)
				if (((nearAngle >= MinLookAngle) && (nearAngle <= MaxLookAngle))
						|| ((farAngle >= MinLookAngle) && (farAngle <= MaxLookAngle))
						|| ((nearAngle <= MinLookAngle) && (farAngle >= MaxLookAngle))) {
					alloweBeamList.add(b);
				}
				
				/*COME ERA 
				 * 				if (((nearAngle >= MinLookAngle) && (nearAngle <= MaxLookAngle))
						|| ((farAngle >= MinLookAngle) && (farAngle <= MaxLookAngle))
						|| ((nearAngle <= MinLookAngle) && (farAngle >= MaxLookAngle))) {
					alloweBeamList.add(b);
				}
				
				
				COME E' PER NOI :
								if (((nearAngle >= MinLookAngle) && (nearAngle <= MaxLookAngle))
						&& ((farAngle >= MinLookAngle) && (farAngle <= MaxLookAngle))
						) {
					alloweBeamList.add(b);
				}
				 * 
				 */
			} // end if
			else {
				/**
				 * have check only on min
				 */
				// if(nearAngle <= this.MaxLookAngle )
				/**
				 * the check that the farAngle is above minLookAngle has been performed Above if
				 * true add to allowed list beam
				 */
				if ((nearAngle <= MinLookAngle) && (farAngle >= MinLookAngle)) {
					alloweBeamList.add(b);
				}
			} // end else

		} // end for;

		/**
		 * setting satellite list to allowed list
		 */
		s.setBeams(alloweBeamList);

		/**
		 * if allowed list is empty return false
		 */
		if (alloweBeamList.size() == 0) {
			retval = false;
		}
		return retval;
	}// end method

	/**
	 * @return the lineRequestString
	 */
	public String getLineRequestString() {
		return this.lineRequestString;
	}// end method

	/**
	 * @return the circleRadius
	 */
	public String getCircleRadius() {
		return this.circleRadius;
	}// end method

	/**
	 *
	 * @return the target center as string
	 */
	public String getTargetCenter() {
		return this.targetCenter;
	}// end method

	/**
	 * @return the requiredPercentageOfCoverage
	 */
	public double getRequiredPercentageOfCoverage() {
		return this.requiredPercentageOfCoverage;
	}// end method

	/**
	 * True if centered area
	 *
	 * @return the hasTargetCenter
	 */
	public boolean getHasTargetCenter() {
		return this.hasTargetCenter;
	}// end method

	/**
	 * @param requiredPercentageOfCoverage the requiredPercentageOfCoverage to set
	 */
	public void setRequiredPercentageOfCoverage(double requiredPercentageOfCoverage) {
		this.requiredPercentageOfCoverage = requiredPercentageOfCoverage;// TODO
																			// Gestire
																			// diversamente
																			// e
																			// configurare
																			// la
																			// gridSpacing
																			// in
																			// funzione
																			// di
																			// spotlight
																			// o
																			// meno
	}// end method

	  public static void findLocalPath() {
	        try {
	            String canonicalPath = new File(".").getCanonicalPath();
	            logger.debug("Current directory path using canonical path method :- " + canonicalPath);
	 
	            String usingSystemProperty = System.getProperty("user.dir");
	            logger.debug("Current directory path using system property:- " + usingSystemProperty);
	 
	        } catch (IOException e) {
	        	logger.debug("IOException Occured" + e.getMessage());
	        }
	    }
	 
	  
	public static String getSRPFVersion() throws IOException, XmlPullParserException {
		
		return "2.2.9p";
//		findLocalPath();
//		String version = null;
//		MavenXpp3Reader reader = new MavenXpp3Reader();
//		
//		Model model = reader.read(new FileReader("/pom.xml"));
//		logger.debug(model.getId());
//		logger.debug(model.getGroupId());
//		logger.debug(model.getArtifactId());
//		logger.debug(model.getVersion());
//		version = model.getVersion();
//		return version;
	}

	/**
	 * Contructor
	 *
	 * @param prdoc
	 * @throws XPathExpressionException
	 */
	public PRRequestParameter(final Document prdoc) throws XPathExpressionException

	{
		this.doc = prdoc;
		try {

			/**
			 * Retrieving ProReqNode
			 */
			NodeList progReqList = prdoc.getElementsByTagNameNS(FeasibilityConstants.ProgReqTagNameNS,
					FeasibilityConstants.ProgReqTagName);
			if (progReqList.getLength() == 0) {
				/**
				 * Just Throw
				 */
				throw new XPathExpressionException("Unable to foung Programming Request");
			}
			Element progReq = (Element) progReqList.item(0);

			/**
			 * evaluating prid and ugs id
			 */
			evaluateProgReqId(prdoc);
			evaluateServiceReqId(prdoc);
			evaluateUGSId(prdoc);

			logger.info("SRPF version :  " + getSRPFVersion());
			try {
				String value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.SPARC_INSTALLATION_DIR_CONF_KEY);

				String checkSumPath = value + "/md5sum.txt";
				logger.debug("SPARC CHECKSUM PATH : "+checkSumPath);
				File file = new File(checkSumPath);

				BufferedReader br = new BufferedReader(new FileReader(file));

				String st;
				while ((st = br.readLine()) != null) {
					
					//  SPARC_V4.1.tar.gz
					if(st.contains("SPARC"))
					{
						String sparcVersion = st.substring(st.indexOf("SPARC"), st.length());
						logger.info("SPARC VERSION : "+sparcVersion);
					}
				}
				br.close();

			} catch (Exception e) {
				DateUtils.getLogInfo(e, logger);
			}
			logger.info("serviceRequestId :  " + this.progReqId);
			logger.info("ProgReqId :  " + this.serviceRequestId);

			/*
			 * try { evaluateProgReqId(prdoc); evaluateUGSId(prdoc); }
			 * catch(NumberFormatException e) { //TODO Gestire eccezione
			 *
			 * throw new XPathExpressionException("Error in parsing ");
			 *
			 * }
			 */
			/**
			 * evaluating list aim
			 */
			evaluateListAim(prdoc);

			/**
			 * evaluating stereo parameters
			 */
			evaluateFeasibilityKind(progReq);
			if (this.stereo) {
				/**
				 * in case of stereo request you must search for min max e delta look angle
				 */
				this.stereoDeltaAngle = getAdditionalDoubleValue(progReq,
						FeasibilityConstants.StereoDeltaLookAngleParameterName);
				this.stereoMinAngle = getAdditionalDoubleValue(progReq,
						FeasibilityConstants.StereoMinLookAngleParameterName);
				this.stereoMaxAngle = getAdditionalDoubleValue(progReq,
						FeasibilityConstants.StereoMaxLookAngleParameterName);

			}

			/**
			 * Retrieving programming parameters node
			 */
			NodeList nl = prdoc.getElementsByTagNameNS(FeasibilityConstants.ProgrammingParametersTagNameNS,
					FeasibilityConstants.ProgrammingParametersTagName);

			if (nl.getLength() == 0) {
				// logger.error("Unable to found" +
				// FeasibilityConstants.ProgrammingParametersTagName);
				/**
				 * Just throw
				 */
				throw new XPathExpressionException(FeasibilityConstants.ProgrammingParametersTagName + " not found! ");
			}

			Element programmingParametersNode = (Element) nl.item(0);

			/**
			 * programming area
			 */

			buildProgrammingAreaRequest(programmingParametersNode);

			/**
			 * check for Di2S and eventually retrieve the minimunAoI
			 */

			evaluateDi2SAvailabilityParameters(programmingParametersNode);

			/**
			 * evaluating mission type
			 */
			NodeList platformNodeList = programmingParametersNode.getElementsByTagNameNS(
					FeasibilityConstants.PlatformTagNameNS, FeasibilityConstants.PlatformTagName);
			for (int i = 0; i < platformNodeList.getLength(); i++) {
				Element platform = (Element) platformNodeList.item(i);
				this.mission = getChildElementText(platform, FeasibilityConstants.MissionTagName,
						FeasibilityConstants.MissionTagNameNS);
				if (this.mission.equalsIgnoreCase(FeasibilityConstants.CSK_NAME)) {
					/**
					 * CSK mission parameters
					 */
					this.sensorModeCSK = getChildElementText(platform, FeasibilityConstants.SensorModeTagName,
							FeasibilityConstants.SensorModeTagNameNS);
					this.allowedSatelliteListCSK = getChildElementListText(platform,
							FeasibilityConstants.SatelliteTagName, FeasibilityConstants.SatelliteTagNameNS);
					this.allowedBeamListCSK = getChildElementListText(platform, FeasibilityConstants.BeamIdTagName,
							FeasibilityConstants.BeamIdTagNameNS);
				} else {
					/**
					 * CSG mission parametrer
					 */
					this.sensorModeCSG = getChildElementText(platform, FeasibilityConstants.SensorModeTagName,
							FeasibilityConstants.SensorModeTagNameNS);
					this.allowedSatelliteListCSG = getChildElementListText(platform,
							FeasibilityConstants.SatelliteTagName, FeasibilityConstants.SatelliteTagNameNS);
					this.allowedBeamListCSG = getChildElementListText(platform, FeasibilityConstants.BeamIdTagName,
							FeasibilityConstants.BeamIdTagNameNS);

				}
			} // end for
			if (platformNodeList.getLength() > 1) {
				/**
				 * We have both CSK and CSG parameters so combined case
				 */
				this.mission = FeasibilityConstants.COMBINED_NAME;
				this.isCombined = true;
			}

			/**
			 * Evaluating Validity interval
			 */
			this.startTime = getChildElementText(programmingParametersNode,
					FeasibilityConstants.PrValidityStartTimeTagName, FeasibilityConstants.PrValidityStartTimeTagNameNS);
			this.stopTime = getChildElementText(programmingParametersNode,
					FeasibilityConstants.PrValidityStopTimeTagName, FeasibilityConstants.PrValidityStopTimeTagNameNS);

			/**
			 * Parsing repetitive periodic constraint
			 */
			parseValidityTimePeriodicity(programmingParametersNode);

			/**
			 * Parsing sensing constraint
			 */
			parseSensingConstraint(programmingParametersNode);

			/**
			 * Searching for required coverage
			 */
			String requiredCoverege = getChildElementText(programmingParametersNode,
					FeasibilityConstants.PRCoveragePercentageRequiredTagName,
					FeasibilityConstants.PRCoveragePercentageRequiredTagNameNS);

			try {
				if (!requiredCoverege.equals("")) {
					this.requiredPercentageOfCoverage = Double.valueOf(requiredCoverege);
				}
			} catch (Exception e) {
				/**
				 * Error just throw
				 */
				throw new XPathExpressionException(e.getMessage());
			}

			/**
			 * Get sensor
			 */
			this.sensor = getChildElementText(programmingParametersNode, FeasibilityConstants.SensorTagName,
					FeasibilityConstants.SensorTagNameNS);

			/**
			 * get required orbit
			 */
			String requiredOrbitDir = getChildElementText(programmingParametersNode,
					FeasibilityConstants.orbitDirectionTagName, FeasibilityConstants.orbitDirectionTagNameNS);
			if (!requiredOrbitDir.equals("")) {
				this.requestedOrbitDirection = FeasibilityConstants.getOrbitDirValue(requiredOrbitDir);
			}

			/**
			 * Check for passThrough
			 */
			String passThrougFlagString = getChildElementText(programmingParametersNode,
					FeasibilityConstants.PassThroughFlagTagName, FeasibilityConstants.PassThroughFlagTagNameNS);
			if (passThrougFlagString.equals(FeasibilityConstants.TruePassThoughString)) {
				/**
				 * if passthrrough retriebe Acquisition Station list
				 */
				this.isPassThrough = true;
				this.acquisitionStationList = getChildElementListText(programmingParametersNode,
						FeasibilityConstants.AcquisitionStationTagName,
						FeasibilityConstants.AcquisitionStationTagNameNS);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			DateUtils.getLogInfo(e1, logger);

		} catch (XmlPullParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			DateUtils.getLogInfo(e1, logger);

			
		}
	} // RequestParameter constructor

	/**
	 * parse the ValidityTimePeriodicity section
	 *
	 * @param programmingParametersNode
	 * @throws XPathExpressionException
	 */
	private void parseValidityTimePeriodicity(Element programmingParametersNode) throws XPathExpressionException {
		/**
		 * Parsing Valitdity Time periodicity
		 */
		NodeList validityTimePeriodicityList = programmingParametersNode.getElementsByTagNameNS(
				FeasibilityConstants.ValidityTimePeriodicityTagNameNS,
				FeasibilityConstants.ValidityTimePeriodicityTagName);

		if (validityTimePeriodicityList.getLength() != 0) {
			/**
			 * Periodic
			 */
			this.periodic = true;
			Element validityTimePeriodicity;
			validityTimePeriodicity = (Element) validityTimePeriodicityList.item(0);
			try {
				/**
				 * Parsing granularity
				 */
				this.periodicGranularity = Integer.parseInt(getChildElementText(validityTimePeriodicity,
						FeasibilityConstants.granularityTagName, FeasibilityConstants.granularityTagNameNS));

				/**
				 * parsing numbero of iteration
				 */
				this.periodicIteration = Integer.parseInt(getChildElementText(validityTimePeriodicity,
						FeasibilityConstants.iterationsTagName, FeasibilityConstants.iterationsTagNameNS));
			} catch (Exception e) {
				/**
				 * Just throw
				 */
				throw new XPathExpressionException("Malformed " + FeasibilityConstants.ValidityTimePeriodicityTagName
						+ " section in Programming Request");
			}

			/**
			 * Repetitive elemnets
			 */
			NodeList list = validityTimePeriodicity.getElementsByTagNameNS(FeasibilityConstants.RepetitiveTagNameNS,
					FeasibilityConstants.RepetitiveTagName);
			if (list.getLength() != 0) {
				this.repetitive = true;
				try {
					/**
					 * number of iteration
					 */
					this.repetitiveIteration = Integer.parseInt(getChildElementText(validityTimePeriodicity,
							FeasibilityConstants.repetitiveIterationTagName,
							FeasibilityConstants.repetitiveIterationTagNameNS));

					/**
					 * granularity
					 */
					this.repetitiveGranularity = Integer.parseInt(getChildElementText(validityTimePeriodicity,
							FeasibilityConstants.repetitiveGranularityTagName,
							FeasibilityConstants.repetitiveGranularityTagNameNS));
				} catch (Exception e) {
					/**
					 * Just throw
					 */
					throw new XPathExpressionException("Malformed "
							+ FeasibilityConstants.ValidityTimePeriodicityTagName + " section in Programming Request");
				}
			}

		} // end if

	}// end parseValidityTimePeriodicity

	/**
	 * Parse for the sensing contsraint
	 *
	 * @throws XPathExpressionException
	 */
	private void parseSensingConstraint(Element programmingParameters) throws XPathExpressionException {
		// String singleMission="";

		// if(!this.isCombined)
		// {
		// singleMission=this.mission;
		// }

		/**
		 * Retrieving sensing constraint node
		 */
		String currentMission;
		NodeList sensingConstraintsList = programmingParameters.getElementsByTagNameNS(
				FeasibilityConstants.SensingConstraintsTagNameNS, FeasibilityConstants.SensingConstraintsTagName);
		Element currentConstraints;
		for (int i = 0; i < sensingConstraintsList.getLength(); i++) {
			/**
			 * for each mission in sensing constraints
			 */
			currentConstraints = (Element) sensingConstraintsList.item(i);
			currentMission = getChildElementText(currentConstraints, FeasibilityConstants.MissionTagName,
					FeasibilityConstants.MissionTagNameNS);
			if (currentMission.equals("")) {
				/**
				 * If no mission set set the mission using the one read from above
				 */
				currentMission = this.mission;
			}

			if (currentMission.equalsIgnoreCase(FeasibilityConstants.CSK_NAME)) {
				/**
				 * Parsing CSK constraints
				 */
				parseCSKCOnstraints(currentConstraints);
			} else {
				/**
				 * Parsing CSG constraints
				 */
				parseCSGConstraints(currentConstraints);
			}

		} // end for

	}// end parseSensingConstraint

	/**
	 * Parse constraints relevant csk mission
	 *
	 * @param constraints
	 * @throws XPathExpressionException
	 */
	private void parseCSKCOnstraints(Element constraints) throws XPathExpressionException {
		/**
		 * Retrieve polarization
		 */
		this.polarizationCSK = getChildElementText(constraints, FeasibilityConstants.PolarizationTagName,
				FeasibilityConstants.PolarizationTagNameNS);

		/*
		 * if(this.polarizationCSK.equals("")) {
		 * this.polarizationCSK=FeasibilityConstants.DefaultPolarization; }
		 */

		/**
		 * Searchiong for min e max lookangle
		 */
		String retString = getChildElementText(constraints, FeasibilityConstants.MinLookAngleTagName,
				FeasibilityConstants.MinLookAngleTagNameNS);
		try {
			if (!retString.equals("")) {
				this.MinLookAngleCSK = Double.valueOf(retString);
				this.HaveCheckMinLookAngleCSK = true;

			} // end if
		} // end try
		catch (Exception e) {
			/**
			 * Error just throw
			 */
			throw new XPathExpressionException(e.getMessage());
		}

		retString = getChildElementText(constraints, FeasibilityConstants.MaxLookAngleTagName,
				FeasibilityConstants.MaxLookAngleTagNameNS);
		try {
			/**
			 * Retrieving min max look angle
			 */
			if (!retString.equals("")) {
				this.MaxLookAngleCSK = Double.valueOf(retString);
				this.HaveCheckMaxLookAngleCSK = true;
			}

		} catch (Exception e) {
			/**
			 * Error just throw
			 */
			throw new XPathExpressionException(e.getMessage());
		}

		/**
		 * Retrieve look side
		 */
		String requiredLookSide = getChildElementText(constraints, FeasibilityConstants.LookSideTagName,
				FeasibilityConstants.LookSideTagNameNS);

		if (!requiredLookSide.equals("")) {
			this.requestedLookSideCSK = FeasibilityConstants.getLookSideValue(requiredLookSide);
		}
		/**
		 * Checking for required satellites
		 */
		List<String> currentList = getChildElementListText(constraints, FeasibilityConstants.SatelliteTagName,
				FeasibilityConstants.SatelliteTagNameNS);
		for (String satName : currentList) {
			this.allowedSatelliteListCSK.add(satName);
		}

		/**
		 * Checking for allowed beam
		 */
		currentList = getChildElementListText(constraints, FeasibilityConstants.BeamIdTagName,
				FeasibilityConstants.BeamIdTagNameNS);
		for (String beamName : currentList) {
			this.allowedBeamListCSK.add(beamName);
		}

	}// end parseCSKCOnstraints

	/**
	 * Parse constraints relevant CSG
	 *
	 * @param constraints
	 * @throws XPathExpressionException
	 */
	private void parseCSGConstraints(Element constraints) throws XPathExpressionException {
		/**
		 * Retrieve polarization
		 */
		this.polarizationCSG = getChildElementText(constraints, FeasibilityConstants.PolarizationTagName,
				FeasibilityConstants.PolarizationTagNameNS);

		/**
		 * if(this.polarizationCSG.equals("")) {
		 * this.polarizationCSG=FeasibilityConstants.DefaultPolarization; }
		 **/
		// Searchiong for min e max lookangle
		String retString = getChildElementText(constraints, FeasibilityConstants.MinLookAngleTagName,
				FeasibilityConstants.MinLookAngleTagNameNS);
		try {
			/**
			 * Checkinfor min look angle
			 */
			if (!retString.equals("")) {
				this.MinLookAngleCSG = Double.valueOf(retString);
				this.HaveCheckMinLookAngleCSG = true;

			}
		} // end try
		catch (Exception e) {
			/**
			 * Error just throw
			 */
			throw new XPathExpressionException(e.getMessage());
		}

		/**
		 * searching for max look angle
		 */
		retString = getChildElementText(constraints, FeasibilityConstants.MaxLookAngleTagName,
				FeasibilityConstants.MaxLookAngleTagNameNS);
		try {
			if (!retString.equals("")) {

				this.MaxLookAngleCSG = Double.valueOf(retString);
				this.HaveCheckMaxLookAngleCSG = true;
			}

		} catch (Exception e) {
			/**
			 * Error just throw
			 */
			throw new XPathExpressionException(e.getMessage());
		}

		/**
		 * look side
		 */
		String requiredLookSide = getChildElementText(constraints, FeasibilityConstants.LookSideTagName,
				FeasibilityConstants.LookSideTagNameNS);

		if (!requiredLookSide.equals("")) {
			this.requestedLookSideCSG = FeasibilityConstants.getLookSideValue(requiredLookSide);
		}
		/**
		 * Checking for required satellites
		 */
		List<String> currentList = getChildElementListText(constraints, FeasibilityConstants.SatelliteTagName,
				FeasibilityConstants.SatelliteTagNameNS);
		for (String satName : currentList) {
			this.allowedSatelliteListCSG.add(satName);
		}

		/**
		 * Checking for allowed beam
		 */

		currentList = getChildElementListText(constraints, FeasibilityConstants.BeamIdTagName,
				FeasibilityConstants.BeamIdTagNameNS);
		for (String beamName : currentList) {
			this.allowedBeamListCSG.add(beamName);
		}
	} // end parseCSGConstraints

	/**
	 * Evaluate the Di2SAvailability Parametes
	 *
	 * @param programmingParamentersNode
	 * @throws XPathExpressionException
	 */
	private void evaluateDi2SAvailabilityParameters(Element programmingParamentersNode)
			throws XPathExpressionException {

		/**
		 * Check for Di2sAvailability
		 */
		NodeList list = programmingParamentersNode.getElementsByTagNameNS(
				FeasibilityConstants.DI2SAvailabilityTagNameNS, FeasibilityConstants.DI2SAvailabilityTagName);
		this.di2sAvailabilityFlag = false;
		if (list.getLength() != 0) {
			Element di2sElement = (Element) list.item(0);
			this.di2sAvailabilityFlag = FeasibilityConstants.DI2SAvailabilityTrueValue
					.equals(di2sElement.getFirstChild().getTextContent());

		} // end if
		
		logger.debug("this.di2sAvailabilityFlag "+this.di2sAvailabilityFlag);

		if (this.di2sAvailabilityFlag) {
			logger.debug("retrieveMinimumAoI ");

			/**
			 * if Di2S retrieve min area of interest
			 */
			retrieveMinimumAoI(programmingParamentersNode);
		} // end if

	}// end evaluateDi2SAvailabilityParameters

	/**
	 * Fill the minimum area of interest in case of Di2SRequest
	 *
	 * @param programmingParamentersNode
	 * @throws XPathExpressionException
	 */
	private void retrieveMinimumAoI(Element programmingParamentersNode) throws XPathExpressionException {
		/**
		 * searchinhg in additional parameters
		 */
		NodeList addParamList = programmingParamentersNode.getElementsByTagNameNS(
				FeasibilityConstants.AdditionalProgrammingParamTagNameNS,
				FeasibilityConstants.AdditionalProgrammingParamTagName);
		Element currentElement;
		boolean foundMinAOI = false;
		this.minimumAoICoincidentWithAoI = false;
		for (int i = 0; i < addParamList.getLength(); i++) {
			/**
			 * Searching for MinAoII
			 */
			currentElement = (Element) addParamList.item(i);
			
			logger.debug("there are addParams?"+currentElement);
			logger.debug(getChildElementText(currentElement, FeasibilityConstants.AdditionalProgrammingNameTagName,
			FeasibilityConstants.AdditionalProgrammingNameTagNameNS));
			if (FeasibilityConstants.MinimumAoIValue
					.equalsIgnoreCase(getChildElementText(currentElement, FeasibilityConstants.AdditionalProgrammingNameTagName,
							FeasibilityConstants.AdditionalProgrammingNameTagNameNS))) {
				this.minimumAoI = getChildElementText(currentElement, FeasibilityConstants.PosListTagName,
						FeasibilityConstants.PosListTagNameNS);
				logger.debug("this.minimumAoI "+this.minimumAoI);

				/**
				 * MinAoii exists so it doesn'n match with target area
				 */
				foundMinAOI = true;
				break;
			}

		} // end for
		
		if (!foundMinAOI)
		{
			this.minimumAoICoincidentWithAoI = true;

		}
		
	}// end retrieveMinimumAoI

	/**
	 * Evaluate the listAim Tag. Only feasubility and FeasibilityExtension are
	 * allowed on SRPF
	 *
	 * @param doc
	 * @throws XPathExpressionException
	 */
	private void evaluateListAim(Document doc) throws XPathExpressionException {
		this.listAim = getChildElementText(doc, FeasibilityConstants.listAimTagName,
				FeasibilityConstants.listAimTagNameNS);
	}

	/**
	 * Evaluate the kind type: interferometric or stereo flag
	 *
	 * @param doc
	 * @throws XPathExpressionException
	 */
	private void evaluateFeasibilityKind(Element el) throws XPathExpressionException {
		this.interferometric = false;
		this.stereo = false;
		/**
		 * We have search in additional parameters
		 */
		NodeList additionalProgrammingParamListl = el.getElementsByTagNameNS(
				FeasibilityConstants.AdditionalProgrammingParamTagNameNS,
				FeasibilityConstants.AdditionalProgrammingParamTagName);

		Element additionalProgrammingParam;
		NodeList stringValueList;
		Node currElem;

		for (int j = 0; j < additionalProgrammingParamListl.getLength(); j++) {
			/**
			 * iterating on additional parameters
			 */
			additionalProgrammingParam = (Element) additionalProgrammingParamListl.item(j);
			stringValueList = additionalProgrammingParam.getElementsByTagNameNS(
					FeasibilityConstants.stringValueTagNameNS, FeasibilityConstants.stringValueTagName);
			for (int i = 0; i < stringValueList.getLength(); i++) {
				currElem = stringValueList.item(i);

				if (currElem.getFirstChild().getTextContent()
						.compareToIgnoreCase(FeasibilityConstants.interferometricValue) == 0) {
					/**
					 * fount interferometric have exit
					 */
					this.interferometric = true;
					break;
				} else if (currElem.getFirstChild().getTextContent()
						.compareToIgnoreCase(FeasibilityConstants.stereoPairsValue) == 0) {
					/**
					 * Found stereo have exit
					 */
					this.stereo = true;
					break;
				}

			} // end for

			/**
			 * have exit because interferometric or stereo
			 */
			if (this.interferometric || this.stereo) {
				break;
			}
		} // end for

	}// end evaluateFeasibilityKind

	/**
	 * Return the doubleValue value of a given additional parameter
	 *
	 * @param el
	 * @param parameterName
	 * @return
	 * @throws FeasibilityException
	 */
	private double getAdditionalDoubleValue(Element el, String parameterName) throws XPathExpressionException {
		/**
		 * Value to be returned
		 */
		double value = 0.0;
		/**
		 * Check flag if parameters has been found
		 */
		boolean found = false;
		/**
		 * Additional parameters list
		 */
		NodeList additionalProgrammingParamListl = el.getElementsByTagNameNS(
				FeasibilityConstants.AdditionalProgrammingParamTagNameNS,
				FeasibilityConstants.AdditionalProgrammingParamTagName);

		Element additionalProgrammingParam;
		NodeList nameList;

		for (int j = 0; j < additionalProgrammingParamListl.getLength(); j++) {
			/**
			 * For each addtional paraeter
			 */
			additionalProgrammingParam = (Element) additionalProgrammingParamListl.item(j);
			/**
			 * Search form name
			 */
			nameList = additionalProgrammingParam.getElementsByTagNameNS(
					FeasibilityConstants.AdditionalProgrammingNameTagNameNS,
					FeasibilityConstants.AdditionalProgrammingNameTagName);
			if (nameList.getLength() > 0) {
				String name = nameList.item(0).getFirstChild().getTextContent();
				if (name.compareToIgnoreCase(parameterName) == 0) {
					/**
					 * Name match the requested parmeter
					 */
					try {
						/**
						 * Getting double Value
						 */
						String doubleValue = XMLUtils.getChildElementText(additionalProgrammingParam,
								FeasibilityConstants.doubleValueTagName, FeasibilityConstants.doubleValueTagNameNS);
						value = Double.parseDouble(doubleValue);
						/**
						 * Found true
						 */
						found = true;
					} catch (Exception e) {
						// found = false;
						/**
						 * Just Log
						 */
						this.tracer.critical(EventType.SOFTWARE_EVENT,
								ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
								"Error in searching additional stereo " + parameterName);

					}
					break;
				} // end if
			} // end if

		} // end for

		if (!found) {
			/**
			 * Just throw
			 */
			throw new XPathExpressionException("Error in searching additional stereo " + parameterName);
		}

		return value;
	}// end method

	/**
	 * Evaluate tehe Programming request ID
	 *
	 * @param doc
	 * @throws XPathExpressionException
	 */
	private void evaluateProgReqId(Document doc) throws XPathExpressionException

	{
		String idString = getChildElementText(doc, FeasibilityConstants.ProgReqIdTagName,
				FeasibilityConstants.ProgReqIdTagNameNS);
		// this.progReqId = Integer.parseInt(idString);
		this.progReqId = idString;
	}// end method

	/**
	 * Evaluate tehe Programming request ID
	 *
	 * @param doc
	 * @throws XPathExpressionException
	 */
	private void evaluateServiceReqId(Document doc) throws XPathExpressionException

	{
		String idString = getChildElementText(doc, FeasibilityConstants.serviceRequestIdTagName,
				FeasibilityConstants.serviceRequestIdTagNameNS);
		// this.progReqId = Integer.parseInt(idString);
		this.serviceRequestId = idString;
	}// end method

	/**
	 * Evaluate the programming request UGS owner
	 *
	 * @param doc
	 * @throws XPathExpressionException
	 */
	private void evaluateUGSId(Document doc) throws XPathExpressionException {
		// Getting string
		String ugsString = getChildElementText(doc, FeasibilityConstants.ProgReqUGSIdTagName,
				FeasibilityConstants.ProgReqUGSIdTagNameNS);
		this.ugsId = Integer.parseInt(ugsString);
	}// end method

	/**
	 * Programming area node
	 *
	 * @param programmingAreaElement
	 * @return The programming area node
	 */
	private Element findAreaTypeElement(Element programmingAreaElement) {

		Node child = null;
		Element retval = null;

		/**
		 * searching inside programmingarea element
		 */
		for (child = programmingAreaElement.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child instanceof Element) {
				String name = child.getLocalName();
				/**
				 * Checking if name id Polygon or CircleByCenterPoint or LineString or Point
				 */
				if (name.equals(FeasibilityConstants.PolygonTagName)
						|| name.equals(FeasibilityConstants.CircleByCenterPointTagName)
						|| name.equals(FeasibilityConstants.LineStringTagName)
						|| name.equals(FeasibilityConstants.IOPPointTagName)) {
					retval = (Element) child;
					/**
					 * Have exit
					 */
					break;
				} // end if

			} // end if
		} // end for
		return retval;
	}// end method

	/**
	 * Build the Programming area request
	 *
	 * @param programmingParametersNode
	 * @throws XPathExpressionException
	 */
	private void buildProgrammingAreaRequest(Element programmingParametersNode) throws XPathExpressionException {
		// logger.info("buildProgrammingAreaRequest");
		/**
		 * Searching programming area
		 */
		NodeList nl = programmingParametersNode.getElementsByTagNameNS(FeasibilityConstants.ProgrammingAreaTagNameNS,
				FeasibilityConstants.ProgrammingAreaTagName);
		if (nl.getLength() == 0) {
			// logger.error("Unable to found" +
			// FeasibilityConstants.ProgrammingAreaTagName);
			/**
			 * Error Just Throw
			 */
			throw new XPathExpressionException(FeasibilityConstants.ProgrammingAreaTagName + " not found! ");
		}

		/**
		 * programming area element
		 */
		Element programmingAreaElement = (Element) nl.item(0);

		// //System.out.println("==================================="+programmingAreaElement.getNodeName()+"===========================");

		// Node intermedio = programmingAreaElement.getFirstChild();

		Element targetElement = findAreaTypeElement(programmingAreaElement);

		if (targetElement == null) {
			// logger.error("Unable to found Programming Area info");
			/**
			 * Just Throw
			 */
			throw new XPathExpressionException("Unable to found Programming Area info");
		}

		// String targetNodeName = targetElement.getNodeName();
		String targetNodeName = targetElement.getLocalName();
		// logger.info("first child "+ programmingAreaElement.getNodeName()+ "
		// of requestType: " +targetNodeName);

		/**
		 * Setting area type
		 */
		if (targetNodeName.equals(FeasibilityConstants.PolygonTagName)) {
			/**
			 * Polygon
			 */
			this.requestType = PolygonRequestType;
			/**
			 * retrieving pos list
			 */
			this.posList = getChildElementText(targetElement, FeasibilityConstants.PosListTagName,
					FeasibilityConstants.PosListTagNameNS);

			if (this.posList.equals("")) {
				// logger.error("Polygonal programmining area without posList");
				/**
				 * just throw
				 */
				throw new XPathExpressionException("Polygonal programmining area without posList");
			}

			nl = programmingParametersNode.getElementsByTagNameNS(FeasibilityConstants.TargetCenteredPointTagNameNS,
					FeasibilityConstants.TargetCenteredPointTagName);
			if (nl.getLength() != 0) {
				/**
				 * Found target center
				 */
				Element targetCenterEl = (Element) nl.item(0);
				this.hasTargetCenter = true;
				this.targetCenter = getChildElementText(targetCenterEl, FeasibilityConstants.PosTagName,
						FeasibilityConstants.PosTagNameNS);
				this.evaluateTargetCenterInLLH(this.targetCenter);
			} // end if

		} // end if
		else if (targetNodeName.equals(FeasibilityConstants.CircleByCenterPointTagName)) {
			/**
			 * Circle
			 */

			this.requestType = CircleRequestType;
			this.hasTargetCenter = true;
			this.targetCenter = getChildElementText(targetElement, FeasibilityConstants.PosTagName,
					FeasibilityConstants.PosTagNameNS);
			/**
			 * By default target center is the circle center
			 */
			this.evaluateTargetCenterInLLH(this.targetCenter);
			this.circleRadius = getChildElementText(targetElement, FeasibilityConstants.RadiusTagName,
					FeasibilityConstants.RadiusTagNameNS);

		} else if (targetNodeName.equals(FeasibilityConstants.LineStringTagName)) {
			/**
			 * Line string
			 */

			this.requestType = lineRequestType;
			this.lineRequestString = getChildElementText(targetElement, FeasibilityConstants.PosListTagName,
					FeasibilityConstants.PosListTagNameNS);
			/**
			 * retrieving pos list
			 */
			this.posList = getChildElementText(targetElement, FeasibilityConstants.PosListTagName,
					FeasibilityConstants.PosListTagNameNS);

			if (this.posList.equals("")) {
				// logger.error("Polygonal programmining area without posList");
				/**
				 * Just throw
				 */
				throw new XPathExpressionException("Polygonal programmining area without posList");
			}
			nl = programmingParametersNode.getElementsByTagNameNS(FeasibilityConstants.TargetCenteredPointTagNameNS,
					FeasibilityConstants.TargetCenteredPointTagName);
			if (nl.getLength() != 0) {
				/**
				 * evaluating target center
				 */
				Element targetCenter = (Element) nl.item(0);
				this.hasTargetCenter = true;
				this.targetCenter = getChildElementText(targetCenter, FeasibilityConstants.PosTagName,
						FeasibilityConstants.PosTagNameNS);
				this.evaluateTargetCenterInLLH(this.targetCenter);
			}
		} else if (targetNodeName.equals(FeasibilityConstants.IOPPointTagName))
		// else if(targetNodeName.equals(FeasibilityConstants.IOPPointTagName))
		{
			/**
			 * Case point
			 */
			this.requestType = pointRequestType;
			this.posList = getChildElementText(targetElement, FeasibilityConstants.PosTagName,
					FeasibilityConstants.PosTagNameNS);
			/**
			 * The target center is the point
			 */
			this.evaluateTargetCenterInLLH(this.posList);
			this.targetCenter = this.posList;
			this.hasTargetCenter = true;
			try {
				/**
				 * Check if point duration
				 */
				String duration = getChildElementText(targetElement, FeasibilityConstants.DurationTagName,
						FeasibilityConstants.DurationTagNameNS);
				this.durationForPuntualPR = Integer.parseInt(duration);
				this.requestType = pointRequestWithDuration;

			} catch (Exception e) {
				this.durationForPuntualPR = 0;
				this.tracer.warning(EventType.APPLICATION_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Error in evaluating duration in Point duration request: " + e.getMessage());
				/**
				 * Error just log
				 */
				// Duration not available
			}
		} else {
			/**
			 * Error just throw
			 */
			// logger.error("unknown programming area type: " +targetNodeName);
			throw new XPathExpressionException("unknown programming area type: " + targetNodeName);
		}
	}// buildProgrammingAreaRequest

	/**
	 * Return the targeted point or null in case of error;
	 *
	 * @return the targeted poin in llh or null in case of error
	 */
	double[] getTargetedPointLLH() {

		if (this.targetCenterLLH == null) {
			/**
			 * Tokeinze string
			 */
			StringTokenizer tokens = new StringTokenizer(this.targetCenter);

			if (tokens.countTokens() < 2) {
				/**
				 * wrong numer of tokens just return
				 */
				return null;
			} // end if

			try {
				/**
				 * Parsing values
				 */
				double lat = Double.valueOf(tokens.nextToken());
				double longitude = Double.valueOf(tokens.nextToken());
				/**
				 * Setting latitude and Longitute
				 */
				this.targetCenterLLH = new double[3];
				this.targetCenterLLH[0] = lat;
				this.targetCenterLLH[1] = longitude;
				this.targetCenterLLH[2] = 0;
			} // end try
			catch (Exception e) {
				this.targetCenterLLH = null;
			}
		} // end if

		return this.targetCenterLLH;

	}// end getTargetedPointLLH

	/**
	 * Set the target center
	 *
	 * @param llh
	 */
	public void setTargetCenter(double[] llh) {
		// setting true
		this.hasTargetCenter = true;
		this.targetCenterLLH = llh;
	}// end method

	/**
	 * Set the target center starting from a pos list
	 *
	 * @param center
	 *
	 *               public void setTargetCenter(String center) {
	 *               this.evaluateTargetCenterInLLH(center); }//end method
	 */

	/**
	 * Evalyuate the target point in LLH
	 */
	private void evaluateTargetCenterInLLH(String center) {
		// double[] targetPoint=null;
		/**
		 * Target if null
		 */
		this.targetCenterLLH = null;
		StringTokenizer tokens = new StringTokenizer(center);

		/**
		 * Wrong tokens return
		 */
		if (tokens.countTokens() < 2) {

			return;
		}

		try {
			/**
			 * Parsing values
			 */
			double lat = Double.valueOf(tokens.nextToken());
			double longitude = Double.valueOf(tokens.nextToken());
			/**
			 * Setting latitude and Longitute
			 */
			this.targetCenterLLH = new double[3];
			this.targetCenterLLH[0] = lat;
			this.targetCenterLLH[1] = longitude;
			this.targetCenterLLH[2] = 0;
		} // end try
		catch (Exception e) {
			this.targetCenterLLH = null;
		} // enf catch
	}// end evaluateTargetCenterInLLH

	/**
	 *
	 * @return the programming request ID
	 */
	public String getProgReqId()

	{
		return this.progReqId;
	}// end method

	/**
	 * Set the programming request id
	 *
	 * @param progReqId
	 */
	public void setProgReqId(String progReqId) {
		this.progReqId = progReqId;
	}// end method

	/**
	 * @return the requestType
	 */
	public int getRequestProgrammingAreaType() {
		return this.requestType;
	}// end method

	/**
	 * @return the doc
	 */
	public Document getDoc() {
		return this.doc;
	}// end method

	/**
	 * @param doc the doc to set
	 */
	public void setDoc(Document doc) {
		this.doc = doc;
	}// end method

	/**
	 * @return the polarization for CSK
	 */
	public String getPolarizationCSK() {
		return this.polarizationCSK;
	}

	/**
	 * @param set the polarization for csk
	 */
	public void setPolarizationCSK(String polarization) {
		this.polarizationCSK = polarization;
	}// end method

	/**
	 * @return the polarization for CSG
	 */
	public String getPolarizationCSG() {
		return this.polarizationCSG;
	}// end method

	/**
	 * @param set the polarization for csG
	 */
	public void setPolarizationCSG(String polarization) {
		this.polarizationCSG = polarization;
	}// end method

	/**
	 * @return the minLookAngle for CSK
	 */
	public double getMinLookAngleCSK() {
		return this.MinLookAngleCSK;
	}// end method

	/**
	 * @param minLookAngle the minLookAngle to set for CSK
	 */
	public void setMinLookAngleCSK(double minLookAngle) {
		this.MinLookAngleCSK = minLookAngle;
	}// end method

	/**
	 * @return the minLookAngle for CSG
	 */
	public double getMinLookAngleCSG() {
		return this.MinLookAngleCSG;
	}// end method

	/**
	 * @param minLookAngle the minLookAngle to set for CSG
	 */
	public void setMinLookAngleCSG(double minLookAngle) {
		this.MinLookAngleCSG = minLookAngle;
	}// end method

	/**
	 * @return the maxLookAngle for CSK
	 */
	public double getMaxLookAngleCSK() {
		return this.MaxLookAngleCSK;
	}// end method

	/**
	 * @param maxLookAngle the maxLookAngle to set CSK
	 */
	public void setMaxLookAngleCSK(double maxLookAngle) {
		this.MaxLookAngleCSK = maxLookAngle;
	}// end method

	/**
	 * @return the maxLookAngle for CSG
	 */
	public double getMaxLookAngleCSG() {
		return this.MaxLookAngleCSG;
	}// end method

	/**
	 * @param maxLookAngle the maxLookAngle to set CSG
	 */
	public void setMaxLookAngleCSG(double maxLookAngle) {
		this.MaxLookAngleCSG = maxLookAngle;
	}// end method

	/**
	 * @return the allowedSatelliteList for CSK
	 */
	public List<String> getAllowedSatelliteListCSK() {
		return this.allowedSatelliteListCSK;
	}// end method

	/**
	 * @param allowedSatelliteList the allowedSatelliteList to set CSK
	 */
	public void setAllowedSatelliteListCSK(List<String> allowedSatelliteList) {
		this.allowedSatelliteListCSK = allowedSatelliteList;
	}// end method

	/**
	 * @return the allowedSatelliteList for CSG
	 */
	public List<String> getAllowedSatelliteListCSG() {
		return this.allowedSatelliteListCSG;
	}// end method

	/**
	 * @param allowedSatelliteList the allowedSatelliteList to set CSG
	 */
	public void setAllowedSatelliteListCSG(List<String> allowedSatelliteList) {
		this.allowedSatelliteListCSG = allowedSatelliteList;
	}// end method

	/**
	 * @return the allowedBeamList for CSK
	 */
	public List<String> getAllowedBeamListCSK() {
		return this.allowedBeamListCSK;
	}// end method

	/**
	 * @param allowedBeamList the allowedBeamList to set CSK
	 */
	public void setAllowedBeamListCSK(List<String> allowedBeamList) {
		this.allowedBeamListCSK = allowedBeamList;
	}// end method

	/**
	 * @return the allowedBeamList for CSG
	 */
	public List<String> getAllowedBeamListCSG() {
		return this.allowedBeamListCSG;
	}// end method

	/**
	 * @param allowedBeamList the allowedBeamList to set CSG
	 */
	public void setAllowedBeamListCSG(List<String> allowedBeamList) {
		this.allowedBeamListCSG = allowedBeamList;
	}// end method

	/**
	 * @return the sensor
	 */
	public String getSensor() {
		return this.sensor;
	}// end method

	/**
	 * @param sensor the sensor to set
	 */
	public void setSensor(String sensor) {
		this.sensor = sensor;
	}// end method

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}// end method

	/**
	 * @param stopTime the stopTime to set
	 */
	public void setStopTime(String stopTime) {
		this.stopTime = stopTime;
	}// end method

	/**
	 * @param mission the mission to set
	 */
	public void setMission(String mission) {
		this.mission = mission;
	}// end method

	/**
	 * @param posList the posList to set
	 */
	public void setPosList(String posList) {
		this.posList = posList;
	}// end method

	/**
	 * @param sensorMode the sensorMode to set CSK
	 */
	public void setSensorModeCSK(String sensorMode) {
		this.sensorModeCSK = sensorMode;
	}// end method

	/**
	 * @param sensorMode the sensorMode to set CSG
	 */
	public void setSensorModeCSG(String sensorMode) {
		this.sensorModeCSG = sensorMode;
	}// end method

	/**
	 * Check if the access meets the PR request constraint
	 *
	 * @param access
	 * @return true if valid
	 */
	public boolean isValidAccess(Access access)

	{

		boolean isValid = true;

		/**
		 * check if access matches for look side
		 */

		isValid = isValidLookSide(access.getLookSide(), access.getSatellite().getMissionName())
				&& isValidOrbitDirection(access.getOrbitDirection());

		///

		return isValid;

	}// end method

	/**
	 * Check if a string is in the a list. If lowerCase true convert the string and
	 * list item to lowecase. It also returns true if the list is empty
	 *
	 * @param element
	 * @param list
	 * @param toLowerCase flag
	 * @return true if the element is in the list or if the list is empty
	 */
	private boolean checkIfStringIsInlist(String element, List<String> list, boolean toLowerCase) {
		/**
		 * if empty list true by default
		 */
		boolean retval = true;

		for (String s : list) {

			retval = false;
			/**
			 * lowercase true
			 */
			if (toLowerCase) {
				if (element.toLowerCase().equals(s.toLowerCase())) {
					return true;
				}
			} else {
				if (element.equals(s)) {
					return true;
				}
			}
		}

		return retval;
	}// end method

	/**
	 * check if the lookside for match the request
	 *
	 * @param lookSide
	 * @param mission
	 * @return treu if match
	 */
	public boolean isValidLookSide(int lookSide, String mission) {
		/**
		 * return flag
		 */
		boolean isValid = false;

		/**
		 * default check on csk
		 */
		int requestedLookSide = this.requestedLookSideCSK;
		if (mission.equalsIgnoreCase(FeasibilityConstants.CSG_NAME)) {
			/**
			 * if csg ccheck on CSG
			 */
			requestedLookSide = this.requestedLookSideCSG;
		}

		if ((requestedLookSide == FeasibilityConstants.BothLookSide) || (lookSide == requestedLookSide)) {
			isValid = true;
		}

		return isValid;
	}// end method

	/**
	 * check if the orbit direction for match the request
	 *
	 * @param orbitDirection
	 * @return
	 */
	public boolean isValidOrbitDirection(int orbitDirection) {
		boolean isValid = false;
		/**
		 * Check on orbit
		 */
		if ((this.requestedOrbitDirection == FeasibilityConstants.BothOrbitDirection)
				|| (this.requestedOrbitDirection == FeasibilityConstants.AnyOrbitDirection)
				|| (orbitDirection == this.requestedOrbitDirection)) {
			isValid = true;
		}

		return isValid;
	}// end method

	/**
	 * @return the requestedLookSide CSK
	 */
	public int getRequestedLookSideCSK() {
		return this.requestedLookSideCSK;
	}// end method

	/**
	 * @param requestedLookSide the requestedLookSide to set CSK
	 */
	public void setRequestedLookSideCSK(int requestedLookSide) {
		this.requestedLookSideCSK = requestedLookSide;
	}// end method

	/**
	 * @return the requestedLookSide CSG
	 */
	public int getRequestedLookSideCSG() {
		return this.requestedLookSideCSG;
	}// end method

	/**
	 * @param requestedLookSide the requestedLookSide to set CSG
	 */
	public void setRequestedLookSideCSG(int requestedLookSide) {
		this.requestedLookSideCSG = requestedLookSide;
	}// end method

	/**
	 * @return the requestedOrbitDirection
	 */
	public int getRequestedOrbitDirection() {
		return this.requestedOrbitDirection;
	}// end method

	/**
	 * @param requestedOrbitDirection the requestedOrbitDirection to set
	 */
	public void setRequestedOrbitDirection(int requestedOrbitDirection) {
		this.requestedOrbitDirection = requestedOrbitDirection;
	}// end method

	/**
	 * @return the sensorMode CSK
	 */
	public String getSensorModeCSK() {
		return this.sensorModeCSK;
	}// end method

	/**
	 * @return the sensorMode CSG
	 */
	public String getSensorModeCSG() {
		return this.sensorModeCSG;
	}// end method

	/**
	 * @return the startTime
	 */
	public String getStartTime() {
		return this.startTime;
	}// end method

	/**
	 * @return the stopTime
	 */
	public String getStopTime() {
		return this.stopTime;
	}// end method

	/**
	 * @return the mission
	 */
	public String getMission() {
		return this.mission;
	}// end method

	/**
	 *
	 * @return the posList
	 */
	public String getPosList()

	{
		return this.posList;
	}// end method

	/**
	 * Check if the request is polar
	 *
	 * @return true if polar request
	 * @throws GridException
	 */
	public boolean isPolarRequest() throws GridException {
		// logger.debug("getPolygonEnclosingArea");
		/**
		 * we have check on circle
		 */
		if (this.requestType == CircleRequestType) {
			return isPolarCircle();
		}

		boolean isPolar = false;
		/**
		 * For punctual request polar make no sense
		 */
		if ((this.requestType == pointRequestType) || (this.requestType == pointRequestWithDuration)) {
			// throw new GridException("unimplemented request");
			return isPolar;
		} // end if

		StringTokenizer tokens = new StringTokenizer(this.posList);

		if ((tokens.countTokens() == 0) || (((tokens.countTokens()) % 2) != 0)) // No
																				// elements
																				// or
																				// spare
																				// elements
		{
			/**
			 * Error on tokens just throw
			 */
			throw new GridException("Wrong Polygon");
		} // end if

		int numberOfPoint = tokens.countTokens() / 2;

		// logger.info("Number of point " + numberOfPoint);

		double latitude = 0;

		while (tokens.hasMoreElements()) {
			/**
			 * Iterting on tokens
			 */
			try {
				/**
				 * parsing tokens latitude
				 */
				latitude = latitude + Double.valueOf(tokens.nextToken());
				tokens.nextToken();

			} // end try
			catch (NumberFormatException e) {
				/**
				 * Just Throw
				 */
				throw new GridException(e.getMessage());
			}

		} // end while

		/**
		 * mean latitude
		 */
		latitude = latitude / numberOfPoint;

		/**
		 * if mean latitude above polar limit polar request
		 */
		if (Math.abs(latitude) > Math.abs(this.polarLimit)) {
			isPolar = true;
		}

		return isPolar;
	} // end getPolygonEnclosingArea

	/**
	 * check if a circle is polar
	 *
	 * @return
	 * @throws GridException
	 */
	private boolean isPolarCircle() throws GridException {
		boolean isPolar = false;
		// this.targetCenter;
		/**
		 * Traget center for cirlebypoint request is the center of circle
		 */
		StringTokenizer tokens = new StringTokenizer(this.targetCenter);

		if (tokens.countTokens() != 2) // No elements or spare elements
		{
			/**
			 * Just throw
			 */
			throw new GridException("Wrong center");
		}

		double latitude;
		try {
			/**
			 * parsing latitude
			 */
			latitude = Double.valueOf(tokens.nextToken());
		} catch (NumberFormatException e) {
			/**
			 * jaust throw
			 */
			throw new GridException(e.getMessage());
		}
		/**
		 * evaluating polar flag
		 */
		if (Math.abs(latitude) > Math.abs(this.polarLimit)) {
			isPolar = true;
		}

		return isPolar;
	}// end method

	/**
	 * Get the text of a children of an elemennt
	 *
	 * @param elementFather
	 * @param childName
	 * @param childNS
	 * @return
	 * @throws XPathExpressionException
	 */
	private String getChildElementText(Element elementFather, String childName, String childNS)
			throws XPathExpressionException

	{
		/**
		 * Return the text on a child element
		 */
		String retVal = "";

		/**
		 * Searching chils
		 */
		NodeList nl = elementFather.getElementsByTagNameNS(childNS, childName);

		if (nl.getLength() != 0) {
			/**
			 * Found child returning text
			 */
			retVal = nl.item(0).getFirstChild().getTextContent();
		}

		return retVal;
	} // end getChilElementText

	/**
	 * get the text of a an element
	 *
	 * @param doc
	 * @param childName
	 * @return
	 * @throws XPathExpressionException
	 */
	private String getChildElementText(Document doc, String childName, String childNS) throws XPathExpressionException

	{
		String retVal = "";
		/**
		 * Searching chils
		 */
		NodeList nl = doc.getElementsByTagNameNS(childNS, childName);

		if (nl.getLength() != 0) {
			/**
			 * Found child returning text
			 */
			retVal = nl.item(0).getFirstChild().getTextContent();
		}

		return retVal;
	} // end getChilElementText

	/**
	 * Fill a list with text of child elemnets
	 *
	 * @param elementFather
	 * @param childName
	 * @param childNS
	 * @return list of element text
	 */
	private List<String> getChildElementListText(Element elementFather, String childName, String childNS) {
		/**
		 * ret list
		 */
		List<String> list = new ArrayList<>();

		/**
		 * Child list
		 */
		NodeList nl = elementFather.getElementsByTagNameNS(childNS, childName);

		String appo;

		for (int i = 0; i < nl.getLength(); i++) {

			/**
			 * each child add text in list
			 */
			appo = nl.item(i).getFirstChild().getTextContent();

			list.add(appo);
		}

		return list;
	}// end method

	/**
	 *
	 * @return the ugs id owner of the request
	 */
	public int getUgsId() {
		return this.ugsId;
	}// end method

	/**
	 *
	 * @return the duration for a request of type point duration 0 otherwise
	 */
	public int getDurationForPuntualPR() {
		return this.durationForPuntualPR;
	}// end method

	/**
	 *
	 * @return the Di2SAvailability flag
	 */
	public boolean isDi2sAvailabilityFlag() {
		return this.di2sAvailabilityFlag;
	}// end method

	/**
	 * Deafult condtructor
	 */
	public PRRequestParameter()

	{
		// TODO Auto-generated constructor stub
	}// end method

	/**
	 * Clone the object
	 */
	@Override
	public Object clone() {

		try {
			// call super class
			return super.clone();
		} // end try
		catch (Exception e) {
			throw new Error("Something impossible just happened");
		} // end catch
	}// end method

} // end RequestParameter class

/*
 *
 * class GMLContext implements NamespaceContext
 *
 * {
 *
 * @Override public String getNamespaceURI(String prefix)
 *
 * {
 *
 * if(prefix == null)
 *
 * { throw new IllegalArgumentException("prefix is null");
 *
 *
 * } else if(prefix.equals("gml")) { return "http://www.opengis.net/gml/3.2"; }
 * else { return null; }
 *
 *
 * }
 *
 * @Override public String getPrefix(String uri)
 *
 * { // TODO Auto-generated method stub return null; }
 *
 * @Override public Iterator getPrefixes(String uri)
 *
 * { // TODO Auto-generated method stub return null; }
 *
 *
 * }
 *
 *
 *
 * class IOPppContext implements NamespaceContext
 *
 * {
 *
 * @Override public String getNamespaceURI(String prefix)
 *
 * {
 *
 * if(prefix == null)
 *
 * { throw new IllegalArgumentException("prefix is null");
 *
 *
 * } else if(prefix.equals("IOPpp")) { return
 * "http://www.telespazio.com/IOP/schemas/programming"; } else { return null; }
 *
 *
 * }
 *
 * @Override public String getPrefix(String uri)
 *
 * { // TODO Auto-generated method stub return null; }
 *
 * @Override public Iterator getPrefixes(String uri)
 *
 * { // TODO Auto-generated method stub return null; }
 *
 *
 * }
 *
 *
 * class IOPcmContext implements NamespaceContext
 *
 * {
 *
 * @Override public String getNamespaceURI(String prefix)
 *
 * {
 *
 * if(prefix == null)
 *
 * { throw new IllegalArgumentException("prefix is null");
 *
 *
 * } else if(prefix.equals("IOPcm")) {
 *
 *
 * return "http://www.telespazio.com/IOP/schemas/common"; } else { return null;
 * }
 *
 *
 * }
 *
 * @Override public String getPrefix(String uri)
 *
 * { // TODO Auto-generated method stub return null; }
 *
 * @Override public Iterator getPrefixes(String uri)
 *
 * { // TODO Auto-generated method stub return null; }
 *
 *
 *
 *
 * }//end class
 */
