/**
*
* MODULE FILE NAME:	EventManager.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Used for logging events 
*
* PURPOSE:			Used for logging events 
*
* CREATION DATE:	15-01-2016
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
 * Used for logging events 
 * @author Girolamo Castaldo
 * @version 1.0
 *
 */
public class EventManager 
{
	private static final Logger log = initLog(); // log4j2 logger
	
    private CommonFieldsManager commonMgr = null; // Handles fields in common between trace and event managers
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

        return LogManager.getLogger(EventManager.class);
    } // end method
    
    /**
     * Creates a new CommonFieldsManager to store common parameters
     */
    public EventManager () 
    {
        PropertiesReader propReader = PropertiesReader.getInstance();
    	String logOriginator = propReader.getProperty(LogMisc.ORIGINATOR_PARAM);
    	try
    	{
            this.commonMgr = new CommonFieldsManager(TraceType.EVENT, logOriginator);
    	}//end try
    	catch (UnknownHostException uhe)
    	{
    		//using default
    		String defaultHostname = propReader.getProperty(LogMisc.DEFAULT_HOSTNAME_PARAM);
    	    this.commonMgr = new CommonFieldsManager(TraceType.EVENT, logOriginator, defaultHostname);
    	    //logging
    	    warning(EventType.LOG_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Hostname incorrectly configured on system");
    	}//end catch

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
    	//set event
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
    	//setting events
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
    	//setting
        this.eventType = eventType;
        //setting
        this.probableCause = probableCause;
        //setting
        this.specificProblem = specificProblem;
        //setting
        this.serviceID = serviceID;
        //setting
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
    }//end method

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
    	//formatting 
        String formattedEvent = this.commonMgr.getHostNameOriginator() + LogMisc.SEPARATOR + 
            this.commonMgr.getLogOriginator() + LogMisc.SEPARATOR + 
            this.commonMgr.getLogGenerationTime() + LogMisc.SEPARATOR +
            this.eventSeverity + LogMisc.SEPARATOR + this.eventType + LogMisc.SEPARATOR + 
            this.probableCause + LogMisc.SEPARATOR + this.specificProblem + LogMisc.SEPARATOR + 
            this.serviceID + LogMisc.SEPARATOR + this.productionID;
        //formatted string
        //returning formatted event
        return formattedEvent;
    } // end method
} // end class