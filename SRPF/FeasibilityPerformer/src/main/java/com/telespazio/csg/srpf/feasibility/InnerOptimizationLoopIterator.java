/**
*
* MODULE FILE NAME:	InnerOptimizationLoopIterator.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Perform an inner optimization loop in Feasibility evaluation of feasibility
*
* PURPOSE:			Perform Feasibility
*
* CREATION DATE:	05-02-2016
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
* 11-05-2016 | Amedeo Bancone  |1.1| findOptimalStrip Modified to align to the new objective function and normalization function
* --------------------------+------------+----------------+-------------------------------
* * --------------------------+------------+----------------+-------------------------------
* 11-07-2016 | Amedeo Bancone  |2.0| modified iterate to take into account the evaluation of mean elevation. Inserted in useful access the ones comprised between start and stop also if belonging to other DTOs
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.feasibility;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.PropertiesReader;

/**
 * perfrom an iteration for the inner loop of the optimization algo in case
 * strip
 *
 * @author Amedeo Bancone
 * @version 2.0
 *
 */
public class InnerOptimizationLoopIterator

{

    /**
     * logger
     */
    // private TraceManager tracer = new TraceManager();
    static final Logger logger = LogManager.getLogger(InnerOptimizationLoopIterator.class.getName());
    // Coefficient for the objective function
    /**
     * number is better
     */
    protected double P1;
    /**
     * new is better
     */
    protected double P2;
    /**
     * first is better
     */
    protected double P3;

    /**
     * true if have use new access only in strip evaluation they should be
     * always be setted to true by configuration
     */
    protected boolean haveUseOnlyNewAccesses = true;

    /**
     * Original grid list
     */
    protected List<String> gridPointList = new ArrayList<String>();

    /**
     * temporary strip list for outer iteration
     */
    protected List<String> temporaryStripList = new ArrayList<String>();

    /**
     * temporary grid list for outer iteration
     */
    protected List<String> temporaryGridPointList = new ArrayList<String>();

    //MODIFICA 23.08 mappa invece di lista di Strip
    protected TreeMap<String, Strip> temporaryStrip  = new TreeMap<String, Strip>() ;

    /**
     * number of new point covered
     */
    protected int numberOfNewPoint =0;

    /**
     * temporary DTO map
     */
    // private Map<Strip, DTO> teporaryDTOmap = new TreeMap<Strip, DTO>();

    /**
     * Return f AR for the current iteration null if no strip holds the point
     *
     * @param selectedPoint
     * @param alreadyFoundPointList
     * @return found AR null if none
     */
    public AcqReq iterate(final GridPoint selectedPoint, List<String> alreadyFoundPointList)

