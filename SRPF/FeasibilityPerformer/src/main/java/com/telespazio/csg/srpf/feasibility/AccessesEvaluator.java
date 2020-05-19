/**
*
* MODULE FILE NAME:	AccessesEvaluator.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Class used to find accesses on grid point
*
* PURPOSE:			Perform Feasibility
*
* CREATION DATE:	15-12-2015
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
*    16-08-2016   |Amedeo Bancone |2.0 | Added check on paw ; added evaluateSatelliteAccesses for a single satellite; added check for passthrough
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.feasibility;

import java.util.List;
import java.util.TreeMap;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeForm;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionNewtonForm;
import org.apache.commons.math3.analysis.solvers.NewtonRaphsonSolver;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.telespazio.csg.srpf.dataManager.bean.BeamBean;
import com.telespazio.csg.srpf.dataManager.bean.EpochBean;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.DateUtils;
import com.telespazio.csg.srpf.utils.ReferenceFrameUtils;

/**
 * Evalute the accesses for a satellite list over a given list of point
 *
 * @author Amedeo Bancone
 * @version 2.0
 *
 *
 */
public class AccessesEvaluator

{

    static final Logger logger = LogManager.getLogger(AccessesEvaluator.class.getName());
    
    /**
     * static final Logger logger =
     * LogManager.getLogger(AccessesEvaluator.class.getName());
     *
     */
    TraceManager tracer = new TraceManager();

    /**
     * Grid point list
     */
    protected List<GridPoint> gridPointList;

    private TreeMap<String, GridPoint> gridPointMap = new TreeMap<String, GridPoint>();

    /**
     * Satellite list
     */
    protected List<Satellite> satelliteList;

    /**
     * pr request
     */
    protected PRRequestParameter pr;

    // sammple windows
    /**
     * First sampele
     */
    protected EpochBean s0;

    /**
     * second sample
     */
    protected EpochBean s1;

    /**
     * third sample
     */
    protected EpochBean s2;

    /**
     * fourth sample
     */
    protected EpochBean s3;

    // components along x of satellite velocity on a point
    /**
     * second sample vel
     */
    protected double dot1;

    /**
     * Third sample vel
     */
    protected double dot2;

    /**
     * minOffNadir
     */
    protected double minOffNadir;

    /**
     * maxOffNadir
     */
    protected double maxOffNadir;

    /**
     * number of samples to not be used at begin and at the end of timeline in
     * order to be sure to avoid possible issue on interpolation during the
     * sparc input preparation for feasibility
     *
     */
    protected int numberOfGuardSample = FeasibilityConstants.NumberOfGuardSample;

    // Solever
    protected double solverEps = FeasibilityConstants.SolvEps;

    /**
     * Retrun the number of samples to not be used at begin and at the end of
     * timeline in order to be sure to avoid possible issue on interpolation
     * during the sparc input preparation for feasibility
     *
     * @return the number of samples to not be used at begin and at the end of
     *         timeline in order to be sure to avoid possible issue on
     *         interpolation during the sparc input preparation for feasibility
     */
    public int getNumberOfGuardSample()
    {
        return this.numberOfGuardSample;
    }// end method

    /**
     * set the number of samples to not be used at begin and at the end of
     * timeline in order to be sure to avoid possible issue on interpolation
     * during the sparc input preparation for feasibility
     *
     * @param numberOfGuardSample
     */
    public void setNumberOfGuardSample(int numberOfGuardSample)
    {
        this.numberOfGuardSample = numberOfGuardSample;
    }// end method

    /**
     * Evaluate the satellite accesses for a given list of satellite. The
     * Accesses are added to the access list properties of the given satellite
     *
     * @param satelliteList
     * @param gridPointList
     * @param pr
     */
    public void evaluateSatelliteAccesses(final List<Satellite> satelliteList, final List<GridPoint> gridPointList, final PRRequestParameter pr)
    {

        this.satelliteList = satelliteList;
        this.gridPointList = gridPointList;
        logger.trace("MODIFICA NUMBER OF GRID POINT " + gridPointList.size());

        buildGridPointMap(gridPointList, gridPointMap);
        this.pr = pr;

        /**
         * Evaluating access for each satellite in the list
         */
        for (Satellite s : satelliteList)
        {
            evaluateSatelliteAccessEvents(s);
        } // end for

        // Access is implements also the comparator interface so it can be used
        // as Comparator

    } // end evaluateSatelliteAccesses

