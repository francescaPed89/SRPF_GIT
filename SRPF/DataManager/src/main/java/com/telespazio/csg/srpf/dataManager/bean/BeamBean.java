/**
*
* MODULE FILE NAME:	BeamBean.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Define a structure used to model Beams in DB
*
* PURPOSE:			Used for DB data
*
* CREATION DATE:	10-01-2016
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
 * Define a structure used to model Beams in DB
 * 
 * @author Abed Alissa
 * @version 1.0
 *
 */
public class BeamBean
{


    // sat name
    private String satName;

    // beam name
    private String beamName;

    // near off nadir (specifc for sat)
    private double nearOffNadir;

    // far off nadir (specifc for sat)
    private double farOffNadir;

    // sensor mode
    private int sensorMode;

    // sensor mode name
    private String sensorModeName;

    // true if beam is enabled on satellite
    private int isEnabled;

    // near off nadir
    private double nearOffNadirBeam;

    // far off nadir
    private double farOffNadirBeam;

    // image dimension 1
    private double swDim1;

    // image dimension 2
    private double swDim2;

    // true if spotlight
    private boolean isSpotLight;

    // id on DB
    private int idBeam;

    // dto main duration
    private int dtoMinDuration;

    // dto max duration
    private int dtoMaxDuration;

    // restore time
    private int resTime;

    
    private int dtoDurationSquared = 0;
    
    /**
     * @author Abed Alissa
     * @version 1.0
     * @since
     * @return beamName
     */
    public String getBeamName()
    {
        return this.beamName;
    }// end method

    /**
     * @author Abed Alissa
     * @version 1.0
     * @since
     * @return farOffNadir
     */
    public double getFarOffNadir()
    {
        return this.farOffNadir;
    }// end method

    /**
     * @return true if enabled
     */
    public int getIdBeam()
    {
        return this.idBeam;
    }// end method

    public int getIsEnabled()
    {
        return this.isEnabled;
    }// end method

    /**
     * @return near offnadir
     */
    public double getNearOffNadir()
    {
        return this.nearOffNadir;
    }// end method

    /**
     * @return far off nadir
     */
    public String getSatName()
    {
        return this.satName;
    }// end method

    /**
     * @return sensor mode
     */
    public int getSensorMode()
    {
        return this.sensorMode;
    }// end method

    /**
     * set beam name
     * 
     * @param beamName
     */
    public void setBeamName(String beamName)
    {
        this.beamName = beamName;
    }// end method

    public void setFarOffNadir(double farOffNadir)
    {
        this.farOffNadir = farOffNadir;
    }// end method

    /**
     * set id
     * 
     * @param idBeam
     */
    public void setIdBeam(int idBeam)
    {
        this.idBeam = idBeam;
    }// end method

    public void setIsEnabled(int isEnabled)
    {
        this.isEnabled = isEnabled;
    }// end method

    /**
     * set off nadir
     * 
     * @param nearOffNadir
     */
    public void setNearOffNadir(double nearOffNadir)
    {
        this.nearOffNadir = nearOffNadir;
    }// end method

    /**
     * set sat name
     * 
     * @param satName
     */
    public void setSatName(String satName)
    {
        this.satName = satName;
    }// end method

    /**
     * set sensor mode
     * 
     * @param sensorMode
     */
    public void setSensorMode(int sensorMode)
    {
        this.sensorMode = sensorMode;
    }// end method

    /**
     * 
     * @return near off nadir
     */
    public double getNearOffNadirBeam()
    {
        return this.nearOffNadirBeam;
    }// end method

    /**
     * set near offnadir
     * 
     * @param nearOffNadirBeam
     */
    public void setNearOffNadirBeam(double nearOffNadirBeam)
    {
        this.nearOffNadirBeam = nearOffNadirBeam;
    }// end method

    /**
     * 
     * @return far offnadir
     */
    public double getFarOffNadirBeam()
    {
        return this.farOffNadirBeam;
    }// end method

    /**
     * set far off nadir
     * 
     * @param farOffNadirBeam
     */
    public void setFarOffNadirBeam(double farOffNadirBeam)
    {
        this.farOffNadirBeam = farOffNadirBeam;
    }// end method

    /**
     * 
     * @return dimension 1
     */
    public double getSwDim1()
    {
        return this.swDim1;
    }

    /**
     * set dimension 1
     * 
     * @param swDim1
     */
    public void setSwDim1(double swDim1)
    {
        this.swDim1 = swDim1;
    }// end method

    /**
     * 
     * @return dimendion 2
     */
    public double getSwDim2()
    {
        return this.swDim2;
    }// end method

    /**
     * set dimension 2
     * 
     * @param swDim2
     */
    public void setSwDim2(double swDim2)
    {
        this.swDim2 = swDim2;
    }// end method

    /**
     * 
     * @return true if beam is spotlight
     */
    public boolean isSpotLight()
    {
        return this.isSpotLight;
    }// end method

    /**
     * Set spotlightness
     * 
     * @param isSpotLight
     */
    public void setSpotLight(boolean isSpotLight)
    {
        this.isSpotLight = isSpotLight;
    }// end method

    /**
     * 
     * @return sensor mode name
     */
    public String getSensorModeName()
    {
        return this.sensorModeName;
    }

    public void setSensorModeName(String sensorModeName)
    {
        this.sensorModeName = sensorModeName;
    }// end method

    /**
     * 
     * @return minduration for DTO
     */
    public int getDtoMinDuration()
    {
        return this.dtoMinDuration;
    }// end method

    /**
     * set dto min duration
     * 
     * @param dtoMinDuration
     */
    public void setDtoMinDuration(int dtoMinDuration)
    {
        this.dtoMinDuration = dtoMinDuration;
    }// end method

    /**
     * 
     * @return DTO max duration
     */
    public int getDtoMaxDuration()
    {
        return this.dtoMaxDuration;
    }// end method

    /**
     * Set DTO max duration
     * 
     * @param dtoMaxDuration
     */
    public void setDtoMaxDuration(int dtoMaxDuration)
    {
        this.dtoMaxDuration = dtoMaxDuration;
    }// end method

    /**
     * 
     * @return sensormode restore time
     */
    public int getResTime()
    {
        return this.resTime;
    }// end method

    /**
     * set dto restore time
     * 
     * @param resTime
     */
    public void setResTime(int resTime)
    {
        this.resTime = resTime;
    }// end method



	@Override
	public String toString() {
		return "BeamBean [satName=" + satName + ", beamName=" + beamName + ", nearOffNadir=" + nearOffNadir
				+ ", farOffNadir=" + farOffNadir + ", sensorMode=" + sensorMode + ", sensorModeName=" + sensorModeName
				+ ", isEnabled=" + isEnabled + ", nearOffNadirBeam=" + nearOffNadirBeam + ", farOffNadirBeam="
				+ farOffNadirBeam + ", swDim1=" + swDim1 + ", swDim2=" + swDim2 + ", isSpotLight=" + isSpotLight
				+ ", idBeam=" + idBeam + ", dtoMinDuration=" + dtoMinDuration + ", dtoMaxDuration=" + dtoMaxDuration
				+ ", resTime=" + resTime + ", dtoDurationSquared=" + dtoDurationSquared + "]";
	}

	public int getDtoDurationSquared() {
		return dtoDurationSquared;
	}

	public void setDtoDurationSquared(int dtoDurationSquared) {
		this.dtoDurationSquared = dtoDurationSquared;
	}

}// end class
