package avr_debug_server;

import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

/*
 * Key structure:
 * 00|F|AB|27
 *  a|b| c| d
 * 
 * a - mcu number
 * b - calendar day in hex modulo 16
 * c - 2 random letters
 * d - 2 last digits of String.hashCode()
 * */

public class DeviceDispatcher {
	/*
	 * private Calendar calendar
	 * */
	private CopyOnWriteArrayList<TargetDevice> devices;
	
	//one more parameter - calendar
	public DeviceDispatcher() {
		/*
		 * Possible load device list from file in future
		 * */
		devices = new CopyOnWriteArrayList<>();
		/*
		 * this.calendar = calendar
		 * */
	}
	
	public boolean addNewDevice(int number, String target, String path){
		//Not the best solution, but array has a small size
		for(TargetDevice cur : devices)
			if(cur.getNumber() == number)
				return false;
		devices.add(new TargetDevice(number, target, path));
		return true;
	}
	
	public boolean removeDevice(int number){
		for(TargetDevice cur : devices)
			if(cur.getNumber() == number){
				devices.remove(cur);
				return true;
			}
		return false;
	}
	
	public void handleNewRequest(Socket socket){

		/*
		 * Read key from socket
		 * */
		Message message = Messenger.readMessage(socket);
		if(message == null)
			return;
		String key = message.getText();
		
		/*
		 * Check key using calendar. If user can not debug now, return error to user
		 * Get from calendar number of MCU (mcuNumber) for this user's key
		 * */
		int mcuNumber = 0;
		
		/*
		 * Check status of device! And make sure the file ttyUSB* exists!!!
		 * */
		TargetDevice device = null;
		for(TargetDevice cur : devices){
			if(cur.getNumber() == mcuNumber){
				device = cur;
				break;
			}
		}
		if(device == null){
			Messenger.writeMessage(socket, new Message("BAD_DEV"));
			return;
		} 

		
		//Return OK to user and transfer of control to device
		Messenger.writeMessage(socket, new Message("OK"));
		device.handleNewRequest(key, socket);
	}
	
}
