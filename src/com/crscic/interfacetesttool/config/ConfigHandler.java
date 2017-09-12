/**
 * 
 */
package com.crscic.interfacetesttool.config;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import com.crscic.interfacetesttool.log.Log;
import com.crscic.interfacetesttool.xmlhelper.XmlHelper;

/**
 * 
 * @author ken_8
 * 2017年9月12日 下午10:23:08
 */
public class ConfigHandler
{
	private XmlHelper xml;
	
	public ConfigHandler(XmlHelper xml)
	{
		this.xml = xml;
	}
	
	public String getConnectType()
	{
		return xml.getSingleElement("/root/config/type").getStringValue();
	}
	
	public SocketConfig getSocketConfig()
	{
		Element socketNode = xml.getSingleElement("/root/socket");
		return XmlHelper.fill(socketNode, SocketConfig.class);
	}
	
	public ComConfig getComConfig()
	{
		Element comNode = xml.getSingleElement("/root/com");
		return XmlHelper.fill(comNode, ComConfig.class);
	}
	
	/**
	 * 获取协议配置信息
	 * 
	 * @return
	 * @author ken_8 2017年9月12日 上午12:25:04
	 */
	public Config getConfig()
	{
		Config proConfig = new Config();
		proConfig.setProFilePath(xml.getSingleElement("/root/protocol").attributeValue("config"));
		Log.info("协议文件路径：" + proConfig.getProFilePath());
		// 发送配置
		List<SendConfig> sendCfgList = new ArrayList<SendConfig>();
		List<Element> proList = xml.getElements("//interval");
		for (Element pro : proList)
		{
			SendConfig proCfg = new SendConfig();
			proCfg.setInterval(pro.attributeValue("time"));
			proCfg.setProtocol(pro.attributeValue("protocol"));
			sendCfgList.add(proCfg);
		}
		proConfig.setSendConfig(sendCfgList);
		// 回复配置
		List<ReplyConfig> replyCfgList = new ArrayList<ReplyConfig>();
		List<Element> respEleList = xml.getElements("//response");
		for (Element respEle : respEleList)
		{
			ReplyConfig replyCfg = new ReplyConfig();
			replyCfg.setField(respEle.element("field").getStringValue());
			replyCfg.setValue(respEle.elementTextTrim("value"));
			replyCfg.setHead(respEle.elementTextTrim("head"));
			replyCfg.setNodeClass(respEle.elementTextTrim("class"));
			replyCfg.setPro(respEle.elementTextTrim("pro"));
			replyCfg.setQuoteField(respEle.element("quote").element("field").getTextTrim());
			replyCfg.setQuoteFieldName(respEle.element("quote").element("field").attributeValue("name"));

			replyCfgList.add(replyCfg);
		}
		proConfig.setReplyConfig(replyCfgList);

		// 打印配置信息
		Log.info("发送协议概况：");
		for (int i = 0; i < proConfig.getSendConfig().size(); i++)
		{
			SendConfig sendCfg = proConfig.getSendConfig().get(i);
			Log.info("协议" + (i + 1) + "：" + sendCfg.getProtocol() + "，发送间隔：" + sendCfg.getInterval() + "毫秒");
		}
		Log.info("回复协议概况：");
		for (int i = 0; i < proConfig.getReplyConfig().size(); i++)
		{
			ReplyConfig repCfg = proConfig.getReplyConfig().get(i);
			Log.info("当据请求的第" + repCfg.getField() + "字节中内容为" + repCfg.getValue() + "时回复协议：" + repCfg.getPro());
			Log.info("    请求的报文头：" + repCfg.getHead() + "，响应消息中的" + (String) repCfg.getQuoteFieldName(String.class)
					+ "字段使用请求中第" + repCfg.getQuoteField() + "字节中的内容");
		}

		return proConfig;
	}
}
