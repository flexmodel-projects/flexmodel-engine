package tech.wetech.flexmodel.validator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.generator.DatetimeNowValueGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
class ValidatorFacadeTest {

  private MapMappedModels mappedModels;
  private ValidatorFacade validatorFacade;

  public static final String MODEL_NAME = "students";

  @BeforeEach
  void setUp() {
    this.mappedModels = new MapMappedModels();
    this.validatorFacade = new ValidatorFacade("system", mappedModels);

    Entity entity = new Entity(MODEL_NAME);
    // 主键
    BigintField idField = new BigintField("id");
    idField.setComment("Primary Key");
    entity.addField(new IDField("id").setGeneratedValue(IDField.GeneratedValue.AUTO_INCREMENT).setComment("Primary Key"));
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
    mappedModels.persist("system", entity);

    // 路径
    StringField baseUrl = new StringField("url");
    baseUrl.setComment("路径");
    baseUrl.addValidator(new URLValidator());
    entity.addField(baseUrl);
    mappedModels.persist("system", entity);

    // 日期时间验证
    DatetimeField sDatetime = new DatetimeField("s_datetime");
    sDatetime.setComment("日期时间验证");
    sDatetime.addValidator(new DatetimeRangeValidator(
      LocalDateTime.of(2024, 1, 1, 0, 0, 0),
      LocalDateTime.of(2024, 12, 31, 23, 59, 59))
    );
    sDatetime.addValidator(new DatetimeMinValidator(
      LocalDateTime.of(2024, 1, 1, 0, 0, 0)
    ));
    sDatetime.addValidator(new DatetimeMaxValidator(
      LocalDateTime.of(2024, 12, 31, 23, 59, 59))
    );
    entity.addField(sDatetime);

    // 日期验证
    DateField sDate = new DateField("s_date");
    sDate.setComment("日期验证");
    sDate.addValidator(new DateRangeValidator(LocalDate.of(2024, 1, 1),
      LocalDate.of(2024, 12, 31)
    ));
    sDate.addValidator(new DateMinValidator(
      LocalDate.of(2024, 1, 1)
    ));
    sDate.addValidator(new DateMaxValidator(
      LocalDate.of(2024, 12, 31)
    ));
    entity.addField(sDate);

  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void testNotNullValidator() {
    Assertions.assertDoesNotThrow(() -> {
      HashMap<String, Object> data = new HashMap<>();
      data.put("name", "张三");
      data.put("age", 1);
      validatorFacade.validate(MODEL_NAME, data);
    });
    Assertions.assertThrows(DataValidException.class,
      () -> validatorFacade.validateAll(MODEL_NAME, Map.of()));
    HashMap<String, Object> data = new HashMap<>();
    data.put("name", null);
    data.put("age", null);
    Assertions.assertThrows(DataValidException.class,
      () -> validatorFacade.validate(MODEL_NAME, data));
  }

  @Test
  void testNumberRangeValidator() {
    Assertions.assertDoesNotThrow(() -> {
      HashMap<String, Object> data = new HashMap<>();
      data.put("age", 1);
      validatorFacade.validate(MODEL_NAME, data);
    });
    Assertions.assertDoesNotThrow(() -> {
      HashMap<String, Object> data = new HashMap<>();
      data.put("age", 100);
      validatorFacade.validate(MODEL_NAME, data);
    });
    Assertions.assertThrows(DataValidException.class, () -> {
      HashMap<String, Object> data = new HashMap<>();
      data.put("age", 101);
      validatorFacade.validate(MODEL_NAME, data);
    });
  }

  @Test
  void testMainValidator() {
    validatorFacade.validate(MODEL_NAME, Map.of("email", "cjbi@outlook.com"));
    Assertions.assertThrows(DataValidException.class, () -> {
      validatorFacade.validate(MODEL_NAME, Map.of("email", "Illegal text"));
    });
  }

  @Test
  void testUrlValidator() {
    validatorFacade.validate(MODEL_NAME, Map.of("url", "https://www.baidu.com"));
    validatorFacade.validate(MODEL_NAME, Map.of("url", "https://www.baidu.com/path"));
    Assertions.assertThrows(DataValidException.class,
      () -> validatorFacade.validate(MODEL_NAME, Map.of("url", "Illegal text")));
  }

  @Test
  void testDatetimeRangeValidator() {
    validatorFacade.validate(MODEL_NAME, Map.of("s_datetime", LocalDateTime.of(2024, 1, 1, 0, 0, 0)));
    Assertions.assertThrows(DataValidException.class,
      () -> validatorFacade.validate(MODEL_NAME, Map.of("s_datetime", LocalDateTime.of(2025, 1, 1, 0, 0, 0))));
  }

  @Test
  void testDateRangeValidator() {
    validatorFacade.validate(MODEL_NAME, Map.of("s_date", LocalDate.of(2024, 1, 1)));
    Assertions.assertThrows(DataValidException.class,
      () -> validatorFacade.validate(MODEL_NAME, Map.of("s_date", LocalDate.of(2025, 1, 1))));
  }

}
