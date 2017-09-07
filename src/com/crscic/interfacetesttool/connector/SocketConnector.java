/**
 * 
 */
package com.crscic.interfacetesttool.connector;

import com.crscic.interfacetesttool.entity.SocketConfig;
import com.k.socket.SocketClient;
import com.k.socket.SocketServer;

/**
 * 
 * @author zhaokai 2017年9月7日 下午3:12:32
 */
public class SocketConnector implements Connector
{
	private String type;
	private String ip;
	private int port;
	private boolean keepAlive;
	private SocketClient client;
	private SocketServer server;
	
	public SocketConnector(SocketConfig sockCfg)
	{
		this.type = sockCfg.getType();
		this.ip = sockCfg.getIp();
		this.port = Integer.parseInt(sockCfg.getPort());
		this.keepAlive = sockCfg.getKeepAlive();
	}

	/* (non-Javadoc)
	 * @see com.crscic.interfacetesttool.connector.Connector#start()
	 */
	@Override
	public void start()
	{
		if (type.toLowerCase().equals("client"))
		{
			client = new SocketClient(this.ip, this.port, keepAlive);
		}
		else
		{
			server = new SocketServer(this.port);
		}
	}

	/* (non-Javadoc)
	 * @see com.crscic.interfacetesttool.connector.Connector#startReply()
	 */
	@Override
	public void startReply()
	{
		// TODO Auto-generated method stub
		
	}
}
