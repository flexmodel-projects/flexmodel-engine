package tech.wetech.flexmodel;

import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.parser.ASTNodeConverter;
import tech.wetech.flexmodel.parser.impl.ModelParser;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试IDL解析功能
 *
 * @author cjbi
 */
public class IDLParserTest {

    @Test
    void testParseSimpleModel() throws Exception {
        String idlString = """
            model User {
              id: String @id @default(uuid()),
              name: String @length(255),
              age: Int,
              email: String @unique,
              createdAt: DateTime @default(now()),
            }
            """;

        ModelParser parser = new ModelParser(new StringReader(idlString));
        List<ModelParser.ASTNode> ast = parser.CompilationUnit();

        assertNotNull(ast);
        assertEquals(1, ast.size());

        ModelParser.ASTNode node = ast.get(0);
        assertTrue(node instanceof ModelParser.Model);

        ModelParser.Model model = (ModelParser.Model) node;
        assertEquals("User", model.name);
        assertEquals(5, model.fields.size());

        // 验证字段
        var idField = model.fields.stream()
            .filter(f -> "id".equals(f.name))
            .findFirst()
            .orElse(null);
        assertNotNull(idField);
        assertEquals("String", idField.type);
        assertFalse(idField.optional);
        assertEquals(2, idField.annotations.size());
        assertEquals("id", idField.annotations.get(0).name);
    }

    @Test
    void testParseModelWithEnum() throws Exception {
        String idlString = """
            enum UserStatus {
              ACTIVE,
              INACTIVE,
              PENDING
            }

            model User {
              id: String @id @default(uuid()),
              name: String,
              status: UserStatus @default(ACTIVE),
            }
            """;

        ModelParser parser = new ModelParser(new StringReader(idlString));
        List<ModelParser.ASTNode> ast = parser.CompilationUnit();

        assertNotNull(ast);
        assertEquals(2, ast.size());

        // 验证枚举
        ModelParser.ASTNode enumNode = ast.get(0);
        assertTrue(enumNode instanceof ModelParser.Enumeration);

        ModelParser.Enumeration enumeration = (ModelParser.Enumeration) enumNode;
        assertEquals("UserStatus", enumeration.name);
        assertEquals(3, enumeration.elements.size());
        assertTrue(enumeration.elements.contains("ACTIVE"));
        assertTrue(enumeration.elements.contains("INACTIVE"));
        assertTrue(enumeration.elements.contains("PENDING"));

        // 验证模型
        ModelParser.ASTNode modelNode = ast.get(1);
        assertTrue(modelNode instanceof ModelParser.Model);

        ModelParser.Model model = (ModelParser.Model) modelNode;
        assertEquals("User", model.name);
        assertEquals(3, model.fields.size());
    }

    @Test
    void testParseModelWithRelations() throws Exception {
        String idlString = """
            model Post {
              id: String @id @default(uuid()),
              title: String,
              content: String,
              authorId: String,
              author: User @relation(localField: "authorId", foreignField: "id"),
            }

            model User {
              id: String @id @default(uuid()),
              name: String,
              posts: Post[] @relation(localField: "id", foreignField: "authorId"),
            }
            """;

        ModelParser parser = new ModelParser(new StringReader(idlString));
        List<ModelParser.ASTNode> ast = parser.CompilationUnit();

        assertNotNull(ast);
        assertEquals(2, ast.size());

        // 验证Post模型
        ModelParser.Model postModel = (ModelParser.Model) ast.get(0);
        assertEquals("Post", postModel.name);
        assertEquals(5, postModel.fields.size());

        // 验证关系字段
        var authorField = postModel.fields.stream()
            .filter(f -> "author".equals(f.name))
            .findFirst()
            .orElse(null);
        assertNotNull(authorField);
        assertEquals("User", authorField.type);
        assertEquals(1, authorField.annotations.size());
        assertEquals("relation", authorField.annotations.get(0).name);

        var relationParams = authorField.annotations.get(0).parameters;
        assertEquals("authorId", relationParams.get("localField"));
        assertEquals("id", relationParams.get("foreignField"));
    }

    @Test
    void testASTNodeConverter() throws Exception {
        String idlString = """
            model User {
              id: String @id @default(uuid()),
              name: String @length(255),
              age: Int,
            }
            """;

        ModelParser parser = new ModelParser(new StringReader(idlString));
        List<ModelParser.ASTNode> ast = parser.CompilationUnit();

        // 转换为SchemaObject
        ModelParser.ASTNode node = ast.get(0);
        SchemaObject schemaObject = ASTNodeConverter.toSchemaObject(node);

        assertNotNull(schemaObject);
        assertTrue(schemaObject instanceof EntityDefinition);

        EntityDefinition entity = (EntityDefinition) schemaObject;
        assertEquals("User", entity.getName());
        assertEquals(3, entity.getFields().size());

        // 验证字段
        var idField = entity.getField("id");
        assertNotNull(idField);
        assertTrue(idField.isIdentity());

        var nameField = entity.getField("name");
        assertNotNull(nameField);
        assertEquals("String", nameField.getType());
    }

    @Test
    void testParseInvalidSyntax() {
        String invalidIdl = """
            model User {
              id: String @id @default(uuid()),
              name: String,
              // 缺少闭合大括号
            """;

        assertThrows(Exception.class, () -> {
            ModelParser parser = new ModelParser(new StringReader(invalidIdl));
            parser.CompilationUnit();
        });
    }

    @Test
    void testParseEmptyString() throws Exception {
        ModelParser parser = new ModelParser(new StringReader(""));
        List<ModelParser.ASTNode> ast = parser.CompilationUnit();

        assertNotNull(ast);
        assertTrue(ast.isEmpty());
    }
}
