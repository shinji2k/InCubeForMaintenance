package com.crscic.interfacetesttool;

import org.dom4j.DocumentException;
import com.crscic.interfacetesttool.config.Config;
import com.crscic.interfacetesttool.config.ConfigHandler;
import com.crscic.interfacetesttool.config.SendConfig;
import com.crscic.interfacetesttool.connector.ComConnector;
import com.crscic.interfacetesttool.connector.Connector;
import com.crscic.interfacetesttool.connector.SocketConnector;
import com.crscic.interfacetesttool.data.Data;
import com.crscic.interfacetesttool.data.ProtocolConfig;
import com.crscic.interfacetesttool.exception.ConnectException;
import com.crscic.interfacetesttool.exception.GenerateDataException;
import com.crscic.interfacetesttool.exception.ParseXMLException;
import com.crscic.interfacetesttool.log.Log;
import com.crscic.interfacetesttool.xmlhelper.XmlHelper;

public class DataFactory
{

	private ConfigHandler config;
	private ConfigHandler dataConfig;
	private Connector connector;
	private XmlHelper configXml;
	private XmlHelper dataXml;

	public DataFactory(String configPath) throws DocumentException
	{
		configXml = new XmlHelper();
		Log.info("读取程序配置：" + configPath);
		configXml.loadXml(configPath);
		config = new ConfigHandler(configXml);
	}

	/**
	 * 获取连接器
	 * 
	 * @return
	 * @author ken_8 2017年9月12日 上午12:25:33
	 */
	public Connector getConnector() throws DocumentException
	{
		Log.info("接口类型为：" + config.getConnectType());
		if (config.getConnectType().toLowerCase().equals("socket"))
			connector = new SocketConnector(config.getSocketConfig());
		else if (config.getConnectType().toLowerCase().equals("com"))
			connector = new ComConnector(config.getComConfig());

		return connector;
	}

	public void noName() throws ParseXMLException, GenerateDataException, DocumentException, ConnectException
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


	/**
	 * 生成发送数据
	 * 
	 * @author zhaokai 2017年9月12日 下午12:38:22
	 * @throws GenerateDataException
	 */
	public byte[] getSendData(ProtocolConfig proStruct) throws GenerateDataException
	{
		Data data = new Data();
		return data.getSendData(proStruct);
	}
}
