/**
*
* MODULE FILE NAME:	CMIngestor.java
*
* MODULE TYPE:		Main program
*
* FUNCTION:			Interact with CM
*
* PURPOSE:			Interact with CM
*
* CREATION DATE:	21-11-2015
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

package com.telespazio.csg.srpf.cm;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.telespazio.csg.srpf.exception.InvalidNameException;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.Checksum;
import com.telespazio.csg.srpf.utils.PropertiesReader;

import CM.CMAPI.CM_Exception;
import CM.CMAPI.CM_Filter;
import CM.CMAPI.CM_INotification;
import CM.CMAPI.CM_Message;
import CM.CMAPI.CM_Services;

/**
 * Implements the CMIngestor component, used to receive and handle request from
 * CM
 * 
 * @author Girolamo Castaldo
 * @version 1.0
 */
public class CMIngestor
{

    private TraceManager tracer; // Handles trace logging
    // private EventManager eventMgr; // Handles event logging

    private CM_Services cmServ; // Used to communicate with CM
    // private Properties cmIngestorConf; // Contains configuration for this
    // CMIngestor
    private PropertiesReader cmIngestorConf; // Contains configuration for this
                                             // CMIngestor

    private String feasibilityMsgClass; // Feasibility message class
    private String ODREFMsgClass; // ODREF message class
    private String ODNOMMsgClass; // ODNOM message class
    private String ODMTPMsgClass; // ODMTP message class
    private String ODSTPMsgClass; // ODSTP message class
    private String PAWMsgClass; // PAW message class
    private String AllocationPlanClass;

    private String FeasibilityRefinementMsgClass;
    private String SOEMsgClass;

    // set true if have purge messages
    private boolean havePurgeMessageBoxAtStart = false;

    // Parameters for polling
    private boolean haveApplyPollingPolicy = false;
    private long pollingInterval = 250;

    /**
     * Starts the CMIngestor and registers the callbacks needed to get
     * notifications from CM
     *
     * @throws InvalidNameException
     *             Thrown if component name format is invalid
     */
    public CMIngestor() throws InvalidNameException
    {

        this.cmIngestorConf = PropertiesReader.getInstance();
        String componentName = this.cmIngestorConf.getProperty(CMConstants.CM_INGESTOR_NAME); // This
                                                                                              // CMIngestor's
                                                                                              // component
                                                                                              // name
        String configFile = this.cmIngestorConf.getConfigFile();
        this.tracer = new TraceManager();
        // this.eventMgr = new EventManager();

        this.tracer.log("CMIngestor started");
        this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.SYSTEM_STARTUP, "CMIngestor started");

