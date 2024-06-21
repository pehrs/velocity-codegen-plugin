package com.pehrs.velocitycodegenplugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public abstract class TemplateRepo {

  static final String home = System.getenv("HOME");

  static final Path configPath = Path.of(home, ".config", "velocity-gencode-plugin");
  static final Path templatesPath = Path.of(configPath.toFile().getAbsolutePath(), "templates");

  static final String sampleTemplate = """
public void mergeFrom($class.getName() from) {
#foreach($field in $fields)
#if(!$field.isStatic())
    this.set$StringUtils.capitalize($field.getName())(from.get$StringUtils.capitalize($field.getName())());
#end
#end
}      
      """;
  public static List<File> getTemplateFiles() {

    File templatesDir = templatesPath.toFile();
    // Make sure path exists
    if(!templatesDir.exists()) {
      templatesDir.mkdirs();
    }

    File[] templateFiles = templatesDir.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return file.getName().endsWith(".vm");
      }
    });

    if(templateFiles.length == 0) {
      // Add a sample template
      Path samplePath = Paths.get(templatesDir.getAbsolutePath(), "mergeFrom.vm");
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(samplePath.toFile(), false))){
        writer.write(sampleTemplate);
        writer.newLine();
      } catch (IOException e) {
        e.printStackTrace();
      }
      templateFiles = new File[]{
          samplePath.toFile()
      };
    }

    return Arrays.asList(templateFiles);
  }

  public static List<String> getTemplateNames() {
    return getTemplateFiles().stream()
        .map(file -> file.getName().replace(".vm", ""))
        .toList();
  }

  public static Optional<String> getTemplate(String name) {
    Path templatePath = Paths.get(templatesPath.toFile().getAbsolutePath(), name + ".vm");
    if(templatePath.toFile().exists()) {
      try {
        String content = new String(Files.readAllBytes(templatePath), StandardCharsets.UTF_8);
        return Optional.of(content);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      return Optional.empty();
    }
  }
}
