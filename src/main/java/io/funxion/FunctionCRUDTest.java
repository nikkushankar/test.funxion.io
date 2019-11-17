package io.funxion;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Base64;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import io.funxion.hailstorm.TestCase;

public class FunctionCRUDTest extends TestCase {
	private static String BASE_URL = "http://localhost:8080/FunxionService";
	private static String APIKEY = "3a534509-46dd-42c7-83bf-0f67f2ed57f9";

	public static void main(String[] args) {
		FunctionCRUDTest testCase = new FunctionCRUDTest();
		testCase.start(args);
	}

	@Override
	protected void execute() throws Exception {
		startStep("SAVE_FUNCTION");
		String uuid = saveFunction(APIKEY);
		endStep("SAVE_FUNCTION");
		Thread.sleep(100);

		startStep("READ_FUNCTION_UUID");
		readFunctionByUUID(APIKEY,uuid);
		endStep("READ_FUNCTION_UUID");
		Thread.sleep(100);

		startStep("READ_FUNCTION_BYNAME");
		readFunctionByName(APIKEY,"CalculateSquare");
		endStep("READ_FUNCTION_BYNAME");
		Thread.sleep(100);

		startStep("UPDATE_FUNCTION");
		updateFunction(APIKEY,uuid);
		endStep("UPDATE_FUNCTION");
		Thread.sleep(100);

		startStep("READ_UPDATED_FUNCTION");
		readFunctionByUUID(APIKEY,uuid);
		endStep("READ_UPDATED_FUNCTION");
		Thread.sleep(100);

		startStep("DELETE_FUNCTION");
		deleteFunction(APIKEY,uuid);
		endStep("DELETE_FUNCTION");
		Thread.sleep(100);
		
		startStep("READ_DELETED_FUNCTION");
		readFunctionByUUID(APIKEY,uuid);
		endStep("READ_DELETED_FUNCTION");
		Thread.sleep(100);

	}
	private void readFunctionByUUID(String APIKEY, String uuid) throws Exception {
		String URL = BASE_URL + "/api/FunctionService?action=GET_FUNCTION";
		String payload = Json.createObjectBuilder()
				.add("uuid",uuid)
				.build().toString();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(URL))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer ".concat(APIKEY))
				.POST(BodyPublishers.ofString(payload))
				.build();
			HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
			debug("Response Code :"+response.statusCode());
			JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
			JsonObject object = jsonReader.readObject();
			jsonReader.close();
			debug(object.toString());
	}
	private void readFunctionByName(String APIKEY, String name) throws Exception {
		String URL = BASE_URL + "/api/FunctionService?action=GET_FUNCTION";
		String payload = Json.createObjectBuilder()
				.add("name",name)
				.build().toString();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(URL))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer ".concat(APIKEY))
				.POST(BodyPublishers.ofString(payload))
				.build();
			HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
			debug("Response Code :"+response.statusCode());
			JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
			JsonObject object = jsonReader.readObject();
			jsonReader.close();
			debug(object.toString());
	}
	private void updateFunction(String APIKEY, String uuid) throws Exception {
		String URL = BASE_URL + "/api/FunctionService?action=SAVE_FUNCTION";
		String encodedFn = new String(Base64.getEncoder().encode("param => {};".getBytes()));
		String payload = Json.createObjectBuilder()
				.add("name", "TestFunction")
				.add("tags", "DEMO,TEST,TEST2")
				.add("language", "javascript")
				.add("code", encodedFn)
				.add("testcases", "[{\"input\":{},\"output\":{}},{\"input\": {},\"output\": {}}]")
				.add("active","true")
				.add("uuid",uuid)
				.build().toString();

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(URL))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer ".concat(APIKEY))
				.POST(BodyPublishers.ofString(payload))
				.build();
			HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
			debug("Response Code :"+response.statusCode());
			JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
			JsonObject object = jsonReader.readObject();
			jsonReader.close();
			debug(object.toString());
			
			object.getJsonObject("data").getString("uuid");
	}
	private void deleteFunction(String APIKEY,String uuid) throws Exception {
		String URL = BASE_URL + "/api/FunctionService?action=DELETE_FUNCTION";
		String encodedFn = new String(Base64.getEncoder().encode("param => {};".getBytes()));
		String payload = Json.createObjectBuilder()
				.add("uuid",uuid)
				.build().toString();

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(URL))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer ".concat(APIKEY))
				.POST(BodyPublishers.ofString(payload))
				.build();
			HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
			debug("Response Code :"+response.statusCode());
			JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
			JsonObject object = jsonReader.readObject();
			jsonReader.close();
			debug(object.toString());			
	}
	private String saveFunction(String APIKEY) throws Exception {
		String URL = BASE_URL + "/api/FunctionService?action=SAVE_FUNCTION";
		String encodedFn = new String(Base64.getEncoder().encode("param => {};".getBytes()));
		String payload = Json.createObjectBuilder()
				.add("name", "TestFunction")
				.add("tags", "DEMO,TEST")
				.add("language", "javascript")
				.add("code", encodedFn)
				.add("testcases", "[{\"input\":{},\"output\":{}},{\"input\": {},\"output\": {}}]")
				.add("active","true")
				.add("uuid","")
				.build().toString();

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(URL))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer ".concat(APIKEY))
				.POST(BodyPublishers.ofString(payload))
				.build();
			HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
			debug("Response Code :"+response.statusCode());
			JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
			JsonObject object = jsonReader.readObject();
			jsonReader.close();
			debug(object.toString());
			
			String uuid = object.getJsonObject("data").getString("uuid");
			return uuid;
	}
}
