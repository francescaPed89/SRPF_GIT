/**
*
* MODULE FILE NAME:	HolesPatcher.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Find dto to patch holes in optimal solution due to quantization error
*
* PURPOSE:			Increase coverage
*
* CREATION DATE:	20-01-2017
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

import java.util.ArrayList;
import java.util.List;

import com.telespazio.csg.srpf.logging.TraceManager;
import com.telespazio.csg.srpf.logging.constants.EventType;
import com.telespazio.csg.srpf.logging.constants.ProbableCause;

/**
 *
 * This class is responsible to patch holes due to quantization error
 *
 * @author Amedeo Bancone
 * @version 1.0
 *
 */
class HolesPatcher

{

    /**
     * Logger
     */
    private TraceManager tracer = new TraceManager();

    /**
     * Optimal acq list
     */
    private List<AcqReq> optimalAcqList;

    /**
     * PR request
     */
    private PRRequestParameter request;

    /**
     * flag for spotlight
     */
    private boolean isSpotLight = false;

    /**
     * gridder
     */
    private Gridder gridder;

    /**
     * satellite list
     */
    private List<Satellite> satList;

    /**
     * Constrictor
     *
     * @param optimalAcqList
     * @param request
     * @param gridder
     * @param satList
     */
    HolesPatcher(List<AcqReq> optimalAcqList, PRRequestParameter request, Gridder gridder, List<Satellite> satList)
    {
        /**
         * copying parameters
         */
        this.optimalAcqList = optimalAcqList;
        this.request = request;
        this.gridder = gridder;
        this.satList = satList;

        this.isSpotLight = false;

        /**
         * Check if spotlight case
         */
        if (optimalAcqList.size() != 0)
        {
            /**
             * Check on first DTO
             */
            DTO d = optimalAcqList.get(0).getDTOList().get(0);
            if (d instanceof SpotLightDTO)
            {
                this.isSpotLight = true;
            } // end if

        } // end if
    }// end method

    /**
     * Try to recoer holes in the optimal solution
     *
     * @param optimalAcqList
     * @return the optimal soutio acq list with new ACQ filling holes
     */
    List<AcqReq> checkForHoles()
    {
        this.tracer.debug("seraching for holes");

        int requestedOrbitDrection = this.request.getRequestedOrbitDirection();
        // In case of any we performed two feasibility one for acending and one
        // for descending so we have to use only access having the
        // orbit direction used in the optimal solution
        if ((requestedOrbitDrection == FeasibilityConstants.AnyOrbitDirection) && (this.optimalAcqList.size() != 0))
        {
            int orbitDirection = this.optimalAcqList.get(0).getDTOList().get(0).getOrditDirection();

            this.request.setRequestedOrbitDirection(orbitDirection);
        } // end if

        try
        {
            // //System.out.println("============================Searching holes
            // ");
            /**
             * seraching holes in grid
             */
            List<GridPoint> listOfHles = this.gridder.getHolesCenter(this.optimalAcqList);
            // //System.out.println("========Found " + listOfHles.size() + "
            // Holes");
            this.tracer.debug("Found " + listOfHles.size() + " Holes");
            AcqReq acq;

            /**
             * for each holes in the list
             */
            for (GridPoint p : listOfHles)
            {
                /**
                 * evaluate AR patching
                 */
                acq = getAcqOnHole(p);

                // No Ar so nex iteration
                if (acq == null)
                {
                    continue;
                }
                // setting id
                acq.setId(Integer.toString(this.optimalAcqList.size() + 1));

                // Adding the new AR to list
                this.optimalAcqList.add(acq);
            }
        } // end try
        catch (Exception e)
        {
            // System.err.println("Error in searching holes: " +
            // e.getMessage());

            this.tracer.minor(EventType.SOFTWARE_EVENT, ProbableCause.CONFIGURATION_OR_CUSTOMIZATION_ERROR, e.getMessage());
        } // end catch
        finally
        {
            /**
             * resetting the request orbit direction. It could be changed above
             */
            this.request.setRequestedOrbitDirection(requestedOrbitDrection);
        } // end finally

        return this.optimalAcqList;
    }// end method

