/**
*
* MODULE FILE NAME:	PlatformActivityWindowBean.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Define a structure to model Sensor modes in DB
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

/**
 * Define a structure to model Sensor modes in DB
 * 
 * @author Abed Alissa
 * @version 1.0
 *
 */
public class SensorModeBean
{

    // Name
    private String sensorModeName;
    // id
    private int idsensorMode;
    // true if spotlight
    private boolean isSpotLight;

    /**
     * @author Abed Alissa
     * @version 1.0
     * @since
     * @return id of sensor mode
     */
    public int getIdsensorMode()
    {
        return this.idsensorMode;
    }// end method

    /**
     * Set sensor mode
     * 
     * @author Abed Alissa
     * @version 1.0
     * @since
     * @param idsensorMode
     */
    public void setIdsensorMode(int idsensorMode)
    {
        this.idsensorMode = idsensorMode;
    }// end method

    /**
     * @author Abed Alissa
     * @version 1.0
     * @since
     * @return name of sensor mode
     */
    public String getSensorModeName()
    {
        return this.sensorModeName;
    }// end method

    /**
     * Ser sensor mode name
     * 
     * @author Abed Alissa
     * @version 1.0
     * @since
     * @param sensorModeName
     */
    public void setSensorModeName(String sensorModeName)
    {
        this.sensorModeName = sensorModeName;
    }// end method

    /**
     * 
     * @return true if spotlght
     */
    public boolean isSpotLight()
    {
        return this.isSpotLight;
    }// end method

    /**
     * set if spotlight
     * 
     * @param isSpotLight
     */
    public void setSpotLight(boolean isSpotLight)
    {
        this.isSpotLight = isSpotLight;
    }// end method

}// end class
