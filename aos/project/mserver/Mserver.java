package aos.project.mserver;

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
import java.net.SocketException;
import java.rmi.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;

import aos.project.common.ChunkMeta;
import aos.project.common.Host;

public class Mserver implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static int CHUNKSIZE = 8192;
	private static ServerSocket mServerListener = null;

	private static int mServerPort = 4043;
	public static int M = 3; // servers
	private static int NUMREPLICA = 2;
	private static int serverId;
	public static boolean flag_create_file = false;

	public static ObjectInputStream m_isS_ob = null;
	public static ObjectOutputStream m_osS_ob = null;

	public static Socket mSocketS = null;
	public static TreeMap<Integer, ObjectOutputStream> mServerOutStream;
	public static TreeMap<ObjectInputStream, Integer> misSserverIdMap;
	private static TreeMap<Integer, Host> serverIdPortMap;
	public static List<Integer> serverIds_list;
	public static TreeMap<Integer, Long> serverIdUpdateTimeMap;

	public static HashMap<String, List<ChunkMeta>> fileNameMeta;
	private static List<ChunkMeta> chunkListMserver;
	private static List<ChunkMeta> temp_arList;
	public static ConcurrentHashMap<Integer,Boolean> flagMap ;

	public static void main(String[] args) {
		flagMap = new ConcurrentHashMap<>();
		createConnection();
		System.out.println("CONNECTION DONE");
		fileNameMeta = new HashMap<>();
	//	chunkListMserver = new ArrayList<>();
		new Thread(new MserverListener()).start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			mServerListener = new ServerSocket(mServerPort);
			System.out.println("creating heart beat thread");
			new Thread(new MserverHeartBeatListener()).start();
			while (true) {
				MServerThread sT;
				try {
					System.out.println("listening on port " + mServerPort);
					sT = new MServerThread(mServerListener.accept());
					Thread t = new Thread(sT);
					t.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				System.out.println("closing");
				m_isS_ob.close();
				m_osS_ob.close();
				mServerListener.close();
				mSocketS.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	/*
	 * creating connection with server
	 */
	private static void createConnection() {
		System.out.println("M server creating connection with servers");
		mServerOutStream = new TreeMap<>();
		serverIdPortMap = new TreeMap<>();
		serverIdUpdateTimeMap = new TreeMap<>();
		Scanner scanner_server = null;
		try {
			serverIds_list = Collections.synchronizedList(new ArrayList<Integer>());
			/*scanner_server = new Scanner(
					new File(
							"C:\\Users\\harpritc\\workspace\\DFS\\config\\server_config.txt"));
			*/
			//linux
			scanner_server = new Scanner(
					new File( "/home/012/h/hs/hsc160030/AOS_2/DFS_2/config/server_config_linux.txt"));
			for (int i = 0; i < M; i++) {
				String line = scanner_server.nextLine().trim();
				String[] parts = line.split("\\s+");
				serverId = Integer.parseInt(parts[0]);
				serverIdPortMap.put(serverId,
						new Host(parts[1], Integer.parseInt(parts[2])));
				serverIds_list.add(serverId);
				serverIdUpdateTimeMap
						.put(i, System.currentTimeMillis() / 1000L);
				flagMap.put(i, true);
			}
			scanner_server.close();
			reconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void reconnect() {
		try {
			for (int i = 0; i < M; i++) {
				String sIp = serverIdPortMap.get(i).hostName;
				int sPort = serverIdPortMap.get(i).port;
				System.out.println(sIp + " " + sPort);
				mSocketS = new Socket(sIp, sPort);
				System.out.println("connecting ....");
				// m_isS = new BufferedReader(new
				// InputStreamReader(mSocketS.getInputStream()));
				// m_osS = new PrintWriter(mSocketS.getOutputStream());
				m_osS_ob = new ObjectOutputStream(mSocketS.getOutputStream());
				m_osS_ob.flush();
				m_isS_ob = new ObjectInputStream(mSocketS.getInputStream());
				System.out.println("INPUT STREAM ....");

				System.out.println("OUTPUT STREAM ....");
				mServerOutStream.put(i, m_osS_ob);

				// misSserverIdMap.put(m_isS,i);
			}
		} catch (IOException e) {
			System.err.println("error in connecting");
			e.printStackTrace();
		}
	}

	public static void createFile(String fileName, int chId,ObjectOutputStream osM_ob) {

		if (mSocketS != null & m_isS_ob != null && m_osS_ob != null) {
			
			List<Integer> tempSIDList = new ArrayList<>(serverIds_list);
			for(int i=0;i<flagMap.size();i++){
				if(flagMap.get(i) == false){
					tempSIDList.remove(new Integer(i));
				}
			}
			
			Collections.shuffle(tempSIDList);
		 
		    List<Integer> randomServerIds = tempSIDList.subList(0, NUMREPLICA);
			
			if(chId == 0){
				chunkListMserver = new ArrayList<>();
			}
			System.out.println("sending message to server to create file");
			// fileName, chId, sId,size
			ChunkMeta ch = new ChunkMeta(fileName, chId, randomServerIds, 0);
			//chunkListMserver = new ArrayList<>();
			chunkListMserver.add(ch);
			fileNameMeta.put(fileName, chunkListMserver);
			// new Thread(new MserverListener()).start();
			sendMessageCreate(fileName, randomServerIds, chId);
			
			
		}

	}

	// Append

	public static synchronized void appendFile(String fileName, int byteCount,
			ObjectOutputStream osM_ob) {

		if (mSocketS != null & m_isS_ob != null && m_osS_ob != null) {
			temp_arList = new ArrayList<>();
			temp_arList = fileNameMeta.get(fileName);
			int servId=0;
					
			int S = fileNameMeta.get(fileName).get(temp_arList.size() - 1).size
					+ byteCount;
			System.out.println("........s........." + S);
			if (S > CHUNKSIZE) {
				for(int i=0;i<NUMREPLICA;i++){
					servId = fileNameMeta.get(fileName).get(temp_arList.size() - 1).sIdList.get(i);
					System.out.println("........Adding null to file........." + S);
					sendMessageAppendNull(fileName, servId);
					System.out.println("........creating new chunk........." + S);
					int chIdinc = fileNameMeta.get(fileName).get(
							temp_arList.size() - 1).chId;
					createFile(fileName, chIdinc + 1,osM_ob);
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
				temp_arList = fileNameMeta.get(fileName);
			}
			
			fileNameMeta.get(fileName).get(temp_arList.size() - 1).size += byteCount;
			String downOrUp = "up";
			for(int sid : fileNameMeta.get(fileName).get(temp_arList.size() - 1).sIdList){
				if(flagMap.get(sid) == false){
					downOrUp = "down";
				}
			}
			for(int sid : fileNameMeta.get(fileName).get(temp_arList.size() - 1).sIdList){
				if(flagMap.get(sid) == true){
					//servId = fileNameMeta.get(fileName).get(temp_arList.size() - 1).sIdList.get(i);
					
					System.out
							.println("sending message to client with meta info to append file");
					// new Thread(new Mserver()).start();
					sendMessageAppendClient(fileName, sid, osM_ob,downOrUp);
				}
				
			}
					
		}
			

	}
	
	// padding null to end of file// send to server
	private static void sendMessageAppendNull(String fileName, int serverId) {

		try {
			mServerOutStream.get(serverId).writeObject(
					"Mserver:PADNULL:" + serverId + ":" + fileName );
			mServerOutStream.get(serverId).flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out
				.println("message sent to server append null to file");
	}

	// read

	public static void readFile(String fileName, int startByte, int byteCount,
			ObjectOutputStream osM_ob) {
		
		
		
		if (mSocketS != null & m_isS_ob != null && m_osS_ob != null) {

			int sB = startByte % CHUNKSIZE;
			int chunkNum = startByte / CHUNKSIZE;
			List<Integer> tempSIdList = fileNameMeta.get(fileName).get(chunkNum).sIdList;
			
			for(int i:tempSIdList){
				if(flagMap.get(i) == false){
					tempSIdList.remove(new Integer(i));
				}else if(!tempSIdList.contains(i)){
					tempSIdList.add(i);
				}
			}
			int random_servId = tempSIdList.get(new Random().nextInt(tempSIdList.size()));
			//int servId = fileNameMeta.get(fileName).get(chunkNum).sIdList.get(0);
			
			if(sB + byteCount > CHUNKSIZE){
				int bytesFirstChunk = CHUNKSIZE - sB;
				sendMessageReadFirstChunkClient(fileName, random_servId, sB, bytesFirstChunk, chunkNum,
				osM_ob);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int bytesSecondChunk = byteCount - bytesFirstChunk;
				System.out.println("fileNameMeta " + fileNameMeta);
				int serverId = fileNameMeta.get(fileName).get(chunkNum+1).sIdList.get(0);
				int chIdd = fileNameMeta.get(fileName).get(chunkNum+1).chId;
				sendMessageReadClient(fileName, serverId, 1, bytesSecondChunk, chIdd,
						osM_ob);
				
			}else{
				System.out
				.println("sending message to client with meta info to read file");
				// new Thread(new Mserver()).start();
				sendMessageReadClient(fileName, random_servId, sB, byteCount, chunkNum,
						osM_ob);
			}
			
			
		}

	}

	// meta info to client
	private static void sendMessageReadClient(String fileName, int servId,
			int sB, int byteCount, int chunkNum, ObjectOutputStream osM_ob) {
		try {
			osM_ob.writeObject("Mserver:READ:" + servId + ":" + sB + ":"
					+ byteCount + ":" + chunkNum + ":" + fileName);
			osM_ob.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("message sent to server to read file");
	}
	
	// meta info to client for multiple chunk read
		private static void sendMessageReadFirstChunkClient(String fileName, int servId,
				int sB, int byteCount, int chunkNum, ObjectOutputStream osM_ob) {
			try {
				osM_ob.writeObject("Mserver:firstChunkRead:" + servId + ":" + sB + ":"
						+ byteCount + ":" + chunkNum + ":" + fileName);
				osM_ob.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("message sent to server to read to first chunk file");
		}
	// to server
	private static void sendMessageCreate(String fileName, List<Integer> serverIdList,
			int chId) {
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<serverIdList.size();i++){
			sb.append(serverIdList.get(i));
			sb.append(",");
		}
		
		for(int i=0;i<serverIdList.size();i++){
			try {
				mServerOutStream.get(serverIdList.get(i)).writeObject(
						"Mserver:CREATE:" + sb.toString() + ":" + fileName + ":" + chId);
				mServerOutStream.get(serverIdList.get(i)).flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("message sent to server to create file");
		}
		
	}

	// meta's info to client
	private static void sendMessageAppendClient(String fileName, int serverId,
			ObjectOutputStream osM,String flag) {

		try {
			osM.writeObject("Mserver:APPEND:" + serverId + ":" + fileName + ":" + flag);
			osM.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out
				.println("message sent to client with meta info to append file");
	}

}
