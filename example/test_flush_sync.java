/* -*-mode:java; c-basic-offset:2; -*- */
import java.io.*;
import com.jcraft.jzlib.*;

// Test deflate() with full flush
class test_flush_sync{

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
    int len = hello.length;

    Deflater deflater = new Deflater();

    err=deflater.deflateInit(JZlib.Z_DEFAULT_COMPRESSION);
    CHECK_ERR(deflater, err, "deflate");

    deflater.setInput(hello);
    deflater.setOutput(compr);

    deflater.avail_in = 3;

    err = deflater.deflate(JZlib.Z_FULL_FLUSH);
    CHECK_ERR(deflater, err, "deflate");

    compr[3]++;              // force an error in first compressed block
    deflater.avail_in=len-3;

    err = deflater.deflate(JZlib.Z_FINISH);
    if(err!=JZlib.Z_STREAM_END){
      CHECK_ERR(deflater, err, "deflate");
    }
    err = deflater.end();
    CHECK_ERR(deflater, err, "deflateEnd");
    comprLen=(int)(deflater.total_out);

    Inflater inflater = new Inflater();

    inflater.setInput(compr);
    inflater.avail_in=2;

    inflater.setOutput(uncompr);
    
    err=inflater.inflate(JZlib.Z_NO_FLUSH);
    CHECK_ERR(inflater, err, "inflate");

    inflater.avail_in = comprLen-2;

    err=inflater.sync();
    CHECK_ERR(inflater, err, "inflateSync");

    err=inflater.inflate(JZlib.Z_FINISH);
    if (err != JZlib.Z_DATA_ERROR) {
      System.out.println("inflate should report DATA_ERROR");
        /* Because of incorrect adler32 */
      System.exit(1);
    }

    err=inflater.end();
    CHECK_ERR(inflater, err, "inflateEnd");

    int j=0;
    for(;j<uncompr.length; j++) if(uncompr[j]==0) break;

    System.out.println("after inflateSync(): hel"+new String(uncompr, 0, j));
  }

  static void CHECK_ERR(ZStream z, int err, String msg) {
    if(err!=JZlib.Z_OK){
      if(z.msg!=null) System.out.print(z.msg+" "); 
      System.out.println(msg+" error: "+err); 

      System.exit(1);
    }
  }
}
