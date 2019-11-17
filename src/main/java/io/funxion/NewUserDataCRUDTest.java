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
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import io.funxion.hailstorm.TestCase;

public class NewUserDataCRUDTest extends TestCase {
	private static String BASE_URL = "http://localhost:8080/FunxionService";

	public static void main(String[] args) {
		NewUserDataCRUDTest testCase = new NewUserDataCRUDTest();
		String url = System.getenv("BASE_URL");
		if(url != null)BASE_URL=url;
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
		
		startStep("CREATEBOOK");
		JsonObject book = createBook(token);
		endStep("CREATEBOOK");
		debug("Book ISBN : ".concat(book.getString("identifier")));
		Thread.sleep(100);
		
		startStep("READ");
		String price = readBook(token,book.getString("identifier"));
		Assert(price,"15.49","UnExpected Price");
		endStep("READ");
		debug("Book Price is : ".concat(price));
		Thread.sleep(100);
		
		startStep("UPDATE");
		Integer dbId = updateBook(token,book.getString("identifier"));
		endStep("UPDATE");
		Assert(dbId,"2","UnExpected ID");
		Thread.sleep(100);

		startStep("READUPDATED");
		String newprice = readBook(token,book.getString("identifier"));
		Assert(newprice,"20.00","UnExpected Price");
		endStep("READUPDATED");
		debug("Book Price is : ".concat(newprice));
		Thread.sleep(100);
		
		startStep("DELETE");
		Integer count = deleteBook(token,book.getString("identifier"));
		Assert(count,1,"UnExpected Count");
		endStep("DELETE");
		Thread.sleep(100);
		
		startStep("DROP");
		Integer dropCount = deleteBookType(token);
		Assert(dropCount,1,"UnExpected Count");
		endStep("DROP");
		Thread.sleep(100);		
	}

	private Integer deleteBookType(String token) throws Exception {
		String URL = BASE_URL + "/api/DataService?action=DELETE_TYPE";
		String payload = Json.createObjectBuilder()
				.add("type", "BOOK")
				.build().toString();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(URL))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer ".concat(new String(token)))
				.POST(BodyPublishers.ofString(payload))
				.build();
		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			endStepWithError("STEP8");
			if(response.statusCode() == 400) {
				JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
				JsonObject respObject = jsonReader.readObject();
				jsonReader.close();
				throw new Exception(String.format("ResponseCode=%d,Error Message=%s", response.statusCode(),respObject.getString("message")));
			}
			throw new Exception(String.format("Error Accessing URI:ResponseCode=%d", response.statusCode()));
		}				
		JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
		JsonObject respObject = jsonReader.readObject();
		jsonReader.close();
		int dropCount = Integer.parseInt(respObject.getString("count"));
		
		URL = BASE_URL + "/api/DataService?action=LIST_TYPES";
		payload = Json.createObjectBuilder()
				.build().toString();
		HttpRequest listRequest = HttpRequest.newBuilder()
				.uri(URI.create(URL))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer ".concat(new String(token)))
				.POST(BodyPublishers.ofString(payload))
				.build();
		HttpResponse<String> listResponse = httpClient.send(listRequest, BodyHandlers.ofString());
		JsonReader listReader = Json.createReader(new StringReader(listResponse.body()));
		JsonObject listRespObject = listReader.readObject();
		listReader.close();
		JsonArray types = listRespObject.getJsonArray("types");
		if(types.contains("BOOK")) throw new Exception("Book Entity Not deleted");
		if (response.statusCode() != 200) {
			endStepWithError("STEP8");
			throw new Exception(String.format("Error Accessing URI:ResponseCode=%d", response.statusCode()));
		}				
		
		return dropCount;
	}

	private Integer deleteBook(String token, String identifier) throws Exception {
		String URL = BASE_URL + "/api/DataService?action=DELETE";
		String payload = Json.createObjectBuilder()
				.add("type", "BOOK")
				.add("identifier", identifier)
				.build().toString();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(URL))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer ".concat(new String(token)))
				.POST(BodyPublishers.ofString(payload))
				.build();
		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			endStepWithError("STEP7");
			throw new Exception(String.format("Error Accessing URI:ResponseCode=%d", response.statusCode()));
		}
		JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
		JsonObject respObject = jsonReader.readObject();
		jsonReader.close();
		int deleteCount = Integer.parseInt(respObject.getString("count"));
		
		return deleteCount;
	}

	private Integer updateBook(String token, String identifier) throws Exception{
		String URL = BASE_URL + "/api/DataService?action=UPDATE";
		JsonObjectBuilder attributes = Json.createObjectBuilder()
				.add("name", "Pet Sematory")
				.add("price", "20.00")
				.add("author", "Stephen King");
		JsonObject payload = Json.createObjectBuilder()
				.add("type", "BOOK")
				.add("identifier", identifier)
				.add("attributes", attributes)
				.build();

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(URL))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer ".concat(new String(token)))
				.POST(BodyPublishers.ofString(payload.toString()))
				.build();
		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			endStepWithError("STEP4");
			throw new Exception(String.format("Error Accessing URI:ResponseCode=%d", response.statusCode()));
		}
		JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
		JsonObject respObject = jsonReader.readObject();
		jsonReader.close();		
		return Integer.parseInt(respObject.getString("identifier"));
	}

	private String readBook(String token, String identifier) throws Exception {
		String URL = BASE_URL + "/api/DataService?action=READ";
		String payload = Json.createObjectBuilder()
				.add("type", "BOOK")
				.add("identifier", identifier)
				.build().toString();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(URL))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer ".concat(new String(token)))
				.POST(BodyPublishers.ofString(payload))
				.build();
		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
		JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
		JsonObject respObject = jsonReader.readObject();
		jsonReader.close();
		
		JsonReader attrReader = Json.createReader(new StringReader(respObject.getString("attributes")));
		String price = attrReader.readObject().getString("price");
		attrReader.close();
		
		if (response.statusCode() != 200) {
			endStepWithError("STEP4");
			throw new Exception(String.format("Error Accessing URI:ResponseCode=%d", response.statusCode()));
		}
		return price;		
	}

	private JsonObject createBook(String token) throws Exception{
		String URL = BASE_URL + "/api/DataService?action=CREATE";
		String identifier = Long.toString(Instant.now().getEpochSecond());
		debug(identifier);
		
		JsonObjectBuilder attributes = Json.createObjectBuilder()
				.add("name", "Pet Sematory")
				.add("price", "15.49")
				.add("author", "Stephen King");
		JsonObject payload = Json.createObjectBuilder()
				.add("type", "BOOK")
				.add("identifier", identifier)
				.add("attributes", attributes)
				.build();
		
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(URL))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer ".concat(new String(token)))
				.POST(BodyPublishers.ofString(payload.toString()))
				.build();
		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
		JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
		JsonObject object = jsonReader.readObject();
		debug(object.toString());
		jsonReader.close();
		
		if (response.statusCode() != 200) {
			endStepWithError("STEP1");
			throw new Exception(String.format("Error Accessing URI:ResponseCode=%d", response.statusCode()));
		}
		return payload;
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
}
