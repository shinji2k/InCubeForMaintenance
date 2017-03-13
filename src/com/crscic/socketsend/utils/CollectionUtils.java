package com.crscic.socketsend.utils;

import java.util.List;

/**
 * 集合相关的工具类
 * 
 * @author zhaokai
 * @version 2017年2月14日 下午5:05:00
 */
public class CollectionUtils
{
	/**
	 * 主要针对基本类型byte的数组将内容追加至List中
	 *
	 * @param list
	 * @param array
	 * @author zhaokai
	 * @version 2017年2月14日 下午5:07:10
	 */
	public static void copyArrayToList(List<Byte> list, byte[] array)
	{
		if (array == null)
			return;
		for (int i = 0; i < array.length; i++)
			list.add(array[i]);
	}
	
	/**
	 * 将byte[]数组装箱为Byte[]
	 *
	 * @param b
	 * @return
	 * @author zhaokai
	 * @version 2017年2月16日 下午1:58:16
	 */
	public static Byte[] byteToByte(byte[] b)
	{
		if (b == null)
			return null;
		Byte[] res = new Byte[b.length];
		for (int i = 0; i < b.length; i++)
			res[i] = b[i];
		return res;
	}
}