    private void buildGridPointMap(List<GridPoint> gridPointList2, TreeMap<String, GridPoint> gridPointMap2)
    {

        for (int i = 0; i < gridPointList2.size(); i++)
        {
            gridPointMap2.put(gridPointList2.get(i).getUnivocalKey(), gridPointList2.get(i));
        }
    }

    /**
     * Evaluate the satellite accesses for a given satellite. The Accesses are
     * added to the access list properties of the given satellite
     *
     * @param satellite
     * @param gridPointList
     * @param pr
     */
    public void evaluateSatelliteAccesses(Satellite satellite, final List<GridPoint> gridPointList, final PRRequestParameter pr)
    {

        this.gridPointList = gridPointList;
        this.pr = pr;
        /**
         * Evaluating access Events on the satellite
         */
        evaluateSatelliteAccessEvents(satellite);

    }// end evaluateSatelliteAccesses

    /**
     *
     * Evaluate the access on the grid for the satellite
     *
     * @param satellite
     */
    protected void evaluateSatelliteAccessEvents(Satellite satellite)

    {
        // Perform a test in each point in the grid
       
        logger.debug("evaluateSatelliteAccessEvents: " );

        /**
         * List of epochs on wich search access
         */
        List<EpochBean> epochList = satellite.getEpochs();

        /**
         * We have to search below thw guard
         */
        int epochListSize = epochList.size() - this.numberOfGuardSample;

        /**
         * Set Max and Min off nadir
         *
         */
        setMinMaxOffNadir(satellite);
        int cont = 0;

        // for each point in the grid
        for (GridPoint currentPoint : this.gridPointList)

        {
            logger.debug( " Gridpoint: " + currentPoint.getId());
            // TODO : capire numberOfGuardSample
            int i = 0 + this.numberOfGuardSample;
            while ((i + 3) < epochListSize)

            {
                // logger.debug("Run on epoch: " +i + " Gridpoint: " +
                // currentPoint.getId());

                this.s0 = epochList.get(i);
                this.s1 = epochList.get(i + 1);
                this.s2 = epochList.get(i + 2);
                this.s3 = epochList.get(i + 3);

                this.dot1 = evaluateE1ComponentAlongPS(currentPoint, this.s1);

                this.dot2 = evaluateE1ComponentAlongPS(currentPoint, this.s2);

                // Check if the component of e1 along the vector gridpooint -
                // satpos changes the sign
                // if so a possible access happens

                // logger.debug("dot1: "+ dot1 + " dot2: "+dot2);

                if ((this.dot1 > 0) != (this.dot2 > 0)) // sign is changed
                {

                    // logger.debug("Sign changed:");

                    /**
                     * Coarse estimation
                     */
                    Vector3D coarseSv = coarseSVEstimation(this.s1, this.s2, currentPoint);

                    if (isAbleToView(currentPoint, coarseSv))
                    {

                        // logger.debug("Able to view");
                        // event found
                        // evaluate the list of access on that point
                        evaluateAccesses(currentPoint, satellite, i);

                    } // end if
                    else
                    {
                        logger.debug( " isAbleToView = false!");
                    }

                }
                i++;
            } // end while

//            if (cont == 0)
//            {
//                logger.debug("Run on epoch: " + i);
//                logger.debug("s0 :" + this.s0.toString());
//                logger.debug("s1 :" + this.s1.toString());
//                logger.debug("s2 :" + this.s2.toString());
//                logger.debug("s3:" + this.s3.toString());
//
//                cont++;
//            }

            // Check if the component of velocity along the direction

        } // end for in gridgetoXyz

    }// end evaluateSatelliteAccess

    /**
     * Evaluate the min and max off nadir angle for the beams involved
     *
     * @param sat
     */
    protected void setMinMaxOffNadir(Satellite sat)

