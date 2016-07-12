package avrdebug.communication;
import java.io.Serializable;

public class SimpleDeviceInfo implements Serializable {
	private static final long serialVersionUID = -1625758309288602485L;
	private int id;
	private String mcuName;
	private String note;
	
	public int getId() {
		return id;
	}

	public String getMcuName() {
		return mcuName;
	}

	public String getNote() {
		return note;
	}

	public SimpleDeviceInfo(int id, String mcuName){
		this(id, mcuName, null);
	}
	
	public SimpleDeviceInfo(int id, String mcuName, String note) {
		this.id = id;
		this.mcuName = mcuName;
		this.note = note;
	}
	
}
