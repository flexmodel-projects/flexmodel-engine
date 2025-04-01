package tech.wetech.flexmodel.parser;

import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.SchemaObject;
import tech.wetech.flexmodel.parser.impl.ModelParser;
import tech.wetech.flexmodel.parser.impl.ParseException;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public class ASTNodeConverterTest {

  @Test
  void test() throws ParseException {
    InputStream is = this.getClass().getClassLoader().getResourceAsStream("sample_input.sdl");
    ModelParser modelParser = new ModelParser(is);
    List<ModelParser.ASTNode> list = modelParser.CompilationUnit();
    List<SchemaObject> objectList = new ArrayList<>();
    for (ModelParser.ASTNode astNode : list) {
      objectList.add(ASTNodeConverter.toSchemaObject(astNode));
    }
    System.out.println(new JacksonObjectConverter().toJsonString(objectList));
    List<ModelParser.ASTNode> astNodeList = new ArrayList<>();
    for (SchemaObject schemaObject : objectList) {
      astNodeList.add(ASTNodeConverter.fromSchemaObject(schemaObject));
    }
    System.out.println(astNodeList);
  }

  @Test
  void test2() {
    String json = """
      [
          {
            "name": "fs_datasource",
            "type": "ENTITY",
            "fields": [
              {
                "name": "name",
                "type": "ID",
                "unique": false,
                "nullable": true,
                "modelName": "fs_datasource",
                "generatedValue": "STRING_NOT_GENERATED"
              },
              {
                "name": "type",
                "type": "Enum",
                "from": "DatasourceType",
                "multiple": false,
                "unique": false,
                "nullable": true,
                "modelName": "fs_datasource"
              },
              {
                "name": "config",
                "type": "JSON",
                "unique": false,
                "nullable": true,
                "modelName": "fs_datasource"
              },
              {
                "name": "createdAt",
                "type": "DateTime",
                "unique": false,
                "nullable": true,
                "modelName": "fs_datasource",
                "generatedValue": "NOW_ON_CREATE"
              },
              {
                "name": "updatedAt",
                "type": "DateTime",
                "unique": false,
                "nullable": true,
                "modelName": "fs_datasource",
                "generatedValue": "NOW_ON_CREATE_AND_UPDATE"
              },
              {
                "name": "enabled",
                "type": "Boolean",
                "unique": false,
                "comment": "",
                "nullable": false,
                "modelName": "fs_datasource",
                "defaultValue": true
              }
            ],
            "indexes": []
          },
          {
            "name": "fs_api_definition",
            "type": "ENTITY",
            "fields": [
              {
                "name": "id",
                "type": "ID",
                "unique": true,
                "nullable": true,
                "modelName": "fs_api_definition",
                "generatedValue": "ULID"
              },
              {
                "name": "name",
                "type": "String",
                "length": 255,
                "unique": false,
                "nullable": false,
                "modelName": "fs_api_definition"
              },
              {
                "name": "parentId",
                "type": "String",
                "length": 255,
                "unique": false,
                "nullable": true,
                "modelName": "fs_api_definition"
              },
              {
                "name": "type",
                "type": "Enum",
                "from": "ApiType",
                "multiple": false,
                "unique": false,
                "nullable": false,
                "modelName": "fs_api_definition",
                "defaultValue": "FOLDER"
              },
              {
                "name": "method",
                "type": "String",
                "length": 255,
                "unique": false,
                "nullable": true,
                "modelName": "fs_api_definition"
              },
              {
                "name": "path",
                "type": "String",
                "length": 255,
                "unique": false,
                "nullable": true,
                "modelName": "fs_api_definition"
              },
              {
                "name": "createdAt",
                "type": "DateTime",
                "unique": false,
                "nullable": false,
                "modelName": "fs_api_definition",
                "generatedValue": "NOW_ON_CREATE"
              },
              {
                "name": "updatedAt",
                "type": "DateTime",
                "unique": false,
                "nullable": false,
                "modelName": "fs_api_definition",
                "generatedValue": "NOW_ON_CREATE_AND_UPDATE"
              },
              {
                "name": "meta",
                "type": "JSON",
                "unique": false,
                "nullable": true,
                "modelName": "fs_api_definition"
              },
              {
                "name": "enabled",
                "type": "Boolean",
                "unique": false,
                "comment": "",
                "nullable": false,
                "modelName": "fs_api_definition",
                "defaultValue": true
              }
            ],
            "indexes": []
          },
          {
            "name": "fs_api_log",
            "type": "ENTITY",
            "fields": [
              {
                "name": "id",
                "type": "ID",
                "unique": false,
                "nullable": true,
                "modelName": "fs_api_log",
                "generatedValue": "ULID"
              },
              {
                "name": "level",
                "type": "Enum",
                "from": "LogLevel",
                "multiple": false,
                "unique": false,
                "nullable": false,
                "modelName": "fs_api_log"
              },
              {
                "name": "uri",
                "type": "String",
                "length": "2000",
                "unique": false,
                "nullable": false,
                "modelName": "fs_api_log"
              },
              {
                "name": "data",
                "type": "JSON",
                "unique": false,
                "nullable": false,
                "modelName": "fs_api_log"
              },
              {
                "name": "createdAt",
                "type": "DateTime",
                "unique": false,
                "nullable": false,
                "modelName": "fs_api_log",
                "generatedValue": "NOW_ON_CREATE"
              }
            ],
            "indexes": [
              {
                "name": "IDX_level",
                "modelName": "fs_api_log",
                "fields": [
                  {
                    "fieldName": "level",
                    "direction": "ASC"
                  }
                ],
                "unique": false
              }
            ]
          },
          {
            "name": "fs_identity_provider",
            "type": "ENTITY",
            "fields": [
              {
                "name": "name",
                "type": "ID",
                "unique": false,
                "nullable": true,
                "modelName": "fs_datasource",
                "generatedValue": "STRING_NOT_GENERATED"
              },
              {
                "name": "createdAt",
                "type": "DateTime",
                "unique": false,
                "nullable": false,
                "modelName": "fs_identity_provider",
                "generatedValue": "NOW_ON_CREATE"
              },
              {
                "name": "provider",
                "type": "JSON",
                "unique": false,
                "nullable": true,
                "modelName": "fs_identity_provider"
              },
              {
                "name": "updatedAt",
                "type": "DateTime",
                "unique": false,
                "nullable": false,
                "modelName": "fs_identity_provider",
                "generatedValue": "NOW_ON_CREATE_AND_UPDATE"
              }
            ],
            "indexes": []
          },
          {
            "name": "fs_config",
            "comment": "配置",
            "type": "ENTITY",
            "fields": [
              {
                "name": "id",
                "type": "ID",
                "unique": true,
                "nullable": true,
                "modelName": "fs_config",
                "generatedValue": "ULID"
              },
              {
                "name": "key",
                "comment": "名称",
                "type": "String",
                "length": 255,
                "unique": false,
                "nullable": false,
                "modelName": "fs_config"
              },
              {
                "name": "value",
                "type": "String",
                "unique": false,
                "nullable": true,
                "modelName": "fs_config"
              },
              {
                "name": "createdAt",
                "type": "DateTime",
                "unique": false,
                "nullable": false,
                "modelName": "fs_api_definition",
                "generatedValue": "NOW_ON_CREATE"
              },
              {
                "name": "updatedAt",
                "type": "DateTime",
                "unique": false,
                "nullable": false,
                "modelName": "fs_config",
                "generatedValue": "NOW_ON_CREATE_AND_UPDATE"
              }
            ],
            "indexes": [
              {
                "name": "IDX_key",
                "modelName": "fs_config",
                "fields": [
                  {
                    "fieldName": "key",
                    "direction": "ASC"
                  }
                ],
                "unique": true
              }
            ]
          },
          {
            "type": "ENTITY",
            "name": "Teacher",
            "fields": [
              {
                "name": "id",
                "type": "ID",
                "modelName": "Teacher",
                "unique": false,
                "nullable": true,
                "generatedValue": "BIGINT_NOT_GENERATED"
              },
              {
                "name": "teacherName",
                "type": "String",
                "modelName": "Teacher",
                "unique": false,
                "nullable": true,
                "length": 255
              },
              {
                "name": "subject",
                "type": "String",
                "modelName": "Teacher",
                "unique": false,
                "nullable": true,
                "length": 255
              }
            ],
            "indexes": []
          },
          {
            "type": "ENTITY",
            "name": "Student",
            "fields": [
              {
                "name": "id",
                "type": "ID",
                "modelName": "Student",
                "unique": false,
                "nullable": true,
                "generatedValue": "BIGINT_NOT_GENERATED"
              },
              {
                "name": "studentName",
                "type": "String",
                "modelName": "Student",
                "unique": false,
                "nullable": true,
                "length": 255
              },
              {
                "name": "gender",
                "type": "Enum",
                "from": "UserGender",
                "multiple": false,
                "modelName": "Student",
                "unique": false,
                "nullable": true
              },
              {
                "name": "interest",
                "type": "Enum",
                "from": "user_interest",
                "multiple": true,
                "modelName": "Student",
                "unique": false,
                "nullable": true
              },
              {
                "name": "age",
                "type": "Int",
                "modelName": "Student",
                "unique": false,
                "nullable": true
              },
              {
                "name": "classId",
                "type": "Long",
                "modelName": "Student",
                "unique": false,
                "nullable": true
              },
              {
                "type": "Relation",
                "name": "studentClass",
                "comment": "班级",
                "modelName": "Student",
                "multiple": false,
                "from": "Classes",
                "localField": "classId",
                "foreignField": "id",
                "cascadeDelete": false
              },
              {
                "name": "studentDetail",
                "type": "Relation",
                "modelName": "Student",
                "unique": false,
                "nullable": true,
                "multiple": false,
                "from": "StudentDetail",
                "localField": "id",
                "foreignField": "studentId",
                "cascadeDelete": true
              }
            ],
            "indexes": [
              {
                "modelName": "Student",
                "name": "IDX_studentName",
                "fields": [
                  {
                    "fieldName": "studentName",
                    "direction": "ASC"
                  }
                ],
                "unique": false
              },
              {
                "modelName": "Student",
                "name": "IDX_classId",
                "fields": [
                  {
                    "fieldName": "classId",
                    "direction": "ASC"
                  }
                ],
                "unique": false
              }
            ]
          },
          {
            "type": "ENTITY",
            "name": "StudentDetail",
            "fields": [
              {
                "name": "id",
                "type": "ID",
                "modelName": "StudentDetail",
                "unique": false,
                "nullable": true,
                "generatedValue": "AUTO_INCREMENT"
              },
              {
                "name": "studentId",
                "type": "Long",
                "modelName": "StudentDetail",
                "unique": false,
                "nullable": true
              },
              {
                "type": "Relation",
                "name": "student",
                "comment": "学生",
                "modelName": "StudentDetail",
                "multiple": false,
                "from": "Student",
                "localField": "studentId",
                "foreignField": "id",
                "cascadeDelete": false
              },
              {
                "name": "description",
                "type": "String",
                "largeObjects": true,
                "modelName": "StudentDetail",
                "unique": false,
                "nullable": true
              }
            ],
            "indexes": []
          },
          {
            "type": "ENTITY",
            "name": "Classes",
            "fields": [
              {
                "name": "id",
                "type": "ID",
                "modelName": "Classes",
                "unique": false,
                "nullable": true,
                "generatedValue": "BIGINT_NOT_GENERATED"
              },
              {
                "name": "classCode",
                "type": "String",
                "modelName": "Classes",
                "unique": false,
                "nullable": true,
                "length": 255
              },
              {
                "name": "className",
                "type": "String",
                "modelName": "Classes",
                "unique": false,
                "nullable": true,
                "length": 255
              },
              {
                "type": "Relation",
                "name": "students",
                "modelName": "Classes",
                "unique": false,
                "nullable": true,
                "multiple": true,
                "from": "Student",
                "localField": "id",
                "foreignField": "classId",
                "cascadeDelete": true
              }
            ]
          },
          {
            "type": "ENTITY",
            "name": "Course",
            "fields": [
              {
                "name": "courseNo",
                "type": "ID",
                "modelName": "Course",
                "unique": false,
                "nullable": true,
                "generatedValue": "STRING_NOT_GENERATED"
              },
              {
                "name": "courseName",
                "type": "String",
                "modelName": "Course",
                "unique": false,
                "nullable": true,
                "length": 255
              }
            ],
            "indexes": [
              {
                "name": "IDX_courseNo",
                "modelName": "Course",
                "fields": [
                  {
                    "fieldName": "courseNo",
                    "direction": "ASC"
                  }
                ],
                "unique": false
              }
            ]
          },
          {
            "name": "ApiType",
            "type": "ENUM",
            "elements": [
              "FOLDER",
              "API"
            ],
            "comment": "接口类型"
          },
          {
            "name": "DatasourceType",
            "type": "ENUM",
            "elements": [
              "SYSTEM",
              "USER"
            ],
            "comment": "数据源类型"
          },
          {
            "name": "UserGender",
            "type": "ENUM",
            "elements": [
              "UNKNOWN",
              "MALE",
              "FEMALE"
            ],
            "comment": "性别"
          },
          {
            "name": "user_interest",
            "type": "ENUM",
            "elements": [
              "chang",
              "tiao",
              "rap",
              "daLanQiu"
            ],
            "comment": "兴趣"
          },
          {
            "name": "LogLevel",
            "type": "ENUM",
            "elements": [
              "DEBUG",
              "INFO",
              "WARN",
              "ERROR"
            ],
            "comment": "日志等级"
          },
          {
            "name": "select_log_count",
            "statement": "select count(*) as total from fs_api_log",
            "type": "NATIVE_QUERY"
          }
        ]
      """;
    List<Map<String, Object>> list = new JacksonObjectConverter().parseToMapList(json);
    List<SchemaObject> schemaObjects = new JacksonObjectConverter().convertValueList(list, SchemaObject.class);
    StringBuilder sb = new StringBuilder();
    for (SchemaObject schemaObject : schemaObjects) {
      sb.append(ASTNodeConverter.fromSchemaObject(schemaObject)).append("\n");
    }
    System.out.println(sb);
  }

}
