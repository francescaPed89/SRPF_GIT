/**
*
* MODULE FILE NAME:	AcqReq.java
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
* DESIGN ISSUE:		2.0
*
* INTERFACES:
*
* SUBORDINATES:
*
* MODIFICATION HISTORY:
*
*
* --------------------------+------------+----------------+-------------------------------
* 16-05-2016 | Amedeo Bancone  |1.1| modify dto expansion to take into account the case of single acquisition duplication. Added removeDuplicatedDTO method
* --------------------------+------------+----------------+-------------------------------
* 15-02-2018 | Amedeo Bancone  |2.0| Modified for take into account:
* 									 Extension,
* 									 refinement
* 									 Stereo request
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.feasibility;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.xpath.XPathExpressionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.DateUtils;
import com.telespazio.csg.srpf.utils.XMLUtils;

/**
 * Class for AR
 *
 * @author Amedeo Bancone
 * @version 2.0
 *
 *
 */
public class AcqReq implements Comparable<AcqReq>, Cloneable

{
    static final Logger logger = LogManager.getLogger(AcqReq.class.getName());

    /**
     * static final Logger logger =
     * LogManager.getLogger(AcqReq.class.getName());
     */
    private TraceManager tracer = new TraceManager();
    // private int id;
    /**
     * ID
     */
    private String id;

    /**
     * Mission
     */
    // private String mission;

    private Set<String> missionSet = new TreeSet<>();

    /**
     * DTO LIST
     */
    private ArrayList<DTO> dtoList = new ArrayList<>();

    /**
     * Time ext info
     */
    private String timeExtesionInfo = "";

    /**
     * PosList
     */
    private String posList;

    /**
     * Usa facor
     */
    private double suf = 0;

    /**
     * In case of interferometric this is a link the the AR holding the DTO
     * linked to the current AR. The ID of linked DTO shall be the same
     */
    private AcqReq linkedAR = null;

    /**
     * specify if master in a stereo link
     *
     */
    private boolean stereoMaster = false;

    /**
     * specify if the AR has a stereo link
     *
     */
    private boolean stereoLink = false;

    /**
     * return true if no stero link have been found
     *
     * @return true if stereo ha found
     */
    public boolean isStereoLink()
    {
        return this.stereoLink;
    }// end method

    /**
     * set the stereo link
     *
     * @param stereoLink
     */
    public void setStereoLink(boolean stereoLink)
    {
        this.stereoLink = stereoLink;
    }// end method

    /**
     * return true if the ACQ is a stereo master aca
     *
     * @return true if a master in stereo pair
     */
    public boolean isStereoMaster()
    {
        return this.stereoMaster;
    }// end method

    /**
     * set the stereo master flag: if true the Acq is considered master in the
     * stereo pair algo
     *
     * @param stereoMaster
     */
    public void setStereoMaster(boolean stereoMaster)
    {
        this.stereoMaster = stereoMaster;
    }// end method

    /**
     * Clone an AR. NB in case of interferometric the linked AR miust be set
     * externally
     */
    @Override
    public AcqReq clone()
    {
        AcqReq ar = new AcqReq();

        /**
         * Cloning parameters
         */
        ar.id = this.id;

        // ar.mission=this.mission;

        ar.setMission(this.missionSet);

        for (DTO d : this.dtoList)
        {
            /**
             * Cloning DTO
             */
            ar.dtoList.add(d.clone());
        } // Enf for

        ar.timeExtesionInfo = this.timeExtesionInfo;

        ar.posList = this.posList;
        ar.suf = this.suf;

        return ar;
    }// end method

    /**
     * Default constructor
     */
    public AcqReq()
    {

    }// end constructor

    /**
     * Build the AR norm a dom node
     *
     * @param aeEl
     * @throws XPathExpressionException
     * @throws FeasibilityException
     * @throws GridException
     */
    public AcqReq(Element aeEl) throws XPathExpressionException, FeasibilityException, GridException
    {
        /**
         * Retrienving the AR indo
         */
        this.timeExtesionInfo = XMLUtils.getChildElementText(aeEl, FeasibilityConstants.timeExtensionInfoTagName, FeasibilityConstants.timeExtensionInfoTagNameNS);
        String idString = XMLUtils.getChildElementText(aeEl, FeasibilityConstants.AcquisitionRequestIDTagName, FeasibilityConstants.AcquisitionRequestIDTagNameNS);
        this.id = idString;

        /**
         * Retrieving the DTO List element from DOM
         */
        NodeList DTOList = aeEl.getElementsByTagNameNS(FeasibilityConstants.DTOTagNameNS, FeasibilityConstants.DTOTagName);

        this.posList = getARpolygonPosList(aeEl);

        DTO d;
        for (int i = 0; i < DTOList.getLength(); i++)
        {
            /**
             * Building DTO by the elements
             */
            d = new DTO((Element) DTOList.item(i));
            this.dtoList.add(d);
        } // end for

        if (this.dtoList.size() == 0)
        {
            /**
             * If AR as no DTO exception is thrown This should never happens
             */
            throw new FeasibilityException("Found AR with ID: " + idString + " without DTO ");
        }

    }// end constructor