    {
        /**
         * Beam List
         */
        List<BeamBean> beams = sat.getBeams();

        this.minOffNadir = 0;
        this.maxOffNadir = 0;

        if (beams != null)
        {
            int size = beams.size();
            BeamBean currentBeam = null;
            double angle;

            boolean isFirst = true;

            for (int i = 0; i < size; i++)

            {
                currentBeam = beams.get(i);

                if (!isFirst)
                {
                    // not first
                    angle = currentBeam.getNearOffNadir();
                    if (angle < this.minOffNadir)
                    {
                        /**
                         * The angle is less
                         */
                        this.minOffNadir = angle;
                    }

                    angle = currentBeam.getFarOffNadir();

                    if (angle > this.maxOffNadir)
                    {
                        /**
                         * the angle is major
                         */
                        this.maxOffNadir = angle;
                    }
                } // end if is first
                else
                {
                    /**
                     * if current beam is the first of the list
                     */
                    this.minOffNadir = currentBeam.getNearOffNadir();
                    this.maxOffNadir = currentBeam.getFarOffNadir();
                    isFirst = false;

                }

            } // end for
        } // enf if
    }// end setMinMaxOffNadir

    /**
     *
     * @param p
     * @param satEpoch
     * @return E1 components along PS
     */
    protected double evaluateE1ComponentAlongPS(GridPoint p, EpochBean satEpoch)

    {
        // (GridPoint - Satpos) * e1
        // evaluating vale
        double retval = p.getEcef().subtract(satEpoch.getoXyz()).dotProduct(satEpoch.getoE1xE1yE1z());
        return retval;
    } // end evaluateE1ComponentAlongPS

    /**
     *
     * @param gridPointPos
     *            ECEF coord
     * @param satPos
     *            ECEF coord
     * @param e1
     *            e1 versor in the sat ref frame
     * @return
     *
     *         protected double evaluateE1ComponentAlongPS(Vector3D
     *         gridPointPos, Vector3D satPos, Vector3D e1) { //evaluating vale
     *         double retval = gridPointPos.subtract(satPos).dotProduct(e1);
     *         return retval; } //End method
     */
    /**
     * Evaluate the accesses over the point p after that a possible access event
     * has been detected
     *
     * @param p
     * @param satellite
     * @param startingPointWindowIndex,
     *            starting point index of the current sliding window
     */
    protected void evaluateAccesses(GridPoint p, Satellite satellite, int startingPointWindowIndex)

