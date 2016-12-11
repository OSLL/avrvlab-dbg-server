package avrdebug.reservation;

import java.io.IOException;
import java.util.Date;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.DateTime;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ReservationSystemConnector {
	static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private String address;
	
	public ReservationSystemConnector(String reservationApiAddress) {
		address = reservationApiAddress;
		if(!address.endsWith("/"))
			address+="/";
	}
	
	public ReservationResponse getReserveInfo(String token){
		
        GenericUrl url = new GenericUrl(address + token);
        HttpRequest request;
		try {
			request = HTTP_TRANSPORT.createRequestFactory().buildGetRequest(url);
			HttpResponse response = request.execute();
			ReservationInfo reservInfo = new ReservationInfo();
			
	        String StringResponse = response.parseAsString();
	        JsonElement jelement = new JsonParser().parse(StringResponse);
	        JsonObject  jobject = jelement.getAsJsonObject();
	        
	        JsonObject reservation = jobject.get("reservation").getAsJsonObject();
	        String startTime = reservation.get("start_datetime").toString();
	        String endTime = reservation.get("end_datetime").toString();
	        reservInfo.setStartTime(new Date(new DateTime(deleteQuotes(startTime)).getValue()));
	        reservInfo.setEndTime(new Date(new DateTime(deleteQuotes(endTime)).getValue()));
	        reservInfo.setToken(token);
	        
	        JsonObject resource = reservation.get("resource").getAsJsonObject();
	        reservInfo.setResourceId(Integer.parseInt(resource.get("id").toString()));
	        reservInfo.setResourceType(deleteQuotes(resource.get("type").toString()));

	        response.disconnect();
	        
	        return new ReservationSuccessResponse(reservInfo);
		}catch (HttpResponseException e){
    		return new ReservationErrorResponse("Reservation server return status code: "+ e.getStatusCode() + " ("+ e.getStatusMessage() +")");
		}
		catch (IOException e) {
			return new ReservationErrorResponse("IO error");
		}
		
		
	}
	
	private String deleteQuotes(String s){
		return s.substring(1, s.length()-1);
	}
	
}
