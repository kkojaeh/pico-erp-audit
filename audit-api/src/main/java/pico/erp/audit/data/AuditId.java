package pico.erp.audit.data;

import java.io.Serializable;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = {"alias", "id"})
@ToString
public class AuditId {

  @NotNull
  private String alias;

  @NotNull
  private Serializable id;

  public static AuditId from(@NotNull String alias, @NotNull Serializable id) {
    return new AuditId(alias, id);
  }

}
