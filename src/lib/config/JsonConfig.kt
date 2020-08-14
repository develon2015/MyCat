package lib.config

import java.io.File

/**
 * 读取简单的键值对 JSON 配置文件
 */
class JsonConfig(file: File) {

	private val global = HashMap<String, String>()

	constructor(fileName: String) : this(File(fileName))

	init {
		val content = String(file.readBytes(), Charsets.UTF_8)
		//content = content.replace("""(\r|\n|\t)""".toRegex(), "")

		val regex = """"(.+)":\s*("(?:.+)"|(?:\d+))""".toRegex()
		val r = regex.findAll(content)
		r.forEachIndexed { _, it ->
			var (key, value) = it.destructured
			with (""""(.*)"""".toRegex().matchEntire(value)) {
				if (this != null) {
					value = groups[1]?. value ?: value
				}
			}

			global[key] = value
		}
	}

	fun get(key: String) = global[key] ?: throw RuntimeException("Can't resolve key '$key'")

}
