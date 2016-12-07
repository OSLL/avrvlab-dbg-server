package avrdebug.configs;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DevicesConfigs {
	private ArrayList<DeviceConfigsItem> devices;
	
	public DevicesConfigs(File devicesConfigsFile) throws IOException{
		devices = new ArrayList<>();
		load(devicesConfigsFile);
	}
	
	public ArrayList<DeviceConfigsItem> getDeviceList(){
		return devices;
	}
	
	private void load(File file) throws IOException{
		FileInputStream fileInputStream = null;
		fileInputStream = new FileInputStream(file);
		load(fileInputStream);
	}
	
	private void load(InputStream input){
		String[] lines = loadStrings(input);
		DeviceConfigsItem item = null;
 		Pattern pattern = Pattern.compile("\\[\\d+\\]");
		for(String line : lines){
	 		line.trim();
	 		if (line.length() == 0 || line.charAt(0) == '#')
	 			continue;
	 		Matcher matcher = pattern.matcher(line);
	 		if(matcher.find()){
	 			String id = line.substring(matcher.start()+1, matcher.end()-1);
	 			if(item != null)
	 				devices.add(item);
 				item = new DeviceConfigsItem();
	 				item.setProperty("id", id);
	 			continue;
	 		}
	 		
	 		int equals = line.indexOf('=');
	 		if (equals != -1) {
	 			String key = line.substring(0, equals).trim();
	 			String value = line.substring(equals + 1).trim();
	 			if(key!=null){
	 				item.setProperty(key, value);
	 			}
	 		}
		}
		if(item != null)
			devices.add(item);
	}
	
	private String[] loadStrings(InputStream input){
		Scanner sc = new Scanner(input);
		ArrayList<String> res = new ArrayList<String>();
		
		while(sc.hasNextLine()){
			res.add(sc.nextLine());
		}
		sc.close();
		String[] arrayRes = new String[res.size()];
		res.toArray(arrayRes);
		return arrayRes;
	}
	
	
}
