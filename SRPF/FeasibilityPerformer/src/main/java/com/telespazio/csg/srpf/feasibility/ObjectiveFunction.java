/**
*
* MODULE FILE NAME:	ObjectiveFunction.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Create function cost
*
* PURPOSE:			Perform Feasibility
*
* CREATION DATE:	05-02-2016
*
* AUTHORS:			Amedeo Bancone
*
* DESIGN ISSUE:		1.1.0
*
* INTERFACES:
*
* SUBORDINATES:
*
* MODIFICATION HISTORY:
*
*
*
* --------------------------+------------+----------------+-------------------------------
* 19-05-2016 | Amedeo Bancone  |1.1| Modified to align to the new objective function and normalization function
* --------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.feasibility;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.telespazio.csg.srpf.utils.NumericUtils;

/**
 * Class holding the objective function.
 *
 * @author Amedeo Bancone
 * @version 1.1.0
 *
 */
public class ObjectiveFunction

{

    static final Logger logger = LogManager.getLogger(ObjectiveFunction.class.getName());

    // Coefficient for the wight
    /**
     * new / number is better
     */
    private double P1;
    /**
     * new is better
     */
    private double P2;
    /**
     * first is better
     */
    private double P3;

    private boolean firstCheck = true;
    /**
     * min e max value to be used in the normalization function for the three
     * element of function cost
     */
    private double firstMin = 0.0;
    private double firstMax = 0.0;

    private double secondMin = 0.0;
    private double secondMax = 0.0;

    private double thirdMin = 0.0;
    private double thirdMax = 0.0;

    /**
     * Strip Value map holds the value of objective function for each strip
     */
    private Map<String, ObjectiveValue> valueMap = new TreeMap<>();

    /**
     * Return the optimal strip
     * @param list 
     *
     * @return optiptimal strip
     */
    public Strip getOptimizedStrip(List<Strip> list)
    {
        /**
         * return value
         */
        Strip returneOptimalStrip = null;

        String optimalId = null;
        /**
         * true if first strip in list
         */
        boolean isFirst = true;
        /**
         * max value of function cost
         */
        double maxValue = 0;

        /*
         * vurrent value of function cost
         */
        double currentValue = 0;
        logger.trace("MODIFICA : deve restituire valore massimo di chiave ??");
        logger.trace("MODIFICA : print map " + this.valueMap);

        // double computeLocalSecondMin = computeLocalSecondMin(valueMap);
        // double computeLocalThirdMin = computeLocalThirdMin(valueMap);
        /**
         * for each entry in the map
         */
        for (Entry<String, ObjectiveValue> e : this.valueMap.entrySet())
        {

            /**
             * Evaluating current value
             */
            currentValue = e.getValue().getValue(this.firstMin, this.firstMax, this.secondMin, this.secondMax, this.thirdMin, this.thirdMax);
            logger.trace("MODIFICA : normalization for Strip : " + e.getKey());
            logger.trace("MODIFICA : normalization : " + currentValue);

            // //System.out.println("========: Current value: " + currentValue);

            if (isFirst)
            {
                /**
                 * if first value
                 */
                isFirst = false;
                /**
                 * max
                 */
                maxValue = currentValue;
                /**
                 * retval strip
                 */
                optimalId = e.getKey();

            } // end if
            else
            {
                /**
                 * if current value > max perform switch
                 */
                if (maxValue < currentValue)
                {
                    maxValue = currentValue;
                    optimalId = e.getKey();
                } // end if
            } // end else
        } // end for

        returneOptimalStrip = findStripByUnivocalId(optimalId,list);
        // //System.out.println("========: Current value Max: " + maxValue);
        logger.trace("MODIFICA : optimized strip returned : " + returneOptimalStrip);

        return returneOptimalStrip;
    }// end getOptimizedStrip

    // private double computeLocalThirdMin(Map<Strip, ObjectiveValue> valueMap)
    // {
    // double thirdMin = 0;
    // if(valueMap.size()>0)
    // {
    // thirdMin =
    // }
    // for (Map.Entry<Strip, ObjectiveValue> map : valueMap.entrySet()) {
    //
    // }
    // return thirdMin;
    // }
    //
    // private double computeLocalSecondMin(Map<Strip, ObjectiveValue> valueMap)
    // {
    //
    // double thirdMin = 0;
    // for (Map.Entry<Strip, ObjectiveValue> map : valueMap.entrySet()) {
    //
    // }
    // return thirdMin;
    // }

