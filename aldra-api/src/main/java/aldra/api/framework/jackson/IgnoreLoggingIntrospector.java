package aldra.api.framework.jackson;

import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;

public class IgnoreLoggingIntrospector extends JacksonAnnotationIntrospector {

  @Override
  public boolean hasIgnoreMarker(MapperConfig<?> config, AnnotatedMember m) {
    return super.hasIgnoreMarker(config, m) || m.hasAnnotation(IgnoreLogging.class);
  }
}
