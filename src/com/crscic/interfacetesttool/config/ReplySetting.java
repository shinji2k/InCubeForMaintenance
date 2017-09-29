/**
 * 
 */
package com.crscic.interfacetesttool.config;

import java.util.List;

/**
 * @author zhaokai
 *
 * 2017年9月11日 上午10:23:58
 */
public class ReplySetting
{
	private String settingFilePath;
	private List<Response> responseList;
	
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
	 * @return the responseList
	 */
	public List<Response> getResponseList()
	{
		return responseList;
	}
	/**
	 * @param responseList the responseList to set
	 */
	public void setResponseList(List<Response> responseList)
	{
		this.responseList = responseList;
	}
}
