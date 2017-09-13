/**
 * 
 */
package com.crscic.interfacetesttool;

import org.dom4j.DocumentException;

import com.crscic.interfacetesttool.config.Config;
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
			
			SendService service = new SendService();
			
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
