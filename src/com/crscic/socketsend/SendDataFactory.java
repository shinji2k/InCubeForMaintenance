package com.crscic.socketsend;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.crscic.socketsend.data.IntervalInfo;
import com.crscic.socketsend.data.SocketInfo;
import com.crscic.socketsend.exception.AppException;
import com.crscic.socketsend.socket.SocketClient;
import com.crscic.socketsend.socket.SocketServer;
import com.crscic.socketsend.utils.ByteUtils;
import com.crscic.socketsend.utils.StringUtils;
import com.crscic.socketsend.xmlhelper.XmlHelper;

public class SendDataFactory implements Runnable
{

	private Socket s;
	private XmlHelper xh;
	private boolean run = true;
	private List<IntervalInfo> intervalInfoList;
	private String proConfigPath;
	private SocketInfo si;
	private String configPath;

	public SendDataFactory(Socket s, String configPath)
	{
		this.s = s;
		init(configPath);
	}

	public SendDataFactory()
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
		SocketServer ss = new SocketServer(Integer.parseInt(si.getPort()));
		ss.setConfigPath(configPath);
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
		
		SocketClient sc = new SocketClient(si.getIp(), Integer.parseInt(si.getPort()), this.configPath);
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
	 * 获取发送的数据
	 *
	 * @author zhaokai
	 * @version 2017年2月13日 上午11:10:13
	 */
	public byte[] getSendData(String protocolName)
	{
		byte[] b = null;
		try
		{
			b = xh.getDataByProtocol(protocolName);
			// System.out.println(ByteUtils.byteArraytoHexString(b));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return b;
	}

	public void run()
	{
		xh = new XmlHelper(proConfigPath);
		System.out.println("client : " + s.getRemoteSocketAddress());
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
						byte b[] = xh.getDataByProtocol(ii.getProtocolName());
						System.out.println("send : " + ByteUtils.byteArrayToString(b));
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
					System.out.println("get in hex : " + ByteUtils.byteArraytoHexString(bytes));
					System.out.println("get in asc : " + ByteUtils.byteArrayToString(bytes));
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

	/**
	 * 关闭连接
	 * 
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

	public static void main(String[] args)
	{
		SendDataFactory sdf = new SendDataFactory();
		sdf.init("config/config.xml");
		sdf.startSocketClient();
//		sdf.startSocketServer();
		// String hexString = "ffffffff";
		// System.out.println(ByteUtils.byteArraytoHexString(ByteUtils.hexStringToBytes(hexString)));
	}
}
