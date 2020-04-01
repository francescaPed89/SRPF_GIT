/**
*
* MODULE FILE NAME:	FeasibilityExtensor.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Perform the Feasibility Extension
*
* PURPOSE:			Extends the feasyibility of AR
*
* CREATION DATE:	23-01-2017
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathExpressionException;

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
 * This class implements the feasibility extension
 *
 * @author Amedeo Bancone
 * @version 1.0
 *
 */
public class FeasibilityExtensor extends FeasibilityPerformer

{

    /**
     * log
     */
    private TraceManager tracer = new TraceManager();

    /**
     * full ACQ list This list is composed by the stll to be planned ARs and the
     * new ones. It is used to evaluate the full coverage in the optimization
     * process devoted to discatd AR that not increase the full coverage
     */
    private List<AcqReq> fullAcqReqlist = new ArrayList<>();

    /**
     * List of DTO alredy present in the request
     */
    private List<DTO> oldDTOList = new ArrayList<>();

    /**
     * Mark the ar as evaluated for extension
     */
    private static String postfixExtensionPrefix = "R";

    /**
     * True if have perform coverage optimization
     */
    private boolean havePerformCoverageOptimization = false;

    /**
     * The minimum coverage of the AR to be expanded that allow the check on
     * centroid optimization
     */
    private double minCoverageToConsiderCentroid = 0.98;

    /**
     * coverage of the full extension list
     *
     */
    private double unoptimizedCoverage;

