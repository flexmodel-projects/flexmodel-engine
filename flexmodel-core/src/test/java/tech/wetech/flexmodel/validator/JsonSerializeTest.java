package tech.wetech.flexmodel.validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.generator.DatetimeNowValueGenerator;

/**
 * @author cjbi
 */
public class JsonSerializeTest {

  @Test
  void test() {
    Entity entity = new Entity("students");
    // 主键
    BigintField idField = new BigintField("id");
    idField.setComment("Primary Key");
    entity.addField(new IDField("id").setGeneratedValue(IDField.GeneratedValue.UUID).setComment("Primary Key"));
    entity.setComment("学生表");
    // 姓名
    StringField name = new StringField("name");
    name.setComment("姓名");
    name.setNullable(false);
    name.setLength(10);
    name.addValidator(new RegexpValidator("^.{2,5}$"));
    entity.addField(name);
    BigintField age = new BigintField("age");
    age.setComment("年龄");
    age.addValidator(new NumberRangeValidator<>(1, 100));
    age.addValidator(new NumberMinValidator<>(1));
    age.addValidator(new NumberMaxValidator<>(100));
    entity.addField(age);
    // 备注
    TextField description = new TextField("description");
    description.setComment("备注");
    entity.addField(description);
    // 生日
    DateField birthday = new DateField("birthday");
    birthday.setComment("生日");
    entity.addField(birthday);
    // 是否禁用
    BooleanField isLocked = new BooleanField("isLocked");
    isLocked.setNullable(false);
    isLocked.setDefaultValue(false);
    isLocked.setComment("是否禁用");
    entity.addField(isLocked);
    // 创建时间
    DatetimeField createDatetime = new DatetimeField("createDatetime");
    createDatetime.setComment("创建日期时间");
    createDatetime.setNullable(false);
    createDatetime.setGenerator(new DatetimeNowValueGenerator());
    entity.addField(createDatetime);
    // 扩展信息
    JsonField extra = new JsonField("extra");
    extra.setComment("扩展信息");
    entity.addField(extra);
    // 邮箱
    StringField email = new StringField("email");
    email.setComment("邮箱");
    email.setLength(20);
    email.addValidator(new EmailValidator());
    entity.addField(email);

    String json = JsonUtils.getInstance().stringify(entity);
    Entity deSerializeEntity = JsonUtils.getInstance().parseToObject(json, Entity.class);
    Assertions.assertNotNull(deSerializeEntity);
  }

}
