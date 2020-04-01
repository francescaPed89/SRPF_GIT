/**
*
* MODULE FILE NAME:	DateUtils.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Perform Operation on Date Time
*
* PURPOSE:			Perform Operation on Date Time
*
* CREATION DATE:	18-11-2015
*
* AUTHORS:			Amedeo Bancone
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
* 	 18-04-2016  | Amedeo Bancone  |1.1  | fromISOToCSKDate added formatter to accept Z UTC zone
* 										 fromCSKDateToISOFMTDateTime add Z to the output
* --------------------------+------------+----------------+-------------------------------
* 	 18-07-2016  | Amedeo Bancone  |2.0  | fromISOToCSKDate added formatter to accept Z UTC zone
* 										 fromCSKDateToISOFMTDateTime add Z to the output
*-------------------------+------------+----------------+-------------------------------
*
* PROCESSING
*/

package com.telespazio.csg.srpf.utils;

import java.time.format.DateTimeFormatter;
import java.util.StringTokenizer;

import org.apache.logging.log4j.Logger;

/**
 * Class for performing operation on date
 * 
 * @author Amedeo Bancone
 *
 * @version 2.0
 * 
 */
public class DateUtils

{

    // private static double CSKOffset = 2451544.5;
    // private final static DateTimeFormatter isoFmt =
    // ISODateTimeFormat.dateHourMinuteSecondMillis();

    /// Implementation using java time SE 8
    // juslian starting date
    public static java.time.LocalDate CSKStartDate = java.time.LocalDate.of(2000, 1, 1);
    // julian starting date time
    public static java.time.LocalDateTime CSKStartDateTime = java.time.LocalDateTime.of(2000, 1, 1, 0, 0, 0);

    /**
     * evaluate the fractional part of csk date
     * 
     * @param hour
     * @param minute
     * @param sec
     * @param nano
     *            //microseconds
     * @return fractional day
     */
    private static double fractionalJulian(int hour, int minute, int sec, int nano)
    {
        // sec
        double days = sec + (nano / 1.0e9);
        // minute
        days = minute + (days / 60.0);
        // hour
        days = hour + (days / 60.0);
        // divide for 24 hour
        return days / 24.0;

    }// end method

    /**
     * return hh.mm.ss.microsec starting from the fractional days of a generic
     * julian date
     * 
     * @param days
     * @return vector holding hh mm ss
     */
    private static int[] hhmmsecmsecFromFractional(double days)
    {
        // returning vector
        int[] retval = new int[4];
        // evaluating hour
        double hours = days * 24.0;
        // integer
        int hour = (int) hours;
        // decimal
        hours = hours - hour;
        if (hour == 24) // rounding issue it shouldn't happer
        {
            retval[0] = 23;
            retval[1] = 59;
            retval[2] = 59;
            retval[3] = 999999;

            return retval;
        } // end if
        retval[0] = hour;

        // evaluating minutes
        double minutes = hours * 60.0;
        int minute = (int) minutes;
        minutes = minutes - minute;

        if (minute == 60)// rounding issue it shouldn't happer
        {
            retval[1] = 59;
            retval[2] = 59;
            retval[3] = 999999;
            return retval;
        } // end if
        retval[1] = minute;

        // evaluating seconds
        double secs = minutes * 60.0;
        int sec = (int) secs;
        secs = secs - sec;

        if (sec == 60)// rounding issue it shouldn't happer
        {

            retval[2] = 59;
            retval[3] = 999999;
            return retval;
        } // end if

        retval[2] = sec;

        int micro = (int) Math.round(secs * 1.0e6);

        if (micro == 1000000)// rounding issue it shouldn't happer
        {
            retval[3] = 999999;
            return retval;
        } // end if

        retval[3] = micro;
        // returning
        return retval;
    }// end method

    /**
     * Given a Java8 DateTime data in ISO format return the csk date
     * 
     * @param sdate
     * @return julian date
     */
    public static double fromDateTimeTOCSKDate(java.time.LocalDateTime date)
    {

        double retval = 0;
        // evaluating days
        long days = java.time.temporal.ChronoUnit.DAYS.between(CSKStartDate, java.time.LocalDate.of(date.getYear(), date.getMonth(), date.getDayOfMonth()));
        // evaluating fraction
        double fractionalDay = fractionalJulian(date.getHour(), date.getMinute(), date.getSecond(), date.getNano());
        // evaluating julian date
        retval = days + fractionalDay;
        // returning
        return retval;
    }// end method

