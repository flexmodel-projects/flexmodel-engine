package tech.wetech.flexmodel.parser;

import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.parser.impl.ModelParser;
import tech.wetech.flexmodel.parser.impl.ParseException;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * @author cjbi
 */
public class ModelParserTest {

  public static final String SDL = """
      // 班级
        model Classes {
          id: ID @generatedValue("BIGINT_NOT_GENERATED"),
          classCode: String @unique @length(255),
          className? : String  @default("A班级"),
          students: Student[] @relation(localField: "id", foreignField: "classId", cascadeDelete: true),
        }

        // 学生
        model Student {
          id: ID @generatedValue("BIGINT_NOT_GENERATED"),
          studentName: String @length(255), // 学生名称
          gender: UserGender,
          interest: user_interest[],
          age: Int,
          classId: Long,
          studentClass: Classes @relation(localField: "classId", foreignField: "id"),
          studentDetail: StudentDetail @relation(localField: "id", foreignField: "studentId", cascadeDelete: true),
          @index(name:"IDX_studentName", unique: false, fields: [classId, studentName(sort: "desc")]),
        }

        // 学生详情
        model StudentDetail {
          id: ID @generatedValue("BIGINT_NOT_GENERATED"),
          studentId: Long @unique,
          // 详细信息
          description: String @lob,
          student: Student @relation(localField: "studentId", foreignField: "id"),
        }

        // 用户性别
        enum  UserGender {
          UNKNOWN,
          MALE,
          FEMALE
        }

        // 用户爱好
        enum user_interest {
          chang,
          tiao,
          rap,
          daLanQiu
        }
    """;

  @Test
  void test() throws ParseException {
    // 从标准输入解析，解析结果为 AST 节点列表
    ModelParser parser = new ModelParser(new ByteArrayInputStream(SDL.getBytes()));
    List ast = parser.CompilationUnit();
    System.out.println("Parsing successful.");
    // 遍历打印 AST
    for (Object node : ast) {
      System.out.println(node);
    }
  }

}
