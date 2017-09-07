/**
 * 
 */
package com.crscic.interfacetesttool.entity;

/**
 * Com接口的配置实体类
 * @author zhaokai
 * 2017年9月6日 下午6:28:15
 */
public class ComConfig
{
	private String port;
	private String baudrate;
	private String databit;
	private String stopbit;
	private String parity;
	
	public String getPort()
	{
		return port;
	}
	public void setPort(String port)
	{
		this.port = port;
	}
	public String getBaudrate()
	{
		return baudrate;
	}
	public void setBaudrate(String baudrate)
	{
		this.baudrate = baudrate;
	}
	public String getDatabit()
	{
		return databit;
	}
	public void setDatabit(String databit)
	{
		this.databit = databit;
	}
	public String getStopbit()
	{
		return stopbit;
	}
	public void setStopbit(String stopbit)
	{
		this.stopbit = stopbit;
	}
	public String getParity()
	{
		return parity;
	}
	public void setParity(String parity)
	{
		this.parity = parity;
	}
}
