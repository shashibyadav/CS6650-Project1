package com.project.one;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.project.one.client.Client;
import com.project.one.client.ClientConfig;
import com.project.one.server.ProjectEnums.ConnectionType;

public class ClientRun {
	
	public static void dummyData (Client client) throws IOException, ClassNotFoundException {
		client.putData("Name1", "Shashi Bhushan Yadav");
		client.putData("Name2", "Priyanka Salla");
		client.putData("Name3", "Dhruvil Jhala");
		client.putData("Name4", "Pratyusha Parashar");
		client.putData("Name5", "Amritanj Ayush");

		client.getData("Name1");
		client.getData("Name2");
		client.getData("Name3");
		client.getData("Name4");
		client.getData("Name5");

		client.deleteData("Name1");
		client.deleteData("Name2");
		client.deleteData("Name3");
		client.deleteData("Name4");
		client.deleteData("Name5");

		client.getData("Name1");
		client.getData("Name2");
		client.getData("Name3");
		client.getData("Name4");
		client.getData("Name5");
	}

	public static void printOptions() {
		System.out.println();
		System.out.println("Enter one of the code to perform actions");
		System.out.println("1. GET");
		System.out.println("2. PUT");
		System.out.println("3. DELETE");
		System.out.println("4. CLOSE SERVER and EXIT");
		System.out.println("5. EXIT");
		System.out.println();
	}

	private static void window(Client client) throws IOException, ClassNotFoundException {
		boolean loop = true;
		BufferedReader reader = null;
		while (loop) {
			reader = new BufferedReader(new InputStreamReader(System.in));
			ClientRun.printOptions();
			int action = Integer.parseInt(reader.readLine());
			String key = null, value = null, request = null;
			switch (action) {
			case 1:
				System.out.println("Enter key to fetch");
				request = reader.readLine();
				client.getData(request);
				break;
			case 2:
				System.out.println("Enter key");
				key = reader.readLine();
				System.out.println("Enter value");
				value = reader.readLine();
				client.putData(key, value);
				break;
			case 3:
				System.out.println("Enter key to delete");
				key = reader.readLine();
				client.deleteData(key);
				break;
			case 4:
				loop = false;
				client.stopServerCall();
				client.stopClient();
				break;
			default:
				loop = false;
				client.stopClient();
				break;
			}

		}
	}

	public static void run(String[] args) throws IOException, ClassNotFoundException {
		ConnectionType type = Enum.valueOf(ConnectionType.class, args[0]);
		String serverHost = (args.length >= 2) ? args[1] : null;
		int serverPort = (args.length >= 3) ? Integer.parseInt(args[2]) : -1;
		ClientConfig tcpCConfig = new ClientConfig();
		String clientHost = null;
		int clientPort = -1;
		if (type == ConnectionType.TCP) {
			
		} else if (type == ConnectionType.UDP && args.length > 3) {
			clientHost = (args.length >= 4) ? args[3] : null;
			clientPort = (args.length >= 5) ? Integer.parseInt(args[4]) : -1;
		}
		tcpCConfig.setServerPort(serverPort);
		tcpCConfig.setType(type);
		tcpCConfig.setServerHost(serverHost);
		tcpCConfig.setClientHost(clientHost);
		tcpCConfig.setClientPort(clientPort);
		tcpCConfig.setRequestTimeout(5000);
		Client client = new Client(tcpCConfig);
		client.startClient();
		ClientRun.dummyData(client);
		ClientRun.window(client);
	}

}
