/**
*
* MODULE FILE NAME:	SatellitePassBO.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			It modellizes a Satellite Pass Buisinness Object
*
* PURPOSE:			Used in passthrough feasibility
*
* CREATION DATE:	13-12-2016
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

package com.telespazio.csg.srpf.dataManager.bo;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.NamingException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.telespazio.csg.srpf.dataManager.bean.SatellitePassBean;
import com.telespazio.csg.srpf.dataManager.dao.SatellitePassDAO;
import com.telespazio.csg.srpf.dataManager.util.DataManagerConstants;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
import com.telespazio.csg.srpf.utils.DateUtils;
import com.telespazio.csg.srpf.utils.PropertiesReader;
import com.telespazio.csg.srpf.utils.XMLUtils;

/**
 *
 * Satellite Pass Class
 * 
 * @author Amedeo Bancone
 * @version 1.0
 *
 */
public class SatellitePassBO

{

    TraceManager tm = new TraceManager();

    // Allocation plan xsd file
    private String allocPlanXSD;

    // SOE xsd file
    private String soeXSD;

    /**
     * Constructor
     * 
     * @throws IOException
     */
    public SatellitePassBO() throws IOException
    {

        // retrieving XSD path for allocation plan
        String value = PropertiesReader.getInstance().getProperty(DataManagerConstants.ALLOC_PLAN_XSD_CONF_KEY_NAME);

        if (value != null)
        {
            // Value not null
            this.allocPlanXSD = value;
        } // end if
        else
        {
            // log
            this.tm.critical(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + DataManagerConstants.ALLOC_PLAN_XSD_CONF_KEY_NAME + " in configuration");
            // Throw
            throw new IOException("Unable to found " + DataManagerConstants.ALLOC_PLAN_XSD_CONF_KEY_NAME + " in configuration");
        } // end else

        // Rerieving XSD PATH for SOE
        value = PropertiesReader.getInstance().getProperty(DataManagerConstants.SOE_XSD_CONF_KEY_NAME);
        if (value != null)
        {
            // not null
            this.soeXSD = value;
        } // end if
        else
        {
            // log
            this.tm.critical(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Unable to found " + DataManagerConstants.SOE_XSD_CONF_KEY_NAME + " in configuration");
            // throw
            throw new IOException("Unable to found " + DataManagerConstants.SOE_XSD_CONF_KEY_NAME + " in configuration");
        } // end else

    }// end method

    /**
     * Return a SatPassList whose validity window is inside the the specified
     * temporal duratiom
     * 
     * @param satId
     * @param initialEpoch
     * @param finalEpoch
     * @return SatPassList
     * @throws Exception
     */
    public ArrayList<SatellitePassBean> selectSatellitePass(int satId, double initialEpoch, double finalEpoch) throws Exception
    {
        // list to be returned
        ArrayList<SatellitePassBean> satPassList = new ArrayList<>();
        SatellitePassDAO dao = null;
        try
        {
            // Accessing DB
            dao = new SatellitePassDAO();
            satPassList = dao.selectSatellitePass(satId, initialEpoch, finalEpoch);
        } // end try
        catch (Exception e)
        {
            // rethrow
            this.tm.critical(EventType.SOFTWARE_EVENT, "Error selected Epochs ", e.getMessage());
            throw e;
        } // end catch
        finally
        {
            // close connection
            if (dao != null)
            {
                dao.closeConnection();
            }
        } // end finally
          // return
        return satPassList;
    }// end methods

    /**
     * Return a SatPassList whose validity window is inside the the specified
     * temporal duratiom
     * 
     * @param asIdList
     *            list of requested station
     * @param satId
     * @param initialEpoch
     * @param finalEpoch
     * @return SatPassList
     * @throws Exception
     */
    public ArrayList<SatellitePassBean> selectSatellitePass(List<String> asIdList, int satId, double initialEpoch, double finalEpoch) throws Exception
    {
        // list to be returned
        ArrayList<SatellitePassBean> satPassList = new ArrayList<>();
        SatellitePassDAO dao = null;
        try
        {
            // accessing DB
            dao = new SatellitePassDAO();
            satPassList = dao.selectSatellitePass(asIdList, satId, initialEpoch, finalEpoch);
        } // end try
        catch (Exception e)
        {
            // Rethrow
            this.tm.critical(EventType.SOFTWARE_EVENT, "Error selected Epochs ", e.getMessage());
            throw e;
        } // end catch
        finally
        {
            // close connection
            if (dao != null)
            {
                dao.closeConnection();
            }
        } // end finally
          // returning list
        return satPassList;
    }// end methods

    /**
     * Return a Map of satellite pass against satellite inside the specified
     * window
     * 
     * @param asIdList
     * @param initialEpoch
     * @param finalEpoch
     * @return Map of satellite pass against satellite inside the specified
     *         window
     * @throws Exception
     */
    public Map<String, ArrayList<SatellitePassBean>> selectSatellitePass(List<String> asIdList, double initialEpoch, double finalEpoch) throws Exception
    {
        // map to be returned
        Map<String, ArrayList<SatellitePassBean>> satPassMap = new TreeMap<>();
        SatellitePassDAO dao = null;
        try
        {
            // accessing DB
            dao = new SatellitePassDAO();
            satPassMap = dao.selectSatellitePass(asIdList, initialEpoch, finalEpoch);
            /*
             * for(Map.Entry<String, ArrayList<SatellitePassBean>> passlist:
             * satPassMap.entrySet()) {
             * //System.out.println("Satllite:"+passlist.getKey()+":"); }
             */
        } // end try
        catch (Exception e)
        {
            // rethrow
            this.tm.critical(EventType.SOFTWARE_EVENT, "Error selected Epochs selectSatellitePass", e.getMessage());
            throw e;
        } // end catch
        finally
        {
            // close connection
            if (dao != null)
            {
                dao.closeConnection();
            }
        } // end finally

        // return map
        return satPassMap;

    }// end method

    /**
     * Update the satellite pass table form a SOE xml
     * 
     * @param filrname
     * @throws Exception
     */
    public List<SatellitePassBean> importSatellitePassFromSOE(String fileName) throws Exception
    {

        // list to be returned
        List<SatellitePassBean> satPassList = new ArrayList<>();
        // Document factory
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        // Take into account name space
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        System.out.println("file of soe data :"+fileName);
        // parsing xml file
        Document doc = dBuilder.parse(fileName);
        System.out.println("Loaded document " + fileName);
        // loading schema
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        
        System.out.println("VALIDATED VERSUS soeXSD SCHEMA " + this.soeXSD);

        Schema schema = factory.newSchema(new File(this.soeXSD));
        // validating xml
        schema.newValidator().validate(new DOMSource(doc));

        // retireving pass bean from XML
        satPassList = getPassListFromSoe(doc);
        
        System.out.println("satPassList " + satPassList);

        
        /*
         * //System.out.println("Trovati: " + satPassList.size());
         * for(SatellitePassBean b: satPassList) {
         * //System.out.println(b.getSatelliteName());
         * //System.out.println(b.getAsId());
         * //System.out.println(b.getCntactCounter());
         * //System.out.println(DateUtils.fromCSKDateToISOFMTDateTime(b.
         * getVisibiliyStart()));
         * //System.out.println(DateUtils.fromCSKDateToISOFMTDateTime(b.
         * getVisibilityStop())); }
         */
        System.out.println("document " + fileName + " is valid");
        // DAO
        SatellitePassDAO dao = null;
        try
        {
            // Accessing DB
            dao = new SatellitePassDAO();
            dao.importSatellitePassFromSOE(satPassList);
            // satPassList = dao.selectSatellitePass(asIdList,satId,
            // initialEpoch, finalEpoch);
        } // end try
        catch (Exception e)
        {
            // rethrow
            this.tm.critical(EventType.SOFTWARE_EVENT, "Error in importing Satellite Pass form SOE ", e.getMessage());
            throw e;
        } // end catch
        finally
        {
            // closeconnetion
            if (dao != null)
            {
                dao.closeConnection();
            }
        } // end finally

        // returning
        return satPassList;

    }// end method

    /**
     * Search X_PASS event in the soe and treturn in the list
     * 
     * @param doc
     * @return Satellite Pass
     * @throws XPathExpressionException
     */
    private List<SatellitePassBean> getPassListFromSoe(Document doc) throws XPathExpressionException
    {
        // List to be returned
        List<SatellitePassBean> satPassList = new ArrayList<>();
        // Soe Element List
        NodeList satelliteEventNodeList = doc.getElementsByTagNameNS(DataManagerConstants.SatelliteEventNS, DataManagerConstants.SatelliteEventTagName);
        
        System.out.println("satelliteEventNodeList"+satelliteEventNodeList);
        // list lenght
        int numberofSatelliteEvent = satelliteEventNodeList.getLength();
        System.out.println("numberofSatelliteEvent"+numberofSatelliteEvent);

        // temporary elements
        Element currentSatelliteEvent;
        // Element currentEventType;
        SatellitePassBean currentSatellitePass;
        String currentEventTypeValue = "";
        String currentExtAngleAsString = "";
        double currentExtAngle = 0;

        // int counter=0;
        // for each element in the list
        // check if xpass
        // then if true add to return list
        for (int i = 0; i < numberofSatelliteEvent; i++)
        {
            currentSatelliteEvent = (Element) satelliteEventNodeList.item(i);
            System.out.println("currentSatelliteEvent"+currentSatelliteEvent);
            currentExtAngle = 0;
//X_PASS   
            currentEventTypeValue = XMLUtils.getChildElementText(currentSatelliteEvent, DataManagerConstants.EventTypeSOETagName, DataManagerConstants.EventTypeSOENS);
            currentExtAngleAsString = null;
            currentExtAngleAsString = XMLUtils.getChildElementText(currentSatelliteEvent, DataManagerConstants.SOEExtAngleTagName, DataManagerConstants.SOEExtAngleTagNameNS);
            System.out.println("currentExtAngleAsString"+currentExtAngleAsString);
            System.out.println("currentEventTypeValue"+currentEventTypeValue);

//            try
//            {
            	if(!currentExtAngleAsString.isEmpty() && currentExtAngleAsString !=null)
            	{
            	       currentExtAngle = Double.parseDouble(currentExtAngleAsString);
            	}

//            } // end try
//            catch (Exception e)
//            {
//                this.tm.warning(EventType.APPLICATION_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, "Wrong Value for XExtAngle in soe: " + e.getMessage());
//                continue;
//            } // end catcth
            
            System.out.println("currentExtAngle"+currentExtAngle);

            if (currentEventTypeValue.equals(DataManagerConstants.EventType_X_PASS_VALUE) && (currentExtAngle == 0))
            {
                // counter++;
                try
                {
                    System.out.println("try to add currentSatellitePass");

                    currentSatellitePass = getSatellitePassFromSatelliteSoe(currentSatelliteEvent);
                    satPassList.add(currentSatellitePass);
                    System.out.println("currentSatelliteEvent AFTER ADD "+currentSatelliteEvent);

                } // end try
                catch (Exception e)
                {
                    // //System.out.println("Eccezione " + e.getMessage());
                    this.tm.warning(EventType.APPLICATION_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, e.getMessage());
                } // end catch
            } // end if
            else
            {
                System.out.println("nothing to add");

            }
        } // end for

        // //System.out.println("Numero di SOE: " + numberofSatelliteSoe);
        // //System.out.println("Numero di X_PASS: " + counter);
        return satPassList;
    }// end method

    /**
     * retrieve a satellite pass from a satellitesole xml element
     * 
     * @param satelliteEvent
     * @return satellite pass
     * @throws XPathExpressionException
     */
    private SatellitePassBean getSatellitePassFromSatelliteSoe(Element satelliteEvent) throws XPathExpressionException
    {
        // value to be returned
        SatellitePassBean satellitePass = new SatellitePassBean();
        // extract sat name

        String satelliteName = XMLUtils.getChildElementText(satelliteEvent, DataManagerConstants.SatelliteTagName, DataManagerConstants.SatelliteTagNameNS);
        satellitePass.setSatelliteName(satelliteName);
        System.out.println("satelliteName "+satelliteName);
        // extract station name
        String stationId = XMLUtils.getChildElementText(satelliteEvent, DataManagerConstants.StationIDTagName, DataManagerConstants.StationIDTagNameNS);
        
        System.out.println("stationId "+stationId);

        // getChildElementText(satelliteEvent,
        // DataManagerConstants.StationNameTagName);

        // map station name qith asid
        String asId = stationId;
        /*
         * String asId=PropertiesReader.getInstance().getProperty(stationId);
         * if(asId==null) { //no match found in configuration throw new
         * XPathExpressionException("Unable to find match StationName <-> asId in configuration for "
         * + ":"+stationId+":"); }//end if
         */
        satellitePass.setAsId(asId);
        System.out.println("asId "+asId);

        // extract contact counter
        String contactCounterString = XMLUtils.getChildElementText(satelliteEvent, DataManagerConstants.SOEEventCounterTagName, DataManagerConstants.SOEEventCounterTagNameNS);
        System.out.println("contactCounterString "+contactCounterString);

        // getChildElementText(satelliteEvent,
        // DataManagerConstants.EventCounterTagName);
        satellitePass.setContactCounter(Long.parseLong(contactCounterString));

        // filling info
        fillSatellitePassTimingInfo(satellitePass, satelliteEvent);
        System.out.println("satellitePass "+satellitePass);

        // return
        return satellitePass;
    }// end method

    /**
     * Extract the event time for soe this time infos shall be used to update
     * satellite pass
     * 
     * @param satPass
     * @param satelliteSoe
     * @throws XPathExpressionException
     */
    private void fillSatellitePassTimingInfo(SatellitePassBean satPass, Element satelliteSoe) throws XPathExpressionException
    {
        // start time
        String timeStartString = "";
        // stop time
        String timeStopString = "";

        // retriving time
        timeStartString = XMLUtils.getChildElementText(satelliteSoe, DataManagerConstants.SOEEventStartTagName, DataManagerConstants.SOEEventStartTagNameNS);
        System.out.println("timeStartString "+timeStartString);

        // retrieving time
        timeStopString = XMLUtils.getChildElementText(satelliteSoe, DataManagerConstants.SOEEventStopTagName, DataManagerConstants.SOEEventStopTagNameNS);
        System.out.println("timeStopString "+timeStopString);

        // conversion
        double appo = DateUtils.fromEpochToCSKDateForSOe(timeStartString);
        
        System.out.println("appo timeStartString"+appo);

        // setting
        satPass.setVisibiliyStart(appo);
        // conversion
        appo = DateUtils.fromEpochToCSKDateForSOe(timeStopString);
        
        System.out.println("appo timeStopString "+appo);

        // setting
        satPass.setVisibilityStop(appo);

    }// end method

    /**
     * Import an Allocation Plan
     * 
     * @param fileName
     * @throws Exception
     * @throws NamingException
     */
    public void importSatellitePass(String fileName) throws NamingException, Exception
    {
        // List to be returned
        List<SatellitePassBean> satPassList = new ArrayList<>();
        // DB FActory
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        // take into account namespace
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        // parse file
        Document doc = dBuilder.parse(fileName);

        this.tm.debug("Loaded document " + fileName);
        // building schema
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new File(this.allocPlanXSD));
        // validating against schema
        schema.newValidator().validate(new DOMSource(doc));

        // retrieving satllite passes list
        NodeList nList = doc.getElementsByTagNameNS(DataManagerConstants.SatellitePassTagNameNS, DataManagerConstants.SatellitePassTagName);

        SatellitePassBean satPass;

        // retrieving the satellite pass
        for (int temp = 0; temp < nList.getLength(); temp++)
        {
            // extract satellite pass bean
            satPass = getSatellitePassBean((Element) (nList.item(temp)));
            if (satPass != null)
            {
                // add bean to list
                satPassList.add(satPass);
            } // end if
        } // end for

        // DAO for access DB
        SatellitePassDAO satellitePassDAO = null;
        satellitePassDAO = new SatellitePassDAO();

        try
        {
            // trying to update DB
            satellitePassDAO.uploadsatellitePassList(satPassList);
        } // encd try
        catch (Exception se)
        {
            // rollback
            satellitePassDAO.rollback();
            this.tm.critical(EventType.SOFTWARE_EVENT, "Error inserting data into table SATELLITE_PASS ", se.getMessage());
            // rethorw
            throw se;
        } // end catch
        finally
        {
            if (satellitePassDAO != null)
            {
                // close transaction
                satellitePassDAO.closeTransaction();
                // close connection
                satellitePassDAO.closeConnection();
            } // end if

        } // end finally

    }// end importSatellitePass

