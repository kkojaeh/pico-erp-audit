package pico.erp.audit.data;

import java.util.Date;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import pico.erp.shared.data.Auditor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommitData {

  Auditor committer;

  Date commitDate;

  Set<ChangeData> changes;

  boolean initial;
}
