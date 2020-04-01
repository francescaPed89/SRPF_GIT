/**
*
* MODULE FILE NAME:	InnerOptimizationIterationLoopSpotlight.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Perform an inner optimization loop in Feasibility evaluation of feasibility case spotlight
*
* PURPOSE:			Perform Feasibility
*
* CREATION DATE:	05-02-2016
*
* AUTHORS:			Amedeo Bancone
*
* DESIGN ISSUE:		1.1
*
* INTERFACES:
*
* SUBORDINATES:
*
* MODIFICATION HISTORY:
*
*
* --------------------------+------------+----------------+-------------------------------
* 11-05-2016 | Amedeo Bancone  |1.1| removed checks for point near polar region
* 									 modified algo
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.feasibility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;

/**
 * Perform Inner optimization loop
 *
 * @author Amedeo Bancone
 * @version 1.1
 *
 *
 */

public class InnerOptimizationIterationLoopSpotlight extends InnerOptimizationLoopIterator

{

    // static final Logger logger =
    // LogManager.getLogger(InnerOptimizationIterationLoopSpotlight.class.getName());
    /**
     * logger
     */
    private TraceManager tracer = new TraceManager();

    /**
     * Temporary DTO map strip vs map used in evaluating optimal dto
     */
    private Map<String, StripWrapper> temporaryDTOmap = new TreeMap<>();

    /**
     * Constructor
     *
     * @param p1
     *            optmization wwight (number is better)
     * @param p2
     *            optimization weight (new is better)
     * @param p3
     *            optimization weight (first is better)
     * @param temporaryGridPoint
     * @param temporaryStripList
     * @param numberOfAlreadyFoundpoints
     */
    public InnerOptimizationIterationLoopSpotlight(double p1, double p2, double p3, final List<String> gridPointList, List<String> temporaryGridPointList, List<Strip> temporaryStripList)
    {
        /**
         * Call superclass constructor
         */
        super(p1, p2, p3, gridPointList, temporaryGridPointList, temporaryStripList);
    }// end method

    /**
     * Perform an iteration on the selectedPoint and return the relevant AR null
     * if none
     *
     * @param selectedPoint
     * @param alreadyFoundPointList
     * @return AR null if none
     */
    @Override
    public AcqReq iterate(GridPoint selectedPoint, List<String> alreadyFoundPointList)

