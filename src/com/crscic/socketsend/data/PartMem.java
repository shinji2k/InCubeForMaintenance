package com.crscic.socketsend.data;

import java.util.List;

import org.dom4j.Element;

/**
 * 缓存节点信息，当时不完成生成，将节点信息缓存，留置二次填充时进行生成
 * 
 * @author zhaokai
 * @version 2017年4月11日 下午2:51:45
 */
public class PartMem
{
	/**
	 * 子节点
	 */
	private Element childPart;

	/**
	 * 当前节点长度
	 */
	private String length;

	/**
	 * 填充字节
	 */
	private String fillByteString;

	/**
	 * 填充方向，左填充还是右填充
	 */
	private String fillDirection;

	/**
	 * 节点类型
	 */
	private String childType;

	/**
	 * 节点属性name的值，用来标识节点唯一性
	 */
	private String name;

	/**
	 * 
	 */
	private List<String> rangeList;

	public Element getChildPart()
	{
		return childPart;
	}

	public List<String> getRangeList()
	{
		return rangeList;
	}

	public void setRangeList(List<String> rangeList)
	{
		this.rangeList = rangeList;
	}

	public void setChildPart(Element childPart)
	{
		this.childPart = childPart;
	}

	public String getLength()
	{
		return length;
	}

	public void setLength(String length)
	{
		this.length = length;
	}

	public String getChildType()
	{
		return childType;
	}

	public void setChildType(String childType)
	{
		this.childType = childType;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public PartMem()
	{
	}

	public String getFillByteString()
	{
		return fillByteString;
	}

	public void setFillByteString(String fillByteString)
	{
		this.fillByteString = fillByteString;
	}

	public String getFillDirection()
	{
		return fillDirection;
	}

	public void setFillDirection(String fillDirection)
	{
		this.fillDirection = fillDirection;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof PartMem)
		{
			PartMem m = (PartMem) o;
			return m.getName().equals(this.name);
		}
		return super.equals(o);
	}

	public int hashCode()
	{
		return this.name.hashCode();
	}

}
