/**
*
* MODULE FILE NAME:	AcqReq.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Class used modelize / perform operation on DTO
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
* 16-05-2016 | Amedeo Bancone  |1.1|evaluateLLHcorners visibility changed to protected
* --------------------------+------------+----------------+-------------------------------
* 20-02-2018 | Amedeo Bancone  |2.0| added meanElevation calculation
*                                    modified evaluation of the pointing module vector (getPointingModule)
*                                    added methods to returns the velocity of satellite at start and end of DTO
*                                    added parameters and methods to return value used to build the sample needed by SPARC
*                                    modified to take into account the evaluation of the orbitnumber and track number
*                                    modified to take into account passthrough
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
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.referencing.GeodeticCalculator;
import org.w3c.dom.Element;

import com.telespazio.csg.srpf.dataManager.bean.BeamBean;
import com.telespazio.csg.srpf.dataManager.bean.EpochBean;
import com.telespazio.csg.srpf.dataManager.bean.PlatformActivityWindowBean;
import com.telespazio.csg.srpf.dataManager.bean.SatellitePassBean;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.DateUtils;
import com.telespazio.csg.srpf.utils.ReferenceFrameUtils;
import com.telespazio.csg.srpf.utils.XMLUtils;

/**
 * Class implementing a DTO
 *
 * @author Amedeo Bancone
 * @version 2.0
 *
 *
 */
public class DTO implements Cloneable

{
	static final Logger logger = LogManager.getLogger(DTO.class.getName());

	/**
	 * Log
	 */
	private TraceManager tracer = new TraceManager();
	// static final Logger logger = LogManager.getLogger(DTO.class.getName());

	/**
	 * ID
	 */
	protected String id;

	/**
	 * Start Time
	 */
	protected double startTime;

	/**
	 * Stop Time
	 */
	protected double stopTime;

	/**
	 * True if odref based
	 */
	protected boolean isOdrefBased;

	/**
	 * sat start pos
	 */
	protected Vector3D startPosition;

	/**
	 * Sat stop pos
	 */
	protected Vector3D endPosition;

	/**
	 * Satellite vel at start
	 */
	protected Vector3D startVelocity;

	/**
	 * Sat vel at stop
	 */
	protected Vector3D endVelocity;

	/**
	 * Satellite
	 */
	protected Satellite sat = null;

	/**
	 * Mean elevation of DTO's scene
	 *
	 */
	protected double meanElevation = 0;

	/**
	 * True if the mean elevation has been evaluated
	 */
	boolean hasDTOMeanElevationEvaluated = false;

	/**
	 * Look side -100 means no evaluated (Fake value)
	 */
	protected int lookSide = -100;

	/*
	 * private double [] fourthCorner; private double [] thirdCorner; private double
	 * [] secondCorner; private double [] firstCorner;
	 */

	/**
	 * DTO Corners
	 */
	protected double[][] corners = new double[4][3];

	/**
	 * Strip on which dto has been evaluates
	 */
	protected Strip strip;

	/**
	 * List of accesses
	 */
	protected List<Access> dtoAccessList = new ArrayList<Access>();

	/**
	 * If the DTO is interferometrically linked to a master DTO this reference
	 * points to the accesslist of the master
	 */
	protected List<Access> masterInterferometricDtoAccessList = null;

	/**
	 * PAW
	 */
	protected PlatformActivityWindowBean paw = null;

	/**
	 * Usage factor
	 */
	protected double suf = 0;

	// section used for CSG beam, used in build saprc input
	/**
	 * number of sample in azimuth in case of standard product (in case of spotlight
	 * this shall be used) default value 2
	 */
	protected int numberOfSampleInAzimuthStandard = 2;

	/**
	 * number of sample in azimuth in case of extended product default 5
	 */
	protected int numberOfSampleInAzimuthExetnded = 5;

	/**
	 * limit defining a standard product extension expressed in millisec default
	 * 10000
	 */
	protected long standardProductTimeLimit = 10000;

	/**
	 * Number of sample used to evaluate the mean scene elevation for a given
	 * azimuth step
	 */
	protected int numberOfsampleInRangeForScene = 1;

	// parameter used to evaluate the nearOffnadir mean elevation

	/**
	 * Step in meter to evaluate the mean elevation at near offNadir
	 */
	protected int nearOffNadirStepLenght = 0;

	/**
	 * number of step for the half sampling interval
	 */
	protected int nearOfNadirNumberOfStep = 2;

	/**
	 * Step in meter to evaluate the mean elevation at far offNadir
	 */
	protected int farOffNadirStepLenght = 0;

	/**
	 * number of step for the half sampling interval
	 */
	protected int farOfNadirNumberOfStep = 2;

	/**
	 * Step in meter to evaluate the mean elevation at far subSatellitePoint
	 */
	protected int subSatelliteStepLenght = 0;

	/**
	 * number of step for the half sampling interval
	 */
	protected int subSatelliteNumberOfStep = 2;
	// End section for SPARC Evaluation parameters
	/////////////////////////////////////////////////////////////////

	/**
	 * Near off Nadir in deg
	 */
	protected double nearOffNadir = 0;

	/**
	 * Far off Nadir
	 */
	protected double farOffNadir = 0;

	/**
	 * if true the dto has been built on ODREF based DTO expansion
	 */
	protected boolean isExpandedDTO = false;

	/**
	 * orbit id evaluated on the basis of the algo specified by pacaccio
	 */
	protected long orbitId = 0;

	/**
	 * track number evaluated
	 *
	 */
	protected long trackNumber = 0;

	/**
	 * Name of the satellite
	 */
	protected String satName = "";

	/**
	 * Orbit direction a
	 */
	protected int orbitDirection = -100;

	/**
	 * SPARC info
	 */
	protected String sparcInfo = "";

	/**
	 * AR Element in XML Tree whose DTO belongs used during the refinement phase
	 */
	protected Element arElement = null;

	/**
	 * Element of DTO in xml tree
	 */
	protected Element dtoElement = null;

	/**
	 * Mission Name
	 */
	protected String missionName = "";

	/**
	 * True if DTO coul be refined
	 */
	protected boolean isRefinable = false;

	/**
	 * used in refinement to byuild input sparc
	 */
	protected String polarization = "";

	/**
	 * Used in refinement to byuild input sparc
	 *
	 */
	protected String sarMode = "";

	/**
	 * Used in refinement to byuild input sparc
	 */
	public String sarBeamName = "";

	/**
	 * stereo lists
	 */
	protected ArrayList<DTO> slaveDTOList = new ArrayList<>();
	/**
	 * Stereo list master
	 */
	protected ArrayList<DTO> masterDTOList = new ArrayList<>();

	/**
	 * True if master in a stereo pair
	 */
	protected boolean master = false;

	/**
	 * True if stereo paired
	 */
	protected boolean stereoLincked = false;

	// element for passThrough

	/**
	 * Data volume. It should be evaluated by sparc
	 */
	protected double dtoSize = 0;

	// DWL speed. it sould be in megabit / Seconds
	// protected double dwlSpeed=260.0;

	/**
	 *
	 * @return the access list of the master interferometric DTO if any null
	 *         otherwise
	 */
	public List<Access> getMasterInterferometricDtoAccessList() {
		return this.masterInterferometricDtoAccessList;
	}// end method

	/**
	 * Set the masterInterferometricDtoAccessList
	 *
	 * @param masterInterferometricDtoAccessList
	 */
	public void setMasterInterferometricDtoAccessList(List<Access> masterInterferometricDtoAccessList) {
		this.masterInterferometricDtoAccessList = masterInterferometricDtoAccessList;
	}// end method

	/**
	 * set the amount of data to be stored in taking the DTO
	 *
	 * @param volume
	 */
	protected void setDtoSize(double volume) {
		this.dtoSize = volume;
	}// end method

	/**
	 * the foreseen duration of dwl in julian format
	 *
	 * @return the foreseen duration of dwl in julian format
	 */
	protected double getDWLDuration() {
		double retval = 0;
		// evaluating seconds
		long seconds = Math.round(this.dtoSize / FeasibilityConstants.DWLSpeed) + 1;
		// transforming in julian
		retval = DateUtils.secondsToJulian(seconds);
		return retval;
	}// end method

	/**
	 * return true if stereo linked with other dtos
	 *
	 * @return true if stereo linked
	 */
	public boolean isStereoLincked() {
		return this.stereoLincked;
	}// end method

	/**
	 * set true if the DTO as a stereo link
	 *
	 * @param stereoLincked
	 */
	public void setStereoLincked(boolean stereoLincked) {
		this.stereoLincked = stereoLincked;
	}// end method

	/**
	 * Return the list of slave DTO
	 *
	 * @return stereo slaves list
	 */
	public ArrayList<DTO> getSlaveDTOList() {
		return this.slaveDTOList;
	}// end method

	/**
	 * set the slave list in streo mode
	 *
	 * @param slaveDTOList
	 */
	public void setSlaveDTOList(ArrayList<DTO> slaveDTOList) {
		this.slaveDTOList = slaveDTOList;
	}// end method

	/**
	 * return the list of masters
	 *
	 * @return master stereo list
	 */
	public ArrayList<DTO> getMasterDTOList() {
		return this.masterDTOList;
	}// end method

