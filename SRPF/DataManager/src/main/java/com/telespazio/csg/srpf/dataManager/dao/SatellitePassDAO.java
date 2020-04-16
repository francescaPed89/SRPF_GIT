/**
*
* MODULE FILE NAME:	SatellitePassDAO.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			It modelizes a Satellite Pass Data Abstract Object
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

package com.telespazio.csg.srpf.dataManager.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.telespazio.csg.srpf.dataManager.GenericDAO;
import com.telespazio.csg.srpf.dataManager.bean.SatellitePassBean;
import com.telespazio.csg.srpf.dataManager.bo.SatellitePassBO;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;

/**
 * It modelizes a Satellite Pass Data Abstract Object
 * 
 * @author Amedeo Bancone
 * @version 1.0
 *
 */
public class SatellitePassDAO extends GenericDAO

{

    /**
     * log object
     */
    private TraceManager tm = new TraceManager();

    /**
     * Insert statement
     */
    private static final String SatellitePassInsert = "INSERT INTO SATELLITE_PASS (ID,SATELLITE, ASID,CONTACT_COUNTER, VISIBILITY_START_TIME, VISIBILITY_STOP_TIME) " + "VALUES (SATELLITE_PASS_SEQ.nextval,?,?,?,?,?)";

    /**
     * Default constructor
     * 
     * @throws NamingException
     * @throws Exception
     */
    public SatellitePassDAO() throws NamingException, Exception
    {
        super();
        // TODO Auto-generated constructor stub
    }// end method

    /**
     * Insert a list of satellite pass on db
     * 
     * @param satellitePassList
     * @throws NamingException
     * @throws Exception
     */
    public void uploadsatellitePassList(List<SatellitePassBean> satellitePassList) throws NamingException, Exception

    {

        this.tm.debug("inside SatellitePassDAO.uploadsatellitePassList");
        Map<String, Integer> satelliteMap = getSatelliteMapID();
        // delete pass plan
        deleleSatellitePassList(satellitePassList, satelliteMap);

        PreparedStatement insertStatement = null;
        try

        {
    		if(con==null || con.isClosed())
    		{
    			con = super.initConnection();
    		}
            con.setAutoCommit(false); // no autocommit
            insertStatement = con.prepareStatement(SatellitePassInsert);
            // looping on pass
            for (SatellitePassBean satPass : satellitePassList)
            {

                Integer satelliteId = satelliteMap.get(satPass.getSatelliteName());
                // if no satellite throw
                if (satelliteId == null)
                {
                    throw new Exception("Unkonwn satellite " + satPass.getSatelliteName());
                } // end if

                insertStatement.setInt(1, satelliteId.intValue());
                insertStatement.setString(2, satPass.getAsId());
                insertStatement.setLong(3, satPass.getCntactCounter());
                insertStatement.setDouble(4, satPass.getVisibiliyStart());
                insertStatement.setDouble(5, satPass.getVisibilityStop());
                // executing update
                insertStatement.executeUpdate();

            } // end for

            this.tm.debug("Inserted " + satellitePassList.size() + " entries");
        } // end try

        catch (NoSuchElementException e)

        {
            // rollback
            con.rollback();
            // close statenment
            closeStatement(insertStatement);

        } // end catch

        finally
        {
            // close statement
            closeStatement(insertStatement);
            con.close();
        } // end finally

        this.tm.debug("Data inserted");
    }// end method

