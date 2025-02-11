package org.example.autotest;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FeatureFileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FeatureFileUtil.class);

    public static void saveFeatureFile(AnActionEvent event, String featureContent, String featureName) {
        logger.debug("Inside saveFeatureFile method");

        String projectPath = event.getProject().getBasePath();
        if (projectPath == null) {
            logger.debug("Project path is null");
            return;
        }

        String featureFolderPath = projectPath + "/generated-features";
        String featureFilePath = featureFolderPath + "/generated_" + featureName + ".feature";

        File featureFolder = new File(featureFolderPath);
        if (!featureFolder.exists()) {
            logger.debug("Creating feature folder: {}", featureFolderPath);
            boolean created = featureFolder.mkdirs();
            if (!created) {
                logger.error("Failed to create feature folder");
                return;
            }
        }

        File featureFile = new File(featureFilePath);
        try {
            if (!featureFile.exists()) {
                logger.debug("Creating feature file: {}", featureFilePath);
                boolean fileCreated = featureFile.createNewFile();
                if (!fileCreated) {
                    logger.error("Failed to create feature file");
                    return;
                }
            }

            // **Ensure initial content is written before VirtualFile lookup**
            try (FileWriter writer = new FileWriter(featureFile)) {
                writer.write(featureContent);
            }
        } catch (IOException e) {
            logger.error("Error handling feature file: {}", featureFilePath, e);
            return;
        }

        ApplicationManager.getApplication().runWriteAction(() -> {
            VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(featureFile);
            if (virtualFile != null) {
                logger.debug("Saving feature file: {}", featureFilePath);
                try {
                    VfsUtil.saveText(virtualFile, featureContent);
                    virtualFile.refresh(false, false); // Ensure file changes are reflected in IntelliJ
                } catch (IOException e) {
                    logger.error("Error saving feature file: {}", featureFilePath, e);
                }
            } else {
                logger.error("VirtualFile is null for {}", featureFilePath);
            }
        });
    }
}
