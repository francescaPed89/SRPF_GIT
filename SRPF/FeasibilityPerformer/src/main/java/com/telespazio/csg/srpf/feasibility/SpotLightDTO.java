/**
*
* MODULE FILE NAME:	SpotLightDTO.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Class impementing a SPOTLIGHT DTO
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
* 11-05-2016 | Amedeo Bancone  |1.1| createDTO method changed to align it to the new algo
* 									 spotLightImageCorners has been modified in order to align it to the new algo
* --------------------------+------------+----------------+-------------------------------
* --------------------------+------------+----------------+-------------------------------
* 11-02-2018 | Amedeo Bancone  |2.0| added evaluation of start and stop velocity and position also for spotlight
* 									 sorted dtoAccessList
* 									 added evaluation of near e far off nadir
* 									 added squareStart and squareStop time
* 									 Modified for take into account:
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

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.w3c.dom.Element;

import com.telespazio.csg.srpf.dataManager.bean.BeamBean;
import com.telespazio.csg.srpf.dataManager.bean.EpochBean;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.DateUtils;
import com.telespazio.csg.srpf.utils.ReferenceFrameUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Class impementing a SPOTLIGHT DTO
 *
 * @author Amedeo Bancone
 * @version 2.0
 *
 *
 */
public class SpotLightDTO extends DTO

{

	// log
	private TraceManager tracer = new TraceManager();

	// Access on which the DTO is built
	private Access centeredAccess;

	// private double dtoDuration;
	private double dtoHalfDuration;

	private double spotlightDim1; // along
	private double spotlightDim2; // across

	// true if good dto
	private boolean isGoodDTO = true;

	// Access time at the beginning square
	private double squareStart = 0;
	// position
	private Vector3D satellitePositionAtSquareStart = new Vector3D(0, 0, 0);
	// velocity
	private Vector3D satelliteVelocityAtSquareStart = new Vector3D(0, 0, 0);

	// Access time at the end of the square
	private double squareStop = 0;
	// position
	private Vector3D satellitePositionAtSquareStop = new Vector3D(0, 0, 0);
	// velocity
	private Vector3D satelliteVelocityAtSquareStop = new Vector3D(0, 0, 0);

	// Used in refinement to byuild input sparc
	protected boolean di2sConfirmationFlag = false;

	/***
	 *
	 * @return position at squre start
	 */
	public Vector3D getSatellitePositionAtSquareStart() {
		return this.satellitePositionAtSquareStart;
	}// end method

	/**
	 * Set the satellite position at the square start
	 *
	 * @param satellitePositionAtSquareStart
	 */
	public void setSatellitePositionAtSquareStart(Vector3D satellitePositionAtSquareStart) {
		this.satellitePositionAtSquareStart = satellitePositionAtSquareStart;
	}// end method

	/**
	 *
	 * @return velocity at squre start
	 */
	public Vector3D getSatelliteVelocityAtSquareStart() {
		return this.satelliteVelocityAtSquareStart;
	}// end method

	/**
	 * Set the satellite velocity at the square start
	 *
	 * @param satelliteVelocityAtSquareStart
	 */
	public void setSatelliteVelocityAtSquareStart(Vector3D satelliteVelocityAtSquareStart) {
		this.satelliteVelocityAtSquareStart = satelliteVelocityAtSquareStart;
	}// end method

	/**
	 *
	 * @return position at square stop
	 */
	public Vector3D getSatellitePositionAtSquareStop() {
		return this.satellitePositionAtSquareStop;
	}// end method

	/**
	 * Set the satellite position at the square stop
	 *
	 * @param satellitePositionAtSquareStop
	 */
	public void setSatellitePositionAtSquareStop(Vector3D satellitePositionAtSquareStop) {
		this.satellitePositionAtSquareStop = satellitePositionAtSquareStop;
	}// end method

	/**
	 *
	 * @return velocity at square stop
	 */
	public Vector3D getSatelliteVelocityAtSquareStop() {
		return this.satelliteVelocityAtSquareStop;
	}// end method

	/**
	 * Set the satellite velocity at the square stop
	 *
	 * @param satelliteVelocityAtSquareStop
	 */
	public void setSatelliteVelocityAtSquareStop(Vector3D satelliteVelocityAtSquareStop) {
		this.satelliteVelocityAtSquareStop = satelliteVelocityAtSquareStop;
	}// end method

	/**
	 * return the di2sconfirmation flag
	 *
	 * @return
	 */
	public boolean isDi2sConfirmationFlag() {
		return this.di2sConfirmationFlag;
	}// end method

	/**
	 * set the di2s confirmation flag
	 *
	 * @param di2sConfirmationFlag
	 */
	public void setDi2sConfirmationFlag(boolean di2sConfirmationFlag) {
		this.di2sConfirmationFlag = di2sConfirmationFlag;
	}// end method

	// boolean haveRemovePoint = false; //point near polar region if true, so it
	// should be removed from the grid point

	// List odf access inside the spolight square
	// private List<Access> dtoAccessList = new ArrayList<Access>();