    {
        /**
         * Access
         */
        Access access = null;

        // List<Access> subAccessesList = null;
        /**
         * Mobile window
         */
        double[] epochTimes =
        { this.s0.getEpoch(), this.s1.getEpoch(), this.s2.getEpoch(), this.s3.getEpoch() };

        // double [] epochTimes =
        // {DateTimeUtils.fromJulianDay(s0.getEpoch()),DateTimeUtils.fromJulianDay(s1.getEpoch()),DateTimeUtils.fromJulianDay(s2.getEpoch()),DateTimeUtils.fromJulianDay(s3.getEpoch())};

        /**
         * X,Y,Z,VX,VY,VZ vector of position and velocity componenets
         */
        double[] Xs =
        { this.s0.getX(), this.s1.getX(), this.s2.getX(), this.s3.getX() };
        double[] Ys =
        { this.s0.getY(), this.s1.getY(), this.s2.getY(), this.s3.getY() };
        double[] Zs =
        { this.s0.getZ(), this.s1.getZ(), this.s2.getZ(), this.s3.getZ() };
        double[] VXs =
        { this.s0.getVx(), this.s1.getVx(), this.s2.getVx(), this.s3.getVx() };
        double[] VYs =
        { this.s0.getVy(), this.s1.getVy(), this.s2.getVy(), this.s3.getVy() };
        double[] VZs =
        { this.s0.getVz(), this.s1.getVz(), this.s2.getVz(), this.s3.getVz() };

        /**
         * e1 compomnents vector on time windows
         */
        double[] e1ComponentAlongPS =
        { evaluateE1ComponentAlongPS(p, this.s0), this.dot1, this.dot2, evaluateE1ComponentAlongPS(p, this.s3) };

        // logger.debug("Interpolating");
        // PolynomialFunctionLagrangeForm polyE1ComponentAlongPS = new
        // PolynomialFunctionLagrangeForm(epochTimes,e1ComponentAlongPS);

        try
        {
            /**
             * Newton solver
             */
            NewtonRaphsonSolver solver = new NewtonRaphsonSolver(this.solverEps);

            /**
             * Newton polinomial for of
             */
            PolynomialFunctionNewtonForm polyE1ComponentAlongPS = interpolate(epochTimes, e1ComponentAlongPS);
            double accessTime = 0;

            // logger.info("Solving");

            /**
             * Evaluating access time
             */
            accessTime = solver.solve(FeasibilityConstants.SolvMaxEval, polyE1ComponentAlongPS, epochTimes[1], epochTimes[2]);

            /**
             * Lagrange polinomial for the components are used to evaluate zero
             * doppler satellite positio and velocity
             */
            double X = new PolynomialFunctionLagrangeForm(epochTimes, Xs).value(accessTime);
            double Y = new PolynomialFunctionLagrangeForm(epochTimes, Ys).value(accessTime);
            double Z = new PolynomialFunctionLagrangeForm(epochTimes, Zs).value(accessTime);
            double Vx = new PolynomialFunctionLagrangeForm(epochTimes, VXs).value(accessTime);
            double Vy = new PolynomialFunctionLagrangeForm(epochTimes, VYs).value(accessTime);
            double Vz = new PolynomialFunctionLagrangeForm(epochTimes, VZs).value(accessTime);

            /**
             * Zero doppler satellite position and velocity
             */
            Vector3D satPosAtZeroDoppler = new Vector3D(X, Y, Z);
            Vector3D satVelAtZeroDoppler = new Vector3D(Vx, Vy, Vz);

            double offNadirAngle = evaluateOffNadirAngle(p.getEcef(), satPosAtZeroDoppler);

            // check if offnadirAngle is inside min / max off nadir

            double absOffNadirAngle = Math.abs(offNadirAngle);

            // logger.debug("absOffNadir " +absOffNadirAngle );

            /**
             * If off nadir angle inside min e max is a possible access
             */
            if ((absOffNadirAngle >= this.minOffNadir) && (absOffNadirAngle <= this.maxOffNadir))
            {

//                logger.debug("Access time from : " + DateUtils.fromCSKDateToDateTime(epochTimes[1]) + " and " + DateUtils.fromCSKDateToDateTime(epochTimes[2]) + ":" + " " + accessTime + " " + DateUtils.fromCSKDateToDateTime(accessTime) + " elaspsed: " + (accessTime - epochTimes[1]) + " poly degree: " + polyE1ComponentAlongPS.degree() + " Valore a access time :" + polyE1ComponentAlongPS.value(accessTime));
//                logger.debug("Poly: " + polyE1ComponentAlongPS.value(accessTime) + " " + polyE1ComponentAlongPS.value(epochTimes[1]) + " " + polyE1ComponentAlongPS.value(epochTimes[2]));
//
//                logger.debug(absOffNadirAngle + " angle in interval: " + minOffNadir + " : " + maxOffNadir + " e1comp: " + e1ComponentAlongPS);

                // subAccessesList = new ArrayList<Access>();
                int lookSide = evaluateLookSide(satPosAtZeroDoppler, satVelAtZeroDoppler, p.getEcef());
                int orbitDir = evaluateOrbitDirection(satVelAtZeroDoppler);

                // check if the satellite can look at that side
                if (!satellite.checkLookSide(lookSide))
                {
                    return;
                }

                List<BeamBean> beams = satellite.getBeams();

                // logger.debug(" beamList size: " + beams.size());

                BeamBean currentBeam = null;

                for (int i = 0; i < beams.size(); i++)
                {
                    currentBeam = beams.get(i);

                    // //System.out.println(absOffNadirAngle + " >= " +
                    // currentBeam.getNearOffNadir() +" && " +
                    // absOffNadirAngle+" <= "+currentBeam.getFarOffNadir() );

                    if ((absOffNadirAngle >= currentBeam.getNearOffNadir()) && (absOffNadirAngle <= currentBeam.getFarOffNadir()))
                    {
                    	satellite.updateMinMaxDuration(currentBeam);
                        /*
                         * Access(GridPoint point, String satelliteId, double
                         * accessTime, double offNadir, int lookSide, String
                         * beamId, int orbitDirection, int orbitId, Vector3D
                         * satellitePos, Vector3D satelliteVel)
                         */
                        // logger.debug("Found access");
                        access = new Access(satellite.getMissionName(), p, satellite, accessTime, absOffNadirAngle, lookSide, currentBeam, orbitDir,
                                // s1.getIdOrbit(),
                                evaluateOrbitNumber(satPosAtZeroDoppler), satPosAtZeroDoppler, satVelAtZeroDoppler, this.s1.getDataType(), startingPointWindowIndex);

                        // Check if look side and orbit direction are in line
                        // with the request and if the satellite can look at
                        // that side

                        boolean checkAgainstPr = true;
                        boolean checkForPassThrough = true;
                        if (this.pr != null)
                        {
                            /**
                             * Checking against pr consraint
                             */
                            checkAgainstPr = this.pr.isValidAccess(access);
                            if (this.pr.isPassThrough())
                            {
                                /**
                                 * Checking against passwtrough
                                 */
                                checkForPassThrough = satellite.checkAccessAgainstsatellitePass(access);
                            }

                        } // end if

                        /**
                         * checking if access satillete against allowed lookside
                         * on satellte satellite paw
                         */
                        if (checkAgainstPr && checkForPassThrough && satellite.checkLookSide(lookSide) && satellite.checkForPaw(access))
                        {

                            satellite.addAccess(access);
                        }

                    }
                } // end for

            } // end if

        } // End try
        catch (Exception e)
        {
        	DateUtils.getLogInfo(e, logger);
            // subAccessesList=null;
            // logger.error("Eccezione: " + e.getMessage());
            this.tracer.warning(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_ERROR, e.getMessage());
        } // end catch

        // return subAccessesList;
    } // end getAccess

