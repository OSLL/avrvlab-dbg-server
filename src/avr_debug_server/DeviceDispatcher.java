package avr_debug_server;

import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class DeviceDispatcher {
	/*
	 * private Calendar calendar
	 * */
	private CopyOnWriteArrayList<TargetDevice> devices;
	
	//one more parameter - calendar
	public DeviceDispatcher() {
		/*
		 * this.calendar = calendar
		 * */
	}
	
	public void handleNewRequest(Socket socket){
		
		/*
		 * Read key from socket
		 * */
		
		/*
		 * Check key using calendar. If user can not debug now, return error to user
		 * Get from calendar number of MCU for this user's key
		 * */
		
		/*
		 * Find target device if devices using NOT ArrayList index - should be used targetDevice.getNumber()
		 * If no such device - return error to user
		 * */
		
		/*
		 * Return OK to user and transfer of control to device
		 * targetDevice.handleNewRequest(key, socket);
		 * */
	}
	
}
