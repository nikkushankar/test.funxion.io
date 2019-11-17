package io.funxion;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;
import java.util.Map;

import io.funxion.hailstorm.TestCase;

public class FormServiceTest extends TestCase {
	private static String BASE_URL = "http://localhost:8080/FunxionService";
	private static String APIKEY = "3a534509-46dd-42c7-83bf-0f67f2ed57f9";
    public static HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) throws UnsupportedEncodingException {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), "UTF-8"));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }
	public static void main(String[] args) {
		FormServiceTest testCase = new FormServiceTest();
		testCase.start(args);
	}

	@Override
	protected void execute() throws Exception {
		startStep("SubmitForm");
		submitForm(APIKEY);
		endStep("SubmitForm");
		Thread.sleep(100);
	}

	private void submitForm(String APIKEY) throws Exception {
		String URL = BASE_URL + "/api/FormService/Form2";
		Map<Object, Object> data = new HashMap<>();
        data.put("username", "abc");
        data.put("password", "123");
        data.put("custom", "secret");
        data.put("ts", System.currentTimeMillis());
		
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(URL))
				.header("Content-Type", "application/x-www-form-urlencoded")
				.header("Authorization", "Bearer ".concat(APIKEY))
				.POST(ofFormData(data))
				.build();
		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
		
	}

}
