package aldra.api.settings;

import aldra.api.framework.jackson.IgnoreLoggingIntrospector;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.TimeZone;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

public class JacksonSettings {

  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.JAPAN);

  private static final DateTimeFormatter DATE_TIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.JAPAN);

  @Primary
  @Bean
  public ObjectMapper objectMapper() {
    return configureCommonSettings().build();
  }

  @Bean(name = "loggingObjectMapper")
  public ObjectMapper loggingObjectMapper() {
    return configureCommonSettings()
        .annotationIntrospector(new IgnoreLoggingIntrospector())
        .build();
  }

  private JsonMapper.Builder configureCommonSettings() {
    val customModule =
        new SimpleModule()
            .addSerializer(LocalDate.class, new LocalDateSerializer(DATE_FORMAT))
            .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMAT))
            .addDeserializer(LocalDate.class, new LocalDateDeserializer(DATE_FORMAT))
            .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMAT));

    return JsonMapper.builder()
        .defaultLocale(Locale.JAPAN)
        .defaultTimeZone(TimeZone.getTimeZone(ZoneId.of("Asia/Tokyo")))
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DateTimeFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS)
        .disable(DateTimeFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
        .enable(EnumFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
        .addModule(customModule);
  }
}
