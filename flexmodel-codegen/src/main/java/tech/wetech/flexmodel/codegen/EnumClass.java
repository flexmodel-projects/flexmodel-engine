package tech.wetech.flexmodel.codegen;

import tech.wetech.flexmodel.Enum;

import java.util.List;

/**
 * @author cjbi
 */
public class EnumClass extends AbstractClass<EnumClass> {

  private List<String> elements;
  private Enum original;

  public static EnumClass buildEnumClass(String packageName, String schemaName, Enum anEnum) {
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

  public Enum getOriginal() {
    return original;
  }

  public EnumClass setOriginal(Enum original) {
    this.original = original;
    return this;
  }
}
