package avr_debug_server;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class DeviceFinder{
	private static final File file = new File("/dev/");
	private static final Pattern pattern = Pattern.compile("ttyUSB\\d+");
	
	public static String[] printList(){
		ArrayList<String> result = new ArrayList<>();
		String list[] = file.list();
		for(String s : list)
			if(matched(s))
				result.add(file.getAbsolutePath() + "/" + s);
		String res[] = new String[result.size()];
		result.toArray(res);
		return res;
	}
	
	private static boolean matched(String s){
		return pattern.matcher(s).matches();
	}	
	
}