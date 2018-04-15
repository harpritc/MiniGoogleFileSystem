package aos.project.client;

import java.io.IOException;
import java.io.Serializable;

public class ClientListenerServer implements Runnable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void run() {
		System.out.println("client listening server.. ");
		try {
			while (true) {
				// String input = Client.isM.readLine();
				String input_ser = null;
				/*
				for(int i=0;i<Client.cServerInStream.size();i++){
					
					try {
						input_ser = (String) Client.cServerInStream.get(i).readObject();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				*/
					try {
						input_ser = (String) (String) Client.isS_ob.readObject();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					// System.out.println("input from M server " + input);
					System.out.println("input from normal server " + input_ser);
					
				
				if (input_ser == null) {
					break;
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
