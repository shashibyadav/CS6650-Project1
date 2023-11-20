package com.project.one.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.project.one.server.ProjectEnums.ConnectionType;
import com.project.one.server.ProjectEnums.MethodType;
import com.project.one.server.ProjectEnums.RequestKeys;
import com.project.one.utills.BusinessException;
import com.project.one.utills.ServerSocketM;
import com.project.one.utills.SocketM;
import com.project.one.utills.Utills;

public class Server {
	private ServerSocketM tcpSocket = null;
	private DatagramSocket udpSocket = null;
	private ExecutorService executor = null;
	private ServerConfig sConfig = null;
	private Map<String, Object> config = new ConcurrentHashMap<String, Object>();
	private List<Future<?>> futureList = new LinkedList<Future<?>>();
	private Set<SocketM> tcpSocketSet = new CopyOnWriteArraySet<SocketM>();
	private Set<SocketM> udpSocketSet = new CopyOnWriteArraySet<SocketM>();
	private Map<String, Object> store = new ConcurrentHashMap<String, Object>();
	private Queue<DatagramPacket> udpDatagramPackQ = new ConcurrentLinkedQueue<DatagramPacket>();
	private File logFile = null;
	private PrintWriter logWriter = null;

	public Server(ServerConfig sConfig) {
		this.executor = sConfig.getExecutor();
		this.sConfig = sConfig;
		this.configSetup();
	}
	
	private void logggerEx (Exception e) {
		e.printStackTrace(this.logWriter);
		this.logWriter.flush();
	}

