package handson.article

import net.bytebuddy.utility.RandomString
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*
import javax.persistence.*

@EnableEurekaClient
@SpringBootApplication
class ArticleService

fun main(args: Array<String>) {
	runApplication<ArticleService>(*args)
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
class Author(

	val id: String = "Unknown",

	val name: String = "Anonymous"

)

@RepositoryRestResource //articles 안해도 기본으로 articles 로 되네. 어떻게 복수형으로 바꿨지 신기하네
interface ArticleRepository : JpaRepository<Article, String>
