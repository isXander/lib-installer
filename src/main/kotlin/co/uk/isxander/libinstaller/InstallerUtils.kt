package co.uk.isxander.libinstaller

import com.google.gson.*
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.awt.Font
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.swing.*


object InstallerUtils {

    fun readFile(file: File): JsonHolder {
        return try { JsonHolder(FileUtils.readFileToString(file)) }
        catch (e: IOException) { JsonHolder() }
    }

    fun fetchJSON(url: String): JsonHolder {
        return JsonHolder(fetchString(url))
    }

    fun fetchString(_url: String): String {
        val url = _url.replace(" ", "%20")
        println("Fetching $url")

        var connection: HttpURLConnection? = null
        var instream: InputStream? = null
        try {
            val u = URL(url)
            connection = u.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.useCaches = true
            connection.addRequestProperty("User-Agent", "Mozilla/4.76 (XanderLib)")
            connection.readTimeout = 15000
            connection.connectTimeout = 15000
            connection.doOutput = false
            instream = connection.inputStream
            return IOUtils.toString(instream, Charset.defaultCharset())
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                connection?.disconnect()
                instream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return "Failed to fetch"
    }

    fun jsonArrayContains(arr: JsonArray, value: String): Boolean {
        for (element in arr) {
            if (element.isJsonPrimitive) {
                val primitive = element as JsonPrimitive
                if (primitive.isString) {
                    if (primitive.asString == value) return true
                }
            }
        }
        return false
    }

    fun download(_url: String, outputFile: File, windowName: String, windowDescription: String): Boolean {
        val url = _url.replace(" ", "%20")
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        val frame = JFrame(windowName)
        val bar = JProgressBar()
        val label = JLabel(windowDescription, SwingConstants.CENTER)
        label.setSize(600, 120)
        frame.contentPane.add(label)
        frame.contentPane.add(bar)
        val layout = GroupLayout(frame.contentPane)
        frame.contentPane.layout = layout
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(label, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE.toInt())
                                .addComponent(bar, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt()))
                        .addContainerGap()))
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(label, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bar, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())))
        frame.isResizable = false
        bar.isBorderPainted = true
        bar.minimum = 0
        bar.isStringPainted = true
        val font: Font = bar.font
        bar.font = Font(font.name, font.style, font.size * 2)
        label.font = Font(font.name, font.style, font.size * 2)
        frame.pack()
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
        var connection: HttpURLConnection? = null
        var instream: InputStream? = null
        try {
            FileOutputStream(outputFile).use { outputStream ->
                val u = URL(url)
                connection = u.openConnection() as HttpURLConnection
                connection!!.requestMethod = "GET"
                connection!!.useCaches = true
                connection!!.addRequestProperty("User-Agent", "Mozilla/4.76 (XanderLib)")
                connection!!.readTimeout = 15000
                connection!!.connectTimeout = 15000
                connection!!.doOutput = true
                instream = connection!!.inputStream
                val contentLength = connection!!.contentLength
                val buffer = ByteArray(1024)
                bar.maximum = contentLength
                var read: Int
                bar.value = 0
                while (instream!!.read(buffer).also { read = it } > 0) {
                    outputStream.write(buffer, 0, read)
                    bar.value = bar.value + 1024
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            frame.dispose()
            return false
        } finally {
            try {
                if (connection != null) {
                    connection!!.disconnect()
                }
                if (instream != null) {
                    instream!!.close()
                }
            } catch (e: Exception) {
                println("Failed cleaning up ModCoreInstaller#download")
                e.printStackTrace()
            }
        }
        frame.dispose()
        return true
    }

    fun addToClasspath(file: File) {
        try {
            val url = file.toURI().toURL()

            val classLoader = ModCoreInstaller::class.java.classLoader
            val method = classLoader::class.java.getDeclaredMethod("addURL", URL::class.java)
            method.isAccessible = true
            method.invoke(classLoader, url)
        } catch (e: Exception) {
            throw RuntimeException("Unexpected exception.", e)
        }
    }

}

class JsonHolder(private var obj: JsonObject) {

    constructor(raw: String?) : this(JsonObject()) {
        if (raw != null) {
            try {
                obj = JsonParser().parse(raw).asJsonObject
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    constructor() : this(JsonObject())

    override fun toString(): String {
        return obj.toString()
    }

    fun put(key: String, value: Boolean): JsonHolder {
        obj.addProperty(key, value)
        return this
    }

    fun put(key: String, value: String): JsonHolder {
        obj.addProperty(key, value)
        return this
    }

    fun put(key: String, value: JsonElement): JsonHolder {
        obj.add(key, value)
        return this
    }

    fun optBoolean(key: String, default: Boolean = false): Boolean? {
        return try { obj.get(key).asBoolean }
        catch (e: Exception) { default }
    }

    fun optString(key: String, default: String? = ""): String? {
        return try { obj.get(key).asString }
        catch (e: Exception) { default }
    }

    fun optJSONArray(key: String, default: JsonArray? = JsonArray()): JsonArray? {
        return try { obj.get(key).asJsonArray }
        catch (e: Exception) { default }
    }

    fun has(key: String): Boolean {
        return obj.has(key)
    }

    fun getKeys(): List<String> {
        val tmp = ArrayList<String>()

        for (entry in obj.entrySet()) {
            tmp.add(entry.key)
        }

        return tmp
    }

}
