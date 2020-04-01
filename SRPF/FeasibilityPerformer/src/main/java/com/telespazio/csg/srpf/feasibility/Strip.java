/**
*
* MODULE FILE NAME:	AcqReq.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Class used modelize a satellite strip on grid pouint list
*
* PURPOSE:			Perform Feasibility
*
* CREATION DATE:	17-12-2015
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

import com.telespazio.csg.srpf.dataManager.bean.BeamBean;

/**
 * This Class represent a strip. It is charactreizes by: - Satellite - Beam -
 * StartTime (internally stored as CSK time) - StopTime (internally stored as
 * CSK time) - List af accessed involved in strip
 *
 * @author Amedeo Bancone
 *
 */
public class Strip implements Comparable<Strip>

{

    // static final Logger logger = LogManager.getLogger(Strip.class.getName());
    // id
    private int id;
    // private double startTime;
    // private double stopTime;
    // satellite
    private String satelliteId;
    // beam
    private String beamId;
    // access list
    private List<Access> accessList;
    // true in case of odref
    private boolean isOdrefBased;

    // list of accesses usable in algo
    private List<Access> stillUsableAccess;

    //MODIFICA Agiunto arrayList per gestione dei punti all'interno della mattonella della spotlight come nuovi punti per la strip associata
    private List<Access> stillUsableAccessForSpotlight = new ArrayList<Access>();
    // TODO To be deleted
    /// Specific for spotlight
    // this flag is used in spotlight algo. If true this strip has been already
    // used in a iteration
    private boolean alreadyChoosed = false;

    // boundary of the interval of Access no more usabel in spotlight algo
    private double upperTimeGuard = -1.0;
    // boundary of the interval of Access no more usabel in spotlight algo
    private double lowerTimeGuard = -1.0;

    /**
     * set the strip id
     *
     * @param id
     */
    public void setId(int id)
    {
        this.id = id;
    }// end method

    /**
     * @return the alreadyChoosed
     */
    public boolean isAlreadyChoosed()
    {
        return this.alreadyChoosed;
    }// end method

    /**
     * @param alreadyChoosed
     *            the alreadyChoosed to set
     */
    public void setAlreadyChoosed(boolean alreadyChoosed)
    {
        this.alreadyChoosed = alreadyChoosed;
    }// end method

    /**
     * @return the upperTimeGuard
     */
    public double getUpperTimeGuard()
    {
        return this.upperTimeGuard;
    }// end method

    /**
     * @param upperTimeGuard
     *            the upperTimeGuard to set
     */
    public void setUpperTimeGuard(double upperTimeGuard)
    {
        this.upperTimeGuard = upperTimeGuard;
    }// end method

    /**
     * @return the lowerTimeGuard
     */
    public double getLowerTimeGuard()
    {
        return this.lowerTimeGuard;
    }// end method

    /**
     * @param lowerTimeGuard
     *            the lowerTimeGuard to set
     */
    public void setLowerTimeGuard(double lowerTimeGuard)
    {
        this.lowerTimeGuard = lowerTimeGuard;
    }// end method

    /**
     * Return true if the access is outside the guard zone
     *
     * @param accessTime
     * @return true if the access is still usable
     */
    public boolean isAccessUsable(double accessTime)
    {
        // true if noy chosen
        if (!this.alreadyChoosed)
        {
            return true;
        }
        // usable if outside
        return (accessTime < this.lowerTimeGuard) || (accessTime > this.upperTimeGuard);
    }// end method

    /**
     * check if the strip can be used (spotlight )
     *
     * @return true if usable
     */
    public boolean isStripUsable()
    {
        // by default false
        boolean usable = false;

        // Check if the boundary interval is inside the tolerance interval of
        // the strip. only access out of the time guard interval can be used
        usable = this.stillUsableAccess.size() > 0;
        return usable;
    }// end method

