package com.telespazio.csg.srpf.feasibility;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.telespazio.csg.srpf.dataManager.bean.BeamBean;
import com.telespazio.csg.srpf.dataManager.bean.SatelliteBean;

public class ObjectiveFunctionTest
{

    /*
     *     public String getUnivocalKey()
    {
        // TODO: chiedere a Riccardo se così è univoco
        return this.getId() + "_" + this.getBeamId() + "_" + this.getSatelliteId() + "_" + this.getStartTime() + "_" + this.getStopTime();
    }
     * 
     */
    @Test
    public void testFindStripByUnivocalId() throws Exception
    {
        String optId = "1_S2B-027_SSAR1_6010.2552_652.255";
        Access access1 = new Access();
        SatelliteBean satBeam= new SatelliteBean();
        satBeam.setSatelliteName("SSAR1");
        Satellite sat= new Satellite(satBeam);
        access1.setSatellite(sat);
        BeamBean beam1 = new BeamBean();
        beam1.setIdBeam(6);
        beam1.setBeamName("S2B-027");
        access1.setBeam(beam1);
        List<Access> accList1 = new ArrayList<Access>();
        accList1.add(access1);
        Strip strip1 = new Strip(0,accList1);


        strip1.setAccessList(accList1);
        
        strip1.setBeamId("S2B-027");
        strip1.setId(1);
        strip1.setSatelliteId("SSAR1");
        
        
        Strip strip2 = new Strip(1,accList1);


        strip2.setAccessList(accList1);
        strip2.setBeamId("S2B-028");
        strip2.setId(2);
        strip2.setSatelliteId("SSAR2");
        
        Strip strip3 = new Strip(1,accList1);


        strip3.setAccessList(accList1);
        strip3.setBeamId("S2B-027");
        strip3.setId(2);
        strip3.setSatelliteId("SSAR2");
        
        List<Strip> allStrips = new ArrayList<Strip>();
        allStrips.add(strip2);
        allStrips.add(strip3);

        allStrips.add(strip1);

        
       Strip returnedStrip =  ObjectiveFunction.findStripByUnivocalId(optId, allStrips);
       assertEquals(returnedStrip, strip1);
       
       allStrips.clear();
       returnedStrip =  ObjectiveFunction.findStripByUnivocalId(optId, allStrips);
       assertEquals(null, returnedStrip);

    }

}
