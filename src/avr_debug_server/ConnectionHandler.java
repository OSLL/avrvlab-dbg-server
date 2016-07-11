package avr_debug_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class ConnectionHandler extends Thread{
	
	private Socket socket;
	private int port;
	private DeviceDispatcher deviceDispatcher;
	private ServerSocket server;
	
	public ConnectionHandler(int port, DeviceDispatcher deviceDispatcher) {
		this.port = port;
		this.deviceDispatcher = deviceDispatcher;
	}
	
	public void run() {
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Error starting server socket");
			System.exit(1);
		}
		while(true){
			try {
				if(isInterrupted()){
					server.close();
					return;
				}
				socket = server.accept();
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Message message = Messenger.readMessage(socket);
							if(message == null)
								throw new IOException();
							switch(message.getText()){
							case "LOAD":
								deviceDispatcher.handleNewRequest(socket);
								break;
							}
						} catch (IOException e) {
							System.err.println("Error communication with connected client");
						}
					}
				});
				thread.start();
			} catch (IOException e) {
				System.err.println("Error closing socket");
				continue;
			} 
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		if(!server.isClosed())
			server.close();
		super.finalize();
	}
	
}
