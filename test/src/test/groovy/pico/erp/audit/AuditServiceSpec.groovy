package pico.erp.audit

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import pico.erp.shared.ApplicationInitializer
import pico.erp.shared.IntegrationConfiguration
import pico.erp.shared.Public
import spock.lang.Specification

import java.time.OffsetDateTime

@SpringBootTest(classes = [IntegrationConfiguration], properties = "audit.enabled=true")
@Transactional
@Rollback
@ActiveProfiles("test")
@Configuration
class AuditServiceSpec extends Specification {

  @Lazy
  @Autowired
  AuditService auditService

  @Lazy
  @Autowired
  List<ApplicationInitializer> applicationInitializers

  @Bean
  @Public
  AuditConfiguration auditConfiguration() {
    return AuditConfiguration.builder()
      .packageToScan("pico.erp.audit")
      .build()
  }

  def setup() {
    applicationInitializers.forEach({ initializer -> initializer.initialize() })
  }

  def "테스트"() {
    when:
    def test = new User(
      id: "a",
      name: "고",
      age: 34,
      createdDate: OffsetDateTime.now(),
      roles: new HashSet<UserRole>(Arrays.asList(UserRole.USER))
    )
    auditService.commit(test)

    def test2 = new User(
      id: "a",
      name: "재훈",
      age: 35,
      createdDate: OffsetDateTime.now()
    )
    auditService.commit(test2)

    def test3 = new User(
      id: "a",
      name: "재훈",
      age: 36,
      createdDate: OffsetDateTime.now()
    )
    auditService.commit(test3)

    def commits = auditService.get(AuditId.from("user", "a"))
    println(commits)
    then:

    commits.size() == 3
  }


}
