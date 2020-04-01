/**
*
* MODULE FILE NAME:	DSMTReporter.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Singleton Class used to report events to DSMT
*
* PURPOSE:			USED send reports to DSTM
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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.DateUtils;
import com.telespazio.csg.srpf.utils.PropertiesReader;

import CM.CMAPI.CME_MessageType;
import CM.CMAPI.CME_TransferMode;
import CM.CMAPI.CM_Envelope;
import CM.CMAPI.CM_Exception;
import CM.CMAPI.CM_Message;
import CM.CMAPI.CM_Services;

/**
 * Singleton Class used to send report to DSMT System
 * 
 * @author Amedeo Bancone
 * @version 1.0
 *
 *
 */
public class DSMTReporter

{

    // log
    TraceManager tracer = new TraceManager();

    // CM Component ID used in report xml
    private String cmComponentId = "";

    // CM Component name: used as sender in cm Message envelope
    private String componentName = "";
    // DSTM cm address
    private String dsmtCmAddress = "";
    // list of receivers
    private String[] receivers = new String[1];
    // class
    private String eventReportMsgClass = "EventReport";

    // Minimum report type
    private DSMTReporterConstants.ReportType minimumReportType = DSMTReporterConstants.ReportType.INFORMATION;

    // Site name to be inserted in the Report
    private String site = "Not configured";

    // Name of the subsystem generating report
    private String subsystemName = "Not Configured";

    // Name of the component generating report
    private String subsystemComponent = "Not Configured";

    Lock lock = new ReentrantLock();

    /**
     * return the instance of Class
     * 
     * @return instance
     */
    public static DSMTReporter getInstance()
    {
        return DSMTReporterHelper.INSTANCE;
    }// end method

    /**
     * Send a report to DSMT
     * 
     * @param sec
     * @param confFlag
     * @param confLevel
     * @param severity
     * @param reportTipe
     * @param eventCode
     * @param description
     * @param cmServ
     * @return the string of sent report
     */
    public String sendReport(DSMTReporterConstants.SecurityEnum sec, DSMTReporterConstants.ConfidentialFlag confFlag, DSMTReporterConstants.ConfidentialLevel confLevel, DSMTReporterConstants.Severity severity, DSMTReporterConstants.ReportType reportTipe, String eventCode, String description, CM_Services cmServ)
    {

        String report = "";
        if (reportTipe.compareTo(this.minimumReportType) >= 0)
        {
            // Buildoing report
            report = buildReport(DSMTReporterConstants.getSecurity(sec), DSMTReporterConstants.getConfidentialFlag(confFlag), DSMTReporterConstants.getConfidentialLevel(confLevel), DSMTReporterConstants.getSeverity(severity), DSMTReporterConstants.getReportType(reportTipe), eventCode, description);

            // acquiring lock
            this.lock.lock();
            try
            {
                // Buildong enevelope
                CM_Envelope envelop = new CM_Envelope();

                envelop.SetSender(this.componentName); // setting sender
                envelop.SetReceivers(this.receivers); // setting receiver
                CM_Message msg = new CM_Message(); // create message
                msg.SetClass(this.eventReportMsgClass); // set class
                envelop.SetTransferMode(CME_TransferMode.CMC_OneWay); // set
                                                                      // transfre
                                                                      // type
                msg.SetType(CME_MessageType.CMC_RequestMessage); // set request
                                                                 // type
                // building body
                msg.SetBody(report.getBytes().length, report.getBytes());
                msg.SetEnvelope(envelop);
                this.tracer.log("Sending EventReport to: " + this.receivers[0]);
                try
                {
                    cmServ.SendMessage(msg); // sending message
                } // end try
                catch (CM_Exception cme)
                {
                    // just log
                    this.tracer.major(EventType.COMMUNICATION_EVENT, ProbableCause.REMOTE_NODE_TRANSMISSION_ERROR, "CM Exception: " + cme.getMessage());

                } // end catch

            } // end try
            finally
            {
                // releasing lock
                this.lock.unlock();
            } // end finally
        } // end if

        // returning report
        return report;

    }// end method

    /**
     * Build the xml string
     * 
     * @param security
     * @param confidentialFlag
     * @param confidentialLevel
     * @param severity
     * @param reportType
     * @param eventCode
     * @param description
     * @return string holding the report
     */
    private String buildReport(String security, String confidentialFlag, String confidentialLevel, int severity, String reportType, String eventCode, String description

    )
    {
        // evaluate creation time
        String creationTime = DateUtils.getCurrentDateInISOFMT();
        // building XML header
        // xml version
        String report = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        // xml main tag
        report = report + "\n<EventReport xsi:schemaLocation=\"http://www.telespazio.com/CSG/schemas/reports/dsmt DSMTEvent.xsd\" xmlns=\"http://www.telespazio.com/CSG/schemas/reports/dsmt\" xmlns:IOPcm=\"http://www.telespazio.com/IOP/schemas/common\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";

        // adding header
        report = report + "\n<Header>";
        // originator
        report = report + "\n<IOPcm:originator>" + this.cmComponentId + "</IOPcm:originator>";
        // creation time
        report = report + "\n<IOPcm:creationTime>" + creationTime + "</IOPcm:creationTime>";
        // confidential info
        report = report + "\n<IOPcm:ConfidentialInfo>";
        // security level
        report = report + "\n<IOPcm:security>" + security + "</IOPcm:security>";
        // conf flag
        report = report + "\n<IOPcm:confidentialFlag>" + confidentialFlag + "</IOPcm:confidentialFlag>";
        // conf level
        report = report + "\n<IOPcm:confidentialLevel>" + confidentialLevel + "</IOPcm:confidentialLevel>";
        report = report + "\n</IOPcm:ConfidentialInfo>";
        // closing header
        report = report + "\n</Header>";

        // Adding event
        report = report + "\n<Event>";
        // site
        report = report + "\n<site>" + this.site + "</site>";
        // subsistem
        report = report + "\n<subsystem>" + this.subsystemName + "</subsystem>";
        // componenet
        report = report + "\n<component>" + this.subsystemComponent + "</component>";
        // creation
        report = report + "\n<time>" + creationTime + "</time>";
        // severity
        report = report + "\n<severity>" + severity + "</severity>";
        // type
        report = report + "\n<type>" + reportType + "</type>";
        // code
        report = report + "\n<code>" + eventCode + "</code>";
        // description
        report = report + "\n<description>" + description + "</description>";
        report = report + "\n</Event>";
        report = report + "\n</EventReport>";
        return report;
    }// end method

