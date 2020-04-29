/**
*
* MODULE FILE NAME:	WSController.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Implements the WSController component, used to receive and handle request from CM Ingestor
*
* PURPOSE:			Web Service
*
* CREATION DATE:	19-2-2016
*
* AUTHORS:			Girolamo Castldo
* 
* DESIGN ISSUE:		2.0
*
* INTERFACES:		
*
* SUBORDINATES:		
*
* MODIFICATION HISTORY:
*
*            
* --------------------------+------------+----------------+-------------------------------
*  20-09-2017 | Amedeo Bancone  |2.0| Modified for Allocation Plan Soe Refinement extension
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.backend;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.telespazio.csg.srpf.dataManager.bo.PlatformActivityWindowBO;
import com.telespazio.csg.srpf.dataManager.bo.SatelliteBO;
import com.telespazio.csg.srpf.dataManager.bo.SatellitePassBO;
import com.telespazio.csg.srpf.dataManager.util.DataManagerConstants;
import com.telespazio.csg.srpf.feasibility.FeasibilityBroker;
import com.telespazio.csg.srpf.feasibility.FeasibilityPerformer;
import com.telespazio.csg.srpf.feasibility.GridException;
import com.telespazio.csg.srpf.feasibility.refiner.FeasibilityRefiner;
//import com.telespazio.csg.srpf.logging.EventManager;
import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;
/**
 *  Implements the WSController component, used to receive and handle request from CM Ingestor
 *  @author Girolamo Castaldo
 *
 *  
 *  @version 2.0 Modified for Allocation Plan Soe Refinement extension
 *  
 */
public class WSController 
{
    private TraceManager tracer = new TraceManager(); // Handles trace logging
    //private EventManager eventMgr; // Handles event logging
    
	
    /**
	 * Handles feasibility requests
	 * @param requestPath Path to the request xml
	 * @return Path to the response xml
	 * @throws SrpfBackendException If there's an error during feasibility performance
	 */
    public String performFeasibility(String requestPath) throws SrpfBackendException 
    {		
        this.tracer.log("performFeasibility request: " + requestPath);
        this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "PerformFeasibility request received: " + Paths.get(requestPath).getFileName().toString());
        //this.eventMgr.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "performFeasibility request received");

        File requestFile = new File(requestPath);

        if (!requestFile.exists())
        {
            this.tracer.log("File not found");
            this.tracer.major(EventType.SW_RESOURCE_EVENT, ProbableCause.FILE_ERROR, "File not found");
            throw new SrpfBackendException("File not found");
        } // end if

        //String xsdPath = this.propReader.getProperty(WSControllerConstants.FEASIBILITY_XSD_PATH_PARAM);
        //String lookupTablePath = this.propReader.getProperty(WSControllerConstants.FEASIBILITY_LOOKUP_TABLE_PATH_PARAM);
        String returnPath = null;
        
        try 
        {
            //FeasibilityPerformer performer = new FeasibilityPerformer(xsdPath, lookupTablePath);
        	FeasibilityBroker broker = new FeasibilityBroker();
        	returnPath=broker.performFeasibility(requestPath);
        	//FeasibilityPerformer performer = new FeasibilityPerformer();
            //returnPath = performer.performFeasibility(requestPath);
        } // end try
        /*catch (IOException | XPathExpressionException | SAXException | ParserConfigurationException | GridException | TransformerException e) 
        {
            this.tracer.log(e.getMessage());
            this.tracer.major(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_PROGRAM_ERROR, e.getMessage());
            throw new SrpfBackendException(e.getMessage());
        } // end catch
        */
        catch (Exception e)
        {
            this.tracer.log(e.getMessage());
            this.tracer.major(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_PROGRAM_ERROR, e.getMessage());
            throw new SrpfBackendException(e.getMessage());
        } // end catch