	/**
	 * Clone a spothlight dto
	 */
	@Override
	public SpotLightDTO clone() {
		// create dto
		SpotLightDTO d = new SpotLightDTO();
		// copy parameters
		copyParameters(d);
		// duplicating others parameters
		d.centeredAccess = this.centeredAccess;
		d.dtoHalfDuration = this.dtoHalfDuration;
		d.spotlightDim1 = this.spotlightDim1;
		d.spotlightDim2 = this.spotlightDim2;
		d.isGoodDTO = this.isGoodDTO;
		d.squareStart = this.squareStart;
		d.squareStop = this.squareStop;
		// returing
		return d;
	}// end method

	/**
	 *
	 * @return The time (CSK time) at the beginning of the square
	 */
	public double getSquareStart() {
		return this.squareStart;
	}// end method

	/**
	 * Set the time (CSK time) at the end of the square
	 *
	 * @param squareStart
	 */
	public void setSquareStart(double squareStart) {
		this.squareStart = squareStart;
	}// end method

	/**
	 * Get the CSK time at the end of the square
	 *
	 * @return the time at the end of the square
	 */
	public double getSquareStop() {
		return this.squareStop;
	}// end method

	/**
	 * Set the CSK time at the end of the square
	 *
	 * @param squareStop
	 */
	public void setSquareStop(double squareStop) {
		this.squareStop = squareStop;
	}// end method

	/**
	 * @return the accessesInsideSpotList
	 */
	public List<Access> getAccessesInsideSpotList() {
		return this.dtoAccessList;
	}// end method

	/**
	 * Set default cut
	 */
	private void setDefaultCutInAzimuthForSparc() {
		// defaukt cut dor sparc input
		this.numberOfSampleInAzimuthExetnded = 3;
		this.numberOfSampleInAzimuthStandard = 3;
	}// end method

	/**
	 * Default constructor
	 */
	public SpotLightDTO() {
		setDefaultCutInAzimuthForSparc();
	}// end method

	/**
	 *
	 * @param dtoElement
	 * @throws XPathExpressionException
	 * @throws GridException
	 */
	public SpotLightDTO(Element dtoElement) throws XPathExpressionException, GridException {
		super(dtoElement);
		setDefaultCutInAzimuthForSparc();
	}// end method

	/**
	 * SppotLight DTO constructor
	 *
	 * @param centeredAccess
	 * @param strip
	 * @throws Exception
	 */
	public SpotLightDTO(Access centeredAccess, Strip strip) throws Exception {

		setDefaultCutInAzimuthForSparc();
		this.centeredAccess = centeredAccess;
		this.strip = strip;
		this.dtoAccessList.add(this.centeredAccess);
		this.sat = strip.getAccessList().get(0).getSatellite();

		// beam
		BeamBean beam = this.sat.getBeams().get(0);

		logger.trace("MODIFICA _ number of beams found for sat :" + this.sat.getBeams().size());

		logger.trace("MODIFICA _ for sat :" + this.sat.getName());

		this.spotlightDim1 = beam.getSwDim1() * 1000; // Along

		this.spotlightDim2 = beam.getSwDim2() * 1000; // Across


		/*
		 * get all satellites that impact the current strip for each satellite get all
		 * the beams for the related sensor mode merge the list of beams returned for
		 * the requested angle (?) get all and only the spotlight 2a spotlightDim1 and
		 * spotlightDime that satisfy the angle
		 */

		double offNadir = centeredAccess.getOffNadir();
		String sensorModeName = centeredAccess.getBeam().getSensorModeName();

		logger.trace("sensorModeName :" + sensorModeName);

		if (sensorModeName.equalsIgnoreCase("SPOTLIGHT-2A") || sensorModeName.equalsIgnoreCase("SPOTLIGHT-1A")) {

			List<BeamBean> beamList =  this.sat.getBeams();
			List<BeamBean> getOnlyValidBeams = getOnlyBeamsInIntervalforAngle(beamList, offNadir);
			logger.trace("dinamyc swath :" + centeredAccess.toString());

	

			logger.trace("centeredAccess :" + centeredAccess.toString());

			logger.trace("only valid beams for angle :" + offNadir);

			logger.trace("compute spotlightDim1 with the highest value of the list of onlyValidBeams");
			if (getOnlyValidBeams.size() > 0) {
				this.spotlightDim1 = getOnlyValidBeams.get(getOnlyValidBeams.size() - 1).getSwDim1() * 1000;
				this.spotlightDim2 = getOnlyValidBeams.get(getOnlyValidBeams.size() - 1).getSwDim2() * 1000;
			}
			logger.trace("spotlightDim1 : " + this.spotlightDim1);

			logger.trace("spotlightDim2 : " + this.spotlightDim2);


		}

		try {
			createDTO();

			// fillAccessListInsideSpot();
			// Forse da trasfromare in ECEF
			fillAccessListInsideSpotENU();

		} catch (Exception e) {
			// logger.warn(e.getMessage())
			this.isGoodDTO = false;

		} // end catch

	}// end constructor

	private List<BeamBean> getOnlyBeamsInIntervalforAngle(List<BeamBean> beamList, double offNadir) {
		List<BeamBean> validBam = new ArrayList<BeamBean>();
		for (int i = 0; i < beamList.size(); i++) {

			if (offNadir >= beamList.get(i).getNearOffNadir() &&  offNadir<=beamList.get(i).getFarOffNadir() ) {
				validBam.add(beamList.get(i));
			}
		}
		return validBam;
	}

