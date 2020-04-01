/**
*
* MODULE FILE NAME:	DSMTReporterConstants.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Provide constants used in  DSMTReporter
*
* PURPOSE:		    Provide constants used in  DSMTReporter
*
* CREATION DATE:	03-10-2017
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

package com.telespazio.csg.srpf.dsmt;

/**
 * Provide constants used in DSMTReporter
 * 
 * @author Amedeo Bancone
 * @version 1.0
 *
 */
public class DSMTReporterConstants

{

    // Security constants
    private static String OperationalSecurity = "Operational";
    // Security constants
    private static String NotOperationalSecurity = "NotOperational";

    // ALLOWED Security flag enumeration
    public enum SecurityEnum
    {
        OPERATIONAL, NOTOPERATIONAL;
    }// end enum

    // Confidential flag values
    private static String ConfidentianFlagValue = "C";
    // Confidential flag values
    private static String NotConfidentianFlagValue = "NC";

    // ConfidentiAL FLAG ENUMERATION
    public enum ConfidentialFlag
    {
        C, NC;
    }// END ENUM

    // Confidential levels for report
    private static String UnclissifiedReport = "unclassified";
    // Confidential levels for report
    private static String RestrictedReport = "restricted";
    // Confidential levels for report
    private static String ConfidentialReport = "confidential";
    // Confidential levels for report
    private static String SecretReport = "secret";
    // Confidential levels for report
    private static String TopSecretReport = "topSecret";

    // Confidential level enum
    public static enum ConfidentialLevel
    {
        UNCLASSIFIED, RESTRICTED, CONFIDENTIAL, SECRET, TOPSECRET;
    }// end enum

    // Report Types
    public static String InformationReportType = "INFORMATION";
    // Report Types
    public static String WarningReportType = "WARNING";
    // Report Types
    public static String ErrorReportType = "ERROR";

    // Report type enumaration
    public enum ReportType
    {
        INFORMATION, WARNING, ERROR;
    }// end enum

    // SeverityValue
    private static int noSeverity = 4;
    // SeverityValue
    private static int low = 3;
    // SeverityValue
    private static int high = 2;
    // SeverityValue
    private static int veryHigh = 1;

    // Severity enum
    public enum Severity
    {
        NOSEVERITY, LOW, HIGH, VERIHIGH;
    }// end enum

    /**
     * Return the string of Security given the enum value
     * 
     * @param sec
     * @return string of security
     */
    public static String getSecurity(SecurityEnum sec)
    {
        // value to return
        String value = NotOperationalSecurity;
        // default NotOperationalSecurity
        switch (sec)
        {
            case OPERATIONAL:
                value = OperationalSecurity; // operational
                break;

            default:
                break;
        }// end switch
        return value;
    }// End Method

    /**
     * Return the string of Confidential Flag given the enum value
     * 
     * @param sec
     * @return a string of confidentioal flag
     */
    public static String getConfidentialFlag(ConfidentialFlag flag)
    {
        // return string
        String value = NotConfidentianFlagValue;
        // default not coinfidential
        switch (flag)
        {
            case C:
                value = ConfidentianFlagValue;
                break;

            default:
                break;
        }// end switch
         // returinig
        return value;
    }// End Method

    /**
     * Return the string of Confidential level given the enum value
     * 
     * @param flag
     * @return string of Confidential level
     */
    public static String getConfidentialLevel(ConfidentialLevel flag)
    {
        // retval
        String value = TopSecretReport;
        // default TopSecret
        switch (flag)
        {
            case UNCLASSIFIED:
                value = UnclissifiedReport; // unclissuified
                break;
            case RESTRICTED:
                value = RestrictedReport; // restricted
                break;
            case CONFIDENTIAL:
                value = ConfidentialReport; // confidential
                break;
            case SECRET:
                value = SecretReport; // secret
                break;
            default:
                break;
        }// end switch
        return value;
    }// End Method

    /**
     * Return the string of Report Type given the enum value
     * 
     * @param flag
     * @return string of Report Type
     */
    public static String getReportType(ReportType flag)
    {
        // return value
        String value = InformationReportType;
        // default InformationReportType
        switch (flag)
        {
            case WARNING:
                value = WarningReportType; // warning
                break;
            case ERROR:
                value = ErrorReportType; // error
                break;
            default:
                break;
        }// end switch
        return value;
    }// End Method

    /**
     * Return the severity level, given the severity flag
     * 
     * @param flag
     * @return severity level
     */
    public static int getSeverity(Severity flag)
    {
        // retval
        int retval = noSeverity;
        // default no severity
        switch (flag)
        {
            case LOW:
                retval = low; // low
                break;
            case HIGH:
                retval = high; // high
                break;
            case VERIHIGH:
                retval = veryHigh; // very high
                break;
            default:
                break;
        }// end switch

        return retval;
    }// end method

    // Configuration key name
    // component id
    public static String ComponentIdForDSMTReporter_Conf_KEY = "ComponentIdForDSMTReporter";
    // site
    public static String SiteForDSMTReporter_Conf_KEY = "SiteForDSMTReporter";
    // component
    public static String SubsystemNameForDSMTReporter_Conf_KEY = "SubsystemNameForDSMTReporter";
    // component
    public static String SubsystemComponentForDSMTReporter_Conf_KEY = "SubsystemComponentForDSMTReporter";
    // type
    public static String MinimumReportType_Conf_KEY = "MinimumReportType";
    // address
    public static String DSMTCmAddress_Conf_Key = "DSMTCmAddress";
    // class
    public static String DSMTEventReportMSGClass_Conf_Key = "DSMTEventReportMSGClass";

    // CODE
    public static String ODSTPReceived = "004";
    // CODE
    public static String ODMTPReceived = "003";
    // CODE
    public static String ODNOMReceived = "002";
    // CODE
    public static String ODREFReceived = "001";

} // end Class
