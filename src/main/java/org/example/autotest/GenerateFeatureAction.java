package org.example.autotest;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.example.autotest.FeatureFileUtil.saveFeatureFile;
import static org.example.autotest.WordToJsonConverter.convertWordToPrompt;

public class GenerateFeatureAction extends AnAction {
    private static final Logger logger = LoggerFactory.getLogger(WordToJsonConverter.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {

        logger.info("Generate Feature File Action Triggered");
        Project project = event.getProject();

        // Open File Chooser Dialog to select a .docx file
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false)
                .withTitle("Select a Word Document")
                .withFileFilter(file -> file.getName().endsWith(".docx"));

        FileChooser.chooseFile(descriptor, project, null, (VirtualFile virtualFile) -> {
            String docFilePath = virtualFile.getPath();
            logger.info("Selected Word Document: {}", docFilePath);

            ProgressManager.getInstance().run(new Task.Modal(event.getProject(), "Generating Feature File...", false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {

                    // Step 1: Convert Word to Prompt
                    String prompt = convertWordToPrompt(docFilePath);
                    logger.info("word.docx successfully converted to prompt");

                    // Step 2: Send JSON to OpenAI API
                    String featureFileContent = ApiClient.sendJsonToApi(prompt);

                    // Step 3: Save the Feature File
                    WriteCommandAction.runWriteCommandAction(project, () ->
                            saveFeatureFile(event, featureFileContent)
                    );
                }
            });

            // Step 4: Show Success Message
            Messages.showInfoMessage("Feature file generated successfully!", "Success");
        });
    }
}
