/**
 * 
 */
package com.crscic.interfacetesttool.data;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author ken_8
 * 2017年9月10日 下午11:50:07
 */
public class Part
{
	private String type;
	private String value;
	private String split;
	/**
	 * "fill-byte"
	 */
	private String fillByte;
	/**
	 * fill-direction
	 */
	private String fillDirection;
	/**
	 * "class"
	 */
	private String valueClass;
	private String len;
	
	private List<Part> childNodeList;
	private Map<String, String> attribute;

	/**
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type)
	{
		this.type = type;
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
	 * @return the split
	 */
	public String getSplit()
	{
		return split;
	}

	/**
	 * @param split the split to set
	 */
	public void setSplit(String split)
	{
		this.split = split;
	}

	/**
	 * @return the fillByte
	 */
	public String getFillByte()
	{
		return fillByte;
	}

	/**
	 * @param fillByte the fillByte to set
	 */
	public void setFillByte(String fillByte)
	{
		this.fillByte = fillByte;
	}

	/**
	 * @return the fillDirection
	 */
	public String getFillDirection()
	{
		return fillDirection;
	}

	/**
	 * @param fillDirection the fillDirection to set
	 */
	public void setFillDirection(String fillDirection)
	{
		this.fillDirection = fillDirection;
	}

	/**
	 * @return the valueClass
	 */
	public String getValueClass()
	{
		return valueClass;
	}

	/**
	 * @param valueClass the valueClass to set
	 */
	public void setValueClass(String valueClass)
	{
		this.valueClass = valueClass;
	}

	/**
	 * @return the len
	 */
	public String getLen()
	{
		return len;
	}

	/**
	 * @param len the len to set
	 */
	public void setLen(String len)
	{
		this.len = len;
	}

	/**
	 * @return the childNode
	 */
	public List<Part> getChildNodeList()
	{
		return childNodeList;
	}

	/**
	 * @param childNode the childNode to set
	 */
	public void setChildNodeList(List<Part> childNodeList)
	{
		this.childNodeList = childNodeList;
	}

	/**
	 * @return the attribute
	 */
	public Map<String, String> getAttribute()
	{
		return attribute;
	}

	/**
	 * @param attribute the attribute to set
	 */
	public void setAttribute(Map<String, String> attribute)
	{
		this.attribute = attribute;
	}
}
