/**
*
* MODULE FILE NAME:	FeasibilityRefiner.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			This class perform the DTOs ' refinement
*
* PURPOSE:			DTO 's refinement
*
* CREATION DATE:	12-03-2017
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.telespazio.csg.srpf.dataManager.inMemoryOrbitalData.EphemeridInMemoryDB;
import com.telespazio.csg.srpf.dem.DEMManager;
import com.telespazio.csg.srpf.feasibility.Access;
import com.telespazio.csg.srpf.feasibility.DTO;
import com.telespazio.csg.srpf.feasibility.FeasibilityConstants;
import com.telespazio.csg.srpf.feasibility.FeasibilityException;
import com.telespazio.csg.srpf.feasibility.GridException;
import com.telespazio.csg.srpf.feasibility.GridPoint;
import com.telespazio.csg.srpf.feasibility.Satellite;
import com.telespazio.csg.srpf.feasibility.SpotLightDTO;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.DateUtils;
import com.telespazio.csg.srpf.utils.PropertiesReader;
import com.telespazio.csg.srpf.utils.XMLUtils;

/**
 *
 * This class perform the feasibility refinement
 *
 * @autor Amedeo Bancone
 * @version 1.0
 *
 */
public class FeasibilityRefiner
{
	static final Logger logger = LogManager.getLogger(FeasibilityRefiner.class.getName());

    // Trecer
    private TraceManager tracer = new TraceManager();

    // schema path
    protected String xsdPath;

    // DEM
    protected DEMManager dem;

    // XML DOC
    protected Document doc;

    // working directory
    protected String workingDir;

    // List holding the name of beam used as spotlight
    protected List<String> spotlightSensorModeList;

    // list of passthrough PR
    // protected List<String> passTrhoughPRList = new ArrayList<String>();

    // Map of pr with Passttrouh
    protected Map<String, Element> prPassThoughMap = new TreeMap<>();

    // MAPS ar dto map vs pr
    protected Map<String, Map<String, Map<String, DTO>>> prArMap = new TreeMap<>();

    // Maps holding Satellite object
    protected Map<String, Satellite> satelliteMap = new TreeMap<>();

    // Min startTime in the request, used to SELECT paw
    protected double minStartTime = 0;
    // max starttome in the request used for paw
    protected double maxStopTime = 0;
    // XML prefixes map
    protected Map<String, String> prefixNSMap = new TreeMap<>();

    /**
     * default Constructor
     *
     * @throws Exception
     */
    public FeasibilityRefiner() throws Exception
    {
        String value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.MULTI_MISSION_CHECK_CONFLICT_XSD_PATH_CONF_KEY);
        if (value != null)
        {

            this.xsdPath = value;
        } // end if
        else
        {
            // logger.fatal("Unable to found " +
            // FeasibilityConstants.XSD_PATH_CONF_KEY + " in conffiguration");
            // log
            this.tracer.critical(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.XSD_PATH_CONF_KEY + " in configuration");
            // throw
            throw new IOException("Unable to found " + FeasibilityConstants.MULTI_MISSION_CHECK_CONFLICT_XSD_PATH_CONF_KEY + " in configuration");
        } // end else

