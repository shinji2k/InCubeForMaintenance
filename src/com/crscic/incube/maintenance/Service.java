package com.crscic.incube.maintenance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.crscic.incube.config.ParseSetting;
import com.crscic.incube.config.Request;
import com.crscic.incube.config.Response;
import com.crscic.incube.connector.Connector;
import com.crscic.incube.data.Data;
import com.crscic.incube.data.ProtocolConfig;
import com.crscic.incube.entity.Position;
import com.crscic.incube.exception.ConnectException;
import com.crscic.incube.exception.GenerateDataException;
import com.crscic.incube.log.Log;
import com.k.util.ByteUtils;
import com.k.util.CollectionUtils;
import com.k.util.StringUtils;

/**
 * 
 * @author zhaokai 2017年9月13日 下午4:57:09
 */
public class Service
{
	public static boolean running = true;

	public void startParseService(Connector connector, ParseSetting parseSetting,
			List<ProtocolConfig> parseProCfgList, int loopCnt)
	{
		if (!connector.isOpen())
		{
			try
			{
				connector.openConnect();
			}
			catch (ConnectException e)
			{
				Log.error("打开连接失败", e);
			}
		}
		Thread parseThread = new Thread(new ParseService(connector, parseSetting, parseProCfgList, loopCnt));
		parseThread.start();
	}

	private byte[] getQuoteByteArray(byte[] src, Position quotePos)
	{
		return CollectionUtils.subArray(src, quotePos.getStartPos(), quotePos.getStopPos());
	}

