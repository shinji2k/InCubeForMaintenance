package com.crscic.interfacetesttool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentException;

import com.crscic.interfacetesttool.config.ConfigHandler;
import com.crscic.interfacetesttool.config.ParseSetting;
import com.crscic.interfacetesttool.config.ReplySetting;
import com.crscic.interfacetesttool.config.Request;
import com.crscic.interfacetesttool.config.Response;
import com.crscic.interfacetesttool.config.SendSetting;
import com.crscic.interfacetesttool.connector.ComConnector;
import com.crscic.interfacetesttool.connector.Connector;
import com.crscic.interfacetesttool.connector.SocketConnector;
import com.crscic.interfacetesttool.data.Data;
import com.crscic.interfacetesttool.data.ProtocolConfig;
import com.crscic.interfacetesttool.exception.GenerateDataException;
import com.crscic.interfacetesttool.exception.ParseXMLException;
import com.crscic.interfacetesttool.log.Log;
import com.crscic.interfacetesttool.xmlhelper.XmlHelper;

public class DataFactory
{
	private static ConfigHandler setting;
	private Connector connector;
	private XmlHelper configXml;
	
	public ParseSetting getParseSetting(String configFilePath) throws DocumentException
	{
		XmlHelper xml = new XmlHelper(configFilePath);
		ConfigHandler config = new ConfigHandler(xml);
		ParseSetting parseSetting = config.getParseSetting();
		return parseSetting;
	}

	public DataFactory(String configPath) throws DocumentException
	{
		configXml = new XmlHelper();
		Log.debug("加载配置文件：" + configPath);
		configXml.loadXml(configPath);
	}

	/**
	 * 获取连接器
	 * 
	 * @return
	 * @author ken_8 2017年9月12日 上午12:25:33
	 */
	public Connector getConnector() throws DocumentException
	{
		if (setting == null)
			setting = new ConfigHandler(configXml);
		if (setting.getConnectType().toLowerCase().equals("socket"))
			connector = new SocketConnector(setting.getSocketConfig());
		else if (setting.getConnectType().toLowerCase().equals("com"))
			connector = ComConnector.getInstance(setting.getComConfig());

		return connector;
	}

	/**
	 * 获取发送接口的配置信息
	 * 
	 * @return
	 * @author zhaokai
	 * @date 2017年9月28日 下午10:53:15
	 */
	public SendSetting getSendSetting()
	{
		if (setting == null)
			setting = new ConfigHandler(configXml);
		return setting.getSendSetting();
	}

	/**
	 * 获取回复接口的配置信息
	 * 
	 * @return
	 * @author zhaokai
	 * @date 2017年9月28日 下午10:52:52
	 */
	public ReplySetting getReplySetting()
	{
		if (setting == null)
			setting = new ConfigHandler(configXml);
		return setting.getReplySetting();
	}

	/**
	 * 生成发送数据
	 * 
	 * @author zhaokai 2017年9月12日 下午12:38:22
	 * @throws GenerateDataException
	 */
	public byte[] getSendData(ProtocolConfig proConfig) throws GenerateDataException
	{
		Data data = new Data();
		return data.getSendData(proConfig, new HashMap<String, byte[]>());
	}

	/**
	 * 获取协议配置文件中的协议字段配置信息
	 * 
	 * @param settingFilePath
	 * @param responseList
	 * @return
	 * @author zhaokai
	 * @throws DocumentException
	 * @throws ParseXMLException
	 * @date 2017年9月29日 下午6:36:05
	 */
	public <T> List<ProtocolConfig> getDataConfig(String settingFilePath, List<T> entityList)
			throws DocumentException, ParseXMLException
	{
		List<ProtocolConfig> proCfgList = null;
		if (entityList.size() == 0)
			return proCfgList;
		List<String> protocolList = new ArrayList<String>();
		if (entityList.get(0).getClass().getSimpleName().equals("String"))
		{
			for (int i = 0; i < entityList.size(); i++)
				protocolList.add((String) entityList.get(i));
		}
		else if (entityList.get(0).getClass().getSimpleName().equals("Response"))
		{
			for (int i = 0; i < entityList.size(); i++)
			{
				Response response = (Response) entityList.get(i);
				protocolList.add(response.getProtocol());
			}
		}
		else if (entityList.get(0).getClass().getSimpleName().equals("Request"))
		{
			for (int i = 0; i < entityList.size(); i++)
			{
				Request request = (Request) entityList.get(i);
				protocolList.add(request.getSendProtocol());
			}
		}

		XmlHelper dataXml = new XmlHelper();
		dataXml.loadXml(settingFilePath);
		ConfigHandler dataConfig = new ConfigHandler(dataXml);

		proCfgList = dataConfig.getProtocolConfigList(protocolList);
		return proCfgList;
	}

	/**
	 * 获取deviceList.xml中配置的设备和设备对应的配置文件信息
	 * 
	 * @return
	 * @author ken_8
	 * @create 2017年10月11日 下午11:45:05
	 */
	public static Map<String, String> getDeviceInfo()
	{
		return ConfigHandler.getDeviceInfo();
	}

}
