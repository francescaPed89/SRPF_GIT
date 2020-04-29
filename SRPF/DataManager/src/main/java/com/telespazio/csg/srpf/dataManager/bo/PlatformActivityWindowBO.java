/**
*
* MODULE FILE NAME:	PlatformActivityWindowBO.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Define the Businnes Object for the PAW model stored on DB
*
* PURPOSE:			Used for DB data
*
* CREATION DATE:	11-01-2016
*
* AUTHORS:			Abed Alissa
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.naming.NamingException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.telespazio.csg.srpf.dataManager.bean.PlatformActivityWindowBean;
import com.telespazio.csg.srpf.dataManager.dao.PlatformActivityWindowDAO;
import com.telespazio.csg.srpf.dataManager.util.DataManagerConstants;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.utils.DateUtils;
import com.telespazio.csg.srpf.utils.PropertiesReader;

/**
 *
 * Define the Businnes Object for the PAW model stored on DB
 * 
 * @author Abed Alissa
 * @version 1.0
 *
 */
public class PlatformActivityWindowBO {

	// log class
	TraceManager tm = new TraceManager();

	/**
	 * Delete the PAW Table and insert the data held in xmlFilePath
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param xmlFilePath
	 * @return
	 * @throws NamingException
	 * @throws Exception
	 */
	/*
	 * public boolean importPaw(String xmlFilePath) throws NamingException,
	 * Exception {
	 * 
	 * PlatformActivityWindowDAO dao = new PlatformActivityWindowDAO(); boolean
	 * success = false;
	 * 
	 * try {
	 * 
	 * dao.deleteTablePaw("GSIF_PAW");
	 * 
	 * File xmlFile = new File(xmlFilePath); DocumentBuilderFactory dbFactory =
	 * DocumentBuilderFactory .newInstance();
	 * 
	 * DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	 * 
	 * dBuilder.setEntityResolver(new EntityResolver() {
	 * 
	 * @Override public InputSource resolveEntity(String publicId, String systemId)
	 * throws SAXException, IOException { if
	 * (systemId.contains("GSIF_PlatformActivityWindow.dtd")) {
	 * 
	 * return new InputSource(new StringReader("")); //return new InputSource(new
	 * FileReader(PropertiesReader.getInstance().getProperty("PAW_FILE")));
	 * 
	 * } else { return null; } } });
	 * 
	 * Document doc = dBuilder.parse(xmlFile); doc.getDocumentElement().normalize();
	 * 
	 * SchemaFactory factory =
	 * SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI); Schema schema
	 * = factory.newSchema(new
	 * File(PropertiesReader.getInstance().getProperty("XSD_PAW")));
	 * schema.newValidator().validate(new DOMSource(doc));
	 * 
	 * tm.debug( "Root element :" + doc.getDocumentElement().getNodeName());
	 * 
	 * NodeList nList = doc.getElementsByTagName("SatelliteSchedule");
	 * 
	 * for (int temp = 0; temp < nList.getLength(); temp++) {
	 * 
	 * Node nNode = nList.item(temp);
	 * 
	 * tm.debug("\nCurrent Element :" + nNode.getNodeName());
	 * 
	 * if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	 * 
	 * Element eElement = (Element) nNode;
	 * 
	 * NodeList nList3 = eElement .getElementsByTagName("SatelliteId"); Node nNode3
	 * = nList3.item(0); Element eElement3 = (Element) nNode3; tm.debug(
	 * "Sat Name : " + eElement3.getAttribute("Value")); String satelliteName =
	 * eElement3.getAttribute("Value");
	 * 
	 * NodeList nList4 = eElement.getElementsByTagName("Activity");
	 * 
	 * for (int temp2 = 0; temp2 < nList4.getLength(); temp2++) { Node nNode4 =
	 * nList4.item(temp2); Element eElement4 = (Element) nNode4; NodeList nList5 =
	 * eElement .getElementsByTagName("ActivityType"); Node nNode5 =
	 * nList5.item(temp2); Element eElement5 = (Element) nNode5; String activityType
	 * = eElement5.getAttribute("Value");
	 * 
	 * int activityId = Integer.parseInt(
	 * eElement4.getElementsByTagName("ActivityId") .item(0).getTextContent());
	 * double activityStartTime = DateUtils .fromEpochToCSKDate(eElement4
	 * .getElementsByTagName( "ActivityStartTime") .item(0).getTextContent());
	 * double activityStopTime = DateUtils .fromEpochToCSKDate(eElement4
	 * .getElementsByTagName( "ActivityStopTime") .item(0).getTextContent());
	 * 
	 * NodeList nList6 = eElement .getElementsByTagName("DeferrableFlag"); Node
	 * nNode6 = nList6.item(temp2); Element eElement6 = (Element) nNode6; String
	 * sdeferrableFlag = eElement6 .getAttribute("Value");
	 * 
	 * tm.debug("Activity Type : " + activityType); tm.debug("Activity Id : " +
	 * eElement4.getElementsByTagName("ActivityId") .item(0).getTextContent());
	 * tm.debug("Activity Start Time : " + eElement4 .getElementsByTagName(
	 * "ActivityStartTime") .item(0).getTextContent());
	 * tm.debug("Activity Stop ime : " + eElement4 .getElementsByTagName(
	 * "ActivityStopTime") .item(0).getTextContent()); tm.debug(
	 * "Deferrable Flag : " + sdeferrableFlag);
	 * 
	 * boolean deferrableFlag = Boolean .parseBoolean(sdeferrableFlag);
	 * 
	 * if (activityId == 0 && activityStartTime == 0 && activityStopTime == 0 &&
	 * sdeferrableFlag == null) { tm.debug("xml file mal formed.");
	 * 
	 * } else if (activityStartTime > activityStopTime) {
	 * 
	 * tm.debug( "activityStartTime must be less than activityStopTime.");
	 * 
	 * } else
	 * 
	 * dao.uploadPaw(satelliteName, activityType, activityId, activityStartTime,
	 * activityStopTime, deferrableFlag); success = true; }
	 * 
	 * }
	 * 
	 * }
	 * 
	 * } //end try catch (FileNotFoundException e) {
	 * tm.critical(EventType.SOFTWARE_EVENT,
	 * "Input File Not Found.",e.getMessage()); throw e;
	 * 
	 * }//end catch
	 * 
	 * catch (SQLException se) {
	 * 
	 * dao.rollback(); tm.critical(EventType.SOFTWARE_EVENT,
	 * "Error inserting data into table: Duplicating data into xml file",se.
	 * getMessage()); throw se;
	 * 
	 * }//end catch catch (IOException io) {
	 * 
	 * tm.critical(EventType.SOFTWARE_EVENT, "Input Output Error.",io.getMessage());
	 * throw io;
	 * 
	 * } //end catch finally { dao.closeTransaction(); dao.closeConnection();
	 * 
	 * }//end finally return success;
	 * 
	 * }// end method
	 * 
	 */
	/**
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param idSatellite
	 * @param initialEpoch
	 * @param finalEpoch
	 * @return
	 * @throws Exception
	 */
	public ArrayList<PlatformActivityWindowBean> selectPawData(int idSatellite, double initialEpoch, double finalEpoch)
			throws Exception {

		PlatformActivityWindowDAO dao = null;
		// ArrayList<PlatformActivityWindowBean> pawListRid = new
		// ArrayList<PlatformActivityWindowBean>();
		ArrayList<PlatformActivityWindowBean> pawListAll = new ArrayList<>();

		try {
			dao = new PlatformActivityWindowDAO();

			// ManagerLogger.logInfo(this, ": Inside method selectPawData ");
			this.tm.debug("Inside method selectPawData ");
			pawListAll = dao.selectPawDataAll(idSatellite, initialEpoch, finalEpoch);

			/*
			 * for (int i = 0; i < pawListAll.size(); i++) {
			 * 
			 * PlatformActivityWindowBean pawBean = pawListAll.get(i);
			 * 
			 * double activityStartTime = pawBean.getActivityStartTime(); double
			 * activityStopTime = pawBean.getActivityStopTime();
			 * 
			 * if (activityStartTime < initialEpoch && activityStopTime > initialEpoch) {
			 * 
			 * pawListRid.add(pawBean);
			 * 
			 * } // end if
			 * 
			 * if (activityStartTime > initialEpoch && activityStartTime < finalEpoch) {
			 * 
			 * pawListRid.add(pawBean);
			 * 
			 * } // end if
			 * 
			 * } // end for
			 */

		} // end try
		catch (Exception e) {
			this.tm.critical(EventType.SOFTWARE_EVENT, "Error selected Epochs ", e.getMessage());
			throw e;
		} // end catch
		finally {
			if (dao != null) {
				dao.closeConnection();
			}
		}
		// return pawListRid;
		return pawListAll;
	} // end method

