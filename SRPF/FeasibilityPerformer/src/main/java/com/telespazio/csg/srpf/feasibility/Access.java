/**
*
* MODULE FILE NAME:	Access.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Class used to modelize an access on a grid point
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
*     06-03-2018      | Amedeo Bancone  |2.0 | Added Copy contructor
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*
* @author Amedeo Bancone
* @version  2.0
*/

package com.telespazio.csg.srpf.feasibility;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.telespazio.csg.srpf.dataManager.bean.BeamBean;
import com.telespazio.csg.srpf.dataManager.bean.EpochBean;
import com.telespazio.csg.srpf.utils.ReferenceFrameUtils;

/**
 * Class holding the data related to Access on a grid point
 *
 * @author Amedeo Bancone
 * @version 2.0
 *
 *
 */
public class Access implements Comparable<Access>
{
	static final Logger logger = LogManager.getLogger(Access.class.getName());

//    @Override
//    public String toString()
//    {
//        return "Access [point=" + point + ", missionName=" + missionName + ", accessTime=" + accessTime + ", offNadir=" + offNadir + ", lookSide=" + lookSide + ", orbitDirection=" + orbitDirection + ", orbitId=" + orbitId + ", satellitePos=" + satellitePos + ", satelliteVel=" + satelliteVel + ", orbitType=" + orbitType + ", beam=" + beam + ", satellite=" + satellite + ", startingPointWindowIndex=" + startingPointWindowIndex + ", id=" + id + "]";
//    }

    @Override
    public String toString()
    {
//    	logger.debug("accessTime "+this.getAccessTime());
//    	logger.debug("lookSide "+this.getLookSide());
//    	logger.debug("orbitDirection "+this.getOrbitDirection());
//    	logger.debug("orbitId "+this.getOrbitId());
//    	logger.debug("satellitePos "+this.getSatellitePos());
//    	logger.debug("satelliteVel "+this.getSatelliteVel());
//    	logger.debug("orbitType "+this.getOrbitType());
//    	logger.debug("beam "+this.getBeam());
        return "" ;
    }
    public GridPoint getPoint() {
		return point;
	}
	/*
     * + A grid point
     */
    private GridPoint point = null;
    /*
     * + The mission nane
     */

    private String missionName = null;
    /**
     * Julian time
     */

    private double accessTime = 0;

    /**
     * off nadir angle
     */
    private double offNadir = 0;

    /**
     * look side
     */
    private int lookSide = 0;

    /**
     * orbit direction
     */
    private int orbitDirection = 0;

    /**
     * orbit id number
     */
    private long orbitId = 0;

    /**
     * Satellite position
     */
    private Vector3D satellitePos = null;

    /**
     * Satlellite velocity
     */
    private Vector3D satelliteVel = null;

    /**
     * detemine if ODSTP OMTP ODNOM ODREF
     */
    private int orbitType =0;

    /**
     * The bean of the representing the beam
     */
    private BeamBean beam =null;

    /**
     * Satellite accessing the point
     */
    private Satellite satellite = null;

    /**
     * starting index of the time window involved in the access
     */
    private int startingPointWindowIndex = 0;

    /**
     * Unique identifier
     */
    int id=0;

    /**
     * Set the unique id
     *
     * @param id
     */
    public void setId(int id)
    {
        /**
         * The id
         */
        this.id = id;
    }// end method

    /**
     * return the unique id
     *
     * @return the unique id
     */
    public int getId()
    {
        return this.id;
    }// end method

