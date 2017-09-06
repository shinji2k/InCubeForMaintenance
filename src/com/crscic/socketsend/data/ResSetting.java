package com.crscic.socketsend.data;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.crscic.socketsend.utils.ByteUtils;

public class ResSetting
{
	/**
	 * 截取消息首位置
	 */
	private int startPos;

	/**
	 * 截取消息尾位置
	 */
	private int stopPos;

	/**
	 * 指令类型
	 */
	private byte[] commandType;

	/**
	 * 返回的协议，需要与发送协议配置文件中的协议节点对应
	 */
	private String resProtocol;

	/**
	 * 引用请求中字段的列表，字段名称需要与发送协议配置文件中的字段属性对应
	 */
	private Map<String, int[]> quoteMap;

	/**
	 * 存储报文头信息，用来在解析请求时分辨单条报文
	 */
	private byte[] head;

	// TODO: 目前想是定义规则文件来应对请求中一个id是要求返回所有采集点ID的情况创立的规则文件。具体实现尚未想好
	private File ruleFile;

	/**
	 * 设置起始和截止位置
	 *
	 * @param pos
	 * @author zhaokai
	 * @version 2017年5月3日 上午9:39:20
	 */
	public void setPos(String pos[])
	{
		this.startPos = Integer.parseInt(pos[0]);
		this.stopPos = Integer.parseInt(pos[1]);
		if (this.startPos > 0)
			this.startPos--;
		if (this.stopPos > 0)
			this.stopPos--;
	}

	public int getStartPos()
	{
		return startPos;
	}

	public void setStartPos(int startPos)
	{
		this.startPos = startPos;
	}

	public int getStopPos()
	{
		return stopPos;
	}

	public void setStopPos(int stopPos)
	{
		this.stopPos = stopPos;
	}

	public byte[] getCommandType()
	{
		return commandType;
	}

	public void setCommandType(String commandTypeString)
	{
		this.commandType = ByteUtils.hexStringToBytes(commandTypeString);
	}

	public void setCommandType(byte[] commandType)
	{
		this.commandType = commandType;
	}

	public String getResProtocol()
	{
		return resProtocol;
	}

	public void setResProtocol(String resProtocol)
	{
		this.resProtocol = resProtocol;
	}

	public Map<String, int[]> getQuoteMap()
	{
		return quoteMap;
	}

	public void setQuoteMap(Map<String, String[]> quoteMap)
	{
		Map<String, int[]> tmp = new HashMap<String, int[]>();
		for (String key : quoteMap.keySet())
		{
			String posString[] = quoteMap.get(key);
			int posInt[] = new int[2];
			posInt[0] = Integer.parseInt(posString[0]);
			posInt[1] = Integer.parseInt(posString[1]);
			for (int i = 0; i < 2; i++)
			{
				if (posInt[i] > 0)
					posInt[i] -= 1;
			}
			tmp.put(key, posInt);
		}
		this.quoteMap = tmp;
	}

	public File getRuleFile()
	{
		return ruleFile;
	}

	public void setRuleFile(File ruleFile)
	{
		this.ruleFile = ruleFile;
	}

	public byte[] getHead()
	{
		return head;
	}

	public void setHead(byte[] head)
	{
		this.head = head;
	}

	public void setHead(String headString)
	{
		this.head = ByteUtils.hexStringToBytes(headString);
	}

}
