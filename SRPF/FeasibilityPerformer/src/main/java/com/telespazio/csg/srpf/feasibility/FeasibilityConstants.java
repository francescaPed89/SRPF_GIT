/**
*
* MODULE FILE NAME:	FeasibilityConstants.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			This class list the constants used in the feasibility perfomer package
*
* PURPOSE:			Perform Feasibility
*
* CREATION DATE:	17-12-2015
*
* AUTHORS:			Amedeo Bancone
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
* 16-05-2016 | Amedeo Bancone  |1.1| Fixed bug on maxLookAngle method
*                                    in order to align to the new spotLight algo commented unused constants:
*                                    CSKSatelliteInclinationDegree
*                                    CSKSatPeriod
*                                    CSKSatMeanMotion
*                                    CSKSatelliteInclinationRadians
* --------------------------+------------+----------------+-------------------------------
* 28-02-2018 | Amedeo Bancone  |2.0| Added constant to align to C4
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.feasibility;

import com.telespazio.csg.srpf.dataManager.util.DataManagerConstants;
import com.telespazio.csg.srpf.utils.DateUtils;
import com.telespazio.csg.srpf.utils.PropertiesReader;

/**
 *
 * This class list the constants used in the feasibility perfomer package
 *
 * @author Amedeo Bancone
 * @version 2.0
 */
public class FeasibilityConstants
{

    // Numeric Constant
    public final static double Mega = 1e6;
    // Numeric Constant
    public final static double half = 0.5;
    // Numeric Constant
    public final static int forTwo = 2;
    // Numeric Constant
    public final static double Kilo = 1000.0;
    // Numeric Constant
    public final static int hundred = 100;
    // Numeric Constant
    public final static double SolvEps = 1E-12;
    // Numeric Constant
    public final static int SolvMaxEval = 10000;
    // Polarization
    public final static String defaultPolarization = "VV";

    /**
     * The value of SIZE returned by SPARC must be multiplied for 4
     */
    // public final static int dtoSizeMultiplicator=2;

    /**
     * Number of sample to be leaved in performing access evaluation
     */
    public final static int NumberOfGuardSample = 4;

    // Mission Constant
    public final static String CSK_NAME = "CSK";
    // Mission Constant
    public final static String CSG_NAME = "CSG";
    // Mission Constant
    public final static String COMBINED_NAME = "COMBINED";

    // Numeric Constant
    public final static double CircleToPolygonAngularStep = 2.0;

    // Numeric Constant
    public final static double MinimalPRValidityDuration = 1.0;
    // Numeric Constant
    public final static double MaximalPRvalidityDuration = 90;
    // Numeric Constant
    public final static double MaxAreaOfInterest = 15000000000.0; // m2
    // Numeric Constant
    public final static double MinCoverage = 50.0; // 50%

    // look side numeric constant
    public final static int RighLookSide = DataManagerConstants.ID_ALLOWED_lOOK_RIGHT;
    // look side numeric constant
    public final static int LeftLookSide = DataManagerConstants.ID_ALLOWED_lOOK_LEFT;
    // look side numeric constant
    public final static int BothLookSide = DataManagerConstants.ID_ALLOWED_lOOK_BOTH;

    // look side String constant
    public final static String LeftLookSideAsString = "Left";
    // look side String constant
    public final static String RightLookSideAsString = "Right";
    // look side String constant
    public final static String BothLookSideAsString = "Both";

    // orbit constants
    public final static int AscendingOrbit = 0;
    // orbit constants
    public final static int DescendingOrbit = 1;
    // orbit constants
    public final static int BothOrbitDirection = 2;
    // orbit constants
    public final static int AnyOrbitDirection = 3;

    // orbit constants as string
    public final static String AscendingOrbitAsString = "Ascending";
    // orbit constants as string
    public final static String DescendingOrbitAsString = "Descending";
    // orbit constants as string
    public final static String BothOrbitAsString = "Both";
    // orbit constants as string
    public final static String AnyorbitAsString = "Any";

    // pol
    public final static String DefaultPolarization = "VV";

    /**
     *
     * @param lookSide
     * @return look side string
     */
    public final static String getLookSideString(int lookSide)
    {
        /**
         * Default
         */
        String retval = BothLookSideAsString;

        /**
         * Switch
         */
        switch (lookSide)
        {
            case RighLookSide:
                retval = RightLookSideAsString;
                break;
            case LeftLookSide:
                retval = LeftLookSideAsString;
                break;
            default:
                break;
        }// end switch

        return retval;
    }// end method

