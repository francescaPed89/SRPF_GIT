
package com.telespazio.csg.srpf.feasibility;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.telespazio.csg.srpf.dataManager.bean.BeamBean;
import com.telespazio.csg.srpf.dataManager.bean.SatelliteBean;

public class InnerOptimizationLoopIteratorTest
{

    @Test
    public void testFindTmaxInStripList() throws Exception
    {

        InnerOptimizationLoopIterator innerloopIter = new InnerOptimizationLoopIterator();

        Access accessP1 = new Access();
        Access accessP2 = new Access();

        SatelliteBean satBean = new SatelliteBean();
        satBean.setIdSatellite(1);
        satBean.setSatelliteName("SAT1");
        Satellite satellite = new Satellite(satBean);

        BeamBean beam = new BeamBean();
        beam.setBeamName("beamName");
        beam.setIdBeam(2);

        List<Access> accessListP1 = new ArrayList<>();
        accessP1.setAccessTime(600);
        accessP1.setSatellite(satellite);
        accessP1.setBeam(beam);
        accessListP1.add(accessP1);

        accessP2.setAccessTime(560);
        accessP2.setSatellite(satellite);
        accessP2.setBeam(beam);

        accessListP1.add(accessP2);

        Strip strip1 = new Strip(0, accessListP1); // startTime willbe accessP1
                                                   // -> 600

        Access accessP3 = new Access();
        accessP3.setSatellite(satellite);
        accessP3.setBeam(beam);

        Access accessP4 = new Access();
        accessP4.setAccessTime(400);
        accessP4.setSatellite(satellite);
        accessP4.setBeam(beam);

        List<Access> accessListP2 = new ArrayList<>();
        accessListP2.add(accessP3);
        accessListP2.add(accessP4);

        Strip strip2 = new Strip(0, accessListP2); // startTime willbe accessP2
                                                   // -> 200

        Access accessP5 = new Access();
        accessP5.setAccessTime(1000);
        accessP5.setSatellite(satellite);
        accessP5.setBeam(beam);

        Access accessP6 = new Access();
        accessP6.setAccessTime(200);
        accessP6.setSatellite(satellite);
        accessP6.setBeam(beam);

        List<Access> accessListP3 = new ArrayList<>();
        accessListP3.add(accessP5);
        accessListP3.add(accessP6);

        Strip strip3 = new Strip(0, accessListP3); // startTime willbe accessP3
                                                   // -> 1000

        List<Strip> stripList = new ArrayList<>();
        stripList.add(strip1);
        stripList.add(strip2);
        stripList.add(strip3);

        innerloopIter.findTmaxInStripList(stripList);
    }

}
