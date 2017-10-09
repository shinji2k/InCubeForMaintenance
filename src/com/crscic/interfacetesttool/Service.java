package com.crscic.interfacetesttool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentException;

import com.crscic.interfacetesttool.config.ReplySetting;
import com.crscic.interfacetesttool.config.Response;
import com.crscic.interfacetesttool.config.SendSetting;
import com.crscic.interfacetesttool.connector.Connector;
import com.crscic.interfacetesttool.data.Data;
import com.crscic.interfacetesttool.data.ProtocolConfig;
import com.crscic.interfacetesttool.entity.Position;
import com.crscic.interfacetesttool.exception.ConnectException;
import com.crscic.interfacetesttool.exception.GenerateDataException;
import com.crscic.interfacetesttool.exception.ParseXMLException;
import com.crscic.interfacetesttool.log.Log;
import com.k.util.ByteUtils;
import com.k.util.CollectionUtils;

/**
 * 
 * @author zhaokai 2017年9月13日 下午4:57:09
 */
public class Service
{
	public void startSendService(Connector connector, SendSetting sendSetting, List<ProtocolConfig> proCfgList)
	{
		new SendService().startSendService(connector, sendSetting, proCfgList);
	}
	
	public void startReplyService(Connector connector, ReplySetting replySetting, List<ProtocolConfig> proCfgList)
	{
		new ReplyService().startReplyService(connector, replySetting, proCfgList);
	}
	
	/**
	 * 启动发送和回复服务，此方法会创建线程，不能再LoadRunner中使用
	 * 
	 * @param connector
	 * @param proSetting
	 * @param dataConfig
	 * @throws ParseXMLException
	 * @throws GenerateDataException
	 * @throws DocumentException
	 * @throws ConnectException
	 * @author ken_8 2017年9月19日 上午12:07:27
	 */
	public void startService(Connector connector, SendSetting sendSetting, List<ProtocolConfig> sendProCfgList, ReplySetting replySetting, List<ProtocolConfig> replyProCfgList)
	{
		Thread sendThread = new Thread(new SendService(connector, sendSetting, sendProCfgList));
		sendThread.start();
		Thread replyThread = new Thread(new ReplyService(connector, replySetting, replyProCfgList));
		replyThread.start();
	}
	
	private byte[] getQuoteByteArray(List<Byte> request, Position quotePos)
	{
		return CollectionUtils.subArray(request, quotePos.getStartPos(), quotePos.getStopPos());
	}

	/**
	 * 判断请求中指定区间的byte是否与给定的byte[]相同
	 * 
	 * @param request
	 *            请求byte[]
	 * @param cmdTypePos
	 *            截取区间
	 * @param targetCmdTypeString
	 *            目标byte[]
	 * @return
	 * @author ken_8 2017年9月15日 上午12:04:18
	 */
	private boolean needReply(List<Byte> request, Position cmdTypePos, String targetCmdTypeString,
			String nodeClassString)
	{
		boolean res = false;
		byte[] targetCmdType = nodeClassString.equals("byte") ? ByteUtils.hexStringToBytes(targetCmdTypeString)
				: targetCmdTypeString.getBytes();
		byte[] reqCmdType = CollectionUtils.subArray(request, cmdTypePos.getStartPos(), cmdTypePos.getStopPos());
		if (CollectionUtils.isSameArray(reqCmdType, targetCmdType))
			res = true;
		return res;
	}

	/**
	 * 从收到的请求中根据请求的头尾信息切出完整的请求，并将该信息从原集合中删除。 该信息之前的信息也一并删除，之后的信息保留
	 * 
	 * @param request
	 *            接收到的请求List
	 * @param headString
	 *            请求头的16进制串，字符串形式
	 * @param tailString
	 *            请求尾的16进制串，字符串形式
	 * @return
	 * @author zhaokai 2017年9月14日 下午8:02:00
	 */
	private List<List<Byte>> getFullRequest(List<Byte> request, String headString, String tailString)
	{
		byte[] head = ByteUtils.hexStringToBytes(headString);
		byte[] tail = ByteUtils.hexStringToBytes(tailString);
		List<List<Byte>> reqs = new ArrayList<List<Byte>>();
		int headPos;
		while ((headPos = CollectionUtils.indexOf(request, head)) != -1) // 发现头之后继续
		{
			if (headPos > 0)
			{
				CollectionUtils.removeBefore(request, headPos - 1);
				headPos = 0;
			}

			int tailPos = CollectionUtils.indexOf(request, tail);
			if (tailPos == -1) // 没找到尾
				return reqs;

			int subStopPos = tailPos + tail.length - 1;
			List<Byte> singleRequest = CollectionUtils.subList(request, headPos, subStopPos);

			// 删除旧数据
			if (subStopPos + 1 == request.size())
				request.clear();
			else
				CollectionUtils.removeBefore(request, subStopPos);

			reqs.add(singleRequest);
		}
		return reqs;
	}
	
	/**
	 * @param proCfgList
	 * @param protocol
	 * @return
	 * @author zhaokai
	 * @date 2017年9月28日 下午10:17:30
	 */
	public static ProtocolConfig getProtocolConfigByProtocolName(List<ProtocolConfig> proCfgList, String protocol)
	{
		for (ProtocolConfig proCfg : proCfgList)
		{
			if (proCfg.getProtocolName().equals(protocol))
				return proCfg;
		}
		return null;
	}

	public Service()
	{
		super();
	}
	
	
	class SendService implements Runnable
	{
		private Connector connector;
		private SendSetting sendSetting;
		private List<ProtocolConfig> proCfgList;
		
		public SendService()
		{
		}

		public SendService(Connector connector, SendSetting sendSetting, List<ProtocolConfig> proCfgList)
		{
			this.connector = connector;
			this.sendSetting = sendSetting;
			this.proCfgList = proCfgList;
		}

