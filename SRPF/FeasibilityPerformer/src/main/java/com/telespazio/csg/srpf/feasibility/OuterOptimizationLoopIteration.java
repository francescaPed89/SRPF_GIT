/**
*
* MODULE FILE NAME:	OuterOptimizationLoopIteration.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Perform an inner optimization loop in Feasibility evaluation of feasibility case spotlight
*
* PURPOSE:			Perform Feasibility
*
* CREATION DATE:	05-02-2016
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

package com.telespazio.csg.srpf.feasibility;

import java.util.List;

/**
 * This class perform one iteration for the outer loop of the optmization algo
 * for strip mode
 *
 * It is characterize by:
 *
 * - number of acquisition - duration - startTime - stopTime - coverage (number
 * of point of the grid captured by the strip)/(number of point in the grid)
 *
 * @author Amedeo Bancone
 * @version 1.0
 *
 */

public class OuterOptimizationLoopIteration

{

    // static final Logger logger =
    // LogManager.getLogger(OuterOptimizationLoopIteration.class.getName());
    /**
     * min start time of AR
     */
    private double startTime;
    /**
     * Max stop Time of AR
     */
    private double stopTime;
    /**
     * Coverage of ARLIST
     */
    private double coverage;
    /**
     * id of iteration
     */
    private int id;

    /**
     * List of iteration
     */
    private List<AcqReq> acqReqList;

    /**
     * number of point not covered by iteration
     */
    private int uncoveredNumberOfPoints = 0;

    /**
     * @return the id
     */
    public int getId()
    {
        return this.id;
    }// end method

    /**
     * @param id
     *            the id to set
     */
    public void setId(int id)
    {
        this.id = id;
    }// end method

    /**
     * Constructor
     *
     * @param acqReqList
     * @param coverage
     */
    public OuterOptimizationLoopIteration(final List<AcqReq> acqReqList, final double coverage, final int uncoveredNumberOfPoints)
    {
        /**
         * Filling parameters
         */
        this.acqReqList = acqReqList;
        this.coverage = coverage;
        this.uncoveredNumberOfPoints = uncoveredNumberOfPoints;
        /**
         * evaluating start e stop time
         */
        evaluateTimes();

    }// end OuterOptimizationLoopIterationStripMode

    /**
     *
     * @return return the number of uncovered grid point in the iteration
     */
    public int getUncoveredNumberOfPoints()
    {
        return this.uncoveredNumberOfPoints;
    }// end method

    /**
     * Evaluate startTime and stopTime
     */
    private void evaluateTimes()

    {
        // logger.info("evaluateTimes");
        /**
         * Initializing min and max
         */
        double minStart = 0;
        double maxStop = 0;
        boolean isFirst = true;
        /**
         * For each AR in list
         */
        for (AcqReq ar : this.acqReqList)
        {
            /**
             * For each DTO in list
             */
            for (DTO dto : ar.getDTOList())
            {

                if (isFirst)
                {
                    /**
                     * In case of first Min and max are the value of start and
                     * stop of DTO
                     */
                    isFirst = false;
                    minStart = dto.getStartTime();
                    maxStop = dto.getStopTime();
                } // end if
                else
                {
                    /**
                     * Else we have perform check and if necessary switch
                     */
                    if (dto.getStartTime() < minStart)
                    {
                        minStart = dto.getStartTime();
                    }

                    if (dto.getStopTime() > maxStop)
                    {
                        maxStop = dto.getStopTime();
                    }
                }

            } // end inner for
        } // end outer for
        /**
         * setting startTime and stoptime
         */
        this.startTime = minStart;
        this.stopTime = maxStop;
    }// evaluateTimes

    /**
     * @return the startTime
     */
    public double getStartTime()
    {
        return this.startTime;
    }// end method

    /**
     * @return the stopTime
     */
    public double getStopTime()
    {
        return this.stopTime;
    }// end method

    /**
     * @return the coverage
     */
    public double getCoverage()
    {
        return this.coverage;
    }// end method

    /**
     * @return the acqReqList
     */
    public List<AcqReq> getAcqReqList()
    {
        return this.acqReqList;
    }// end method

