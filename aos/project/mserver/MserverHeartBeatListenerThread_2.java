package aos.project.mserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import aos.project.common.ChunkMeta;

public class MserverHeartBeatListenerThread_2 implements Runnable,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ObjectInputStream isM_ob = null;
	private static int sIDown = 0;
	public static ConcurrentHashMap<String, List<ChunkMeta>> mapObj = new ConcurrentHashMap<>();
	
	public MserverHeartBeatListenerThread_2(ObjectInputStream isM_ob){
		this.isM_ob = isM_ob;
		
	}
	@Override
	public void run() {
		while (true) {
			Object inputObj = null;
			synchronized (MserverHeartBeatListenerThread_2.class){
				// System.out.println("server up");
				
				try {
					try {
						inputObj = isM_ob.readObject();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				if (inputObj == null) {
					break;
				}
			}
			
				// string
				if (inputObj instanceof String) {
					String input = (String) inputObj;
					String[] parts = input.split(":");
					switch (parts[1]) {
					case ("HEARTBEAT"): {
						System.out.println("input from client " + input
								+ " flag " + Mserver.flagMap);
						long timeUpdate = Long.parseLong(parts[0]);
						int sId = Integer.parseInt(parts[2]);
						Mserver.serverIdUpdateTimeMap.put(sId, timeUpdate);
						break;
					}

					case ("SERVER_DOWN"): {
						// System.out.println("input from client " + input +
						// " flag "+ Mserver.flag);
						if (checkIsServerDown()) {
							//System.out.println("server down MAP " + Mserver.flagMap);
							Mserver.flagMap.put(sIDown, false);
						}
						
						break;
					}

					case ("SERVER_UP"): {
						// System.out.println("input from client " + input +
						// " flag "+ Mserver.flag);
						
						if(!checkIsServerDown()){
							Mserver.flagMap.put(sIDown, true);
						}
						
						//System.out.println("server up...");
						/*
						try {
							Thread.currentThread().sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						*/
						break;
						
					}
					}
				} else if (inputObj instanceof HashMap) {
					System.out.println("input obj map " + inputObj);
					mapObj = (ConcurrentHashMap<String, List<ChunkMeta>>) inputObj;
					System.out.println("input hashmap from client " + mapObj);
					
					for (String name : mapObj.keySet()) {
						System.out.println("before changing hashmap mserver  " + Mserver.fileNameMeta.keySet() + 
								" values "  ) ;
						for(int i=0;i<  Mserver.fileNameMeta.get(name).size();i++){
							System.out.println(Mserver.fileNameMeta.get(name).get(i).chunkName);
						}
						for (ChunkMeta i : mapObj.get(name)) {
							Mserver.fileNameMeta.get(name).set(i.chId, i);
							System.out.println("chunk id  " + i.chId);
						}
						System.out.println("after changing hashmap mserver  " + Mserver.fileNameMeta.keySet() + 
								" values "  );
						for(int i=0;i<  Mserver.fileNameMeta.get(name).size();i++){
							System.out.println(Mserver.fileNameMeta.get(name).get(i).chunkName);
						}
					}
					
				}		
		}
	}
	
	private boolean checkIsServerDown() {

		long initTime = System.currentTimeMillis() / 1000L;
		for (int i = 0; i < Mserver.M; i++) {
			// System.out.println("checking diff " +
			// (Mserver.serverIdUpdateTimeMap.get(i) - initTime));
			if (initTime - Mserver.serverIdUpdateTimeMap.get(i) >= 15) {
				sIDown = i;
				//System.out.println("server" + i + " is down");
				return true;

			}
		}
		return false;
	}

}
