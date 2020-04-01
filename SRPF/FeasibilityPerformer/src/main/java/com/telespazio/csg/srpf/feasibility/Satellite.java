/**
*
* MODULE FILE NAME:	GridPoint.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			This Class modelize a Satellite form a Feasibility perspective
*
* PURPOSE:			Feasibility
*
* CREATION DATE:	17-11-2015
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
* --------------------------+------------+----------------+-------------------------------
* 11-05-2016 | Amedeo Bancone  |1.1|  in order to align to the new spotLight algo:
*  										deleted initConfig method and all
*  										deleted getMeanMotion method
*  										 deleted getorbitInclination
* --------------------------+------------+----------------+-------------------------------
* * --------------------------+------------+----------------+-------------------------------
* 11-07-2017 | Amedeo Bancone  |2.0|  added version of getPositionAt and getVelocityAt based only on access time. They used the getIndex helper function
*  									  modified for use generic as per DataManager modification
*  									  added SatellitePass management for passthrough
*  									  added the track offeset used to evalua the track number
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.feasibility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeForm;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.telespazio.csg.srpf.dataManager.bean.BeamBean;
import com.telespazio.csg.srpf.dataManager.bean.EpochBean;
import com.telespazio.csg.srpf.dataManager.bean.PlatformActivityWindowBean;
import com.telespazio.csg.srpf.dataManager.bean.SatelliteBean;
import com.telespazio.csg.srpf.dataManager.bean.SatellitePassBean;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.DateUtils;
import com.telespazio.csg.srpf.utils.PropertiesReader;
import com.telespazio.csg.srpf.utils.ReferenceFrameUtils;

/**
 *
 * Class that holds information related to a satellite
 *
 * @author Amedeo Bancone
 * @version 2.0
 *
 *
 */
public class Satellite implements Comparable<Satellite>

{

	// static final Logger logger =
	// LogManager.getLogger(Satellite.class.getName());
	/**
	 * logger
	 */
	static final Logger logger = LogManager.getLogger(Satellite.class.getName());

	/**
	 * Satellite bean as for DB
	 */
	private SatelliteBean satellite;

	/**
	 * Epochs list
	 */
	private ArrayList<EpochBean> epochs;
	/**
	 * Beams list
	 */
	private ArrayList<BeamBean> beams = null;

	/**
	 * Platform activity window list
	 */
	private ArrayList<PlatformActivityWindowBean> pawList = null;

	/**
	 * list of satellite pass
	 */
	private ArrayList<SatellitePassBean> satellitePassList = null;

	public Satellite() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 *
	 * min duration for a stripmap
	 */
	private double stripMapMinimalDuration;
	/**
	 * max duration for a strip
	 */
	private double stripMapMaximalDuration;
	/**
	 * time for restore a sensor
	 */
	private double sensorRestoreTime;

	/**
	 * spotLight durationStep
	 */
	private double spotLightTimeStep;

	/**
	 * half of the minimal duration for a spotLight
	 */
	private double spotLightHalfTimeStep;

	// orbit inclination
	// private double orbitInclination;

	// Mean Motion
	// private double meanMotion;

	/**
	 * List of access belonging the satellite
	 */
	private List<Access> accessList = new ArrayList<>();

	/**
	 * List of strip belonging the satellite
	 */
	private List<Strip> stripList = new ArrayList<>();

	/**
	 * Track Offeset
	 */
	private int trackOffSet = 0;

	/**
	 * In case of feasibility extension it holds the old dtos against which the new
	 * generates strips have to be checked in order to avoid conflicts
	 */
	private List<DTO> oldDTOList = new ArrayList<>();

	/**
	 * depending on the senso mode is used to evaluate if a satellite cab be used
	 * for acquisition afetr a prevois acuisition
	 */
	private double threshold;

	/**
	 * Number of milliseconds to be added to threshold
	 */
	private double extraTimeThreshold = 0;

	/**
	 * Coefficient for time guard
	 */
	private static double guardCoefficient = 0.75;

	/**
	 * Retrun the underlyuing bean
	 *
	 * @return sat bean
	 */
	public SatelliteBean getUnderlyingSatelliteBean() {
		return this.satellite;
	}// end method

	/***
	 * In case of feasibility extension it returns the old dtos against which the
	 * new generates strips have to be checked in order to avoid conflicts
	 *
	 * @return old dto list
	 */
	public List<DTO> getOldDTOList() {
		return this.oldDTOList;
	}// end method

	/**
	 * In case of feasibility extension it sets the old dtos against which the new
	 * generates strips have to be checked in order to avoid conflicts
	 *
	 * @param oldDTOList
	 */
	public void setOldDTOList(List<DTO> oldDTOList) {
		this.oldDTOList = oldDTOList;
	}// end method

	/**
	 *
	 * @return track offset
	 */
	public int getTrackOffSet() {
		return this.trackOffSet;
	}// end method

	/**
	 * Return the track number given an orbit id
	 *
	 * @param orbitId
	 * @return trackNumber
	 */
	public long getTrackNumber(long orbitId) {
		/**
		 * Evaluating track number
		 */
		long trackNumber = ((orbitId + this.trackOffSet) % FeasibilityConstants.TRACK_NUMBER_MODULE_DIVISOR) + 1;
		return trackNumber;
	}// end method

	/**
	 *
	 * @return the satellitePass list
	 */
	public ArrayList<SatellitePassBean> getSatellitePassList() {
		return this.satellitePassList;
	}// end method

	/**
	 * Set the satellite pass list
	 *
	 * @param satellitePassList
	 */
	public void setSatellitePassList(ArrayList<SatellitePassBean> satellitePassList) {
		this.satellitePassList = satellitePassList;
	}// end method

	/**
	 * Return Access list
	 *
	 * @return
	 */
	public List<Access> getAccessList() {
		return this.accessList;
	}// end method

	/**
	 * Set access list
	 *
	 * @param accessList
	 */
	public void setAccessList(List<Access> accessList) {
		this.accessList = accessList;
	}// end method

	/**
	 * Add access to the access list
	 *
	 * @param a
	 */
	public void addAccess(Access a) {
		// adding access
		a.setId(this.accessList.size() + 1);
		this.accessList.add(a);
	}// end method

