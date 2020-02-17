package handson.account

import net.bytebuddy.utility.RandomString
import org.hibernate.annotations.CreationTimestamp
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.validation.BindException
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotEmpty
import javax.validation.groups.Default

@EnableEurekaClient
@SpringBootApplication
class AccountService

fun main(args: Array<String>) {
	runApplication<AccountService>(*args)
}

@RefreshScope
@RestController
class TestController(
	@Value("\${say.cheese}") private val say: String
) {

	@GetMapping("/say")
	fun say() : String {
		return say
	}

}

@RestController
@RequestMapping("/")
class AccountController(
	private val repo: AccountRepository
) {

	@PostMapping
	fun create(@RequestBody account: Account) = repo.save(account)

	@GetMapping
	fun list(): List<Account> = repo.findAll()

	@GetMapping("/{id}")
	fun details(@PathVariable id: String) = repo.findById(id).get()

	@PatchMapping("/{id}/name")
	fun patchName(@PathVariable id: String,
				  @Validated(value = [ValidationGroups.UpdateName::class]) @RequestBody account: Account, bindingResult: BindingResult) {
		if (bindingResult.hasErrors()) {
			throw BindException(bindingResult)
		}
		repo.findById(id)
			.ifPresent {
				val a = it.copy(name = account.name)
				repo.save(a)
			}
	}

	@DeleteMapping("/{id}")
	fun delete(@PathVariable id: String) = repo.deleteById(id)

}

interface AccountRepository : JpaRepository<Account, String>

@Entity
@Table(name = "ACCOUNTS")
data class Account(

	@Id
	val id: String = RandomString.make(30),

	val username: String? = null,

	val password: String? = null,

	val email: String? = null,

	@get:NotEmpty(groups = [ValidationGroups.UpdateName::class])
	val name: String? = null,

	val articleCount: Int = 0,

	@CreationTimestamp
	val createdAt: Date? = Date()

)

object ValidationGroups {

	interface UpdateName : Default

}