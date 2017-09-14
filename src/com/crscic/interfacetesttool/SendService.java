package com.crscic.interfacetesttool;

import java.util.ArrayList;
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

	}

	public void startService(Connector connector, ProtocolSetting setting, ConfigHandler dataConfig)	//TODO:加相关日志输出
			throws ParseXMLException, GenerateDataException, DocumentException, ConnectException
	{
		// 获取协议配置
		connector.openConnect();
		Long lastSendTime = System.currentTimeMillis();
		Data sendData = new Data();
		List<Byte> recvList = new ArrayList<Byte>();
		while (true)
		{
			// 生成协议数据
			for (SendConfig sendCfg : setting.getSendConfig())
			{
				Long currInterval = System.currentTimeMillis() - lastSendTime;
				if (currInterval >= Long.parseLong(sendCfg.getInterval()))
				{
					byte[] data = sendData.getSendData(dataConfig.getProtocolConfig(sendCfg.getProtocol()),
							new HashMap<String, byte[]>());
					connector.send(data);
					lastSendTime = System.currentTimeMillis();
				}
			}
			
			byte[] recvData = connector.receive();// TODO:怎么判断拿到的是完整的数据呢？
			CollectionUtils.copyArrayToList(recvList, recvData);
			for (ReplyConfig replyCfg : setting.getReplyConfig())
			{

				// 查看是否完整
				byte[] singleRequest = getRequest(recvList, replyCfg.getHead());
				if (singleRequest == null) // 不完整
					continue;
				// 是不是需要的请求
				if (!needReply(singleRequest, replyCfg.getCmdTypePos(), replyCfg.getValue(), replyCfg.getNodeClass()))
					continue;
				// 设置引用字段
				Map<String, byte[]> quoteMap = new HashMap<String, byte[]>();
				byte[] quoteBytes = getQuoteByteArray(singleRequest, replyCfg.getQuotePos());
				List<String> quotePartNameList = replyCfg.getQuoteFieldName();
				for (String quotePartName : quotePartNameList)
					quoteMap.put(quotePartName, quoteBytes);
				// 获取返回消息
				byte[] data = sendData.getSendData(dataConfig.getProtocolConfig(replyCfg.getProtocol()), quoteMap);
				connector.send(data);

				recvList.clear();
			}
		}//TODO:长短连接判断部分
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
