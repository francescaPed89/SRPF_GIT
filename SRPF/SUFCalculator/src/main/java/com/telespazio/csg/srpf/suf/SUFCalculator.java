
/**
*
* MODULE FILE NAME:	Checksum.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Constants used during SUF calculation
*
* PURPOSE:			Quota Evaluation
*
* CREATION DATE:	18-03-2016
*
* AUTHORS:			Girolamo Castaldo
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
*    18-02-2018 | Amedeo Bancone  | 2.0 | implements BIC for CSG and became Singleton
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.suf;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.DateUtils;
import com.telespazio.csg.srpf.utils.PropertiesReader;

/**
 * Calculates SUF as sum of component functions
 * 
 * @author Girolamo Castaldo
 * @version 2.0
 * 
 */
public class SUFCalculator
{
    // the parameters used for CSK
    /*
     * private String sensorMode; private double startTime; private double
     * stopTime; private String lookSide; private String beam;
     */
	static final Logger logger = LogManager.getLogger(SUFCalculator.class.getName());

    
	// Property for CSK
    private Properties QUMProperties;

    // Beam using additional maneuvre. USED for CSK
    private List<String> rollRequiredBeams = new ArrayList<>();
    // end parametres used for CSK

    // This map associate sensor mode to BIC parameters, used in CSG BIC
    // evaluation algo
    private Map<String, CSGBicParameters> sensormodeBicParametersMap = new TreeMap<String, CSGBicParameters>();


	// field
    private static String CSG_FIELD_DELIM = ":";
    // comment line
    private static String CSG_COMMENT_LINE = "#";

    // log
   // TraceManager tm = new TraceManager();

    /*
     * public SUFCalculator(String sensorMode, double startTime, double
     * stopTime, String lookSide, String beam) throws IOException {
     * this.sensorMode = sensorMode; this.startTime = startTime; this.stopTime =
     * stopTime; this.lookSide = lookSide; this.beam = beam;
     * 
     * initQUMPropertiers(); initRollRequiredBeams(); }
     */

    
    
    /**
     * Constructor
     * 
     * @author Amedeo Bancone
     */
    public SUFCalculator()
    {
        try
        {
           // this.tm.log("Initializing BIC Parameters ");
            // CSK initialization
            initQUMPropertiers();
            initRollRequiredBeams();
            // CSG initialization
            initializeCSGBICParameters();

        }
        catch (IOException e)
        {
            // do nothing
            // just log
           // this.tm.critical(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, e.getMessage());

        } // end catch

    }// end method

    /**
     * This method fill sensormodeBicParametersMap
     * 
     * @author Amedeo Bancone
     */
    private void initializeCSGBICParameters()
    {
        // getting file path
        String csgBicParametrsConfigurationFile = PropertiesReader.getInstance().getProperty(QUMConstants.CSG_QUM_CONFIG_FILE_PATH_PARAM);

       // this.tm.log("Initializing BIC Parameters for CSG");
        BufferedReader br = null;
        // reading configuration from file
        try
        {
            InputStream fis = new FileInputStream(csgBicParametrsConfigurationFile);
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null)
            {
                if (line.startsWith(CSG_COMMENT_LINE) || line.isEmpty())
                {
                    // reading comment
                    continue;
                } // end if
                StringTokenizer tokens = new StringTokenizer(line, CSG_FIELD_DELIM);

                try
                {

                    // getting tokens
                    String sensorMode = tokens.nextToken().trim();
                    
                   // this.tm.debug("Initializing BIC Parameters for " + sensorMode);

//                    double power = Double.parseDouble(tokens.nextToken().trim());//power
//
//                    double threE = Double.parseDouble(tokens.nextToken().trim()); //seconds
//                    double bicRefE = Double.parseDouble(tokens.nextToken().trim()); //bic
//
//                    double threDV = Double.parseDouble(tokens.nextToken().trim());//seconds
//                    double bicRefDV = Double.parseDouble(tokens.nextToken().trim());//bic
//
//                    double alpha = Double.parseDouble(tokens.nextToken().trim());
//                    double beta = Double.parseDouble(tokens.nextToken().trim());
//
//                    double leftExtraCost = Double.parseDouble(tokens.nextToken().trim());
                    
                	//#sensormode:POWER:BIC_THRE_E:BIC_THRE_D:BIC_REF_D:alpha:beta:left_EXTRA_COST

                    double power = Double.parseDouble(tokens.nextToken().trim());//power

                    double threE = Double.parseDouble(tokens.nextToken().trim()); //seconds
                    double threDV = Double.parseDouble(tokens.nextToken().trim());//seconds

                    double bicRefDV = Double.parseDouble(tokens.nextToken().trim());//bic

                    double alpha = Double.parseDouble(tokens.nextToken().trim());
                    double beta = Double.parseDouble(tokens.nextToken().trim());

                    double leftExtraCost = Double.parseDouble(tokens.nextToken().trim());
                    
                    // add to map
                    this.sensormodeBicParametersMap.put(sensorMode, new CSGBicParameters(power,threE, threDV,bicRefDV, alpha, beta,leftExtraCost));

                } // end try
                catch (Exception e)
                {
                    // do nothing
                    // just log
                	logger.error(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Malformed line in " + csgBicParametrsConfigurationFile + " " + e.getMessage());

                } // end catch

            } // END WHILE

        } // end try
        catch (Exception e)
        {
            // do nothing
            // just log
           // this.tm.critical(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Error in initializing BIC parameters for CSG " + e.getMessage());

        } // end catch
        finally
        {
            if (br != null)
            {
                try
                {
                    // close reader
                    br.close();
                } // end try
                catch (Exception e)
                {
                    // do nothing
                    // just log
                   // this.tm.warning(EventType.APPLICATION_EVENT, "Error closing file", e.getMessage());
                } // end catch
            } // end if
        } // end finally

    }// end initializeCSGBICParameters

