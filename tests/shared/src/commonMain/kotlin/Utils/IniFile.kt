package Utils


import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readLine

expect fun getResource(resourceName: String): RawSource

class IniFile(iniFilePath: String) {
    private val iniData: Map<String, Map<String, String>>

    init {
        val creatingMap: MutableMap<String, MutableMap<String, String>> = mutableMapOf()

        var section = ""
        val fileBuffer = getResource(iniFilePath).buffered()
        generateSequence {
            fileBuffer.readLine()
        }.filter { it.isNotBlank() }
            .map { it.trim() }
            .forEach { line ->
                when {
                    line.startsWith("[") && line.endsWith("]") -> {
                        section = line.removePrefix("[").removeSuffix("]")
                        creatingMap[section] = mutableMapOf()
                    }

                    line.contains("=") -> {
                        val key = line.substringBefore("=").trim()
                        val value = line.substringAfter("=").trim()
                        creatingMap.getOrPut(section)
                        {
                            mutableMapOf()
                        }[key] = value
                    }
                }
            }
        iniData = creatingMap
    }

    operator fun get(section: String, key: String): String = iniData[section]?.get(key)
        ?: throw NullPointerException("No key: $key in section: $section in ini config file")
}

val IniConfig by lazy {
    IniFile("TestData.ini")
}