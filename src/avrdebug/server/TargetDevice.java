package avrdebug.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

import avrdebug.communication.Message;
import avrdebug.communication.Messenger;

public class TargetDevice implements AvariceListener{
	private static int initialPort = 4242;
	private int number; 	//index number of programmer-debugger
	private String name; 	//controller model (eg "atmega128")
	private String path; 	//path to device (eg "/dev/ttyUSB0")
	private int port; 		//AVaRICE port which GDB connect  
	private String status;	//status of microcontroller (READY, DEBUG, UNAVAILABLE)
	private String currentClientKey; //key of client working with MCU now
	private Socket currentClientSocket;
	private Avarice avarice;
	private String sketchFilename;
	
	public String getStatus() {
		//! Need to check file (path) existing !
		synchronized (this) {
			return status;
		}
	}

	public String getCurrentClientKey() {
			return currentClientKey;
	}

	public int getNumber() {
		return number;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public TargetDevice(int number, String mcu, String path) {
		this.number = number;
		name = mcu;
		this.path = path;
		status = "READY";
		port = initialPort + this.number; 
		sketchFilename = number+"-mcu-sketch.hex";
		currentClientKey = null;
		currentClientSocket = null;
	}
	
	public void handleNewRequest(String clientKey, Socket socket){
		stopService();
		synchronized (this) {
			status = "DEBUG";
			currentClientKey = clientKey;
			currentClientSocket = socket;
			try {
				loadFile();
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
			currentClientKey = null;
				status = "READY";				
		}
	}

	@Override
	public void successfulStart() {
		System.out.println("Avarice started");
		if(currentClientSocket == null)
			return;
		Messenger.writeMessage(currentClientSocket, new Message("OK", port));
		try {
			currentClientSocket.close();
		} catch (IOException e) {
		}
		currentClientSocket = null;
	}

	@Override
	public void unsuccessfulStart(String reason) {
		System.out.println("Avarice error: " + reason);
		if(currentClientSocket == null)
			return;
		Messenger.writeMessage(currentClientSocket, new Message(reason));
		try {
			currentClientSocket.close();
		} catch (IOException e) {
		}
		currentClientSocket = null;
	}

	@Override
	public void avariceFinished() {
		System.out.println("Avarice finished");
		synchronized (this) {
			status = "READY";
		}
		currentClientKey = null;
		avarice = null;
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
	
	@Override
	public boolean equals(Object obj) {
		return ((number == ((TargetDevice)obj).number)||
				(path.equals( ((TargetDevice)obj).path )));
	}
	
}
