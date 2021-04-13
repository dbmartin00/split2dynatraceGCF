package io.split.dbm.integrations.split2dynatrace;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONObject;

import com.google.cloud.functions.HttpFunction;
import com.google.gson.Gson;

public class App implements HttpFunction
{
	//	public static void 
	//	main(String[] args) throws Exception {
	public void service(com.google.cloud.functions.HttpRequest request, com.google.cloud.functions.HttpResponse response) throws Exception {
		long start = System.currentTimeMillis();
		SplitChange change = new Gson().fromJson(new InputStreamReader(request.getInputStream()), SplitChange.class);
		System.out.println("successfully parsed change in " + (System.currentTimeMillis() - start) + "ms");
//		System.out.println(change);

		Configuration config = Configuration.fromFile("split2dynatrace.config");

		JSONObject annotation = buildPostForDynatrace(change, config);

		System.out.println("DEBUG - " + annotation.toString());

		postEventToDynatrace(config, annotation);

		PrintWriter writer = new PrintWriter(response.getWriter());
		writer.println("change parsed and posted to DynaTrace");
		writer.flush();
		writer.close();
		System.out.println("finished in " + (System.currentTimeMillis() - start) + "ms");
	}

	private static JSONObject buildPostForDynatrace(SplitChange change, Configuration config)
			throws IOException {
		AttachedRules rules = AttachedRules.fromFile("attachedRules.json");

		JSONObject annotation = new JSONObject();
		annotation.put("eventType", "CUSTOM_ANNOTATION");
//		annotation.put("start", change.time - (5 * 60 * 1000)); // started five minutes ago
//		annotation.put("end", change.time);

		rules.tagRule[0].meTypes = config.entities;
		rules.tagRule[0].tags[0].key = "splitTag";
		rules.tagRule[0].tags[0].value = change.name;
		
		annotation.put("attachRules", new JSONObject(new Gson().toJson(rules))).toString(2);
		annotation.put("source", "Split.io");
		annotation.put("annotationType", "split rule change in environment " + change.environmentName);
		annotation.put("annotationDescription", change.description);
		Map<String, Object> customProperties = new TreeMap<String, Object>();
		customProperties.put("a. description", change.description);
		customProperties.put("b. editor", change.editor);
		customProperties.put("c. environmentName", change.environmentName);
		customProperties.put("d. link", change.link);
		customProperties.put("e. split name", change.name);
		customProperties.put("f. changeNumber", "" + change.changeNumber);
		customProperties.put("g. schemaVersion", "" + change.schemaVersion);
		customProperties.put("h. type", change.type);
		customProperties.put("i. definition", change.definition);
		annotation.put("customProperties", customProperties);
		
		return annotation;
	}

	private static void postEventToDynatrace(Configuration config, JSONObject annotation)
			throws IOException, InterruptedException {
		long start = System.currentTimeMillis();
		System.out.println("INFO - Sending annotations to Dynatrace");
		//            // Build Request
		HttpRequest request = HttpRequest.newBuilder(URI.create(config.dynatraceUrl + "/api/v1/events"))
				.header("Content-type", "application/json")
				.header("Authorization", "Api-Token " + config.dynatraceApiKey)
				.POST(HttpRequest.BodyPublishers.ofString(annotation.toString()))
				.build();

		// Process Response
		HttpClient httpClient = HttpClient.newHttpClient();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if(response.statusCode() >= 400) {
			System.err.printf("ERROR - Sending events to Dynatrace failed: status=%s response=%s %n", response.statusCode(), response.body());
		} else {
			System.out.println(response.statusCode());
		}
		System.out.println("INFO - finished sending annotations to Dynatrace in " + (System.currentTimeMillis() - start) + "ms");
	}
}
