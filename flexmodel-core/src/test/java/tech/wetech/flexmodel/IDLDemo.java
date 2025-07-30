package tech.wetech.flexmodel;

import tech.wetech.flexmodel.parser.ASTNodeConverter;
import tech.wetech.flexmodel.parser.impl.ModelParser;

import java.io.StringReader;
import java.util.List;

/**
 * IDL解析功能演示
 * 
 * @author cjbi
 */
public class IDLDemo {

    public static void main(String[] args) {
        try {
            // 测试IDL字符串
            String idlString = """
                // 用户模型
                model User {
                  id: String @id @default(uuid()),
                  name: String @length(255),
                  age: Int,
                  email: String @unique,
                  createdAt: DateTime @default(now()),
                }
                
                // 用户状态枚举
                enum UserStatus {
                  ACTIVE,
                  INACTIVE,
                  PENDING
                }
                """;

            System.out.println("=== IDL解析演示 ===");
            System.out.println("原始IDL:");
            System.out.println(idlString);
            System.out.println();

            // 解析IDL
            ModelParser parser = new ModelParser(new StringReader(idlString));
            List<ModelParser.ASTNode> ast = parser.CompilationUnit();
            
            System.out.println("解析结果:");
            for (ModelParser.ASTNode node : ast) {
                System.out.println(node);
            }
            System.out.println();

            // 转换为SchemaObject
            System.out.println("转换为SchemaObject:");
            for (ModelParser.ASTNode node : ast) {
                SchemaObject schemaObject = ASTNodeConverter.toSchemaObject(node);
                if (schemaObject != null) {
                    System.out.println("类型: " + schemaObject.getClass().getSimpleName());
                    System.out.println("名称: " + schemaObject.getName());
                    System.out.println("IDL: " + schemaObject.getIdl());
                    System.out.println();
                }
            }

            System.out.println("=== IDL解析成功 ===");
            
        } catch (Exception e) {
            System.err.println("IDL解析失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 