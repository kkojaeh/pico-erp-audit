package pico.erp.audit.core;

import org.javers.core.Javers;

public interface AuditConfigurer {

  void initialize();

  Javers buildJavers();

  boolean isInitialized();

}
