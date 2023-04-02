package com.test.testCaseGenerator;

import okhttp3.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;

public class ChatGPTTestCaseGenerator {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-3.5-turbo";
    private static final String ORGANIZATION_ID = "org-3jUhNUCnTErEgzfr9YOolBK5";
    private static final String API_KEY = "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";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("ChatGPT Swing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        Container container = frame.getContentPane();
        container.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new BorderLayout());
        JLabel inputLabel = new JLabel("Please provide Usecase");
        inputPanel.add(inputLabel, BorderLayout.NORTH);

        JTextArea inputArea = new JTextArea(5, 30);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);

        container.add(inputPanel, BorderLayout.NORTH);

        JPanel outputPanel = new JPanel(new BorderLayout());
        JLabel outputLabel = new JLabel("Generated TestCase");
        outputPanel.add(outputLabel, BorderLayout.NORTH);

        JTextArea outputArea = new JTextArea();
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setEditable(false);
        outputPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        container.add(outputPanel, BorderLayout.CENTER);

        JButton generateTestCaseButton = new JButton("Generate TestCase");
        generateTestCaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = inputArea.getText();
                String response = sendRequestToGPT(message);
                outputArea.setText(response);
            }
        });
        container.add(generateTestCaseButton, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private static String sendRequestToGPT(String message) {
        String prefixedMessage = "Please generate testcase for the provided usecase which generates, 1. Description 2. Steps 3. Expected Result\n" + message;
        prefixedMessage = prefixedMessage.replaceAll("\n", " "); // Add this line to replace newline characters with spaces
        OkHttpClient client = new OkHttpClient();

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String requestJson = createRequestJson(prefixedMessage);
        System.out.println("Request JSON: " + requestJson);
        RequestBody requestBody = RequestBody.create(JSON, requestJson);

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .header("OpenAI-Organization", ORGANIZATION_ID)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String jsonResponse = response.body().string();
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray choices = jsonObject.getJSONArray("choices");
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject messageObject = firstChoice.getJSONObject("message");
            String content = messageObject.getString("content");

            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String createRequestJson(String message) {
        return "{\n" +
                "  \"model\": \"" + MODEL + "\",\n" +
                "  \"messages\": [{\"role\": \"user\", \"content\": \"" + message + "\"}]\n" +
                "}";
    }
}
