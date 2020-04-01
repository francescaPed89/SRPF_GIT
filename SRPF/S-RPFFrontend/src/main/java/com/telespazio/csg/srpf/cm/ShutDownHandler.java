/**
*
* MODULE FILE NAME:	ShutDownHandler.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Helper class to handle external signals (ctrl-c, window close, etc.)
*
* PURPOSE:
*
* CREATION DATE:	21-03-2016
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

import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;

/**
 * Helper class to handle external signals (ctrl-c, window close, etc.)
 *
 * @author Girolamo Castaldo
 * @version 1.0
 */
public class ShutDownHandler extends Thread
{

    private TraceManager tracer; // Handles trace logging
    // private EventManager eventMgr; // Handles event logging

    private CMIngestor ingestor;

    /**
     * Sets the component to monitor
     * 
     * @param componentName
     *            Name of the component to monitor
     * @param tracer
     *            Handles trace logging
     * @param eventMgr
     *            Handles event logging
     */
    public ShutDownHandler(final String componentName, final TraceManager tracer, CMIngestor ingest)
    {
        // set tracer
        this.tracer = tracer;
        // this.eventMgr = eventMgr;
        // set ingestor
        this.ingestor = ingest;
    }// end method

    @Override
    /**
     * Intercepts shutdown signals and performs cleanup
     */
    public void run()
    {
        this.tracer.log("Shutdown signal intercepted: cleaning up before closing");
        this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.SYSTEM_SHUTDOWN, "Shutdown signal intercepted: cleaning up before closing");
        // this.eventMgr.information(EventType.APPLICATION_EVENT,
        // ProbableCause.SYSTEM_SHUTDOWN, "Shutdown signal intercepted: cleaning
        // up before closing");
        // unregister
        // this perform the needed clean up
        unregister();
        this.tracer.debug("Unregistered from CM baskets and notifications");
    }// end method

    /**
     * Unregisters the component from CM baskets and notifications
     */
    private void unregister()
    {
        this.ingestor.unregister();

    } // end method
} // end class
