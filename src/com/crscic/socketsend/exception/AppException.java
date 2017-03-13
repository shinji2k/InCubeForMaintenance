package com.crscic.socketsend.exception;

import org.dom4j.Element;

public class AppException extends Exception
{
	private static final long serialVersionUID = 1L;
	
	public AppException(String msg) 
	{
		super(msg);
	}
	
	public static AppException filepathErr(Element target)
	{
		String errMsg = "文件路径为空，" + getAttributeInfo(target);
		return new AppException(errMsg);
	}
	
	public static AppException nodeErr(Element target, String nodeName)
	{
		String errMsg = nodeName + "节点解析错误，" + getAttributeInfo(target);
		return new AppException(errMsg);
	}
	
	public static AppException lengthErr(Element target)
	{
		String errMsg = "生成的长度与Xml中给定的长度不符，" + getAttributeInfo(target);
		return new AppException(errMsg);
	}
	
	public static String getAttributeInfo(Element target)
	{
		String errMsg = "节点属性：";
		int attrCnt = target.attributeCount();
		if (attrCnt == 0)
			return errMsg + "节点无属性信息";
		for (int i = 0; i < attrCnt; i++)
			errMsg += target.attribute(i).getName() + "=" 
					+ target.attributeValue(target.attribute(i).getName()) + ",";
		return errMsg.substring(0, errMsg.length() - 1);
	}
	
	public static AppException lostAttribute(Element target, String attributeName)
	{
		String errMsg = "缺少" + attributeName + "属性，" + getAttributeInfo(target);
		return new AppException(errMsg);
	}

	public static AppException nullNodeErr(Element target, String nodeName)
	{
		String errMsg = "找不到子节点：" + nodeName + "，" + getAttributeInfo(target);
		return new AppException(errMsg);
	}
}