    {
        // TODO Auto-generated method stub

        /**
         * Claering temèporary map
         */
        this.temporaryDTOmap.clear();

        /**
         * Searching for strip holeding the point
         */
        List<Strip> listOfStripHoldingThePoint = findStripHoldingPoint(selectedPoint);

        if (listOfStripHoldingThePoint.isEmpty())
        {
            /**
             * empty list no strip found, have exit
             */
            this.temporaryGridPointList.remove(selectedPoint.getUnivocalKey());
            return null;
        }

        /**
         * list of worng strip
         */
        List<Strip> wrongStripList = new ArrayList<>();

        SpotLightDTO d;

        Access access;
        // for each strip holding the point
        for (Strip s : listOfStripHoldingThePoint)
        {
            try
            {
                access = s.getAccessForPoint(selectedPoint);

                /*
                 * if(!s.isAccessUsable(accessTime)) //the access is inside the
                 * zone of the strip not usable { continue; }
                 */
                /**
                 * building dto
                 */
                d = new SpotLightDTO(access, s);

                if (!d.isGood())
                {
                    /**
                     * if the evaluated DTO is wrong add current strip to bad
                     * strip and check on the next
                     */
                    // tracer.debug("wrong dto");
                    wrongStripList.add(s);
                    continue;
                } // end if

                String stripKey = s.getUnivocalKey();
                logger.trace("MODIFICA STRIPID FOR POINT " + stripKey);
                /**
                 * adding dto to the temporary strip map
                 */
                this.temporaryDTOmap.put(stripKey, new StripWrapper(s, d));

            } // end try
            catch (Exception e)
            {
                /**
                 * Error we must return
                 */
                this.tracer.critical(EventType.SOFTWARE_EVENT, ProbableCause.SOFTWARE_ERROR, "Spotlight Iteration exception: " + e.getMessage());
                this.temporaryGridPointList.remove(selectedPoint.getUnivocalKey());
                return null;

            } // end catch

        } // end For

        /**
         * removing wrong strip from the list of strip holding the point
         */
        for (Strip s : wrongStripList)
        {
            // logger.warn("Removing wrong DTO");
            // //System.out.println("===================removing wrong strip:
            // =============================");
            // this.temporaryStripList.remove(s);
            listOfStripHoldingThePoint.remove(s);
        }

        // logger.info("listOfStripHoldingThePoint size " +
        // listOfStripHoldingThePoint.size());

        if ((listOfStripHoldingThePoint.size() == 0) || (this.temporaryDTOmap.size() == 0))
        {
            /**
             * Now the list of strip holding point is empty or the map of DTO is
             * empty so no DTO opoint Remove point from temporary grid and
             * return null
             */

            String univocalKeyGp = selectedPoint.getUnivocalKey();
            this.temporaryGridPointList.remove(univocalKeyGp);
            return null;
        }

        /**
         * Searching for the optimal strip and then building AR
         */
        Strip optimalStrip = findOptimalStrip(listOfStripHoldingThePoint, alreadyFoundPointList);
        AcqReq ar = new AcqReq();
        String stripKey = optimalStrip.getUnivocalKey();
        logger.trace("MODIFICA STRIPID " + stripKey);

        SpotLightDTO dto = this.temporaryDTOmap.get(stripKey).getSpotDto();
        ar.addDTO(dto);
        ar.setMission(optimalStrip.getAccessList().get(0).getMissionName());

        // logger.info("Spot new point: "
        // +getNumberOfNewPoint(optimalStrip,alreadyFoundPointList) );
        /**
         * evaluating the new number of point covered
         */
        this.numberOfNewPoint = getNumberOfNewPoint(optimalStrip, alreadyFoundPointList).size();
        
        List<GridPoint> newGridPoint = getNumberOfNewPoint(optimalStrip, alreadyFoundPointList);

        
        logger.trace("MODIFICA UPDATE accsses of optimized strip " + this.temporaryDTOmap.get(stripKey).getSpotDto().getAccessesInsideSpotList().size());
        logger.trace("MODIFICA alreadyFoundPointList size : " + alreadyFoundPointList.size());

        /**
         * Removing new point covered from the temporary grid and adding them to
         * the already found list
         */
        
        if(newGridPoint!=null && !newGridPoint.isEmpty())
        {
            for (GridPoint gp : newGridPoint)

            {
                logger.trace("MODIFICA UPDATE grid point for access :" + gp);
                logger.trace("MODIFICA alreadyFoundPointList size : " + alreadyFoundPointList.size());
                logger.trace("MODIFICA temporaryGridPointList size : " + this.temporaryGridPointList.size());

                if (!alreadyFoundPointList.contains(gp.getUnivocalKey()))// it is a
                                                                        // new point
                                                                        // !
                {
                    logger.trace("MODIFICA adding new gridPoint");

                    alreadyFoundPointList.add(gp.getUnivocalKey());
                }
                logger.trace("MODIFICA alreadyFoundPointList size : " + alreadyFoundPointList.size());

                if (this.temporaryGridPointList.contains(gp.getUnivocalKey()))
                {
                    this.temporaryGridPointList.remove(gp.getUnivocalKey());
                }

            } // end for
        }


        /**
         * Now we have to remove from useable accesses all the accesses of
         * satellites overlapping (threashold) included the new DTO
         */

        Satellite sat = optimalStrip.getAccessList().get(0).getSatellite();
        double upperGuard = dto.getStopTime() + (FeasibilityConstants.forTwo * sat.getSpotLightTimeStep());
        double lowerGuard = dto.getStartTime() - (FeasibilityConstants.forTwo * sat.getSpotLightTimeStep());

        /**
         * list of access to be deleted
         */
        List<Access> tobeDeletedList = new ArrayList<>();
        double time;

        /*
         * 
         * 
         * // for(Access a : optimalStrip.getAccessList()) for (Access a :
         * optimalStrip.getStillUsableAccessList()) { time = a.getAccessTime();
         * if ((time > lowerGuard) && (time < upperGuard)) { //TODO perchè non
         * vengono gestiti i tempi di guardia? ; } tobeDeletedList.add(a); } //
         * end for
         * 
         */
        // for(Access a : optimalStrip.getAccessList())
        for (Access a : optimalStrip.getStillUsableAccessList())
        {
            /**
             * Adding to to be deleted list all accesses inside guard intervel
             */
            time = a.getAccessTime();
            if ((time > lowerGuard) && (time < upperGuard))
            {
                tobeDeletedList.add(a);
            }
        } // end for

        // List<Access> accessInStrip = optimalStrip.getAccessList();
        List<Access> accessInStrip = optimalStrip.getStillUsableAccessList();
        for (Access a : tobeDeletedList)
        {
            /**
             * remove form the still usable access the to be deletd access
             */
            accessInStrip.remove(a);
        } // end for
        if (accessInStrip.size() == 0)
        {
            /**
             * if the usable list is empty the strip is not usable anymore
             */
            this.temporaryStripList.remove(optimalStrip.getUnivocalKey());
            this.temporaryStrip.remove(optimalStrip.getUnivocalKey());

        } // end if

        /**
         * Checking on the other strips belonging the satellite for conflicting
         * accesses on DTO
         */
        removeAccessesForAllStripsBelongingTheOrbit(optimalStrip, dto);

        return ar;
    }// end iterate

