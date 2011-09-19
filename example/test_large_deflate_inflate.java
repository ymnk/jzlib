/* -*-mode:java; c-basic-offset:2; -*- */
import java.io.*;
import com.jcraft.jzlib.*;

// Test deflate() with large buffers and dynamic change of compression level
class test_large_deflate_inflate{

  static final byte[] hello="hello, hello! ".getBytes();
  static{
    hello[hello.length-1]=0;
  }

  public static void main(String[] arg){
    int err;
    int comprLen=40000;
    int uncomprLen=comprLen;
    byte[] compr=new byte[comprLen];
    byte[] uncompr=new byte[uncomprLen];

    Deflater deflater = null;
    try{
      deflater = new Deflater(JZlib.Z_BEST_SPEED);
    }
    catch(GZIPException e){
    }

    deflater.setInput(uncompr);
    deflater.setOutput(compr);

    // At this point, uncompr is still mostly zeroes, so it should compress
    // very well:

    err=deflater.deflate(JZlib.Z_NO_FLUSH);
    CHECK_ERR(deflater, err, "deflate");
    if(deflater.avail_in!=0){
      System.out.println("deflate not greedy");
      System.exit(1);
    }

    // Feed in already compressed data and switch to no compression:
    deflater.params(JZlib.Z_NO_COMPRESSION, JZlib.Z_DEFAULT_STRATEGY);
    deflater.setInput(compr);
    deflater.avail_in=comprLen/2;
    err=deflater.deflate(JZlib.Z_NO_FLUSH);
    CHECK_ERR(deflater, err, "deflate");

    // Switch back to compressing mode:
    deflater.params(JZlib.Z_BEST_COMPRESSION, JZlib.Z_FILTERED);
    deflater.setInput(uncompr);
    err=deflater.deflate(JZlib.Z_NO_FLUSH);
    CHECK_ERR(deflater, err, "deflate");

    err=deflater.deflate(JZlib.Z_FINISH);
    if(err!=JZlib.Z_STREAM_END){
      System.out.println("deflate should report Z_STREAM_END");
      System.exit(1);
    }
    err=deflater.end();
    CHECK_ERR(deflater, err, "deflateEnd");

    Inflater inflater = new Inflater();

    inflater.setInput(compr);

    while(true){
      inflater.setOutput(uncompr);
      err=inflater.inflate(JZlib.Z_NO_FLUSH);
      if(err==JZlib.Z_STREAM_END) break;
      CHECK_ERR(inflater, err, "inflate large");
    }

    err=inflater.end();
    CHECK_ERR(inflater, err, "inflateEnd");

    if (inflater.getTotalOut() != 2*uncomprLen + comprLen/2) {
       System.out.println("bad large inflate: "+inflater.total_out);
       System.exit(1);
    }
    else {
      System.out.println("large_inflate(): OK");
    }
  }

  static void CHECK_ERR(ZStream z, int err, String msg) {
    if(err!=JZlib.Z_OK){
      if(z.msg!=null) System.out.print(z.msg+" "); 
      System.out.println(msg+" error: "+err); 

      System.exit(1);
    }
  }
}