	/**
	 * set the list of master
	 *
	 * @param masterDTOList
	 */
	public void setMasterDTOList(ArrayList<DTO> masterDTOList) {
		this.masterDTOList = masterDTOList;
	}// end method

	/**
	 * return true if master
	 *
	 * @return true id master stereo
	 */
	public boolean isMaster() {
		return this.master;
	}// end method

	/**
	 * set the master flag
	 *
	 * @param master
	 */
	public void setMaster(boolean master) {
		this.master = master;
	}// end method

	/**
	 * add a DTO d to the master list
	 *
	 * @param d
	 */
	public void addDTOToStereoMasterList(DTO d) {
		this.masterDTOList.add(d);
	}// end method

	/**
	 * add a DTO d to slave list
	 *
	 * @param d
	 */
	public void addDTOToStereoSlaveList(DTO d) {
		this.slaveDTOList.add(d);
	}// end method

	/**
	 * Used only during refinement to build SPARC input
	 *
	 * @return sar beam beme
	 */
	public String getSarBeamName() {
		String returnSarBeamName = null;
		if (this.dtoAccessList.size() != 0) {
			/**
			 * The name of beam from the first acces
			 */
			this.sarBeamName = this.dtoAccessList.get(0).getBeamId();
			returnSarBeamName = this.sarBeamName;
		}
		else
		{
			this.sarBeamName =returnSarBeamName;
		}
		return returnSarBeamName;
	}// end method

	/**
	 * Used only during refinement to build SPARC input
	 *
	 * @param sarBeamName
	 */
	public void setSarBeamName(String sarBeamName) {
		this.sarBeamName = sarBeamName;
	}

	/**
	 * Used only during refinement to build SPARC input
	 *
	 * @return sarmode
	 */
	public String getSarMode() {
		if (this.dtoAccessList.size() != 0) {
			/**
			 * The sarBean
			 */
			this.sarMode = this.dtoAccessList.get(0).getBeam().getSensorModeName();
		}

		return this.sarMode;
	}// end method

	/***
	 * Used only during refinement to build SPARC input
	 *
	 * @param sarMode
	 */
	public void setSarMode(String sarMode) {
		this.sarMode = sarMode;
	}// end method

	/**
	 * Used only during refinement to build SPARC input
	 *
	 * @return polarization
	 */
	public String getPolarization() {
		return this.polarization;
	}// end method

	/**
	 * Set the polarization
	 *
	 * @param polarization
	 */
	public void setPolarization(String polarization) {
		this.polarization = polarization;
	}// end method

	/**
	 *
	 * @return true if the DTO is refinable false otherwise
	 */
	public boolean isRefinable() {
		return this.isRefinable;
	}// end method

	/**
	 * Set the flag for refinability
	 *
	 * @param isRefinable
	 */
	public void setRefinable(boolean isRefinable) {
		logger.debug("###############################################  setRefinable  " + isRefinable);
		this.isRefinable = isRefinable;
	}// end method

	/**
	 *
	 * @return the name of the mission
	 */
	public String getMissionName() {
		String retval = this.missionName;

		if ((this.dtoAccessList != null) && !this.dtoAccessList.isEmpty()) {
			/**
			 * From the first access
			 */
			retval = this.dtoAccessList.get(0).getMissionName();
		}

		return retval;
	}// end method

	/**
	 * Return the dto element in the xml tree Used in refinement
	 *
	 * @return
	 */
	public Element getDtoElement() {
		return this.dtoElement;
	}// end method

	/**
	 * Return the AR Element in the XML tree whose the dto belongs used in
	 * refinement
	 *
	 * @return
	 */
	public Element getARElement() {
		return this.arElement;
	}// end method

	/**
	 * Set the Element whose DTO belongs in xml tree
	 *
	 * @param ar
	 */
	public void setARElement(Element ar) {
		this.arElement = ar;
	}// end method

	/**
	 * Clone the DTO
	 */
	@Override
	public DTO clone() {
		DTO d = new DTO();
		/**
		 * Copying parameters
		 */
		copyParameters(d);

		return d;
	}// end method

	/**
	 * Copy the parameters of the DTO to d
	 *
	 * @param d
	 */
	protected void copyParameters(DTO d) {

		/**
		 *
		 * Copying parameters
		 */
		d.id = this.id;
		d.startTime = this.getStartTime();
		d.stopTime = this.getStopTime();
		d.isOdrefBased = this.isOdrefBased;
		d.startPosition = new Vector3D(this.startPosition.toArray());
		d.endPosition = new Vector3D(this.endPosition.toArray());
		d.startVelocity = new Vector3D(this.startVelocity.toArray());
		d.endVelocity = new Vector3D(this.endVelocity.toArray());
		d.sat = this.sat;
		d.meanElevation = this.meanElevation;
		d.hasDTOMeanElevationEvaluated = this.hasDTOMeanElevationEvaluated;

		d.lookSide = this.lookSide;

		// corners

		for (int i = 0; i < d.corners.length; i++) {
			/**
			 * Copying corners
			 */
			for (int j = 0; j < d.corners[i].length; j++) {
				d.corners[i][j] = this.corners[i][j];
			}
		} // end for

		/**
		 *
		 * Copying the rest of parameters
		 *
		 *
		 *
		 */
		d.strip = this.strip;
		d.dtoAccessList = this.dtoAccessList;
		d.paw = this.paw;
		d.suf = this.suf;

		d.numberOfSampleInAzimuthStandard = this.numberOfSampleInAzimuthStandard;
		d.numberOfSampleInAzimuthExetnded = this.numberOfSampleInAzimuthExetnded;

		d.standardProductTimeLimit = this.standardProductTimeLimit;
		d.numberOfsampleInRangeForScene = this.numberOfsampleInRangeForScene;

		d.nearOffNadirStepLenght = this.nearOffNadirStepLenght;
		d.nearOfNadirNumberOfStep = this.nearOfNadirNumberOfStep;

		d.farOffNadirStepLenght = this.farOffNadirStepLenght;
		d.farOfNadirNumberOfStep = this.farOfNadirNumberOfStep;

		d.subSatelliteStepLenght = this.subSatelliteStepLenght;
		d.subSatelliteNumberOfStep = this.subSatelliteNumberOfStep;

		d.nearOffNadir = this.nearOffNadir;
		d.farOffNadir = this.farOffNadir;

		d.isExpandedDTO = this.isExpandedDTO;

		/**
		 * Orbit id
		 */
		d.orbitId = this.orbitId;

		/**
		 * Track number
		 */
		d.trackNumber = this.trackNumber;

		d.satName = this.satName;

		d.orbitDirection = this.orbitDirection;

		d.sparcInfo = this.sparcInfo;

	}// end method

	/**
	 *
	 * @return Sprc Info
	 */
	public String getSparcInfo() {
		return this.sparcInfo;
	}// end method

	/**
	 * Set the sparc Info
	 *
	 * @param sparcInfo
	 */
	public void setSparcInfo(String sparcInfo) {

		this.sparcInfo = sparcInfo;
	}// end method

	/**
	 * Set the staName
	 *
	 * @param satName
	 */
	public void setSatName(String satName) {
		this.satName = satName;
	}// end method

	/**
	 * Return true if he dto has been built on ODREF based DTO expansion
	 *
	 * @return
	 */
	public boolean isExpandedDTO()

	{
		return this.isExpandedDTO;
	}// end method

	/**
	 *
	 * Set true if he dto has been built on ODREF based DTO expansion
	 *
	 * @param isExpandedDTO
	 */
	public void setExpandedDTO(boolean isExpandedDTO) {
		this.isExpandedDTO = isExpandedDTO;
	}// end method

	/**
	 *
	 * @return number of step in azimuth for standard product
	 */
	public int getNumberOfSampleInAzimuthStandard() {
		return this.numberOfSampleInAzimuthStandard;
	}// end method

	/**
	 * Set the number of sample for standard product in azimuth
	 *
	 * @param numberOfSampleInAzimuthStandard
	 */
	public void setNumberOfSampleInAzimuthStandard(int numberOfSampleInAzimuthStandard) {
		this.numberOfSampleInAzimuthStandard = numberOfSampleInAzimuthStandard;
	}// end method

	/**
	 *
	 * @return number of step in azimuth for extended product
	 */
	public int getNumberOfSampleInAzimuthExetnded() {
		return this.numberOfSampleInAzimuthExetnded;
	}// end method

	/**
	 * Set the number of sample for extended product in azimuth
	 *
	 * @param numberOfSampleInAzimuthExetnded
	 */
	public void setNumberOfSampleInAzimuthExetnded(int numberOfSampleInAzimuthExetnded) {
		this.numberOfSampleInAzimuthExetnded = numberOfSampleInAzimuthExetnded;
	}// end method

	/**
	 *
	 * @return the maximun lenght of a standard product expressed in seconds
	 */
	public long getStandardProductExtensionLimit()

	{
		return this.standardProductTimeLimit;
	}// end method

