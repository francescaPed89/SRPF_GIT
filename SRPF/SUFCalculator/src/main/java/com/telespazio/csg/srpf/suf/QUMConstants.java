
/**
*
* MODULE FILE NAME:	QUMConstants.java
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

package com.telespazio.csg.srpf.suf;

/**
 * Constants used during SUF calculation Used only for CSK
 * 
 * @author Girolamo Castaldo
 * @version 1.0
 * 
 * 
 */

public class QUMConstants
{

    public static final String CSG_QUM_CONFIG_FILE_PATH_PARAM = "CSGQUMConfigFilePath";
    public static final String SENSOR_MODE_STRIPMAP = "STRIPMAP";

    public static final String QUM_CONFIG_FILE_PATH_PARAM = "QUMConfigFilePath";
    public static final int SUF_THERMAL_ENHANCEMENT = 0;
    public static final int SUF_TRANSMITTING_POWER = 1;
    public static final int SUF_DATA_TAKE_VOLUME = 2;
    public static final int SUF_DATA_TAKE_ACQUISITION_DURATION = 3;
    public static final int SUF_DOWNLINK_DURATION = 4;
    public static final int SUF_ON_BOARD_DATA_LATENCY = 5;
    public static final int SUF_ACQUISITION_OPPORTUNITY = 6;
    public static final int SUF_SLEW_MANOEUVRE_DURATION = 7;
    public static final int SUF_EXT_MANOEUVRE_DURATION = 8;
    public static final int SUF_GROUND_DATA_NETWORK_USAGE_DURATION = 9;
    public static final int SUF_PARAMETERS_NUM = 10;

    // Sensor Modes
    public static final String SENSOR_MODE_SP_ENHANCED = "SP_ENHANCED";
    public static final String SENSOR_MODE_STR_HIMAGE = "STR_HIMAGE";
    public static final String SENSOR_MODE_STR_PINGPONG = "STR_PINGPONG";
    public static final String SENSOR_MODE_SCN_WIDE = "SCN_WIDE";
    public static final String SENSOR_MODE_SCN_HUGE = "SCN_HUGE";

    // Look Side
    public static final String LOOK_SIDE_LEFT = "Left";
    public static final String LOOK_SIDE_RIGHT = "Right";

    // Beam
    public static final String BEAM_ES_30 = "ES-30";
    public static final String BEAM_ES_31 = "ES-31";
    public static final String BEAM_ES_32 = "ES-32";
    public static final String BEAM_ES_33 = "ES-33";
    public static final String BEAM_ES_34 = "ES-34";
    public static final String BEAM_ES_35 = "ES-35";
    public static final String BEAM_H4_0A = "H4-0A";

    // K Factors
    public static final String K_FACTOR_THERMAL_ENHANCEMENT_NARROW_PARAM = "KFactorThermalEnhancementNarrow";
    public static final String K_FACTOR_THERMAL_ENHANCEMENT_WIDE_PARAM = "KFactorThermalEnhancementWide";
    public static final String K_FACTOR_TRANSMITTING_POWER_NARROW_PARAM = "KFactorTransmittingPowerNarrow";
    public static final String K_FACTOR_TRANSMITTING_POWER_WIDE_PARAM = "KFactorTransmittingPowerWide";
    public static final String K_FACTOR_DATA_TAKE_VOLUME_NARROW_PARAM = "KFactorDataTakeVolumeNarrow";
    public static final String K_FACTOR_DATA_TAKE_VOLUME_WIDE_PARAM = "KFactorDataTakeVolumeWide";
    public static final String K_FACTOR_DATA_TAKE_ACQUISITION_DURATION_NARROW_PARAM = "KFactorDataTakeAcquisitionDurationNarrow";
    public static final String K_FACTOR_DATA_TAKE_ACQUISITION_DURATION_WIDE_PARAM = "KFactorDataTakeAcquisitionDurationWide";
    public static final String K_FACTOR_DOWNLINK_DURATION_NARROW_PARAM = "KFactorDownlinkDurationNarrow";
    public static final String K_FACTOR_DOWNLINK_DURATION_WIDE_PARAM = "KFactorDownlinkDurationWide";
    public static final String K_FACTOR_ON_BOARD_DATA_LATENCY_NARROW_PARAM = "KFactorOnBoardDataLatencyNarrow";
    public static final String K_FACTOR_ON_BOARD_DATA_LATENCY_WIDE_PARAM = "KFactorOnBoardDataLatencyWide";
    public static final String K_FACTOR_ACQUISITION_OPPORTUNITY_NARROW_PARAM = "KFactorAcquisitionOpportunityNarrow";
    public static final String K_FACTOR_ACQUISITION_OPPORTUNITY_WIDE_PARAM = "KFactorAcquisitionOpportunityWide";
    public static final String K_FACTOR_SLEW_MANOEUVRE_DURATION_NARROW_PARAM = "KFactorSlewManoeuvreDurationNarrow";
    public static final String K_FACTOR_SLEW_MANOEUVRE_DURATION_WIDE_PARAM = "KFactorSlewManoeuvreDurationWide";
    public static final String K_FACTOR_EXT_MANOEUVRE_DURATION_NARROW_PARAM = "KFactorExtManoeuvreDurationNarrow";
    public static final String K_FACTOR_EXT_MANOEUVRE_DURATION_WIDE_PARAM = "KFactorExtManoeuvreDurationWide";
    public static final String K_FACTOR_GROUND_DATA_NETWORK_USAGE_DURATION_NARROW_PARAM = "KFactorGroundDataNetworkUsageDurationNarrow";
    public static final String K_FACTOR_GROUND_DATA_NETWORK_USAGE_DURATION_WIDE_PARAM = "KFactorGroundDataNetworkUsageDurationWide";

