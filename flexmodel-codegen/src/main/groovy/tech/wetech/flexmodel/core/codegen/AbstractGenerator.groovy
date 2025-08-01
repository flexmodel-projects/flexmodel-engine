package tech.wetech.flexmodel.core.codegen

import groovy.io.GroovyPrintWriter
import groovy.util.logging.Log
import org.apache.groovy.io.StringBuilderWriter
import tech.wetech.flexmodel.codegen.GenerationContext
import tech.wetech.flexmodel.codegen.Generator

/**
 * @author cjbi
 */
@Log
abstract class AbstractGenerator implements Generator {

  /**
   * 子类需实现该方法，返回输出文件路径
   */
  String getTargetFile(GenerationContext context, String targetDirectory) {
    return null
  }

  @Override
  List<File> generate(GenerationContext context, String dir) {
    List<File> files = []
    // 生成并写入主内容、模型和枚举
    files += processAndWrite(context, dir, this.&write)
    while (context.nextModel()) {
      files += processAndWrite(context, dir, this.&writeModel)
    }
    while (context.nextEnum()) {
      files += processAndWrite(context, dir, this.&writeEnum)
    }
    return files
  }

  @Override
  List<String> generate(GenerationContext context) {
    List<String> outputs = []
    outputs << collect(context, this.&write)
    while (context.nextModel()) {
      outputs << collect(context, this.&writeModel)
    }
    while (context.nextEnum()) {
      outputs << collect(context, this.&writeEnum)
    }
    // 过滤空字符串
    return outputs.findAll { it }
  }

  /**
   * 将生成内容写入文件并返回文件列表
   */
  private List<File> processAndWrite(GenerationContext context, String dir, Closure writerFunc) {
    String content = collect(context, writerFunc)
    if (!content) {
      return []
    }
    String targetPath = getTargetFile(context, dir)
    if (!targetPath) {
      return []
    }
    File file = new File(targetPath)
    file.parentFile?.mkdirs()
    file.write(content)
    return [file]
  }

  /**
   * 执行写入逻辑，将输出拼接为字符串并返回
   */
  private String collect(GenerationContext context, Closure writerFunc) {
    def writer = new StringBuilderWriter()
    writerFunc.call(new GroovyPrintWriter(writer), context)
    return writer.toString()
  }

  /**
   * 子类可覆盖：用于写入通用内容
   */
  void write(PrintWriter out, GenerationContext context) {
    // 默认空实现
  }

  /**
   * 子类可覆盖：用于写入模型内容
   */
  void writeModel(PrintWriter out, GenerationContext context) {
    // 默认空实现
  }

  /**
   * 子类可覆盖：用于写入枚举内容
   */
  void writeEnum(PrintWriter out, GenerationContext context) {
    // 默认空实现
  }
}