	/**
	 * Return a map holding the paw list for each satellite
	 * 
	 * @param start time
	 * @param stop  stop time
	 * @return paw map
	 * @throws Exception
	 */
	public Map<String, ArrayList<PlatformActivityWindowBean>> getPaws(double start, double stop) throws Exception {
		// map to be returned
		Map<String, ArrayList<PlatformActivityWindowBean>> pawMap = new TreeMap<>();
		PlatformActivityWindowDAO dao = null;

		try {
			dao = new PlatformActivityWindowDAO();

			// ManagerLogger.logInfo(this, ": Inside method selectPawData ");
			this.tm.debug("Inside method getPaws ");
			pawMap = dao.getPaws(start, stop);

		} // end try
		catch (Exception e) {
			// log error
			this.tm.critical(EventType.SOFTWARE_EVENT, "Error selected Epochs ", e.getMessage());
			// rethrow
			throw e;
		} // end catch
		finally {
			// close connection if not null
			if (dao != null) {
				dao.closeConnection();
			}
		} // end finally

		// return map
		return pawMap;
	}// end getPaws

	/**
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param finalEpoch
	 * @throws Exception
	 */
	public void deletePawData(double finalEpoch) throws Exception {

		PlatformActivityWindowDAO dao = null;

		try {
			dao = new PlatformActivityWindowDAO();

			// ManagerLogger.logInfo(this, ": Inside method deletePawData ");
			this.tm.debug("PlatformActivityWindowBO" + ": Inside method deletePawData ");
			dao.deletePawData(finalEpoch);

		} // end try
		catch (Exception e) {
			//System.out.println("Error deleting PAW " + e.getMessage());
			this.tm.critical(EventType.SOFTWARE_EVENT, "Error deleting PAW ", e.getMessage());
			throw e;
		} // end catch
		dao.closeConnection();

	} // end method

