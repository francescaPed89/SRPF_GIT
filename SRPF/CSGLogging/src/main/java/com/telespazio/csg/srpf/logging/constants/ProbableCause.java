/**
*
* MODULE FILE NAME:	ProbableCause.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Define constants to define probable cause in log
*
* PURPOSE:			Used for logging purposes 
*
* CREATION DATE:	13-01-2016
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

package com.telespazio.csg.srpf.logging.constants;

/**
 * Define constants to define probable cause in log
 * @author Girolamo Castaldo
 * @version 1.0
 *
 */
public class ProbableCause 
{
	public static final String APPLICATION_SUBSYSTEM_FAILURE = "0002"; // Application subsystem failure
	public static final String CALL_ESTABLISHMENT_ERROR = "0004"; // Call establishment error
	public static final String COMMUNICATIONS_PROTOCOL_ERROR = "0005"; // Communications protocol error
	public static final String COMMUNICATIONS_SUBSYSTEM_FAILURE = "0006"; // Communications subsystem failure
	public static final String CONFIGURATION_OR_CUSTOMIZATION_ERROR = "0007"; // Configuration or customization error
	public static final String CORRUPT_DATA = "0009"; // Corrupt data error
	public static final String DEGRADED_SIGNAL = "0012"; // Degraded signal error
	public static final String DTE_DCE_INTERFACE_ERROR = "0013"; // DTE-DCE interface error
	public static final String FILE_ERROR = "0017"; // File error
	public static final String FRAMING_ERROR = "0020"; // Framing error
	public static final String LAN_ERROR = "0025"; // LAN error
	public static final String LOCAL_NODE_TRANSMISSION_ERROR = "0027"; // Local node transmission error
	public static final String LOSS_OF_FRAME = "0028"; // Loss of frame error
	public static final String LOSS_OF_SIGNAL = "0029"; // Loss of signal error
	public static final String OUT_OF_MEMORY = "0032"; // Out of memory error
	public static final String REMOTE_NODE_TRANSMISSION_ERROR = "0042"; // Remote node transmission error
	public static final String STORAGE_CAPACITY_PROBLEM = "0049"; // Storage capacity error
	public static final String SOFTWARE_ERROR = "0046"; // Software error
	public static final String SOFTWARE_PROGRAM_ABNORMALLY_TERMINATED = "0047"; // Program abnormally terminated error
	public static final String SOFTWARE_PROGRAM_ERROR = "0048"; // Program error
	public static final String UNDERLYING_RESOURCE_UNAVAILABLE = "0056"; // Underlying resource unavailable error
	public static final String PLA_MMC_APPLICATION_INFO = "0200"; // PLA-MMC application info
	public static final String INFORMATION_INFO = "0500"; // Information info
	public static final String PROCESS_STATE_CHANGE = "1000"; // Process state change
	public static final String HOST_STATE_CHANGE = "1001"; // Host state change
	public static final String COMPONENT_STATE_CHANGE = "1002"; // Component state change
	public static final String SUBSYSTEM_STATE_CHANGE = "1003"; // Subsystem state change
	public static final String ELEMENT_STATE_CHANGE = "1004"; // Element state change
	public static final String CPU_PROBLEM = "2000"; // CPU problem error
	// Security Events
	public static final String SYSTEM_STARTUP = "3000"; // System startup event
	public static final String SYSTEM_SHUTDOWN = "3001"; // System shutdown event
	public static final String SYSTEM_RESTART = "3002"; // System restart event
	public static final String AUTHENTICATION_FAILURE = "3003"; // Application failure event
	public static final String BREACH_OF_CONFIDENTIALITY = "3004"; // Breach of confidentiality event
	public static final String CABLE_TAMPER = "3005"; // Cable tamper event
	public static final String DELAYED_INFORMATION = "3006"; // Delayed information event
	public static final String DENIAL_OF_SERVICE = "3007"; // Denial of service event
	public static final String DUPLICATE_INFORMATION = "3008"; // Duplicate information event
	public static final String INFORMATION_MISSING = "3009"; // Information missing event
	public static final String INFORMATION_MODIFICATION_DETECTED = "3010"; // Information modification detected event
	public static final String INFORMATION_OUT_OF_SEQUENCE = "3011"; // Information out of sequence event
	public static final String INTRUSION_DETECTION = "3012"; // Intrusion detection event
	public static final String KEY_EXPIRED = "3013"; // Key expired event
	public static final String NON_REPUDIATION_FAILURE = "3014"; //Non repudiation failure event
	public static final String OUT_OF_HOURS_ACTIVITY = "3015"; // Out of hours activity event
	public static final String OUT_OF_SERVICE = "3016"; // Out of service event
	public static final String PROCEDURAL_ERROR = "3017"; // Procedural error event
	public static final String UNAUTHORIZED_ACCESS_ATTEMPT = "3018"; // Unauthorized access attempt event
	public static final String UNEXPECTED_INFORMATION = "3019"; // Unexpected information event
	public static final String UNSPECIFIED_REASON = "3020"; // Unspecified reason event
	public static final String CHECKSUM_VERIFICATION_FAILURE = "3021"; // Checksum verification failure event
	public static final String SECURITY_EVENT_INFO = "4010"; // Security event info
	public static final String CRYPTO_DEVICE_ERROR = "4020"; // Crypto device error event
} // end class
