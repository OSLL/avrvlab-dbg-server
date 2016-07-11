package avr_debug_server;

import java.net.Socket;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Random;
import java.util.SortedSet;

public class ReserveCalendarManager {
	private SortedSet<ReserveListItem> reserve;
	
	public ReserveCalendarManager(SortedSet<ReserveListItem> reserve) {
		this.reserve = reserve;
	}
	
	public void handleNewRequest(Socket socket){
		Message message = Messenger.readMessage(socket);
		if(message==null)
			return;
		switch (message.getText()) {
		case "ADD":
			SimpleReserveItem item = Messenger.readSimpleReserveItem(socket);
			String key = generateKey(item);
			reserve.add(new ReserveListItem(key, item.getMcuId(), item.getStartTime(), item.getEndTime()));
			Messenger.writeMessage(socket, new Message(key, 0));
			deleteOldReserve();
			break;
		}
	}
	
	public void deleteOldReserve(){
		Calendar now = new GregorianCalendar();
		Iterator<ReserveListItem> it = reserve.iterator();
		while(it.hasNext()){
			ReserveListItem cur = it.next(); 
			if(cur.getEndTime().compareTo(now) < 0){
				reserve.remove(cur);
				it = reserve.iterator();
			}
			else
				break;
		}
	}
	
	public int getMcuIdByKey(String key){
		ReserveListItem item = null;
		for(ReserveListItem cur : reserve){
			if(cur.getKey().equals(key)){
				item = cur;
				break;
			}
		}
		if(item==null)
			return -1;
		Calendar now = new GregorianCalendar();
		if( (item.getStartTime().compareTo(now) <= 0) && (item.getEndTime().compareTo(now) > 0) )
			return item.getMcuId();
		return -2;
	}
	
	private String generateKey(SimpleReserveItem item){
		String key = "";
		key = key + item.getMcuId();
		Integer day = item.getStartTime().get(Calendar.DATE);
		key = key + Integer.toHexString(day%16).toUpperCase();
		Random rand = new Random();
		char a = (char)(rand.nextInt(26)+'A');
		char b = (char)(rand.nextInt(26)+'A');
		key = key + a + b;
		int hash = key.hashCode()%100;
		key = key + hash;
		return key;
	}

}
