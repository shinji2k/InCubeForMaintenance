/**
 * 
 */
package com.k.test;

import java.util.List;

import org.dom4j.DocumentException;
import org.junit.Test;

import com.crscic.interfacetesttool.DataFactory;
import com.crscic.interfacetesttool.config.ProtocolSetting;
import com.crscic.interfacetesttool.connector.Connector;
import com.crscic.interfacetesttool.data.Data;
import com.crscic.interfacetesttool.data.ProtocolConfig;
import com.crscic.interfacetesttool.exception.ConnectException;
import com.crscic.interfacetesttool.exception.GenerateDataException;
import com.crscic.interfacetesttool.exception.ParseXMLException;
import com.k.util.ByteUtils;

/**
 * 
 * @author zhaokai
 * 2017年9月7日 下午3:41:38
 */
public class Tester
{
	@Test
	public void readConfigTest()
	{
		try
		{
			DataFactory df = new DataFactory("config/config.xml");
			//TODO: initConfig-getProtocolConfig
			Connector conn = df.getConnector();
			
//			String out = ByteUtils.byteArraytoHexString(df.getSendData(proStructList.get(0)));
//			System.out.println(out);
			//TODO: initProtocol-getProtocolData one or more
			//TODO: sendProtocol
//			conn.openConnect();
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
		catch (GenerateDataException e)
		{
			e.printStackTrace();
		}
		catch (ConnectException e)
		{
			e.printStackTrace();
		}
	}
}
