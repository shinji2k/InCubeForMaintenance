package com.crscic.interfacetesttool.xmlhelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.k.reflect.ReflectUtils;
import com.k.util.StringUtils;

public class XmlHelper
{
	private SAXReader saxReader;
	private Document xmlDocument;
	
	public void loadXml(String xmlPath) throws DocumentException
	{
		saxReader = new SAXReader();
		xmlDocument = saxReader.read(xmlPath);
	}
	
	public Element getSingleElement(String xpath)
	{
		if (StringUtils.isNullOrEmpty(xpath))
			return null;
		return (Element) xmlDocument.selectSingleNode(xpath);
	}
	
	public List<Element> getElements(String xpath)
	{
		if (StringUtils.isNullOrEmpty(xpath))
			return null;
		List<?> elements = xmlDocument.selectNodes(xpath);
		List<Element> res = new ArrayList<Element>();
		for (int i = 0; i < elements.size(); i++)
			res.add((Element) elements.get(i));
		return res;
	}
	
	public static List<Element> getElements(Element ele)
	{
		List<Element> childEleList = new ArrayList<Element>();;
		List<?> elements = ele.elements();
		if (elements.size() > 0)
		{
			for (int i = 0; i < elements.size(); i++)
				childEleList.add((Element) elements.get(i));
		}
		
		return childEleList;
	}
	
	public static List<Attribute> getAttributes(Element ele)
	{
		List<Attribute> attrList = new ArrayList<Attribute>();;
		List<?> attrObjList = ele.attributes();
		if (attrObjList.size() > 0)
		{
			for (int i = 0; i < attrObjList.size(); i++)
				attrList.add((Attribute) attrObjList.get(i));
		}
		
		return attrList;
	}

	/**
	 * 将xml某节点下所有子节点自动填装。 要求：该节点下只有一级子节点，且填装类的属性与节点名称一致。 注意：暂不支持属性的填装
	 * 
	 * @param filePath
	 * @param t
	 * @return zhaokai 2017年9月6日 下午6:33:25
	 */
	public static <T> T fill(Element element, Class<T> t)
	{
		T ret = null;
		Method[] methods = t.getDeclaredMethods();
		try
		{
			ret = t.newInstance();
			List<?> elements = element.elements();
			List<Element> nodeList = new ArrayList<Element>();
			for(int i = 0; i < elements.size(); i++)
				nodeList.add((Element) elements.get(i));

			for (Element node : nodeList)
			{
				Method setMethod = ReflectUtils.getSetMethod(methods, node.getName());
				setMethod.invoke(ret, node.getTextTrim());
			}
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
 			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
		}
		return ret;
	}

	/***************************************************************/
	public XmlHelper()
	{
		super();
	}

	public XmlHelper(String xmlPath) throws DocumentException
	{
		loadXml(xmlPath);
	}
}
