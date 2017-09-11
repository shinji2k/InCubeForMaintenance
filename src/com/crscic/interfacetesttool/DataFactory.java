package com.crscic.interfacetesttool;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.crscic.interfacetesttool.connector.ComConnector;
import com.crscic.interfacetesttool.connector.Connector;
import com.crscic.interfacetesttool.connector.SocketConnector;
import com.crscic.interfacetesttool.data.IntervalInfo;
import com.crscic.interfacetesttool.data.ResInfo;
import com.crscic.interfacetesttool.data.ResSetting;
import com.crscic.interfacetesttool.data.SocketInfo;
import com.crscic.interfacetesttool.entity.ComConfig;
import com.crscic.interfacetesttool.entity.InterfaceType;
import com.crscic.interfacetesttool.entity.ProtocolConfig;
import com.crscic.interfacetesttool.entity.ReplyConfig;
import com.crscic.interfacetesttool.entity.SendConfig;
import com.crscic.interfacetesttool.entity.SocketConfig;
import com.crscic.interfacetesttool.exception.AppException;
import com.crscic.interfacetesttool.log.Log;
import com.crscic.interfacetesttool.socket.SocketClient;
import com.crscic.interfacetesttool.socket.SocketServer;
import com.crscic.interfacetesttool.xmlhelper.XmlHelper;
import com.k.util.ByteUtils;
import com.k.util.CollectionUtils;
import com.k.util.StringUtils;

public class DataFactory implements Runnable
{

	private Socket s;
	private XmlHelper xh;
	private boolean run = true;
	private List<IntervalInfo> intervalInfoList;
	private String proConfigPath;
	private SocketInfo si;
	private String configPath;
	private List<ResSetting> responseList;
	/*******************************************************************/
	private Connector connector;
	private XmlHelper configXml;
	private XmlHelper dataXml;
	private ProtocolConfig proConfig;

	public DataFactory(String configPath) throws DocumentException
	{
		configXml = new XmlHelper();
		Log.info("读取配置文件：" + configPath);
		configXml.loadXml(configPath);
	}

	public Connector getConnector()
	{
		Element configNode = configXml.getSingleElement("//config");
		InterfaceType interfaceType = configXml.fill(configNode, InterfaceType.class);
		Log.info("接口类型为：" + interfaceType.getType());
		setConnector(interfaceType.getType());
		return connector;
	}

	private void setConnector(String connectorType)
	{
		Log.info("初始化接口...");
		Element connectorNode = null;
		if (connectorType.toLowerCase().equals("socket"))
		{
			connectorNode = configXml.getSingleElement("/root/socket");
			SocketConfig sockCfg = configXml.fill(connectorNode, SocketConfig.class);
			connector = new SocketConnector(sockCfg);
		}
		else if (connectorType.toLowerCase().equals("com"))
		{
			connectorNode = configXml.getSingleElement("/root/com");
			ComConfig comCfg = configXml.fill(connectorNode, ComConfig.class);
			connector = new ComConnector(comCfg);
		}
	}

	public ProtocolConfig getProtocolConfig()
	{
		proConfig = new ProtocolConfig();
		proConfig.setProFilePath(configXml.getSingleElement("/root/protocol").attributeValue("config"));
		Log.info("协议文件路径：" + proConfig.getProFilePath());
		// 发送配置
		List<SendConfig> sendCfgList = new ArrayList<SendConfig>();
		List<Element> proList = configXml.getElements("//interval");
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
		List<Element> respEleList = configXml.getElements("//response");
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
			Log.info("    请求的报文头：" + repCfg.getHead() + "，响应消息中的" + (String) repCfg.getQuoteFieldName(String.class) + "字段使用请求中第" + repCfg.getQuoteField() + "字节中的内容");
		}

