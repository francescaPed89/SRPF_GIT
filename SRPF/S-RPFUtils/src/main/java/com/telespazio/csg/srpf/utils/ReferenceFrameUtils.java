/**
*
* MODULE FILE NAME:	ReferenceFrameUtils.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Perform Operation on Date Time
*
* PURPOSE:			Perform Operation on Date Time
*
* CREATION DATE:	18-01-2016
*
* AUTHORS:			Amedeo Bancone
*
* DESIGN ISSUE:		1.1
*
* INTERFACES:
*
* SUBORDINATES:
*
* MODIFICATION HISTORY:
*
*
* --------------------------+------------+----------------+-------------------------------
* 	 18-05-2016  | Amedeo Bancone  |1.1  | method teta2DRotation added
* 										   method teta2DInverseRotation
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.utils;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * This class holds some utility to manage Reference Frame
 * 
 * @author Amedeo Bancone
 * 
 * @version 1.1.0 method teta2DRotation added method teta2DInverseRotation
 *
 */
public class ReferenceFrameUtils

{

    // ellipsoid semiax
    public static final double wgs84_a = 6378137.0;
    // ellipsoid semiax
    public static final double wgs84_b = 6356752.314245;
    // eccenyt
    private static final double d = wgs84_b / wgs84_a;
    // public static final double flatness = 1-d;
    public static final double wgs84_e2 = 0.0066943799901975848; // (1-d*d); //
    public static final double wgs84_a2 = wgs84_a * wgs84_a; // to speed things
                                                             // up a bit
    // to speed things up a bit
    public static final double wgs84_b2 = wgs84_b * wgs84_b;

    // to speed things up a bit
    public static double pi4 = 0.25 * Math.PI;
    // to speed things up a bit
    public static double doublesemiax = 2 * wgs84_b;

    /**
     * 
     * @param llhRefPos
     *            deg in degree
     * @param _ecefpos
     */
    public static double[] ecef2enuLLHref(final double[] llhRefPos, final double[] _ecefpos)

    {
        return ecef2enu(llh2ecef(llhRefPos, true), _ecefpos);
    }// end method

    /**
     * return enu coordinate given ecef pos and a reference position
     * 
     * @param _refpos
     *            ECEF, reference position
     * @param _ecefpos
     *            ECEF, position
     * @return enu coordinate
     */
    public static double[] ecef2enu(final double[] _refpos, final double[] _ecefpos)

    {
        // return vectot
        double[] enu = new double[3];

        // ref pos x
        double xr = _refpos[0];
        // refpos y
        double yr = _refpos[1];
        // ref pos z
        double zr = _refpos[2];

        // ecef
        double x = _ecefpos[0];
        double y = _ecefpos[1];
        double z = _ecefpos[2];

        double phiP = Math.atan2(zr, Math.sqrt((xr * xr) + (yr * yr)));

        // double phiP = Math.atan2(Math.sqrt(xr*xr+yr*yr),zr);

        double lambda = Math.atan2(yr, xr);

        double xmxr = x - xr;
        double ymyr = y - yr;
        double zmzr = z - zr;

        double cosLambd = Math.cos(lambda);
        double sinLambd = Math.sin(lambda);

        double cosPhiP = Math.cos(phiP);
        double sinPhiP = Math.sin(phiP);

        // east
        enu[0] = (-sinLambd * xmxr) + (cosLambd * ymyr);
        // north
        enu[1] = ((-sinPhiP * cosLambd * xmxr) - (sinPhiP * sinLambd * ymyr)) + (cosPhiP * zmzr);
        // up
        enu[2] = (cosPhiP * cosLambd * xmxr) + (cosPhiP * sinLambd * ymyr) + (sinPhiP * zmzr);

        // return value
        return enu;
    } // end ecef2enu

    /**
     * Transorm from enu to ecef
     * 
     * @param _refpos
     *            reference point in ecef
     * @param _ecefpos
     * @return ecef coordinate
     */
    public static double[] enu2ecef(final double[] _refpos, final double[] enu)

