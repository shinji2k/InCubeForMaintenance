/**
 * 
 */
package com.crscic.interfacetesttool.data;

import java.util.List;

/**
 * 
 * @author zhaokai
 * 2017年9月12日 下午12:27:44
 */
public class ProtocolStructure
{
	private String protocolName;
	private List<Part> part;
	/**
	 * @return the protocolName
	 */
	public String getProtocolName()
	{
		return protocolName;
	}
	/**
	 * @param protocolName the protocolName to set
	 */
	public void setProtocolName(String protocolName)
	{
		this.protocolName = protocolName;
	}
	/**
	 * @return the part
	 */
	public List<Part> getPart()
	{
		return part;
	}
	/**
	 * @param part the part to set
	 */
	public void setPart(List<Part> part)
	{
		this.part = part;
	}
	
	
}
