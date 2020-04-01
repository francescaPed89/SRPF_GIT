package com.telespazio.csg.srpf.suf;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import com.telespazio.csg.srpf.utils.DateUtils;

public class SUFCalculatorTest {

	@Test
	public void testCalculateSUF_STRIP() throws Exception {

		// SUFCalculator sufCalc = new SUFCalculator();
		SUFCalculator sufCalc = new SUFCalculator();
	    Map<String, CSGBicParameters> sensormodeBicParametersMap = new TreeMap<String, CSGBicParameters>();
	    sufCalc.setSensormodeBicParametersMap(sensormodeBicParametersMap);

		sufCalc.getSensormodeBicParametersMap().put("STRIPMAP", new CSGBicParameters(13548.1,7.38,7.38,0.5, 1, 0,0.8));
		sufCalc.getSensormodeBicParametersMap().put("SPOTLIGHT-2A", new CSGBicParameters(18603.59,16.06,16.06,1.87, 1, 0,0.8));

		double startTime = DateUtils.fromEpochToCSKDate("2019-12-12 17:48:00.0");
		
		double stopTime= DateUtils.fromEpochToCSKDate("2019-12-12 17:48:06.6");
		boolean isLeft = false;
		double bic = sufCalc.calculateSUF(QUMConstants.SENSOR_MODE_STRIPMAP, startTime, stopTime, null, isLeft);
		assertEquals(1.0,bic,0);
		//System.out.println(bic);
		
		isLeft = true;
		bic = sufCalc.calculateSUF(QUMConstants.SENSOR_MODE_STRIPMAP, startTime, stopTime, null, isLeft);
		assertEquals(1.8,bic,0);
		//System.out.println(bic);
		
		startTime = DateUtils.fromEpochToCSKDate("2019-12-12 17:48:00.0");
		stopTime= DateUtils.fromEpochToCSKDate("2019-12-12 17:48:16.6");
		isLeft = false;
		bic = sufCalc.calculateSUF(QUMConstants.SENSOR_MODE_STRIPMAP, startTime, stopTime, null, isLeft);
		//assertEquals(1.0,bic,0);
		//System.out.println(bic);
	}

	@Test
	public void testCalculateSUFStringDoubleDoubleStringBoolean() throws Exception {

		// SUFCalculator sufCalc = new SUFCalculator();
		SUFCalculator sufCalc = new SUFCalculator();
	    Map<String, CSGBicParameters> sensormodeBicParametersMap = new TreeMap<String, CSGBicParameters>();
	    sufCalc.setSensormodeBicParametersMap(sensormodeBicParametersMap);

		sufCalc.getSensormodeBicParametersMap().put("STRIPMAP", new CSGBicParameters(13548.1,7.38,7.38,0.5, 1, 0,0.8));

		sufCalc.getSensormodeBicParametersMap().put("SPOTLIGHT-2A", new CSGBicParameters(18603.59,16.06,16.06,1.87, 1, 0,0.8));

		double startTime = DateUtils.fromEpochToCSKDate("2019-12-12 17:48:00.0");
		
		double stopTime= DateUtils.fromEpochToCSKDate("2019-12-12 17:48:06.6");
		boolean isLeft = false;
		double bic = sufCalc.calculateSUF("SPOTLIGHT-2A", startTime, stopTime, null, isLeft);
		//System.out.println(bic);
		assertEquals(2.9881854392166787,bic,0);
		
		isLeft = true;
		bic = sufCalc.calculateSUF("SPOTLIGHT-2A", startTime, stopTime, null, isLeft);
		//System.out.println(bic);
		assertEquals(3.7881854392166785,bic,0);

	}

	@Test
	public void testCalculateSUF() throws Exception {

		// SUFCalculator sufCalc = new SUFCalculator();
		SUFCalculator sufCalc = new SUFCalculator();
	    Map<String, CSGBicParameters> sensormodeBicParametersMap = new TreeMap<String, CSGBicParameters>();
	    sufCalc.setSensormodeBicParametersMap(sensormodeBicParametersMap);

		sufCalc.getSensormodeBicParametersMap().put("STRIPMAP", new CSGBicParameters(13548.1,7.38,7.38,0.5, 1, 0,0.8));

		sufCalc.getSensormodeBicParametersMap().put("SPOTLIGHT-2A", new CSGBicParameters(18603.59,16.06,16.06,1.87, 1, 0,0.8));

		double startTime = DateUtils.fromEpochToCSKDate("2019-12-12 17:48:00.0");
		
		double stopTime= DateUtils.fromEpochToCSKDate("2019-12-12 17:48:06.6");
		boolean isLeft = false;
		double bic = sufCalc.calculateSUF("SPOTLIGHT-2A", startTime, stopTime, null, isLeft);
		//System.out.println(bic);
		assertEquals(2.9881854392166787,bic,0);
		
		isLeft = true;
		bic = sufCalc.calculateSUF("SPOTLIGHT-2A", startTime, stopTime, null, isLeft);
		//System.out.println(bic);
		assertEquals(3.7881854392166785,bic,0);

	}
	
}
