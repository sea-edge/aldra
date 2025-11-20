package aldra.api.settings;

import aldra.api.framework.jackson.IgnoreLoggingIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.TimeZone;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

public class JacksonSettings {

  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.JAPAN);

  private static final DateTimeFormatter DATE_TIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.JAPAN);

  @Primary
  @Bean
  public ObjectMapper objectMapper() {
    return configureCommonSettings();
  }

  @Bean(name = "loggingObjectMapper")
  public ObjectMapper loggingObjectMapper() {
    val mapper = configureCommonSettings();
    mapper.setAnnotationIntrospector(new IgnoreLoggingIntrospector());
    return mapper;
  }

  private ObjectMapper configureCommonSettings() {
    ObjectMapper mapper = new ObjectMapper();

    // Property naming strategy
    mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    // Features configuration
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);

    // Timezone and locale
    mapper.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Asia/Tokyo")));
    mapper.setLocale(Locale.JAPAN);

    // Register modules
    mapper.registerModule(new JavaTimeModule());
    mapper.registerModule(new Jdk8Module());

    // Custom date/time serializers and deserializers
    SimpleModule customModule = new SimpleModule();
    customModule.addSerializer(LocalDate.class, new LocalDateSerializer(DATE_FORMAT));
    customModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMAT));
    customModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DATE_FORMAT));
    customModule.addDeserializer(
        LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMAT));
    mapper.registerModule(customModule);

    return mapper;
  }
}
