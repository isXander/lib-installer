package co.uk.isxander.libinstaller

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.minecraft.launchwrapper.Launch
import net.minecraft.launchwrapper.LaunchClassLoader
import org.apache.commons.io.FileUtils
import java.awt.Font
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Set
import javax.swing.*

internal object ModCoreInstaller {

    private const val VERSION_URL: String = "https://api.sk1er.club/modcore_versions"
    private const val CLASS_NAME: String = "club.sk1er.mods.core.ModCore"
    private var errored: Boolean = false
    private var error: String? = null
    private lateinit var dataDir: File
    private var runningModCore: Boolean = false

    fun isInitialised(): Boolean {
        try {
            val objects = LinkedHashSet<String>()
            objects.add(CLASS_NAME)
            Launch.classLoader.clearNegativeEntries(objects)
            val invalidClasses = LaunchClassLoader::class.java.getDeclaredField("invalidClasses")
            invalidClasses.isAccessible = true
            val obj = invalidClasses.get(ModCoreInstaller::class.java.classLoader) as Set<String>
            obj.remove(CLASS_NAME)
            return Class.forName(CLASS_NAME) != null
        } catch (e: Exception) {}
        return false
    }

    fun isErrored(): Boolean {
        return errored
    }

    fun getError(): String? {
        return error
    }

    private fun bail(error: String) {
        errored = true
        ModCoreInstaller.error = error
    }

    fun isInitialised(gameDir: File) {
        if (!runningModCore) {
            return
        }

        try {
            val modCore = Class.forName(CLASS_NAME)
            val instanceMethod = modCore.getMethod("getInstance")
            val initialise = modCore.getMethod("initialize", File::class.java)
            val modCoreObject = instanceMethod.invoke(null)
            initialise.invoke(modCoreObject, gameDir)
            println("Loaded ModCore successfully.")
            return
        } catch (e: Exception) {
            e.printStackTrace()
        }

        println("Failed to initialise ModCore")
    }

    fun initialise(gameDir: File): Int {
        if (isInitialised()) return -1

        dataDir = File(gameDir, "modcore")
        if (!dataDir.exists()) {
            if (!dataDir.mkdirs()) {
                bail("Unable to create necessary files.")
                return 1
            }
        }

        val jsonHolder = InstallerUtils.fetchJSON(VERSION_URL)
        var latestRemote = jsonHolder.optString("1.8.9")
        val failed = jsonHolder.getKeys().isEmpty() || !jsonHolder.optBoolean("success")

        val metadataFile = File(dataDir, "metadata.json")
        val localMetadata = InstallerUtils.readFile(metadataFile)
        if (failed) latestRemote = localMetadata.optString("1.8.9")
        val modCoreFile = File(dataDir, "Sk1er Modcore-${latestRemote} (1.8.9).jar")

        if (!modCoreFile.exists() || !localMetadata.optString("1.8.9").equals(latestRemote, ignoreCase = true) && !failed) {
            // File does not exist, or is out of date, download
            val old = File(dataDir, "Sk1er Modcore-${localMetadata.optString("1.8.9")} (1.8.9).jar")
            if (old.exists()) old.delete()

            if (!download("https://static.sk1er.club/repo/mods/modcore/${latestRemote}/1.8.9/ModCore-${latestRemote} (1.8.9).jar", latestRemote, modCoreFile, localMetadata)) {
                bail("Unabled to download.")
                return 2
            }
        }

        InstallerUtils.addToClasspath(modCoreFile)

        if (!isInitialised()) {
            bail("Something went wrong and it did not add the jar to the classpath. Local file exists? ${modCoreFile.exists()}")
            return 3
        }

        runningModCore = true
        return 0
    }

    private fun download(url: String, version: String, file: File, versionData: JsonHolder): Boolean {
        println("Downloading ModCore  version $version from: $url")
        val success = InstallerUtils.download(url, file, "Downloading ModCore", "Sk1er ModCore $version")
        if (success) {
            FileUtils.write(File(dataDir, "metadata.json"), versionData.put("1.8.9", version).toString())
        }

        return success
    }

}