	private void logger(String msg) {
		try {
			this.logWriter.println(msg);
			this.logWriter.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (this.sConfig.isLogs()) {
			System.out.println(msg);
		}
	}

	private void configSetup() {
		this.config.put("serverStarted", false);
	}

	public void startServer() throws IOException {
		if (this.tcpSocket == null
				&& (this.sConfig.getType() == ConnectionType.TCP || this.sConfig.getType() == ConnectionType.ALL)) {
			this.tcpSocket = new ServerSocketM(this.sConfig.getTcpPort(),
					InetAddress.getByName(this.sConfig.getServerHost()));
		}
		if (this.udpSocket == null
				&& (this.sConfig.getType() == ConnectionType.UDP || this.sConfig.getType() == ConnectionType.ALL)) {
			this.udpSocket = new DatagramSocket(this.sConfig.getUdpPort(),
					InetAddress.getByName(this.sConfig.getServerHost()));
			this.udpSocket.setSoTimeout(this.sConfig.getRequestTimeout());
		}
		// Starting logger
		String filename = "./Server_log_" + System.currentTimeMillis() + ".txt";
		this.logFile = new File(filename);
		if (this.logFile.exists()) {
			this.logFile.delete();
		}
		this.logFile.createNewFile();
		this.logWriter = new PrintWriter(this.logFile);

		final Map<String, Object> config = this.config;
		final ServerSocketM tcpServerSocket = this.tcpSocket;
		final DatagramSocket udpServerSocket = this.udpSocket;
		final Set<SocketM> tcpSocketSet = this.tcpSocketSet;
		final Set<SocketM> udpSocketSet = this.udpSocketSet;
		final Map<String, Object> store = this.store;
		final Server self = this;
		this.config.put("serverStarted", true);
		// Cleaup thread
		Future<?> futureCleanUp = this.executor.submit(new Runnable() {

			@Override
			public void run() {
				self.logger("CleanUp thread started");
				while ((boolean) config.get("serverStarted")) {
					try {
						for (SocketM socket : tcpSocketSet) {
							if (socket.isClosed()) {
								tcpSocketSet.remove(socket);
							}
						}
						for (SocketM socket : udpSocketSet) {
							if (socket.isClosed()) {
								tcpSocketSet.remove(socket);
							}
						}
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						if ((boolean) config.get("serverStarted")) {
							self.logggerEx(e);
						}
					}
				}
			}
		});
		this.futureList.add(futureCleanUp);
		if (this.sConfig.getType() == ConnectionType.TCP || this.sConfig.getType() == ConnectionType.ALL) {
			// Thread accepting TCP new connections
			Future<?> futureTCP = this.executor.submit(new Runnable() {

				@Override
				public void run() {
					self.logger("TCP accept Thread started");
					while ((boolean) config.get("serverStarted")) {
						SocketM sSocket = null;
						try {
							self.logger("waiting for client");
							sSocket = tcpServerSocket.accept();
							sSocket.initialize();
							sSocket.setKeepAlive(true);
							tcpSocketSet.add(sSocket);
						} catch (Exception ex) {
							if ((boolean) config.get("serverStarted")) {
								self.logggerEx(ex);
							}
						}
						Thread.yield();
					}
				}

			});

			// Thread processing TCP request
			Future<?> futureTCPProcess = this.executor.submit(new Runnable() {

				@Override
				public void run() {
					self.logger("TCP process thread started");
					while ((boolean) config.get("serverStarted")) {
						for (SocketM socket : tcpSocketSet) {
							try {
								if (socket.isConnected() && !socket.isClosed()
										&& socket.getInputStream().available() > 0) {
									Server.tcpProcessRequest(socket, self, store);
								}
							} catch (ClassNotFoundException | IOException e) {
								if ((boolean) config.get("serverStarted")) {
									self.logggerEx(e);
								}
							}
						}
						Thread.yield();
					}

				}
			});
			this.futureList.add(futureTCP);
			this.futureList.add(futureTCPProcess);

		}
		if (this.sConfig.getType() == ConnectionType.UDP || this.sConfig.getType() == ConnectionType.ALL) {
			// Thread accepting UDP new connections
			Future<?> futureUDP = this.executor.submit(new Runnable() {

				@Override
				public void run() {
					self.logger("UDP accept thread started");
					while ((boolean) config.get("serverStarted")) {
						try {
							if (udpServerSocket.isClosed()) {
								throw new BusinessException("UDP socket is Closed");
							}
							Server.readUDPPacket(udpServerSocket, self);
						} catch (IOException | BusinessException e) {
							if ((boolean) config.get("serverStarted")) {
								self.logggerEx(e);
							}
						}
						Thread.yield();
					}
				}

			});
			// Thread processing UDP request
			Future<?> futureUDPProcess = this.executor.submit(new Runnable() {

				@Override
				public void run() {
					self.logger("UDP process thread started");
					while ((boolean) config.get("serverStarted")) {
						try {
							if (!self.udpDatagramPackQ.isEmpty()) {
								DatagramPacket dp = self.udpDatagramPackQ.poll();
								Server.udpProcessRequest(dp, self, store);
							}
						} catch (ClassNotFoundException | IOException e) {
							if ((boolean) config.get("serverStarted")) {
								self.logggerEx(e);
							}
						}
						Thread.yield();
					}
				}
			});
			this.futureList.add(futureUDP);
			this.futureList.add(futureUDPProcess);
		}
		this.logger("Server started");
	}

	private String run(Map<String, Object> request) {
		String data = (String) request.get(MethodType.RUN.toString());
		char[] charArr = data.toCharArray();
		StringBuilder sb = new StringBuilder();
		for (int i = charArr.length - 1; i >= 0; i--) {
			char c = data.charAt(i);
			if (!Character.isAlphabetic(c)) {
				sb.append(c);
				continue;
			}
			if (Character.isUpperCase(c)) {
				sb.append(Character.toLowerCase(c));
			} else if (Character.isLowerCase(c)) {
				sb.append(Character.toUpperCase(c));
			}
		}
		return sb.toString();
	}

	private static void tcpProcessRequest(SocketM socket, Server self, Map<String, Object> store)
			throws ClassNotFoundException, IOException {
		Map<RequestKeys, Object> data = Server.readInput(socket);
		MethodType rKey = (MethodType) data.get(RequestKeys.type);
		Map<String, Object> rData = Utills.getRequestData(data);
		Map<String, Object> result = new HashMap<String, Object>();
		Map<RequestKeys, Object> response = null;
		switch (rKey) {
		case PUT:
			for (String key : rData.keySet()) {
				store.put(key, rData.get(key));
			}
			break;
		case GET:
			for (String key : rData.keySet()) {
				result.put(key, store.get(key));
			}
			response = Utills.generateRequest(rKey, result);
			Server.sendOutput(socket, response);
			break;
		case DELETE:
			for (String key : rData.keySet()) {
				store.remove(key);
			}
			break;
		case STOP:
			self.stopServer();
			break;
		case RUN:
			String tempData = self.run(rData);
			result.put(MethodType.RUN.toString(), tempData);
			response = Utills.generateRequest(rKey, result);
			Server.sendOutput(socket, response);
			break;
		default:
			break;
		}
	}

	private static void udpProcessRequest(DatagramPacket packet, Server self, Map<String, Object> store)
			throws ClassNotFoundException, IOException {
		Map<RequestKeys, Object> data = Server.readUDPInput(packet, self);
		MethodType rKey = (MethodType) data.get(RequestKeys.type);
		Map<String, Object> rData = Utills.getRequestData(data);
		Map<String, Object> result = new HashMap<String, Object>();
		Map<RequestKeys, Object> response = null;
		switch (rKey) {
		case PUT:
			for (String key : rData.keySet()) {
				store.put(key, rData.get(key));
			}
			break;
		case GET:
			for (String key : rData.keySet()) {
				result.put(key, store.get(key));
			}
			response = Utills.generateRequest(rKey, result);
			Server.sendUDPOutput(packet, self, response);
			break;
		case DELETE:
			for (String key : rData.keySet()) {
				store.remove(key);
			}
			break;
		case STOP:
			self.stopServer();
			break;
		case RUN:
			String tempData = self.run(rData);
			result.put(MethodType.RUN.toString(), tempData);
			response = Utills.generateRequest(rKey, result);
			Server.sendUDPOutput(packet, self, response);
			break;
		default:
			break;
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<RequestKeys, Object> readUDPInput(DatagramPacket packet, Server self)
			throws IOException, ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());
		ObjectInputStream ois = new ObjectInputStream(bais);
		Map<RequestKeys, Object> request = (Map<RequestKeys, Object>) ois.readObject();
		return request;

	}

	private static void readUDPPacket(DatagramSocket socket, Server self) throws IOException {
		byte[] data = new byte[self.sConfig.getUdpPacketLength()];
		DatagramPacket buffer = new DatagramPacket(data, self.sConfig.getUdpPacketLength());
		socket.receive(buffer);
		self.udpDatagramPackQ.add(buffer);
	}

	private static void sendUDPOutput(DatagramPacket packet, Server self, Map<RequestKeys, Object> request)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(self.sConfig.getUdpPacketLength());
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(request);
		byte[] data = baos.toByteArray();
		InetAddress ia = InetAddress.getByName(self.sConfig.getClientHost());
		int clientPort = self.sConfig.getClientUDPPort();
		if (packet != null) {
			ia = packet.getAddress();
			clientPort = packet.getPort();
		}
		DatagramPacket buffer = new DatagramPacket(data, data.length, ia, clientPort);
		self.udpSocket.send(buffer);
	}

