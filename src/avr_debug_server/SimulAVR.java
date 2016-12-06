package avr_debug_server;

import avrdebug.communication.SimulAVRConfigs;
import avrdebug.communication.SimulAVRInitData;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SimulAVR extends Thread{
    private static String simulAvrPath = "./simulavr";
    private static String dumpFile = "simulavr.dump";

    private static boolean isDumped = false;
    private static boolean isLoaded = false;

    private static SimulAVRInitData data = null;
    
	private SimulAVRConfigs simulavrConfig;
	private String port;
	private String sketchFilename;
	private String vcdTraceFilename;
	private String vcdInputTraceFilename;
	private String cpuTraceFilename;
	private Process simulAvrProcess;
	private SimulAVRListener listener;
	private boolean needToKill;

	public void setNeedToKill(boolean needToKill) {
		this.needToKill = needToKill;
	}

	public SimulAVR(SimulAVRConfigs simulavrConfig, int port, String sketchFilename, String vcdTraceFilename, String cpuTraceFilename, SimulAVRListener listener){
		this.simulavrConfig = simulavrConfig;
		this.port = port+"";
		this.sketchFilename = sketchFilename;
		this.vcdTraceFilename = vcdTraceFilename;
		this.cpuTraceFilename = cpuTraceFilename;
		this.vcdInputTraceFilename = this.vcdTraceFilename+"-input";
		this.listener = listener;
		this.setDaemon(true);
		needToKill = false;
	}
	
	public void run(){
		ArrayList<String> params = collectOptions();
		String[] commandLine = new String[params.size()];
		commandLine = params.toArray(commandLine);
		try {
			simulAvrProcess = Runtime.getRuntime().exec(commandLine, null, null);
			InputStream stream = simulAvrProcess.getInputStream();
			InputStreamReader isr = new InputStreamReader(stream);
			char buf[] = new char[15];
			int count;
			boolean isNotified = false;
			while(true){
				if(Thread.interrupted()){
					System.out.println("Interrupted!");
					throw new InterruptedException();
				}
				if(isr.ready()){
					count = isr.read(buf);
					for(int i=0;i<count;i++)
						System.out.print(buf[i]);
				}
					
						
				try{
					simulAvrProcess.exitValue();
					break;
				}catch(IllegalThreadStateException e){
					sleep(100);
					if(!isNotified){
						listener.started();
						isNotified = true;
					}
					continue;
				}
				
			}
			System.out.println("Simulavr finished");
			listener.finishedSuccess();
		} catch (IOException e) {
			finishing();
			e.printStackTrace();
			listener.finishedBad();
		} catch (InterruptedException e) {
			finishing();
			try {
				sleep(100);
			} catch (InterruptedException e1) {
			}
			System.out.println("Simulavr interrupted");
			listener.interrupted();
		}
	}

	private static long getPidOfProcess(Process p) {
		long pid = -1;

		try {
			if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
				Field f = p.getClass().getDeclaredField("pid");
				f.setAccessible(true);
				pid = f.getLong(p);
				f.setAccessible(false);
			}
		} catch (Exception e) {
			pid = -1;
		}
		return pid;
	}

	private ArrayList<String> collectOptions(){
		ArrayList<String> params = new ArrayList<String>();
		params.add(simulAvrPath);
		params.add("--file");
		params.add(sketchFilename);
		params.add("--device");
		params.add(simulavrConfig.getSelectedMcu());
		params.add("--cpufrequency");
		params.add(simulavrConfig.getCpuFreq()+"");
		if(simulavrConfig.isVCDTraceEnable()){
			try {
				createVcdInputFile();
				params.add("-c");
				params.add("vcd:"+vcdInputTraceFilename+":"+vcdTraceFilename);
			} catch (IOException e) {
			}
		}
		if(simulavrConfig.isDebugEnable()){
			params.add("--gdbserver");
			params.add("-p");
			params.add(port);
		}
//		if(simulavrConfig.isTraceEnable()){
//			params.add("--trace");
//			params.add(cpuTraceFilename);
//		}
		if(!simulavrConfig.isDebugEnable()){
			if(simulavrConfig.getMaxRunTime()>0){
				params.add("-m");
				params.add(simulavrConfig.getMaxRunTime()+"");
			}
				
		}
		return params;
	}
	
	private void createVcdInputFile() throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(vcdInputTraceFilename));
		for (String source : simulavrConfig.getVcdSources().keySet()) {
			if (simulavrConfig.getVcdSources().get(source)) {
				bw.write(source);
				bw.newLine();
			}
		}
		bw.close();
	}
	
	private void finishing(){
		if(simulAvrProcess==null)
			return;
		try {
			if(needToKill){
				System.out.println("Kill process");
				simulAvrProcess.destroy();
				return;
			}
			System.out.println("Send ctrl+c");
			Runtime.getRuntime().exec("kill -SIGINT "+getPidOfProcess(simulAvrProcess));
			System.out.println("Ctrl+c sended");
		} catch (IOException e) {
			simulAvrProcess.destroy();
		}
//		if(simulAvrProcess!=null){
//			simulAvrProcess.destroy();
//		}
			
	}
	
	@Override
	protected void finalize() throws Throwable {
		if(simulAvrProcess!=null){
			simulAvrProcess.destroy();
		}
		super.finalize();
	}
	
    /**
     * @return
     *  SimulAVR map that contains devices as keys
     *  and lists of VCD-sources as values
     *
     * @throws Exception
     */
    public static SimulAVRInitData getInitData()
            throws Exception {
        // If there is already loaded in memory info
        if (isLoaded && SimulAVR.data != null) {
            return SimulAVR.data;
        }

        // If there is no cache file that contain info
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        if (checkDumped()) {
            try {
                fis = new FileInputStream(SimulAVR.dumpFile);
                ois = new ObjectInputStream(fis);

                SimulAVRInitData data = (SimulAVRInitData) ois.readObject();
                SimulAVR.data = data;
                return data;
            } catch (IOException e) {
                // Just go forward
            } catch (ClassNotFoundException e) {
                // Just go forward
            } finally {
                if (ois != null) {
                    ois.close();
                }
                if (fis != null) {
                    fis.close();
                }
            }
        }


        // Need to parse init data from simulavr help
        try {
            return loadInitData();
        } catch(Exception e) {
            throw e;
        }
    }


    public static void setAvrFile(String fileName) {
        SimulAVR.simulAvrPath = fileName;
    }

    public static String getAvrFile() {
        return SimulAVR.simulAvrPath;
    }

    public static void setDumpFile(String filename) {
        SimulAVR.dumpFile = filename;
    }

    public static String getDumpFile() {
        return SimulAVR.dumpFile;
    }

    /**
     * Update init data in cache.
     * For example, when new version of simulAVR was installed
     *
     * @throws Exception
     */
    public static void updateInitData() throws Exception {
        try {
            loadInitData();
        } catch (Exception e) {
            throw new Exception("Failed update cache: " + e.getMessage());
        }
    }

    /**
     * Parse init data from simulavr, then stores
     * it in cache and loads in memory.
     *
     * @return Loaded init data
     * @throws Exception
     */
    private static SimulAVRInitData loadInitData() throws Exception {
        InputStreamReader is = null;
        BufferedReader input = null;
        try {
            Process avrProc = Runtime.getRuntime().exec(SimulAVR.simulAvrPath + " -h");
            avrProc.waitFor();

            is = new InputStreamReader(avrProc.getInputStream());
            input = new BufferedReader(is);

            HashMap<String, ArrayList<String>> map = new HashMap<>();

            String line;
            while ((line = input.readLine()) != null) {
                if (line.equals("Currently available device types:") ||
                    line.equals("Supported devices:")) { // Line differs in some versions

                    // Read device types
                    while ((line = input.readLine()) != null) {
                    	if(line.equals(""))
                    		continue;
                        map.put(line.trim(), null);
                    }
                    break;
                }
            }

            String execString = SimulAVR.simulAvrPath + " -o - -d ";
            ArrayList<String> list;
            for (Map.Entry<String, ArrayList<String>> entry : map.entrySet()) {
                if (entry.getKey().equals("")) {
                    continue;
                }
                avrProc = Runtime.getRuntime().exec(execString + entry.getKey());
                avrProc.waitFor();

                is = new InputStreamReader(avrProc.getInputStream());
                input = new BufferedReader(is);

                line = input.readLine(); // Non informative line
                if (line == null) {
                    continue;//throw new IOException();
                }

                list = new ArrayList<>();
                while ((line = input.readLine()) != null) {
                    list.add(line);
                }

                entry.setValue(list);
            }

            SimulAVR.data = new SimulAVRInitData();
            SimulAVR.data.setMcuVCDSources(map);

            SimulAVR.isLoaded = true;

            dumpInitData();

            return data;
        } catch (IOException e) {
            throw new Exception("Failed to execute simulavr process: " + SimulAVR.simulAvrPath);
        } finally {
            if (is != null) {
                is.close();
            }
            if (input != null) {
                input.close();
            }
        }
    }

    /**
     * Creates file that contains all init data for SimulAVR
     */
    private static void dumpInitData() throws Exception {
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try {
            fos = new FileOutputStream(SimulAVR.dumpFile);
            out = new ObjectOutputStream(fos);

            out.writeObject(SimulAVR.data);

            SimulAVR.isDumped = true;
        } catch (FileNotFoundException e) {
            throw new Exception("Dump file not writable!");
        } catch (IOException e) {
            throw new Exception("Failed to open cache file");
        } finally {
            if (out != null) {
                out.close();
            }
        	if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * Checks cache file
     *
     * @return Return true if cache available else false
     */
    private static boolean checkDumped() {
        if (SimulAVR.isDumped) {
            return true;
        }

        File file = new File(dumpFile);
        if(!file.exists() || file.isDirectory() || file.length() == 0)
        	return false;

        SimulAVR.isDumped = true;
        return true;
    }
}
