/* -*-mode:java; c-basic-offset:2; -*- */
import java.io.*;
import com.jcraft.jzlib.*;

public class test_stream_deflate_inflate{
  public static void main(String[] args){
    try{
      String hello = "Hello World!";

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ZOutputStream zOut = new ZOutputStream(out, JZlib.Z_BEST_COMPRESSION);
      ObjectOutputStream objOut = new ObjectOutputStream(zOut);
      objOut.writeObject(hello);
      zOut.close();

      ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
      ZInputStream zIn = new ZInputStream(in);
      ObjectInputStream objIn = new ObjectInputStream(zIn);
      System.out.println(objIn.readObject());
    }
    catch (Exception e){
      e.printStackTrace();
    }
  }
}
