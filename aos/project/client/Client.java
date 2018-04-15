package aos.project.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;

import aos.project.common.Host;

public class Client implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static int N = 2; // number of clients
	private static int M = 3; // Number of servers
	private static int clientId;
	private static int serverId;
	private static int port;
	private static int mServerPort = 4043;
	private static int NUMREPLICA = 2;
	//private static String mServerAddr = "localhost";
	public static String mServerAddr = "dc03.utdallas.edu";
	private static TreeMap<Integer, Host> clientIdPortMap;
	private static TreeMap<Integer, Host> serverIdPortMap;
	private static List<Integer> serverIds_list;
	private static TreeMap<Integer, ObjectOutputStream> cServerOutStream;
	public static TreeMap<Integer, ObjectInputStream> cServerInStream;

	public static Socket clientSocketM = null; // m server
	private static Socket clientSocketS = null; // normal server
	private static String buffer;

	public static ObjectInputStream isM_ob = null; // input stream Mserver
	public static ObjectOutputStream osM_ob = null; // output Stream M server

	public static ObjectInputStream isS_ob = null; // input stream normal server
	private static ObjectOutputStream osS_ob = null; // output stream normal
														// server

	public Client(int clientId) {
		Client.clientId = clientId;
		Client.port = clientIdPortMap.get(clientId).port;

	}

	public static void main(String[] args) {

		if (args.length < 1) {
			System.out.println("Please pass client id as argument");
		}
		readConfig();
		Client client = new Client(Integer.parseInt(args[0]));
		client.createConnection();
		//String fileName = "test";
		int chId = 0;
		Scanner sc = new Scanner(System.in);
		//new Thread(new ClientListener()).start();
		
			while (true) {
			System.out.println("Enter Command");
			String command = sc.nextLine().trim();
			String[] parts = command.split(" ");
			switch (parts[0]) {
			case "CREATE": {
				if(parts.length < 2){
					System.out.println("SYNTAX : CREATE FILENAME");
					break;
				}
				String fileName = parts[1];
				client.createFile(fileName, chId);
				break;
			}
			case "APPEND": {
				if(parts.length < 3){
					System.out.println("SYNTAX : APPEND FILENAME NoOfBytes");
					break;
				}
				String fileName = parts[1];
				int bytesToAppend = Integer.parseInt(parts[2]);
				client.appendFile(fileName,bytesToAppend);
				break;
			}
			case "READ": {
				if(parts.length < 4){
					System.out.println("SYNTAX : READ FILENAME STARTBYTE NoOfBytes");
					break;
				}
				String fileName = parts[1];
				int offset = Integer.parseInt(parts[2]);
				int bytesToRead = Integer.parseInt(parts[3]);
				client.readFile(fileName, offset, bytesToRead);
				break;
			}
			}
		}
	}

	/*
	 * reading client config file mapping client id with Ip and port
	 */
	private static void readConfig() {
		System.out.println("reading config ... ");
		clientIdPortMap = new TreeMap<>();
		serverIdPortMap = new TreeMap<>();
		Scanner scanner_client = null;
		Scanner scanner_server = null;
		try {
			/*
			scanner_client = new Scanner(
					new File(
							"C:\\Users\\harpritc\\workspace\\DFS\\config\\client_config.txt"));
			scanner_server = new Scanner(
					new File(
							"C:\\Users\\harpritc\\workspace\\DFS\\config\\server_config.txt"));
			*/
			//linux
			scanner_client = new Scanner(
					new File( "/home/012/h/hs/hsc160030/AOS_2/DFS/config/client_config_linux.txt"));
			scanner_server = new Scanner(
					new File( "/home/012/h/hs/hsc160030/AOS_2/DFS/config/server_config_linux.txt"));
		} catch (FileNotFoundException e) {
			System.out.println("file not found");
			e.printStackTrace();
		}
		for (int i = 0; i < N; i++) {
			String line = scanner_client.nextLine().trim();
			String[] parts = line.split("\\s+");
			clientId = Integer.parseInt(parts[0]);
			clientIdPortMap.put(clientId,
					new Host(parts[1], Integer.parseInt(parts[2])));
		}
		scanner_client.close();
		/*
		 * reading server config file mapping server id with Ip and port
		 */
		serverIds_list = new ArrayList<>();
		for (int i = 0; i < M; i++) {
			String line = scanner_server.nextLine().trim();
			String[] parts = line.split("\\s+");
			serverId = Integer.parseInt(parts[0]);
			serverIdPortMap.put(serverId,
					new Host(parts[1], Integer.parseInt(parts[2])));
			serverIds_list.add(serverId);
		}
		scanner_server.close();
	}

	/*
	 * creating connection
	 */
	private void createConnection() {

		// create socket connection with M server
		System.out.println("creating connection ....");
		cServerOutStream = new TreeMap<>();
		cServerInStream = new TreeMap<>();
		// create connection with servers
		/*
		try {
			for (int i = 0; i < M; i++) {
				String sIp = serverIdPortMap.get(i).hostName;
				int sPort = serverIdPortMap.get(i).port;
				clientSocketS = new Socket(sIp, sPort);
				osS_ob = new ObjectOutputStream(clientSocketS.getOutputStream());
				osS_ob.flush();
				isS_ob = new ObjectInputStream(clientSocketS.getInputStream());
				cServerInStream.put(i, isS_ob);
				cServerOutStream.put(i, osS_ob);
			}
		} catch (UnknownHostException e) {
			System.err.println("Don't know about normal server host");
		} catch (IOException e) {
			e.printStackTrace();
			System.out
					.println("Errror in creating connection with normal server");
		}
		*/
		
		try {
			clientSocketM = new Socket(mServerAddr, mServerPort);
			osM_ob = new ObjectOutputStream(clientSocketM.getOutputStream());
			osM_ob.flush();
			isM_ob = new ObjectInputStream(clientSocketM.getInputStream());

		} catch (UnknownHostException e) {
			System.err.println("Don't know about m server host");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Errror in creating connection with m server");
		}
		
	}

	
	// CONNECTION WITH SERVER
	private static void severConnection(int serverId){
		try{
			String sIp = serverIdPortMap.get(serverId).hostName;
			int sPort = serverIdPortMap.get(serverId).port;
			clientSocketS = new Socket(sIp, sPort);
			osS_ob = new ObjectOutputStream(clientSocketS.getOutputStream());
			osS_ob.flush();
			isS_ob = new ObjectInputStream(clientSocketS.getInputStream());
			//cServerInStream.put(serverId, isS_ob);
			cServerOutStream.put(serverId, osS_ob);
		}
		catch (UnknownHostException e) {
		    System.err.println("Don't know about normal server host");
		}
		catch(IOException e){
			e.printStackTrace();
			System.out.println("Errror in creating connection with normal server");
		}
	}
	
	private static void mServerConnection(){
		try {
			clientSocketM = new Socket(mServerAddr, mServerPort);
			osM_ob = new ObjectOutputStream(clientSocketM.getOutputStream());
			osM_ob.flush();
			//isM_ob = new ObjectInputStream(clientSocketM.getInputStream());

		} catch (UnknownHostException e) {
			System.err.println("Don't know about m server host");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Errror in creating connection with m server");
		}
	}
	
	/*
	 * creating file
	 */
	private void createFile(String fileName, int chId) {
		//if (clientSocketM != null && isM_ob != null && osM_ob != null) {
			System.out.println("creating File ...");
			try {
				// new Thread(new ClientListener()).start();
				sendMessageCreate("CREATE", fileName, chId);
				Thread.sleep(1000);
				

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		//}
	}

	/*
	 * Appending file
	 */

	private String bufferfill(String character, int size) {
		StringBuilder sb = new StringBuilder();
		while (sb.toString().length() < size) {
			sb.append(character);
		}
		return sb.toString();
	}

	private void appendFile(String fileName,int size) {
		buffer = bufferfill("ABCDEFGHIJKLMNOPQRSTUVWXYZ", size);
		int byteCount = buffer.length();
		//if (clientSocketM != null && isM_ob != null && osM_ob != null) {
			System.out.println("appending File ...");
			try {
				// new Thread(new ClientListener()).start();
				sendMessageAppendMserver("APPEND", fileName, byteCount);
				System.out.println(" message send for append : waiting from m server");
				Thread.sleep(1000);
				for(int i=0;i<NUMREPLICA;i++){
					String input = null;
					try {
						 input = (String) isM_ob.readObject();
						 System.out.println(input);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					String[] parts = input.split(":");
					// append file in server
	
					// mserver:append:server id:filename
					if (input.contains("APPEND")) {
						Client.sendMessageAppendServer("APPEND",
								Integer.parseInt(parts[2]), parts[3]);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		//}
	}

	// read file
	private void readFile(String fileName, int startByte, int byteCount) {
		//if (clientSocketM != null  && osM_ob != null) {
			System.out.println("reading File ...");
			try {
				// new Thread(new ClientListener()).start();
				sendMessageReadMserver("READ", fileName, startByte, byteCount);
				Thread.sleep(1000);
				
				String input = null;
				try {
					 input = (String) isM_ob.readObject();
					 System.out.println(input);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				String[] parts = input.split(":");
				// mserver:read:server id:
				// startyByte:byteCount:chunkNum:filename
				if (input.contains("firstChunkRead") && input.contains("Mserver")) {
					Client.sendMessageReadServer("READ",
							Integer.parseInt(parts[2]),
							Integer.parseInt(parts[3]),
							Integer.parseInt(parts[4]),
							Integer.parseInt(parts[5]), parts[6]);
					
					
					try {
						 input = (String) isM_ob.readObject();
						 System.out.println(input);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					 parts = input.split(":");
				}
				
				//if (input.contains("READ") && input.contains("Mserver")) {
					Client.sendMessageReadServer("READ",
							Integer.parseInt(parts[2]),
							Integer.parseInt(parts[3]),
							Integer.parseInt(parts[4]),
							Integer.parseInt(parts[5]), parts[6]);
				//}
				



			} catch (ArrayIndexOutOfBoundsException e1) {
				System.out.println("...Server is Down ....");
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		//}
	}

	// create
	private void sendMessageCreate(String command, String fileName, int chId) {
		if (command.equals("CREATE")) {
			//mServerConnection();
			System.out.println("sending create message ...");
			try {
				osM_ob.writeObject(clientId + ":" + command + ":" + fileName
						+ ":" + chId);
				osM_ob.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	// append

	private static void sendMessageAppendMserver(String command,
			String fileName, int byteCount) {
		if (command.equals("APPEND")) {
			//mServerConnection();
			System.out.println("sending append message ...");
			try {
				osM_ob.writeObject(clientId + ":" + command + ":" + fileName
						+ ":" + byteCount);
				osM_ob.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	public static void sendMessageAppendServer(String command, int serverId,
			String fileName) {
		if (command.equals("APPEND")) {
			System.out.println("sending append message ...");
			 severConnection(serverId);
			// new Thread(new ClientListenerServer()).start();
			try {
				cServerOutStream.get(serverId).writeObject(clientId + ":" + serverId + ":" + command
						+ ":" + fileName + ":" + buffer);
				cServerOutStream.get(serverId).flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String input_ser = null;
			try {
				input_ser = (String) (String) isS_ob.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("input from normal server " + input_ser);

		}

	}

	// read
	private void sendMessageReadMserver(String command, String fileName,
			int startByte, int byteCount) {
		//mServerConnection();
		System.out.println("sending read message ...");
		try {
			osM_ob.writeObject(clientId + ":" + command + ":" + fileName + ":"
					+ startByte + ":" + byteCount);
			osM_ob.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void sendMessageReadServer(String command, int serverId,
			int startByte, int byteCount, int chunkNum, String fileName) {

		System.out.println("sending read message ...");
		 severConnection(serverId);
		// new Thread(new ClientListenerServer()).start();
		

		try {
			cServerOutStream.get(serverId).writeObject(clientId + ":" + serverId + ":" + command + ":"
					+ fileName + ":" + startByte + ":" + byteCount + ":"
					+ chunkNum);
			cServerOutStream.get(serverId).flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String input_ser = null;
		try {
			input_ser = (String) (String) isS_ob.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("input from normal server " + input_ser);


	}

	public int getPort() {
		return Client.port;
	}

	/*
	 * listener thread
	 */

}