    /**
     * Return the orbit direction as string
     *
     * @param orbit
     * @return orbit string
     */
    public final static String getOrbitDirectionAsString(int orbit)
    {
        /**
         * Default
         */
        String retval = AnyorbitAsString;

        /**
         * Switch
         */
        switch (orbit)
        {
            case AscendingOrbit:
                retval = AscendingOrbitAsString;
                break;
            case DescendingOrbit:
                retval = DescendingOrbitAsString;
                break;
            case BothOrbitDirection:
                retval = BothOrbitAsString;
                break;
            default:
                break;
        }// end switch

        return retval;
    }// end method

    /**
     * return the constant for look side starting from the int
     *
     * @param sideString
     * @return int representing look side
     */
    public static final int getLookSideValue(String sideString)
    {
        // value to be returned default both
        int side = FeasibilityConstants.BothLookSide;

        if (toLowerCaseCompare(sideString, RightLookSideAsString))
        {
            side = RighLookSide;
        } // end if
        else if (toLowerCaseCompare(sideString, LeftLookSideAsString))
        {
            side = LeftLookSide;
        } // end side
          // return value
        return side;
    } // end method

    /**
     * return the constant for orbit direction starting from the string
     *
     * @param orbitString
     * @return the constant for orbit direction starting from the string
     */
    public static final int getOrbitDirValue(String orbitString)
    {
        int orbidDir = BothOrbitDirection;
        // evaluating default: both
        if (toLowerCaseCompare(orbitString, AscendingOrbitAsString))
        {
            orbidDir = AscendingOrbit;
        } // end if
        else if (toLowerCaseCompare(orbitString, DescendingOrbitAsString))
        {
            orbidDir = DescendingOrbit;
        } // end else if
        else if (toLowerCaseCompare(orbitString, AnyorbitAsString))
        {
            orbidDir = AnyOrbitDirection;
        } // end else
          // return value
        return orbidDir;
    }// end method

    /**
     * Compare two string ignore case
     *
     * @param s1
     * @param s2
     * @return true if the strings match
     */
    private final static boolean toLowerCaseCompare(String s1, String s2)
    {
        return s1.toLowerCase().equals(s2.toLowerCase());
    }// end method

    /**
     * the dwl speed reading from configuration
     *
     * @return the dwl speed reading from configuration
     */
    private static double getDWLFromConfiguration()
    {
        /**
         * Default value
         */
        double speed = 260.0;

        try
        {
            // //System.out.println("Reading speed from configuration");
            /**
             * Reading dwl from configuration
             */
            String speedAsString = PropertiesReader.getInstance().getProperty(DWL_SPEED_CONF_KEY, "260.0");
            double value = Double.parseDouble(speedAsString);
            speed = value;

        } // end try
        catch (Exception e)
        {
            /**
             * Misconfigured default used
             */
            // System.err.println("Ecce");
            e.printStackTrace();
        } // end catch

        return speed;
    }// end getDWLFromConfiguration

    public final static double DWLSpeed = getDWLFromConfiguration();

    // public final static int OdstpType = DataManagerConstants.TYPE_ODSTP;
    // public final static int OdmtpType = DataManagerConstants.TYPE_ODMTP;
    // public final static int OdnomType = DataManagerConstants.TYPE_ODNOM;
    public final static int OdrefType = DataManagerConstants.TYPE_ODREF;

    // Sampling rate for orbital data in seconds
    public final static long OdstpSamplingRate = DataManagerConstants.OdstpSamplingRate;
    // Sampling rate for orbital data in seconds
    public final static long OdmtpSamplingRate = DataManagerConstants.OdmtpSamplingRate;
    // Sampling rate for orbital data in seconds
    public final static long OdnomSamplingRate = DataManagerConstants.OdnomSamplingRate;
    // Sampling rate for orbital data in seconds
    public final static long OdrefSamplingRate = DataManagerConstants.OdrefSamplingRate;

    // Repetition period for ODREF in days
    public final static double RepetitionODREFPeriod = 16.0;

    // public final static double julianminute = 6.944449996808544E-4;
    public final static double julianminute = DateUtils.secondsToJulian(60);

    // Time to determine if the orbit has changed
    public final static double StripTimeOffest = 4 * julianminute;