        // Check if the CM message box must be purged at start
        try
        {
            /**
             * Check if the msg box of CM should be emptied
             */
            String havePurgeString = this.cmIngestorConf.getProperty(CMConstants.HAVE_PURGE_CM_MESSAGE_BOX_CONF_KEY);
            if (havePurgeString == null)
            {
                this.tracer.warning(EventType.APPLICATION_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + CMConstants.HAVE_PURGE_CM_MESSAGE_BOX_CONF_KEY + " in configuration file: by default the old CM Messages will be kept in the message box");

            }
            else
            {
                if (Integer.parseInt(havePurgeString) == 0)
                {
                    /**
                     * Set to false the pruge
                     */
                    this.havePurgeMessageBoxAtStart = false;
                }
                else
                {
                    /**
                     * set to true the purge
                     */
                    this.havePurgeMessageBoxAtStart = true;
                } // end else

            } // end else
        } // end try
        catch (Exception e)
        {
            this.tracer.warning(EventType.APPLICATION_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Error in parsing " + CMConstants.HAVE_PURGE_CM_MESSAGE_BOX_CONF_KEY + " parameter in configuration file: by default the old CM Messages will be kept in the message box");
        }

        // this.eventMgr.information(EventType.APPLICATION_EVENT,
        // ProbableCause.SYSTEM_STARTUP, "CMIngestor started");

        try
        {
            /**
             * Evaluate the MD5SUM of configuration file
             */
            String configFileMD5 = Checksum.getFileChecksum(MessageDigest.getInstance("MD5"), new File(configFile));
            String message = "Configuration read from: " + configFile + ", MD5: " + configFileMD5;
            this.tracer.log(message);
            this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "Configuration read");
            // this.eventMgr.information(EventType.APPLICATION_EVENT,
            // ProbableCause.INFORMATION_INFO, message);

            if (!isValidComponentName(componentName))
            {
                String errorMessage = CMConstants.CM_COMPONENT_INVALID_NAME; // Error
                                                                             // message
                                                                             // for
                                                                             // invalid
                                                                             // component
                                                                             // name
                this.tracer.log(errorMessage);
                this.tracer.major(EventType.APPLICATION_EVENT, ProbableCause.CALL_ESTABLISHMENT_ERROR, errorMessage);
                // this.eventMgr.critical(EventType.APPLICATION_EVENT,
                // ProbableCause.CALL_ESTABLISHMENT_ERROR, errorMessage);
                throw new InvalidNameException(errorMessage);
            }

            this.cmServ = new CM_Services(componentName);
            ShutDownHandler shutdown = new ShutDownHandler(componentName, this.tracer, this); // Handler
                                                                                              // for
                                                                                              // cleanup
                                                                                              // purposes
                                                                                              // at
                                                                                              // shutdown
            Runtime.getRuntime().addShutdownHook(shutdown); // Adds the shutdown
                                                            // handler to this
                                                            // class

            settingPollingParameters();

            // Generate un excetption if the
            // connection fails
            testConnection();
            // empty old pending messages
            if (this.havePurgeMessageBoxAtStart)
            {
                this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "Deleting old messages in CM Box");
                /**
                 * Empty the msg box of CM
                 */
                emptyMessageBox();
            } // end if

            // retrieving message classes
            fillMessageClasses();

            unregister();

            /**
             * Perform registration for message classes
             */
            registerForFeasibilityRequest();
            this.tracer.log("Registered for feasibility messages");
            registerForODREFRequest();
            this.tracer.log("Registered for ODREF messages");
            registerForODNOMRequest();
            this.tracer.log("Registered for ODNOM messages");
            registerForODMTPRequest();
            this.tracer.log("Registered for ODMTP messages");
            registerForODSTPRequest();
            this.tracer.log("Registered for ODSTP messages");
            registerForPAWRequest();
            this.tracer.log("Registered for PAW messages");
            registerForAllocationPlanRequest();
            this.tracer.log("Registered for Allocation Plan messages");
            registerForFeasibilityRefinementRequest();
            this.tracer.log("Registered for feasibilityRefinement messages");
            registerForSOE();
            this.tracer.log("Registered for SOE messages");

            CM_Filter filter = new CM_Filter();
            CM_Message msg;

