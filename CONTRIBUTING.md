# 贡献指南

感谢您对FlexModel项目的关注！我们欢迎所有形式的贡献，包括但不限于：

- 🐛 报告Bug
- 💡 提出新功能建议
- 📝 改进文档
- 🔧 提交代码修复
- 🧪 编写测试
- 🌍 翻译文档

## 📋 目录

- [开发环境设置](#开发环境设置)
- [代码规范](#代码规范)
- [提交规范](#提交规范)
- [Pull Request流程](#pull-request流程)
- [发布流程](#发布流程)
- [问题报告](#问题报告)

## 🛠️ 开发环境设置

### 环境要求

- **Java**: 21+
- **Maven**: 3.6+
- **IDE**: IntelliJ IDEA (推荐) 或 Eclipse
- **Git**: 2.0+

### 本地开发设置

1. **Fork项目**
   ```bash
   # 在GitHub上fork项目
   # 然后克隆你的fork
   git clone https://github.com/YOUR_USERNAME/flexmodel-engine.git
   cd flexmodel-engine
   ```

2. **添加上游仓库**
   ```bash
   git remote add upstream https://github.com/flexmodel-projects/flexmodel-engine.git
   ```

3. **构建项目**
   ```bash
   mvn clean install
   ```

4. **运行测试**
   ```bash
   # 运行所有测试
   mvn test
   
   # 运行特定模块测试
   mvn test -pl flexmodel-core
   
   # 运行集成测试
   mvn test -pl integration-tests
   ```

### IDE配置

#### IntelliJ IDEA

1. **导入项目**
   - 选择 `File` → `Open`
   - 选择项目根目录的 `pom.xml`
   - 选择 `Open as Project`

2. **配置代码风格**
   - 安装 `CheckStyle-IDEA` 插件
   - 导入项目中的 `checkstyle.xml` 配置

3. **配置Maven**
   - 确保Maven设置正确
   - 配置Maven JDK为Java 21

#### Eclipse

1. **导入项目**
   - `File` → `Import` → `Maven` → `Existing Maven Projects`
   - 选择项目根目录

2. **配置代码风格**
   - 安装 `Checkstyle` 插件
   - 导入项目中的 `checkstyle.xml` 配置

## 📝 代码规范

### Java代码规范

#### 1. 命名规范

```java
// 类名：PascalCase
public class UserService {
    // 常量：UPPER_SNAKE_CASE
    public static final String DEFAULT_USER_NAME = "anonymous";
    
    // 字段名：camelCase
    private String userName;
    
    // 方法名：camelCase
    public void createUser(String name) {
        // 局部变量：camelCase
        String formattedName = formatName(name);
    }
}
```

#### 2. 注释规范

```java
/**
 * 用户服务类，提供用户相关的业务操作
 * 
 * @author cjbi
 * @since 1.0.0
 */
public class UserService {
    
    /**
     * 创建新用户
     * 
     * @param name 用户名称，不能为空
     * @param email 用户邮箱，必须符合邮箱格式
     * @return 创建的用户对象
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    public User createUser(String name, String email) {
        // 参数验证
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        
        // 业务逻辑
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        
        return userRepository.save(user);
    }
}
```

#### 3. 异常处理

```java
public class UserService {
    
    public User findUserById(Long id) {
        try {
            return userRepository.findById(id);
        } catch (DataAccessException e) {
            log.error("查询用户失败，用户ID: {}", id, e);
            throw new UserNotFoundException("用户不存在: " + id, e);
        }
    }
}
```

#### 4. 日志规范

```java
public class UserService {
    
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    public void processUser(User user) {
        log.debug("开始处理用户: {}", user.getId());
        
        try {
            // 处理逻辑
            log.info("用户处理成功: {}", user.getId());
        } catch (Exception e) {
            log.error("用户处理失败: {}", user.getId(), e);
            throw e;
        }
    }
}
```

### 测试规范

#### 1. 单元测试

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    @DisplayName("创建用户成功")
    void shouldCreateUserSuccessfully() {
        // Given
        String name = "John Doe";
        String email = "john@example.com";
        User expectedUser = new User();
        expectedUser.setName(name);
        expectedUser.setEmail(email);
        
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);
        
        // When
        User result = userService.createUser(name, email);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getEmail()).isEqualTo(email);
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    @DisplayName("创建用户时用户名为空应抛出异常")
    void shouldThrowExceptionWhenNameIsEmpty() {
        // Given
        String name = "";
        String email = "john@example.com";
        
        // When & Then
        assertThatThrownBy(() -> userService.createUser(name, email))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("用户名不能为空");
    }
}
```

#### 2. 集成测试

```java
@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class UserServiceIntegrationTest {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    @DisplayName("集成测试：创建并查询用户")
    void shouldCreateAndFindUser() {
        // Given
        String name = "Jane Doe";
        String email = "jane@example.com";
        
        // When
        User createdUser = userService.createUser(name, email);
        User foundUser = userService.findUserById(createdUser.getId());
        
        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getName()).isEqualTo(name);
        assertThat(foundUser.getEmail()).isEqualTo(email);
    }
}
```

## 📋 提交规范

### 提交消息格式

我们使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

#### 类型说明

- **feat**: 新功能
- **fix**: 修复Bug
- **docs**: 文档更新
- **style**: 代码格式调整（不影响功能）
- **refactor**: 代码重构
- **perf**: 性能优化
- **test**: 测试相关
- **chore**: 构建过程或辅助工具的变动

#### 示例

```bash
# 新功能
git commit -m "feat: 添加用户管理功能"

# 修复Bug
git commit -m "fix: 修复用户查询时的空指针异常"

# 文档更新
git commit -m "docs: 更新README文档"

# 代码重构
git commit -m "refactor: 重构用户服务类"

# 性能优化
git commit -m "perf: 优化用户查询性能"

# 测试相关
git commit -m "test: 添加用户服务单元测试"

# 构建相关
git commit -m "chore: 更新Maven依赖版本"
```

### 提交前检查

在提交代码前，请确保：

1. **代码编译通过**
   ```bash
   mvn clean compile
   ```

2. **测试通过**
   ```bash
   mvn test
   ```

3. **代码风格检查**
   ```bash
   mvn checkstyle:check
   ```

4. **静态代码分析**
   ```bash
   mvn spotbugs:check
   ```

## 🔄 Pull Request流程

### 1. 创建分支

```bash
# 确保本地代码是最新的
git fetch upstream
git checkout main
git merge upstream/main

# 创建功能分支
git checkout -b feature/user-management
```

### 2. 开发功能

在分支上进行开发，遵循代码规范。

### 3. 提交代码

```bash
# 添加修改的文件
git add .

# 提交代码
git commit -m "feat: 添加用户管理功能"

# 推送到远程仓库
git push origin feature/user-management
```

### 4. 创建Pull Request

1. 在GitHub上创建Pull Request
2. 填写PR模板
3. 添加相关标签
4. 请求代码审查

### 5. PR模板

```markdown
## 描述
简要描述这个PR的变更内容

## 类型
- [ ] Bug修复
- [ ] 新功能
- [ ] 文档更新
- [ ] 代码重构
- [ ] 性能优化
- [ ] 测试相关
- [ ] 构建相关

## 相关Issue
Closes #123

## 测试
- [ ] 单元测试通过
- [ ] 集成测试通过
- [ ] 手动测试完成

## 检查清单
- [ ] 代码遵循项目规范
- [ ] 添加了必要的测试
- [ ] 更新了相关文档
- [ ] 提交消息符合规范

## 截图（如果适用）
```

## 🚀 发布流程

### 版本号规范

我们使用 [语义化版本](https://semver.org/) 规范：

- **主版本号**: 不兼容的API修改
- **次版本号**: 向下兼容的功能性新增
- **修订号**: 向下兼容的问题修正

### 发布步骤

1. **准备发布分支**
   ```bash
   git checkout -b release/v1.0.0
   ```

2. **更新版本号**
   ```bash
   # 更新pom.xml中的版本号
   mvn versions:set -DnewVersion=1.0.0
   ```

3. **更新文档**
   - 更新CHANGELOG.md
   - 更新版本说明

4. **创建发布标签**
   ```bash
   git tag -a v1.0.0 -m "Release version 1.0.0"
   git push origin v1.0.0
   ```

5. **合并到主分支**
   ```bash
   git checkout main
   git merge release/v1.0.0
   git push origin main
   ```

## 🐛 问题报告

### Bug报告模板

```markdown
## Bug描述
简要描述Bug的内容

## 重现步骤
1. 步骤1
2. 步骤2
3. 步骤3

## 期望行为
描述期望的正确行为

## 实际行为
描述实际发生的错误行为

## 环境信息
- 操作系统: 
- Java版本: 
- FlexModel版本: 
- 数据库版本: 

## 错误日志
```
错误日志内容
```

## 其他信息
任何其他相关信息
```

### 功能请求模板

```markdown
## 功能描述
详细描述新功能的需求

## 使用场景
描述在什么情况下需要这个功能

## 实现建议
如果有实现建议，请提供

## 其他信息
任何其他相关信息
```

## 🤝 社区行为准则

### 我们的承诺

为了营造一个开放和友好的环境，我们承诺：

- 尊重所有贡献者
- 欢迎不同观点和经验
- 优雅地接受建设性批评
- 专注于对社区最有利的事情
- 对其他社区成员表现出同理心

### 我们的标准

不可接受的行为包括：

- 使用性暗示的语言或图像
- 恶意攻击、侮辱/贬损性评论
- 骚扰或跟踪
- 发布他人的私人信息
- 其他可能被认为不适当的行为

## 📞 联系我们

如果您有任何问题或建议，请通过以下方式联系我们：

- 📧 邮件: support@flexmodel.io
- 💬 讨论区: [GitHub Discussions](https://github.com/flexmodel-projects/flexmodel-engine/discussions)
- 🐛 问题报告: [GitHub Issues](https://github.com/flexmodel-projects/flexmodel-engine/issues)

## 🙏 致谢

感谢所有为FlexModel项目做出贡献的开发者和用户！

---

**让我们一起让FlexModel变得更好！** 🚀