    // Algo constants, it should be considered as default value. It should be
    // congìfigurable parameters

    // this is a value to considert that a change of sign in longitude means an
    // overcome of the line of date

    public final static double LongitudeLimitToUnderstandForLineDate = 160.0;
    // SpotLight
    // public final static double CSKSatelliteInclinationDegree = 98.21;
    // public final static double CSKSatelliteInclinationRadians =
    // Math.toRadians(CSKSatelliteInclinationDegree);
    // public final static double CSKSatPeriod = 5825.6679873029;
    // public final static double CSKSatMeanMotion =
    // 1.0/(CSKSatPeriod/60.0/1440.0); //Mean motion: number of ornbit per day

    public final static double CSKSpotlightTimeStep = DateUtils.secondsToJulian(6);
    public final static double CSKSpotlightHalfTimeStep = 0.5 * CSKSpotlightTimeStep;
    // public final static double CSKSpotLightSqureSide = 10000;
    public final static double SpotLightGridSpacing = 0.05;

    // public final static double CSKSpotLighgtBoundaryTolerance =
    // DateUtils.secondsToJulian(120); //difference between strip interval and
    // boundary interval

    // used to check for possible overlapping strip / DTO in the stripmap algo
    // and in the strip generation . Also used in path holes
    public final static double ManouvreTolerance = DateUtils.secondsToJulian(1200);

    // Define the half interval for searching access in Feasibility Refinement
    public final static double RefinementHalfInterval = DateUtils.secondsToJulian(1200);

    // StripMap algo
    public final static double CSKSTRIPMAPMinimalDTODuration = DateUtils.secondsToJulian(6); // six
                                                                                             // seconds
    public final static double CKSTRIPMAPMaximalDuration = DateUtils.secondsToJulian(600); // ten
                                                                                           // minutes
    public final static double CSKSTRIPMapRestTime = DateUtils.secondsToJulian(5); // time
                                                                                   // needed
                                                                                   // to
                                                                                   // restore
                                                                                   // sensor

    // default grid spacing
    public final static double StripMapGridSpacing = 0.1032; // deg
    // public final static double StripMapGridSpacing = 0.05;

    // public final static double StripMapGridSpacing = 4.0;

    // the post fix used to build the configuration key used to retrrieve the
    // max area of interest in configuration file
    public final static String MaxAreaOfInterestPostFix = "_MAX_AREA";

    // circle to polygon constants

    // public final double radiusIncrementation = 1.0; //increments rhe radius
    // of the are of this factor
    // public final double angularSpacing = 2.0 ; //angle in degree sating the
    // single arc in the circle during the tranformation

    // Coefficient default for the three components of optimization function
    // (Inner loop)
    public final static double IP1 = 1.0;
    // Coefficient default for the three components of optimization function
    // (Inner loop)
    public final static double IP2 = 1.0;
    // Coefficient default for the three components of optimization function
    // (Inner loop)
    public final static double IP3 = 1.0;

    // Coefficient default for the three components of optimization function
    // (Outer loop)
    public final static double OP1 = 1.0;
    // Coefficient default for the three components of optimization function
    // (Outer loop)
    public final static double OP2 = 1.0;
    // Coefficient default for the three components of optimization function
    // (Outer loop)
    public final static double OP3 = 1.0;

    ///////
    /// XML_NAMESPACES

    // TPZ Programming Namespace
    public final static String ProgNS = "http://www.telespazio.com/IOP/schemas/programming";
    // TPZ Common Namespace
    public final static String CommonNS = "http://www.telespazio.com/IOP/schemas/common";
    // GML Namespace
    public final static String GmlNS = "http://www.opengis.net/gml/3.2";

    // XML TAG
    public final static String FeasibilityAnalysisResponseTagName = "FeasibilityAnalysisResponse";
    // XML TAG
    public final static String AnalysePRListResponseTagName = "AnalysePRListResponse";
    // XML TAG
    public final static String listAimTagName = "listAim";
    public final static String listAimTagNameNS = ProgNS;
    // XML TAG
    public final static String listAimFeasibilityValue = "Feasibility";
    // XML TAG
    public final static String listAimFeasibilityExtensionValue = "FeasibilityExtension";