	@SuppressWarnings("unchecked")
	private static Map<RequestKeys, Object> readInput(SocketM socket) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = socket.getObjectInputStream();
		Map<RequestKeys, Object> response = (Map<RequestKeys, Object>) ois.readObject();
		return response;
	}

	private static void sendOutput(SocketM socket, Map<RequestKeys, Object> request) throws IOException {
		ObjectOutputStream oos = socket.getObjectOutputStream();
		oos.writeObject(request);
		oos.flush();
	}

//	public void wait

	public boolean stopServer() {
		try {
			this.logger("Server Stop Called");
			this.config.put("serverStarted", false);
			for (SocketM socket : tcpSocketSet) {
				if (!socket.isClosed()) {
					socket.close();
				}
			}
			for (SocketM socket : udpSocketSet) {
				if (!socket.isClosed()) {
					socket.close();
				}
			}
			if (this.tcpSocket != null && !this.tcpSocket.isClosed()) {
				this.tcpSocket.close();
			}
			if (this.udpSocket != null && !this.udpSocket.isClosed()) {
				this.udpSocket.close();
			}
			for (Future<?> future : this.futureList) {
				future.cancel(true);
			}
			this.logger("Child thread interrupt called");
			this.executor.shutdownNow();
			while (!this.executor.isTerminated() && !Thread.interrupted()) {
			}
			this.logger("Server Stopped");
			this.logWriter.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}