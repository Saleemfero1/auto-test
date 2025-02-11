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
import java.util.Arrays;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class DeepGenerate {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(DeepGenerate.class);
    private static final String url = String.format(ConfigLoader.getUrlFormat(),
            ConfigLoader.getEndPoint(), ConfigLoader.getDeploymentName(), ConfigLoader.getApiVersion());

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(ConfigLoader.getConnectTimeout(), TimeUnit.SECONDS)
            .readTimeout(ConfigLoader.getReadTimeout(), TimeUnit.SECONDS)
            .writeTimeout(ConfigLoader.getWriteTimeout(), TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    public static final String SYSTEM_CONTENT_PROMPT = "You are an AI expert specialized in generating detailed Gherkin test cases.";

    public static String fetchTestCaseHeadings(String functionality,  @NotNull ProgressIndicator indicator) {

        indicator.setText("Generating feature file..");

        // Prepare request body
        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode messages = requestBody.putArray("messages");

        // System message
        ObjectNode systemMessage = objectMapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are an AI expert specialized in extracting all possible completely Gherkin test case headings for the given input.");
        messages.add(systemMessage);

        // User message
        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", "Note: Extract and return only the Gherkin test case headings in an array and don't give Markdown code block give as a string only and any comments only give array for the following features: " + functionality);
        messages.add(userMessage);

        requestBody.put("max_tokens", ConfigLoader.getMaxToken());
        logger.debug("Request JSON: {}", requestBody);

        indicator.setText("Generating feature file...");
        // Prepare request
        RequestBody body = RequestBody.create(requestBody.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("api-key", ConfigLoader.getApiKey())
                .build();

        indicator.setText("Generating Feature file....");
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

        String scenarios;
        List<String> scenarioList;
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return null; // Return empty list on failure
            }
            ObjectNode responseJson = objectMapper.readValue(response.body().string(), ObjectNode.class);
            scenarios = responseJson.get("choices").get(0).get("message").get("content").asText();
            ObjectMapper objectMapper = new ObjectMapper();
            String[] scenarioArray = objectMapper.readValue(scenarios, String[].class);
            scenarioList = Arrays.asList(scenarioArray);

        } catch (IOException e) {
            return e.getMessage();
        }finally {
            // Stop progress updater thread after response is received
            progressUpdater.interrupt();
        }

        indicator.setText("Generating feature file.....");
        String fullTestCase;
        fullTestCase = generateGherkinTestCases(scenarioList,functionality, indicator);

        return fullTestCase;
    }

    public static String generateGherkinTestCases(List<String> headings, String functionality, @NotNull ProgressIndicator indicator) {
        if (headings.isEmpty()) {
            logger.warn("No headings provided for test case generation.");
            return "";
        }

        indicator.setText("Generating feature file......");

        StringBuilder allTestCases = new StringBuilder();

        for (int i = 0; i < headings.size(); i += 3) {
            indicator.setText("Generating testcase "+i + 1);
            List<String> batch = headings.subList(i, Math.min(i + 3, headings.size()));

            // Prepare request body
            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode messages = requestBody.putArray("messages");

            // System message
            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", SYSTEM_CONTENT_PROMPT);
            messages.add(systemMessage);
            indicator.setText("Generating testcase1 "+(i + 1)+".");
            // User message
            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", "Generate Gherkin test cases clearly for the following headings:\n" + String.join("\n", batch) + "\nFunctionality: " + functionality);
            messages.add(userMessage);

            requestBody.put("max_tokens", ConfigLoader.getMaxToken());
            logger.debug("Request JSON for test case generation: {}", requestBody.toString());

            indicator.setText("Generating testcase "+(i + 1)+"..");
            // Prepare request
            RequestBody body = RequestBody.create(requestBody.toString(), MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("api-key", ConfigLoader.getApiKey())
                    .build();

            var finalI = i+1;
            Thread progressUpdater = new Thread(() -> {
                String baseText = "Generating testcase "+ finalI;
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
                    logger.error("Test case generation API request failed with status code: {}", response.code());
                    continue;
                }

                indicator.setText("Generating testcase "+(i + 1)+"...");
                ObjectNode responseJson = objectMapper.readValue(response.body().string(), ObjectNode.class);
                String generatedTestCases = responseJson.get("choices").get(0).get("message").get("content").asText();

                allTestCases.append(generatedTestCases).append("\n\n");

            } catch (IOException e) {
                logger.error("Error generating test cases: {}", e.getMessage());
            }finally {
                // Stop progress updater thread after response is received
                progressUpdater.interrupt();
            }
        }

        indicator.setText("Generating testcases Done!");

        return allTestCases.toString().trim();
    }
}
