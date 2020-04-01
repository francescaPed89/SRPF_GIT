package com.telespazio.csg.srpf.feasibility;

import org.junit.Test;

public class SPARCManagerTest {

	@Test
	public void testInitialize() throws Exception {

		String test = "e394be70a25ee454d5afade8d584af4f  SPARC_V4.1.tar.gz";
		if ((test) != null) {
			
			//  SPARC_V4.1.tar.gz
			if(test.contains("SPARC"))
			{
				String sparcVersion = test.substring(test.indexOf("SPARC"), test.length());
				//System.out.println("USING SPARC VERSION : "+sparcVersion);
			}
		}
	}

}