	/**
	 * return the strip list
	 *
	 * @return
	 */
	public List<Strip> getStripList() {
		return this.stripList;
	}// end method

	/**
	 * set the strip list
	 *
	 * @param stripList
	 */
	public void setStripList(List<Strip> stripList) {
		this.stripList = stripList;
	}// end method

	/**
	 * Generate the list of strips belonging the satellite
	 */
	public void generateStrips() {
		/**
		 * Sort the access list on time base
		 */
		this.accessList.sort(new AccessComparatorByTime());
		/**
		 * dividing access on beam basis
		 */
		Map<String, List<Access>> beamAccessMap = divideAccessForSatBeam(this.accessList);

		/**
		 * Clearing the strip list
		 */
		this.stripList.clear();

		for (Map.Entry<String, List<Access>> entry1 : beamAccessMap.entrySet()) {
			/**
			 * for each beam
			 */
			List<Access> tempAccessList = entry1.getValue();
			while (!tempAccessList.isEmpty()) {
				/**
				 * building strip
				 */
				foundStrip(tempAccessList);
			} // end while
		} // end for

		/**
		 * In case of feasibility extension for stripmap we have to check that the
		 * strips doesn't conflict with older DTO so we have permorm a check aginst old
		 * DTO list. In case of feasibility or spotlight the old DTO list must be empty
		 */
		performCheckAginstOldDTOList();
	}// end method

	/**
	 * In case of feasibilkity extension for strimap modes, we have to elininate all
	 * the strips conflicting with the old DTO list. At the aim befor performing the
	 * strip generation we have to set the oldDTOList paramenter
	 */
	private void performCheckAginstOldDTOList() {
		/**
		 * list of strips to be deleted
		 */
		List<Strip> toBeDeletedStripList = new ArrayList<>();
		for (Strip s : this.stripList) {
			if (!isValidStrip(s)) {
				/**
				 * Check if strip must be deleted in case add list to be deleted
				 */
				toBeDeletedStripList.add(s);
			}
		} // end for

		for (Strip s : toBeDeletedStripList) {
			/**
			 * Removing strips
			 */
			this.stripList.remove(s);
		} // end for
	}// end method

	/**
	 * In case of feasibilkity extension for strimap modes, we have to elininate all
	 * the strips conflicting with the old DTO list. At the aim befor performing the
	 * strip generation we have to set the oldDTOList paramenter
	 *
	 * @param s strip
	 * @return treu if strip is valid
	 */
	private boolean isValidStrip(Strip s) {
		/**
		 * Valid start
		 */
		boolean isValid = true;
		/**
		 * Current dto info
		 */
		double dtoStart;
		double dtoStop;
		String dtoSatName;
		/**
		 * strip timing info
		 */
		double currentStripStartTime;
		double currentStripStopTime;

		/**
		 * for each dto
		 */
		for (DTO dto : this.oldDTOList) {
			/**
			 * Set dto info
			 */
			dtoStart = dto.getStartTime();
			dtoStop = dto.getStopTime();
			dtoSatName = dto.getSatName();
			currentStripStartTime = s.getStartTime();
			currentStripStopTime = s.getStopTime();

			/**
			 * if DTO and Strip belongs to the same Satellite
			 */
			if (dtoSatName.equals(this.getName())) {

				// if(((dtoStart - currentStripStopTime) >
				// this.getSensorRestoreTime()) ||
				// ((currentStripStartTime - dtoStop)
				// >this.getSensorRestoreTime() ) )
				// if(((dtoStart - currentStripStopTime) > this.getTreshold())
				// ||
				// ((currentStripStartTime - dtoStop) >this.getTreshold() ) )

				if (((dtoStart - currentStripStopTime) > FeasibilityConstants.ManouvreTolerance)
						|| ((currentStripStartTime - dtoStop) > FeasibilityConstants.ManouvreTolerance)) {
					/**
					 * No overlap we can check the next DTO
					 */
					continue;
				} else {
					/**
					 * overlap remove not usable list
					 */
					removeNotUsableAccess(dto, s);
					/**
					 * if no more accesses in strip return false
					 */
					if (s.getAccessList().size() == 0) {
						isValid = false;
						break;
					} // end if
				} // end else
			} // end if
		} // end for

		return isValid;
	}// end method

	/**
	 * Remove from the strip the access thatr conflicts with the DTO
	 *
	 * @param d dto
	 * @param s strip
	 */
	private void removeNotUsableAccess(DTO d, Strip s) {
		/**
		 * Valid Avvacce
		 */
		ArrayList<Access> newAccessList = new ArrayList<>();
		double upperLimit = d.getStopTime() + this.getTreshold();
		double currentTime;
		/**
		 * To be performed only if they are on the same side otherwhiswe the strip is
		 * not useable
		 */
		if (d.getLookSide() == s.getAccessList().get(0).getLookSide()) {
			for (Access a : s.getStillUsableAccessList()) {
				currentTime = a.getAccessTime();

				if (currentTime <= upperLimit) {
					/**
					 * Access not usable
					 */
					continue;
				} else {
					/**
					 * Access usable adding to new list
					 */
					newAccessList.add(a);
				}
			} // end for
		} // end if

		/**
		 * Setting strip access list to new list
		 */
		s.setAccessList(newAccessList);
	}// end method

	/**
	 * Divide the accesses for sat beam
	 *
	 * @param satelliteAccessMap
	 * @return map beams against access list
	 */
	private Map<String, List<Access>> divideAccessForSatBeam(List<Access> list) {
		/**
		 * return map
		 */
		Map<String, List<Access>> satelliteBeamAccessMap = new TreeMap<>();
		List<Access> currentList;
		/**
		 * current map key It's the beam's name
		 */
		String key;
		for (Access a : list) {
			/**
			 * Setting the key
			 */
			key = a.getBeamId();
			/**
			 * return the associated list null if none
			 */
			currentList = satelliteBeamAccessMap.get(key);
			if (currentList == null) {
				/**
				 * we have create a list put the list in the map add the access
				 */
				currentList = new ArrayList<>();
				satelliteBeamAccessMap.put(key, currentList);
				currentList.add(a);
			} // end if
			else {
				/**
				 * list already exists add access
				 */
				currentList.add(a);
			} // end else

		} // end for

		return satelliteBeamAccessMap;
	}// end divideAccessForSatBeam

