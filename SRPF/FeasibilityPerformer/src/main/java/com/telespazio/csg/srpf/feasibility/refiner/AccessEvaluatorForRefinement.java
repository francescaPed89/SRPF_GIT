/**
*
* MODULE FILE NAME:	AccessEvaluatorForRefinement.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			This class perform the access evaluation in case of refinement
*
* PURPOSE:			DTO 's refinement
*
* CREATION DATE:	15-03-2017
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

import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeForm;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionNewtonForm;
import org.apache.commons.math3.analysis.solvers.NewtonRaphsonSolver;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.telespazio.csg.srpf.dataManager.bean.BeamBean;
import com.telespazio.csg.srpf.feasibility.Access;
import com.telespazio.csg.srpf.feasibility.AccessesEvaluator;
import com.telespazio.csg.srpf.feasibility.GridPoint;
import com.telespazio.csg.srpf.feasibility.Satellite;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;

/**
 *
 * This class extend the Accessevaluator. It's a specialization for evaluate
 * access in case of refinement
 *
 * @author Amedeo Bancone
 * @version 1.0
 */
public class AccessEvaluatorForRefinement extends AccessesEvaluator

{

    // logger
    TraceManager tracer = new TraceManager();

    // Flag for checking pass trought check
    protected boolean haveCheckForPassThrough = false;

    /**
     * return true in case of passthrough
     *
     * @return true in case of passthrough
     */
    public boolean isHaveCheckForPassThrough()
    {
        return this.haveCheckForPassThrough;
    }// end method

    /**
     * Set the passthrogh flag
     *
     * @param haveCheckForPassThrough
     */
    public void setHaveCheckForPassThrough(boolean haveCheckForPassThrough)
    {
        this.haveCheckForPassThrough = haveCheckForPassThrough;
    }// end method

    /**
     * Evaluate the accesses over the point p after that a possible access event
     * has been detected
     *
     * @param p
     * @param satellite
     * @param startingPointWindowIndex,
     *            starting point index of the current sliding window
     */
    @Override
    protected void evaluateAccesses(GridPoint p, Satellite satellite, int startingPointWindowIndex)

    {
        Access access = null;

        // List<Access> subAccessesList = null;

        double[] epochTimes =
        { this.s0.getEpoch(), this.s1.getEpoch(), this.s2.getEpoch(), this.s3.getEpoch() };

        // double [] epochTimes =
        // {DateTimeUtils.fromJulianDay(s0.getEpoch()),DateTimeUtils.fromJulianDay(s1.getEpoch()),DateTimeUtils.fromJulianDay(s2.getEpoch()),DateTimeUtils.fromJulianDay(s3.getEpoch())};
        /**
         * Evaluating PVT solution on time sliding window's points
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
         * Evaluating e1 component on time sliding window's points
         */
        double[] e1ComponentAlongPS =
        { evaluateE1ComponentAlongPS(p, this.s0), this.dot1, this.dot2, evaluateE1ComponentAlongPS(p, this.s3) };

        try
        {

            // logger.debug("Interpolating");
            // PolynomialFunctionLagrangeForm polyE1ComponentAlongPS = new
            // PolynomialFunctionLagrangeForm(epochTimes,e1ComponentAlongPS);

            NewtonRaphsonSolver solver = new NewtonRaphsonSolver(1E-12);

            PolynomialFunctionNewtonForm polyE1ComponentAlongPS = interpolate(epochTimes, e1ComponentAlongPS);
            double accessTime = 0;

            // logger.info("Solving");
            // evaluate access time
            accessTime = solver.solve(10000, polyE1ComponentAlongPS, epochTimes[1], epochTimes[2]);

            // build polinomials
            double X = new PolynomialFunctionLagrangeForm(epochTimes, Xs).value(accessTime);
            double Y = new PolynomialFunctionLagrangeForm(epochTimes, Ys).value(accessTime);
            double Z = new PolynomialFunctionLagrangeForm(epochTimes, Zs).value(accessTime);
            double Vx = new PolynomialFunctionLagrangeForm(epochTimes, VXs).value(accessTime);
            double Vy = new PolynomialFunctionLagrangeForm(epochTimes, VYs).value(accessTime);
            double Vz = new PolynomialFunctionLagrangeForm(epochTimes, VZs).value(accessTime);

            // zero doppler PVT
            Vector3D satPosAtZeroDoppler = new Vector3D(X, Y, Z);
            Vector3D satVelAtZeroDoppler = new Vector3D(Vx, Vy, Vz);

            // off nadir
            double offNadirAngle = evaluateOffNadirAngle(p.getEcef(), satPosAtZeroDoppler);

            double absOffNadirAngle = Math.abs(offNadirAngle);

            // evaluate look side
            int lookSide = evaluateLookSide(satPosAtZeroDoppler, satVelAtZeroDoppler, p.getEcef());
            // evaluate orbit
            int orbitDir = evaluateOrbitDirection(satVelAtZeroDoppler);

            // check if the satellite can look at that side
            if (!satellite.checkLookSide(lookSide))
            {
                return;
            }

            // List<BeamBean> beams = satellite.getBeams();

            // logger.debug(" beamList size: " + beams.size());

            BeamBean currentBeam = null;
            ;

            // //System.out.println(absOffNadirAngle + " >= " +
            // currentBeam.getNearOffNadir() +" && " + absOffNadirAngle+" <=
            // "+currentBeam.getFarOffNadir() );

            // create access
            access = new Access(satellite.getMissionName(), p, satellite, accessTime, absOffNadirAngle, lookSide, currentBeam, orbitDir,
                    // s1.getIdOrbit(),
                    evaluateOrbitNumber(satPosAtZeroDoppler), satPosAtZeroDoppler, satVelAtZeroDoppler, this.s1.getDataType(), startingPointWindowIndex);

            // Check if look side and orbit direction are in line with the
            // request and if the satellite can look at that side

            boolean checkAgainstPr = true;
            boolean checkForPassThrough = true;

            if (checkAgainstPr && checkForPassThrough && satellite.checkLookSide(lookSide) && satellite.checkForPaw(access))
            {

                satellite.addAccess(access);
            } // end if

        } // end tryu
        catch (TooManyEvaluationsException | IllegalArgumentException e)
        {
            // subAccessesList=null;
            // logger.error("Eccezione: " + e.getMessage());
            // do nothing
            // just log
            this.tracer.warning(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_ERROR, e.getMessage());
        } // end catch

        // return subAccessesList;
    } // end getAccess

}// end class
