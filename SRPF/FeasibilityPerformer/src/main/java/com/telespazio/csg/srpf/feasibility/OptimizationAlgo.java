/**
*
* MODULE FILE NAME:	InnerOptimizationLoopIterator.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			This class implemnets the Algorithm to found the optimized solution
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
* 11-07-2016 | Amedeo Bancone  |1.1| modified checkIfSingleAcquirableSpotLightCase to check if DTO is good
* 									 added guard on the maxNumer of iteration
* 									 added the capability to perform repeatible flip run
* --------------------------+------------+----------------+-------------------------------
*
*
* PROCESSING
*/

package com.telespazio.csg.srpf.feasibility;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.NumericUtils;
import com.telespazio.csg.srpf.utils.PropertiesReader;

/**
 * This class implemnets the Algorithm to found the optimized solution
 *
 * @author Amedeo Bancone
 * @version 1.1
 *
 *
 */
public class OptimizationAlgo implements OptimizationAlgoInterface

{

    // static final Logger logger =
    // LogManager.getLogger(OptimizationAlgo.class.getName());
    final Logger logger = LogManager.getLogger(FeasibilityPerformer.class.getName());

    /**
     * logger
     */
    private TraceManager tracer = new TraceManager();

    /**
     * gridpoint list
     */
    private List<String> gridPointList = new ArrayList<String>();
    private TreeMap<String,GridPoint> gridPointMap = new TreeMap<String,GridPoint>();
    /**
     * strip list
     */
    private List<Strip> stripList = new ArrayList<>();

    /**
     * list of outer iteration
     */
    private List<OuterOptimizationLoopIteration> outerIterationList = new ArrayList<>();

    // Coefficient for the objective function (Inner LOOP)
    /**
     * first coefficient (new/total)
     */
    private double IP1;
    /**
     * second coefficient (new)
     */
    private double IP2;
    /**
     * third coefficient (first is better)
     */
    private double IP3;

    // Coefficient for the objective function (Outer LOOP)
    /**
     * first coefficient it multiply coverage
     */
    private double OP1;
    /**
     * second coefficient it multiply (1 - NumerOfAR/MaxNumberOfAR)
     */
    private double OP2;
    /**
     * third coefficient it multiply (1 - duration/maxduration)
     */
    private double OP3;

    /**
     * gridder
     */
    private Gridder gridder = null;

    /**
     * index of the optimal solution
     */
    private int optimalIndex = -1;

    /**
     * flag for spotlight
     */
    private boolean isSpotLight = false;

    /**
     * this flag states if the request can be acquired by only one AcqReq
     */
    private boolean isSingleAcq = false;
    /**
     * list of satellite
     */
    private List<Satellite> satList;

    /**
     * used to build a pseudocasual distibution
     */
    private Random randomGenerator = null;
    /**
     * flag for using repeatible run
     */
    private boolean useRepeatibleRun = false;

    /**
     * t his flag states if the centroid must to be used as leading point in the
     * flip. Only in the first flip of the first iteration shall be used and
     * only in case of the centroid is internal to the area of interest
     */
    private boolean haveCheckForCentroid = false;

    /**
     * numneber of uncovered points
     */
    private int uncoveredNumberOfPoints = 0;

    /**
     * Return true if the PR as only one AcqReq
     *
     * @return true if the request has only one AcqReq
     */
    @Override
    public boolean isSingleAcquired()
    {
        return this.isSingleAcq;
    }// end method

    /**
     *
     * @param accessList
     * @param gridPointList
     * @param gridder
     * @param isSpotLight
     * @param OP1
     * @param OP2
     * @param OP3
     */
    public OptimizationAlgo(final List<Satellite> satList, final List<GridPoint> gridPointList, Gridder gridder, boolean isSpotLight, double OP1, double OP2, double OP3)

    {
        /**
         * Initializing value
         */
        for(int i=0;i<gridPointList.size();i++)
        {
            this.gridPointList.add(gridPointList.get(i).getUnivocalKey());
            this.gridPointMap.put(gridPointList.get(i).getUnivocalKey(), gridPointList.get(i));
        }

        this.OP1 = FeasibilityConstants.OP1;
        this.OP2 = FeasibilityConstants.OP2;
        this.OP3 = FeasibilityConstants.OP3;

        this.IP1 = FeasibilityConstants.IP1;
        this.IP2 = FeasibilityConstants.IP2;
        this.IP3 = FeasibilityConstants.IP3;

        this.gridder = gridder;
        this.isSpotLight = isSpotLight;

        this.satList = satList;
        this.useRepeatibleRun = false;

        /**
         * reading configuration
         */
        initializeCoefficient();

        /*
         * this.IP1=IP1; this.IP2=IP2; this.IP3=IP3;
         */
    }// end method