	/**
	 * Found a single strip
	 *
	 * @param list
	 */
	private void foundStrip(List<Access> list) {
		/**
		 * Strip start and stop time
		 */
		double startTime;
		double stopTime;
		/**
		 * first access
		 */
		Access a = list.get(0);

		/**
		 * setting start and stop of strip to first access
		 */
		startTime = a.getAccessTime();
		stopTime = a.getAccessTime();
		/**
		 * strip acccess lisy
		 */
		List<Access> stripAccessList = new ArrayList<>();
		stripAccessList.add(a);

		int size = list.size();
		int i = 0;
		Access currentAccess;
		Access lastAccess = a;

		int lookSide = a.getLookSide();

		for (i = 1; i < size; i++) {
			/**
			 * for each access in access list
			 */
			currentAccess = list.get(i);

			/**
			 * check if the same orbit
			 */
			if (FeasibilityConstants.StripTimeOffest > (currentAccess.getAccessTime() - lastAccess.getAccessTime())) {
				if (currentAccess.getLookSide() == lookSide) {
					/**
					 * Check if the same look side then add to strip list set stoptime strip to
					 * current accest time
					 */
					stripAccessList.add(currentAccess);
					stopTime = currentAccess.getAccessTime();

					lastAccess = currentAccess;
				}

			} else {
				/**
				 * New orbit found end of strip must return
				 */
				break;
			}

		} // end for

		// list.removeAll(stripAccessList);

		for (Access ac : stripAccessList) {
			/**
			 * removing from access list the access added to the current stip
			 */
			list.remove(ac);
		} // end for

		if (startTime <= stopTime) {
			/**
			 * create strip object add to strip list
			 */
			Strip s = new Strip(this.stripList.size(), stripAccessList);
			this.stripList.add(s);
		} // end if

	}// end foundStrip

	/**
	 * @return the allowedLookside
	 */
	public int getAllowedLookside() {
		return this.satellite.getIdAllowedLookSide();
	}// end method

	/**
	 * Constructor Build a satellite from its bean and configuration
	 *
	 * @param satellite
	 */
	public Satellite(SatelliteBean satellite) {
		/**
		 * setting bean
		 */
		this.satellite = satellite;

		/**
		 * Reading property
		 */
		String value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.EXTRA_THRESHOLD_GUARD_CONF_KEY);
		if (value != null) {
			try {

				long iValue = Long.parseLong(value);
				this.extraTimeThreshold = DateUtils.millisecondsToJulian(iValue);
			} catch (Exception e) {
				/**
				 * misconfigurd property using default
				 */
				// logger.warn("Unable to found " +
				// FeasibilityConstants.MAX_AREA_OF_INTEREST_CONF_KEY + " in
				// conffiguration");
				logger.error(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
						"wrng format for  " + FeasibilityConstants.EXTRA_THRESHOLD_GUARD_CONF_KEY
								+ " in configuration");

			}

		} else {
			/**
			 * not configurd property using default
			 */
			// logger.warn("Unable to found " +
			// FeasibilityConstants.MAX_AREA_OF_INTEREST_CONF_KEY + " in
			// conffiguration");
			logger.error(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR,
					"Unable to found " + FeasibilityConstants.EXTRA_THRESHOLD_GUARD_CONF_KEY + " in configuration");

		}

		/**
		 * the following are default value to be used in case the parameters are missed
		 * anyway they are modified in setBeams with the valued retireved for the sensor
		 * mode in the DB
		 */
		this.stripMapMinimalDuration = FeasibilityConstants.CSKSTRIPMAPMinimalDTODuration; // OK
		this.stripMapMaximalDuration = FeasibilityConstants.CKSTRIPMAPMaximalDuration; // ok
		this.sensorRestoreTime = FeasibilityConstants.CSKSTRIPMapRestTime;// ok
		this.spotLightTimeStep = FeasibilityConstants.CSKSpotlightTimeStep;// ok
		this.spotLightHalfTimeStep = FeasibilityConstants.CSKSpotlightHalfTimeStep;// ok

		this.threshold = this.sensorRestoreTime + (0.5 * this.stripMapMinimalDuration) + this.extraTimeThreshold;

		this.trackOffSet = satellite.getTrackOffset();
		// setting the track number fro configuration
		// TODO consider to put in DB the track number

