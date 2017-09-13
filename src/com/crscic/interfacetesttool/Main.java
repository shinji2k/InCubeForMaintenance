/**
 * 
 */
package com.crscic.interfacetesttool;

import org.dom4j.DocumentException;

import com.crscic.interfacetesttool.config.Config;
import com.crscic.interfacetesttool.config.ConfigHandler;
import com.crscic.interfacetesttool.connector.Connector;
import com.crscic.interfacetesttool.exception.ConnectException;
import com.crscic.interfacetesttool.exception.GenerateDataException;
import com.crscic.interfacetesttool.exception.ParseXMLException;

/**
 * 
 * @author zhaokai
 * 2017年9月13日 下午5:34:16
 */
public class Main
{
	public static void main(String[] args)
	{
		try
		{
			DataFactory factory = new DataFactory("config\\config.xml");
			Config setting = factory.getSetting();
			ConfigHandler dataConfig = factory.getDataConfig();
			Connector connector = factory.getConnector();
			
			SendService service = new SendService();
			service.startService(connector, dataConfig);
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
