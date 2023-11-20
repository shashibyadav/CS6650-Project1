package com.project.one.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.project.one.server.ProjectEnums.ConnectionType;
import com.project.one.server.ProjectEnums.MethodType;
import com.project.one.server.ProjectEnums.RequestKeys;
import com.project.one.utills.SocketM;
import com.project.one.utills.Utills;

public class Client {
	private SocketM socket = null;
	private DatagramSocket udpSocket = null;
	private ExecutorService executor = null;
	private InetAddress iaServer = null;
	@SuppressWarnings("unused")
	private InetAddress iaClient = null;
	private ClientConfig cConfig = null;

	public Client(ClientConfig cConfig) throws UnknownHostException {
		this.cConfig = cConfig;
		this.executor = cConfig.getExecutor();
		this.iaServer = InetAddress.getByName(cConfig.getServerHost());
		this.iaClient = InetAddress.getByName(cConfig.getClientHost());
	}

	private void logger(String msg) {
		if (this.cConfig.isLogs()) {
			System.out.println(msg);
		}
	}

	public void startClient() throws IOException {
		if (this.cConfig.getType() == ConnectionType.TCP) {
			this.socket = this.socket != null ? this.socket
					: new SocketM(this.cConfig.getServerHost(), this.cConfig.getServerPort());
			this.socket.setKeepAlive(true);
			this.socket.initialize();
		}
		if (this.cConfig.getType() == ConnectionType.UDP) {
			if (this.cConfig.getClientPort() > -1) {
				this.udpSocket = this.udpSocket != null ? this.udpSocket : new DatagramSocket(this.cConfig.getClientPort());
			} else {
				this.udpSocket = this.udpSocket != null ? this.udpSocket : new DatagramSocket();
			}
			this.udpSocket.setSoTimeout(this.cConfig.getRequestTimeout());
		}
		this.logger("Client started");
	}

	public void sendObjectRequest(Map<RequestKeys, Object> request) throws IOException {
		ObjectOutputStream oos = this.socket.getObjectOutputStream();
		oos.writeObject(request);
		oos.flush();
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> readObjectRequestUDP(boolean wait) throws IOException, ClassNotFoundException {
		try {
			byte[] dataBucket = new byte[this.cConfig.getUdpPacketLength()];
			DatagramPacket buffer = new DatagramPacket(dataBucket, this.cConfig.getUdpPacketLength());
			this.udpSocket.receive(buffer);
			ByteArrayInputStream bais = new ByteArrayInputStream(buffer.getData());
			ObjectInputStream ois = new ObjectInputStream(bais);
			if (wait) {
				Map<RequestKeys, Object> response = (Map<RequestKeys, Object>) ois.readObject();
				Map<String, Object> data = Utills.getRequestData(response);
				return data;
			} else {
				if (ois.available() > 0) {
					Map<RequestKeys, Object> response = (Map<RequestKeys, Object>) ois.readObject();
					Map<String, Object> data = Utills.getRequestData(response);
					return data;
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void sendObjectRequestUDP(Map<RequestKeys, Object> request) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(this.cConfig.getUdpPacketLength());
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(request);
		byte[] data = baos.toByteArray();
		DatagramPacket buffer = new DatagramPacket(data, data.length, this.iaServer, this.cConfig.getServerPort());
		this.udpSocket.send(buffer);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> readObjectRequest(boolean wait) throws IOException, ClassNotFoundException {
		try {
			ObjectInputStream ois = this.socket.getObjectInputStream();
			if (wait) {
				Map<RequestKeys, Object> response = (Map<RequestKeys, Object>) ois.readObject();
				Map<String, Object> data = Utills.getRequestData(response);
				return data;
			} else {
				if (ois.available() > 0) {
					Map<RequestKeys, Object> response = (Map<RequestKeys, Object>) ois.readObject();
					Map<String, Object> data = Utills.getRequestData(response);
					return data;
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void putData(String key, Object data) throws IOException {
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put(key, data);
		Map<RequestKeys, Object> request = Utills.generateRequest(MethodType.PUT, payload);
		if (this.cConfig.getType() == ConnectionType.TCP) {
			this.sendObjectRequest(request);
		} else if (this.cConfig.getType() == ConnectionType.UDP) {
			this.sendObjectRequestUDP(request);
		}
	}

	public void getData(String key) throws IOException, ClassNotFoundException {
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put(key, true);
		Map<RequestKeys, Object> request = Utills.generateRequest(MethodType.GET, payload);
		Map<String, Object> result = null;
		if (this.cConfig.getType() == ConnectionType.TCP) {
			this.sendObjectRequest(request);
			result = this.readObjectRequest(true);
		} else if (this.cConfig.getType() == ConnectionType.UDP) {
			this.sendObjectRequestUDP(request);
			result = this.readObjectRequestUDP(true);
		}
		System.out.println(result);
	}

	public String runData(String text) throws ClassNotFoundException, IOException {
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put(MethodType.RUN.toString(), text);
		Map<RequestKeys, Object> request = Utills.generateRequest(MethodType.RUN, payload);
		Map<String, Object> result = null;
		if (this.cConfig.getType() == ConnectionType.TCP) {
			this.sendObjectRequest(request);
			result = this.readObjectRequest(true);
		} else if (this.cConfig.getType() == ConnectionType.UDP) {
			this.sendObjectRequestUDP(request);
			result = this.readObjectRequestUDP(true);
		}
		return (String) result.get(MethodType.RUN.toString());

	}

	public void deleteData(String key) throws IOException {
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put(key, true);
		Map<RequestKeys, Object> request = Utills.generateRequest(MethodType.DELETE, payload);
		if (this.cConfig.getType() == ConnectionType.UDP) {
			this.sendObjectRequestUDP(request);
		} else if (this.cConfig.getType() == ConnectionType.TCP) {
			this.sendObjectRequest(request);
		}
	}

	public void readData() throws IOException, ClassNotFoundException {
		Map<String, Object> data = this.readObjectRequest(false);
		this.logger(data.toString());
	}

	public void stopClient() {
		if (this.socket != null && !this.socket.isClosed()) {
			try {
				this.socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (this.udpSocket != null && !this.udpSocket.isClosed()) {
			this.udpSocket.close();
		}
		this.logger("Client Stopped");
	}

	public void stopServerCall() throws IOException {
		Map<RequestKeys, Object> request = Utills.generateRequest(MethodType.STOP, null);
		if (this.cConfig.getType() == ConnectionType.TCP) {
			this.sendObjectRequest(request);
		} else if (this.cConfig.getType() == ConnectionType.UDP) {
			this.sendObjectRequestUDP(request);
		}
	}

	public void startClientAsync(int waitTime) {
		final Client self = this;
		final int wait = waitTime;
		this.executor.submit(new Runnable() {

			@Override
			public void run() {
				try {
					if (wait >= 0) {
						Thread.sleep(wait);
					}
					self.startClient();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});
	}
}