    /**
     * Retrieeve the posList string inside the AcqReq polygon
     *
     * @param AR
     * @return The pos list string
     * @throws FeasibilityException
     * @throws XPathExpressionException
     */
    private String getARpolygonPosList(Element AR) throws FeasibilityException, XPathExpressionException
    {
        String retval = "";
        /**
         * Searching for polygon
         */
        NodeList list = AR.getElementsByTagNameNS(FeasibilityConstants.ExteriorTagNameNS, FeasibilityConstants.ExteriorTagName);
        if (list.getLength() == 0)
        {
            /**
             * If no polygon
             */
            throw new FeasibilityException("Found AcquisitionRequest  without polygon");
        }

        retval = XMLUtils.getChildElementText((Element) list.item(0), FeasibilityConstants.PosListTagName, FeasibilityConstants.PosListTagNameNS);

        if (retval.equals(""))
        {
            /**
             * If no poslist
             */
            throw new FeasibilityException("Found AcquisitionRequest  without polygon");
        }

        return retval;
    } // end method

    /**
     * Return thr poslist as for XML
     *
     * @return pos list string
     */
    public String getPosList()
    {
        return this.posList;
    }// end method

    /**
     * Set the poslist
     *
     * @param posList
     */
    public void setPosList(String posList)
    {
        this.posList = posList;
    }// end method

    /**
     *
     * @return timeExtesionInfo
     */
    public String getTimeExtesionInfo()
    {
        return this.timeExtesionInfo;
    }// end method

    /**
     *
     * @param timeExtesionInfo
     */
    public void setTimeExtesionInfo(String timeExtesionInfo)
    {
        this.timeExtesionInfo = timeExtesionInfo;
    }// end method

    /**
     * return the linked interferometric AR null otherwise
     *
     * @return linked AR
     */
    public AcqReq getLinkedAR()
    {
        return this.linkedAR;
    }// end method

    /**
     * Set the linked interferometric AR
     *
     * @param linkedAR
     */
    public void setLinkedAR(AcqReq linkedAR)
    {
        this.linkedAR = linkedAR;
    }// end method

    /**
     * add a dto to the dto list. It change the id of the DTO to list size+1
     *
     * @param dto
     */
    public void addDTO(DTO dto)
    {

        int dtoId = this.dtoList.size() + 1;
        /**
         * Set DTO id
         */
        dto.setId("" + dtoId);
        this.dtoList.add(dto);

    }// end method

    /**
     * Retrun the suf of the AR acutally the greater suf of the DTO belonging
     * the DTOs list is given
     *
     * @return SUF
     */
    public double getSuf()
    {
        return this.suf;
    }// end method

    /**
     * Set the valute of the AR suf
     *
     * @param suf
     */
    public void setSuf(double suf)
    {
        this.suf = suf;
    }// end method

    /**
     * check if the dto conflict with the AcqReq. If no conflict return false
     *
     * @param dto
     * @param acqRef
     * @return true if conflict
     */
    public boolean dtoConflictWithAR(DTO dto)
    {
        this.tracer.debug("Resolving conflict dtoConflictWithAR");
        /**
         * default
         */
        boolean conflict = false;
        String dtoSatName = dto.getSatName();
        String acqrefSatName = this.dtoList.get(0).getSatName();

        double startTime = dto.getStartTime();
        double stopTime = dto.getStopTime();

        if (dtoSatName.equals(acqrefSatName))
        {
            /**
             * Evaluating threshold
             */
    		logger.debug("from dtoConflictWithAR");

            double threshold = FeasibilityConstants.forTwo * dto.getDtoAccessList().get(0).getSatellite().getStripMapMinimalDuration();

            double upperLimit;
            double lowerLimit;

            for (DTO refDTO : this.dtoList)
            {
                /**
                 * Setting limits
                 */
                lowerLimit = refDTO.getStartTime() - threshold;
                upperLimit = refDTO.getStopTime() + threshold;

                if (((startTime > lowerLimit) && (startTime < upperLimit)) || ((stopTime > lowerLimit) && (stopTime < upperLimit)))
                {
                    /**
                     * Conflict found have exit
                     */
                    conflict = true;
                    this.tracer.debug("found conflicting DTO");
                    break;
                } // end if
            } // end for

            // if(((optimalStripStartTime - currentStripStopTime) >
            // optimalStripSat.getSensorRestoreTime()) ||
            // ((currentStripStartTime - optimalStripStopTime)
            // >optimalStripSat.getSensorRestoreTime() ) )

        } // end if

        return conflict;
    }// end dtoConflictWithAR