    // Acq extensioninfo
    // XML TAG
    public final static String timeExtensionInfoTagName = "timeExtensionInfo";
    public final static String timeExtensionInfoTagNameNS = ProgNS;
    // XML TAG
    public final static String stillToBePlannedValue = "stillToBePlanned";
    public final static String toBeExtendedValue = "toBeExtended";
    // XML TAG
    public final static String ProgReqTagName = "ProgReq";
    public final static String ProgReqTagNameNS = ProgNS;

    // XML TAG
    public final static String ProgReqUGSIdTagName = "UGSId";
    public final static String ProgReqUGSIdTagNameNS = CommonNS;

    // XML TAG
    public final static String ProgReqIdTagName = "ProgReqId";
    public final static String ProgReqIdTagNameNS = CommonNS;
    
    
    public final static String serviceRequestIdTagName = "serviceRequestId";
    public final static String serviceRequestIdTagNameNS = CommonNS;


    // XML TAG
    public final static String ProgrammingParametersTagName = "ProgrammingParameters";
    public final static String ProgrammingParametersTagNameNS = ProgNS;
    // XML TAG
    public final static String ProgrammingAreaTagName = "ProgrammingArea";
    public final static String ProgrammingAreaTagNameNS = ProgNS;

    // XML TAG
    public final static String AcquisitionRequestTagName = "AcqReq";
    public final static String AcquisitionRequestTagNameNS = ProgNS;
    // XML TAG
    public final static String AcquisitionRequestIDTagName = "AcqReqId";
    public final static String AcquisitionRequestIDTagNameNS = CommonNS;

    // public final static String AcquisitionRankTangName = "rank";
    // public final static String AcquisitionRankTangNameNS = ProgNS;
    // XML TAG
    public final static String MissionTagName = "mission";
    public final static String MissionTagNameNS = CommonNS;
    // XML TAG
    public final static String SensorModeTagName = "sensorMode";
    public final static String SensorModeTagNameNS = CommonNS;
    // XML TAG
    public final static String PlatformTagName = "Platform"; // Used as child of
                                                             // ProgrammingParameters
    public final static String PlatformTagNameNS = ProgNS; // Used as child of
                                                           // ProgrammingParameters
    // XML TAG
    public final static String SensingConstraintsTagName = "SensingConstraints";
    public final static String SensingConstraintsTagNameNS = ProgNS;

    // public final static String PlatformInSarConstraintTagName ="Platform";
    // //Used as child of IOPpp:SensingConstraints/SarConstraints
    // public final static String PlatformInSarConstraintTagNameNS =CommonNS;
    // //Used as child of IOPpp:SensingConstraints/SarConstraints
    // XML TAG
    public final static String PrValidityStartTimeTagName = "timeStart";
    public final static String PrValidityStartTimeTagNameNS = CommonNS;
    // XML TAG
    public final static String PrValidityStopTimeTagName = "timeStop";
    public final static String PrValidityStopTimeTagNameNS = CommonNS;

    // this flag shall not used anymore cause a change on the XSD
    // public final static String InterferometricFlagTagNAme =
    // "interferometricFlag";
    // public final static String InterferometricFlagTagNAmeNS =CommonNS;

    // XML TAG
    public final static String CircleByCenterPointTagName = "CircleByCenterPoint";
    public final static String CircleByCenterPointTagNameNS = GmlNS;
    // XML TAG
    public final static String LineStringTagName = "LineString";
    public final static String LineStringTagNameNS = GmlNS;
    // XML TAG
    public final static String PointTagName = "Point";
    public final static String PointTagNameNS = GmlNS;
    // XML TAG
    public final static String PosTagName = "pos";
    public final static String PosTagNameNS = GmlNS;
    // XML TAG
    public final static String RadiusTagName = "radius";
    public final static String RadiusTagNameNS = GmlNS;
    // XML TAG
    public final static String DurationTagName = "duration";
    public final static String DurationTagNameNS = CommonNS;
    // XML TAG
    public final static String IOPPointTagName = "Point";
    // public final static String IOPPointTagNameNS =CommonNS;
    // XML TAG
    public final static String TargetCenteredPointTagName = "TargetCenteredPoint";
    public final static String TargetCenteredPointTagNameNS = ProgNS;
    // XML TAG
    public final static String PolygonTagName = "Polygon";
    public final static String PolygonTagNameNS = GmlNS;
    // XML TAG
    public final static String PolygonIDAttributeName = "id";
    public final static String PolygonIDAttributeNameNS = GmlNS;
    // XML TAG
    public final static String ExteriorTagName = "exterior";
    public final static String ExteriorTagNameNS = GmlNS;
    // XML TAG
    public final static String LinearRingTagName = "LinearRing";
    public final static String LinearRingTagNameNS = GmlNS;
    // XML TAG
    public final static String PosListTagName = "posList";
    public final static String PosListTagNameNS = GmlNS;
    // XML TAG
    public final static String DTOTagName = "DTO";
    public final static String DTOTagNameNS = ProgNS;
    // XML TAG
    public final static String DTOIdTagName = "DTOId";
    public final static String DTOIdTagNameNS = CommonNS;

