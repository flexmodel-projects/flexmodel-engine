package tech.wetech.flexmodel;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.calculations.DateNowValueCalculator;
import tech.wetech.flexmodel.calculations.DatetimeNowValueCalculator;

import static tech.wetech.flexmodel.Direction.DESC;
import static tech.wetech.flexmodel.Projections.*;

/**
 * @author cjbi
 */
class SchemaOperationsTest extends AbstractSessionIntegrationTest {

  void createStudentEntity(String entityName) {
    Entity entity = session.createEntity(
      entityName, e -> e.setComment("学生")
        .addField(new IDField("id").setGeneratedValue(IDField.DefaultGeneratedValue.IDENTITY).setComment("Primary Key"))
    );
    // string
    StringField name = new StringField("name");
    name.setComment("姓名");
    name.setNullable(false);
    name.setLength(10);
    entity.addField(name);
    session.createField(entityName, name);
    // text
    TextField description = new TextField("description").setComment("备注");
    entity.addField(description);
    session.createField(entityName, description);
    // number
    IntField age = new IntField("age");
    age.setComment("年龄");
    entity.addField(age);
    session.createField(entityName, age);
    // boolean
    BooleanField deleted = new BooleanField("is_deleted");
    deleted.setComment("软删除");
    deleted.setDefaultValue(false);
    entity.addField(deleted);
    session.createField(entityName, deleted);
    //datetime
    DatetimeField createDatetime = new DatetimeField("createDatetime");
    createDatetime.setComment("创建日期时间");
    createDatetime.addCalculation(new DatetimeNowValueCalculator());
    entity.addField(createDatetime);
    session.createField(entityName, createDatetime);
    // date
    DateField birthday = new DateField("birthday");
    birthday.setComment("出生日期");
    birthday.addCalculation(new DateNowValueCalculator());
    entity.addField(birthday);
    session.createField(entityName, birthday);
    // json
    JsonField interests = new JsonField("interests");
    interests.setComment("兴趣爱好");
    entity.addField(interests);
    session.createField(entityName, interests);
  }

  private void createScoreEntity(String scoreModelName) {
    session.createEntity(scoreModelName, sScore ->
      sScore.addField(new BigintField("student_id"))
        .addField(new StringField("course_name"))
        .addField(new DecimalField("score"))
    );
  }

  void dropModel(String entityName) {
    session.dropModel(entityName);
  }

  @Test
  void testCreateEntity() {
    createStudentEntity("testCreateEntity_holiday");
  }

  @Test
  void testDropEntity() {
    String entityName = "testDropEntity_holiday";
    createStudentEntity(entityName);
    dropModel(entityName);
  }

  @Test
  void testCreateField() {
    String entityName = "testCreateField_students";
    createStudentEntity(entityName);
    dropModel(entityName);
  }

  @Test
  void testDropField() {
    String entityName = "testDropField_students";
    createStudentEntity(entityName);
    session.dropField(entityName, "name");
    session.dropField(entityName, "description");
    session.dropField(entityName, "age");
    session.dropField(entityName, "is_deleted");
    session.dropField(entityName, "createDatetime");
    session.dropField(entityName, "birthday");
    dropModel(entityName);
  }

  @Test
  void testCreateIndex() {
    String entityName = "testDropField_students";
    createStudentEntity(entityName);
    // when include single field
    Index index = new Index(entityName, "IDX_name");
    index.addField("name");
    session.createIndex(index);
    session.dropIndex(entityName, "IDX_name");
    // when include multiple field
    Index multipleFiledIndex = new Index(entityName);
    multipleFiledIndex.addField("birthday");
    multipleFiledIndex.addField("age", DESC);
    multipleFiledIndex.addField("is_deleted", DESC);
    multipleFiledIndex.setName("IDX_compound");
    session.createIndex(multipleFiledIndex);
    session.dropIndex(entityName, "IDX_compound");
    dropModel(entityName);
  }

  @Test
  void testCreateView() {
    String viewName = "testCreateView_student_score_report";
    String studentModelName = "testCreateView_students";
    String scoreModelName = "testCreateView_student_scores";
    createStudentEntity(studentModelName);
    createScoreEntity(scoreModelName);
    createScoreReportView(viewName, studentModelName, scoreModelName);
  }

  private void createScoreReportView(String viewName, String studentModelName, String scoreModelName) {
    session.createView(viewName, studentModelName, query ->
      query.setProjection(projection -> projection
          .addField("student_id", field("id"))
          .addField("student_name", max(field("name")))
          .addField("score_sum", sum(field(scoreModelName + ".score")))
          .addField("score_avg", avg(field(scoreModelName + ".score")))
          .addField("course_count", count(field(scoreModelName + ".course_name")))
        )
        .setJoins(joiners -> joiners
          .addInnerJoin(joiner -> joiner.setFrom(scoreModelName).setLocalField("id").setForeignField("student_id"))
        )
        .setFilter("""
          {
            "!=": [{ "var": ["id"] }, 999]
          }
          """)
        .setGroupBy(groupBy ->
          groupBy.addField("id")
        )
        .setSort(sort -> sort
          .addOrder("id", DESC)
        )
        .setLimit(100)
        .setOffset(10)
    );
  }

  @Test
  void testDropView() {
    String viewName = "testDropView_student_score_report";
    String studentModelName = "testDropView_students";
    String scoreModelName = "testDropView_student_scores";
    createStudentEntity(studentModelName);
    createScoreEntity(scoreModelName);
    createScoreReportView(viewName, studentModelName, scoreModelName);
    dropModel(viewName);
  }


  @Test
  void testCreateSequence() {
    String seqName = "user_seq";
    session.createSequence(seqName, 1, 1);
    long sequenceNextVal = 0;
    for (int i = 0; i < 10; i++) {
      sequenceNextVal = session.getSequenceNextVal(seqName);
    }
    Assertions.assertEquals(10, sequenceNextVal);
    session.dropSequence(seqName);
  }

}
