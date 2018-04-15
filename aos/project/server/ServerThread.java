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
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import aos.project.common.ChunkMeta;

public class ServerThread implements Runnable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Socket sConnection = null;
	private ObjectInputStream isS_ob = null;
	private ObjectOutputStream osS_ob = null;

	private final static int CHUNKSIZE = 8192;
	private List<ChunkMeta> tempArrListAppend;
	public static List<ChunkMeta> chunkListServer;
	public static boolean flag = true;

	public ServerThread(Socket sConnection) {
		this.sConnection = sConnection;
	}

	private String buffer(String character, int size) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(character);
		}
		return sb.toString();
	}

	public void stopRunning() {
		flag = false;
		System.out.println("stopping thread");
		// sT.stopRunning();
	}

	public void startRunning() {
		flag = true;
		// sT.startRunning();
	}

	@Override
	public void run() {
		System.out.println("Server Thread listening , flag " + flag);
		try {
			osS_ob = new ObjectOutputStream(sConnection.getOutputStream());
			osS_ob.flush();
			isS_ob = new ObjectInputStream(sConnection.getInputStream());

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		while (true) {
			// System.out.println("Server Thread running, flag " + flag);
			try {
				if (flag) {

					System.out.println("reading input , flag " + flag);
					String input = null;
					try {
						input = (String) isS_ob.readObject();
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					}

					// input = isS.readLine();

					if (input == null) {
						break;
					}
					System.out.println("input to Server " + input);

					String[] parts = input.split(":");
							// "Mserver:CREATE:" + serverIdlist+ fileName + chId
					if (input.contains("CREATE")) {
						if ((Server.serverMeta.containsKey(parts[3]))
								&& Integer.parseInt(parts[4]) == 0) {
							System.out
									.println("File already exist, Create file with different name");
							osS_ob.writeObject(" From Server: File name already exist");
							osS_ob.flush();
						} 
						 else {
							 if (Server.serverMeta.containsKey(parts[3])) {
								 chunkListServer = new ArrayList<>();
								 chunkListServer = Server.serverMeta.get(parts[3]);
								 System.out.println("chunkListServer " + chunkListServer);
							 }else{
								 chunkListServer = new ArrayList<>();
							 }
						
							 List<String> serverList = new ArrayList<String>(Arrays.asList(
									 parts[2].split(",")));
							 List<Integer> serverIdList = new ArrayList<>();
							 for(String s:serverList ){
								 serverIdList.add(Integer.valueOf(s));
							 }
							// chunkListServer =
							// Server.serverMeta.get(parts[3]);
							// ChunkMeta : filename,chId,serverId,size
							System.out.println("server_meta BEFORE " + Server.serverMeta);
							
							chunkListServer.add(new ChunkMeta(parts[3], Integer
									.parseInt(parts[4]), serverIdList, 0));
							Server.serverMeta.put(parts[3], chunkListServer);
							System.out.println("server_meta " + Server.serverMeta);
							/*File file_1 = new File(
									"C:\\Users\\harpritc\\workspace\\DFS\\Server_"
											+ parts[2]
											+ "\\"
											+ Server.serverMeta
													.get(parts[3])
													.get(chunkListServer.size()-1).chunkName);
							*/
							String chunkName = "";
							for(int i=0;i<Server.serverMeta.get(parts[3]).size();i++){
								if(Integer.parseInt(parts[4]) == Server.serverMeta.get(parts[3]).get(i).chId){
									chunkName = Server.serverMeta.get(
										parts[3]).get(i).chunkName;
								}
							}
							File file_1 = new File(
									"/home/012/h/hs/hsc160030/AOS_2/DFS/Server_"
											+ Server.serverId
											+ "/"
											+ chunkName);
							
							if (file_1.createNewFile()) {
								System.out.println("file created");
							} else {
								System.out.println("file not created");
							}
							//osS_ob.writeObject(" From Server: Created file in Server Id " + Server.serverId);
							//osS_ob.flush();
					}
				}
					
					//Mserver:PADNULL:" + serverId + ":" + fileName
					if(input.contains("PADNULL")){
						tempArrListAppend = new ArrayList<>();
						tempArrListAppend = Server.serverMeta
								.get(parts[3]);
						int S = Server.serverMeta.get(parts[3]).get(
								tempArrListAppend.size() - 1).size;
								System.out.println("before .....padding ................");
						if (S < CHUNKSIZE) {
							System.out.println("padding ................");
							String chunkName = Server.serverMeta.get(
									parts[3]).get(
									tempArrListAppend.size() - 1).chunkName;
							System.out.println(	"chunkName " + chunkName);
							FileWriter fw = new FileWriter(
									"/home/012/h/hs/hsc160030/AOS_2/DFS/Server_"
											+ parts[2] + "/"
											+ chunkName, true);
							
							
							/*FileWriter fw = new FileWriter(
									"/home/012/h/hs/hsc160030/AOS_2/DFS/Server_"
											+ parts[2] + "/"
											+ chunkName, true);*/
							BufferedWriter bw = new BufferedWriter(fw);
							PrintWriter out = new PrintWriter(bw);
							String buf = buffer("\0", CHUNKSIZE - S);
							System.out.println("buf " + buf);
							out.println(buf);
							out.flush();
							out.close();
						}
					}
					// clientId + ":" + serverId + ":" + command + ":" +
					// fileName + ":" + buffer
					if (input.contains("APPEND")) {
						if (!(Server.serverMeta.containsKey(parts[3]))) {
							System.out
									.println("File does not exist, Need to create file. Can not append");
							osS_ob.writeObject(" From Server: File does not exist, Need to create file. Can not append");
							osS_ob.flush();
						} else {
							try {
								osS_ob.writeObject(" From Server: append");
								osS_ob.flush();
								tempArrListAppend = new ArrayList<>();
								tempArrListAppend = Server.serverMeta
										.get(parts[3]);
								Server.serverMeta.get(parts[3]).get(
										tempArrListAppend.size() - 1).size += parts[4]
										.length();
								String chunkName = Server.serverMeta.get(
										parts[3]).get(
										tempArrListAppend.size() - 1).chunkName;
								/*
								FileWriter fw = new FileWriter(
										"C:\\Users\\harpritc\\workspace\\DFS\\Server_"
												+ parts[1] + "\\" + chunkName,
										true);
								*/
								FileWriter fw = new FileWriter(
										"/home/012/h/hs/hsc160030/AOS_2/DFS/Server_"
												+ parts[1] + "/" + chunkName,
										true);
								
								BufferedWriter bw = new BufferedWriter(fw);
								PrintWriter out = new PrintWriter(bw);
								out.println(parts[4]);
								out.flush();
								out.close();
								System.out.println("sending message to client");
								osS_ob.writeObject(" From Server: appended file Server id  " + Server.serverId);
								osS_ob.flush();
							} catch (IOException e) {
								e.printStackTrace();
							}

						}

					}

					// clientId + ":" + serverId + ":" + command + ":" +
					// fileName + ":" + startByte + ":" + byteCount + ":" +
					// chunkNum
					if (input.contains("READ")) {
						if (!(Server.serverMeta.containsKey(parts[3]))) {
							System.out
									.println("File does not exist, Need to create file. Can not read");
							osS_ob.writeObject(" From Server: File does not exist, Need to create file. Can not read");
							osS_ob.flush();
						} else {
							try {
								byte[] b = new byte[Integer.parseInt(parts[5])];
								String chunkName = "";
								for(int i=0;i<Server.serverMeta.get(parts[3]).size();i++){
										if(Integer.parseInt(parts[6]) == Server.serverMeta.get(parts[3]).get(i).chId){
											chunkName = Server.serverMeta.get(
												parts[3]).get(i).chunkName;
										}
								}
								
								/*
								RandomAccessFile raf = new RandomAccessFile(
										"C:\\Users\\harpritc\\workspace\\DFS\\Server_"
												+ parts[1] + "\\" + chunkName,
										"rw");
								*/
								RandomAccessFile raf = new RandomAccessFile(
										"/home/012/h/hs/hsc160030/AOS_2/DFS/Server_"
												+ parts[1] + "/" + chunkName,
										"rw");
								raf.seek(Integer.parseInt(parts[4]) - 1);
								raf.readFully(b);
								String s = new String(b);
								System.out.println("reading from server " + s);
								System.out
										.println("sending message to client read");
								osS_ob.writeObject(" From Server: reading file "
										+ s +  " Server id  " + Server.serverId);
								osS_ob.flush();
							} catch (IOException e) {
								e.printStackTrace();
							}

						}
					}
				}

			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
	}

}