    {
        // ret vector
        double[] ecefpos = new double[3];

        double xr = _refpos[0];
        double yr = _refpos[1];
        double zr = _refpos[2];

        // evaluating transformation angle
        double phiP = Math.atan2(zr, Math.sqrt((xr * xr) + (yr * yr)));

        // double phiP = Math.atan2( Math.sqrt(xr*xr+yr*yr),zr);

        // evaluating transformation angle
        double lambda = Math.atan2(yr, xr);

        // evaluating transformation matrix element
        double cosLambd = Math.cos(lambda);
        double sinLambd = Math.sin(lambda);

        double cosPhiP = Math.cos(phiP);
        double sinPhiP = Math.sin(phiP);

        // Applying transformation
        // x
        ecefpos[0] = ((-sinLambd * enu[0]) - (cosLambd * sinPhiP * enu[1])) + (cosLambd * cosPhiP * enu[2]) + xr;
        // y
        ecefpos[1] = ((cosLambd * enu[0]) - (sinLambd * sinPhiP * enu[1])) + (sinLambd * cosPhiP * enu[2]) + yr;
        // z
        ecefpos[2] = (cosPhiP * enu[1]) + (sinPhiP * enu[2]) + zr;

        // returning
        return ecefpos;
    } // end ecef2enu

    /**
     * return ecef position
     * 
     * @param llh
     * @param degree
     *            true if llh are in degree
     * @return ecef position
     */
    public static double[] llh2ecef(double[] llh, boolean degree)

    {
        // ret vecor
        double[] ecef = new double[3];

        double latitude = llh[0];
        double longitude = llh[1];

        // if deg true
        if (degree)
        {
            latitude = Math.toRadians(latitude); // to rad
            longitude = Math.toRadians(longitude); // to rad
        } // end if

        double sinLat = Math.sin(latitude);
        double cosLat = Math.cos(latitude);
        double sinLon = Math.sin(longitude);
        double cosLon = Math.cos(longitude);

        double v = wgs84_a / Math.sqrt(1 - (wgs84_e2 * (sinLat * sinLat)));
        // x
        ecef[0] = (v + llh[2]) * cosLat * cosLon;
        // y
        ecef[1] = (v + llh[2]) * cosLat * sinLon;
        // z
        ecef[2] = ((v * (1 - wgs84_e2)) + llh[2]) * sinLat;

        // returning
        return ecef;
    }// end method

    /**
     * Tranform an ecef position in LLH
     * 
     * @param ecefPosn
     * @param degree
     *            true if degree are required
     * @return llh
     */
    public static double[] ecef2llh(double[] ecefPosn, boolean degree)

    {
        return ecef2llh(new Vector3D(ecefPosn), degree);
    }// end method

    /**
     * Tranform an ecef position in LLH
     * 
     * @param ecefPosn
     * @param degree
     *            true if degree are required
     * @return llg
     */
    public static double[] ecef2llh(Vector3D ecefPosn, boolean degree)

    {

        double ep = Math.sqrt((wgs84_a2 - wgs84_b2) / wgs84_b2);
        // getting ecef
        double x = ecefPosn.getX();
        double y = ecefPosn.getY();
        double z = ecefPosn.getZ();

        // module of vector x,y
        double p = Math.sqrt((x * x) + (y * y));

        double th = Math.atan2(wgs84_a * z, wgs84_b * p);
        // longitude
        double longitude = Math.atan2(y, x);
        // latitude
        double lat = Math.atan2(z + (ep * ep * wgs84_b * Math.sin(th) * Math.sin(th) * Math.sin(th)), p - (wgs84_e2 * wgs84_a * Math.cos(th) * Math.cos(th) * Math.cos(th)));
        double N = wgs84_a / Math.sqrt(1 - (wgs84_e2 * Math.sin(lat) * Math.sin(lat)));
        double alt = (p / Math.cos(lat)) - N;

        // in case of degree
        if (degree)
        {
            longitude = Math.toDegrees(longitude); // toi deg
            lat = Math.toDegrees(lat); // to deg
        } // end if

        // returning vector
        double[] retval =
        { lat, longitude, alt };
        // return
        return retval;
    }// end method

