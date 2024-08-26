package tech.wetech.flexmodel.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public class NamedParamStatement implements AutoCloseable {

  private final PreparedStatement prepStmt;
  private final ParsedSql parsedSql;

  protected NamedParamStatement(Connection conn, String statementWithNames, String[] retrieveGeneratedKeys) throws SQLException {
    this.parsedSql = parseSqlStatement(statementWithNames);
    if (retrieveGeneratedKeys.length == 0) {
      prepStmt = conn.prepareStatement(parsedSql.getActualSql());
    } else {
      prepStmt = conn.prepareStatement(parsedSql.getActualSql(), retrieveGeneratedKeys);
    }
  }

  protected NamedParamStatement(Connection conn, String statementWithNames, boolean returnFirstId) throws SQLException {
    this.parsedSql = parseSqlStatement(statementWithNames);
    if (returnFirstId) {
      prepStmt = conn.prepareStatement(parsedSql.getActualSql(), PreparedStatement.RETURN_GENERATED_KEYS);
    } else {
      prepStmt = conn.prepareStatement(parsedSql.getActualSql());
    }
  }

  public NamedParamStatement(Connection conn, String statementWithNames) throws SQLException {
    this(conn, statementWithNames, new String[]{});
  }

  public void addBatch() throws SQLException {
    prepStmt.addBatch();
  }

  public int[] executeBatch() throws SQLException {
    return prepStmt.executeBatch();
  }

  public void setParameters(Map<String, Object> paramMap) throws SQLException {
    for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      this.setObject(key, value);
    }
  }

  public PreparedStatement getPreparedStatement() {
    return prepStmt;
  }

  public ResultSet executeQuery() throws SQLException {
    return prepStmt.executeQuery();
  }

  public int executeUpdate() throws SQLException {
    return prepStmt.executeUpdate();
  }

  public ResultSet getGeneratedKeys() throws SQLException {
    return prepStmt.getGeneratedKeys();
  }

  @Override
  public void close() throws SQLException {
    prepStmt.close();
  }

  public void setObject(String name, Object value) throws SQLException {
    List<Integer> indexes = getIndexes(name);
    for (Integer index : indexes) {
      prepStmt.setObject(index, value);
    }
  }

  private List<Integer> getIndexes(String name) {
    List<Integer> list = new ArrayList<>();
    for (int i = 0; i < parsedSql.getParameterNames().size(); i++) {
      if (name.equals(parsedSql.getParameterNames().get(i))) {
        list.add(i + 1);
      }
    }
    return list;
  }

  public ParsedSql parseSqlStatement(String sql) {
    sql += ' ';
    ParsedSql parsedSql = new ParsedSql(sql);
    char[] statement = sql.toCharArray();
    int i = 0;
    while (i < statement.length) {
      int skipToPosition = i;
      while (i < statement.length) {
        skipToPosition = skipCommentsAndQuotes(statement, i);
        if (i == skipToPosition) {
          break;
        } else {
          i = skipToPosition;
        }
      }
      if (i >= statement.length) {
        break;
      }
      char c = statement[i];
      if (c == ':') {
        int j = i + 1;
        if (c == ':' && j < statement.length && statement[j] == ':') {
          // Postgres-style "::" casting operator should be skipped
          i = i + 2;
          continue;
        }
        while (j < statement.length) {
          c = statement[j];
          if (isParameterSeparator(c)) {
            parsedSql.addNamedParameter(sql.substring(i + 1, j), i + 1, j);
            i = j;
            break;
          }
          j++;
        }
      }
      i++;
    }


    return parsedSql;
  }

  /**
   * Set of characters that qualify as comment or quotes starting characters.
   */
  private static final String[] START_SKIP = new String[]{"'", "\"", "--", "/*"};

  /**
   * Set of characters that at are the corresponding comment or quotes ending characters.
   */
  private static final String[] STOP_SKIP = new String[]{"'", "\"", "\n", "*/"};

  private static final String PARAMETER_SEPARATORS = "\"':&,;()|=+-*%/\\<>^";

  /**
   * An index with separator flags per character code.
   * Technically only needed between 34 and 124 at this point.
   */
  private static final boolean[] separatorIndex = new boolean[128];

  static {
    for (char c : PARAMETER_SEPARATORS.toCharArray()) {
      separatorIndex[c] = true;
    }
  }

  /**
   * Skip over comments and quoted names present in an SQL statement.
   *
   * @param statement character array containing SQL statement
   * @param position  current position of statement
   * @return next position to process after any comments or quotes are skipped
   */
  private static int skipCommentsAndQuotes(char[] statement, int position) {
    for (int i = 0; i < START_SKIP.length; i++) {
      if (statement[position] == START_SKIP[i].charAt(0)) {
        boolean match = true;
        for (int j = 1; j < START_SKIP[i].length(); j++) {
          if (statement[position + j] != START_SKIP[i].charAt(j)) {
            match = false;
            break;
          }
        }
        if (match) {
          int offset = START_SKIP[i].length();
          for (int m = position + offset; m < statement.length; m++) {
            if (statement[m] == STOP_SKIP[i].charAt(0)) {
              boolean endMatch = true;
              int endPos = m;
              for (int n = 1; n < STOP_SKIP[i].length(); n++) {
                if (m + n >= statement.length) {
                  // last comment not closed properly
                  return statement.length;
                }
                if (statement[m + n] != STOP_SKIP[i].charAt(n)) {
                  endMatch = false;
                  break;
                }
                endPos = m + n;
              }
              if (endMatch) {
                // found character sequence ending comment or quote
                return endPos + 1;
              }
            }
          }
          // character sequence ending comment or quote not found
          return statement.length;
        }
      }
    }
    return position;
  }

  private boolean isParameterSeparator(char c) {
    return (c < 128 && separatorIndex[c]) || Character.isWhitespace(c);
  }

  static class ParsedSql {
    private final String originalSql;
    private String actualSql;
    private final List<String> parameterNames = new ArrayList<>();
    private final List<int[]> parameterIndexes = new ArrayList<>();

    public ParsedSql(String originalSql) {
      this.originalSql = originalSql;
    }

    /**
     * Add a named parameter parsed from this SQL statement.
     *
     * @param parameterName the name of the parameter
     * @param startIndex    the start index in the original SQL String
     * @param endIndex      the end index in the original SQL String
     */
    void addNamedParameter(String parameterName, int startIndex, int endIndex) {
      this.parameterNames.add(parameterName);
      this.parameterIndexes.add(new int[]{startIndex, endIndex});
    }

    public String getOriginalSql() {
      return originalSql;
    }

    public List<String> getParameterNames() {
      return parameterNames;
    }

    int[] getParameterIndexes(int parameterPosition) {
      return this.parameterIndexes.get(parameterPosition);
    }

    String getActualSql() {
      if (actualSql == null) {
        List<String> paramNames = parameterNames;
        if (paramNames.isEmpty()) {
          return originalSql;
        }
        StringBuilder actualSql = new StringBuilder(originalSql.length());
        int lastIndex = 0;
        for (int i = 0; i < paramNames.size(); i++) {
          int[] indexes = this.getParameterIndexes(i);
          int startIndex = indexes[0];
          int endIndex = indexes[1];
          actualSql.append(originalSql, lastIndex, startIndex - 1);
          actualSql.append('?');
          lastIndex = endIndex;
        }
        actualSql.append(originalSql, lastIndex, originalSql.length());
        this.actualSql = actualSql.toString();
      }
      return actualSql;
    }

  }

}
