@Grab(group = "commons-cli", module = "commons-cli", version = "1.3.1")
@Grab(group = "org.apache.maven", module = "maven-model", version = "3.3.9")
import org.apache.commons.cli.*
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.maven.model.io.xpp3.MavenXpp3Writer

import java.nio.file.Paths

String normalizeProperty(String key){
    if(key == null) return null;
    else if(key.startsWith("\${") && key.endsWith("}"))
        return key.substring(2, key.length() - 1)
    else return key;
}

Properties createProperties(final Properties p1, final Properties p2){
    final result = new Properties()
    result.putAll(p1)
    result.putAll(p2)
    return result;
}

def flattenPOMs(final MavenXpp3Reader reader, Properties properties, final String pomFile, final String directory, final String outPomFile){
    new FileInputStream(Paths.get(directory, pomFile).toFile()).withStream {is ->
        final model = reader.read(is)
        println "Processing $model.artifactId"
        //collect properties
        properties = createProperties(properties, model.getProperties())
        //replace version
        def key = normalizeProperty(model.getVersion())
        if(key != null && properties.containsKey(key))
            model.setVersion properties.getProperty(key)
        //replace in all dependencies
        model.getDependencies().each {dependency ->
            key = normalizeProperty(dependency.getVersion())
            if(key != null && properties.containsKey(key))
                dependency.setVersion properties.getProperty(key)
        }
        //save into new file
        new FileOutputStream(Paths.get(directory, outPomFile).toFile()).withStream {os ->
            new MavenXpp3Writer().write(os, model)
        }
        //iterate through modules
        model.getModules().each {module ->
            final moduleDirectory = Paths.get(directory, module).toFile().getCanonicalPath()
            flattenPOMs(reader, properties, pomFile, moduleDirectory, outPomFile)
        }
    }
}

def flattenPOMs(final String pomFile, final String directory, final String outPomFile){
    flattenPOMs(new MavenXpp3Reader(), new Properties(), pomFile, directory, outPomFile)
}

//BEGIN MAIN
final options = new Options()
final POM_FILE_OPTION = "f"
options.addOption POM_FILE_OPTION, "pomFile", true, "Name of all POM files"
final DIR_OPTION = "d"
options.addOption DIR_OPTION, "directory", true, "Path to directory with root POM"
final HELP_OPTION = "h";
options.addOption HELP_OPTION, "help", false, "Print this message"
final OUT_FILE_OPTION = "o";
options.addOption OUT_FILE_OPTION, "out", true, "Name of output POM files"
final parser = new DefaultParser()
final cmd = parser.parse(options, args)
//print help
if(cmd.hasOption(HELP_OPTION)){
    final formatter = new HelpFormatter()
    formatter.printHelp "flatpom <pomFile> <directory>", options
}
flattenPOMs cmd.getOptionValue(POM_FILE_OPTION), cmd.getOptionValue(DIR_OPTION), cmd.getOptionValue(OUT_FILE_OPTION)
