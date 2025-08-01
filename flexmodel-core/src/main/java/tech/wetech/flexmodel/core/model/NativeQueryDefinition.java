package tech.wetech.flexmodel.core.model;

import tech.wetech.flexmodel.core.query.Query;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 本地查询
 *
 * @author cjbi
 */
public class NativeQueryDefinition extends AbstractModelDefinition<NativeQueryDefinition> {

  private String statement;

  public NativeQueryDefinition(String name) {
    this.name = name;
  }

  public String getStatement() {
    return statement;
  }

  public NativeQueryDefinition setStatement(String statement) {
    this.statement = statement;
    return this;
  }

  @Override
  public String getType() {
    return "NATIVE_QUERY";
  }

  /**
   * 提取参数
   *
   * @return
   */
  public Set<String> getParameters() {
    return extractParameters(statement);
  }

  /**
   * 提取字符串中的所有参数名，格式为 ${name}
   *
   * @param text 需要提取参数的字符串
   * @return 包含提取到的参数名的列表
   */
  private Set<String> extractParameters(String text) {
    Set<String> parameters = new HashSet<>();

    // 正则表达式用于匹配 ${...}
    Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
    Matcher matcher = pattern.matcher(text);

    // 查找所有匹配的参数
    while (matcher.find()) {
      parameters.add(matcher.group(1)); // group(1) 返回括号内的内容
    }

    return parameters;
  }

  @Override
  public List<Query.QueryField> getFields() {
    return List.of();
  }

  @Override
  public boolean equals(Object obj) {
    if (this.getName() != null && obj instanceof NativeQueryDefinition) {
      return this.getName().equals(((NativeQueryDefinition) obj).getName());
    }
    return false;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "<" + getName() + ">";
  }

}