    /**
     * Return map holding the association satellite name satellite id
     * 
     * @return a map holding the association satellite name satellite id
     * @throws SQLException
     */
    public Map<String, Integer> getSatelliteMapID() throws SQLException
    {
        // map to be returned
        Map<String, Integer> satMap = new TreeMap<>();

        this.tm.debug("Inside getSatelliteMapID");
        // query string
        String query = "SELECT ID_SATELLITE, SATELLITE_NAME  from SATELLITE ";
        String satName;
        int satId;

        PreparedStatement st = null;
        ResultSet rs = null;
        try
        {
			if (con == null || con.isClosed()) {

				con = initConnection();
				
				}
			st = con.prepareStatement(query);
			
            // executing
            this.tm.debug("Inside method getSatelliteMapID: " + query);
            rs = st.executeQuery();
            // getting data
            while (rs.next())

            {

                satId = rs.getInt("ID_SATELLITE");
                satName = rs.getString("SATELLITE_NAME");

                satMap.put(satName, new Integer(satId));

            } // end

        } // end try
        catch (Exception e)

        {
            // log
            this.tm.critical(EventType.SOFTWARE_EVENT, "Execution error of the query by prepared statement: ", e.getMessage());
            // rethrow
            try {
				throw e;
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

        } // end catch
        finally
        {
            // close rs set
            if (rs != null)
            {
                rs.close();
            }
            // close stm
            st.close();
            con.close();
        } // end finally

        // returning map
        return satMap;
    }// End method

    /**
     * a satellite pass list
     * 
     * @throws SQLException
     */
    private void deleleSatellitePassList(List<SatellitePassBean> satPassList, Map<String, Integer> satelliteMap) throws SQLException
    {
        Statement deleteStatement = null;
        // no autocommit
        con.setAutoCommit(false);
        try
        {
			if (con == null || con.isClosed()) {

				con = initConnection();
				
				}
            // for each pass delete in DB
            for (SatellitePassBean satPass : satPassList)
            {
                Integer satelliteId = satelliteMap.get(satPass.getSatelliteName());
                if (satelliteId == null)
                {
                    continue;
                } // end if
                  // stm string
                String deleteString = "delete from SATELLITE_PASS where SATELLITE=" + satelliteId.intValue() + " and ASID=" + satPass.getAsId() + " and CONTACT_COUNTER=" + satPass.getCntactCounter();
                deleteStatement = con.createStatement();
                deleteStatement.executeUpdate(deleteString);
                closeStatement(deleteStatement);
            } // end for

        } // end try
        catch (Exception e)
        {
            // log
            // rollback
            // rethorw
            this.tm.warning(EventType.APPLICATION_EVENT, ProbableCause.APPLICATION_SUBSYSTEM_FAILURE, e.getMessage());
            con.rollback();
            try {
				throw e;
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        } // end catch
        finally
        {
            // close stm
            if (deleteStatement != null)
            {
                ;
            }
            closeStatement(deleteStatement);
            con.close();
        } // end finally
    }// End Method

    /**
     * Return a list of satellite pass inside the specified window
     * 
     * @param idSatellite
     * @param initialEpoch
     * @param finalEpoch
     * @return list of satellite pass
     * @throws Exception
     */
    public ArrayList<SatellitePassBean> selectSatellitePass(int idSatellite, double initialEpoch, double finalEpoch) throws Exception
    {

        /**
         * Query string
         */
        String query = "SELECT  SATELLITE, ASID , CONTACT_COUNTER, VISIBILITY_START_TIME, VISIBILITY_STOP_TIME" + " FROM SATELLITE_PASS where SATELLITE=" + idSatellite + " and VISIBILITY_START_TIME >" + initialEpoch + " AND VISIBILITY_START_TIME<" + finalEpoch + " AND VISIBILITY_STOP_TIME>" + initialEpoch + " AND VISIBILITY_STOP_TIME<" + finalEpoch;

        this.tm.debug(" selectPawDataAll:  " + query);

        PreparedStatement st = null;
        ResultSet rs = null;
        ArrayList<SatellitePassBean> satPassList = new ArrayList<>();

        SatellitePassBean satPassData = null;
        try
        {
			if (con == null || con.isClosed()) {

				con = initConnection();
				
				}
	        con.setAutoCommit(false); // autocmmint false

			st = con.prepareStatement(query);
            // executing query
            rs = st.executeQuery();
            // getting data
            while (rs.next())
            {
                satPassData = new SatellitePassBean();

                satPassData.setSatelliteId(rs.getInt(1));
                satPassData.setAsId(rs.getString(2));
                satPassData.setContactCounter(rs.getLong(3));
                satPassData.setVisibiliyStart(rs.getDouble(4));
                satPassData.setVisibilityStop(rs.getDouble(5));
                satPassList.add(satPassData);
            } // end while
        } // end catch
        catch (SQLException e)
        {
            this.tm.critical(EventType.SOFTWARE_EVENT, "Errore in SELECT from GSIF PAW: ", e.getMessage());
            // rethroiw
            throw e;

        } // end catch
        finally
        {
            // close rs set
            if (rs != null)
            {
                rs.close();
            }
            // close stm
            st.close();
            con.close();
//            con.close();
        } // end finally
        return satPassList;
    } // end method

    /**
     * Return a list of satellite pass inside the specified window
     * 
     * @param asIdList
     *            list of requested station
     * @param idSatellite
     * @param initialEpoch
     * @param finalEpoch
     * @return list of satellite pass
     * @throws Exception
     */
    public ArrayList<SatellitePassBean> selectSatellitePass(List<String> asIdList, int idSatellite, double initialEpoch, double finalEpoch) throws Exception
    {

        // Building clause related to aslist

        String asIdClause = "";

        boolean isTrue = true;
        // looping against as id
        for (String s : asIdList)
        {
            if (isTrue)
            {
                asIdClause += "sp.ASID=" + s;
                isTrue = false;
            } // end if
            else
            {
                asIdClause += " or sp.ASID=" + s;
            } // end else

        } // end for
        /*
         * String query =
         * "SELECT  SATELLITE, ASID , CONTACT_COUNTER, VISIBILITY_START_TIME, VISIBILITY_STOP_TIME"
         * + " FROM SATELLITE_PASS where SATELLITE=" + idSatellite +
         * " and VISIBILITY_START_TIME >"+ initialEpoch
         * +" AND VISIBILITY_START_TIME< "+ finalEpoch +
         * "AND VISIBILITY_STOP_TIME>" + initialEpoch
         * +"AND VISIBILITY_STOP_TIME<" + finalEpoch + " and (" + asIdClause
         * +")";
         */
        String query = "SELECT sp.SATELLITE, sp.ASID , sp.CONTACT_COUNTER, sp.VISIBILITY_START_TIME, sp.VISIBILITY_STOP_TIME, s.SATELLITE_NAME" + " FROM SATELLITE_PASS sp inner join SATELLITE s on sp.SATELLITE=s.ID_SATELLITE where sp.SATELLITE=" + idSatellite + " and sp.VISIBILITY_START_TIME >" + initialEpoch + " AND sp.VISIBILITY_START_TIME< " + finalEpoch + " AND sp.VISIBILITY_STOP_TIME>" + initialEpoch + " AND sp.VISIBILITY_STOP_TIME<" + finalEpoch + " and (" + asIdClause + ")";

        this.tm.debug(" selectSatellitePass:  " + query);
        con.setAutoCommit(false);
        // no autocommit
        PreparedStatement st =null;
        ResultSet rs = null;
        ArrayList<SatellitePassBean> satPassList = new ArrayList<>();
        // pass
        SatellitePassBean satPassData = null;
        try
        {
        	
			if (con == null || con.isClosed()) {

				con = initConnection();
				
				}
			st = con.prepareStatement(query);
            // executing query
            rs = st.executeQuery();
            // getting data
            while (rs.next())
            {
                satPassData = new SatellitePassBean();
                satPassData.setSatelliteId(rs.getInt(1));
                satPassData.setAsId(rs.getString(2));
                satPassData.setContactCounter(rs.getLong(3));
                satPassData.setVisibiliyStart(rs.getDouble(4));
                satPassData.setVisibilityStop(rs.getDouble(5));
                satPassData.setSatelliteName(rs.getString(6));
                satPassList.add(satPassData);
            } // end while
        } // end try
        catch (SQLException e)
        {
            this.tm.critical(EventType.SOFTWARE_EVENT, "Errore in SELECT from GSIF PAW: ", e.getMessage());
            // rethorwing
            throw e;

        } // end cacth
        finally
        {
            // close rs set
            if (rs != null)
            {
                rs.close();
            }
            // close stm
            st.close();
            con.close();
//            con.close();
        } // end finally
        return satPassList;
    } // end method

    /**
     * Return a Map of satellite pass against satellite inside the specified
     * window
     * 
     * @param asIdList
     * @param initialEpoch
     * @param finalEpoch
     * @return map of satellite pass
     * @throws Exception
     */
    public Map<String, ArrayList<SatellitePassBean>> selectSatellitePass(List<String> asIdList, double initialEpoch, double finalEpoch) throws Exception
    {
        // map to be returned
        Map<String, ArrayList<SatellitePassBean>> satPassMap = new TreeMap<>();

        // Building clause related to aslist

        String asIdClause = "";

        boolean isTrue = true;
        // looping on as id
        for (String s : asIdList)
        {
            if (isTrue)
            {
                asIdClause += "sp.ASID=" + s;
                isTrue = false;
            } // end if
            else
            {
                asIdClause += " or sp.ASID=" + s;
            } // end else

        } // end for
        /*
         * String query =
         * "SELECT  SATELLITE, ASID , CONTACT_COUNTER, VISIBILITY_START_TIME, VISIBILITY_STOP_TIME"
         * + " FROM SATELLITE_PASS where SATELLITE=" + idSatellite +
         * " and VISIBILITY_START_TIME >"+ initialEpoch
         * +" AND VISIBILITY_START_TIME< "+ finalEpoch +
         * "AND VISIBILITY_STOP_TIME>" + initialEpoch
         * +"AND VISIBILITY_STOP_TIME<" + finalEpoch + " and (" + asIdClause
         * +")";
         */
        String query = "SELECT sp.SATELLITE, sp.ASID , sp.CONTACT_COUNTER, sp.VISIBILITY_START_TIME, sp.VISIBILITY_STOP_TIME, s.SATELLITE_NAME" + " FROM SATELLITE_PASS sp inner join SATELLITE s on sp.SATELLITE=s.ID_SATELLITE where " + " sp.VISIBILITY_START_TIME >" + initialEpoch + " AND sp.VISIBILITY_START_TIME< " + finalEpoch + " AND sp.VISIBILITY_STOP_TIME>" + initialEpoch + " AND sp.VISIBILITY_STOP_TIME<" + finalEpoch + " and (" + asIdClause + ") order by s.SATELLITE_NAME, sp.VISIBILITY_START_TIME";

        this.tm.debug(" selectSatellitePass:  " + query);
        // statement
        PreparedStatement st =null;
        ResultSet rs = null;
        ArrayList<SatellitePassBean> satPassList = new ArrayList<>();
        // sta pass
        SatellitePassBean satPassData = null;
        String currentSatellite = "";
        String newSatellite = "";
        try
        {
			if (con == null || con.isClosed()) {

				con = initConnection();
				
				}
			st = con.prepareStatement(query);
			
            // executing query
            rs = st.executeQuery();
            // getting data
            while (rs.next())
            {
                newSatellite = rs.getString(6);
                if (!currentSatellite.equals(newSatellite))
                {
                    currentSatellite = newSatellite;
                    satPassList = new ArrayList<>();
                    satPassMap.put(currentSatellite, satPassList);
                    // added to map
                } // end if
                satPassData = new SatellitePassBean();
                satPassData.setSatelliteId(rs.getInt(1));
                satPassData.setAsId(rs.getString(2));
                satPassData.setContactCounter(rs.getLong(3));
                satPassData.setVisibiliyStart(rs.getDouble(4));
                satPassData.setVisibilityStop(rs.getDouble(5));
                satPassData.setSatelliteName(rs.getString(6));
                satPassList.add(satPassData);
            } // end while
        } // end try
        catch (SQLException e)
        {
            this.tm.critical(EventType.SOFTWARE_EVENT, "Errore in SELECT from GSIF PAW: ", e.getMessage());
            /// rethrow
            throw e;

        } // end catch
        finally
        {
            // close rs set
            if (rs != null)
            {
                rs.close();
            }
            // close stm
            st.close();
           con.close();
        } // end finally
        return satPassMap;
    } // end method

    /**
     * remove from db all the satellite pass whose the stopVisibility is older
     * than finalEpoch
     * 
     * @param finalEpoch
     * @throws SQLException
     */
    public void deletePassOlderThan(double finalEpoch) throws SQLException
    {
        // delete string
        String deleteStatementString = "delete from SATELLITE_PASS where VISIBILITY_STOP_TIME<" + finalEpoch;
        Statement deleteStatement = null;
        try
        {
			if (con == null || con.isClosed()) {

				con = initConnection();
				
				}
            con.setAutoCommit(false); // no autocommit
            deleteStatement = con.createStatement(); // creating
            deleteStatement.executeUpdate(deleteStatementString); // executing
        }
        catch (Exception e)
        {
            // rollback
            con.rollback();
            // rethrow
            try {
				throw e;
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        } // end catch
        finally
        {
            if (deleteStatement != null)
            {
                closeStatement(deleteStatement);
            } // end if
            con.close();
        } // end finally

    }// end method

    /**
     * remove from db all the satellite pass included in the interval
     * 
     * @param initial
     *            epoch
     * @param finalEpoch
     * @throws SQLException
     */
    public void deletePassInTheInterval(double initialEpoch, double finalEpoch) throws SQLException
    {
        // statement string
        String deleteStatementString = "delete from SATELLITE_PASS where VISIBILITY_START_TIME > " + initialEpoch + " and  VISIBILITY_STOP_TIME<" + finalEpoch;
        if (initialEpoch > finalEpoch)
        {
            // throw
            throw new SQLException("Final epoch must be greater than initial epoch");
        } // end if
        Statement deleteStatement = null;
        try
        {			if (con == null || con.isClosed()) {

			con = initConnection();
			
			}
            // autocommit false
            con.setAutoCommit(false);
            deleteStatement = con.createStatement();
            deleteStatement.executeUpdate(deleteStatementString);
        }
        catch ( Exception e)
        {
            // rollback
            con.rollback();
            // rethrow
            try {
				throw e;
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        } // end catch
        finally
        {
            if (deleteStatement != null)
            {
                closeStatement(deleteStatement);
            } // end if
            con.close();
        } // end finally

    }// end method

    /**
     * remove from db passes for the specified satellite whose the
     * stopVisibility is older than finalEpoch
     * 
     * @param sarName
     * @param initial
     *            epoch
     * @param finalEpoch
     * @throws SQLException
     */
    public void deletePassInTheIntervalForSatellite(String sarName, double initialEpoch, double finalEpoch) throws SQLException
    {

        // get sat id
        Integer satId = getSatelliteMapID().get(sarName);
        Statement deleteStatement = null;
        // is sat not exist throw
        if (satId == null)
        {
            throw new SQLException("Satellite " + sarName + " does not exist"); // throwing
        } // end if
          // delete string
        String deleteStatementString = "delete from SATELLITE_PASS where VISIBILITY_START_TIME > " + initialEpoch + " and  VISIBILITY_STOP_TIME<" + finalEpoch + " and SATELLITE=" + satId.intValue();
        if (initialEpoch > finalEpoch)
        {
            throw new SQLException("Final epoch must be greater than initial epoch"); // triowing
        } // end if

        try
        {
			if (con == null || con.isClosed()) {

				con = initConnection();
				
				}
            // no autocommit
            con.setAutoCommit(false);
            deleteStatement = con.createStatement();
            deleteStatement.executeUpdate(deleteStatementString);
        }
        catch (Exception e)
        {
            // rollback
            con.rollback();
            // rethrow
            try {
				throw e;
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        } // end cacth
        finally
        {
            if (deleteStatement != null)
            {
                closeStatement(deleteStatement);
            } // end if
             con.close();
        } // end finally

    }// end method

    /**
     * Update the satellite pass table using the data held in satPassList
     * 
     * @param satellitePassList
     * @throws SQLException
     */
    public void importSatellitePassFromSOE(List<SatellitePassBean> satellitePassList) throws SQLException
    {  
    	 // System.out.println("Inside importSatellitePassFromSOE");

        PreparedStatement updateStatement = null;
        Map<String, Integer> satelliteMap = getSatelliteMapID();
        String update = "UPDATE SATELLITE_PASS SET VISIBILITY_START_TIME=?" + " ,VISIBILITY_STOP_TIME=?" + " WHERE CONTACT_COUNTER=?" + " and SATELLITE=?" + " and ASID=?";

        // String update1 = "UPDATE SATELLITE_PASS SET VISIBILITY_START_TIME=?" + " ,VISIBILITY_STOP_TIME=?" + " WHERE CONTACT_COUNTER=?";

        try

        {
			if (con == null || con.isClosed()) {

				con = initConnection();
				
				}
	
            con.setAutoCommit(false);
            updateStatement = con.prepareStatement(update);
            // updating for each pass
            for (SatellitePassBean satPass : satellitePassList)
            {
                Integer satelliteId = satelliteMap.get(satPass.getSatelliteName());

                if (satelliteId == null)
                {
                    throw new SQLException("Unkonwn satellite " + satPass.getSatelliteName());
                } // end if

                /*
                 * String
                 * update3="UPDATE SATELLITE_PASS SET VISIBILITY_START_TIME=" +
                 * satPass.getVisibiliyStart() +" ,VISIBILITY_STOP_TIME=" +
                 * satPass.getVisibilityStop()
                 * +" WHERE CONTACT_COUNTER="+satPass.getCntactCounter() +
                 * " and SATELLITE=" + satelliteId
                 * +" and ASID="+satPass.getAsId(); updateStatement =
                 * con.prepareStatement(update3);
                 * 
                 * //System.out.println(update3);
                 */
                /*
                 * String
                 * update2="UPDATE SATELLITE_PASS SET VISIBILITY_START_TIME=" +
                 * satPass.getVisibiliyStart() +" ,VISIBILITY_STOP_TIME=" +
                 * satPass.getVisibilityStop()
                 * +" WHERE CONTACT_COUNTER="+satPass.getCntactCounter();
                 * 
                 * //System.out.println(update2);
                 */

                // updateStatement = con.prepareStatement(update2);

                updateStatement.setDouble(1, satPass.getVisibiliyStart());
                updateStatement.setDouble(2, satPass.getVisibilityStop());
                updateStatement.setLong(3, satPass.getCntactCounter());
                // System.out.println(updateStatement);

                updateStatement.setInt(4, satelliteId);
                updateStatement.setString(5, satPass.getAsId());

                updateStatement.executeUpdate();
                // closeStatement(updateStatement);
            } // end for
            // System.out.println("Inserted " + satellitePassList.size() + " entries");

        } // end try

        catch (Exception e)

        {
        	 // System.out.println("NoSuchElementException");

            con.rollback();

//            closeStatement(updateStatement);

        } // end catch

        finally
        {

            closeStatement(updateStatement);
            con.close();
        } // end finally

        this.tm.debug("Data inserted");
    }// end method

}// End Class
