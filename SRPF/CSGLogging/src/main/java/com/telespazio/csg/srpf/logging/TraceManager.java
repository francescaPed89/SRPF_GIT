/**
*
* MODULE FILE NAME:	TraceManager.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Used to log normal activities
*
* PURPOSE:			Used for logging purposes 
*
* CREATION DATE:	18-01-2016
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

package com.telespazio.csg.srpf.logging;

import java.net.UnknownHostException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.telespazio.csg.srpf.logging.constants.EventSeverity;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.LogMisc;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.logging.constants.TraceType;
import com.telespazio.csg.srpf.logging.exceptions.LoggerConfigurationException;
import com.telespazio.csg.srpf.utils.PropertiesReader;

/**
 * 
 * @author Girolamo Castaldo
 * @version 1.0
 *
 */
public class TraceManager 
{
    private static final Logger log = initLog();

    private CommonFieldsManager commonMgr = null; // Handles fields in common between trace and event managers
    private String mainProcessActivity = ""; // Main activity of the process
    private String additionalInfo = ""; // Additional information needed to investigate an occurring software problem
    private char eventSeverity; // Severity level of the event
    private String eventType = ""; // Event type
    private String probableCause = ""; // Probable cause of the event
    private String specificProblem = ""; // Specific problem associated to the event
    private String serviceID = ""; // Service ID of the event
    private String productionID = ""; // Production ID of the event

    /**
     * Creates and initializes the log4j2 logger
     * @return log4j2 logger
     */
    private static Logger initLog()
    {
        PropertiesReader propReader = PropertiesReader.getInstance(); // Singleton to read from conf file
        String log4jConfFilePath = propReader.getProperty(LogMisc.LOG4J_CONF_FILE_PATH_PARAM); // log4j2.xml file path
        System.setProperty(LogMisc.LOG4J_CONF_FILE_PARAM, log4jConfFilePath);

        return LogManager.getLogger(TraceManager.class);
    } // end method

