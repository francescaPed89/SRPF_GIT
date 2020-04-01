/**
*
* MODULE FILE NAME:	EphemeridSynchronixer.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Perform the sincornization of orbital data
*
* PURPOSE:			perform context initialization
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


import com.telespazio.csg.srpf.dataManager.inMemoryOrbitalData.EphemeridInMemoryDB;

/**
 * 
 * @author Amedeo Bancone
 * @version 1.0
 * This class perform thye ephemerid synchronizattion
 *
 */
public class EphemeridSynchronixer implements Runnable
{

	
	/**
	 * run the task
	 */
	@Override
	public void run()
	{
		
		try
		{	
			/**
			 *  Refressh the ephemerid
			 */
			EphemeridInMemoryDB.getInstance().refreshDB();
		}//end try
		catch(Throwable e)
		{
			//nothing to log
			//no make sense perform any operation
		}//end cacth
		
				
	}//end method

}//end class
