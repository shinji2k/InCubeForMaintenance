package com.crscic.interfacetesttool.entity;

public interface IConfig
{
	public String getPortNum();
	public void setPortNum(String portNum);
	public String getBaudrate();
	public void setBaudrate(String baudrate);
	public String getDatabit();
	public void setDatabit(String databit);
	public String getStopbit();
	public void setStopbit(String stopbit);
	public String getParity();
	public void setParity(String parity);
	
	public String getType();
	public void setType(String type);
	public String getIp();
	public void setIp(String ip);
	public String getPort();
	public void setPort(String port);

}