    /**
     * Return the time needed to perform the PR using the results of this
     * iteration
     *
     * @return the time needed to perform the PR using the results of this
     *         iteration
     */
    public double getDuration()
    {
        /**
         * evaluating
         */
        double retVal = (this.stopTime - this.startTime);
        return retVal;
    }// end method

    /**
     * return thhe numeber of acquisition
     *
     * @return
     */
    public int getNumberOfAcquisition()
    {
        return this.acqReqList.size();
    }// end method

    /**
     * Return the number of AcqReq found in this iteration
     *
     * @return number of AcqReq found in this iteration
     */
    public int getAcqNumber()
    {
        return this.acqReqList.size();
    }// end method

    @Override
    public String toString()
    {
        String retString =  "OuterOptimizationLoopIteration [startTime=" + this.startTime + ", stopTime=" + this.stopTime + ", coverage=" + this.coverage + ", id=" + this.id + ",uncoveredNumberOfPoints=" + this.uncoveredNumberOfPoints + "]";
    
        retString= retString +"acqReqList :  ";
        for(int i=0;i<this.acqReqList.size();i++)
        {
            retString= retString + this.acqReqList.get(i)+"\n";
        }
        return retString;
    }

    /**
     * Dump the iteration on a file. for test purposes only
     *
     * @param filePath
     */
    /*
     * public void dumpToFile(String outFilePath)
     *
     * { try { //logger.debug("Dumping to file iteration on file: " +
     * outFilePath); BufferedWriter out = new BufferedWriter(new
     * FileWriter(outFilePath));
     *
     * out.write("Iterazione: " + id + "\n"); out.write("Start : " +
     * DateUtils.fromCSKDateToISOFMTDateTime(startTime)+"\n");
     * out.write("Stop : " +
     * DateUtils.fromCSKDateToISOFMTDateTime(stopTime)+"\n");
     * out.write("Number of AR: " + acqReqList.size()+"\n");
     * out.write("coverage (Points/totalpoints ): " + coverage+"\n");
     *
     * //logger.info("AcqList Size in iteration: " + acqReqList.size());
     * for(AcqReq currentAcq : acqReqList) { String acqString =
     * currentAcq.dumpToString(); out.write(acqString); out.write("\n"); }
     *
     * out.close();
     *
     * } catch (IOException e) { // TODO Auto-generated catch block
     * e.printStackTrace(); } }
     */

    /**
     * for test purposes only
     *
     * @param dir
     * @throws IOException
     */
    /*
     * public void dumpDTOsToFile(String dir) throws IOException { for(AcqReq
     * currentAcq : acqReqList) { currentAcq.dumpArlistOnFiles(dir); } }
     */

    /**
     * Dump the list of DTO in a GPX file For test Purposes only
     *
     * @param fileName
     * @throws IOException
     */
    /*
     * public void dumpSolutionToGPX(String fileName) throws IOException {
     *
     * BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
     *
     * String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
     * "<gpx version=\"1.0\" creator=\"Performer\""+
     * " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""+
     * " xmlns=\"http://www.topografix.com/GPX/1/0\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">"
     * ;
     *
     * String secondSection = "<name>PROVA</name>"+ "<desc>PROVA</desc>"+
     * "<trk>"+ "<name>PROVA</name>"+ "<number>1</number>";
     *
     * out.write(header+"\n"); out.write(secondSection+"\n");
     *
     *
     * DTO d; double [][] corners; for(AcqReq a : this.acqReqList) {
     * out.write("<trkseg>\n"); d=a.getDTOList().get(0);
     *
     * corners = d.getCorners(); double lat; double longitude; for(int i = 0;
     * i<corners.length;i++) { lat = corners[i][0]; longitude = corners[i][1];
     * out.write("<trkpt lat=\""+lat + "\""+" lon=\""+longitude+"\"></trkpt>\n"
     * );
     *
     * }
     *
     * lat = corners[0][0]; longitude = corners[0][1];
     * out.write("<trkpt lat=\""+lat + "\""+" lon=\""+longitude+"\"></trkpt>\n"
     * ); out.write("</trkseg>\n"); }//end for
     *
     *
     * String end = " </trk></gpx>";
     *
     * out.write(end); out.close();
     *
     * }
     */
}// end class