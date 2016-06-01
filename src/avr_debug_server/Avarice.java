package avr_debug_server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Avarice {
	
	private Thread m_process;
	private Process avariceProcess;
	ConnectionHandler handler;
	private String avaricePath;
	ArrayList<String> params = new ArrayList<String>();
	private String filePath="";

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	private String target = "atmega128";

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	private String programmerPath = "/dev/ttyUSB0";

	public String getProgrammerPath() {
		return programmerPath;
	}

	public void setProgrammerPath(String programmerPath) {
		this.programmerPath = programmerPath;
	}

	private String port = "4242";

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public Avarice(final String avaricePath, ConnectionHandler handler) {
		this.avaricePath = avaricePath;
		this.handler = handler;
		m_process = new Thread(new Runnable() {
			@Override
			public void run() {
				runAvarice();
			}
		});
	}

	public void start() {
		stop();
		if (m_process.isAlive()) {
			throw new IllegalStateException("Avarice has already been started");
		}
		m_process.start();
	}

	public boolean isReadyForStart(){
		return m_process.getState().toString().equals("NEW")||
				m_process.getState().toString().equals("TERMINATED");
	}

	public void stop(){
		if(avariceProcess!=null)
			avariceProcess.destroy();
		if(m_process!=null)
			m_process.interrupt();
		m_process = null;
		m_process = new Thread(new Runnable() {
			@Override
			public void run() {
				runAvarice();
			}
		});		
	}
	
	private void runAvarice() {
		System.out.println("(!)");
		Pattern pattern = Pattern.compile("[\\w\\d\\s.,<>:-]*Downloading FLASH image to target[.]*\n$");;
		params = new ArrayList<String>();
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

		String[] commandLine = new String[params.size()];
		commandLine = params.toArray(commandLine);
		try {
			avariceProcess = Runtime.getRuntime().exec(commandLine, null, null);
			String allOutput = "";
			InputStream stream = avariceProcess.getInputStream();
			InputStreamReader isr = new InputStreamReader(stream);
			char buf[] = new char[10];
			int count;
			while((count = isr.read(buf)) != -1){
				allOutput= allOutput + new String(buf,0,count);
				for(int i=0;i<count;i++)
					System.out.print(buf[i]);
				if(pattern.matcher(allOutput).matches()){
					try {
						Thread.sleep(300);
						if(isr.ready()){
							handler.startError();
							stop();
							break;
						}
						handler.startOk();
					} catch (InterruptedException e) {
						handler.startError();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Avarice finished");
	}
	
	@Override
	protected void finalize() throws Throwable {
		stop();
		super.finalize();
	}
}
