package avrdebug.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import avrdebug.communication.Message;
import avrdebug.communication.Messenger;
import avrdebug.communication.SimpleDeviceInfo;

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
	private static File devicesSystemPath = new File("/dev/");
	private static Pattern devicesNamePattern = Pattern.compile("ttyUSB\\d+");
	private static String supportedDevicesFile = "avarice_supported_devices.txt";
	private ReserveCalendarManager calendarManager;
	private CopyOnWriteArrayList<TargetDevice> devices;
	private DevicesTableModel model;
	
	private Simulator simulator = new Simulator(0);
	
	//one more parameter - calendar
	public DeviceDispatcher(ReserveCalendarManager calendarManager) {
		/*
		 * Possible load device list from file in future
		 * */
		devices = new CopyOnWriteArrayList<>();
		model = new DevicesTableModel(devices);
		this.calendarManager = calendarManager;
	}
	
	public DevicesTableModel getModel(){
		return model;
	}
	
	public boolean addNewDevice(int number, String target, String path){
		//Not the best solution, but array has a small size
		if(target == null || path == null)
			return false;
		TargetDevice candidate = new TargetDevice(number, target, path);
		for(TargetDevice cur : devices)
			if(cur.equals(candidate))
				return false;
		devices.add(candidate);
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

		Message message = Messenger.readMessage(socket);
		if(message == null)
			return;
		String key = message.getText();
		
		int mcuNumber = calendarManager.getMcuIdByKey(key);
		if(mcuNumber < 0){
			Messenger.writeMessage(socket, new Message("ACCESS_ERROR"));
			System.out.println("Access error: " + mcuNumber);
			return;
		}
		
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
	
	public void handleNewSimulatorRequest(Socket socket){
		simulator.handleNewRequest("symul", socket);
	}
	
	public void stopAllService(){
		for(TargetDevice cur : devices){
			cur.stopService();
		}
	}
	
	public void stopExpiredSessions(HashSet<String> sessions){
		for(TargetDevice cur : devices){
			if(sessions.contains(cur.getCurrentClientKey()))
				cur.stopService();
		}
	}
	
	public static String[] getSystemDeviceList(){
		ArrayList<String> result = new ArrayList<>();
		String list[] = devicesSystemPath.list();
		for(String s : list)
			if(isDeviceSystemNameMatches(s))
				result.add(devicesSystemPath.getAbsolutePath() + "/" + s);
		String res[] = new String[result.size()];
		result.toArray(res);
		return res;
	}
	
	public ArrayList<SimpleDeviceInfo> getSimpleDeviceInfo(){
		ArrayList<SimpleDeviceInfo> res = new ArrayList<>();
		for(TargetDevice cur : devices)
			res.add(new SimpleDeviceInfo(cur.getNumber(), cur.getName()));
		return res;
	}
	
	private static boolean isDeviceSystemNameMatches(String s){
		return devicesNamePattern.matcher(s).matches();
	}
	
	static public String[] getSupportedDevices(){
		ArrayList<String> list = new ArrayList<String>();
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(supportedDevicesFile));
		String s;
			while((s=in.readLine())!=null){
				list.add(s);
			}
			in.close();
		} catch (IOException e) {
			String result[] = {null};
			return result;
		}
		String result[] = new String[list.size()];
		list.toArray(result);
		return result;
	}
	
	
}