    /**
     * Constructor
     *
     * @param missionName
     * @param point
     *            grid point
     * @param satelliteId
     * @param accessTime
     * @param offNadir
     * @param lookSide
     * @param beam
     * @param orbitDirection
     * @param orbitId
     * @param satellitePos
     * @param satelliteVel
     * @param orbitType
     * @param startingPointWindowIndex,
     *            starting point of the sliding window where the access fall
     */
    public Access(String missionName, GridPoint point, Satellite satellite, double accessTime, double offNadir, int lookSide, final BeamBean beam, int orbitDirection, long orbitId, Vector3D satellitePos, Vector3D satelliteVel, int orbitType, int startingPointWindowIndex)
    {
        logger.debug("inside access");

        /**
         *
         * Setting the parameters
         *
         */
        this.missionName = missionName;
        this.point = point;
        this.accessTime = accessTime;
        this.offNadir = offNadir;
        this.lookSide = lookSide;
        this.orbitDirection = orbitDirection;
        this.orbitId = orbitId;
        this.satellitePos = satellitePos;
        this.satelliteVel = satelliteVel;
        this.orbitType = orbitType;
        this.beam = beam;
        this.startingPointWindowIndex = startingPointWindowIndex;
        this.satellite = satellite;


    }// end method
    
    public Access(String missionName, GridPoint point, double accessTime, double offNadir, int lookSide, final BeamBean beam, int orbitDirection, long orbitId, Vector3D satellitePos, Vector3D satelliteVel, int orbitType, int startingPointWindowIndex)
    {
   

        /**
         *
         * Setting the parameters
         *
         */
        this.missionName = missionName;
        this.point = point;
        this.accessTime = accessTime;
        this.offNadir = offNadir;
        this.lookSide = lookSide;
        this.orbitDirection = orbitDirection;
        this.orbitId = orbitId;
        this.satellitePos = satellitePos;
        this.satelliteVel = satelliteVel;
        this.orbitType = orbitType;
        this.beam = beam;
        this.startingPointWindowIndex = startingPointWindowIndex;
   


    }// end method

    /**
     * Copy Constructor
     *
     * @param acc
     */
    public Access(Access acc)
    {
        /**
         *
         * Setting the parameters
         *
         */
        this.missionName = acc.missionName;
        this.point = acc.point;
        this.satellite = acc.satellite;
        this.accessTime = acc.accessTime;
        this.offNadir = acc.offNadir;
        this.lookSide = acc.lookSide;
        this.orbitDirection = acc.orbitDirection;
        this.orbitId = acc.orbitId;
        this.satellitePos = acc.satellitePos;
        this.satelliteVel = acc.satelliteVel;
        this.orbitType = acc.orbitType;
        this.beam = acc.beam;
        this.startingPointWindowIndex = acc.startingPointWindowIndex;
    }// end method

    /**
     * Default constr
     */
    public Access()
    {

    }// end method

    /**
     * @return the startingPointWindowIndex
     */
    public int getStartingPointWindowIndex()
    {
        /**
         * index
         */
        return this.startingPointWindowIndex;
    }// end method

    /**
     * @param startingPointWindowIndex
     *            the startingPointWindowIndex to set
     */
    public void setStartingPointWindowIndex(int startingPointWindowIndex)
    {
        /**
         * The index
         */
        this.startingPointWindowIndex = startingPointWindowIndex;
    }// end method

    /**
     * @return the beam
     */
    public BeamBean getBeam()
    {
        /**
         * The beam
         */
        return this.beam;
    }// end method

    /**
     * @param beam
     *            the beam to set
     */
    public void setBeam(BeamBean beam)
    {
        /**
         * The beam
         */
        this.beam = beam;
    }// end method

    /**
     * Set the orbit type
     *
     * @param orbitType
     *            the orbitType to set
     */
    public void setOrbitType(int orbitType)
    {
        /**
         * The orbit type
         */
        this.orbitType = orbitType;
    }// end method

    /**
     * @return the missionName
     */
    public String getMissionName()
    {
        /**
         * The mission name
         */
        return this.missionName;
    }// end method

    /**
     * @param missionName
     *            the missionName to set
     */
    public void setMissionName(String missionName)
    {
        /**
         * Mission
         */
        this.missionName = missionName;
    }// end method