	/**
	 * 判断请求中指定区间的byte是否与给定的byte[]相同
	 * 
	 * @param src
	 *            请求byte[]
	 * @param pairPos
	 *            截取区间
	 * @param targetCmdTypeString
	 *            目标byte[]
	 * @return
	 * @author ken_8 2017年9月15日 上午12:04:18
	 */
	private boolean isPaired(List<Byte> src, Position pairPos, byte[] keyWord)
	{
		boolean res = false;
		byte[] part = CollectionUtils.subArray(src, pairPos.getStartPos(), pairPos.getStopPos());
		if (CollectionUtils.isSameArray(part, keyWord))
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
	 * @author zhaokai
	 * @create 2017年9月14日 下午8:02:00
	 */
	private List<List<Byte>> getFullMessage(List<Byte> request, String headString, String tailString)
	{
		boolean nullHead = false;
		boolean nullTail = false;
		byte[] head;
		byte[] tail;
		// 认为不存在头、尾全为空的情况
		// 若没有头，则用尾当头用来截取
		if (StringUtils.isNullOrEmpty(headString))
		{
			head = ByteUtils.hexStringToBytes(tailString);
			nullHead = true;
		}
		else
		{
			head = ByteUtils.hexStringToBytes(headString);
		}

		// 若没有尾，则用头当尾用来截取
		if (StringUtils.isNullOrEmpty(tailString))
		{
			tail = ByteUtils.hexStringToBytes(headString);
			nullTail = true;
		}
		else
		{
			tail = ByteUtils.hexStringToBytes(tailString);
		}

		List<List<Byte>> reqs = new ArrayList<List<Byte>>();
		int headPos;
		while ((headPos = CollectionUtils.indexOf(request, head)) != -1) // 发现头之后继续
		{
			if (headPos > 0)
			{
				CollectionUtils.removeBefore(request, headPos - 1);
				headPos = 0;
			}

			int tailPos = CollectionUtils.indexOf(request, tail, head.length);
			if (tailPos == -1) // 没找到尾
				return reqs;

			int subStopPos = tailPos + tail.length - 1;
			// 若为没头或没尾的情况，截取时需要对截取位置做调整，让出缺失的头或尾
			if (nullHead)
				headPos += head.length;
			if (nullTail)
				subStopPos -= tail.length;
			List<Byte> singleRequest = CollectionUtils.subList(request, headPos, subStopPos);

			// 删除旧数据
			if (subStopPos + 1 == request.size())
			{
				request.clear();
			}
			else
			{
				// 若头为空，就需要留下上一条的尾来做下一条的头
				if (nullHead)
					subStopPos -= tail.length;
				CollectionUtils.removeBefore(request, subStopPos);
			}

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

	/**
	 * 根据配置中的quote节点内容获取数据源中对应字段的内容作为配对依据。
	 * quote节点中配置多个子节点时按照配置顺序将字段内容拼接，第一个节点为数组第一个元素
	 * 
	 * @param req
	 * @param quoteInfo
	 * @return
	 * @author zhaokai
	 * @date 2017年10月15日 下午3:34:14
	 */
	private byte[] getPairKey(byte[] req, Map<String, Position> quoteInfo)
	{
		List<Byte> keyByteList = new ArrayList<Byte>();
		for (String key : quoteInfo.keySet())
		{
			byte[] keyPart = this.getQuoteByteArray(req, quoteInfo.get(key));
			keyByteList = CollectionUtils.copyArrayToList(keyByteList, keyPart);
		}
		return CollectionUtils.toByteArray(keyByteList);
	}

	class ParseService implements Runnable
	{
		private Connector connector;
		private ParseSetting parseSetting;
		private List<ProtocolConfig> proCfgList;
		private int loopCnt;

		public ParseService()
		{
		}

		public ParseService(Connector connector, ParseSetting parseSetting, List<ProtocolConfig> proCfgList,
				int loopCnt)
		{
			this.connector = connector;
			this.parseSetting = parseSetting;
			this.proCfgList = proCfgList;
			this.loopCnt = loopCnt;
		}

		public void startParseService(Connector connector, ParseSetting parseSetting, List<ProtocolConfig> proCfgList,
				int loopCnt)
		{
			try
			{
				synchronized (connector)
				{
					if (!connector.isOpen())
						connector.openConnect();
				}
			}
			catch (ConnectException e)
			{
				Log.error("打开连接失败", e);
				return;
			}

			// 超时and重试，若没有设置则认为永不超时和无限重试
			boolean noTimeout = false;
			if (StringUtils.isNullOrEmpty(parseSetting.getTimeout()))
			{
				noTimeout = true;
				Log.debug("永不超时");
			}
			long timeout = 0;
			if (!noTimeout)
			{
				timeout = Long.parseLong(parseSetting.getTimeout());
				Log.debug("超时时间：" + timeout + "毫秒");
			}

			boolean noRetry = false;
			if (StringUtils.isNullOrEmpty(parseSetting.getRetry()))
			{
				noRetry = true;
				Log.debug("无限重试");
			}
			int retry = 0;
			if (!noRetry)
			{
				retry = Integer.parseInt(parseSetting.getRetry());
				Log.debug("重试次数：" + retry);
			}

			try
			{
				byte[] b;
				// 按设备数量循环
				Data sendData = new Data();
				for (int i = 0; i < loopCnt; i++)
				{
					int loop = 0;
					List<Request> requestList = CollectionUtils.copyList(parseSetting.getRequest(), null);
					Map<String, byte[]> failedMem = new HashMap<String, byte[]>();
					while (loop < retry || noRetry)
					{
						if (requestList.size() == 0)
							break;
						nextRequest: for (int j = 0; j < requestList.size(); j++)
						{
							Request request = requestList.get(j);
							if (failedMem.containsKey(request.getSendProtocol()))
							{
								b = failedMem.get(request.getSendProtocol());
							}
							else
							{
								// 生成请求数据
								ProtocolConfig proConfig = getProtocolConfigByProtocolName(proCfgList,
										request.getSendProtocol());
								b = sendData.getSendData(proConfig, new HashMap<String, byte[]>(), null);
							}
							// 读取response节点配置
							Response response = request.getResponse();
							// 设置解析响应是否配对用的关键字，看看是从value中取值还是quote
							Map<String, Position> quoteInfo = response.getQuoteInfo();
							byte[] keyByteArray = null;
							if (quoteInfo != null)
								keyByteArray = getPairKey(b, quoteInfo);
							else
								keyByteArray = Data.getByteArrayByClass(response.getValue(), response.getNodeClass());

							if (!noRetry)
								Log.debug("第" + (loop + 1) + "次发送...");
							connector.send(b);
							Log.info("发送：" + ByteUtils.byteToHexString(b));
							// 理论上发完请求会马上回复内容
							long beginTime = System.currentTimeMillis();
							// 将收到的内容添加到缓冲区
							List<Byte> recvList = new ArrayList<Byte>();
							while ((System.currentTimeMillis() - beginTime) < timeout || noTimeout)
							{
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

								// 查看是否完整的响应，如果是完整的，则取出响应并从缓冲区中移除该响应
								// 这个response不是指配置中的Response节点，而是实际的响应报文
								List<List<Byte>> responseMsgList = getFullMessage(recvList, response.getHead(),
										response.getTail());
								if (requestList.size() == 0) // 无完整请求
									continue;
								// 返回的是多条完整的请求，循环处理每个请求
								for (List<Byte> singleResponseMsg : responseMsgList)
								{
									Log.debug("收到请求：" + ByteUtils.byteToHexString(singleResponseMsg));
									// 是不是需要回复的请求
									if (!isPaired(singleResponseMsg, response.getCmdTypePos(), keyByteArray))
									{
										Log.debug("不是配对的回复");
										continue;
									}
									Log.info("收到请求：" + ByteUtils.byteToHexString(singleResponseMsg));
									// 配对成功后移除当前请求
									failedMem.remove(request.getSendProtocol());
									requestList.remove(request);
									continue nextRequest;
								}
								// 是否要去掉sleep？
								Thread.sleep(50);
							}
							failedMem.put(request.getSendProtocol(), b);
							Log.debug("本次接收配对超时");
						}
						loop++;
					}
				}
				Log.info("完成.");
			}
			catch (ConnectException e)
			{
				Log.error("连接错误", e);
			}
			catch (InterruptedException e)
			{
				Log.error("挂起失败", e);
			}
			catch (GenerateDataException e)
			{
				Log.error("生成数据失败", e);
			}
			finally
			{
				try
				{
					connector.closeConnect();
				}
				catch (ConnectException e)
				{
					Log.error("关闭连接失败", e);
				}
			}
		}

		@Override
		public void run()
		{
			startParseService(connector, parseSetting, proCfgList, loopCnt);
		}

	}
}
