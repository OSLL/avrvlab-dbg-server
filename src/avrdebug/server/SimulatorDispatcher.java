package avrdebug.server;

import java.net.Socket;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;

import avrdebug.communication.Message;
import avrdebug.communication.Messenger;
import avrdebug.reservation.ReservationInfo;

public class SimulatorDispatcher {
	private CopyOnWriteArrayList<Simulator> simulators;
	
	public SimulatorDispatcher() {
		simulators = new CopyOnWriteArrayList<>();
	}
	
	public boolean addNewDevice(int id){
		
		Simulator candidate = new Simulator(id);
		for(Simulator cur : simulators)
			if(cur.equals(candidate))
				return false;
		simulators.add(candidate);
		return true;
	}
	
	public boolean removeDevice(int id){
		for(Simulator cur : simulators)
			if(cur.getId() == id){
				simulators.remove(cur);
				return true;
			}
		return false;
	}
	
	public void handleNewRequest(Socket socket, ReservationInfo reserveInfo){
		Simulator simul = null;
		for(Simulator cur : simulators){
			if(cur.getId() == reserveInfo.getResourceId()){
				simul = cur;
				break;
			}
		}
		if(simul == null){
			Messenger.writeMessage(socket, new Message("Simulator not exists", -4));
			return;
		}
		Messenger.writeMessage(socket, new Message("OK", 0));
		simul.handleNewRequest(socket, reserveInfo);
	}
	
	public void stopExpiredSessions(HashSet<String> sessions){
		for(Simulator cur : simulators){
			if(cur.getStatus() == Simulator.INUSE){
				if(cur.getCurrentReserveInfo().getEndTime().before(new Date())){
					cur.stopService();
				}
			}
		}
	}
	
}
