/**
 * 
 */
package com.crscic.interfacetesttool.config;

import java.util.List;
import java.util.Map;

/**
 * @author zhaokai
 *
 * 2017年9月11日 上午10:23:41
 */
public class SendSetting
{
	private String settingFilePath;
	private Map<String, Long> protocolMap; 
	private List<String> protocolList;
	
	/**
	 * @return the settingFilePath
	 */
	public String getSettingFilePath()
	{
		return settingFilePath;
	}
	/**
	 * @param settingFilePath the settingFilePath to set
	 */
	public void setSettingFilePath(String settingFilePath)
	{
		this.settingFilePath = settingFilePath;
	}
	/**
	 * @return the protocolMap
	 */
	public Map<String, Long> getProtocolMap()
	{
		return protocolMap;
	}
	/**
	 * @param protocolMap the protocolMap to set
	 */
	public void setProtocolMap(Map<String, Long> protocolMap)
	{
		this.protocolMap = protocolMap;
	}
	/**
	 * @return the protocolList
	 */
	public List<String> getProtocolList()
	{
		return protocolList;
	}
	/**
	 * @param protocolList the protocolList to set
	 */
	public void setProtocolList(List<String> protocolList)
	{
		this.protocolList = protocolList;
	}
	
	
}
