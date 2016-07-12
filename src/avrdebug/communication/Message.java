package avrdebug.communication;

import java.io.Serializable;

public class Message implements Serializable{
	private static final long serialVersionUID = 3369837356017943798L;
	private String text;
	private int parameter;
		
	public String getText() {
		return text;
	}

	public int getParameter() {
		return parameter;
	}

	public Message(String text, int param) {
		this.text = text;
		this.parameter = param;
	}
	
	public Message(String text) {
		this.text = text;
		this.parameter = 0;
	}	
}
