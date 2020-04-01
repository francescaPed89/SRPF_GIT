/**
*
* MODULE FILE NAME:	SatelliteBean.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Define a structure to model Satellite in DB
*
* PURPOSE:			Used for DB data
*
* CREATION DATE:	09-01-2016
*
* AUTHORS:			Abed Alissa
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
* 		10-11-2026 | Amedeo Bancone  | 2.0            |Added the fields and methos to manage trak number
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.dataManager.bean;

/**
 * Define a structure to model Satellite in DB
 * 
 * @author Abed Alissa
 * @version 2.0
 *
 *
 */
public class SatelliteBean implements Comparable<SatelliteBean>
{

    // Sat id as for DB
    private int idSatellite;

    // Mission ID as for DB
    private int idMission;

    // Satellite name
    private String satelliteName;

    // Mission Name
    private String missionName;

    // true if enabled
    private int isEnabled;

    // Allowed look side
    private String allowedLookSide;

    // id of allowed look side
    private int idAllowedLookSide;

    // satellite track offset
    private int trackOffset;

    /**
     * @author Amedeo Bancone
     * @return trackOffset
     */
    public int getTrackOffset()
    {
        return this.trackOffset;
    }// end method

    /**
     * Set the track offset
     * 
     * @author Amedeo Bancone
     * @param trackOffset
     */
    public void setTrackOffset(int trackOffset)
    {
        this.trackOffset = trackOffset;
    }

    /**
     * @return sat id
     */
    public int getIdSatellite()
    {
        return this.idSatellite;
    }// end method

    /**
     * @param idSatellite
     */
    public void setIdSatellite(int idSatellite)
    {
        this.idSatellite = idSatellite;
    }// end method

    /**
     * @return mission id
     */
    public int getIdMission()
    {
        return this.idMission;
    }// end method

    /**
     * Set mission id
     * 
     * @param idMission
     */
    public void setIdMission(int idMission)
    {
        this.idMission = idMission;
    }// end method

    /**
     * @return satellite name
     */
    public String getSatelliteName()
    {
        return this.satelliteName;
    }// end method

    /**
     * Set sat name
     * 
     * @param satelliteName
     */
    public void setSatelliteName(String satelliteName)
    {
        this.satelliteName = satelliteName;
    }// end method

    /**
     * @return mission name
     */
    public String getMissionName()
    {
        return this.missionName;
    }// end method

    /**
     * set mission name
     * 
     * @param missionName
     */
    public void setMissionName(String missionName)
    {
        this.missionName = missionName;
    }// end method

    /**
     * 
     * @return allowed side
     */
    public String getAllowedLookSide()
    {
        return this.allowedLookSide;
    }// end method

    /**
     * set allowed look side
     * 
     * @param allowedLookSide
     */
    public void setAllowedLookSide(String allowedLookSide)
    {
        this.allowedLookSide = allowedLookSide;
    }// end method

    /**
     * Return id of allowed look side: - 0 none - 1 Right - 2 Left - 3 Both
     * 
     * @return id of look side
     */
    public int getIdAllowedLookSide()
    {
        return this.idAllowedLookSide;
    }// end method

    /**
     * Set id of allowed look side - 0 none - 1 Right - 2 Left - 3 Both
     * 
     * @param idAllowedLookSide
     */
    public void setIdAllowedLookSide(int idAllowedLookSide)
    {
        this.idAllowedLookSide = idAllowedLookSide;
    }// end method

    /**
     * Set if is enabled
     * 
     * @param isEnabled
     */
    public void setIsEnabled(int isEnabled)
    {
        this.isEnabled = isEnabled;
    }// end method

    /**
     * 
     * @return f enablebed
     */
    public int getIsEnabled()
    {
        return this.isEnabled;
    }// end method

    /**
     * Compare satellites on Name basis
     * 
     * @param o
     *            other satellite
     */
    @Override
    public int compareTo(SatelliteBean o)
    {
        return this.satelliteName.compareTo(o.satelliteName);

    }// end method

}// end class