    /**
     * For ODREF based DTO this methods find the DTO that will be reproposed on
     * CSK period basis.
     *
     * @param prValisityStop
     * @param isSingleAqcuired
     */
    public void expandDTOList(double prValisityStop, boolean isSingleAqcuired)
    {
        this.tracer.debug("Expanding DTO");
        
        logger.debug("prValisityStop as double : "+prValisityStop);

        logger.debug("prValisityStop : "+ DateUtils.fromCSKDateToDateTime(prValisityStop));
        List<DTO> newDTOList = new ArrayList<>();

        for (DTO d : this.dtoList)
        {

            double tstart = d.getStartTime();
            double tstop = d.getStopTime();
            double tcenter = d.getStartTime() + (tstop - tstart)/2;
//            logger.debug("tstop dto as double: "+ tstop);
//
//            logger.debug("tstop dto : "+ DateUtils.fromCSKDateToDateTime(tstop));

            /**
             * square parameters to be updated in case of spotlight
             */
            double tsquareStart = 0;
            double tsquareStop = 0;

            /**
             * stop time for the next dto
             *
             */
            double nextStopTime;

            /**
             * Access used in the new DTO
             */
            Access a;
            if (d.isOdrefBased())
            {
                /**
                 * If DTO is based on odref it must be expanded
                 */
                double incrementalTime = 0;
                nextStopTime = tstop + (FeasibilityConstants.RepetitionODREFPeriod);
                
                incrementalTime= tstop + (FeasibilityConstants.RepetitionODREFPeriod);
//                logger.debug("adding 16 days to tstop  "+DateUtils.fromCSKDateToDateTime(nextStopTime));
                DTO newDto;
           
                double repetitionTime =  FeasibilityConstants.RepetitionODREFPeriod;
//                logger.debug("repetitionTime : "+repetitionTime);
                /**
                 * Exansion must not overcame PR Vvalidity
                 */
                while (incrementalTime <= prValisityStop)
                {
                	//i++;
                    // logger.info("You must add dto cause next stop is: " +
                    // DateUtils.fromCSKDateToISOFMTDateTime(nextStopTime));



                    if (d instanceof SpotLightDTO)
                    {
                        /**
                         * ID DTO is spot expansion is based in target center
                         * point
                         */
                        
                         SpotLightDTO d1 = (SpotLightDTO) d;

                        a = new Access(d1.getCenteredAccess());
                        
                        a.setAccessTime(incrementalTime);
                        
//                        logger.debug("d1.getCenteredAccess().getAccessTime(): "+DateUtils.fromCSKDateToDateTime(d1.getCenteredAccess().getAccessTime()));
//
//                        logger.debug("ACCESSO SET ACCESS TIME : "+DateUtils.fromCSKDateToDateTime(a.getAccessTime()));
//                        
//                        logger.debug("ACCESSO SET SPOTLIGHT BEFORE REPETITION: "+DateUtils.fromCSKDateToDateTime(d1.getSquareStart()));//valore base

                        
                        tsquareStart = d1.getSquareStart() + repetitionTime;
                        tsquareStop = d1.getSquareStop() + repetitionTime;
                        d1.setSquareStart(tsquareStart);
                        d1.setSquareStop(tsquareStop);
                        d1.setCenteredAccess(a);
                        d1.setCenteredAccess(a);
                        newDto = new SpotLightDTO(d1);
                        newDto.setStartTime(d1.getStartTime() + repetitionTime);
                        newDto.setStopTime(d1.getStopTime() +repetitionTime);
                        
//                        logger.debug("ACCESSO SET SPOTLIGHT AFTER REPETITION: "+DateUtils.fromCSKDateToDateTime(d1.getSquareStart()));
//                        logger.debug("ACCESSO SET SPOTLIGHT WITHOUT REPETITION: "+d1);
              

                    } // end if
                    else
                    {    		logger.debug("from expandDTOList");

                        logger.debug("non deve entrarci mai !");
                        /**
                         * Expansion is based on first access
                         */
                        a = new Access(d.getDtoAccessList().get(0));
                        a.setAccessTime(d.getDtoAccessList().get(0).getAccessTime() + repetitionTime);
                        newDto = new DTO(d);
                        newDto.setStartTime(d.getStartTime() + repetitionTime);
                        newDto.setStopTime(d.getStopTime() +repetitionTime);
                    }

                    
                    /**
                     * Setting satellite PVT
                     */
                    newDto.setSatPosAtStart(d.getSatPosAtStart());
                    newDto.setSatPosAtEnd(d.getSatPosAtEnd());
                    newDto.setSatVelAtStart(d.getSatVelAtStart());
                    newDto.setSatVelAtSEnd(d.getSatEndVelocity());
                    newDto.setExpandedDTO(true);
//                    logger.debug("ACCESSO SET newDto "+newDto);

                    // newDto.setSuf(d.getSuf());

                    ArrayList<Access> dtoList = new ArrayList<>();

                    // evaluate the new access list
                    // this is useful in case of interferometric mission
                    /*
                     * for(Access currentAccess : d.getDtoAccessList()) {
                     *
                     * a=new Access(currentAccess);
                     * a.setAccessTime(currentAccess.getAccessTime()+
                     * repetitionTime); dtoList.add(a); }
                     */
                    dtoList.add(a); // since CSG single acquired are not
                                    // expanded we don't need the full access
                                    // list to evaluate the AOI in sparc input
                    logger.debug("from expandDTOList");
                    newDto.setDtoAccessList(dtoList);

               

                    // in case of CSG we have use sparc
                    // so the following steps must be performed to ensure that
                    // sparc doesn't fails
                    // in evaluating along track cut
                    if (!newDto.getMissionName().equals(FeasibilityConstants.CSK_NAME))
                    {

                        // double evalAtEndTime=newDto instanceof SpotLightDTO?
                        // ((SpotLightDTO)newDto).getSquareStop() :
                        // newDto.getStopTime();
                        // poco leggibile, se qualcun altro deve metterci mano
                        // meglio essere più espliciti
                        double evalAtEndTime = newDto.getStopTime();
                        /*
                         * if(newDto instanceof SpotLightDTO) { SpotLightDTO
                         * d2=(SpotLightDTO)newDto;
                         * evalAtEndTime=d2.getSquareStop(); }//end if
                         */
                        try
                        {

                            newDto.getSat().getEpochAt(evalAtEndTime);
                            // If you are able to evaluate
                            // position and velocity at end
                            // the sparc will able to perform its own task
                            newDTOList.add(newDto);
                        } // end try
                        catch (Exception e)
                        {
                            this.tracer.warning(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "In expandind DTO the new DTO generate un exception at end time epoch evaluation");
                        } // end catch

                    } // end if
                    else
                    {
                        newDTOList.add(newDto);
                    } // end if
                    /**
                     * nexstop time is repetitionTime +
                     * FeasibilityConstants.RepetitionODREFPeriod
                     */
                    //nextStopTime = tstop + (i * FeasibilityConstants.RepetitionODREFPeriod);
                   
                    
                    
//                    logger.debug("tstop TIME : "+DateUtils.fromCSKDateToDateTime(tstop));
                   // i++;
                   // logger.debug("i vale (dopo incremento)  : "+ i);

                  //  repetitionTime = i * FeasibilityConstants.RepetitionODREFPeriod;
                    incrementalTime = incrementalTime + repetitionTime;

//                    logger.debug("incrementalTime TIME : "+DateUtils.fromCSKDateToDateTime(incrementalTime));

                } // end while
                if(incrementalTime > prValisityStop)
                {
//                	
//                    logger.debug("newDTOList ELEMENTS : "+newDTOList.size());
//                    for(int j=0;j<newDTOList.size();j++)
//                    {
//                    	logger.debug(newDTOList.get(j));
//                    }
//                    logger.debug("incrementalTime > prValisityStop");
                }
            } // end if
        } // end for

//        logger.debug("dtoList ELEMENTS PRIMA ADD: "+this.dtoList.size());
//        for(int j=0;j<this.dtoList.size();j++)
//        {
//        	logger.debug(this.dtoList.get(j));
//        }
        
        /**
         * Adding DTO to the ACQReq
         */

        for (DTO d : newDTOList)
        {
            addDTO(d);
        }
        
//        logger.debug("newDTOList ELEMENTS DOPO ADD: "+this.dtoList.size());
//        for(int j=0;j<this.dtoList.size();j++)
//        {
//        	logger.debug(this.dtoList.get(j));
//        }
        
        if (isSingleAqcuired)
        {
            /**
             * In case of single acquisition expansion could duplicate DTO So
             * duplivate must be removed
             */
            removeDuplicatedDTO();
        }

    }// end expandDTOList

    
    
//    /**
//     * For ODREF based DTO this methods find the DTO that will be reproposed on
//     * CSK period basis.
//     *
//     * @param prValisityStop
//     * @param isSingleAqcuired
//     */
//    public void expandDTOList(double prValisityStop, boolean isSingleAqcuired)
//    {
//        this.tracer.debug("Expanding DTO");
//        
//        logger.debug("prValisityStop as double : "+prValisityStop);
//
//        logger.debug("prValisityStop : "+ DateUtils.fromCSKDateToDateTime(prValisityStop));
//        List<DTO> newDTOList = new ArrayList<>();
//
//        for (DTO d : this.dtoList)
//        {
//
//            double tstart = d.getStartTime();
//            double tstop = d.getStopTime();
//            logger.debug("tstop dto as double: "+ tstop);
//
//            logger.debug("tstop dto : "+ DateUtils.fromCSKDateToDateTime(tstop));
//
//            /**
//             * square parameters to be updated in case of spotlight
//             */
//            double tsquareStart = 0;
//            double tsquareStop = 0;
//
//            /**
//             * stop time for the next dto
//             *
//             */
//            double nextStopTime;
//
//            /**
//             * Access used in the new DTO
//             */
//            Access a;
//            if (d.isOdrefBased())
//            {
//                /**
//                 * If DTO is based on odref it must be expanded
//                 */
//                nextStopTime = tstop + FeasibilityConstants.RepetitionODREFPeriod;
//                logger.debug("adding 16 days to tstop  "+DateUtils.fromCSKDateToDateTime(nextStopTime));
//                double incrementalTime = 0;
//                DTO newDto;
//                int i = 1;
//                /**
//                 * Exansion must not overcame PR Vvalidity
//                 */
//                while (incrementalTime <= prValisityStop)
//                {
//                	//i++;
//                    // logger.info("You must add dto cause next stop is: " +
//                    // DateUtils.fromCSKDateToISOFMTDateTime(nextStopTime));
//
//                    double repetitionTime = i * FeasibilityConstants.RepetitionODREFPeriod;
//                    logger.debug("repetitionTime : "+repetitionTime);
//                    logger.debug("i vale (inizio) : "+i);
//
//                    if (d instanceof SpotLightDTO)
//                    {
//                        /**
//                         * ID DTO is spot expansion is based in target center
//                         * point
//                         */
//             
//                        /*
//                         * MODIFICA VECCHIA IMPLEMENTAZIONE
//                         * 
//                         *               
//                         *               SpotLightDTO d1 = (SpotLightDTO) d;
//                                        newDto = new SpotLightDTO(d1);
//                         */
//                        
//                         SpotLightDTO d1 = (SpotLightDTO) d;
//
//
//                        a = new Access(d1.getCenteredAccess());
//                        
//                        a.setAccessTime(d1.getCenteredAccess().getAccessTime() + repetitionTime);
//                        
//                        logger.debug("d1.getCenteredAccess().getAccessTime(): "+DateUtils.fromCSKDateToDateTime(d1.getCenteredAccess().getAccessTime()));
//
//                        logger.debug("ACCESSO SET ACCESS TIME : "+DateUtils.fromCSKDateToDateTime(a.getAccessTime()));
//                        incrementalTime = d1.getCenteredAccess().getAccessTime();
//                        
//                        tsquareStart = d1.getSquareStart() + repetitionTime;
//                        tsquareStop = d1.getSquareStop() + repetitionTime;
//                        d1.setSquareStart(tsquareStart);
//                        d1.setSquareStop(tsquareStop);
//                        d1.setCenteredAccess(a);
//                        d1.setCenteredAccess(a);
//                        newDto = new SpotLightDTO(d1);
//
//                        /*
//                         * ((SpotLightDTO)newDto).
//                         * setSatellitePositionAtSquareStart(d1.
//                         * getSatellitePositionAtSquareStart());
//                         * ((SpotLightDTO)newDto).
//                         * setSatellitePositionAtSquareStop(d1.
//                         * getSatellitePositionAtSquareStop());
//                         * ((SpotLightDTO)newDto).
//                         * setSatelliteVelocityAtSquareStart(d1.
//                         * getSatelliteVelocityAtSquareStart());
//                         * ((SpotLightDTO)newDto).
//                         * setSatelliteVelocityAtSquareStop(d1.
//                         * getSatelliteVelocityAtSquareStop());
//                         */
//
//                    } // end if
//                    else
//                    {
//                        logger.debug("non deve entrarci mai !");
//                        /**
//                         * Expansion is based on first access
//                         */
//                        a = new Access(d.getDtoAccessList().get(0));
//                        a.setAccessTime(d.getDtoAccessList().get(0).getAccessTime() + repetitionTime);
//                        newDto = new DTO(d);
//                    }
//
//                    
//                    /**
//                     * Setting satellite PVT
//                     */
//                    newDto.setSatPosAtStart(d.getSatPosAtStart());
//                    newDto.setSatPosAtEnd(d.getSatPosAtEnd());
//                    newDto.setSatVelAtStart(d.getSatVelAtStart());
//                    newDto.setSatVelAtSEnd(d.getSatEndVelocity());
//                    newDto.setStartTime(tstart + repetitionTime);
//                    newDto.setStopTime(nextStopTime);
//                    newDto.setExpandedDTO(true);
//
//                    // newDto.setSuf(d.getSuf());
//
//                    ArrayList<Access> dtoList = new ArrayList<>();
//
//                    // evaluate the new access list
//                    // this is useful in case of interferometric mission
//                    /*
//                     * for(Access currentAccess : d.getDtoAccessList()) {
//                     *
//                     * a=new Access(currentAccess);
//                     * a.setAccessTime(currentAccess.getAccessTime()+
//                     * repetitionTime); dtoList.add(a); }
//                     */
//                    dtoList.add(a); // since CSG single acquired are not
//                                    // expanded we don't need the full access
//                                    // list to evaluate the AOI in sparc input
//                    newDto.setDtoAccessList(dtoList);
//
//                   // i++;
//
//                    // in case of CSG we have use sparc
//                    // so the following steps must be performed to ensure that
//                    // sparc doesn't fails
//                    // in evaluating along track cut
//                    if (!newDto.getMissionName().equals(FeasibilityConstants.CSK_NAME))
//                    {
//
//                        // double evalAtEndTime=newDto instanceof SpotLightDTO?
//                        // ((SpotLightDTO)newDto).getSquareStop() :
//                        // newDto.getStopTime();
//                        // poco leggibile, se qualcun altro deve metterci mano
//                        // meglio essere più espliciti
//                        double evalAtEndTime = newDto.getStopTime();
//                        /*
//                         * if(newDto instanceof SpotLightDTO) { SpotLightDTO
//                         * d2=(SpotLightDTO)newDto;
//                         * evalAtEndTime=d2.getSquareStop(); }//end if
//                         */
//                        try
//                        {
//
//                            newDto.getSat().getEpochAt(evalAtEndTime);
//                            // If you are able to evaluate
//                            // position and velocity at end
//                            // the sparc will able to perform its own task
//                            newDTOList.add(newDto);
//                        } // end try
//                        catch (Exception e)
//                        {
//                            this.tracer.warning(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "In expandind DTO the new DTO generate un exception at end time epoch evaluation");
//                        } // end catch
//
//                    } // end if
//                    else
//                    {
//                        newDTOList.add(newDto);
//                    } // end if
//                    /**
//                     * nexstop time is repetitionTime +
//                     * FeasibilityConstants.RepetitionODREFPeriod
//                     */
//                    //nextStopTime = tstop + (i * FeasibilityConstants.RepetitionODREFPeriod);
//                   
//                    
//                    
//                    logger.debug("tstop TIME : "+DateUtils.fromCSKDateToDateTime(tstop));
//                    logger.debug("i vale (dopo incremento)  : "+ i);
//
//                    logger.debug("nextStopTime TIME : "+DateUtils.fromCSKDateToDateTime(nextStopTime));
//
//                } // end while
//
//            } // end if
//        } // end for
//
//        /**
//         * Adding DTO to the ACQReq
//         */
//
//        for (DTO d : newDTOList)
//        {
//            addDTO(d);
//        }
//        if (isSingleAqcuired)
//        {
//            /**
//             * In case of single acquisition expansion could duplicate DTO So
//             * duplivate must be removed
//             */
//            removeDuplicatedDTO();
//        }
//
//    }// end expandDTOList

    
    
    
    
    
    
    
    /**
     * In case of single acquistion this method remove duplicated DTO
     */
    /*
     * private void removeDuplicatedDTO(){
     * this.tracer.debug("Searching for duplicated DTO"); Set<DTO> dtoSet = new
     * TreeSet<DTO>(new Comparator<DTO>() {
     *
     * @Override public int compare(DTO arg0, DTO arg1) {
     * if(arg0.getStartTime()==arg1.getStartTime() &&
     * arg0.getStopTime()==arg1.getStopTime() &&
     * arg0.getSatName().equals(arg1.getSatName()) &&
     * arg0.getBeamId().equals(arg1.getBeamId()) && arg0.getLookSide() ==
     * arg1.getLookSide() && arg0.getOrditDirection() ==
     * arg1.getOrditDirection() ){ return 0; } return 1;
     *
     * } }); for(DTO d : this.dtoList){ dtoSet.add(d); }
     *
     * int oldSize= this.dtoList.size();
     *
     * this.dtoList = new ArrayList<DTO>(); for(DTO d : dtoSet){ this.addDTO(d);
     * }
     *
     * int newSize = this.dtoList.size();
     *
     * int numberOfRemovedDTO = oldSize - newSize; if(numberOfRemovedDTO>0){
     * this.tracer.debug("Removed " + numberOfRemovedDTO + " duplicated DTO"); }
     *
     *
     * }//end removeDuplicatedDTO
     */

