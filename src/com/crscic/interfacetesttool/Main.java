/**
 * 
 */
package com.crscic.interfacetesttool;

import org.dom4j.DocumentException;

import com.crscic.interfacetesttool.config.ProtocolSetting;
import com.crscic.interfacetesttool.config.ConfigHandler;
import com.crscic.interfacetesttool.connector.Connector;
import com.crscic.interfacetesttool.exception.ConnectException;
import com.crscic.interfacetesttool.exception.GenerateDataException;
import com.crscic.interfacetesttool.exception.ParseXMLException;
import com.crscic.interfacetesttool.log.Log;

/**
 * 
 * @author zhaokai 2017年9月13日 下午5:34:16
 */
public class Main
{
	public void test()
	{
		Log.debug("test debug");
	}
	public static void main(String[] args)
	{
		try
		{
			DataFactory factory = new DataFactory("config\\config.xml");
			ProtocolSetting proSetting = factory.getProtocolSetting();
			ConfigHandler dataConfig = factory.getDataConfig(proSetting);
			Connector connector = factory.getConnector();

			SendService service = new SendService();
			service.startService(connector, proSetting, dataConfig);
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