    /**
     * Get sat ref frame
     * 
     * @param satPos
     * @param satVel
     */
    public static Vector3D[] getSatelliteReferenceFrame(Vector3D satPos, Vector3D satVel)

    {

        double[] llh = ecef2llh(satPos, false);

        double lat = llh[0];
        double longitude = llh[1];
        double altitude = llh[2];

        Vector3D e3 = new Vector3D(-Math.cos(lat) * Math.cos(longitude), -Math.cos(lat) * Math.sin(longitude), -Math.sin(lat));
        Vector3D v2 = e3.crossProduct(satVel);
        Vector3D e2 = v2.normalize();
        Vector3D e1 = e2.crossProduct(e3);
        Vector3D[] result =
        { e1, e2, e3 };

        return result;

    }// end method

    /**
     * Project a point to stereo plan
     * 
     * @param lat,
     *            latitude in deg
     * @param longitude,
     *            longitude in deg
     * @return position in stereo plan
     */
    public static double[] fromLatLongToStereo(double lat, double longitude)
    {

        // returning vector
        double[] retval = new double[2];

        // to rad
        double latRad = Math.toRadians(lat);
        // to rad
        double longRad = Math.toRadians(longitude);

        double tang = Math.tan(pi4 - (0.5 * latRad));

        if (lat < 0)
        {
            tang = 1 / tang;
        }

        // x
        retval[0] = doublesemiax * tang * Math.sin(longRad);
        // y
        retval[1] = doublesemiax * tang * Math.cos(longRad);

        // returning
        return retval;

    }// end method

    /**
     * Trasfrom a position in stereographic position to lat long
     * 
     * @param x
     * @param y
     * @param isSud
     *            to be set to true in case of south pole
     * @return lat lon
     */
    public static double[] fromStereoToLatLon(double x, double y, boolean isSud)
    {
        // ret vector
        double[] retval = new double[2];

        double longRad = Math.atan2(x, y);
        double latRad;
        // sine
        double sinLon = Math.sin(longRad);
        // cosine
        double cosLon = Math.cos(longRad);

        // 90 deg
        if (sinLon == 0)
        {
            latRad = 2 * (pi4 - Math.atan2(y, cosLon * doublesemiax));
        } // end if
        else
        {
            latRad = 2 * (pi4 - Math.atan2(x, sinLon * doublesemiax));
        } // end if

        // latitude
        retval[0] = Math.toDegrees(latRad);
        if (retval[0] > 90)
        {
            retval[0] = retval[0] - 360;

        } // end if
          // south pole
        if (isSud)
        {
            retval[0] = -retval[0];
        }
        retval[1] = Math.toDegrees(longRad);
        // returning
        return retval;
    }// end method

    /**
     * Return the trasformed of the point p according a rotation of the plane of
     * an angle teta around the origin
     * 
     * @param p
     *            point
     * @param teta
     * @return trasfromed point
     */
    public static double[] teta2DRotation(double[] p, double teta)
    {
        // evaluating tranformation matrix element
        double cosTeta = Math.cos(teta);
        double sinTeta = Math.sin(teta);

        // returning vector
        double[] retval = new double[2];

        // Applying tranformation
        retval[0] = (p[0] * cosTeta) + (p[1] * sinTeta);

        retval[1] = (-p[0] * sinTeta) + (p[1] * cosTeta);
        // returning
        return retval;
    }// end method

    /**
     * Inverse of teta2DRotation
     * 
     * @param p
     *            point
     * @param teta
     * @return trasformed point
     */
    public static double[] teta2DInverseRotation(double[] p, double teta)
    {
        // evaluating tranformation matrix element
        double cosTeta = Math.cos(teta);
        double sinTeta = Math.sin(teta);
        // returning vector
        double[] retval = new double[2];
        // Applying tranformation
        retval[0] = (p[0] * cosTeta) - (p[1] * sinTeta);

        retval[1] = (p[0] * sinTeta) + (p[1] * cosTeta);
        // returning
        return retval;
    }// end method

}// end class
