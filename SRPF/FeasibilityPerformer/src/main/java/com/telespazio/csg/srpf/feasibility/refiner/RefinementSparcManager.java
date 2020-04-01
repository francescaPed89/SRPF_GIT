/**
*
* MODULE FILE NAME:	RefinementSparcManager.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			It generates input to be sent to SPARC  and elaborates output for Refinement purpose
*
* PURPOSE:			Used to interact with SPARC
*
* CREATION DATE:	19-07-2017
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

package com.telespazio.csg.srpf.feasibility.refiner;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.telespazio.csg.srpf.feasibility.DTO;
import com.telespazio.csg.srpf.feasibility.FeasibilityConstants;
import com.telespazio.csg.srpf.feasibility.FeasibilityException;
import com.telespazio.csg.srpf.feasibility.SPARCManager;
import com.telespazio.csg.srpf.feasibility.SpotLightDTO;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.DateUtils;
import com.telespazio.csg.srpf.utils.PropertiesReader;
import com.telespazio.csg.srpf.utils.ReferenceFrameUtils;
import com.telespazio.csg.srpf.utils.XMLUtils;

/**
 * It generates input to be sent to SPARC and elaborates output for Refinement
 * purpose
 *
 * @author Amedeo Bancone
 * @version 1.0
 *
 */
public class RefinementSparcManager extends SPARCManager

{

    // log
    private TraceManager tracer = new TraceManager();
    // string for sparc
    protected final String SPARCRefinementActivity = "DTOParametersRefinement";

    // true if at least one refinible dto has been found
    // if false sparc wil not be invoked
    private boolean foundRefineableDTO = false;

    // Maps holding the pr vs ar dtp maos
    Map<String, Map<String, Map<String, DTO>>> prArMap = null;

    /**
     *
     * Constructor
     *
     * @param prArMap
     *            map prid vs ar dto map
     * @param sparcMode
     * @param workingDir
     * @throws ParserConfigurationException
     * @throws ParserConfigurationException,
     *             TransformerException
     */
    public RefinementSparcManager(Map<String, Map<String, Map<String, DTO>>> prArMap, int sparcMode, String workingDir) throws ParserConfigurationException, TransformerException
    {
        super();
        this.workingDir = workingDir;
        this.sparcMode = sparcMode;
        // schema default
        this.outPutSchema = "/opt/SRPF/SPARC/XML_SCHEMAS/RefinementOutput.xsd";

        this.noNamespaceSchemaLocationAttrValue = "RefinementInput.xsd";
        // sparc input filename default
        this.sparcInputFileName = "sparc_input_refinement.xml";
        // sparc output file name default
        this.sparcOutputFileName = "sparc_output_refinement.xml";

        this.prArMap = prArMap;
        /**
         * this should be set to true cause is a protected member of super class
         * that should be set to true in this case
         *
         * @see SPARCManager
         */
        this.foundDTO = true;
        // initializing
        initialize();
        // building input for sparc
        buildInputForRefinement();

    } // end methods

