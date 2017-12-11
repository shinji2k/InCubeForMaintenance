package com.crscic.interfacetesttool.config;

/**
 * 
 * @author ken_8
 * @create 2017年10月12日 下午11:44:31
 */
public class Request
{
	private String pro;
	private Response response;
	
	/**
	 * @return the sendProtocol
	 */
	public String getSendProtocol()
	{
		return pro;
	}
	/**
	 * @param sendProtocol the sendProtocol to set
	 */
	public void setSendProtocol(String sendProtocol)
	{
		this.pro = sendProtocol;
	}
	/**
	 * @return the response
	 */
	public Response getResponse()
	{
		return response;
	}
	/**
	 * @param response the response to set
	 */
	public void setResponse(Response response)
	{
		this.response = response;
	}
}