	/**
	 * Extract the paw mission from file name
	 * 
	 * @param pawPath
	 * @return The paw mission
	 */
	private String getPawMission(String pawPath) {
		// retval
		String retString = "";
		// file objects from string path
		File file = new File(pawPath);
		// Tokenize name
		StringTokenizer tokens = new StringTokenizer(file.getName(), "_");
		// the first token identify the mission
		retString = tokens.nextToken();
		// returning
		return retString;
	}// end method

	/**
	 * 
	 * @param dateAsString
	 * @param isCSG
	 * @return teh julian 2000 date
	 * @throws java.time.format.DateTimeParseException
	 */
	private double getCSKdate(String dateAsString, boolean isCSG) throws java.time.format.DateTimeParseException {
		// returning value
		double retval = 0;

		if (isCSG) // if CSG PAW ISO Fomat is used
		{
			try {
				// in CSG we can use both date and epoch
				retval = DateUtils.fromISOToCSKDate(dateAsString); // form iso
																	// conversion
			} // end try
			catch (Exception e) {
				retval = DateUtils.fromEpochToCSKDate(dateAsString); // from CSK
																		// epoch
																		// conversion
			} // end catch

		} // end if
		else // CSK use epoch format
		{
			retval = DateUtils.fromEpochToCSKDate(dateAsString); // from CSK
																	// epoch
																	// conversion
		} // end else
			// returning
		return retval;
	}// end method

