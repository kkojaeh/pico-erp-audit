package pico.erp.audit.data;

import java.util.Collection;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder
public class AuditConfiguration {

  String packageToScan;

  @Singular
  Collection<Class<?>> entities;

  @Singular
  Collection<Class<?>> valueObjects;

}
