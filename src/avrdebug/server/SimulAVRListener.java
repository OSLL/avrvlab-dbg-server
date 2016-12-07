package avrdebug.server;

public interface SimulAVRListener {
	
	public void started();
	
	public void finishedSuccess();
	
	public void finishedBad();
	
	public void interrupted();
	
}
