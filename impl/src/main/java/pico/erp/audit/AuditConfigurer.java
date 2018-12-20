package pico.erp.audit;

import org.javers.core.Javers;

public interface AuditConfigurer {

  Javers buildJavers();

  void initialize();

  boolean isInitialized();

}