    public static Strip findStripByUnivocalId(String optimalId, List<Strip> list)
    {
       String[] elements =  optimalId.trim().split("_");
       Strip stripReturned = null;
        String stripToSearchIdString = elements[0].trim();
        String stripToSearchBeam = elements[1].trim();
        String stripToSearchSatelliteId = elements[2].trim();
        int stripToSearchId = Integer.parseInt(stripToSearchIdString);
//        //System.out.println("."+stripToSearchId +".");
//        //System.out.println("."+stripToSearchBeam+".");
//        //System.out.println("."+stripToSearchSatelliteId+".");

        for(int i=0;i<list.size();i++)
        {
//            //System.out.println(list.get(i).getBeamId()+" VS "+stripToSearchBeam);
//            //System.out.println(list.get(i).getId()+" VS "+stripToSearchId);
//            //System.out.println(list.get(i).getSatelliteId()+" VS "+stripToSearchSatelliteId);

            if(list.get(i).getBeamId().equals(stripToSearchBeam) && stripToSearchId==list.get(i).getId() &&
                    list.get(i).getSatelliteId().equals(stripToSearchSatelliteId))
            {
                stripReturned = list.get(i);
                break;
            }
        }
        return stripReturned;
    }

    /**
     * Return the objective function value
     *
     * @param s
     * @return
     */
    public double getValue(Strip s)
    {
        String stripKey = s.getUnivocalKey();

        return this.valueMap.get(stripKey).getValue(this.firstMin, this.firstMax, this.secondMin, this.secondMax, this.thirdMin, this.thirdMax);
    }// end method

    /**
     * Constructor
     *
     * @param p1
     *            (new /number is better)
     * @param p2
     *            (new is better )
     * @param p3
     *            (first is better)
     */
    public ObjectiveFunction(double p1, double p2, double p3)

    {
        /**
         * Constructor
         */
        this.P1 = p1;
        this.P2 = p2;
        this.P3 = p3;
    }// end method

    /**
     * Add the valkue of function cost for a strip
     *
     * @param newTotalInstripRatio
     * @param numberOfNewPointIsBetter
     * @param firstIsBetter
     * @param s
     *            strip
     */
    public void addValuteAt(final double newTotalInstripRatio, final double numberOfNewPointIsBetter, final double firstIsBetter, final Strip s)
    {

        ObjectiveValue value = new ObjectiveValue(this.P1, this.P2, this.P3, newTotalInstripRatio, numberOfNewPointIsBetter, firstIsBetter);
        logger.trace("MODIFICA addValuteAt  P1 " + this.P1);
        logger.trace("MODIFICA addValuteAt  P2 " + this.P2);
        logger.trace("MODIFICA addValuteAt  P3 " + this.P3);

        // firstMin e firstMax relative to coverage
        if (newTotalInstripRatio < this.firstMin)
        {
            /**
             * switch min
             */
            this.firstMin = newTotalInstripRatio;
        }
        else if (newTotalInstripRatio > this.firstMax)
        {
            /**
             * switch max
             */
            this.firstMax = newTotalInstripRatio;
        }

        // firstMin e firstMax relative to new points

        if ((numberOfNewPointIsBetter < this.secondMin) || (this.secondMin == 0))
        {

            /**
             * switch min
             */
            this.secondMin = numberOfNewPointIsBetter;
        }
        else if (numberOfNewPointIsBetter > this.secondMax)
        {
            /**
             * switch max
             */
            this.secondMax = numberOfNewPointIsBetter;
        }

        // firstMin e firstMax relative to strip time

        if ((firstIsBetter < this.thirdMin) || this.firstCheck)
        {
            /**
             * switch min
             */
            this.thirdMin = firstIsBetter;
            this.firstCheck=false;
        }
        else if (firstIsBetter > this.thirdMax)
        {
            /**
             * switch max
             */
            this.thirdMax = firstIsBetter;
        }
        String stripKey = s.getUnivocalKey();

        this.valueMap.put(stripKey, value);
    }// end method

}// end Class ObjectiveFunction