    /**
     * Initialize value from configuration
     */
    private void initialize()
    {
        // get schema from configuration
        String value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.SPARC_REFINEMENT_OUT_SCHEMA_CONF_KEY);
        if (value != null)
        {
            this.outPutSchema = value;
        } // end if
        else
        {
            // do nothing
            // just log
            this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.SPARC_REFINEMENT_OUT_SCHEMA_CONF_KEY + " in configuration");
        } // end else
    }// end methods

    /**
     * Build the input file
     *
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    private void buildInputForRefinement() throws ParserConfigurationException, TransformerException
    {

        this.tracer.log("Building SPARC input for Refinement");

        // working dir
        String fileName = this.workingDir + System.getProperty("file.separator") + this.sparcInputFileName;// +
                                                                                                           // java.time.LocalDateTime.now().toString();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        // creating XML doc
        Document doc = db.newDocument();
        // creating DTO list element
        Element DTOList = doc.createElement(DTOListTagName);

        DTOList.setAttribute(xsiAttr, xsiAttrValue);
        DTOList.setAttribute(cmnAttr, cmnAttrValue);
        DTOList.setAttribute(noNamespaceSchemaLocationAttr, this.noNamespaceSchemaLocationAttrValue);

        doc.appendChild(DTOList);

        if (this.prArMap == null)
        {
            this.prArMap = new TreeMap<>();
        } // end if

        DTO currentDto;

        // false until we found a DTO refinable, so we can invoke SPARC
        this.foundRefineableDTO = false;

        String currentPRID = "";
        String currentARID = "";
        String currentDTOID = "";
        // looping on pr
        for (Map.Entry<String, Map<String, Map<String, DTO>>> armap : this.prArMap.entrySet())
        {

            currentPRID = armap.getKey();

            // Cicle on AR
            for (Map.Entry<String, Map<String, DTO>> dtoMap : armap.getValue().entrySet())
            {
                currentARID = dtoMap.getKey();
                // Cicle on DTO
                for (Map.Entry<String, DTO> dtoEntry : dtoMap.getValue().entrySet())
                {
                    currentDTOID = dtoEntry.getKey();
                    currentDto = dtoEntry.getValue();
                    // XMLUtils.setChildElementText(currentDto.getDtoElement(),
                    // FeasibilityConstants.SPARCInfoTagName, "Pippo lippo");
                    // adding DTO element if refinable
                    if (currentDto.isRefinable())
                    {
                        this.foundRefineableDTO = true;
                        Element DTOElement = doc.createElement(DTOTagName);
                        DTOList.appendChild(DTOElement);
                        fillingDTOElement(currentPRID, currentARID, currentDTOID, currentDto, DTOElement, doc);
                    } // end if

                } // end for
            } // end for

        } // end for
          // dumping on file
        dumpXmlDomTreeToFile(doc, fileName);
    }// end Methods

    /**
     * Fill the DTOElement
     *
     * @param PRID
     * @param ARID
     * @param DTOID
     * @param dto
     * @param DTOElement
     * @param doc
     */
    private void fillingDTOElement(String PRID, String ARID, String DTOID, DTO dto, Element DTOElement, Document doc)
    {
        Element DTOInfoElement = doc.createElement(DTOInfoTagName);
        DTOElement.appendChild(DTOInfoElement);

        // Inserting DTOInfo
        Element PRIDElement = doc.createElement(PRIDTagName);
        Text text = doc.createTextNode(PRID);
        PRIDElement.appendChild(text);
        DTOInfoElement.appendChild(PRIDElement);
        // inserting AR ID
        Element ARIDElement = doc.createElement(ARIDTagName);
        text = doc.createTextNode(ARID);
        ARIDElement.appendChild(text);
        DTOInfoElement.appendChild(ARIDElement);
        // insering DTO id
        Element DTOIDElement = doc.createElement(DTOIDTagName);
        text = doc.createTextNode(DTOID);
        DTOIDElement.appendChild(text);
        DTOInfoElement.appendChild(DTOIDElement);
        // SPARC MODE
        Element SPARCModeElement = doc.createElement(SPARCModeTagName);
        text = doc.createTextNode("" + this.sparcMode);
        SPARCModeElement.appendChild(text);
        DTOInfoElement.appendChild(SPARCModeElement);
        // evaluating flags
        // Stripmap
        // di2s

        String stripflag = trueFlag;
        String di2Sflag = falseFlag;

        //prima di modifica 24.02.2020
        if (dto instanceof SpotLightDTO)
        {
            stripflag = falseFlag; // no strip
            if (((SpotLightDTO) dto).isDi2sConfirmationFlag())
            {
                di2Sflag = trueFlag; // di2s flag
            } // end if
        }
        else
        {
        	
        	//check the duration of the dto
        	//if it is larger than the minDuration set the flag to true, set false otherwise
            int realDuration = (int)(DateUtils.fromCSKDurationToMilliSeconds(dto.getStopTime()) -DateUtils.fromCSKDurationToMilliSeconds(dto.getStartTime()));
            // spotlight dto
            if(realDuration<=dto.getBeam().getDtoDurationSquared())
            {
            	stripflag = falseFlag;
            }
            

        }
          // adding elements
          // Adding stripmapflag
        Element StripMapFlagElement = doc.createElement(StripmapFlagTagName);
        text = doc.createTextNode(stripflag);
        StripMapFlagElement.appendChild(text);
        DTOInfoElement.appendChild(StripMapFlagElement);

        // Adding DI2SFLAg
        Element DI2SFlagElement = doc.createElement(DI2SFlagTagName);
        text = doc.createTextNode(di2Sflag);
        DI2SFlagElement.appendChild(text);
        DTOInfoElement.appendChild(DI2SFlagElement);

        // ADDING SARMODEFLAG
        Element SarModeElement = doc.createElement(SARModeTagName);
        text = doc.createTextNode(dto.getSarMode());
        SarModeElement.appendChild(text);
        DTOInfoElement.appendChild(SarModeElement);

        // ADDING SARMODEFLAG
        Element SarBeamElement = doc.createElement(SARBeamTagName);
        text = doc.createTextNode(dto.getSarBeamName());
        SarBeamElement.appendChild(text);
        DTOInfoElement.appendChild(SarBeamElement);

        // ADDING SARPOLARIZATION
        String polarization = dto.getPolarization();
        if (!polarization.isEmpty())
        {
            Element SarPolarizationElement = doc.createElement(SARPolarizationTagName);
            text = doc.createTextNode(polarization);
            SarPolarizationElement.appendChild(text);
            DTOInfoElement.appendChild(SarPolarizationElement);
        } // end if

        // ADDING LOOKSIDE
        Element SarLookSideElement = doc.createElement(SARLookTagName);
        text = doc.createTextNode(FeasibilityConstants.getLookSideString(dto.getLookSide()));
        SarLookSideElement.appendChild(text);
        DTOInfoElement.appendChild(SarLookSideElement);

        // ADDING OrbitDirection
        Element SarOrbitDirectionElement = doc.createElement(SAROrbitDirectionTagName);
        text = doc.createTextNode(FeasibilityConstants.getOrbitDirectionAsString(dto.getOrditDirection()));
        SarOrbitDirectionElement.appendChild(text);
        DTOInfoElement.appendChild(SarOrbitDirectionElement);

        // inserting corners
        insertCornersToDTOINfo(doc, DTOInfoElement, dto);

        // inserting AZIMUTH CUT
        Element AzimuthCutElement = doc.createElement(AzimuthCutTagName);
        DTOElement.appendChild(AzimuthCutElement);
        // inserting azimuth cut
        insertAzimutCutSamples(doc, AzimuthCutElement, dto);

        // INSERTING SPARCINFO
        Element SPARCInfoElement = doc.createElement(SparcInfoTagName);
        text = doc.createTextNode(dto.getSparcInfo());
        SPARCInfoElement.appendChild(text);
        DTOElement.appendChild(SPARCInfoElement);

    }// end method

    /**
     * Insert the samples in the AzimuthCut Element
     *
     * @param doc
     * @param AzimuthCutElement
     * @param dto
     */
    private void insertAzimutCutSamples(Document doc, Element AzimuthCutElement, DTO dto)
    {
        // In refinement only to sample are used
        AzimuthCutElement.setAttribute(countAttrName, "" + 2);

        double startAccessTime;
        Vector3D startPosition;
        Vector3D startVelocity;

        double stopAccessTime;
        Vector3D stopPosition;
        Vector3D stopVelocity;

        if (dto instanceof SpotLightDTO)
        {
            // case spotlight
            SpotLightDTO sdto = (SpotLightDTO) dto;
            startAccessTime = sdto.getSquareStart();
            startPosition = sdto.getSatellitePositionAtSquareStart(); // Use
                                                                      // square
            startVelocity = sdto.getSatelliteVelocityAtSquareStart();

            stopAccessTime = sdto.getSquareStart();
            stopPosition = sdto.getSatellitePositionAtSquareStop(); // Use
                                                                    // square
            stopVelocity = sdto.getSatelliteVelocityAtSquareStop();
        } // end if
        else
        {
            // case strip
            startAccessTime = dto.getStartTime();
            startPosition = dto.getSatPosAtStart();
            startVelocity = dto.getSatVelAtStart();

            stopAccessTime = dto.getStopTime();
            // stopPosition=dto.getSatPosAtStart();
            stopPosition = dto.getSatPosAtEnd();
            // stopVelocity =dto.getSatVelAtStart();
            stopVelocity = dto.getSatEndVelocity();
        } // end else

        // inserting first sample
        Element Sample = doc.createElement(SampleTagName);
        AzimuthCutElement.appendChild(Sample);
        // filling sample
        fillSample(doc, Sample, 1, startAccessTime, startPosition, startVelocity);

        // Inserting second sample
        Sample = doc.createElement(SampleTagName);
        AzimuthCutElement.appendChild(Sample);
        // filling sample
        fillSample(doc, Sample, 2, stopAccessTime, stopPosition, stopVelocity);

    }// end method

    /**
     * Fill the Sample Element in Azimuth cut section
     *
     * @param doc
     * @param SampleElement
     * @param id
     * @param accessTime
     * @param satPos
     * @param satVelocity
     */
    private void fillSample(Document doc, Element SampleElement, int id, double accessTime, Vector3D satPos, Vector3D satVelocity)
    {
        // evaluate the azimuth cut sample:
        // position
        // velocity
        // timing
        // number of cut

        ///
        // Inserinng id
        Element SampleIDElement = doc.createElement(SampleIDTagName);
        Text text = doc.createTextNode("" + id);
        SampleIDElement.appendChild(text);
        SampleElement.appendChild(SampleIDElement);

        // inserting access time
        Element AccessTimeElement = doc.createElement(AccessTimeTagName);
        // creating text node
        text = doc.createTextNode(String.format(Locale.US, "%f", accessTime * sparcTimeConversion));
        AccessTimeElement.appendChild(text);
        SampleElement.appendChild(AccessTimeElement);

        // Inserting satellite position
        Element SatellitePositionElement = doc.createElement(SatellitePositionTagName);
        // inserting X
        Element xElement = doc.createElement(XTagName);
        text = doc.createTextNode("" + satPos.getX());
        xElement.appendChild(text);
        SatellitePositionElement.appendChild(xElement);
        // inserting y
        Element yElement = doc.createElement(YTagName);
        text = doc.createTextNode("" + satPos.getY());
        yElement.appendChild(text);
        SatellitePositionElement.appendChild(yElement);
        // inserting z
        Element zElement = doc.createElement(ZTagName);
        text = doc.createTextNode("" + satPos.getZ());
        zElement.appendChild(text);
        SatellitePositionElement.appendChild(zElement);
        // appending
        SampleElement.appendChild(SatellitePositionElement);

        // inserting satellite velocity
        Element SatelliteRelativeVelocityElement = doc.createElement(SatelliteRelativeVelocityTagName);
        // inserting X
        xElement = doc.createElement(XTagName);
        text = doc.createTextNode("" + satVelocity.getX());
        xElement.appendChild(text);
        SatelliteRelativeVelocityElement.appendChild(xElement);
        // inserting y
        yElement = doc.createElement(YTagName);
        text = doc.createTextNode("" + satVelocity.getY());
        yElement.appendChild(text);
        // appending
        SatelliteRelativeVelocityElement.appendChild(yElement);
        // inserting z
        zElement = doc.createElement(ZTagName);
        text = doc.createTextNode("" + satVelocity.getZ());
        // appending
        zElement.appendChild(text);
        SatelliteRelativeVelocityElement.appendChild(zElement);
        // appending
        SampleElement.appendChild(SatelliteRelativeVelocityElement);

        // inserting SatelliteHeight
        double[] satellitePosLLH = ReferenceFrameUtils.ecef2llh(satPos, true);
        Element SatelliteHeightElement = doc.createElement(SatelliteHeightTagName);
        text = doc.createTextNode("" + satellitePosLLH[2]);
        SatelliteHeightElement.appendChild(text);
        // appending
        SampleElement.appendChild(SatelliteHeightElement);

    }// end method

    /**
     * Perform refinement and return the pr map
     *
     * @return pr map
     * @throws IOException
     * @throws InterruptedException
     * @throws FeasibilityException
     */
    public Map<String, Map<String, Map<String, DTO>>> refine() throws FeasibilityException
    {
        // Sparc must be invoked only if there are DTO that is possible to
        // refine, in this case the sparc_refinement_input has been correctly
        // created
        if (this.foundRefineableDTO)
        {
            try
            {
                int retval = runProcess(this.SPARCRefinementActivity);
                if (retval != 0)
                {
                    throw new FeasibilityException("Error in running sparc external process. Process ended with code " + retval);
                } // end if
            } // end try
            catch (IOException | InterruptedException e)
            {
                // System.err.println("+++++++++++++++refine " +
                // e.getMessage());
                // log
                this.tracer.critical(EventType.SOFTWARE_EVENT, ProbableCause.APPLICATION_SUBSYSTEM_FAILURE, " Error in running SPARC: " + e.getMessage());
                // throw
                throw new FeasibilityException(e.getMessage());
            } // end cacth

            // parsing results
            parseSparcRefinementOutput();
        } // end if

        return this.prArMap;

    }// end method

    /**
     * Parse and validate the sparc refinement output
     *
     * @throws FeasibilityException
     */
    private void parseSparcRefinementOutput() throws FeasibilityException
    {
        this.tracer.debug("parsing SPARC Feasibility response");
        // building path of file to be parsed
        String responsePath = this.workingDir + File.separator + this.sparcOutputFileName;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db;
        try
        {
            this.tracer.debug("Validating refinement sparc output");
            db = dbf.newDocumentBuilder();
            // //System.out.println("====================================PIPPOLIPPO=========================
            // " + responsePath);
            // //System.out.println("===================================="+db.isValidating());
            // parsing
            Document doc = db.parse(responsePath);
            // //System.out.println("====================================PIPPOLIPPO1=========================
            // " + responsePath);
            Node root = doc.getFirstChild();

            // building schema
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(this.outPutSchema));
            // validading aginst schema
            schema.newValidator().validate(new DOMSource(doc));

            this.tracer.debug("Sparc output is a valid ducument");

            NodeList dtoElementList = doc.getElementsByTagName(DTOTagName);

            Element currentDTOElement;
            // looping on elements
            for (int i = 0; i < dtoElementList.getLength(); i++)
            {
                currentDTOElement = (Element) dtoElementList.item(i);
                // parsing DTO element
                parsingCurrentDTOElement(currentDTOElement);
            } // end for

        } // end try
        catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e)
        {
            // System.err.println("++++++++++++++++ parseSparcRefinementOutput"
            // + e.getMessage());
            // log
            this.tracer.critical(EventType.SOFTWARE_EVENT, ProbableCause.APPLICATION_SUBSYSTEM_FAILURE, " Error in running parsing SPARC output: " + e.getMessage());
            // rethrow
            throw new FeasibilityException(e.getMessage());
        } // end catch

    }// end method

    /**
     * Parse the current DTO element and update the relevant DTO
     *
     * @param dtoElement
     * @throws XPathExpressionException
     * @throws FeasibilityException
     */
    private void parsingCurrentDTOElement(Element dtoElement) throws XPathExpressionException, FeasibilityException
    {
        // getting pr id
        String prId = XMLUtils.getChildElementText(dtoElement, PRIDTagName);
        // getting ar id
        String arId = XMLUtils.getChildElementText(dtoElement, ARIDTagName);
        // getting dto id
        String dtoId = XMLUtils.getChildElementText(dtoElement, DTOIDTagName);

        DTO d;

        Map<String, Map<String, DTO>> pr = this.prArMap.get(prId);
        if (pr != null)
        {
            Map<String, DTO> ar = pr.get(arId);
            if (ar != null)
            {
                d = ar.get(dtoId);
                if (d != null)
                {
                    // //System.out.println("======================= " + prId + "
                    // " + arId + " DTO "+ dtoId);
                    // update dto
                    updateCurrentDTOWithRefinementoutputDTOElement(d, dtoElement);
                } // end if
                else
                {
                    // do nothing
                    // just throw
                    throw new FeasibilityException("Unknown DTO  " + dtoId + " held in Ar " + arId + " held in PR " + prId + " in SPARC Refinement Output");
                } // end else
            } // end if
            else
            {
                // do nothing
                // just throw
                throw new FeasibilityException("Unknown AR  " + arId + " held in PR " + prId + " in SPARC Refinement Output");
            } // end else
        } // end if
        else
        {
            // do nothing
            // just throw
            throw new FeasibilityException("Unknown PRID " + prId + " in SPARC Refinement Output");
        } // end else

    }// end method

    /**
     * Update the DTO
     *
     * @param dto
     * @param dtoElement
     * @throws XPathExpressionException
     * @throws FeasibilityException
     */
    private void updateCurrentDTOWithRefinementoutputDTOElement(DTO dto, Element dtoElement) throws XPathExpressionException, FeasibilityException
    {

        NodeList sampleList = dtoElement.getElementsByTagName(SampleTagName);

        int numberOfAzimuthSample = sampleList.getLength();
        // two cut in case of refine
        if ((numberOfAzimuthSample != 2) && (numberOfAzimuthSample != 0))
        {
            // do nothing
            // just throw
            throw new FeasibilityException("Wrong number of azimuth cut sample in SPARC Refinement Output");
        } // end if

        // not cut no refinable
        if (numberOfAzimuthSample == 0)
        {
            dto.setRefinable(false);
        } // end if
        else
        {
            String sparcInfo = XMLUtils.getChildElementText(dtoElement, SparcInfoTagName);

            dto.setSparcInfo(sparcInfo);

            /**
             * @TODO Eventualmente recuperare la dtosize e fare check sul
             *       passthrough nella Feasibility Refiner
             */

            // Evaluating timing information
            double startTime = 0;
            double stopTime = 0;
            String startTimeAsString = XMLUtils.getChildElementText((Element) sampleList.item(0), AccessTimeTagName);
            String stopTimeAsString = XMLUtils.getChildElementText((Element) sampleList.item(1), AccessTimeTagName);
            try
            {
                // getting timoing info
                startTime = Double.parseDouble(startTimeAsString);
                stopTime = Double.parseDouble(stopTimeAsString);

                // startTime=startTime/86400.0;
                // stopTime=stopTime/86400.0;

                startTime = startTime / SPARCManager.sparcTimeConversion;
                stopTime = stopTime / SPARCManager.sparcTimeConversion;

                if (dto instanceof SpotLightDTO)
                {
                    // spotlight dto
                    SpotLightDTO d = (SpotLightDTO) dto;

                    double dtoStart = d.getStartTime();
                    double dtoStop = d.getStopTime();
                    double duration = dtoStop - dtoStart;

                    double originalSquareStart = d.getSquareStart();

                    double shift = originalSquareStart - startTime;

                    dtoStart = dtoStart + shift;
                    dtoStop = dtoStart + duration;

                    // setting time
                    d.setStartTime(dtoStart);
                    d.setStopTime(dtoStop);

                    d.setSquareStart(startTime);
                    d.setSquareStop(stopTime);
                } // end else
                else
                {
                    // strip dto
                    // setting time
                    dto.setStartTime(startTime);
                    dto.setStopTime(stopTime);
                } // end else

                // getting corners
                NodeList nl = ((Element) sampleList.item(0)).getElementsByTagName(DTONearCornerTagName);
                Element currentCorner = (Element) nl.item(0);
                double[] firstCorner = getLLHFromCornerElement(currentCorner);

                nl = ((Element) sampleList.item(0)).getElementsByTagName(DTOFarCornerTagName);
                currentCorner = (Element) nl.item(0);
                double[] secondCorner = getLLHFromCornerElement(currentCorner);
                ;

                nl = ((Element) sampleList.item(1)).getElementsByTagName(DTONearCornerTagName);
                currentCorner = (Element) nl.item(0);
                double[] fourthCorner = getLLHFromCornerElement(currentCorner);

                nl = ((Element) sampleList.item(1)).getElementsByTagName(DTOFarCornerTagName);
                currentCorner = (Element) nl.item(0);
                double[] thirdCorner = getLLHFromCornerElement(currentCorner);

                // setting corners
                dto.setFirstCorner(firstCorner);
                dto.setSecondCorner(secondCorner);
                // setting corners
                dto.setThirdCorner(thirdCorner);
                dto.setFourthCorner(fourthCorner);

            } // end try
            catch (Exception e)
            {
                // System.err.println("+++++++++++++++++++updateCurrentDTOWithRefinementoutputDTOElement"
                // + e.getMessage());
                // do nothing
                // just throw
                new FeasibilityException("Error in elaborating Sparc Refinement Output" + e.getMessage());
            } // end catch

        } // end else

    }// end method

    /**
     * Given a Corner element return the llh coordinates
     *
     * @param corner
     * @return llh
     * @throws XPathExpressionException
     */
    private double[] getLLHFromCornerElement(Element corner) throws XPathExpressionException
    {
        // return vector
        double[] llh = new double[3];
        // getting llh
        String latitudeAsString = XMLUtils.getChildElementText(corner, LatitudeTagName);
        String longitudeAsString = XMLUtils.getChildElementText(corner, LongitudeTagName);
        String heightAsString = XMLUtils.getChildElementText(corner, HeightTagName);

        try
        {
            llh[0] = Double.parseDouble(latitudeAsString);
            llh[1] = Double.parseDouble(longitudeAsString);
            llh[2] = Double.parseDouble(heightAsString);
        } // end try
        catch (Exception e)
        {
            // System.err.println("+++++++++++++++++++getLLHFromCornerElement" +
            // e.getMessage());
            // do nothing
            // just log
            new FeasibilityException("Error in elaborating DTOCorners in Sparc Refinement Output " + e.getMessage());
        } // end try
          // returning
        return llh;
    }// end method

}// end class
