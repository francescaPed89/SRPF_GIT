
package com.telespazio.csg.srpf.feasibility;

// TODO: Auto-generated Javadoc
/**
 * The Class StripWrapper.
 */
public class StripWrapper
{

    /** The strip. */
    private Strip strip = null;

    /** The spot dto. */
    private SpotLightDTO spotDto = null;

    /**
     * Instantiates a new strip wrapper.
     *
     * @param strip
     *            the strip
     * @param spotDto
     *            the spot dto
     */
    public StripWrapper(Strip strip, SpotLightDTO spotDto)
    {
        super();
        this.strip = strip;
        this.spotDto = spotDto;
    }

    /**
     * Gets the spot dto.
     *
     * @return the spot dto
     */
    public SpotLightDTO getSpotDto()
    {
        return this.spotDto;
    }

    /**
     * Sets the spot dto.
     *
     * @param spotDto
     *            the new spot dto
     */
    public void setSpotDto(SpotLightDTO spotDto)
    {
        this.spotDto = spotDto;
    }

    /**
     * Gets the strip.
     *
     * @return the strip
     */
    public Strip getStrip()
    {
        return this.strip;
    }

    /**
     * Sets the strip.
     *
     * @param strip
     *            the new strip
     */
    public void setStrip(Strip strip)
    {
        this.strip = strip;
    }

}
