/**
*
* MODULE FILE NAME:	GenericDAO.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Define a generic Data Object Model used to connect oracle DB
*
* PURPOSE:			Used for DB Connection
*
* CREATION DATE:	08-01-2016
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

package com.telespazio.csg.srpf.dataManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.utils.PropertiesReader;

/**
 * Generic Data Access Ovbject
 * 
 * @author Abed Alissa
 * @version 1.0
 *
 */
public class GenericDAO
{

    static final Logger logger = LogManager.getLogger(GenericDAO.class.getName());

    
    // private DataSource _ds = null;
    // private static Vector _debug_cons = null;
    // connection
    protected Connection con;
   // TraceManager tm = new TraceManager();

    /**
     * @author Abed Alissa
     * @version 1.0
     * @since
     * @return connetion
     * @throws Exception
     */
    protected Connection initConnection() throws Exception
    {
        // Connection to be returned
        Connection connection = null;
        try
        {

            // load the Driver Class
            Class.forName(PropertiesReader.getInstance().getProperty("DB_DRIVER_CLASS"));

            // create the connection now
            // by using the configuration
            // parameters: connection string
            // username
            // Passwd
            connection = DriverManager.getConnection(PropertiesReader.getInstance().getProperty("DB_URL"), PropertiesReader.getInstance().getProperty("DB_USERNAME"), PropertiesReader.getInstance().getProperty("DB_PASSWORD"));
            //System.out.println(connection);
        } // end try
        catch (ClassNotFoundException | SQLException e)
        {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            throw e;

        } // end catch
        return connection;
    } // end method

    /**
     * @author Abed Alissa
     * @version 1.0
     * @date
     * @return con
     * @throws Exception
     */
    public Connection getConnection() throws Exception
    {

        GenericDAO.logger.debug("Inside method getConnection " + "asked the con: " + this.con);
        
        GenericDAO.logger.debug("time before init"+new Date());

        // initialize connection
        this.con = initConnection();
        
        GenericDAO.logger.debug("time after init"+new Date());

        // return connection
        return this.con;
    }// end method

    /**
     * Close the con and delete all elements in the Vector "_debug_cons".
     */
    public void closeConnection()
    {
        // test the existence of connection
        if (this.con != null)
        {
            try
            {
                // test if the connection is not closed
                if (!this.con.isClosed())
                {
                    try
                    {
                        String msg = "CLOSED con: " + this.con;
                        // close the connection
                        this.con.close();
                        // log
                    } // end try
                    catch (SQLException e)
                    {
                        // Log in case of error
                        GenericDAO.logger.error(EventType.APPLICATION_EVENT, "Error closing con!", e.getMessage());
                    } // end cacth
                } // end if
            } // end try
            catch (Exception e)
            {
                // Log in case of error
                GenericDAO.logger.error(EventType.APPLICATION_EVENT, "Error closing con!", e.getMessage());
            } // end catch
        } // end if

    }// end method

    /**
     * Constructor that looks for the datasource.
     * 
     *
     * public GenericDAO(String ds_name) throws NamingException, Exception {
     * 
     * tm.debug("Connection," + " ... instantiated"); con = initConnection();
     * mycon = true; }// end method
     */

    /**
     * degault constructor
     * 
     * @throws NamingException
     * @throws Exception
     */
    public GenericDAO() throws NamingException, Exception
    {
        GenericDAO.logger.debug("Connection," + " ... instantiated");
        //System.out.println("Connection," + " ... instantiated");

        // initialize connection
        try
        {
            this.con = initConnection();

        } // end try
        catch (Exception e)
        {
            
            if(this.con != null)
            {
                this.closeConnection();
            }
            // TODO Auto-generated catch block
            // e.printStackTrace();
            throw e;

        } 
        
        // put true
        
    }// end method

    /**
     * Constructor that receives in input a Connection.
     * 
     * @param c
     */
    public GenericDAO(Connection c)
    {

        GenericDAO.logger.debug("Connection," + "USING con " + c);
        // copy connection
        this.con = c;
    }// end method

    /**
     * Constructor that receives in input a GenericDao.
     * 
     * @param d
     * @throws Exception
     */
    public GenericDAO(GenericDAO d) throws Exception
    {

        this.con = d.getConnection();

    }// end method

    /**
     * @author Abed Alissa
     * @version 1.0
     * @since
     */
    public void startTransaction()
    {
        try
        {
            // set to false aitocommit
            this.con.setAutoCommit(false);
            this.con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        } // end try
        catch (SQLException e)
        {
            try
            {
                // close connection in case of error
                this.con.close();
            } // end try
            catch (SQLException ee)
            {
                // log on case of error
                GenericDAO.logger.error(EventType.APPLICATION_EVENT, "Error closing con!", e.getMessage());
            } // end catch
        } // end catch
        catch (Exception e)
        {
            try
            {
                // close connection in case of error
                this.con.close();
            } // end try
            catch (SQLException ee)
            {
                // log on case of error
                GenericDAO.logger.error(EventType.APPLICATION_EVENT, "Error closing con!", ee.getMessage());
            } // end catch
        } // end catch
    }// end method

    /**
     * @author Abed Alissa
     * @version 1.0
     * @since
     */
    public void closeTransaction()
    {
        try
        {
            // commit modification on DB
            this.con.commit();
            // close connection
            this.con.close();
        } // end try
        catch (SQLException e)
        {
            try
            {

                if (!this.con.isClosed())
                {
                    // in case of error
                    // rollback
                    this.con.rollback();
                    // close
                    this.con.close();
                }
            } // end try
            catch (SQLException ee)
            {
                // log in case of error
                GenericDAO.logger.error(EventType.APPLICATION_EVENT, "Error closing transaction!", ee.getMessage());
            } // end catch
        } // end catch
    }// end method

    /**
     * @author Abed Alissa
     * @version 1.0
     * @since
     */
    public void rollback()
    {
        try
        {
            // roll modification
            this.con.rollback();
            // close connection
            this.con.close();
        } // end try
        catch (SQLException e)
        {
            try
            {
                // rollback
                this.con.rollback();
            } // end try
            catch (SQLException ee)
            {
                // log in case of error
                GenericDAO.logger.error(EventType.APPLICATION_EVENT, "Error on rollback!", ee.getMessage());
            } // end catch
        } // end catch
    }// end method

    /**
     * Close a statement
     * 
     * @author Abed Alissa
     * @version 1.0
     * @since
     * @param st
     */
    public void closeStatement(Statement st)

    {
        // check if statement is null
        if (st != null)
        {
            try
            {
                // close stm
                st.close();
                // put to null
                st = null;
            } // end try
            catch (SQLException e)
            {
                // TODO Auto-generated catch block
                // log error
                // e.printStackTrace();
                GenericDAO.logger.error(EventType.APPLICATION_EVENT, "Error closing statement", e.getMessage());
            } // end cacth
        } // end if

    }// end method
}// end class