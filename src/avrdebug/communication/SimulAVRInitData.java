package avrdebug.communication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class SimulAVRInitData implements Serializable{
	private static final long serialVersionUID = -2591376330837482355L;
	private HashMap<String, ArrayList<String>> mcuVCDSources;
	
	public SimulAVRInitData() {
	}
	public HashMap<String, ArrayList<String>> getMcuVCDSources() {
		return mcuVCDSources;
	}
	public void setMcuVCDSources(HashMap<String, ArrayList<String>> mcuVCDSources) {
		this.mcuVCDSources = mcuVCDSources;
	}
}
