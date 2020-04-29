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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.utils.PropertiesReader;

/**
 * Generic Data Access Ovbject
 * 
 * @author Abed Alissa
 * @version 1.0
 *
 */
public class GenericDAO {

	static final Logger logger = LogManager.getLogger(GenericDAO.class.getName());
	TraceManager tm = new TraceManager();
	private boolean mycon = false;

	// private DataSource _ds = null;
	// private static Vector _debug_cons = null;
	// connection
	protected Connection con = null;
	// TraceManager tm = new TraceManager();

	public boolean isDbConnected(Connection con) {
		// final String CHECK_SQL_QUERY = "SELECT 1";
		try {
			if (!con.isClosed() || con != null) {
				return true;
			}
		} catch (SQLException e) {
			return false;
		}
		return false;
	}

	/**
	 * @author Abed Alissa
	 * @version 1.0
	 * @since
	 * @return connetion
	 * @throws Exception
	 */
	protected Connection initConnection() throws Exception {// Connection to be returned
		if (con != null && con.isValid(1))
			//connection == null || !((ValidConnection) connection).isValid()
		{
			return con;
		} else {
			try {

				// load the Driver Class
				Class.forName(PropertiesReader.getInstance().getProperty("DB_DRIVER_CLASS"));

				// create the connection now
				// by using the configuration
				// parameters: connection string
				// username
				// Passwd
				con = DriverManager.getConnection(PropertiesReader.getInstance().getProperty("DB_URL"),
						PropertiesReader.getInstance().getProperty("DB_USERNAME"),
						PropertiesReader.getInstance().getProperty("DB_PASSWORD"));
			} catch (ClassNotFoundException e) {
				logger.error("ClassNotFoundException initConnection "+e);
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error("SQLException initConnection "+e);

			}
			return con;
		} // end method
	}

	
	/**
	 * @author Abed Alissa
	 * @version 1.0
	 * @date
	 * @return con
	 * @throws Exception
	 */
	public Connection getConnection() throws Exception {
		GenericDAO.logger.debug("Inside method getConnection " + "asked the con: " + con);

		GenericDAO.logger.debug("time before init" + new Date());

		if (con == null || con.isClosed()) {
//			System.out.println("connection was closed!");
			con = initConnection();
		}

		GenericDAO.logger.debug("time after init" + new Date());

		// return connection
		return con;
	}// end method

	/**
	 * Close the con and delete all elements in the Vector "_debug_cons".
	 */
	public void closeConnection() {
		// test the existence of connection
		if (con != null) {
			try {
				// test if the connection is not closed
				if (!con.isClosed()) {
					try {
						String msg = "CLOSED con: " + con;
						// close the connection
						con.close();
						// log
					} // end try
					catch (SQLException e) {
						// Log in case of error
						GenericDAO.logger.error(EventType.APPLICATION_EVENT, "Error closing con!", e.getMessage());
					} // end cacth
				} // end if
			} // end try
			catch (Exception e) {
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
	 * tm.debug("Connection," + " ... instantiated"); con = initConnection(); mycon
	 * = true; }// end method
	 */

	/**
	 * degault constructor
	 * 
	 * @throws NamingException
	 * @throws Exception
	 */
	public GenericDAO() throws NamingException, Exception {
		tm.debug("Connection," + " ... instantiated");
		try {
			// initialize connection
			con = initConnection();
			// put true
			mycon = true;
		}

		catch (Exception e) {

			logger.debug("eccezione init connection");
//            
//            if(con != null)
//            {
//                closeConnection();
//            }
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
	public GenericDAO(Connection c) {

		GenericDAO.logger.debug("Connection," + "USING con " + c);
		// copy connection
		con = c;
	}// end method

	/**
	 * Constructor that receives in input a GenericDao.
	 * 
	 * @param d
	 * @throws Exception
	 */
	public GenericDAO(GenericDAO d) throws Exception {

		con = d.getConnection();

	}// end method

	/**
	 * @author Abed Alissa
	 * @version 1.0
	 * @since
	 */
	public void startTransaction() {
		try {
			// set to false aitocommit
			con.setAutoCommit(false);
			con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		} // end try
		catch (SQLException e) {
			try {
				// close connection in case of error
				con.close();
			} // end try
			catch (SQLException ee) {
				// log on case of error
				GenericDAO.logger.error(EventType.APPLICATION_EVENT, "Error closing con!", e.getMessage());
			} // end catch
		} // end catch
		catch (Exception e) {
			try {
				// close connection in case of error
				con.close();
			} // end try
			catch (SQLException ee) {
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
	public void closeTransaction() {
		try {
			// commit modification on DB
			con.commit();
			// close connection
			con.close();
		} // end try
		catch (SQLException e) {
			try {

				if (!con.isClosed()) {
					// in case of error
					// rollback
					con.rollback();
					// close
					con.close();
				}
			} // end try
			catch (SQLException ee) {
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
	public void rollback() {
		try {
			// roll modification
			con.rollback();
			// close connection
			con.close();
		} // end try
		catch (SQLException e) {
			try {
				// rollback
				con.rollback();
			} // end try
			catch (SQLException ee) {
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
		if (st != null) {
			try {
				// close stm
				st.close();
				// put to null
				st = null;
			} // end try
			catch (SQLException e) {
				// TODO Auto-generated catch block
				// log error
				// e.printStackTrace();
				GenericDAO.logger.error(EventType.APPLICATION_EVENT, "Error closing statement", e.getMessage());
			} // end cacth
		} // end if

	}// end method
}// end class