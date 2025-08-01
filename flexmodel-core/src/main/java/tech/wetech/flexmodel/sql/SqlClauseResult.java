package tech.wetech.flexmodel.sql;

import java.util.Map;

/**
 * @author cjbi
 */
public record SqlClauseResult(String sqlClause, Map<String, Object> args) {
}
