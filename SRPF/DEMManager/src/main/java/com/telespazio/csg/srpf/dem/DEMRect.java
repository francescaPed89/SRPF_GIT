/**
*
* MODULE FILE NAME:	DEMManager.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Class used to manage a single file of the  Data Elevation Model
*
* PURPOSE:			Manage DEM
*
* CREATION DATE:	18-12-2015
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
*             Date                |  Name      |   New ver.    | Description
* --------------------------+------------+----------------+-------------------------------
*06-04-2018 | Amedeo Bancone  |1.1 | synchronized access to dem file cause HDF5 library is not fully thread safe
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.dem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5ScalarDS;

/**
 * Class used to manage a single file of the Data Elevation Model
 *
 * @author Amedeo Bancone
 * @version 1.1
 *
 */
public class DEMRect

{
	static final Logger logger = LogManager.getLogger(DEMRect.class.getName());

    // HDF5 Data Set Name
    private final static String DATASETNAME = "/S01/SBI";

    // Synchronization object
    private final static Object syncObj = new Object();

    // HDF5 Attribute in data set file
    private final static String TopLeftCornerAttributeName = "Top Left Geodetic Coordinates";
    // HDF5 Attribute in data set file
    private final static String BottomRightCornerAttributeName = "Bottom Right Geodetic Coordinates";
    // HDF5 Attribute in data set file
    private final static String LineSpacingAttributeName = "Line Spacing";
    // HDF5 Attribute in data set file
    private final static String ColumnSpacingAttributeName = "Column Spacing";

    // Matrix dimension
    private long[] elevationMatrixDimension = new long[2];
    // dim1
    private int elevationMatrixDim0 = 0;
    // dim2
    private int elevationMatrixDim1 = 0;

    // file path
    private String hdf5FilePath;
    // HDF5 File obj
    private H5File hdf5File;
    // Lisnespace
    private double lineSpacing = 0;
    // clos space
    private double columnSpacing = 0;
    // topleft
    private double[] topLeftCorner = new double[2];
    // bottom right
    private double[] bottomRightCorner = new double[2];
    // matrix of elevatoion
    private int[][] elevationMatrix;

    // Internal hdf5 file id
    private int fileId = -1;
    // Internal hdf5 dataset id
    private int datasetId = -1;
    // Internal hdf5 dataspace id
    private int dataspaceId = -1;
    // true if correctly initialiazed
    private boolean isCorrectInit = false;

    // HDF5 dataset
    private H5ScalarDS dataSet;

    /**
     *
     * @return path of dataset file
     */
    public String getFilePath()
    {
        return this.hdf5FilePath;
    }// end method

    /**
     * Set the data set file path
     *
     * @param hdf5FilePath
     */
    public void setFilePath(String hdf5FilePath)
    {
        this.hdf5FilePath = hdf5FilePath;
    }// end method

    /**
     * Default constructor
     */
    public DEMRect()
    {
        this.isCorrectInit = false;
    }// end method

    /**
     *
     * @param hdf5FilePath,
     *            path of the h5 file holding data
     * @throws HDF5Exception
     * @throws Exception
     */
    public DEMRect(final String hdf5FilePath) throws HDF5Exception, Exception
    {
        // logger.debug("Opening file: " + hdf5FilePath);
        this.hdf5FilePath = hdf5FilePath;
        this.hdf5File = new H5File(hdf5FilePath, FileFormat.READ);

        /**
         * Cause the access to hdf5 file are not thread safe we have to
         * synchronize the opening and closing of hdf5 file
         */
        synchronized (syncObj)
        {
            initRect();
        } // end synchro block

        this.isCorrectInit = true;

    }// end method

    /**
     *
     * @throws HDF5Exception
     */
    public void clear() throws HDF5Exception
    {
        // logger.trace("Clearing dem rectancle " + hdf5FilePath);

        if (this.datasetId >= 0)
        {
            this.dataSet.clear(); // Non so se necessario
            this.dataSet.close(this.datasetId);
            this.datasetId = -1;
        } // endif

        if (this.dataspaceId >= 0)
        {
            H5.H5Sclose(this.dataspaceId);
            this.dataspaceId = -1;
        } // end if
        if (this.fileId > 0)
        {
            this.hdf5File.close();
            this.fileId = -1;
            // //System.out.println("===================================CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC========================================");
        }
    }// end method

    /**
     * Retrun elevation give lat and long
     *
     * @param latitude
     * @param longitude
     * @return the elevation corresponding to the given latitude and longitide
     * @throws ArrayIndexOutOfBoundsException
     *             if latitude and longitude are out of the bound of the simple
     *             DEM rectangle
     */
    public int getElevation(final double latitude, final double longitude) throws ArrayIndexOutOfBoundsException

