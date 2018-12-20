package pico.erp.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.javers.core.Javers;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.diff.Change;
import org.javers.core.diff.changetype.NewObject;
import org.javers.core.diff.changetype.ObjectRemoved;
import org.javers.core.diff.changetype.ReferenceChange;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.diff.changetype.container.CollectionChange;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.core.metamodel.object.GlobalId;
import org.javers.core.metamodel.object.InstanceId;
import org.javers.core.metamodel.object.ValueObjectId;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import pico.erp.audit.AuditExceptions.NotRegisteredAuditTypeException;
import pico.erp.shared.Public;
import pico.erp.shared.data.Auditor;

@Service
@Public
@Transactional(propagation = Propagation.REQUIRES_NEW)
@Validated
public class AuditServiceLogic implements AuditService {

  @Autowired
  AuditorAware<Auditor> auditorAware;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  Javers javers;

  @Autowired
  AuditRegistry auditRegistry;

  @Value("${audit.enabled}")
  boolean enabled;

  @Autowired
  private MessageSource messageSource;

  @Override
  public <T> void commit(T instance) {
    if (!auditRegistry.contains(instance.getClass())) {
      throw new NotRegisteredAuditTypeException();
    }
    if (enabled) {
      try {
        javers.commit(getAuthor(), instance);
      } catch (Throwable t) {
        // TODO: 실패시 기록함
      }
    }
  }

  protected ChangeData convertChange(Change c, AuditId auditKey, Class<?> type,
    Locale locale) {
    String fragment = ".";
    GlobalId globalId = c.getAffectedGlobalId();
    if (globalId instanceof ValueObjectId) {
      ValueObjectId voi = (ValueObjectId) globalId;
      fragment = "." + voi.getFragment() + ".";
    }
    if (c instanceof ValueChange) {
      ValueChange vc = (ValueChange) c;

      return ValueChangeData.builder()
        .from(printValue(vc.getLeft()))
        .to(printValue(vc.getRight()))
        .property(vc.getPropertyName())
        .description(messageSource
          .getMessage(auditKey.getAlias() + fragment + vc.getPropertyName(), null,
            auditKey.getAlias() + fragment + vc.getPropertyName(), locale))
        .build();
    } else if (c instanceof NewObject) {
    } else if (c instanceof ObjectRemoved) {
    } else if (c instanceof CollectionChange) {
      CollectionChange cc = (CollectionChange) c;
      return CollectionChangeData.builder()
        .property(cc.getPropertyName())
        .description(messageSource
          .getMessage(auditKey.getAlias() + fragment + cc.getPropertyName(), null,
            cc.getPropertyName(), locale))
        .removed(
          cc.getValueRemovedChanges().stream().map(v -> printValue(v.getValue()))
            .collect(Collectors.toSet())
        )
        .added(
          cc.getValueAddedChanges().stream().map(v -> printValue(v.getValue()))
            .collect(Collectors.toSet())
        )
        .build();
    } else if (c instanceof ReferenceChange) {
      ReferenceChange rc = (ReferenceChange) c;
      return ValueChangeData.builder()
        .from(getDescription(rc.getLeftObject().orElse(null)))
        .to(getDescription(rc.getRightObject().orElse(null)))
        .property(rc.getPropertyName())
        .description(messageSource
          .getMessage(auditKey.getAlias() + fragment + rc.getPropertyName(), null,
            auditKey.getAlias() + fragment + rc.getPropertyName(), locale))
        .build();
    }
    return null;
  }

  @SneakyThrows
  protected CommitData convertCommit(CdoSnapshot snapshot, AuditId auditId, Class<?> type,
    Locale locale) {
    CommitMetadata commitMetadata = snapshot.getCommitMetadata();
    return CommitData.builder()
      .initial(snapshot.isInitial())
      .committer(
        objectMapper.readValue(snapshot.getCommitMetadata().getAuthor(), Auditor.class))
      .commitDate(
        Date.from(commitMetadata.getCommitDate().atZone(ZoneId.systemDefault()).toInstant()))
      .changes(
        javers.findChanges(
          QueryBuilder.byInstanceId(auditId.getId(), type).withCommitId(snapshot.getCommitId())
            .withChildValueObjects()
            .build())
          .stream()
          .map(change -> convertChange(change, auditId, type, locale))
          .filter(Objects::nonNull)
          .collect(Collectors.toSet())
      )
      .build();
  }

  @Override
  public <T> void delete(T instance) {
    if (!auditRegistry.contains(instance.getClass())) {
      throw new NotRegisteredAuditTypeException();
    }
    if (enabled) {
      javers.commitShallowDelete(getAuthor(), instance);
    }
  }

  @Override
  public List<CommitData> get(AuditId auditId) {
    Class<?> type = auditRegistry.get(auditId.getAlias());
    Locale locale = LocaleContextHolder.getLocale();
    return javers.findSnapshots(QueryBuilder.byInstanceId(auditId.getId(), type)
      .withChildValueObjects()
      .build())
      .stream()
      .filter(snapshot -> snapshot.getManagedType().getBaseJavaClass().equals(type) || !snapshot
        .isInitial())
      .map(snapshot -> convertCommit(snapshot, auditId, type, locale))
      .collect(Collectors.toList());
  }

  @SneakyThrows
  protected String getAuthor() {
    Auditor auditor = auditorAware.getCurrentAuditor();
    return objectMapper.writeValueAsString(auditor);
  }

  @SneakyThrows
  private String getDescription(Object object) {
    if (object == null) {
      return null;
    }
    if (object instanceof InstanceId) {
      InstanceId instanceId = (InstanceId) object;
      return instanceId.getCdoId().toString();
    }
    return null;
  }

  protected String printValue(Object value) {
    return value != null ? value.toString() : "";
  }

}
