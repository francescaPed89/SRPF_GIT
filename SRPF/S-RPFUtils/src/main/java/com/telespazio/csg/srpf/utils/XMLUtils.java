/**
*
* MODULE FILE NAME:	XMLUtils.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Provide some utilities to manage an xml DOM
*
* PURPOSE:			Extends the feasibility if AR
*
* CREATION DATE:	8-02-2017
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

package com.telespazio.csg.srpf.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provide some utilities to manage an xml DOM
 * 
 * @author Amedeo Bancone
 * @version 1.0
 *
 */
public class XMLUtils

{

    /**
     * Retrun the text node string of an element
     * 
     * @param element
     * @param childName
     * @return text
     * @throws XPathExpressionException
     */

    public static String getChildElementText(Element element, String childName) throws XPathExpressionException

    {
        // retstring
        String retVal = "";
        // get children list
        NodeList nl = element.getElementsByTagName(childName);

        // no lelement
        if (nl.getLength() == 0)
        {
            return retVal;
        } // end if
          // get text
        retVal = nl.item(0).getFirstChild().getTextContent();

        // returning
        return retVal;
    } // end method

    /**
     * Retrun the text node string of an element
     * 
     * @param element
     * @param childName
     * @param childNS
     * @return text
     * @throws XPathExpressionException
     */
    public static String getChildElementText(Element element, String childName, String childNS) throws XPathExpressionException

    {
        // retstring
        String retVal = "";
        // get children list
        NodeList nl = element.getElementsByTagNameNS(childNS, childName);
        // no child
        if (nl.getLength() == 0)
        {
            return retVal;
        } // end if

        // getting text
        retVal = nl.item(0).getFirstChild().getTextContent();

        // returning
        return retVal;
    } // end method

    /**
     * Write the xml to tehe outfile
     * 
     * @param doc
     * @param outfile
     * @throws TransformerException
     */
    public static void dumpResponseToFile(Document doc, String outfile) throws TransformerException

    {
        // logger.debug("Riempio risposta vuota");
        // file
        File out = new File(outfile);
        // tranformer
        TransformerFactory tfactory = TransformerFactory.newInstance();
        Transformer transformer;
        transformer = tfactory.newTransformer();
        // indent node
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        // 4 balnks
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(out);
        // dumping
        transformer.transform(source, result);

        try
        {
            // trying to set readable for everybody
            out.setReadable(true, false);
        } // end try
        catch (SecurityException ex)
        {
            // do nothing
            ex.printStackTrace();
        } // end catch
    }// end method

    /**
     * Update the value of the texNode of a child element
     * 
     * @param element
     * @param childName
     * @param newText
     * @throws XPathExpressionException
     */
    /*
     * public static void setChildElementText(Element element, String
     * childName,String newText) throws XPathExpressionException {
     * 
     * NodeList nl= element.getElementsByTagName(childName);
     * 
     * if(nl.getLength() == 0) { throw new XPathExpressionException("Element " +
     * childName + " not found"); }
     * 
     * nl.item(0).getFirstChild().setTextContent(newText); }
     */
    /**
     * Update the value of the texNode of a child element (first child only)
     * 
     * @param element
     * @param childName
     * @param newText
     * @param childNS
     * @throws XPathExpressionException
     */
    public static void setChildElementText(Element element, String childName, String newText, String childNS) throws XPathExpressionException
    {
        // getting list
        NodeList nl = element.getElementsByTagNameNS(childNS, childName);

        if (nl.getLength() == 0)
        {
            // do nothing
            // just throw
            throw new XPathExpressionException("Element " + childName + " not found");
        } // end if
          // set text
        nl.item(0).getFirstChild().setTextContent(newText);
    }// end method

    /**
     * Return the first direct child node, null if none
     * 
     * @param father
     * @param childNode
     * @return
     */

    /*
     * public static Node getFirstDirectChild(Element father, String childNode)
     * { Node firstChild=null; Node currentNode;
     * 
     * NodeList nl = father.getElementsByTagName(childNode);
     * 
     * for(int i =0;i< nl.getLength();i++) { currentNode=nl.item(i);
     * if(currentNode.getParentNode().equals(father)) { firstChild=currentNode;
     * break; } }
     * 
     * return firstChild; }
     */
    /**
     * Return the first direct child node, null if none
     * 
     * @param father
     * @param childNode
     * @param childNS
     * @return xml node
     */
    public static Node getFirstDirectChild(Element father, String childNode, String childNS)
    {
        // node to be returned
        Node firstChild = null;
        Node currentNode;
        // get node list
        NodeList nl = father.getElementsByTagNameNS(childNS, childNode);

        // looping
        for (int i = 0; i < nl.getLength(); i++)
        {
            currentNode = nl.item(i);
            if (currentNode.getParentNode().equals(father))
            {
                firstChild = currentNode;
                break;
            } // end if
        } // end for
          // return child
        return firstChild;
    }// end method

    /**
     * Fill a list of text string related child nodes
     * 
     * @param elementFather
     * @param childName
     * @return string list
     */

    public static List<String> getChildElementListText(Element elementFather, String childName)
    {
        // retlist
        List<String> list = new ArrayList<>();
        // getting child node list
        NodeList nl = elementFather.getElementsByTagName(childName);

        String appo;
        // looping on childs
        for (int i = 0; i < nl.getLength(); i++)
        {
            // getting text
            appo = nl.item(i).getFirstChild().getTextContent();

            list.add(appo);
        } // end for
          // returning list
        return list;
    }// end method

    /**
     * Fill a list having text of child
     * 
     * @param elementFather
     * @param childName
     * @param childNS
     * @return List of string
     */
    public static List<String> getChildElementListText(Element elementFather, String childName, String childNS)
    {
        // retlist
        List<String> list = new ArrayList<>();
        // getting child node list
        NodeList nl = elementFather.getElementsByTagNameNS(childNS, childName);

        String appo;
        // looping on childs
        for (int i = 0; i < nl.getLength(); i++)
        {
            // getting text
            appo = nl.item(i).getFirstChild().getTextContent();

            list.add(appo);
        } // end for
          // returning list
        return list;
    }// end method

    /**
     * Create an xml element
     * 
     * @param doc
     * @param namespacePrefixMap
     * @param tagName
     * @param namespaceURI
     * @return xml Element
     */
    public static Element createElement(Document doc, Map<String, String> namespacePrefixMap, String tagName, String namespaceURI)
    {
        // getting prefix from namespace map
        String prefix = namespacePrefixMap.get(namespaceURI);
        Element el = null;

        if (prefix != null)
        {
            // create with prefix
            el = doc.createElement(prefix + ":" + tagName);

        } // end if
        else
        {
            // create using namespace
            el = doc.createElementNS(namespaceURI, tagName);
        } // end else
        return el;
    }// end method

    /**
     * Return a map mapping NamespaceUri with prefix for a given document
     * 
     * @param doc
     * @return namespace map
     */
    public static Map<String, String> createNSPrefixMap(Document doc)
    {
        // map to be returned
        Map<String, String> map = new TreeMap<>();
        // trying to read prefixes form attributes of root node
        NamedNodeMap attrlist = doc.getFirstChild().getAttributes();
        for (int i = 0; i < attrlist.getLength(); i++)
        {
            Node current = attrlist.item(i);
            map.put(current.getNodeValue(), current.getLocalName());
            // map.put(attrlist.item(0), arg1)
        } // end for
          // returning map
        return map;
    }// end method

}// end class
