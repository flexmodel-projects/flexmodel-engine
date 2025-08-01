package tech.wetech.flexmodel.codegen;

import tech.wetech.flexmodel.core.model.EnumDefinition;

import java.util.List;

/**
 * @author cjbi
 */
public class EnumClass extends AbstractClass<EnumClass> {

  private List<String> elements;
  private EnumDefinition original;

  public static EnumClass buildEnumClass(String packageName, String schemaName, EnumDefinition anEnum) {
    String ftName = StringUtils.capitalize(StringUtils.snakeToCamel(anEnum.getName()));

    EnumClass enumClass = new EnumClass()
      .setSchemaName(schemaName)
      .setPackageName(packageName + ".enumeration")
      .setShortClassName(ftName)
      .setName(anEnum.getName());

    enumClass.setFullClassName(enumClass.getPackageName() + "." + ftName);
    enumClass.setVariableName(StringUtils.uncapitalize(ftName));
    enumClass.setElements(anEnum.getElements());
    enumClass.setComment(anEnum.getComment());
    enumClass.setOriginal(anEnum);
    return enumClass;
  }

  public List<String> getElements() {
    return elements;
  }

  public EnumClass setElements(List<String> elements) {
    this.elements = elements;
    return this;
  }

  public EnumDefinition getOriginal() {
    return original;
  }

  public EnumClass setOriginal(EnumDefinition original) {
    this.original = original;
    return this;
  }
}
