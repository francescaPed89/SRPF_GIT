/**
*
* MODULE FILE NAME:	CMConstants.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			CM Interaction
*
* PURPOSE:			CM Interaction
*
* CREATION DATE:	21-11-2015
*
* AUTHORS:			Girolamo Castaldo
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

package com.telespazio.csg.srpf.cm;

/**
 * Miscellaneous constants used to interact with CM
 * 
 * @author Girolamo Castaldo
 * @version 1.0
 */
public class CMConstants
{

    // CM Ingestor
    public static final String CM_INGESTOR_NAME = "ComponentName"; // Component
                                                                   // name
                                                                   // property
                                                                   // name
    public static final String CM_INGESTOR_ELEMENT_ID = "ElementID"; // The
                                                                     // ElementID
                                                                     // part of
                                                                     // the
                                                                     // log/event
                                                                     // originator
    public static final String CM_INGESTOR_ORIGINATOR = "IDUGS:S-RPF:FE"; // The
                                                                          // log/event
                                                                          // originator
    // public static final String CM_INGESTOR_ORIGINATOR =
    // "ElementID::S-RPF::CMIngestor::CMIngestor";
    // public static final int CM_INGESTOR_CLEANUP_SLEEP = 2; // Time (in
    // seconds) to wait before cleaning up file

    public static final String HAVE_PURGE_CM_MESSAGE_BOX_CONF_KEY = "HAVE_PURGE_CM_MESSAGE_BOX";

    // CM
    public static final String CM_COMPONENT_NAME_FORMAT = "ElementID:Subsystem:Component"; // Component
                                                                                           // format
                                                                                           // accepted
                                                                                           // by
                                                                                           // CM
    public static final String CM_COMPONENT_NAME_SEP = ":"; // Component format
                                                            // separator
    public static final String CM_COMPONENT_NAME_REGEX = "[^:]+:{1}[^:]+:{1}[^:]+"; // Regular
                                                                                    // expression
                                                                                    // to
                                                                                    // match
                                                                                    // the
                                                                                    // component
                                                                                    // name
                                                                                    // format
    public static final String CM_COMPONENT_INVALID_NAME = "Invalid component name"; // Invalid
                                                                                     // component
                                                                                     // name
                                                                                     // error
                                                                                     // message

    // polling options
    public static final String CMI_POLLING_FLAG_PROP = "CMI_POLLING_FLAG";
    public static final String CMI_POLLING_FLAG_DEFAULT = "0";

    public static final String CMI_POLLING_INTERVAL_PROP = "CMI_POLLING_INTERVAL";
    public static final String CMI_POLLING_INTERVAL_DEFAULT = "250";

    // WSController
    public static final String WSCONTROLLER_URL = "WSControllerURL"; // WSController
                                                                     // URL
                                                                     // property
                                                                     // name

    // Feasibility
    public static final String CM_FEASIBILITY_TIMEOUT = "FeasibilityTimeout"; // Feasibility
                                                                              // timeout
                                                                              // property
                                                                              // name
    public static final String CM_FEASIBILITY_MSG_CLASS_PROP = "FeasibilityMsgClass"; // Feasibility
                                                                                      // message
                                                                                      // class
                                                                                      // property
                                                                                      // name
    public static final String CM_FEASIBILITY_MSG_CLASS = "FeasibilityAnalysis"; // Feasibility
                                                                                 // message
                                                                                 // class
    public static final String CM_FEASIBILITY_BASKET_PROP = "FeasibilityBasket"; // Destination
                                                                                 // directory
                                                                                 // for
                                                                                 // feasibility
                                                                                 // attachments
                                                                                 // property
                                                                                 // name
    public static final String CM_FEASIBILITY_BASKET = "/opt/SRPF/FrontEnd/basket"; // Destination
                                                                                    // directory
                                                                                    // for
                                                                                    // feasibility
                                                                                    // attachments
                                                                                    // property
                                                                                    // name
    public static final String CM_FEASIBILITY_WORKING_DIR = "FeasibilityWorkingDir"; // Feasibility
                                                                                     // working
                                                                                     // directory
                                                                                     // property
                                                                                     // name

