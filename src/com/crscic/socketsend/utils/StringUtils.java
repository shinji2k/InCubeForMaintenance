package com.crscic.socketsend.utils;

public class StringUtils
{
	public static Boolean isNullOrEmpty(String str)
	{
		if (str == null)
			return true;
		
		if (str.equals(""))
			return true;
		
		return false;
	}
}