	/**
	 * Delete activities enclosed between ValidityStartTime and ValidityStopTime of
	 * PAW file, then insert the ones held in file
	 * 
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @param xmlFilePath
	 * @return true in case of success
	 * @throws NamingException
	 * @throws Exception
	 */
	public boolean updatePaw(String xmlFilePath) throws NamingException, Exception {
		boolean success = false;


		PlatformActivityWindowDAO dao = null;

		
		try {

			dao = new PlatformActivityWindowDAO();
			//System.out.println("PlatformActivityWindowBO" + ": Inside method updatePaw ");

			File xmlFile = new File(xmlFilePath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			//System.out.println("PlatformActivityWindowBO" + ": DocumentBuilderFactory");

			dbFactory.setNamespaceAware(true);

			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			//System.out.println("PlatformActivityWindowBO" + ": setEntityResolver");

			// in case of DTD
			// check against DTD
			dBuilder.setEntityResolver(new EntityResolver() {

				@Override
				public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
					if (systemId.contains("GSIF_PlatformActivityWindow.dtd")) {

						return new InputSource(new StringReader(""));
						// return new InputSource(new
						// FileReader(PropertiesReader.getInstance().getProperty("PAW_FILE")));

					} // end if
					else {
						return null;
					} // end else
				}// end method
			});

			// By default we operate on CSK PAW
			String mission = "CSK";
			//System.out.println("PlatformActivityWindowBO" + ": getPawMission");

			String pawMission = getPawMission(xmlFilePath).trim();

			boolean isCSGPaw = false;

			if (pawMission.equalsIgnoreCase("CSG")) {
				// CSG PAW
				isCSGPaw = true;
				mission = pawMission;
			} // end if

			// parse file
			//System.out.println("PlatformActivityWindowBO" + ": parse");

			Document doc = dBuilder.parse(xmlFile);
			//System.out.println("PlatformActivityWindowBO" + ": normalize");

			// normalize doc
			doc.getDocumentElement().normalize();

			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			// get schema file
			Schema schema = factory.newSchema(new File(PropertiesReader.getInstance().getProperty("XSD_PAW")));
			// validate against schema

			//System.out.println("PlatformActivityWindowBO" + ": validate");

			schema.newValidator().validate(new DOMSource(doc));

			// //System.out.println(
			// "Root element :" + doc.getDocumentElement().getNodeName());

			// Retrieving start and stop time
			NodeList nListTime = doc.getElementsByTagName("ValidityStartTime");
			if (nListTime.getLength() != 1) {
				throw new Exception("Wrong PAW File");
			}

			String startValidityString = nListTime.item(0).getTextContent();
			// double startPawtValidity =
			// DateUtils.fromEpochToCSKDate(startValidityString);
			double startPawtValidity = getCSKdate(startValidityString, isCSGPaw);

			nListTime = doc.getElementsByTagName("ValidityStopTime");
			if (nListTime.getLength() != 1) {
				throw new Exception("Wrong PAW File");
			}
			String stopValidityString = nListTime.item(0).getTextContent();
			// double stopPawtValidity =
			// DateUtils.fromEpochToCSKDate(stopValidityString);
			double stopPawtValidity = getCSKdate(stopValidityString, isCSGPaw);

			// delete all activities
			NodeList nListAct = doc.getElementsByTagName("ActivityType");
			Element ActivityType = (Element) nListAct.item(0);
			String type = ActivityType.getAttribute("Value");

			boolean occulted = false;

			// check if occultation paw
			if (type.equals(DataManagerConstants.OCCULTED_PAW_TYPE)) {
				occulted = true;
			}
			// String type=

			// delete activities
			//System.out.println("PlatformActivityWindowBO" + ": deleteActivityInsideWindow");

			dao.deleteActivityInsideWindow(startPawtValidity, stopPawtValidity, occulted, mission);

			success = true;

			// Retrieving content

			// retrieve schedule
			NodeList nList = doc.getElementsByTagName("SatelliteSchedule");

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				this.tm.debug("\nCurrent Element :" + nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					// sat id
					NodeList nList3 = eElement.getElementsByTagName("SatelliteId");
					Node nNode3 = nList3.item(0);
					Element eElement3 = (Element) nNode3;
					this.tm.debug("Sat Name : " + eElement3.getAttribute("Value"));
					String satelliteName = eElement3.getAttribute("Value");
					// retrieve Activity
					NodeList nList4 = eElement.getElementsByTagName("Activity");

					for (int temp2 = 0; temp2 < nList4.getLength(); temp2++) {
						Node nNode4 = nList4.item(temp2);
						Element eElement4 = (Element) nNode4;
						// activity type
						NodeList nList5 = eElement.getElementsByTagName("ActivityType");
						Node nNode5 = nList5.item(temp2);
						Element eElement5 = (Element) nNode5;
						String activityType = eElement5.getAttribute("Value");

						// Activity ID
						long activityId = Long
								.parseLong(eElement4.getElementsByTagName("ActivityId").item(0).getTextContent());
						// start time
						// double activityStartTime =
						// DateUtils.fromEpochToCSKDate(eElement4.getElementsByTagName("ActivityStartTime").item(0).getTextContent());
						double activityStartTime = getCSKdate(
								eElement4.getElementsByTagName("ActivityStartTime").item(0).getTextContent(), isCSGPaw);

						// stop time
						// double activityStopTime =
						// DateUtils.fromEpochToCSKDate(eElement4.getElementsByTagName("ActivityStopTime").item(0).getTextContent());
						double activityStopTime = getCSKdate(
								eElement4.getElementsByTagName("ActivityStopTime").item(0).getTextContent(), isCSGPaw);

						// deferreable flag
						NodeList nList6 = eElement.getElementsByTagName("DeferrableFlag");
						Node nNode6 = nList6.item(temp2);
						Element eElement6 = (Element) nNode6;
						String sdeferrableFlag = eElement6.getAttribute("Value");
						// logging debug information
						//System.out.println("Activity Type : " + activityType);

						//System.out.println("Deferrable Flag : " + sdeferrableFlag);

						boolean deferrableFlag = Boolean.parseBoolean(sdeferrableFlag);

						if ((activityId == 0) && (activityStartTime == 0) && (activityStopTime == 0)
								&& (sdeferrableFlag == null)) {
							//System.out.println("xml file mal formed.");
							success = false;

						} // end if
						else if (activityStartTime > activityStopTime) {

							//System.out.println("activityStartTime must be less than activityStopTime.");

							success = false;

						} // end else if
						else {
							//System.out.println("PlatformActivityWindowBO" + ": uploadPaw");

							// dao.deleteActivity(satelliteName,activityId);
							// upload
							dao.uploadPaw(satelliteName, activityType, activityId, activityStartTime, activityStopTime,
									deferrableFlag);
							// status true
							success = true;
						} // end else

					} // end for

				} // end if

			} // end for

		} // end try
		catch (FileNotFoundException e) {
			// log
			//System.out.println(EventType.SOFTWARE_EVENT+"Input File Not Found."+ e.getMessage());
			// rethrow
			throw e;

		} // end catch