        value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.DEM_BASE_DIR_PATH_CONF_KEY);
        if (value != null)
        {
            // setting dem
            this.dem = new DEMManager(value);
        } // end if
        else
        {
            // logger.warn("Unable to found " +
            // FeasibilityConstants.DEM_BASE_DIR_PATH_CONF_KEY + " in
            // conffiguration. DEM will return always 0");
            // logging
            this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.DEM_BASE_DIR_PATH_CONF_KEY + " in conffiguration");

        } // end else

        this.tracer.debug("Filling SpotLight seonsor mode list");
        SatelliteBO bo = new SatelliteBO();
        this.spotlightSensorModeList = bo.getSpotLightSensorModeList();
        // filling map
        fillSatelliteMap();

        // initConfiguration();
    }// end method

    /**
     * FIll the satellite map
     *
     * @throws Exception
     */
    protected void fillSatelliteMap() throws Exception
    {
        this.tracer.debug("Filling satellite map");
        SatelliteBO bo = new SatelliteBO();
        // retrieving satellitebean list
        List<SatelliteBean> beanList = bo.getSatelliteBeanList();

        String satName;
        // looping
        for (SatelliteBean bean : beanList)
        {
            // Buildong new satellite
            satName = bean.getSatelliteName();
            Satellite s = new Satellite(bean);
            // adding to map
            this.satelliteMap.put(satName, s);
        } // end for

    }// end Method

    /**
     * Perform the feasibility refinement
     *
     * @param prlistPath
     * @return the path of the response
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws TransformerException
     * @throws GridException
     * @throws XPathExpressionException
     * @throws FeasibilityException
     * @throws InterruptedException
     */
    public String performRefinement(String prlistPath) throws ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException, GridException, FeasibilityException, InterruptedException
    {

        String analysePrResponse = "";
        this.tracer.debug("Performing refinement");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db;
        db = dbf.newDocumentBuilder();
        this.doc = db.parse(prlistPath);
        this.prefixNSMap = XMLUtils.createNSPrefixMap(this.doc);
        Node root = this.doc.getFirstChild();
        this.tracer.debug("Loaded document " + prlistPath);
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new File(this.xsdPath));
        schema.newValidator().validate(new DOMSource(this.doc));
        this.tracer.debug("Parsed valid document");

        // this.tracer.debug("============Renaming doc");
        // renaming response main node
        this.doc.renameNode(root, root.getNamespaceURI(), FeasibilityConstants.AnalysePRListResponseTagName);
        // this.tracer.debug("================Renamed doc");
        // evaluating response path
        analysePrResponse = evaluateResponseName(prlistPath);
        // build map
        buildMap();
        // do refine
        doRefinement();
        // update xml
        updateXML();

        this.tracer.debug("writing request xml...");
        // dump response to xml file
        XMLUtils.dumpResponseToFile(this.doc, analysePrResponse);
        this.tracer.debug("done.");

        // returning response oath
        return analysePrResponse;
    }// end method

    /**
     * Update the XML doc
     *
     * @throws XPathExpressionException
     */
    protected void updateXML() throws XPathExpressionException
    {
        DTO currentDto;
        // Map<String,Map<String,Map<String,DTO>>>
        // Cicle on PR
        for (Map.Entry<String, Map<String, Map<String, DTO>>> armap : this.prArMap.entrySet())
        {
            // boolean passTroughFlag =
            // this.passTrhoughPRList.contains(armap.getKey());

            // Cicle on AR
            for (Map.Entry<String, Map<String, DTO>> dtoMap : armap.getValue().entrySet())
            {
                for (Map.Entry<String, DTO> dtoEntry : dtoMap.getValue().entrySet())
                {
                    currentDto = dtoEntry.getValue();
                    // XMLUtils.setChildElementText(currentDto.getDtoElement(),
                    // FeasibilityConstants.SPARCInfoTagName, "Pippo lippo");
                    updateXMLElement(dtoEntry.getValue());
                }
            } // end for

        } // end for
    }// end method

    /**
     * Update the xml tag
     *
     * @param d
     * @throws XPathExpressionException
     */
    protected void updateXMLElement(DTO d) throws XPathExpressionException
    {

        Element dtoElement = d.getDtoElement();

        Element plannedElement;

        Element ar = d.getARElement();

        boolean haveCreatePlannedElement = true;

        NodeList plannedElementList = dtoElement.getElementsByTagNameNS(FeasibilityConstants.PlannedTagNameNS, FeasibilityConstants.PlannedTagName);
        if (plannedElementList.getLength() == 0)
        {
            // creating planned element cause not exist
            plannedElement = this.doc.createElement(FeasibilityConstants.PlannedTagName);
            plannedElement = XMLUtils.createElement(this.doc, this.prefixNSMap, FeasibilityConstants.PlannedTagName, FeasibilityConstants.PlannedTagNameNS);
            haveCreatePlannedElement = true;
        } // end if
        else
        {
            // updating planned element
            plannedElement = (Element) plannedElementList.item(0);
            haveCreatePlannedElement = false;
        } // end else

        if (d.isRefinable())
        {

            plannedElement.setTextContent(FeasibilityConstants.PlannedTrueValue);
            NodeList dtoSensingTimeList = dtoElement.getElementsByTagNameNS(FeasibilityConstants.DTOSensingTagNameNS, FeasibilityConstants.DTOSensingTagName);
            Element dtoSensingTimeElement = (Element) dtoSensingTimeList.item(0);
            XMLUtils.setChildElementText(dtoSensingTimeElement, FeasibilityConstants.timeStartTagName, DateUtils.fromCSKDateToISOFMTDateTime(d.getStartTime()), FeasibilityConstants.timeStartTagNameNS);
            XMLUtils.setChildElementText(dtoSensingTimeElement, FeasibilityConstants.timeStopTagName, DateUtils.fromCSKDateToISOFMTDateTime(d.getStopTime()), FeasibilityConstants.timeStopTagNameNS);

            NodeList polygonList = dtoElement.getElementsByTagNameNS(FeasibilityConstants.PolygonTagNameNS, FeasibilityConstants.PolygonTagName);
            Element polygonElement = (Element) polygonList.item(0);
            XMLUtils.setChildElementText(polygonElement, FeasibilityConstants.PosListTagName, d.getPosListString(), FeasibilityConstants.PosListTagNameNS);

            XMLUtils.setChildElementText(dtoElement, FeasibilityConstants.SPARCInfoTagName, d.getSparcInfo(), FeasibilityConstants.SPARCInfoTagNameNS);

            // orbit id
            XMLUtils.setChildElementText(dtoElement, FeasibilityConstants.orbitNumberTagName, "" + d.getOrbitId(), FeasibilityConstants.orbitNumberTagNameNS);
            // track number
            XMLUtils.setChildElementText(dtoElement, FeasibilityConstants.trackNumberTagName, "" + d.getTrackNumber(), FeasibilityConstants.trackNumberTagNameNS);

            Element arPolygonElement = (Element) XMLUtils.getFirstDirectChild(ar, FeasibilityConstants.PolygonTagName, FeasibilityConstants.PolygonTagNameNS);

            if (arPolygonElement != null)
            {
                XMLUtils.setChildElementText(arPolygonElement, FeasibilityConstants.PosListTagName, d.getPosListString(), FeasibilityConstants.PosListTagNameNS);
            } // end if

        } // end if
        else
        {
            // not planned
            plannedElement.setTextContent(FeasibilityConstants.PlannedFalseValue);
        } // end else

        if (haveCreatePlannedElement)
        {
            // new planned element have fill it
            NodeList backupFlagNodeList = dtoElement.getElementsByTagNameNS(FeasibilityConstants.BackUpFlagTagNameNS, FeasibilityConstants.BackUpFlagTagName);
            NodeList alertIdNodeList;
            NodeList sparcInfoList;
            /**
             * Because of the existence of one or more of the below elements the
             * position of planned element varies
             */
            if (backupFlagNodeList.getLength() != 0)
            {
                Element backupFlag = (Element) backupFlagNodeList.item(0);
                dtoElement.insertBefore(plannedElement, backupFlag);
            } // end if
            else if ((alertIdNodeList = dtoElement.getElementsByTagNameNS(FeasibilityConstants.AlertIDTagNameNS, FeasibilityConstants.AlertIDTagName)).getLength() != 0)
            {
                Element alertElement = (Element) alertIdNodeList.item(0);
                dtoElement.insertBefore(plannedElement, alertElement);
            } // end else if
            else if ((sparcInfoList = dtoElement.getElementsByTagNameNS(FeasibilityConstants.SPARCInfoTagNameNS, FeasibilityConstants.SPARCInfoTagName)).getLength() != 0)
            {
                Element sparcInfoElement = (Element) sparcInfoList.item(0);
                dtoElement.insertBefore(plannedElement, sparcInfoElement);
            } // end else if
            else
            {
                dtoElement.appendChild(plannedElement);
            } // end else
        } // end if

    }// end

    /**
     * @throws XPathExpressionException
     * @throws FeasibilityException
     * @throws TransformerException
     * @throws ParserConfigurationException
     * @throws InterruptedException
     * @throws IOException
     *
     */
    void doRefinement() throws XPathExpressionException, FeasibilityException, ParserConfigurationException, TransformerException, IOException, InterruptedException
    {
        DTO currentDto;

        boolean isFirstDTO = true;

        double currentStartTime = 0;
        double currentStopTime = 0;

        // Map<String,Map<String,Map<String,DTO>>>
        // Cicle on PR
        for (Map.Entry<String, Map<String, Map<String, DTO>>> armap : this.prArMap.entrySet())
        {

            // Cicle on AR
            for (Map.Entry<String, Map<String, DTO>> dtoMap : armap.getValue().entrySet())
            {
                // Cicle on DTO
                for (Map.Entry<String, DTO> dtoEntry : dtoMap.getValue().entrySet())
                {
                    currentDto = dtoEntry.getValue();
                    // XMLUtils.setChildElementText(currentDto.getDtoElement(),
                    // FeasibilityConstants.SPARCInfoTagName, "Pippo lippo");
                    if (isFirstDTO)
                    {
                        this.minStartTime = currentDto.getStartTime();
                        isFirstDTO = false;
                    }
                    evaluateNewDTOparameters(currentDto);
                    currentStartTime = currentDto.getStartTime();
                    currentStopTime = currentDto.getStopTime();

                    if (currentStartTime < this.minStartTime)
                    {
                        this.minStartTime = currentStartTime;
                    } // end if
                    if (currentStopTime > this.maxStopTime)
                    {
                        this.maxStopTime = currentStopTime;
                    } // end if

                } // end for
            } // end for

        } // end for

        // Bisogna controllare per PAW e PASSTHROUGH

        checkForPaw();
        /*
         * Alessandro ha detto che il controllo per il passthrough non va fatto
         * ad aogni modo va rifatto dopo esecuzione dello sparc che restituisce
         * anche DTO size try { checkForPassThrough(); } catch (Exception e) {
         * throw new FeasibilityException("Error during refinement: " +
         * e.getMessage()); }
         */

        RefinementSparcManager rspm = new RefinementSparcManager(this.prArMap, 0, this.workingDir);

        this.prArMap = rspm.refine();

        /**
         * @TODO eventualmente è qui che va rifatto il check sul passthrough
         */

    }// end method

    /**
     * Set to false the the refinement flag for the DTO that fall inside not
     * deferreable PAW
     */
    private void checkForPaw()
    {
        Map<String, ArrayList<PlatformActivityWindowBean>> satellitePawMap;

        PlatformActivityWindowBO bo = new PlatformActivityWindowBO();
		logger.debug("FROM METHOD checkForPaw");

        try
        {

            String currentPRId;
            String currentARId;
            String currentDTOId;

            DTO currentDTO;
            // paw list
            ArrayList<PlatformActivityWindowBean> pawList;
            // looping on PRs
            for (Map.Entry<String, Map<String, Map<String, DTO>>> armap : this.prArMap.entrySet())
            {
                currentPRId = armap.getKey();
                // Cicle on AR
                for (Map.Entry<String, Map<String, DTO>> dtoMap : armap.getValue().entrySet())
                {
                    currentARId = dtoMap.getKey();
                    // Cicle on DTO
                    for (Map.Entry<String, DTO> dtoEntry : dtoMap.getValue().entrySet())
                    {
                        currentDTO = dtoEntry.getValue();
                        currentDTOId = currentDTO.getId();
                        
//                        int satId = currentDTO.getSat().getSatID();
//                        System.out.println("dto id :"+currentDTOId + " is referred to sat name "+currentDTO.getSatName()+" with id :"+satId);
//                        satellitePawMap = bo.getPawsNewMethod(satId,currentDTO.getStartTime(), currentDTO.getStopTime());

                        // retrieve paws
                        satellitePawMap = bo.getPaws(currentDTO.getStartTime(), currentDTO.getStopTime());
                        pawList = satellitePawMap.get(currentDTO.getSat().getName());
                        Satellite sat = currentDTO.getSat();
                      //  sat.setPawList(pawList);
                        
                        //get all paws in overlap with the current dto 
                      //  satellitePawMap = bo.getPaws(currentDTO.getStartTime(), currentDTO.getStopTime());
                  //      pawList = satellitePawMap.get(currentDTO.getSat().getName());

                        logger.debug("23012020 for dto : "+currentDTO);

                        logger.debug("23012020 all paws in overlap with the current dto : "+pawList);
                        if (currentDTO.isRefinable() && sat.checkIfDTOFallsInsideNotDeferreableCheckWithPAW(currentDTO, pawList))
                        {
                        	System.out.println("###############################################  from check for paw  ");

                            // not refinable
                            currentDTO.setRefinable(false);
                            this.tracer.information(EventType.LOG_EVENT, ProbableCause.INFORMATION_INFO, "During Refinement the DTO: " + currentDTOId + " of AR " + currentARId + " of PR " + currentPRId + " has been set not refineable beacause of not derefferable PAW");
                        } // end if

                    } // end for
                } // end for

            } // end for

        } // end try
        catch (Exception e)
        {
            // do nothing
            // just log
            this.tracer.critical(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, " Error in retrieving PAW during refinement : " + e.getMessage() + " the check on paw shall be disabled");
        } // end catch
    }// end method

    /**
     * Perform a check on passtrough on the PRs
     *
     * @throws Exception
     */
    /*
     * private void checkForPassThrough() throws Exception {
     *
     * String currentPRId;
     *
     *
     * Element currentPRElement;
     *
     * //Cycle on AR for(Map.Entry<String, Map<String,Map<String,DTO>>> armap:
     * prArMap.entrySet()) { currentPRId=armap.getKey(); //getting element
     * currentPRElement = this.prPassThoughMap.get(currentPRId);
     *
     * if(currentPRElement!=null) { //performing check
     * performPassThroughCheckOnPR(currentPRElement, armap, currentPRId); }//end
     * if
     *
     *
     * }//end for }//end checkForPassThrough
     */
    /**
     * perform a check on the DTO of a specific PR
     *
     * @param prElement
     * @param armap
     * @param prId
     * @throws Exception
     */
    /*
     * private void performPassThroughCheckOnPR(Element prElement,
     * Map.Entry<String, Map<String,Map<String,DTO>>> armap, String prId )
     * throws Exception { //this method will be //commented because //check on
     * pass through //need a change on aprc interface //in order to get DWL
     * timing information String currentARId; String currentDTOId; DTO
     * currentDTO;
     *
     * List<String> asIdList = new ArrayList<String>(); double initialEpoch=0;
     * double finalEpoch=0;
     *
     * NodeList nl= prElement.getElementsByTagNameNS(FeasibilityConstants.
     * ProgrammingParametersTagNameNS,FeasibilityConstants.
     * ProgrammingParametersTagName);
     *
     * Element programmingParametersNode = (Element) nl.item(0);
     *
     * String prStartTime =
     * XMLUtils.getChildElementText(programmingParametersNode,
     * FeasibilityConstants.PrValidityStartTimeTagName,FeasibilityConstants.
     * PrValidityStartTimeTagNameNS); String prStopTime =
     * XMLUtils.getChildElementText(programmingParametersNode,
     * FeasibilityConstants.PrValidityStopTimeTagName,FeasibilityConstants.
     * PrValidityStopTimeTagNameNS);
     *
     * initialEpoch = DateUtils.fromISOToCSKDate(prStartTime); finalEpoch =
     * DateUtils.fromISOToCSKDate(prStopTime); asIdList =
     * XMLUtils.getChildElementListText(programmingParametersNode,
     * FeasibilityConstants.AcquisitionStationTagName,FeasibilityConstants.
     * AcquisitionStationTagNameNS);
     *
     *
     *
     * SatellitePassBO bo = new SatellitePassBO(); //pass plan Map<String,
     * ArrayList<SatellitePassBean>> satellitePassMap =
     * bo.selectSatellitePass(asIdList, initialEpoch, finalEpoch); Satellite
     * sat; //Cicle on AR for(Map.Entry<String, Map<String,DTO>>
     * dtoMap:armap.getValue().entrySet()) { currentARId=dtoMap.getKey();
     * //looping for(Map.Entry<String, DTO>
     * dtoEntry:dtoMap.getValue().entrySet()) { currentDTO =
     * dtoEntry.getValue(); currentDTOId = currentDTO.getId();
     * sat=currentDTO.getSat();
     * sat.setSatellitePassList(satellitePassMap.get(sat.getName()));
     * if(currentDTO.isRefinable() &&
     * !sat.checkIfDTOFallsInsideSatellitePass(currentDTO)) { //not refinable
     * currentDTO.setRefinable(false); //log
     * this.tracer.information(EventType.LOG_EVENT,
     * ProbableCause.INFORMATION_INFO, "During Refinement the DTO: " +
     * currentDTOId + " of AR "+ currentARId + " of PR " + prId +
     * " has been set not refineable beacause not satisfy Passtrough constraint anymore"
     * );
     *
     * }//end if
     *
     * }//end for }//end for
     *
     * } //end performPassThroughCheckOnPR
     *
     */

    /**
     * Evaluate the new parameters for DTO no SPARC are involved at this stage
     * It evaluate the new start and stop time (start and square in case of
     * spotlight)
     *
     * @param dto
     * @throws FeasibilityException
     */
    protected void evaluateNewDTOparameters(DTO dto) throws FeasibilityException
    {
        this.tracer.debug("Evaluating DTO parameters");
        String satName = dto.getSatName();
        Satellite s = this.satelliteMap.get(satName);
        dto.setSat(s);
        if (s == null)
        {
            // do nothing
            // just log
            throw new FeasibilityException("Requested refinement for DTO with unknown satellite " + satName);
        }
        // evaluating corner
        double[] startLLH = dto.getFirstCorner();
        startLLH[2] = this.dem.getElevation(startLLH[0], startLLH[1]);
        GridPoint p1 = new GridPoint(1, startLLH);
        // evaluating corner
        double[] stopLLH = dto.getFourtCorner();
        stopLLH[2] = this.dem.getElevation(stopLLH[0], stopLLH[1]);

        GridPoint p2 = new GridPoint(2, stopLLH);
        // Retrieving epochs
        double startingEpochInterval = dto.getStartTime() - FeasibilityConstants.RefinementHalfInterval;
        double endingEpochInterval = dto.getStopTime() + FeasibilityConstants.RefinementHalfInterval;
        ArrayList<EpochBean> epochList = EphemeridInMemoryDB.getInstance().selectEpochs(satName, startingEpochInterval, endingEpochInterval);
        
        
		logger.debug("set epoch FROM evaluateNewDTOparameters (STRIP) ");
		logger.debug("dtoEndtime "+DateUtils.fromCSKDateToDateTime(dto.getStopTime()));

		logger.debug("set endTimeLine as dtoEndtime ("+dto.getStopTime()+"+ 1200 sec : "+FeasibilityConstants.RefinementHalfInterval);
		logger.debug("endTimeLine as date ("+DateUtils.fromCSKDateToDateTime(endingEpochInterval));
		s.setEpochs(epochList);
        // clearing access list
        s.getAccessList().clear();
        AccessEvaluatorForRefinement evaluator = new AccessEvaluatorForRefinement();

        List<GridPoint> gridPointList = new ArrayList<>();

        ArrayList<BeamBean> beams = new ArrayList<>();

        // Platform activity window list
        ArrayList<PlatformActivityWindowBean> pawList = new ArrayList<>();

        // list of satellite pass
        ArrayList<SatellitePassBean> satellitePassList = new ArrayList<>();

        s.setBeams(beams);
        s.setPawList(pawList);
        s.setSatellitePassList(satellitePassList);
        // evaluating access on points
        gridPointList.add(p1);
        gridPointList.add(p2);
        evaluator.evaluateSatelliteAccesses(s, gridPointList, null);

        // //System.out.println("=====================FOUND: " +
        // s.getAccessList().size()+ " acesses") ;

        // evaluate timing info
        evaluateNewTimes(dto, s, p1, p2);

    }// end method

    /**
     * Evaluate the new time for DTO and relevant velocity and pocition of the
     * satellite and new orbit id and track number
     *
     * @param dto
     * @param sat
     * @param startingPoint
     * @param endingpoint
     */
    protected void evaluateNewTimes(DTO dto, Satellite sat, GridPoint startingPoint, GridPoint endingpoint)
    {

        double oldDuration = dto.getStopTime() - dto.getStartTime();
        // //System.out.println("====================old duration: " +
        // DateUtils.fromCSKDurationToMilliSeconds(oldDuration)+"ms");

        double startTime = 0;
        double stopTime = 0;

        long orbitId = 0;
        long trackNumber = 0;
        // looping on accesses
        for (Access a : sat.getAccessList())
        {
            if (a.getGridPoint().equals(startingPoint))
            {
                // getting stat time
                startTime = a.getAccessTime();
                // The new orbitId
                orbitId = a.getOrbitId();
                // the new track number
                trackNumber = sat.getTrackNumber(orbitId);

            } // end if
            else
            {
                stopTime = a.getAccessTime();
            } // end else
        } // end for

        if ((startTime != 0) && (stopTime != 0) && ((stopTime - startTime) > 0))
        {
        	System.out.println("###############################################  evaluate dto times  ");

            // the dto could be refined
            dto.setRefinable(true);
            // Set the new trackNumner
            dto.setTrackNumber(trackNumber);
            // Set the new orbit id
            dto.setOrbitId(orbitId);

            if (dto instanceof SpotLightDTO)
            {
                SpotLightDTO sDto = (SpotLightDTO) dto;
                sDto.setSquareStart(startTime);
                sDto.setSquareStop(stopTime);

                double squareDuration = stopTime - startTime;
                double residualDuration = oldDuration - squareDuration;
                double halfResidulDuration = FeasibilityConstants.half * residualDuration;

                sDto.setStartTime(startTime - halfResidulDuration);
                sDto.setStopTime(stopTime + halfResidulDuration);

                // TODO Gestire eccezzionw
                try
                {
                    logger.debug("ACCESS FROM evaluateNewTimes (access.accessTime) "+DateUtils.fromCSKDateToDateTime(startTime));

                    EpochBean startEpoch = sat.getEpochAt(startTime);
                    EpochBean stopEpoch = sat.getEpochAt(stopTime);

                    sDto.setSatellitePositionAtSquareStart(startEpoch.getoXyz());
                    sDto.setSatelliteVelocityAtSquareStart(startEpoch.getoVxVyVz());

                    sDto.setSatellitePositionAtSquareStop(stopEpoch.getoXyz());
                    sDto.setSatelliteVelocityAtSquareStop(stopEpoch.getoVxVyVz());

                }
                catch (FeasibilityException e)
                {
                    sDto.setRefinable(false);
                    // do nothing
                    // log
                    this.tracer.critical(EventType.SOFTWARE_EVENT, ProbableCause.UNEXPECTED_INFORMATION, e.getMessage());
                } // end catch

            } // end if (dto instanceof SpotLightDTO)
            else
            {
                // STRIPMAP DTO
                dto.setStartTime(startTime);
                dto.setStopTime(stopTime);

                // evaluating PVT
                try
                {
                    EpochBean startEpoch = sat.getEpochAt(startTime);
                    EpochBean stopEpoch = sat.getEpochAt(stopTime);

                    dto.setSatPosAtStart(startEpoch.getoXyz());
                    dto.setSatVelAtStart(startEpoch.getoVxVyVz());

                    dto.setSatPosAtEnd(stopEpoch.getoXyz());
                    dto.setSatVelAtSEnd(stopEpoch.getoVxVyVz());

                } // end try
                catch (FeasibilityException e)
                {
                    // not refinable
                    dto.setRefinable(false);
                    // log
                    this.tracer.critical(EventType.SOFTWARE_EVENT, ProbableCause.UNEXPECTED_INFORMATION, e.getMessage());
                } // end catch

            } // end else
        } // end if (startTime!=0 && stopTime!=0 && (stopTime-startTime)>0)
        else
        {
            // not refinable
            dto.setRefinable(false);
        } // end else

    }// end method

    /**
     * Fill the maps holding information
     *
     * @throws XPathExpressionException
     * @throws GridException
     */
    protected void buildMap() throws XPathExpressionException, GridException
    {
        // Searching for progreq
        NodeList progReqList = this.doc.getElementsByTagNameNS(FeasibilityConstants.ProgReqTagNameNS, FeasibilityConstants.ProgReqTagName);

        Element progReq;
        String progReqId;
        Map<String, Map<String, DTO>> arDTOMap;

        String passTroughFlag;
        // looping on PR
        for (int i = 0; i < progReqList.getLength(); i++)
        {
            progReq = (Element) progReqList.item(i);
            progReqId = XMLUtils.getChildElementText(progReq, FeasibilityConstants.ProgReqIdTagName, FeasibilityConstants.ProgReqIdTagNameNS);
            passTroughFlag = XMLUtils.getChildElementText(progReq, FeasibilityConstants.PassThroughFlagTagName, FeasibilityConstants.PassThroughFlagTagNameNS);
            if (passTroughFlag.equals(FeasibilityConstants.TruePassThoughString))
            {
                // Adding PR to pass throgh map
                this.prPassThoughMap.put(progReqId, progReq);
            } // end if
              // BUILD ARDTO MAP
            arDTOMap = parsingProgReq(progReq);
            // ADD Map To pr map
            this.prArMap.put(progReqId, arDTOMap);
        } // end for
    }// end methods

    /**
     * Perse prog req
     *
     * @param progReq
     * @return a map of ar dto map
     * @throws XPathExpressionException
     * @throws GridException
     */
    protected Map<String, Map<String, DTO>> parsingProgReq(Element progReq) throws XPathExpressionException, GridException
    {
        // String progReqId = XMLUtils.getChildElementText(progReq,
        // FeasibilityConstants.ProgReqIdTagName);
        NodeList arList = progReq.getElementsByTagNameNS(FeasibilityConstants.AcquisitionRequestTagNameNS, FeasibilityConstants.AcquisitionRequestTagName);
        // ar element
        Element ar;
        Map<String, Map<String, DTO>> arDTOMap = new TreeMap<>();

        // id
        String arID;
        // Building a map of dto for each ar
        for (int i = 0; i < arList.getLength(); i++)
        {
            ar = (Element) arList.item(i);
            arID = XMLUtils.getChildElementText(ar, FeasibilityConstants.AcquisitionRequestIDTagName, FeasibilityConstants.AcquisitionRequestIDTagNameNS);
            arDTOMap.put(arID, parseAR(ar));
            // prArMap.put(progReqId, arDTOMap);

        } // end for

        return arDTOMap;

    }// end method

    /**
     * Create a map of Ar vs DTO
     *
     * @param ar
     * @return ar DTO Map
     * @throws XPathExpressionException
     * @throws GridException
     */
    protected Map<String, DTO> parseAR(Element ar) throws XPathExpressionException, GridException
    {
        // map to be returned
        Map<String, DTO> dtoMap = new TreeMap<>();
        String dtoId;
        Element dtoelement;
        String di2savailabiliytconfirmarmationflagString = XMLUtils.getChildElementText(ar, FeasibilityConstants.DI2SAvailabilityConfirmationTagName, FeasibilityConstants.DI2SAvailabilityConfirmationTagNameNS);
        boolean di2sAvailabilityConfirmationFlag = false;
        if (di2savailabiliytconfirmarmationflagString.equals(FeasibilityConstants.DI2SAvailabilityConfirmationTrueValue))
        {
            di2sAvailabilityConfirmationFlag = true;
        } // end if

        NodeList dtoList = ar.getElementsByTagNameNS(FeasibilityConstants.DTOTagNameNS, FeasibilityConstants.DTOTagName);
        // current dto
        DTO currentdto;

        String sensorMode;
        // looping on dto list
        for (int i = 0; i < dtoList.getLength(); i++)
        {
            dtoelement = (Element) dtoList.item(i);
            sensorMode = XMLUtils.getChildElementText(dtoelement, FeasibilityConstants.SensorModeTagName, FeasibilityConstants.SensorModeTagNameNS);
            // spotlight
            if (this.spotlightSensorModeList.contains(sensorMode))
            {
                // spot dto
                currentdto = new SpotLightDTO(dtoelement);
                ((SpotLightDTO) currentdto).setDi2sConfirmationFlag(di2sAvailabilityConfirmationFlag);
            } // end if
            else
            {
                // no spot dto
                currentdto = new DTO(dtoelement);
            } // end else

            // Adding the element AR, the pos list shall be updated
            currentdto.setARElement(ar);
            // getting id
            dtoId = XMLUtils.getChildElementText(dtoelement, FeasibilityConstants.DTOIdTagName, FeasibilityConstants.DTOIdTagNameNS);
            // adding dto to map
            // //System.out.println("-------ADDING DTO: " + dtoId + " to map");
            dtoMap.put(dtoId, currentdto);

        } // end for
          // returning
        return dtoMap;
    }// end method

    /**
     * Evaluate the response name according to the naming convention
     *
     * @param requestPath
     * @return response path
     */
    protected String evaluateResponseName(final String requestPath)

    {
        String response = "";

        File file = new File(requestPath);

        String dirPath = file.getParent();
        // working dir
        this.workingDir = dirPath;

        // logger.debug("input dir: " + dirPath);

        String fileName = file.getName();
        // logger.debug("input fileName: " + fileName);

        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        // evaluating time stamp
        java.time.LocalDateTime dateTime = LocalDateTime.now();
        String timestamp = dateTime.format(fmt);
        // tokenizing
        StringTokenizer tokens = new StringTokenizer(fileName, "_");
        // building response
        response = dirPath + File.separator + tokens.nextToken() + "_" + timestamp + ".ANALYSE_PR_LIST_RES.xml";

        // returning path

        return response;
    }// end response

}// end class
