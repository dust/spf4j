package org.spf4j.maven.plugin.avro.avscp;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.avro.Protocol;
import org.apache.avro.Schema;
import org.apache.avro.compiler.idl.Idl;
import org.apache.avro.compiler.idl.ParseException;
import org.apache.avro.compiler.specific.SpecificCompiler;
import org.apache.avro.generic.GenericData;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

@Mojo(name = "avro-compile", requiresDependencyResolution = ResolutionScope.COMPILE)
@Execute(phase = LifecyclePhase.GENERATE_SOURCES)
public final class SchemaCompileMojo
        extends SchemaMojoBase {


  /**
   * The field visibility indicator for the fields of the generated class, as string values of
   * SpecificCompiler.FieldVisibility. The text is case insensitive.
   */
  @Parameter(name = "fieldVisibility", defaultValue = "PRIVATE")
  private String fieldVisibility;

  /**
   * The directory (within the java classpath) that contains the velocity templates to use for code generation. The
   * default value points to the templates included with the avro-maven-plugin.
   */
  @Parameter(name = "templateDirectory",
          defaultValue = "/org/apache/avro/compiler/specific/templates/java/classic/")
  protected String templateDirectory;

  /**
   * Determines whether or not to create setters for the fields of the record. The default is to create setters.
   */
  @Parameter(name = "createSetters",
          defaultValue = "false")
  protected boolean createSetters;



  protected void doCompileIDL(String filename) throws IOException {
    try {
      List<String> cpElements = mavenProject.getCompileClasspathElements();
      Idl parser;

      List<URL> runtimeUrls = new ArrayList<URL>();

      // Add the source directory of avro files to the classpath so that
      // imports can refer to other idl files as classpath resources
      runtimeUrls.add(sourceDirectory.toURI().toURL());
      runtimeUrls.add(dependenciesDirectory.toURI().toURL());

      // If runtimeClasspathElements is not empty values add its values to Idl path.
      if (cpElements != null && !cpElements.isEmpty()) {
        for (Object runtimeClasspathElement : cpElements) {
          String element = (String) runtimeClasspathElement;
          runtimeUrls.add(new File(element).toURI().toURL());
        }
      }

      URLClassLoader projPathLoader = new URLClassLoader(runtimeUrls.toArray(new URL[runtimeUrls.size()]),
              Thread.currentThread().getContextClassLoader());
      File file = new File(sourceDirectory, filename);
      parser = new Idl(file, projPathLoader);
      Protocol protocol = parser.CompilationUnit();
      Collection<Schema> types = protocol.getTypes();
      for (Schema schema : types) {
        String targetName = schema.getFullName().replaceAll("\\.", File.separator) + ".avsc";
        Path destinationFile = generatedAvscTarget.toPath().resolve(targetName);
        Files.createDirectories(destinationFile.getParent());
        Files.write(destinationFile,
                schema.toString().getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE);
      }
      SpecificCompiler compiler = new SpecificCompiler(protocol);
      compiler.setStringType(GenericData.StringType.String);
      compiler.setTemplateDir(templateDirectory);
      compiler.setFieldVisibility(SpecificCompiler.FieldVisibility.valueOf(fieldVisibility));
      compiler.setCreateSetters(createSetters);
      compiler.compileToDestination(null, generatedJavaTarget);
    } catch (ParseException e) {
      throw new IOException(e);
    } catch (DependencyResolutionRequiredException drre) {
      throw new IOException(drre);
    }
  }

  protected void doCompileSchemas(String[] filenames)
          throws IOException {
    Schema.Parser parser = new Schema.Parser();
    for (String fileName : filenames) {
      File src = new File(sourceDirectory, fileName);
      Schema schema = parser.parse(src);
      String targetName = schema.getFullName().replaceAll("\\.", File.separator) + ".avsc";
      Path destination = generatedAvscTarget.toPath().resolve(targetName);
      Files.createDirectories(destination.getParent());
      Files.copy(src.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
      SpecificCompiler compiler = new SpecificCompiler(schema);
      compiler.setTemplateDir(templateDirectory);
      compiler.setStringType(GenericData.StringType.String);
      compiler.setFieldVisibility(SpecificCompiler.FieldVisibility.valueOf(fieldVisibility));
      compiler.setCreateSetters(createSetters);
      compiler.setOutputCharacterEncoding(mavenProject.getProperties().getProperty("project.build.sourceEncoding"));
      compiler.compileToDestination(src, generatedJavaTarget);
    }
  }

  protected void doCompileProtocol(String filename) throws IOException {
    File src = new File(sourceDirectory, filename);
    Protocol protocol = Protocol.parse(src);
    Collection<Schema> types = protocol.getTypes();
    for (Schema schema : types) {
      String targetName = schema.getFullName().replaceAll("\\.", File.separator) + ".avsc";
      Path destinationFile = generatedAvscTarget.toPath().resolve(targetName);
      Files.createDirectories(destinationFile.getParent());
      Files.write(destinationFile, schema.toString().getBytes(StandardCharsets.UTF_8),
              StandardOpenOption.CREATE);
    }
    SpecificCompiler compiler = new SpecificCompiler(protocol);
    compiler.setTemplateDir(templateDirectory);
    compiler.setStringType(GenericData.StringType.String);
    compiler.setFieldVisibility(SpecificCompiler.FieldVisibility.valueOf(fieldVisibility));
    compiler.setCreateSetters(createSetters);
    compiler.compileToDestination(src, generatedJavaTarget);
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    Log logger = this.getLog();
    logger.info("Generationg java code + schemas");
    try {
      doCompileSchemas(getFiles("**/*.avsc"));
    } catch (IOException ex) {
      throw new MojoExecutionException("cannot compile schemas", ex);
    }

    for (String file : getFiles("**/*.avpr")) {
      try {
        doCompileProtocol(file);
      } catch (IOException ex) {
        throw new MojoExecutionException("cannot compile protocol " + file, ex);
      }
    }
    for (String file : getFiles("**/*.avdl")) {
      try {
        doCompileIDL(file);
      } catch (IOException ex) {
        throw new MojoExecutionException("cannot compile IDL " + file, ex);
      }
    }
    mavenProject.addCompileSourceRoot(generatedJavaTarget.getAbsolutePath());
    Resource resource = new Resource();
    resource.setDirectory(this.generatedAvscTarget.getAbsolutePath());
    resource.addInclude("**/*.avsc");
    mavenProject.addResource(resource);
    Resource resource2 = new Resource();
    resource2.setDirectory(this.sourceDirectory.getAbsolutePath());
    resource2.addInclude("**/*.avpr");
    resource2.addInclude("**/*.avdl");
    mavenProject.addResource(resource2);
  }

  public String[] getFiles(final String pattern) {
    FileSetManager fsm = new FileSetManager();
    FileSet fs = new FileSet();
    fs.setDirectory(sourceDirectory.getAbsolutePath());
    fs.addInclude(pattern);
    fs.setFollowSymlinks(false);
    return fsm.getIncludedFiles(fs);
  }

}