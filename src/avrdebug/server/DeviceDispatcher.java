package avrdebug.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;

import avrdebug.communication.Message;
import avrdebug.communication.Messenger;
import avrdebug.communication.SimpleDeviceInfo;
import avrdebug.reservation.ReservationInfo;

public class DeviceDispatcher {

	private CopyOnWriteArrayList<TargetDevice> devices;
	
	public DeviceDispatcher() {
		devices = new CopyOnWriteArrayList<>();
	}
	
	public boolean addNewDevice(int id, String target, String path){
		//Not the best solution, but array has a small size
		if(target == null || path == null)
			return false;
		TargetDevice candidate = new TargetDevice(id, target, path);
		for(TargetDevice cur : devices)
			if(cur.equals(candidate))
				return false;
		devices.add(candidate);
		return true;
	}
	
	public boolean removeDevice(int id){
		for(TargetDevice cur : devices)
			if(cur.getId() == id){
				devices.remove(cur);
				return true;
			}
		return false;
	}
	
	public void handleNewRequest(Socket socket, ReservationInfo reserveInfo){
		TargetDevice device = null;
		for(TargetDevice cur : devices){
			if(cur.getId() == reserveInfo.getResourceId()){
				device = cur;
				break;
			}
		}
		if(device == null){
			Messenger.writeMessage(socket, new Message("Simulator not exists", -5));
			return;
		} 
		Messenger.writeMessage(socket, new Message("OK",0));
		device.handleNewRequest(socket, reserveInfo);
	}
	
	public void stopAllService(){
		for(TargetDevice cur : devices){
			cur.stopService();
		}
	}
	
	public void stopExpiredSessions(HashSet<String> sessions){
		for(TargetDevice cur : devices){
			if(cur.getStatus() == TargetDevice.INUSE){
				if(cur.getCurrentReserveInfo().getEndTime().before(new Date())){
					cur.stopService();
				}
			}
		}
	}
	
	public ArrayList<SimpleDeviceInfo> getSimpleDeviceInfo(){
		ArrayList<SimpleDeviceInfo> res = new ArrayList<>();
		for(TargetDevice cur : devices)
			res.add(new SimpleDeviceInfo(cur.getId(), cur.getName()));
		return res;
	}
	
}
