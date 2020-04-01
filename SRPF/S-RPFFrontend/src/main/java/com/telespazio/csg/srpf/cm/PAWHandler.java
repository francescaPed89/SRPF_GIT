/**
*
* MODULE FILE NAME:	PAWHandler.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			OB Data Ingestion
*
* PURPOSE:			OB Data Ingestion
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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.telespazio.csg.srpf.backend.SrpfBackendException_Exception;
import com.telespazio.csg.srpf.backend.WSController;
import com.telespazio.csg.srpf.backend.WSControllerService;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.PropertiesReader;

import CM.CMAPI.CM_Exception;
import CM.CMAPI.CM_File;
import CM.CMAPI.CM_Filter;
import CM.CMAPI.CM_INotification;
import CM.CMAPI.CM_Message;
import CM.CMAPI.CM_Services;

/**
 * Handles PAW requests by CM
 * 
 * @author Girolamo Castaldo
 * @version 1.0
 */
public class PAWHandler implements CM_INotification
{

    private CM_Services cmServ; // Used to communicate with CM
    private PropertiesReader cmIngestorConf; // Configuration parameters for the
                                             // CMIngestor

    private TraceManager tracer; // Handles trace logging
    // private EventManager eventMgr; // Handles event logging

    /*
     * Creates a new handler for PAW requests
     *
     * @param cmServ Used to communicate with CM
     * 
     * @param tracer Handles trace logging
     * 
     * @param eventMgr Handles event logging
     */
    public PAWHandler(final CM_Services cmServ, final TraceManager tracer)
    {
        this.cmServ = cmServ;
        this.cmIngestorConf = PropertiesReader.getInstance();
        this.tracer = tracer;
        // this.eventMgr = eventMgr;
    } // end method

    @Override
    /**
     * Invoked by CM when a new message for a registered class is available
     *
     * @param msgID
     *            ID of the PAW message to fetch from CM
     */
    public void NewMessageAvailable(final int msgID)
    {
        CM_Filter filter = new CM_Filter(); // Filter for CM message fetching
        filter.SetUniqueID(msgID);
        int timeout = Integer.parseInt(this.cmIngestorConf.getProperty(CMConstants.CM_PAW_TIMEOUT, CMConstants.CM_DEFAULT_TIMEOUT)); // Timeout
                                                                                                                                     // for
                                                                                                                                     // fetching
                                                                                                                                     // ODSTP
                                                                                                                                     // requests
                                                                                                                                     // from
                                                                                                                                     // CM
        String attachmentPath = null; // Path of the attachment of the PAW
                                      // message
        String PAWFilePath = null; // The path of the moved file

        try
        {
            CM_Message request = this.cmServ.GetMessage(filter, timeout); // PAW
                                                                          // message
                                                                          // from
                                                                          // CM

            this.tracer.log("Received message with ID: " + msgID);
            this.tracer.information(EventType.COMMUNICATION_EVENT, ProbableCause.INFORMATION_INFO, "Received message with ID: " + msgID);
            this.tracer.debug("It stated: " + new String(request.GetBody()));

            CM_File attachment = request.GetAttachments()[0]; // Attachment of
                                                              // the PAW message

            if (attachment != null)
            {
                attachmentPath = attachment.GetName();
                PAWFilePath = moveAttachment(attachmentPath, new Integer(msgID).toString()); // The
                                                                                             // path
                                                                                             // of
                                                                                             // the
                                                                                             // moved
                                                                                             // file
                this.tracer.debug("Attachment: " + attachmentPath + " moved to working dir");
                performPAW(PAWFilePath);
                this.tracer.log("Operation completed successfully");
            } // end if
            else
            {
                this.tracer.log("Missing attachment");
                this.tracer.major(EventType.SW_RESOURCE_EVENT, ProbableCause.CORRUPT_DATA, "Missing attachment");
                // this.eventMgr.major(EventType.SW_RESOURCE_EVENT,
                // ProbableCause.CORRUPT_DATA, "Missing attachment");
            } // end else
        } // end try
        catch (CM_Exception cme)
        {
            this.tracer.log("CM_Exception: " + cme.getMessage());
            this.tracer.major(EventType.COMMUNICATION_EVENT, ProbableCause.REMOTE_NODE_TRANSMISSION_ERROR, "CM_Exception: " + cme.getMessage());
            // this.eventMgr.major(EventType.COMMUNICATION_EVENT,
            // ProbableCause.REMOTE_NODE_TRANSMISSION_ERROR, "CM_Exception: " +
            // cme.getMessage());
        } // end catch
        catch (IOException ioe)
        {
            this.tracer.log("IOException: " + ioe.getMessage());
            this.tracer.major(EventType.SW_RESOURCE_EVENT, ProbableCause.FILE_ERROR, "IOException: " + ioe.getMessage());
            // this.eventMgr.major(EventType.SW_RESOURCE_EVENT,
            // ProbableCause.FILE_ERROR, "IOException: " + ioe.getMessage());
        } // end catch
        catch (SrpfBackendException_Exception sbe)
        {
            this.tracer.log("SrpfBackendException: " + sbe.getMessage());
            this.tracer.major(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_PROGRAM_ERROR, "SrpfBackendException: " + sbe.getMessage());
        } // end catch
        catch (Exception sbe)
        {
            this.tracer.log("SRPF Exception: " + sbe.getMessage());
            this.tracer.major(EventType.APPLICATION_EVENT, ProbableCause.APPLICATION_SUBSYSTEM_FAILURE, "SRPF Exception: " + sbe.getMessage());
        }
        finally
        {
            if (attachmentPath != null)
            {
                cleanUp(new File(attachmentPath).getParent());
            } // end if
            /*
             * if (PAWFilePath != null) { cleanUp(new
             * File(PAWFilePath).getParent()); } // end if
             * log.info("Cleanup complete");
             */
        } // end finally
    } // end method

