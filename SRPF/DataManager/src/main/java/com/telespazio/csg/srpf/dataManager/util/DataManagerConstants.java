/**
*
* MODULE FILE NAME:	DataManagerConstants.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Define list of constants
*
* PURPOSE:
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

package com.telespazio.csg.srpf.dataManager.util;

/**
 * Define list of constants
 * 
 * @author Abed Alissa
 * @version 1.0
 *
 */
public class DataManagerConstants
{

    /** Opposite of {@link #FAILS}. */
    public static final boolean PASSES = true;
    /** Opposite of {@link #PASSES}. */
    public static final boolean FAILS = false;

    /** Opposite of {@link #FAILURE}. */
    public static final boolean SUCCESS = true;
    /** Opposite of {@link #SUCCESS}. */
    public static final boolean FAILURE = false;

    /**
     * Useful for {@link String} operations, which return an index of
     * <tt>-1</tt> when an item is not found.
     */
    public static final int NOT_FOUND = -1;

    /** System property - <tt>line.separator</tt> */
    public static final String NEW_LINE = System.getProperty("line.separator");
    /** System property - <tt>file.separator</tt> */
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    /** System property - <tt>path.separator</tt> */
    public static final String PATH_SEPARATOR = System.getProperty("path.separator");

    // String constant
    public static final String EMPTY_STRING = "";
    // String constant
    public static final String SPACE = " ";
    // String constant
    public static final String TAB = "\t";
    // String constant
    public static final String SINGLE_QUOTE = "'";
    // String constant
    public static final String PERIOD = ".";
    // String constant
    public static final String DOUBLE_QUOTE = "\"";

    /** Data Type - <tt>line.separator</tt> */
    // Data type definition
    public static final int TYPE_ODSTP = 1;
    // Data type definition
    public static final int TYPE_ODMTP = 2;
    // Data type definition
    public static final int TYPE_ODNOM = 3;
    // Data type definition
    public static final int TYPE_ODREF = 4;

    // look angle side
    public static final int ID_ALLOWED_lOOK_NONE = 0;
    // look angle side
    public static final int ID_ALLOWED_lOOK_RIGHT = 1;
    // look angle side
    public static final int ID_ALLOWED_lOOK_LEFT = 2;
    // look angle side
    public static final int ID_ALLOWED_lOOK_BOTH = 3;
    // look angle side
    public static final String ALLOWED_lOOK_RIGHT = "Right";
    // look angle side
    public static final String ALLOWED_lOOK_LEFT = "Left";
    // look angle side
    public static final String ALLOWED_lOOK_BOTH = "Both";

    // enabbling
    public static final int ENABLED = 1;
    // disabling
    public static final int DISABLED = 0;

    public static final int TIMES_REPEATED_EPOCHS = 23;
    // CSK Period
    public static final int DAYS_REPEATED_EPOCHS = 16;
    // KM
    public static final int KM = 1000;
    // String constant
    public static final String CONFIGURATION_IMPORT = "import";
    // look angle side
    public static final String CONFIGURATION_EXPORT = "export";
    // look angle side
    public static final String CONFIGURATION_UPDATE = "update";
    // look angle side
    public static final String CONFIGURATION_DELETE = "delete";
    // Flag in db
    public static final int DEFERRABLE_FLAG_TRUE = 1;
    // period tolerance
    public static final long PERIOD_TOLLERANCE_TYPE_ODSTP = 120;
    // period tolerance
    public static final long PERIOD_TOLLERANCE_TYPE_ODMTP = 120;
    // period tolerance
    public static final long PERIOD_TOLLERANCE_TYPE_ODNOM = 120;
    // ODSTP tolerance
    public static final long MAX_TOLLERANCE_TYPE_ODSTP = 10;
    // ODMTP tolerance
    public static final long MAX_TOLLERANCE_TYPE_ODMTP = 10;
    // ODNOM TOLERANCE
    public static final long MAX_TOLLERANCE_TYPE_ODNOM = 60;

    // Sampling rate for orbital data in seconds
    // odstp
    public final static long OdstpSamplingRate = 10;
    // odmtp
    public final static long OdmtpSamplingRate = 10;
    // odnom
    public final static long OdnomSamplingRate = 60;
    // odref
    public final static long OdrefSamplingRate = 60;

    // Properties name for the Allocatipon plan XSD in the properties file
    public final static String ALLOC_PLAN_XSD_CONF_KEY_NAME = "ALLOC_PLAN_XSD";
    // SOE XSD PATH conf key
    public final static String SOE_XSD_CONF_KEY_NAME = "SOE_XSD";

