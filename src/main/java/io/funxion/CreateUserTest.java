package io.funxion;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Instant;
import java.util.Base64;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import io.funxion.hailstorm.TestCase;

public class CreateUserTest extends TestCase {
	//private static String BASE_URL = "http://localhost:8080/FunxionService";
	private static String BASE_URL = "https://www.funxion.io";
			
	public static void main(String[] args) {
		CreateUserTest testCase = new CreateUserTest();
		testCase.start(args);
	}

	@Override
	protected void execute() throws Exception {

		startStep("STEP1");
		String email = registerUser();
		endStep("STEP1");
		debug("Registered Email :".concat(email));
		Thread.sleep(100);

		startStep("STEP2");
		String token = generateToken(email);
		endStep("STEP2");
		debug("Generated Token : ".concat(token));
		Thread.sleep(100);
		
		startStep("STEP3");
		Integer count = getTokenCount(email);
		endStep("STEP3");
		debug("TokenList Count : ".concat(count.toString()));
		Thread.sleep(100);
		
		startStep("STEP4");
		Boolean bActive = deactivateToken(email,token);
		endStep("STEP4");
		debug("Current Token Status : ".concat(bActive.toString()));
		Thread.sleep(100);
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
			endStepWithError("STEP1");
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
			endStepWithError("STEP2");
			throw new Exception(String.format("Error Accessing URI:ResponseCode=%d", response.statusCode()));
		}
		return token;
	}
	
	private int getTokenCount(String email) throws Exception {
		String payload = Json.createObjectBuilder()
			.build().toString();
		byte[] encoded = Base64.getEncoder().encode(email.concat(":welcome1").getBytes());
		
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(BASE_URL + "/api/TokenService?action=GET_APIKEY_LIST"))
			.header("Content-Type", "application/json")
			.header("Authorization", "Basic ".concat(new String(encoded)))
			.POST(BodyPublishers.ofString(payload))
			.build();
		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
		JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
		JsonObject object = jsonReader.readObject();
		jsonReader.close();
		if(!"SUCCESS".equalsIgnoreCase(object.getString("status"))) throw new Exception("REQUEST FAILED");
		Integer tokenCount = object.getJsonArray("data").size(); 
		if(!tokenCount.equals(1))throw new Exception("Token Count is not 1 as Expected");
		
		if (response.statusCode() != 200) {
			endStepWithError("STEP2");
			throw new Exception(String.format("Error Accessing URI:ResponseCode=%d", response.statusCode()));
		}
		return tokenCount;
	}
	
	private Boolean deactivateToken(String email, String token) throws Exception {
		String payload = Json.createObjectBuilder()
			.add("token", token)
			.build().toString();
		byte[] encoded = Base64.getEncoder().encode(email.concat(":welcome1").getBytes());
		
		HttpRequest request1 = HttpRequest.newBuilder()
			.uri(URI.create(BASE_URL + "/api/TokenService?action=DEACTIVATE_APIKEY"))
			.header("Content-Type", "application/json")
			.header("Authorization", "Basic ".concat(new String(encoded)))
			.POST(BodyPublishers.ofString(payload))
			.build();
		HttpResponse<String> response1 = httpClient.send(request1, BodyHandlers.ofString());
		JsonReader jsonReader1 = Json.createReader(new StringReader(response1.body()));
		JsonObject object1 = jsonReader1.readObject();
		jsonReader1.close();
		if(!"SUCCESS".equalsIgnoreCase(object1.getString("status"))) throw new Exception("REQUEST FAILED");
		Integer deactivateCount = object1.getJsonObject("data").getInt("deactivateCount"); 
		if(!deactivateCount.equals(1))throw new Exception("Token Count is not 1 as Expected");
		
		if (response1.statusCode() != 200) {
			endStepWithError("STEP2");
			throw new Exception(String.format("Error Accessing URI:ResponseCode=%d", response1.statusCode()));
		}
		
		HttpRequest request2 = HttpRequest.newBuilder()
			.uri(URI.create(BASE_URL + "/api/TokenService?action=GET_APIKEY_LIST"))
			.header("Content-Type", "application/json")
			.header("Authorization", "Basic ".concat(new String(encoded)))
			.POST(BodyPublishers.ofString(payload))
			.build();
		HttpResponse<String> response2 = httpClient.send(request2, BodyHandlers.ofString());
		JsonReader jsonReader2 = Json.createReader(new StringReader(response2.body()));
		JsonObject object2 = jsonReader2.readObject();
		jsonReader2.close();
		if(!"SUCCESS".equalsIgnoreCase(object2.getString("status"))) throw new Exception("REQUEST FAILED");
		Boolean bValid = object2.getJsonArray("data").getJsonObject(0).getBoolean("valid"); 
		if(bValid)throw new Exception("Token Supposed to be invalid");
		
		return bValid;
	}
}
