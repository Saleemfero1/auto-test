package org.example.autotest;

import com.intellij.openapi.progress.ProgressIndicator;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;

public class WordToJsonConverter {
    private static final Logger logger = LoggerFactory.getLogger(WordToJsonConverter.class);

    public static String convertWordToPrompt(String docFilePath, @NotNull ProgressIndicator indicator) {

        logger.debug("inside convertWordToPrompt method");
        StringBuilder promptBuilder = new StringBuilder();
        indicator.setText("Processing File..");
        try (FileInputStream fis = new FileInputStream(docFilePath);
             XWPFDocument document = new XWPFDocument(fis)) {
            logger.debug("Reading Word Document: {}", docFilePath);
            // Extract text from paragraphs
            indicator.setText("Processing File...");
            for (XWPFParagraph para : document.getParagraphs()) {
                indicator.setText("Processing File...");
                String text = para.getText().trim();
                if (!text.isEmpty()) {
                    promptBuilder.append(text).append(" ");
                }
                indicator.setText("Processing file....");
            }
            indicator.setText("Processing file.....");
        } catch (IOException e) {
            logger.error("Error reading Word Document: {}", docFilePath);
            e.printStackTrace();
        }
        indicator.setText("Processing file Done!");
        return promptBuilder.toString().trim();
    }
}
