package avrdebug.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

import com.google.api.client.util.Sleeper;

import avrdebug.communication.Message;
import avrdebug.communication.Messenger;
import avrdebug.communication.SimulAVRConfigs;
import avrdebug.configs.AppConfigs;
import avrdebug.reservation.ReservationInfo;

public class Simulator implements SimulAVRListener {
	public static final String READY = "Ready";
	public static final String INUSE = "In use";
	private static int initialPort = Integer.parseInt(AppConfigs.getProperty("debug.initial_port"));
	private static File tempFolder = new File("simulator-temp-files"); 
	private int id; 	//index number of programmer-debugger
	private int port; 		//AVaRICE port which GDB connect  
	private ReservationInfo currentReserveInfo;
	private Socket currentClientSocket;
	private String sketchFilename;
	private String vcdTraceFilename;
	private String cpuTraceFilename;
	private SimulAVR simulAvr;
	private SimulAVRConfigs simulavrConfig;
	private String status;
	private boolean isStatusSended;
	
	public Simulator(int number){
		this.id = number;
		status = READY;
		// TODO Определить алгоритм присвоения порта
		port = initialPort+this.id;
		// TODO Определить алгоритм именования файлов
		if(!tempFolder.exists())
			tempFolder.mkdir();
		sketchFilename = tempFolder.getAbsolutePath() + "/" + port + "-simulavr-sketch.elf";
		vcdTraceFilename = tempFolder.getAbsolutePath() + "/" + port + "-simulavr-vcd-output";
		cpuTraceFilename = tempFolder.getAbsolutePath() + "/" + port + "-simulavr-cpu-output";

		
	}
			
	public int getId() {
		return id;
	}
	
	public String getStatus(){
		return status;
	}
	
	public ReservationInfo getCurrentReserveInfo() {
		return currentReserveInfo;
	}

	public void handleNewRequest(Socket socket, ReservationInfo reserveInfo){
		Message message = Messenger.readMessage(socket);
		if(message == null)
			return;
		String action = message.getText();
		
		System.out.println("Check for action: " + action);
		if(action.equals("STOP")){
			System.out.println("action: STOP");
			stopService();
			return;
		}
		if(!action.equals("START")){
			try {
				socket.close();
			} catch (IOException e) {
			}
			return;
		}
		
		System.out.println("action: START");
		
		stopService();
		while(status!=READY){
			
		}
		
		Messenger.writeMessage(socket, new Message("READY",0));
		
		currentReserveInfo = reserveInfo;
		currentClientSocket = socket;
		/*remove files
		 *  is exists*/
		File file = new File(sketchFilename);
		if(file.exists())
			file.delete();
		file = new File(vcdTraceFilename);
		if(file.exists())
			file.delete();
		file = new File(cpuTraceFilename);
		if(file.exists())
			file.delete();
		
		try {
			simulavrConfig = Messenger.readSimulAVRConfigs(currentClientSocket);
			if(simulavrConfig == null)
				throw new IOException("Configs error");
			loadFile();
		} catch (IOException e) {
			e.printStackTrace();
			//System.out.println("Simulator: download sketch failed: " + e.getMessage());
			return;
		}
		System.out.println("Prepare to start simulator");
		simulAvr = new SimulAVR(simulavrConfig, port, sketchFilename, vcdTraceFilename, cpuTraceFilename, this);
		isStatusSended = false;
		System.out.println("Simulator started, IN USE");
		status = INUSE;
		simulAvr.start();

	}

	private void loadFile() throws IOException{
		System.out.println("Loading file");
		InputStream inputStream = currentClientSocket.getInputStream();
		DataInputStream dis = new DataInputStream(inputStream);
			long size = dis.readLong();
			RandomAccessFile file = new RandomAccessFile(sketchFilename, "rw");
			for(long i=0; i<size; i++){
				file.writeByte(dis.readByte());
			}
			file.close();
	}
	
	private void sendFile(Socket s, File file) throws IOException{
		OutputStream str = s.getOutputStream();
		DataOutputStream  dos = new DataOutputStream(str);
		dos.writeLong(file.length());
		System.out.println("File to send: " + file.getName() + ":" + file.length());
		RandomAccessFile rfile = new RandomAccessFile(file.getAbsolutePath(), "r");
		byte buff[] = new byte[128];
		int size;
		while((size = rfile.read(buff)) >0){
			dos.write(buff, 0, size);
		}
		rfile.close();
	}
	
	public void stopService(){
		synchronized (this) {
			if(simulAvr != null)
				simulAvr.interrupt();
			if(simulAvr == null){
				status = READY;	
			}
			simulAvr = null;
			currentReserveInfo = null;
						
		}
	}
	
	/*public void killService(){
		synchronized (this) {
			if(simulAvr != null){
				simulAvr.setNeedToKill(true);
				simulAvr.interrupt();
			}
			simulAvr = null;
			currentReserveInfo = null;
			status = READY;				
		}
	}*/	
	
	@Override
	public void started() {
		if(!isStatusSended){
			isStatusSended = true;
			int parameter;
			if(simulavrConfig.isDebugEnable())
				parameter = port;
			else
				parameter = 0;
			Messenger.writeMessage(currentClientSocket, new Message("OK", parameter));
		}
		System.out.println("SimulAVR started");
	}

	@Override
	public void finishedSuccess() {
		System.out.println("SimulAVR finished success");
		if (currentClientSocket == null){
			System.out.println("currentClientSocket == null");
			return;
		}
			
		try {
			if (simulavrConfig.isVCDTraceEnable()) {
				File file = new File(vcdTraceFilename);
				if(file.exists()){
					System.out.println("Sending file back");
					sendFile(currentClientSocket, file);
				}
			}
			currentClientSocket.close();
		} catch (IOException e) {
		}
		//check for result files
		//if exists - send to client
		status = READY;
	}

	@Override
	public void finishedBad() {
		if(!isStatusSended){
			Messenger.writeMessage(currentClientSocket, new Message("ERR", -1));
			isStatusSended = true;
		}		
		System.out.println("SimulAVR finished bad");
		status = READY;
		
	}

	@Override
	public void interrupted() {
		System.out.println("simulator interrupted");
		if (currentClientSocket == null){
			System.out.println("currentClientSocket == null");
			return;
		}
			
		try {
			if (simulavrConfig.isVCDTraceEnable()) {
				File file = new File(vcdTraceFilename);
				if(file.exists()){
					System.out.println("Sending file back");
					sendFile(currentClientSocket, file);
				}
			}
			currentClientSocket.close();
		} catch (IOException e) {
			System.out.println("error while sending result");
		}		
		status = READY;
	}
	
	@Override
	public boolean equals(Object obj) {
		return id == ((Simulator)obj).id;
	}

}
