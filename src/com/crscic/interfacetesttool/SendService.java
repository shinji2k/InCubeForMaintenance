package com.crscic.interfacetesttool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.DocumentException;

import com.crscic.interfacetesttool.config.ConfigHandler;
import com.crscic.interfacetesttool.config.ProtocolSetting;
import com.crscic.interfacetesttool.config.ReplyConfig;
import com.crscic.interfacetesttool.config.SendConfig;
import com.crscic.interfacetesttool.connector.Connector;
import com.crscic.interfacetesttool.data.Data;
import com.crscic.interfacetesttool.exception.ConnectException;
import com.crscic.interfacetesttool.exception.GenerateDataException;
import com.crscic.interfacetesttool.exception.ParseXMLException;
import com.k.util.ByteUtils;
import com.k.util.CollectionUtils;

/**
 * 
 * @author zhaokai
 * 2017年9月13日 下午4:57:09
 */
public class SendService
{
	public SendService()
	{
		
	}
	
	public void startService (Connector connector, ProtocolSetting setting, ConfigHandler dataConfig) throws ParseXMLException, GenerateDataException, DocumentException, ConnectException
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
					byte[] data = sendData.getSendData(dataConfig.getProtocolConfig(sendCfg.getProtocol()), new HashMap<String, byte[]>());
					connector.send(data);
					lastSendTime = System.currentTimeMillis();
				}
				
				//添加接收部分
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
			byte[] recvData = connector.receive();//TODO:怎么判断拿到的是完整的数据呢？
			CollectionUtils.copyArrayToList(recvList, recvData);
			for (ReplyConfig replyCfg : setting.getReplyConfig())
			{
				
				// 查看是否完整
				byte[] singleRequest = getRequest(recvList, replyCfg.getHead());
				if (singleRequest == null)
					continue;
				//TODO:是不是需要的请求
				if (!needReply(recvList, replyCfg.getField()))
					continue;
				// 从请求中获取请求的类型
				byte[] reqCmdType = CollectionUtils.subArray(singleRequest, Integer.parseInt(replyCfg.getField().split(",")[0]), Integer.parseInt(replyCfg.getField().split(",")[1]));
				// 将请求的类型进行匹配
				byte[] cmdType = ByteUtils.hexStringToBytes(replyCfg.getValue());
				if (!CollectionUtils.isSameArray(reqCmdType, cmdType))
					continue;
				
				replyCfg.getNodeClass();
				replyCfg.getProtocol();
				replyCfg.getQuoteField();
				replyCfg.getQuoteFieldName();//list
				replyCfg.getQuoteFieldName(getClass());
				replyCfg.getValue();
				//TODO:获取返回消息
				byte[] data = sendData.getSendData(dataConfig.getProtocolConfig(replyCfg.getProtocol()), new HashMap<String, byte[]>());
				connector.send(data);
				
				recvList.clear();
			}
		}
	}
	
	private boolean needReply(List<Byte> request, String upperAndLowerString)
	{

	}
	
	/**
	 * 从收到的请求中根据请求头切出完整的一条请求，请求中至少包含两个请求头才会截取
	 * @param request 接收到的请求List
	 * @param headString 请求头的16进制串，字符串形式
	 * @return
	 * @author zhaokai
	 * 2017年9月14日 下午8:02:00
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