    // XML TAG
    public final static String LinkedDTOTagNAme = "LinkedDTO";
    public final static String LinkedDTOTagNAmeNS = ProgNS;
    // XML TAG
    public final static String linkTypeTagName = "linkType";
    public final static String linkTypeTagNameNS = ProgNS;
    // XML TAG
    public final static String INTERFERMETRIC_LINK_TYPE = "interferometric";
    public final static String STEREO_PAIR_LINK_TYPE = "stereopair";
    // XML TAG
    public final static String DTOSensingTagName = "DTOsensingTime";
    public final static String DTOSensingTagNameNS = ProgNS;

    // XML TAG
    public final static String timeStartTagName = "timeStart";
    public final static String timeStartTagNameNS = CommonNS;
    // XML TAG
    public final static String timeStopTagName = "timeStop";
    public final static String timeStopTagNameNS = CommonNS;
    // XML TAG
    public final static String DTOInfoTagName = "DTOInfo";
    public final static String DTOInfoTagNameNS = ProgNS;

    // XML TAG
    public final static String SarTagName = "Sar";
    public final static String SarTagNameNS = ProgNS;

    // XML TAG
    public final static String LookSideTagName = "lookSide";
    public final static String LookSideTagNameNS = CommonNS;
    // XML TAG
    public final static String orbitNumberTagName = "orbitNumber";
    public final static String orbitNumberTagNameNS = CommonNS;
    // XML TAG
    public final static String orbitDirectionTagName = "orbitDirection";
    public final static String orbitDirectionTagNameNS = CommonNS;
    // XML TAG
    public final static String trackNumberTagName = "trackNumber";
    public final static String trackNumberTagNameNS = CommonNS;
    // XML TAG
    public final static String SatelliteTagName = "satellite";
    public final static String SatelliteTagNameNS = CommonNS;
    // XML TAG
    public final static String PolarizationTagName = "polarization";
    public final static String PolarizationTagNameNS = CommonNS;
    // XML TAG
    public final static String BeamIdTagName = "beamId";
    public final static String BeamIdTagNameNS = CommonNS;
    // XML TAG
    public final static String LookAngleTagName = "lookAngle";
    public final static String LookAngleTagNameNS = CommonNS;

    // XML TAG
    public final static String MinLookAngleTagName = "minLookAngle";
    public final static String MinLookAngleTagNameNS = CommonNS;
    // XML TAG
    public final static String MaxLookAngleTagName = "maxLookAngle";
    public final static String MaxLookAngleTagNameNS = CommonNS;
    // XML TAG
    public final static String PRCoveragePercentageRequiredTagName = "PRCoveragePercentageRequired";
    public final static String PRCoveragePercentageRequiredTagNameNS = ProgNS;
    // XML TAG
    public final static String SensorTagName = "sensor";
    public final static String SensorTagNameNS = CommonNS;
    // XML TAG
    public final static String PRStatusTagName = "PRStatus";
    public final static String PRStatusTagNameNS = ProgNS;
    // XML TAG
    public final static String statusTagName = "status";
    public final static String statusTagNameNS = CommonNS;
    // XML TAG
    public final static String descriptionTagName = "description";
    public final static String descriptionTagNameNS = CommonNS;
    // XML TAG
    public final static String PRCoverageTagName = "PRCoveragePercentage";
    public final static String PRCoverageTagNameNS = ProgNS;
    // Coverage values
    public final static String CompleteStatusString = "Complete";
    public final static String PartialStatusString = "Partial";
    public final static String FailedStatusString = "Failed";
    // XML TAG
    public final static String TargetDistanceTagName = "targetDistance";
    public final static String TargetDistanceTagNameNS = ProgNS;

