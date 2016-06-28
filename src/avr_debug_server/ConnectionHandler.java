package avr_debug_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;

class ConnectionHandler{
	boolean waiting = true;
	private MainFrame frame;
	private Socket s;
	private int port;
	TargetDevice targetDevice;
	public ConnectionHandler(MainFrame frame, int port) {
		this.frame = frame;
		this.port = port;
		targetDevice = new TargetDevice(0, "atmega128", frame.getProgrammerPath());
	}
	
	ServerSocket server;
	public void handle(){
		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		while(true)
		{
			try {
				s = server.accept();
				waiting = true;
				frame.clientConnected();
				InputStream is = s.getInputStream();
				DataInputStream dis = new DataInputStream(is);
				byte byteCommand[] = new byte[5];
				boolean cont = true;
				while(s.isConnected() && cont){
					int bytesReaded = dis.read(byteCommand);
					if (bytesReaded < 0)
						break;
					System.out.println(new String(byteCommand,"UTF-8"));
					DebugServerCommand command = new DebugServerCommand(byteCommand);
					System.out.println("recieved: " + command.getCommand() + " " + command.getParameter());
					switch (command.getCommand()) {
					case "LOAD":
							targetDevice.handleNewRequest("221B", s);
							cont = false;
							break;
					}
				}
				//while(waiting);
				frame.clientDisconnected();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void loadFile(DataInputStream dis, String filename){
		System.out.println("Loading file");
		try {
			long size = dis.readLong();
			RandomAccessFile file = new RandomAccessFile(filename, "rw");
			for(long i=0; i<size; i++)
				file.writeByte(dis.readByte());
			file.close();
		} catch (IOException e) {
			return;
		}
	}
	
	public void startOk(){
		DebugServerCommand command = new DebugServerCommand("OKEY", (byte)0);
		OutputStream str;
		try {
			str = s.getOutputStream();
			DataOutputStream  dos = new DataOutputStream(str);
			dos.write(command.getData());
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		waiting = false;
	}
	
	public void startError(){
		DebugServerCommand command = new DebugServerCommand("ERRR", (byte)0);
		OutputStream str;
		try {
			str = s.getOutputStream();
			DataOutputStream  dos = new DataOutputStream(str);
			dos.write(command.getData());
			s.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		waiting = false;
	}
	
	@Override
	protected void finalize() throws Throwable {
		if(server!=null)
			server.close();
		if(targetDevice!=null)
			targetDevice.avariceFinished();
		super.finalize();
	}
}
