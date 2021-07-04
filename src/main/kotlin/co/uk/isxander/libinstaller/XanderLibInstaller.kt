package co.uk.isxander.libinstaller

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import net.minecraft.launchwrapper.Launch
import net.minecraft.launchwrapper.LaunchClassLoader
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.Charset
import java.util.Set


object XanderLibInstaller {

    private const val DESIRED_VERSION = "1.0"
    private const val CLASS_NAME = "co.uk.isxander.xanderlib.XanderLib"
    private lateinit var dataDir: File
    private var installed = false

    fun isInstalled(): Boolean {
        return installed
    }

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

    fun initialise(gameDir: File): Int {
        if (isInstalled() || isInitialised()) {
            return -1
        }

        dataDir = File(gameDir, "xanderlib")
        if (!dataDir.exists()) {
            dataDir.mkdirs()
        }

        val data = File(dataDir, "metadata.json")

        val jar = File(dataDir, "/$DESIRED_VERSION/xanderlib.jar")
        File(dataDir, "/$DESIRED_VERSION").mkdirs()

        val metaExists = data.exists()
        var metadata: JsonHolder? = null
        if (metaExists) {
            metadata = InstallerUtils.readFile(data)
        }
        if (!metaExists || !metadata!!.has("installed_versions") || !InstallerUtils.jsonArrayContains(
                metadata.optJSONArray(
                    "installed_versions"
                ), DESIRED_VERSION
            )
        ) {
            download(jar, metadata)
        }

        InstallerUtils.

        return 0
    }

    private fun download(outputJar: File, _versionData: JsonHolder?): Boolean {
        val url = "https://static.isxander.co.uk/mods/xanderlib/$DESIRED_VERSION.jar"
        val success =
            InstallerUtils.download(url, outputJar, "Downloading XanderLib", "isXander's XanderLib v$DESIRED_VERSION")

        if (success) {
            var versionData = _versionData
            if (versionData == null) {
                versionData = JsonHolder()
            }

            val arr = versionData.optJSONArray("installed_versions")
            val versionPrim = JsonPrimitive(DESIRED_VERSION)
            if (!InstallerUtils.jsonArrayContains(arr, DESIRED_VERSION)) arr.add(versionPrim)
            versionData.put("version", versionPrim)
            versionData.put("installed_versions", arr as JsonElement)

            FileUtils.write(File(dataDir, "metadata.json"), versionData.toString(), Charset.defaultCharset())
        }
        return success
    }

}