    /**
     * Creates a new CommonFieldsManager to store common parameters
     * @throws LoggerConfigurationException If there's a problem during the configuration of the logger
     */
    public TraceManager() 
    {
    	//getting configuration
    	PropertiesReader propReader = PropertiesReader.getInstance();
    	int traceType = Integer.parseInt(propReader.getProperty(TraceType.TRACE_TYPE_PARAM));
    	String logOriginator = propReader.getProperty(LogMisc.ORIGINATOR_PARAM);
    	try
    	{
    	    this.commonMgr = new CommonFieldsManager(traceType, logOriginator);
    	}//end try
    	catch (UnknownHostException uhe)
    	{
    		//using default value
    		String defaultHostname = propReader.getProperty(LogMisc.DEFAULT_HOSTNAME_PARAM);
    	    this.commonMgr = new CommonFieldsManager(traceType, logOriginator, defaultHostname);
    	    //log warning
    	    warning(EventType.LOG_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Hostname incorrectly configured on system");
    	}//end catch
    } // end method

    /**
     * Formats and logs level 1 (info) messages
     * @param message Message to log
     */
    public void log(String message)
    {
    	//get trace type
    	int traceType = this.commonMgr.getTraceType();
    	//log only in this case
        if (traceType == TraceType.LOG_LEVEL_1 || traceType == TraceType.LOG_LEVEL_2)
        {
            setMainProcessActivity(message);
            log.info(getFormattedMessage(TraceType.LOG_LEVEL_1));
        }//end if
    } // end method    

    /**
     * Formats and logs level 2 (debug) messages
     * @param message Message to log
     */
    public void debug(String message)
    {
    	//log level
        if (this.commonMgr.getTraceType() == TraceType.LOG_LEVEL_2)
        {
        	//adding info
            setAdditionalInfo(message);
            log.info(getFormattedMessage(TraceType.LOG_LEVEL_2));
        }//end if
    } // end method    

    /**
     * Gets the main activity of the process
     * @return Main activity of the process
     */
    public String getMainProcessActivity() 
    {
        return this.mainProcessActivity;
    } // end method

    /**
     * Gets additional information needed to investigate an occurring software problem
     * @return Additional information needed to investigate an occurring software problem
     */
    public String getAdditionalInfo() 
    {
        return this.additionalInfo;
    } // end method

    /**
     * Stores the main activity of the process
     * @param mainProcessActivity Main activity of the process
     */
    public void setMainProcessActivity(String mainProcessActivity) 
    {
	    this.mainProcessActivity = mainProcessActivity;
    } // end method

    /**
     * Stores additional information needed to investigate an occurring software problem
     * @param additionalInfo Additional information needed to investigate an occurring software problem
     */
    public void setAdditionalInfo (String additionalInfo) 
    {
        this.additionalInfo = additionalInfo;
    } // end method

    /**
     * Formats the event for logging purposes
     * @param traceType Trace level
     * @return Formatted event
     */
    public String getFormattedMessage(int traceType) 
    {
        //int traceType = this.commonMgr.getTraceType();
    	//building
        String formattedMsg = traceType + LogMisc.SEPARATOR + 
            this.commonMgr.getHostNameOriginator() + LogMisc.SEPARATOR + 
            this.commonMgr.getLogOriginator() + LogMisc.SEPARATOR + 
            this.commonMgr.getLogGenerationTime() + LogMisc.SEPARATOR;
        //switching on level
        switch (traceType) 
        {
            case TraceType.LOG_LEVEL_1:
                formattedMsg += this.mainProcessActivity;
                break;
            case TraceType.LOG_LEVEL_2:
                formattedMsg += this.additionalInfo;
                break;
            default:
                break;
        }//end switch
        //returning string
        return formattedMsg;
    } // end method

    /**
     * Formats and logs information priority messages
     * @param eventType Event type
     * @param probableCause Probable cause of the event
     * @param specificProblem Specific problem associated to the event
     * @param serviceID Service ID of the event
     * @param productionID Production ID of the event
     */
    public void information(String eventType, String probableCause, String specificProblem, 
    		String serviceID, String productionID)
    {
    	//set event
    	setEvent(eventType, probableCause, specificProblem, serviceID, productionID);
    	//set severity
    	setEventSeverity(EventSeverity.INFORMATION);
        log.info(getFormattedEvent());
    } // end method

    /**
     * Formats and logs information priority messages, using defaults for missing parameters
     * @param eventType Event type
     * @param probableCause Probable cause of the event
     * @param specificProblem Specific problem associated to the event
     */
    public void information(String eventType, String probableCause, String specificProblem)
    {
    	//set event
  	    setEvent(eventType, probableCause, specificProblem);
  	    //set severity
    	setEventSeverity(EventSeverity.INFORMATION);
        log.info(getFormattedEvent());
    } // end method

    /**
     * Formats and logs critical priority messages
     * @param eventType Event type
     * @param probableCause Probable cause of the event
     * @param specificProblem Specific problem associated to the event
     * @param serviceID Service ID of the event
     * @param productionID Production ID of the event
     */
    public void critical(String eventType, String probableCause, String specificProblem, 
    		String serviceID, String productionID)
    {
    	//set event
    	setEvent(eventType, probableCause, specificProblem, serviceID, productionID);
    	//set severity
    	setEventSeverity(EventSeverity.CRITICAL);
        log.fatal(getFormattedEvent());
    } // end method

    /**
     * Formats and logs critical priority messages, using defaults for missing parameters
     * @param eventType Event type
     * @param probableCause Probable cause of the event
     * @param specificProblem Specific problem associated to the event
     */
    public void critical(String eventType, String probableCause, String specificProblem)
    {
    	//set event
    	setEvent(eventType, probableCause, specificProblem);
    	//set severity
    	setEventSeverity(EventSeverity.CRITICAL);
        log.fatal(getFormattedEvent());
    } // end method

    /**
     * Formats and logs major priority messages
     * @param eventType Event type
     * @param probableCause Probable cause of the event
     * @param specificProblem Specific problem associated to the event
     * @param serviceID Service ID of the event
     * @param productionID Production ID of the event
     */
    public void major(String eventType, String probableCause, String specificProblem, 
    		String serviceID, String productionID)
    {
    	//set event
    	setEvent(eventType, probableCause, specificProblem, serviceID, productionID);
    	//set severity
    	setEventSeverity(EventSeverity.MAJOR);
    	log.log(Level.getLevel(EventSeverity.MAJOR_LEVEL), getFormattedEvent());
    } // end method

    /**
     * Formats and logs major priority messages, using defaults for missing parameters
     * @param eventType Event type
     * @param probableCause Probable cause of the event
     * @param specificProblem Specific problem associated to the event
     */
    public void major(String eventType, String probableCause, String specificProblem)
    {
    	//set event
    	setEvent(eventType, probableCause, specificProblem);
    	//set severity
    	setEventSeverity(EventSeverity.MAJOR);
    	log.log(Level.getLevel(EventSeverity.MAJOR_LEVEL), getFormattedEvent());
    } // end method

    /**
     * Formats and logs minor priority messages
     * @param eventType Event type
     * @param probableCause Probable cause of the event
     * @param specificProblem Specific problem associated to the event
     * @param serviceID Service ID of the event
     * @param productionID Production ID of the event
     */
    public void minor(String eventType, String probableCause, String specificProblem, 
    		String serviceID, String productionID)
    {
    	//set event
    	setEvent(eventType, probableCause, specificProblem, serviceID, productionID);
    	//set severity
    	setEventSeverity(EventSeverity.MINOR);
    	log.log(Level.getLevel(EventSeverity.MINOR_LEVEL), getFormattedEvent());
    } // end method

    /**
     * Formats and logs minor priority messages, using defaults for missing parameters
     * @param eventType Event type
     * @param probableCause Probable cause of the event
     * @param specificProblem Specific problem associated to the event
     */
    public void minor(String eventType, String probableCause, String specificProblem)
    {
    	//set event
    	setEvent(eventType, probableCause, specificProblem);
    	//set severity
    	setEventSeverity(EventSeverity.MINOR);
    	log.log(Level.getLevel(EventSeverity.MINOR_LEVEL), getFormattedEvent());
    } // end method

    /**
     * Formats and logs warning priority messages
     * @param eventType Event type
     * @param probableCause Probable cause of the event
     * @param specificProblem Specific problem associated to the event
     * @param serviceID Service ID of the event
     * @param productionID Production ID of the event
     */
    public void warning(String eventType, String probableCause, String specificProblem, 
    		String serviceID, String productionID)
    {
    	//set severity
    	setEvent(eventType, probableCause, specificProblem, serviceID, productionID);
    	//set severity
    	setEventSeverity(EventSeverity.WARNING);
        log.warn(getFormattedEvent());
    } // end method

    /**
     * Formats and logs warning priority messages, using defaults for missing parameters
     * @param eventType Event type
     * @param probableCause Probable cause of the event
     * @param specificProblem Specific problem associated to the event
     */
    public void warning(String eventType, String probableCause, String specificProblem)
    {
    	//set event
    	setEvent(eventType, probableCause, specificProblem);
    	//set severity
    	setEventSeverity(EventSeverity.WARNING);
        log.warn(getFormattedEvent());
    } // end method

    /**
     * Formats and logs cleared priority messages
     * @param eventType Event type
     * @param probableCause Probable cause of the event
     * @param specificProblem Specific problem associated to the event
     * @param serviceID Service ID of the event
     * @param productionID Production ID of the event
     */
    public void cleared(String eventType, String probableCause, String specificProblem, 
    		String serviceID, String productionID)
    {
    	//set event
    	setEvent(eventType, probableCause, specificProblem, serviceID, productionID);
    	//set severity
    	setEventSeverity(EventSeverity.CLEARED);
    	log.log(Level.getLevel(EventSeverity.CLEARED_LEVEL), getFormattedEvent());
    } // end method

    /**
     * Formats and logs cleared priority messages, using defaults for missing parameters
     * @param eventType Event type
     * @param probableCause Probable cause of the event
     * @param specificProblem Specific problem associated to the event
     */
    public void cleared(String eventType, String probableCause, String specificProblem)
    {
    	//set event
    	setEvent(eventType, probableCause, specificProblem);
    	//set severity
    	setEventSeverity(EventSeverity.CLEARED);
    	log.log(Level.getLevel(EventSeverity.CLEARED_LEVEL), getFormattedEvent());
    } // end method

    /**
     * Stores event parameters
     * @param eventType Event type
     * @param probableCause Probable cause of the event
     * @param specificProblem Specific problem associated to the event
     * @param serviceID Service ID of the event
     * @param productionID Production ID of the event
     */
    public void setEvent(String eventType, String probableCause, String specificProblem, 
    		String serviceID, String productionID)
    {
    	//set param
        this.eventType = eventType;
        //set param
        this.probableCause = probableCause;
        //set param
        this.specificProblem = specificProblem;
        //set param
        this.serviceID = serviceID;
        //set param
        this.productionID = productionID;
    }//end method

    /**
     * Stores some event parameters, using defaults for the missing ones
     * @param eventType Event type
     * @param probableCause Probable cause of the event
     * @param specificProblem Specific problem associated to the event
     */
    public void setEvent(String eventType, String probableCause, String specificProblem)
    {
    	setEvent(eventType, probableCause, specificProblem, LogMisc.SERVICE_ID_DEFAULT, LogMisc.PRODUCTION_ID_DEFAULT);
    } // end method    

    /**
     * Gets the severity level of the event
     * @return Severity level of the event
     */
    public char getEventSeverity() 
    {
        return this.eventSeverity;
    } // end method

    /**
     * Gets the event type
     * @return Event type
     */
    public String getEventType() 
    {
        return this.eventType;
	} // end method

    /**
     * Gets the probable cause of the event
     * @return Probable cause of the event
     */
    public String getProbableCause() 
    {
        return this.probableCause;
    } // end method

    /**
     * Gets the specific problem associated to the event
     * @return Specific problem associated to the event
     */
    public String getSpecificProblem() 
    {
        return this.specificProblem;
    } // end method

    /**
     * Gets the serviceID of the event
     * @return Service ID of the event
     */
    public String getServiceID() 
    {
        return this.serviceID;
    } // end method

    /**
     * Gets the productionID of the event
     * @return Production ID of the event
     */
    public String getProductionID() 
    {
        return this.productionID;
    } // end method

    /**
     * Stores the severity level of the event 
     * @param eventSeverity Severity level of the event
     */
    public void setEventSeverity(char eventSeverity) 
    {
        this.eventSeverity = eventSeverity;
    } // end method

    /**
     * Stores the event type
     * @param eventType Event type
     */
    public void setEventType(String eventType) 
    {
        this.eventType = eventType;
    } // end method

    /**
     * Stores the probable cause of the event
     * @param probableCause Probable cause of the event
     */
    public void setProbableCause(String probableCause) 
    {
        this.probableCause = probableCause;
    } // end method

    /**
     * Stores the specific problem associated to the event
     * @param specificProblem Specific problem associated to the event
     */
    public void setSpecificProblem(String specificProblem) 
    {
        this.specificProblem = specificProblem;
    } // end method

    /**
     * Stores the service ID of the event
     * @param serviceID Service ID of the event
     */
    public void setServiceID(String serviceID) 
    {
        this.serviceID = serviceID;
    } // end method

    /**
     * Stores the production ID of the event
     * @param productionID Production ID of the event
     */
    public void setProductionID(String productionID) 
    {
        this.productionID = productionID;
    } // end method

    /**
     * Formats the event for logging purposes
     * @return Formatted event
     */
    public String getFormattedEvent() 
    {
    	//Buiding
    	//building formatted string
        String formattedEvent = TraceType.EVENT + LogMisc.SEPARATOR + 
        	this.commonMgr.getHostNameOriginator() + LogMisc.SEPARATOR + 
        	this.commonMgr.getLogOriginator() + LogMisc.SEPARATOR + 
            this.commonMgr.getLogGenerationTime() + LogMisc.SEPARATOR +
            this.eventSeverity + LogMisc.SEPARATOR + this.eventType + LogMisc.SEPARATOR + 
            this.probableCause + LogMisc.SEPARATOR + this.specificProblem + LogMisc.SEPARATOR + 
            this.serviceID + LogMisc.SEPARATOR + this.productionID;
       //returning string
       return formattedEvent;
    } // end method    
} // end class