    /**
     * rimuove le strip che sono nella stessa orbita
     *
     * @param optimalStrip
     * @param dto
     */
    private void removeAccessesForAllStripsBelongingTheOrbit(Strip optimalStrip, SpotLightDTO dto)
    {
        /**
         * Satellite whose DTO belongs
         */
        Satellite sat = optimalStrip.getAccessList().get(0).getSatellite();

        /**
         * List of strip ti be deleted
         */
        List<Strip> toBeDeletedStrip = new ArrayList<>();

        /**
         * gusrd interval
         */
        double upperGuard = dto.getStopTime() + (FeasibilityConstants.forTwo * sat.getSpotLightTimeStep());
        double lowerGuard = dto.getStartTime() - (FeasibilityConstants.forTwo * sat.getSpotLightTimeStep());

        /**
         * for each strip
         */
        for (String stripId : this.temporaryStripList)
        {

            Strip s = this.temporaryStrip.get(stripId);
            if (!s.getSatelliteId().equals(sat.getName()))
            {
                // different satellites no operation
                // to perform
                continue;
            }

            if (s.equals(optimalStrip))
            {
                // same strip
                // no operation to perform
                continue;
            }

            if (((optimalStrip.getStartTime() - s.getStopTime()) > sat.getSensorRestoreTime()) || ((s.getStartTime() - optimalStrip.getStopTime()) > sat.getSensorRestoreTime()))
            {
                // no conflict
                // no operation to perform
                continue;
            }

            // list of access to be deleted
            List<Access> tobeDeletedList = new ArrayList<>();
            double time;

            // MODIFICA : non utilizzo
            for (Access a : s.getStillUsableAccessList())
            {
                time = a.getAccessTime();
                // access conflict add to be deleted
                if ((time > lowerGuard) && (time < upperGuard))
                {
                    tobeDeletedList.add(a);

                }
            }

            /*
             * MODIFICA ERA COSI
             * 
             * for (Access a : s.getStillUsableAccessList()) { time =
             * a.getAccessTime(); // access conflict add to be deleted if ((time
             * > lowerGuard) && (time < upperGuard)) { ; }
             * tobeDeletedList.add(a); }
             */

            // List<Access> accessInStrip = optimalStrip.getAccessList();
            List<Access> accessInStrip = s.getStillUsableAccessList();

            for (Access a : tobeDeletedList)
            {
                // removing access to be deleted from still usable

                accessInStrip.remove(a);
            } // end for
            if (accessInStrip.size() == 0)
            {
                // no usable access
                // the strip must be removed
                toBeDeletedStrip.add(s);
            } // end if

        } // end for

        for (Strip s : toBeDeletedStrip)
        {
            // removing empty strips
            this.temporaryStripList.remove(s.getUnivocalKey());
            this.temporaryStrip.remove(s.getUnivocalKey());

        } // end for
    }// end removeAccessesForAllStripsBelongingTheOrbit

    /**
     *
     * @param s
     * @return the number of point intercepted
     */
    @Override
    protected int getNumberOfPoint(Strip s)
    {
        logger.trace("MODIFICA getNumberOfPoint per SPOTLIGHT");
        // logger.info("Using ovveride version of getNumberOfPoint");
        String stripKey = s.getUnivocalKey();

        return this.temporaryDTOmap.get(stripKey).getSpotDto().getNumberOfPoint();
    }// end method

    /**
     *
     */
    @Override
    protected List<GridPoint> getNumberOfNewPoint(final Strip s, final List<String> alreadyFoundPointList)
    {
        List<GridPoint> newPoints = new ArrayList<GridPoint>();
        // logger.info("Using ovveride version of getNumberOfNewPoint");
        String stripKey = s.getUnivocalKey();

        newPoints = this.temporaryDTOmap.get(stripKey).getSpotDto().getNewPointNumber(alreadyFoundPointList);
        return newPoints;
    }// end getNumberOfNewPoint

    /**
     * find the max startTime in the strip list
     *
     * @param stripList
     * @return min starttime
     */
    @Override
    protected double findTmaxInStripList(List<Strip> stripList)
    {

        // logger.info("Using ovveride version of findTmaxInStripList");

        for (Map.Entry<String, StripWrapper> subMap : this.temporaryDTOmap.entrySet())
        {

            logger.trace("MODIFICA SPOTLIGHT temporaryDTOmap : " + subMap.getValue().getStrip().getStartTime());

        }
        /**
         * Searching The max start time in stri list
         */
        double tmax = -1;
        double currentTime = 0;
        for (Strip s : stripList)
        {
            String stripKey = s.getUnivocalKey();

            if (this.temporaryDTOmap.get(stripKey) != null)
            {
                logger.trace("MODIFICA SPOTLIGHT FOUND A MATCH ! " + this.temporaryDTOmap.get(stripKey));
                logger.trace("MODIFICA SPOTLIGHT FOUND A MATCH ! " + this.temporaryDTOmap.get(stripKey).getStrip().getStartTime());

            }
            else
            {
                logger.trace("MODIFICA SPOTLIGHT CANNOT FOUND A MATCH ! ");

            }
            currentTime = this.temporaryDTOmap.get(stripKey).getStrip().getStartTime();
            // if currentTime > max
            if (currentTime > tmax)
            {
                tmax = currentTime;
            }
        } // end for

        logger.trace("MODIFICA SPOTLIGHT found tmax : " + tmax);

        return tmax;
    } // end findTmaxInStripList

}// end class
