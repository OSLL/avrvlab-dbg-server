package avr_debug_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

import javax.swing.plaf.metal.MetalIconFactory.FolderIcon16;

import avrdebug.communication.Message;
import avrdebug.communication.Messenger;
import avrdebug.communication.SimulAVRConfigs;

public class Simulator implements SimulAVRListener {
	private static int initialPort = 4442;
	private static File tempFolder = new File("simulator-temp-files"); 
	private int number; 	//index number of programmer-debugger
	private int port; 		//AVaRICE port which GDB connect  
	private String currentClientKey; //key of client working with MCU now
	private Socket currentClientSocket;
	//private SimulAVR simulavr;
	private String sketchFilename;
	private String vcdTraceFilename;
	private String cpuTraceFilename;
	private SimulAVR simulAvr;
	private SimulAVRConfigs simulavrConfig;
	private String status;
	
	public Simulator(int number){
		this.number = number;
		status = "READY";
		// TODO Определить алгоритм присвоения порта
		port = initialPort+this.number;
		// TODO Определить алгоритм именования файлов
		if(!tempFolder.exists())
			tempFolder.mkdir();
		sketchFilename = tempFolder.getAbsolutePath() + "/" + port + "-simulavr-sketch.elf";
		vcdTraceFilename = tempFolder.getAbsolutePath() + "/" + port + "-simulavr-vcd-output";
		cpuTraceFilename = tempFolder.getAbsolutePath() + "/" + port + "-simulavr-cpu-output";

		
	}
			
	public int getNumber() {
		return number;
	}
	
	public String getStatus(){
		return status;
	}

	public void handleNewRequest(String clientKey, Socket socket){
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
		currentClientKey = clientKey;
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
				throw new IOException();
			loadFile();
		} catch (IOException e) {
			System.out.println("Simulator: download sketch failed");
			return;
		}
		System.out.println("Prepare to start simulator");
		simulAvr = new SimulAVR(simulavrConfig, port, sketchFilename, vcdTraceFilename, cpuTraceFilename, this);
		simulAvr.start();
		System.out.println("Simulator started, IN USE");
		status = "IN USE";
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
			simulAvr = null;
			currentClientKey = null;
				status = "READY";				
		}
	}
	
	@Override
	public void started() {
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
		status = "READY";
	}

	@Override
	public void finishedBad() {
		System.out.println("SimulAVR finished bad");
		status = "READY";
		
	}

	@Override
	public void interrupted() {
		System.out.println("simulator interrupted");
		status = "READY";
	}
	
	@Override
	public boolean equals(Object obj) {
		return number == ((Simulator)obj).number;
	}

}
