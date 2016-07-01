package avr_debug_server;
import javax.swing.ImageIcon;

public class DevicesTableElement {
	/* Change icon filenames and path according with icon
	 * "/home/constantin/refresh.png" is only for testing
	 * */
	private static ImageIcon readyStatus = new ImageIcon("/home/constantin/refresh.png");
	private static ImageIcon debugStatus = new ImageIcon("/home/constantin/refresh.png");
	private static ImageIcon unavaliableStatus = new ImageIcon("/home/constantin/refresh.png");
	private Integer number;
	private String path;
	private String target;
	private ImageIcon status;
	private static ImageIcon trashBin = new ImageIcon("/home/constantin/refresh.png");
	
	public DevicesTableElement(int number, String path, String target, String status) {
		this.number = number;
		this.path = path;
		this.target = target;
		setStatus(status);
	}
	
	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public ImageIcon getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = unavaliableStatus;
		if("READY".equals(status))
			this.status = readyStatus;
		if("DEBUG".equals(status))
			this.status = debugStatus;
	}

	public ImageIcon getTrashBin() {
		return trashBin;
	}

	public void setTrashBin(ImageIcon trashBin) {
	}

	@Override
	public boolean equals(Object obj) {
		return number ==((DevicesTableElement)obj).number;
	}
	
}
