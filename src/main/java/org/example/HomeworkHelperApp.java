package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;
import java.util.Scanner;

public class HomeworkHelperApp {

  //  private static final String OPENAI_API_KEY = System.getenv("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");
  private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");

    public static void main(String[] args) {
        if (OPENAI_API_KEY == null || OPENAI_API_KEY.isEmpty()) {
            System.out.println("Please set the OPENAI_API_KEY environment variable.");
            System.exit(1);
        }

        Scanner scanner = new Scanner(System.in);

        System.out.println("Homework Helper: Ask me anything related to your homework. Type 'exit' to quit.");

        while (true) {
            System.out.print("You: ");
            String userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("exit")) {
                System.out.println("Exiting the homework helper.");
                break;
            }

            String sanitizedInput = userInput.replaceAll("\"", "\"");
            String homeworkResponse = getHomeworkResponse(sanitizedInput);

            System.out.println("Homework Helper: " + homeworkResponse);
            System.out.println();


            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getHomeworkResponse(String userInput) {
        OkHttpClient client = new OkHttpClient();
        String openaiEndpoint = "https://api.openai.com/v1/engines/dgpt-3.5-turbo-1106/completions";

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(mediaType, "{\"prompt\": \"" + userInput + "\", \"max_tokens\": 150}");

        Request request = new Request.Builder()
                .url(openaiEndpoint)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                assert response.body() != null;
                String responseBody = response.body().string();
                System.out.println("Unexpected response code: " + response.code());
                System.out.println("Response Body: " + responseBody);
                throw new IOException("Unexpected response code: " + response);
            }

            assert response.body() != null;
            String responseBody = response.body().string();
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray choices = jsonResponse.getAsJsonArray("choices");

            if (!choices.isEmpty()) {
                return choices.get(0).getAsJsonObject().get("text").getAsString();
            } else {
                return "I'm sorry, I couldn't generate a response.";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Error fetching response from the homework helper.";
        }
    }
}
