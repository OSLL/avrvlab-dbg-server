package avrdebug.server;

public interface AvariceListener {
	
	public void successfulStart();
	
	public void unsuccessfulStart(String reason);
	
	public void avariceFinished();
}
