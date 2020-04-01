/**
*
* MODULE FILE NAME:	DSTMMain.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			STUB for DSTM
*
* PURPOSE:			USED To test interfaces toward DSTM
*
* CREATION DATE:	28-09-2017
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
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import CM.CMAPI.CME_MessageType;
import CM.CMAPI.CME_TransferMode;
import CM.CMAPI.CM_Envelope;
import CM.CMAPI.CM_Exception;
import CM.CMAPI.CM_File;
import CM.CMAPI.CM_Filter;
import CM.CMAPI.CM_Message;
import CM.CMAPI.CM_Services;


/**
 * 
 * Main Class
 *
 */
public class DSMTMain 

{
	
	private static final String defaultMsgClass="EventReport";
	
	private static final String defaultCMAddress="IDUGS:S_SMT:S_DSMT";

	
	public static void main(String[] args) 
	
	{
		
		
		//Event Report msg class
		String msgClass;
		
		
		
		//DSMT CM Address
		String cmID;
		
		Options options = new Options();
		options.addOption("h","help",false,"print this message");
		options.addOption("h","purge",false,"purge pending messages");
		options.addOption("m","message-class",true,"Name of Event Report message class");
		options.addOption("a","cm-address",true,"CM address of DSMT");
		
		
		CommandLineParser parser = new DefaultParser();
		
		try 
		{
			CommandLine line = parser.parse(options, args);
			//msgClass=line.getOptionValue('m',defaultMsgClass);
			//cmID=line.getOptionValue('a', defaultCMAddress);
			
			if(line.hasOption('h'))
			{
				help(options);
			}
			
	
			msgClass=line.getOptionValue('m',defaultMsgClass);
			cmID=line.getOptionValue('a', defaultCMAddress);
			
			//System.out.println("Starting DSMT STUB");
			
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				
				@Override
				public void run() {
					//System.out.println("Shutting down DSMT STUB");
					
				}
			}));
			
			CM_Filter filter = new CM_Filter();
			filter.SetClass(msgClass);
            CM_Message msg ;
			
          
            
            
            CM_Services cmServ=new CM_Services(cmID);
            
            //System.out.println("CM Service instance running");
            
            if(line.hasOption('p'))
			{
            	purgePendingMessages(cmServ);
			}
            
			while(true)
			{
				try
        		{
        			//msg = cmServ.PeekMessage(filter);
        			msg = cmServ.GetMessage(filter, 250);
        			//System.out.println(new String(msg.GetBody()));
        			//System.out.println("===================================================");
        			
        			
        		}
        		catch(CM_Exception cme)
        		{
        			
        			//
        		}
				
				
				try 
				{
					Thread.sleep(250);
				} 
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			
			
			
		}//end try
		catch (Exception e) 
		{
			e.printStackTrace();
		}//end catch
		
		
		
		
	} //end method
	
	/**
	 * print help and exit
	 * @param options
	 */
	static private void help(Options options) 
	{
		// This prints out some help
		HelpFormatter formater = new HelpFormatter();
		formater.printHelp("Main", options);
		System.exit(0);
	
	}//end methods
	
	/**
	 * Purge pending messages
	 * @param cm
	 */
	static private void purgePendingMessages(CM_Services cm) {
		try
    	{
    		//retrieve availabe messages
    		CM_Message [] msgs = cm.GetAvailableMessageInfos();
    		int id=0;
    		//delete messages
    		for(int i =0;i<msgs.length;i++){
    			id=(int) msgs[i].GetUniqueID();
    			//delete message with the
    			//specified id
    			cm.DeleteMessage(id);
    			
    			
    		}//end for
    		
    		
    	}//end try
    	catch (Exception e) {
			//do nothing
		}//end catch
	}//end methods


}//end class
