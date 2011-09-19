/* -*-mode:java; c-basic-offset:2; -*- */
import java.io.*;
import com.jcraft.jzlib.*;

// Test deflate() with small buffers

class test_gzip_inflate{

  static final byte[] hello="hello".getBytes();

  static final byte[] data={ (byte)0x1f, (byte)0x8b, (byte)0x08, (byte)0x00, (byte)0x1a, (byte)0x96, (byte)0xe0, (byte)0x4c, (byte)0x00, (byte)0x03, (byte)0xcb, (byte)0x48, (byte)0xcd, (byte)0xc9, (byte)0xc9, (byte)0x07, (byte)0x00, (byte)0x86, (byte)0xa6, (byte)0x10, (byte)0x36, (byte)0x05, (byte)0x00, (byte)0x00, (byte)0x00 };

  public static void main(String[] arg){
    int err;

    int comprLen=40000;
    int uncomprLen=comprLen;
    byte[] compr=new byte[comprLen];
    byte[] uncompr=new byte[uncomprLen];

    Inflater inflater = null;

    try{
      inflater = new Inflater(JZlib.DEF_WBITS + 32);
    }
    catch(GZIPException e){
      // never happen, because 'JZlib.DEF_WBITS + 32' is valid.
    }

    inflater.setInput(data);
    inflater.setOutput(uncompr);

    while(inflater.total_out<uncomprLen &&
	  inflater.total_in<comprLen) {
      inflater.avail_in=inflater.avail_out=1; // force small buffers
      err=inflater.inflate(JZlib.Z_NO_FLUSH);
      if(err==JZlib.Z_STREAM_END) break;
      CHECK_ERR(inflater, err, "inflate");
    }

    err=inflater.end();
    CHECK_ERR(inflater, err, "inflateEnd");

    int i=0;
    for(;i<hello.length; i++) if(hello[i]==0) break;
    int j=0;
    for(;j<uncompr.length; j++) if(uncompr[j]==0) break;

    if(i==j){
      for(i=0; i<j; i++) if(hello[i]!=uncompr[i]) break;
      if(i==j){
	System.out.println("inflate(): "+new String(uncompr, 0, j));
	return;
      }
    }
    else{
      System.out.println("bad inflate");
    }
  }

  static void CHECK_ERR(Inflater z, int err, String msg) {
    if(err!=JZlib.Z_OK){
      if(z.msg!=null) System.out.print(z.msg+" "); 
      System.out.println(msg+" error: "+err); 
      System.exit(1);
    }
  }
}