    // Effective Image
    public static final String EFFECTIVE_THERMAL_ENHANCEMENT_NARROW_PARAM = "EffectiveThermalEnhancementNarrow";
    public static final String EFFECTIVE_THERMAL_ENHANCEMENT_WIDE_PARAM = "EffectiveThermalEnhancementWide";
    public static final String EFFECTIVE_ENERGY_ISSUED_NARROW_PARAM = "EffectiveEnergyIssuedNarrow";
    public static final String EFFECTIVE_ENERGY_ISSUED_WIDE_PARAM = "EffectiveEnergyIssuedWide";
    public static final String EFFECTIVE_CALIBRATION_POWER_NARROW_PARAM = "EffectiveCalibrationPowerNarrow";
    public static final String EFFECTIVE_CALIBRATION_POWER_WIDE_PARAM = "EffectiveCalibrationPowerWide";
    public static final String EFFECTIVE_DATA_VOLUME_SP_ENHANCED_PARAM = "EffectiveDataVolumeSP_ENHANCED";
    public static final String EFFECTIVE_DATA_VOLUME_STR_HIMAGE_PARAM = "EffectiveDataVolumeSTR_HIMAGE";
    public static final String EFFECTIVE_DATA_VOLUME_STR_PINGPONG_PARAM = "EffectiveDataVolumeSTR_PINGPONG";
    public static final String EFFECTIVE_DATA_VOLUME_SCN_WIDE_PARAM = "EffectiveDataVolumeSCN_WIDE";
    public static final String EFFECTIVE_DATA_VOLUME_SCN_HUGE_PARAM = "EffectiveDataVolumeSCN_HUGE";
    public static final String SENSING_TIME_HEAD_SP_ENHANCED_PARAM = "SensingTimeHeadSP_ENHANCED";
    public static final String SENSING_TIME_HEAD_STR_HIMAGE_PARAM = "SensingTimeHeadSTR_HIMAGE";
    public static final String SENSING_TIME_HEAD_STR_PINGPONG_PARAM = "SensingTimeHeadSTR_PINGPONG";
    public static final String SENSING_TIME_HEAD_SCN_WIDE_PARAM = "SensingTimeHeadSCN_WIDE";
    public static final String SENSING_TIME_HEAD_SCN_HUGE_PARAM = "SensingTimeHeadSCN_HUGE";
    public static final String SENSING_TIME_TRAIL_SP_ENHANCED_PARAM = "SensingTimeTrailSP_ENHANCED";
    public static final String SENSING_TIME_TRAIL_STR_HIMAGE_PARAM = "SensingTimeTrailSTR_HIMAGE";
    public static final String SENSING_TIME_TRAIL_STR_PINGPONG_PARAM = "SensingTimeTrailSTR_PINGPONG";
    public static final String SENSING_TIME_TRAIL_SCN_WIDE_PARAM = "SensingTimeTrailSCN_WIDE";
    public static final String SENSING_TIME_TRAIL_SCN_HUGE_PARAM = "SensingTimeTrailSCN_HUGE";

    // Standard Image
    public static final String STANDARD_THERMAL_ENHANCEMENT_NARROW_PARAM = "StandardThermalEnhancementNarrow";
    public static final String STANDARD_THERMAL_ENHANCEMENT_WIDE_PARAM = "StandardThermalEnhancementWide";
    public static final String STANDARD_ENERGY_ISSUED_NARROW_PARAM = "StandardEnergyIssuedNarrow";
    public static final String STANDARD_ENERGY_ISSUED_WIDE_PARAM = "StandardEnergyIssuedWide";
    public static final String STANDARD_CALIBRATION_POWER_NARROW_PARAM = "StandardCalibrationPowerNarrow";
    public static final String STANDARD_CALIBRATION_POWER_WIDE_PARAM = "StandardCalibrationPowerWide";
    public static final String STANDARD_DATA_VOLUME_NARROW_PARAM = "StandardDataVolumeNarrow";
    public static final String STANDARD_DATA_VOLUME_WIDE_PARAM = "StandardDataVolumeWide";
    public static final String STANDARD_DURATION_NARROW_PARAM = "StandardDurationNarrow";
    public static final String STANDARD_DURATION_WIDE_PARAM = "StandardDurationWide";

    // UGS Profile
    public static final String DEFAULT_DATA_LATENCY_PARAM = "DefaultDataLatency";
    public static final String OTHER_DATA_LATENCY_PARAM = "OtherDataLatency";

    // Constants
    public static final String ZONE_1_COST_PARAM = "Zone1Cost";
    public static final String ZONE_2_COST_PARAM = "Zone2Cost";
    public static final String SLEW_MANOEUVRE_DURATION_PARAM = "SlewManoeuvreDuration";
    public static final String MAXIMUM_ROLL_TIME_PARAM = "MaximumRollTime";
    public static final String DAY_DATA_VOLUME_PARAM = "DayDataVolume";
}// end class
