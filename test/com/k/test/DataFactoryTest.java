/**
 * 
 */
package com.k.test;

import org.dom4j.DocumentException;
import org.junit.Test;

import com.crscic.interfacetesttool.DataFactory;
import com.crscic.interfacetesttool.log.Log;

/**
 * 
 * @author zhaokai
 * 2017年9月7日 下午3:41:38
 */
public class DataFactoryTest
{
	@Test
	public void readConfigTest()
	{
		try
		{
			DataFactory df = new DataFactory("config/config.xml");
			
		}
		catch (DocumentException e)
		{
			e.printStackTrace();
		}
	}
	
	@Test
	public void LogTest()
	{
		Log.info("test log");
	}
}