    /**
     * Evaluate the orbit number given the Satellite pos
     *
     * @param satPos
     * @return
     */
    protected long evaluateOrbitNumber(Vector3D satPos)
    {
        long orbitNumber = this.s1.getIdOrbit();

        /**
         * If type is odref or second and third sammple of time window have the
         * same orbit id nothing to do
         */
        if ((this.s1.getDataType() == FeasibilityConstants.OdrefType) || (this.s1.getIdOrbit() == this.s2.getIdOrbit()))
        {
            orbitNumber = this.s1.getIdOrbit();
        }
        else
        {
            /**
             * LLH satellite position
             */
            double[] llh = ReferenceFrameUtils.ecef2llh(satPos, true);

            if (llh[0] >= 0)
            {
                /**
                 * If satellite latitute above equator
                 */
                orbitNumber = this.s1.getIdOrbit();
            } // end if
            else
            {
                orbitNumber = this.s2.getIdOrbit();
            } // end else

        } // end else

        return orbitNumber;
    }// End method

    /**
     * Check if the access is usable aigainst paw. It return false (not usable)
     * if the access fall inside a paw not deferrable and if the access is not
     * ebvaluated with ODREF. If the paw is defferable the access can be used
     * with warning, whereas if the aceess is evaulated with ODREF because of is
     * repeteable the DTO shall be evaluated in order to be exansed. Other
     * control are needeed in this case directly on DTO.
     *
     * @param access
     * @return false if the access is inside a not dererreable paw so sould be
     *         discarded
     *
     *         protected boolean checkForPaw(Access access) {
     *
     *         boolean retval = true;
     *
     *         Satellite sat = access.getSatellite();
     *
     *
     *         ArrayList<PlatformActivityWindowBean> pawList = sat.getPawList();
     *
     *         EpochBean
     *         accessEpoch=(EpochBean)access.getSatellite().getEpochs().get(access.getStartingPointWindowIndex());
     *         int dataType = accessEpoch.getDataType();
     *
     *         if(dataType==FeasibilityConstants.OdrefType) {
     *
     *         return true; }//end if
     *
     *         double accessTime = accessEpoch.getEpoch();
     *
     *         PlatformActivityWindowBean paw;
     *
     *         for(int i = 0; i < pawList.size();i++) { paw =
     *         (PlatformActivityWindowBean)pawList.get(i);
     *         if(paw.isDeferrableFlag()) {
     *
     *         continue; } else { if(accessTime >= paw.getActivityStartTime() &&
     *         accessTime<=paw.getActivityStopTime()) {
     *
     *         retval = false;
     *
     *         break; }//end if }//End else
     *
     *         }//end for return retval; } //End method
     */
    /**
     * Evaluate the orbit direction.
     *
     * @param satelliteVel
     * @return Return an in stating the orbit direction
     */
    protected int evaluateOrbitDirection(Vector3D satelliteVel)
    {
        /**
         * Default
         */
        int orbitDir = FeasibilityConstants.AscendingOrbit;

        double vz = satelliteVel.getZ();

        if (vz < 0)
        {
            /**
             * If the z component of velocity is <0 orbit descending
             */
            orbitDir = FeasibilityConstants.DescendingOrbit;
        } // end if

        return orbitDir;
    }// end method

