/**
 * 
 */
package com.crscic.interfacetesttool.connector;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * 
 * @author zhaokai
 * 2017年9月7日 下午3:11:54
 */
public interface Connector
{
	/**
	 * 发送数据
	 * 
	 * zhaokai
	 * 2017年9月10日 下午2:12:34
	 */
	public void send();
	public void startReply();
	
	/**
	 * 建立连接
	 * @throws UnknownHostException
	 * @throws IOException
	 * zhaokai
	 * 2017年9月10日 下午1:58:03
	 */
	public void connect() throws UnknownHostException, IOException;
}