	@Override
	public String toString() {
		
		String toStringFromSuperClass = super.toString();
		return "FROM SUPERCLASS : "+toStringFromSuperClass+" SpotLightDTO [tracer=" + tracer + ", centeredAccess=" + centeredAccess + ", dtoHalfDuration="
				+ dtoHalfDuration + ", spotlightDim1=" + spotlightDim1 + ", spotlightDim2=" + spotlightDim2
				+ ", isGoodDTO=" + isGoodDTO + ", squareStart=" + DateUtils.fromCSKDateToDateTime(squareStart) + ", satellitePositionAtSquareStart="
				+ satellitePositionAtSquareStart + ", satelliteVelocityAtSquareStart=" + satelliteVelocityAtSquareStart
				+ ", squareStop=" + DateUtils.fromCSKDateToDateTime(squareStop) + ", satellitePositionAtSquareStop=" + satellitePositionAtSquareStop
				+ ", satelliteVelocityAtSquareStop=" + satelliteVelocityAtSquareStop + ", di2sConfirmationFlag="
				+ di2sConfirmationFlag + "]";
	}


	/**
	 * Copy constructor
	 *
	 * @param d
	 */
	public SpotLightDTO(SpotLightDTO d) {
		// call super class
		super(d);
		// copying parameters
		this.squareStart = d.squareStart;
		this.squareStop = d.squareStop;
		this.centeredAccess = d.centeredAccess;
		this.dtoHalfDuration = d.dtoHalfDuration;
		this.spotlightDim1 = d.spotlightDim1;
		this.spotlightDim2 = d.spotlightDim2;
		this.isGoodDTO = d.isGoodDTO;
		setDefaultCutInAzimuthForSparc();
		// that's all

	}// end constructor

	/**
	 * get the centered Access
	 *
	 * @return
	 */
	public Access getCenteredAccess() {
		return this.centeredAccess;
	}// end method

	/**
	 * set the centered acccess
	 *
	 * @param centeredAccess
	 */
	public void setCenteredAccess(Access centeredAccess) {
		this.centeredAccess = centeredAccess;
	}// end method

	/**
	 *
	 * @return true if is a valid DTO
	 */
	public boolean isGood() {
		return this.isGoodDTO;
	}// end method

	/**
	 * Add to the access list all the accesses whose grid point fall in the DTO
	 * footprint
	 */
	private void fillAccessListInsideSpot() {

		Coordinate[] coords = new Coordinate[5];
		coords[0] = new Coordinate(this.corners[0][0], this.corners[0][1]);
		coords[1] = new Coordinate(this.corners[1][0], this.corners[1][1]);
		coords[2] = new Coordinate(this.corners[2][0], this.corners[2][1]);
		coords[3] = new Coordinate(this.corners[3][0], this.corners[3][1]);
		coords[4] = new Coordinate(this.corners[0][0], this.corners[0][1]);// first
																			// and
																			// last
																			// point
																			// must
																			// coincide

		GeometryFactory fact = new GeometryFactory(); // float precision : full
														// double
		Polygon spotLightPolygon = fact.createPolygon(coords);

		/*
		 * logger.info("Corner 1:" + this.corners[0][0] +" "+ this.corners[0][1]);
		 * logger.info("Corner 2:" + this.corners[1][0] +" "+ this.corners[1][1]);
		 * logger.info("Corner 3:" + this.corners[2][0] +" "+ this.corners[2][1]);
		 * logger.info("Corner 4:" + this.corners[3][0] +" "+ this.corners[3][1]);
		 * logger.info("Corner 5:" + this.corners[0][0] +" "+ this.corners[0][1]);
		 * logger.info("Center: " + this.centeredAccess.getGridPoint().getLLH()[0]+" "+
		 * this.centeredAccess.getGridPoint().getLLH()[1]);
		 */

		Coordinate currentCoord = null;
		Point currentPoint = null;
		double[] currentLLH = null;

		int cont = 0;
		logger.trace("MODIFICA 22.08 this.dtoAccessList.size " + this.dtoAccessList.size());

		for (Access a : this.strip.getAccessList()) {
			currentLLH = a.getGridPoint().getLLH();
			currentCoord = new Coordinate(currentLLH[0], currentLLH[1]);
			currentPoint = fact.createPoint(currentCoord);

			if (currentPoint.within(spotLightPolygon)) {
				cont++;
				// logger.info("within");
				this.dtoAccessList.add(a);
			}

		} // end For
		logger.trace("MODIFICA 22.08 cont size " + cont);

		logger.trace("MODIFICA 22.08 this.dtoAccessList.size " + this.dtoAccessList.size());
	}// end fillAccessListInsideSpot

