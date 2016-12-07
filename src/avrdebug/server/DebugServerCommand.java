package avrdebug.server;

import java.io.UnsupportedEncodingException;

public class DebugServerCommand {
	
	private byte[] command;
	private byte parameter;
	
	public DebugServerCommand(String command, byte param) {
		this.command = new byte[4]; 
		char[] str =  command.toCharArray();
		for(int i=0; i<4; i++)
			this.command[i] = (byte) str[i];
		
		parameter = param;
	}
	
	public DebugServerCommand(byte[] data){
		command = new byte[4]; 
		int len = data.length;
		if(len > 4)
			len = 4;
		System.arraycopy(data, 0, command, 0, 4);
		parameter = data[data.length-1];
	}
	
	public byte[] getData(){
		byte[] data = new byte[5];
		System.arraycopy(command, 0, data, 0, 4);
		data[4] = parameter;
		return data;
	}
	
	public String getCommand(){
		String res;
		try {
			res = new String(command,"UTF-8");
			return res;
		} catch (UnsupportedEncodingException e) {
			return null;
		} 
	}
	
	public byte getParameter(){
		return parameter;
	}
	
}
