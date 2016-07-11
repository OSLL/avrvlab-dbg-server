package avr_debug_server;

import java.io.Serializable;
import java.util.Calendar;

public class SimpleReserveItem implements Serializable {
	private static final long serialVersionUID = 7685405580935724011L;
	private int mcuId;
	private Calendar startTime;
	private Calendar endTime;

	public int getMcuId() {
		return mcuId;
	}

	public Calendar getStartTime() {
		return startTime;
	}

	public Calendar getEndTime() {
		return endTime;
	}

	public SimpleReserveItem(int mcuId, Calendar startTime, Calendar endTime) {
		this.mcuId = mcuId;
		this.startTime = startTime;
		this.endTime = endTime;
	}

}
