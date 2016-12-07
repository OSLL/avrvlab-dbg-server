package avrdebug.server;

import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

import avrdebug.communication.Message;
import avrdebug.communication.Messenger;

public class SimulatorDispatcher {
	private CopyOnWriteArrayList<Simulator> simulators;
	private SimulatorsTableModel model;
	
	public SimulatorDispatcher() {
		simulators = new CopyOnWriteArrayList<>();
		model = new SimulatorsTableModel(simulators);
	}
	
	public SimulatorsTableModel getModel(){
		return model;
	}
	
	public boolean addNewDevice(int number){
		
		Simulator candidate = new Simulator(number);
		for(Simulator cur : simulators)
			if(cur.equals(candidate))
				return false;
		simulators.add(candidate);
		return true;
	}
	
	public boolean removeDevice(int number){
		for(Simulator cur : simulators)
			if(cur.getNumber() == number){
				simulators.remove(cur);
				return true;
			}
		return false;
	}
	
	public void handleNewRequest(Socket socket){
		Message message = Messenger.readMessage(socket);
		if(message == null)
			return;
		String key = message.getText();
		System.out.println("KEY: "+key);
		Simulator currentSimul = null;
		switch (key) {
		case "SIMUL0":
			currentSimul = simulators.get(0);
			break;
		case "SIMUL1":
			currentSimul = simulators.get(1);
			break;
		case "SIMUL2":
			currentSimul = simulators.get(2);
			break;			
		}
		if(currentSimul == null){
			Messenger.writeMessage(socket, new Message("BAD_KEY"));
			System.out.println("BAD_KEY");
			return;
		}
		System.out.println("OK, let's handle" + currentSimul.getNumber());
		Messenger.writeMessage(socket, new Message("OK"));
		currentSimul.handleNewRequest(key, socket);
	}
	
}
