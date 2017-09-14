/**
 * 
 */
package com.crscic.interfacetesttool.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhaokai
 *
 * 2017年9月11日 上午10:23:58
 */
public class ReplyConfig
{
	/**
	 * 截取请求中的字段位置，逗号分隔起止
	 */
	private int fieldStart;
	private int fieldStop;
	/**
	 * 匹配的值
	 */
	private String value;
	private String head;
	//"class"
	private String nodeClass;
	private String protocol;
	/**
	 * 需要引用自请求中的字段
	 */
	private String quoteField;
	/**
	 * 引用字段对应发送数据协议中的字段名称
	 */
	private List<String> quoteFieldName;
	/**
	 * @return the field
	 */
	public String getField()
	{
		return this.fieldStart + "," + this.fieldStop;
	}
	/**
	 * @param field the field to set
	 */
	public void setField(String field)
	{
		String[] pos = field.split(",");
		this.fieldStart = Integer.parseInt(pos[0]);
		this.fieldStop = Integer.parseInt(pos[1]);;
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
	 * @return the quoteField
	 */
	public String getQuoteField()
	{
		return quoteField;
	}
	/**
	 * @param quoteField the quoteField to set
	 */
	public void setQuoteField(String quoteField)
	{
		this.quoteField = quoteField;
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
}
