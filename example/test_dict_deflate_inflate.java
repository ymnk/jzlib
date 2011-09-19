/* -*-mode:java; c-basic-offset:2; -*- */
import java.io.*;
import com.jcraft.jzlib.*;

// Test deflate() with preset dictionary
class test_dict_deflate_inflate{

  static final byte[] dictionary = "hello ".getBytes();
  static final byte[] hello="hello, hello! ".getBytes();
  static{
    dictionary[dictionary.length-1]=0;
    hello[hello.length-1]=0;
  }

  public static void main(String[] arg){
    int err;
    int comprLen=40000;
    int uncomprLen=comprLen;
    byte[] uncompr=new byte[uncomprLen];
    byte[] compr=new byte[comprLen];
    long dictId;

    Deflater deflater = null;
    try {
      deflater = new Deflater(JZlib.Z_BEST_COMPRESSION);
    }
    catch(GZIPException e){
      // never happen, because 'JZlib.Z_BEST_COMPRESSION' is valid.
    }

    err = deflater.setDictionary(dictionary, dictionary.length);
    CHECK_ERR(deflater, err, "deflateSetDictionary");

    dictId=deflater.getAdler();

    deflater.setInput(hello);
    deflater.setOutput(compr);

    err = deflater.deflate(JZlib.Z_FINISH);
    if (err!=JZlib.Z_STREAM_END) {
        System.out.println("deflate should report Z_STREAM_END");
	System.exit(1);
    }
    err = deflater.end();
    CHECK_ERR(deflater, err, "deflateEnd");

    Inflater inflater = new Inflater();

    inflater.setInput(compr);
    inflater.setOutput(uncompr);

    while(true){
      err=inflater.inflate(JZlib.Z_NO_FLUSH);
      if(err==JZlib.Z_STREAM_END){
	  break;
      }
      if(err==JZlib.Z_NEED_DICT){
        if((int)inflater.getAdler() != (int)dictId) {
	    System.out.println("unexpected dictionary");
            System.exit(1);
	}
	err=inflater.setDictionary(dictionary, dictionary.length);
      }
      CHECK_ERR(inflater, err, "inflate with dict");
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