	/*
     * public double calculateSUF() { double SUF = 0;
     * 
     * for (int i = 0; i < QUMConstants.SUF_PARAMETERS_NUM; i++) { SUF = SUF +
     * calculateParameter(i); }
     * 
     * return SUF; }
     */

    /**
     * Evaluate BIC, CSG Case
     * 
     * @author Amedeo Bancone
     * @param sensorMode
     * @param startTime
     * @param stopTime
     * @param beam
     * @return BIC
     * @throws Exception
     */
    public double calculateSUF(String sensorMode, double startTime, double stopTime, String beam, boolean isLeft) throws Exception
    {
       // this.tm.debug("Evaluating suf for CSG DTO");
        double retval = 0;

        int leftCoefficient = 0;
        if (isLeft)
        {
            leftCoefficient = 1;
        } // end if

        // get configuration for selected sensor mode
        CSGBicParameters bicParam = this.sensormodeBicParametersMap.get(sensorMode);
        
        // get configuration for stripmap it is used as reference
        CSGBicParameters bicRefParam = this.sensormodeBicParametersMap.get(QUMConstants.SENSOR_MODE_STRIPMAP);

        if (bicParam == null)
        {
            // do nothing
            // just throw
            throw new Exception("Unable to find BIC parameters for sensormode: " + sensorMode);
        } // end if

        if (bicRefParam == null)
        {
            // do nothing
            // just throw
            throw new Exception("Unable to find BIC parameters for sensormode: " + QUMConstants.SENSOR_MODE_STRIPMAP);
        } // end if

        // evaluate duration in seconds
        long durationAsLong = DateUtils.fromCSKDurationToMilliSeconds(stopTime - startTime);

        double duration = durationAsLong / 1000.0;

        double bicE =  ((bicParam.getPower() * duration)/bicRefParam.getPower())/bicRefParam.getBicThreE();
        if(duration<=bicParam.getBicThreE())
        {
        	bicE = ((bicParam.getPower() * bicParam.getBicThreE())/bicRefParam.getPower())/bicRefParam.getBicThreE();
        }
        
        double bicDV = ((bicParam.getBicRefD()*duration)/bicRefParam.getBicThreD()); 
        if(duration<=bicParam.getBicThreD())
        {
        	bicDV = (bicParam.getBicRefD()); 
        }

        double alpha = bicParam.getAlpha();
        double beta = bicParam.getBeta();
        retval = (alpha * bicE) + (beta * bicDV) + (leftCoefficient * bicParam.getLeftSideExtraCost());

        // returnin
        return retval;

    }// end method

