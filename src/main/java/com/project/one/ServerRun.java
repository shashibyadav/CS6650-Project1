package com.project.one;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.project.one.server.ProjectEnums.ConnectionType;
import com.project.one.server.Server;
import com.project.one.server.ServerConfig;

public class ServerRun {

	public static void run(String[] args) throws IOException, InterruptedException {
		ConnectionType type = Enum.valueOf(ConnectionType.class, args[0]);
		ServerConfig tcpSConfig = new ServerConfig();
		String clientHost = "localhost";
		int clientUDPPort = -1, tcpPort = -1, udpPort = -1;
		String serverHost = (args.length >= 2) ? args[1] : "localhost";
		if (type == ConnectionType.TCP) {
			tcpPort = (args.length >= 3) ? Integer.parseInt(args[2]) : -1;
		} else if (type == ConnectionType.UDP) {
			udpPort = (args.length >= 3) ? Integer.parseInt(args[2]) : -1;
			clientHost = (args.length >= 4) ? args[3] : "localhost";
			clientUDPPort = (args.length >= 5) ? Integer.parseInt(args[4]) : -1;
		} else if (type == ConnectionType.ALL) {
			tcpPort = (args.length >= 3) ? Integer.parseInt(args[2]) : -1;
			udpPort = (args.length >= 4) ? Integer.parseInt(args[3]) : -1;
			clientHost = (args.length >= 5) ? args[4] : "localhost";
			clientUDPPort = (args.length >= 6) ? Integer.parseInt(args[5]) : -1;
		}
		ExecutorService executor = Executors.newCachedThreadPool();
		tcpSConfig.setTcpPort(tcpPort);
		tcpSConfig.setUdpPort(udpPort);
		tcpSConfig.setExecutor(executor);
		tcpSConfig.setType(type);
		tcpSConfig.setClientHost(clientHost);
		tcpSConfig.setServerHost(serverHost);
		tcpSConfig.setClientUDPPort(clientUDPPort);
//		tcpSConfig.logs = true;
		Server server = new Server(tcpSConfig);
		server.startServer();
//        Thread.sleep(60000);
//        server.stopServer();
	}

}
