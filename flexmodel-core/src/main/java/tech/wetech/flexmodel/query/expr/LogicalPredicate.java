package tech.wetech.flexmodel.query.expr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 逻辑表达式（AND/OR）
 *
 * @author cjbi
 */
public class LogicalPredicate extends Predicate {
  private final String operator;
  private final List<Predicate> predicates;

  public LogicalPredicate(String operator, List<Predicate> predicates) {
    super(null, operator, null);
    this.operator = operator;
    this.predicates = predicates;
  }

  @Override
  public Map<String, Object> toMap() {
    List<Map<String, Object>> conditions = new ArrayList<>();
    for (Predicate predicate : predicates) {
      Map<String, Object> condition = predicate.toMap();
      conditions.add(predicate.toMap());
//      if (!condition.containsKey(operator)) {
//        conditions.add(predicate.toMap());
//      }
    }
//    if (conditions.isEmpty()) {
//      return Collections.emptyMap(); // 如果所有条件都是默认条件，返回空 JSON
//    }
    return Collections.singletonMap(operator, conditions);
  }
}
