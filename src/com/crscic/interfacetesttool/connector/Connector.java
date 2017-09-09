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
	public void start();
	public void startReply();
	public void connect() throws UnknownHostException, IOException;
}
