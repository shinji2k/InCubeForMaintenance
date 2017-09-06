package com.crscic.interfacetesttool.data;

import java.util.Map;

public class ResInfo
{
	/**
	 * 返回的协议名称
	 */
	private String protocol;
	
	/**
	 * 需要引用请求中字段的信息
	 */
	private Map<String, byte[]> quoteMap;
	
	public String getProtocol()
	{
		return protocol;
	}
	public void setProtocol(String protocol)
	{
		this.protocol = protocol;
	}
	public Map<String, byte[]> getQuoteMap()
	{
		return quoteMap;
	}
	public void setQuoteMap(Map<String, byte[]> quoteMap)
	{
		this.quoteMap = quoteMap;
	}
}