    {
        // logger.debug("Inner Iteration");

        /**
         * Seaching for string holding the point
         */
        List<Strip> listOfStripHoldingThePoint = findStripHoldingPoint(selectedPoint);

        /**
         * if no strio
         */
        if (listOfStripHoldingThePoint.isEmpty())
        {
            /**
             * strip found remove point from temporary return null
             */
            String univocalKeyGp= selectedPoint.getUnivocalKey();
            this.temporaryGridPointList.remove(univocalKeyGp); // the point is
                                                               // not accessible
            return null;
        }

        // logger.debug("Found list of strip holding the point, list size :\n "
        // +listOfStripHoldingThePoint.size() );
        /**
         * search optimal strip
         */
        Strip optimizedStrip = findOptimalStrip(listOfStripHoldingThePoint, alreadyFoundPointList);

        /**
         * number of new point covered by the strip
         */
        this.numberOfNewPoint = getNumberOfNewPoint(optimizedStrip, alreadyFoundPointList).size();
        logger.trace("MODIFICA selectd gridPoint :" + selectedPoint.getId()+" coordinates : "+selectedPoint.getLLH());

        logger.trace("MODIFICA for optimized strip, the number of new points are :" + this.numberOfNewPoint);

        /**
         * Building ar
         */
        AcqReq ar = new AcqReq();
        DTO dto;

        // DTO dto= new DTO(optimizedStrip);
        // USO SOLO I nuovi accessi, questo riduce la probabilità di usare strip
        // più lunghe del dovuto
        if (this.haveUseOnlyNewAccesses)
        {
            // this.tracer.debug("Using only new accesses in DTO");
            // list of new access
            List<Access> newAccessFound = new ArrayList<>();
            // iterating on accesses
            for (Access a : optimizedStrip.getStillUsableAccessList())
            {
                if (!alreadyFoundPointList.contains(a.getGridPoint().getUnivocalKey()))
                {
                    // if the access is new add to list
                    newAccessFound.add(a);
                }
            }

            //
            List<Access> accessInsideStrip = new ArrayList<>();

            /**
             * Setting threshold used to find accesses falling inside DTO
             */
            double startTime = newAccessFound.get(0).getAccessTime();
            double stopTime = newAccessFound.get(newAccessFound.size() - 1).getAccessTime();
            double currentTime = 0;
            for (Access a : optimizedStrip.getStillUsableAccessList())
            {
                currentTime = a.getAccessTime();
                // if access inside interval add access
                // to list of access insede strip
                if ((currentTime >= startTime) && (currentTime <= stopTime))
                {
                    accessInsideStrip.add(a);
                }
            }

            // Strip usefulStrip = new Strip(optimizedStrip.getId(),
            // newAccessFound);
            //////////////////////////////////////////
            /**
             * This is the strip useful
             */
            Strip usefulStrip = new Strip(optimizedStrip.getId(), accessInsideStrip);
            /**
             * Builo the DTO using only usefull data
             */
            dto = new DTO(usefulStrip);
        }
        else
        {

            /**
             * we use all the access consider to change configuration
             */
            dto = new DTO(optimizedStrip);
        }

        logger.trace("MODIFICA SCELTO DTO :" + optimizedStrip);

        /**
         * Adding DTO to AR
         */
        ar.addDTO(dto);
        ar.setMission(optimizedStrip.getAccessList().get(0).getMissionName());

        // Eliminare tutte le strip dello stesso satellite che overlappano la
        // OPTIMIZED STRIP e che non sono Optimized strip
        /**
         * Remove all data conflicting with the current DTO
         */
        removeOverlappingStrip(dto);

        /**
         * Add new point to alreadyFoundPointList and remove from temporary grid
         */
        GridPoint p;
        logger.trace("MODIFICA SCELTO DTO _ ACCESS LIST:" + dto.getDtoAccessList().size());
		logger.debug("from iterate");

        for (Access a : dto.getDtoAccessList())

        {
            p = a.getGridPoint();
            // new points are included into the total covered points
            if (!alreadyFoundPointList.contains(p.getUnivocalKey()))
            {
                alreadyFoundPointList.add(p.getUnivocalKey());
            }

            String univocalKeyGp= p.getUnivocalKey();

            // if inside the map of points that must be covered there is the
            // current point
            if (this.temporaryGridPointList.contains(univocalKeyGp))
            {
                // remove it from map because now it is covered
                this.temporaryGridPointList.remove(univocalKeyGp);
            }

        } // end for

        logger.trace("MODIFICA temporaryGridPointList updated:" + this.temporaryGridPointList.size());

        return ar;
    }// end iterate

    /**
     *
     * Recalculate the useufl accesses for the strips in temporary strip more
     * over remove all the strips with not usable accesses from temporary strip
     * list
     *
     * @param optimalStrip
     *
     *
     */
    private void removeOverlappingStrip(DTO dto)
    {
        /**
         * List of strips to be removed from temporary list
         */
        List<Strip> toBeRemovedStrip = new ArrayList<>();

        double dtoStart = dto.getStartTime();
        double dtoStop = dto.getStopTime();
        String dtoSatName = dto.getSatName();
        // Satellite sat = dto.getSat();
        // long orbitID = optimalStrip.getAccessList().get(0).getOrbitId();

        double currentStripStartTime;
        double currentStripStopTime;
        /**
         * Iterating on strips
         */
        for (String stripId : this.temporaryStripList)
        {
            
            Strip s = this.temporaryStrip.get(stripId);
            /**
             * Different satellite no conflict continue
             */
            if (!s.getSatelliteId().equals(dtoSatName))
            {
                continue;
            }

            // You can use the orbit ID nevertheless is not very safe
            // if(s.getAccessList().get(0).getOrbitId()==orbitID)
            // {
            // toBeRemovedStrip.add(s);
            // }

            currentStripStartTime = s.getStartTime();
            currentStripStopTime = s.getStopTime();

            // if(((dtoStart - currentStripStopTime) >
            // dtoSat.getSensorRestoreTime()) ||
            // ((currentStripStartTime - dtoStop) >dtoSat.getSensorRestoreTime()
            // ) )

            // if(((dtoStart - currentStripStopTime) > dtoSat.getTreshold()) ||
            // ((currentStripStartTime - dtoStop) >dtoSat.getTreshold() ) )
            if (((dtoStart - currentStripStopTime) > FeasibilityConstants.ManouvreTolerance) || ((currentStripStartTime - dtoStop) > FeasibilityConstants.ManouvreTolerance))
            {
                /**
                 * No overlap continue
                 */
                continue;
            }
            else
            {
                logger.debug("Remove overlapping DTO");

                // this.tracer.debug("Remove overlapping DTO");
                // toBeRemovedStrip.add(s);
                /**
                 * Overlap we must perform more operation
                 */
                
                removeNotUsableAccess(dto, s, toBeRemovedStrip);

            }

        } // end for

        // remove not useable strip
        for (Strip s : toBeRemovedStrip)
        {
            /**
             * Removing strips with no usable access from temporary trip list
             */
            this.temporaryStripList.remove(s.getUnivocalKey());
            this.temporaryStrip.remove(s.getUnivocalKey());

        } // end for

    }// endremoveOverlappingStrip