    /**
     * Evaluate the look side of the satellite
     *
     * @param posAtZeroDoppler
     * @param velAtZeroDoppler
     * @param gridPoint
     * @return the look side (1 right , 2 left)
     */
    protected int evaluateLookSide(Vector3D posAtZeroDoppler, Vector3D velAtZeroDoppler, Vector3D gridPoint)

    {
        int lookSide = FeasibilityConstants.RighLookSide;
        /**
         * e2 component of zero doppler
         */
        Vector3D e2 = ReferenceFrameUtils.getSatelliteReferenceFrame(posAtZeroDoppler, velAtZeroDoppler)[1];

        double dot = e2.dotProduct(gridPoint);

        if (dot < 0)
        {
            /**
             * Id dot product <0 left side
             */
            lookSide = FeasibilityConstants.LeftLookSide;
        } // end if

        return lookSide;
    } // end evaluateLookSide

    /**
     * Return the angle in degree between satellite target direction and
     * off-nadir direction
     *
     * @param gridPoint
     * @param satPosAtZeroDoppler
     * @return off nadir angle
     */
    protected double evaluateOffNadirAngle(Vector3D gridPoint, Vector3D satPosAtZeroDoppler)

    {
        double offNadirAngle = 0;
        /**
         * Evaluate satellite target vector
         *
         */

        Vector3D satTarget = gridPoint.subtract(satPosAtZeroDoppler);
        double satTargetNorm = satTarget.getNorm();

        /**
         * LLH at zero doppler
         */
        double[] llhAtZeroDoppler = ReferenceFrameUtils.ecef2llh(satPosAtZeroDoppler.toArray(), false); // return
                                                                                                        // llh
                                                                                                        // in
                                                                                                        // degree

        llhAtZeroDoppler[2] = 0;

        Vector3D subSatAtZeroDoppler = new Vector3D(ReferenceFrameUtils.llh2ecef(llhAtZeroDoppler, false));
        Vector3D satSSP = subSatAtZeroDoppler.subtract(satPosAtZeroDoppler);
        double satSSPNorm = satSSP.getNorm();

        /**
         * compute sub satellite point targt vector
         */
        Vector3D sspTar = gridPoint.subtract(subSatAtZeroDoppler);
        double sspTarNorm = sspTar.getNorm();

        /**
         * Calcolo offnaditr by using cosine theorema
         */
        offNadirAngle = Math.acos((((satSSPNorm * satSSPNorm) + (satTargetNorm * satTargetNorm)) - (sspTarNorm * sspTarNorm)) / (2 * satSSPNorm * satTargetNorm));

        /**
         * Angle in degree
         */
        offNadirAngle = Math.toDegrees(offNadirAngle);

        return offNadirAngle;

    } // evaluateOffNadirAngle

    /**
     * Give a coarse estimation of the satellite velocity over the grid point
     *
     * @param s1
     * @param s2
     * @param p
     * @return corse esitimation
     */
    protected Vector3D coarseSVEstimation(EpochBean s1, EpochBean s2, GridPoint p)