    /**
     * Evaluate BIC, CSK Case
     * 
     * @param sensorMode
     * @param startTime
     * @param stopTime
     * @param lookSide
     * @param beam
     * @return BIC
     */
    public double calculateSUF(String sensorMode, double startTime, double stopTime, String lookSide, String beam)
    {
        /*
         * this.sensorMode = sensorMode; this.startTime = startTime;
         * this.stopTime = stopTime; this.lookSide = lookSide; this.beam = beam;
         */

       // this.tm.debug("Evaluating suf for CSK DTO");

        double SUF = 0;

        for (int i = 0; i < QUMConstants.SUF_PARAMETERS_NUM; i++)
        {
            SUF = SUF + calculateParameter(i, sensorMode, startTime, stopTime, lookSide, beam);
        } // end for
        
        logger.debug("overall contributes :"+SUF+ " for sensor mode "+sensorMode);
          // return
        return SUF;
    }// end method

    /**
     * inizialize QUM property
     * 
     * @throws IOException
     */
    private void initQUMPropertiers() throws IOException
    {
       // this.tm.log("Initializing BIC Parameters for CSK");
        String QUMPropsFilePath = PropertiesReader.getInstance().getProperty(QUMConstants.QUM_CONFIG_FILE_PATH_PARAM);
        this.QUMProperties = new Properties();
        InputStream input = null; // Input stream for config file
        try
        {
            if (QUMPropsFilePath != null)
            {
                input = new FileInputStream(QUMPropsFilePath);
                this.QUMProperties.load(input);
            } // end if
            else
            {
                // do nothing
                // just log
               // this.tm.critical(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + QUMConstants.QUM_CONFIG_FILE_PATH_PARAM + " in configuration");

            } // end else
        } // end try
        catch (IOException ioe)
        {
            // rethrow
            throw ioe;
        } // end catch
        finally
        {
            if (input != null)
            {
                try
                {
                    // closing file
                    input.close();
                } // end try
                catch (IOException ioe)
                {
                    // do nothing
                    // just log
                   // this.tm.warning(EventType.APPLICATION_EVENT, "Error closing ", ioe.getMessage());
                } // end catch
            } // end if
        } // end finally
    }// end method

    /**
     * inizialize the vector of reuired beams
     */
    private void initRollRequiredBeams()
    {
        this.rollRequiredBeams = new ArrayList<>();
        this.rollRequiredBeams.add(QUMConstants.BEAM_ES_30); // add beam
        this.rollRequiredBeams.add(QUMConstants.BEAM_ES_31); // add beam
        this.rollRequiredBeams.add(QUMConstants.BEAM_ES_32); // add beam
        this.rollRequiredBeams.add(QUMConstants.BEAM_ES_33); // add beam
        this.rollRequiredBeams.add(QUMConstants.BEAM_ES_34); // add beam
        this.rollRequiredBeams.add(QUMConstants.BEAM_ES_35); // add beam
        this.rollRequiredBeams.add(QUMConstants.BEAM_H4_0A); // add beam
    }// end method

    /**
     * Evaluate the value of a parameter
     * 
     * @param parameter
     * @param sensorMode
     * @param startTime
     * @param stopTime
     * @param lookSide
     * @param beam
     * @return parameter value
     */
    private double calculateParameter(int parameter, String sensorMode, double startTime, double stopTime, String lookSide, String beam)
    {
        double value;
        logger.debug("FOR SENSORMODE sensorMode : "+sensorMode);

        /**
         * Switch on a parameter SELECT the related metod
         */
        switch (parameter)
        {
            case QUMConstants.SUF_THERMAL_ENHANCEMENT: // param
                value = calculateThermalEnhancement(sensorMode);
               logger.debug("contributes for SUF_THERMAL_ENHANCEMENT : "+value);
                break;
            case QUMConstants.SUF_TRANSMITTING_POWER: // param
                value = calculateTransmittingPower(sensorMode);
                logger.debug("contributes for SUF_TRANSMITTING_POWER : "+value);

                break;
            case QUMConstants.SUF_DATA_TAKE_VOLUME: // param
                value = calculateDataTakeVolume(sensorMode);
                logger.debug("contributes for SUF_DATA_TAKE_VOLUME : "+value);

                break;
            case QUMConstants.SUF_DATA_TAKE_ACQUISITION_DURATION: // param
                value = calculateDataTakeAcquisitionDuration(sensorMode, startTime, stopTime);
                logger.debug("contributes for SUF_DATA_TAKE_ACQUISITION_DURATION : "+value);

                break;
            case QUMConstants.SUF_DOWNLINK_DURATION: // param
                value = calculateDownlinkDuration(sensorMode);
                logger.debug("contributes for SUF_DOWNLINK_DURATION : "+value);

                break;
            case QUMConstants.SUF_ON_BOARD_DATA_LATENCY:// param
                value = calculateOnBoardDataLatency(sensorMode);
                logger.debug("contributes for SUF_ON_BOARD_DATA_LATENCY : "+value);

                break;
            case QUMConstants.SUF_ACQUISITION_OPPORTUNITY:// param
                value = calculateAcquisitionOpportunity(); // do nathing
                logger.debug("contributes for SUF_ACQUISITION_OPPORTUNITY : "+value);

                break;
            case QUMConstants.SUF_SLEW_MANOEUVRE_DURATION:// param
                value = calculateSlewManoeuvreDuration(sensorMode, lookSide);
                logger.debug("contributes for SUF_SLEW_MANOEUVRE_DURATION : "+value);

                break;
            case QUMConstants.SUF_EXT_MANOEUVRE_DURATION:// param
                value = calculateExtManoeuvreDuration(sensorMode, beam);
                logger.debug("contributes for beam in _EXT_MANOEUVRE_DURATION : "+beam);

                logger.debug("contributes for SUF_EXT_MANOEUVRE_DURATION : "+value);

                break;
            case QUMConstants.SUF_GROUND_DATA_NETWORK_USAGE_DURATION:// param
                value = calculateGroundDataNetworkUsageDuration(sensorMode);
                logger.debug("contributes for SUF_GROUND_DATA_NETWORK_USAGE_DURATION : "+value);

                break;
            default:
                value = 0;
                break;
        }// end switch

        // returning
        return value;
    }// end method

