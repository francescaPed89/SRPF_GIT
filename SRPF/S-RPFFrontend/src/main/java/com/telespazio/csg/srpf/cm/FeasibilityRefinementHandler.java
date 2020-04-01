/**
*
* MODULE FILE NAME:	FeasibilityRefinementHandler.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			It manage the ingestion of AnalisePRList used to perform refinement
*
* PURPOSE:			Used to interact WSController
*
* CREATION DATE:	22-03-2017
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
import com.telespazio.csg.srpf.exception.ExceptionConstants;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.PropertiesReader;

import CM.CMAPI.CME_MessageType;
import CM.CMAPI.CM_Envelope;
import CM.CMAPI.CM_Exception;
import CM.CMAPI.CM_File;
import CM.CMAPI.CM_Filter;
import CM.CMAPI.CM_INotification;
import CM.CMAPI.CM_Message;
import CM.CMAPI.CM_Services;

/**
 *
 * @author Amedeo Bancone
 *
 *         Class for managing Feasibility refinement requests
 *
 */
public class FeasibilityRefinementHandler implements CM_INotification

{

    private CM_Services cmServ; // Used to communicate with CM
    // private Properties cmIngestorConf; // Configuration parameters for the
    // CMIngestor
    private PropertiesReader cmIngestorConf; // Configuration parameters for the
                                             // CMIngestor

    private TraceManager tracer; // Handles trace logging
    // private EventManager eventMgr; // Handles event logging

    /**
     * Creates a new handler for feasibility requests
     *
     * @param cmServ
     *            Used to communicate with CM
     * @param tracer
     *            Handles trace logging
     * @param eventMgr
     *            Handles event logging
     */
    // public FeasibilityHandler (final CM_Services cmServ, final Properties
    // cmIngestorConf, final TraceManager tracer, final EventManager eventMgr)
    public FeasibilityRefinementHandler(final CM_Services cmServ, final TraceManager tracer)
    {

        this.cmServ = cmServ;
        // this.cmIngestorConf = cmIngestorConf;
        this.cmIngestorConf = PropertiesReader.getInstance();
        this.tracer = tracer;
        // this.eventMgr = eventMgr;
    } // end method

