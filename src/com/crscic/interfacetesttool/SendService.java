package com.crscic.interfacetesttool;

import java.util.List;

import org.dom4j.DocumentException;

import com.crscic.interfacetesttool.config.ConfigHandler;
import com.crscic.interfacetesttool.config.SendConfig;
import com.crscic.interfacetesttool.connector.Connector;
import com.crscic.interfacetesttool.data.Data;
import com.crscic.interfacetesttool.exception.ConnectException;
import com.crscic.interfacetesttool.exception.GenerateDataException;
import com.crscic.interfacetesttool.exception.ParseXMLException;

/**
 * 
 * @author zhaokai
 * 2017年9月13日 下午4:57:09
 */
public class SendService
{
	public SendService()
	{
		
	}
	
	public void startService (Connector connector, ConfigHandler dataConfig) throws ParseXMLException, GenerateDataException, DocumentException, ConnectException
	{
		// 获取协议配置
		connector.openConnect();
		Long lastSendTime = System.currentTimeMillis();
		Data sendData = new Data();
		while (true)
		{
			// 生成协议数据
			for (SendConfig sendCfg : dataConfig.getSendConfig())
			{	
				Long currInterval = System.currentTimeMillis() - lastSendTime;
				if (currInterval >= Long.parseLong(sendCfg.getInterval()))
				{
					byte[] data = sendData.getSendData(dataConfig.getProtocolConfig(sendCfg.getProtocol()));
					connector.send(data);
					lastSendTime = System.currentTimeMillis();
				}
				
				//添加接收部分
				else
				{
					try
					{
						Thread.sleep(100);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
}
