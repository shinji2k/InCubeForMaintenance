/**
 * 
 */
package com.k.test;

import org.dom4j.DocumentException;
import org.junit.Test;

import com.crscic.interfacetesttool.DataFactory;
import com.crscic.interfacetesttool.entity.ProtocolConfig;
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
//			Connector conn = df.getConnector();
			//TODO: initConfig-getProtocolConfig
			ProtocolConfig proCfg = df.getProtocolConfig();
			//TODO: initProtocol-getProtocolData one or more
			//TODO: sendProtocol
//			conn.send();
			//TODO: initResponse
//			try
//			{
//				conn.closeConnect();
//			}
//			catch (ConnectException e)
//			{
//			}
			
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
