package pico.erp.audit;

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
public class ValueChangeData extends ChangeData {

  String from;

  String to;

  @Builder
  public ValueChangeData(String property, String description, String from, String to) {
    super(property, description);
    this.from = from;
    this.to = to;
  }
}
