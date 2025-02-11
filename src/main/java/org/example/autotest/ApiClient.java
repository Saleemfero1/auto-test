package org.example.autotest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.openapi.progress.ProgressIndicator;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    private static final String url = String.format(ConfigLoader.getUrlFormat(),
            ConfigLoader.getEndPoint(), ConfigLoader.getDeploymentName(), ConfigLoader.getApiVersion());

    // Increase timeouts to handle slow responses
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(ConfigLoader.getConnectTimeout(), TimeUnit.SECONDS)
            .readTimeout(ConfigLoader.getReadTimeout(), TimeUnit.SECONDS)
            .writeTimeout(ConfigLoader.getWriteTimeout(), TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    public static String sendJsonToApi(String functionality, @NotNull ProgressIndicator indicator) {
        String scenarios = "";
        logger.info("API URL: {}", url);

        indicator.setText("Generating feature file..");
        // Create JSON request body
        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode messages = requestBody.putArray("messages");

        // System message
        ObjectNode systemMessage = objectMapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are an AI expert specialized in generating highly accurate functional test cases. Read the provided user story and design document, and strictly generate comprehensive test cases in Gherkin format (Cucumber) only. Ensure the test cases cover all possible scenarios and edge cases.");
        messages.add(systemMessage);

        // User message
        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", "Generate all possible test cases using gherkin format(cucumber) for the following features: " + functionality);
        messages.add(userMessage);

        indicator.setText("Generating feature file...");

        requestBody.put("max_tokens", ConfigLoader.getMaxToken());
        logger.debug("Request JSON: {}", requestBody.toString());

        RequestBody body = RequestBody.create(requestBody.toString(), MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("api-key", ConfigLoader.getApiKey())
                .build();

        logger.info("Sending request to OpenAI API...");

        indicator.setText("Generating feature file...");

        Thread progressUpdater = new Thread(() -> {
            String baseText = "Generating feature file";
            int dotCount = 0;

            while (!Thread.currentThread().isInterrupted()) {
                String dots = ".".repeat(dotCount % 5);
                indicator.setText(baseText + dots);

                try {
                    Thread.sleep(500); // Update every 500ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                dotCount++;
            }
        });

        // Start updating progress text
        progressUpdater.start();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorResponse = response.body() != null ? response.body().string() : "Unknown error";
                logger.error("Request failed: {} - {}", response.code(), errorResponse);
                throw new RuntimeException("API request failed: " + response.code());
            }

            indicator.setText("Generating feature file....");

            String responseBody = response.body().string();
            logger.info("Response received successfully");

            ObjectNode responseJson = objectMapper.readValue(responseBody, ObjectNode.class);
            scenarios = responseJson.get("choices").get(0).get("message").get("content").asText();
            logger.debug("Extracted test cases: {}", scenarios);

            indicator.setText("Generating feature file done!");


            return scenarios;

        } catch (IOException e) {
            logger.error("Error calling Azure OpenAI: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }finally {
            // Stop progress updater thread after response is received
            progressUpdater.interrupt();
        }
    }
}
