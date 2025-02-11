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
    private static final Logger logger = LoggerFactory.getLogger(GenerateFeatureAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {

        logger.info("Generate Feature File Action Triggered");
        Project project = event.getProject();

        // Open File Chooser Dialog to select a .docx file
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false)
                .withTitle("Select a Word Document")
                .withFileFilter(file -> file.getName().endsWith(".docx"));

        String featureName = Messages.showInputDialog(
                project,
                "Enter the Feature Name:",
                "Feature Name",
                Messages.getQuestionIcon()
        );

        FileChooser.chooseFile(descriptor, project, null, (VirtualFile virtualFile) -> {
            String docFilePath = virtualFile.getPath();
            logger.info("Selected Word Document: {}", docFilePath);

            // Show a modal dialog asking if the user wants to generate deep test cases
            int result = Messages.showYesNoDialog(
                    project,
                    "By default generate option will build basic  and happy-path scenarios test cases." +
                            "If we want more scenarios then we can select generatePro option. Select Yes if you want to go with 'generatePlus'",
                    "GeneratePlus",
                    Messages.getQuestionIcon()
            );

            boolean isDeepGen = (result == Messages.YES);

            ProgressManager.getInstance().run(new Task.Modal(event.getProject(), "AutoTest", false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {

                    indicator.setText("Uploading File..");

                    String prompt = convertWordToPrompt(docFilePath, indicator);
                    logger.info("word.docx successfully converted to prompt");

                    indicator.setText("Generating Feature file.");

                    String featureFileContent;
                    if (isDeepGen)
                        featureFileContent = DeepGenerate.fetchTestCaseHeadings(prompt, indicator);
                    else
                        featureFileContent = ApiClient.sendJsonToApi(prompt, indicator);

                    indicator.setText("Saving to file.");
                    WriteCommandAction.runWriteCommandAction(project, () ->
                            saveFeatureFile(event, featureFileContent, featureName)
                    );
                    indicator.setText("Done.");
                }
            });

            // Step 4: Show Success Message
            Messages.showInfoMessage("Feature file generated successfully!", "Success");
        });
    }
}