    /**
     * Remove duplivate DTO in case of expansion of DTO resulting of a single
     * acquired PR
     */
    private void removeDuplicatedDTO()
    {
        this.tracer.debug("Searching for duplicated DTO");
        /**
         * Current DTO list size
         */
        int oldSize = this.dtoList.size();

        /**
         * New DTO list
         */
        ArrayList<DTO> newList = new ArrayList<>();

        /**
         * Old dto list
         */
        ArrayList<DTO> tempList = new ArrayList<>();

        for (DTO d : this.dtoList)
        {
            /**
             * Filling temporary list
             */
            tempList.add(d);
        }

        /**
         * searching for duplicates
         *
         */
        for (int i = 0; i < this.dtoList.size(); i++)
        {
            DTO currDTO = this.dtoList.get(i);
            boolean hasDuplicated = false;
            for (int j = i + 1; j < tempList.size(); j++)
            {
                DTO currTempDTO = tempList.get(j);
                if (areDTOEquals(currDTO, currTempDTO))
                {
                    /**
                     * Found duplicated
                     */
                    hasDuplicated = true;
                    break;
                }
            } // end for
            if (!hasDuplicated)
            {
                /**
                 * If no duplicated add DTO to new list
                 */
                newList.add(currDTO);
            }
        } // end for

        /**
         * Assigning newList to dtoList
         */
        this.dtoList = newList;

        int newSize = this.dtoList.size();

        int numberOfRemovedDTO = oldSize - newSize;

        /**
         * If the we have removed duplicated we reinitialize the id
         */
        if (numberOfRemovedDTO > 0)
        {
            int i = 1;
            for (DTO d : this.dtoList)
            {
                d.setId("" + i);
                i++;
            } // End for
            this.tracer.debug("Removed " + numberOfRemovedDTO + " duplicated DTO");
        } // end if

    }// end removeDuplicatedDTO

