/**
*
* MODULE FILE NAME:	SatelliteEphemerids.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:		    This class holds the whole orbital data set for a Satellite
*
*
* PURPOSE:			Manage orbital data
*
* CREATION DATE:	10-07-2016
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.telespazio.csg.srpf.dataManager.bean.EpochBean;
import com.telespazio.csg.srpf.dataManager.util.DataManagerConstants;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.DateUtils;
import com.telespazio.csg.srpf.utils.PropertiesReader;
import com.telespazio.csg.srpf.utils.ReferenceFrameUtils;

/**
 * This class holds the whole orbital data set for a Satellite
 * 
 * @author Amedeo Bancone
 * @version 1.0
 *
 */
public class SatelliteEphemerids

{
    static final Logger logger = LogManager.getLogger(SatelliteEphemerids.class.getName());

    // private final int fileNameListSize=4;

    TraceManager tm = new TraceManager();

    // ODSTP orbit data
    private OrbitalData odstp = new OrbitalData();

    // ODMTP orbit data
    private OrbitalData odmtp = new OrbitalData();

    // ODNOM orbit data
    private OrbitalData odnom = new OrbitalData();

    // ODREF obdata
    private OrbitalData odref = new OrbitalData();

    // Expectected number of sample
    // Due to an incongruence between data in CSK ODREF and CSG ODREF
    // Solite minchiate di TPZ
    private final static int ExpectedNumebrOfSampleInODREF = 23040;

    /*
     * public void printSampleRate() {
     * //System.out.println(""+odstp.getObdataFileName() + " rate: " +
     * odstp.getSamplingRate()); //System.out.println(""+odmtp.getObdataFileName()
     * + " rate: " + odmtp.getSamplingRate());
     * //System.out.println(""+odnom.getObdataFileName() + " rate: " +
     * odnom.getSamplingRate()); //System.out.println(""+odref.getObdataFileName()
     * + " rate: " + odref.getSamplingRate()); }
     */

    /**
     * Sample rate read from current file
     */
    long currentSampleRate = 0;
    /**
     * Sat name
     */
    private String satName = "";
    /**
     * sat id
     */
    private int satelliteId = -1;

    /**
     * Constructor
     * 
     * @param satId
     * @param satName
     */
    SatelliteEphemerids(int satId, String satName)
    {
        // Initializing
        this.satelliteId = satId;
        this.satName = satName;
    }// end method