    /**
     * It returns the Access holding the point
     *
     * @param p
     * @return the Access holding the point
     * @throws GridException
     */
    public Access getAccessForPoint(GridPoint p) throws GridException
    {
        Access access = null;
        // looping
        for (Access a : this.accessList)
        {
            if (a.getGridPoint().equals(p))
            {
                // contains
                access = a;
                break;
            } // end if
        } // end for

        if (access == null)
        {
            throw new GridException("point not in grid");
        } // end if
          // returining
        return access;
    }// end getAccessTimeForPoint

    public String getUnivocalKey()
    {
        // TODO: chiedere a Riccardo se così è univoco
        return this.getId() + "_" + this.getBeamId() + "_" + this.getSatelliteId() + "_" + this.getStartTime() + "_" + this.getStopTime();
    }

    // end Specific for spotlight

    /**
     *
     * @param id
     * @param startTime
     * @param stopTime
     * @param satelliteId
     * @param beamId
     * @param accessList
     */
    /*
     * public Strip(int id, double startTime, double stopTime, String
     * satelliteId, String beamId, List<Access> accessList)
     *
     * { super(); this.id=id; this.startTime = startTime; this.stopTime =
     * stopTime; this.satelliteId = satelliteId; this.beamId = beamId;
     * this.accessList = accessList;
     *
     * isOdrefBased = (accessList.get(0).getOrbitType() ==
     * FeasibilityConstants.OdrefType) &&
     * (accessList.get(accessList.size()-1).getOrbitType() ==
     * FeasibilityConstants.OdrefType) ; }
     */


    
    /**
     * Constructor
     *
     * @param id
     * @param accessList
     */
    public Strip(int id, List<Access> accessList)
    {
        this.id = id;
        this.accessList = accessList;
        Access a = accessList.get(0);
        // getting satellite
        this.satelliteId = a.getSatelliteId();
        // getting beam
        this.beamId = a.getBeamId();

        // set usable access to list of access
        this.stillUsableAccess = new ArrayList<>();
        this.stillUsableAccess.addAll(this.accessList);

        if (accessList.get(0).getOrbitType() == FeasibilityConstants.OdrefType)
        {
            this.isOdrefBased = true;
        } // end if

    }// end method

    /**
     *
     * @return thr list of accesses still usable
     */
    public List<Access> getStillUsableAccessList()
    {
        return this.stillUsableAccess;
    }// end method

    /**
     * Set the list of usable access
     *
     * @param stillUsableAccess
     */
    public void setStillUsableAccessList(ArrayList<Access> stillUsableAccess)
    {
        this.stillUsableAccess = stillUsableAccess;
    }// end method

    /**
     * Reset the usebale access in the strip
     */
    public void resetStillUsableAccessList()
    {
        // clear list
        this.stillUsableAccess.clear();
        // add all accesses
        this.stillUsableAccess.addAll(this.accessList);
    }// end method

    /**
     * check if the strip is fully based on odref data
     *
     * @return true if odref
     */
    public boolean isOdrefBased()
    {
        return this.isOdrefBased;
    }// end method

    /**
     * @return the id
     */
    public int getId()

    {
        return this.id;
    }// end method

    /**
     * @return the startTime
     */
    public double getStartTime()
    {
        // TODO : sort by start time desc
        double returntarTTime = 0;
        // getting size
        int size = this.accessList.size();
        // if empty return 0
        if (size > 0)
        {
            returntarTTime = this.accessList.get(0).getAccessTime();
        }
        return returntarTTime;
    }// end method
    /**
     * @param startTime
     *            the startTime to set
     */

    /*
     * public void setStartTime(double startTime)
     *
     * { this.startTime = startTime; }
     */
    /**
     * @return the stopTime
     */
    public double getStopTime()

    {
        // getting size
        int size = this.accessList.size();
        // if empty return 0
        if (size == 0)
        {
            return 0;
        }
        return this.accessList.get(size - 1).getAccessTime();
    }// end method
    /**
     * @param stopTime
     *            the stopTime to set
     */

