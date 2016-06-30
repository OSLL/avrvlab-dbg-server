package avr_debug_server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Messenger {
	static Message readMessage(Socket s){
		try {
			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
			Message message = (Message) ois.readObject();
			return message;
		} catch (IOException | ClassNotFoundException e) {
			System.out.println(e.getClass());
			e.printStackTrace();
			return null;
		}
	}
	
	static void writeMessage(Socket s, Message mess){
		try {
			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
			oos.writeObject(mess);
		} catch (IOException e) {
		}
	}
}