    /**
     * Refresh orbital data. It extract the filename from DB and if the name is
     * changed it creates new lists and sutsitues the old ones
     * 
     * @param fileNames
     */
    void refreshAllData(ArrayList<String> fileNames)
    {
        try
        {
            // epoch bean
            ArrayList<EpochBean> tempList;

            String path = "";
            
            for(int i=0;i<fileNames.size();i++)
            {
                logger.debug("ORBDATA : LOADING : "+fileNames.get(i));
            }
//
//            logger.debug("ORBDATA ODSTP fileName : "+this.odstp.getObdataFileName());
//            logger.debug("ORBDATA ODMTP fileName : "+this.odmtp.getObdataFileName());
//            logger.debug("ORBDATA ODREF fileName : "+this.odref.getObdataFileName());
//            logger.debug("ORBDATA ODNOM fileName : "+this.odnom.getObdataFileName());


            // ODSTP
            try
            {
                path = fileNames.get(0);
                if (!path.equals(this.odstp.getObdataFileName()))
                {
                    //logger.debug("processing AS TYPE_ODSTP "+path);

                    tempList = buidDataSet(path, DataManagerConstants.TYPE_ODSTP, this.satelliteId);
                    this.odstp.setObData(tempList);
                    this.odstp.setObdataFileName(path);
                    if (this.currentSampleRate < 0)
                    {
                        this.currentSampleRate = DataManagerConstants.OdstpSamplingRate;// default
                                                                                        // sample
                                                                                        // rate
                    } // end if
                    this.odstp.setSamplingRate(this.currentSampleRate);
                    this.tm.log("Loaded ODSTP from " + path + " for satellite " + this.satName);
                } // end try

            } // end try
            catch (IOException e)
            {
                // just log
                this.tm.critical(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_ERROR, "Unable to retrieve OBDATA file names " + e.getMessage());
            } // end catch

            // ODMTP
            try
            {
                path = fileNames.get(1);
                if (!path.equals(this.odmtp.getObdataFileName()))
                {
                   // logger.debug("processing AS TYPE_ODMTP "+path);
                    tempList = buidDataSet(path, DataManagerConstants.TYPE_ODMTP, this.satelliteId);
                    this.odmtp.setObData(tempList);
                    this.odmtp.setObdataFileName(path);
                    // reevaluated in build
                    if (this.currentSampleRate < 0)
                    {
                        this.currentSampleRate = DataManagerConstants.OdmtpSamplingRate;// default
                    } // end if
                    this.odmtp.setSamplingRate(this.currentSampleRate);
                    this.tm.log("Loaded ODMTP from " + path + " for satellite " + this.satName);
                } // end if

            } // end try
            catch (IOException e)
            {
                // just log
                this.tm.critical(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_ERROR, "Unable to retrieve OBDATA file names " + e.getMessage());
            } // catch

            // ODNOM
            try
            {
                path = fileNames.get(2);
                if (!path.equals(this.odnom.getObdataFileName()))
                {
                    //logger.debug("processing AS TYPE_ODNOM "+path);
                    tempList = buidDataSet(path, DataManagerConstants.TYPE_ODNOM, this.satelliteId);
                    this.odnom.setObData(tempList);
                    this.odnom.setObdataFileName(path);
                    if (this.currentSampleRate < 0)
                    {
                        this.currentSampleRate = DataManagerConstants.OdnomSamplingRate;// default
                    } // end if
                    this.odnom.setSamplingRate(this.currentSampleRate);
                    this.tm.log("Loaded ODNOM from " + path + " for satellite " + this.satName);
                } // end if
            } // end try
            catch (IOException e)
            {
                // just log
                this.tm.critical(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_ERROR, "Unable to retrieve OBDATA file names " + e.getMessage());
            } // end catch

            // ODREF
            try
            {
                path = fileNames.get(3);
                if (!path.equals(this.odref.getObdataFileName()))
                {
                    //logger.debug("processing AS TYPE_ODREF "+path);

                    tempList = buidDataSet(path, DataManagerConstants.TYPE_ODREF, this.satelliteId);
                    this.odref.setObData(tempList);
                    this.odref.setObdataFileName(path);
                    if (this.currentSampleRate < 0)
                    {
                        this.currentSampleRate = DataManagerConstants.OdrefSamplingRate;// default
                    } // end if
                    this.odref.setSamplingRate(this.currentSampleRate);
                    this.tm.log("Loaded ODREF from " + path + " for satellite " + this.satName);
                } // end if
            } // end try
            catch (IOException e)
            {
                // juats log
                this.tm.critical(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_ERROR, "Unable to retrieve OBDATA file names " + e.getMessage());
            } // end catch

        } // end try
        catch (Exception e)
        {
            // just log
            this.tm.critical(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_ERROR, "Unable to retrieve OBDATA file names " + e.getMessage());
            return;
        } // end catch

    }// end method