    /**
     * Extract the satellite bean data from the SatellitePass xml element
     * 
     * @param satellitePass
     * @return Satellite pass bean
     * @throws XPathExpressionException
     */
    private SatellitePassBean getSatellitePassBean(Element satellitePass) throws XPathExpressionException
    {
        // value to be retuuned
        SatellitePassBean satPassBean = null;
        // extract is allocated element

        String isAllocated = XMLUtils.getChildElementText(satellitePass, DataManagerConstants.AllocatedContactTagName, DataManagerConstants.AllocatedContactTagNameNS);
        String passType = XMLUtils.getChildElementText(satellitePass, DataManagerConstants.SatPassTypeTagName, DataManagerConstants.SatPassTypeTagNameNS);

        // if is allocated
        // extract values from XML
        // then update satPassBean
        if (isAllocated.equals(DataManagerConstants.AllocatedContactTrueValue) && passType.equals(DataManagerConstants.SatPassTypeX_PassValue))
        {
            satPassBean = new SatellitePassBean();
            // extract as id
            String ASID = XMLUtils.getChildElementText(satellitePass, DataManagerConstants.AcquisitionStationTagName, DataManagerConstants.AcquisitionStationTagNameNS);
            // getChildElementText(satellitePass,
            // DataManagerConstants.ASIDTagName);
            satPassBean.setAsId(ASID);
            // extract contact counter
            Long contactCounter = Long.parseLong(XMLUtils.getChildElementText(satellitePass, DataManagerConstants.ContactCounterSatPassTagName, DataManagerConstants.ContactCounterSatPassTagNameNS));
            satPassBean.setContactCounter(contactCounter);
            // extract start time
            String epoch = XMLUtils.getChildElementText(satellitePass, DataManagerConstants.SatPassTimeStartTagName, DataManagerConstants.SatPassTimeStartTagNameNS);

            double startValidity = DateUtils.fromISOToCSKDate(epoch);
            satPassBean.setVisibiliyStart(startValidity);
            // ectract stop time
            epoch = XMLUtils.getChildElementText(satellitePass, DataManagerConstants.SatPassTimeStopTagName, DataManagerConstants.SatPassTimeStopTagNameNS);
            double stopValidity = DateUtils.fromISOToCSKDate(epoch);
            satPassBean.setVisibilityStop(stopValidity);
            // extract satellite
            // Element satelliteIdElement = (Element)
            // satellitePass.getElementsByTagName(DataManagerConstants.SatelliteIdTagName).item(0);

            String satName = XMLUtils.getChildElementText(satellitePass, DataManagerConstants.SatPassSatelliteTagName, DataManagerConstants.SatPassSatelliteTagNameNS);

            satPassBean.setSatelliteName(satName);

        } // end beam

        return satPassBean;
    }// end SatellitePassBean