		return proConfig;
	}

	/****************************************************************/
	public DataFactory(Socket s, String configPath)
	{
		this.s = s;
		init(configPath);
	}

	public DataFactory()
	{
		super();
	}

	/**
	 * 以Server的形式启动socket
	 *
	 * @author zhaokai
	 * @version 2017年3月13日 上午11:14:32
	 */
	public void startSocketServer()
	{
		if (si == null)
		{
			System.out.println("Need init() first.");
			return;
		}
		SocketServer ss = new SocketServer(this);
		ss.start();
	}

	/**
	 * 以client启动socket并自动连接，连接后监听从server返回的消息
	 *
	 * @author zhaokai
	 * @version 2017年3月13日 下午5:17:14
	 */
	public void startSocketClient()
	{
		if (si == null)
		{
			System.out.println("Need init() first.");
			return;
		}

		SocketClient sc = new SocketClient(this);
		try
		{
			sc.start();
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 根据config/config.xml进行初始化，获取socket信息及需要发送协议及发送间隔
	 *
	 * @param configPath
	 * @author zhaokai
	 * @version 2017年3月8日 下午5:09:02
	 */
	@SuppressWarnings("unchecked")
	public void init(String configPath)
	{
		this.configPath = configPath;
		SAXReader saxReader = new SAXReader();
		intervalInfoList = new ArrayList<IntervalInfo>();
		try
		{
			// 读取配置文件
			Document xmlDocument = saxReader.read(configPath);
			Element root = xmlDocument.getRootElement();
			Element socketNode = root.element("socket");
			if (socketNode == null)
				throw new AppException("配置文件：" + configPath + " 解析失败，socket节点错误或不存在。");
			// 初始化socket信息
			si = new SocketInfo();
			Element typeNode = socketNode.element("type");
			if (typeNode == null)
				throw new AppException("配置文件：" + configPath + " 解析失败，socket节点中的type节点错误或不存在。");
			String typeString = typeNode.getTextTrim();
			if (StringUtils.isNullOrEmpty(typeString))
				throw new AppException("配置文件：" + configPath + " 解析失败，socket节点中的type节点为空。");
			si.setType(typeString.toLowerCase());

			if (si.getType().equals("client"))
			{
				Element ipNode = socketNode.element("ip");
				if (ipNode == null)
					throw new AppException("配置文件：" + configPath + " 解析失败，socket节点中的ip节点错误或不存在。");
				String ipString = ipNode.getTextTrim();
				if (StringUtils.isNullOrEmpty(ipString))
					throw new AppException("配置文件：" + configPath + " 解析失败，socket节点中的ip节点为空。");
				si.setIp(ipString);
			}

			Element portNode = socketNode.element("port");
			if (portNode == null)
				throw new AppException("配置文件：" + configPath + " 解析失败，socket节点中的port节点错误或不存在。");
			String portString = portNode.getTextTrim();
			if (StringUtils.isNullOrEmpty(portString))
				throw new AppException("配置文件：" + configPath + " 解析失败，socket节点中的port节点为空。");
			si.setPort(portString);

			// 初始化协议信息
			Element protocolNode = root.element("protocol");
			if (null == protocolNode)
				throw new AppException("配置文件：" + configPath + " 解析失败，protocol节点错误或不存在。");
			String proConfigPath = protocolNode.attributeValue("config");
			if (StringUtils.isNullOrEmpty(proConfigPath))
				throw new AppException("配置文件：" + configPath + " 解析失败，protocol节点的属性config为空或不存在。");
			this.proConfigPath = proConfigPath;
			List<Element> intervals = protocolNode.elements("interval");
			if (0 == intervals.size())
				throw new AppException("配置文件：" + configPath + " 解析失败，protocol节点中的interval节点不存在。");
			for (Element intervalNode : intervals)
			{
				String time = intervalNode.attributeValue("time");
				if (StringUtils.isNullOrEmpty(time))
					throw new AppException("配置文件：" + configPath + " 解析失败，interval节点的属性time为空或不存在。");
				String proName = intervalNode.attributeValue("protocol");
				if (StringUtils.isNullOrEmpty(proName))
					throw new AppException("配置文件：" + configPath + " 解析失败，interval节点的属性protocol为空或不存在。");
				IntervalInfo ii = new IntervalInfo();
				ii.setIntervalMillis(Long.parseLong(time));
				ii.setProtocolName(proName);
				intervalInfoList.add(ii);
			}

			// 初始化自动回复信息
			List<Element> resNodeList = protocolNode.elements("response");
			if (resNodeList.size() > 0)
			{
				responseList = new ArrayList<ResSetting>();
				for (Element resNode : resNodeList)
				{
					ResSetting resSetting = new ResSetting();
					String posString = XmlHelper.getNodeValue(resNode, "field");
					String pos[] = posString.split(",");
					resSetting.setPos(pos);
					String valueString = XmlHelper.getNodeValue(resNode, "value");
					String headString = XmlHelper.getNodeValue(resNode, "head");
					String classString = XmlHelper.getNodeValue(resNode, "class");
					if (classString.equals("byte"))
					{
						resSetting.setCommandType(valueString);
						resSetting.setHead(headString);
					}
					else
					{
						resSetting.setCommandType(valueString.getBytes());
						resSetting.setHead(headString.getBytes());
					}
					List<Element> quoteNodeList = resNode.elements("quote");
					if (quoteNodeList.size() > 0)
					{
						for (Element quoteNode : quoteNodeList)
						{
							Element fieldNode = quoteNode.element("field");
							String quotePosString = fieldNode.getTextTrim();
							String quotePos[] = quotePosString.split(",");
							String quoteNameString = fieldNode.attributeValue("name");
							String quoteNames[] = quoteNameString.split(",");

							Map<String, String[]> quoteMap = new HashMap<String, String[]>();
							for (String quoteName : quoteNames)
								quoteMap.put(quoteName, quotePos);
							resSetting.setQuoteMap(quoteMap);
							// TODO:rule
							responseList.add(resSetting);
						}
					}
				}
			}

		}
		catch (DocumentException e)
		{
			e.printStackTrace();
		}
		catch (AppException ae)
		{
			System.out.println(ae.getMessage());
		}
	}

	/**
	 * 根据配置文件中的配置自适应以服务端或客户端启动socket
	 *
	 * @author zhaokai
	 * @version 2017年5月10日 上午11:13:05
	 */
	public void startSocket()
	{
		if (si == null)
		{
			System.out.println("Call init() first please.");
			return;
		}

		if (si.getType().equals("client"))
			this.startSocketClient();
		else
			this.startSocketServer();
	}

	/**
	 * 远程线程调用。处理接收的消息
	 */
	public void run()
	{
		xh = new XmlHelper(proConfigPath);
		System.out.println("recv : " + s.getRemoteSocketAddress());
		while (run)
		{
			try
			{
				// 处理发送消息
				for (IntervalInfo ii : intervalInfoList)
				{
					if ((System.currentTimeMillis() - ii.getLastSendTimeMillis()) > ii.getIntervalMillis())
					{
						System.out.println("get " + ii.getProtocolName() + " data.");
						byte b[] = xh.getDataByProtocol(ii.getProtocolName(), null);
						System.out.println("==========================================");
						System.out.println("send : " + ByteUtils.byteArrayToString(b));
						System.out.println("send : " + ByteUtils.byteArraytoHexString(b));
						OutputStream os = s.getOutputStream();
						if (s.isConnected())
						{
							os.write(b);
							os.flush();
						}
						else
						{
							run = false;
						}
						ii.setLastSendTimeMillis(System.currentTimeMillis());
					}
				}
				// 接收消息
				InputStream in = s.getInputStream();
				if (in.available() > 0)
				{
					BufferedInputStream bis = new BufferedInputStream(s.getInputStream());
					DataInputStream dis = new DataInputStream(bis);
					int dataLen = dis.available();
					byte[] bytes = new byte[dataLen]; // 一次性读取
					dis.readFully(bytes);
					System.out.println("get : " + ByteUtils.byteArraytoHexString(bytes));
					System.out.println("get : " + ByteUtils.byteArrayToString(bytes));
					// TODO:here：获取对应的协议并生成协议数据发送
					ResInfo ri = getResponseProtocol(bytes);
					if (ri != null)
					{
						byte[] data = xh.getDataByProtocol(ri.getProtocol(), ri.getQuoteMap());
						OutputStream os = s.getOutputStream();
						if (s.isConnected())
						{
							os.write(data);
							os.flush();
						}
						else
						{
							run = false;
						}
					}

				}
				else
				{
					Thread.sleep(10);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				overThis();
			}
		}
	}

	private ResInfo getResponseProtocol(byte[] reqs)
	{
		// TODO:获得返回的协议名，查看是否有需要引用的数据。并且将需要引用的字段传给XMLHelper获得返回数据
		for (ResSetting rs : responseList)
		{
			// 切出报文
			byte[] req = getSingleReqest(reqs, rs.getHead());
			if (req == null) // 若本次获取的报文中不包含head，则认为本次获取的报文不是完整报文，整条舍弃
				continue;
			// 从请求中获取请求的类型
			byte[] reqCmdType = CollectionUtils.subArray(req, rs.getStartPos(), rs.getStopPos());
			// 将请求的类型进行匹配
			byte[] cmdType = rs.getCommandType();
			if (!CollectionUtils.isSameArray(reqCmdType, cmdType))
				continue;
			// 获取回复的协议名
			ResInfo ri = new ResInfo();
			ri.setProtocol(rs.getResProtocol());
			// 设置引用字段的值
			Map<String, int[]> quotePosMap = rs.getQuoteMap();
			Map<String, byte[]> quoteValueMap = new HashMap<String, byte[]>();
			for (String key : quotePosMap.keySet())
			{
				int[] quotePos = quotePosMap.get(key);
				byte[] quoteValue = CollectionUtils.subArray(req, quotePos[0], quotePos[1]);
				quoteValueMap.put(key, quoteValue);
			}
			ri.setQuoteMap(quoteValueMap);
			return ri;
		}
		return null;
	}

	/**
	 * 从reqs中根据标识head，截取包含head在内的往后的部分。 若不包含则返回null 从未知条数的数据中根据头信息获取一条报文。
	 * 
	 * @param reqs
	 * @param head
	 * @return
	 * @author zhaokai
	 * @version 2017年5月11日 下午4:37:50
	 */
	private byte[] getSingleReqest(byte[] reqs, byte[] head)
	{
		boolean flag = false;
		byte[] req = null;
		for (int i = 0; i < reqs.length; i++)
		{
			if (i + head.length > reqs.length)
				break;
			for (int j = 0; j < head.length; j++)
			{
				if (head[j] == reqs[i + j])
				{
					flag = true;
				}
				else
				{
					flag = false;
					break;
				}
			}
			if (flag)
			{
				req = new byte[reqs.length - i];
				for (int j = 0; j < req.length; j++)
					req[j] = reqs[i + j];
				break;
			}
		}
		return req;
	}

	/**
	 * 关闭连接
	 */
	private void overThis()
	{
		if (run)
			run = false;
		if (s != null)
		{
			try
			{
				s.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		System.out.println("关闭：" + s.getRemoteSocketAddress());
	}

	public static void main2(String[] args)
	{
		DataFactory sdf = new DataFactory();
		sdf.init("config/config.xml");
		sdf.startSocket();
		// sdf.startSocketServer();
		// String hexString = "ffffffff";
		// System.out.println(ByteUtils.byteArraytoHexString(ByteUtils.hexStringToBytes(hexString)));
	}

	public SocketInfo getSi()
	{
		return si;
	}

	public void setSi(SocketInfo si)
	{
		this.si = si;
	}

	public String getConfigPath()
	{
		return configPath;
	}

	public void setConfigPath(String configPath)
	{
		this.configPath = configPath;
	}

	public void setSocket(Socket s)
	{
		this.s = s;
	}
}
