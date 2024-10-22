package tech.wetech.flexmodel.graphql;

import graphql.ExecutionResult;
import graphql.execution.instrumentation.DocumentAndVariables;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.parameters.InstrumentationCreateStateParameters;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import graphql.language.*;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.jsonlogic.JsonLogic;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

/**
 * @author cjbi
 */
public class FlexmodelInstrumentation implements Instrumentation {


  private static final Logger log = LoggerFactory.getLogger(FlexmodelInstrumentation.class);

  @Override
  public InstrumentationState createState(InstrumentationCreateStateParameters parameters) {
    return new FlexmodelInstrumentationState();
  }

  @Override
  public DocumentAndVariables instrumentDocumentAndVariables(DocumentAndVariables documentAndVariables, InstrumentationExecutionParameters parameters, InstrumentationState state) {
    final FlexmodelInstrumentationState state1 = (FlexmodelInstrumentationState) state;
    Document document = documentAndVariables.getDocument();
    // 使用 NodeVisitor 遍历节点
    NodeVisitor visitor = new NodeVisitorStub() {
      @Override
      @SuppressWarnings("all")
      public TraversalControl visitDirective(Directive node, TraverserContext context) {
        switch (node.getName()) {
          case "transform":
            String path = node2Path(context.visitedNodes());
            String value = ((StringValue) node.getArgument("get").getValue()).getValue();
            state1.addTransform(path, value);
            break;
        }
        return super.visitDirective(node, context);
      }
    };

    // 使用 NodeTraverser 来遍历文档中的节点
    NodeTraverser traverser = new NodeTraverser();
    traverser.preOrder(visitor, document.getDefinitions());
    return documentAndVariables;
  }

  private String node2Path(Set<?> nodes) {
    StringJoiner joiner = new StringJoiner(".");
    for (Object node : nodes) {
      if (node instanceof Field field) {
        joiner.add(field.getAlias());
      }
    }
    return joiner.toString();
  }

  @Override
  public CompletableFuture<ExecutionResult> instrumentExecutionResult(ExecutionResult executionResult, InstrumentationExecutionParameters parameters, InstrumentationState state) {
    Object data = executionResult.getData();
    try {
      FlexmodelInstrumentationState state1 = (FlexmodelInstrumentationState) state;
      if (data instanceof Map dataMap) {
        state1.getTransformMap().forEach((key, value) -> {
          Map<String, Object> originValue = (Map<String, Object>) evaluate(key, dataMap);
          Object newValue = evaluate(value, originValue);
          dataMap.put(key, newValue);
        });
      }
      return CompletableFuture.completedFuture(executionResult.transform(builder -> builder.data(data)));
    } catch (Exception e) {
      log.error("Execution result transform error: {}", e.getMessage());
      return CompletableFuture.completedFuture(executionResult);
    }
  }

  private Object evaluate(String name, Object data) {
    String[] keys = name.split("\\.");
    Object result = data;

    for (String partial : keys) {
      result = evaluatePartialVariable(partial, result);

      if (result == null) {
        return null;
      }
    }

    return result;
  }

  private Object evaluatePartialVariable(String key, Object data) {
    if (JsonLogic.isEligible(data)) {
      List list = (List) data;
      int index;
      try {
        index = Integer.parseInt(key);
      } catch (NumberFormatException e) {
        throw e;
      }

      if (index < 0 || index >= list.size()) {
        return null;
      }

      return list.get(index);
    }

    if (data instanceof Map) {
      return ((Map) data).get(key);
    }

    return null;
  }


}
