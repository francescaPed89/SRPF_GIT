/**
*
* MODULE FILE NAME:	EpochBean.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Define a structure to model an epoch in DB
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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.telespazio.csg.srpf.utils.DateUtils;

/**
 * Define a structure to model an epoch in DB
 * 
 * @author Abed Alissa
 * @version 1.0
 *
 */
public class EpochBean implements Cloneable
{

    // satellite id
    private int idSatellite;

    // type of epoch
    private int dataType;

    // orbit id
    private long idOrbit;

    // position vector
    Vector3D oXyz;

    // velocity vector
    Vector3D oVxVyVz;

    // E1 vector
    Vector3D oE1xE1yE1z;

    // E2 vector
    Vector3D oE2xE2yE2z;

    // E3 Vector
    Vector3D oE3xE3yE3z;

    // Epoch in julian date time
    private double epoch;

    /**
     * @author Abed Alissa
     * @version 1.0
     * @date
     * @return data type
     */
    public int getDataType()
    {
        return this.dataType;
    }// end method

    /**
     * @author Abed Alissa
     * @version 1.0
     * @date
     * @return E1x vector component
     */
    public double getE1x()
    {
        return getoE1xE1yE1z().getX();
    }// end method

    /**
     * @author Abed Alissa
     * @version 1.0
     * @date
     * @return E1y vector component
     */
    public double getE1y()
    {
        return getoE1xE1yE1z().getY();
    }// end method

    /**
     * @author Abed Alissa
     * @version 1.0
     * @date
     * @return E1z vector component
     */
    public double getE1z()
    {
        return getoE1xE1yE1z().getZ();
    }// end method

    /**
     * @author Abed Alissa
     * @version 1.0
     * @date
     * @return E2x vector component
     */
    public double getE2x()
    {
        return getoE2xE2yE2z().getX();
    }// end method

    /**
     * @author Abed Alissa
     * @version 1.0
     * @date
     * @return E2y vector component
     */
    public double getE2y()
    {
        return getoE2xE2yE2z().getY();
    }// end method

    /**
     * @author Abed Alissa
     * @version 1.0
     * @date
     * @return E2z vector component
     */
    public double getE2z()
    {
        return getoE2xE2yE2z().getZ();
    }// end method

    /**
     * @author Abed Alissa
     * @version 1.0
     * @date
     * @return E3x vector component
     */
    public double getE3x()
    {
        return getoE3xE3yE3z().getX();
    }// end method

    /**
     * @author Abed Alissa
     * @version 1.0
     * @date
     * @return E3y vector component
     */
    public double getE3y()
    {
        return getoE3xE3yE3z().getY();
    }// end method

    /**
     * @author Abed Alissa
     * @version 1.0
     * @date
     * @return E3z vector component
     */
    public double getE3z()
    {
        return getoE3xE3yE3z().getZ();
    }// end method

    /**
     * @return epoch
     */
    public double getEpoch()
    {
        return this.epoch;
    }// end method

    /**
     * 
     * @return orbit id
     */
    public long getIdOrbit()
    {
        return this.idOrbit;
    }// end method

    /**
     * 
     * @return sat id
     */
    public int getIdSatellite()
    {
        return this.idSatellite;
    }// end method

    /**
     * @return E1 vector
     */
    public Vector3D getoE1xE1yE1z()
    {
        return this.oE1xE1yE1z;
    }// end method

    /**
     * @return E2 vector
     */
    public Vector3D getoE2xE2yE2z()
    {
        return this.oE2xE2yE2z;
    }// end method

    /**
     * @return E3 vector
     */
    public Vector3D getoE3xE3yE3z()
    {
        return this.oE3xE3yE3z;
    }// end method

    /**
     * @return velocity vector
     */
    public Vector3D getoVxVyVz()
    {
        return this.oVxVyVz;
    }// end method

    /**
     * @return position vector
     */
    public Vector3D getoXyz()
    {
        return this.oXyz;
    }// end method

    /**
     * 
     * @return Velocity x component
     */
    public double getVx()
    {
        return getoVxVyVz().getX();
    }

    /**
     * 
     * @return Velocity y component
     */
    public double getVy()
    {
        return getoVxVyVz().getY();
    }// end method

    /**
     * 
     * @return Velocity z component
     */
    public double getVz()
    {
        return getoVxVyVz().getZ();
    }// end method

    /**
     * 
     * @return Position x component
     */
    public double getX()
    {
        return getoXyz().getX();
    }// end method

    /**
     * 
     * @return Position y component
     */
    public double getY()
    {
        return getoXyz().getY();
    }// end method

    /**
     * 
     * @return Position z component
     */
    public double getZ()
    {
        return getoXyz().getZ();
    }// end method

    /**
     * Set data type
     * 
     * @param dataType
     */
    public void setDataType(int dataType)
    {
        this.dataType = dataType;
    }// end method

    /**
     * Set epoch as julian
     * 
     * @param epoch
     */
    public void setEpoch(double epoch)
    {
        this.epoch = epoch;
    }// end method

    /**
     * Set orbt id
     * 
     * @param idOrbit
     */
    public void setIdOrbit(long idOrbit)
    {
        this.idOrbit = idOrbit;
    }// end method

    /**
     * @param idSatellite
     */

    /**
     * Set satellite id
     * 
     * @param idSatellite
     */
    public void setIdSatellite(int idSatellite)
    {
        this.idSatellite = idSatellite;
    }// end method

    /**
     * Set E1 vector
     * 
     * @param oE1xE1yE1z
     */
    public void setoE1xE1yE1z(Vector3D oE1xE1yE1z)
    {
        this.oE1xE1yE1z = oE1xE1yE1z;
    }// end method

    /**
     * Set E2 vector
     * 
     * @param oE2xE2yE2z
     */
    public void setoE2xE2yE2z(Vector3D oE2xE2yE2z)
    {
        this.oE2xE2yE2z = oE2xE2yE2z;
    }// end method

    /**
     * Set E3 vector
     * 
     * @param oE3xE3yE3z
     */
    public void setoE3xE3yE3z(Vector3D oE3xE3yE3z)
    {
        this.oE3xE3yE3z = oE3xE3yE3z;
    }// end metho

    /**
     * Set Velocity vector
     * 
     * @param oVxVyVz
     */
    public void setoVxVyVz(Vector3D oVxVyVz)
    {
        this.oVxVyVz = oVxVyVz;
    }// end method

    /**
     * set position vector
     * 
     * @param oXyz
     */
    public void setoXyz(Vector3D oXyz)
    {
        this.oXyz = oXyz;
    }// end method

    /**
     * Clone the object
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    @Override
    public String toString()
    {
        return "EpochBean [idSatellite=" + idSatellite + ", dataType=" + dataType + ", idOrbit=" + idOrbit + ", oXyz=" + oXyz + ", oVxVyVz=" + oVxVyVz + ", oE1xE1yE1z=" + oE1xE1yE1z + ", oE2xE2yE2z=" + oE2xE2yE2z + ", oE3xE3yE3z=" + oE3xE3yE3z + ", epoch=" + DateUtils.fromCSKDateToDateTime(epoch) + "]";
    }

}// end class
