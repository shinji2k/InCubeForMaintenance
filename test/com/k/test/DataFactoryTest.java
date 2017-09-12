/**
 * 
 */
package com.k.test;

import java.util.List;

import org.dom4j.DocumentException;
import org.junit.Test;

import com.crscic.interfacetesttool.DataFactory;
import com.crscic.interfacetesttool.entity.ProtocolConfig;
import com.crscic.interfacetesttool.entity.ProtocolStructure;
import com.crscic.interfacetesttool.exception.ParseXMLException;

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
			List<ProtocolStructure> proStructList = df.getProtocolStructure(proCfg);
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
		catch (ParseXMLException e)
		{
			e.printStackTrace();
		}
	}
}
