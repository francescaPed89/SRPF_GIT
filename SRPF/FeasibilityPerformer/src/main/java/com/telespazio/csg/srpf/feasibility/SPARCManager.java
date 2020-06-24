/**
*
* MODULE FILE NAME:	SPARCManager.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			It generates input to be sent to SPARC proxy ed elaborates output
*
* PURPOSE:			Used to interact with SPARC
*
* CREATION DATE:	26-10-2016
*
* AUTHORS:			Amedeo Bancone
*
* DESIGN ISSUE:		1.2
*
* INTERFACES:
*
* SUBORDINATES:
*
* MODIFICATION HISTORY:
*
*             Date                |  Name      |   New ver.    | Description
* --------------------------+------------+----------------+-------------------------------
* 14-03-2018 | Amedeo Bancone  |1.1 | Modified to interact with the new version of SPARC implementing the modification related to passthrough
* --------------------------+------------+----------------+-------------------------------
* 05-04-2018 | Amedeo Bancone  |1.2 | Fix Bug on creation of sparc input in case of single acquisition
* 									  Synchronized 	start of new SPARC processes
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.feasibility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.referencing.GeodeticCalculator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.telespazio.csg.srpf.dataManager.bean.EpochBean;
import com.telespazio.csg.srpf.dataManager.inMemoryOrbitalData.EphemeridInMemoryDB;
import com.telespazio.csg.srpf.dem.DEMManager;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.DateUtils;
import com.telespazio.csg.srpf.utils.PropertiesReader;
import com.telespazio.csg.srpf.utils.ReferenceFrameUtils;
import com.telespazio.csg.srpf.utils.XMLUtils;

/**
 * This class is responsible to manage the AR to be passed to SPARCProxy. It
 * generates input file and elaborates the output.
 *
 * @author Amedeo Bancone
 * @version 1.2
 *
 *
 */
public class SPARCManager

{
	static final Logger logger = LogManager.getLogger(SPARCManager.class.getName());

	// logger
	private TraceManager tracer = new TraceManager();
	// This section held the xml tag for spac input / output file

	/**
	 * Used to sincronize the forckexec of new sparc process
	 */
	protected final static Object synchronizationObject = new Object();

	// number of seconds to be added at begin and at the end of the DTO interval
	// to build a timeline
	protected static long numberOfSecondsForExtremeOfinterval = 360;

	// XML TAG
	protected static String DTOListTagName = "DTOList";
	// XML TAG
	protected static String xsiAttr = "xmlns:xsi";
	// XML TAG
	protected static String xsiAttrValue = "http://www.w3.org/2001/XMLSchema-instance";
	// XML TAG
	protected static String cmnAttr = "xmlns:cmn";
	// XML TAG
	protected static String cmnAttrValue = "Common";
	// XML TAG
	protected static String noNamespaceSchemaLocationAttr = "xsi:noNamespaceSchemaLocation";

	// Is different in refinement so is not class attribute
	protected String noNamespaceSchemaLocationAttrValue = "FeasibilityInput.xsd";
	// XML TAG
	protected static String DTOTagName = "DTO";
	// XML TAG
	protected static String DTOInfoTagName = "DTOInfo";

	// XML TAG
	protected static String PRIDTagName = "PRID";
	// XML TAG
	protected static String ARIDTagName = "ARID";
	// XML TAG
	protected static String DTOIDTagName = "DTOID";
	// XML TAG
	protected static String SPARCModeTagName = "SPARCMode";
	// XML TAG
	protected static String StripmapFlagTagName = "StripmapFlag";
	// XML TAG
	protected static String DI2SFlagTagName = "DI2SFlag";
	// XML TAG
	protected static String SARModeTagName = "SARMode";
	// XML TAG
	protected static String SARBeamTagName = "SARBeam";
	// XML TAG
	protected static String SARPolarizationTagName = "SARPolarization";
	// XML TAG
	protected static String SARLookTagName = "SARLook";
	// XML TAG
	protected static String SAROrbitDirectionTagName = "SAROrbitDirection";
	// XML TAG
	protected static String DTOEarlyNearCornerTagName = "DTOEarlyNearCorner";
	// XML TAG
	protected static String DTOEarlyFarCornerTagName = "DTOEarlyFarCorner";
	// XML TAG
	protected static String DTOLateNearCornerTagName = "DTOLateNearCorner";
	// XML TAG
	protected static String DTOLateFarCornerTagName = "DTOLateFarCorner";
	// XML TAG
	protected static String DTONearCornerTagName = "DTONearCorner";
	// XML TAG
	protected static String DTOFarCornerTagName = "DTOFarCorner";

	// XML TAG
	protected static String LatitudeTagName = "cmn:Latitude";
	// XML TAG
	protected static String LongitudeTagName = "cmn:Longitude";
	// XML TAG
	protected static String HeightTagName = "cmn:Height";
	// XML TAG
	protected static String AOIInfoTagName = "AOIInfo";
	// XML TAG
	protected static String MinimumAOIInfoTagName = "MinimumAOIInfo";
	// XML TAG
	protected static String OffNadirNearTagName = "OffNadirNear";
	// XML TAG
	protected static String OffNadirFarTagName = "OffNadirFar";
	// XML TAG
	protected static String DTOHeightTagName = "DTOHeight";
	// XML TAG
	protected static String AzimuthCutTagName = "AzimuthCut";
	// XML TAG
	protected static String countAttrName = "count";
	// XML TAG
	protected static String SampleTagName = "Sample";
	// XML TAG
	protected static String SampleIDTagName = "SampleID";
	// XML TAG
	protected static String AccessTimeTagName = "AccessTime";
	// XML TAG
	protected static String SatellitePositionTagName = "SatellitePosition";
	// XML TAG
	protected static String SatelliteRelativeVelocityTagName = "SatelliteRelativeVelocity";

	// protected static String
	// SatelliteInertialVelocityTagName="SatelliteInertialVelocity";
	protected static String XTagName = "cmn:X";
	protected static String YTagName = "cmn:Y";
	protected static String ZTagName = "cmn:Z";

	// XML TAG
	protected static String SatelliteHeightTagName = "SatelliteHeight";
	// XML TAG
	protected static String SubSatelliteHeightTagName = "SubSatelliteHeight";
	// XML TAG
	protected static String OffNadirNearHeightTagName = "OffNadirNearHeight";
	// XML TAG
	protected static String OffNadirFarHeightTagName = "OffNadirFarHeight";
	// XML TAG
	protected static String OffNadirRangeHeightTagName = "OffNadirRangeHeight";
	// XML TAG
	protected static String SparcInfoTagName = "SPARCInfo";
	// XML TAG
	protected static String DTOSizeTagName = "DTOSize";
	// XML TAG
	protected static String trueFlag = "true";
	protected static String falseFlag = "false";

	// constant for converting julian to time used by sparc
	protected static double sparcTimeConversion = 86400.0;
	//

	// pr
	protected PRRequestParameter programmingRequest = null;
	// AR list
	protected List<AcqReq> acqReqList = null;
	// working dir
	protected String workingDir = "";
	// SPARC command string
	protected final String SPARCFeasibilityActivity = "DTOParametersCalculation";
	// sparc path
	protected final String sparcCommand = "sparc_bin/sparc.out";
	// sparc dir
	protected String SparcDirectory = "/opt/SRPF/SPARC";
	// spar log file
	protected final String SparcLogFileName = "sparc.log";
	// sparc input for feas
	protected String sparcInputFileName = "sparc_input.xml";
	// spar out for feas
	protected String sparcOutputFileName = "sparc_output.xml";
	// Feasibility schema
	protected String outPutSchema = "/opt/SRPF/SPARC/XML_SCHEMAS/FeasibilityOutput.xsd";

	// private String
	// refinementOutSchema="/opt/SRPF/SPARC/XML_SCHEMAS/RefinementOutput.xsd";
	// DEM
	protected DEMManager dem = null;
	// true if over line of date
	protected boolean isOverDateLine = false;
	// sparc more
	protected int sparcMode = 0;
	// treu for single acquisition
	private boolean isSingleAcquired = false;

	// Value to check if we are near the line of date
	protected double longitudeLimitToUnderstandForLineDate = 160.0;

	// di2sConfirmation flag
	protected boolean di2sAvailabilityConfirmationFlag = false;

	// in case of combined it could happen taht we only have CSK DTO, so sparc
	// manager must not be called
	protected boolean foundDTO = false;

	/**
	 * Constructor Construct a SPARCManager
	 *
	 * @param sparcMode
	 * @param programmingRequest Programming request
	 * @param acqReqList         list of acquisition request
	 * @param workingDir         folder used to exchange files
	 * @param isSingleAcq        true for single acquisition area
	 * @param isOverDateLine     used to inform the the PR is across the line date
	 * @throws Exception
	 */
	public SPARCManager(int sparcMode, PRRequestParameter programmingRequest, List<AcqReq> acqReqList,
			String workingDir, DEMManager dem, boolean isSingleAcq, boolean isOverdateLine, boolean di2Sconfiramtion)
			throws Exception

	{

		logger.debug("PRREQUESTPARAMETER STOP TIME ------------------------------------------------------ "+programmingRequest.getStopTime());
		double dateAsDouble = DateUtils.fromISOToCSKDate(programmingRequest.getStopTime());
		logger.debug("PRREQUESTPARAMETER dateAsDouble ------------------------------------------------------ "+dateAsDouble);

		// settinhg parameters
		this.dem = dem;
		this.programmingRequest = programmingRequest;

		this.acqReqList = acqReqList;

		this.workingDir = workingDir;

		this.isOverDateLine = isOverdateLine;

		this.sparcMode = sparcMode;

		this.di2sAvailabilityConfirmationFlag = di2Sconfiramtion;

		this.isSingleAcquired = isSingleAcq;
		// initialiizing
		initialize();
		// building input
		buildInputFileForFeasibility(dateAsDouble);
		/*
		 * try
		 *
		 * { buildInputFileForFeasibility(); }
		 *
		 * catch (FileNotFoundException | UnsupportedEncodingException | GridException |
		 * ParserConfigurationException | TransformerException e)
		 *
		 * { // TODO Auto-generated catch block e.printStackTrace(); throw e; }
		 */

	}// end constructor

