package tech.wetech.flexmodel.jsonlogic.evaluator.sql;

import java.util.Map;

/**
 * @author cjbi
 * @date 2022/9/6
 */
public record NamedSqlRenderResult(String sqlClause, Map<String, Object> args) implements SqlRenderResult {

}