	/**
	 * Add to the access list all the accesses whose grid point fall in the DTO
	 * footprint The foot print is translated in ENU reference in order to avoid
	 * poles and line of date issues
	 */
	private void fillAccessListInsideSpotENU() {
		Coordinate[] coords = new Coordinate[5];

		double[] refPos = this.centeredAccess.getGridPoint().getEcef().toArray();

		double[] corner0 = ReferenceFrameUtils.ecef2enu(refPos, ReferenceFrameUtils.llh2ecef(this.corners[0], true));
		double[] corner1 = ReferenceFrameUtils.ecef2enu(refPos, ReferenceFrameUtils.llh2ecef(this.corners[1], true));
		double[] corner2 = ReferenceFrameUtils.ecef2enu(refPos, ReferenceFrameUtils.llh2ecef(this.corners[2], true));
		double[] corner3 = ReferenceFrameUtils.ecef2enu(refPos, ReferenceFrameUtils.llh2ecef(this.corners[3], true));

		coords[0] = new Coordinate(corner0[0], corner0[1]);
		coords[1] = new Coordinate(corner1[0], corner1[1]);
		coords[2] = new Coordinate(corner2[0], corner2[1]);
		coords[3] = new Coordinate(corner3[0], corner3[1]);
		coords[4] = new Coordinate(corner0[0], corner0[1]);// first and last
															// point must
															// coincide

		GeometryFactory fact = new GeometryFactory(); // float precision : full
														// double
		Polygon spotLightPolygon = fact.createPolygon(coords);

		/*
		 * logger.info("Corner 1:" + this.corners[0][0] +" "+ this.corners[0][1]);
		 * logger.info("Corner 2:" + this.corners[1][0] +" "+ this.corners[1][1]);
		 * logger.info("Corner 3:" + this.corners[2][0] +" "+ this.corners[2][1]);
		 * logger.info("Corner 4:" + this.corners[3][0] +" "+ this.corners[3][1]);
		 * logger.info("Corner 5:" + this.corners[0][0] +" "+ this.corners[0][1]);
		 * logger.info("Center: " + this.centeredAccess.getGridPoint().getLLH()[0]+" "+
		 * this.centeredAccess.getGridPoint().getLLH()[1]);
		 */

		Coordinate currentCoord;
		Point currentPoint;
		double[] currentENU;
		for (Access a : this.strip.getAccessList()) {
			currentENU = ReferenceFrameUtils.ecef2enu(refPos, a.getGridPoint().getEcef().toArray());
			currentCoord = new Coordinate(currentENU[0], currentENU[1]);
			currentPoint = fact.createPoint(currentCoord);

			if (currentPoint.within(spotLightPolygon)) {
				// logger.info("within");
				this.dtoAccessList.add(a);
			}

		} // end For

		this.dtoAccessList.sort(new AccessComparatorByTime());
		this.strip.setStillUsableAccessForSpotlight(this.dtoAccessList);
	}// end fillAccessListInsideSpot

	/**
	 * Create a DTO
	 *
	 * @throws Exception
	 */
	private void createDTO() throws Exception {
		// double satelliteAverageGroundSpeed;
		double accessTime = this.centeredAccess.getAccessTime();
		this.isOdrefBased = this.centeredAccess.getOrbitType() == FeasibilityConstants.OdrefType;
		// satelliteAverageGroundSpeed =
		// centeredAccess.getAverageSatelliteGroundSpeed();

		// long dtoNanoSecondsDuration =
		// (long)((this.spotlightDim1/satelliteAverageGroundSpeed)*1e9);

		// this.dtoDuration =
		// DateUtils.nanosecondsToJulian(dtoNanoSecondsDuration);

		// this.dtoHalfDuration = 0.5*this.dtoDuration;

		// May be that in future the parameter should be filled with a value not
		// null
		this.dtoHalfDuration = 0.0;

		double halfTimeStep = this.centeredAccess.getSatellite().getSpotLightHalfTimeStep();
		
		if(this.getBeam()!=null)
		{
			halfTimeStep = FeasibilityConstants.half * DateUtils.millisecondsToJulian(this.getBeam().getDtoMinDuration());

			this.centeredAccess.getSatellite().setSpotLightHalfTimeStep(halfTimeStep);

		}
		//make it dynamic 
		/*
		 * 			this.spotLightTimeStep = DateUtils.millisecondsToJulian(minDuration);// ok
			this.spotLightHalfTimeStep = FeasibilityConstants.half * this.spotLightTimeStep;// ok
		 */
		this.setStartTime(accessTime - this.dtoHalfDuration - halfTimeStep);
		this.setStopTime(accessTime + this.dtoHalfDuration + halfTimeStep);

		Satellite sat = this.centeredAccess.getSatellite();

		this.startPosition = sat.getPositionAt(this.startTime, this.centeredAccess.getStartingPointWindowIndex());
		this.endPosition = sat.getPositionAt(this.stopTime, this.centeredAccess.getStartingPointWindowIndex());
		this.startVelocity = sat.getVelocityAt(this.startTime, this.centeredAccess.getStartingPointWindowIndex());
		this.endVelocity = sat.getVelocityAt(this.stopTime, this.centeredAccess.getStartingPointWindowIndex());

		/**
		 * Magari se si comincia a lavorare su SPOT enormi che al momento non sono
		 * previste forse è il caso di usare queste this.startPosition =
		 * sat.getPositionAt(this.startTime); this.endPosition =
		 * sat.getPositionAt(this.stopTime); this.startVelocity =
		 * sat.getVelocityAt(this.startTime); this.endVelocity =
		 * sat.getVelocityAt(this.stopTime);
		 */

		this.corners = spotLightImageCorners(this.centeredAccess);

		this.evaluateSqurePatameters();
		// //System.out.println("+++++++++++Evaluated Squre
		// parameters+++++++++++++++++");
		// this.orbitId = this.sat.getEpochAt(this.squareStart).getIdOrbit();
		// evaluated in evaluating square paramterers

		// orbit id has bean evaluated in evaluating the evaluateSqurePatameters
		// in
		this.trackNumber = this.sat.getTrackNumber(this.orbitId);

	}// end createDTO

