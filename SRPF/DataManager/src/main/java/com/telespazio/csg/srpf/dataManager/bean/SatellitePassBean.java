/**
*
* MODULE FILE NAME:	SatellitePassBean.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			It modelizes a Satellite Pass
*
* PURPOSE:			Used in passthrough feasibility
*
* CREATION DATE:	13-12-2016
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

package com.telespazio.csg.srpf.dataManager.bean;

/**
 *
 * SatellitePass bean
 * 
 * @author Amedeo Bancone
 * @version 1.0
 *
 */
public class SatellitePassBean

{

    // Identify the sattellite whose the pass belongs
    private String satelliteName = "";

    // id of the satellite
    private int satelliteId = 0;

    // Identify the Acquisition station identifier;
    private String asId;

    // Identify the contact counter for the satellite pass
    private long cntactCounter;

    // Starting of the visibility window
    private double visibiliyStart;

    // Ending of the visibility window
    private double visibilityStop;

    /**
     * return the id of the satellite
     * 
     * @return id
     */
    public int getSatelliteId()
    {
        return this.satelliteId;
    }// end method

    /**
     * set the satellite id
     * 
     * @param satelliteId
     */
    public void setSatelliteId(int satelliteId)
    {
        this.satelliteId = satelliteId;
    }// end method

    /**
     * Return the satellite name
     * 
     * @return name
     */
    public String getSatelliteName()

    {
        return this.satelliteName;
    }// end method

    /**
     * Set the satellite name
     * 
     * @param satelliteName
     */
    public void setSatelliteName(String satelliteName)
    {
        this.satelliteName = satelliteName;
    }// end method

    /**
     * 
     * @return the acquisition station ID
     */
    public String getAsId()
    {
        return this.asId;
    }// end method

    /**
     * Set the acquisition station id
     * 
     * @param asId
     */
    public void setAsId(String asId)
    {
        this.asId = asId;
    }// end method

    /**
     * Get the contact counter
     * 
     * @return contact counter
     */
    public long getCntactCounter()
    {
        return this.cntactCounter;
    }// end method

    /**
     * Set the contact counter
     * 
     * @param cntactCounter
     */
    public void setContactCounter(long cntactCounter)
    {
        this.cntactCounter = cntactCounter;
    }// end method

    /**
     * Get the visibility start time : CSK time
     * 
     * @return the visitbility start time
     */
    public double getVisibiliyStart()
    {
        return this.visibiliyStart;
    }// end method

    /**
     * Set the visibility start time
     * 
     * @param visibiliyStart
     */
    public void setVisibiliyStart(double visibiliyStart)
    {
        this.visibiliyStart = visibiliyStart;
    }// end method

    /**
     * Get the visibility stop time
     * 
     * @returnvisibility stop time
     */
    public double getVisibilityStop()
    {
        return this.visibilityStop;
    }// end method

    /**
     * Set the visibility stop time
     * 
     * @param visibilityStop
     */
    public void setVisibilityStop(double visibilityStop)
    {
        this.visibilityStop = visibilityStop;
    }// end method

}// end class
