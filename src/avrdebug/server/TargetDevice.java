package avrdebug.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

import avrdebug.communication.Message;
import avrdebug.communication.Messenger;
import avrdebug.configs.AppConfigs;
import avrdebug.reservation.ReservationInfo;

public class TargetDevice implements AvariceListener{
	public static final String READY = "Ready";
	public static final String INUSE = "In use";
	private static int initialPort = Integer.parseInt(AppConfigs.getProperty("debug.initial_port"));
	private int id; 	//index number of programmer-debugger
	private String name; 	//controller model (eg "atmega128")
	private String path; 	//path to device (eg "/dev/ttyUSB0")
	private int port; 		//AVaRICE port which GDB connect  
	private String status;	//status of microcontroller (READY, DEBUG, UNAVAILABLE)
	private ReservationInfo currentReserveInfo;
	private Socket currentClientSocket;
	private Avarice avarice;
	private String sketchFilename;
	
	public String getStatus() {
			return status;
	}

	public ReservationInfo getCurrentReserveInfo() {
		return currentReserveInfo;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public TargetDevice(int number, String mcu, String path) {
		this.id = number;
		name = mcu;
		this.path = path;
		status = READY;
		port = initialPort + this.id; 
		sketchFilename = number+"-mcu-sketch.hex";
		currentReserveInfo = null;
		currentClientSocket = null;
	}
	
	public void handleNewRequest(Socket socket, ReservationInfo reserveInfo){
		stopService();
		synchronized (this) {
			status = INUSE;
			currentReserveInfo = reserveInfo;
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
			currentReserveInfo = null;
			status = READY;				
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
		status = READY;
		currentReserveInfo = null;
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
		return ((id == ((TargetDevice)obj).id)||
				(path.equals( ((TargetDevice)obj).path )));
	}
	
}