/**
 *
 * @author Amedeo Bancone
 * @version 1.1.0
 *
 */
class ObjectiveValue
{

    static final Logger logger = LogManager.getLogger(ObjectiveValue.class.getName());

    /**
     * wheight for newTotalInstripRatio
     */
    private double P1;
    /**
     * weight for numberOfNewPointIsBetter
     */
    private double P2;
    /**
     * weight for firstIsBetter
     */
    private double P3;

    /**
     * first addend of cost function
     */
    private double newTotalInstripRatio;
    /**
     * second addend of cost function
     */
    private double numberOfNewPointIsBetter;
    /**
     * third addend of cost function
     */
    private double firstIsBetter;
    /**
     * value of cost function
     */
    private double value;

    /**
     * Return the value of the function given the min e max for each term to be
     * used in the normaliztion function
     *
     * @param firstMin
     * @param firstMax
     * @param secondMin
     * @param secondMax
     * @param thirdMin
     * @param thirdMax
     * @return the value of objective function
     */
    double getValue(double firstMin, double firstMax, double secondMin, double secondMax, double thirdMin, double thirdMax)
    {
       
        logger.trace("MODIFICA ObjectiveValue: firstMin/Max " + firstMin+" // "+firstMax);
        logger.trace("MODIFICA ObjectiveValue: secondMin/Max " + secondMin+" // "+secondMax);
        logger.trace("MODIFICA ObjectiveValue: thirdMin/Max " + thirdMin+" // "+thirdMax);
        
        logger.trace("MODIFICA ObjectiveValue: newTotalInstripRatio " + this.newTotalInstripRatio);
        logger.trace("MODIFICA ObjectiveValue: numberOfNewPointIsBetter " + this.numberOfNewPointIsBetter);
        logger.trace("MODIFICA ObjectiveValue: firstIsBetter " + this.firstIsBetter);

        logger.trace("MODIFICA ObjectiveValue: P1 " + this.P1);
        logger.trace("MODIFICA ObjectiveValue: P2 " + this.P2);
        logger.trace("MODIFICA ObjectiveValue: P3 " + this.P3);

        /**
         * Evaluating valeu value = SUM(Pi*NORMALAZEDVALUEi)
         *
         */
        logger.trace("MODIFICA ObjectiveValue: normalized newTotalInstripRatio " + (this.P1 * NumericUtils.normalize(this.newTotalInstripRatio, firstMax, firstMin)));
        logger.trace("MODIFICA ObjectiveValue: normalized numberOfNewPointIsBetter " + (this.P2 * NumericUtils.normalize(this.numberOfNewPointIsBetter, secondMax, secondMin)));
        logger.trace("MODIFICA ObjectiveValue: normalized firstIsBetter " + (this.P3 * NumericUtils.normalize(this.firstIsBetter, thirdMax, thirdMin)));

        this.value = (this.P1 * NumericUtils.normalize(this.newTotalInstripRatio, firstMax, firstMin)) + (this.P2 * NumericUtils.normalize(this.numberOfNewPointIsBetter, secondMax, secondMin)) + (this.P3 * NumericUtils.normalize(this.firstIsBetter, thirdMax, thirdMin));

        return this.value;
    }// end method

    /**
     * Constructor
     *
     * @param p1
     * @param p2
     * @param p3
     * @param newTotalInstripRatio
     *            (Number of new point number of point in the strip ratio)
     * @param numberOfNewPointIsBetter
     * @param firstIsBetter
     */
    public ObjectiveValue(double p1, double p2, double p3, double newTotalInstripRatio, double numberOfNewPointIsBetter, double firstIsBetter)
    {

        /**
         * setting the attribute
         */

        this.P1 = p1;
        this.P2 = p2;
        this.P3 = p3;
        this.newTotalInstripRatio = newTotalInstripRatio;
        this.numberOfNewPointIsBetter = numberOfNewPointIsBetter;
        this.firstIsBetter = firstIsBetter;

    }// end method

} // end Class