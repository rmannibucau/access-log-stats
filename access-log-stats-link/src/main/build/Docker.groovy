import com.google.cloud.tools.jib.api.Containerizer
import com.google.cloud.tools.jib.api.DockerDaemonImage
import com.google.cloud.tools.jib.api.Jib
import com.google.cloud.tools.jib.api.RegistryImage
import com.google.cloud.tools.jib.configuration.FilePermissions
import com.google.cloud.tools.jib.configuration.LayerConfiguration
import com.google.cloud.tools.jib.event.EventHandlers
import com.google.cloud.tools.jib.filesystem.AbsoluteUnixPath
import com.google.cloud.tools.jib.image.ImageReference
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest

import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.time.Instant

// prepare variables and files
def projectVersion = project.getVersion()
def tag = projectVersion.endsWith("-SNAPSHOT") ?
        projectVersion.replace("-SNAPSHOT", "") + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) :
        projectVersion

def image = "rmannibucau/${project.getArtifactId().replace('-link', '')}"
def repository = project.properties['docker.registry']
final String imageName =
        ((repository == null || repository.trim().isEmpty()) ? "" : (repository + '/')) + image + ':' + tag

def buildDir = Paths.get(project.build.directory)
def zipToAdd = buildDir.resolve("${project.build.finalName}.zip")
if (!zipToAdd.toFile().exists()) {
    throw new IllegalStateException("Missing file ${zipToAdd}")
}
def exploded = new File(project.build.directory, 'maven-jlink') // exploded distro

// setup jib
def workingDir = AbsoluteUnixPath.get("/opt/rmannibucau/${project.artifactId}")
def builder = Jib.from(ImageReference.parse('rmannibucau/jlink-base:11'))
builder.setCreationTime(Instant.now())
builder.setWorkingDirectory(workingDir)
builder.addEnvironmentVariable('LC_ALL', 'en_US.UTF8')
builder.setLabels([
        'com.github.rmannibucau.groupId'   : project.groupId,
        'com.github.rmannibucau.artifactId': project.artifactId,
        'com.github.rmannibucau.version'   : project.version,
        'com.github.rmannibucau.date'      : new Date().toString()
])
// the distro
builder.addLayer(LayerConfiguration.builder()
        .addEntryRecursive(exploded.toPath(), workingDir)
        .build())
// ensure java is executable
builder.addLayer(LayerConfiguration.builder()
        .addEntry(
            exploded.toPath().resolve('bin/java'),
            workingDir.resolve('bin/java'),
            FilePermissions.fromOctalString('700'))
        .build())
builder.setEntrypoint([
        workingDir.resolve('bin/java').toString(),
        '-XX:+UseContainerSupport',
        '--add-modules', 'com.github.rmannibucau.log.access.core',
        'com.github.rmannibucau.log.access.core.Launcher'
])

// build the actual image
def cache = buildDir.resolve('maven/build/cache')
if (repository != null && !repository.trim().isEmpty()) { // push
    log.info("Creating docker image and pushing it on ${repository}")
    final RegistryImage registryImage = RegistryImage.named(imageName)
    def credentials = session.getSettings().getServer(repository)
    if (credentials != null) {
        def result = settingsDecrypter.decrypt(new DefaultSettingsDecryptionRequest(credentials))
        credentials = result == null ? credentials : result.getServer()
        registryImage.addCredential(credentials.username, credentials.password)
    }
    builder.containerize(Containerizer.to(registryImage)
            .setApplicationLayersCache(cache)
            .setBaseImageLayersCache(cache)
            .setToolName("Rmannibucau ${project.groupId}:${project.artifactId} Jib script")
            .setEventHandlers(new EventHandlers()))
    log.info("Pushed image='" + imageName + "', tag='" + tag + "'")
} else { // local
    log.info("Creating docker image and pushing it locally")
    def docker = DockerDaemonImage.named(imageName)
    builder.containerize(Containerizer.to(docker)
            .setApplicationLayersCache(cache)
            .setBaseImageLayersCache(cache)
            .setToolName("Rmannibucau ${project.groupId}:${project.artifactId} Jib script")
            .setEventHandlers(new EventHandlers()))
    log.info("Built local image='${imageName}', tag='${tag}")
}
