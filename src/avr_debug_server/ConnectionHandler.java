package avr_debug_server;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;

class ConnectionHandler{
	Avarice avarice;
	boolean waiting = true;
	private MainFrame frame;
	Socket s;
	public ConnectionHandler(MainFrame frame) {
		this.frame = frame;
		frame.addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				avarice.stop();				
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	ServerSocket server;
	public void handle(){
		System.out.println("Hi!");
		avarice = new Avarice("avarice", this);
		ServerSocket server = null;
		try {
			server = new ServerSocket(3129);
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
					System.out.println("recieced: " + command.getCommand() + " " + command.getParameter());
					switch (command.getCommand()) {
					case "LOAD":
							loadFile(dis, "file.hex");
							break;
					case "STRT":
							if(!avarice.isReadyForStart())
								avarice.stop();
							avarice.setFilePath("file.hex");
							avarice.setProgrammerPath(frame.getProgrammerPath());
							avarice.setTarget(frame.getTargetName());
							avarice.setPort(frame.getPort());
							avarice.start();
							cont = false;
							break;
					}
				}
				while(waiting);
				frame.clientDisconnected();
				//s.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("%");
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
		server.close();
		avarice.stop();
		super.finalize();
	}
}