    /**
     * Given a string holding data in ISO format return the csk (julian) date
     * 
     * @param sdate
     * @return csk date
     */
    public static double fromISOToCSKDate(final String sdate) throws java.time.format.DateTimeParseException
    {
        // parsing
        java.time.LocalDateTime date = java.time.LocalDateTime.parse(sdate, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        // tranforming
        return fromDateTimeTOCSKDate(date);
    }// end method

    /**
     * return the current date time in csk
     * 
     * @return current time as julian
     */
    public static double cskDateTimeNow()
    {
        // get time
        java.time.LocalDateTime date = java.time.LocalDateTime.now();
        // to julian time
        return fromDateTimeTOCSKDate(date);
    }// end method

    
	public static void getLogInfo(Exception e, Logger logger) {
		
		StackTraceElement[] stackTraceElement = e.getStackTrace();
		int lineNumber = stackTraceElement[0].getLineNumber();
		
		logger.error("getLogInfo "+e.getMessage());
		logger.error("getLogInfo "+stackTraceElement[0].toString());
		logger.error("BECAUSE : "+e.getCause());
		logger.error("ON CLASS : "+stackTraceElement[0].getClassName());
		logger.error("ON METHOD "+stackTraceElement[0].getMethodName());
		logger.error("AT LINE "+lineNumber);

		e.printStackTrace();
	}
	
    /**
     * Given a date in CSK format retruns a LocalDAteTime java 8 object
     * 
     * @param cskdate
     * @return LocalDAteTime java 8 object
     */
    public static java.time.LocalDateTime fromCSKDateToDateTime(double cskdate)
    {
        // days
        long cskday = (int) cskdate;
        // fractional
        double fractionalDay = cskdate - cskday;

        // current date in java date
        java.time.LocalDate date = CSKStartDate.plusDays(cskday);

        // get time
        int[] hhmmssmsec = hhmmsecmsecFromFractional(fractionalDay);

        java.time.LocalTime time = java.time.LocalTime.of(hhmmssmsec[0], hhmmssmsec[1], hhmmssmsec[2], hhmmssmsec[3] * 1000);

        java.time.LocalDateTime dateTime = java.time.LocalDateTime.of(date, time);

        // return java date time
        return dateTime;

    }// end method

    /**
     * Given a date in CSK format retrun a String holding the data and timr in
     * iso format with UTC Zone
     * 
     * @param cskdate
     * @return DateTime in iso format
     */
    public static String fromCSKDateToISOFMTDateTime(double cskdate)
    {
        String retval = "";

        /*
         * long cskday = (int) cskdate; double fractionalDay = cskdate -cskday;
         * 
         * java.time.LocalDate date = CSKStartDate.plusDays(cskday);
         * 
         * int[] hhmmssmsec = hhmmsecmsecFromFractional(fractionalDay);
         * 
         * java.time.LocalTime time =
         * java.time.LocalTime.of(hhmmssmsec[0],hhmmssmsec[1],hhmmssmsec[2],
         * hhmmssmsec[3]*1000);
         * 
         * java.time.LocalDateTime dateTime =
         * java.time.LocalDateTime.of(date,time);
         */

        java.time.LocalDateTime dateTime = fromCSKDateToDateTime(cskdate);
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        // retval = dateTime.toString();

        retval = dateTime.format(formatter);

        return retval + "Z";

    }// end method

    /**
     * get a string holding the current datetime in ISO format
     * 
     * @return a string holding the current datetime in ISO format
     */
    public static String getCurrentDateInISOFMT()
    {
        // getting current date time
        java.time.LocalDateTime dateTime = java.time.LocalDateTime.now();
        // formatter
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        String retval = dateTime.format(formatter);
        // adding time zone
        return retval + "Z";
    }// end method

    /*
     * public static String testFormat(double cskdate) {
     * 
     * String retval="";
     * 
     * long cskday = (int) cskdate; double fractionalDay = cskdate -cskday;
     * 
     * java.time.LocalDate date = CSKStartDate.plusDays(cskday);
     * 
     * int[] hhmmssmsec = hhmmsecmsecFromFractional(fractionalDay);
     * 
     * java.time.LocalTime time =
     * java.time.LocalTime.of(hhmmssmsec[0],hhmmssmsec[1],hhmmssmsec[2],
     * hhmmssmsec[3]*1000);
     * 
     * java.time.LocalDateTime dateTime = java.time.LocalDateTime.of(date,time);
     * 
     * retval = date.toString();
     * 
     * retval =
     * retval+"T"+time.getHour()+":"+time.getMinute()+":"+time.getSecond()+"."+
     * time.getNano();
     * 
     * return retval; }
     */
    /**
     * Given a date in CSK format retrun a String holding the date in iso format
     * 
     * @param cskdate
     * @return Date in iso format
     */
    public static String fromCSKDateToISOFMTDate(double cskdate)
    {
        String retval = "";

        long cskday = (int) cskdate;
        double fractionalDay = cskdate - cskday;

        java.time.LocalDate date = CSKStartDate.plusDays(cskday);

        /*
         * int[] hhmmssmsec = hhmmsecmsecFromFractional(fractionalDay);
         * java.time.LocalTime time =
         * java.time.LocalTime.of(hhmmssmsec[0],hhmmssmsec[1],hhmmssmsec[2],
         * hhmmssmsec[3]*1000); java.time.LocalDateTime dateTime =
         * java.time.LocalDateTime.of(date,time); retval = dateTime.toString();
         */

        // java.time.format.DateTimeFormatter formatter =
        // java.time.format.DateTimeFormatter.ISO_DATE;

        retval = date.toString();

        return retval;
    }// end method

    /**
     * Retrun a double representing the number of seconds passed to the methods.
     * The number of seconds shall be less than 86400 (one day)
     * 
     * @param seconds
     * @return number of seconds in julian
     */
    public static double secondsToJulian(long seconds)
    {
        double retval = 0;
        // java date at julian starting time + nanoseconds
        java.time.LocalDateTime t = CSKStartDateTime.plusSeconds(seconds);
        // retval =
        // fractionalJulian(t.getHour(),t.getMinute(),t.getSecond(),t.getNano());
        // getting julian date
        retval = DateUtils.fromDateTimeTOCSKDate(t);
        return retval;
    }// end method

    /**
     * Retuns the numbers of days corresponding to the passed seconds
     * 
     * @param seconds
     * @return
     */
    /*
     * public static double secondsToJulianDays(long seconds) { double retval=0;
     * java.time.LocalDateTime t = CSKStartDateTime.plusSeconds(seconds); retval
     * = DateUtils.fromDateTimeTOCSKDate(t); return retval; }
     */

    /**
     * Convert the specified number of milliseconds to julian fraction
     * 
     * @param millisec
     * @return the julian fraction
     */
    public static double millisecondsToJulian(long millisec)
    {
        double retval = 0;
        // java date at julian starting time + milliseconds
        java.time.LocalDateTime t = CSKStartDateTime.plusNanos(millisec * 1000000);
        // retval =
        // fractionalJulian(t.getHour(),t.getMinute(),t.getSecond(),t.getNano());
        // getting julian date
        retval = DateUtils.fromDateTimeTOCSKDate(t);
        return retval;
    }// end method

    /**
     * Return an elapsed amount of nanoseconds expressed in csk julian time
     * 
     * @param secondNanoseconds
     * @return elapsed amount of nanoseconds expressed in csk julian time
     */
    public static double nanosecondsToJulian(long nanoseconds)
    {
        double retval = 0;
        // java date at julian starting time + nanoseconds
        java.time.LocalDateTime t = CSKStartDateTime.plusNanos(nanoseconds);
        // retval =
        // fractionalJulian(t.getHour(),t.getMinute(),t.getSecond(),t.getNano());
        // getting julian date
        retval = DateUtils.fromDateTimeTOCSKDate(t);
        return retval;
    }// end method

    /**
     * retutn a double representing a dateTime in csk date starting from a
     * string of Epoch: yyyy-MM-DD hh:mm:ss.nnnnnnnnn
     * 
     * @param epoch
     * @return julian
     *
     */
    public static double fromEpochToCSKDate(String epoch) throws java.time.format.DateTimeParseException
    {
        // split
        StringTokenizer tokens = new StringTokenizer(epoch, ".");

        if (tokens.countTokens() < 2)
        {
            epoch = epoch + ".00";
        } // end if

        String pattern = "uuuu-MM-dd HH:mm:ss.n";
        // setting pattern
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern(pattern);
        // getting java date time
        java.time.LocalDateTime date = java.time.LocalDateTime.parse(epoch, formatter);
        // evaluating julian
        return fromDateTimeTOCSKDate(date);
    }// end method

    public static double fromEpochToCSKDateForSOe(String epoch) throws java.time.format.DateTimeParseException
    {
        // split
        StringTokenizer tokens = new StringTokenizer(epoch, ".");

        if (tokens.countTokens() < 2)
        {
            epoch = epoch + ".00";
        } // end if

        String pattern = "uuuu-MM-dd'T'HH:mm:ss.n";
        // setting pattern
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern(pattern);
        // getting java date time
        java.time.LocalDateTime date = java.time.LocalDateTime.parse(epoch, formatter);
        // evaluating julian
        return fromDateTimeTOCSKDate(date);
    }// end method
    /**
     * Given a duration in csk date, return the correspondending duration in
     * seconds the value is rounded
     * 
     * @param cskDuration
     * @return duration in seconds
     */
    public static long fromCSKDurationToSeconds(double cskDuration)
    {
        long retval = 0;
        // evaluate date time from julian value
        java.time.LocalDateTime dateTime = fromCSKDateToDateTime(cskDuration);
        // get nanosec
        long nasoSec = dateTime.getNano();
        retval = java.time.temporal.ChronoUnit.SECONDS.between(CSKStartDateTime, dateTime);

        // rounding
        if (nasoSec > 500000000)
        {
            retval = retval + 1;
        } // end if

        return retval;
    }// end method

    /**
     * Transform a duration expressed in julian format in milliseconds
     * 
     * @param cskDuration
     * @return milliseconds
     */
    public static long fromCSKDurationToMilliSeconds(double cskDuration)
    {
        long retval = 0;
        // evaluate date time from julian value
        java.time.LocalDateTime dateTime = fromCSKDateToDateTime(cskDuration);
        // evaluate milliseconds
        retval = java.time.temporal.ChronoUnit.MILLIS.between(CSKStartDateTime, dateTime);

        return retval;
    }// end method
}// end class
