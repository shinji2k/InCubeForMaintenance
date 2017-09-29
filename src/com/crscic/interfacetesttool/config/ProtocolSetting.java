/**
 * 
 */
package com.crscic.interfacetesttool.config;

import java.util.List;

/**
 * @author zhaokai
 *
 * 2017年9月11日 上午10:22:41
 */
public class ProtocolSetting
{
	private List<SendSetting> sendConfig;
	private List<ReplySetting> replyConfig;
	private String proFilePath;
	/**
	 * @return the sendConfig
	 */
	public List<SendSetting> getSendConfig()
	{
		return sendConfig;
	}
	/**
	 * @param sendConfig the sendConfig to set
	 */
	public void setSendConfig(List<SendSetting> sendConfig)
	{
		this.sendConfig = sendConfig;
	}
	/**
	 * @return the replyConfig
	 */
	public List<ReplySetting> getReplyConfig()
	{
		return replyConfig;
	}
	/**
	 * @param replyConfig the replyConfig to set
	 */
	public void setReplyConfig(List<ReplySetting> replyConfig)
	{
		this.replyConfig = replyConfig;
	}
	/**
	 * @return the proFilePath
	 */
	public String getProFilePath()
	{
		return proFilePath;
	}
	/**
	 * @param proFilePath the proFilePath to set
	 */
	public void setProFilePath(String proFilePath)
	{
		this.proFilePath = proFilePath;
	}
	
	
	
	
}
