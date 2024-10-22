package tech.wetech.flexmodel.graphql;

import graphql.execution.instrumentation.InstrumentationState;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
public class FlexmodelInstrumentationState implements InstrumentationState {

  private final Map<String, String> transformMap = new HashMap<>();

  public void addTransform(String name, String value) {
    transformMap.put(name, value);
  }

  public Map<String, String> getTransformMap() {
    return transformMap;
  }
}
