package tech.wetech.flexmodel;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import tech.wetech.flexmodel.quarkus.session.SessionManaged;

@Path("/hello")
@SessionManaged
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
      return "Hello from Quarkus REST";
    }
}