        this.tracer.log("Feasibility performed successfully");
        this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "Feasibility performed successfully");
        
        //System.gc();
        return returnPath;
    } // end method

    /**
     * Handles ODREF requests
     * @param requestPath Path to the request xml
     * @throws SrpfBackendException If there's an error while storing the new orbital data
     */
    public void manageODREF(String requestPath) throws SrpfBackendException 
    {
        this.tracer.log("manageODREF request: " + requestPath);
        this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "manageODREF request received: " + Paths.get(requestPath).getFileName().toString() );
        //this.eventMgr.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "manageODREF request received");

        File requestFile = new File(requestPath);

        if (!requestFile.exists())
        {
            this.tracer.log("File not found");
            this.tracer.major(EventType.SW_RESOURCE_EVENT, ProbableCause.FILE_ERROR, "File not found");
            throw new SrpfBackendException("File not found");
        } // end if

        try 
        {
        	SatelliteBO bo = new SatelliteBO();
        	bo.updateObdataFileName(requestPath,DataManagerConstants.TYPE_ODREF);
        	
        } // end try
        /*
        catch (IOException | NamingException e) 
        {
            this.tracer.log(e.getMessage());
            this.tracer.major(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_PROGRAM_ERROR, e.getMessage());
            throw new SrpfBackendException(e.getMessage());
        } // end catch
        */
        catch (Exception e) 
        {
            this.tracer.log(e.getMessage());
            this.tracer.major(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_PROGRAM_ERROR, e.getMessage());
            throw new SrpfBackendException(e.getMessage());
        } // end catch
        
        this.tracer.log("ODREF stored successfully");
        this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "ODREF stored successfully");

    } // end method

    /**
     * Handles ODNOM requests
     * @param requestPath Path to the request xml
     * @throws SrpfBackendException If there's an error while storing the new orbital data
     */
	public void manageODNOM(String requestPath) throws SrpfBackendException 
    {
        this.tracer.log("manageODNOM request: " + requestPath);
        this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "manageODNOM request received: " + Paths.get(requestPath).getFileName().toString());
        //this.eventMgr.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "manageODNOM request received");

        File requestFile = new File(requestPath);

        if (!requestFile.exists())
        {
            this.tracer.log("File not found");
            this.tracer.major(EventType.SW_RESOURCE_EVENT, ProbableCause.FILE_ERROR, "File not found");
            throw new SrpfBackendException("File not found");
		} // end if

        try 
        {
        	SatelliteBO bo = new SatelliteBO();
        	bo.updateObdataFileName(requestPath,DataManagerConstants.TYPE_ODNOM);
			
		} // end try
        /*
        catch (IOException | NamingException e) 
        {
            this.tracer.log(e.getMessage());
            this.tracer.major(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_PROGRAM_ERROR, e.getMessage());
            throw new SrpfBackendException(e.getMessage());
        } // end catch
        */
        catch (Exception e) 
        {
            this.tracer.log(e.getMessage());
            this.tracer.major(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_PROGRAM_ERROR, e.getMessage());
            throw new SrpfBackendException(e.getMessage());
        } // end catch
        
        this.tracer.log("ODNOM stored successfully");
        this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "ODNOM stored successfully");

    } // end method

	/**
     * Handles ODMTP requests
     * @param requestPath Path to the request xml
     * @throws SrpfBackendException If there's an error while storing the new orbital data
     */
    public void manageODMTP(String requestPath) throws SrpfBackendException {
        this.tracer.log("manageODMTP request: " + requestPath);
        this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "manageODMTP request received: " + Paths.get(requestPath).getFileName().toString());
        //this.eventMgr.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "manageODMTP request received");

        File requestFile = new File(requestPath);

        if (!requestFile.exists())
        {
            this.tracer.log("File not found");
            this.tracer.major(EventType.SW_RESOURCE_EVENT, ProbableCause.FILE_ERROR, "File not found");
            throw new SrpfBackendException("File not found");
        } // end if

        try 
        {
        	SatelliteBO bo = new SatelliteBO();
        	bo.updateObdataFileName(requestPath,DataManagerConstants.TYPE_ODMTP);
        	
        } // end try
        /*
        catch (IOException | NamingException e) 
        {
            this.tracer.log(e.getMessage());
            this.tracer.major(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_PROGRAM_ERROR, e.getMessage());
            throw new SrpfBackendException(e.getMessage());
        } // end catch
        */
        catch (Exception e) 
        {
            this.tracer.log(e.getMessage());
            this.tracer.major(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_PROGRAM_ERROR, e.getMessage());
            throw new SrpfBackendException(e.getMessage());
        } // end catch
        
        this.tracer.log("ODMTP stored successfully");
        this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "ODMTP stored successfully");

    } // end method

    /**
     * Handles ODSTP requests
     * @param requestPath Path to the request xml
     * @throws SrpfBackendException If there's an error while storing the new orbital data
     */
    public void manageODSTP(String requestPath) throws SrpfBackendException 
    {
        this.tracer.log("manageODSTP request: " + requestPath);
        this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "manageODSTP request received " + Paths.get(requestPath).getFileName().toString());
        //this.eventMgr.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "manageODSTP request received");

        File requestFile = new File(requestPath);

        if (!requestFile.exists())
        {
            this.tracer.log("File not found");
            this.tracer.major(EventType.SW_RESOURCE_EVENT, ProbableCause.FILE_ERROR, "File not found");
            throw new SrpfBackendException("File not found");
        }

        try 
        {
        	SatelliteBO bo = new SatelliteBO();
        	bo.updateObdataFileName(requestPath,DataManagerConstants.TYPE_ODSTP);
        	
        } // end try
        /*
        catch (IOException | NamingException e) 
        {
            this.tracer.log(e.getMessage());
            this.tracer.major(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_PROGRAM_ERROR, e.getMessage());
            throw new SrpfBackendException(e.getMessage());
        } // end catch
        */
        catch (Exception e) 
        {
            this.tracer.log(e.getMessage());
            this.tracer.major(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_PROGRAM_ERROR, e.getMessage());
            throw new SrpfBackendException(e.getMessage());
        } // end catch
        
        this.tracer.log("ODSTP stored successfully");
        this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "ODSTP stored successfully");

    } // end method

    /**
     * Handles PAW requests
     * @param requestPath Path to the request xml
     * @throws SrpfBackendException If there's an error while storing the new orbital data
     */
    public void managePAW(String requestPath) throws SrpfBackendException 
    {
        this.tracer.log("managePAW request: " + requestPath);
        this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "managePAW request received: "+Paths.get(requestPath).getFileName().toString());

        File requestFile = new File(requestPath);

        if (!requestFile.exists())
        {
            this.tracer.log("File not found");
            this.tracer.major(EventType.SW_RESOURCE_EVENT, ProbableCause.FILE_ERROR, "File not found");
            throw new SrpfBackendException("File not found");
        }

        try 
        {
        	//ImportEpochBO oEpochBO = new ImportEpochBO();
			//oEpochBO.uploadODMNRS(requestPath, DataManagerConstants.TYPE_PAW);
        	PlatformActivityWindowBO platformActivityWindowBO = new PlatformActivityWindowBO();
        	if (!platformActivityWindowBO.updatePaw(requestPath))
        	{
        		this.tracer.log("Couldn't update PAW");
        		this.tracer.major(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_PROGRAM_ERROR, "Couldn't update PAW");
        		throw new SrpfBackendException("Couldn't update PAW");
        	}
        } // end try
        /*
        catch (IOException | NamingException e) 
        {
            this.tracer.log(e.getMessage());
            this.tracer.major(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_PROGRAM_ERROR, e.getMessage());
            throw new SrpfBackendException(e.getMessage());
        } // end catch
        */
        catch (Exception e) 
        {
            this.tracer.log(e.getMessage());
            this.tracer.major(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_PROGRAM_ERROR, e.getMessage());
            throw new SrpfBackendException(e.getMessage());
        } // end catch
        
        this.tracer.log("PAW stored successfully");
        this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "PAW stored successfully");

    } // end method
    
    
    /**
     * Handles AllocationPlan requestPath
     * @param requestPath Path to the request xml
     * @throws SrpfBackendException If there's an error while storing the new orbital data
     */
    public void manageAllocationPlan(String requestPath) throws SrpfBackendException 
    {
        this.tracer.log("managePAW request: " + requestPath);
        this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "manageAllocatioPlan request received: " + Paths.get(requestPath).getFileName().toString());

        File requestFile = new File(requestPath);

        if (!requestFile.exists())
        {
            this.tracer.log("File not found");
            this.tracer.major(EventType.SW_RESOURCE_EVENT, ProbableCause.FILE_ERROR, "File not found");
            throw new SrpfBackendException("File not found");
        }

        try 
        {
        	//ImportEpochBO oEpochBO = new ImportEpochBO();
			//oEpochBO.uploadODMNRS(requestPath, DataManagerConstants.TYPE_PAW);
        	SatellitePassBO satellitePass = new SatellitePassBO();
        	satellitePass.importSatellitePass(requestPath);
        	
        	
        } // end try
        /*
        catch (IOException | NamingException e) 
        {
            this.tracer.log(e.getMessage());
            this.tracer.major(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_PROGRAM_ERROR, e.getMessage());
            throw new SrpfBackendException(e.getMessage());
        } // end catch
        */
        catch (Exception e) 
        {
            this.tracer.log(e.getMessage());
            this.tracer.major(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_PROGRAM_ERROR, e.getMessage());
            throw new SrpfBackendException(e.getMessage());
        } // end catch
        
        this.tracer.log("Allocation Pan  stored successfully");
        this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "Allocation Pan stored successfully");

    } // end method

    /**
     * Import SOE xml file
     * @param requestPath
     * @throws SrpfBackendException
     */
    public void manageSoe(String requestPath) throws SrpfBackendException 
    {
    	this.tracer.log("manageSoe request: " + requestPath);
        this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "manageAllocatioPlan request received: " + Paths.get(requestPath).getFileName().toString());

        File requestFile = new File(requestPath);
        
        //if the file doesn't exists
        //and excpetion sould be
        //raised
        if (!requestFile.exists())
        {
            this.tracer.log("File not found");
            this.tracer.major(EventType.SW_RESOURCE_EVENT, ProbableCause.FILE_ERROR, "File not found");
            throw new SrpfBackendException("File not found");
        }//end if

        try 
        {
        	//ImportEpochBO oEpochBO = new ImportEpochBO();
			//oEpochBO.uploadODMNRS(requestPath, DataManagerConstants.TYPE_PAW);
        	SatellitePassBO satellitePass = new SatellitePassBO();
        	satellitePass.importSatellitePassFromSOE(requestPath);
        	
        	
        } // end try
        catch (Exception e) 
        {
            this.tracer.log(e.getMessage());
            this.tracer.major(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_PROGRAM_ERROR, e.getMessage());
            throw new SrpfBackendException(e.getMessage());
        } // end catch
        
        this.tracer.log("SOE successfully");
        this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "SOE stored successfully");


    } // end method
   
    /**
     * Perform refinement
     * @param requestPath
     * @return
     * @throws SrpfBackendException
     */
    public String performRefinement(String requestPath) throws SrpfBackendException 
    {
    	this.tracer.log("performRefinement request: " + requestPath);
        this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "performRefinement request received: " + Paths.get(requestPath).getFileName().toString());
        //this.eventMgr.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "performFeasibility request received");

        File requestFile = new File(requestPath);

        if (!requestFile.exists())
        {
            this.tracer.log("File not found");
            this.tracer.major(EventType.SW_RESOURCE_EVENT, ProbableCause.FILE_ERROR, "File not found");
            throw new SrpfBackendException("File not found");
        } // end if

        //String xsdPath = this.propReader.getProperty(WSControllerConstants.FEASIBILITY_XSD_PATH_PARAM);
        //String lookupTablePath = this.propReader.getProperty(WSControllerConstants.FEASIBILITY_LOOKUP_TABLE_PATH_PARAM);
        String returnPath = null;
        
        try 
        {
           FeasibilityRefiner refiner = new FeasibilityRefiner();
           returnPath=refiner.performRefinement(requestPath);
        } // end try
        /*
        catch (IOException | XPathExpressionException | SAXException | ParserConfigurationException | GridException | TransformerException e) 
        {
            this.tracer.log(e.getMessage());
            this.tracer.major(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_PROGRAM_ERROR, e.getMessage());
            throw new SrpfBackendException(e.getMessage());
        } // end catch
        */
        catch (Exception e)
        {
            this.tracer.log(e.getMessage());
            this.tracer.major(EventType.APPLICATION_EVENT, ProbableCause.SOFTWARE_PROGRAM_ERROR, e.getMessage());
            throw new SrpfBackendException(e.getMessage());
        } // end catch

        this.tracer.log("Refinement performed successfully");
        this.tracer.information(EventType.APPLICATION_EVENT, ProbableCause.INFORMATION_INFO, "Refinement performed successfully");
        
        //System.gc();
        return returnPath;

    } // end method
    
    /**
     * This method will never be used because extension arrive as feasiblity
     * @param requestPath
     * @return path
     * @throws SrpfBackendException
     */
    public String performExtension(String requestPath) throws SrpfBackendException 
    {
    	/**
    	 * This should never be arrived
    	 * 
    	 */
    	if(!requestPath.equals("PIPPO))")){
        	throw new SrpfBackendException("Eccezione");
        }//end if
    	
    	return requestPath+"_back";

    } // end method
    
    
    
} // end class