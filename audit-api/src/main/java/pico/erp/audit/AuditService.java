package pico.erp.audit;

import java.util.List;
import javax.validation.constraints.NotNull;

public interface AuditService {

  <T> void commit(@NotNull T instance);

  <T> void delete(@NotNull T instance);

  List<CommitData> get(@NotNull AuditId auditKey);

}
