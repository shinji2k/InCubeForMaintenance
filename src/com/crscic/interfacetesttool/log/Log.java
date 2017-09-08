/**
 * 
 */
package com.crscic.interfacetesttool.log;

import java.text.SimpleDateFormat;

/**
 * 
 * @author zhaokai
 * 2017年9月7日 下午2:47:01
 */
public class Log
{
	public static void info(String log)
	{
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currTime = date.format(System.currentTimeMillis());
		System.out.println(currTime + " INFO " + log);
	}
}