    /**
     * remove from db all the satellite pass whose the stopVisibility is older
     * than finalEpoch
     * 
     * @param finalEpoch
     * @throws SQLException
     */
    public void deleteSatPassOlderThan(double finalEpoch) throws NamingException, Exception
    {
        SatellitePassDAO dao = null;
        dao = new SatellitePassDAO();

        try
        {
            dao.deletePassOlderThan(finalEpoch);
        } // end try
        catch (SQLException e)
        {
            // rollback
            dao.rollback();
            // rethrow
            throw e;
        } // end catch
        finally
        {
            if (dao != null)
            {
                // close transaction
                dao.closeTransaction();
                // close connection
                dao.closeConnection();
            } // end if

        } // end finally
    }// end deleteSatPassOlderThan

    /**
     * remove from db all the satellite pass whose the stopVisibility is older
     * than finalEpoch
     * 
     * @param initial
     *            epoch
     * @param finalEpoch
     * @throws Exception
     * @throws NamingException
     */
    public void deletePassInTheInterval(double initialEpoch, double finalEpoch) throws NamingException, Exception
    {
        SatellitePassDAO dao = null;
        dao = new SatellitePassDAO();
        try
        {
            // deleting
            dao.deletePassInTheInterval(initialEpoch, finalEpoch);
        } // end try
        catch (SQLException e)
        {
            // rollback
            dao.rollback();
            // rethorw
            throw e;
        } // end catch
        finally
        {
            if (dao != null)
            {
                // close transaction
                dao.closeTransaction();
                // close connection
                dao.closeConnection();
            } // end if

        } // end finally
    }// end deleteSatPassOlderThan

    /**
     * remove from db passes for the specified satellite whose the
     * stopVisibility is older than finalEpoch
     * 
     * @param sarName
     * @param initial
     *            epoch
     * @param finalEpoch
     * @throws Exception
     * @throws NamingException
     */
    public void deletePassInTheIntervalForSatellite(String sarName, double initialEpoch, double finalEpoch) throws NamingException, Exception
    {
        SatellitePassDAO dao = null;
        dao = new SatellitePassDAO();

        try
        {
            dao.deletePassInTheIntervalForSatellite(sarName, initialEpoch, finalEpoch);
        } // end try
        catch (SQLException e)
        {
            // rollback
            dao.rollback();
            // rethrow
            throw e;
        } // end catch
        finally
        {
            if (dao != null)
            {
                // close transaction
                dao.closeTransaction();
                // close connection
                dao.closeConnection();
            } // end if

        } // end finally
    }// end deletePassInTheIntervalForSatellite

}// end class
