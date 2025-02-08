package tech.wetech.flexmodel.codegen;

import java.io.*;
import java.util.Base64;

/**
 * @author cjbi
 */
public class ObjectUtils {

  public static String serialize(Object obj) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(obj);
    oos.flush();
    return Base64.getEncoder().encodeToString(baos.toByteArray());
  }

  public static Object deserialize(String str) throws IOException, ClassNotFoundException {
    byte[] data = Base64.getDecoder().decode(str);
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
    return ois.readObject();
  }

}
