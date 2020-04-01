/**
*
* MODULE FILE NAME:	FeasibilityBroker.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			This class check if the request is a Feasibility or an extension request
*
* PURPOSE:			Feasibility
*
* CREATION DATE:	23-01-2017
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

package com.telespazio.csg.srpf.feasibility;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.PropertiesReader;

/**
 *
 * This class perform the first check in order to choose if the request is a
 * feasibility or an extension
 *
 * @author Amedeo Bancone
 * @version 1.0
 *
 */
public class FeasibilityBroker

{

    /**
     * log
     */
    private TraceManager tracer = new TraceManager();

    /**
     * Path of XSD
     */
    protected String xsdPath = "";

    /**
     * Constructor
     *
     * @throws IOException
     */
    public FeasibilityBroker() throws IOException
    {
        /**
         * Retrieving XSD path
         */
        String value = PropertiesReader.getInstance().getProperty(FeasibilityConstants.XSD_PATH_CONF_KEY);
        if (value != null)
        {

            this.xsdPath = value;
        }
        else
        {
            /**
             * Not configured XSD path
             */
            // logger.fatal("Unable to found " +
            // FeasibilityConstants.XSD_PATH_CONF_KEY + " in conffiguration");
            this.tracer.critical(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + FeasibilityConstants.XSD_PATH_CONF_KEY + " in configuration");
            throw new IOException("Unable to found " + FeasibilityConstants.XSD_PATH_CONF_KEY + " in configuration");
        }
    }// end method

    /**
     * Check the request and choose for Feasibility or Extension
     *
     * @param prlistPath
     * @return
     * @throws Exception
     * @throws TransformerException
     * @throws GridException
     * @throws DateTimeParseException
     */
    public String performFeasibility(final String prlistPath) throws DateTimeParseException, GridException, TransformerException, Exception
    {

        String responsePath = "";

        String listAim;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db;
        db = dbf.newDocumentBuilder();
        Document doc = db.parse(prlistPath);
        Node root = doc.getFirstChild();
        this.tracer.debug("Loaded document " + prlistPath);

        /**
         * Validating doc
         */
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new File(this.xsdPath));
        schema.newValidator().validate(new DOMSource(doc));
        this.tracer.debug("Parsed valid document");

        /**
         * Retrieving list aim
         */
        listAim = getChildElementText(doc, FeasibilityConstants.listAimTagName, FeasibilityConstants.listAimTagNameNS);

        if (listAim.equals(FeasibilityConstants.listAimFeasibilityValue))
        {
            /**
             * Performing Feasibility
             */

            FeasibilityPerformer perf = new FeasibilityPerformer();

            responsePath = perf.performFeasibility(prlistPath);

        } // end if
        else if (listAim.equals(FeasibilityConstants.listAimFeasibilityExtensionValue))
        {
            /**
             * performing extension
             */
            FeasibilityExtensor extensor = new FeasibilityExtensor();
            responsePath = extensor.performExtension(prlistPath, doc);

        }
        else
        {
            /**
             * Wrong list aim A feasibility response with error is returned
             */
            FeasibilityPerformer perf = new FeasibilityPerformer();
            responsePath = perf.dumpWrongListAim(prlistPath, doc);

            this.tracer.critical(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Request of type " + listAim + " are not managed by SRPF");
            // throw new Exception("SRPF not managed request of type: " +
            // listAim);
        }

        return responsePath;
    }// end method

    /**
     * Retrun the text node string of an element
     *
     * @param doc
     * @param childName
     * @return text
     * @throws XPathExpressionException
     */
    private String getChildElementText(Document doc, String childName, String namespace) throws XPathExpressionException

    {
        String retVal = "";

        /**
         * Retrieve text from a child
         */
        NodeList nl = doc.getElementsByTagNameNS(namespace, childName);

        if (nl.getLength() != 0)
        {
            retVal = nl.item(0).getFirstChild().getTextContent();
        }
        /**
         *
         */

        return retVal;
    } // end getChilElementText

}// end class
