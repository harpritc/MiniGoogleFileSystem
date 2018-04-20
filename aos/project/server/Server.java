package aos.project.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
	public static Socket serverToServer = null;
	public static ObjectOutputStream dout = null;
	public static ObjectInputStream din = null;
	public static TreeMap<Integer, ObjectOutputStream> serverOutputStreams;
	public static TreeMap<Integer, ObjectInputStream> serverInputStreams;
	public static ObjectInputStream is_SM_ob = null;
	public static ObjectOutputStream os_SM_ob = null;
	private static List<Integer> other_serverIds;

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
		serverOutputStreams = new TreeMap<>();
		serverInputStreams =  new TreeMap<>();;
		other_serverIds = new ArrayList<>();
		serverMeta = new ConcurrentHashMap<>();
		
		Scanner scanner_server = null;
		Server server = null;
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
				int serId = Integer.parseInt(parts[0]);
				serverIdPortMap.put(serId,
						new Host(parts[1], Integer.parseInt(parts[2])));
			}
			server = new Server(Integer.parseInt(args[0]));
			for (int i = 0; i < M; i++) {
				System.out.println("other server ids " + serverId + " i " + i );
				if(i != serverId){
					System.out.println("adding");
					other_serverIds.add(i);
				}
				
			}
			
			scanner_server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
				server.sendHeartBeat();
				//server.initConnection();
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
				server.copy();
				break;
			}
			}

		}
	}
	
	public void initConnection(){
		 boolean scanning = true;
			System.out.println("third server id " + serverId);
			System.out.println(other_serverIds);
			
			for(int sid : other_serverIds){
				while(scanning){
					String recvIp = serverIdPortMap.get(sid).hostName;
					int recvport = serverIdPortMap.get(sid).port;
					try{
						//System.out.println(" establish connections with" + recvIp + " " + recvport + "and trying again");
						serverToServer = new Socket(recvIp,recvport);
						
						scanning = false;
	  				}catch (UnknownHostException e){
	  					  e.printStackTrace();
	  			    } catch (IOException e) {
	  			    }
				}
				try{
					dout =  new ObjectOutputStream(serverToServer.getOutputStream());
					dout.flush();
					din =  new ObjectInputStream(serverToServer.getInputStream());
					
	    			serverOutputStreams.put(sid,dout); // stream associated with server id
	    			serverInputStreams.put(sid, din);
	    			System.out.println("streams " + serverOutputStreams);
	    			System.out.println("streams in" + serverInputStreams);
	    			scanning = true;
	    			System.out.println("scanning " + scanning);
				}catch(IOException  e){
					e.printStackTrace();
				}
			}
			
	}
	public void sendHeartBeat() {
		t1 = new Thread(new HeartBeat());
		t1.start();
	}
	
	public void copy() {
		List<ChunkMeta> tempList = new ArrayList<>();
		List<Integer> other_sIdList = new ArrayList<>();
		for(Map.Entry<String,List<ChunkMeta>> fileMeta : serverMeta.entrySet()){
			String fileName = fileMeta.getKey();
			tempList = fileMeta.getValue();
			int ind = tempList.size();
			int chId = tempList.get(ind-1).chId;
			for(int sid:tempList.get(ind-1).sIdList){
				if(sid != serverId){
					other_sIdList.add(sid);
				}
			}
			int randSid = other_sIdList.get(new Random().nextInt(other_sIdList.size()));
			
				System.out.println("sending message for copying ");
				String fileContent = "";
				/*
				System.out.println("streams before " + serverOutputStreams);
				System.out.println("streams in before " + serverInputStreams);
				serverOutputStreams.get(randSid).writeObject("COPY:" + fileName + ":" + randSid + ":" + chId);
				serverOutputStreams.get(randSid).flush();
				*/
				String recvIp = serverIdPortMap.get(randSid).hostName;
				int recvport = serverIdPortMap.get(randSid).port;
				try{
					//System.out.println(" establish connections with" + recvIp + " " + recvport + "and trying again");
					serverToServer = new Socket(recvIp,recvport);
					
  				}catch (UnknownHostException e){
  					  e.printStackTrace();
  			    } catch (IOException e) {
  			    }
			
			try{
				dout =  new ObjectOutputStream(serverToServer.getOutputStream());
				dout.flush();
				din =  new ObjectInputStream(serverToServer.getInputStream());
				System.out.println("reading copied file");
				dout.writeObject("COPY:" + fileName + ":" + randSid + ":" + chId);
				dout.flush();
				Thread.sleep(1000);
				
				//fileContent = (String) serverInputStreams.get(randSid).readObject();
				fileContent = (String) din.readObject();
				System.out.println("reading done");
				String chunkName = tempList.get(ind-1).chunkName;
				FileWriter fw = new FileWriter(
						"/home/012/h/hs/hsc160030/AOS_2/DFS_2/Server_"
								+ serverId + "/" + chunkName);
				
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw);
				out.println(fileContent);
				System.out.println("copying complete");
				out.flush();
				out.close();
			} catch (ClassNotFoundException | IOException | InterruptedException e) {
				e.printStackTrace();
			} 
		}
	}

}