    /**
     * Return the acq request filling the hole. If no acq found retun null
     *
     * @param hole
     * @return AR on the given point or null if no AR
     * @throws Exception 
     */
    private AcqReq getAcqOnHole(GridPoint hole) throws Exception
    {
        /**
         * Ar to be returned
         */
        AcqReq acq = null;
        /**
         * Creating a grid with only one point : the hole
         */
        List<GridPoint> pointList = new ArrayList<>();
        pointList.add(hole);

        AccessesEvaluator accessEval = new AccessesEvaluator();

        /**
         * For each satellite Search for ACQ
         */
        for (Satellite s : this.satList)
        {
            /**
             * Backipping ols Accesses
             */
            List<Access> oldAccess = s.getAccessList();

            /**
             * Evaluating Accesses on hole
             */
            s.setAccessList(new ArrayList<Access>());
            accessEval.evaluateSatelliteAccesses(s, pointList, this.request);

            List<Access> newAccessList = s.getAccessList();

            /**
             * Search if The evaluated accesses could be used to create a new AR
             */
            acq = findAcq(newAccessList);

            /**
             * Restoring access list for the satellite
             */
            s.setAccessList(oldAccess);
            /*
             * for(Access a :newAccessList) { s.addAccess(a); }
             */

            // found valid acq we can exit exit
            if (acq != null)
            {
                break;
            }

        } // end for

        return acq;
    }// end method

    /**
     * Find the acq request filling the hole by searchin oh the access list on
     * hole. If no acq found retun null
     *
     * @param accesslistOnHole
     * @return AcqReq if exists null otherwise
     * @throws Exception 
     */
    private AcqReq findAcq(List<Access> accesslistOnHole) throws Exception
    {
        /**
         * AR to be returned
         */
        AcqReq acq = null;

        /**
         * For each access
         */
        for (Access a : accesslistOnHole)
        {
            /**
             * If the access doesn't conflict with already evaluated DTOs
             */
            if (isAccessValidToFillHoles(a))
            {
                /**
                 * Building a strip on that access
                 */
                List<Access> stripAccess = new ArrayList<>();
                stripAccess.add(a);
                Strip s = new Strip(0, stripAccess);
                DTO dto;

                // used for spotlight DTO
                boolean isGoodDTO = true;

                if (this.isSpotLight)
                {
                    /**
                     * Case spotlight
                     */
                    dto = new SpotLightDTO(a, s);
                    isGoodDTO = ((SpotLightDTO) dto).isGood();
                } // end if
                else
                {
                    /**
                     * Case STRIPMODE
                     */
                    dto = new DTO(s);
                } // end else
                if (isGoodDTO)
                {
                    /**
                     * Building AR on evaluated DTO
                     */
                    acq = new AcqReq();
                    acq.addDTO(dto);
                    acq.setMission(a.getSatellite().getMissionName());
                    this.tracer.debug("Found patch for hole");
                    /**
                     * We found an AR So we can exit the for
                     */
                    break;
                } // end if
            } // end if
        } // end for

        return acq;
    }// end method

    /**
     * Return true if that access can be used to fill the hole
     *
     * @param accessOnHole
     * @return true if the access on hole id usable to build a DTO
     */
    private boolean isAccessValidToFillHoles(Access accessOnHole)
    {
        /**
         * Retval
         */
        boolean isValid = true;

        /**
         * Threshold used in check
         */
        double threshold = accessOnHole.getSatellite().getTreshold();

        // double threshold = a.getSatellite().getSensorRestoreTime();
        /*
         * if(optimalAcqList.size()==0) { isValid=false; return isValid; }
         */
        String satellite = accessOnHole.getSatelliteId();
        double epoch = accessOnHole.getAccessTime();

        // iterating on acq
        for (AcqReq acq : this.optimalAcqList)
        {
            // iterating on dto
            for (DTO dto : acq.getDTOList())
            {
                double lowerLimit = dto.getStartTime() - threshold;
                double upperLimit = dto.getStopTime() + threshold;

                // Check if DTO and Access are on the same orbit of the same
                // satellite
                if (satellite.equals(dto.getSatName()))
                {
                    // if they are on the same orbit
                    if (((epoch > (dto.getStartTime() - FeasibilityConstants.ManouvreTolerance)) && (epoch < (dto.getStopTime() + FeasibilityConstants.ManouvreTolerance))))
                    {
                        // they must have the same look side
                        if (accessOnHole.getLookSide() == dto.getLookSide())
                        {
                            // the access must be outide the treshold interval
                            if ((epoch > lowerLimit) && (epoch < upperLimit))
                            {
                                /**
                                 * Access on the same orbit of dto, has the same
                                 * look side but overlap (considering threshold
                                 * the DTO)
                                 */
                                isValid = false;

                            } // end if
                        } // end if
                        else
                        {
                            /**
                             * access on the same orbit of a dto but have
                             * opposite look side
                             */
                            isValid = false;
                        } // end if

                    } // end if
                } // end if
                /**
                 * not valid have exit for
                 */
                if (!isValid)
                {
                    break;
                }
            } // end for dto
            /**
             * not valid have exit for
             */
            if (!isValid)
            {
                break;
            }
        } // end for acq

        return isValid;
    }// end method
}// end class
