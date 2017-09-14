/**
 * 
 */
package com.crscic.interfacetesttool.entity;

/**
 * 字段位置实体
 * @author ken_8
 * 2017年9月14日 下午11:44:31
 */
public class Position
{
	private int startPos;
	private int stopPos;
	
	public void setPosition(String posString, String splitChar)
	{
		String[] posStringArray = posString.split(splitChar);
		this.startPos = Integer.parseInt(posStringArray[0]);
		this.stopPos = Integer.parseInt(posStringArray[1]);
	}
	
	public String getPositionString()
	{
		return this.startPos + "," + this.stopPos;
	}
	
	/**
	 * @return the startPos
	 */
	public int getStartPos()
	{
		return startPos;
	}
	/**
	 * @param startPos the startPos to set
	 */
	public void setStartPos(int startPos)
	{
		this.startPos = startPos;
	}
	/**
	 * @return the stopPos
	 */
	public int getStopPos()
	{
		return stopPos;
	}
	/**
	 * @param stopPos the stopPos to set
	 */
	public void setStopPos(int stopPos)
	{
		this.stopPos = stopPos;
	}
	
	
}
