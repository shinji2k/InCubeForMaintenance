/**
 * 
 */
package com.k.test;

import org.junit.Test;

import com.crscic.interfacetesttool.DataFactory;
import com.crscic.interfacetesttool.SendService;
import com.crscic.interfacetesttool.config.ConfigHandler;
import com.crscic.interfacetesttool.config.ProtocolSetting;
import com.crscic.interfacetesttool.connector.Connector;
import com.crscic.interfacetesttool.exception.ConnectException;

/**
 * 
 * @author zhaokai 2017年9月7日 下午3:41:38
 */
public class Tester
{
	@Test
	public void readConfigTest()
	{
		try
		{
			DataFactory df = new DataFactory("config/config.xml");
			// 获取连接器
			Connector conn = df.getConnector();
			// 获取各种配置
			ProtocolSetting proSetting = df.getProtocolSetting();
			ConfigHandler dataCfg = df.getDataConfig(proSetting);
			// 运行服务，包含发送和回复服务
			SendService service = new SendService();
			service.startService(conn, proSetting, dataCfg);
			// TODO: initResponse
			try
			{
				conn.closeConnect();
			}
			catch (ConnectException e)
			{
			}

		}
		catch (Exception e)
		{

		}
	}
}