	/**
	 *
	 * @param a
	 * @return the corner of the DTO
	 */
	private double[][] spotLightImageCorners(Access a) throws Exception {
		double[][] corners = new double[4][3];

		// evaluating beam boundary
		// double redPoint[][] = evaluateLLHcorners(a, a.getSatellitePos(),
		// a.getSatelliteVel());
		double redPoint[][] = evaluateLLHcorners(a.getSatellitePos(), a.getSatelliteVel());

		// reporting the beam boundary point in an ENU reference centered on the
		// grid point involved in the access

		double[] redEnu0 = ReferenceFrameUtils.ecef2enu(a.getGridPoint().getEcef().toArray(),
				ReferenceFrameUtils.llh2ecef(redPoint[0], true));
		double[] redEnu1 = ReferenceFrameUtils.ecef2enu(a.getGridPoint().getEcef().toArray(),
				ReferenceFrameUtils.llh2ecef(redPoint[1], true));

		// Reports point in a 2D plane: East North
		Vector2D R0 = new Vector2D(redEnu0[0], redEnu0[1]);
		Vector2D R1 = new Vector2D(redEnu1[0], redEnu1[1]);
		// Vector2D gridPoint = new Vector2D(a.getGridPoint().getEcef().getX(),
		// a.getGridPoint().getEcef().getY());

		// Evaluating distance R0-gridpoint in the EN plane
		double R0gridPointDistance = R0.getNorm();

		// Evaluating distance R1 gridPoint
		double R1gridPointDistance = R1.getNorm();
		double teta;

		if (R0gridPointDistance < R1gridPointDistance) {

			teta = Math.atan2(R0.getY(), R0.getX());

		} else {

			teta = Math.atan2(R1.getY(), R1.getX());
		}

		/*
		 * double teta1 = Math.atan2(R0.getY(),R0.getX());
		 *
		 * double teta2 = Math.atan2(R1.getY(),R1.getX());
		 *
		 * double abstetat1=Math.abs(Math.toDegrees(teta1)); double
		 * abstetat2=Math.abs(Math.toDegrees(teta2));
		 *
		 * teta=teta2;
		 *
		 * if(abstetat1<abstetat2) { teta=teta1; }
		 *
		 *
		 *
		 * //System.out.println("---------------------------------TETA:    " +
		 * Math.toDegrees(teta));
		 * //System.out.println("---------------------------------TETA0:    " +
		 * Math.toDegrees(Math.atan2(R0.getY(),R0.getX())));
		 * //System.out.println("---------------------------------TETA1:    " +
		 * Math.toDegrees(Math.atan2(R1.getY(),R1.getX())));
		 */

		/*
		 * double tetaDeg = Math.abs(Math.toDegrees(teta)); if(tetaDeg>90) tetaDeg =
		 * tetaDeg-90.0;
		 *
		 * if(teta<0) tetaDeg=-tetaDeg;
		 *
		 * teta=Math.toRadians(tetaDeg);
		 *
		 */
		// Reporting boudary point in the reference frame (R2) having x axis
		// aligned with the line joining the
		// R0, R1 and the grid point

		double[] R0inR2 = ReferenceFrameUtils.teta2DRotation(R0.toArray(), teta);
		double[] R1inR2 = ReferenceFrameUtils.teta2DRotation(R1.toArray(), teta);

		// //System.out.println("====================R0inR2: " + R0inR2[0] + " : "
		// + R0inR2[1] +" : " + R1inR2[0] + " : " + R1inR2[1]);

		// building
		double leftBoundary;
		double rightBoundary;

		if ((R0inR2[0] > 0) == (R1inR2[0] > 0)) // gridpoint outside the segment
												// R0R1
		{
			/*
			 * //System.out.println("WRONG R0: " + R0inR2[0] + ": R1: " + R1inR2[0] +
			 * ": beam: " + a.getBeamId()+ ": Side:" +
			 * FeasibilityConstants.getLookSideString(a.getLookSide())+ ": Orbit:" +
			 * FeasibilityConstants.getOrbitDirectionAsString(a. getOrbitDirection()) +
			 * ": dist1:" +R0gridPointDistance +": dist2:" +
			 * R1gridPointDistance+": offnadir:" + a.getOffNadir()+ ": beam near:" +
			 * a.getBeam().getNearOffNadir()+": far:"+a.getBeam().getFarOffNadir ());
			 */
			// //System.out.println("--------------------------------PIPPONE-------------------------");
			throw new Exception("wrong dto");
		}

		// boolean isOrange1Right=false;
		if (R0inR2[0] < R1inR2[0]) {
			leftBoundary = R0inR2[0];
			rightBoundary = R1inR2[0];
			// isOrange1Right=true;
		} else {
			leftBoundary = R1inR2[0];
			rightBoundary = R0inR2[0];
		}

		double acrossHalfDistance = FeasibilityConstants.half * this.spotlightDim2; // Across
		double alongHalfDistance = FeasibilityConstants.half * this.spotlightDim1;

		// Points over the line joining the beam boundary and passing for grid
		// po9nt (R2 plane)
		// Those points will dsefine the rectangle of DTO foot print
		double orange1;
		double orange2;

		if ((Math.abs(leftBoundary) > acrossHalfDistance) && (Math.abs(rightBoundary) > acrossHalfDistance)) {

			orange1 = -acrossHalfDistance;
			orange2 = acrossHalfDistance;

		} else if (Math.abs(leftBoundary) < acrossHalfDistance) {
			orange1 = leftBoundary;
			orange2 = leftBoundary + (2 * acrossHalfDistance);
		} else {
			orange1 = rightBoundary - (2 * acrossHalfDistance);
			orange2 = rightBoundary;
		}

		if (R0inR2[0] > 0) {
			double appo = orange1;
			orange1 = orange2;
			orange2 = appo;
		}

		// building sR2 spot corners case descending
		double[] c1 = { orange1, alongHalfDistance };
		double[] c2 = { orange2, alongHalfDistance };
		double[] c3 = { orange2, -alongHalfDistance };
		double[] c4 = { orange1, -alongHalfDistance };

		if (a.getOrbitDirection() == FeasibilityConstants.AscendingOrbit) {
			c1[1] = -alongHalfDistance;
			c2[1] = -alongHalfDistance;
			c3[1] = alongHalfDistance;
			c4[1] = alongHalfDistance;

		}

		// reporting corners in EN plane
		double[] c1EN = ReferenceFrameUtils.teta2DInverseRotation(c1, teta);
		double[] c2EN = ReferenceFrameUtils.teta2DInverseRotation(c2, teta);
		double[] c3EN = ReferenceFrameUtils.teta2DInverseRotation(c3, teta);
		double[] c4EN = ReferenceFrameUtils.teta2DInverseRotation(c4, teta);

		/*
		 * double [] c1EN = ReferenceFrameUtils.teta2DRotation(c1, -teta); double []
		 * c2EN = ReferenceFrameUtils.teta2DRotation(c2, -teta); double [] c3EN =
		 * ReferenceFrameUtils.teta2DRotation(c3, -teta); double [] c4EN =
		 * ReferenceFrameUtils.teta2DRotation(c4, -teta);
		 */
		// from ENU to ECEF

		double[] c1ENU = { c1EN[0], c1EN[1], 0 };
		double[] c2ENU = { c2EN[0], c2EN[1], 0 };
		double[] c3ENU = { c3EN[0], c3EN[1], 0 };
		double[] c4ENU = { c4EN[0], c4EN[1], 0 };

		double[] appo = ReferenceFrameUtils.enu2ecef(a.getGridPoint().getEcef().toArray(), c1ENU);
		double[] appo1 = ReferenceFrameUtils.ecef2llh(appo, true);

		corners[0] = appo1;

		appo = ReferenceFrameUtils.enu2ecef(a.getGridPoint().getEcef().toArray(), c2ENU);
		appo1 = ReferenceFrameUtils.ecef2llh(appo, true);
		corners[1] = appo1;

		appo = ReferenceFrameUtils.enu2ecef(a.getGridPoint().getEcef().toArray(), c3ENU);
		appo1 = ReferenceFrameUtils.ecef2llh(appo, true);
		corners[2] = appo1;

		appo = ReferenceFrameUtils.enu2ecef(a.getGridPoint().getEcef().toArray(), c4ENU);
		appo1 = ReferenceFrameUtils.ecef2llh(appo, true);
		corners[3] = appo1;

		return corners;
	}// end spotLightImageCorners

