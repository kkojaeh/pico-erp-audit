package pico.erp.audit;

import java.util.List;
import javax.validation.constraints.NotNull;
import pico.erp.audit.data.AuditId;
import pico.erp.audit.data.CommitData;

public interface AuditService {

  <T> void commit(@NotNull T instance);

  <T> void delete(@NotNull T instance);

  List<CommitData> get(@NotNull AuditId auditKey);

}
