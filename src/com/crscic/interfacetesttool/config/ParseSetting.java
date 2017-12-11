package com.crscic.interfacetesttool.config;

import java.util.List;

/**
 * 
 * @author ken_8
 * @create 2017年10月12日 下午11:10:19
 */
public class ParseSetting
{
	private String timeout;
	private String retry;
	private String protocolFile;
	private List<Request> request;
	/**
	 * @return the timeOut
	 */
	public String getTimeout()
	{
		return timeout;
	}
	/**
	 * @param timeOut the timeOut to set
	 */
	public void setTimeout(String timeout)
	{
		this.timeout = timeout;
	}
	/**
	 * @return the retry
	 */
	public String getRetry()
	{
		return retry;
	}
	/**
	 * @param retry the retry to set
	 */
	public void setRetry(String retry)
	{
		this.retry = retry;
	}
	/**
	 * @return the protocolFile
	 */
	public String getProtocolFile()
	{
		return protocolFile;
	}
	/**
	 * @param protocolFile the protocolFile to set
	 */
	public void setProtocolFile(String protocolFile)
	{
		this.protocolFile = protocolFile;
	}
	/**
	 * @return the request
	 */
	public List<Request> getRequest()
	{
		return request;
	}
	/**
	 * @param request the request to set
	 */
	public void setRequest(List<Request> request)
	{
		this.request = request;
	}
}
