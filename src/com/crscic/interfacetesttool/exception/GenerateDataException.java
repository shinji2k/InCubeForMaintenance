/**
 * 
 */
package com.crscic.interfacetesttool.exception;

import com.crscic.interfacetesttool.log.Log;

/**
 * @author zhaokai
 *
 * 2017年9月12日 下午2:32:05
 */
public class GenerateDataException extends Exception
{
	private static final long serialVersionUID = 5252257370112197585L;
	
	/**
	 * 当子节点中某节点为空时，抛出异常病记录日志
	 * 
	 * @param nodeAttrName
	 *            父节点name属性值
	 * @param nullNodeName
	 *            空节点的节点name
	 * @param judgedString
	 *            进行判断的值
	 * @throws GenerateDataException
	 * @author zhaokai 2017年9月12日 下午6:20:52
	 */
	public static void nullNodeValueException(String nodeAttrName, String nullNodeName) throws GenerateDataException
	{
		Log.error(nodeAttrName + "中子节点" + nullNodeName + "不能为空");
		throw new GenerateDataException();
	}
}
