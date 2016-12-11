package avrdebug.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import avrdebug.communication.Message;
import avrdebug.communication.Messenger;
import avrdebug.communication.SimulAVRInitData;
import avrdebug.configs.AppConfigs;
import avrdebug.reservation.ReservationErrorResponse;
import avrdebug.reservation.ReservationInfo;
import avrdebug.reservation.ReservationResponse;
import avrdebug.reservation.ReservationSuccessResponse;
import avrdebug.reservation.ReservationSystemConnector;

class ConnectionHandler extends Thread{
	private static final String simulatorDeviceNick = "Virtual";
	private static final String mcuDeviceNick = "Real";
	private Socket socket;
	private int port;
	private DeviceDispatcher deviceDispatcher;
	private SimulatorDispatcher simulatorDispatcher;
	private ServerSocket server;
	private ReservationSystemConnector reservationSystem;
	
	public ConnectionHandler(int port, DeviceDispatcher deviceDispatcher, SimulatorDispatcher simulatorDispatcher) {
		this.port = port;
		this.deviceDispatcher = deviceDispatcher;
		this.simulatorDispatcher = simulatorDispatcher;
		reservationSystem = new ReservationSystemConnector(AppConfigs.getProperty("reservation.api.address"));
	}
	
	public void run() {
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Error starting server socket: " + e.getMessage());
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
							String command = message.getText();
							switch(command){
							case "DEBUG_MCU":
							case "DEBUG_SIMUL":
								//client wants debug
								ReservationInfo reserveInfo = null;
								//get client's token
								message = Messenger.readMessage(socket);
								if(message==null)
									break;
								//check token
								ReservationResponse reserveResp = reservationSystem.getReserveInfo(message.getText());
								if(reserveResp instanceof ReservationErrorResponse){
									Messenger.writeMessage(socket, new Message("Wrong token",-1));
									socket.close();
									break;
								}
								//if token correct
								if(reserveResp instanceof ReservationSuccessResponse){
									reserveInfo = ((ReservationSuccessResponse)reserveResp).getReservationInfo();
									//what target client wants
									switch (command) {
									case "DEBUG_MCU":
										if(mcuDeviceNick.equals(reserveInfo.getResourceType())){ //if user's target same reserved target
											deviceDispatcher.handleNewRequest(socket, reserveInfo);
										}
										else{
											Messenger.writeMessage(socket, new Message("Wrong target. Check selected target. Token target: " + reserveInfo.getResourceType(),-2));
										}
										break;
									case "DEBUG_SIMUL":
										if(simulatorDeviceNick.equals(reserveInfo.getResourceType())){//if user's target same reserved target
											simulatorDispatcher.handleNewRequest(socket, reserveInfo);
										}
										else{
											Messenger.writeMessage(socket, new Message("Wrong target. Check selected target. Token target: " + reserveInfo.getResourceType(),-3));
										}										
										break;
									default:
										socket.close();
										break;
									}
								}
								break;


//							case "LOAD":
//								deviceDispatcher.handleNewRequest(socket);
//								break;
//							case "SIMUL":
//								System.out.println("GET: SIMUL");
//								simulatorDispatcher.handleNewRequest(socket);
//								break;
							case "GET_INIT_SIMUL_CONFIG":
								try {
									Messenger.writeSimulAVRInitData(socket, SimulAVR.getInitData());
								} catch (Exception e) {
									SimulAVRInitData data = new SimulAVRInitData();
									data.setMcuVCDSources(null);
									Messenger.writeSimulAVRInitData(socket, data);
								}
								break;
							default:
								System.out.println("Wrong Message");
								socket.close();
								break;
							}
						} catch (IOException e) {
							System.err.println("Error communication with connected client");
						}
						
					}

				});
				thread.start();
			} catch (IOException e) {
				System.err.println("Error closing socket: " + e.getMessage());
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
