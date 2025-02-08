package org.example.autotest;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;

public class WordToJsonConverter {
    private static final Logger logger = LoggerFactory.getLogger(WordToJsonConverter.class);

    public static String convertWordToPrompt(String docFilePath) {

        logger.debug("inside convertWordToPrompt method");
        StringBuilder promptBuilder = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(docFilePath);

             XWPFDocument document = new XWPFDocument(fis)) {
            logger.debug("Reading Word Document: {}", docFilePath);
            // Extract text from paragraphs
            for (XWPFParagraph para : document.getParagraphs()) {
                String text = para.getText().trim();
                if (!text.isEmpty()) {
                    promptBuilder.append(text).append(" ");
                }
            }
        } catch (IOException e) {
            logger.error("Error reading Word Document: {}", docFilePath);
            e.printStackTrace();
        }

        return promptBuilder.toString().trim();
    }
}