    /**
     * Remove accesses from usable acceses of the strip
     *
     * @param dto
     * @param strip
     * @param toBeRemovedStrip
     */
    private void removeNotUsableAccess(DTO dto, Strip strip, List<Strip> toBeRemovedStrip)
    {
        /**
         * List of the still usable accesses
         */
        ArrayList<Access> newStillUsableAccessList = new ArrayList<>();

        // double lowerLimit = dto.getStartTime() -
        // dto.getSat().getSensorRestoreTime()-(0.5*dto.getSat().getStripMapMinimalDuration());
        /**
         *
         */
        double upperLimit = dto.getStopTime() + dto.getSat().getTreshold();// dto.getSat().getSensorRestoreTime()
                                                                           // +(0.5*dto.getSat().getStripMapMinimalDuration());

        double currentTime = 0;
        /**
         * If they are on the same look side then we can still use the strip
         * otherwise the strip is not usable because of the manovre
         */
        if (dto.getLookSide() == strip.getAccessList().get(0).getLookSide())
        {
            /**
             * for each access
             */
            for (Access a : strip.getStillUsableAccessList())
            {
                currentTime = a.getAccessTime();

                //MODIFICA 21.08 dovrebbe ssere ripristinato il controllo più stringente anche sul lower limit, no?
                // if(currentTime >=lowerLimit && currentTime <=upperLimit)
                if (currentTime <= upperLimit)
                {
                    // below limit not usable
                    continue;
                } // end if
                else
                {
                    // above limit usable
                    newStillUsableAccessList.add(a);
                } // end else
            } // end for
        } // end if
//       logger.debug("SETTING the still usable access " +strip.getStillUsableAccessList().size());

        /**
         * setting the list to the new one
         */
        strip.setStillUsableAccessList(newStillUsableAccessList);
//        logger.debug("SETTIED the still usable access" +strip.getStillUsableAccessList().size());
        if (newStillUsableAccessList.size() == 0)
        {
            /**
             * Usable list is empty the strip must be removed
             */
            toBeRemovedStrip.add(strip);
        } // end if

    }// end methoid

    /**
     *
     * @param s
     * @return the number of point intercepted
     */
    protected int getNumberOfPoint(Strip s)
    {

        
        return s.getStillUsableAccessList().size();
    }// end method

