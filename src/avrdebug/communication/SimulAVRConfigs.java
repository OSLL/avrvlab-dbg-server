package avrdebug.communication;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class SimulAVRConfigs implements Serializable {
	private static final long serialVersionUID = -235225560474202938L;
	/** Current selected MCU*/
	private String selectedMcu;
	/** MCU's CPU frequency*/
	private long cpuFreq;
	/** If true - simulator provides tracing AVR core*/
	private boolean isTraceEnable;
	/** If true - gdb-server will be started on debug server*/
	private boolean isDebugEnable;
	/** Maximum simulator run time. 0 or -1 if option is not required*/
	private long maxRunTime;
	/** If true - simulator provides VCD trace dump*/
	private boolean isVCDTraceEnable;
	/** List of available VCD-trace sources with flags (flag==true if source should be traced) */
	private LinkedHashMap<String, Boolean> vcdSources;
	
	public SimulAVRConfigs() {
	}
	public String getSelectedMcu() {
		return selectedMcu;
	}
	public void setSelectedMcu(String selectedMcu) {
		this.selectedMcu = selectedMcu;
	}
	public long getCpuFreq() {
		return cpuFreq;
	}
	public void setCpuFreq(long cpuFreq) {
		this.cpuFreq = cpuFreq;
	}
	public boolean isTraceEnable() {
		return isTraceEnable;
	}
	public void setTraceEnable(boolean isTraceEnable) {
		this.isTraceEnable = isTraceEnable;
	}
	public boolean isDebugEnable() {
		return isDebugEnable;
	}
	public void setDebugEnable(boolean isDebugEnable) {
		this.isDebugEnable = isDebugEnable;
	}
	public long getMaxRunTime() {
		return maxRunTime;
	}
	public void setMaxRunTime(long maxRunTime) {
		this.maxRunTime = maxRunTime;
	}
	public boolean isVCDTraceEnable() {
		return isVCDTraceEnable;
	}
	public void setVCDTraceEnable(boolean isVCDTraceEnable) {
		this.isVCDTraceEnable = isVCDTraceEnable;
	}
	public LinkedHashMap<String, Boolean> getVcdSources() {
		return vcdSources;
	}
	public void setVcdSources(LinkedHashMap<String, Boolean> vcdSources) {
		this.vcdSources = vcdSources;
	}
}
