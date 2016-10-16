package avr_debug_server;

import avrdebug.communication.SimulAVRInitData;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SimulAVR {
    private static String avrFile = "simulavr";
    private static String dumpFile = "simulavr.dump";

    private static boolean isDumped = false;
    private static boolean isLoaded = false;

    private static SimulAVRInitData data = null;

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
        if (isLoaded) {
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
        SimulAVR.avrFile = fileName;
    }

    public static String getAvrFile() {
        return SimulAVR.avrFile;
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
            Process avrProc = Runtime.getRuntime().exec(SimulAVR.avrFile + " -h");
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
                        map.put(line.trim(), null);
                    }
                    break;
                }
            }

            String execString = SimulAVR.avrFile + " -o - -d ";
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
                    throw new IOException();
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
            throw new Exception("Failed to execute simulavr process: " + SimulAVR.avrFile);
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
            throw e;
        } catch (IOException e) {
            throw new Exception("Failed to open cache file");
        } finally {
            if (fos != null) {
                fos.close();
            }
            if (out != null) {
                out.close();
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

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(SimulAVR.dumpFile);
        } catch (FileNotFoundException e) {
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    return false;
                }
            }
        }

        SimulAVR.isDumped = true;
        return true;
    }
}
