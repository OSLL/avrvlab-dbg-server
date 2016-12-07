package avrdebug.server;

import java.io.IOException;
import java.net.Socket;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import avrdebug.communication.Message;
import avrdebug.communication.Messenger;
import avrdebug.communication.SimpleReserveItem;

public class ReserveCalendarManager {
	private SortedSet<ReserveListItem> reserve;
	
	public ReserveCalendarManager(SortedSet<ReserveListItem> reserve) {
		this.reserve = reserve;
	}
	
	public void handleAddRequest(Socket socket){
		SimpleReserveItem item = Messenger.readSimpleReserveItem(socket);
		for(ReserveListItem cur : reserve)
			if(isIntersection(item, cur)){
				Messenger.writeMessage(socket, new Message(null, -1));
				try {
					socket.close();
				} catch (IOException e) {
				}
				return;
			}
		String key = generateKey(item);
		reserve.add(new ReserveListItem(key, item.getMcuId(), item.getStartTime(), item.getEndTime()));
		System.out.println("Add reserve: " + key + " "+ item.getStartTime().getTime()+ " - " + item.getEndTime().getTime());
		Messenger.writeMessage(socket, new Message(key, 0));
		//deleteOldReserve();
	}
	
	private boolean isIntersection(SimpleReserveItem item1, ReserveListItem item2){
		if(item1.getStartTime().getTimeInMillis() < item2.getStartTime().getTimeInMillis())
			if(item1.getEndTime().getTimeInMillis()>item2.getStartTime().getTimeInMillis())
				return true;
			else
				return false;
		else
			if(item2.getEndTime().getTimeInMillis()>item1.getStartTime().getTimeInMillis())
				return true;
			else
				return false;

	}
	
	public SortedSet<SimpleReserveItem> getSimpleReserveInfo(){
		SortedSet<SimpleReserveItem> set = new TreeSet<SimpleReserveItem>();
		for(ReserveListItem cur: reserve)
			set.add(new SimpleReserveItem(cur.getMcuId(), cur.getStartTime(), cur.getEndTime()));
		return set;
	}
	
	public HashSet<String> deleteOldReserve(){
		HashSet<String> set = new HashSet<>();
		Calendar now = new GregorianCalendar();
		Iterator<ReserveListItem> it = reserve.iterator();
		while(it.hasNext()){
			ReserveListItem cur = it.next(); 
			if(cur.getEndTime().compareTo(now) < 0){
				set.add(cur.getKey());
				reserve.remove(cur);
				it = reserve.iterator();
			}
			else
				break;
		}
		if(set.isEmpty())
			return null;
		return set;
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
