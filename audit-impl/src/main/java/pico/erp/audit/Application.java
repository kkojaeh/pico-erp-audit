package pico.erp.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.javers.core.Javers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import pico.erp.audit.core.AuditConfigurer;
import pico.erp.audit.data.AuditConfiguration;
import pico.erp.shared.ApplicationInitializer;
import pico.erp.shared.ApplicationStarter;
import pico.erp.shared.Public;
import pico.erp.shared.SpringBootConfigs;
import pico.erp.shared.impl.ApplicationImpl;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Slf4j
@SpringBootConfigs
public class Application implements ApplicationStarter {

  public static final String CONFIG_NAME = "audit/application";

  public static final String CONFIG_NAME_PROPERTY = "spring.config.name=audit/application";

  public static final Properties DEFAULT_PROPERTIES = new Properties();

  static {
    DEFAULT_PROPERTIES.put("spring.config.name", CONFIG_NAME);
  }

  @Autowired
  private ApplicationContext applicationContext;

  public static SpringApplication application() {
    return new SpringApplicationBuilder(Application.class)
      .properties(DEFAULT_PROPERTIES)
      .web(false)
      .build();
  }

  public static void main(String[] args) {
    application().run(args);
  }

  @Autowired
  private AuditConfigurer auditConfigurer;

  @Autowired
  private TaskScheduler taskScheduler;

  @Override
  public boolean isWeb() {
    return false;
  }

  @Bean
  public Javers javers() {
    return auditConfigurer.buildJavers();
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Override
  public int getOrder() {
    return Integer.MIN_VALUE;
  }

  @Bean
  @ConditionalOnMissingBean(AuditConfiguration.class)
  public AuditConfiguration noOpAuditConfiguration() {
    return AuditConfiguration.builder().build();
  }

  @Override
  public pico.erp.shared.Application start(String... args) {
    return new ApplicationImpl(application().run(args));
  }

  @Public
  @Bean
  public ApplicationInitializer applicationInitializer() {
    return () -> {
      if (!auditConfigurer.isInitialized()) {
        auditConfigurer.initialize();
      }
    };
  }

}
