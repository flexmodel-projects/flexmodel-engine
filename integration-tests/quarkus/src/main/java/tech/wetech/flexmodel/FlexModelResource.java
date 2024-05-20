package tech.wetech.flexmodel;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

/**
 * @author cjbi
 */
@Path("/flexmodel")
public class FlexModelResource {

  @GET
  @Path("/list")
  public Object list() {
    System.out.println("1111111111111");
    return "hello world";
  }

}