    /**
     * Build the data starting from file
     * 
     * @param obdataFilename
     * @param satId
     * @throws IOException
     */
    private ArrayList<EpochBean> buidDataSet(String obdataFilename, int dataType, int satId) throws IOException
    {
        // input file
        InputStream fis = new FileInputStream(obdataFilename);

        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        // reader
        BufferedReader br = new BufferedReader(isr);

        // current line
        String line;

        // Reading file heading

        line = br.readLine();

        this.currentSampleRate = getSamplingRate(line);
        // Reading info satellite

        // Reading Epoch
        double julianDate;
        double X;
        double Y;
        double Z;
        double VX;
        double VY;
        double VZ;
        long orbitId;

        // This value holds for CSK
        // double toJulianOffset = CSKtoJulianOFFSET;
        // double toJulianOffset = 0;

        // Building Epoch list

        ArrayList<EpochBean> epochList = new ArrayList<>();

        // looping on file line
        while ((line = br.readLine()) != null)

        {
            // split line in tokens
            StringTokenizer epochTokens = new StringTokenizer(line);
            try

            {
                // MANCA ID SATELLITE
                // getting data
                epochTokens.nextToken();
                julianDate = Double.valueOf(epochTokens.nextToken());
                X = Double.valueOf(epochTokens.nextToken()) * 1000;
                Y = Double.valueOf(epochTokens.nextToken()) * 1000;
                Z = Double.valueOf(epochTokens.nextToken()) * 1000;
                VX = Double.valueOf(epochTokens.nextToken()) * 1000;
                VY = Double.valueOf(epochTokens.nextToken()) * 1000;
                VZ = Double.valueOf(epochTokens.nextToken()) * 1000;

                // Jumping to next useful token
                epochTokens.nextToken();
                epochTokens.nextToken();
                epochTokens.nextToken();
                epochTokens.nextToken();
                epochTokens.nextToken();
                epochTokens.nextToken();

                orbitId = Integer.valueOf(epochTokens.nextToken());
                // building posyition
                Vector3D pos = new Vector3D(X, Y, Z);
                // building velocity
                Vector3D vel = new Vector3D(VX, VY, VZ);

                Vector3D[] EUnitVector = ReferenceFrameUtils.getSatelliteReferenceFrame(pos, vel);
                // evaluating ancillary
                EpochBean epochbean = new EpochBean();
                epochbean.setoE1xE1yE1z(EUnitVector[0]);
                epochbean.setoE2xE2yE2z(EUnitVector[1]);
                epochbean.setoE3xE3yE3z(EUnitVector[2]);
                epochbean.setoXyz(pos);
                epochbean.setoVxVyVz(vel);

                epochbean.setIdOrbit(orbitId);
                epochbean.setEpoch(julianDate);
                // setting data type
                epochbean.setDataType(dataType);
                // setting sat id
                epochbean.setIdSatellite(satId);
                // add epoch to list
                epochList.add(epochbean);

            } // end try

            catch (NoSuchElementException e)

            {
                // close
                // rethrow
                br.close();
                throw new IOException("File " + obdataFilename + "malformed");
            } // end catch

        } // ENdD while
        br.close();

        // check on inconsinstency of CSG ODREF
        // we remove one sample line if necessary
        int listSize = epochList.size();
        if ((dataType == DataManagerConstants.TYPE_ODREF) && (listSize == (ExpectedNumebrOfSampleInODREF + 1)))
        {
            epochList.remove(listSize - 1);
        } // end if
          // returning
        return epochList;

    }// end method

