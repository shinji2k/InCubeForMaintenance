package com.crscic.interfacetesttool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentException;

import com.crscic.interfacetesttool.config.ConfigHandler;
import com.crscic.interfacetesttool.config.ProtocolSetting;
import com.crscic.interfacetesttool.config.ReplyConfig;
import com.crscic.interfacetesttool.config.SendConfig;
import com.crscic.interfacetesttool.connector.Connector;
import com.crscic.interfacetesttool.data.Data;
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
public class SendService
{
	public SendService()
	{
		super();
	}

	/**
	 * 单独启动发送服务，根据配置向对端发送报文
	 * 
	 * @author ken_8 2017年9月18日 下午11:50:38
	 */
	public void startSendService(Connector connector, ProtocolSetting proSetting, ConfigHandler dataConfig)
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
		for (int i = 0; i < proSetting.getSendConfig().size(); i++)
			interval.put(proSetting.getSendConfig().get(i).getProtocol(), System.currentTimeMillis());
		Data sendData = new Data();
		while (true)
		{
			try
			{
				// 生成协议数据
				for (SendConfig sendCfg : proSetting.getSendConfig())
				{
					Long currInterval = System.currentTimeMillis() - interval.get(sendCfg.getProtocol());
					if (currInterval >= Long.parseLong(sendCfg.getInterval()))
					{
						byte[] data = sendData.getSendData(dataConfig.getProtocolConfig(sendCfg.getProtocol()),
								new HashMap<String, byte[]>());
						Log.info("发送协议" + sendCfg.getProtocol() + "：" + ByteUtils.byteArraytoHexString(data));
						connector.send(data);
						interval.put(sendCfg.getProtocol(), System.currentTimeMillis());
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

	/**
	 * 单独启动回复服务，自动回复配置的报文
	 * 
	 * @author ken_8 2017年9月18日 下午11:51:13
	 */
	public void startReplyService(Connector connector, ProtocolSetting proSetting, ConfigHandler dataConfig)
	{//TODO：需要修改接收部分，改为循环接收
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
		while (true)
		{
			try
			{
				// 没有进行自动回复配置，无需进行自动回复
				if (proSetting.getReplyConfig().size() == 0)
					continue;

				// 假设Socket每次发送的都是完整的报文。
				byte[] recvData = connector.receive();
				// 连接异常中断时会出现接收数据为空的情况
				if (recvData == null)
				{
					connector.closeConnect();
					connector.openConnect();
					continue;
				}
				/**
				 * 目前的判断逻辑是采用两个请求头中间的部分视作一条完整的数据。 但这种方式的弊端是每两条请求就会丢弃一条请求，弃包率太高。
				 * 但增加包尾的判断的话又得增加配置内容，提高了配置的复杂度。 因此暂时不判断接收是否完整
				 */
				Log.debug("接收：" + ByteUtils.byteArraytoHexString(recvData));
				// CollectionUtils.copyArrayToList(recvList, recvData);
				for (ReplyConfig replyCfg : proSetting.getReplyConfig())
				{
					/**
					 * // 查看是否完整 byte[] singleRequest = getRequest(recvList,
					 * replyCfg.getHead()); if (singleRequest == null) // 不完整
					 * continue; Log.info("收到请求：" +
					 * ByteUtils.byteArraytoHexString(singleRequest));
					 */
					// 是不是需要的请求
					if (!needReply(recvData, replyCfg.getCmdTypePos(), replyCfg.getValue(), replyCfg.getNodeClass()))
					{
						Log.info("不是需要回复的请求");
						continue;
					}
					// 设置引用字段
					Map<String, byte[]> quoteMap = new HashMap<String, byte[]>();
					byte[] quoteBytes = getQuoteByteArray(recvData, replyCfg.getQuotePos());
					Log.debug("引用内容：" + ByteUtils.byteArraytoHexString(quoteBytes));
					List<String> quotePartNameList = replyCfg.getQuoteFieldName();
					for (String quotePartName : quotePartNameList)
					{
						Log.debug("    字段：" + quotePartName + " | 值：" + ByteUtils.byteArraytoHexString(quoteBytes));
						quoteMap.put(quotePartName, quoteBytes);
					}
					// 获取返回消息
					byte[] data = sendData.getSendData(dataConfig.getProtocolConfig(replyCfg.getProtocol()), quoteMap);
					Log.info("回复协议" + replyCfg.getProtocol() + "：" + ByteUtils.byteArraytoHexString(data));
					connector.send(data);

					// recvList.clear();
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

	/**
	 * 启动发送和回复服务，此方法会创建线程，不能再LoadRunner中使用
	 * @param connector
	 * @param proSetting
	 * @param dataConfig
	 * @throws ParseXMLException
	 * @throws GenerateDataException
	 * @throws DocumentException
	 * @throws ConnectException
	 * @author ken_8
	 * 2017年9月19日 上午12:07:27
	 */
	public void startService(Connector connector, ProtocolSetting proSetting, ConfigHandler dataConfig)
			throws ParseXMLException, GenerateDataException, DocumentException, ConnectException
	{//TODO：改为多线程
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
		// 获取协议配置
		Map<String, Long> interval = new HashMap<String, Long>();
		for (int i = 0; i < proSetting.getSendConfig().size(); i++)
			interval.put(proSetting.getSendConfig().get(i).getProtocol(), System.currentTimeMillis());
		Data sendData = new Data();
		// List<Byte> recvList = new ArrayList<Byte>();
		while (true)
		{
			try
			{
				Thread.sleep(300);
			}
			catch (InterruptedException e)
			{
				Log.error("Sleep失败");
				e.printStackTrace();
			}

			// 生成协议数据
			for (SendConfig sendCfg : proSetting.getSendConfig())
			{
				Long currInterval = System.currentTimeMillis() - interval.get(sendCfg.getProtocol());
				if (currInterval >= Long.parseLong(sendCfg.getInterval()))
				{
					byte[] data = sendData.getSendData(dataConfig.getProtocolConfig(sendCfg.getProtocol()),
							new HashMap<String, byte[]>());
					Log.info("发送协议" + sendCfg.getProtocol() + "：" + ByteUtils.byteArraytoHexString(data));
					connector.send(data);
					interval.put(sendCfg.getProtocol(), System.currentTimeMillis());
				}
			}

			// 没有进行自动回复配置，无需进行自动回复
			if (proSetting.getReplyConfig().size() == 0)
				continue;

			// 假设Socket每次发送的都是完整的报文。
			byte[] recvData = connector.receive();
			// 连接异常中断时会出现接收数据为空的情况
			if (recvData == null)
			{
				connector.closeConnect();
				connector.openConnect();
				continue;
			}
			/**
			 * 目前的判断逻辑是采用两个请求头中间的部分视作一条完整的数据。 但这种方式的弊端是每两条请求就会丢弃一条请求，弃包率太高。
			 * 但增加包尾的判断的话又得增加配置内容，提高了配置的复杂度。 因此暂时不判断接收是否完整
			 */
			Log.debug("接收：" + ByteUtils.byteArraytoHexString(recvData));
			// CollectionUtils.copyArrayToList(recvList, recvData);
			for (ReplyConfig replyCfg : proSetting.getReplyConfig())
			{
				/**
				 * // 查看是否完整 byte[] singleRequest = getRequest(recvList,
				 * replyCfg.getHead()); if (singleRequest == null) // 不完整
				 * continue; Log.info("收到请求：" +
				 * ByteUtils.byteArraytoHexString(singleRequest));
				 */
				// 是不是需要的请求
				if (!needReply(recvData, replyCfg.getCmdTypePos(), replyCfg.getValue(), replyCfg.getNodeClass()))
				{
					Log.info("不是需要回复的请求");
					continue;
				}
				// 设置引用字段
				Map<String, byte[]> quoteMap = new HashMap<String, byte[]>();
				byte[] quoteBytes = getQuoteByteArray(recvData, replyCfg.getQuotePos());
				Log.debug("引用内容：" + ByteUtils.byteArraytoHexString(quoteBytes));
				List<String> quotePartNameList = replyCfg.getQuoteFieldName();
				for (String quotePartName : quotePartNameList)
				{
					Log.debug("    字段：" + quotePartName + " | 值：" + ByteUtils.byteArraytoHexString(quoteBytes));
					quoteMap.put(quotePartName, quoteBytes);
				}
				// 获取返回消息
				byte[] data = sendData.getSendData(dataConfig.getProtocolConfig(replyCfg.getProtocol()), quoteMap);
				Log.info("回复协议" + replyCfg.getProtocol() + "：" + ByteUtils.byteArraytoHexString(data));
				connector.send(data);

				// recvList.clear();
			}

		} // TODO:长短连接判断部分
	}

	private byte[] getQuoteByteArray(byte[] request, Position quotePos)
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
	private boolean needReply(byte[] request, Position cmdTypePos, String targetCmdTypeString, String nodeClassString)
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
	 * 从收到的请求中根据请求头切出完整的一条请求，请求中至少包含两个请求头才会截取
	 * 
	 * @param request
	 *            接收到的请求List
	 * @param headString
	 *            请求头的16进制串，字符串形式
	 * @return
	 * @author zhaokai 2017年9月14日 下午8:02:00
	 */
	private byte[] getRequest(List<Byte> request, String headString)
	{
		byte[] head = ByteUtils.hexStringToBytes(headString);

		boolean flag = false;
		byte[] req = null;
		for (int i = 0; i < request.size(); i++)
		{
			if (i + request.size() > request.size())
				break;
			for (int j = 0; j < head.length; j++)
			{
				if (head[j] == request.get(i + j))
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
				req = new byte[request.size() - i];
				for (int j = 0; j < req.length; j++)
					req[j] = request.get(i + j);
				break;
			}
		}
		return req;
	}

}
