package avr_debug_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

import avrdebug.communication.Messenger;
import avrdebug.communication.SimulAVRConfigs;

public class Simulator implements SimulAVRListener {
	private static int initialPort = 4442;
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
	
	public Simulator(int number){
		this.number = number;
		// TODO Определить алгоритм присвоения порта
		port = initialPort+this.number;
		// TODO Определить алгоритм именования файлов
		sketchFilename = port + "-simulavr-sketch.elf";
		vcdTraceFilename = port + "-simulavr-vcd-output";
		cpuTraceFilename = port + "-simulavr-cpu-output";
	}
	
	public void handleNewRequest(String clientKey, Socket socket){
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
		simulAvr = new SimulAVR(simulavrConfig, port, sketchFilename, vcdTraceFilename, cpuTraceFilename, this);
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
		RandomAccessFile rfile = new RandomAccessFile(file.getAbsolutePath(), "r");
		byte buff[] = new byte[128];
		int size;
		while((size = rfile.read(buff)) >0){
			dos.write(buff, 0, size);
		}
		rfile.close();
	}
	
	@Override
	public void started() {
		System.out.println("SimulAVR started");
	}

	@Override
	public void finishedSuccess() {
		System.out.println("SimulAVR finished success");
		if (currentClientSocket == null)
			return;
		try {
			if (simulavrConfig.isVCDTraceEnable()) {
				File file = new File(vcdTraceFilename);
				if(file.exists())
					sendFile(currentClientSocket, file);
			}
			currentClientSocket.close();
		} catch (IOException e) {
		}
		//check for result files
		//if exists - send to client
	}

	@Override
	public void finishedBad() {
		System.out.println("SimulAVR finished bad");
		
	}
}
