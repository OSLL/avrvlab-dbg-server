package avr_debug_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

public class TargetDevice implements AvariceListener{
	private static int initialPort = 4242;
	private int number; 	//index number of programmer-debugger
	private String name; 	//controller model (eg "atmega128")
	private String path; 	//path to device (eg "/dev/ttyUSB0")
	private String port; 	//AVaRICE port which GDB connect  
	private String status;	//status of microcontroller (READY, DEBUG, UNAVAILABLE)
	private String currentClientKey; //key of client working with MCU now
	private Socket currentClientSocket;
	private Avarice avarice;
	private String sketchFilename;
	
	public String getStatus() {
		return status;
	}

	public String getCurrentClientKey() {
		return currentClientKey;
	}

	public int getNumber() {
		return number;
	}

	public TargetDevice(int number, String mcu, String path) {
		this.number = number;
		name = mcu;
		this.path = path;
		status = "READY";
		port = ""+(initialPort + this.number); 
		sketchFilename = number+"-mcu-sketch.hex";
		currentClientKey = null;
		currentClientSocket = null;
	}
	
	public void handleNewRequest(String clientKey, Socket socket){
		synchronized (this) {
			stopService();
			status = "DEBUG";
			currentClientKey = clientKey;
			currentClientSocket = socket;
			try {
				InputStream inputStream = currentClientSocket.getInputStream();
				DataInputStream dataInputStream = new DataInputStream(inputStream);
				loadFile(dataInputStream);
			} catch (IOException e) {
				System.out.println("Download sketch failed");
				stopService();
				return;
			}
			
			avarice = new Avarice(name, path, sketchFilename, port, this);
			avarice.start();
		}
	}

	public void stopService(){
		synchronized (this) {
			if(avarice != null)
				avarice.interrupt();
			avarice = null;
			status = "READY";
		}
	}

	@Override
	public void successfulStart() {
		System.out.println("Avarice started");
		if(currentClientSocket == null)
			return;
		try {
			/*
			 * Say to Client about Avarice start
			 * */
			DebugServerCommand command = new DebugServerCommand("OKEY", (byte)0);
			OutputStream str = currentClientSocket.getOutputStream();
			DataOutputStream  dos = new DataOutputStream(str);
			dos.write(command.getData());
			/*
			 * */
			
			currentClientSocket.close();
		} catch (IOException e) {
			currentClientSocket = null;
		}
	}

	@Override
	public void unsuccessfulStart(String reason) {
		System.out.println("Avarice error: " + reason);
		if(currentClientSocket == null)
			return;
		try {
			/*
			 * Say to Client about Error
			 * */
			DebugServerCommand command = new DebugServerCommand("ERRR", (byte)0);
			OutputStream str = currentClientSocket.getOutputStream();
			DataOutputStream  dos = new DataOutputStream(str);
			dos.write(command.getData());
			/*
			 * */
			currentClientSocket.close();
		} catch (IOException e) {
			currentClientSocket = null;
		}		
	}

	@Override
	public void avariceFinished() {
		System.out.println("Avarice finished");
		status = "READY";
		currentClientKey = null;
		avarice = null;
	}
	
	private void loadFile(DataInputStream dis) throws IOException{
		
		System.out.println("Loading file");
		
			long size = dis.readLong();
			RandomAccessFile file = new RandomAccessFile(sketchFilename, "rw");
			for(long i=0; i<size; i++){
				file.writeByte(dis.readByte());
			}
			file.close();
	}
	
}
