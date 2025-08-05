package uk.cloudmc.swrc.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class NYTAPI {
    private static final HttpClient client = HttpClient.newHttpClient();

    public static String getWordleAnswer(int year, int month, int day) {
        String url = String.format("https://www.nytimes.com/svc/wordle/v2/%04d-%02d-%02d.json", year, month, day);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                return json.get("solution").getAsString();
            } else {
                System.err.println("Failed to fetch Wordle answer: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }
}
