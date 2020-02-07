package mmssaa.registry

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer

@EnableEurekaServer
@SpringBootApplication
class ServiceDiscovery

fun main(args: Array<String>) {
	runApplication<ServiceDiscovery>(*args)
}
