package com.crscic.socketsend.data;

import java.util.List;

import org.dom4j.Element;

public class PartMem
{
	private Element childPart;
	private String length;
	private String childType;
	private String name;
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
		// TODO Auto-generated constructor stub
	}

}
