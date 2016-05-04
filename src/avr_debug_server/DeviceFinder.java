package avr_debug_server;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class DeviceFinder{
	File file = new File("/dev/");
	Pattern pattern = Pattern.compile("ttyUSB\\d+");
	
	String[] printList(){
		ArrayList<String> result = new ArrayList<>();
		String list[] = file.list();
		for(String s : list)
			if(matched(s))
				result.add(file.getAbsolutePath() + "/" + s);
		String res[] = new String[result.size()];
		result.toArray(res);
		return res;
	}
	
	private boolean matched(String s){
		return pattern.matcher(s).matches();
	}	
	
}