package avrdebug.communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.SortedSet;

public class Messenger {
	public static Message readMessage(Socket s){
		try {
			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
			Message message = (Message) ois.readObject();
			return message;
		} catch (IOException | ClassNotFoundException e) {
			return null;
		}
	}
	
	public static void writeMessage(Socket s, Message mess){
		try {
			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
			oos.writeObject(mess);
		} catch (IOException e) {
		}
	}
	
	public static SimpleReserveItem readSimpleReserveItem(Socket s){
		try {
			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
			SimpleReserveItem item = (SimpleReserveItem) ois.readObject();
			return item;
		} catch (IOException | ClassNotFoundException e) {
			return null;
		}		
	}

	public static void writeSimpleReserveItem(Socket s, SimpleReserveItem item){
		try {
			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
			oos.writeObject(item);
		} catch (IOException e) {
		}		
	}
	
	public static SortedSet<SimpleReserveItem> readSimpleReserveItemSet(Socket s){
		try {
			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
			@SuppressWarnings("unchecked")
			SortedSet<SimpleReserveItem> set = (SortedSet<SimpleReserveItem>) ois.readObject();
			return set;
		} catch (IOException | ClassNotFoundException e) {
			return null;
		}		
	}

	public static void writeSimpleReserveItemSet(Socket s, SortedSet<SimpleReserveItem> item){
		try {
			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
			oos.writeObject(item);
		} catch (IOException e) {
		}		
	}
	
	public static ArrayList<SimpleDeviceInfo> readSimpleDeviceInfoList(Socket s){
		try {
			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
			@SuppressWarnings("unchecked")
			ArrayList<SimpleDeviceInfo> list = (ArrayList<SimpleDeviceInfo>) ois.readObject();
			return list;
		} catch (IOException | ClassNotFoundException e) {
			return null;
		}		
	}

	public static void writeSimpleDeviceInfoList(Socket s, ArrayList<SimpleDeviceInfo> item){
		try {
			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
			oos.writeObject(item);
		} catch (IOException e) {
		}		
	}
	
}