    /*
     * public void setStopTime(double stopTime)
     *
     * { this.stopTime = stopTime; }
     */
    /**
     * @return the satelliteId
     */
    public String getSatelliteId()

    {
        return this.satelliteId;
    }// end method

    /**
     * @param satelliteId
     *            the satelliteId to set
     */
    public void setSatelliteId(String satelliteId)

    {
        this.satelliteId = satelliteId;
    }// end method

    /**
     * @return the beamId
     */
    public String getBeamId()

    {
        return this.beamId;
    }// end method

    /**
     * @param beamId
     *            the beamId to set
     */
    public void setBeamId(String beamId)

    {
        this.beamId = beamId;
    }// end method

    public BeamBean getBeam()
    {
        return this.accessList.get(0).getBeam();
    }// end method

    /**
     * @return the accessList
     */
    public List<Access> getAccessList()

    {
        return this.accessList;
    }// end method

    /**
     * @param accessList
     *            the accessList to set
     */
    public void setAccessList(List<Access> accessList)

    {
        this.accessList = accessList;
    }// end method

    /**
     * return the stip duration
     *
     * @return Strip duration
     */
    public double duration()
    {
        return (this.getStopTime() - this.getStartTime());
    }// end method

    /**
     *
     * @param p
     * @return true if contains point
     */
    public boolean containsPoint(GridPoint p)
    {
        // logger.debug("Searching for point: " + p.getId());

        boolean retval = false;
        for (Access a : this.stillUsableAccess)
        {
            if (a.getGridPoint().getId() == p.getId())
            {
                // contains
                retval = true;

                return retval;
            } // end if
        } // end for
          // returning
        return retval;
    }// end containsPoint

    /**
     * For test purposes only
     */
    /*
     * public String dumpToString() { String retval =""; StringWriter out = new
     * StringWriter(); out.write("StripID: "+this.id +"; Sat id: " +
     * this.satelliteId + "; beamId:  " + this.beamId + "; startTime:  " +
     * DateUtils.fromCSKDateToISOFMTDateTime(this.getStartTime()) +
     * "; stopTime " +
     * DateUtils.fromCSKDateToISOFMTDateTime(this.getStopTime())+"\n");
     *
     * for(Access a : this.accessList) { out.write(a.dumpToStringReduced());
     * out.write("\n"); }
     *
     * retval = out.toString(); return retval; }
     */

    /**
     * Compare two strips on id basis
     *
     * @param obj
     * @return true if equals
     */
    @Override
    public boolean equals(Object obj)
    {
        // cast
        Strip s = (Strip) obj;
        return (this.id == s.id);
    }// end method

    /**
     * Compare two strips on id basis
     *
     * @param s
     * @return - 0 s.id = this.id - -1 if s.id > this.id - 1 if s.id<this.id
     */
    @Override
    public int compareTo(Strip s)
    {
        // default 0
        int retval = 0;
        if (this.id < s.id)
        {
            retval = -1;
        } // end if
        else if (this.id > s.id)
        {
            retval = 1;
        } // end else
          // returning
        return retval;
    }// end method

    @Override
    public String toString()
    {
        return "Strip [id=" + this.id + ", satelliteId=" + this.satelliteId + ", beamId=" + this.beamId + ", accessList=" + this.accessList.size() + ", isOdrefBased=" + this.isOdrefBased + ", stillUsableAccess=" + this.stillUsableAccess.size() + ", alreadyChoosed=" + this.alreadyChoosed + ", upperTimeGuard=" + this.upperTimeGuard + ", lowerTimeGuard=" + this.lowerTimeGuard + "]";
    }

    public List<Access> getStillUsableAccessForSpotlight()
    {
        return stillUsableAccessForSpotlight;
    }

    public void setStillUsableAccessForSpotlight(List<Access> stillUsableAccessForSpotlight)
    {
        this.stillUsableAccessForSpotlight = stillUsableAccessForSpotlight;
    }

}// end class
