package avrdebug.reservation;

public class ReservationSuccessResponse extends ReservationResponse {

	private ReservationInfo reservationInfo = null;
	public ReservationSuccessResponse() {
	}
	
	public ReservationSuccessResponse(ReservationInfo reservationInfo) {
		this.reservationInfo = reservationInfo;
	}

	public ReservationInfo getReservationInfo() {
		return reservationInfo;
	}

	public void setReservationInfo(ReservationInfo reservationInfo) {
		this.reservationInfo = reservationInfo;
	}
	
}
