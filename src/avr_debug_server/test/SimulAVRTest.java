package avr_debug_server.test;

import org.junit.Test;
import avr_debug_server.SimulAVR;
import avrdebug.communication.SimulAVRInitData;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by jorgen on 01.11.16.
 */
public class SimulAVRTest {
    @Test
    public void getInitData() throws Exception {
        SimulAVR.setAvrFile("/home/constantin/simulavr");

        SimulAVRInitData data = null;
        data = SimulAVR.getInitData();

        assertNotNull(data);

        SimulAVR.updateInitData();
        data = SimulAVR.getInitData();

        assertNotNull(data);

        SimulAVR.setDumpFile("new_dump.dump");

        data = SimulAVR.getInitData();

        assertNotNull(data);
	
    }


    @Test
    public void updateInitData() throws Exception {
        File file = new File(SimulAVR.getDumpFile());
        file.delete();

        assertFalse(file.exists());

        // Should update data and dump it
        SimulAVR.updateInitData();

        assertTrue(file.exists());

        // Should not return null after update
        SimulAVRInitData data = SimulAVR.getInitData();

        assertNotNull(data);
    }

}
