package com.telespazio.csg.srpf.dataManager.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashSet;

import org.junit.Test;

import com.telespazio.csg.srpf.logging.constants.EventType;

public class ConfigurationDaoTest {
    private static String StartComment = "M";

	@Test
	public void testImportConfigurationBeamCsv() throws Exception {
		
		File dir = new File("C:/Users/franc/OneDrive/Desktop/");
		dir.mkdirs();
		File csvFile = new File(dir, "DBConfiguration_ICUGS.psv");
		csvFile.createNewFile();
		
		importConfigurationBeamCsv(csvFile);

}
	
	  /**
     * Import beam form file
     * 
     * @author Abed Alissa
     * @version 1.0
     * @date
     * @param csvFile
     * @throws Exception
     */
    public void importConfigurationBeamCsv(File csvFile) throws Exception
    {
        // populate beam table
        // staring from files
        // reader
        BufferedReader br = null;
        String line = "";
        // split char
        String cvsSplitBy = "|";
        PreparedStatement pstm = null;
        try
        {
            LinkedHashSet<String> allBeams = new LinkedHashSet<>();
            // set autocommit to false
            String[] sensorModeNames;
            br = new BufferedReader(new FileReader(csvFile));
            int i = 0;
            // for each line
            while ((line = br.readLine()) != null)
            {
            	
//            	System.out.println(line);
                if (!line.startsWith(StartComment))
                {
                    // use comma as separator
                    // list tokens
                    sensorModeNames = line.split("\\" + cvsSplitBy);
                    //	System.out.println("sensorModeNames: "+sensorModeNames);

                	 //	System.out.println("lenght: "+sensorModeNames.length);

                    if (sensorModeNames.length > 10)
                    {
                        // extract sensormode
                        String sensorModeName = sensorModeNames[1].trim();
                        // extract beam
                        String beamName = sensorModeNames[8].trim();
           
                        System.out.println(allBeams);
                        // get sensormode id
                        int idSensorMode = 0;
                        while (!allBeams.contains(beamName))
                        {
                            System.out.println("ENTERED FOR BEAM "+beamName);

                            i++;
                            // getting angles
                            double nearOffNadir = Double.parseDouble(sensorModeNames[9].trim());
                            double farOffNadir = Double.parseDouble(sensorModeNames[10].trim());

//                            double nearOffNadirSat = Double.parseDouble(sensorModeNames[6].trim());
//                            // extracting off nadir angles
//                            double farOffNadirSat = Double.parseDouble(sensorModeNames[7].trim());

                            String isEnabledString = sensorModeNames[11].trim();
                            int isEnabled = 0;

                            // evaluating foe enables
                            if (isEnabledString.equalsIgnoreCase("Yes"))
                            {
                                isEnabled = 1;
                            }
                            else
                            {
                                isEnabled = 0;
                            } // end else
                              // extracting dimensions
                            double swDim1 = Double.parseDouble(sensorModeNames[12].trim());
                            double swDim2 = Double.parseDouble(sensorModeNames[13].trim());
                            // extracting durations
                            int dtoMinDuration = Integer.parseInt(sensorModeNames[15].trim());
                            int dtoMaxDuration = Integer.parseInt(sensorModeNames[16].trim());
                            int resTime = Integer.parseInt(sensorModeNames[17].trim());
                            int dtoDurationSquared = 0;
                                                     System.out.println("--------------------------------------------------------------");

                            if(sensorModeNames.length > 18)
                            {                            System.out.println("dtoDurationSquared > 18");

                            	try
                            	{
                                    dtoDurationSquared = Integer.parseInt(sensorModeNames[18].trim());
                            		System.out.println("dtoDurationSquaredOK++++++++++++++++"+dtoDurationSquared);

                            	}
                            	catch(Exception e)
                            	{
                            		dtoDurationSquared = 0;
                            		System.out.println("dtoDurationSquaredERROR*********"+dtoDurationSquared);

                            		continue;
                            	}
                            }
                            System.out.println("dtoDurationSquared"+dtoDurationSquared);

                            // SW_DIM1,SW_DIM2, DTO_MIN_DURATION,
                            // DTO_MAX_DURATION,RES_TIME) VALUES ('" + j + "','"
                            // + sensorModeName + "'" + "," + isSpotLight + ", "
                            // + swDim1 + ", " + swDim2 + ", " + dtoMinDuration
                            // + ", " + dtoMaxDuration + ", " + resTime + " )";
                            // query string
                            String uploadBeam = "INSERT into BEAM ("
                                    + "ID_BEAM, "
                                    + "BEAM_NAME,"
                                    + "NEAR_OFF_NADIR,"
                                    + "FAR_OFF_NADIR, "
                                    +"SENSOR_MODE,"
                                    + "IS_ENABLED,"
                                    + "SW_DIM1,"
                                    + "SW_DIM2,"
                                    + "DTO_MIN_DURATION,"
                                    + "DTO_MAX_DURATION, "
                                    + "RES_TIME, "
                                    + "DTO_DURATION_SQUARED) "
                                    + ""
                                    + "VALUES (" 
                                    + i + ",'" 
                                    + beamName + "'," 
                                    + nearOffNadir + "," 
                                    + farOffNadir + "," 
                                    + idSensorMode + "," 
                                    + isEnabled + "," 
                                    + swDim1 + "," 
                                    + swDim2 + "," 
                                    + dtoMinDuration + "," 
                                    + dtoMaxDuration + "," 
                                    + resTime + ","
                                    +dtoDurationSquared+")";

                            // //System.out.println(uploadBeam);
                            // executing query
                            allBeams.add(beamName);
                            pstm = null;

                        } // end while

                    }
                } // end if
            } // end while
System.out.println(allBeams.size());
        } // end try
        catch (IOException ex)
        {
            // rethrow
            throw ex;
        } // end catch
        finally
        {
            // clse statement
            if (pstm != null)
            {
                pstm.close();
            }
            // close reader
            if (br != null)
            {
                br.close();
            }
        } // end finally
    }// end method	}
}
