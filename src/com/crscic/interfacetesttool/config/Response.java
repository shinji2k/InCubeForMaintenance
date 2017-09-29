package com.crscic.interfacetesttool.config;

import java.util.ArrayList;
import java.util.List;

import com.crscic.interfacetesttool.entity.Position;

/**
 * @author zhaokai
 *
 * 2017年9月28日 下午10:56:14
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
	//"class"
	private String nodeClass;
	private String protocol;
	/**
	 * 需要引用自请求中的字段
	 */
	private Position quotePos;
	/**
	 * 引用字段对应发送数据协议中的字段名称
	 */
	private List<String> quoteFieldName;
	
	
	
	
	
	public void setQuoteField(String field)
	{
		this.quotePos = new Position();
		this.quotePos.setPosition(field, ",");
	}
	
	public String getQuoteField()
	{
		return this.quotePos.getPositionString();
	}
	
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
	 * @param value the value to set
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
	 * @param head the head to set
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
	 * @param nodeClass the nodeClass to set
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
	 * @param protocol the protocol to set
	 */
	public void setProtocol(String pro)
	{
		this.protocol = pro;
	}
	/**
	 * @return the quoteFieldName
	 */
	public List<String> getQuoteFieldName()
	{
		return quoteFieldName;
	}
	public Object getQuoteFieldName(Class<?> clazz)
	{
		if (clazz.equals(String.class))
		{
			String ret = "";
			for (String quoteFieldName : this.quoteFieldName)
				ret = ret + quoteFieldName + ",";
			ret = ret.substring(0, ret.length() - 1);
			return ret;
		}
		else if (clazz.equals(List.class))
		{
			return this.quoteFieldName;
		}
		
		return null;
	}
	/**
	 * @param quoteFieldName the quoteFieldName to set
	 */
	public void setQuoteFieldName(String quoteFieldNameInXml)
	{
		String[] quoteFieldNameArray = quoteFieldNameInXml.split(",");
		this.quoteFieldName = new ArrayList<String>();
		for (String quoteFieldName : quoteFieldNameArray)
			this.quoteFieldName.add(quoteFieldName);
	}

	/**
	 * @return the cmdTypePos
	 */
	public Position getCmdTypePos()
	{
		return cmdTypePos;
	}

	/**
	 * @param cmdTypePos the cmdTypePos to set
	 */
	public void setCmdTypePos(Position cmdTypePos)
	{
		this.cmdTypePos = cmdTypePos;
	}

	/**
	 * @return the quotePos
	 */
	public Position getQuotePos()
	{
		return quotePos;
	}

	/**
	 * @param quotePos the quotePos to set
	 */
	public void setQuotePos(Position quotePos)
	{
		this.quotePos = quotePos;
	}

	/**
	 * @param quoteFieldName the quoteFieldName to set
	 */
	public void setQuoteFieldName(List<String> quoteFieldName)
	{
		this.quoteFieldName = quoteFieldName;
	}

	/**
	 * @return the tail
	 */
	public String getTail()
	{
		return tail;
	}

	/**
	 * @param tail the tail to set
	 */
	public void setTail(String tail)
	{
		this.tail = tail;
	}

}
