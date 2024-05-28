package tech.wetech.flexmodel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cjbi
 */
@RequestMapping
@RestController
public class DatasourceController {

  @Autowired
  Session session;

  @GetMapping("/hello")
  public Object hello() {
    return session.find("Student", query -> query);
  }

}
