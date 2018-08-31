package pico.erp.audit;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Id;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pico.erp.audit.annotation.Audit;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Audit(alias = "user")
public class User {

  @Id
  String id;

  String name;

  int age;

  Set<UserRole> roles = new HashSet<>();

  @Transient
  OffsetDateTime createdDate;
}
