package handson.gateway

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.netflix.zuul.EnableZuulProxy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableZuulProxy
@EnableEurekaClient
@SpringBootApplication
class GatewayService

fun main(args: Array<String>) {
	runApplication<GatewayService>(*args)
}

@Configuration
class GatewayConfig {

	@Bean
	fun defaultFilter(): ZuulFilter = PreFilter()

}

class PreFilter: ZuulFilter() {

	private val logger = LoggerFactory.getLogger(this.javaClass)

	override fun run(): Any? {
		val ctx = RequestContext.getCurrentContext()
		val request = ctx.request
		logger.info("*** [${request.method}] request to ${request.requestURL}");
		return null
	}

	override fun shouldFilter(): Boolean {
		//filter 의 실행 여부
		return true
	}

	override fun filterType(): String {
		//pre, routing, post, error
		return "pre"
	}

	override fun filterOrder(): Int {
		return 0
	}

}