    /**
     * Check if two dto are equal
     *
     * @param arg0
     * @param arg1
     * @return true if two DTO are duplicated
     */
    private boolean areDTOEquals(DTO arg0, DTO arg1)
    {
        String start0 = DateUtils.fromCSKDateToISOFMTDateTime(arg0.startTime);
        String start1 = DateUtils.fromCSKDateToISOFMTDateTime(arg1.startTime);
        String stop0 = DateUtils.fromCSKDateToISOFMTDateTime(arg0.stopTime);
        String stop1 = DateUtils.fromCSKDateToISOFMTDateTime(arg1.stopTime);
        /*
         * if(start0.equals(start1)&&stop0.equals(stop1)&&
         * arg0.getSatName().equals(arg1.getSatName()) &&
         * arg0.getBeamId().equals(arg1.getBeamId()) && arg0.getLookSide() ==
         * arg1.getLookSide() && arg0.getOrditDirection() ==
         * arg1.getOrditDirection() ){ return 0; } return 1;
         */
        /**
         * Duplicate if: same start && same stop && same beam && same sat &&
         * same side && same orbit direction
         */
        boolean retval = (start0.equals(start1) && stop0.equals(stop1) && arg0.getSatName().trim().equals(arg1.getSatName().trim()) && arg0.getBeamId().trim().equals(arg1.getBeamId().trim()) && (arg0.getLookSide() == arg1.getLookSide()) && (arg0.getOrditDirection() == arg1.getOrditDirection()));

        return retval;
    }// end areDTOEquals