    @Override
    /**
     * Invoked by CM when a new message for a registered class is available
     *
     * @param msgID
     *            ID of the feasibility message to fetch from CM
     */
    public void NewMessageAvailable(final int msgID)
    {

        CM_Filter filter = new CM_Filter(); // Filter for CM message fetching
        filter.SetUniqueID(msgID);
        int timeout = Integer.parseInt(this.cmIngestorConf.getProperty(CMConstants.CM_FEASIBILITY_REF_TIMEOUT, CMConstants.CM_DEFAULT_TIMEOUT)); // Timeout
                                                                                                                                                 // for
                                                                                                                                                 // fetching
                                                                                                                                                 // feasibility
                                                                                                                                                 // requests
                                                                                                                                                 // from
                                                                                                                                                 // CM
        String attachmentPath = null; // Path of the attachment of the
                                      // feasibility message
        String feasibilityFilePath = null; // The path of the moved file
        String result = null; // Result of the feasibility analysis
        CM_Message request = null;

        try
        {
            request = this.cmServ.GetMessage(filter, timeout); // Feasibility
                                                               // message from
                                                               // CM

            this.tracer.log("Received message with ID: " + msgID);
            this.tracer.information(EventType.COMMUNICATION_EVENT, ProbableCause.INFORMATION_INFO, "Received message with ID: " + msgID);
            // this.eventMgr.information(EventType.COMMUNICATION_EVENT,
            // ProbableCause.INFORMATION_INFO, "Received message with ID: " +
            // msgID);
            this.tracer.debug("It stated: " + new String(request.GetBody()));

            CM_File attachment = request.GetAttachments()[0]; // Attachment of
                                                              // the feasibility
                                                              // message
            if (attachment != null)
            {
                attachmentPath = attachment.GetName();
                feasibilityFilePath = moveAttachment(attachmentPath, new Integer(msgID).toString()); // The
                                                                                                     // path
                                                                                                     // of
                                                                                                     // the
                                                                                                     // moved
                                                                                                     // file
                this.tracer.debug("Attachment: " + attachmentPath + " moved to working dir");
                result = performFeasibilityRefinement(feasibilityFilePath);
            } // end if
            else
            {
                this.tracer.log("Missing attachment: generating error response");
                this.tracer.major(EventType.SW_RESOURCE_EVENT, ProbableCause.CORRUPT_DATA, "Missing attachment: generating error response");
                // this.eventMgr.major(EventType.SW_RESOURCE_EVENT,
                // ProbableCause.CORRUPT_DATA, "Missing attachment: generating
                // error response");
                result = generateErrorXML(ExceptionConstants.EXCEPTION_LOCATOR, ExceptionConstants.MISSING_ATTACHMENT_EXCEPTION_CODE, ExceptionConstants.MISSING_ATTACHMENT_EXCEPTION_TEXT);
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
            result = generateErrorXML(ExceptionConstants.EXCEPTION_LOCATOR, ExceptionConstants.INPUT_OUTPUT_EXCEPTION_CODE, ExceptionConstants.INPUT_OUTPUT_EXCEPTION_TEXT);
        } // end catch
        catch (SrpfBackendException_Exception sbe)
        {
            this.tracer.log("SRPF Exception: " + sbe.getMessage());
            this.tracer.major(EventType.APPLICATION_EVENT, ProbableCause.APPLICATION_SUBSYSTEM_FAILURE, "SRPF Exception: " + sbe.getMessage());
            // this.eventMgr.major(EventType.APPLICATION_EVENT,
            // ProbableCause.APPLICATION_SUBSYSTEM_FAILURE, "SRPF Exception: " +
            // sbe.getMessage());
            result = generateErrorXML(ExceptionConstants.EXCEPTION_LOCATOR, ExceptionConstants.PERFORMER_ERROR_EXCEPTION_CODE, ExceptionConstants.PERFORMER_ERROR_EXCEPTION_TEXT);
            this.tracer.debug("Result: " + result);
        } // end catch
        catch (Exception sbe)
        {
            this.tracer.log("SRPF Exception: " + sbe.getMessage());
            this.tracer.major(EventType.APPLICATION_EVENT, ProbableCause.APPLICATION_SUBSYSTEM_FAILURE, "SRPF Exception: " + sbe.getMessage());
            // this.eventMgr.major(EventType.APPLICATION_EVENT,
            // ProbableCause.APPLICATION_SUBSYSTEM_FAILURE, "SRPF Exception: " +
            // sbe.getMessage());
            result = generateErrorXML(ExceptionConstants.EXCEPTION_LOCATOR, ExceptionConstants.PERFORMER_ERROR_EXCEPTION_CODE, ExceptionConstants.PERFORMER_ERROR_EXCEPTION_TEXT);
            this.tracer.debug("Result: " + result);
        } // end catch

        try
        {
            this.tracer.debug("Result: " + result);
            if (result != null)
            {
                CM_Message response = buildResponse(request, result); // Response
                                                                      // to send
                                                                      // to CM
                this.cmServ.SendMessage(response);
                this.tracer.log("Response sent to CM");
            } // end if
        } // end try
        catch (CM_Exception cme)
        {
            this.tracer.log("CM_Exception: " + cme.getMessage());
            this.tracer.major(EventType.COMMUNICATION_EVENT, ProbableCause.REMOTE_NODE_TRANSMISSION_ERROR, "CM Exception: " + cme.getMessage());
            // this.eventMgr.major(EventType.COMMUNICATION_EVENT,
            // ProbableCause.REMOTE_NODE_TRANSMISSION_ERROR, "CM Exception: " +
            // cme.getMessage());
        } // end catch
        finally
        {
            if (attachmentPath != null)
            {
                cleanUp(new File(attachmentPath).getParent());
            } // end if
            /*
             * if (feasibilityFilePath != null) { cleanUp(new
             * File(feasibilityFilePath).getParent()); } // end if
             * log.info("Cleanup complete");
             */
        } // end finally
    } // end method

    /**
     * Moves attachment to a working directory
     *
     * @param attachmentPath
     *            Path of the attachment of the feasibility message
     * @param msgID
     *            ID of the feasibility message fetched from CM
     *
     * @throws IOException
     *             If there are problems with the moving of attachment
     * @return The path of the moved file
     */
    private String moveAttachment(final String attachmentPath, final String msgID) throws IOException
    {
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        // String workingDir =
        // this.cmIngestorConf.getProperty(CMConstants.CM_FEASIBILITY_REF_WORKING_DIR)
        // + "/" + msgID + "_" + currentDateTime; // Working directory where the
        // attachments are moved
        String workingDir = this.cmIngestorConf.getProperty(CMConstants.CM_FEASIBILITY_REF_WORKING_DIR);

        if (workingDir == null)
        {
            String msg = "No working dir configured for FeasibilityRefinementRequest";
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
     * Generates the error XML to include in the body of the response to CM
     *
     * @param locator
     *            The exception generator
     * @param exceptionCode
     *            Exception code to include in the XML
     * @param exceptionText
     *            Exception text to include in the XML
     *
     * @return Generated error XML
     */
    private String generateErrorXML(final String locator, final String exceptionCode, final String exceptionText)
    {
        StringBuffer xmlBuffer = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<ows:Exception exceptionCode=\"");
        xmlBuffer.append(exceptionCode);
        xmlBuffer.append("\" locator=\"");
        xmlBuffer.append(locator);
        xmlBuffer.append("\" xmlns:ows=\"http://www.opengis.net/ows\" " + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + "xsi:schemaLocation=\"http://www.opengis.net/ows owsExceptionReport.xsd \">" + "<ows:ExceptionText>" + exceptionText + "</ows:ExceptionText></ows:Exception>");
        return xmlBuffer.toString();
    } // end method

    /**
     * Contacts the web service (WSController) which will actually perform the
     * feasibility analysis
     *
     * @param feasibilityFilePath
     *            The path of the moved file
     *
     * @return The result of the analysis
     * @throws MalformedURLException
     *             If the URL of the WSController is malformed
     * @throws IOException
     *             If there's a problem in the init phase of the analysis
     * @throws SrpfBackendException_Exception
     *             If the WSController can't perform the analysis
     */
    private String performFeasibilityRefinement(final String feasibilityFilePath) throws IOException, MalformedURLException, SrpfBackendException_Exception
    {
        String wsControllerURL = this.cmIngestorConf.getProperty(CMConstants.WSCONTROLLER_URL); // URL
                                                                                                // to
                                                                                                // invoke
                                                                                                // methods
                                                                                                // from
                                                                                                // WSController
        WSControllerService wscService = new WSControllerService(new URL(wsControllerURL)); // WSController
                                                                                            // web
                                                                                            // service
        WSController wscProxy = wscService.getWSController(); // WSController
                                                              // proxy used to
                                                              // invoke methods
        this.tracer.debug("Starting analysis");
        String result = wscProxy.performRefinement(feasibilityFilePath); // Result
                                                                         // of
                                                                         // the
                                                                         // feasibility
                                                                         // request
        return result;
    } // end method

    /**
     * Build the response to send to CM
     *
     * @param request
     *            The message received from CM
     * @param result
     *            Result of the feasibility analysis
     * @throws SrpfBackendException_Exception
     *             If something goes wrong while handling the request
     * @return The response message to send to CM
     */
    private CM_Message buildResponse(final CM_Message request, final String result)
    {
        CM_Message response = new CM_Message(); // Response to the feasibility
                                                // request
        CM_Envelope envelope = new CM_Envelope(); // Envelope of the response
                                                  // message
        String[] receivers = new String[1]; // Receivers for the response
                                            // message

        receivers[0] = request.GetEnvelope().GetSender();
        envelope.SetReceivers(receivers);
        envelope.SetSender(request.GetEnvelope().GetReceivers()[0]);
        response.SetTransactionID(request.GetTransactionID());
        response.SetType(CME_MessageType.CMC_ResponseMessage);
        response.SetClass(request.GetClass());

        if (result.startsWith(ExceptionConstants.EXCEPTION_XML_START))
        {
            // response.SetBody(result.length(), result.getBytes());
            response.SetBody(result.getBytes().length, result.getBytes());
        } // end if
        else
        {
            addAttachment(response, result);
        } // end else

        response.SetEnvelope(envelope);
        // return
        return response;
    } // end method

    /**
     * Add the attachment to the response to be sent to CM
     * 
     * @param response
     *            The response to be sent to CM
     * @param attachmentPath
     *            Path of the attachment to be sent to CM
     */
    private void addAttachment(CM_Message response, final String attachmentPath)
    {
        CM_File[] attachments = new CM_File[1]; // Array of attachments to send
                                                // back to CM
        CM_File attachment = new CM_File(); // The actual attachment to send
                                            // back to CM
        attachment.SetName(attachmentPath);
        attachment.SetClass(CMConstants.CM_FEASIBILITY_MSG_CLASS);
        attachments[0] = attachment;
        response.SetAttachments(attachments);
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

}// end class