	/**
	 * set the maximum extension of a standard product expressed in seconds
	 *
	 * @param standardProductExtensionLimit
	 */
	public void setStandardProductExtensionLimit(long standardProductExtensionLimit) {
		this.standardProductTimeLimit = standardProductExtensionLimit;
	}// end method

	/**
	 *
	 * @return the number of sample to evaluate the mean elevation of the scene at a
	 *         given azimuth position
	 */
	public int getNumberOfsampleInRangeForScene()

	{
		String beamId = this.getBeamId();

		/**
		 * retrieveing from beam db If no configured the default is returned
		 */
		int retval = SparcBeamDB.getInstance().getNumberOfsampleInRangeForScene(beamId);

		/**
		 * If no configured the default is returned
		 */
		if (retval < 0) {
			retval = this.numberOfsampleInRangeForScene;
		}
		return retval;
	}// end method

	/**
	 * Set the number of sample to evaluate the mean elevation of the scene at a
	 * given azimuth position default value
	 *
	 * @param numberOfsampleInRangeForScene
	 */
	public void setNumberOfsampleInRangeForScene(int numberOfsampleInRangeForScene) {
		this.numberOfsampleInRangeForScene = numberOfsampleInRangeForScene;
	}// end method

	/**
	 *
	 * @return the lenght of the step (meters) to evaluate the mean elevation at
	 *         near off nadir
	 */
	public int getNearOffNadirStepLenght() {
		String beamId = this.getBeamId();
		/**
		 * retrieveing from beam db If no configured the default is returned
		 */
		int retval = SparcBeamDB.getInstance().getNearOffNadirStepLenght(beamId);
		if (retval < 0) {
			/**
			 * If no configured the default is returned
			 */
			retval = this.nearOffNadirStepLenght;
		}
		return retval;

	}// end method

	/**
	 * Set the lenght of step (meters) to evaluate the mean elevation at near off
	 * nadir
	 *
	 * @param nearOffNadirStepLenght
	 */
	public void setNearOffNadirStepLenght(int nearOffNadirStepLenght)

	{
		this.nearOffNadirStepLenght = nearOffNadirStepLenght;
	}// end method

	/**
	 *
	 * @return the number of step (half interval) to evaluate the mean elevation at
	 *         near off nadir
	 */
	public int getNearOfNadirNumberOfStep()

	{
		String beamId = this.getBeamId();
		/**
		 * retrieveing from beam db If no configured the default is returned
		 */
		int retval = SparcBeamDB.getInstance().getNearOfNadirNumberOfStep(beamId);
		if (retval < 0) {
			/**
			 * If no configured the default is returned
			 */
			retval = this.nearOfNadirNumberOfStep;
		}
		return retval;

	}// end method

	/**
	 * Set the number of step (half interval) to evaluate the mean elevation at near
	 * off nadir
	 *
	 * @param nearOfNadirNumberOfStep
	 */
	public void setNearOfNadirNumberOfStep(int nearOfNadirNumberOfStep) {
		this.nearOfNadirNumberOfStep = nearOfNadirNumberOfStep;
	}

	/**
	 *
	 * @return the lenght of the step (meters) to evaluate the mean elevation at far
	 *         off nadir
	 */
	public int getFarOffNadirStepLenght() {
		String beamId = this.getBeamId();
		/**
		 * retrieveing from beam db If no configured the default is returned
		 */
		int retval = SparcBeamDB.getInstance().getFarOffNadirStepLenght(beamId);
		if (retval < 0) {
			/**
			 * If no configured the default is returned
			 */
			retval = this.farOffNadirStepLenght;
		}
		return retval;

	}// end method

	/**
	 * Set the lenght of the step (meters) to evaluate the mean elevation at near
	 * off nadir
	 *
	 * @param farOffNadirStepLenght
	 */
	public void setFarOffNadirStepLenght(int farOffNadirStepLenght) {
		this.farOffNadirStepLenght = farOffNadirStepLenght;
	}// end method

	/**
	 *
	 * @return the number of step (half interval) to evaluate the mean elevation at
	 *         far off nadir
	 */
	public int getFarOfNadirNumberOfStep() {
		String beamId = this.getBeamId();
		/**
		 * retrieveing from beam db If no configured the default is returned
		 */
		int retval = SparcBeamDB.getInstance().getFarOffNadirNumberOfStep(beamId);
		if (retval < 0) {
			/**
			 * If no configured the default is returned
			 */
			retval = this.farOfNadirNumberOfStep;
		}
		return retval;

	}// end method

	/**
	 * Set the number of step (half interval) to evaluate the mean elevation at far
	 * off nadir
	 *
	 * @param farOfNadirNumberOfStep
	 */
	public void setFarOfNadirNumberOfStep(int farOfNadirNumberOfStep) {
		this.farOfNadirNumberOfStep = farOfNadirNumberOfStep;
	}

	/**
	 *
	 * @return the lenght of the step (meters) to evaluate the mean elevation at sub
	 *         satellite point
	 */
	public int getSubSatelliteStepLenght() {
		String beamId = this.getBeamId();
		/**
		 * retrieveing from beam db If no configured the default is returned
		 */
		int retval = SparcBeamDB.getInstance().getSubSatelliteStepLengh(beamId);
		if (retval < 0) {
			/**
			 * If no configured the default is returned
			 */
			retval = this.subSatelliteStepLenght;
		}
		return retval;

	}// end method

	/**
	 * Set the lenght of the step (meters) to evaluate the mean elevation at sub
	 * satellite point
	 *
	 * @param subSatelliteStepLenght
	 */
	public void setSubSatelliteStepLenght(int subSatelliteStepLenght) {
		this.subSatelliteStepLenght = subSatelliteStepLenght;
	}// end method

	/**
	 *
	 * @return the number of step to evaluate the mean elevation at sub satellite
	 *         point
	 */
	public int getSubSatelliteNumberOfStep() {
		String beamId = this.getBeamId();
		/**
		 * retrieveing from beam db If no configured the default is returned
		 */
		int retval = SparcBeamDB.getInstance().getSubSatelliteNumberOfStep(beamId);
		if (retval < 0) {

			retval = this.subSatelliteNumberOfStep;
		}
		return retval;

	}// end method

	/**
	 * Return the number of azimth step for evaluating the sparc input.
	 *
	 * @return
	 */
	int getNumberOfAzimuthSampleStep() {

		String beamId = this.getBeamId();

		/**
		 * retrieveing from beam db If no configured the default is returned
		 */

		/**
		 * to be used for non extended data
		 */
		int numberOfStandard = SparcBeamDB.getInstance().getNumberOfSampleInAzimuthStandard(beamId);
		/**
		 * Used for extended DTO
		 */
		int nunberOfExtended = SparcBeamDB.getInstance().getNumberOfSampleInAzimuthExtended(beamId);
		int retval;
		if (numberOfStandard < 0) {
			retval = this.numberOfSampleInAzimuthStandard;
		} else {
			retval = numberOfStandard;
		}

		/**
		 * Evaluating duration
		 */
		double duration = this.getStopTime() - this.getStartTime();

		/**
		 * retrieveing from beam db If no configured the default is used
		 */
		int standardProductDuration = SparcBeamDB.getInstance().getStandardProductTimeLimit(beamId);
		double standardProductLimitAsJulian;
		if (standardProductDuration < 0) {
			standardProductLimitAsJulian = DateUtils.millisecondsToJulian(this.standardProductTimeLimit);
		} else {
			standardProductLimitAsJulian = DateUtils.millisecondsToJulian(standardProductDuration);
		}

		/**
		 * Extended DTO in this case the number of extended will be used
		 */
		if (duration > standardProductLimitAsJulian) {
			if (nunberOfExtended < 0) {
				retval = this.numberOfSampleInAzimuthExetnded;
			} else {
				retval = nunberOfExtended;
			}
		}

		return retval;

	}// end methods

	/**
	 * Set the number of step to evaluate the mean elevation at sub satellite point
	 *
	 * @param subSatelliteNumberOfStep
	 */
	public void setSubSatelliteNumberOfStep(int subSatelliteNumberOfStep) {
		this.subSatelliteNumberOfStep = subSatelliteNumberOfStep;
	}// end method

	/**
	 * Set satellite
	 *
	 * @param sat
	 */
	public void setSat(Satellite sat) {
		this.sat = sat;
	}// end method

	/**
	 *
	 * @return the DTO satellite
	 */
	public Satellite getSat() {
		return this.sat;
	}// end method

	/**
	 * Constructor
	 */
	public DTO()

	{

	}// end method

