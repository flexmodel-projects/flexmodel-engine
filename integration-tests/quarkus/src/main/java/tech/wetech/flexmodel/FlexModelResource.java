package tech.wetech.flexmodel;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

/**
 * @author cjbi
 */
@Path("/flexmodel")
public class FlexModelResource {

  @Inject
  SessionFactory sessionFactory;

  @GET
  @Path("/list")
  public Object list() {
    Session session = sessionFactory.createSession("default");
    session.close();
    System.out.println("1111111111111");
    return "hello world";
  }

}