    // Satellite pass tagName
    public final static String satellitePassTagName = "SatellitePass";
    // XML TAG
    public final static String isAllocatedStringTrue = "TRUE";
    // XML TAG
    public final static String isAllocatedTagNane = "IsAllocated";
    // XML TAG
    public final static String ASIDTagName = "ASId";
    // XML TAG
    public final static String ContactCounterTagName = "ContactCounter";
    // XML TAG
    public final static String VisibilityStartTimeTagName = "VisibilityStartTime";
    // XML TAG
    public final static String VisibilityStopTimeTagName = "VisibilityStopTime";
    // XML TAG
    public final static String SatelliteIdTagName = "SatelliteId";

    // XML TAG
    public final static String SatelliteSoeTagName = "SatelliteSOE";
    // XML TAG
    public final static String SatelliteIdValueAttr = "Value";
    // XML TAG
    public final static String EventInfoTagName = "EventInfo";
    // XML TAG
    public final static String EventStartTimeTagName = "EventStartTime";
    // XML TAG
    public final static String EventStopTimeTagName = "EventStopTime";
    // XML TAG
    public final static String XExtAngleTagName = "XExtAngle";
    // XML TAG
    public final static String EventTypeTagName = "EventType";
    // XML TAG
    public final static String EventTypeValueAttr = "Value";
    // XML TAG
    public final static String EventType_X_PASS_AttrValue = "X_PASS";

    // XML TAG
    public final static String EventCounterTagName = "EventCounter";

    // XML TAG
    public final static String StationNameTagName = "StationName";

    // PAW
    public final static String OCCULTED_PAW_TYPE = "STTOCCULTATION";

    // SOE DUALSAR
    // common namespace
    public final static String commonNS = "http://www.telespazio.com/IOP/schemas/common";
    // SOE Namespace
    public final static String soeNS = "http://www.telespazio.com/SDC/schemas/soe";
    // TagName
    public final static String SatelliteEventTagName = "SatelliteEvent";
    // NS
    public final static String SatelliteEventNS = soeNS;
    // TAGNAME
    public final static String EventTypeSOETagName = "eventType";
    // NS
    public final static String EventTypeSOENS = soeNS;

    // XML VALUE
    public final static String EventType_X_PASS_VALUE = "X_PASS";

    // TagName
    public final static String SatelliteTagName = "satellite";
    // NS
    public final static String SatelliteTagNameNS = commonNS;
    // TagName
    public final static String StationIDTagName = "stationId";
    // NS
    public final static String StationIDTagNameNS = soeNS;
    // TagName
    public final static String SOEEventCounterTagName = "eventCounter";
    // NS
    public final static String SOEEventCounterTagNameNS = soeNS;

    // TagName
    public final static String SOEExtAngleTagName = "XExtAngle";
    // NS
    public final static String SOEExtAngleTagNameNS = soeNS;

    // TagName
    public final static String SOEEventStartTagName = "timeStart";
    // NS
    public final static String SOEEventStartTagNameNS = commonNS;

    // TagName
    public final static String SOEEventStopTagName = "timeStop";
    // NS
    public final static String SOEEventStopTagNameNS = commonNS;

    // SATPASS

    // NS
    public final static String satPassNS = "http://www.telespazio.com/IOP/schemas/satpass";
    // TAG
    public final static String SatellitePassTagName = "SatellitePass";
    // NS
    public final static String SatellitePassTagNameNS = satPassNS;

    // TAGNAME
    public final static String ContactCounterSatPassTagName = "contactCounter";
    // NS
    public final static String ContactCounterSatPassTagNameNS = commonNS;
    // TAGNAME
    public final static String AcquisitionStationTagName = "acquisitionStation";
    // NS
    public final static String AcquisitionStationTagNameNS = commonNS;

    // TAGNAME
    public final static String SatPassTypeTagName = "passType";
    // NS
    public final static String SatPassTypeTagNameNS = satPassNS;
    // value
    public final static String SatPassTypeX_PassValue = "X_PASS";

    // TAGNAME
    public final static String AllocatedContactTagName = "allocatedContact";
    // NS
    public final static String AllocatedContactTagNameNS = satPassNS;
    // true
    public final static String AllocatedContactTrueValue = "true";

    // TAGNAME
    public final static String SatPassTimeStartTagName = "timeStart";
    // NS
    public final static String SatPassTimeStartTagNameNS = commonNS;

    // TAGNAME
    public final static String SatPassTimeStopTagName = "timeStop";
    // NS
    public final static String SatPassTimeStopTagNameNS = commonNS;

    // TAGNAME
    public final static String SatPassSatelliteTagName = "satellite";
    // NS
    public final static String SatPassSatelliteTagNameNS = commonNS;

}// end class
