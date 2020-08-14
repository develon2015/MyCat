package lib.log

val log = Logger()

class Logger(private val name: String = "Logger") {
	fun d(vararg _msg: Any?) {
        val msg = _msg.joinToString(separator = " ") {
			it ?.toString() ?: "[null]"
		}

		val trace: Array<StackTraceElement>?  = Thread.currentThread().stackTrace

		if (trace == null || trace.size < 3) return println("Logger@$name $msg")

		val caller = trace[2]

		with (caller) {
			val output = "Logger@$name ~ $fileName($lineNumber) ~ $className # $methodName() ${ Thread.currentThread().name }: $msg"
			println(output)
		}
	}
}