            while (true)
            {
                try
                {
                    /**
                     * The polling policy have been selected
                     */
                    if (this.haveApplyPollingPolicy)
                    {

                        try
                        {
                            msg = this.cmServ.PeekMessage(filter);
                            this.managePolledMsg(msg);

                        }
                        catch (CM_Exception cme)
                        {
                            // it doesn't make sense to log anything
                            // this.tracer.log(cme.getMessage());
                            this.tracer.major(EventType.COMMUNICATION_EVENT, ProbableCause.REMOTE_NODE_TRANSMISSION_ERROR, cme.getMessage());
                            // this.eventMgr.major(EventType.COMMUNICATION_EVENT,
                            // ProbableCause.REMOTE_NODE_TRANSMISSION_ERROR,
                            // cme.getMessage());
                        }

                        Thread.sleep(this.pollingInterval);

                    } // end if
                    else
                    {
                        Thread.sleep(1000);
                    } // end else

                } // end try
                catch (InterruptedException ie)
                {
                    break;
                } // end catch
            } // end while
        } // end try
        catch (NoSuchAlgorithmException | IOException e)
        {
            this.tracer.log(e.getMessage());
            this.tracer.minor(EventType.APPLICATION_EVENT, ProbableCause.CHECKSUM_VERIFICATION_FAILURE, e.getMessage());
            // this.eventMgr.minor(EventType.APPLICATION_EVENT,
            // ProbableCause.CHECKSUM_VERIFICATION_FAILURE, e.getMessage());
        } // end catch
        catch (CM_Exception cme)
        {
            this.tracer.log(cme.getMessage());
            this.tracer.major(EventType.COMMUNICATION_EVENT, ProbableCause.REMOTE_NODE_TRANSMISSION_ERROR, cme.getMessage());
            // this.eventMgr.major(EventType.COMMUNICATION_EVENT,
            // ProbableCause.REMOTE_NODE_TRANSMISSION_ERROR, cme.getMessage());
        } // end catch
        finally
        {
            unregister();
            this.tracer.log("Unregistered from CM baskets and notifications");
        } // end finally
    } // end method

    /**
     * Retrieve the message classes
     */
    private void fillMessageClasses()
    {
        // Feasibility message class
        this.feasibilityMsgClass = this.cmIngestorConf.getProperty(CMConstants.CM_FEASIBILITY_MSG_CLASS_PROP, CMConstants.CM_FEASIBILITY_MSG_CLASS);
        // ODREF message class
        this.ODREFMsgClass = this.cmIngestorConf.getProperty(CMConstants.CM_ODREF_MSG_CLASS_PROP, CMConstants.CM_ODREF_MSG_CLASS);
        // ODNOM message class
        this.ODNOMMsgClass = this.cmIngestorConf.getProperty(CMConstants.CM_ODNOM_MSG_CLASS_PROP, CMConstants.CM_ODNOM_MSG_CLASS);
        // ODMTP message class
        this.ODMTPMsgClass = this.cmIngestorConf.getProperty(CMConstants.CM_ODMTP_MSG_CLASS_PROP, CMConstants.CM_ODMTP_MSG_CLASS);

        // ODSTP message class
        this.ODSTPMsgClass = this.cmIngestorConf.getProperty(CMConstants.CM_ODSTP_MSG_CLASS_PROP, CMConstants.CM_ODSTP_MSG_CLASS);

        // ODSTP message class
        this.PAWMsgClass = this.cmIngestorConf.getProperty(CMConstants.CM_PAW_MSG_CLASS_PROP, CMConstants.CM_PAW_MSG_CLASS);

        // ODSTP message class
        this.AllocationPlanClass = this.cmIngestorConf.getProperty(CMConstants.CM_ALL_PLAN_MSG_CLASS_PROP, CMConstants.CM_ALL_PLAN_MSG_CLASS);

        // Refinement message class
        this.FeasibilityRefinementMsgClass = this.cmIngestorConf.getProperty(CMConstants.CM_FEASIBILITY_REF_MSG_CLASS_PROP, CMConstants.CM_FEASIBILITY_REF_MSG_CLASS); // Feasibility
                                                                                                                                                                       // message
                                                                                                                                                                       // class

        // SOE Message class
        this.SOEMsgClass = this.cmIngestorConf.getProperty(CMConstants.CM_SOE_MSG_CLASS_PROP, CMConstants.CM_SOE_MSG_CLASS); // Feasibility
                                                                                                                             // message
                                                                                                                             // class

        // 9

    }// end method

    /**
     * Test if the connection with helper can be perfprmed In case negative an
     * excetion is raised
     * 
     * @throws CM_Exception
     */
    private void testConnection() throws CM_Exception
    {
        // CM Filter
        CM_Filter filter = new CM_Filter();
        CM_Message msg;

        // vaid connection flag
        boolean isValidConnection = false;
        /**
         * Number of temptative to check if a connection could be exstablished
         */
        int retry = 10;

        // chech for connection
        while (true)
        {
            try
            {
                // try to retrieve a generic message
                // if the CM is not active an exception
                // is raised
                msg = this.cmServ.PeekMessage(filter);
                isValidConnection = true;

            } // end try
            catch (CM_Exception e)
            {

                retry--;
                if (e.getCause() instanceof java.net.ConnectException)
                {
                    this.tracer.log(e.getMessage());
                    this.tracer.major(EventType.COMMUNICATION_EVENT, ProbableCause.REMOTE_NODE_TRANSMISSION_ERROR, e.getMessage());
                    // this.eventMgr.major(EventType.COMMUNICATION_EVENT,
                    // ProbableCause.REMOTE_NODE_TRANSMISSION_ERROR,
                    // e.getMessage());
                    if (retry < 0)
                    {
                        // used all the temptative
                        // a connection with CM could be exstabilshed
                        // an exception is thrown
                        throw e;
                    } // end if
                    else
                    {
                        try
                        {
                            Thread.sleep(1000);
                        }
                        catch (InterruptedException e1)
                        {
                            // It doesn't make sense
                            // to log anything
                        } // end catch
                    } // end catch
                } // end if
                else
                {
                    isValidConnection = true;
                } // end else

            } // end catch
            if (isValidConnection)
            {
                break;
            }
        } // end while

    }// end method

    /**
     * remove pending messages from the CM BOX
     */
    private void emptyMessageBox()
    {

        try
        {
            // retrieve availabe messages
            CM_Message[] msgs = this.cmServ.GetAvailableMessageInfos();
            int id = 0;
            // delete messages
            for (int i = 0; i < msgs.length; i++)
            {
                id = (int) msgs[i].GetUniqueID();
                // delete message with the
                // specified id
                this.cmServ.DeleteMessage(id);

                this.tracer.log("Deleting old message " + id);
            } // end for

        } // end try
        catch (Exception e)
        {
            // do nothing
        } // end catch

    }// end method

    /**
     * Set the polling flag and the polling refresh time
     */
    private void settingPollingParameters()
    {
        // retrieve the polling flag
        // this will determin if polling or
        // unsolicited strategy will be used
        String value = this.cmIngestorConf.getProperty(CMConstants.CMI_POLLING_FLAG_PROP, CMConstants.CMI_POLLING_FLAG_DEFAULT);

        try
        {
            // evaluate the policy used
            int ivalue = Integer.parseInt(value);
            if (ivalue != 0)
            {
                this.haveApplyPollingPolicy = true;
            }
        } // end try
        catch (Exception e)
        {
            // this.haveApplyPollingPolicy=false;
            this.tracer.minor(EventType.APPLICATION_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Malformed parameter " + CMConstants.CMI_POLLING_FLAG_PROP + " in configuration file");
        } // end catch

        // retrieve the polling
        // interval
        value = this.cmIngestorConf.getProperty(CMConstants.CMI_POLLING_INTERVAL_PROP, CMConstants.CMI_POLLING_INTERVAL_DEFAULT);

        try
        {
            // set the polling
            // interval
            long lvalue = Long.parseLong(value);
            this.pollingInterval = lvalue;
        } // end try
        catch (Exception e)
        {
            // this.pollingInterval=250;
            this.tracer.minor(EventType.APPLICATION_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Malformed parameter " + CMConstants.CMI_POLLING_INTERVAL_PROP + " in configuration file");
        } // end catch

        // log the policy used
        if (this.haveApplyPollingPolicy)
        {
            this.tracer.log("Polling Policy shall be used with polling interval: " + this.pollingInterval);
        } // end if
        else
        {
            this.tracer.log("Unsolicited policy shall be used");
        } // end else

    }// end method

    /*
     * Validate component name format
     * 
     * @param componentName This component's name
     */
    private final boolean isValidComponentName(final String componentName)
    {
        boolean validName = false; // Holds validation result
        if (componentName.matches(CMConstants.CM_COMPONENT_NAME_REGEX))
        {
            validName = true;
        } // end if
        return validName;
    } // end method

    /**
     * Registers a callback to receive feasibility requests from CM
     * 
     * @throws CM_Exception
     */
    private final void registerForFeasibilityRequest() throws CM_Exception// ,
                                                                          // MalformedURLException
    {
        String basket = this.cmIngestorConf.getProperty(CMConstants.CM_FEASIBILITY_BASKET_PROP, CMConstants.CM_FEASIBILITY_BASKET); // Destination
                                                                                                                                    // directory
                                                                                                                                    // for
                                                                                                                                    // feasibility
                                                                                                                                    // attachments
        this.feasibilityMsgClass = this.cmIngestorConf.getProperty(CMConstants.CM_FEASIBILITY_MSG_CLASS_PROP, CMConstants.CM_FEASIBILITY_MSG_CLASS); // Feasibility
                                                                                                                                                     // message
                                                                                                                                                     // class
        this.cmServ.RegisterInBasket(this.feasibilityMsgClass, basket);
        if (!this.haveApplyPollingPolicy)
        {
            this.cmServ.RegisterForUnsolicitedNotification(this.feasibilityMsgClass, new FeasibilityHandler(this.cmServ, this.tracer));
        } // end if

    } // end method

    /**
     * Registers a callback to receive ODREF requests from CM
     * 
     * @throws CM_Exception
     */
    private final void registerForODREFRequest() throws CM_Exception
    {
        String basket = this.cmIngestorConf.getProperty(CMConstants.CM_ODREF_BASKET_PROP, CMConstants.CM_ODREF_BASKET); // Destination
                                                                                                                        // directory
                                                                                                                        // for
                                                                                                                        // ODREF
                                                                                                                        // attachments
        this.ODREFMsgClass = this.cmIngestorConf.getProperty(CMConstants.CM_ODREF_MSG_CLASS_PROP, CMConstants.CM_ODREF_MSG_CLASS); // ODREF
                                                                                                                                   // message
                                                                                                                                   // class
        this.cmServ.RegisterInBasket(this.ODREFMsgClass, basket);// register in
                                                                 // basket

        // if not polling strategy
        // register for unsolicited notification
        // on CM
        if (!this.haveApplyPollingPolicy)
        {
            this.cmServ.RegisterForUnsolicitedNotification(this.ODREFMsgClass, new ODREFHandler(this.cmServ, this.tracer));
        } // end if
    } // end method

    /**
     * Registers a callback to receive ODNOM requests from CM
     * 
     * @throws CM_Exception
     */
    private final void registerForODNOMRequest() throws CM_Exception
    {
        String basket = this.cmIngestorConf.getProperty(CMConstants.CM_ODNOM_BASKET_PROP, CMConstants.CM_ODNOM_BASKET); // Destination
                                                                                                                        // directory
                                                                                                                        // for
                                                                                                                        // ODNOM
                                                                                                                        // attachments
        this.ODNOMMsgClass = this.cmIngestorConf.getProperty(CMConstants.CM_ODNOM_MSG_CLASS_PROP, CMConstants.CM_ODNOM_MSG_CLASS); // ODNOM
                                                                                                                                   // message
                                                                                                                                   // class
        this.cmServ.RegisterInBasket(this.ODNOMMsgClass, basket); // registering
                                                                  // in basket
        // if not polling strategy
        // register for unsolicited notification
        // on CM
        if (!this.haveApplyPollingPolicy)
        {
            this.cmServ.RegisterForUnsolicitedNotification(this.ODNOMMsgClass, new ODNOMHandler(this.cmServ, this.tracer));
        } // end if
    } // end method

    /**
     * Registers a callback to receive ODMTP requests from CM
     * 
     * @throws CM_Exception
     */
    private final void registerForODMTPRequest() throws CM_Exception
    {
        String basket = this.cmIngestorConf.getProperty(CMConstants.CM_ODMTP_BASKET_PROP, CMConstants.CM_ODMTP_BASKET); // Destination
                                                                                                                        // directory
                                                                                                                        // for
                                                                                                                        // ODMTP
                                                                                                                        // attachments
        this.ODMTPMsgClass = this.cmIngestorConf.getProperty(CMConstants.CM_ODMTP_MSG_CLASS_PROP, CMConstants.CM_ODMTP_MSG_CLASS); // ODMTP
                                                                                                                                   // message
                                                                                                                                   // class
        this.cmServ.RegisterInBasket(this.ODMTPMsgClass, basket); // registering
                                                                  // in basket
        // if not polling strategy
        // register for unsolicited notification
        // on CM
        if (!this.haveApplyPollingPolicy)
        {
            this.cmServ.RegisterForUnsolicitedNotification(this.ODMTPMsgClass, new ODMTPHandler(this.cmServ, this.tracer));
        } // end if
    } // end method

    /**
     * Registers a callback to receive ODSTP requests from CM
     * 
     * @throws CM_Exception
     */
    private final void registerForODSTPRequest() throws CM_Exception
    {
        String basket = this.cmIngestorConf.getProperty(CMConstants.CM_ODSTP_BASKET_PROP, CMConstants.CM_ODSTP_BASKET); // Destination
                                                                                                                        // directory
                                                                                                                        // for
                                                                                                                        // ODSTP
                                                                                                                        // attachments
        this.ODSTPMsgClass = this.cmIngestorConf.getProperty(CMConstants.CM_ODSTP_MSG_CLASS_PROP, CMConstants.CM_ODSTP_MSG_CLASS); // ODSTP
                                                                                                                                   // message
                                                                                                                                   // class
        this.cmServ.RegisterInBasket(this.ODSTPMsgClass, basket);// register in
                                                                 // basket

        // if not polling strategy
        // register for unsolicited notification
        // on CM
        if (!this.haveApplyPollingPolicy)
        {
            this.cmServ.RegisterForUnsolicitedNotification(this.ODSTPMsgClass, new ODSTPHandler(this.cmServ, this.tracer));
        } // end if
    } // end method

    /**
     * Registers a callback to receive PAW requests from CM
     * 
     * @throws CM_Exception
     */
    private final void registerForPAWRequest() throws CM_Exception
    {
        String basket = this.cmIngestorConf.getProperty(CMConstants.CM_PAW_BASKET_PROP, CMConstants.CM_PAW_BASKET); // Destination
                                                                                                                    // directory
                                                                                                                    // for
                                                                                                                    // ODSTP
                                                                                                                    // attachments
        this.PAWMsgClass = this.cmIngestorConf.getProperty(CMConstants.CM_PAW_MSG_CLASS_PROP, CMConstants.CM_PAW_MSG_CLASS); // ODSTP
                                                                                                                             // message
                                                                                                                             // class
        this.cmServ.RegisterInBasket(this.PAWMsgClass, basket);// register in
                                                               // basket

        // if not polling strategy
        // register for unsolicited notification
        // on CM
        if (!this.haveApplyPollingPolicy)
        {
            this.cmServ.RegisterForUnsolicitedNotification(this.PAWMsgClass, new PAWHandler(this.cmServ, this.tracer));
        } // end if
    } // end method

    /**
     * Registers a callback to receive AllocationPlan requests from CM
     * 
     * @throws CM_Exception
     */
    private final void registerForAllocationPlanRequest() throws CM_Exception
    {
        String basket = this.cmIngestorConf.getProperty(CMConstants.CM_ALL_PLAN_BASKET_PROP, CMConstants.CM_ALL_PLAN_BASKET); // Destination
                                                                                                                              // directory
                                                                                                                              // for
                                                                                                                              // ODSTP
                                                                                                                              // attachments
        this.AllocationPlanClass = this.cmIngestorConf.getProperty(CMConstants.CM_ALL_PLAN_MSG_CLASS_PROP, CMConstants.CM_ALL_PLAN_MSG_CLASS); // ODSTP
                                                                                                                                               // message
                                                                                                                                               // class
        this.cmServ.RegisterInBasket(this.AllocationPlanClass, basket);// registering
                                                                       // in
                                                                       // backet

        // if not polling strategy
        // register for unsolicited notification
        // on CM
        if (!this.haveApplyPollingPolicy)
        {
            this.cmServ.RegisterForUnsolicitedNotification(this.AllocationPlanClass, new ALLOCATIONPLANHandler(this.cmServ, this.tracer));
        } // end if
    } // end method

    /**
     * Registers a callback to receive feasibility requests from CM
     * 
     * @throws CM_Exception
     */
    private final void registerForFeasibilityRefinementRequest() throws CM_Exception// ,
                                                                                    // MalformedURLException
    {
        String basket = this.cmIngestorConf.getProperty(CMConstants.CM_FEASIBILITY_REF_BASKET_PROP, CMConstants.CM_FEASIBILITY_REF_BASKET); // Destination
                                                                                                                                            // directory
                                                                                                                                            // for
                                                                                                                                            // feasibility
                                                                                                                                            // attachments
        this.FeasibilityRefinementMsgClass = this.cmIngestorConf.getProperty(CMConstants.CM_FEASIBILITY_REF_MSG_CLASS_PROP, CMConstants.CM_FEASIBILITY_REF_MSG_CLASS); // Feasibility
                                                                                                                                                                       // message
                                                                                                                                                                       // class
        this.cmServ.RegisterInBasket(this.FeasibilityRefinementMsgClass, basket);
        if (!this.haveApplyPollingPolicy)
        {
            this.cmServ.RegisterForUnsolicitedNotification(this.FeasibilityRefinementMsgClass, new FeasibilityRefinementHandler(this.cmServ, this.tracer));
        }

    } // end method

    /**
     * Registers a callback to receive feasibility requests from CM
     * 
     * @throws CM_Exception
     */
    private final void registerForSOE() throws CM_Exception// ,
                                                           // MalformedURLException
    {
        String basket = this.cmIngestorConf.getProperty(CMConstants.CM_SOE_BASKET_PROP, CMConstants.CM_SOE_BASKET); // Destination
                                                                                                                    // directory
                                                                                                                    // for
                                                                                                                    // feasibility
                                                                                                                    // attachments
        this.SOEMsgClass = this.cmIngestorConf.getProperty(CMConstants.CM_SOE_MSG_CLASS_PROP, CMConstants.CM_SOE_MSG_CLASS); // Feasibility
                                                                                                                             // message
                                                                                                                             // class
        this.cmServ.RegisterInBasket(this.SOEMsgClass, basket);
        if (!this.haveApplyPollingPolicy)
        {
            this.cmServ.RegisterForUnsolicitedNotification(this.SOEMsgClass, new SOEHandler(this.cmServ, this.tracer));
        }

    } // end method

    /**
     * Unregisters the ingestor from CM baskets and notifications
     */
    void unregister()
    {
        try
        {
            this.cmServ.UnregisterInBasket(this.feasibilityMsgClass);
        }
        catch (Exception e)
        {
            // this.tracer.debug(e.getMessage());
        } // end catch

        try
        {
            this.cmServ.UnregisterInBasket(this.ODREFMsgClass);
        }
        catch (Exception e)
        {
            // this.tracer.debug(e.getMessage());
        } // end catch
        try
        {
            this.cmServ.UnregisterInBasket(this.ODNOMMsgClass);
        }
        catch (Exception e)
        {
            // this.tracer.debug(e.getMessage());
        } // end catch
        try
        {
            this.cmServ.UnregisterInBasket(this.ODMTPMsgClass);
        }
        catch (Exception e)
        {
            // this.tracer.debug(e.getMessage());
        } // end catch
        try
        {
            this.cmServ.UnregisterInBasket(this.ODSTPMsgClass);
        }
        catch (Exception e)
        {
            // this.tracer.debug(e.getMessage());
        } // end catch
        try
        {
            this.cmServ.UnregisterInBasket(this.PAWMsgClass);
        }
        catch (Exception e)
        {
            // this.tracer.debug(e.getMessage());
        } // end catch
        try
        {
            this.cmServ.UnregisterInBasket(this.AllocationPlanClass);
        }
        catch (Exception e)
        {
            // this.tracer.debug(e.getMessage());
        } // end catch
        try
        {
            this.cmServ.UnregisterInBasket(this.FeasibilityRefinementMsgClass);
        }
        catch (Exception e)
        {
            // this.tracer.debug(e.getMessage());
        } // end catch
        try
        {
            this.cmServ.UnregisterInBasket(this.SOEMsgClass);
        }
        catch (Exception e)
        {
            // this.tracer.debug(e.getMessage());
        } // end catch

        try
        {
            this.cmServ.UnregisterForUnsolicitedNotification(this.feasibilityMsgClass);
        }
        catch (Exception e)
        {
            // this.tracer.debug(e.getMessage());
        } // end catch
        try
        {
            this.cmServ.UnregisterForUnsolicitedNotification(this.ODREFMsgClass);
        }
        catch (Exception e)
        {
            // this.tracer.debug(e.getMessage());
        } // end catch
        try
        {
            this.cmServ.UnregisterForUnsolicitedNotification(this.ODNOMMsgClass);
        }
        catch (Exception e)
        {
            // this.tracer.debug(e.getMessage());
        } // end catch
        try
        {
            this.cmServ.UnregisterForUnsolicitedNotification(this.ODMTPMsgClass);
        }
        catch (Exception e)
        {
            // this.tracer.debug(e.getMessage());
        } // end catch
        try
        {
            this.cmServ.UnregisterForUnsolicitedNotification(this.ODSTPMsgClass);
        }
        catch (Exception e)
        {
            // this.tracer.debug(e.getMessage());
        } // end catch
        try
        {
            this.cmServ.UnregisterForUnsolicitedNotification(this.PAWMsgClass);
        }
        catch (Exception e)
        {
            // this.tracer.debug(e.getMessage());
        } // end catch
        try
        {
            this.cmServ.UnregisterForUnsolicitedNotification(this.AllocationPlanClass);
        }
        catch (Exception e)
        {
            // this.tracer.debug(e.getMessage());
        }
        try
        {
            this.cmServ.UnregisterForUnsolicitedNotification(this.FeasibilityRefinementMsgClass);
        }
        catch (Exception e)
        {
            // this.tracer.debug(e.getMessage());
        } // end catch
        try
        {
            this.cmServ.UnregisterForUnsolicitedNotification(this.SOEMsgClass);
        }
        catch (Exception e)
        {
            // this.tracer.debug(e.getMessage());
        } // end catch

        /*
         * catch (Exception cme) { this.tracer.debug(cme.getMessage()); }
         */ // end catch
    } // end method

    /**
     * manage messages in polling mode
     * 
     * @param msg
     *            Message
     */
    void managePolledMsg(CM_Message msg)
    {
        if (msg != null)
        {
            CM_INotification notification = null;
            // get the messa class
            String messageClass = msg.GetClass();

            if (messageClass.equals(this.feasibilityMsgClass))
            {
                // create hdl
                notification = new FeasibilityHandler(this.cmServ, this.tracer);
            } // end else
            else if (messageClass.equals(this.ODSTPMsgClass))
            {
                // create hdl
                notification = new ODSTPHandler(this.cmServ, this.tracer);
            } // end else
            else if (messageClass.equals(this.ODMTPMsgClass))
            {
                // create hdl
                notification = new ODMTPHandler(this.cmServ, this.tracer);
            } // end else
            else if (messageClass.equals(this.ODNOMMsgClass))
            {
                // create hdl
                notification = new ODNOMHandler(this.cmServ, this.tracer);
            } // end else
            else if (messageClass.equals(this.ODREFMsgClass))
            {
                // create hdl
                notification = new ODREFHandler(this.cmServ, this.tracer);
            } // end else
            else if (messageClass.equals(this.PAWMsgClass))
            {
                // create hdl
                notification = new PAWHandler(this.cmServ, this.tracer);
            } // end else
            else if (messageClass.equals(this.AllocationPlanClass))
            {
                // create hdl
                notification = new ALLOCATIONPLANHandler(this.cmServ, this.tracer);
            } // end else
            else if (messageClass.equals(this.FeasibilityRefinementMsgClass))
            {
                // create hdl
                notification = new FeasibilityRefinementHandler(this.cmServ, this.tracer);
            } // end else
            else if (messageClass.equals(this.SOEMsgClass))
            {
                // create hdl
                notification = new SOEHandler(this.cmServ, this.tracer);
            } // end else
            else
            {
                // unknown message class
                this.tracer.log("Unckown Message class: " + messageClass);
                return;
            } // end else

            // rfetrieve msg id
            int id = (int) msg.GetUniqueID();
            // Start the thread
            // that will manage the message
            Thread t = new Thread(new ManagePolledMessage(notification, id));

            t.start();
        } // end if

    }// end method

    /**
     * Entry point for CMIngestor launch
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
            /**
             * Create an instance of CM ingestor
             */
            CMIngestor ingestor = new CMIngestor(); // A new CMIngestor
                                                    // component
        } // end try
        catch (InvalidNameException ine)
        {
            System.err.println("Invalid component name");
        } // end catch
    } // end method
} // end class

/**
 * Create perform action on message defined as HDL
 * 
 * @author Amedeo Bancone
 * @version 1.0
 *
 */
class ManagePolledMessage implements Runnable
{

    private CM_INotification hdl;
    private int msgId;

    /**
     * 
     * @param hdl
     * @param id
     */
    public ManagePolledMessage(CM_INotification hdl, int id)
    {
        /*
         * set the handler
         */
        this.hdl = hdl;
        /**
         * set the msd id
         */
        this.msgId = id;
    }// end method

    /**
     * ren method
     */
    @Override
    public void run()
    {

        this.hdl.NewMessageAvailable(this.msgId);

    }// end method

}// end class
