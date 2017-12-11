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
 * 处理所有读取配置的操作
 * 
 * @author ken_8
 * @create 2017年9月12日 下午10:23:08
 */
public class ConfigHandler
{
	private XmlHelper xml;

	public ConfigHandler(XmlHelper xml)
	{
		this.xml = xml;
	}

	/**
	 * 获取解析相关设置信息
	 * 
	 * @return
	 * @author zhaokai
	 * @create 2017年10月13日 下午2:49:14
	 */
	public ParseSetting getParseSetting()
	{
		// 发送重试次数与超时时间
		Element recvSetting = xml.getSingleElement("/root/recv-setting");
		ParseSetting parseSetting = XmlHelper.fill(recvSetting, ParseSetting.class);

		// 协议文件路径
		parseSetting.setProtocolFile(xml.getSingleElement("/root/protocol-file").getTextTrim());

		// 请求列表，有多个请求
		List<Element> requestNodeList = xml.getElements("/root/request");
		List<Request> requestList = new ArrayList<Request>();
		for (Element requestNode : requestNodeList)
		{
			Request request = new Request();
			request.setSendProtocol(requestNode.elementTextTrim("pro"));

			// 回复信息
			Element respEle = requestNode.element("response");
			Response response = getResponse(respEle);
			
			request.setResponse(response);
			requestList.add(request);
		}

		parseSetting.setRequest(requestList);

		return parseSetting;
	}

	public String getConnectType()
	{
		return xml.getSingleElement("/root/config/type").getStringValue();
	}

	public SocketSetting getSocketConfig()
	{
		Element socketNode = xml.getSingleElement("/root/socket");
		return XmlHelper.fill(socketNode, SocketSetting.class);
	}

	public ComSetting getComConfig()
	{
		Element comNode = xml.getSingleElement("/root/com");
		return XmlHelper.fill(comNode, ComSetting.class);
	}

	/**
	 * 返回发送协议设置信息
	 * 
	 * @return
	 * @author zhaokai 
	 * @create 2017年9月27日 下午11:15:07
	 */
	public SendSetting getSendSetting()
	{
		// 发送配置
		Element sendNode = xml.getSingleElement("/root/send");
		if (sendNode == null)
			return null;
		List<Element> proList = XmlHelper.getElements(sendNode);
		if (proList.size() == 0)
			return null;

		Log.debug("协议概况：");
		SendSetting sendSetting = new SendSetting();
		// 协议路径
		sendSetting.setSettingFilePath(sendNode.attributeValue("file"));
		Map<String, Long> proMap = new HashMap<String, Long>();
		List<String> protocolList = new ArrayList<String>();
		for (int i = 0; i < proList.size(); i++)
		{
			String protocol = proList.get(i).attributeValue("protocol");
			protocolList.add(protocol);
			Long interval = Long.parseLong(proList.get(i).attributeValue("time"));
			proMap.put(protocol, interval);
			Log.debug("    协议" + (i + 1) + "：" + protocol + "，发送间隔：" + interval + "毫秒");
		}
		sendSetting.setProtocolList(protocolList);
		sendSetting.setProtocolMap(proMap);
		return sendSetting;
	}

