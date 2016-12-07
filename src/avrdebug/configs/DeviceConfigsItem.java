package avrdebug.configs;

import java.util.LinkedHashMap;

public class DeviceConfigsItem extends LinkedHashMap<String, String>{
	private static final long serialVersionUID = 936723792898306779L;

	public DeviceConfigsItem() {
		super();
	}
	
	public void setProperty(String property, String value){
		this.put(property, value);
	}
	
	public String getProperty(String property){
		return this.get(property);
	}
	
}
