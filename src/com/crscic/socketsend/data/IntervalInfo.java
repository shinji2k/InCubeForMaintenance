package com.crscic.socketsend.data;

public class IntervalInfo
{
	private long lastSendTimeMillis = 0;
	private long intervalMillis;
	private String protocolName;
	
	public long getLastSendTimeMillis()
	{
		return lastSendTimeMillis;
	}
	public void setLastSendTimeMillis(long lastSendTimeMillis)
	{
		this.lastSendTimeMillis = lastSendTimeMillis;
	}
	public long getIntervalMillis()
	{
		return intervalMillis;
	}
	public void setIntervalMillis(long intervalMillis)
	{
		this.intervalMillis = intervalMillis;
	}
	public String getProtocolName()
	{
		return protocolName;
	}
	public void setProtocolName(String protocolName)
	{
		this.protocolName = protocolName;
	}
	

}