		/*
		 * String trackNumberOffsetPropKey=satellite.getSatelliteName()+
		 * FeasibilityConstants.TRACK_NUMBER_OFFSET_POSTFIX_CONF_KEY; String value
		 * =PropertiesReader.getInstance().getProperty(trackNumberOffsetPropKey) ;
		 * if(value!=null) { try { this.trackOffSet=Integer.parseInt(value); } catch
		 * (Exception e) { this.trackOffSet=0;
		 * this.tracer.critical(EventType.SOFTWARE_EVENT,
		 * ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Malformed  " +
		 * trackNumberOffsetPropKey + " in configuration"); }
		 *
		 * } else { this.tracer.critical(EventType.SOFTWARE_EVENT,
		 * ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " +
		 * trackNumberOffsetPropKey + " in configuration");
		 *
		 * }
		 */
	}// end method

	/**
	 * @return the spotLightTimeStep
	 */
	public double getSpotLightTimeStep() {
		return this.spotLightTimeStep;
	}// end method

	/**
	 * @return the spotLightHalfTimeStep
	 */
	public double getSpotLightHalfTimeStep() {
		return this.spotLightHalfTimeStep;
	}// end method

	public void setSpotLightHalfTimeStep(double spotLightHalfTimeStep) {
		this.spotLightHalfTimeStep = spotLightHalfTimeStep;
	}

	/**
	 * return the minimal duration of a DTO stripmap
	 *
	 * @return min duration
	 */
	public double getStripMapMinimalDuration() {
		return this.stripMapMinimalDuration;
	}// end method

	/**
	 * return the Maximal duration of a DTO stripmap
	 *
	 * @return max duration
	 */
	public double getStripMapMaximalDuration() {
		return this.stripMapMaximalDuration;
	}// end method

	/**
	 * @return the sensorRestoreTime
	 */
	public double getSensorRestoreTime() {
		return this.sensorRestoreTime;
	}// end method

	//////
	/**
	 * return the id of satellite as for db
	 *
	 * @return sat id
	 */
	public int getSatID() {
		return this.satellite.getIdSatellite();
	}// end method

	/**
	 *
	 * @return satellite name
	 */
	public String getName() {
		return this.satellite.getSatelliteName();
	}// end method

	/**
	 * return the list of beam
	 *
	 * @return list of beams
	 */
	public ArrayList<BeamBean> getBeams() {
		return this.beams;
	}// end method

	/**
	 *
	 * @return list of epochs
	 */
	public List<EpochBean> getEpochs() {
		return this.epochs;
	}// end method

	/**
	 *
	 * @param epochs
	 */
	public void setEpochs(ArrayList<EpochBean> epochs)

	{
		this.epochs = epochs;
	}// end method

	/**
	 * Check fir allowed look side
	 *
	 * @param lookSide
	 * @return treu if allowed side
	 */
	public boolean checkLookSide(int lookSide) {
		/**
		 * retval
		 */
		boolean isGood = false;

		int allowedSide = this.satellite.getIdAllowedLookSide();

		/**
		 * true of allowedSide = both or lookside = allowedLookside
		 */
		if ((allowedSide == FeasibilityConstants.BothLookSide) || (lookSide == allowedSide)) {
			isGood = true;
		}
		return isGood;
	}// en method

	/**
	 * This function set the list of beams used in the feasibility for the
	 * satellite.
	 *
	 * @param beams
	 */
	public void setBeams(ArrayList<BeamBean> beams) {
		this.beams = beams;

		BeamBean beam;

		if (beams.size() != 0) {
			beam = this.beams.get(0);
			int minDuration = beam.getDtoMinDuration();
			int maxDuration = beam.getDtoMaxDuration();
			int maxResTime = beam.getResTime();

			/**
			 * Setting parameters using value retrieved by DB
			 */
			logger.debug("MODIFICA 14.11 SIZE LIST BEAM :  "+this.beams.size());
			if (this.beams.get(0).getSensorModeName().equalsIgnoreCase("SPOTLIGHT-2A")) {
				minDuration = computeMinOrMaxDuration(this.beams, true);
				maxDuration = computeMinOrMaxDuration(this.beams, false);
				maxResTime = computeMaxResTime(this.beams);
			}

			this.stripMapMinimalDuration = DateUtils.millisecondsToJulian(minDuration); // OK
			this.stripMapMaximalDuration = DateUtils.millisecondsToJulian(maxDuration); // ok
			this.spotLightTimeStep = DateUtils.millisecondsToJulian(minDuration);// ok
			this.spotLightHalfTimeStep = FeasibilityConstants.half * this.spotLightTimeStep;// ok
			this.sensorRestoreTime = DateUtils.millisecondsToJulian(maxResTime);
			this.threshold = this.sensorRestoreTime + (0.5 * this.stripMapMinimalDuration) + this.extraTimeThreshold;
//
			logger.info("DTO MAX DURATION: " + maxDuration);
			logger.info("DTO MIN DURATION: " + minDuration);
			logger.info("DTO REST TIME : " +maxResTime);
			logger.info("spotLightHalfTimeStep  : " +DateUtils.fromCSKDurationToMilliSeconds(spotLightHalfTimeStep));

		}

	}// en method

	private int computeMaxResTime(ArrayList<BeamBean> beams) {
		int resTime = 0;
		if (beams.size() > 0) {
			resTime = beams.get(0).getResTime();

			for (int i = 1; i < beams.size(); i++) {
				if (beams.get(i).getResTime() > resTime) {
					resTime = beams.get(i).getResTime();
				}
			}
		}
		return resTime;
	}

	private int computeMinOrMaxDuration(ArrayList<BeamBean> beams, boolean min) {
		int duration = 0;
		if (beams.size() > 0) {
			if (min) {
				duration = beams.get(0).getDtoMinDuration();
			} else {
				duration = beams.get(0).getDtoMaxDuration();
			}
			for (int i = 1; i < beams.size(); i++) {
				if (min) {
					if (beams.get(i).getDtoMinDuration() < duration) {
						duration = beams.get(i).getDtoMinDuration();
					}
				} else {
					if (beams.get(i).getDtoMaxDuration() > duration) {
						duration = beams.get(i).getDtoMaxDuration();
					}
				}
			}
		}
		return duration;
	}

	/**
	 * return the list of paw
	 *
	 * @return PAW list
	 */
	public ArrayList<PlatformActivityWindowBean> getPawList() {
		return this.pawList;
	}// en method

	/**
	 * Set the list of paw
	 *
	 * @param pawList
	 */
	public void setPawList(ArrayList<PlatformActivityWindowBean> pawList) {
		this.pawList = pawList;
	}// en method

	/**
	 * Return an interpolated value of the satellite position in the time window
	 * starting from the index startingPointWindowIndex
	 *
	 * @param accessTime
	 * @param startingPointWindowIndex
	 * @return position in ecef
	 */
	public Vector3D getPositionAt(double accessTime, int startingPointWindowIndex) {
		/**
		 * Retval
		 */
		Vector3D retval = null;

		/**
		 * sample to build lagrange polynomial
		 */
		EpochBean s0 = this.getEpochs().get(startingPointWindowIndex);
		EpochBean s1 = this.getEpochs().get(startingPointWindowIndex + 1);
		EpochBean s2 = this.getEpochs().get(startingPointWindowIndex + 2);
		EpochBean s3 = this.getEpochs().get(startingPointWindowIndex + 3);

		/**
		 * building vecor for polinomial
		 */
		double[] epochTimes = { s0.getEpoch(), s1.getEpoch(), s2.getEpoch(), s3.getEpoch() };
		double[] Xs = { s0.getX(), s1.getX(), s2.getX(), s3.getX() };
		double[] Ys = { s0.getY(), s1.getY(), s2.getY(), s3.getY() };
		double[] Zs = { s0.getZ(), s1.getZ(), s2.getZ(), s3.getZ() };
		/**
		 * evaluating position component
		 */
		double X = new PolynomialFunctionLagrangeForm(epochTimes, Xs).value(accessTime);
		double Y = new PolynomialFunctionLagrangeForm(epochTimes, Ys).value(accessTime);
		double Z = new PolynomialFunctionLagrangeForm(epochTimes, Zs).value(accessTime);

		/**
		 * building position vector
		 */
		retval = new Vector3D(X, Y, Z);

		return retval;
	}// en method

	/**
	 * Return an interpolated value of the satellite position in the time window
	 * starting from the index startingPointWindowIndex
	 *
	 * @param accessTime
	 * @param startingPointWindowIndex
	 * @return velocity in m/s
	 */
	public Vector3D getVelocityAt(double accessTime, int startingPointWindowIndex) {
		/**
		 * Retval
		 */
		Vector3D retval = null;

		/**
		 * sample to build lagrange polynomial
		 */
		EpochBean s0 = this.getEpochs().get(startingPointWindowIndex);
		EpochBean s1 = this.getEpochs().get(startingPointWindowIndex + 1);
		EpochBean s2 = this.getEpochs().get(startingPointWindowIndex + 2);
		EpochBean s3 = this.getEpochs().get(startingPointWindowIndex + 3);

		/**
		 * building vecor for polinomial
		 */
		double[] epochTimes = { s0.getEpoch(), s1.getEpoch(), s2.getEpoch(), s3.getEpoch() };
		double[] VXs = { s0.getVx(), s1.getVx(), s2.getVx(), s3.getVx() };
		double[] VYs = { s0.getVy(), s1.getVy(), s2.getVy(), s3.getVy() };
		double[] VZs = { s0.getVz(), s1.getVz(), s2.getVz(), s3.getVz() };

		/**
		 * evaluating velocity component
		 */

		double Vx = new PolynomialFunctionLagrangeForm(epochTimes, VXs).value(accessTime);
		double Vy = new PolynomialFunctionLagrangeForm(epochTimes, VYs).value(accessTime);
		double Vz = new PolynomialFunctionLagrangeForm(epochTimes, VZs).value(accessTime);

		/**
		 * building velocity vector
		 */
		retval = new Vector3D(Vx, Vy, Vz);

		return retval;
	}// en method

	/**
	 * Return the vector velocity in ECEF. In case of the accessTime doesn't fall
	 * between a sliding window of four epoch an FeasibilityException exception is
	 * thrown
	 *
	 * @param accessTime
	 * @return The vector velocity
	 * @throws FeasibilityException
	 */
	public Vector3D getVelocityAt(double accessTime) throws FeasibilityException

	{
		Vector3D retval = null;
		/**
		 * searching for index
		 */
		int startingPointWindowIndex = getIndex(accessTime);
		/**
		 * evaluating velocity
		 */
		retval = getVelocityAt(accessTime, startingPointWindowIndex);

		return retval;
	}// en method

	/**
	 * Return the vector velocity in ECEF. In case of the accessTime doesn't fall
	 * between a sliding window of four epoch an FeasibilityException exception is
	 * thrown
	 *
	 * @param accessTime
	 * @return The vector velocity
	 * @throws FeasibilityException
	 */
	public Vector3D getPositionAt(double accessTime) throws FeasibilityException {
		Vector3D retval = null;
		/**
		 * searching for index
		 */
		int startingPointWindowIndex = getIndex(accessTime);

		/**
		 * evaluating position
		 */
		retval = getPositionAt(accessTime, startingPointWindowIndex);

		return retval;
	}// en method

	/**
	 * Return the epoch bean In case of the accessTime doesn't fall between a
	 * sliding window of four epoch an FeasibilityException exception is thrown
	 *
	 * @param accessTime
	 * @return the EpochBean
	 * @throws FeasibilityException
	 */
	public EpochBean getEpochAt(double accessTime) throws FeasibilityException {

		// EpochBean epochBean = new EpochBean();
		/**
		 * searching for index
		 */
		int startingPointWindowIndex = getIndex(accessTime);

		/*
		 * EpochBean s0 = (EpochBean) this.getEpochs().get(startingPointWindowIndex);
		 * EpochBean s1 = (EpochBean) this.getEpochs().get(startingPointWindowIndex+1);
		 * EpochBean s2 = (EpochBean) this.getEpochs().get(startingPointWindowIndex+2);
		 * EpochBean s3 = (EpochBean) this.getEpochs().get(startingPointWindowIndex+3);
		 *
		 * double [] epochTimes =
		 * {s0.getEpoch(),s1.getEpoch(),s2.getEpoch(),s3.getEpoch()};
		 *
		 * //Evaluating velocity double [] VXs =
		 * {s0.getVx(),s1.getVx(),s2.getVx(),s3.getVx()}; double [] VYs =
		 * {s0.getVy(),s1.getVy(),s2.getVy(),s3.getVy()}; double [] VZs =
		 * {s0.getVz(),s1.getVz(),s2.getVz(),s3.getVz()}; double Vx = new
		 * PolynomialFunctionLagrangeForm(epochTimes,VXs).value(accessTime); double Vy =
		 * new PolynomialFunctionLagrangeForm(epochTimes,VYs).value(accessTime); double
		 * Vz = new PolynomialFunctionLagrangeForm(epochTimes,VZs).value(accessTime);
		 *
		 * //evaluating postion double [] Xs =
		 * {s0.getX(),s1.getX(),s2.getX(),s3.getX()}; double [] Ys =
		 * {s0.getY(),s1.getY(),s2.getY(),s3.getY()}; double [] Zs =
		 * {s0.getZ(),s1.getZ(),s2.getZ(),s3.getZ()}; double X = new
		 * PolynomialFunctionLagrangeForm(epochTimes,Xs).value(accessTime); double Y =
		 * new PolynomialFunctionLagrangeForm(epochTimes,Ys).value(accessTime); double Z
		 * = new PolynomialFunctionLagrangeForm(epochTimes,Zs).value(accessTime);
		 *
		 *
		 * epochBean.setoVxVyVz(new Vector3D(Vx,Vy,Vz)); epochBean.setoXyz(new
		 * Vector3D(X,Y,Z));
		 */
		return getEpochAt(accessTime, startingPointWindowIndex);
	}// en method

	/**
	 * Return the epoch bean In case of the accessTime doesn't fall between a
	 * sliding window of four epoch an FeasibilityException exception is thrown
	 *
	 * @param accessTime
	 * @param startingIndex
	 * @return the EpochBean
	 * @throws FeasibilityException
	 */
	public EpochBean getEpochAt(double accessTime, int startingIndex) {
		/**
		 * Epoch to be returned
		 */
		EpochBean epochBean = new EpochBean();

		int startingPointWindowIndex = startingIndex;

//		logger.debug("returned startingIndex : "+startingIndex);
		/**
		 * sample to build lagrange polynomial
		 */
		EpochBean s0 = this.getEpochs().get(startingPointWindowIndex);
		EpochBean s1 = this.getEpochs().get(startingPointWindowIndex + 1);
		EpochBean s2 = this.getEpochs().get(startingPointWindowIndex + 2);
		EpochBean s3 = this.getEpochs().get(startingPointWindowIndex + 3);

//		logger.debug("returned s0 : "+DateUtils.fromCSKDateToDateTime(s0.getEpoch()));
//		logger.debug("returned s1 : "+DateUtils.fromCSKDateToDateTime(s1.getEpoch()));
//		logger.debug("returned s2 : "+DateUtils.fromCSKDateToDateTime(s2.getEpoch()));
//		logger.debug("returned s3 : "+DateUtils.fromCSKDateToDateTime(s3.getEpoch()));

		/**
		 * build time vector
		 */
		double[] epochTimes = { s0.getEpoch(), s1.getEpoch(), s2.getEpoch(), s3.getEpoch() };

		/**
		 * Evaluating velocity
		 */
		double[] VXs = { s0.getVx(), s1.getVx(), s2.getVx(), s3.getVx() };
		double[] VYs = { s0.getVy(), s1.getVy(), s2.getVy(), s3.getVy() };
		double[] VZs = { s0.getVz(), s1.getVz(), s2.getVz(), s3.getVz() };
		double Vx = new PolynomialFunctionLagrangeForm(epochTimes, VXs).value(accessTime);
		double Vy = new PolynomialFunctionLagrangeForm(epochTimes, VYs).value(accessTime);
		double Vz = new PolynomialFunctionLagrangeForm(epochTimes, VZs).value(accessTime);

		/**
		 * evaluating postion
		 */
		double[] Xs = { s0.getX(), s1.getX(), s2.getX(), s3.getX() };
		double[] Ys = { s0.getY(), s1.getY(), s2.getY(), s3.getY() };
		double[] Zs = { s0.getZ(), s1.getZ(), s2.getZ(), s3.getZ() };
		double X = new PolynomialFunctionLagrangeForm(epochTimes, Xs).value(accessTime);
		double Y = new PolynomialFunctionLagrangeForm(epochTimes, Ys).value(accessTime);
		double Z = new PolynomialFunctionLagrangeForm(epochTimes, Zs).value(accessTime);

		/**
		 * Setting velocity position and data type
		 */
		epochBean.setoVxVyVz(new Vector3D(Vx, Vy, Vz));
		epochBean.setoXyz(new Vector3D(X, Y, Z));
		epochBean.setDataType(s1.getDataType());

		/*
		 * Vector3D[] EUnitVector = ReferenceFrameUtils
		 * .getSatelliteReferenceFrame(epochBean.getoXyz(), epochBean.getoVxVyVz());
		 *
		 * epochBean.setoE1xE1yE1z(EUnitVector[0]);
		 * epochBean.setoE2xE2yE2z(EUnitVector[1]);
		 * epochBean.setoE3xE3yE3z(EUnitVector[2]);
		 */
		/**
		 * Setting orbit id
		 */
		if ((s1.getDataType() == FeasibilityConstants.OdrefType) || (s1.getIdOrbit() == s2.getIdOrbit())) {
			/**
			 * Case odref
			 */
			epochBean.setIdOrbit(s1.getIdOrbit());
		} else {
			/**
			 * Case not odref
			 */
			double[] llh = ReferenceFrameUtils.ecef2llh(epochBean.getoXyz(), true);
			if (llh[0] >= 0) {
				/**
				 * Above equator
				 */
				epochBean.setIdOrbit(s1.getIdOrbit());
			} else {
				/**
				 * below equator
				 */
				epochBean.setIdOrbit(s2.getIdOrbit());
			}

		}
		// //System.out.println("====================Evaluated BEan");
		return epochBean;
	}// end method

	/**
	 * Return the starting index of the four sample epochs window where the access
	 * time fall In case of the accessTime doesn't fall between a sliding window of
	 * four epoch an FeasibilityException exception is thrown
	 *
	 * @param accessTime
	 * @return
	 * @throws FeasibilityException
	 */
	private int getIndex(double accessTime) throws FeasibilityException {
		
		int posThatMustBeReturned = 0;

		//sort epochs by time in ascending orger
		
		//get the last epoch
		
		//if the accessTime > last epoch
		//set the access time as last epoch time - 4
		
		try {

			int startingPointWindowIndex = 0;
			double currentAccessTime = 0;
			EpochBean currentEpoch;
//			logger.debug("MODIFICA this.epochs.size : " + this.epochs.size());
//			logger.debug("MODIFICA FIRST EPOCH: " + this.epochs.get(0).toString());
//			logger.debug("MODIFICA LAST EPOCH: " + this.epochs.get(this.epochs.size()-1).toString());

//			logger.debug("VERSUS accessTime : " + DateUtils.fromCSKDateToDateTime(accessTime));

			ArrayList<Integer> epochThatMustBeRemoved = new ArrayList<Integer>();
			for (startingPointWindowIndex = 0; startingPointWindowIndex < this.epochs
					.size(); startingPointWindowIndex++) {
				/**
				 * we iterate over epochs
				 *
				 */
				currentEpoch = this.epochs.get(startingPointWindowIndex);
				currentAccessTime = currentEpoch.getEpoch();
				if (currentAccessTime > accessTime) {
//					if(epochThatMustBeRemoved.size()==0)
//					{
//						logger.debug("PRIMO SCHIANTO IN POS  : " + startingPointWindowIndex);
//
//					}
					/**
					 * if epoch has atime access > of time we have exit
					 */
					epochThatMustBeRemoved.add(startingPointWindowIndex);

					// MODIFICA RIMOSSO BREAK
					// break;
				}
			}

			for (int i = 0; i < epochThatMustBeRemoved.size(); i++) {
				if (i == 0) {
					posThatMustBeReturned = epochThatMustBeRemoved.get(i);
//					logger.debug("RESTITUITO VALORE posThatMustBeReturned"+posThatMustBeReturned);
				}
				// int posToRem = epochThatMustBeRemoved.get(i);
				// this.epochs.remove(posToRem);
			}
//			logger.debug("MODIFICA this.epochs.size : " + this.epochs.size());

			/**
			 * window must start the sample before
			 */
			// MODIFICA COMMENTATO DECREMENTO
			// startingPointWindowIndex--;

			startingPointWindowIndex = this.epochs.size() - 1;
			posThatMustBeReturned--;
//			logger.debug("startingPointWindowIndex : " + startingPointWindowIndex);

			if ((startingPointWindowIndex < 0) || (this.epochs.size() -posThatMustBeReturned + 1 < FeasibilityConstants.NumberOfGuardSample)) {
				/**
				 * just throw because of outside valid sample
				 */
				logger.error("There are less than " + FeasibilityConstants.NumberOfGuardSample + " epochs.");

				throw new FeasibilityException(
						"There are less than " + FeasibilityConstants.NumberOfGuardSample + " epochs.");
			}

			/*
			 * if ((startingPointWindowIndex < 0) || (startingPointWindowIndex >
			 * (this.epochs.size() - FeasibilityConstants.NumberOfGuardSample))) { //just
			 * throw because of outside valid sample
			 * 
			 * throw new
			 * FeasibilityException("The time is outside of the available orbit samples"); }
			 */

		} catch (Exception e) {
			DateUtils.getLogInfo(e, logger);
		}
//		logger.debug("returned posThatMustBeReturned : " +posThatMustBeReturned);

		return posThatMustBeReturned;
	}// end method

	/**
	 * Check if the access is usable aigainst paw. It return false (not usable) if
	 * the access fall inside a paw not deferrable and if the access is not
	 * ebvaluated with ODREF. If the paw is defferable the access can be used with
	 * warning, whereas if the aceess is evaulated with ODREF because of is
	 * repeteable the DTO shall be evaluated in order to be exansed. Other control
	 * are needeed in this case directly on DTO.
	 *
	 * @param access
	 * @return true if acces is usable
	 */
	public boolean checkForPaw(Access access) {
		/**
		 * retval
		 */
		boolean retval = true;
		EpochBean accessEpoch = this.getEpochs().get(access.getStartingPointWindowIndex());
		//
		// int dataType = accessEpoch.getDataType();

		/**
		 * Access time
		 */
		double accessTime = accessEpoch.getEpoch();

		PlatformActivityWindowBean paw;
		/**
		 * guard time
		 */
		double guardTime = guardCoefficient * this.getStripMapMinimalDuration();

		if (this.pawList != null) {
			/**
			 * searching in paw
			 */
			for (int i = 0; i < this.pawList.size(); i++) {
				paw = this.pawList.get(i);
				if (paw.isDeferrableFlag()) {
					/**
					 * If the paw is deferrable the access can be used, with alert. More control are
					 * needed on DTO
					 */
					continue;
				} else {
					if ((accessTime >= (paw.getActivityStartTime() - guardTime))
							&& (accessTime <= (paw.getActivityStopTime() + guardTime))) {
						/**
						 * The access is inside a not ferrerable paw and is not based on so it does not
						 * make sense to use it
						 */
						retval = false;
					}
				}

			} // end for

		} // end if

		return retval;
	}// end method

	/**
	 * Return true if the DTO falls inside a not deferreable PAW
	 *
	 * @param dto
	 * @return true if the DTO falls inside a not deferreable PAW
	 */
	public boolean checkIfDTOFallsInsideNotDeferreablePAW(DTO dto) {
		/**
		 * Retval
		 */
		boolean retval = false;
		if (this.pawList != null) {
			/**
			 * DTO timing parameters
			 */
			double dtoStopTime = dto.getStopTime();
			double dtoStartTime = dto.getStartTime();
			double activityStart;
			double activityStop;

			for (PlatformActivityWindowBean paw : this.pawList) {
				/**
				 * for each paw evaluate start anfd stop
				 */
				activityStart = paw.getActivityStartTime();
				activityStop = paw.getActivityStopTime();

				if ((dtoStopTime < activityStart) || (dtoStartTime > activityStop)) {
					/**
					 * DTO outside check the next paw
					 */
					continue;
				} else {
					if (!paw.isDeferrableFlag()) {
						/**
						 * DTO inside not deferreble paw have exit
						 */
						retval = true;
						break;
					}
				}

			} // end for
		} // end if

		// double dtoStartTime = dto.getStartTime();
		return retval;
	}// end checkIfDTOFallsInsideNotDeferreablePAW

	
	public boolean checkIfDTOFallsInsideNotDeferreableCheckWithPAW(DTO dto, ArrayList<PlatformActivityWindowBean> satellitePawMap) {
		/**
		 * Retval
		 */
		boolean retval = false;
		if (satellitePawMap != null && !satellitePawMap.isEmpty()) {
			/**
			 * DTO timing parameters
			 */
			double dtoStopTime = dto.getStopTime();
			double dtoStartTime = dto.getStartTime();
			double activityStart;
			double activityStop;

			for (PlatformActivityWindowBean paw : satellitePawMap) {
				/**
				 * for each paw evaluate start anfd stop
				 */
				activityStart = paw.getActivityStartTime();
				activityStop = paw.getActivityStopTime();

				if ((dtoStopTime < activityStart) || (dtoStartTime > activityStop)) {
					/**
					 * DTO outside check the next paw
					 */
					continue;
				} else {
					if (!paw.isDeferrableFlag()) {
						/**
						 * DTO inside not deferreble paw have exit
						 */
						retval = true;
						break;
					}
				}

			} // end for
		} // end if

		// double dtoStartTime = dto.getStartTime();
		return retval;
	}// end checkIfDTOFallsInsideNotDeferreablePAW

	/**
	 * Return true if the access belongs to an
	 *
	 * @param access
	 * @return
	 */
	public boolean checkAccessAgainstsatellitePass(Access access) {
		/**
		 * Retval
		 */
		boolean retval = false;
		double currentStartTime;
		double currentStopTime;
		double guard = FeasibilityConstants.half * this.getStripMapMinimalDuration();
		double currentAccessTime = access.getAccessTime();
		if (this.satellitePassList != null) {
			for (SatellitePassBean satPas : this.satellitePassList) {
				/**
				 * For each pass set start and stop
				 */
				currentStartTime = satPas.getVisibiliyStart() + guard;
				currentStopTime = satPas.getVisibilityStop() - guard;
				if ((currentAccessTime > currentStartTime) && (currentAccessTime < currentStopTime)) {
					/**
					 * Access inside pass have exit
					 */
					retval = true;
					break;
				} // end if
			} // end for
		} // end if
		return retval;
	} // end checkAccessAgainstsatellitePass

	/**
	 * Check if a DTO falls inside a satellite pass used in check for refine
	 *
	 * @param d
	 * @return true if the dto falls in a satellite pass
	 */
	/*
	 * public boolean checkIfDTOFallsInsideSatellitePass(DTO d) { // // retval //
	 * boolean retval = false; // // DTO start and stop // double
	 * dtoStart=d.getStartTime(); double dtoStop= d.getStopTime(); // //end check =
	 * dto.start+dwlduration // double endCheck = dtoStart+d.getDWLDuration();
	 *
	 * if(endCheck<dtoStop) { // // end check less dto duration // so // endcheck =
	 * dto stop // endCheck=dtoStop; }
	 *
	 * double currentSatellitePassStart=0; double currentSatellitePassStop=0;
	 * if(this.satellitePassList!=null) { for(SatellitePassBean currentPass :
	 * this.satellitePassList) { // // for each pass plan // check if dto.start ,
	 * dto.start + dto.dwl // falls inside pass visibility // //
	 * currentSatellitePassStart=currentPass.getVisibiliyStart();
	 * currentSatellitePassStop=currentPass.getVisibilityStop();
	 *
	 * if(dtoStart>=currentSatellitePassStart && endCheck <=
	 * currentSatellitePassStop) { retval=true; break; }
	 *
	 * } // end for }//end if
	 *
	 * return retval; }//end checkIfDTOFallsInsideSatellitePass
	 */

	/**
	 * Used for check hoel and extension
	 *
	 * @return a threshold to be taken into account in order to consuiidethe
	 *         satellite still usable
	 *
	 */
	public double getTreshold() {
		return this.threshold;
	}// en method

	/**
	 *
	 * @return mission name
	 */
	public String getMissionName() {
		return this.satellite.getMissionName();
	}// end method

	/**
	 * satellites equsls if have the same name
	 *
	 * @param obj (satellite)
	 * @true if equals
	 */
	@Override
	public boolean equals(Object obj) {
		Satellite s = (Satellite) obj;
		// evaluating
		return (this.satellite.getIdSatellite() == s.satellite.getIdSatellite());
	}// end method

	/**
	 * compare two satellute
	 *
	 * @param s satellite
	 * @return 0 if equals -1 if s> 1 if < s
	 */
	@Override
	public int compareTo(Satellite s) {
		/**
		 * used for sort activities by list sort
		 */
		int retval = 0;
		if (this.satellite.getIdSatellite() < s.satellite.getIdSatellite()) {
			retval = -1;
		} else if (this.satellite.getIdSatellite() > s.satellite.getIdSatellite()) {
			retval = 1;
		}
		return retval;
	}// end method

	public void updateMinMaxDuration(BeamBean currentBeam) {
//		logger.debug("MODIFICA : current beam with valid near and far :"+currentBeam.toString());
//		
//		logger.debug("stripMapMinimalDuration BEFORE "+DateUtils.fromCSKDurationToMilliSeconds(this.stripMapMinimalDuration));
//		logger.debug("stripMapMaximalDuration BEFORE "+DateUtils.fromCSKDurationToMilliSeconds(this.stripMapMaximalDuration));
//		logger.debug("spotLightTimeStep BEFORE "+DateUtils.fromCSKDurationToMilliSeconds(this.spotLightTimeStep));
//		logger.debug("spotLightHalfTimeStep BEFORE "+DateUtils.fromCSKDurationToMilliSeconds(this.spotLightHalfTimeStep));
//		logger.debug("sensorRestoreTime BEFORE "+DateUtils.fromCSKDurationToMilliSeconds(this.sensorRestoreTime));

		this.stripMapMinimalDuration = DateUtils.millisecondsToJulian(currentBeam.getDtoMinDuration()); // OK
		this.stripMapMaximalDuration = DateUtils.millisecondsToJulian(currentBeam.getDtoMaxDuration()); // ok
		this.spotLightTimeStep = DateUtils.millisecondsToJulian(currentBeam.getDtoMinDuration());// ok
		this.spotLightHalfTimeStep = FeasibilityConstants.half * this.spotLightTimeStep;// ok
		this.sensorRestoreTime = DateUtils.millisecondsToJulian(currentBeam.getResTime());
		this.threshold = this.sensorRestoreTime + (0.5 * this.stripMapMinimalDuration) + this.extraTimeThreshold;
//		
//		logger.debug("stripMapMinimalDuration AFTER "+currentBeam.getDtoMinDuration());
//		logger.debug("stripMapMaximalDuration AFTER "+currentBeam.getDtoMaxDuration());
//		logger.debug("spotLightTimeStep AFTER "+currentBeam.getDtoMinDuration());
//		logger.debug("spotLightHalfTimeStep AFTER "+(FeasibilityConstants.half * currentBeam.getDtoMinDuration()));
//		logger.debug("sensorRestoreTime AFTER "+currentBeam.getResTime());
	}

} // end class