	/**
	 * Evaluate the square times (access and stop) and angle
	 *
	 * @throws Exception
	 */
	private void evaluateSqurePatameters() throws Exception {
		// evaluating early
		evaluateEarlySquareParameters();
		// evaluate late
		evaluateLateSquareParameters();

		if (this.nearOffNadir < this.farOffNadir) {

			if (this.squareStart > this.squareStop) {
				// //System.out.println("+++++++++++++++++STOP<START+++++++++++++++++++++++");
				// just throw
				// throw new Exception("Wrong DTO");

				double appo = this.squareStart;
				this.squareStart = this.squareStop;
				this.squareStop = appo;
				double[][] appoCorners = new double[4][3];
				appoCorners[0] = this.corners[0];
				appoCorners[1] = this.corners[1];
				appoCorners[2] = this.corners[2];
				appoCorners[3] = this.corners[3];

				this.corners[0] = appoCorners[3];
				this.corners[1] = appoCorners[2];
				this.corners[2] = appoCorners[1];
				this.corners[3] = appoCorners[0];

			} // end if
			/*
			 * else { throw new Exception("Wrong DTO"); }
			 */

		} // end if

		// Pezza per inversione
		if (this.nearOffNadir > this.farOffNadir) {

			// //System.out.println("___________________INVERSIONE______________________");

			// inversion
			double appo = this.nearOffNadir;
			this.nearOffNadir = this.farOffNadir;
			this.farOffNadir = appo;

			appo = this.squareStart;
			this.squareStart = this.squareStop;
			this.squareStop = appo;

			/// pezza per polo???
			if (this.squareStart > this.squareStop) {
				// just log
				throw new Exception("Wrong DTO");
			} // end if

			double[][] orderedCorners = new double[4][3];

			orderedCorners[0] = this.getThirdCorner();
			orderedCorners[1] = this.getFourtCorner();
			orderedCorners[2] = this.getFirstCorner();
			orderedCorners[3] = this.getSecondCorner();

			this.corners = orderedCorners;
		} // end if
	}// end method

