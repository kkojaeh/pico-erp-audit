package pico.erp.audit;

import javax.validation.constraints.NotNull;

public interface AuditRegistry {

  boolean contains(@NotNull Class<?> type);

  Class<?> get(@NotNull String alias);

  void register(@NotNull String alias, @NotNull Class<?> type);

}