    /**
     * Remove DTO under not deferrable paw and mark the DTO that are under
     * deferrable paw
     */
    public void cheCheckForPaw()
    {
        /**
         * List of DTO that are outside paw
         */
        ArrayList<DTO> notPawwedDTO = new ArrayList<>();

        /**
         * iterating on DTO List
         */
        for (DTO d : this.dtoList)
        {
            /**
             * Check if DTO is under paw
             */
            if (!d.checkForUndeferreblePaw())
            {
                notPawwedDTO.add(d);
            }

        } // end for

        /**
         * Reassign dto list
         */
        this.dtoList = notPawwedDTO;

    }// end cheCheckForPaw

    /**
     * Remove DTO that are outside pass plan Used for passThrough
     */
    public void checkForPassPlan()
    {
        ArrayList<DTO> usableDTO = new ArrayList<>();

        /**
         * Iterate on dto List
         */
        for (DTO d : this.dtoList)
        {
            if (d.checkForPassPlan())
            {
                usableDTO.add(d);
            }
        }

        /**
         * Reassign DTO List
         */
        this.dtoList = usableDTO;
    }// end checkForPassPlan

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }// end method

    /**
     * @return the id
     */
    public String getId()
    {
        return this.id;
    }// end method

    /**
     * @return the mission
     */
    public Set<String> getMission()
    {
        return this.missionSet;
    }// end method

    /**
     * @param mission
     *            the mission to set
     */
    public void setMission(String mission)
    {
        // this.mission = mission;
        this.missionSet.add(mission);
    }// end method

    /**
     * duplicate set
     *
     * @param mission
     */
    public void setMission(Set<String> mission)
    {
        for (String s : mission)
        {
            this.missionSet.add(s);
        }
    }

    /**
     *
     * @return the DTO list
     */
    public ArrayList<DTO> getDTOList()

    {
        return this.dtoList;
    }// end method

    /**
     * set the dto list
     *
     * @param dtoList
     */
    public void setDTOList(ArrayList<DTO> dtoList)
    {
        this.dtoList = dtoList;
    }// end method

    /**
     * Retrurn an AR form interferometricSat linked to the current AR, morever
     * remove all DTO without interferometric pair
     *
     * @param interferometricSat
     * @param decorrelationTime
     * @param decorrelationTolerance
     * @return thr interferometric AR
     */
    public AcqReq getinterferometricAcqReq(final Satellite interferometricSat, double decorrelationTime, double decorrelationTolerance, double stopValidityTime)
    {
        /**
         * AR to be returned
         */
        AcqReq retval = new AcqReq();

        retval.setMission(this.missionSet);

        /**
         * Interferometric dto list
         */
        ArrayList<DTO> interferometricDtoList = new ArrayList<>();

        ArrayList<DTO> goodDTO = new ArrayList<>();
        logger.debug("RETURNED interferometricSat "+interferometricSat);
        logger.debug("RETURNED decorrelationTime "+decorrelationTime);
        logger.debug("RETURNED decorrelationTolerance "+decorrelationTolerance);
        logger.debug("RETURNED stopValidityTime "+stopValidityTime);

        /**
         * Iterate on dto list
         */
        for (DTO d : this.dtoList)
        {
            /**
             * Retrieving DTO interferometric
             */
            DTO interferometricDTO = d.getInterferometricDTO(interferometricSat, decorrelationTime, decorrelationTolerance, stopValidityTime);
logger.debug("RETURNED interfDTO "+interferometricDTO);
            /**
             * Add dto if not null
             */
            if (interferometricDTO != null)
            {
                interferometricDtoList.add(interferometricDTO);
                goodDTO.add(d);
            }

        } // end fir

        if (interferometricDtoList.size() != 0)
        {
            /**
             * If the interferometric list is not empty
             */
            retval.setDTOList(interferometricDtoList);
            this.linkedAR = retval;
            retval.setLinkedAR(this);
            this.dtoList = goodDTO;
        } // end if
        else
        {
            /**
             * if interferometricList is empty
             */
            retval = null;
        }

        return retval;
    }// end getinterferometricAcqReq

    /**
     * Dump the AR to a String: for test purposes only
     *
     * @return
     */
    /*
     * public String dumpToString() { String retval ="";
     *
     * StringWriter out = new StringWriter(); out.write("ARID: " + id +
     * " mission  " + mission + "\n");
     *
     * for(DTO d : dtoList) { //double[] llhSatStartPos =
     * ReferenceFrameUtils.ecef2llh(d.getSatPosAtStart().toArray(), true);
     * //double[] llhSatEndPos =
     * ReferenceFrameUtils.ecef2llh(d.getSatPosAtEnd().toArray(), true);
     *
     * out.write("\tDTO " + d.getId() + "    isOderef: " +
     * d.isOdrefBased()+"\n");
     *
     * out.write("\tStrip id: " + d.getStrip().getId()+"\n");
     *
     * out.write("\t\t from " +
     * DateUtils.fromCSKDateToISOFMTDateTime(d.getStartTime()) + " to "+
     * DateUtils.fromCSKDateToISOFMTDateTime(d.getStopTime())+"\n");
     *
     *
     * out.write("\t\t " + d.getSatName() + " side: " +
     * FeasibilityConstants.getLookSideString(d.getLookSide())+ " orbit dir: " +
     * FeasibilityConstants.getOrbitDirectionAsString(d.getOrditDirection())+
     * "\n");
     *
     *
     *
     * out.write("\t\t Beam: " + d.getBeam().getBeamName()+" near angle: "+
     * d.getBeam().getNearOffNadir() + " far angle: " +
     * d.getBeam().getFarOffNadir()+"\n"); out.write("\t\t off nadir at start: "
     * + d.getStrip().getAccessList().get(0).getOffNadir()+"\n");
     * out.write("\t\t corner1: lat " + d.getFirstCorner()[0]+ " lon " +
     * d.getFirstCorner()[1] +"\n");
     *
     * out.write("\t\t corner2: lat " + d.getSecondCorner()[0]+ " lon " +
     * d.getSecondCorner()[1] +"\n"); out.write("\t\t corner3: lat " +
     * d.getThirdCorner()[0]+ " lon " + d.getThirdCorner()[1] +"\n");
     * out.write("\t\t corner4: lat " + d.getFourtCorner()[0]+ " lon " +
     * d.getFourtCorner()[1] +"\n"); out.write("\t\t------------------------");
     * }
     *
     *
     * return out.toString(); }
     */
    /*
     * public void dumpArlistOnFiles(String outDir) throws IOException { for(DTO
     * d: dtoList) { String outFilePath =
     * outDir+java.io.File.separator+"AR"+this.id+"DTO"+d.getId()+".txt";
     * BufferedWriter out = new BufferedWriter(new FileWriter(outFilePath));
     *
     * double[][] corners = d.getCorners();
     *
     * for(int i = 0; i< corners.length;i++) {
     * out.write(corners[i][0]+";"+corners[i][1]+"\n"); }
     * out.write(corners[0][0]+";"+corners[0][1]); out.close(); } }
     */

    @Override
    public String toString()
    {
        return "AcqReq [tracer=" + this.tracer + ", id=" + this.id + ", missionSet=" + this.missionSet + ", dtoList SIZE" + this.dtoList.size() + ", DTO list = " + this.dtoList + "]";
    }

    /**
     * @return true if two ar have the same id
     */
    @Override
    public boolean equals(Object obj)
    {
        AcqReq acq = (AcqReq) obj;
        /**
         * Retval
         */
        boolean retval = this.id.equals(acq.id);
        return retval;
    }

    /**
     * compare two AR on ID basis
     *
     * @return 0 if id are the same 1 if id0 > id1 -1 if id0<id1
     */
    @Override
    public int compareTo(AcqReq acq)
    {
        return this.id.compareTo(acq.id);
    }

}
