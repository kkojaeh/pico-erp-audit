package pico.erp.audit;

import java.util.Collection;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.repository.api.JaversRepository;
import org.javers.repository.sql.ConnectionProvider;
import org.javers.repository.sql.DialectName;
import org.javers.repository.sql.SqlRepositoryBuilder;
import org.javers.spring.jpa.JpaHibernateConnectionProvider;
import org.javers.spring.jpa.TransactionalJaversBuilder;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.AbstractLazyCreationTargetSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import pico.erp.audit.AuditConfigurerImpl.JaversConfiguration;
import pico.erp.audit.annotation.Audit;

@Component
@Import(JaversConfiguration.class)
public class AuditConfigurerImpl implements AuditConfigurer {

  private final TransactionalJaversBuilder builder = TransactionalJaversBuilder.javers();

  @Autowired
  ConnectionProvider connectionProvider;

  @Autowired
  JaversProperties javersProperties;

  @Autowired
  PlatformTransactionManager platformTransactionManager;

  @Autowired
  private AuditRegistry auditRegistry;

  private boolean initialized = false;

  @Lazy
  @Autowired
  private Collection<AuditConfiguration> configurations;

  @Transactional
  @Override
  public Javers buildJavers() {
    JaversRepository javersRepository = SqlRepositoryBuilder.sqlRepository().
      withConnectionProvider(connectionProvider)
      .withDialect(javersProperties.getDialect())
      .build();
    javersRepository.ensureSchema();
    builder
      .withTxManager(platformTransactionManager)
      .registerJaversRepository(javersRepository);
    return ProxyFactory.getProxy(Javers.class, new JaversTargetSource(builder));
  }

  @SneakyThrows
  public void initialize() {
    initialized = true;
    if (configurations == null || configurations.isEmpty()) {
      return;
    }

    configurations.forEach(configuration -> {
      configuration.getEntities().stream()
        .filter(type -> !type.equals(Object.class))
        .forEach(builder::registerEntity);
      configuration.getValueObjects().stream()
        .filter(type -> !type.equals(Object.class))
        .forEach(builder::registerValueObject);
    });

    ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
      false);
    provider.addIncludeFilter(new AnnotationTypeFilter(Audit.class));

    configurations.stream()
      .map(configuration -> configuration.getPackageToScan())
      .filter(packageName -> !StringUtils.isEmpty(packageName))
      .flatMap(packageName -> provider.findCandidateComponents(packageName).stream())
      .filter(beanDefinition -> beanDefinition instanceof ScannedGenericBeanDefinition)
      .map(beanDefinition -> (ScannedGenericBeanDefinition) beanDefinition)
      .forEach(scannedGenericBeanDefinition -> {
        Map<String, Object> attributes = scannedGenericBeanDefinition.getMetadata()
          .getAnnotationAttributes(Audit.class.getName());
        try {
          Class<?> type = Class.forName(scannedGenericBeanDefinition.getBeanClassName());
          String alias = attributes.get("alias").toString();
          if (!auditRegistry.contains(type)) {
            auditRegistry.register(alias, type);
          }
        } catch (ClassNotFoundException e) {
        }
      });

  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Configuration
  public static class JaversConfiguration {

    @Bean
    public ConnectionProvider connectionProvider() {
      return new JpaHibernateConnectionProvider();
    }

    @Bean
    @ConfigurationProperties("javers")
    public JaversProperties javersProperties() {
      return new JaversProperties();
    }
  }

  @Data
  private static class JaversProperties {

    DialectName dialect;

  }

  @AllArgsConstructor
  private class JaversTargetSource extends AbstractLazyCreationTargetSource {

    final JaversBuilder builder;

    @Override
    protected Object createObject() throws Exception {
      return builder.build();
    }

    @Override
    public Class<?> getTargetClass() {
      return Javers.class;
    }

  }

}
