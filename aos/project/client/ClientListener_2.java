package aos.project.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class ClientListener_2 implements Runnable,Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ObjectInputStream isM_ob = null;
	public ClientListener_2(ObjectInputStream isM_ob){
		this.isM_ob = isM_ob;
	}
	@Override
	public void run() {
		System.out.println("client listening_2.. ");
		try {
			while (true) {
				String input = null;
				//synchronized (ClientListener.class){
					try {
						input = (String) isM_ob.readObject();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					System.out.println("input from M server " + input);
					// System.out.println("input from normal server " + input_ser);
					if (input == null) {
						break;
					}
				//}
					String[] parts = input.split(":");
					// append file in server
	
					// mserver:append:server id:filename
					if (input.contains("APPEND")) {
						Client.sendMessageAppendServer("APPEND",
								Integer.parseInt(parts[2]), parts[3]);
					}
					// mserver:read:server id:
					// startyByte:byteCount:chunkNum:filename
					if (input.contains("READ") && input.contains("Mserver")) {
						Client.sendMessageReadServer("READ",
								Integer.parseInt(parts[2]),
								Integer.parseInt(parts[3]),
								Integer.parseInt(parts[4]),
								Integer.parseInt(parts[5]), parts[6]);
					}
				
			}
			Client.osM_ob.close();
			ClientListener.isM_ob.close();
			Client.clientSocketM.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
