package aos.project.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import aos.project.common.ChunkMeta;
import aos.project.common.Host;

public class Server implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static int sPort;
	public static int serverId;
	private static int M = 3;
	private static TreeMap<Integer, Host> serverIdPortMap;
	public static TreeMap<Integer, PrintWriter> sServerOutStream;

	public static ServerSocket sListener = null;

	public static Socket serverToMSoc = null;
	public static ObjectInputStream is_SM_ob = null;
	public static ObjectOutputStream os_SM_ob = null;

	public static int mServerPort = 4043;
	//public static String mServerAddr = "localhost";
	public static String mServerAddr = "dc03.utdallas.edu";
	public static volatile ConcurrentHashMap<String, List<ChunkMeta>> serverMeta;
	private static Thread t;
	private static Thread t1;
	private static String FLAG;

	public Server(int serverId) {
		Server.serverId = serverId;
		Server.sPort = serverIdPortMap.get(serverId).port;
	    
	}

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);

		serverIdPortMap = new TreeMap<>();
		sServerOutStream = new TreeMap<>();
		serverMeta = new ConcurrentHashMap<>();
		Scanner scanner_server = null;
		try {
			/*scanner_server = new Scanner(
					new File(
							"C:\\Users\\harpritc\\workspace\\DFS\\config\\server_config.txt"));
			*/
			//linux
			scanner_server = new Scanner(
					new File( "/home/012/h/hs/hsc160030/AOS_2/DFS/config/server_config_linux.txt"));
			for (int i = 0; i < M; i++) {
				String line = scanner_server.nextLine().trim();
				String[] parts = line.split("\\s+");
				serverId = Integer.parseInt(parts[0]);
				serverIdPortMap.put(serverId,
						new Host(parts[1], Integer.parseInt(parts[2])));
			}
			scanner_server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Server server = new Server(Integer.parseInt(args[0]));
		// Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		ServerListener sL = null;
		while (true) {
			System.out.println("enter flag");
			FLAG = sc.nextLine();

			switch (FLAG) {

			case "SERVER_UP": {

				sL = new ServerListener();
				// ServerListener.startRunning();
				t = new Thread(sL);
				t.setPriority(8);
				t.start();
				sendHeartBeat();
				break;
			}
			case "SERVER_DOWN": {
				// ServerListener.stopRunning();
				ServerThread.flag = false;
				HeartBeat.flag = false;
				/*
				 * try { sListener.close(); } catch (IOException e) {
				 * e.printStackTrace(); }
				 */
				break;
			}
			case "RESTART": {
				// ServerListener.startRunning();
				ServerThread.flag = true;
				HeartBeat.flag = true;
				break;
			}
			}

		}
	}

	public static void sendHeartBeat() {
		t1 = new Thread(new HeartBeat());
		t1.start();
	}

}