    /**
     * Moves attachment to a working directory
     *
     * @param attachmentPath
     *            Path of the attachment of the PAW message
     * @param msgID
     *            ID of the PAW message fetched from CM
     *
     * @throws IOException
     *             If there are problems with the moving of attachment
     * @return The path of the moved file
     */
    private String moveAttachment(final String attachmentPath, final String msgID) throws IOException
    {
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        // String workingDir =
        // this.cmIngestorConf.getProperty(CMConstants.CM_PAW_WORKING_DIR) + "/"
        // + msgID + "_" + currentDateTime; // Working directory where the
        // attachments are moved
        String workingDir = this.cmIngestorConf.getProperty(CMConstants.CM_PAW_WORKING_DIR);

        if (workingDir == null)
        {
            String msg = "No working dir configured for PAW";
            this.tracer.critical(EventType.APPLICATION_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, msg);
            throw new IOException(msg);
        }

        workingDir = workingDir + "/" + msgID + "_" + currentDateTime; // Working
                                                                       // directory
                                                                       // where
                                                                       // the
                                                                       // attachments
                                                                       // are
                                                                       // moved
        Files.createDirectory(Paths.get(workingDir));
        File attachment = new File(attachmentPath); // File encapsulation of the
                                                    // attachment path
        Path sourceFile = attachment.toPath(); // Path encapsulation of the
                                               // attachment File
        Path destinationFile = new File(workingDir, attachment.getName()).toPath(); // Destination
                                                                                    // path
                                                                                    // of
                                                                                    // the
                                                                                    // attachment
        Files.move(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        // Files.setPosixFilePermissions(Paths.get(workingDir),
        // PosixFilePermissions.fromString("rwxrwxrwx"));
        // Files.setPosixFilePermissions(destinationFile,
        // PosixFilePermissions.fromString("rwxrwxrwx"));
        return destinationFile.toString();
    } // end method

    /**
     * Contacts the web service (WSController) which will actually perform the
     * PAW analysis
     *
     * @param PAWFilePath
     *            The path of the moved file
     *
     * @throws MalformedURLException
     *             If the URL of the WSController is malformed
     * @throws SrpfBackendException_Exception
     *             If the WSController can't perform the analysis
     */
    private void performPAW(final String PAWFilePath) throws MalformedURLException, SrpfBackendException_Exception
    {
        // tracer.log("performPAW");
        String wsControllerURL = this.cmIngestorConf.getProperty(CMConstants.WSCONTROLLER_URL); // URL
                                                                                                // to
                                                                                                // invoke
                                                                                                // methods
                                                                                                // from
                                                                                                // WSController
        // tracer.log("WSControllerURL: " + wsControllerURL);
        WSControllerService wscService = new WSControllerService(new URL(wsControllerURL)); // WSController
                                                                                            // web
                                                                                            // service
        // tracer.log("Got service");
        WSController wscProxy = wscService.getWSController(); // WSController
                                                              // proxy used to
                                                              // invoke methods
        // tracer.log("Got proxy");
        wscProxy.managePAW(PAWFilePath); // Result of the PAW request
        // tracer.log("managePAW end");
    } // end method

    /**
     * Deletes a directory and all contained files
     *
     * @param directory
     *            Path of the directory to delete
     */
    private void cleanUp(final String directory)
    {
        try
        {
            if (directory != null)
            {
                Path directoryPath = Paths.get(directory); // Path containing
                                                           // the files to
                                                           // delete
                SimpleFileVisitor<Path> fileVisitor = new SimpleFileVisitor<Path>() // Used
                                                                                    // to
                                                                                    // traverse
                                                                                    // the
                                                                                    // file
                                                                                    // tree
                {

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
                    {
                        // log.info("Deleting file: " + file.toString());
                        // delete file
                        // and returns
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }// end method
                };

                Files.walkFileTree(directoryPath, fileVisitor);
                // log.info("Deleting directory: " + directoryPath);
                Files.delete(directoryPath);
            } // end if
        } // end try
        catch (IOException ioe)
        {
            this.tracer.log("Cleanup error for " + ioe.getMessage());
            // this.eventMgr.warning(EventType.SW_RESOURCE_EVENT,
            // ProbableCause.FILE_ERROR,
            // SpecificProblem.OPERATION_CLASSIFIED_FILE_DELETION);
        } // end catch
    } // end method
} // end class