	/**
	 * Build a DTO from a DOM NODE basically used in Refinement and Extension
	 *
	 * @param dtoel
	 * @throws XPathExpressionException
	 * @throws GridException
	 */
	public DTO(Element dtoel) throws XPathExpressionException, GridException {
		/**
		 * Build DTO from A dom node
		 */
		this.dtoElement = dtoel;

		/**
		 * Building time info satellite and mission
		 */
		String satellite = XMLUtils.getChildElementText(dtoel, FeasibilityConstants.SatelliteTagName,
				FeasibilityConstants.SatelliteTagNameNS);
		String startTime = XMLUtils.getChildElementText(dtoel, FeasibilityConstants.timeStartTagName,
				FeasibilityConstants.timeStartTagNameNS);
		String timeStop = XMLUtils.getChildElementText(dtoel, FeasibilityConstants.timeStopTagName,
				FeasibilityConstants.timeStopTagNameNS);

		this.setSatName(satellite);
		this.setStartTime(DateUtils.fromISOToCSKDate(startTime));
		this.setStopTime(DateUtils.fromISOToCSKDate(timeStop));

		this.missionName = XMLUtils.getChildElementText(dtoel, FeasibilityConstants.MissionTagName,
				FeasibilityConstants.MissionTagNameNS);

		/**
		 * Building geometric info
		 */
		String posList = XMLUtils.getChildElementText(dtoel, FeasibilityConstants.PosListTagName,
				FeasibilityConstants.PosListTagNameNS);
		buildCornersFromPosList(posList);

		/**
		 * Building id
		 *
		 * orbit info
		 *
		 * side info
		 */
		String idString = XMLUtils.getChildElementText(dtoel, FeasibilityConstants.DTOIdTagName,
				FeasibilityConstants.DTOIdTagNameNS);
		// this.id=Integer.parseInt(idString);

		this.id = idString;

		String orbitDirectionAsString = XMLUtils.getChildElementText(dtoel, FeasibilityConstants.orbitDirectionTagName,
				FeasibilityConstants.orbitDirectionTagNameNS);
		if (orbitDirectionAsString.equalsIgnoreCase(FeasibilityConstants.AscendingOrbitAsString)) {
			this.orbitDirection = FeasibilityConstants.AscendingOrbit;
		} else {
			this.orbitDirection = FeasibilityConstants.DescendingOrbit;
		}

		String lookSideString = XMLUtils.getChildElementText(dtoel, FeasibilityConstants.LookSideTagName,
				FeasibilityConstants.LookSideTagNameNS);

		this.lookSide = FeasibilityConstants.getLookSideValue(lookSideString);

		// //System.out.println("==================================================
		// " + lookSideString);
		/**
		 * Building sensor info
		 */
		this.polarization = XMLUtils.getChildElementText(dtoel, FeasibilityConstants.PolarizationTagName,
				FeasibilityConstants.PolarizationTagNameNS);
		this.sarBeamName = XMLUtils.getChildElementText(dtoel, FeasibilityConstants.BeamIdTagName,
				FeasibilityConstants.BeamIdTagNameNS);
		this.sarMode = XMLUtils.getChildElementText(dtoel, FeasibilityConstants.SensorModeTagName,
				FeasibilityConstants.SensorModeTagNameNS);

		this.sparcInfo = XMLUtils.getChildElementText(dtoel, FeasibilityConstants.SPARCInfoTagName,
				FeasibilityConstants.SPARCInfoTagNameNS);

	}// End construuctor

	/**
	 * Build the four corners starting form a poslist string
	 *
	 * @param posList
	 * @throws GridException
	 */
	protected void buildCornersFromPosList(String posList) throws GridException {
		/**
		 * Tokeinze poslist
		 */
		StringTokenizer tokens = new StringTokenizer(posList);

		if ((tokens.countTokens() == 0) || (((tokens.countTokens()) % 2) != 0)) // No
																				// elements
																				// or
																				// spare
																				// elements
		{
			/**
			 * Number of tokens is not right
			 */
			throw new GridException("Wrong Polygon");
		}

		int numberOfCorner = (tokens.countTokens() / 2) - 1;
		double currentLatitude;
		double currentLongitude;

		for (int i = 0; i < numberOfCorner; i++) {
			/**
			 * building corners from tokens
			 */
			currentLatitude = Double.valueOf(tokens.nextToken());
			currentLongitude = Double.valueOf(tokens.nextToken());

			this.corners[i][0] = currentLatitude;
			this.corners[i][1] = currentLongitude;

			/**
			 * Dem not used here
			 */
			this.corners[i][2] = 0;

		} // end for

	}// end method

	/**
	 * return the list of Access belonging the DTO
	 *
	 * @return
	 */
	public List<Access> getDtoAccessList() {
		return this.dtoAccessList;
	}// end method

	/**
	 * set the dto accesslist
	 *
	 * @param dtoAccessList
	 */
	public void setDtoAccessList(List<Access> dtoAccessList) {
		this.dtoAccessList = dtoAccessList;
		logger.debug("SET DTO ACCESS LIST : "+this.dtoAccessList.size());

	}// end method

	/**
	 * IF decorrelation time == 8 days or decorrelation time == 16 days, we force to
	 * use interferometric DTO in future resprect to the base DTO
	 *
	 * @param baseAccess
	 * @param interfAccess
	 * @param decorrelationTime
	 * @return
	 *
	 *         protected boolean checkOnPrecedenceForInterefometric(double
	 *         baseAccess,double interfAccess,double decorrelationTime) { //IF
	 *         decorrelation time == 8 days or decorrelation time == 16 days, //we
	 *         force to use interferometric DTO in future resprect to the base DTO
	 *         boolean retval=true;
	 *         if(decorrelationTime==DateUtils.secondsToJulian(86400*8) ||
	 *         decorrelationTime==DateUtils.secondsToJulian(86400*16)) {
	 *         retval=(baseAccess<interfAccess); }//end if
	 *
	 *         return retval; }//end checkOnPrecedenceForInterefometric
	 */

