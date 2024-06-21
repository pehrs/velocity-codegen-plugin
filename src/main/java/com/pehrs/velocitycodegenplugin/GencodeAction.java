package com.pehrs.velocitycodegenplugin;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.lang.JavaVersion;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.parser.ParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GencodeAction extends AnAction {

  private static final Logger LOG = Logger.getInstance(GencodeAction.class);

  @Override
  public void update(@NotNull AnActionEvent actionEvent) {
    VirtualFile projectRoot = actionEvent.getProject().getProjectFile().getParent().getParent();
    @Nullable PsiFile psiFile = actionEvent.getData(CommonDataKeys.PSI_FILE);

    // Make sure we are only enabled for Java files
    actionEvent.getPresentation().setEnabledAndVisible(psiFile instanceof PsiJavaFile);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    // Using the event, implement an action.
    // For example, create and show a dialog.

    Project currentProject = event.getProject();

    @Nullable PsiFile psiFile = event.getData(
        CommonDataKeys.PSI_FILE);
    // LOG.info("file: " + psiFile);

//    @Nullable PsiElement psiElement = event.getData(
//        CommonDataKeys.PSI_ELEMENT);
//    System.out.println("element: " + psiElement);

//    StringBuilder message =
//        new StringBuilder(event.getPresentation().getText() + " Selected!");
//    // If an element is selected in the editor, add info about it.
//    Navigatable selectedElement = event.getData(CommonDataKeys.NAVIGATABLE);
//    if (selectedElement != null) {
//      message.append("\nSelected Element: ").append(selectedElement);
//    }
//    String title = event.getPresentation().getDescription();
//    Messages.showMessageDialog(
//        currentProject,
//        message.toString(),
//        title,
//        Messages.getInformationIcon());
    try {
      SelectTemplateDialog templateDialog = new SelectTemplateDialog();
      if (templateDialog.showAndGet()) {
        String generatedCode = "";
        if (psiFile != null) {
          if (psiFile instanceof PsiJavaFile psiJavaFile) {
            @NotNull JavaVersion javaVersion = psiJavaFile.getLanguageLevel()
                .toJavaVersion();

            PsiClass[] classes = psiJavaFile.getClasses();
            if (classes != null && classes.length > 0) {
              String selectedTemplate = templateDialog.getSelectedTemplate();
              LOG.info("selected: " + selectedTemplate);
              if (selectedTemplate != null) {
                PsiClass klass = classes[0];
                LOG.info("CLASS " + klass.getName());
                for (PsiField field : klass.getAllFields()) {
                  LOG.info("  field: " + field.getName());
                }
                try {
                  String template = TemplateRepo.getTemplate(selectedTemplate)
                      .orElseGet(() -> null);
                  generatedCode = generateCode(currentProject, javaVersion, klass, template);
                } catch (ParseException e) {
                  throw new RuntimeException(e);
                }
              }
            }

            final String codeToInsert = generatedCode;
            Editor editor = event.getData(CommonDataKeys.EDITOR);
            // Application app = ApplicationManager.getApplication();

            WriteCommandAction.runWriteCommandAction(currentProject, () -> {
              // EditorModificationUtil.insertStringAtCaret(editor, codeToInsert);
              @NotNull Document doc = editor.getDocument();
              doc.insertString(editor.getCaretModel().getOffset(), codeToInsert);
            });
          }
        }

      }
    } catch (Exception ex) {
      LOG.error(ex.getMessage(), ex);
    }
  }

  private String generateCode(
      Project project,
      @NotNull JavaVersion javaVersion, PsiClass klass, String template) throws ParseException {
    // FIXME:
    //   without the following class loader initialization, I get the
    //   following exception when running as Eclipse plugin:
    //   org.apache.velocity.exception.VelocityException: The specified
    //   class for ResourceManager
    //   (org.apache.velocity.runtime.resource.ResourceManagerImpl) does not
    //   implement org.apache.velocity.runtime.resource.ResourceManager;
    //   Velocity is not initialized correctly.
    final ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(GencodeAction.class.getClassLoader());

    VelocityEngine engine = new VelocityEngine();
    // engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
    engine.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, MyNullLogChute.class.getName());
    engine.init();

    VelocityContext context = new VelocityContext();
    context.put("StringUtils", org.apache.commons.lang3.StringUtils.class);
    context.put("date", new Date());
    context.put("class", klass);
    context.put("allFields",
        Arrays.stream(klass.getAllFields()).map(field -> FieldDeclaration.fromPsiField(field))
            .toList()
    );
    context.put("fields",
        Arrays.stream(klass.getFields()).map(field -> FieldDeclaration.fromPsiField(field)).toList()
    );
    context.put("javaVersion", javaVersion);
    context.put("javaFeatureVersion", javaVersion.feature);
    context.put("javaMinorVersion", javaVersion.minor);
    context.put("javaUpdateVersion", javaVersion.update);

    @NotNull PsiClassType psiType = PsiType.getTypeByName(
        CommonClassNames.JAVA_LANG_CLASS, project, GlobalSearchScope.EMPTY_SCOPE);
    for (PsiField field : klass.getAllFields()) {
      @NotNull PsiType t = field.getType();

      System.out.println("is assignable from List: " +
          field.getType().isAssignableFrom(psiType));

      System.out.println("type: " + t.getCanonicalText());
    }

    StringWriter writer = new StringWriter();
//    String template = """
//        public void mergeFrom($class.getName() from) {
//        #foreach($field in $fields)
//             this.set$StringUtils.capitalize($field.getName())(from.get$StringUtils.capitalize($field.getName())());
//        #end
//        }
//            """;
    engine.evaluate(context, writer, "LOG_TAG", template);

    LOG.info("GENERATED:\n" + writer.toString());

    // FIXME: set back default class loader
    Thread.currentThread().setContextClassLoader(oldContextClassLoader);

    return writer.toString();
  }
}