    /**
     * Private constructor
     */
    private DSMTReporter()
    {
        initialiaze();
    }// end method

    /**
     * 
     * Helper class used for the INSTANCE GENERATION
     *
     */
    private static class DSMTReporterHelper
    {

        private static DSMTReporter INSTANCE; // The single instance of
                                              // PropertiesReader
        static
        {
            INSTANCE = new DSMTReporter();
        }
    } // end class

    /**
     * Initialize the system
     */
    private void initialiaze()
    {
        // reading values from configuration
        String value = PropertiesReader.getInstance().getProperty(DSMTReporterConstants.ComponentIdForDSMTReporter_Conf_KEY);
        if (value != null)
        {

            this.cmComponentId = value;
        } // end if
        else
        {
            // just log
            this.tracer.critical(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + DSMTReporterConstants.ComponentIdForDSMTReporter_Conf_KEY + " in configuration. No report shall be sent to DSMT");

        } // end else
          // reading values from configuration
        value = PropertiesReader.getInstance().getProperty(DSMTReporterConstants.SiteForDSMTReporter_Conf_KEY);
        if (value != null)
        {

            this.site = value;
        } // end if
        else
        {
            // just log
            this.tracer.critical(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + DSMTReporterConstants.SiteForDSMTReporter_Conf_KEY + " in configuration");

        } // end else

        // reading values from configuration
        value = PropertiesReader.getInstance().getProperty(DSMTReporterConstants.SubsystemNameForDSMTReporter_Conf_KEY);
        if (value != null)
        {

            this.subsystemName = value;
        } // end if
        else
        {
            // just log
            this.tracer.critical(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + DSMTReporterConstants.SubsystemNameForDSMTReporter_Conf_KEY + " in configuration");

        } // end else
          // reading values from configuration
        value = PropertiesReader.getInstance().getProperty(DSMTReporterConstants.SubsystemComponentForDSMTReporter_Conf_KEY);
        if (value != null)
        {

            this.subsystemComponent = value;
        } // end if
        else
        {
            // just log
            this.tracer.critical(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + DSMTReporterConstants.SubsystemComponentForDSMTReporter_Conf_KEY + " in configuration");

        } // end else
          // reading values from configuration
        value = PropertiesReader.getInstance().getProperty(DSMTReporterConstants.MinimumReportType_Conf_KEY);
        if (value != null)
        {
            if (value.equalsIgnoreCase(DSMTReporterConstants.WarningReportType))
            {
                this.minimumReportType = DSMTReporterConstants.ReportType.WARNING;
            } // end if
            else if (value.equalsIgnoreCase(DSMTReporterConstants.ErrorReportType))
            {
                this.minimumReportType = DSMTReporterConstants.ReportType.ERROR;
            } // end else
        } // end if
        else
        {
            // just log
            this.tracer.critical(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + DSMTReporterConstants.MinimumReportType_Conf_KEY + " in configuration");

        } // end else
          // reading values from configuration
        value = PropertiesReader.getInstance().getProperty("ComponentName");
        if (value != null)
        {

            this.componentName = value;
        } // end else
        else
        {
            // just log
            this.tracer.critical(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + "ComponentName" + " in configuration");

        } // end else
          // reading values from configuration
        value = PropertiesReader.getInstance().getProperty(DSMTReporterConstants.DSMTCmAddress_Conf_Key);
        if (value != null)
        {

            this.dsmtCmAddress = value;
        } // end if
        else
        {
            // just log
            this.tracer.critical(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + DSMTReporterConstants.DSMTCmAddress_Conf_Key + " in configuration");

        } // end else
          // Filling receivers list
        this.receivers[0] = this.dsmtCmAddress;
        // reading values from configuration
        value = PropertiesReader.getInstance().getProperty(DSMTReporterConstants.DSMTEventReportMSGClass_Conf_Key);
        if (value != null)
        {

            this.eventReportMsgClass = value;
        } // end if
        else
        {
            // just log
            this.tracer.critical(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + DSMTReporterConstants.DSMTEventReportMSGClass_Conf_Key + " in configuration");

        } // end else

    }// end method

}// end class