    /**
     * Return the requested timeline for the satllite
     * 
     * @param startTime
     * @param stopTime
     * @return list of epochs
     */
    ArrayList<EpochBean> selectEpochData(double startTime, double stopTime)
    {

        ArrayList<EpochBean> globalList = new ArrayList<>();

        ArrayList<EpochBean> odlist = this.odstp.getObData();

        ArrayList<EpochBean> temppList = new ArrayList<>();

        EpochBean currentEpochBean;
        double currentEpoch;

        // searching in odstp
        for (int i = 0; i < odlist.size(); i++)
        {
            currentEpochBean = odlist.get(i);
            currentEpoch = currentEpochBean.getEpoch();

            if ((currentEpoch > startTime) && (currentEpoch <= stopTime))
            {
                temppList.add(currentEpochBean);
            } // end if

        } // end for

        long periodTollernceTypeOdstp;

        if (temppList.size() > 0)
        {
            double firstEpochOdstp = temppList.get(0).getEpoch();
            double diffOdstp = firstEpochOdstp - startTime;
            String sTolleranceOdstp = PropertiesReader.getInstance().getProperty("PERIOD_TOLLERANCE_TYPE_ODSTP");
            if (sTolleranceOdstp != null)
            {
                periodTollernceTypeOdstp = Long.parseLong(PropertiesReader.getInstance().getProperty("PERIOD_TOLLERANCE_TYPE_ODSTP").trim());
            } // end if
            else
            {
                periodTollernceTypeOdstp = DataManagerConstants.PERIOD_TOLLERANCE_TYPE_ODSTP;
            } // end else
            boolean validOdstp = diffOdstp < DateUtils.secondsToJulian(periodTollernceTypeOdstp);
            double maxEpochOdstp = temppList.get(temppList.size() - 1).getEpoch();
            if (validOdstp)
            {
                globalList.addAll(temppList);
                // ManagerLogger.logDebug(this,
                // "------epochListOdstp-------- " + epochListOdstp.size());
                this.tm.debug("epochListOdstp: " + temppList.size());
                startTime = maxEpochOdstp;
                if ((stopTime - maxEpochOdstp) < DateUtils.secondsToJulian(DataManagerConstants.MAX_TOLLERANCE_TYPE_ODSTP))
                {

                    return globalList;

                } // end if

            } // end if
        } // end if

        // searching in odmtp

        // Raccording for transition between odstp and odmtp
        startTime = startTime + DateUtils.secondsToJulian(DataManagerConstants.OdmtpSamplingRate);
        // startTime =
        // startTime+DateUtils.secondsToJulian(odmtp.getSamplingRate());
        if (startTime >= stopTime)
        {
            return globalList;
        }

        temppList.clear();
        odlist = this.odmtp.getObData();
        for (int i = 0; i < odlist.size(); i++)
        {
            currentEpochBean = odlist.get(i);
            currentEpoch = currentEpochBean.getEpoch();

            if ((currentEpoch > startTime) && (currentEpoch <= stopTime))
            {
                temppList.add(currentEpochBean);
            } // end if

        } // end for

        long periodTollernceTypeOdmtp;

        if (temppList.size() > 0)
        {
            double firstEpochOdmtp = temppList.get(0).getEpoch();
            double diffOdmtp = firstEpochOdmtp - startTime;
            String sTolleranceOdmpt = PropertiesReader.getInstance().getProperty("PERIOD_TOLLERANCE_TYPE_ODMTP");
            if (sTolleranceOdmpt != null)
            {
                periodTollernceTypeOdmtp = Long.parseLong(PropertiesReader.getInstance().getProperty("PERIOD_TOLLERANCE_TYPE_ODMTP").trim());
            } // end if
            else
            {
                periodTollernceTypeOdmtp = DataManagerConstants.PERIOD_TOLLERANCE_TYPE_ODMTP;
            } // end else ODMTP
            boolean validOdmtp = diffOdmtp < DateUtils.secondsToJulian(periodTollernceTypeOdmtp);
            double maxEpochOdmtp = temppList.get(temppList.size() - 1).getEpoch();
            if (validOdmtp)
            {
                globalList.addAll(temppList);
                // ManagerLogger.logDebug(this,
                // "------epochListOdmtp-------- " + epochListOdmtp.size());
                this.tm.debug("epochListOdmtp: " + temppList.size());
                startTime = maxEpochOdmtp;
                if ((stopTime - maxEpochOdmtp) < DateUtils.secondsToJulian(DataManagerConstants.MAX_TOLLERANCE_TYPE_ODMTP))
                {

                    return globalList;

                } // end if

            } // end if

        } // end if

        // Searching in ODNOM
        // Raccording for transition between odmtp and odmnom
        startTime = startTime + DateUtils.secondsToJulian(DataManagerConstants.OdnomSamplingRate);
        // startTime =
        // startTime+DateUtils.secondsToJulian(odnom.getSamplingRate());
        if (startTime >= stopTime)
        {
            return globalList;
        }

        long periodTollernceTypeOdnom;
        temppList.clear();
        odlist = this.odnom.getObData();

        for (int i = 0; i < odlist.size(); i++)
        {
            currentEpochBean = odlist.get(i);
            currentEpoch = currentEpochBean.getEpoch();

            if ((currentEpoch > startTime) && (currentEpoch <= stopTime))
            {
                temppList.add(currentEpochBean);
            } // end if

        } // end for

        if (temppList.size() > 0)
        {
            double firstEpochOdnom = temppList.get(0).getEpoch();
            double diffOdnom = firstEpochOdnom - startTime;
            String sTolleranceOdnom = PropertiesReader.getInstance().getProperty("PERIOD_TOLLERANCE_TYPE_ODNOM");
            if (sTolleranceOdnom != null)
            {
                periodTollernceTypeOdnom = Long.parseLong(PropertiesReader.getInstance().getProperty("PERIOD_TOLLERANCE_TYPE_ODNOM").trim());
            } // end if
            else
            {
                periodTollernceTypeOdnom = DataManagerConstants.PERIOD_TOLLERANCE_TYPE_ODNOM;
            } // end else ODNOM
            boolean validOdnom = diffOdnom < DateUtils.secondsToJulian(periodTollernceTypeOdnom);
            double maxEpochOdnom = temppList.get(temppList.size() - 1).getEpoch();
            if (validOdnom)
            {
                globalList.addAll(temppList);
                // ManagerLogger.logDebug(this,
                // "------temppList-------- " + temppList.size());
                this.tm.debug("epochListOdnom: " + temppList.size());
                startTime = maxEpochOdnom;
                if ((stopTime - maxEpochOdnom) < DateUtils.secondsToJulian(DataManagerConstants.MAX_TOLLERANCE_TYPE_ODNOM))
                {

                    return globalList;

                } // end if

            } // end if

        } // end if

        // //System.out.println("Serching in ODREF");

        // searching in odref

        // Raccording for transition between odmtp and odmnom
        startTime = startTime + DateUtils.secondsToJulian(DataManagerConstants.OdrefSamplingRate);
        // startTime =
        // startTime+DateUtils.secondsToJulian(odref.getSamplingRate());
        if (startTime >= stopTime)
        {
            return globalList;
        }

        temppList.clear();
        odlist = this.odref.getObData();

        if (odlist.size() == 0)
        {
            return globalList;
        } // end if

        double minEpochOdref = odlist.get(0).getEpoch();
        double difEpoch = (startTime - minEpochOdref) / DataManagerConstants.DAYS_REPEATED_EPOCHS;// 16;
        int nRepeatedEpochs = (int) difEpoch;

        boolean haverun = true;

        while (haverun)
        {
            // initialEpochAtt = initialEpoch - nRepeatedEpochs * 16;
            // finalEpochAtt = finalEpoch - nRepeatedEpochs * 16;

            for (int i = 0; i < odlist.size(); i++)
            {
                double tt = odlist.get(i).getEpoch();
                tt = tt + (DataManagerConstants.DAYS_REPEATED_EPOCHS * nRepeatedEpochs);

                if ((tt > startTime) && (tt <= stopTime))
                {

                    EpochBean epochBean = odlist.get(i);
                    // EpochBean cloned = (EpochBean)cloneObject((EpochBean)
                    // allEpochsListREF.get(i));
                    EpochBean cloned;
                    try
                    {
                        cloned = (EpochBean) epochBean.clone();
                        cloned.setEpoch(tt);
                        temppList.add(cloned);
                    }
                    catch (CloneNotSupportedException e)
                    {

                    } // end catch

                    // ((EpochBean) epochListREF.get(k)).setEpoch(tt );

                } // end if

                if (tt > stopTime)
                {
                    haverun = false;
                }

            } // end for
            /*
             * if (finalEpochAtt < maxEpochOdref) { return epochListREF; } //
             * end if else
             */

            nRepeatedEpochs++;
        } // end while

        // tm.debug("epochListOdref: " + temppList.size());

        globalList.addAll(temppList);

        // //System.out.println("RETURN");

        return globalList;
    }// end selectEpochData

    /**
     * Evaluate sampleRate form header
     * 
     * @param line
     * @return sample rate
     */
    private long getSamplingRate(String line)
    {
        long retval = -1;
        // Split string
        StringTokenizer tokens = new StringTokenizer(line);
        if (tokens.countTokens() >= 8)
        {
            tokens.nextToken();
            tokens.nextToken();
            tokens.nextToken();
            tokens.nextToken();
            tokens.nextToken();
            tokens.nextToken();
            tokens.nextToken();
            // jaumping to usefyl token
            String sampleAsString = tokens.nextToken();
            try
            {
                // convert form string
                retval = Long.valueOf(sampleAsString);
            } // end try
            catch (Exception e)
            {
                // log
                // return -1
                retval = -1;
                this.tm.warning(EventType.APPLICATION_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Wrong header in orbital data");
            } // end catch
        } // end if
        return retval;

    }// end getSamplingRate

}// end class