    /**
     * evaluate thermal enhancement
     * 
     * @param sensorMode
     * @return thermal enhancement
     */
    private double calculateThermalEnhancement(String sensorMode)
    {
        double kFactor;
        double effectiveThermalEnhancement;
        double standardThermalEnhancement;
        // check on sensor mode
        if (QUMConstants.SENSOR_MODE_SP_ENHANCED.equals(sensorMode))
        {
            // sp_enh
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_THERMAL_ENHANCEMENT_NARROW_PARAM));
            effectiveThermalEnhancement = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.EFFECTIVE_THERMAL_ENHANCEMENT_NARROW_PARAM));
            standardThermalEnhancement = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.STANDARD_THERMAL_ENHANCEMENT_NARROW_PARAM));
        } // end if
        else
        {
            // other sensr mode
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_THERMAL_ENHANCEMENT_WIDE_PARAM));
            effectiveThermalEnhancement = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.EFFECTIVE_THERMAL_ENHANCEMENT_WIDE_PARAM));
            standardThermalEnhancement = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.STANDARD_THERMAL_ENHANCEMENT_WIDE_PARAM));
        } // end else
          // //System.out.println("kFactor: " + kFactor + ";
          // effectiveThermalEnhancement: " + effectiveThermalEnhancement + ";
          // standardThermalEnhancement: " + standardThermalEnhancement + ";
          // result: " + kFactor * (effectiveThermalEnhancement /
          // standardThermalEnhancement));
          // returning
        return kFactor * (effectiveThermalEnhancement / standardThermalEnhancement);
    }// end method

    /**
     * evaluate transmitting power
     * 
     * @param sensorMode
     * @return transmitting power
     */
    private double calculateTransmittingPower(String sensorMode)
    {
        // factors
        double kFactor;
        double effectiveEnergyIssued;
        double standardEnergyIssued;
        double effectiveCalibrationPower;
        double standardCalibrationPower;
        // check on sensor mode
        // read values form prop

        if (QUMConstants.SENSOR_MODE_SP_ENHANCED.equals(sensorMode))
        {
            // SP_EN
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_TRANSMITTING_POWER_NARROW_PARAM));
            effectiveEnergyIssued = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.EFFECTIVE_ENERGY_ISSUED_NARROW_PARAM));
            standardEnergyIssued = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.STANDARD_ENERGY_ISSUED_NARROW_PARAM));
            effectiveCalibrationPower = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.EFFECTIVE_CALIBRATION_POWER_NARROW_PARAM));
            standardCalibrationPower = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.STANDARD_CALIBRATION_POWER_NARROW_PARAM));
        } // end if
        else
        {
            // OTHER SENSOR
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_TRANSMITTING_POWER_WIDE_PARAM));
            effectiveEnergyIssued = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.EFFECTIVE_ENERGY_ISSUED_WIDE_PARAM));
            standardEnergyIssued = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.STANDARD_ENERGY_ISSUED_WIDE_PARAM));
            effectiveCalibrationPower = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.EFFECTIVE_CALIBRATION_POWER_WIDE_PARAM));
            standardCalibrationPower = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.STANDARD_CALIBRATION_POWER_WIDE_PARAM));
        } // end else
          // //System.out.println("kFactor: " + kFactor + ";
          // effectiveEnergyIssued: " + effectiveEnergyIssued + ";
          // effectiveCalibrationPower: " + effectiveCalibrationPower + ";
          // standardEnergyIssued: " + standardEnergyIssued + ";
          // standardCalibrationPower: " + standardCalibrationPower + "; result:
          // " + kFactor * ((effectiveEnergyIssued + effectiveCalibrationPower)
          // / (standardEnergyIssued + standardCalibrationPower)));
          // returning
        return kFactor * ((effectiveEnergyIssued + effectiveCalibrationPower) / (standardEnergyIssued + standardCalibrationPower));
    }// end method

    /**
     * return data take volume
     * 
     * @param sensorMode
     * @return volume
     */
    private double calculateDataTakeVolume(String sensorMode)
    {
        double kFactor;
        double effectiveDataVolume;
        double standardDataVolume;

        // check sensor mode
        // read values from poperty
        if (QUMConstants.SENSOR_MODE_SP_ENHANCED.equals(sensorMode))
        {
            // SP_EN
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_DATA_TAKE_VOLUME_NARROW_PARAM));
            effectiveDataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.EFFECTIVE_DATA_VOLUME_SP_ENHANCED_PARAM));
            standardDataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.STANDARD_DATA_VOLUME_NARROW_PARAM));
        } // end if
        else if (QUMConstants.SENSOR_MODE_STR_HIMAGE.equals(sensorMode))
        {
            // STR HI
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_DATA_TAKE_VOLUME_WIDE_PARAM));
            effectiveDataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.EFFECTIVE_DATA_VOLUME_STR_HIMAGE_PARAM));
            standardDataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.STANDARD_DATA_VOLUME_WIDE_PARAM));
        } // end else
        else if (QUMConstants.SENSOR_MODE_STR_PINGPONG.equals(sensorMode))
        {
            // PING PONG
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_DATA_TAKE_VOLUME_WIDE_PARAM));
            effectiveDataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.EFFECTIVE_DATA_VOLUME_STR_PINGPONG_PARAM));
            standardDataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.STANDARD_DATA_VOLUME_WIDE_PARAM));
        } // end else
        else if (QUMConstants.SENSOR_MODE_SCN_WIDE.equals(sensorMode))
        {
            // SCAN WIDE
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_DATA_TAKE_VOLUME_WIDE_PARAM));
            effectiveDataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.EFFECTIVE_DATA_VOLUME_SCN_WIDE_PARAM));
            standardDataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.STANDARD_DATA_VOLUME_WIDE_PARAM));
        } // end else
        else
        {
            // OTHER
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_DATA_TAKE_VOLUME_WIDE_PARAM));
            effectiveDataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.EFFECTIVE_DATA_VOLUME_SCN_HUGE_PARAM));
            standardDataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.STANDARD_DATA_VOLUME_WIDE_PARAM));
        } // end else
          // //System.out.println("kFactor: " + kFactor + "; effectiveDataVolume:
          // " + effectiveDataVolume + "; standardThermalEnhancement: " +
          // standardDataVolume + "; result: " + kFactor * (effectiveDataVolume
          // / standardDataVolume));
          // returning factor
        return kFactor * (effectiveDataVolume / standardDataVolume);
    }// end method

    /**
     * data take opportunity
     * 
     * @param sensorMode
     * @param startTime
     * @param stopTime
     * @return data take opportunity
     */
    private double calculateDataTakeAcquisitionDuration(String sensorMode, double startTime, double stopTime)
    {
        // init facort
        double kFactor;
        double sensingTimeHead;
        double sensingTimeTrail;
        // converting duration in seconds
        long dtoDuration = DateUtils.fromCSKDurationToSeconds(stopTime - startTime);
        double standardImageDuration;

        // controlling sensor mode
        // and get values form poperty
        if (QUMConstants.SENSOR_MODE_SP_ENHANCED.equals(sensorMode))
        {
            // Sensor mode SP_EN
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_DATA_TAKE_ACQUISITION_DURATION_NARROW_PARAM));
            sensingTimeHead = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.SENSING_TIME_HEAD_SP_ENHANCED_PARAM));
            sensingTimeTrail = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.SENSING_TIME_TRAIL_SP_ENHANCED_PARAM));
            standardImageDuration = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.STANDARD_DURATION_NARROW_PARAM));
        } // end if
        else if (QUMConstants.SENSOR_MODE_STR_HIMAGE.equals(sensorMode))
        {
            // Sensor mode STR_HI
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_DATA_TAKE_ACQUISITION_DURATION_WIDE_PARAM));
            sensingTimeHead = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.SENSING_TIME_HEAD_STR_HIMAGE_PARAM));
            sensingTimeTrail = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.SENSING_TIME_TRAIL_STR_HIMAGE_PARAM));
            standardImageDuration = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.STANDARD_DURATION_WIDE_PARAM));
        } // end else if
        else if (QUMConstants.SENSOR_MODE_STR_PINGPONG.equals(sensorMode))
        {
            // Sensor mode PING
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_DATA_TAKE_ACQUISITION_DURATION_WIDE_PARAM));
            sensingTimeHead = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.SENSING_TIME_HEAD_STR_PINGPONG_PARAM));
            sensingTimeTrail = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.SENSING_TIME_TRAIL_STR_PINGPONG_PARAM));
            standardImageDuration = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.STANDARD_DURATION_WIDE_PARAM));
        } // end else if
        else if (QUMConstants.SENSOR_MODE_SCN_WIDE.equals(sensorMode))
        {
            // Sensor mode SCAN_W
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_DATA_TAKE_ACQUISITION_DURATION_WIDE_PARAM));
            sensingTimeHead = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.SENSING_TIME_HEAD_SCN_WIDE_PARAM));
            sensingTimeTrail = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.SENSING_TIME_TRAIL_SCN_WIDE_PARAM));
            standardImageDuration = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.STANDARD_DURATION_WIDE_PARAM));
        } // end else if
        else
        {
            // OTHER
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_DATA_TAKE_ACQUISITION_DURATION_WIDE_PARAM));
            sensingTimeHead = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.SENSING_TIME_HEAD_SCN_HUGE_PARAM));
            sensingTimeTrail = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.SENSING_TIME_TRAIL_SCN_HUGE_PARAM));
            standardImageDuration = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.STANDARD_DURATION_WIDE_PARAM));
        } // end else
          // //System.out.println("kFactor: " + kFactor + "; sensingTimeHead: " +
          // sensingTimeHead + "; dtoDuration: " + dtoDuration + ";
          // sensingTimeTrail: " + sensingTimeTrail + "; standardImageDuration:
          // " + standardImageDuration + "; result: " + kFactor *
          // ((sensingTimeHead + dtoDuration + sensingTimeTrail) /
          // standardImageDuration));
          // returninf factor
        return kFactor * ((sensingTimeHead + dtoDuration + sensingTimeTrail) / standardImageDuration);
    }// end method

    /**
     * downlink duration
     * 
     * @param sensorMode
     * @return downlink duration
     */
    private double calculateDownlinkDuration(String sensorMode)
    {
        double kFactor;
        double effectiveDataVolume;
        double standardDataVolume;
        // checking sensor mode
        // retireve parameters from property
        if (QUMConstants.SENSOR_MODE_SP_ENHANCED.equals(sensorMode))
        {
            // SP_EN
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_DOWNLINK_DURATION_NARROW_PARAM));
            effectiveDataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.EFFECTIVE_DATA_VOLUME_SP_ENHANCED_PARAM));
            standardDataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.STANDARD_DATA_VOLUME_NARROW_PARAM));
        } // end if
        else if (QUMConstants.SENSOR_MODE_STR_HIMAGE.equals(sensorMode))
        {
            // HI_IMAG
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_DOWNLINK_DURATION_WIDE_PARAM));
            effectiveDataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.EFFECTIVE_DATA_VOLUME_STR_HIMAGE_PARAM));
            standardDataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.STANDARD_DATA_VOLUME_WIDE_PARAM));
        } // end else
        else if (QUMConstants.SENSOR_MODE_STR_PINGPONG.equals(sensorMode))
        {
            // PING PONG
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_DOWNLINK_DURATION_WIDE_PARAM));
            effectiveDataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.EFFECTIVE_DATA_VOLUME_STR_PINGPONG_PARAM));
            standardDataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.STANDARD_DATA_VOLUME_WIDE_PARAM));
        } // end else
        else if (QUMConstants.SENSOR_MODE_SCN_WIDE.equals(sensorMode))
        {
            // SCN WIDE
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_DOWNLINK_DURATION_WIDE_PARAM));
            effectiveDataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.EFFECTIVE_DATA_VOLUME_SCN_WIDE_PARAM));
            standardDataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.STANDARD_DATA_VOLUME_WIDE_PARAM));
        } // end else
        else
        {
            // OTHER
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_DOWNLINK_DURATION_WIDE_PARAM));
            effectiveDataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.EFFECTIVE_DATA_VOLUME_SCN_HUGE_PARAM));
            standardDataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.STANDARD_DATA_VOLUME_WIDE_PARAM));
        } // end else
          // //System.out.println("kFactor: " + kFactor + "; effectiveDataVolume:
          // " + effectiveDataVolume + "; standardDataVolume: " +
          // standardDataVolume + "; result: " + kFactor * (effectiveDataVolume
          // / standardDataVolume));
          // returning
        return kFactor * (effectiveDataVolume / standardDataVolume);
    }// end method

    /**
     * Evaluate on board data latency
     * 
     * @param sensorMode
     * @return latency
     */
    private double calculateOnBoardDataLatency(String sensorMode)
    {
        double kFactor;
        double otherDataLatency = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.OTHER_DATA_LATENCY_PARAM));
        // for sp_EN
        if (QUMConstants.SENSOR_MODE_SP_ENHANCED.equals(sensorMode))
        {
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_ON_BOARD_DATA_LATENCY_NARROW_PARAM));
        } // end if
        else
        {
            // OTHER SENSOR MODE
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_ON_BOARD_DATA_LATENCY_WIDE_PARAM));
        } // end else
          // //System.out.println("kFactor: " + kFactor + "; otherDataLatency: " +
          // otherDataLatency + "; result: " + kFactor * otherDataLatency);
          // returning
        return kFactor * otherDataLatency;
    } // end method

    /**
     * Do nothing
     * 
     * @return 0
     */
    private double calculateAcquisitionOpportunity()
    {
        // //System.out.println("result: " + 0);
        return 0;
    } // end method

    /**
     * Evaluate slew manoevre
     * 
     * @param sensorMode
     * @param lookSide
     * @return slew
     */
    private double calculateSlewManoeuvreDuration(String sensorMode, String lookSide)
    {
        double slewManoeuvreDuration;
        // if right nothing
        if (QUMConstants.LOOK_SIDE_RIGHT.equals(lookSide))
        {
            // //System.out.println("result: " + 0);
            slewManoeuvreDuration = 0;
        } // end if
        else
        {
            // left so we have evaluate
            double kFactor;
            double zone1Cost = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.ZONE_1_COST_PARAM));
            double zone2Cost = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.ZONE_2_COST_PARAM));
            double slewManoeuvreDurationConst = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.SLEW_MANOEUVRE_DURATION_PARAM));

            if (QUMConstants.SENSOR_MODE_SP_ENHANCED.equals(sensorMode))
            {
                // SP_EN
                kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_SLEW_MANOEUVRE_DURATION_NARROW_PARAM));
            } // end if
            else
            {
                // OTHER SENSOR MODE
                kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_SLEW_MANOEUVRE_DURATION_WIDE_PARAM));
            } // end else
              // //System.out.println("kFactor: " + kFactor + "; zone1Cost: " +
              // zone1Cost + "; zone2Cost: " + zone2Cost + ";
              // slewManoeuvreDurationConst" + slewManoeuvreDurationConst + ";
              // result: " + kFactor * (Math.max(zone1Cost, zone2Cost) *
              // slewManoeuvreDurationConst));
            slewManoeuvreDuration = kFactor * (Math.max(zone1Cost, zone2Cost) * slewManoeuvreDurationConst);
        } // end else

        return slewManoeuvreDuration;
    } // end method

    /**
     * Evaluate manoeuvre duration
     * 
     * @param sensorMode
     * @param beam
     * @return duration
     */
    private double calculateExtManoeuvreDuration(String sensorMode, String beam)
    {
        double extManoeuvreDuration;
        // no marovre
        if (!this.rollRequiredBeams.contains(beam))
        {
            // //System.out.println("result: " + 0);
            extManoeuvreDuration = 0;
        } // end if
        else
        {
            // evaluating manovre
            double kFactor;
            double zone1Cost = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.ZONE_1_COST_PARAM));
            double zone2Cost = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.ZONE_2_COST_PARAM));
            double maximumRollTime = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.MAXIMUM_ROLL_TIME_PARAM));

            if (QUMConstants.SENSOR_MODE_SP_ENHANCED.equals(sensorMode))
            {
                // SP_ENAN
                kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_EXT_MANOEUVRE_DURATION_NARROW_PARAM));
            } // end if
            else
            {
                // OTHR
                kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_EXT_MANOEUVRE_DURATION_WIDE_PARAM));
            } // end else
              // //System.out.println("kFactor: " + kFactor + "; zone1Cost: " +
              // zone1Cost + "; zone2Cost: " + zone2Cost + "; maximumRollTime" +
              // maximumRollTime + "; result: " + kFactor * (Math.max(zone1Cost,
              // zone2Cost) * maximumRollTime));
            extManoeuvreDuration = kFactor * (Math.max(zone1Cost, zone2Cost) * maximumRollTime);
        } // end else

        // returning
        return extManoeuvreDuration;
    } // end method

    /**
     * evaluate ground network duration
     * 
     * @param sensorMode
     * @return duration
     */
    private double calculateGroundDataNetworkUsageDuration(String sensorMode)
    {
        double kFactor;
        double dataVolume;
        double dayDataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.DAY_DATA_VOLUME_PARAM));
        // check on sensor mode
        // read parameters from property
        if (QUMConstants.SENSOR_MODE_SP_ENHANCED.equals(sensorMode))
        {
            // SP_EN
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_GROUND_DATA_NETWORK_USAGE_DURATION_NARROW_PARAM));
            dataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.EFFECTIVE_DATA_VOLUME_SP_ENHANCED_PARAM));
        } // end if
        else if (QUMConstants.SENSOR_MODE_STR_HIMAGE.equals(sensorMode))
        {
            // STR_HI
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_GROUND_DATA_NETWORK_USAGE_DURATION_WIDE_PARAM));
            dataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.EFFECTIVE_DATA_VOLUME_STR_HIMAGE_PARAM));
        } // end else
        else if (QUMConstants.SENSOR_MODE_STR_PINGPONG.equals(sensorMode))
        {
            // PING PONG
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_GROUND_DATA_NETWORK_USAGE_DURATION_WIDE_PARAM));
            dataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.EFFECTIVE_DATA_VOLUME_STR_PINGPONG_PARAM));
        } // end else
        else if (QUMConstants.SENSOR_MODE_SCN_WIDE.equals(sensorMode))
        {
            // SCN WIDE
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_GROUND_DATA_NETWORK_USAGE_DURATION_WIDE_PARAM));
            dataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.EFFECTIVE_DATA_VOLUME_SCN_WIDE_PARAM));
        } // end else
        else
        {
            // OTHER
            kFactor = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.K_FACTOR_GROUND_DATA_NETWORK_USAGE_DURATION_WIDE_PARAM));
            dataVolume = Double.parseDouble(this.QUMProperties.getProperty(QUMConstants.EFFECTIVE_DATA_VOLUME_SCN_HUGE_PARAM));
        } // end else
          // //System.out.println("kFactor: " + kFactor + "; dataVolume: " +
          // dataVolume + "; dayDataVolume: " + dayDataVolume + "; result: " +
          // kFactor * (dataVolume / dayDataVolume));
          // RETVAL
        return kFactor * (dataVolume / dayDataVolume);
    } // end method

    /**
     * Helper class used for the INSTANCE GENERATION
     * 
     * @author Amedeo Bancone
     * @version 1.0
     *
     */
    private static class SUFCalculatorHelper
    {

        private static SUFCalculator INSTANCE; // The single instance of
                                               // PropertiesReader
        static
        {

            INSTANCE = new SUFCalculator();

        }// end static
    } // end class
    
    
    public Map<String, CSGBicParameters> getSensormodeBicParametersMap() {
		return sensormodeBicParametersMap;
	}

	public void setSensormodeBicParametersMap(Map<String, CSGBicParameters> sensormodeBicParametersMap) {
		this.sensormodeBicParametersMap = sensormodeBicParametersMap;
	}
    /**
     * Entry point to get the single instance of the class
     * 
     * @return The single instance of the class
     */
    public static SUFCalculator getInstance()
    {

        return SUFCalculatorHelper.INSTANCE;
    } // end method
}