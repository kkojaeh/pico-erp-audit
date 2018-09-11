package pico.erp.audit.impl;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import pico.erp.audit.AuditExceptions.AlreadyAuditAliasExistsException;
import pico.erp.audit.AuditExceptions.NotRegisteredAuditTypeException;
import pico.erp.audit.AuditRegistry;

@Component
@Validated
public class AuditRegistryImpl implements AuditRegistry {

  private final Map<String, Class<?>> registry = new HashMap<>();

  @Override
  public boolean contains(Class<?> type) {

    return registry.containsValue(type);
  }

  @Override
  public Class<?> get(String alias) {
    if (!registry.containsKey(alias)) {
      throw new NotRegisteredAuditTypeException();
    }
    return registry.get(alias);
  }

  @Override
  public void register(String alias, Class<?> type) {
    if (registry.containsKey(alias)) {
      Class<?> registered = registry.get(alias);
      if (!registered.equals(type)) {
        throw new AlreadyAuditAliasExistsException();
      }
    }
    registry.put(alias, type);
  }

}