	public ReplySetting getReplySetting()
	{
		Element respNode = xml.getSingleElement("/root/reply");
		if (respNode == null)
			return null;

		List<Element> respEleList = xml.getElements("//response");
		boolean nullReply = false;

		Log.debug("自动回复概况：");
		// 缺少response节点
		if (respEleList.size() == 0 || respEleList.get(0).elements().size() == 0)
			nullReply = true;
		ReplySetting replySetting = new ReplySetting();
		replySetting.setSettingFilePath(respNode.attributeValue("file"));
		List<Response> responseList = new ArrayList<Response>();
		for (int i = 0; i < respEleList.size(); i++)
		{
			Element respEle = respEleList.get(i);
			// response内没有子节点
			if (respEle.elements().size() == 0)
			{
				nullReply = true;
				break;
			}
			Response response = getResponse(respEle);
			responseList.add(response);

			// 打印配置信息
			Log.debug("  当据请求的第" + response.getField() + "字节中内容为" + response.getValue() + "时回复协议："
					+ response.getProtocol());
			Log.debug("    请求的报文头：" + (StringUtils.isNullOrEmpty(response.getHead()) ? "无" : response.getHead()));
			Log.debug("    请求的报文尾：" + (StringUtils.isNullOrEmpty(response.getTail()) ? "无" : response.getTail()));
			if (response.getQuoteInfo() != null && response.getQuoteInfo().keySet().size() > 0)
				for (String quoteField : response.getQuoteInfo().keySet())
					Log.debug("    响应消息中的" + quoteField + "字段使用请求中第" + response.getQuotePosString(quoteField) + "字节中的内容");
		}
		replySetting.setResponseList(responseList);
		if (nullReply)
			Log.debug("   没有配置自动回复信息，不进行自动回复");

		return replySetting;
	}

	/**
	 * 返回协议配置列表
	 * 
	 * @param config
	 * @return
	 * @throws ParseXMLException
	 * @author zhaokai
	 * @date 2017年9月12日 下午12:37:38
	 */
	public List<ProtocolConfig> getProtocolConfigList(List<String> protocolList) throws ParseXMLException
	{
		List<ProtocolConfig> proCfgList = new ArrayList<ProtocolConfig>();
		if (protocolList.size() == 0)
			return proCfgList;
		Log.debug("正在获取协议配置...");
		// 获取发送协议
		for (String protocol : protocolList)
		{
			ProtocolConfig pro = getProtocolConfig(protocol);
			proCfgList.add(pro);
		}
		return proCfgList;
	}

	public ProtocolConfig getProtocolConfig(String protocolName) throws ParseXMLException
	{
		// 获取协议Element
		Log.debug("获取协议 ： " + protocolName);
		Element proEle = xml.getSingleElement("/root/" + protocolName);
		if (proEle == null)
		{
			Log.error("找不到协议：" + protocolName);
			throw new ParseXMLException();
		}
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
	 * @author zhaokai
	 * @create 2017年9月12日 下午12:37:06
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

	/**
	 * 获取deviceList.xml中配置的设备和设备对应的配置文件信息
	 * 
	 * @return
	 * @author ken_8
	 * @create 2017年10月11日 下午11:47:35
	 */
	public static Map<String, String> getDeviceInfo()
	{
		Map<String, String> deviceInfo = null;
		XmlHelper xml;
		try
		{
			xml = new XmlHelper("config/deviceList.xml");
			List<Element> deviceNodeList = xml.getElements("//device");
			if (deviceNodeList.size() == 0)
				return null;
			deviceInfo = new HashMap<String, String>();
			for (Element deviceNode : deviceNodeList)
				deviceInfo.put(deviceNode.getTextTrim(), deviceNode.attributeValue("file"));
		}
		catch (DocumentException e)
		{
			Log.error("读取设备列表设置错误", e);
		}
		return deviceInfo;
	}

	/**
	 * response节点内容注入
	 * @param respEle
	 * @return
	 * @author ken_8
	 * @create 2017年10月15日 上午12:22:58
	 */
	public Response getResponse(Element respEle)
	{
		Response response = new Response();
		response.setField(respEle.elementTextTrim("field"));
		response.setValue(respEle.elementTextTrim("value"));
		response.setHead(respEle.elementTextTrim("head"));
		response.setTail(respEle.elementTextTrim("tail"));
		response.setNodeClass(respEle.elementTextTrim("class"));
		response.setProtocol(respEle.elementTextTrim("pro"));
		Element quoteNode = respEle.element("quote");
		if (quoteNode != null)
		{
			List<Element> quoteFieldList = XmlHelper.getElements(quoteNode);
			for (Element quoteField : quoteFieldList)
				response.setQuoteInfo(quoteField.attributeValue("name"), quoteField.getTextTrim());
		}
		
		return response;
	}
}