    // ODREF
    public static final String CM_ODREF_TIMEOUT = "ODREFTimeout"; // ODREF
                                                                  // timeout
                                                                  // property
                                                                  // name
    public static final String CM_ODREF_MSG_CLASS_PROP = "ODREFMsgClass"; // ODREF
                                                                          // message
                                                                          // class
                                                                          // property
                                                                          // name
    public static final String CM_ODREF_MSG_CLASS = "ODREF"; // ODREF message
                                                             // class
    public static final String CM_ODREF_BASKET_PROP = "ODREFBasket"; // Destination
                                                                     // directory
                                                                     // for
                                                                     // ODREF
                                                                     // attachments
                                                                     // property
                                                                     // name
    public static final String CM_ODREF_BASKET = "/opt/SRPF/FrontEnd/basket"; // Destination
                                                                              // directory
                                                                              // for
                                                                              // ODREF
                                                                              // attachments
                                                                              // property
                                                                              // name
    public static final String CM_ODREF_WORKING_DIR = "ODREFWorkingDir"; // ODREF
                                                                         // working
                                                                         // directory
                                                                         // property
                                                                         // name

    // ODNOM
    public static final String CM_ODNOM_TIMEOUT = "ODNOMTimeout"; // ODNOM
                                                                  // timeout
                                                                  // property
                                                                  // name
    public static final String CM_ODNOM_MSG_CLASS_PROP = "ODNOMMsgClass"; // ODNOM
                                                                          // message
                                                                          // class
                                                                          // property
                                                                          // name
    public static final String CM_ODNOM_MSG_CLASS = "ODNOM"; // ODNOM message
                                                             // class
    public static final String CM_ODNOM_BASKET_PROP = "ODNOMBasket"; // Destination
                                                                     // directory
                                                                     // for
                                                                     // ODNOM
                                                                     // attachments
                                                                     // property
                                                                     // name
    public static final String CM_ODNOM_BASKET = "/opt/SRPF/FrontEnd/basket"; // Destination
                                                                              // directory
                                                                              // for
                                                                              // ODNOM
                                                                              // attachments
                                                                              // property
                                                                              // name
    public static final String CM_ODNOM_WORKING_DIR = "ODNOMWorkingDir"; // ODNOM
                                                                         // working
                                                                         // directory
                                                                         // property
                                                                         // name

    // ODMTP
    public static final String CM_ODMTP_TIMEOUT = "ODMTPTimeout"; // ODMTP
                                                                  // timeout
                                                                  // property
                                                                  // name
    public static final String CM_ODMTP_MSG_CLASS_PROP = "ODMTPMsgClass"; // ODMTP
                                                                          // message
                                                                          // class
                                                                          // property
                                                                          // name
    public static final String CM_ODMTP_MSG_CLASS = "ODMTP"; // ODMTP message
                                                             // class
    public static final String CM_ODMTP_BASKET_PROP = "ODMTPBasket"; // Destination
                                                                     // directory
                                                                     // for
                                                                     // ODMTP
                                                                     // attachments
                                                                     // property
                                                                     // name
    public static final String CM_ODMTP_BASKET = "/opt/SRPF/FrontEnd/basket"; // Destination
                                                                              // directory
                                                                              // for
                                                                              // ODMTP
                                                                              // attachments
                                                                              // property
                                                                              // name
    public static final String CM_ODMTP_WORKING_DIR = "ODMTPWorkingDir"; // ODMTP
                                                                         // working
                                                                         // directory
                                                                         // property
                                                                         // name

    // ODSTP
    public static final String CM_ODSTP_TIMEOUT = "ODSTPTimeout"; // ODSTP
                                                                  // timeout
                                                                  // property
                                                                  // name
    public static final String CM_ODSTP_MSG_CLASS_PROP = "ODSTPMsgClass"; // ODSTP
                                                                          // message
                                                                          // class
                                                                          // property
                                                                          // name
    public static final String CM_ODSTP_MSG_CLASS = "ODSTP"; // ODSTP message
                                                             // class
    public static final String CM_ODSTP_BASKET_PROP = "ODSTPBasket"; // Destination
                                                                     // directory
                                                                     // for
                                                                     // ODSTP
                                                                     // attachments
                                                                     // property
                                                                     // name
    public static final String CM_ODSTP_BASKET = "/opt/SRPF/FrontEnd/basket"; // Destination
                                                                              // directory
                                                                              // for
                                                                              // ODSTP
                                                                              // attachments
                                                                              // property
                                                                              // name
    public static final String CM_ODSTP_WORKING_DIR = "ODSTPWorkingDir"; // ODSTP
                                                                         // working
                                                                         // directory
                                                                         // property
                                                                         // name

