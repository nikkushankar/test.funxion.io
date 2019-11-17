package io.funxion;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Instant;
import java.util.Base64;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import io.funxion.hailstorm.TestCase;

public class ExecuteFunctionTest extends TestCase {
	private static String BASE_URL;

	public static void main(String[] args) {
		ExecuteFunctionTest testCase = new ExecuteFunctionTest();
		String url = System.getenv("BASE_URL");
		BASE_URL = url==null?"http://localhost:8080/FunxionService":url;
		
		testCase.start(args);
		
	}

	@Override
	protected void execute() throws Exception {
		startStep("REGISTER");
		String email = registerUser();
		endStep("REGISTER");
		debug("Registered Email :".concat(email));
		Thread.sleep(100);

		startStep("GENTOKEN");
		String token = generateToken(email);
		endStep("GENTOKEN");
		debug("Generated Token : ".concat(token));
		Thread.sleep(100);
		
		startStep("EXEC_PrimeCalculator");
		executePrimeCalculator(token);
		endStep("EXEC_PrimeCalculator");
		Thread.sleep(100);
		
		startStep("EXEC_CalculateSquare");
		executeCalculateSquare(token);
		endStep("EXEC_CalculateSquare");
		Thread.sleep(100);
		
		startStep("EXEC_DataReadWrite");
		executeDataReadWrite(token);
		endStep("EXEC_DataReadWrite");
		Thread.sleep(100);
		
		startStep("EXEC_InternalFunctionCaller");
		executeInternalFunctionCaller(token);
		endStep("EXEC_InternalFunctionCaller");
		Thread.sleep(100);
	}
	private void executeInternalFunctionCaller(String APIKEY) throws Exception {
		String URL = BASE_URL + "/api/ExecService?name=InternalFunctionCaller";
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
	private void executeDataReadWrite(String APIKEY) throws Exception {
		String URL = BASE_URL + "/api/ExecService?name=DataReadWrite";
		String payload = Json.createObjectBuilder()
			.add("price", 56)
			.add("symbol", "ORCL")
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
		if(! (object.getInt("price") == 56)) throw new Exception("Incorrect Result"); 		
	}
	
	private void executePrimeCalculator(String APIKEY) throws Exception {
		String URL = BASE_URL + "/api/ExecService?name=PrimeCalculator";
		String payload = Json.createObjectBuilder()
			.add("counter", "10")
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
		if(! (object.getInt("nextprime") == 31)) throw new Exception("Incorrect Result"); 		
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
	private String registerUser() throws Exception {
		String email = String.format("rguptan%d@funxion.io",Instant.now().toEpochMilli());
		String payload = Json.createObjectBuilder()
			.add("addSamples", "on")
			.add("inputEmail", email)
			.add("inputPassword", "welcome1")
			.build().toString();
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(BASE_URL + "/api/public/AuthService?action=REGISTER"))
			.header("Content-Type", "application/json")
			.POST(BodyPublishers.ofString(payload))
			.build();
		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
		JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
		JsonObject object = jsonReader.readObject();
		jsonReader.close();
		if(!"SUCCESS".equalsIgnoreCase(object.getString("status"))) throw new Exception("REQUEST FAILED"); 
		
		if (response.statusCode() != 200) {
			throw new Exception(String.format("Error Accessing URI:ResponseCode=%d", response.statusCode()));
		}
		return email;
	}

	private String generateToken(String email) throws Exception {
		String payload = Json.createObjectBuilder().build().toString();
		byte[] encoded = Base64.getEncoder().encode(email.concat(":welcome1").getBytes());
		
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(BASE_URL + "/api/TokenService?action=GENERATE_APIKEY"))
			.header("Content-Type", "application/json")
			.header("Authorization", "Basic ".concat(new String(encoded)))
			.POST(BodyPublishers.ofString(payload))
			.build();
		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
		JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
		JsonObject object = jsonReader.readObject();
		jsonReader.close();
		if(!"SUCCESS".equalsIgnoreCase(object.getString("status"))) throw new Exception("REQUEST FAILED");
		String token = object.getJsonObject("data").getString("token"); 
		
		if (response.statusCode() != 200) {
			throw new Exception(String.format("Error Accessing URI:ResponseCode=%d", response.statusCode()));
		}
		return token;
	}	
}
