package avrdebug.configs;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Scanner;

public class AppConfigs {
	private static LinkedHashMap<String, String> configs = null;
	
	public AppConfigs(File ServerConfigFile) throws IOException{
		configs = new LinkedHashMap<>();
		load(ServerConfigFile);
	}
	
	public static String getProperty(String property){
		if(configs == null){
			return null;
		}
		return configs.get(property);
	}
	
	private void load(File file) throws IOException{
		FileInputStream fileInputStream = null;
		fileInputStream = new FileInputStream(file);
		load(fileInputStream);
	}
	
	private void load(InputStream input){
		String[] lines = loadStrings(input);
		for(String line : lines){
	 		line.trim();
	 		if (line.length() == 0 || line.charAt(0) == '#')
	 			continue;
	 		
	 		int equals = line.indexOf('=');
	 		if (equals != -1) {
	 			String key = line.substring(0, equals).trim();
	 			String value = line.substring(equals + 1).trim();
	 			if(key!=null){
	 				configs.put(key, value);
	 			}
	 		}
		}

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
