/**
 * 
 */
package com.crscic.interfacetesttool.connector;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

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
	
	private Socket connect;
	
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
		
	}

	/* (non-Javadoc)
	 * @see com.crscic.interfacetesttool.connector.Connector#startReply()
	 */
	@Override
	public void startReply()
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.crscic.interfacetesttool.connector.Connector#connect()
	 */
	@Override
	public void connect() throws UnknownHostException, IOException
	{
		if (type.toLowerCase().equals("client"))
		{
			connect = new Socket(ip, port);
		}
		else
		{
			SocketServer server = new SocketServer(this.port, keepAlive);
		}
	}
}