	/**
	 * Evaluate late square times (square exita) and far off nadir of the spotlight
	 * square
	 *
	 * @throws Exception
	 */
	private void evaluateLateSquareParameters() throws Exception {
		this.farOffNadir = this.centeredAccess.getBeam().getFarOffNadir();
		double[] llh = new double[3];
		llh[0] = this.getThirdCorner()[0];
		llh[1] = this.getThirdCorner()[1];

		// llh[0]=dto.getSecondCorner()[0];
		// llh[1]=dto.getSecondCorner()[1];
		llh[2] = this.getMeanElevation();

		GridPoint p = new GridPoint(1, llh);
		this.getSquareParameters(p, false);
	}// end method

	/**
	 * Evaluate early square times (access a) and near off nadir of the spotlight
	 * square
	 *
	 * @throws Exception
	 */
	private void evaluateEarlySquareParameters() throws Exception {
		this.nearOffNadir = this.centeredAccess.getBeam().getNearOffNadir();
		double[] llh = new double[3];
		llh[0] = this.getFirstCorner()[0];
		llh[1] = this.getFirstCorner()[1];

		// llh[0]=dto.getSecondCorner()[0];
		// llh[1]=dto.getSecondCorner()[1];
		llh[2] = this.getMeanElevation();

		GridPoint p = new GridPoint(1, llh);
		this.getSquareParameters(p, true);
	}// end method

	/**
	 * Evaluate early square (squareStart and near offnadir) in case isEarly is true
	 * Evaluate late square (squareStop and far offnadir) in case isEarly is false
	 *
	 * @param point
	 * @param isEarly
	 * @throws Exception
	 */
	private void getSquareParameters(GridPoint point, boolean isEarly) throws Exception {
		// list
		List<Access> oldAccess = this.sat.getAccessList();
		// list
		List<Access> newAccess = new ArrayList<>();
		this.sat.setAccessList(newAccess);
		List<GridPoint> gpList = new ArrayList<>();
		gpList.add(point);

		AccessesEvaluator accessEvaluator = new AccessesEvaluator();

		// List<Satellite> satList = new ArrayList<Satellite>();
		// satList.add(dto.getSat());
		// evaluating accesses
		accessEvaluator.evaluateSatelliteAccesses(this.sat, gpList, null);
		// accessEvaluator.evaluateSatelliteAccesses(satList,
		// gpList,this.programmingRequest);

		double angle = 0;

		boolean isAccessTimeFound = false;
		// looping on accesses
		for (Access a : this.sat.getAccessList()) {
			if ((a.getAccessTime() > this.getStartTime()) && (a.getAccessTime() < this.getStopTime())) {
				isAccessTimeFound = true;
				if (isEarly) {
					this.squareStart = a.getAccessTime();
					this.orbitId = a.getOrbitId();
				} // end if
				else {
					this.squareStop = a.getAccessTime();
				} // end else
					// have exit from loop
				if (a.getBeamId().equals(this.getBeamId()) && (a.getLookSide() == this.getLookSide())) {
					angle = a.getOffNadir();
					break;
				} // end if
			} // end if

		} // end for

		if (!isAccessTimeFound) // No access has been found for that corner
		{
			// just throw

			throw new Exception("wrong dto");
		} // end if

		if (angle != 0) {
			if (isEarly) {
				this.nearOffNadir = angle;
			} // end if
			else {
				this.farOffNadir = angle;
			} // end else
		} // end if

		this.sat.setAccessList(oldAccess);

	}// end method