    /**
     * Constructor
     *
     * @throws IOException
     */
    public FeasibilityExtensor() throws IOException
    {
        super();
        /**
         * Setting false check on centroid
         */
        this.haveCheckForCentroidIfSpotLightAndArea = false;

        // standard use of area
        this.haveCheckForSingleAcquisitionInBuildingARElement = false;

        /**
         * max area from property
         */
        String value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.HAVE_PERFORM_OPTIMIZATION_ON_EXTENSION_COVERAGE_CONF_HEY);
        if (value != null)
        {
            try
            {
                int dValue = Integer.valueOf(value);
                if (dValue == 1)
                {
                    this.havePerformCoverageOptimization = true;
                }
            }
            catch (Exception e)
            {
                /**
                 * Misconfigured using default
                 */
                // logger.warn("Unable to found " +
                // FeasibilityConstants.MAX_AREA_OF_INTEREST_CONF_KEY + " in
                // conffiguration");
                this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Malformed " + FeasibilityConstants.HAVE_PERFORM_OPTIMIZATION_ON_EXTENSION_COVERAGE_CONF_HEY + " in configuration");

            }

        } // end if
        else
        {
            /**
             * Not configured using default
             */
            // logger.warn("Unable to found " +
            // FeasibilityConstants.MAX_AREA_OF_INTEREST_CONF_KEY + " in
            // conffiguration");
            this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.HAVE_PERFORM_OPTIMIZATION_ON_EXTENSION_COVERAGE_CONF_HEY + " in configuration");

        } // end else

    }// end method

    /**
     * Perform Extensionn on the prListPath file
     *
     * @param prListPath
     * @return the path of response file
     * @throws Exception
     */
    public String performExtension(String prListPath) throws Exception
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db;
        db = dbf.newDocumentBuilder();
        this.doc = db.parse(prListPath);

        this.tracer.debug("Loaded document " + prListPath);
        /**
         * Validating schema
         *
         *
         */
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new File(this.xsdPath));
        schema.newValidator().validate(new DOMSource(this.doc));
        this.tracer.debug("Parsed valid document");

        /**
         * Performing extension
         */
        return performExtension(prListPath, this.doc);
    }// end method

    /**
     * Perform Extensionn on the prListPath file
     *
     * @param prListPath
     * @param doc
     * @return the path of response file
     * @throws Exception
     */
    public String performExtension(String prListPath, Document doc) throws Exception
    {
        this.tracer.log("Performing extension");
        this.doc = doc;
        /**
         * Creating prefic map
         */
        this.namespaceMap = XMLUtils.createNSPrefixMap(this.doc);

        /**
         * Working dir
         */
        this.prListWorkingDir = new File(prListPath).getParent();

        /**
         * Response path
         */
        String responsePath = this.evaluateResponseName(prListPath);
        // extensionList= getUsefulAR(extensionList);
        Node root = this.doc.getFirstChild();

        /**
         * Builing request
         */
        PRRequestParameter request = new PRRequestParameter(doc);
        double startValidityTime = DateUtils.fromISOToCSKDate(request.getStartTime());
        double stopValidityTime = DateUtils.fromISOToCSKDate(request.getStopTime());

        /**
         * Upper time sohould be ebvaluated in case of optimization of the time
         * line see Feasibililtyperformer
         *
         */
        double upperTimeTorequest = stopValidityTime;

        /**
         * We 'll not search for hole due quantization error
         */
        this.haveCheckForHoles = false;

        /**
         * Set to false periodic and repetetive flag
         */

        request.setPeriodic(false);
        request.setRepetitive(false);

        /**
         * Building the satellite list with all the needs
         */
        fillsatelliteList(request, startValidityTime, upperTimeTorequest, stopValidityTime);

        /**
         * Evaluating if spotlight
         */
        BeamBean beam = this.satList.get(0).getBeams().get(0);
        boolean isSpotLight = beam.isSpotLight();
        this.gridSpacing = this.stripMapGridSpacing;
        if (isSpotLight)
        {
            // logger.info("-------------------------SPOT-LIGHT-----------------------");
            this.gridSpacing = this.spotlightGridSpacing;
        }
        /**
         * Setting grid per sensor mode
         */
        getGridSpcingPerSensorMode(beam.getSensorModeName());

        this.tracer.debug("grid spacing: " + this.gridSpacing);

        /**
         * Renaming root node
         */
        this.doc.renameNode(root, root.getNamespaceURI(), FeasibilityConstants.FeasibilityAnalysisResponseTagName);
        List<AcqReq> arToBeExtended = findArToBeExtended();

        if (arToBeExtended.size() == 0)
        {
            /**
             * No ar to extends found
             */
            fillPRStatusWithError("Extension request with no toBeExtended flagged ARs");

        }
        else
        {
            /**
             * Any case
             */
            checkForAnyOrbitCase(arToBeExtended, request);

            List<AcqReq> extensionList = new ArrayList<>();

            /// Questo blocco va modificato perchè pensiamo di estendere tutto
            /// insieme
            /*
             * for(Element e: arToBeExtended) { List<AcqReq> extList =
             * perforAcquisitionRequestExtension(e,request,stopValidityTime,
             * isSpotLight); //TODO performAcqReqReindexing(extList); //TODO
             * for(AcqReq a : extList) {
             *
             * extensionList.add(a); addARDTOtoOld(a); } }
             */

            // AllinOne faccio l'esetnsione tutto insieme
            /**
             * Performing operation
             */
            AlgoRetVal retval = performAcquisitionRequestExtension(arToBeExtended, request, stopValidityTime, isSpotLight);

            OptimizationAlgoInterface algo = retval.getAlgo();

            /**
             * Retrieving AR list
             */
            extensionList = retval.getAcquisitionRequestList();

            List<AcqReq> originalExtensionList = new ArrayList<>();
            for (AcqReq a : extensionList)
            {
                originalExtensionList.add(a);
            }

            /**
             * . No ar found
             */
            if (originalExtensionList.size() == 0)
            {
                fillPRStatusWithError("Unable to extends the requested ARs");
            }
            else
            {
                /**
                 * Retrieve coverage
                 */
                this.unoptimizedCoverage = this.gridder.getCoverage(extensionList);

                /**
                 * For interferometric and stereo the optimization is disallowed
                 * optimization make sense if we have more than one AR the
                 * optimization is optional
                 */
                if (!request.isInterferometric() && !request.isStereo() && (extensionList.size() > 1) && this.havePerformCoverageOptimization)
                {
                    /**
                     * Using only AR that increment coverage
                     */
                    extensionList = getUsefulAR(extensionList);
                    if (extensionList.isEmpty())
                    {
                        /**
                         * In this case we return original List
                         */
                        extensionList = originalExtensionList;
                    }

                } // end if

                /**
                 * Renaming AR id
                 */
                String prefix = arToBeExtended.get(0).getId() + FeasibilityExtensor.postfixExtensionPrefix;
                performAcqReqReindexing(extensionList, prefix);

                /**
                 * Adding response to XML
                 */
                addResponseToXML(extensionList, request, algo.isSingleAcquired());
                this.tracer.debug("Dumping feasibility response in file: " + responsePath);

            } // end else originalExtensionList.size()==0
              // in case of stereo and interferometric we dont perform
              // optimization

        } // end external else arToBeExtended.size()==0

        /**
         * Dumping response
         */
        XMLUtils.dumpResponseToFile(this.doc, responsePath);
        return responsePath;
    }// end method

    /**
     * Return only AR increasing coverage
     *
     * @param arList
     * @return useful ar
     */
    private List<AcqReq> getUsefulAR(List<AcqReq> arList)
    {
        // //System.out.println("====================================Using
        // useful");

        /**
         * the optimization process devoted to discatd AR that not increase the
         * full coverage
         */
        this.tracer.log("Performing extension coverage optimization");

        List<AcqReq> usefulAcqReq = new ArrayList<>();
        try
        {
            double coverage = 0;
            if (this.fullAcqReqlist.size() != 0)
            {
                /**
                 * Coverage
                 */
                coverage = this.gridder.getCoverage(this.fullAcqReqlist);
            }
            double currentCoverage = coverage;
            /**
             * For each AR
             */
            for (AcqReq ar : arList)
            {
                ar.setId(Integer.toString(this.fullAcqReqlist.size() + 1));
                this.fullAcqReqlist.add(ar);
                currentCoverage = this.gridder.getCoverage(this.fullAcqReqlist);
                if (currentCoverage > coverage)
                {
                    /**
                     * to useful AR
                     */
                    coverage = currentCoverage;
                    usefulAcqReq.add(ar);
                }
                else
                {
                    /**
                     * Removing ar
                     */
                    this.fullAcqReqlist.remove(ar);
                }
            } // end for
        } // end catch
        catch (GridException e)
        {
            e.printStackTrace();
            usefulAcqReq = arList;
        }
        finally
        {
            for (int i = 0; i < usefulAcqReq.size(); i++)
            {
                /**
                 * Reindexing AR
                 */
                usefulAcqReq.get(i).setId(Integer.toString(i + 1));
            } // for
        } // end finally

        return usefulAcqReq;
    }// end method

    /**
     * Return the coverage to be used in response
     *
     * @param acqList
     * @return
     * @throws GridException
     */
    @Override
    protected double getUsedCoverage(List<AcqReq> acqList) throws GridException
    {
        /**
         * Evaluate coverage
         */
        double coverage = this.gridder.getCoverage(acqList) * FeasibilityConstants.hundred;
        /*
         * this.tracer.debug("==================Original coverage" +
         * this.unoptimizedCoverage);
         * this.tracer.debug("================== coverage" + coverage);
         */

        if (this.unoptimizedCoverage > coverage)
        {
            coverage = this.unoptimizedCoverage;
        }
        /**
         *
         */
        return coverage;
    }// end getUsedCoverage

    /**
     * Evaluate the right id for each AcqReq in ARLIST
     *
     * @param acqList
     */
    private void performAcqReqReindexing(List<AcqReq> acqList, String prefix)
    {
        String currentId;
        /**
         * reindesx AR ID
         */
        for (AcqReq a : acqList)
        {
            currentId = prefix + a.getId();
            a.setId(currentId);
        }
    }// END METHOD

    /**
     * Add the DTO belonging the ar to the old dto list
     *
     * @param ar
     *
     *            private void addARDTOtoOld(AcqReq ar) { for(DTO d :
     *            ar.getDTOList()) { this.oldDTOList.add(d); } }
     */

    /**
     * Evaluate access
     *
     * @param request
     */
    private void evaluateAccess(PRRequestParameter request)
    {
        // Calcolo accessi

        this.tracer.debug("Evaluating accesses");

        /**
         * Clearing accesses
         */
        for (Satellite s : this.satList)
        {
            s.getAccessList().clear();
        }

        /**
         * Evaluating new accesses
         */
        AccessesEvaluator eval = new AccessesEvaluator();
        eval.evaluateSatelliteAccesses(this.satList, this.gridPointList, request);
        this.tracer.debug("Accesses evaluated");

        int numberOfAccess = 0;
        /**
         * Evaluating accesses number for log
         */
        for (Satellite sat : this.satList)
        {
            numberOfAccess = numberOfAccess + sat.getAccessList().size();
        }
        this.tracer.debug("Found " + numberOfAccess + " accesses");

    }// end method

    /**
     * Remove accesses falling in old DTO
     */
    private void filterAccessAgainstOldDTO()
    {
        List<Access> currentAccessList;

        /**
         * for each satellite
         */
        for (Satellite s : this.satList)
        {
            currentAccessList = new ArrayList<>();

            for (Access a : s.getAccessList())
            {
                /**
                 * Check for acces if is valid
                 */
                if (checkIfAccessIsValid(a))
                {
                    currentAccessList.add(a);
                    a.setId(currentAccessList.size());
                }
            }
            s.setAccessList(currentAccessList);
        } // end for

    }// end method

    /**
     * Check access time against the oldDTO only for spotlight
     *
     * @param a
     * @return
     */
    boolean checkIfAccessIsValid(Access a)
    {
        boolean isValid = true;
        Satellite sat = a.getSatellite();
        double threshold = sat.getTreshold();
        double epoch = a.getAccessTime();
        /**
         * For each dto
         */
        for (DTO dto : this.oldDTOList)
        {
            double lowerLimit = dto.getStartTime() - threshold;
            double upperLimit = dto.getStopTime() + threshold;
            if (sat.getName().equals(dto.getSatName()) && ((epoch > lowerLimit) && (epoch < upperLimit)))
            {
                /**
                 * Access inside a old DTO
                 */
                isValid = false;
                /**
                 * Have exit
                 */
                break;

            } // end if
        } // end for
        return isValid;
    }// end method

    /**
     * Fill the grid for extension
     *
     * @param ARList
     * @param request
     * @throws XPathExpressionException
     * @throws GridException
     * @throws FeasibilityException
     */
    /*
     * private void fillGrid(List<Element> ARList,PRRequestParameter request)
     * throws XPathExpressionException, GridException, FeasibilityException {
     * this.gridder = buildGrid(request);
     *
     *
     *
     * List<GridPoint> currentGridPointList; String posList; double[][] corners;
     *
     * Gridder currenrGridder;
     *
     * this.gridPointList.clear();
     *
     * for(Element ar :ARList ) { currentGridPointList =new
     * ArrayList<GridPoint>(); corners =getARCorners(ar); posList =
     * ((PolygonGridder)this.gridder).getIntersectionPosList(corners);
     * PRRequestParameter currentRequst = (PRRequestParameter)request.clone();
     *
     * currentRequst.setPosList(posList);
     * currenrGridder=buildGrid(currentRequst);
     * currenrGridder.fillGrid(currentGridPointList);
     *
     * for(GridPoint p :currentGridPointList ) {
     * p.setId(this.gridPointList.size()+1); this.gridPointList.add(p); }
     *
     *
     * }//end for
     *
     *
     * }
     */

    /**
     * Fill the grid using only point falling inside AR to be extended
     *
     * @param ARList
     * @param request
     * @throws XPathExpressionException
     * @throws GridException
     * @throws FeasibilityException
     */
    private void fillGrid(List<AcqReq> ARList, PRRequestParameter request) throws XPathExpressionException, GridException, FeasibilityException
    {
        /**
         * Build gridder
         */
        this.gridder = buildGrid(request);

        /**
         * Fill the grid
         */
        this.gridder.fillGrid(this.gridPointList);

        
        double toBeExapandedArCoverage = 1;

        /**
         * Not puntual request
         */
        if ((request.getRequestProgrammingAreaType() != PRRequestParameter.pointRequestType) && (request.getRequestProgrammingAreaType() != PRRequestParameter.pointRequestWithDuration))
        {

            /**
             * Evaluate the coverage of the Ar to be expanded
             */
            toBeExapandedArCoverage = this.gridder.getCoverage(ARList);
            /**
             * We can consider that the request could be single acquired
             */
            if (toBeExapandedArCoverage > this.minCoverageToConsiderCentroid)
            {
                this.haveCheckForCentroidIfSpotLightAndArea = true;
            }

            /**
             * List on wich performing feasibility
             */
            List<GridPoint> residualGrid = new ArrayList<>();

            List<GridPoint> currentGridPointList;
            // String posList;
            double[][] corners;

            // Gridder currenrGridder;

            /**
             * For each AR
             */
            for (AcqReq ar : ARList)
            {
                // currentGridPointList =new ArrayList<GridPoint>();
                corners = ar.getDTOList().get(0).getCorners();

                /**
                 * retrieve gridpoint inside AR
                 */
                currentGridPointList = this.gridder.getGridpointInsdeAR(this.gridPointList, corners);

                int currrentId = 0;

                for (GridPoint p : currentGridPointList)
                {
                    /**
                     * remove grid point from original grid
                     */

                    this.gridPointList.remove(p);

                    currrentId = residualGrid.size() + 1;
                    p.setId(currrentId);
                    /**
                     * Add gridpoint to the residual grid
                     */
                    residualGrid.add(p);

                    logger.debug("ADDING POINT with id : "+p.getId()+" , coordinates : "+p.getLLH());
                }

            } // end for

            /*
             * this.haveCheckForSingleAcquisitionInBuildingARElement=false;
             * if(toBeExapandedArCoverage>=1) {
             * this.haveCheckForSingleAcquisitionInBuildingARElement=true;
             * }//end if
             */

            /**
             * Setting grid point list to residual
             */
            this.gridPointList = residualGrid;

        } // end if
    }// end method

    /**
     * Esegue l'estensione delle AR tutte insieme
     *
     * @param ARList
     * @param request
     * @param stopValidityTime
     * @param isSpotLight
     * @return AlgoRetVAl
     * @throws GridException
     * @throws XPathExpressionException
     * @throws FeasibilityException
     * @throws IOException
     */
    AlgoRetVal performAcquisitionRequestExtension(List<AcqReq> ARList, PRRequestParameter request, double stopValidityTime, boolean isSpotLight) throws XPathExpressionException, GridException, FeasibilityException, IOException
    {
        // OptimizationAlgoInterface algo=null;

        /**
         * Filling grid
         */
        fillGrid(ARList, request);

        /**
         *
         * inserting centoid
         */
        if (isSpotLight && this.haveCheckForCentroidIfSpotLightAndArea && ((request.getRequestProgrammingAreaType() == PRRequestParameter.CircleRequestType) || (request.getRequestProgrammingAreaType() == PRRequestParameter.PolygonRequestType)))
        {
            this.tracer.debug("Inserting centroid on grid");
            insertCenterInGridPointList();

        }

        /**
         * Building multi geometry
         */
        this.gridder.buildMultiGeometryForExtension(ARList);

        /**
         * Evaluating accesses
         */
        evaluateAccess(request);

        /**
         * In case of spotlight we have to eliminate all the satellite access
         * falling inside a time window overlapping the old satellite dto
         */
        if (isSpotLight)
        {
            /**
             * Filtering access
             */
            filterAccessAgainstOldDTO();
        }
        else
        {
            /**
             * In case of stripmap request for each satellite we have to
             * eliminate all the strips overallping the old dto
             */
            for (Satellite s : this.satList)
            {
                s.setOldDTOList(this.oldDTOList);
            }
        }

        AlgoRetVal retVal = optimalAcqReqList(request, stopValidityTime, isSpotLight, false);

        return retVal;

    }// end method

    /**
     * Check if the request have any as orbit selection. In this case will force
     * to use the type of orbit chosen during the feasibility phase
     *
     * @param request
     * @throws XPathExpressionException
     */
    private void checkForAnyOrbitCase(List<AcqReq> arToBeExtended, PRRequestParameter request)
    {
        if (request.getRequestedOrbitDirection() == FeasibilityConstants.AnyOrbitDirection)
        {
            for (AcqReq ar : arToBeExtended)
            {
                // int orbitDir=ar.getDTOList().get(0).getOrditDirection();
                /**
                 * Retieve orbit direction
                 */
                request.setRequestedOrbitDirection(ar.getDTOList().get(0).getOrditDirection());

            } // end for
        } // end if

    }// end method

    /**
     * Extend a single Acquisition Request
     *
     * @param AR
     * @return
     * @throws GridException
     * @throws XPathExpressionException
     * @throws FeasibilityException
     * @throws IOException
     *
     *             private List<AcqReq>
     *             perforAcquisitionRequestExtension(Element
     *             AR,PRRequestParameter request,double stopValidityTime,
     *             boolean isSpotLight) throws XPathExpressionException,
     *             GridException, FeasibilityException, IOException {
     *
     *             AcqReq ar = new AcqReq(AR);
     *
     *
     *
     *
     *             request.setPosList(ar.getPosList()); this.gridder =
     *             buildGrid(request);
     *             this.gridder.fillGrid(this.gridPointList);
     *
     *
     *
     *             evaluateAccess(request);
     *
     *             //In case of spotlight we have to eliminate all the satellite
     *             access falling inside a time window overlapping the old
     *             satellite dto if(isSpotLight) { filterAccessAgainstOldDTO();
     *             } else { //In case of stripmap request for each satellite we
     *             have to eliminate all the strips overallping the old dto
     *             for(Satellite s : this.satList) {
     *             s.setOldDTOList(this.oldDTOList); } }
     *
     *             AlgoRetVal retVal =
     *             optimalAcqReqList(request,stopValidityTime,isSpotLight,false);
     *             OptimizationAlgoInterface algo = retVal.getAlgo();
     *
     *             List<AcqReq> optimalAcqList =
     *             retVal.getAcquisitionRequestList();
     *
     *             return optimalAcqList; }//end method
     */

    /**
     * Retrun the dom element list of the AR to be extended
     *
     * @return the dom element list of the AR to be extended
     * @throws XPathExpressionException
     * @throws FeasibilityException
     * @throws GridException
     */
    private List<AcqReq> findArToBeExtended() throws XPathExpressionException, FeasibilityException, GridException
    {
        /**
         * AR to be extended list
         */
        ArrayList<AcqReq> toBeExtendedArList = new ArrayList<>();

        this.tracer.debug("Searching for AR to be extended");

        /*
         * NodeList arIdList =
         * this.doc.getElementsByTagNameNS(FeasibilityConstants.
         * AcquisitionRequestIDTagNameNS,FeasibilityConstants.
         * AcquisitionRequestIDTagName); int maxId=0; for(int i =0; i<
         * arIdList.getLength();i++) { Element el = (Element)arIdList.item(i);
         * int currentId =
         * Integer.parseInt(el.getFirstChild().getTextContent());
         * ////System.out.println("================ARID: " + currentId);
         * if(currentId>maxId) maxId=currentId; }//end for
         */

        NodeList arList = this.doc.getElementsByTagNameNS(FeasibilityConstants.AcquisitionRequestTagNameNS, FeasibilityConstants.AcquisitionRequestTagName);
        if (arList.getLength() != 0)
        {
            Node parentElement = arList.item(0).getParentNode();
            List<Element> toBeRemoved = new ArrayList<>();

            AcqReq currentAcq;
            Element curreElement;
            for (int i = 0; i < arList.getLength(); i++)
            {
                /**
                 * Cicling on element
                 */
                curreElement = (Element) arList.item(i);
                currentAcq = new AcqReq(curreElement);
                if (currentAcq.getTimeExtesionInfo().equals(FeasibilityConstants.toBeExtendedValue))
                {
                    /**
                     * Adding AR
                     */
                    toBeExtendedArList.add(currentAcq);

                }
                else if (currentAcq.getTimeExtesionInfo().equals(FeasibilityConstants.stillToBePlannedValue))
                {
                    /**
                     * The AR is still to be planned So we add it to the
                     * fullAcqReqlist to be used in coverage optimization
                     */
                    addDTOTOoldList(currentAcq);
                    this.fullAcqReqlist.add(currentAcq);
                }
                toBeRemoved.add(curreElement);
            } // end node

            for (Element e : toBeRemoved)
            {
                // //System.out.println("=======================Removing
                // element");

                /**
                 * Removing AR from DOM
                 */
                parentElement.removeChild(e);
                parentElement.normalize();
            } // enf for

            removeUnesedField();
        } // end for

        return toBeExtendedArList;
    }// end method

    /**
     * Add the DTO belonging the AR to the list of old DTO
     *
     * @param AR
     * @throws XPathExpressionException
     */
    private void addDTOTOoldList(AcqReq AR) throws XPathExpressionException
    {

        for (DTO d : AR.getDTOList())
        {
            /**
             * Adding to old DTOs
             */
            this.oldDTOList.add(d);
        }
    }// end method

    /**
     * Remove PR_FA PRSTatus and PRCoverage tags
     */
    private void removeUnesedField()
    {
        NodeList list = this.doc.getElementsByTagNameNS(FeasibilityConstants.PR_FA_TagNameNS, FeasibilityConstants.PR_FA_TagName);
        Node currentNode;
        if (list.getLength() != 0)
        {
            currentNode = list.item(0);
            /**
             * Remove node
             */
            removeNode(currentNode);
        }

        list = this.doc.getElementsByTagNameNS(FeasibilityConstants.PRStatusTagNameNS, FeasibilityConstants.PRStatusTagName);
        if (list.getLength() != 0)
        {
            currentNode = list.item(0);
            /**
             * Remove node
             */
            removeNode(currentNode);
        }

        list = this.doc.getElementsByTagNameNS(FeasibilityConstants.PRCoverageTagNameNS, FeasibilityConstants.PRCoverageTagName);
        if (list.getLength() != 0)
        {
            currentNode = list.item(0);
            /**
             * Remove node
             */
            removeNode(currentNode);
        }

    }// end method

    /**
     * Remove the node from dom
     *
     * @param node
     */
    private void removeNode(Node node)
    {
        /**
         * Removing chold
         */
        node.getParentNode().removeChild(node);
        this.doc.normalize();
    }// end method

}// end class
