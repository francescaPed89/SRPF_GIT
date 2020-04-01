/**
*
* MODULE FILE NAME:	PlatformActivityWindowBean.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Define a structure to model PAW in DB
*
* PURPOSE:			Used for DB data
*
* CREATION DATE:	09-01-2016
*
* AUTHORS:			Abed Alissa
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

import com.telespazio.csg.srpf.utils.DateUtils;

/**
 * Define a structure to model PAW in DB
 * 
 * @author Abed Alissa
 * @version 1.0
 *
 */
public class PlatformActivityWindowBean
{

    // sat id as for DB
    private int satId;

    // sat name
    private String satName;

    // activity id
    private long activityId;

    // activity type
    private String activityType;

    // start time
    private double activityStartTime;

    // stop time
    private double activityStopTime;

    // true for deferreable
    private boolean deferrableFlag;

    /**
     * @author Abed Alissa
     * @version 1.0
     * @date
     * @return sat name
     */
    public String getSatName()
    {
        return this.satName;
    }// end method

    /**
     * set sat name
     * 
     * @author Abed Alissa
     * @version 1.0
     * @date
     * @param satName
     */
    public void setSatName(String satName)
    {
        this.satName = satName;
    }// end method

    /**
     * @author Abed Alissa
     * @version 1.0
     * @date
     * @return true if deferreable
     */
    public boolean isDeferrableFlag()
    {
        return this.deferrableFlag;
    }// end method

    /**
     * Set deferreable flag
     * 
     * @author Abed Alissa
     * @version 1.0
     * @date
     * @param deferrableFlag
     */
    public void setDeferrableFlag(boolean deferrableFlag)
    {
        this.deferrableFlag = deferrableFlag;
    }// end method

    /**
     * @author Abed Alissa
     * @version 1.0
     * @date
     * @return start time
     */
    public double getActivityStartTime()
    {
        return this.activityStartTime;
    }// end method

    /**
     * set start time
     * 
     * @param activityStartTime
     */
    public void setActivityStartTime(double activityStartTime)
    {
        this.activityStartTime = activityStartTime;
    }// end method

    /**
     * 
     * @return activity stop tyme
     */
    public double getActivityStopTime()
    {
        return this.activityStopTime;
    }// end method

    /**
     * set stop time
     * 
     * @param activityStopTime
     */
    public void setActivityStopTime(double activityStopTime)
    {
        this.activityStopTime = activityStopTime;
    }// end method

    /**
     * 
     * @return type
     */
    public String getActivityType()
    {
        return this.activityType;
    }// end method

    /**
     * Set type
     * 
     * @param activityType
     */
    public void setActivityType(String activityType)
    {
        this.activityType = activityType;
    }// end method

    /**
     * 
     * @return sat id as for db
     */
    public int getSatId()
    {
        return this.satId;
    }// end method

    /**
     * Set sat id
     * 
     * @param satId
     */
    public void setSatId(int satId)
    {
        this.satId = satId;
    }// end method

    /**
     * 
     * @return activity id
     */
    public long getActivityId()
    {
        return this.activityId;
    }// end method

    /**
     * Set activity id
     * 
     * @param activityId
     */
    public void setActivityId(long activityId)
    {
        this.activityId = activityId;
    }// end method

	@Override
	public String toString() {
		return "PlatformActivityWindowBean [satId=" + satId + ", satName=" + satName + ", activityId=" + activityId
				+ ", activityType=" + activityType + ", activityStartTime=" + DateUtils.fromCSKDateToDateTime(activityStartTime) + ", activityStopTime="
				+ DateUtils.fromCSKDateToDateTime(activityStopTime) + ", deferrableFlag=" + deferrableFlag + "]";
	}

}// end class