	/**
	 * Default constructor
	 */
	public SPARCManager() {
		initialize();
	}// end constructor

	/**
	 * retrieve info from configuration
	 */
	private void initialize() {
		String value = PropertiesReader.getInstance()
				.getProperty(FeasibilityConstants.LONGITUDE_LIMIT_TO_UNDERSTAND_FOR_LINE_DATE_CONF_KEY);
		if (value != null) {
			try {
				double dValue = Double.valueOf(value);
				this.longitudeLimitToUnderstandForLineDate = dValue;
			} // end try
			catch (Exception e) {
				// just log

				this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"Unable to found " + FeasibilityConstants.LONGITUDE_LIMIT_TO_UNDERSTAND_FOR_LINE_DATE_CONF_KEY
								+ " in configuration");
			} // end catch

		} // end if
		else {
			// just log
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.LONGITUDE_LIMIT_TO_UNDERSTAND_FOR_LINE_DATE_CONF_KEY
							+ " in configuration");
		} // end else

		// Sparc installation DIR

		value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.SPARC_INSTALLATION_DIR_CONF_KEY);
		if (value != null) {
			this.SparcDirectory = value;

			
			/*
			 * 			try {
				value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.SPARC_INSTALLATION_DIR_CONF_KEY);

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
						logger.debug("USING SPARC VERSION : "+sparcVersion);
					}
				}
				br.close();

			} catch (Exception e) {
				DateUtils.getLogInfo(e, logger);
			}
			 */

		} // end if
		else {
			// just log
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.SPARC_INSTALLATION_DIR_CONF_KEY + " in configuration");
		} // end else

		// retrieving schema
		value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.SPARC_FEASIBILITY_OUT_SCHEMA_CONF_KEY);
		if (value != null) {
			this.outPutSchema = value;
		} // end if
		else {
			// just log
			this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.SPARC_FEASIBILITY_OUT_SCHEMA_CONF_KEY
							+ " in configuration");
		} // end else

		/*
		 * value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.
		 * SPARC_REFINEMENT_OUT_SCHEMA_CONF_KEY); if(value!=null) {
		 * this.refinementOutSchema=value; } else {
		 * this.tracer.warning(EventType.SOFTWARE_EVENT,
		 * ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " +
		 * FeasibilityConstants.SPARC_REFINEMENT_OUT_SCHEMA_CONF_KEY +
		 * " in configuration"); }
		 */

	}// end method

	/**
	 * Build the input file to be passed to SPARC in case of Feasibility
	 *
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws GridException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws FeasibilityException
	 */
	private void buildInputFileForFeasibility(double stopValidityTime)


	{
		DocumentBuilder db = null;
		try {
		this.tracer.log("Building SPARC input for feasibility");

		// building sparc input path
		String fileName = this.workingDir + System.getProperty("file.separator") + this.sparcInputFileName;// +
																											// java.time.LocalDateTime.now().toString();
		// creating document
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	

				db = dbf.newDocumentBuilder();
			
		
		// new doc
		Document doc = db.newDocument();
		// create dto list element
		Element DTOList = doc.createElement(DTOListTagName);
		// setting doc attributes
		DTOList.setAttribute(xsiAttr, xsiAttrValue);
		DTOList.setAttribute(cmnAttr, cmnAttrValue);
		DTOList.setAttribute(noNamespaceSchemaLocationAttr, this.noNamespaceSchemaLocationAttrValue);

		doc.appendChild(DTOList);

		// looping on AR list
		for (AcqReq a : this.acqReqList) {
			// SPRC work onnly on CSG satelllite
			// if(!a.getMission().equals(FeasibilityConstants.CSK_NAME))
			// {

			for (DTO dto : a.getDTOList()) {
				// looping on DTO
				if (!dto.getMissionName().equals(FeasibilityConstants.CSG_NAME)) {
					continue;
				}

				this.foundDTO = true;
				Element DTOElement = doc.createElement(DTOTagName);
				DTOList.appendChild(DTOElement);
				Element DTOInfoElement = doc.createElement(DTOInfoTagName);
				Element AzimuthCutElement = doc.createElement(AzimuthCutTagName);

				DTOElement.appendChild(DTOInfoElement);
				DTOElement.appendChild(AzimuthCutElement);
				// creating pr id
				Element PRIDElement = doc.createElement(PRIDTagName);
				Text text = doc.createTextNode("" + this.programmingRequest.getProgReqId());
				PRIDElement.appendChild(text);
				DTOInfoElement.appendChild(PRIDElement);
				Element ARIDElement = doc.createElement(ARIDTagName);
				text = doc.createTextNode("" + a.getId());
				ARIDElement.appendChild(text);
				// appending ARID element
				DTOInfoElement.appendChild(ARIDElement);

					addDTOToDOMForFeasibility(doc, DTOInfoElement, AzimuthCutElement, dto, stopValidityTime);

			} // end for

			// }//end if
		} // end for

			dumpXmlDomTreeToFile(doc, fileName);
		} catch (TransformerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("TransformerException"+e1);
		}
		catch (GridException | FeasibilityException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
			System.out.println("FeasibilityException OR GridException"+ex);
		}catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("ParserConfigurationException"+e);
		}

	}// end method

	/**
	 * Dump the dom treenode into fileName
	 *
	 * @param doc
	 * @param fileName
	 * @throws TransformerException
	 */
	protected void dumpXmlDomTreeToFile(Document doc, String fileName) throws TransformerException {
		DOMSource source = new DOMSource(doc);
		File outputFile = new File(fileName);
		StreamResult result = new StreamResult(outputFile);
		// transformers
		TransformerFactory tfact = TransformerFactory.newInstance();
		Transformer transformer = tfact.newTransformer();
		// inserting new line and space between node
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		// dumping
		transformer.transform(source, result);
	}// end method

	/**
	 * Add dto info to the file to be passed to SPARC
	 *
	 * @param writer
	 * @param dto
	 * @param stopValidityTime
	 * @throws GridException
	 * @throws FeasibilityException
	 */
	// private void addDTOToDOMForFeasibility(PrintWriter writer, final DTO dto)
	// throws GridException
	private void addDTOToDOMForFeasibility(Document doc, Element DTOInfoElement, Element AzimuthCutElement, DTO dto,
			double stopValidityTime) throws GridException, FeasibilityException {

		// inseting DTO info
		insertDTOInfoForFeasibilityInputFile(doc, DTOInfoElement, dto);
		// inserting cut
		insertAzimuthCutForFeasibility(doc, AzimuthCutElement, dto, stopValidityTime);

	}// end method

	/**
	 * Insert the Azimuth cut in the Sparc Input
	 *
	 * @param doc
	 * @param AzimuthCutElement
	 * @param dto
	 * @throws GridException
	 * @throws FeasibilityException
	 */
	private void insertAzimuthCutForFeasibility(Document doc, Element AzimuthCutElement, DTO dto,
			double stopValidityTime) throws GridException, FeasibilityException {
		// Number of sample in azimuth
		int numberOfStep = dto.getNumberOfAzimuthSampleStep();
		AzimuthCutElement.setAttribute(countAttrName, "" + numberOfStep);
		double currentTime = dto.startTime;

//		logger.debug("ACCESSTIME FROM dto " + dto);
//
//		logger.debug("ACCESSTIME FROM insertAzimuthCutForFeasibility dto.startTime "
//				+ DateUtils.fromCSKDateToDateTime(dto.startTime));

		double timeStep;
		if (dto instanceof SpotLightDTO) {
			SpotLightDTO d = (SpotLightDTO) dto;
			logger.debug("ACCESSTIME FROM dto square " + dto);

			timeStep = (d.getSquareStop() - d.getSquareStart()) / (numberOfStep - 1);
			/*
			 * //System.out.println("----------------------STEP: " + timeStep);
			 * //System.out.println("**************************SQUARE START:  " +
			 * d.getSquareStart());
			 * //System.out.println("**************************SQUARE STOP:  " +
			 * d.getSquareStop());
			 */
			currentTime = d.getSquareStart();
//			logger.debug("ACCESSTIME FROM insertAzimuthCutForFeasibility dto.currentTime "
//					+ DateUtils.fromCSKDateToDateTime(currentTime));

		} // end if
		else {
			timeStep = (dto.getStopTime() - dto.getStartTime()) / (numberOfStep - 1);
		} // end else

//		logger.debug("ACCESSTIME FROM numberOfStep " + numberOfStep);

		// writer.println(" ---tempi: "+dto.getStartTime()+"
		// "+dto.getStopTime());
		for (int currentStep = 0; currentStep < numberOfStep; currentStep++) {
			// Creating sample
			Element SampleElement = doc.createElement(SampleTagName);

			// Adding sampledid
			Element SampleIdElement = doc.createElement(SampleIDTagName);
			Text text = doc.createTextNode("" + (currentStep + 1));
			SampleIdElement.appendChild(text);
			SampleElement.appendChild(SampleIdElement);
			// Adding sample tiem
			Element AccessTimeElement = doc.createElement(AccessTimeTagName);

			text = doc.createTextNode(String.format(Locale.US, "%f", currentTime * sparcTimeConversion));
			AccessTimeElement.appendChild(text);
			SampleElement.appendChild(AccessTimeElement);

			addingSatelliteDataToSample(doc, SampleElement, dto, currentTime, stopValidityTime);

			AzimuthCutElement.appendChild(SampleElement);

			currentTime += timeStep;
//			logger.debug("ACCESSTIME FROM inside FOR " + DateUtils.fromCSKDateToDateTime(currentTime));

			/*
			 * if(currentStep==numberOfStep-2) { currentTime=dto.getStopTime(); } else {
			 * currentTime+=timeStep; }
			 */
		} // end for

	}// end method

	/**
	 * Evaluate and add the satellite info to azimuth cut sample. It also add the
	 * elevation
	 *
	 * @param doc
	 * @param SampleElement
	 * @param dto
	 * @param current       time
	 * @throws GridException
	 * @throws FeasibilityException
	 */
	private void addingSatelliteDataToSample(Document doc, Element SampleElement, DTO dto, double currentTime,
			double stopValidityTime) throws GridException, FeasibilityException {
//		logger.debug("ACCESSTIME FROM addingSatelliteDataToSample " + DateUtils.fromCSKDateToDateTime(currentTime));

		Satellite satellite = dto.getSat();
		String satelliteName = satellite.getName();
		EpochBean epoch = null;

		// //System.out.println("======================================satellite
		// name: " + satelliteName);

		// If thr DTO is expanded in case of timeline optimization the orbital
		// data could be not present in the time line so we have to extract it
		if (dto.isExpandedDTO) {

//			logger.debug("EXPANDED currentTime as date (" + DateUtils.fromCSKDateToDateTime(currentTime));

			double beginTimeLine = dto.getStartTime() - DateUtils.secondsToJulian(numberOfSecondsForExtremeOfinterval);

//				logger.debug("EXPANDED stopValidityTime " + DateUtils.fromCSKDateToDateTime(stopValidityTime));

			double endTimeLine = stopValidityTime;
			// per test rimuoviamo endTimeLine e settiamo come endTime la fine della
			// validity

			// double endTimeLine = dto.getStopTime() +
			// DateUtils.secondsToJulian(numberOfSecondsForExtremeOfinterval);
			ArrayList<EpochBean> temporaryEpochList = EphemeridInMemoryDB.getInstance().selectEpochs(satelliteName,
					beginTimeLine, endTimeLine);
			ArrayList<EpochBean> oldList = (ArrayList<EpochBean>) satellite.getEpochs();

//
//				logger.debug("dtoEndtime " + DateUtils.fromCSKDateToDateTime(dto.getStopTime()));
//				logger.debug("endTimeLine as date (" + DateUtils.fromCSKDateToDateTime(endTimeLine));

			satellite.setEpochs(temporaryEpochList);
			epoch = satellite.getEpochAt(currentTime);
			satellite.setEpochs(oldList);
		} // end if
		else {
//			logger.debug("NOT EXPANDED currentTime as date (" + DateUtils.fromCSKDateToDateTime(currentTime));

			epoch = satellite.getEpochAt(currentTime);
		} // end else

		Vector3D satellitePosEcef = epoch.getoXyz();
		Element SatellitePositionElement = doc.createElement(SatellitePositionTagName);

		Element xElement = doc.createElement(XTagName);
		Text text = doc.createTextNode("" + satellitePosEcef.getX());
		xElement.appendChild(text);
		SatellitePositionElement.appendChild(xElement);

		Element yElement = doc.createElement(YTagName);
		text = doc.createTextNode("" + satellitePosEcef.getY());
		yElement.appendChild(text);
		SatellitePositionElement.appendChild(yElement);

		Element zElement = doc.createElement(ZTagName);
		text = doc.createTextNode("" + satellitePosEcef.getZ());
		zElement.appendChild(text);
		SatellitePositionElement.appendChild(zElement);

		SampleElement.appendChild(SatellitePositionElement);

		// Adding relative velocitry
		Vector3D satelliteVelEcef = epoch.getoVxVyVz();

		Element SatelliteRelativeVelocityElement = doc.createElement(SatelliteRelativeVelocityTagName);

		xElement = doc.createElement(XTagName);
		text = doc.createTextNode("" + satelliteVelEcef.getX());
		xElement.appendChild(text);
		SatelliteRelativeVelocityElement.appendChild(xElement);

		yElement = doc.createElement(YTagName);
		text = doc.createTextNode("" + satelliteVelEcef.getY());
		yElement.appendChild(text);
		SatelliteRelativeVelocityElement.appendChild(yElement);

		zElement = doc.createElement(ZTagName);
		text = doc.createTextNode("" + satelliteVelEcef.getZ());
		zElement.appendChild(text);
		SatelliteRelativeVelocityElement.appendChild(zElement);

		SampleElement.appendChild(SatelliteRelativeVelocityElement);

		// TODO Velocità inerziale da calcolare quando ci saranno i polinom
		// Adding inertial velocity

		/*
		 * Vector3D satelliteVelInertial=epoch.getoVxVyVz();
		 *
		 * Element SatelliteInertialVelocityElement =
		 * doc.createElement(SatelliteInertialVelocityTagName);
		 *
		 * xElement = doc.createElement(XTagName); text =
		 * doc.createTextNode(""+satelliteVelInertial.getX());
		 * xElement.appendChild(text);
		 * SatelliteInertialVelocityElement.appendChild(xElement);
		 *
		 * yElement = doc.createElement(YTagName); text =
		 * doc.createTextNode(""+satelliteVelInertial.getY());
		 * yElement.appendChild(text);
		 * SatelliteInertialVelocityElement.appendChild(yElement);
		 *
		 * zElement = doc.createElement(ZTagName); text =
		 * doc.createTextNode(""+satelliteVelInertial.getZ());
		 * zElement.appendChild(text);
		 * SatelliteInertialVelocityElement.appendChild(zElement);
		 *
		 * SampleElement.appendChild(SatelliteInertialVelocityElement);
		 */

		double[] satellitePosLLH = ReferenceFrameUtils.ecef2llh(satellitePosEcef, true);

		// Inserting satellite altitude
		Element SatelliteHeightElement = doc.createElement(SatelliteHeightTagName);
		text = doc.createTextNode("" + satellitePosLLH[2]);
		SatelliteHeightElement.appendChild(text);
		SampleElement.appendChild(SatelliteHeightElement);

		// evaluating scene altitude
		double[] sceneAlts = evaluateSceneAlt(dto, satellitePosEcef, satelliteVelEcef, dto.getNearOffnadir(),
				dto.getFarOffNadir());

		// Adding SubSatellite heghjt
		Element SubSatelliteHeightElement = doc.createElement(SubSatelliteHeightTagName);
		text = doc.createTextNode("" + sceneAlts[3]);
		SubSatelliteHeightElement.appendChild(text);
		SampleElement.appendChild(SubSatelliteHeightElement);

		// Adding offNadirNear

		Element OffNadirNearHeightElement = doc.createElement(OffNadirNearHeightTagName);
		text = doc.createTextNode("" + sceneAlts[1]);
		OffNadirNearHeightElement.appendChild(text);
		SampleElement.appendChild(OffNadirNearHeightElement);

		// Adding offNadirFar

		Element OffNadirFarHeightElement = doc.createElement(OffNadirFarHeightTagName);
		text = doc.createTextNode("" + sceneAlts[2]);
		OffNadirFarHeightElement.appendChild(text);
		SampleElement.appendChild(OffNadirFarHeightElement);

		// Appending range scene height

		Element RangeHeightElement = doc.createElement(OffNadirRangeHeightTagName);
		text = doc.createTextNode("" + sceneAlts[0]);
		RangeHeightElement.appendChild(text);
		SampleElement.appendChild(RangeHeightElement);

	}// end method

	/**
	 * Add the DTOInfo to the Feadibility sparc info
	 *
	 * @param doc            XML dom
	 * @param DTOInfoElement DTOInfo element
	 * @param dto            DTO
	 */
	private void insertDTOInfoForFeasibilityInputFile(Document doc, Element DTOInfoElement, DTO dto) {

//		logger.debug("INVOKING insertDTOInfoForFeasibilityInputFile");
		Element DTOIDElement = doc.createElement(DTOIDTagName);
		Text text = doc.createTextNode("" + dto.getId());
		DTOIDElement.appendChild(text);
		/**
		 * Inserting element
		 */
		DTOInfoElement.appendChild(DTOIDElement);

		Element SparcModeElement = doc.createElement(SPARCModeTagName);
		text = doc.createTextNode("" + this.sparcMode);
		SparcModeElement.appendChild(text);
		/**
		 * Inserting element
		 */
		DTOInfoElement.appendChild(SparcModeElement);

		// Inserting Stripmap flag
		String flag = trueFlag;
		if (dto instanceof SpotLightDTO) {

			// //System.out.println("-------------------Spot DTO");
			flag = falseFlag;
			
		} // end if
		else {

	        	//check the duration of the dto
	        	//if it is larger than the minDuration set the flag to true, set false otherwise
	            int realDuration = (int)(DateUtils.fromCSKDurationToMilliSeconds(dto.getStopTime()) -DateUtils.fromCSKDurationToMilliSeconds(dto.getStartTime()));
	            // spotlight dto
				logger.debug("24.02.2020 DTO duration "+realDuration);
				logger.debug("24.02.2020 versus DTO getDtoDurationSquared "+dto.getBeam().getDtoDurationSquared());

	            if(realDuration<=dto.getBeam().getDtoDurationSquared())
	            {
	            	flag = falseFlag;

	            }
		} // end else
		
		logger.debug("24.02.2020 stripFlas is : "+flag);

		
		Element StripMapFlagElement = doc.createElement(StripmapFlagTagName);
		text = doc.createTextNode(flag);
		StripMapFlagElement.appendChild(text);
		/**
		 * Inserting element
		 */
		DTOInfoElement.appendChild(StripMapFlagElement);

		// Inserting DI2SFlag

		// if(programmingRequest.isDi2sAvailabilityFlag())
		if (this.di2sAvailabilityConfirmationFlag) {
			flag = trueFlag;
			// lsflag = falseFlag;
		} // end if
		else {
			flag = falseFlag;
		} // end else

		Element DI2SFlagElement = doc.createElement(DI2SFlagTagName);
		text = doc.createTextNode(flag);
		DI2SFlagElement.appendChild(text);
		/**
		 * Inserting element
		 */
		DTOInfoElement.appendChild(DI2SFlagElement);

		// Inserting SAR Mode

		Element SARModeElement = doc.createElement(SARModeTagName);
		text = doc.createTextNode(dto.getBeam().getSensorModeName());
		SARModeElement.appendChild(text);
		/**
		 * Inserting element
		 */
		DTOInfoElement.appendChild(SARModeElement);

		// Inserting SARBEAM
		Element SARBeamElement = doc.createElement(SARBeamTagName);
		text = doc.createTextNode(dto.getBeamId());
		SARBeamElement.appendChild(text);
		/**
		 * Inserting element
		 */
		DTOInfoElement.appendChild(SARBeamElement);

		// Inserting Polarization
		String polarization = this.programmingRequest.getPolarizationCSG();
		if (!polarization.isEmpty()) {
			Element SARPolarizationElement = doc.createElement(SARPolarizationTagName);
			text = doc.createTextNode(polarization);
			SARPolarizationElement.appendChild(text);
			/**
			 * Inserting element
			 */
			DTOInfoElement.appendChild(SARPolarizationElement);
		} // end if

		// Inserting look side
		Element SARLookElement = doc.createElement(SARLookTagName);
		text = doc.createTextNode(FeasibilityConstants.getLookSideString(dto.getLookSide()));
		SARLookElement.appendChild(text);
		DTOInfoElement.appendChild(SARLookElement);

		// Inserting orbit direction
		Element SAROrbitDirectionElement = doc.createElement(SAROrbitDirectionTagName);
		text = doc.createTextNode(FeasibilityConstants.getOrbitDirectionAsString(dto.getOrditDirection()));
		SAROrbitDirectionElement.appendChild(text);
		/**
		 * Inserting element
		 */
		DTOInfoElement.appendChild(SAROrbitDirectionElement);

		// insering corners
		insertCornersToDTOINfo(doc, DTOInfoElement, dto);
		// insertinng AOOINFO
		insertAOIInfoToDTOInfo(doc, DTOInfoElement, dto);

		// TODO caso delle minimumAOOINfo

	}// end method

	/**
	 * Add the DTOInfo to the Feadibility sparc info
	 *
	 * @param doc            XML dom
	 * @param DTOInfoElement DTOInfo element
	 * @param dto            DTO
	 */
	private void insertAOIInfoToDTOInfo(Document doc, Element DTOInfoElement, DTO dto) {
		// off nadir angles
		double nearAngle = 0.0;
		double farAngle = 0.0;

		double currentAccessAngle = 0;
		// first
		boolean isFirst = true;

		// in case of slave interf dto
		List<Access> accessList = dto.getMasterInterferometricDtoAccessList();
		logger.debug("from insertAOIInfoToDTOInfo");

		// no slave interferometric
		if (accessList == null) {
			accessList = dto.getDtoAccessList();
		} // end if

		/**
		 * if single acquired the near and far off nadir are the mminimum and the
		 * maximum of the off nadir belonging the DTO accesses
		 */
		if (this.isSingleAcquired) {

			for (Access a : accessList) {
				currentAccessAngle = Math.abs(a.getOffNadir());
				if (isFirst) {
					// first iteration
					nearAngle = currentAccessAngle;
					farAngle = currentAccessAngle;
					isFirst = false;
				} // end if
				else {
					if (currentAccessAngle < nearAngle) {
						nearAngle = currentAccessAngle;
					} // end if
					else if (currentAccessAngle > farAngle) {
						farAngle = currentAccessAngle;
					} // end else
				} // end else

			} // end for
		} // end if
		else // not single acquisition
		{

			nearAngle = dto.getNearOffnadir();
			farAngle = dto.getFarOffNadir();
		} // end else

		// creating xml nodes
		Element AOIInfoElement = doc.createElement(AOIInfoTagName);
		Element OffNadirNearElement = doc.createElement(OffNadirNearTagName);
		Text text = doc.createTextNode("" + nearAngle);
		OffNadirNearElement.appendChild(text);
		AOIInfoElement.appendChild(OffNadirNearElement);
		// offnadir
		Element OffNadirFarElement = doc.createElement(OffNadirFarTagName);
		text = doc.createTextNode("" + farAngle);
		OffNadirFarElement.appendChild(text);
		AOIInfoElement.appendChild(OffNadirFarElement);
		// DTOheight
		Element DTOheightElement = doc.createElement(DTOHeightTagName);
		text = doc.createTextNode("" + dto.getMeanElevation());
		DTOheightElement.appendChild(text);
		AOIInfoElement.appendChild(DTOheightElement);
		// adding info
		DTOInfoElement.appendChild(AOIInfoElement);

		// Inserting minimum AoI
		if (this.di2sAvailabilityConfirmationFlag) {
			if (!this.programmingRequest.isMinimumAoICoincidentWithAoI()) {
				nearAngle = 0.0;
				farAngle = 0.0;
				currentAccessAngle = 0;
				isFirst = true;
				for (Access a : accessList) {
					if (a.getGridPoint().isBelongsToMinimumAoI()) {
						currentAccessAngle = Math.abs(a.getOffNadir());
						if (isFirst) {
							nearAngle = currentAccessAngle;
							farAngle = currentAccessAngle;
							isFirst = false;
						} // end if
						else {
							if (currentAccessAngle < nearAngle) {
								nearAngle = currentAccessAngle;
							} // end if
							else if (currentAccessAngle > farAngle) {
								farAngle = currentAccessAngle;
							} // end else
						} // end else
					} // end if

				} // end for
			} // end if

			Element MinimumAOIInfoElement = doc.createElement(MinimumAOIInfoTagName);
			Element MinimumOffNadirNearElement = doc.createElement(OffNadirNearTagName);

			text = doc.createTextNode("" + nearAngle);
			MinimumOffNadirNearElement.appendChild(text);
			/**
			 * Inserting element
			 */
			MinimumAOIInfoElement.appendChild(MinimumOffNadirNearElement);

			Element MinimumOffNadirFarElement = doc.createElement(OffNadirFarTagName);
			text = doc.createTextNode("" + farAngle);
			MinimumOffNadirFarElement.appendChild(text);
			/**
			 * Inserting element
			 */
			MinimumAOIInfoElement.appendChild(MinimumOffNadirFarElement);
			/**
			 * Inserting element
			 */
			Element MinimumDTOheightElement = doc.createElement(DTOHeightTagName);
			text = doc.createTextNode("" + dto.getMeanElevation());
			MinimumDTOheightElement.appendChild(text);
			MinimumAOIInfoElement.appendChild(MinimumDTOheightElement);
			/**
			 * Inserting element
			 */
			DTOInfoElement.appendChild(MinimumAOIInfoElement);

		} // end if
	}// end method

	/**
	 * Add the corners to DTOInfo to the Feadibility sparc info
	 *
	 * @param doc                        XML dom
	 * @param DTOInfoElement             DTOInfo element
	 * @param dto                        DTO
	 * @param haveEvaluateMeanElevation, if true the mean will be used for corners
	 *                                   otherwhise 0 will be used
	 */
	protected void insertCornersToDTOINfo(Document doc, Element DTOInfoElement, DTO dto)

	{
		try
		{
			// Inserting early near
			logger.debug("insertCornersToDTOINfo");
			logger.debug("dto.getFirstCorner()"+dto.getFirstCorner());
	

			double[] corner = dto.getFirstCorner();
			logger.debug("corner[0]" +corner[0]);
			logger.debug("corner[1]" +corner[1]);
			
			int height = this.dem.getElevation(corner[0], corner[1]);
			logger.debug("height" +height);

			Element DTOEarlyNearCornerElement = doc.createElement(DTOEarlyNearCornerTagName);
			Element latitudeElement = doc.createElement(LatitudeTagName);
			Text text = doc.createTextNode("" + corner[0]);
			/**
			 * Inserting element
			 */
			latitudeElement.appendChild(text);
			/**
			 * Inserting element
			 */
			DTOEarlyNearCornerElement.appendChild(latitudeElement);
			Element longitudeElement = doc.createElement(LongitudeTagName);
			text = doc.createTextNode("" + corner[1]);
			/**
			 * Inserting element
			 */
			longitudeElement.appendChild(text);
			DTOEarlyNearCornerElement.appendChild(longitudeElement);
			Element heightElement = doc.createElement(HeightTagName);
			text = doc.createTextNode("" + height);
			heightElement.appendChild(text);
			/**
			 * Inserting element
			 */
			DTOEarlyNearCornerElement.appendChild(heightElement);
			DTOInfoElement.appendChild(DTOEarlyNearCornerElement);

			// inserting early FAR
			logger.debug("corner1 processed");

			corner = dto.getSecondCorner();
			height = this.dem.getElevation(corner[0], corner[1]);

			Element DTOEarlyFarCornerElement = doc.createElement(DTOEarlyFarCornerTagName);
			latitudeElement = doc.createElement(LatitudeTagName);
			text = doc.createTextNode("" + corner[0]);
			latitudeElement.appendChild(text);
			/**
			 * Inserting element
			 */
			DTOEarlyFarCornerElement.appendChild(latitudeElement);
			longitudeElement = doc.createElement(LongitudeTagName);
			text = doc.createTextNode("" + corner[1]);
			/**
			 * Inserting element
			 */
			longitudeElement.appendChild(text);
			DTOEarlyFarCornerElement.appendChild(longitudeElement);
			heightElement = doc.createElement(HeightTagName);
			text = doc.createTextNode("" + height);
			/**
			 * Inserting element
			 */
			heightElement.appendChild(text);
			DTOEarlyFarCornerElement.appendChild(heightElement);
			/**
			 * Inserting element
			 */
			DTOInfoElement.appendChild(DTOEarlyFarCornerElement);
			logger.debug("corner2 processed");

			// Inserting Late near
			corner = dto.getFourtCorner();
			height = this.dem.getElevation(corner[0], corner[1]);

			Element DTOLateNearCornerElement = doc.createElement(DTOLateNearCornerTagName);
			latitudeElement = doc.createElement(LatitudeTagName);
			text = doc.createTextNode("" + corner[0]);
			/**
			 * Inserting element
			 */
			latitudeElement.appendChild(text);
			DTOLateNearCornerElement.appendChild(latitudeElement);
			longitudeElement = doc.createElement(LongitudeTagName);
			text = doc.createTextNode("" + corner[1]);
			/**
			 * Inserting element
			 */
			longitudeElement.appendChild(text);
			DTOLateNearCornerElement.appendChild(longitudeElement);
			heightElement = doc.createElement(HeightTagName);
			text = doc.createTextNode("" + height);
			/**
			 * Inserting element
			 */
			heightElement.appendChild(text);
			DTOLateNearCornerElement.appendChild(heightElement);
			/**
			 * Inserting element
			 */
			DTOInfoElement.appendChild(DTOLateNearCornerElement);
			logger.debug("corner3 processed");

			// Inserting Late Far
			corner = dto.getThirdCorner();
			height = this.dem.getElevation(corner[0], corner[1]);

			Element DTOLateFarCornerElement = doc.createElement(DTOLateFarCornerTagName);
			latitudeElement = doc.createElement(LatitudeTagName);
			text = doc.createTextNode("" + corner[0]);
			latitudeElement.appendChild(text);
			DTOLateFarCornerElement.appendChild(latitudeElement);
			longitudeElement = doc.createElement(LongitudeTagName);
			text = doc.createTextNode("" + corner[1]);
			/**
			 * Inserting element
			 */
			longitudeElement.appendChild(text);
			DTOLateFarCornerElement.appendChild(longitudeElement);
			heightElement = doc.createElement(HeightTagName);
			text = doc.createTextNode("" + height);
			/**
			 * Inserting element
			 */
			heightElement.appendChild(text);
			DTOLateFarCornerElement.appendChild(heightElement);
			/**
			 * Inserting element
			 */
			DTOInfoElement.appendChild(DTOLateFarCornerElement);
			logger.debug("corner4 processed");

		}
		catch(Exception e)
		{
			DateUtils.getLogInfo(e, logger);
		}

	}// end method

	/**
	 * evaluate the altitude scene return a
	 *
	 * @param DTO
	 * @param satPos
	 * @param satVeel
	 * @return array holding the altitudes full alt[0] near alt[1] far alt[2] subsat
	 *         alt[3]
	 * @throws GridException
	 */
	private double[] evaluateSceneAlt(DTO dto, Vector3D satPos, Vector3D satVel, double nearOffNadirP,
			double farOffNadirP) throws GridException {
		double[] sceneAlts = new double[4];
		double nearOffNadir = nearOffNadirP;
		double farOffNadir = farOffNadirP;
		if (dto.getLookSide() == FeasibilityConstants.LeftLookSide) {
			nearOffNadir = -nearOffNadir;
			farOffNadir = -farOffNadir;
		} // end if

		double[][] corners = dto.evaluateLLHcorners(satPos, satVel, nearOffNadir, farOffNadir);
		sceneAlts[0] = evaluateFullSceneAltitude(dto, corners[0][0], corners[0][1], corners[1][0], corners[1][1]);

		// evaluating near mean altitude
		sceneAlts[1] = evaluteMeanAltitudeAroundPoint(corners[0][0], corners[0][1], corners[0][0], corners[0][1],
				corners[1][0], corners[1][1], dto.getNearOffNadirStepLenght(), dto.getNearOfNadirNumberOfStep());

		// evaluating far off nadir mean elevation
		sceneAlts[2] = evaluteMeanAltitudeAroundPoint(corners[1][0], corners[1][1], corners[0][0], corners[0][1],
				corners[1][0], corners[1][1], dto.getFarOffNadirStepLenght(), dto.getFarOfNadirNumberOfStep());

		// sat poss
		double[] satellitePosLLH = ReferenceFrameUtils.ecef2llh(satPos, true);
		// sceneAlts[3]=dem.getElevation(satellitePosLLH[0],
		// satellitePosLLH[1]);

		// evaluating sub satellite mean elevation
		// sceneAlts[3] = evaluteMeanAltitudeAroundPoint(satellitePosLLH[0],
		// satellitePosLLH[1],
		// corners[0][0],corners[0][1],corners[1][0],corners[1][1],
		// dto.getSubSatelliteStepLenght(), dto.getSubSatelliteNumberOfStep());
		// scene
		sceneAlts[3] = evaluteMeanAltitudeAroundPoint(satellitePosLLH[0], satellitePosLLH[1], satellitePosLLH[0],
				satellitePosLLH[1], corners[0][0], corners[0][1], dto.getSubSatelliteStepLenght(),
				dto.getSubSatelliteNumberOfStep());

		// writer.println("SubSat puntual: " +
		// dem.getElevation(satellitePosLLH[0], satellitePosLLH[1]));
		// writer.println("Near: " +
		// dem.getElevation(corners[0][0],corners[0][1]));
		// writer.println("Far: " +
		// dem.getElevation(corners[1][0],corners[1][1]));
		return sceneAlts;
	}// end method

	/**
	 * Evaluate the mean elevation at a given latitude/longitude.
	 *
	 * @param latitude     latitide of the point
	 * @param longitude    longitude of the point
	 * @param nearLat      latitude at near off nadir
	 * @param nearLong     longitude at near off nadir
	 * @param farLat       latitude at far off nadir
	 * @param farLong      longitude at far off nadir
	 * @param stpeLenght   length of step to build the mean
	 * @param numberOfStep the half interval number of step
	 * @return mean elevation
	 * @throws GridException
	 * @throws ArrayIndexOutOfBoundsException
	 */
	private double evaluteMeanAltitudeAroundPoint(double latitude, double longitude, double nearLat, double nearLong,
			double farLat, double farLong, double stpeLenght, int numberOfStep)
			throws ArrayIndexOutOfBoundsException, GridException {
		// retval
		double retval = 0;
		if (this.programmingRequest.isPolarRequest()) {
			// case polar
			retval = evaluteMeanAltitudeAroundPointPolarCase(latitude, longitude, nearLat, nearLong, farLat, farLong,
					stpeLenght, numberOfStep);
		} // end if
		else {
			// case not polar
			retval = evaluteMeanAltitudeAroundPointNonPolar(latitude, longitude, nearLat, nearLong, farLat, farLong,
					stpeLenght, numberOfStep);
		} // end else
			// returning
		return retval;
	}// end method

	/**
	 * Evaluate the mean elevation at a given latitude/longitude in polar area.
	 *
	 * @param latitude     latitide of the point
	 * @param longitude    longitude of the point
	 * @param nearLat      latitude at near off nadir
	 * @param nearLong     longitude at near off nadir
	 * @param farLat       latitude at far off nadir
	 * @param farLong      longitude at far off nadir
	 * @param stpeLenght   length of step to build the mean
	 * @param numberOfStep the half interval number of step
	 * @return mean elevation
	 * @throws GridException
	 */
	private double evaluteMeanAltitudeAroundPointPolarCase(double latitude, double longitude, double nearLat,
			double nearLong, double farLat, double farLong, double stpeLenght, int numberOfStep) {
		// retval
		double alt = 0;

		// evaluating stereo
		double[] stereoNear = ReferenceFrameUtils.fromLatLongToStereo(nearLat, nearLong);
		double[] stereoFar = ReferenceFrameUtils.fromLatLongToStereo(farLat, farLong);

		double[] meanPoint = ReferenceFrameUtils.fromLatLongToStereo(latitude, longitude);

		boolean isSud = false;
		// south pole
		if (nearLat < 0) {
			isSud = true;
		}

		double xstart = stereoNear[0];
		double xstop = stereoFar[0];
		double ystart = stereoNear[1];
		double ystop = stereoFar[1];

		if (stereoNear[0] > stereoFar[0]) {
			xstart = stereoFar[0];
			xstop = stereoNear[0];
			ystart = stereoFar[1];
			ystop = stereoNear[1];
		} // end if

		// if((xstop-xstart)==0)
		if (((ystop - ystart) == 0) || ((xstop - xstart) == 0) || (stpeLenght == 0) || (numberOfStep == 0)) {
			// TODO non so se può capitare
			// throw new GridException("DTO degenerated latitdudes or longitudes
			// of near and far point are equals");

			return this.dem.getElevation(latitude, longitude);
		} // end if

		double m = (ystop - ystart) / (xstop - xstart);
		double y;

		// devo ciclare intorno al punto su cui valurare la media

		xstart = meanPoint[0] - (stpeLenght * numberOfStep);
		xstop = meanPoint[0] + (stpeLenght * numberOfStep);

		double currentx = xstart;

		// it was half interval
		numberOfStep = 2 * numberOfStep;
		for (int i = 0; i < numberOfStep; i++) {
			// adding
			y = (m * (currentx - xstart)) + ystart;
			double[] latLon = ReferenceFrameUtils.fromStereoToLatLon(currentx, y, isSud);

			alt = alt + this.dem.getElevation(latLon[0], latLon[1]);

			currentx = currentx + stpeLenght;

		} // end for
			// evaluating mean
		alt = alt / numberOfStep;

		// returning
		return alt;

	}// end method

	/**
	 * Evaluate the mean elevation at a given latitude/longitude in a generic area.
	 *
	 * @param latitude     latitide of the point
	 * @param longitude    longitude of the point
	 * @param nearLat      latitude at near off nadir
	 * @param nearLong     longitude at near off nadir
	 * @param farLat       latitude at far off nadir
	 * @param farLong      longitude at far off nadir
	 * @param stpeLenght   length of step to build the mean
	 * @param numberOfStep the half interval number of step
	 * @return mean elevation
	 */
	private double evaluteMeanAltitudeAroundPointNonPolar(double latitude, double longitude, double nearLat,
			double nearLong, double farLat, double farLong, double stepLenght, int numberOfStep) {
		// retval
		double alt = 0;
		// check for line date
		boolean haveTranslate = false;

		// if(((nearLong > 0) != (farLong > 0)) && this.isOverDateLine )
		if ((Math.abs(nearLong) > this.longitudeLimitToUnderstandForLineDate)
				|| (Math.abs(farLong) > this.longitudeLimitToUnderstandForLineDate)) {
			haveTranslate = true;
			if (nearLong > 0) {
				nearLong = nearLong - 360;
			} // end if

			if (farLong > 0) {
				farLong = farLong - 360;
			} // end if
		} // end if

		double startLong = nearLong;
		double stopLong = farLong;

		double startLat = nearLat;
		double stopLat = farLat;

		if (farLong < nearLong) {
			startLong = farLong;
			stopLong = nearLong;

			startLat = farLat;
			stopLat = nearLat;

		} // end if

		// TODO capire come valutare step

		if (((stopLong - startLong) == 0) || ((stopLat - startLat) == 0) || (stepLenght == 0) || (numberOfStep == 0)) {
			return this.dem.getElevation(latitude, longitude);
		} // end if

		// evaluating angular step =
		GeodeticCalculator geoCalc = new GeodeticCalculator();
		// geodetc problem
		geoCalc.setStartingGeographicPoint(0, latitude);
		geoCalc.setDirection(90.0, stepLenght);
		// longitude variation
		// //System.out.println("Lat: " + latitude + " long: " + longitude);
		double angularStep = geoCalc.getDestinationGeographicPoint().getX();

		// //System.out.println("Angular Step: " + angularStep+ " meter: " +
		// stepLenght);

		double m = (stopLat - startLat) / (stopLong - startLong);

		double lat;

		startLong = longitude - (2 * numberOfStep * angularStep);
		// startLat = latitude;
		numberOfStep = 2 * numberOfStep;

		double currentLong = startLong;

		for (int i = 0; i < numberOfStep; i++) {

			lat = (m * (currentLong - startLong)) + startLat;

			if (haveTranslate && (currentLong < -180)) {
				alt = alt + this.dem.getElevation(lat, currentLong + 360);
			} // end if
			else {
				alt = alt + this.dem.getElevation(lat, currentLong);
			} // end else

			currentLong += angularStep;

		} // end for
			// evaluating mean elevation
		alt = alt / numberOfStep;
		// returning
		return alt;
	}// end method

	/**
	 * Evaluate the elevation mean scene altitude at a given azimuth given the near
	 * and far corner
	 *
	 * @dto
	 * @param nearLat
	 * @param nearLong
	 * @param farLat
	 * @param farLong
	 * @return the altitude
	 * @throws GridException
	 */
	private double evaluateFullSceneAltitude(DTO dto, double nearLat, double nearLong, double farLat, double farLong)
			throws GridException {
		// return
		double alt = 0;

		if (this.programmingRequest.isPolarRequest()) {
			alt = evaluateFullSceneAltitudeInPolarZone(dto, nearLat, nearLong, farLat, farLong);
		} // end if
		else {
			alt = evaluateFullSceneAltitudeInNonPolarZone(dto, nearLat, nearLong, farLat, farLong);
		} // end else

		// returning
		return alt;
	}// end method

	/**
	 * Evaluate altitudes in case of not polar zones
	 *
	 * @param dto
	 * @param nearLat
	 * @param nearLong
	 * @param farLat
	 * @param farLong
	 * @return altitude
	 * @throws GridException
	 */
	private double evaluateFullSceneAltitudeInNonPolarZone(DTO dto, double nearLat, double nearLong, double farLat,
			double farLong) {
		double alt = 0;

		// //System.out.println("Caso non polare");

		boolean haveTranslate = false;
		double originalNearLong = nearLong; // hold the initial value
		double originalFarLong = farLong; // hold the initial value
		// if(((nearLong > 0) != (farLong > 0)) && this.isOverDateLine )
		if ((Math.abs(nearLong) > this.longitudeLimitToUnderstandForLineDate)
				|| (Math.abs(farLong) > this.longitudeLimitToUnderstandForLineDate)) {
			haveTranslate = true;
			if (nearLong < 0) {
				nearLong = nearLong + 360;
			} // end if

			if (farLong < 0) {
				farLong = farLong + 360;
			} // end if
		} // end if

		double startLong = nearLong;
		double stopLong = farLong;

		double startLat = nearLat;
		double stopLat = farLat;

		if (farLong < nearLong) {
			startLong = farLong;
			stopLong = nearLong;

			startLat = farLat;
			stopLat = nearLat;

		} // end if

		// TODO capire come valutare step
		int numOfStep = dto.getNumberOfsampleInRangeForScene();

		double angularStepLenth = 0;

		if (((numOfStep - 1) != 0) && ((stopLong - startLong) != 0)) {
			angularStepLenth = (stopLong - startLong) / (numOfStep - 1);
		}

		if (angularStepLenth == 0) {

			// no translation needed
			alt = 0.5 * (this.dem.getElevation(nearLat, originalNearLong)
					+ this.dem.getElevation(farLat, originalFarLong));

			return alt;
			// throw new GridException("DTO degenerated");
		} // end if

		double m = (stopLat - nearLat) / (stopLong - startLong);

		double lat;
		double currentLong = startLong;

		for (int i = 0; i < numOfStep; i++) {

			lat = (m * (currentLong - startLong)) + startLat;

			if (haveTranslate && (currentLong > 180)) {
				alt = alt + this.dem.getElevation(lat, currentLong - 360);
			} // end if
			else {
				alt = alt + this.dem.getElevation(lat, currentLong);
			} // end else

			/*
			 * if(i==numOfStep-2) { currentLong=stopLong; } else {
			 * currentLong+=angularStepLenth; }
			 */

			currentLong += angularStepLenth;
		} // end for

		alt = alt / numOfStep;

		// double nearAlt = dem.getElevation(nearLat, nearLong);
		// double farAlt = dem.getElevation(farLat, farLong);
		// alt = 0.5*(nearAlt+farAlt);
		return alt;
	}// end method

	/**
	 * Evaluate the elevation mean scene altitude given the near and far corner at
	 * pole
	 *
	 * @param dto
	 * @param nearLat
	 * @param nearLong
	 * @param farLat
	 * @param farLong
	 * @return altitude
	 * @throws GridException
	 */
	private double evaluateFullSceneAltitudeInPolarZone(DTO dto, double nearLat, double nearLong, double farLat,
			double farLong) {

		// //System.out.println("-----------CASO Polare--------------------");

		double alt = 0;
		double[] stereoNear = ReferenceFrameUtils.fromLatLongToStereo(nearLat, nearLong);
		double[] stereoFar = ReferenceFrameUtils.fromLatLongToStereo(farLat, farLong);

		boolean isSud = false;

		if (nearLat < 0) {
			isSud = true;
		}

		double xstart = stereoNear[0];
		double xstop = stereoFar[0];
		double ystart = stereoNear[1];
		double ystop = stereoFar[1];

		if (stereoNear[0] > stereoFar[0]) {
			xstart = stereoFar[0];
			xstop = stereoNear[0];
			ystart = stereoFar[1];
			ystop = stereoNear[1];
		} // end if

		int numberOfStep = dto.getNumberOfsampleInRangeForScene();

		double stepLength = 0;
		if ((numberOfStep - 1) != 0) {
			stepLength = (xstop - xstart) / (numberOfStep - 1);
		}

		double currentx = xstart;

		if (stepLength == 0) {
			// TODO forse è il caso di ciclare su Y avendo X costante
			alt = 0.5 * (this.dem.getElevation(nearLat, nearLong) + this.dem.getElevation(farLat, farLong));
			return alt;
			// throw new GridException("DTO degeneratint");
		} // end if

		double m = (ystop - ystart) / (xstop - xstart);

		double y;

		for (int i = 0; i < numberOfStep; i++) {

			y = (m * (currentx - xstart)) + ystart;
			double[] latLon = ReferenceFrameUtils.fromStereoToLatLon(currentx, y, isSud);

			alt = alt + this.dem.getElevation(latLon[0], latLon[1]);
			/*
			 * if(i==numberOfStep-2) { currentx=xstop; } else { currentx+=stepLength; }
			 *
			 * currentx+=stepLength;
			 */
		} // end for

		alt = alt / numberOfStep;

		return alt;
	}// end method

	/**
	 * return the polynomials in the interval start stop of the DTO
	 *
	 * @param dto
	 * @return polynomials
	 */
	private PolynomialSplineFunction[] getPolynomials(DTO dto) {

		PolynomialSplineFunction[] polys = new PolynomialSplineFunction[9];
		String satellite = dto.getSatName();
		double beginTimeLine = dto.getStartTime() - DateUtils.secondsToJulian(numberOfSecondsForExtremeOfinterval);
		double endTimeLine = dto.getStopTime() + DateUtils.secondsToJulian(numberOfSecondsForExtremeOfinterval);

		List<EpochBean> listOfUsableBean = EphemeridInMemoryDB.getInstance().selectEpochs(satellite, beginTimeLine,
				endTimeLine);
		/*
		 * if(listOfUsableBean.size()>=3) {
		 *
		 * //System.out.println("DTO START: " +
		 * DateUtils.fromCSKDateToISOFMTDateTime(dto.getStartTime()));
		 * //System.out.println("DTO STOP: " +
		 * DateUtils.fromCSKDateToISOFMTDateTime(dto.getStopTime()));
		 * //System.out.println("Numero di campioni: " +listOfUsableBean.size() );
		 * //System.out.println("Begin: " +
		 * DateUtils.fromCSKDateToISOFMTDateTime(beginTimeLine) );
		 * //System.out.println("End: " +
		 * DateUtils.fromCSKDateToISOFMTDateTime(endTimeLine) );
		 *
		 * for(EpochBean e : listOfUsableBean) { //System.out.println("time: " +
		 * DateUtils.fromCSKDateToISOFMTDateTime(e.getEpoch()) );
		 *
		 * }
		 *
		 * }
		 */

		int numberOfUsableIndex = listOfUsableBean.size();

		double[] times = new double[numberOfUsableIndex];
		double[] ecefX = new double[numberOfUsableIndex];
		double[] ecefY = new double[numberOfUsableIndex];
		double[] ecefZ = new double[numberOfUsableIndex];
		double[] ecefVX = new double[numberOfUsableIndex];
		double[] ecefVY = new double[numberOfUsableIndex];
		double[] ecefVZ = new double[numberOfUsableIndex];

		double[] inertialVX = new double[numberOfUsableIndex];
		double[] inertialVY = new double[numberOfUsableIndex];
		double[] inertialVZ = new double[numberOfUsableIndex];

		int i = 0;
		for (EpochBean e : listOfUsableBean) {
			times[i] = e.getEpoch();
			ecefX[i] = e.getX();
			ecefY[i] = e.getY();
			ecefZ[i] = e.getZ();

			ecefVX[i] = e.getVx();
			ecefVY[i] = e.getVy();
			ecefVZ[i] = e.getVz();
			// Le componenti inerziali ancora non ci sono

			i++;
		}

		polys[0] = new SplineInterpolator().interpolate(times, ecefX);
		polys[1] = new SplineInterpolator().interpolate(times, ecefY);
		polys[2] = new SplineInterpolator().interpolate(times, ecefZ);
		polys[3] = new SplineInterpolator().interpolate(times, ecefVX);
		polys[4] = new SplineInterpolator().interpolate(times, ecefVY);
		polys[5] = new SplineInterpolator().interpolate(times, ecefVZ);

		// polys[6] = new SplineInterpolator().interpolate(times, inertialVX);
		// polys[7] = new SplineInterpolator().interpolate(times, inertialVY);
		// polys[8] = new SplineInterpolator().interpolate(times, inertialVZ);

		return polys;
	}// end method
	/*
	 * private PolynomialSplineFunction [] getPolynomials(DTO dto) {
	 *
	 * PolynomialSplineFunction [] polys = new PolynomialSplineFunction[9];
	 *
	 * //TODO Magari va differenziato tra spot e strip
	 *
	 * List listOfEpochs = dto.getDtoAccessList().get(0).getSatellite().getEpochs();
	 *
	 * List<EpochBean> listOfUsableBean = new ArrayList<EpochBean>();
	 *
	 * //evaluate first access Access firstAccess=dto.getDtoAccessList().get(0);
	 * Access lastAccess =
	 * dto.getDtoAccessList().get(dto.getDtoAccessList().size()-1); int
	 * firstAccessStartingIndex=firstAccess.getStartingPointWindowIndex();
	 *
	 * //Add the first four epochs to list of useful Epochs
	 *
	 * listOfUsableBean.add((EpochBean) listOfEpochs.get(firstAccessStartingIndex));
	 * listOfUsableBean.add((EpochBean)
	 * listOfEpochs.get(firstAccessStartingIndex+1));
	 * listOfUsableBean.add((EpochBean)
	 * listOfEpochs.get(firstAccessStartingIndex+2));
	 * listOfUsableBean.add((EpochBean)
	 * listOfEpochs.get(firstAccessStartingIndex+3)); int
	 * currentIndex=firstAccessStartingIndex+3; EpochBean currentEpoch = (EpochBean)
	 * listOfEpochs.get(currentIndex);
	 * while(currentEpoch.getEpoch()<dto.getStopTime()) { currentIndex++;
	 * currentEpoch = (EpochBean) listOfEpochs.get(currentIndex);
	 * listOfUsableBean.add(currentEpoch); }
	 *
	 * int numberOfUsableIndex=listOfUsableBean.size();
	 *
	 *
	 * double [] times = new double[numberOfUsableIndex]; double [] ecefX = new
	 * double[numberOfUsableIndex]; double [] ecefY = new
	 * double[numberOfUsableIndex]; double [] ecefZ = new
	 * double[numberOfUsableIndex]; double [] ecefVX = new
	 * double[numberOfUsableIndex]; double [] ecefVY = new
	 * double[numberOfUsableIndex]; double [] ecefVZ = new
	 * double[numberOfUsableIndex]; double [] inertialVX = new
	 * double[numberOfUsableIndex]; double [] inertialVY = new
	 * double[numberOfUsableIndex]; double [] inertialVZ = new
	 * double[numberOfUsableIndex];
	 *
	 * int i =0; for(EpochBean e : listOfUsableBean) { times[i] = e.getEpoch();
	 * ecefX[i] = e.getX(); ecefY[i] = e.getY(); ecefZ[i] = e.getZ();
	 *
	 * ecefVX[i] = e.getVx(); ecefVY[i] = e.getVy(); ecefVZ[i] = e.getVz(); //Le
	 * componenti inerziali ancora non ci sono
	 *
	 * i++; }
	 *
	 * polys[0] = new SplineInterpolator().interpolate(times, ecefX); polys[1] = new
	 * SplineInterpolator().interpolate(times, ecefY); polys[2] = new
	 * SplineInterpolator().interpolate(times, ecefZ); polys[3] = new
	 * SplineInterpolator().interpolate(times, ecefVX); polys[4] = new
	 * SplineInterpolator().interpolate(times, ecefVY); polys[5] = new
	 * SplineInterpolator().interpolate(times, ecefVZ);
	 *
	 * //polys[6] = new SplineInterpolator().interpolate(times, inertialVX);
	 * //polys[7] = new SplineInterpolator().interpolate(times, inertialVY);
	 * //polys[8] = new SplineInterpolator().interpolate(times, inertialVZ);
	 *
	 *
	 * return polys; }//end method
	 *
	 */

	/**
	 * @throws FeasibilityException Return the list of Acquisition request for
	 *                              feasibility purposes @return Acquisition request
	 *                              List @throws
	 */
	public List<AcqReq> getAcqList() throws FeasibilityException

	{
		// only for DTO
		if (this.foundDTO) {
			try {
				int retval = runProcess(this.SPARCFeasibilityActivity);

				this.tracer.log("External SPARC Process returned " + retval);
				if (retval != 0) {
					// just throw
					throw new FeasibilityException(
							"Error in running sparc external process. Process ended with code " + retval);
				} // end if

			} // end try
			catch (IOException | InterruptedException e) {
				this.tracer.critical(EventType.SOFTWARE_EVENT, ProbableCause.APPLICATION_SUBSYSTEM_FAILURE,
						" Error in running SPARC: " + e.getMessage());
				throw new FeasibilityException(e.getMessage());
			} // end catch
		} // end if

		// returning
		return getAcqListFromSPARCFeasibilityResponse();
	}// end method

	/**
	 *
	 * @return the list of acq after parsing output file
	 * @throws FeasibilityException
	 */
	private List<AcqReq> getAcqListFromSPARCFeasibilityResponse() throws FeasibilityException {
		this.tracer.debug("Cloning ARLIST");

		List<AcqReq> currentList = new ArrayList<>();
		// looping on AR
		for (AcqReq ar : this.acqReqList) {
			currentList.add(ar.clone());
		} // end for

		Map<String, AcqReq> arMap = new TreeMap<>();

		// looping on AR
		for (AcqReq ar : currentList) {
			arMap.put(ar.getId(), ar);
		} // end for

		// In case of interferometriq have link the AR in the currentList
		if (this.programmingRequest.isInterferometric()) {
			this.tracer.debug("Interferometric request, linking AR in the cloned list");

			for (AcqReq ar : this.acqReqList) {
				String currentId = ar.getId();
				String linkedId = ar.getLinkedAR().getId();

				AcqReq toBeLinkedAR = arMap.get(currentId);
				AcqReq linkedAR = arMap.get(linkedId);
				toBeLinkedAR.setLinkedAR(linkedAR);
			} // end for

		} // end if

		if (this.foundDTO) // necessary for combined with no CSG DTO FOUND
		{
			parseFeasibilityResponse(arMap); // parsing
		} // end if

		currentList = removeEmptyAr(currentList);

		// returning
		return currentList;
	}// end methood

	/**
	 *
	 * @param currentList
	 */
	private List<AcqReq> removeEmptyAr(List<AcqReq> currentList) {
		this.tracer.debug("Remove empty ar");
		List<AcqReq> newList = new ArrayList<>();
		// List<AcqReq> toBeRemoved=new ArrayList<AcqReq>();

		// in case of interferometric we have check that linked DTO are still
		// present.
		// This check should be performed after passtrough check in case of
		// passtroogh request
		if (this.programmingRequest.isInterferometric()) {
			checkIfInterferometricAreConsistent(currentList);
		}

		for (AcqReq ar : currentList) {
			if (!ar.getDTOList().isEmpty()) {
				newList.add(ar);
			} // end if
		} // end for

		/*
		 * List<AcqReq> linkedList=new ArrayList<AcqReq>();
		 *
		 * linkedList.addAll(newList);
		 *
		 * if(this.programmingRequest.isInterferometric()) {
		 * this.tracer.debug("Remove ar with not linked AR"); for(AcqReq ar : newList) {
		 * AcqReq linkedAR = ar.getLinkedAR(); if(linkedAR!=null) {
		 * if(!linkedList.contains(linkedAR)) { toBeRemoved.add(ar); } } }//end for
		 * for(AcqReq ar : toBeRemoved) { linkedList.remove(ar); } }//end if
		 */
		return newList;

	}// end methods

	/**
	 * Check for interferometric linked DTO. It removes linked orphan DTOs
	 *
	 * @param arList
	 */
	private void checkIfInterferometricAreConsistent(List<AcqReq> arList) {
		// looping on AR
		for (AcqReq ar : arList) {
			ArrayList<DTO> newDTOList = new ArrayList<>();
			List<DTO> lknkedDTOList = ar.getLinkedAR().getDTOList();
			// looping on DTO
			for (DTO dto : ar.getDTOList()) {
				String id = dto.getId();

				for (DTO linkedDTO : lknkedDTOList) {
					if (id.equals(linkedDTO.getId())) // on interferometric
														// request linked DTO are
														// built having the same
														// ID
					{
						newDTOList.add(dto);
						break;
					} // end if
				} // end for on linked dto

			} // end for on DTO list
			ar.setDTOList(newDTOList);
		} // end for on arList

	}// end checkIfInterferometricAreConsistent

	/**
	 * Parse the feasibility response
	 *
	 * @throws FeasibilityException
	 * @throws ParserConfigurationException
	 */
	protected void parseFeasibilityResponse(Map<String, AcqReq> arMap) throws FeasibilityException {
		this.tracer.debug("parsing SPARC Feasibility response");
		// evaluating path
		String responsePath = this.workingDir + File.separator + this.sparcOutputFileName;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		// set namespace aware
		dbf.setNamespaceAware(true);
		DocumentBuilder db;
		try {
			this.tracer.debug("Validating feasibility sparc output");
			db = dbf.newDocumentBuilder();
			// parsing response
			Document doc = db.parse(responsePath);
			Node root = doc.getFirstChild();
			// validating against schema
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(new File(this.outPutSchema));
			schema.newValidator().validate(new DOMSource(doc));

			this.tracer.debug("Sparc output is a valid ducument");
			// retrieving dto elements
			NodeList dtoElementList = doc.getElementsByTagName(DTOTagName);
			// current element
			Element currentDTOElement;
			// lopping on elements
			for (int i = 0; i < dtoElementList.getLength(); i++) {
				currentDTOElement = (Element) dtoElementList.item(i);
				parseDTOElementInFeasibilityOut(arMap, currentDTOElement);
			} // end for

		} // end try
		catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException
				| FeasibilityException e) {
			// log
			this.tracer.critical(EventType.SOFTWARE_EVENT, ProbableCause.APPLICATION_SUBSYSTEM_FAILURE,
					" Error in running parsing SPARC output: " + e.getMessage());
			// rethrow
			throw new FeasibilityException(e.getMessage());
		} // end catch

	}// end method

	/**
	 * Parse a DTO element and modify the relevant DTO
	 *
	 * @param arMap
	 * @param dto
	 * @throws XPathExpressionException
	 */
	protected void parseDTOElementInFeasibilityOut(Map<String, AcqReq> arMap, Element dto)
			throws FeasibilityException, XPathExpressionException {

		String ARID = XMLUtils.getChildElementText(dto, ARIDTagName);
		String DTOID = XMLUtils.getChildElementText(dto, DTOIDTagName);

		String sparcInfo = XMLUtils.getChildElementText(dto, SparcInfoTagName);

		String dtoSizeAsString = XMLUtils.getChildElementText(dto, DTOSizeTagName);
		logger.debug("DTO SIZE "+dtoSizeAsString);
		double dtoSize = 0;

		try {
			dtoSize = Double.valueOf(dtoSizeAsString);
			logger.debug("DTO SIZE AS DOUBLE "+dtoSize);

		} // end try
		catch (Exception e) {
			// just log
			logger.error("candidate DTO for rejection, not found a valid size!");
		} // end catch

		this.tracer.debug("parsing SPARC Feasibility response DTO " + DTOID + " of AR " + ARID);

		/*
		 * try { id=Integer.parseInt(DTOID); } catch(Exception e) { throw new
		 * FeasibilityException("Extra DTO with worng id sparc response "); }
		 */
		AcqReq currentAcq = arMap.get(ARID);
		if (currentAcq == null) {
			throw new FeasibilityException("Extra DTO in sparc response ");
		} // end if

		DTO currentDTO = null;
		for (DTO d : currentAcq.getDTOList()) {
			if (d.getId().equals(DTOID)) {
				// end loooping
				currentDTO = d;
				break;
			} // end if
		} // end for

		if (currentDTO == null) {
			// just log
			throw new FeasibilityException("DTO " + DTOID + " of AR " + ARID + " not found in sparc response");
		} // end if

		NodeList sampleList = dto.getElementsByTagName(SampleTagName);

		int lenght = sampleList.getLength();

		if (lenght == 0) {
			this.tracer.log("DTO " + DTOID + " of AR " + ARID + " rejected by SPARC");
			currentAcq.getDTOList().remove(currentDTO);
		} // end if
		else {
			if (lenght < 2) {
				// just log
				throw new FeasibilityException(
						"DTO " + DTOID + " of AR " + ARID + " has only " + lenght + " sample in sparc response");
			} // end if
			this.tracer.log("Elaborating AR" + ARID + " DTO " + DTOID + " from SPARC response");
			modifyDTOWithFeasibilityOutParameters(currentDTO, sampleList);
			// set sparc info
			currentDTO.setSparcInfo(sparcInfo);

			currentDTO.setDtoSize(dtoSize);// *FeasibilityConstants.dtoSizeMultiplicator);
		} // end else

	}// end methods

	/**
	 * Modify the DTO taking into account the sparc results for feasibility
	 *
	 * @param dto
	 * @param sampleList
	 * @throws XPathExpressionException
	 * @throws FeasibilityException
	 */
	protected void modifyDTOWithFeasibilityOutParameters(DTO dto, NodeList sampleList)
			throws XPathExpressionException, FeasibilityException {

		this.tracer.debug(" Modifiyng parameters of DTO " + dto.getId());

		int sampleListLenght = sampleList.getLength();

		Element firstSample = (Element) sampleList.item(0);

		Element lastSample = (Element) sampleList.item(sampleListLenght - 1);

		/**
		 * Facc
		 */

		double startTime;
		double stopTime;

		double earlyNear;
		double earlyfar;

		double lateNear;
		double lateFar;

		String appo;

		int lookSide = dto.getLookSide();

		try {
			// retrieving values
			appo = XMLUtils.getChildElementText(firstSample, AccessTimeTagName);

			// refers to DTO start in case of STRIP the square start in case of
			// spotlight
			startTime = Double.parseDouble(appo) / sparcTimeConversion;
			appo = XMLUtils.getChildElementText(firstSample, OffNadirNearTagName);
			earlyNear = Double.parseDouble(appo);
			appo = XMLUtils.getChildElementText(firstSample, OffNadirFarTagName);
			earlyfar = Double.parseDouble(appo);

			appo = XMLUtils.getChildElementText(lastSample, AccessTimeTagName);
			// refers to DTO stop in case of STRIP the square stop in case of
			// spotlight
			stopTime = Double.parseDouble(appo) / sparcTimeConversion;
			appo = XMLUtils.getChildElementText(lastSample, OffNadirNearTagName);
			lateNear = Double.parseDouble(appo);
			appo = XMLUtils.getChildElementText(lastSample, OffNadirFarTagName);
			lateFar = Double.parseDouble(appo);

		} // end try
		catch (Exception e) {
			// just log
			throw new FeasibilityException("DTO " + dto.getId() + " has malformed fields in sprc response");
		} // end catch

		Satellite s = dto.getSat();

		EpochBean startEpoch = s.getEpochAt(startTime);
		EpochBean stopEpoch = s.getEpochAt(stopTime);

		double[][] earlyCorners;
		double[][] farCorners;

		// if look side left we have to change the signum of the angles
		if (lookSide == FeasibilityConstants.LeftLookSide) {
			earlyCorners = dto.evaluateLLHcorners(startEpoch.getoXyz(), startEpoch.getoVxVyVz(), -earlyNear, -earlyfar);
			farCorners = dto.evaluateLLHcorners(stopEpoch.getoXyz(), stopEpoch.getoVxVyVz(), -lateNear, -lateFar);
		} // end if
		else {
			earlyCorners = dto.evaluateLLHcorners(startEpoch.getoXyz(), startEpoch.getoVxVyVz(), earlyNear, earlyfar);
			farCorners = dto.evaluateLLHcorners(stopEpoch.getoXyz(), stopEpoch.getoVxVyVz(), lateNear, lateFar);
		} // end else

		/**
		 * Setting corners
		 */
		dto.setFirstCorner(earlyCorners[0]);
		/**
		 * Setting corners
		 */
		dto.setSecondCorner(earlyCorners[1]);
		/**
		 * Setting corners
		 */
		dto.setThirdCorner(farCorners[1]);
		/**
		 * Setting corners
		 */
		dto.setFourthCorner(farCorners[0]);

		// in case of spotlight the value returned by sparc startTime and
		// stopTime refer to square access. In order to evaluate the start and
		// stop of DTO we have
		// to perform teh calculation below
		if (dto instanceof SpotLightDTO) {
			// TODO fare quello che si deve fare
			// spotlight
			SpotLightDTO d = (SpotLightDTO) dto;

			double dtoStart = d.getStartTime();
			double dtoStop = d.getStopTime();
			double duration = dtoStop - dtoStart;

			double originalSquareStart = d.getSquareStart();

			double shift = originalSquareStart - startTime;

			dtoStart = dtoStart + shift;
			dtoStop = dtoStart + duration;

			d.setStartTime(dtoStart);
			d.setStopTime(dtoStop);

			d.setSquareStart(startTime);
			d.setSquareStop(stopTime);
		} // end if
		else {
			// not spot
			dto.setStartTime(startTime);
			dto.setStopTime(stopTime);

		} // end else

	}// end methods

	/**
	 * Run the SPARC process
	 *
	 * @param typeOfActivity
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected int runProcess(String typeOfActivity) throws IOException, InterruptedException {
		this.tracer.log("try to run external SPARC process");
		ProcessBuilder pb = new ProcessBuilder(this.sparcCommand, typeOfActivity, this.workingDir);
		String pathLog = this.workingDir + File.separator + this.SparcLogFileName;
		this.tracer.debug("SPARC LOG: " + pathLog);
		File log = new File(pathLog);
		pb.redirectErrorStream(true);
		pb.redirectOutput(Redirect.appendTo(log));// appending stdout to log
		pb.directory(new File(this.SparcDirectory));

		Process process;

		/**
		 * In order to not increase the load of system we allow only one forkexec at
		 * time
		 */
		synchronized (synchronizationObject) {
			process = pb.start();
		} // end synchro
		int errCode = process.waitFor();
		return errCode;

	}// end method

}// end class
