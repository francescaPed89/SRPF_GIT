/**
*
* MODULE FILE NAME:	OrbitalData.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:		     This class is the basic brick for the in memory management of orbital data.
*					 It basically holds a list containing the EpochsBean of generic type
*                    for a generic satellite
*
* PURPOSE:			Manage orbital data
*
* CREATION DATE:	09-07-2016
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

package com.telespazio.csg.srpf.dataManager.inMemoryOrbitalData;

import java.util.ArrayList;

import com.telespazio.csg.srpf.dataManager.bean.EpochBean;

/**
 * This class is the basic brick for the in memory management of orbital data.
 * It basically holds a list containing the EpochsBean of generic type for a
 * generic satellite
 * 
 * @author Amedeo Bancone
 * @version 1.0
 *
 *
 */
public class OrbitalData

{

    // name of the file whose data belonging
    private String obdataFileName = "";

    // List of EpochBean
    private ArrayList<EpochBean> obdata = new ArrayList<>();

    /**
     * Sampling rate seeconds
     */
    private long samplingRate = 10;

    /**
     * Get dsampling rate
     * 
     * @return sampling rate
     */
    public long getSamplingRate()
    {
        return this.samplingRate;
    }// end method

    public void setSamplingRate(long _samplingRate)
    {

        this.samplingRate = _samplingRate;
    }// end method

    /**
     * default constructor
     */
    OrbitalData()
    {

    }// end method

    /**
     * return the obdata list
     * 
     * @return
     */
    ArrayList<EpochBean> getObData()
    {
        return this.obdata;
    }// end method

    /**
     * Set the obdata list
     * 
     * @param obdata
     */
    void setObData(ArrayList<EpochBean> _obdata)
    {
        this.obdata = _obdata;
        // I'iìm not sure that the above work fine in multicore environment
        /*
         * this.obdata.clear(); for(EpochBean e: _obdata) this.obdata.add(e);
         */

    }// end method

    /**
     * return the current file whose data belongs
     * 
     * @return the current file whose data belongs
     */
    String getObdataFileName()

    {
        return this.obdataFileName;
    }// end method

    /**
     * 
     * @param obdataFileName
     *            return the current file whosa data belongs
     * 
     */
    void setObdataFileName(String obdataFileName)
    {
        this.obdataFileName = obdataFileName;
    }// end method

}// end class