    /**
     * Find the optimal strip in a list
     *
     * @param list
     * @param alreadyFoundGridListPoint
     * @return the optimal strip
     */
    protected Strip findOptimalStrip(final List<Strip> list, List<String> alreadyFoundGridListPoint)
    {
        // Francesca : capire valori di costo funzione
        // logger.debug("Finding optimal strip");
        /**
         * retval
         */
        Strip retval = null;

        /**
         * Objective function to be maximized to evaluate the optimal
         */
        ObjectiveFunction objectiveFunction = new ObjectiveFunction(this.P1, this.P2, this.P3);

        /**
         * evaluate min start time time in stip list
         */
        double tMax = findTmaxInStripList(list);
        logger.trace("MODIFICA : tMax global   :  " + tMax);
        logger.trace("MODIFICA : tMax global as DATE  :  " + com.telespazio.csg.srpf.utils.DateUtils.fromCSKDateToDateTime(tMax));

        /**
         * element of objective function
         */
        /**
         * newIsBetter / numberOfPoint
         */
        double newTotalInstripRatio = 0;

        /**
         * number of new point intercepted
         */
        double newIsBetter = 0.0;

        logger.trace("MODIFICA strip total size : " + list.size());

        /**
         * 1 - start/tstartMax
         */
        double firstIsBetter = 0.0;
        int i = 0;
        
        //TODO : inserire qui prima del fot la soluzione min e max di ogni fattore!
        
        computeMinMaxValue(list,alreadyFoundGridListPoint,objectiveFunction,tMax);
        for (Strip s : list)
        {

            logger.trace("MODIFICA : startTime current strip :  " + s.getStartTime());
            logger.trace("MODIFICA : stopTime current strip :  " + s.getStopTime());

            i++;
            logger.trace("MODIFICA Objective value for strip " + s.getId() + " is: ");
            logger.trace("MODIFICA strip getAccessList size : " + s.getAccessList().size());

            // newTotalInstripRatio =
            // (double)(getNumberOfPoint(s))/gridPointList.size();

            if (alreadyFoundGridListPoint.size() == 0)
            {
                logger.trace("MODIFICA : first iteration (" + i + ")");

                /**
                 * all points are new
                 */
                newTotalInstripRatio = 1.0;
                newIsBetter = getNumberOfPoint(s);
                logger.trace("MODIFICA : newIsBetter " + newIsBetter);
                logger.trace("MODIFICA : newTotalInstripRatio " + newTotalInstripRatio);

            }
            else
            {

                logger.trace("MODIFICA : iteration " + i);

                // newIsBetter =
                // (double)(getNumberOfNewPoint(s,alreadyFoundGridListPoint))/alreadyFoundGridListPoint.size();

                newIsBetter = (getNumberOfNewPoint(s, alreadyFoundGridListPoint)).size();
                logger.trace("MODIFICA : newIsBetter " + newIsBetter);

                newTotalInstripRatio = newIsBetter / getNumberOfPoint(s);
                logger.trace("MODIFICA : newTotalInstripRatio " + newTotalInstripRatio);

            }

            // long startLong = System.currentTimeMillis() + ((long) (tMax *
            // 1000));

            firstIsBetter = 1 - (s.getStartTime() / tMax);
            logger.trace("MODIFICA : firstIsBetter " + firstIsBetter);

            /**
             * adding current value
             */
            objectiveFunction.addValuteAt(newTotalInstripRatio, newIsBetter, firstIsBetter, s);

            logger.trace("MODIFICA : alreadyFoundGridListPoint " + alreadyFoundGridListPoint.size());

            logger.trace("MODIFICA :  s: " + s.getId() + " n1: " + newTotalInstripRatio + " n2: " + newIsBetter + " n3: " + firstIsBetter);
            logger.trace("MODIFICA : s: " + s.getId() + " t1: " + s.getAccessList().size() + " t2: " + getNumberOfNewPoint(s, alreadyFoundGridListPoint) + " n3: " + (tMax - s.getStartTime()));
            logger.trace("MODIFICA : objectiveFunction " + objectiveFunction.getValue(s));

        } // end for

        // logger.warn("Value for optimized function is :");

        /**
         * return strip maximizing the function
         */
        retval = objectiveFunction.getOptimizedStrip(list);

        logger.debug("Value for optimized function is :" + objectiveFunction.getValue(retval));

        return retval;
    }// end findOptimalStrip

    private void computeMinMaxValue(List<Strip> list, List<String> alreadyFoundGridListPoint, ObjectiveFunction objectiveFunction, double tMax)
    {
        int newIsBetter=0;
        double firstIsBetter=0;
        double newTotalInstripRatio=0;
        for (Strip s : list)
        {
            // newTotalInstripRatio =
            // (double)(getNumberOfPoint(s))/gridPointList.size();

            if (alreadyFoundGridListPoint.size() == 0)
            {

                /**
                 * all points are new
                 */
                newTotalInstripRatio = 1.0;
                newIsBetter = getNumberOfPoint(s);
            }
            else
            {
                newIsBetter = (getNumberOfNewPoint(s, alreadyFoundGridListPoint)).size();
   
                newTotalInstripRatio = newIsBetter / getNumberOfPoint(s);

            }

            // long startLong = System.currentTimeMillis() + ((long) (tMax *
            // 1000));

            firstIsBetter = 1 - (s.getStartTime() / tMax);
            /**
             * adding current value
             */
            objectiveFunction.addValuteAt(newTotalInstripRatio, newIsBetter, firstIsBetter, s);
} // end for

    }

    /**
     * Return the number of new point incercepted by the strip
     *
     * @param s
     * @param alreadyFoundPointList
     * @return numbero of new point
     */
    protected List<GridPoint> getNumberOfNewPoint(final Strip s, final List<String> alreadyFoundPointList)
    {
        List<GridPoint> newPoint = new ArrayList<GridPoint>();
        logger.trace("MODIFICA getNumberOfPoint per STRIP");

        //int retval = 0;

        if(s.getStillUsableAccessForSpotlight()!=null && !s.getStillUsableAccessForSpotlight().isEmpty())
        {
            for (Access a : s.getStillUsableAccessForSpotlight())
            {
                /**
                 * for each access evaluated if the grid point is inside the
                 * alreadyFoundPointList if not retval = retval +1
                 */
                if (!alreadyFoundPointList.contains(a.getGridPoint().getUnivocalKey()))
                {
                    newPoint.add(a.getGridPoint());
                    //retval++;
                }
            } // end for
        }
        else
        {
            for (Access a : s.getStillUsableAccessList())
            {
                /**
                 * for each access evaluated if the grid point is inside the
                 * alreadyFoundPointList if not retval = retval +1
                 */
                if (!alreadyFoundPointList.contains(a.getGridPoint().getUnivocalKey()))
                {
                    //retval++;
                    newPoint.add(a.getGridPoint());

                }
            } // end for
        }


        return newPoint;

    }// end getNumberOfNewPoint