    // XML TAG
    public final static String PR_FA_TagName = "PR_FA";
    public final static String PR_FA_TagNameNS = ProgNS;
    // XML TAG
    public final static String PRSUFTagName = "PRSUF";
    public final static String PRSUFTagNameNS = ProgNS;
    // XML TAG
    public final static String EarliestExecutionDateTagName = "EarliestExecutionDate";
    public final static String EarliestExecutionDateTagNameNS = ProgNS;
    // XML TAG
    public final static String LikelyExecutionDateTagName = "LikelyExecutionDate";
    public final static String LikelyExecutionDateTagNameNS = ProgNS;
    // XML TAG
    public final static String RemainingExecutionAttemptsTagName = "RemainingExecutionAttempts";
    public final static String RemainingExecutionAttemptsTagNameNS = ProgNS;
    // XML TAG
    public final static String DTOSufTagName = "SUFDTO";
    public final static String DTOSufTagNameNS = ProgNS;

    // Parmetrs for refinement
    // XML TAG
    public final static String PlannedTagName = "planned";
    public final static String PlannedTagNameNS = ProgNS;

    public final static String PlannedFalseValue = "false";
    public final static String PlannedTrueValue = "true";
    public final static String BackUpFlagTagName = "backUpFlag";
    public final static String BackUpFlagTagNameNS = ProgNS;

    // XML TAG
    public final static String AlertIDTagName = "AlertId";
    public final static String AlertIDTagNameNS = ProgNS;
    // XML TAG
    public final static String AlertDescriptionTagName = "AlertDescription";
    public final static String AlertDescriptionTagNameNS = ProgNS;

    // XML TAG
    public final static String PassThroughFlagTagName = "passThroughFlag";
    public final static String PassThroughFlagTagNameNS = ProgNS;

    public final static String TruePassThoughString = "true";
    // XML TAG
    public final static String AcquisitionStationTagName = "acquisitionStation";
    public final static String AcquisitionStationTagNameNS = ProgNS;

    // Di2S Tags
    public final static String DI2SAvailabilityTagName = "DI2SAvailability";
    public final static String DI2SAvailabilityTagNameNS = CommonNS;

    public final static String DI2SAvailabilityTrueValue = "true";
    public final static String DI2SAvailabilityFalseValue="false";
    // XML TAG
    public final static String DI2SAvailabilityConfirmationTagName = "DI2SAvailabilityConfirmation";
    public final static String DI2SAvailabilityConfirmationTagNameNS = CommonNS;

    public final static String DI2SAvailabilityConfirmationTrueValue = "true";
    public final static String DI2SAvailabilityConfirmationFalseValue = "false";

    // The minimum area of interest in Di2S is set as additional parameter whose
    // name in minimumAoI
    public final static String MinimumAoIValue = "minimumAoI";

    // The minimum area of interest is a geoValue (Polygon)
    // public final static String geoValuetagName ="geoValue";
    // public final static String geoValuetagNameNS =CommonNS;

    // Sparc Parameters
    public final static String SPARCInfoTagName = "SPARCinfo";
    public final static String SPARCInfoTagNameNS = ProgNS;

    // TAG for interferometric
    public final static String AdditionalProgrammingParamTagName = "AdditionalProgrammingParam";
    public final static String AdditionalProgrammingParamTagNameNS = ProgNS;
    // XML TAG
    public final static String AdditionalProgrammingNameTagName = "name";
    public final static String AdditionalProgrammingNameTagNameNS = CommonNS;
    // XML TAG
    public final static String stringValueTagName = "stringValue";
    public final static String stringValueTagNameNS = CommonNS;
    public final static String interferometricValue = "Interferometric";

    // Stereo parameters
    public final static String doubleValueTagName = "doubleValue";
    public final static String doubleValueTagNameNS = CommonNS;
    public final static String stereoPairsValue = "StereoPair";
    public final static String StereoDeltaLookAngleParameterName = "deltaLookAngle";
    public final static String StereoMinLookAngleParameterName = "minLookAngle";
    public final static String StereoMaxLookAngleParameterName = "maxLookAngle";

    // combined tag
    // XML TAG
    public final static String combinedReqFlagTagName = "combinedReqFlag";
    public final static String combinedReqFlagTagNameNS = ProgNS;
    public final static String combinedReqFlagTrueValue = "true";