    {
        // logger.trace("getting elevation for: " + latitude + " " + longitude);
//         logger.debug("TOP LEFT CORNER latitude "+this.topLeftCorner[0]);
//         logger.debug("TOP LEFT CORNER longitude "+this.topLeftCorner[1]);
//         logger.debug("this.lineSpacing "+this.lineSpacing);
//         logger.debug("this.columnSpacing "+this.columnSpacing);

        int elevation = 0;
        if (!this.isCorrectInit)
        {
            // logger.trace("returning null elevation");
            return elevation;
        } // end if

        double dlat = (this.topLeftCorner[0] - latitude) / this.lineSpacing;
//        logger.debug("dlat "+dlat);

        // Evaluate x inndecx
        int x = (int) Math.floor(dlat);
//        logger.debug("x "+x);

        double dlong = (longitude - this.topLeftCorner[1]) / this.columnSpacing;

        // Eavluate y index
        int y = (int) Math.floor(dlong);
        
//        logger.debug("dlong "+dlong);
//        logger.debug("y "+y);

        // getting elevation
        elevation = this.elevationMatrix[x][y];
//        logger.debug("elevation "+elevation);

        // logger.trace("i : " + x + " j: " + y + " found in file " +
        // hdf5FilePath + " Elevation: " + elevation);

        return elevation;
    }// end method

    /**
     * Load the hdf5 file in memory
     *
     * @throws HDF5Exception
     * @throws Exception
     */
    private void initRect() throws HDF5Exception, Exception
    {
        try
        {
            // open file
            this.fileId = this.hdf5File.open();
            // //System.out.println("====================================================
            // "+Thread.currentThread().getId()+"DEM MANAGER INIT RECT file id:
            // " + fileId);
            this.dataSet = (H5ScalarDS) this.hdf5File.get(DATASETNAME);
            this.datasetId = this.dataSet.open();
            // initilizing attribute
            initAttribute();

            // //System.out.println("====================================================
            // "+Thread.currentThread().getId()+"Attributi inizializzati");

            // logger.trace("Elevation Matrix Dimension: " + elevationMatrixDim0
            // + "x" + elevationMatrixDim1 );

            this.elevationMatrix = new int[this.elevationMatrixDim0][this.elevationMatrixDim1];

            if (this.datasetId >= 0)
            {
                // logger.trace("Getting H5 DataSpace");
                this.dataspaceId = H5.H5Dget_space(this.datasetId);
                // //System.out.println("====================================================
                // "+Thread.currentThread().getId()+"H5Dget_space");

            } // endif

            if (this.dataspaceId >= 0)
            {
                // logger.trace("Reserving memory");
                H5.H5Sget_simple_extent_dims(this.dataspaceId, this.elevationMatrixDimension, null);
                // //System.out.println("====================================================
                // "+Thread.currentThread().getId()+"H5Sget_simple_extent_dims");
            } // enif

            // reading data
            if (this.datasetId >= 0)
            {
                // //System.out.println("=======================================================================
                // "+ Thread.currentThread().getId()+" Reading data");
                H5.H5Dread(this.datasetId, HDF5Constants.H5T_NATIVE_INT, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, this.elevationMatrix);

            } // end if
              // //System.out.println("=======================================================================
              // "+ Thread.currentThread().getId()+"Dati letti");
        } // end try
        finally
        {
            this.clear();
            // //System.out.println("=======================================================================
            // "+ Thread.currentThread().getId()+" Ripulito");
        } // end finally

    }// end method

    /**
     * Read the HDF5 attributes form file
     *
     * @throws HDF5Exception
     */
    private void initAttribute() throws HDF5Exception
    {
        // logger.warn("Reading Attributes");
        java.util.List<Attribute> attributeList = this.dataSet.getMetadata();
        for (Attribute attr : attributeList)
        {
            if (attr.getName().equals(LineSpacingAttributeName))
            {

                this.lineSpacing = ((double[]) attr.getValue())[0];

            } // end if
            else if (attr.getName().equals(ColumnSpacingAttributeName))
            {
                this.columnSpacing = ((double[]) attr.getValue())[0];
            } // end else if
            else if (attr.getName().equals(TopLeftCornerAttributeName))
            {
                this.topLeftCorner[0] = ((double[]) attr.getValue())[0];
                this.topLeftCorner[1] = ((double[]) attr.getValue())[1];
                // topLeftCorner[2]=((double[])attr.getValue())[2];
            } // end else if
            else if (attr.getName().equals(BottomRightCornerAttributeName))
            {
                this.bottomRightCorner[0] = ((double[]) attr.getValue())[0];
                this.bottomRightCorner[1] = ((double[]) attr.getValue())[1];
                // bottomRightCorner[2]=((double[])attr.getValue())[2];
            } // end else if

        } // end for

        // logger.trace("RectDimension lenght: " + dataSet.getDims().length);

        this.elevationMatrixDimension[0] = this.dataSet.getDims()[0];
        this.elevationMatrixDimension[1] = this.dataSet.getDims()[1];

        this.elevationMatrixDim0 = (int) this.dataSet.getDims()[0];
        this.elevationMatrixDim1 = (int) this.dataSet.getDims()[1];

        // logger.warn("Attributed read");

    }// end method

    /**
     * Desctructor
     */
    @Override
    protected void finalize() throws Throwable
    {
        // clearing
        this.clear();
        // call superclass
        super.finalize();
    }// end method
} // end class DEMRect
