package com.crscic.interfacetesttool.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.crscic.interfacetesttool.entity.Position;

/**
 * @author zhaokai
 *
 *         2017年9月28日 下午10:56:14
 */
public class Response
{
	/**
	 * 截取请求中的字段位置，逗号分隔起止
	 */
	private Position cmdTypePos;
	/**
	 * 匹配的值
	 */
	private String value;
	private String head;
	private String tail;
	// "class"
	private String nodeClass;
	private String protocol;

	private Map<String, Position> quoteInfo;

	/**
	 * 需要引用自请求中的字段
	 */
	// private Position quotePos;
	/**
	 * 引用字段对应发送数据协议中的字段名称
	 */
	// private List<String> quoteFieldName;

	public void setQuoteInfo(String fieldStr, String posStr)
	{
		if (quoteInfo == null)
			quoteInfo = new HashMap<String, Position>();

		Position pos = new Position();
		pos.setPosition(posStr, ",");

		String[] quoteFieldNameArray = fieldStr.split(",");
		for (String quoteFieldName : quoteFieldNameArray)
			quoteInfo.put(quoteFieldName, pos);
	}

	public Map<String, Position> getQuoteInfo()
	{
		return quoteInfo;
	}

	// public void setQuoteField(String field)
	// {
	// this.quotePos = new Position();
	// this.quotePos.setPosition(field, ",");
	// }

	// public String getQuoteField()
	// {
	// return this.quotePos.getPositionString();
	// }

	public void setField(String field)
	{
		this.cmdTypePos = new Position();
		this.cmdTypePos.setPosition(field, ",");
	}

	public String getField()
	{
		return this.cmdTypePos.getPositionString();
	}

	/**
	 * @return the value
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value)
	{
		this.value = value;
	}

	/**
	 * @return the head
	 */
	public String getHead()
	{
		return head;
	}

	/**
	 * @param head
	 *            the head to set
	 */
	public void setHead(String head)
	{
		this.head = head;
	}

	/**
	 * @return the nodeClass
	 */
	public String getNodeClass()
	{
		return nodeClass;
	}

	/**
	 * @param nodeClass
	 *            the nodeClass to set
	 */
	public void setNodeClass(String nodeClass)
	{
		this.nodeClass = nodeClass;
	}

	/**
	 * @return the protocol
	 */
	public String getProtocol()
	{
		return protocol;
	}

	/**
	 * @param protocol
	 *            the protocol to set
	 */
	public void setProtocol(String pro)
	{
		this.protocol = pro;
	}

	/**
	 * @return the quoteFieldName
	 */
	// public List<String> getQuoteFieldName()
	// {
	// return quoteFieldName;
	// }
	@Deprecated
	public Object getQuoteFieldName(Class<?> clazz)
	{
		if (clazz.equals(String.class))
		{
			String ret = "";
			// for (String quoteFieldName : this.quoteFieldName)
			for (String quoteFieldName : this.quoteInfo.keySet())
				ret = ret + quoteFieldName + ",";
			ret = ret.substring(0, ret.length() - 1);
			return ret;
		}
		else if (clazz.equals(List.class))
		{
			List<String> fieldNameList = new ArrayList<String>();
			for (String fieldName : this.quoteInfo.keySet())
				fieldNameList.add(fieldName);
			return fieldNameList;
		}

		return null;
	}

	/**
	 * @param quoteFieldName
	 *            the quoteFieldName to set
	 */
	public void setQuoteFieldName(String quoteFieldNameInXml)
	{
		// String[] quoteFieldNameArray = quoteFieldNameInXml.split(",");
		// this.quoteFieldName = new ArrayList<String>();
		// for (String quoteFieldName : quoteFieldNameArray)
		// this.quoteFieldName.add(quoteFieldName);
	}

	/**
	 * @return the cmdTypePos
	 */
	public Position getCmdTypePos()
	{
		return cmdTypePos;
	}

	/**
	 * @param cmdTypePos
	 *            the cmdTypePos to set
	 */
	public void setCmdTypePos(Position cmdTypePos)
	{
		this.cmdTypePos = cmdTypePos;
	}

	/**
	 * 字符串形式返回位置信息，起始和截止以逗号分隔
	 * @return
	 * @author zhaokai
	 * @create 2017年10月9日 下午6:21:44
	 */
	public String getQuotePosString(String quoteFieldName)
	{
		if (this.quoteInfo == null)
			return null;
		return this.quoteInfo.get(quoteFieldName).getPositionString();
	}

	/**
	 * @param quotePos
	 *            the quotePos to set
	 */
	// public void setQuotePos(Position quotePos)
	// {
	// this.quotePos = quotePos;
	// }

	/**
	 * @param quoteFieldName
	 *            the quoteFieldName to set
	 */
	// public void setQuoteFieldName(List<String> quoteFieldName)
	// {
	// this.quoteFieldName = quoteFieldName;
	// }

	/**
	 * @return the tail
	 */
	public String getTail()
	{
		return tail;
	}

	/**
	 * @param tail
	 *            the tail to set
	 */
	public void setTail(String tail)
	{
		this.tail = tail;
	}

}
