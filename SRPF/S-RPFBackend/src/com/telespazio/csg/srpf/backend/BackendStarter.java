/**
*
* MODULE FILE NAME:	BackendStarter.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Perform the inizialization of context for web application
*
* PURPOSE:			perform contecxt initialization
*
* CREATION DATE:	09-03-2017
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
package com.telespazio.csg.srpf.backend;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import com.telespazio.csg.srpf.feasibility.FeasibilityConstants;
import com.telespazio.csg.srpf.feasibility.SparcBeamDB;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.suf.SUFCalculator;
import com.telespazio.csg.srpf.utils.Checksum;
import com.telespazio.csg.srpf.utils.PropertiesReader;

/**
 * 
 * @author Amedeo Bancone
 * @version 1.0
 * 
 *          This class perfrom initialization for the WSControler. It is loaded
 *          at statuup of tomcat, moreover it also start the in memory db for
 *          the ephemerids
 *
 */
@WebListener
public class BackendStarter implements ServletContextListener

{

	// Handles trace logging
	private TraceManager tracer;

	// Configuration properties
	private PropertiesReader propReader;

	// seconds for Ephemerid DB refresh time
	long timeSleep = 20;

	// Scheduler for running tasks. Used to perfor Ephemerid DB refresh
	private ScheduledExecutorService scheduler;

	/**
	 * Initialize context of Web Application. Used By tomcat
	 * 
	 * @param arg0 Context
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		/**
		 * Initialize configuration properties
		 */
		this.propReader = PropertiesReader.getInstance();
		/**
		 * Retrieve configuration file path
		 */
		String configFile = this.propReader.getConfigFile();
		this.tracer = new TraceManager();
		// this.eventMgr = new EventManager();

		// initialize SPARC_BEAM_DB
		SparcBeamDB.getInstance();
		// Initizlize QUM
		SUFCalculator.getInstance();

		//System.out.println("Configured DWL Speed: " + FeasibilityConstants.DWLSpeed);

		this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.SYSTEM_STARTUP, "WSController started");
		// this.eventMgr.information(EventType.APPLICATION_EVENT,
		// ProbableCause.SYSTEM_STARTUP, "WSController started");

		try {
			/**
			 * Evaluate and log confiuration file MD% sum
			 */
			String configFileMD5 = Checksum.getFileChecksum(MessageDigest.getInstance("MD5"), new File(configFile));
			String message = "Configuration read from: " + configFile + ", MD5: " + configFileMD5;
			
			//System.out.println(message);
			// //System.out.println(EventType.APPLICATION_EVENT,
			// ProbableCause.INFORMATION_INFO, "Configuration read");
			this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, message);
		} // end try
		catch (NoSuchAlgorithmException | IOException e) {
			//System.out.println(e.getMessage());
			// logger.error(EventType.APPLICATION_EVENT,
			// ProbableCause.CHECKSUM_VERIFICATION_FAILURE, e.getMessage());
			this.tracer.minor(EventType.APPLICATION_EVENT, ProbableCause.CHECKSUM_VERIFICATION_FAILURE, e.getMessage());
		} // end catch

		/**
		 * Retrieve refresh time for orbital data ephemerid DB
		 */
		String value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.ODBATA_REFRESH_TIME_CONF_KEY);
		if (value != null) {
			try {
				/**
				 * Transform sString to long
				 */
				long lValue = Long.parseLong(value);
				this.timeSleep = lValue;
			} // end try
			catch (Exception e) {
				//System.out.println("error in configuration");

				// logger.error(EventType.SOFTWARE_EVENT,
				// ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Malformed " +
				// FeasibilityConstants.ODBATA_REFRESH_TIME_CONF_KEY + " in configuration");
			} // end catch
		} // end if
		else {
			//System.out.println("error in configuration2");
			// logger.error(EventType.SOFTWARE_EVENT,
			// ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " +
			// FeasibilityConstants.ODBATA_REFRESH_TIME_CONF_KEY + " in configuration");

		} // end else

		/**
		 * Initialize scheduler
		 */
		//System.out.println("opening a new Thread");
		scheduler = Executors.newSingleThreadScheduledExecutor();
		/**
		 * Add to scheduler the ephemerid DB refhesh task. It will be executed every
		 * timeSleep seconds
		 */
		scheduler.scheduleAtFixedRate(new EphemeridSynchronixer(), 0, this.timeSleep, TimeUnit.SECONDS);
		//System.out.println("WSController started");

	}// End method



	/**
	 * This function destroy the server context used by tomcat
	 * 
	 * @param arg0
	 */
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		/**
		 * Stop the scheduler Basically the the refersh of DB is terminated
		 */
		//System.out.println("closing the thread");
		scheduler.shutdownNow();
		try {
			/**
			 * Wait for scheduler termination for max 20 seconds
			 */
			scheduler.awaitTermination(10, TimeUnit.SECONDS);
		} // end try
		catch (InterruptedException e) {
			// nothing to log
			// no make sense perform any operation
		} finally {
			if (!scheduler.isTerminated()) {
				scheduler.shutdown();
			}
		}
		this.tracer.log("WSController stopped");

	}// end method

}// end class
