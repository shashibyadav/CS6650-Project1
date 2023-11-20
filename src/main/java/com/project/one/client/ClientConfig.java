package com.project.one.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.project.one.server.ProjectEnums.ConnectionType;
import com.project.one.utills.Utills;

public class ClientConfig {
	private int clientPort = -1;
	private int serverPort = -1;
	private ConnectionType type = ConnectionType.TCP;
	private ExecutorService executor = Executors.newCachedThreadPool();
	private int udpPacketLength = 65535;
	private String serverHost = "localhost";
	private String clientHost = "localhost";
	private int requestTimeout = 60000;
	private boolean logs = false;
	public int getClientPort() {
		return clientPort;
	}
	public void setClientPort(int clientPort) {
		this.clientPort = clientPort;
	}
	public int getServerPort() {
		return serverPort;
	}
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}
	public ConnectionType getType() {
		return type;
	}
	public void setType(ConnectionType type) {
		this.type = type;
	}
	public ExecutorService getExecutor() {
		return executor;
	}
	public void setExecutor(ExecutorService executor) {
		this.executor = (executor != null) ? executor : this.executor;
	}
	public int getUdpPacketLength() {
		return udpPacketLength;
	}
	public void setUdpPacketLength(int udpPacketLength) {
		this.udpPacketLength = (udpPacketLength < this.udpPacketLength) ? this.udpPacketLength : udpPacketLength;
	}
	public String getServerHost() {
		return serverHost;
	}
	public void setServerHost(String serverHost) {
		this.serverHost = (Utills.isEmptyString(serverHost)) ? "localhost" : serverHost;
	}
	public String getClientHost() {
		return clientHost;
	}
	public void setClientHost(String clientHost) {
		this.clientHost = (Utills.isEmptyString(clientHost)) ? "localhost" : clientHost;
	}
	public int getRequestTimeout() {
		return requestTimeout;
	}
	public void setRequestTimeout(int requestTimeout) {
		this.requestTimeout = (requestTimeout < this.requestTimeout) ? this.requestTimeout : requestTimeout
				;
	}
	public boolean isLogs() {
		return logs;
	}
	public void setLogs(boolean logs) {
		this.logs = logs;
	}
	
	
}