    // Periodic / repetitive tags
    public final static String ValidityTimePeriodicityTagName = "ValidityTimePeriodicity";
    public final static String ValidityTimePeriodicityTagNameNS = ProgNS;
    // public final static String PeriodicTagName ="Periodic";
    // public final static String PeriodicTagNameNS =ProgNS;
    // XML TAG
    public final static String granularityTagName = "granularity";
    public final static String granularityTagNameNS = ProgNS;
    // XML TAG
    public final static String iterationsTagName = "iterations";
    public final static String iterationsTagNameNS = ProgNS;
    // XML TAG
    public final static String RepetitiveTagName = "Repetitive";
    public final static String RepetitiveTagNameNS = ProgNS;
    // XML TAG
    public final static String repetitiveIterationTagName = "repetitiveIteration";
    public final static String repetitiveIterationTagNameNS = ProgNS;
    // XML TAG
    public final static String repetitiveGranularityTagName = "repetitiveGranularity";
    public final static String repetitiveGranularityTagNameNS = ProgNS;

    // public final static String PeriodicInfoTagName ="PeriodicInfo";
    // public final static String PeriodicInfoTagNameNS =ProgNS;

    // This section held the name of the key used for retieve it in
    // configuration file

    // Max area of interest allowed dimension
    public final static String MAX_AREA_OF_INTEREST_CONF_KEY = "MAX_AREA_OF_INTEREST";

    // Min coverage for consider request at least partially satisfied
    // CONF KEY
    public final static String MIN_COVERAGE_CONF_KEY = "MIN_COVERAGE";
    // CONF KEY
    public final static String STRIPMAP_GRID_SPACING_CONF_KEY = "STRIPMAP_GRID_SPACING";
    // CONF KEY
    public final static String SPOTLIGHT_GRID_SPACING_CONF_KEY = "SPOTLIGHT_GRID_SPACING";
    // CONF KEY
    public final static String PERFORM_FEASIBILITY_IN_THE_PAST_CONF_KEY = "PERFORM_FEASIBILITY_IN_THE_PAST";
    // CONF KEY
    public final static String DEM_BASE_DIR_PATH_CONF_KEY = "DEM_BASE_DIR_PATH";
    // path for Feasibility
    public final static String XSD_PATH_CONF_KEY = "XSD_PATH";
    // CONF KEY
    public final static String MULTI_MISSION_CHECK_CONFLICT_XSD_PATH_CONF_KEY = "MULTI_MISSION_CHECK_CONFLICT_XSD_PATH";

    // CONF KEY
    public final static String NUMBER_OF_OUTER_ITERATION_CONF_KEY = "NUMBER_OF_OUTER_ITERATION";
    // CONF KEY
    public final static String MINIMAL_PR_VALIDITY_DURATION_CONF_KEY = "MINIMAL_PR_VALIDITY_DURATION";
    // CONF KEY
    public final static String MAX_PR_VALIDITY_DURATION_CONF_KEY = "MAX_PR_VALIDITY_DURATION";
    // CONF KEY
    public final static String LONGITUDE_LIMIT_TO_UNDERSTAND_FOR_LINE_DATE_CONF_KEY = "LONGITUDE_LIMIT_TO_UNDERSTAND_FOR_LINE_DATE";

    // public final static String CSK_SAT_ORBIT_INCLINATION_CONF_KEY
    // ="CSK_SAT_ORBIT_INCLINATION";

    // public final static String CSK_SAT_MEAN_MOTION_CONF_KEY
    // ="CSK_SAT_MEAN_MOTION";
    // CONF KEY
    public final static String HAVE_DENSIFY_AREA_PERIMETER_CONF_KEY = "HAVE_DENSIFY_AREA_PERIMETER";
    // CONF KEY
    public final static String PERIMETER_DENSIFIER_TOLERANCE_CONF_KEY = "PERIMETER_DENSIFIER_TOLERANCE";
    // CONF KEY
    public final static String HAVE_DENSIFY_LINE_CONF_KEY = "HAVE_DENSIFY_LINE";
    // CONF KEY
    public final static String LINE_DENSIFIER_TOLERANCE_CONF_KEY = "LINE_DENSIFIER_TOLERANCE";
    // CONF KEY
    public final static String HAVE_USE_ONLY_NEW_ACCESS_CONF_KEY = "HAVE_USE_ONLY_NEW_ACCESS";
    // CONF KEY
    public final static String HAVE_OPTIMIZE_TIMELINE_CONF_KEY = "HAVE_OPTIMIZE_TIMELINE";
    // CONF KEY
    public final static String HAVE_CHECK_CONFLICT_CONF_KEY = "HAVE_CHECK_CONFLICT";

