package io.funxion;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import io.funxion.hailstorm.TestCase;

public class CalcSquareLoad extends TestCase {
	private static String BASE_URL,APIKEY;

	public static void main(String[] args) {
		CalcSquareLoad testCase = new CalcSquareLoad();
		
		String url = System.getenv("BASE_URL");
		BASE_URL = url==null?"http://localhost:8080/FunxionService":url;
		System.out.println("BASE_URL="+BASE_URL);
		
		APIKEY = System.getenv("APIKEY");
		if(APIKEY==null) {
			System.out.println("Missing APIKEY env variable");
			System.exit(1);
		}
		testCase.start(args);		
	}

	@Override
	protected void execute() throws Exception {
		Thread.sleep(50);
		startStep("EXEC_CalculateSquare");
		executeCalculateSquare(APIKEY);
		endStep("EXEC_CalculateSquare");		
	}
	private void executeCalculateSquare(String APIKEY) throws Exception {
		String URL = BASE_URL + "/api/ExecService?name=CalculateSquare";
		String payload = Json.createObjectBuilder()
			.add("number", "10")
			.build().toString();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(URL))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer ".concat(APIKEY))
				.POST(BodyPublishers.ofString(payload.toString()))
				.build();
		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			throw new Exception(String.format("Error Accessing URI:ResponseCode=%d", response.statusCode()));
		}
		JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
		JsonObject object = jsonReader.readObject();
		jsonReader.close();
		debug(payload.toString().concat("|").concat(object.toString()));
		if(! (object.getInt("square") == 100)) throw new Exception("Incorrect Result");	 		
	}	
}