    /**
     * Used only for test porposes
     *
     * @return
     */
    /*
     * public String dumpToString() { String retval ="";
     *
     * StringWriter out = new StringWriter(); out.write(point.getId()+";");
     * out.write(String.format("%f;%f;%f;",
     * point.getLLH()[0],point.getLLH()[1],point.getLLH()[2]));
     * out.write(satellite.getName()+";"); out.write(orbitId+";");
     *
     * String orbitDirectionString =
     * (orbitDirection==FeasibilityConstants.AscendingOrbit)?"A":"D";
     * out.write(orbitDirectionString+";");
     *
     * String lookSideString =
     * (lookSide==FeasibilityConstants.RighLookSide)?"R":"L";
     * out.write(lookSideString+";");
     *
     * out.write(this.getBeamId()+";"); out.write(offNadir+";");
     * //out.write(DateUtils.fromJulianToISOFMT(accessTime)+":");
     * out.write(DateUtils.fromCSKDateToISOFMTDateTime(accessTime)+";"); double
     * [] llhSat = ReferenceFrameUtils.ecef2llh(satellitePos.toArray(), true);
     * out.write(String.format("%f;%f;%f;",llhSat[0],llhSat[1],llhSat[2]));
     * //out.write(String.format("%f;%f;%f;",satellitePos.getX(),satellitePos.
     * getY(),satellitePos.getZ()));
     * out.write(String.format("%f;%f;%f",satelliteVel.getX(),satelliteVel.getY(
     * ),satelliteVel.getZ())); retval = out.toString(); return retval; }
     */

    /**
     * Methods that dumps a subset of access information to String: Inteded for
     * test purposes
     */
    /*
     * public String dumpToStringReduced() { String retval ="";
     *
     * StringWriter out = new StringWriter(); out.write(point.getId()+";");
     * out.write(String.format("%f;%f;%f;",
     * point.getLLH()[0],point.getLLH()[1],point.getLLH()[2]));
     * out.write(satellite.getName()+";");
     *
     * String orbitDirectionString =
     * (orbitDirection==FeasibilityConstants.AscendingOrbit)?"A":"D";
     * out.write(orbitDirectionString+";"); String lookSideString =
     * (lookSide==FeasibilityConstants.RighLookSide)?"R":"L";
     * out.write(lookSideString+";"); out.write(this.getBeamId()+";");
     * out.write(offNadir+";");
     * out.write(DateUtils.fromCSKDateToISOFMTDateTime(accessTime)+";");
     *
     * double [] llhSatPos= ReferenceFrameUtils.ecef2llh(satellitePos.toArray(),
     * true); out.write(String.format("%f;%f;%f;",
     * llhSatPos[0],llhSatPos[1],llhSatPos[2]));
     *
     * retval = out.toString(); return retval; }
     */
    /**
     * the ground speed module at access point
     *
     * @return the ground speed module at access point
     */
    public double getSatGroundSpeed()
    {
        /**
         * Evaluating the ground speed Module
         *
         */
        double[] llhSat = ReferenceFrameUtils.ecef2llh(this.satellitePos, true);
        double a = ReferenceFrameUtils.wgs84_a + llhSat[2];
        return this.satelliteVel.getNorm() * (ReferenceFrameUtils.wgs84_a / a);
    }// end method

    /**
     * average satellite speed at ground near the accessed point
     *
     * @return the average satellite speed at ground near the accessed point
     */
    public double getAverageSatelliteGroundSpeed()
    {
        /**
         * LLH position
         */
        double[] llhSat = ReferenceFrameUtils.ecef2llh(this.satellitePos, true);

        double a = ReferenceFrameUtils.wgs84_a + llhSat[2];

        double b = ReferenceFrameUtils.wgs84_a / a;

        EpochBean startingEpoch = this.satellite.getEpochs().get(this.startingPointWindowIndex + 1);
        EpochBean endingEpoch = this.satellite.getEpochs().get(this.startingPointWindowIndex + 2);

        Vector3D initialSatVel = startingEpoch.getoVxVyVz();
        Vector3D endingSatVel = endingEpoch.getoVxVyVz();

        /**
         * Average speed
         */
        double avgSpeed = FeasibilityConstants.half * (endingSatVel.getNorm() + initialSatVel.getNorm()) * b;

        return avgSpeed;
    }// end method

