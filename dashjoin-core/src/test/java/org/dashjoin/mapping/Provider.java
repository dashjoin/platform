package org.dashjoin.mapping;

import static java.util.Arrays.asList;
import static org.dashjoin.util.MapUtil.of;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.SecurityContext;
import com.google.common.collect.ImmutableMap;

public class Provider extends AbstractSource {

  @Override
  public Map<String, List<Map<String, Object>>> gather(SecurityContext sc) throws Exception {
    return ImmutableMap.of("t", asList(of("ID", 1, "name", "a"), of("ID", 2, "name", "b")));
  }
}