    {
        // logger.debug("coarseSVEstimation");
        Vector3D coarseSV = null;

        Vector3D u = s1.getoXyz().subtract(s2.getoXyz());
        double a = u.getNorm();

        u = u.normalize();

        double b = s1.getoXyz().subtract(p.getEcef()).getNorm();
        double c = s2.getoXyz().subtract(p.getEcef()).getNorm();

        /**
         * Half perimeter
         */
        double halfPerimeter = FeasibilityConstants.half * (a + b + c);

        /**
         * Using the Herone formulae to evaluate coare estimation
         */
        double S = Math.sqrt(halfPerimeter * (halfPerimeter - a) * (halfPerimeter - b) * (halfPerimeter - c));
        double h = (2 * S) / a;

        double x = Math.sqrt((c * c) - (h * h));

        coarseSV = s2.getoXyz().add(u.scalarMultiply(x));

        return coarseSV;
    }// eND METHOD

    /**
     * Check if the satellite is in visibility over a grid point
     *
     * @param refPoint
     * @param targetPoint
     * @return true if the satellite if able to see the point
     */
    protected boolean isAbleToView(GridPoint refPoint, Vector3D targetPoint)
    {
        boolean isAble = false;

        // double [] enu = ReferenceFrameUtils.ecef2enu(refPoint.getLLH(),
        // targetPoint.toArray());
        /**
         * Enu coordinate
         */
        double[] enu = ReferenceFrameUtils.ecef2enu(refPoint.getEcef().toArray(), targetPoint.toArray());

        /**
         * Angle above horizon
         */
        double angleAboveHorizon = Math.atan2(enu[2], Math.sqrt((enu[0] * enu[0]) + (enu[1] * enu[1])));

        // logger.debug("Angle above hozizon: " + angleAboveHorizon );

        /**
         * True if the anglee is >0
         */
        isAble = (angleAboveHorizon > 0);

        return isAble;
    }// end isAbleToView

    /**
     * Return a polynomial function in Newton form interpolating the given
     * points.
     *
     * @param x
     * @param y
     * @return Polinomal newton form
     * @throws IllegalArgumentException
     */
    public PolynomialFunctionNewtonForm interpolate(double x[], double y[]) throws IllegalArgumentException
    {

        /**
         * When used for interpolation, the Newton form formula becomes p(x) =
         * f[x0] + f[x0,x1](x-x0) + f[x0,x1,x2](x-x0)(x-x1) + ... +
         * f[x0,x1,...,x[n-1]](x-x0)(x-x1)...(x-x[n-2]) Therefore, a[k] =
         * f[x0,x1,...,xk], c[k] = x[k].
         * <p>
         * Note x[], y[], a[] have the same length but c[]'s size is one less.
         * </p>
         */
        final double[] c = new double[x.length - 1];
        System.arraycopy(x, 0, c, 0, c.length);

        final double[] a = computeDividedDifference(x, y);
        if (a == null)
        {
            return null;
        }
        return new PolynomialFunctionNewtonForm(a, c);

    }// end method

    /**
     * Used to evaluate newton polynomial function
     *
     * @param x
     * @param y
     * @return divided difference array
     * @throws IllegalArgumentException
     */
    protected static double[] computeDividedDifference(final double x[], final double y[]) throws IllegalArgumentException

    {
        /**
         * Initialize
         */
        final double[] divdiff = y.clone(); // initialization

        final int n = x.length;
        final double[] a = new double[n];
        a[0] = divdiff[0];
        for (int i = 1; i < n; i++)
        {
            for (int j = 0; j < (n - i); j++)
            {
                final double denominator = x[j + i] - x[j];
                if (denominator == 0.0)
                {
                    /**
                     * This happens only when two abscissas are identical throw
                     * new DuplicateSampleAbscissaException(x[j], j, j+i);
                     */
                    // //System.out.println("----------------------------------------"
                    // + x[j+i]);
                    throw new IllegalArgumentException("Duplicated ascissa");
                }
                divdiff[j] = (divdiff[j + 1] - divdiff[j]) / denominator;
            } // end for
            a[i] = divdiff[0];
        } // end for

        return a;
    }// end method

}// end Class
