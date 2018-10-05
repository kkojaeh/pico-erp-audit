package pico.erp.audit;

import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CollectionChangeData extends ChangeData {

  Set<String> removed;

  Set<String> added;

  @Builder
  public CollectionChangeData(String property, String description, Set<String> removed,
    Set<String> added) {
    super(property, description);
    this.removed = removed;
    this.added = added;
  }
}
