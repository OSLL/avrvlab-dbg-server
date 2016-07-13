package avr_debug_server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Avarice extends Thread{
	private final String avaricePath = "avarice";
	private String target; 			//controller model (eg "atmega128")
	private String programmerPath; 	//path to device (eg "/dev/ttyUSB0")
	private String filePath; 		//path to .hex file to download in target before debug
	private String port; 			//AVaRICE port which GDB connect  
	private Process avariceProcess; //process handle
	private AvariceListener listener;  
	private boolean isSuccessStarted = false; //true if now AVaRICE is running without errors
	
	public Avarice(String target, String programmerPath, String filePath, int port, AvariceListener listener) {
		this.target = target;
		this.programmerPath = programmerPath;
		this.filePath = filePath;
		this.port = port+"";
		this.listener = listener;
		this.setDaemon(true);
	}
	
	public void run(){
		Pattern pattern = Pattern.compile("[\\w\\d\\s.,<>:-]*Downloading FLASH image to target[.]*\n$");
		ArrayList<String> params = collectOptions();
		String[] commandLine = new String[params.size()];
		commandLine = params.toArray(commandLine);
		try {
			avariceProcess = Runtime.getRuntime().exec(commandLine, null, null);
			String allOutput = "";
			InputStream stream = avariceProcess.getInputStream();
			InputStreamReader isr = new InputStreamReader(stream);
			char buf[] = new char[15];
			int count;
			while(true){
				if(Thread.interrupted()){
					System.out.println("Interrupted!");
					throw new InterruptedException();
				}
				
				if(isr.ready()){ //if some simbols avaliable for read
					count = isr.read(buf);
					if(count == -1)
					{
						System.out.println("count == -1");
						break;
					}
						
				}
				else{
					try{
						avariceProcess.exitValue();
						//System.out.println("exitValue()");
						break;
					}catch(IllegalThreadStateException e){
						//System.out.println("IllegalThreadStateException");
						sleep(100);
						continue;	
					}
					
				}
				if(!isSuccessStarted){ //if AVaRICE not started
					allOutput = allOutput + new String(buf,0,count);
					for(int i=0;i<count;i++)
						System.out.print(buf[i]);
					if(!isr.ready())
						sleep(300);
					if(isr.ready())
						continue;
					if(pattern.matcher(allOutput).matches()){
						//OK
						isSuccessStarted = true;
						//System.out.println("OK");
						//handler.startOk();
						listener.successfulStart();
					}else{
						//Error - AVaRICE prints abnormal output 
						//System.out.println("Error");
						//handler.startError();
						listener.unsuccessfulStart("START_ERROR");
						finishing();
						listener.avariceFinished();
						return;
					}
				}

			}
			//Normal AVaRICE exiting
			if(!isSuccessStarted){
				System.out.println("START_ERROR");
				//handler.startError();
				listener.unsuccessfulStart("START_ERROR");
			}
			//System.out.println("Bye bye");
			listener.avariceFinished();
		} catch (InterruptedException e){
			finishing();
			System.out.println("InterruptedException");
			listener.avariceFinished();
			return;
		}
		catch (IOException e) {
			System.out.println("AVaRICE communication error:");
			e.printStackTrace();
			finishing();
			//handler.startError();
			listener.unsuccessfulStart("IO_ERROR");
			listener.avariceFinished();
			return;
		}
	}
	
	private ArrayList<String> collectOptions(){
		ArrayList<String> params = new ArrayList<String>();
		params.add(avaricePath);
		params.add("--erase");
		params.add("--program");
		params.add("--file");
		params.add(filePath);
		params.add("--part");
		params.add(target);
		params.add("--jtag");
		params.add(programmerPath);
		params.add(":"+port);
		return params;
	}
	
	private void finishing(){
		if(avariceProcess!=null){
			avariceProcess.destroy();
			System.out.println("destroy Avarice");
		}
			
	}
	
	@Override
	protected void finalize() throws Throwable {
		if(avariceProcess!=null){
			avariceProcess.destroy();
		}
			
		super.finalize();
	}
}
