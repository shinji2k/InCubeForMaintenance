/**
 * 
 */
package com.crscic.interfacetesttool.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import com.crscic.interfacetesttool.data.Part;
import com.crscic.interfacetesttool.data.ProtocolConfig;
import com.crscic.interfacetesttool.exception.ParseXMLException;
import com.crscic.interfacetesttool.log.Log;
import com.crscic.interfacetesttool.xmlhelper.XmlHelper;
import com.k.util.StringUtils;

/**
 * 
 * @author ken_8 2017年9月12日 下午10:23:08
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

	public List<SendConfig> getSendConfig()
	{
		// 发送配置
		List<SendConfig> sendCfgList = new ArrayList<SendConfig>();
		List<Element> proList = xml.getElements("//interval");
		Log.info("协议概况：");
		for (int i = 0; i < proList.size(); i++)
		{
			SendConfig proCfg = new SendConfig();
			proCfg.setInterval(proList.get(i).attributeValue("time"));
			proCfg.setProtocol(proList.get(i).attributeValue("protocol"));
			sendCfgList.add(proCfg);
			Log.info("协议" + (i + 1) + "：" + proCfg.getProtocol() + "，发送间隔：" + proCfg.getInterval() + "毫秒");
		}
		return sendCfgList;
	}

	public List<ReplyConfig> getReplyConfig()
	{
		List<ReplyConfig> replyCfgList = new ArrayList<ReplyConfig>();
		List<Element> respEleList = xml.getElements("//response");
		Log.info("自动回复概况：");
		for (int i = 0; i < respEleList.size(); i++)
		{
			ReplyConfig replyCfg = new ReplyConfig();
			Element respEle = respEleList.get(i);
			replyCfg.setField(respEle.element("field").getStringValue());
			replyCfg.setValue(respEle.elementTextTrim("value"));
			replyCfg.setHead(respEle.elementTextTrim("head"));
			replyCfg.setNodeClass(respEle.elementTextTrim("class"));
			replyCfg.setPro(respEle.elementTextTrim("pro"));
			replyCfg.setQuoteField(respEle.element("quote").element("field").getTextTrim());
			replyCfg.setQuoteFieldName(respEle.element("quote").element("field").attributeValue("name"));

			replyCfgList.add(replyCfg);
			
			// 打印配置信息
			Log.info("当据请求的第" + replyCfg.getField() + "字节中内容为" + replyCfg.getValue() + "时回复协议：" + replyCfg.getPro());
			Log.info("    请求的报文头：" + replyCfg.getHead() + "，响应消息中的" + (String) replyCfg.getQuoteFieldName(String.class)
					+ "字段使用请求中第" + replyCfg.getQuoteField() + "字节中的内容");
		}


		return replyCfgList;
	}

	/**
	 * 获取程序配置中有关协议的部分
	 * 
	 * @return
	 * @author ken_8 2017年9月12日 上午12:25:04
	 * @throws ParseXMLException
	 */
	public Config getConfig() throws ParseXMLException
	{
		Config proConfig = new Config();
		proConfig.setProFilePath(xml.getSingleElement("/root/protocol").attributeValue("config"));

		if (StringUtils.isNullOrEmpty(proConfig.getProFilePath()))
		{
			Log.error("协议配置文件路径为空");
			throw new ParseXMLException();
		}

		Log.info("协议文件路径：" + proConfig.getProFilePath());
		proConfig.setSendConfig(getSendConfig());
		proConfig.setReplyConfig(getReplyConfig());

		return proConfig;
	}

	/**
	 * 返回协议配置列表
	 * 
	 * @param config
	 * @return
	 * @throws ParseXMLException
	 * @author zhaokai 2017年9月12日 下午12:37:38
	 */
	public List<ProtocolConfig> getProtocolConfigList(Config config) throws ParseXMLException
	{
		List<ProtocolConfig> proCfgList = new ArrayList<ProtocolConfig>();
		try
		{
			xml.loadXml(config.getProFilePath());
			Log.info("正在生成协议数据...");
			// 获取发送协议
			for (SendConfig pro : config.getSendConfig())
			{
				Log.info("协议：" + pro.getProtocol() + ":");
				ProtocolConfig protocol = getProtocolConfig(pro.getProtocol());
				proCfgList.add(protocol);
			}
		}
		catch (DocumentException e)
		{
			Log.error("读取协议配置文件错误", e);
			throw new ParseXMLException();
		}

		return proCfgList;
	}
	
	public ProtocolConfig getProtocolConfig(String protocolName)
	{
		// 获取协议Element
		Element proEle = xml.getSingleElement("/root/" + protocolName);
		// 填装协议配置实体
		List<Part> partList = fillPart(proEle);

		ProtocolConfig protocol = new ProtocolConfig();
		protocol.setProtocolName(protocolName);
		protocol.setPart(partList);
		
		return protocol;
	}
	
	/**
	 * 将Part节点内容填充的实体
	 * 
	 * @param ele
	 * @return
	 * @author zhaokai 2017年9月12日 下午12:37:06
	 */
	
	private List<Part> fillPart(Element ele)
	{
		List<Part> partList = new ArrayList<Part>();
		List<Element> partEleList = XmlHelper.getElements(ele);
		for (Element partEle : partEleList)
		{
			Part part = new Part();
			// 设置节点属性
			Map<String, String> attrMap = null;
			List<Attribute> attrList = XmlHelper.getAttributes(partEle);
			if (attrList.size() > 0)
			{
				attrMap = new HashMap<String, String>();
				for (Attribute attr : attrList)
					attrMap.put(attr.getName(), attr.getValue());
			}
			part.setAttribute(attrMap);

			// 类型
			part.setType(partEle.element("type").getTextTrim());
			// 分隔符
			part.setSplit(partEle.element("split") == null ? null : partEle.element("split").getTextTrim());
			// 补全字节
			part.setFillByte(partEle.element("fill-byte") == null ? null : partEle.element("fill-byte").getTextTrim());
			// 补全方向
			part.setFillDirection(
					partEle.element("fill-direction") == null ? null : partEle.element("fill-direction").getTextTrim());
			// value类型
			part.setValueClass(partEle.element("class") == null ? null : partEle.element("class").getTextTrim());
			// 长度
			part.setLen(partEle.element("len") == null ? null : partEle.element("len").getTextTrim());
			// 值，分子节点和数值两种情况
			Element valueNode = partEle.element("value");
			if (valueNode.element("part") == null)
			{
				part.setValue(partEle.element("value").getTextTrim());
				part.setChildNodeList(new ArrayList<Part>());
			}
			else
			{
				part.setChildNodeList(fillPart(valueNode));
			}

			partList.add(part);
		}
		return partList;
	}
}
