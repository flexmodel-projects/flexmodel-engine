package tech.wetech.flexmodel.codegen

import groovy.text.SimpleTemplateEngine
import org.junit.jupiter.api.Test

/**
 * @author cjbi
 */
class GroovyTest {


  @Test
  void testTemplate(){
    def templateText = '''
      <html>
      <head><title>欢迎</title></head>
      <body>
      <h1>你好, ${name}!</h1>
      <p>你已经 ${age} 岁了。</p>
      <p>当${age}大于18岁时，你是成年人。</p>
      <%
      if (age > 18) {
          out << "<p>你是成年人。</p>"
      } else {
          out << "<p>你是未成年人。</p>"
      }
      %>
      </body>
      </html>
        '''
    def engine = new SimpleTemplateEngine()
    def string = engine.createTemplate(templateText).make(name: "James", age: 20).toString()
    println string
  }

}
