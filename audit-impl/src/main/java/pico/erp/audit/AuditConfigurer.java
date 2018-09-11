package pico.erp.audit;

import org.javers.core.Javers;

public interface AuditConfigurer {

  void initialize();

  Javers buildJavers();

  boolean isInitialized();

}
