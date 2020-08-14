package emcat

import lib.log.log
import org.apache.catalina.connector.Connector
import org.apache.catalina.startup.Tomcat
import java.io.File
import org.apache.catalina.Context
import org.apache.catalina.LifecycleListener
import org.apache.catalina.LifecycleEvent
import org.apache.catalina.LifecycleState
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer

/**
 * 嵌入式Tomcat的封装
 */
class MyCat(host: String = "0.0.0.0", port: Int = 8080, baseDir: String = ".") {
	private val tomcat = Tomcat()
	private val connector =  Connector()
	val ctx: Context
	
	init {
        // 设置connector实例的监听地址和端口
		connector.setProperty("address", host)
		connector.port = port
		tomcat.setHostname("MyCat")

		// 配置工作目录
		val root = File(baseDir)
		if (!root.exists()) root.mkdirs()
		if (!root.isDirectory) throw RuntimeException("无法创建目录${ root.absolutePath }")
		tomcat.setBaseDir(root.absolutePath)

        // 工作目录下必须创建一个webapps目录
		val webapps = File("${ baseDir }${ File.separatorChar }webapps")
		if (!webapps.exists()) webapps.mkdirs()
		if (!webapps.isDirectory) throw RuntimeException("无法创建目录${ webapps.absolutePath }")

		// 实例化一个应用程序上下文，需要提供应用程序的标识和webapps目录下的路径
		ctx = tomcat.addContext("", ".") // 空串contextPath""代表ROOT即根URL：/
		// Tomcat.addServlet() 添加Servlet
		// context#addServletMappingDecoded() 设置Servlet映射
		
		log.d("初始化嵌入式Tomcat, 监听于 ${ host }:${ port }, 工作目录 ${ ctx.catalinaBase.absolutePath }")
	}
	
	/** 开始监听服务, 在此之前通过spring()启动Spring框架 */
	fun service() {
		tomcat.connector = connector
		tomcat.start()
		tomcat.server.await()
	}
	
	/** 注入spring框架 */
	fun spring(webAppInitializer: AbstractAnnotationConfigDispatcherServletInitializer) {
		ctx.addLifecycleListener(object : LifecycleListener {
			override fun lifecycleEvent(event: LifecycleEvent) {
				if (event.lifecycle.state == LifecycleState.STARTING_PREP) {
					log.d("注入spring框架")
					try {
						webAppInitializer.onStartup(ctx.servletContext)
					} catch(e: Throwable) {
						log.d("注入失败: ${ e::class.java.name } -> ${ e.message }")
						throw RuntimeException(e)
					}
					log.d("注入spring框架成功")
					ctx.removeLifecycleListener(this)
				}
			}
		})
	}
}