	/**
	 * Specialized method for spotlight dto Return the interferometric DTO null
	 * othrewise
	 *
	 * @param interferometricSat
	 * @param decorrelationTime
	 * @param decorrelationTolerance
	 * @param stopValidityTime
	 * @return
	 */
	@Override
	public DTO getInterferometricDTO(final Satellite interferometricSat, double decorrelationTime,
			double decorrelationTolerance, double stopValidityTime) {
		// returnig value
		SpotLightDTO retval = null;

		// sing the centered target point

		double epoch = this.centeredAccess.getAccessTime();

		// list
		List<Access> interferometricAccessList = new ArrayList<>();

		// int beamID = acc.getBeam().getIdBeam();
		// beam name
		String beamName = this.centeredAccess.getBeamId();
		int side = this.centeredAccess.getLookSide();
		int orbitDir = this.centeredAccess.getOrbitDirection();

		int basePointId = this.centeredAccess.getGridPoint().getId();

		/*
		 * double[][] interfcorners = new double[4][3];
		 * interfcorners[0][0]=this.corners[0][0];
		 * interfcorners[0][1]=this.corners[0][1];
		 * interfcorners[1][0]=this.corners[1][0];
		 * interfcorners[1][1]=this.corners[1][1];
		 */

		// looping on accesses
		for (Access a : interferometricSat.getAccessList()) {
			double difftime = a.getAccessTime() - epoch;
			double absDifftime = Math.abs(difftime);
			double difftime1 = Math.abs(absDifftime - decorrelationTime);

			int pointId = a.getGridPoint().getId();
			// check for decorrelation
			// if(difftime1 < decorrelationTolerance && side == a.getLookSide()
			// && beamName.equals(a.getBeamId()) &&
			// orbitDir==a.getOrbitDirection() &&
			// (basePointId==pointId) &&
			// checkOnPrecedenceForInterefometric(epoch, a.getAccessTime(),
			// decorrelationTime))
			if ((difftime1 < decorrelationTolerance) && (side == a.getLookSide()) && beamName.equals(a.getBeamId())
					&& (orbitDir == a.getOrbitDirection()) && (basePointId == pointId) && (epoch < a.getAccessTime())) {

				interferometricAccessList.add(a);
				retval = new SpotLightDTO(this);
				retval.setStartTime(this.startTime + difftime);
				retval.setStopTime(this.stopTime + difftime);

				retval.setSquareStart(this.squareStart + difftime);
				retval.setSquareStop(this.squareStop + difftime);

				retval.setOdrefBased(a.getOrbitType() == FeasibilityConstants.OdrefType);
                logger.debug("from getInterferometricDTO2");

				retval.setDtoAccessList(interferometricAccessList);

				retval.setMasterInterferometricDtoAccessList(this.dtoAccessList);

				retval.setCorners(this.corners);
				retval.setSat(interferometricSat);

				// Set the mean elevation used to evaluate SPRC INFO
				retval.meanElevation = this.getMeanElevation();
				retval.hasDTOMeanElevationEvaluated = true;
				retval.setSatPosAtStart(this.startPosition);
				retval.setSatPosAtEnd(this.endPosition);

				retval.setSatVelAtStart(this.startVelocity);
				retval.setSatVelAtSEnd(this.endVelocity);

				retval.orbitId = a.getOrbitId();
				retval.trackNumber = a.getSatellite().getTrackNumber(a.getOrbitId());

				try {
					// evaluating Epochs
					EpochBean epochAtStart = interferometricSat.getEpochAt(this.startTime + difftime);
					EpochBean epochAtStop = interferometricSat.getEpochAt(this.stopTime + difftime);
					// setting positions
					retval.setSatPosAtStart(epochAtStart.getoXyz());
					retval.setSatPosAtEnd(epochAtStop.getoXyz());
					// setting velocity
					retval.setSatVelAtStart(epochAtStart.getoVxVyVz());
					retval.setSatVelAtSEnd(epochAtStop.getoVxVyVz());

				} // end catch
				catch (FeasibilityException e) {
					this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.INFORMATION_INFO,
							"Error in evaluating Epochs for interferometric DTO. Use linked values if CSK otherwhise discard");
					/**
					 * Using the satellite PVT ofe base satellute for the interferometric one
					 */
					if (this.centeredAccess.getMissionName().equals(FeasibilityConstants.CSK_NAME)) {
						retval.setSatPosAtStart(this.startPosition);
						retval.setSatPosAtEnd(this.endPosition);
						retval.setSatVelAtStart(this.startVelocity);
						retval.setSatVelAtSEnd(this.endVelocity);
					} // end if
					else {
						// if CSG mission we could have problems in evaluating
						// sparc input
						// so discard the DTO
						retval = null;
					} // end else

				} // end catch

				// we can break the for here
				// because this is the only possibility to satisfy the
				// interferometric constraints
				break;

			} // end if

		} // end for

		// IF DTO stop id outside validity range or fall inside a paw it must be
		// discarded
		if (retval != null) {
			if ((retval.getStopTime() > stopValidityTime) || retval.checkForUndeferreblePaw()) {
				retval = null;
			}
		} // end if

		// returning
		return retval;

	}// end method

	/**
	 *
	 * @return the number of point falling inside the DTO
	 */
	public int getNumberOfPoint() {
		return this.dtoAccessList.size();
	}// end method

	/**
	 *
	 * @param list, list holding the accumuled point
	 * @return the number of new point falling inside the DTO
	 */
	public List<GridPoint> getNewPointNumber(List<String> list) {
		List<GridPoint> newPoints = new ArrayList<GridPoint>();
		// retval
		// int retval = 0;

		GridPoint p = null;
		// looping on accesses
		for (Access a : this.dtoAccessList) {
			p = a.getGridPoint();

			if (!list.contains(p.getUnivocalKey())) {
				// retval++;
				newPoints.add(p);
			} // end if
		} // end for

		// returning
		return newPoints;
	}// end method

}// end class
