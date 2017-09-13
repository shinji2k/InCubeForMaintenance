/**
 * 
 */
package com.crscic.interfacetesttool;

import org.dom4j.DocumentException;

import com.crscic.interfacetesttool.config.Config;
import com.crscic.interfacetesttool.config.ConfigHandler;
import com.crscic.interfacetesttool.config.SendConfig;
import com.crscic.interfacetesttool.connector.Connector;
import com.crscic.interfacetesttool.data.Data;
import com.crscic.interfacetesttool.exception.ConnectException;
import com.crscic.interfacetesttool.exception.GenerateDataException;
import com.crscic.interfacetesttool.exception.ParseXMLException;
import com.crscic.interfacetesttool.xmlhelper.XmlHelper;

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
	
	
	
	public void noName(Connector connector) throws ParseXMLException, GenerateDataException, DocumentException, ConnectException
	{
		Config proSetting = config.getConfig();
		// 获取协议配置
		dataXml = new XmlHelper();
		dataXml.loadXml(proSetting.getProFilePath());
		dataConfig = new ConfigHandler(dataXml);
		connector.openConnect();
		Long lastSendTime = System.currentTimeMillis();
		Data sendData = new Data();
		while (true)
		{
			// 生成协议数据
			for (SendConfig sendCfg : proSetting.getSendConfig())
			{	
				Long currInterval = System.currentTimeMillis() - lastSendTime;
				if (currInterval >= Long.parseLong(sendCfg.getInterval()))
				{
					byte[] data = sendData.getSendData(dataConfig.getProtocolConfig(sendCfg.getProtocol()));
					connector.send(data);
					lastSendTime = System.currentTimeMillis();
				}
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