		catch (SQLException se) {
			// rollback
			if(dao !=null)
			{
				dao.rollback();
			}
		
			// log
			//System.out.println(EventType.SOFTWARE_EVENT+ "Error inserting data into table: Duplicating data into xml file"+	se.getMessage());
			// rethrow
			throw se;

		} // end catch
		catch (IOException io) {

			//System.out.println(EventType.SOFTWARE_EVENT+ "Input output exception."+ io.getMessage());
			// retrow exception
			throw io;

		} // end catch
		finally {
			if(	dao != null)
			{
				// close transaction
				dao.closeTransaction();
				// close connection
				dao.closeConnection();
			}


		} // end finally

		// return status
		return success;

	}// end method

	public List<PlatformActivityWindowBean> getPawsNewMethod(int satId, double startTime,
			double stopTime) {
		// map to be returned
		List<PlatformActivityWindowBean> pawList = new ArrayList<PlatformActivityWindowBean>();
				PlatformActivityWindowDAO dao = null;

				try {
					dao = new PlatformActivityWindowDAO();

					// ManagerLogger.logInfo(this, ": Inside method selectPawData ");
					this.tm.debug("Inside method getPaws ");
					pawList = dao.getPawsList(satId,startTime, stopTime);

				} // end try
				catch (Exception e) {
					// log error
					this.tm.critical(EventType.SOFTWARE_EVENT, "Error selected Epochs ", e.getMessage());
					// rethrow
					try {
						throw e;
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} // end catch
				finally {
					// close connection if not null
					if (dao != null) {
						dao.closeConnection();
					}
				} // end finally

				// return map
				return pawList;
	}

}// end class