    // PAW
    public static final String CM_PAW_TIMEOUT = "PAWTimeout"; // PAW timeout
                                                              // property name
    public static final String CM_PAW_MSG_CLASS_PROP = "PAWMsgClass"; // PAW
                                                                      // message
                                                                      // class
                                                                      // property
                                                                      // name
    public static final String CM_PAW_MSG_CLASS = "PAW"; // PAW message class
    public static final String CM_PAW_BASKET_PROP = "PAWBasket"; // Destination
                                                                 // directory
                                                                 // for PAW
                                                                 // attachments
                                                                 // property
                                                                 // name
    public static final String CM_PAW_BASKET = "/opt/SRPF/FrontEnd/basket"; // Destination
                                                                            // directory
                                                                            // for
                                                                            // PAW
                                                                            // attachments
                                                                            // property
                                                                            // name
    public static final String CM_PAW_WORKING_DIR = "PAWWorkingDir"; // PAW
                                                                     // working
                                                                     // directory
                                                                     // property
                                                                     // name

    // ALLOCATION PLAN
    public static final String CM_ALL_PLAN_TIMEOUT = "ALLPLANTimeout"; // PAW
                                                                       // timeout
                                                                       // property
                                                                       // name
    public static final String CM_ALL_PLAN_MSG_CLASS_PROP = "ALLPLANMsgClass"; // PAW
                                                                               // message
                                                                               // class
                                                                               // property
                                                                               // name
    public static final String CM_ALL_PLAN_MSG_CLASS = "AllocationPlan"; // PAW
                                                                         // message
                                                                         // class
    public static final String CM_ALL_PLAN_BASKET_PROP = "ALLPLANBasket"; // Destination
                                                                          // directory
                                                                          // for
                                                                          // PAW
                                                                          // attachments
                                                                          // property
                                                                          // name
    public static final String CM_ALL_PLAN_BASKET = "/opt/SRPF/FrontEnd/basket"; // Destination
                                                                                 // directory
                                                                                 // for
                                                                                 // PAW
                                                                                 // attachments
                                                                                 // property
                                                                                 // name
    public static final String CM_ALL_PLAN_WORKING_DIR = "ALLPLANWorkingDir"; // PAW
                                                                              // working
                                                                              // directory
                                                                              // property
                                                                              // name

    // Refinement
    public static final String CM_FEASIBILITY_REF_TIMEOUT = "FeasibilityRefTimeout"; // Feasibility
                                                                                     // timeout
                                                                                     // property
                                                                                     // name
    public static final String CM_FEASIBILITY_REF_MSG_CLASS_PROP = "FeasibilityRefMsgClass"; // Feasibility
                                                                                             // message
                                                                                             // class
                                                                                             // property
                                                                                             // name
    public static final String CM_FEASIBILITY_REF_MSG_CLASS = "AnalysePRList"; // Feasibility
                                                                               // message
                                                                               // class
    public static final String CM_FEASIBILITY_REF_BASKET_PROP = "FeasibilityRefBasket"; // Destination
                                                                                        // directory
                                                                                        // for
                                                                                        // feasibility
                                                                                        // attachments
                                                                                        // property
                                                                                        // name
    public static final String CM_FEASIBILITY_REF_BASKET = "/opt/SRPF/FrontEnd/basket"; // Destination
                                                                                        // directory
                                                                                        // for
                                                                                        // feasibility
                                                                                        // attachments
                                                                                        // property
                                                                                        // name
    public static final String CM_FEASIBILITY_REF_WORKING_DIR = "FeasibilityRefWorkingDir"; // Feasibility
                                                                                            // working
                                                                                            // directory
                                                                                            // property
                                                                                            // name

    // SOE
    public static final String CM_SOE_TIMEOUT = "SOETimeout"; // Feasibility
                                                              // timeout
                                                              // property name
    public static final String CM_SOE_MSG_CLASS_PROP = "SOEMsgClass"; // Feasibility
                                                                      // message
                                                                      // class
                                                                      // property
                                                                      // name
    public static final String CM_SOE_MSG_CLASS = "SOE"; // Feasibility message
                                                         // class
    public static final String CM_SOE_BASKET_PROP = "SOEBasket"; // Destination
                                                                 // directory
                                                                 // for
                                                                 // feasibility
                                                                 // attachments
                                                                 // property
                                                                 // name
    public static final String CM_SOE_BASKET = "/opt/SRPF/FrontEnd/basket"; // Destination
                                                                            // directory
                                                                            // for
                                                                            // feasibility
                                                                            // attachments
                                                                            // property
                                                                            // name
    public static final String CM_SOE_WORKING_DIR = "SOEWorkingDir"; // Feasibility
                                                                     // working
                                                                     // directory
                                                                     // property
                                                                     // name

    public static final String CM_DEFAULT_TIMEOUT = "2";

    /**
     * Deafault constracort Do nothing
     */
    public CMConstants()
    {
    }
} // end class
