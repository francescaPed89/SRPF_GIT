/**
*
* MODULE FILE NAME:	OptimizationAlgoPointDuration.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Specialization of the optimization algo for the case of Point Duration requests
*
* PURPOSE:			Used to perform the feasibility in case of point duration requestes
*
* CREATION DATE:	29-11-2016
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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.telespazio.csg.srpf.dataManager.bean.EpochBean;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.DateUtils;

/**
 * Specialization of the optimization algo for the case of Point Duration
 * requests
 *
 * @author Amedeo Bancone
 * @version 1.0
 *
 */
public class OptimizationAlgoPointDuration implements OptimizationAlgoInterface

{
	static final Logger logger = LogManager.getLogger(OptimizationAlgoPointDuration.class.getName());

    /**
     * log
     */
    TraceManager tm = new TraceManager();
    /**
     * Satellite list
     */
    private List<Satellite> satelliteList;

    /**
     * duration of acquisition
     */
    private double duration;

    /**
     * Stop Validity of PR
     */
    private double stopValidity;

    /**
     * AcqList result
     */
    private List<AcqReq> optimalAcqReq = new ArrayList<>();

    /**
     * Constructor
     *
     * @param accessList
     * @param duration
     *            in CSKTIME
     * @param the
     *            stopValidity time for the pr
     */
    public OptimizationAlgoPointDuration(List<Satellite> satelliteList, double duration, double stopValidity)
    {
        /**
         * Initielizing
         */
        this.satelliteList = satelliteList;
        this.duration = duration;

        this.stopValidity = stopValidity;

        /**
         * evaluating min e max duration for sensor mode
         *
         */
        double maxDuration = satelliteList.get(0).getStripMapMaximalDuration();
        double minDuration = satelliteList.get(0).getStripMapMinimalDuration();

        if (this.duration <= minDuration)
        {
            /**
             * the duration can not be less than min duration of a DTO
             */
            this.duration = minDuration;
        } // end if
        else if (this.duration > maxDuration)
        {
            /**
             * the acquisition duration can not be greather than the max
             * duration of a DTO
             */
            this.duration = maxDuration;
        } // end else if

    }// end method

    /**
     * Return the optimal Acquisition Request List. If no AR has been found
     * return an empty list
     *
     * @return The optimized AcqReq
     */
    @Override
    public List<AcqReq> getOptimalAcqReqList()
    {
        return this.optimalAcqReq;
    }// end method

    /**
     * Return true if the PR as only one AcqReq. In case of single point it
     * returns always true
     *
     * @return true if the request has only one AcqReq
     */
    @Override
    public boolean isSingleAcquired()
    {
        boolean val = false;
        /**
         * if empty is false
         */
        if (this.optimalAcqReq.size() != 0)
        {
            val = true;
        }
        return val;
    }// end method

    /**
     * perform numberOfIteration for the outer optimization loop. In this case
     * p1=p2=p3=1
     *
     * @param numberOfIteration.
     *            Unused in this case only one iteration is performed
     */
    @Override
    public void performOptimizationLoop(int numberOfIteration)
    {
        /**
         * AR
         */
        AcqReq ar = new AcqReq();
        DTO d= null;
        /**
         * building DTO
         */
        for (Satellite sat : this.satelliteList)
        {
            for (Access a : sat.getAccessList())
            {
                /**
                 * Building and ADDING DTO to AR
                 */
                d = buildDTO(a);
                if (d != null)
                {
                    ar.addDTO(d);
                } // end if
            } // end for

        } // end for
        if (ar.getDTOList().size() != 0)
        {
            /**
             * Setting AR Parameters and adding to optimal list
             */
            ar.setMission(ar.getDTOList().get(0).getSat().getMissionName());
            ar.setId("1");
            this.optimalAcqReq.add(ar);
        } // end if
    }// end method

    /**
     * Build the DTO for a given access
     *
     * @param a
     *            Access
     * @return DTO on given access null otherwise
     */
    private DTO buildDTO(Access a)
    {
        this.tm.debug("Building DTO for point duration request");
        DTO d = null;
        try
        {
            double startTime = a.getAccessTime();
            double stopTime = a.getAccessTime() + this.duration;

            logger.debug("ACCESS FROM buildDTO "+DateUtils.fromCSKDateToDateTime(stopTime));
            // check if the DTO is inside the validity rime of the PR
            if (stopTime <= this.stopValidity)
            {
                Satellite sat = a.getSatellite();
                /**
                 * Retrieving satellite PVT at start and stop
                 */
                EpochBean epochBeanAtStart = sat.getEpochAt(startTime);
                EpochBean epochBeanAtStop = sat.getEpochAt(stopTime);
                /**
                 * Adding access to access list
                 */
                ArrayList<Access> list = new ArrayList<>();
                list.add(a);
                /**
                 * creating fake strip
                 */
                Strip strip = new Strip(1, list);
                /**
                 * Building DTO
                 */
                d = new DTO(strip);
                d.setStartTime(startTime);
                d.setStopTime(stopTime);
                /**
                 * Evaluating corners
                 */
                double[][] earlyCorner = d.evaluateLLHcorners(epochBeanAtStart.getoXyz(), epochBeanAtStart.getoVxVyVz());
                double[][] lateCorner = d.evaluateLLHcorners(epochBeanAtStop.getoXyz(), epochBeanAtStop.getoVxVyVz());

                /**
                 * Setting corners
                 */
                d.setFirstCorner(earlyCorner[0]);
                d.setSecondCorner(earlyCorner[1]);

                d.setThirdCorner(lateCorner[1]);
                d.setFourthCorner(lateCorner[0]);
            } // end if

        } // end try
        catch (Exception e)
        {
            // out of time not access not usable
            this.tm.warning(EventType.APPLICATION_EVENT, ProbableCause.APPLICATION_SUBSYSTEM_FAILURE, "Error on building DTO: " + e.getMessage());
        }

        return d;
    }// end method

    /**
     * @return the number of the grid not covered
     */
    @Override
    public int getUncoveredNumberOfPoints()
    {
        // TODO Auto-generated method stub
        return 0;
    }// end method
}// end class
