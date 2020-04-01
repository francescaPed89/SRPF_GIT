/**
*
* MODULE FILE NAME:	CSGBicParameters.java
*
* MODULE TYPE:		Class definition
*
* FUNCTION:			Helper Class holding the parameters used in evaluating the BIC for CSG DTO
*
* PURPOSE:			SUF Evaluation
*
* CREATION DATE:	01-02-2018
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

package com.telespazio.csg.srpf.suf;

// TODO: Auto-generated Javadoc
/**
 * Helper Class holding the parameters used to evaluate BIC for CSG DTO.
 *
 * @author Amedeo Bancone
 * @version 1.0
 */
public class CSGBicParameters {

	/** The power lower. */
	// power
	private double power = 0;
	
	/** The bic thre E. */
	// E threshold
	private double bicThreE = 0;

	
	/** The bic thre D. */
	// D Trheshold
	private double bicThreD = 0;

	/** The bic ref D. */
	private double bicRefD = 0;

	/** The alpha. */
	// alpha
	private double alpha = 1;
	
	/** The beta. */
	private double beta = 0;

	/** The left side extra cost. */
	// extra cost to be added in case of left look side
	private double leftSideExtraCost = 0;

	/**
	 * Instantiates a new CSG bic parameters.
	 *
	 * @param powerLower the power lower
	 * @param powerUpper the power upper
	 * @param bicThreE the bic thre E
	 * @param bicRefE the bic ref E
	 * @param bicThreD the bic thre D
	 * @param bicRefD the bic ref D
	 * @param alpha the alpha
	 * @param beta the beta
	 * @param leftSideExtraCost the left side extra cost
	 */
	public CSGBicParameters(double power, double bicThreE, double bicThreD,
			double bicRefD, double alpha, double beta, double leftSideExtraCost) {
		
		//power,threE, threDV,bicRefDV, alpha, beta,leftExtraCost));
		super();
		this.power = power;
		this.bicThreE = bicThreE;
		this.bicThreD = bicThreD;
		this.bicRefD = bicRefD;
		this.alpha = alpha;
		this.beta = beta;
		this.leftSideExtraCost = leftSideExtraCost;
	}

//    /**
//     * 
//     * @param power
//     * @param bicThreE
//     * @param bicThreD
//     * @param alpha
//     */
//    public CSGBicParameters(double powerLower, double bicThreE, double bicThreD, double alpha, double leftExtraCost)
//    {
//        // calling super
//        super();
//        // setting parameters
//        this.powerLower = powerLower;
//        this.bicThreE = bicThreE;
//        this.bicThreD = bicThreD;
//        // setting alpha
//        this.alpha = alpha;
//        // setting left extrea cost
//        this.leftSideExtraCost = leftExtraCost;
//
//    }// end method

	/**
 * Deafault constructor.
 */
	public CSGBicParameters() {
		// calling super
		super();
		// setting defaults
		this.power = 0;

		this.bicThreE = 0;
		this.bicThreD = 0;
		this.leftSideExtraCost = 0;
		// default 0.5
		this.alpha = 0.5;
	}// end method

	/**
	 * Gets the bic thre E.
	 *
	 * @return bicThreE
	 */
	public double getBicThreE() {
		return this.bicThreE;
	}// end method

	/**
	 * Set bicThreE.
	 *
	 * @param bicThreE the new bic thre E
	 */
	public void setBicThreE(double bicThreE) {
		this.bicThreE = bicThreE;
	}// end method

	/**
	 * Gets the bic thre D.
	 *
	 * @return bicThreD
	 */
	public double getBicThreD() {
		return this.bicThreD;
	}// end method

	/**
	 * set bicThreD.
	 *
	 * @param bicThreD the new bic thre D
	 */
	public void setBicThreD(double bicThreD) {
		this.bicThreD = bicThreD;
	}// end method

	/**
	 * Gets the alpha.
	 *
	 * @return alpha
	 */
	public double getAlpha() {
		return this.alpha;
	}// end method

	/**
	 * Set alpha.
	 *
	 * @param alpha the new alpha
	 */
	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}// end method

	/**
	 * Gets the left side extra cost.
	 *
	 * @return left extra cost
	 */
	public double getLeftSideExtraCost() {
		return this.leftSideExtraCost;
	}// end method

	/**
	 * Set left extra cost.
	 *
	 * @param leftSideExtraCost the new left side extra cost
	 */
	public void setLeftSideExtraCost(double leftSideExtraCost) {
		this.leftSideExtraCost = leftSideExtraCost;
	}// end method


	/**
	 * Gets the power upper.
	 *
	 * @return the power upper
	 */
	public double getPower() {
		return power;
	}

	/**
	 * Sets the power upper.
	 *
	 * @param powerUpper the new power upper
	 */
	public void setPowerUpper(double power) {
		this.power = power;
	}


	/**
	 * Gets the bic ref D.
	 *
	 * @return the bic ref D
	 */
	public double getBicRefD() {
		return bicRefD;
	}

	/**
	 * Sets the bic ref D.
	 *
	 * @param bicRefD the new bic ref D
	 */
	public void setBicRefD(double bicRefD) {
		this.bicRefD = bicRefD;
	}

	/**
	 * Gets the beta.
	 *
	 * @return the beta
	 */
	public double getBeta() {
		return beta;
	}

	/**
	 * Sets the beta.
	 *
	 * @param beta the new beta
	 */
	public void setBeta(double beta) {
		this.beta = beta;
	}

}// end class