    /**
     * @return the pointId
     */
    public GridPoint getGridPoint()
    {
        /**
         * The grid point
         */
        return this.point;
    }// end method

    /**
     * @param pointId
     *            the pointId to set
     */
    public void setPoint(GridPoint point)
    {
        /**
         * The grid point
         */
        this.point = point;
    }// end method

    /**
     * @return the satelliteId
     */
    public String getSatelliteId()
    {
        return this.satellite.getName();
    }// end method

    /**
     * @return the satellite
     */
    public Satellite getSatellite()
    {
        return this.satellite;
    }// end method

    /**
     * @param satellite
     *            the satellite to set
     */
    public void setSatellite(Satellite satellite)
    {
        this.satellite = satellite;
    }// end method

    /**
     * @return the accessTime
     */
    public double getAccessTime()
    {
        return this.accessTime;
    }// end method

    /**
     * @param accessTime
     *            the accessTime to set
     */
    public void setAccessTime(double accessTime)
    {
        this.accessTime = accessTime;
    }// end method

    /**
     * @return the offNadir
     */
    public double getOffNadir()
    {
        return this.offNadir;
    }// end method

    /**
     * @param offNadir
     *            the offNadir to set
     */
    public void setOffNadir(double offNadir)
    {
        this.offNadir = offNadir;
    }// end method

    /**
     * @return the lookSide
     */
    public int getLookSide()
    {
        return this.lookSide;
    }// end method

    /**
     * @param lookSide
     *            the lookSide to set
     */
    public void setLookSide(int lookSide)
    {
        this.lookSide = lookSide;
    }// end method

    /**
     * @return the beamId
     */
    public String getBeamId()
    {
        return this.beam.getBeamName();
    }// end method

    /**
     *
     * /**
     *
     * @return the orbitDirection
     */
    public int getOrbitDirection()
    {
        return this.orbitDirection;
    }// end method

    /**
     * @param orbitDirection
     *            the orbitDirection to set
     */
    public void setOrbitDirection(int orbitDirection)
    {
        this.orbitDirection = orbitDirection;
    }// end method

    /**
     * @return the orbitId
     */
    public long getOrbitId()
    {
        return this.orbitId;
    }// end method

    /**
     * @param orbitId
     *            the orbitId to set
     */
    public void setOrbitId(long orbitId)
    {
        this.orbitId = orbitId;
    }// end method

    /**
     * @return the satellitePos
     */
    public Vector3D getSatellitePos()
    {
        return this.satellitePos;
    }// end method

    /**
     * @param satellitePos
     *            the satellitePos to set
     */
    public void setSatellitePos(Vector3D satellitePos)
    {
        this.satellitePos = satellitePos;
    }// end method

    /**
     * @return the satelliteVel
     */
    public Vector3D getSatelliteVel()
    {
        return this.satelliteVel;
    }// end method

    /**
     * @param satelliteVel
     *            the satelliteVel to set
     */
    public void setSatelliteVel(Vector3D satelliteVel)
    {
        this.satelliteVel = satelliteVel;
    }// end method

    /**
     *
     * @return type of the orbit
     */
    public int getOrbitType()
    {
        return this.orbitType;
    }// end method

    /**
     * Compare two access on id basis
     *
     * @return - 0 if same id - -1 if id < id of second access - 1 id id > id of
     *         second access
     */
    @Override
    public int compareTo(Access a)
    {
        /**
         * equals
         */
        int retval = 0;
        if (this.id < a.id)
        {
            /**
             * less
             */
            retval = -1;
        } // end if
        else if (this.id > a.id)
        {
            /**
             * major
             */
            retval = 1;
        } // end else
        return retval;

    }// end method

    /**
     * @return true if the two id are the same
     */
    @Override
    public boolean equals(Object obj)
    {
        Access s = (Access) obj;
        /**
         * True if the id are the same
         */
        boolean retval = (this.id == s.id);
        return retval;
    }// end method

}// end class