		/**
		 * 单独启动发送服务，根据配置向对端发送报文
		 * 
		 * @author ken_8 2017年9月18日 下午11:50:38
		 */
		public void startSendService(Connector connector, SendSetting sendSetting, List<ProtocolConfig> proCfgList)
		{
			try
			{
				if (!connector.isOpen())
					connector.openConnect();
			}
			catch (ConnectException e)
			{
				Log.error("打开连接失败", e);
				return;
			}
			// 初始化发送间隔
			Map<String, Long> interval = new HashMap<String, Long>();
			Map<String, Long> protocolMap = sendSetting.getProtocolMap();
			int proCnt = sendSetting.getProtocolMap().size();
			if (proCnt == 0)
			{
				Log.warn("没有配置发送内容，请检查配置文件");
				return;
			}
			for (String protocol : protocolMap.keySet())
				interval.put(protocol, System.currentTimeMillis());
			Data sendData = new Data();
			while (true)
			{
				try
				{
					// 生成协议数据
					for (String protocol : protocolMap.keySet())
					{
						Long currInterval = System.currentTimeMillis() - interval.get(protocol);
						if (currInterval >= protocolMap.get(protocol))
						{
							byte[] data = sendData.getSendData(getProtocolConfigByProtocolName(proCfgList, protocol),
									new HashMap<String, byte[]>());
							Log.info("发送协议" + protocol + "：" + ByteUtils.byteToHexString(data));
							connector.send(data);
							interval.put(protocol, System.currentTimeMillis());
						}
					}
					Thread.sleep(300);
				}
				catch (InterruptedException e)
				{
					Log.error("Sleep失败");
					e.printStackTrace();
				}
				catch (GenerateDataException e)
				{
					Log.error("报文生成失败", e);
				}
				catch (ConnectException e)
				{
					Log.error("报文发送失败", e);
				}
			}

		}

		@Override
		public void run()
		{
			startSendService(connector, sendSetting, proCfgList);
		}

	}

	class ReplyService implements Runnable
	{
		private Connector connector;
		private ReplySetting replySetting;
		private List<ProtocolConfig> proCfgList;
		
		public ReplyService()
		{
		}
		
		public ReplyService(Connector connector, ReplySetting replySetting, List<ProtocolConfig> proCfgList)
		{
			this.connector = connector;
			this.replySetting = replySetting;
			this.proCfgList = proCfgList;
		}

		/**
		 * 单独启动回复服务，自动回复配置的报文
		 * 
		 * @author ken_8 2017年9月18日 下午11:51:13
		 */
		public void startReplyService(Connector connector, ReplySetting replySetting, List<ProtocolConfig> proCfgList)
		{
			try
			{
				if (!connector.isOpen())
					connector.openConnect();
			}
			catch (ConnectException e)
			{
				Log.error("打开连接失败", e);
				return;
			}

			Data sendData = new Data();

			List<Byte> recvList = new ArrayList<Byte>();
			try
			{
				while (true)
				{
					// 没有进行自动回复配置，无需进行自动回复
					if (replySetting.getResponseList().size() == 0)
					{
						Log.error("没有检测到回复配置");
						break;
					}

					// 将收到的内容添加到缓冲区
					recvList.addAll(connector.receive());
					// 连接异常中断时会出现接收数据为空的情况
					if (recvList.size() == 0)
					{
						if (!connector.isOpen())
						{
							connector.closeConnect();
							connector.openConnect();
						}
						continue;
					}

					for (Response response : replySetting.getResponseList())
					{

						// 查看是否完整的请求，如果是完整的，则取出完整请求并从缓冲区中移除该请求
						List<List<Byte>> requestList = getFullRequest(recvList, response.getHead(), response.getTail());
						if (requestList.size() == 0) // 无完整请求
							continue;
						// 返回的是多条完整的请求，循环处理每个请求
						for (List<Byte> singleRequest : requestList)
						{
							Log.info("收到请求：" + ByteUtils.byteToHexString(singleRequest));
							// 是不是需要回复的请求
							if (!needReply(singleRequest, response.getCmdTypePos(), response.getValue(),
									response.getNodeClass()))
							{
								Log.info("不是需要回复的请求");
								continue;
							}
							// 设置引用字段
							Map<String, byte[]> quoteMap = new HashMap<String, byte[]>();
							Map<String, Position> quoteInfo = response.getQuoteInfo();
							for (String quotePartName : quoteInfo.keySet())
							{
								byte[] quoteBytes = getQuoteByteArray(singleRequest, quoteInfo.get(quotePartName));
								Log.debug("引用内容：" + ByteUtils.byteToHexString(quoteBytes));
								Log.debug("    字段：" + quotePartName + " | 值：" + ByteUtils.byteToHexString(quoteBytes));
								quoteMap.put(quotePartName, quoteBytes);
							}

							// 获取返回消息
							byte[] data = sendData.getSendData(
									getProtocolConfigByProtocolName(proCfgList, response.getProtocol()), quoteMap);
							Log.info("回复协议" + response.getProtocol() + "：" + ByteUtils.byteToHexString(data));
							connector.send(data);
						}
					}

					Thread.sleep(300);
				}
			}
			catch (InterruptedException e)
			{
				Log.error("Sleep失败");
			}
			catch (GenerateDataException e)
			{
				Log.error("报文生成失败", e);
			}
			catch (ConnectException e)
			{
				Log.error("报文发送失败", e);
			}
			finally
			{
				try
				{
					connector.closeConnect();
				}
				catch (ConnectException e)
				{
					e.printStackTrace();
				}
			}
		}

		@Override
		public void run()
		{
			startReplyService(connector, replySetting, proCfgList);
		}
	}
}