    /**
     * find the max startTime in the strip list
     *
     * @param stripList
     * @return macx start time
     */
    protected double findTmaxInStripList(List<Strip> stripList)
    {
        // logger.warn("Finding tmax in list of size: " );
        // logger.debug(stripList.size());
        double retval = 0;

        boolean isFirst = true;

        for (Strip s : stripList)
        {
            if (isFirst)
            {
                /**
                 * for the first access tmax is starttime
                 */
                isFirst = false;

                retval = s.getStartTime();
//                logger.trace("MODIFICA : isFirst = true, first element of list  :  " + retval);

            }
            else
            {
                /**
                 * Switching if retval is less then starttime
                 */
                if (retval < s.getStartTime())
                {
                    retval = s.getStartTime();
//                    logger.trace("MODIFICA : found an higher time  :  " + retval);

                }
            }
        } // end For
//        logger.debug("found tmax: " + retval);
        return retval;
    } // end findTmaxInStripList

    /**
     * Find the list of strip holding the point
     *
     * @param p
     * @return list ho strips holding the point
     */
    protected List<Strip> findStripHoldingPoint(GridPoint p)
    {
        List<Strip> list = new ArrayList<>();
        for (String stripId : this.temporaryStripList)
        {
            
            Strip s = this.temporaryStrip.get(stripId);
            /**
             * Add strip to list if contains point
             */
            if (s.containsPoint(p))
            {
                // logger.warn("Found strip " + s.getId());
                list.add(s);
            }
        } // end for

        return list;
    }// end findStripHoldingPoint

    InnerOptimizationLoopIterator()
    {

    }

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
    InnerOptimizationLoopIterator(double p1, double p2, double p3, final List<String> gridPointList, List<String> temporaryGridPointList, List<Strip> temporaryStripList

    )
    {

        /**
         * initialiting weight
         */
        this.P1 = p1;
        this.P2 = p2;
        this.P3 = p3;
        
        for(int i=0;i<temporaryStripList.size();i++)
        {
            this.temporaryStripList.add(temporaryStripList.get(i).getUnivocalKey());
            this.temporaryStrip.put(temporaryStripList.get(i).getUnivocalKey(),temporaryStripList.get(i));
        }
        this.gridPointList = gridPointList;
        this.temporaryGridPointList = temporaryGridPointList;

        /**
         * Initializinng configuration
         */
        String value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.HAVE_USE_ONLY_NEW_ACCESS_CONF_KEY);
        if (value != null)
        {
            try
            {
                // HAVE_USE_ONLY_NEW_ACCESS_CONF_KEY is a boolean variable that
                // allow to use only new access (=1) or not (=0)
                double iValue = Integer.valueOf(value);
                if (iValue == 0)
                {
                    this.haveUseOnlyNewAccesses = false;
                }
                else if (iValue == 1)
                {

                    this.haveUseOnlyNewAccesses = true;
                }

            }
            catch (Exception e)
            {
                /**
                 * No Parameter in configuration using default
                 */
                // logger.warn("Unable to found " +
                // FeasibilityConstants.MAX_AREA_OF_INTEREST_CONF_KEY + " in
                // conffiguration");
                InnerOptimizationLoopIterator.logger.error(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.HAVE_USE_ONLY_NEW_ACCESS_CONF_KEY + " in configuration");

            }

        } // end if
        else
        {
            /**
             * Misconfigured parameter using default
             */
            // logger.warn("Unable to found " +
            // FeasibilityConstants.MAX_AREA_OF_INTEREST_CONF_KEY + " in
            // conffiguration");
            InnerOptimizationLoopIterator.logger.error(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.HAVE_USE_ONLY_NEW_ACCESS_CONF_KEY + " in configuration");

        } // end else

    }// end method

    /**
     *
     * @return number of new point
     */
    public int getNumberOfNewPoint()
    {
        // new point
        return this.numberOfNewPoint;
    }// end method
}// end InnerOptimizationLoopIteratorStripMode
