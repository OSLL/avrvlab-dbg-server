package avrdebug.server;
import java.io.Serializable;
import java.util.Calendar;


public class ReserveListItem implements Serializable, Comparable<ReserveListItem>{

	private static final long serialVersionUID = -9150545569875406266L;
	private String key;
	private int mcuId;
	private Calendar startTime;
	private Calendar endTime;
	
	public String getKey() {
		return key;
	}

	public int getMcuId() {
		return mcuId;
	}

	public Calendar getStartTime() {
		return startTime;
	}

	public Calendar getEndTime() {
		return endTime;
	}

	public ReserveListItem(String key, int mcuId, Calendar startTime, Calendar endTime) {
		this.key = key;
		this.mcuId = mcuId;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	@Override
	public int compareTo(ReserveListItem o) {
		return startTime.compareTo(o.getStartTime());
	}

}
