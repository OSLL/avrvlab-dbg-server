package avrdebug.reservation;

public class ReservationErrorResponse extends ReservationResponse {

	private String message;
	
	public ReservationErrorResponse(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
	
}