    // configuration key for optimization algo
    public final static String OP1_CONF_KEY = "OP1";
    public final static String OP2_CONF_KEY = "OP2";
    public final static String OP3_CONF_KEY = "OP3";
    // CONF KEY
    public final static String IP1_CONF_KEY = "IP1";
    public final static String IP2_CONF_KEY = "IP2";
    public final static String IP3_CONF_KEY = "IP3";

    // configuration key for repeatible run
    public final static String USE_REPEATIBLE_RUN_CONF_KEY = "USE_REPEATIBLE_RUN";

    // Polar region limit

    public final static String POLAR_LIMIT_CONF_KEY = "POLAR_LIMIT";

    // Interferometric parametrs
    // CONF KEY
    public final static String INTERFEROMETRIC_MISSIONS_CONF_KEY = "INTERFEROMETRIC_MISSION";
    // CONF KEY
    public final static String DECORRELATION_TOLERANCE_CONF_KEY = "DECORRELATION_TOLERANCE";

    // EPEHEM REFRESH TIME
    public final static String ODBATA_REFRESH_TIME_CONF_KEY = "ODBATA_REFRESH_TIME";

    // CONF KEY
    public final static String HAVE_CHECK_FOR_HOLES_CONF_KEY = "HAVE_CHECK_FOR_HOLES";
    // CONF KEY
    public final static String NOT_ALLOWED_HOLE_AREA_RATIO_CONF_KEY = "NOT_ALLOWED_HOLE_AREA_RATIO";

    // CONF KEY
    public final static String EXTRA_THRESHOLD_GUARD_CONF_KEY = "EXTRA_THRESHOLD_GUARD";
    // CONF KEY
    public final static String HAVE_USE_SPARC_CONF_KEY = "HAVE_USE_SPARC";
    // CONF KEY
    public final static String SPARC_MODE_CONF_KEY = "SPARC_MODE";
    // CONF KEY
    public final static String SPARC_INSTALLATION_DIR_CONF_KEY = "SPARC_INSTALLATION_DIR";
    // CONF KEY
    public final static String SPARC_FEASIBILITY_OUT_SCHEMA_CONF_KEY = "SPARC_FEASIBILITY_OUT_SCHEMA";
    // CONF KEY
    public final static String SPARC_REFINEMENT_OUT_SCHEMA_CONF_KEY = "SPARC_REFINEMENT_OUT_SCHEMA";

    // DWL SPEED CONG KEY
    public final static String DWL_SPEED_CONF_KEY = "DWL_SPEED";

    // CONFIGURATION KEY to transform point in square
    public final static String POINT_TO_SQUARE_DIMENSION_CONF_KEY = "POINT_TO_SQUARE_DIMENSION";

    // Postfix to append to name of the satellite in order to evaluate the track
    // number offset ptoperties for a satellite
    // For SAR1 we'll have SAR1__TRACK_NUMBER_OFFSET
    // public final static String TRACK_NUMBER_OFFSET_POSTFIX_CONF_KEY =
    // "_TRACK_NUMBER_OFFSET";
    // CONF KEY
    public final static long TRACK_NUMBER_MODULE_DIVISOR = 237;

    // STEREO PARAMETERS
    public final static String MIN_STEREO_COVERAGE_BETWEEN_STEREO_CONF_KEY = "MIN_STEREO_COVERAGE_BETWEEN_STEREO";

    // EXTENSION COVERAGE OPTIMIZATION
    public final static String HAVE_PERFORM_OPTIMIZATION_ON_EXTENSION_COVERAGE_CONF_HEY = "HAVE_PERFORM_OPTIMIZATION_ON_EXTENSION_COVERAGE";

    // Periodic constants
    // Error status if basic solution doesn't exist
    /*
     * public final static String DefaultErrorStringForPeriodicRepetitive=
     * "A solution exists for the Basic Validity range but it doesn't exist for one or more remaining Feasibility Ranges"
     * ;
     */
    // "A solution exists for the Basic Validity range but it doesn&apos;t exist
    // for one or more remaining Feasibility Ranges";
    public final static String FullErrorStringForPeriodicRepetitive = "A solution does not exist neither for the Basic Validity range nor the remaining Feasibility Ranges";

    public final static String ErrorOnNextRanges = "A solution exists for the Basic Validity range but it does not exist for one or more remaining Feasibility Ranges";

}// end class