    /**
     * Initialize
     */
    private void initializeCoefficient()
    {
        /**
         * reaading property
         */
        String value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.OP1_CONF_KEY);
        if (value != null)
        {
            try
            {
                double dValue = Double.valueOf(value);
                this.OP1 = dValue;

            }
            catch (Exception e)
            {
                /**
                 * misconfigured using default
                 */
                this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.OP1_CONF_KEY + " in configuration");
            }

        }
        else
        {
            /**
             * not configured using default
             */
            this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.OP1_CONF_KEY + " in configuration");
        }
        /**
         * reaading property
         */
        value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.OP2_CONF_KEY);
        if (value != null)
        {
            try
            {
                double dValue = Double.valueOf(value);
                this.OP2 = dValue;

            }
            catch (Exception e)
            {
                /**
                 * misconfigured using default
                 */
                this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.OP2_CONF_KEY + " in configuration");
            }

        }
        else
        {
            /**
             * not configured using default
             */
            this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.OP2_CONF_KEY + " in configuration");
        }
        /**
         * reaading property
         */
        value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.OP3_CONF_KEY);
        if (value != null)
        {
            try
            {
                double dValue = Double.valueOf(value);
                this.OP3 = dValue;

            }
            catch (Exception e)
            {
                /**
                 * misconfigured using default
                 */
                this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.OP3_CONF_KEY + " in configuration");
            }

        }
        else
        {
            /**
             * not configured using default
             */
            this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.OP3_CONF_KEY + " in configuration");
        }

        /**
         * reaading property
         */
        value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.IP1_CONF_KEY);
        if (value != null)
        {
            try
            {
                double dValue = Double.valueOf(value);
                this.IP1 = dValue;

            }
            catch (Exception e)
            {
                /**
                 * misconfigured using default
                 */
                this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.IP1_CONF_KEY + " in configuration");
            }

        }
        else
        {
            /**
             * not configured using default
             */
            this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.IP1_CONF_KEY + " in configuration");
        }

        /**
         * reaading property
         */
        value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.IP2_CONF_KEY);
        if (value != null)
        {
            try
            {
                double dValue = Double.valueOf(value);
                this.IP2 = dValue;

            }
            catch (Exception e)
            {
                /**
                 * misconfigured using default
                 */
                this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.IP2_CONF_KEY + " in configuration");
            }

        }
        else
        {
            /**
             * not configured using default
             */
            this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.IP2_CONF_KEY + " in configuration");
        }
        /**
         * reaading property
         */
        value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.IP3_CONF_KEY);
        if (value != null)
        {
            try
            {
                double dValue = Double.valueOf(value);
                this.IP3 = dValue;

            }
            catch (Exception e)
            {
                /**
                 * misconfigured using default
                 */
                this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.IP3_CONF_KEY + " in configuration");
            }

        }
        else
        {
            /**
             * not configured using default
             */
            this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.IP3_CONF_KEY + " in configuration");
        }

        /**
         * reaading property
         */
        value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.USE_REPEATIBLE_RUN_CONF_KEY);
        if (value != null)
        {
            try
            {
                int iValue = Integer.valueOf(value);
                if (iValue != 0)
                {
                    this.useRepeatibleRun = true;
                }

            }
            catch (Exception e)
            {
                /**
                 * misconfigured using default
                 */
                this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.USE_REPEATIBLE_RUN_CONF_KEY + " in configuration");
            }

        }
        else
        {
            /**
             * not configured using default
             */
            this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.USE_REPEATIBLE_RUN_CONF_KEY + " in configuration");
        }

    }// end method

    /**
     * Create all the possible strip
     */
    public void generateStripsList()

    {
        this.logger.debug("Evaluating strip");
        /**
         * clering strip
         */
        this.stripList.clear();

        for (Satellite s : this.satList)
        {
            /**
             * for each satellite generate strips
             */
            s.generateStrips();

            for (Strip strip : s.getStripList())
            {
                /**
                 * for each strip evaluating id and adding to strip list
                 */
                strip.setId(this.stripList.size() + 1);

                this.stripList.add(strip);
            } // end for
        } // end for

        this.logger.debug("Found " + this.stripList.size() + " strips");

        // dumpStripToFile("/home/amedeo/TESTDATA/strips.txt");

    }// end generateStrips

    /**
     * perform numberOfIteration for the optimization outer loop
     *
     * @param numberOfIteration
     * @param P1
     *            coefficient for the first term for the target function
     * @param P2
     *            coefficient for the second term for the target function
     * @param P3
     *            coefficient for the third term for the target function
     * @throws Exception 
     */
    public void performOptimizationLoop(final int numberOfIteration, final double P1, final double P2, final double P3) throws Exception

    {
        /**
         * initializing coefficient
         */
        this.IP1 = P1;
        this.IP2 = P2;
        this.IP3 = P3;

        /**
         * current outer iteration
         */
        OuterOptimizationLoopIteration currentOuterIter;
        /**
         * check if single acquisition
         */
        AcqReq acq = checkIfSingleAcquirable();

        if (acq != null)
        {
            /**
             * single acquisition
             */
            this.logger.debug("PR single acquisition");
            /**
             * build AR List
             */
            List<AcqReq> list = new ArrayList<>();
            list.add(acq);
            /**
             * Build iteration
             */
            currentOuterIter = new OuterOptimizationLoopIteration(list, 1.0, 0);
            this.outerIterationList.add(currentOuterIter);
            /**
             * setting flag to true
             */
            this.isSingleAcq = true;
            /**
             * no more operation needed return
             */
            return;
        } // end if

        if (this.isSpotLight)
        {
            /**
             * case spotlight we searching for centroid If exist the centroid is
             * in the last position of the grid list
             */
            String univGridPintKey = this.gridPointList.get(this.gridPointList.size() - 1); //
            GridPoint p = fromStringToGridPoint(univGridPintKey,this.gridPointMap);
            logger.debug("GRID POINT LAST POSITION "+p);
            logger.debug("GRID POINT isCentroid "+p.isCentroid());

            if (p.isCentroid())
            {
                if (p.isInternal())
                {
                    logger.debug("IS INTERNAL");

                    /**
                     * In this case we have check for centroid
                     */
                    this.haveCheckForCentroid = true;

                    // //System.out.println("
                    // =====================CENTROIDE=====================: " +
                    // p.getLLH()[0] + " " + p.getLLH()[1]);
                }
                else
                {
                    /**
                     * external centroid we have to remove it form gridpoint
                     * list
                     */
                    logger.debug("IS EXTERNAL. REMOVE");
                    logger.debug("this.gridPointList SIZE "+this.gridPointList.size());

                    this.gridPointList.remove(p.getUnivocalKey());
                    logger.debug("this.gridPointList SIZE"+this.gridPointList.size());

                }
            } // end if

        } // end if

        /**
         * performing outer iterations
         */
        for (int i = 0; i < numberOfIteration; i++)
        {
            this.logger.debug("NEW ITERATION : "+i);

            long seed = i;
            if (!this.useRepeatibleRun)
            {
                /**
                 * evaluating seed for random generation repeatible
                 */
                seed = i + System.currentTimeMillis();
            } // end if
            else
            {
                /**
                 * evaluating seed for random generation pure random
                 */
                seed = i + (i * 200009);
            } // end else

            this.randomGenerator = new Random(seed);

            this.logger.debug("Performing outer iteration");

            /**
             * evaluating outer iteration
             */
            currentOuterIter = performOuterIteration();
            // logger.info("Adding outer iteration");
            /**
             * setting id adding to outer iteration list
             */
            currentOuterIter.setId(this.outerIterationList.size() + 1);
            this.outerIterationList.add(currentOuterIter);
            this.logger.debug("MODIFICA adding to the solutions's family : " + currentOuterIter.toString());
            

        } // end for

    }// end method

    private GridPoint fromStringToGridPoint(String univGridPintKey, TreeMap<String, GridPoint> gridPointMap2)
    {
        GridPoint gp = null;
        if(gridPointMap2.containsKey(univGridPintKey))
        {
            gp = gridPointMap2.get(univGridPintKey);
        }
        return gp ;
    }

    /**
     * perform numberOfIteration for the outer optimization loop. In this case
     * p1=p2=p3=1
     *
     * @param numberOfIteration
     */
    @Override
    public void performOptimizationLoop(final int numberOfIteration)
    {
        try
        {
            performOptimizationLoop(numberOfIteration, this.IP1, this.IP2, this.IP3);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }// end method

    /**
     * Return a strip that maximize the objective function in the current
     * subiteration
     *
     * @return outer iteration
     */

    private OuterOptimizationLoopIteration performOuterIteration()
    {

    	//TODO : 23062020 vedere con Ric
        /**
         * retval
         */
        OuterOptimizationLoopIteration retval = null;

        /**
         * the temporary grid point list to be used in the current iteration
         */
        List<String> temporaryGridPointList = new ArrayList<>();

        // temporaryGridPointList.addAll(this.gridPointList);
        
        logger.trace("MODIFICA 23.08 temporaryGridPointList _"+temporaryGridPointList.size());

        logger.trace("MODIFICA 23.08 GRID POINT LIST _"+this.gridPointList.size());
        /**
         * filling list
         */
        for (String p : this.gridPointList)
        {
            temporaryGridPointList.add(p);
        }

        logger.trace("MODIFICA 23.08 temporaryGridPointList _"+temporaryGridPointList.size());

        /**
         * temporary strip list to be used in the current iteration
         */
        List<Strip> temporaryStripList = new ArrayList<>();

        for (Strip s : this.stripList)
        {
            /**
             * filling list
             */
            s.resetStillUsableAccessList();
            temporaryStripList.add(s);
        }

        /**
         * grid point index on which perform inner iteration
         */
        int pointIndex;
        GridPoint currentGridPoint;
        int numberOfpointInbncluded = 0;
        AcqReq currentAcq;
        /**
         * list of already found point
         */
        List<String> alreadyFoundPointList = new ArrayList<>();
        /**
         * list of acquisition
         */
        List<AcqReq> iterationAcqList = new ArrayList<>();

        /**
         * inner iteration
         */
        InnerOptimizationLoopIterator iteration;

        if (this.isSpotLight)
        {
            /**
             * case spotlighT
             */
            iteration = new InnerOptimizationIterationLoopSpotlight(this.IP1, this.IP2, this.IP3, this.gridPointList, temporaryGridPointList, temporaryStripList);
        }
        else
        {
            /**
             * strip mode
             */
            iteration = new InnerOptimizationLoopIterator(this.IP1, this.IP2, this.IP3, this.gridPointList, temporaryGridPointList, temporaryStripList);
        }

        // If we have points in the grids an strips

        // logger.debug("temporaryGridPointList size: " +
        // temporaryGridPointList.size());
        // logger.debug("temporaryStripList size: " +
        // temporaryStripList.size());

        /**
         * in the worst case the number of inner iteration equals the number of
         * point in the grid
         */
        int maxNumOfIteration = temporaryGridPointList.size();

        /**
         * performing inner iteration. until we have point in list we have strip
         * in list we doesn't overcome max iteration
         */
        while ((temporaryGridPointList.size() > 0) && (temporaryStripList.size() > 0) && (maxNumOfIteration > 0))
        {
            // SELECT randomly a pivot in the temporaryGridPointList
            // pointIndex =
            // NumericUtils.randomInteger(temporaryGridPointList.size()-1);
            // pointIndex=(int)(randomGenerator.nextDouble()*temporaryGridPointList.size());

            if (this.haveCheckForCentroid)
            {
                /**
                 * in case of spotlight with centroid only on the first outer
                 * iteration and at first flip we have to use the centroid .
                 */

                this.haveCheckForCentroid = false;
                /**
                 * the centroid is at the last point
                 */
                pointIndex = temporaryGridPointList.size() - 1;
                this.tracer.debug("Using centroid in flip");
            } // end if
            else
            {
                // TODO Francesca : invece di scegliere un punto random,
                // scegliere punto tra i residui che copre meglio l'area rimasta
                pointIndex = this.randomGenerator.nextInt(temporaryGridPointList.size());
                // pointIndex=(int)(randomGenerator.nextDouble()*temporaryGridPointList.size());
            } // end else

            // This should not happen. Anyway for the sake of security......
            if (pointIndex == temporaryGridPointList.size())
            {
                pointIndex--;
            }

            String univGridPintKey = temporaryGridPointList.get(pointIndex);

            currentGridPoint = fromStringToGridPoint(univGridPintKey, this.gridPointMap);
            
            logger.trace("SCELTO PUNTO : " + currentGridPoint);

            // logger.warn("Point index: " + pointIndex);
            /**
             * performing iteration
             */
            currentAcq = iteration.iterate(currentGridPoint, alreadyFoundPointList);
            /**
             * decrease numofiteration
             */
            maxNumOfIteration--;

            if (currentAcq == null)
            {
                /**
                 * selected point not accessed continue
                 */
                // logger.info("selected point not accessed");
                continue;
            } // end if

            logger.trace("MODIFICA numberOfpointInbncluded "+numberOfpointInbncluded);
            logger.trace("MODIFICA iteration.getNumberOfNewPoint() "+iteration.getNumberOfNewPoint());

            numberOfpointInbncluded = numberOfpointInbncluded + iteration.getNumberOfNewPoint();
            logger.trace("MODIFICA numberOfpointInbncluded "+numberOfpointInbncluded);

            /**
             * Increase by one the ACQ id and add ar to list
             */
            currentAcq.setId(Integer.toString(iterationAcqList.size() + 1));
            iterationAcqList.add(currentAcq);

        } // end while

        this.logger.debug("NUMBER OF TOTAL POINTS gridPointList.size() " + this.gridPointList.size());
        /**
         * coverage on number of point basis
         */
        double coverage = ((double) numberOfpointInbncluded) / this.gridPointList.size();

        if (this.gridder != null)
        {
            try
            {
                /**
                 * evaluating coverage
                 */
                coverage = this.gridder.getCoverage(iterationAcqList);
            }
            catch (GridException e)
            {
                this.tracer.warning(EventType.APPLICATION_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, " Error in evaluating coverage during optimization phase");
                coverage = ((double) numberOfpointInbncluded) / this.gridPointList.size();
                this.logger.trace("MODIFICA : normalized coverage : " + coverage);
            }
        }
        else
        {
            /**
             * It could not happens
             */
            coverage = ((double) numberOfpointInbncluded) / this.gridPointList.size();
            this.logger.trace("MODIFICA ERROR : normalized coverage : " + coverage);

        }

        /**
         * id is given when inserting in list
         */
        retval = new OuterOptimizationLoopIteration(iterationAcqList, coverage, temporaryGridPointList.size());

        this.logger.trace("MODIFICA FINAL : retval in OuterIteration: " + retval);

        return retval;
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
        // logger.info("getOptimalAcqReqList");

        if (this.isSingleAcq)
        {
            /**
             * case single
             */
            // logger.info("single ACQ");
            this.optimalIndex = 0;
            this.uncoveredNumberOfPoints = 0;
            /**
             * no more operation we have return
             */
            return this.outerIterationList.get(0).getAcqReqList();
        } // end if

        if (this.outerIterationList.size() == 0)
        {
            // logger.warn("Empty optimization");
            /**
             * No solution we have return
             */
            this.uncoveredNumberOfPoints = this.gridPointList.size();
            return new ArrayList<>(); // return empty list request not
                                      // feasible
        } // end if

        /**
         * Searching for optimized iteration
         */

        double maxDuration = 0;
        double maxArNumber = 0;
        double maxCoverage = 0;

        OuterOptimizationLoopIteration itLoop = this.outerIterationList.get(0);
        /**
         * initializing parameters
         */
        maxDuration = itLoop.getDuration();
        maxArNumber = itLoop.getAcqNumber();
        maxCoverage = itLoop.getCoverage();
        double minCoverage = maxCoverage;
        double currentCoverage = minCoverage;

        /**
         * finding max
         */
        for (int i = 1; i < this.outerIterationList.size(); i++)
        {
            itLoop = this.outerIterationList.get(i);
            /**
             * searching for max duration and maxNumber of AR
             */
            if (maxDuration < itLoop.getDuration())
            {
                maxDuration = itLoop.getDuration();
            }
            if (maxArNumber < itLoop.getAcqNumber())
            {
                maxArNumber = itLoop.getAcqNumber();
            }

            currentCoverage = itLoop.getCoverage();
            /**
             * Searching for max coverage
             */
            if (maxCoverage < currentCoverage)
            {
                maxCoverage = itLoop.getCoverage();
            }
            else if (minCoverage > currentCoverage)
            {
                minCoverage = currentCoverage;
            }

        } // end for

        // logger.info("Checking for AR");

        if ((maxDuration == 0) || (maxArNumber == 0) || (maxCoverage == 0)) // No
                                                                            // ar
                                                                            // has
                                                                            // been
                                                                            // found
        {
            // logger.warn("NO AR FOUND");
            /**
             * No AR FOUND return
             */
            return new ArrayList<>();
        }

        /**
         * Finding optimal iteration searchin min and max for normaliztion
         * process
         */

        double firstMinValue = minCoverage;
        double firstMaxValue = maxCoverage;

        double secondMinValue = 0;
        double secondMaxValue = 0;

        double thirdMinValue = 0;
        double thirdMaxValue = 0;

        double appo = 0;

        if ((this.outerIterationList == null) && (this.outerIterationList.size() > 0))
        {
            appo = 1.0 - (this.outerIterationList.get(0).getAcqNumber() / maxArNumber);
            secondMaxValue = appo;
            secondMinValue = appo;

            appo = 1.0 - (this.outerIterationList.get(0).getDuration() / maxDuration);

            thirdMinValue = appo;
            thirdMaxValue = appo;
        }

        for (OuterOptimizationLoopIteration it : this.outerIterationList)
        {

            appo = 1.0 - (it.getAcqNumber() / maxArNumber);

            if (appo > secondMaxValue)
            {
                /**
                 * switch
                 */
                secondMaxValue = appo;
            }
            else if (appo < secondMinValue)
            {
                /**
                 * switch
                 */
                secondMinValue = appo;
            }

            appo = 1.0 - (it.getDuration() / maxDuration);

            if (appo > thirdMaxValue)
            {
                /**
                 * switch
                 */
                thirdMaxValue = appo;
            }
            else if (appo < thirdMinValue)
            {
                /**
                 * switch
                 */
                thirdMinValue = appo;
            }

        }

        this.optimalIndex = 0;
        itLoop = this.outerIterationList.get(0);

        this.logger.trace("MODIFICA : coverage max/min : " + firstMaxValue + "/" + firstMinValue);
        this.logger.trace("MODIFICA : acqNumber max/min : " + secondMaxValue + "/" + secondMinValue);
        this.logger.trace("MODIFICA : duration max/min : " + thirdMaxValue + "/" + thirdMinValue);

        // //System.out.println("==============Coverage: " +
        // itLoop.getCoverage());
        /**
         * Evaluate current value for outer cost function on first outer
         * iteration
         */
        double optimalValue = (this.OP1 * (NumericUtils.normalize(itLoop.getCoverage(), firstMaxValue, firstMinValue))) + (this.OP2 * (NumericUtils.normalize((1 - (itLoop.getAcqNumber() / maxArNumber)), secondMaxValue, secondMinValue))) + (this.OP3 * (NumericUtils.normalize((1 - (itLoop.getDuration() / maxDuration)), thirdMaxValue, thirdMinValue)));
        double currentValue = 0;

        /*
         * //System.out.println("==============Coverage: " + itLoop.getCoverage()
         * + " " + (NumericUtils.normalize(itLoop.getCoverage(), firstMaxValue,
         * firstMinValue)) + " " +
         * (NumericUtils.normalize((1-itLoop.getAcqNumber()/maxArNumber),
         * secondMaxValue,secondMinValue)) + " " +
         * (NumericUtils.normalize((1-itLoop.getDuration()/maxDuration),
         * thirdMaxValue,thirdMinValue)));
         *
         * //System.out.println("==============Value: " +optimalValue);
         */
        // logger.info("external opt function vale: " + optimalValue);
        this.logger.trace("MODIFICA :  GLOBAL firstMaxValue : " + firstMaxValue);
        this.logger.trace("MODIFICA :  GLOBAL firstMinValue : " + firstMinValue);
        this.logger.trace("MODIFICA :  GLOBAL secondMaxValue : " + secondMaxValue);
        this.logger.trace("MODIFICA :  GLOBAL secondMinValue : " + secondMinValue);
        this.logger.trace("MODIFICA :  GLOBAL thirdMaxValue : " + thirdMaxValue);
        this.logger.trace("MODIFICA :  GLOBAL thirdMinValue : " + thirdMinValue);
        this.logger.trace("MODIFICA :  GLOBAL maxArNumber : " + maxArNumber);
        this.logger.trace("MODIFICA :  GLOBAL maxDuration : " + maxDuration);

        this.logger.debug("MODIFICA : first solution : " + itLoop);
        this.logger.debug("MODIFICA : optimal value for first solution : " + optimalValue);

        for (int j = 1; j < this.outerIterationList.size(); j++)
        {

            itLoop = this.outerIterationList.get(j);
            this.logger.debug("\n\nMODIFICA :  solution : " + itLoop);

            /**
             * Evaluate current value for outer cost function on current
             * iteration
             */
            currentValue = (this.OP1 * (NumericUtils.normalize(itLoop.getCoverage(), firstMaxValue, firstMinValue))) + (this.OP2 * (NumericUtils.normalize((1 - (itLoop.getAcqNumber() / maxArNumber)), secondMaxValue, secondMinValue))) + (this.OP3 * (NumericUtils.normalize((1 - (itLoop.getDuration() / maxDuration)), thirdMaxValue, thirdMinValue)));
            this.logger.trace("MODIFICA : current value for solution : " + currentValue);

            // logger.info("external opt function vale: " + currentValue);

            if (currentValue > optimalValue)
            {
                /**
                 * if cost function increase value switch
                 */
                this.optimalIndex = j;
                optimalValue = currentValue;
            }
        } // end for

        this.logger.trace("MODIFICA : Returning optimal " + optimalValue);
        this.logger.trace("MODIFICA : Returning optimal SOLUTION " + this.outerIterationList.get(this.optimalIndex));

        // logger.info("Returning optimal");
        /**
         * Number of uncoverd point
         */
        this.uncoveredNumberOfPoints = this.outerIterationList.get(this.optimalIndex).getUncoveredNumberOfPoints();
        return this.outerIterationList.get(this.optimalIndex).getAcqReqList();

    }// end getOptimalAcqReqList

    /**
     * Check if the request is single acquireable and fill the relevant AR
     * return null otherwise
     *
     * @return The single AR null otherwise
     * @throws Exception 
     */
    private AcqReq checkIfSingleAcquirable() throws Exception
    {
        this.logger.debug("Checking if single acquirable");

        this.isSingleAcq = false;

        /**
         * case spotlight return case spotlight
         */
        if (this.isSpotLight)
        {
            return checkIfSingleAcquirableSpotLightCase();
        }

        // logger.info("Check if single");

        AcqReq acq = new AcqReq();

        for (Strip s : this.stripList)
        {
            // logger.info("Strip size: " + s.getAccessList().size());
            // logger.info("Access list size: " + this.gridPointList.size());

            DTO dto =null;

            Satellite sat = s.getAccessList().get(0).getSatellite();
            /**
             * single aqcuirable if all grid point are in covered by accesses of
             * a single strip with duration less than max duration in this case
             * we create a DTO and add it to AR
             */
            if ((s.getAccessList().size() == this.gridPointList.size()) && (s.duration() <= sat.getStripMapMaximalDuration()))
            {
                // logger.info("Is single");
                this.isSingleAcq = true;
                // logger.info("adding dto");
                dto = new DTO(s);
                acq.addDTO(dto);
                acq.setMission(sat.getMissionName());
                acq.setId("1");
                // logger.info("DTO Added dto");

            }
        } // end for

        if (!this.isSingleAcq)
        {
            acq = null;
        }

        return acq;
    } // end checkIfSingleAcquirable

    /**
     * Check if the request is single in case of spotlight
     *
     * @return the single AR null otherwise
     * @throws Exception 
     */
    private AcqReq checkIfSingleAcquirableSpotLightCase() throws Exception
    {
        AcqReq acq = new AcqReq();
        
        for (Strip s : this.stripList)
        {
            logger.debug("Modifica 14.11 : strip accessList cardinality :" +s.getAccessList().size());
            logger.debug("Modifica 14.11 : strip accessList cardinality :" +s.getAccessList());

            SpotLightDTO dto;
            GridPoint center = null;
            boolean haveEvaluateCenter = true;
            
            if (s.getAccessList().size() == this.gridPointList.size()) // could
                                                                       // be
                                                                       // single
                                                                       // acquisition
            {
                /**
                 * evaluating center
                 */
                if (haveEvaluateCenter)
                {
                    haveEvaluateCenter = false;
                    center = getCenter(s.getAccessList());

                }

                Access a = getAccessFromGroidPoint(s.getAccessList(), center);

                /**
                 * building dto
                 */
                dto = new SpotLightDTO(a, s);

                // Considero >= poichè considero il centro due volte

                /**
                 * Single Aqcruired if all the point in grid fall inside the DTO
                 */
                if ((dto.isGood() && (dto.getAccessesInsideSpotList().size() >= this.gridPointList.size()))) // Single
                                                                                                             // acquisition
                {
                    /**
                     * Single add it to AR
                     */
                    this.isSingleAcq = true;
                    acq.setMission(s.getAccessList().get(0).getMissionName());
                    acq.addDTO(dto);
                }
                logger.debug("Modifica 14.11 : strip singularely acquirable :" +s.getId());

            } // end if
            else
            {
                logger.debug("Modifica 14.11 : strip not singularely acquirable :" +s.getId());

            }
        } // end for

        if (acq.getDTOList().size() == 0)
        {
            /**
             * no single
             */
            acq = null;
        }
        else
        {
            this.logger.debug("Single Acquisition");
            /**
             * Setting mission and id
             */
            // acq.setMission(this.stripList.get(0).getAccessList().get(0).getMissionName());
            acq.setId("1");
        }

        return acq;

    }// end method

    /**
     * Return the GridPoint nearest the center of the point held in the access
     * list
     *
     * @param list
     * @return the GridPoint nearest the center of the grid
     */
    private GridPoint getCenter(List<Access> list)
    {

        /**
         * ECEF coordinate
         */
        double X = 0;
        double Y = 0;
        double Z = 0;

        /**
         * evalkuating mean point
         */
        for (Access a : list)
        {
            X = X + a.getGridPoint().getEcef().getX();
            Y = Y + a.getGridPoint().getEcef().getY();
            Z = Z + a.getGridPoint().getEcef().getZ();
        }

        X = X / list.size();
        Y = Y / list.size();
        Z = Z / list.size();
        /**
         * mean point
         */
        Vector3D center = new Vector3D(X, Y, Z);

        /**
         * evaluating distance of the first point
         */
        GridPoint p = list.get(0).getGridPoint();
        double distance = Math.abs(p.getEcef().distance(center));

        GridPoint currentPoint;
        double currentDistance;

        /**
         * for each point in list
         */
        for (int i = 1; i < list.size(); i++)
        {
            currentPoint = list.get(i).getGridPoint();
            if (currentPoint.isCentroid())
            {
                /**
                 * if the point is the centroid this is the center no more
                 * operatio
                 */
                p = currentPoint;
                this.tracer.debug("Found centroid in the strip");
                break;
            }
            currentDistance = Math.abs(currentPoint.getEcef().distance(center));
            if (currentDistance < distance)
            {
                /**
                 * if the point is near switch
                 */
                p = currentPoint;
                distance = currentDistance;
            }
        }

        return p;

    }// end getCenter

    /**
     * Return the access in the list related to the grid point p
     *
     * @param list
     * @param p
     * @return return the grid point null otherwise
     */
    private Access getAccessFromGroidPoint(List<Access> list, GridPoint p)
    {

        Access retval = null;
        for (Access a : list)
        {
            /**
             * Found point Have exit
             */
            if (a.getGridPoint().equals(p))
            {
                retval = a;
                break;
            } // end if
        } // end for
        return retval;

    } // end Access

    /*
     * private Access getAccessFromGroidPoint(List<Access> list, GridPoint p) {
     * if(p == null) return null; for (Access a : list) {
     * if(a.getGridPoint().equals(p)) return a; }//end for return null; } //end
     * Access
     */

    /**
     * For test purposes only
     *
     * @param filePath
     */
    /*
     * public void dumpStripToFile(String outFilePath) { try {
     * //logger.debug("Dumping to file: " + outFilePath); BufferedWriter out =
     * new BufferedWriter(new FileWriter(outFilePath));
     *
     *
     *
     * for(Strip currentStrip : stripList) { String accessString =
     * currentStrip.dumpToString(); out.write(accessString); out.write("\n");
     * out.write(
     * "====================================================================================================================\n"
     * ); }
     *
     * out.close();
     *
     * } catch (IOException e) { // TODO Auto-generated catch block
     * e.printStackTrace(); } }//end dumpStripToFile
     */

    /**
     * For test Purposes only do not use. return the index of the best outer
     * iteration
     *
     * @return
     */
    public int getOptimalIndex()
    {
        return this.optimalIndex;
    }// end method

    /**
     * For test purposes only
     *
     * @return
     */
    public List<OuterOptimizationLoopIteration> getOuterItetionList()
    {
        return this.outerIterationList;
    }// end method

    /**
     *
     * @return the number of grid point not covered
     */
    @Override
    public int getUncoveredNumberOfPoints()
    {
        return this.uncoveredNumberOfPoints;
    }// end method

}// end Class
