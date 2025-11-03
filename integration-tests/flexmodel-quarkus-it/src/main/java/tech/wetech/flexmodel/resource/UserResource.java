package tech.wetech.flexmodel.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.entity.FsUser;
import tech.wetech.flexmodel.service.UserService;

import java.util.List;
import java.util.Map;

/**
 * 用户资源示例
 * 展示如何使用Session管理系统
 *
 * @author cjbi
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

  private static final Logger log = LoggerFactory.getLogger(UserResource.class);

  @Inject
  UserService userService;

  /**
   * 获取所有用户
   */
  @GET
  public Uni<List<Map<String, Object>>> getAllUsers() {
    try {
      return userService.getAllUsers();
    } catch (Exception e) {
      log.error("Error getting users", e);
      return Uni.createFrom().failure(e);
    }
  }

  /**
   * 根据ID获取用户
   */
  @GET
  @Path("/{id}")
  public Response getUserById(@PathParam("id") String id) {
    try {
      Map<String, Object> user = userService.getUserById(id);

      if (user == null) {
        return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
      }

      return Response.ok(user).build();
    } catch (Exception e) {
      log.error("Error getting user by id: {}", id, e);
      return Response.serverError().entity("Error getting user: " + e.getMessage()).build();
    }
  }

  /**
   * 创建新用户
   */
  @POST
  public Response createUser(FsUser userData) {
    try {
      Map<String, Object> result = userService.createUser(userData);
      return Response.status(Response.Status.CREATED).entity(result).build();
    } catch (Exception e) {
      log.error("Error creating user", e);
      return Response.serverError().entity("Error creating user: " + e.getMessage()).build();
    }
  }

  /**
   * 更新用户
   */
  @PUT
  @Path("/{id}")
  public Response updateUser(@PathParam("id") String id, FsUser userData) {
    try {
      Map<String, Object> result = userService.updateUser(id, userData);
      return Response.ok(result).build();
    } catch (Exception e) {
      log.error("Error updating user with id: {}", id, e);
      return Response.serverError().entity("Error updating user: " + e.getMessage()).build();
    }
  }

  /**
   * 删除用户
   */
  @DELETE
  @Path("/{id}")
  public Response deleteUser(@PathParam("id") String id) {
    try {
      Map<String, Object> result = userService.deleteUser(id);
      return Response.ok(result).build();
    } catch (Exception e) {
      log.error("Error deleting user with id: {}", id, e);
      return Response.serverError().entity("Error deleting user: " + e.getMessage()).build();
    }
  }
}