	/**
	 * Return the interferometric DTO null othrewise
	 *
	 * @param interferometricSat
	 * @param decorrelationTime
	 * @param decorrelationTolerance
	 * @param stopValidityTime
	 * @return Interferometric DTO or null
	 */
	public DTO getInterferometricDTO(final Satellite interferometricSat, double decorrelationTime,
			double decorrelationTolerance, double stopValidityTime) {

		DTO retval = null;

		// int middleAccessIndex = this.dtoAccessList.size()/2;
		/**
		 * Access used in decorrelation activities
		 */
		Access acc = this.dtoAccessList.get(0);

		/**
		 * Time of the access
		 */
		double epoch = acc.getAccessTime();

		int basePointId = acc.getGridPoint().getId();

		/**
		 * List of interferometric accesses
		 */
		List<Access> interferometricAccessList = new ArrayList<>();

		// int beamID = acc.getBeam().getIdBeam();

		/**
		 * Retrieving beam side orbit direction
		 */
		String beamName = acc.getBeamId();
		int side = acc.getLookSide();
		int orbitDir = acc.getOrbitDirection();

		/*
		 * double[][] interfcorners = new double[4][3];
		 * interfcorners[0][0]=this.corners[0][0];
		 * interfcorners[0][1]=this.corners[0][1];
		 * interfcorners[1][0]=this.corners[1][0];
		 * interfcorners[1][1]=this.corners[1][1];
		 */

		/**
		 * Iterating on access of coupled satellite
		 */
		for (Access a : interferometricSat.getAccessList()) {
			/**
			 * Evaluating time difference
			 */
			double difftime = a.getAccessTime() - epoch;
			double absDifftime = Math.abs(difftime);
			double difftime1 = Math.abs(absDifftime - decorrelationTime);

			int pointId = a.getGridPoint().getId();

			// if(difftime1 < decorrelationTolerance && side == a.getLookSide()
			// && beamName.equals(a.getBeamId()) &&
			// orbitDir==a.getOrbitDirection() &&
			// (basePointId==pointId) &&
			// checkOnPrecedenceForInterefometric(epoch, a.getAccessTime(),
			// decorrelationTime))
			if ((difftime1 < decorrelationTolerance) && (side == a.getLookSide()) && beamName.equals(a.getBeamId())
					&& (orbitDir == a.getOrbitDirection()) && (basePointId == pointId) && (epoch < a.getAccessTime())) {
				/**
				 * current access satisfy decorrelation constraint has the same beam orbit and
				 * side of the base access so it coul be added to the interferometric access
				 * list
				 */

				interferometricAccessList.add(a);

				/**
				 * Building interferometric DTO
				 */
				retval = new DTO(this);

				retval.setStartTime(this.startTime + difftime);
				retval.setStopTime(this.stopTime + difftime);
				retval.setOdrefBased(a.getOrbitType() == FeasibilityConstants.OdrefType);
                logger.debug("from getInterferometricDTO1");

				retval.setDtoAccessList(interferometricAccessList);

				retval.setMasterInterferometricDtoAccessList(this.dtoAccessList);

				retval.setCorners(this.corners);
				retval.setSat(interferometricSat);

				// Set the mean elevation used to evaluate SPRC INFO
				retval.meanElevation = this.getMeanElevation();
				retval.hasDTOMeanElevationEvaluated = true;

				retval.orbitId = a.getOrbitId();
				retval.trackNumber = a.getSatellite().getTrackNumber(a.getOrbitId());

				// Settinng fake value for start/stop velocity start/stop
				// position

				try {
					/**
					 * Recalculating satellite PVT
					 */
					EpochBean epochAtStart = interferometricSat.getEpochAt(this.startTime + difftime);
					EpochBean epochAtStop = interferometricSat.getEpochAt(this.stopTime + difftime);

					retval.setSatPosAtStart(epochAtStart.getoXyz());
					retval.setSatPosAtEnd(epochAtStop.getoXyz());
					retval.setSatVelAtStart(epochAtStart.getoVxVyVz());
					retval.setSatVelAtSEnd(epochAtStop.getoVxVyVz());

					// retval.evaluateLLHcorners(satPos, satVel)

				} // end cacth
				catch (FeasibilityException e) {
					this.tracer.warning(EventType.SOFTWARE_EVENT, ProbableCause.INFORMATION_INFO,
							"Error in evaluating Epochs for interferometric DTO. Use linked values if CSK otherwhise discard");
					/**
					 * Using the satellite PVT ofe base satellute for the interferometric one
					 */
					if (acc.getMissionName().equals(FeasibilityConstants.CSK_NAME)) {
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

				} // end catche

				// we can break the for here
				// because this is the only possibility to satisfy the
				// interferometric constraints
				break;

			} // end if

		} // end for

		// IF DTO stop id outside validity range or fall inside a paw it must be
		// discarded
		if (retval != null) {
			logger.debug("from getInterferometricDTO");
			logger.trace("MODIFICA DTO : setDtoAccessList " + retval.getDtoAccessList());

			/**
			 * No interferometric so null will bw returned
			 */
			if ((retval.getStopTime() > stopValidityTime) || retval.checkForUndeferreblePaw()) {
				retval = null;
			}
		}

		return retval;
	}// end getInterferometricDTO

	/**
	 * Copy constructor
	 *
	 * @param d
	 */
	public DTO(DTO d) {
		/**
		 *
		 * Copying
		 *
		 * parameters
		 *
		 */
		this.id = d.id;
		this.startTime = d.startTime;
		this.stopTime = d.stopTime;
		this.isOdrefBased = d.isOdrefBased;
		this.corners = d.corners;
		this.strip = d.strip;
		this.dtoAccessList = d.dtoAccessList;
		this.sat = d.sat;
		this.suf = d.suf;
		this.nearOffNadir = d.getNearOffnadir();
		this.farOffNadir = d.getFarOffNadir();
		this.orbitId = d.getOrbitId();
		this.trackNumber = d.trackNumber;
		this.lookSide = d.lookSide;
		this.orbitDirection = d.orbitDirection;
		this.meanElevation = d.getMeanElevation();
		this.hasDTOMeanElevationEvaluated = true;
		this.isExpandedDTO = d.isExpandedDTO;

	}// end method

	/**
	 * If the DTO fall inside a pw this method return the relevan paw otherwise null
	 *
	 * @return the paw if exists otherwise null;
	 */
	public PlatformActivityWindowBean getPaw() {
		return this.paw;
	}// end method

	/**
	 * return the suf of the DTO
	 *
	 * @return the suf of DTO
	 *
	 */
	public double getSuf() {
		return this.suf;
	}// end method

	/**
	 * Set the suf of DTO
	 *
	 * @param suf
	 */
	public void setSuf(double suf) {
		this.suf = suf;
	}// end method

	/**
	 *
	 * @param id
	 * @param startTime
	 * @param stopTime
	 * @param isOdrefBased
	 * @param lastAccessNearFarCorner
	 * @param lastAccessFarCorner
	 * @param firstFarCorner
	 * @param firstAccessNearCorner
	 * @param tracknumber
	 * @param orbitId
	 *
	 */
	public DTO(String id, double startTime, double stopTime, boolean isOdrefBased, double[] lastAccessNearFarCorner,
			double[] lastAccessFarCorner, double[] firstFarCorner, double[] firstAccessNearCorner, Satellite sat,
			long trakNumber, long orbitId) {
		/**
		 * Assigning
		 *
		 * parameters
		 *
		 */
		this.id = id;
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.isOdrefBased = isOdrefBased;
		this.corners[3] = lastAccessNearFarCorner;
		this.corners[2] = lastAccessFarCorner;
		this.corners[1] = firstFarCorner;
		this.corners[0] = firstAccessNearCorner;
		this.sat = sat;
		this.trackNumber = trakNumber;
		this.orbitId = orbitId;

	}// end method

	/**
	 * Condtructor Build DTO from a Strip
	 *
	 * @param strip
	 * @throws FeasibilityException
	 */
	public DTO(Strip strip) {
		this.strip = strip;
		this.startTime = strip.getStartTime();
		this.stopTime = strip.getStopTime();
		this.id = "0";
		this.sat = strip.getAccessList().get(0).getSatellite();

		/**
		 * Building DTO
		 *
		 * from strip
		 */
		buildDTO(strip);
	}// end method

	/**
	 * Check the strip duration and cuts it to maximal duration in case and delete
	 * the not available access
	 *
	 * @param s
	 */
	private void checkForMaxDuration(Strip s) {
		/**
		 * Check for duration
		 */

		this.startTime = s.getStillUsableAccessList().get(0).getAccessTime();
		this.stopTime = s.getStillUsableAccessList().get(s.getStillUsableAccessList().size() - 1).getAccessTime();

		/**
		 * Max duration
		 */
		double maxDuration = this.sat.getStripMapMaximalDuration();

		/**
		 *
		 */
		if ((this.stopTime - this.startTime) > maxDuration) {
			/**
			 * Strip overcame duration so stop time is reduced
			 */
			this.stopTime = this.startTime + maxDuration;
			double time;
			for (Access a : s.getStillUsableAccessList()) {
				time = a.getAccessTime();
				if ((time >= this.startTime) && (time <= this.stopTime)) {
					/**
					 * Only accesses not ovecaming duration
					 *
					 * are used
					 */
					this.dtoAccessList.add(a);
				}
			} // end for

			/**
			 * Stop time now shall be the access time of the last access
			 */
			this.stopTime = this.dtoAccessList.get(this.dtoAccessList.size() - 1).getAccessTime();
			// s.getStillUsableAccessList().clear();

		} // end if
		else {
			/**
			 * All the access can be used
			 */
			this.dtoAccessList.addAll(s.getStillUsableAccessList());
			// s.getStillUsableAccessList().clear();
		}

	}// end method

	/**
	 * Return the mean elevation of the scene.
	 *
	 * @return mean elevation of the scene
	 */
	public double getMeanElevation() {
		/**
		 * check if the mean elevation has already evakuated and if the accesslist is
		 * empty
		 */
		if (!this.hasDTOMeanElevationEvaluated && (this.dtoAccessList.size() > 0)) {
			for (Access a : this.dtoAccessList) {
				/**
				 * for each access
				 */
				this.meanElevation = this.meanElevation + a.getGridPoint().getLLH()[2];

			} // end for

			/**
			 * Evaluate average
			 */
			this.meanElevation = this.meanElevation / this.dtoAccessList.size();
			this.hasDTOMeanElevationEvaluated = true;
		}
		return this.meanElevation;
	}// end method

	/**
	 * Set the mean elevation (Used to force the mean elevation to a specified
	 * value)
	 *
	 * @param elevation
	 */
	public void setMeanElevation(double elevation) {
		// setting value
		this.meanElevation = elevation;
		// put evaluated flag to true
		this.hasDTOMeanElevationEvaluated = true;
	}// end method

	/**
	 * create a DTO
	 *
	 * @param s
	 * @return
	 * @throws FeasibilityException
	 */
	private void buildDTO(Strip s) {
		/**
		 * Check on strip
		 */
		if (s == null) {
			return;
		}

		/**
		 * Check for max duration
		 */
		checkForMaxDuration(s);

		/**
		 * Evaluate the min duration for DTO
		 */
		double minimalDuration = this.sat.getStripMapMinimalDuration();
		Access firstAccess = this.dtoAccessList.get(0);
		Access lastAccess = this.dtoAccessList.get(this.dtoAccessList.size() - 1);

		this.startPosition = firstAccess.getSatellitePos();
		this.endPosition = lastAccess.getSatellitePos();

		this.startVelocity = firstAccess.getSatelliteVel();
		this.endVelocity = lastAccess.getSatelliteVel();

		double duration = this.stopTime - this.startTime;

		this.orbitId = firstAccess.getOrbitId();

		if (duration < minimalDuration) {
			// logger.debug("DEGENERATO");

			/**
			 * If dureation less than minumim
			 *
			 * the DTO shall be expanded to
			 *
			 * the min by a factor : min - duration
			 */
			double residual = minimalDuration - duration;

			/**
			 * New stop time
			 */
			this.stopTime = this.stopTime + (residual / 2);

			/**
			 * New start time
			 */
			this.startTime = this.startTime - (residual / 2);

			// Satellite sat = firstAccess.getSatellite();

			int index = firstAccess.getStartingPointWindowIndex();

			// this.startPosition = sat.getPositionAt(this.startTime,index);
			// this.startVelocity = sat.getVelocityAt(this.startTime, index);

			// EpochBean startEpochBean = sat.getEpochAt(this.startTime, index);
			EpochBean startEpochBean;
			try {
				/**
				 * Retrieving satellite at new start
				 */
				startEpochBean = this.sat.getEpochAt(this.startTime);

			} catch (FeasibilityException e) {
				startEpochBean = this.sat.getEpochAt(this.startTime, index);
				this.tracer.warning(EventType.APPLICATION_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						e.getMessage());
			}

			this.startPosition = startEpochBean.getoXyz();
			this.startVelocity = startEpochBean.getoVxVyVz();

			this.orbitId = startEpochBean.getIdOrbit();

			index = lastAccess.getStartingPointWindowIndex();
			// this.endPosition = sat.getPositionAt(this.stopTime,index);
			// this.endVelocity = sat.getVelocityAt(this.stopTime, index);

			EpochBean stopEpochBean;
			try {
				/**
				 * Evaluating satellite
				 *
				 * info at new stop time
				 */
				stopEpochBean = this.sat.getEpochAt(this.stopTime);

			} catch (FeasibilityException e) {
				stopEpochBean = this.sat.getEpochAt(this.stopTime, index);
				this.tracer.warning(EventType.APPLICATION_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						e.getMessage());
			}

			this.endPosition = stopEpochBean.getoXyz();
			this.endVelocity = stopEpochBean.getoVxVyVz();

		} // End if

		/**
		 * Evaluating track number
		 */
		this.trackNumber = this.sat.getTrackNumber(this.orbitId);

		// double
		// [][]firstCorners=evaluateLLHcorners(firstAccess,this.startPosition,this.startVelocity);
		// double [][] secondCorners =
		// evaluateLLHcorners(lastAccess,this.endPosition,this.endVelocity);

		/**
		 * Evaluating corners
		 */
		double[][] firstCorners = evaluateLLHcorners(this.startPosition, this.startVelocity);
		double[][] secondCorners = evaluateLLHcorners(this.endPosition, this.endVelocity);

		this.isOdrefBased = s.isOdrefBased();
		this.corners[0] = firstCorners[0];
		this.corners[1] = firstCorners[1];
		this.corners[2] = secondCorners[1];
		this.corners[3] = secondCorners[0];

	}// end method

	/**
	 * return true if the DTO is under an undeferreble paw, so not usable More over
	 * it also check the flag for deferreble flag
	 *
	 * @return true if the DTO is under an undeferreble paw, so not usable
	 */
	public boolean checkForUndeferreblePaw() {
		boolean retval = false;

		PlatformActivityWindowBean currentPaw;
logger.debug("PAW_Management : check for mission : "+this.sat.getMissionName());
logger.debug("PAW_Management : check for sat : "+this.sat.getName());

		/**
		 * Satellite paw list
		 */
		ArrayList<PlatformActivityWindowBean> pawList = this.sat.getPawList();

		double pawStart;
		double pawStop;

		for (int i = 0; i < pawList.size(); i++) {

			/**
			 * For each paw in list
			 *
			 * check is the dto is inside paw
			 */
			currentPaw = pawList.get(i);
			logger.debug("PAW_Management : check paw : "+currentPaw);

			pawStart = currentPaw.getActivityStartTime();
			pawStop = currentPaw.getActivityStopTime();

			if ((this.stopTime < pawStart) || (this.startTime > pawStop)) {
				/**
				 * outside paw
				 */
				continue;
			} else {
				if (currentPaw.isDeferrableFlag()) {
					/**
					 * Inside deferreable paw
					 */
					this.paw = currentPaw;
					// return false;
				} else {
					retval = true;
					/**
					 * Must exit from the for
					 */
					break;
					// return true;
				}
			}

		} // end for
		return retval;
	}// end method

	/**
	 *
	 * @return in case of pass through it return true if the dto is inside a
	 *         SatellitePass
	 */
	public boolean checkForPassPlan() {
		boolean retval = false;

		/**
		 * Satllite pass list
		 */
		List<SatellitePassBean> satPassList = this.sat.getSatellitePassList();

		logger.debug("DTO START " + DateUtils.fromCSKDateToDateTime(this.startTime));
		logger.debug("DTO STOP  : " + DateUtils.fromCSKDateToDateTime(this.stopTime));

		logger.debug("DWL DURATION : " + this.getDWLDuration());

		/**
		 * end check is starTime + DWL Duration
		 */
		double endCheck = this.startTime + this.getDWLDuration();

		logger.debug("DTO END CHECK  : " + DateUtils.fromCSKDateToDateTime(endCheck));

		/*
		 * tracer. debug("==================Perfoming check for passrhrough on DTO " +
		 * this.id); tracer.debug("=================DTO Size " + this.dtoSize);
		 * tracer.debug("================DWL Duration" +
		 * DateUtils.fromCSKDurationToSeconds(this.getDWLDuration()));
		 */
		// check if DWL duration is
		// less than DTO duration
		if (endCheck < this.stopTime) {
			this.tracer.warning(EventType.APPLICATION_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"The evaluated DWL Duration ends before the stop of DTO acquisition. In passthrough check the stop time of DTO will be used");

			/**
			 * End check < stop time: this sould never happens
			 */
			endCheck = this.stopTime;
		}

		if (satPassList != null) {
			/**
			 * For each pass
			 */
			for (SatellitePassBean satPass : satPassList) {
				/**
				 * check that start + endcheck is inside pass visibility
				 */
				if ((this.startTime >= satPass.getVisibiliyStart()) && (endCheck <= satPass.getVisibilityStop())) {
					/**
					 * We have a correspondence we can return
					 */
					retval = true;
					break;
				}
			} // end for
		} // end if

		return retval;
	}// end checkForPassPlan

	/**
	 * Return near and far corner given the access and satellite position and
	 * velocity Use the value of the beam
	 *
	 * @param satPos
	 * @param satVel
	 * @return
	 */
	public double[][] evaluateLLHcorners(Vector3D satPos, Vector3D satVel)

	{
		/**
		 * Retrieving beam nadirs
		 */
		double nearOffNadirP = this.getBeam().getNearOffNadir();
		double farOffNadirP = this.getBeam().getFarOffNadir();

		// if(access.getLookSide()==FeasibilityConstants.RighLookSide)
		if (this.getLookSide() == FeasibilityConstants.LeftLookSide) {
			/**
			 * In case o left we must change the nadir sign
			 */
			nearOffNadirP = -nearOffNadirP;
			farOffNadirP = -farOffNadirP;
		}

		/**
		 * Evaluating corners
		 */
		return evaluateLLHcorners(satPos, satVel, nearOffNadirP, farOffNadirP);
	}// end method

	/**
	 * Return near and far corner given the access and satellite position and
	 * velocity and off nadir angle. Used directly in SPARCManager. The opening of
	 * beam is specified
	 *
	 * @param satPos
	 * @param satVel
	 * @param nearOffNadir
	 * @param farOffNadir
	 * @return near and far corner given the access and satellite position and
	 *         velocity and off nadir angle
	 */
	public double[][] evaluateLLHcorners(Vector3D satPos, Vector3D satVel, double nearOffNadirDeg,
			double farOffNadirDeg)

	{

		// logger.debug("Evaluating corners");

		/**
		 * Changinh the angle value to radians
		 */
		double nearOffNadirP = Math.toRadians(nearOffNadirDeg);
		double farOffNadirP = Math.toRadians(farOffNadirDeg);

		double[][] llhCorners = new double[2][3];

		// Vector3D satPos = access.getSatellitePos();
		// Vector3D satVel = access.getSatelliteVel();

		// satellite reference frame
		Vector3D[] e = ReferenceFrameUtils.getSatelliteReferenceFrame(satPos, satVel);

		// Rotation rotation = new
		// Rotation(e[0],nearOffNadir,RotationConvention.VECTOR_OPERATOR);
		Vector3D pointingVersor = e[1].scalarMultiply(Math.sin(nearOffNadirP))
				.add(e[2].scalarMultiply(Math.cos(nearOffNadirP)));

		double pointingModule = getPointingModule(satPos.toArray(), pointingVersor.toArray(), this.getMeanElevation());

		Vector3D ecefPoint = satPos.add(pointingVersor.scalarMultiply(pointingModule));

		// return llh in degree
		double[] llh = ReferenceFrameUtils.ecef2llh(ecefPoint.toArray(), true);
		llhCorners[0] = llh;
		// finding far corner

		// Rotation rotation = new
		// Rotation(e[0],farOffNadir,RotationConvention.VECTOR_OPERATOR);
		/**
		 * Perfoming operation on rotatotion
		 */
		pointingVersor = e[1].scalarMultiply(Math.sin(farOffNadirP)).add(e[2].scalarMultiply(Math.cos(farOffNadirP)));
		pointingModule = getPointingModule(satPos.toArray(), pointingVersor.toArray(), this.getMeanElevation());
		ecefPoint = satPos.add(pointingVersor.scalarMultiply(pointingModule));

		/**
		 * return llh in degree
		 */
		llh = ReferenceFrameUtils.ecef2llh(ecefPoint.toArray(), true);
		llhCorners[1] = llh;

		return llhCorners;
	}// end getLLHCorners

	/**
	 * return the module of the pointing vector
	 *
	 * @param ecefPos
	 * @param pointingVersor
	 * @param h              mean elevation
	 * @return the module of the pointing vector
	 */
	private double getPointingModule(double[] ecefPos, double[] pointingVersor, double h) {
		double retval = 0;
		/**
		 * Geode flatness
		 */
		double modifiedFlatness = 1 - ((ReferenceFrameUtils.wgs84_b + h) / (ReferenceFrameUtils.wgs84_a + h));
		double d = (1 - modifiedFlatness);

		// double d = (1-ReferenceFrameUtils.flatness);

		double a = (((pointingVersor[0] * pointingVersor[0]) + (pointingVersor[1] * pointingVersor[1])) * d * d)
				+ (pointingVersor[2] * pointingVersor[2]);
		double b = 2 * ((d * d * ((pointingVersor[0] * ecefPos[0]) + (pointingVersor[1] * ecefPos[1])))
				+ (pointingVersor[2] * ecefPos[2]));
		// double c = d*d*(ecefPos[0]*ecefPos[0]+ ecefPos[1]*ecefPos[1])+
		// ecefPos[2]*ecefPos[2]-ReferenceFrameUtils.wgs84_a2*d*d;

		double eqRadius = ReferenceFrameUtils.wgs84_a + h;
		double d2 = d * d;
		double eqRadius2 = eqRadius * eqRadius;
		double c = ((d * d * ((ecefPos[0] * ecefPos[0]) + (ecefPos[1] * ecefPos[1]))) + (ecefPos[2] * ecefPos[2]))
				- (eqRadius2 * d2);

		/**
		 *
		 * Finding root of second degree equation
		 *
		 */
		double discr = Math.sqrt((b * b) - (4 * a * c));
		double root1 = (-b + discr) / (2 * a);
		double root2 = (-b - discr) / (2 * a);

		/**
		 * we have use the littlest
		 */
		if (root1 < root2) {
			retval = root1;
		} else {
			retval = root2;
		}

		return retval;

	}// end method

	/**
	 *
	 * Get the target distance in km from the swath center. Present if in the
	 * request the <TargetCenteredpoint> is present
	 *
	 * @param llhTargetCenter in degree
	 * @return
	 */
	/*
	 * public double getTargetDistance(double [] llhTargetCenter) {
	 *
	 * double distance =0;
	 *
	 *
	 * double [] corner1Ecef = ReferenceFrameUtils.llh2ecef(this.corners[0], true);
	 * double [] corner2Ecef = ReferenceFrameUtils.llh2ecef(this.corners[1], true);
	 * double [] corner3Ecef = ReferenceFrameUtils.llh2ecef(this.corners[2], true);
	 * double [] corner4Ecef = ReferenceFrameUtils.llh2ecef(this.corners[3], true);
	 *
	 * double [] geometricCenterECEF = new double[3];
	 *
	 * geometricCenterECEF[0]=
	 * 0.25*(corner1Ecef[0]+corner2Ecef[0]+corner3Ecef[0]+corner4Ecef[0] );
	 * geometricCenterECEF[1]=
	 * 0.25*(corner1Ecef[1]+corner2Ecef[1]+corner3Ecef[1]+corner4Ecef[1] );
	 * geometricCenterECEF[2]=
	 * 0.25*(corner1Ecef[2]+corner2Ecef[2]+corner3Ecef[2]+corner4Ecef[2] );
	 *
	 *
	 *
	 * double [] llhCenter = ReferenceFrameUtils.ecef2llh(geometricCenterECEF,true);
	 *
	 * try { GeodeticCalculator calc = new GeodeticCalculator();
	 * calc.setStartingGeographicPoint(llhCenter[1], llhCenter[0]);
	 * calc.setDestinationGeographicPoint(llhTargetCenter[1],llhTargetCenter[0]) ;
	 * distance=calc.getOrthodromicDistance()/1000.0; } catch(Exception e) { //TODO
	 * loggare double [] targetCenterEcef =
	 * ReferenceFrameUtils.llh2ecef(llhTargetCenter, true); Vector3D geoCenter = new
	 * Vector3D(geometricCenterECEF); Vector3D targetCenter = new
	 * Vector3D(targetCenterEcef);
	 *
	 * distance = geoCenter.subtract(targetCenter).getNorm()/1000.0;
	 *
	 * }
	 *
	 *
	 * return distance; }
	 */

	/**
	 * Evaluating target distance
	 *
	 * @param llhTargetCenter
	 * @return target distance
	 */
	public double getTargetDistance(double[] llhTargetCenter) {

		double distance = 0;

		/**
		 * Ecef of corners point
		 */
		/*
		 * double [] corner1Ecef = ReferenceFrameUtils.llh2ecef(this.corners[0], true);
		 * double [] corner2Ecef = ReferenceFrameUtils.llh2ecef(this.corners[1], true);
		 * double [] corner3Ecef = ReferenceFrameUtils.llh2ecef(this.corners[2], true);
		 * double [] corner4Ecef = ReferenceFrameUtils.llh2ecef(this.corners[3], true);
		 */
		/**
		 * Evaluating middle point of start and stop line of the DTO
		 */
		/*
		 * double [] firstMiddlePoint =
		 * {FeasibilityConstants.half*(corner1Ecef[0]+corner2Ecef[0]),
		 * FeasibilityConstants.half*(corner1Ecef[1]+corner2Ecef[1]),
		 * FeasibilityConstants.half*(corner1Ecef[2]+corner2Ecef[2])}; double []
		 * secondMiddlePoint =
		 * {FeasibilityConstants.half*(corner3Ecef[0]+corner4Ecef[0]),
		 * FeasibilityConstants.half*(corner3Ecef[1]+corner4Ecef[1]),
		 * FeasibilityConstants.half*(corner3Ecef[2]+corner4Ecef[2])};
		 */
		/**
		 * Transforming middle point in LLH
		 */
		/*
		 * double [] firstMiddlePointLLH =
		 * ReferenceFrameUtils.ecef2llh(firstMiddlePoint, true); double []
		 * secondMiddlePointLLH = ReferenceFrameUtils.ecef2llh(secondMiddlePoint, true);
		 */

		/**
		 * Evaluating mean point at start and stop DTO
		 */

		double[] firstMiddlePointLLH = new double[2];
		double[] secondMiddlePointLLH = new double[2];

		firstMiddlePointLLH[0] = FeasibilityConstants.half * (this.corners[0][0] + this.corners[1][0]);
		firstMiddlePointLLH[1] = FeasibilityConstants.half * (this.corners[0][1] + this.corners[1][1]);

		secondMiddlePointLLH[0] = FeasibilityConstants.half * (this.corners[2][0] + this.corners[3][0]);
		secondMiddlePointLLH[1] = FeasibilityConstants.half * (this.corners[2][1] + this.corners[3][1]);

		// Applico formula ERONE per calcolare are triangolo formato tra i due
		// middle point ed il targetpoint

		/**
		 * We apply Herone formula to evaluate the triangle having target and the two
		 * middle point as vertexes The distance is the height respect the base formed
		 * by the two middle points
		 */

		GeodeticCalculator calc = new GeodeticCalculator();

		/**
		 * First side
		 */
		calc.setStartingGeographicPoint(firstMiddlePointLLH[1], firstMiddlePointLLH[0]);
		calc.setDestinationGeographicPoint(secondMiddlePointLLH[1], secondMiddlePointLLH[0]);
		double base = calc.getOrthodromicDistance();

		/**
		 * Second side
		 */
		calc.setDestinationGeographicPoint(llhTargetCenter[1], llhTargetCenter[0]);
		double firstMiddleToTargetDistance = calc.getOrthodromicDistance();

		/**
		 * Third side
		 */
		calc.setStartingGeographicPoint(secondMiddlePointLLH[1], secondMiddlePointLLH[0]);
		calc.setDestinationGeographicPoint(llhTargetCenter[1], llhTargetCenter[0]);
		double secondMiddleToTargetDistance = calc.getOrthodromicDistance();

		double halfPerimeter = FeasibilityConstants.half
				* (base + firstMiddleToTargetDistance + secondMiddleToTargetDistance);

		/**
		 * Area Herone
		 */
		double A2 = halfPerimeter * (halfPerimeter - base) * (halfPerimeter - firstMiddleToTargetDistance)
				* (halfPerimeter - secondMiddleToTargetDistance);

		if (A2 <= 0) {
			distance = 0;
		} else {
			/**
			 * Triangle Area formula
			 */
			double A = Math.sqrt(A2);
			distance = (2 * A) / base;
		}

		return distance / FeasibilityConstants.Kilo;
	}// getTargetDistance

	/**
	 *
	 * @return the near off nadir
	 */
	public double getNearOffnadir() {
		if ((this.nearOffNadir == 0) && (this.getBeam() != null)) {
			this.nearOffNadir = this.getBeam().getNearOffNadir();
		}

		/*
		 * if(this.getLookSide()==FeasibilityConstants.LeftLookSide) { nearOffNadir =
		 * -nearOffNadir;
		 *
		 * }
		 */

		return this.nearOffNadir;

	}// end method

	/**
	 *
	 * @return the far offnadir
	 */
	public double getFarOffNadir() {
		if ((this.farOffNadir == 0) && (this.getBeam() != null)) {
			this.farOffNadir = this.getBeam().getFarOffNadir();
		}

		/*
		 * if(this.getLookSide()==FeasibilityConstants.LeftLookSide) { farOffNadir =
		 * -farOffNadir;
		 *
		 * }
		 */

		return this.farOffNadir;
	}// end method

	/**
	 * Set the near offNadir
	 *
	 * @param nearOffDeg near off nadir in degree
	 */
	public void setNearOffNadir(double nearOffDeg) {
		this.nearOffNadir = nearOffDeg;
	}// end method

	/**
	 * set the far far off nadir
	 *
	 * @param farOffDeg in degree
	 */
	public void serFarOffNadir(double farOffDeg) {
		this.farOffNadir = farOffDeg;
	}// end method

	/**
	 * @return the id
	 */
	public String getId()

	{
		return this.id;
	}// end method

	/**
	 * @param id the id to set
	 */
	public void setId(String id)

	{
		this.id = id;
	}// end method

	/**
	 * @return the startTime
	 */
	public double getStartTime()

	{
		return this.startTime;
	}// end method

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(double startTime)

	{
		this.startTime = startTime;
	}// end method

	/**
	 * @return the stopTime
	 */
	public double getStopTime() {
		return this.stopTime;
	}// end method

	/**
	 * @param stopTime the stopTime to set
	 */
	public void setStopTime(double stopTime) {
		this.stopTime = stopTime;
	}// end method

	/**
	 * @return the isOdrefBased
	 */
	public boolean isOdrefBased() {
		return this.isOdrefBased;
	}// end method

	/**
	 * @param isOdrefBased the isOdrefBased to set
	 */
	public void setOdrefBased(boolean isOdrefBased) {
		this.isOdrefBased = isOdrefBased;
	}// end method

	/**
	 * @return the fourthCorner
	 */
	public double[] getFourtCorner() {
		return this.corners[3];
	}// end method

	/**
	 * @param fourthCorner the fourthCorner to set
	 */
	public void setFourthCorner(double[] fourthCorner) {
		this.corners[3] = fourthCorner;
	}// end method

	/**
	 * @return the thirdCorner
	 */
	public double[] getThirdCorner()

	{
		return this.corners[2];
	}// end method

	/**
	 * @param thirdCorner the thirdCorner to set
	 */
	public void setThirdCorner(double[] thirdCorner)

	{
		this.corners[2] = thirdCorner;
	}// end method

	/**
	 * @return the secondCorner
	 */
	public double[] getSecondCorner()

	{
		return this.corners[1];
	}// end method

	/**
	 * @param secondCorner the secondCorner to set
	 */
	public void setSecondCorner(double[] secondCorner)

	{
		this.corners[1] = secondCorner;
	}// end method

	/**
	 * @return the firstCorner
	 */
	public double[] getFirstCorner()

	{
		return this.corners[0];
	}// end method

	/**
	 * @param firstCorner the firstCorner to set
	 */
	public void setFirstCorner(double[] firstCorner)

	{
		this.corners[0] = firstCorner;
	}// end method

	/**
	 * return the satellite
	 *
	 * @return
	 */
	public String getSatName() {
		// return this.strip.getSatelliteId();
		String reval = this.satName;
		if ((this.dtoAccessList != null) && !this.dtoAccessList.isEmpty()) {
			/**
			 * The sat name as for first access
			 */
			this.satName = this.getDtoAccessList().get(0).getSatelliteId();
		}

		return this.satName;
	}// end method

	/**
	 * return the look side
	 *
	 * @return
	 */
	public int getLookSide() {
		if (this.lookSide < 0) {
			/**
			 * Look side could be only 1 or 2 so < 0 is a value not allowed so it maust be
			 * evaluated
			 */
			if (this.getDtoAccessList().size() > 0) {
				/**
				 * As for first access
				 */
				this.lookSide = this.getDtoAccessList().get(0).getLookSide();
			}

		}

		return this.lookSide;
	}// end method

	/**
	 * return the orbit direction
	 *
	 * @return orbit direction
	 */
	public int getOrditDirection() {
		// return this.strip.getAccessList().get(0).getOrbitDirection();
		/**
		 * Look side could be only 1 or 2 so < 0 is a value not allowed so it must be
		 * evaluated
		 */
		if (this.orbitDirection < 0) {
			/**
			 * As for first access
			 */
			this.orbitDirection = this.getDtoAccessList().get(0).getOrbitDirection();
		}
		return this.orbitDirection;
	}// end method

	/**
	 *
	 * @return the orbit number
	 *
	 */
	public long getOrbitId() {
		// Va rielaborato
		// return this.dtoAccessList.get(0).getOrbitId();
		return this.orbitId;

	}// end method

	/**
	 * Set the orbit id
	 *
	 * @param orbitId
	 */
	public void setOrbitId(long orbitId) {
		this.orbitId = orbitId;
	}// end method

	/**
	 *
	 * @return the track number
	 */
	public long getTrackNumber() {
		return this.trackNumber;
	}// end method

	/**
	 * Set the track number
	 *
	 * @param trackNumber
	 */
	public void setTrackNumber(long trackNumber) {
		this.trackNumber = trackNumber;
	}// end method

	/**
	 * return the mean value of beam angle
	 *
	 * @return
	 */
	public double getLookAngle() {
		BeamBean beam = this.getDtoAccessList().get(0).getBeam();
		/**
		 * Average betwean near and far
		 */
		double retval = FeasibilityConstants.half * (beam.getFarOffNadir() + beam.getNearOffNadir());
		return retval;
	}// end method

	/**
	 *
	 * @return ID of the beam
	 */
	public String getBeamId() {
		String returnedBeam = null;
		// return this.strip.getBeamId();
		if (this.dtoAccessList.size() > 0) {
			returnedBeam =  this.dtoAccessList.get(0).getBeamId();
		}
		return returnedBeam;
	}// end method

	/**
	 * return the beam
	 *
	 * @return
	 */
	public BeamBean getBeam() {
		BeamBean returnedBeam = null;

		if (this.dtoAccessList.size() > 0) {
			returnedBeam = this.dtoAccessList.get(0).getBeam();
		}
		return returnedBeam;
	}// end method

	/**
	 * return the entire strip associated to DTO
	 *
	 * @return
	 */
	public Strip getStrip() {
		return this.strip;
	}// end method

	/**
	 *
	 * @return Satellite position at start
	 */
	public Vector3D getSatPosAtStart() {
		// return this.strip.getAccessList().get(0).getSatellitePos();
		return this.startPosition;
	}// end method

	/**
	 * Set the position at start
	 *
	 * @param pos
	 */
	public void setSatPosAtStart(Vector3D pos) {
		this.startPosition = pos;
	}// end method

	/**
	 * Return the position of satelliate at the end
	 *
	 * @return Satellite position at emd of DTO
	 */
	public Vector3D getSatPosAtEnd() {
		// int endPosition= strip.getAccessList().size()-1;
		// return this.strip.getAccessList().get(endPosition).getSatellitePos();
		return this.endPosition;
	}// end method

	/**
	 * Set the position at end
	 *
	 * @param pos
	 */
	public void setSatPosAtEnd(Vector3D pos) {
		this.endPosition = pos;
	}// end method

	/**
	 *
	 * @return SAtellite velocity at start of DTO
	 */
	public Vector3D getSatVelAtStart() {
		return this.startVelocity;
	}// end method

	/**
	 * Set velocity at start
	 *
	 * @param vel
	 */
	public void setSatVelAtStart(Vector3D vel) {
		this.startVelocity = vel;
	}// end method

	/**
	 *
	 * @return SAtellite velocity at end of DTO
	 */
	public Vector3D getSatEndVelocity() {
		return this.endVelocity;
	}// end method

	/**
	 * Set velocity at end
	 *
	 * @param vel
	 */
	public void setSatVelAtSEnd(Vector3D vel) {
		this.endVelocity = vel;
	}// end method

	/**
	 * return the list of corner as posList string
	 *
	 * @return poslist as string
	 */
	public String getPosListString() {
		String retval = "";

		/**
		 * Poslist as string
		 */
		for (int i = 0; i < this.corners.length; i++) {
			retval = retval + this.corners[i][0] + " " + this.corners[i][1] + " ";
		}

		retval = retval + this.corners[0][0] + " " + this.corners[0][1];

		return retval;
	}// end getPosListStrin

	/**
	 * reurn the corners of the DTO
	 *
	 * @return the corners of the DTO
	 */
	public double[][] getCorners() {
		return this.corners;
	}// end method

	/**
	 * @param corners the corners to set
	 */
	public void setCorners(double[][] corners) {
		this.corners = corners;
	}// end method

	public void setStrip(Strip strip) {
		this.strip = strip;
	}// end method

	
	@Override
	public String toString() {

		String retString = "DTO [id=" + id + ", this.beamName  "+this.sarBeamName+", this.beam  "+this.getBeam()+",  startTime=" + DateUtils.fromCSKDateToDateTime(startTime) + ", stopTime="
				+ DateUtils.fromCSKDateToDateTime(stopTime)+",  startTime CSK =" + startTime + ", stopTime CSK="
						+ stopTime+" this.nearOffNadir"+this.nearOffNadir+", this.farOff +"+this.farOffNadir+", corners=" + Arrays.toString(corners) + "";
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 3; j++) {
				retString = retString + (corners[i][j]) + " - ";
			}
		}
		return retString;
	}

}// end class
