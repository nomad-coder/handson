package handson.article

import net.bytebuddy.utility.RandomString
import org.h2.tools.DeleteDbFiles
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.http.ResponseEntity
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.patchForObject
import java.util.*
import javax.annotation.PreDestroy
import javax.persistence.*

@EnableEurekaClient
@SpringBootApplication
class ArticleService

fun main(args: Array<String>) {
	runApplication<ArticleService>(*args)
}

//지워도 되고 안 지워도 된다. 이건 선택
@Profile("local")
@Configuration
class LocalConfig {

	@PreDestroy
	fun onDestroy() {
		DeleteDbFiles.execute("~/handsondb", "account", true)
	}

}

@Configuration
class RootConfig {

	@Bean
	@LoadBalanced	//이놈이 없으면 application-name 으로 통신 안된다. 아님 직접 게이트웨이에 요청하는 방법도.
	fun restTemplate(): RestTemplate {
		return RestTemplate(HttpComponentsClientHttpRequestFactory())
	}

}

@RestController
@RequestMapping("/articles")
class AccountController(
	private val repo: ArticleRepository,
	private val rest: RestTemplate
) {

	@PostMapping
	fun create(@RequestBody article: Article): ResponseEntity<Article> {
		repo.save(article)
		val rel = linkTo(AccountController::class.java).slash(article.id).withSelfRel()
		this.sendArticleCount(article.author.id)
		return ResponseEntity.created(rel.toUri()).body(article)
	}

	private fun sendArticleCount(authorId: String) {
		val count = repo.countByAuthor(authorId)
		rest.patchForObject<String>("http://account-service/$authorId/article-count", mapOf("articleCount" to count), String::class)
	}

}

@Entity
@Table(name = "ARTICLES")
data class Article(

	@Id
	val id: String = RandomString.make(30),

	var title: String? = null,

	@Lob
	var content: String? = null,

	@Embedded
	@AttributeOverrides(
		value = [
			AttributeOverride(name = "id", column = Column(name = "AUTHOR_ID"))
			, AttributeOverride(name = "name", column = Column(name = "AUTHOR_NAME"))
		]
	)
	val author: Author = Author(),

	@CreationTimestamp
	val createdAt: Date? = Date(),

	@UpdateTimestamp
	var updatedAt: Date? = null

)

@Embeddable
class Author (

	val id: String = "Unknown",

	val name: String = "Anonymous"

) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Author

		if (id != other.id) return false

		return true
	}

	override fun hashCode(): Int {
		return id.hashCode()
	}
}

@RepositoryRestResource //articles 안해도 기본으로 articles 로 되네. 어떻게 복수형으로 바꿨지 신기하네
interface ArticleRepository : JpaRepository<Article, String> {

	@Query("select count(a) from Article a where a.author.id = :authorId")
	fun countByAuthor(authorId: String): Int

}
