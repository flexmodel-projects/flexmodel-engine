package tech.wetech.flexmodel.sql;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.JdbcDatabaseContainer;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.calculations.DateNowValueCalculator;
import tech.wetech.flexmodel.calculations.DatetimeNowValueCalculator;
import tech.wetech.flexmodel.calculations.DefaultValueCalculator;
import tech.wetech.flexmodel.validations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author cjbi
 */
class JdbcMappedModelTest extends AbstractIntegrationTest {

  @BeforeAll
  static void init() {
    acceptCommand((s, container) -> container.start());
  }

  @AfterAll
  static void destroy() {
    acceptCommand((key, container) -> container.stop());
  }

  @Test
  void test() {
    acceptCommand((s, container) -> {
      if (container instanceof JdbcDatabaseContainer<?> jdbcDatabaseContainer) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcDatabaseContainer.getJdbcUrl());
        dataSource.setUsername(jdbcDatabaseContainer.getUsername());
        dataSource.setPassword(jdbcDatabaseContainer.getPassword());
        JdbcMappedModels jdbcMappedModel = new JdbcMappedModels(dataSource);

        final String schemaName = "default";
        final String MODEL_NAME = "students";

        Entity entity = new Entity(MODEL_NAME);

        entity.addField(new IDField("id").setGeneratedValue(IDField.DefaultGeneratedValue.UUID).setComment("Primary Key"));
        entity.setComment("学生表");
        // 姓名
        StringField name = new StringField("name");
        name.setComment("姓名");
        name.setNullable(false);
        name.setLength(10);
        name.addValidation(new RegexpValidator("^.{2,5}$"));
        entity.addField(name);
        BigintField age = new BigintField("age");
        age.setComment("年龄");
        age.addValidation(new NumberRangeValidator<>(1, 100));
        age.addValidation(new NumberMinValidator<>(1));
        age.addValidation(new NumberMaxValidator<>(100));
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
        createDatetime.addCalculation(new DatetimeNowValueCalculator());
        entity.addField(createDatetime);
        // 扩展信息
        JsonField extra = new JsonField("extra");
        extra.setComment("扩展信息");
        entity.addField(extra);
        // 邮箱
        StringField email = new StringField("email");
        email.setComment("邮箱");
        email.setLength(20);
        email.addValidation(new EmailValidator());
        entity.addField(email);
        // 路径
        StringField baseUrl = new StringField("url");
        baseUrl.setComment("路径");
        baseUrl.addValidation(new URLValidator());
        entity.addField(baseUrl);

        // 日期时间验证
        DatetimeField sDatetime = new DatetimeField("s_datetime");
        sDatetime.setComment("日期时间验证");
        sDatetime.addCalculation(new DefaultValueCalculator<>(LocalDateTime.now()));
        sDatetime.addCalculation(new DatetimeNowValueCalculator());
        sDatetime.addValidation(new DatetimeRangeValidator(
          LocalDateTime.of(2024, 1, 1, 0, 0, 0),
          LocalDateTime.of(2024, 12, 31, 23, 59, 59))
        );
        sDatetime.addValidation(new DatetimeMinValidator(
          LocalDateTime.of(2024, 1, 1, 0, 0, 0)
        ));
        sDatetime.addValidation(new DatetimeMaxValidator(
          LocalDateTime.of(2024, 12, 31, 23, 59, 59))
        );
        entity.addField(sDatetime);

        // 日期验证
        DateField sDate = new DateField("s_date");
        sDate.setComment("日期验证");
        sDate.addValidation(new DateRangeValidator(LocalDate.of(2024, 1, 1),
          LocalDate.of(2024, 12, 31)
        ));
        sDate.addValidation(new DateMinValidator(
          LocalDate.of(2024, 1, 1)
        ));
        sDate.addValidation(new DateMaxValidator(
          LocalDate.of(2024, 12, 31)
        ));
        sDate.addCalculation(new DateNowValueCalculator());
        entity.addField(sDate);

        jdbcMappedModel.persist(schemaName, entity);
        Model model = jdbcMappedModel.getModel(schemaName, entity.name());
        jdbcMappedModel.remove(schemaName, model.name());
      }
    });
  }
}
