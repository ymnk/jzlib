/* -*-mode:java; c-basic-offset:2; -*- */
/*
Copyright (c) 2011 ymnk, JCraft,Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright 
     notice, this list of conditions and the following disclaimer in 
     the documentation and/or other materials provided with the distribution.

  3. The names of the authors may not be used to endorse or promote products
     derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * This program is based on zlib-1.1.3, so all credit should go authors
 * Jean-loup Gailly(jloup@gzip.org) and Mark Adler(madler@alumni.caltech.edu)
 * and contributors of zlib.
 */

package com.jcraft.jzlib;

import java.io.UnsupportedEncodingException;
import java.util.zip.Deflater;

/**
 * @see "http://www.ietf.org/rfc/rfc1952.txt"
 * @see "http://www.gnu.org/software/gzip/"
 */
public class GZIPHeader implements Cloneable {
  boolean text = false;
  private final boolean fhcrc = false;
  long time;
  int xflags;
  int os = 255;
  byte[] extra;
  byte[] name;
  byte[] comment;
  int hcrc;
  long crc;
  boolean done = false;
  long mtime = 0;

  public void setModifiedTime(long mtime) {
    this.mtime = mtime;
  }

  public long getModifiedTime() {
    return mtime;
  }

  public void setOS(int os) {
    if((0<=os && os <=13) || os==255){
      this.os=os;
    } else {
        throw new IllegalArgumentException("os: "+os);
    }
  }

  public int getOS(){
    return os;
  }

  public void setName(String name) {
    try{
      this.name=name.getBytes("ISO-8859-1");
    }
    catch(UnsupportedEncodingException e){
      throw new IllegalArgumentException("name must be in ISO-8859-1 "+name);
    }
  }

  public String getName(){
    if(name==null) return "";
    try {
        return new String(name, "ISO-8859-1");
    } catch (UnsupportedEncodingException e) {
        throw new InternalError(e.toString());
    }
  }

  public void setComment(String comment) {
    try{
      this.comment=comment.getBytes("ISO-8859-1");
    }
    catch(UnsupportedEncodingException e){
      throw new IllegalArgumentException("comment must be in ISO-8859-1 "+name);
    }
  }

  public String getComment(){
    if(comment==null) return "";
    try {
        return new String(comment, "ISO-8859-1");
    } catch (UnsupportedEncodingException e) {
        throw new InternalError(e.toString());
    }
  }

  public void setCRC(long crc){
    this.crc = crc;
  }

  public long getCRC(){
    return crc;
  }

  void put(Deflate d){
    int flag = 0;
    if(text){
      flag |= 1;     // FTEXT: Extra text
    }
    if(fhcrc){
      flag |= 2;     // FHCRC: Header CRC16 -- Do not set! gzip considers this flag as continuation of multi-part gzip file!
    }
    if(extra!=null){
      flag |= 4;     // FEXTRA: Extra field
    }
    if(name!=null){
      flag |= 8;    // FNAME: File name
    }
    if(comment!=null){
      flag |= 16;   // FCOMMENT: File comment
    }
    // flag bit 5: rfc: reserved; gzip: file is encrypted
    // flag bits 6 & 7 are reserved
    int xfl = 0;
    if(d.level == JZlib.Z_BEST_SPEED || d.level == JZlib.Z_NO_COMPRESSION){
      xfl |= 4;
    }
    else if (d.level == JZlib.Z_BEST_COMPRESSION){
      xfl |= 2;
    }

    d.put_short((short)0x8b1f);          // ID1 ID2
    d.put_byte((byte)Deflater.DEFLATED); // CM(Compression Method)
    d.put_byte((byte)flag);
    d.put_byte((byte)mtime);
    d.put_byte((byte)(mtime>>8));
    d.put_byte((byte)(mtime>>16));
    d.put_byte((byte)(mtime>>24));
    d.put_byte((byte)xfl);
    d.put_byte((byte)os);

    if(extra!=null){
      d.put_byte((byte)extra.length);
      d.put_byte((byte)(extra.length>>8));
      d.put_byte(extra, 0, extra.length);
    }

    if(name!=null){
      d.put_byte(name, 0, name.length);
      d.put_byte((byte)0);
    }

    if(comment!=null){
      d.put_byte(comment, 0, comment.length);
      d.put_byte((byte)0);
    }
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    GZIPHeader gheader = (GZIPHeader)super.clone();
    byte[] tmp;
    if(gheader.extra!=null){
      tmp=new byte[gheader.extra.length];
      System.arraycopy(gheader.extra, 0, tmp, 0, tmp.length);
      gheader.extra = tmp;
    }

    if(gheader.name!=null){
      tmp=new byte[gheader.name.length];
      System.arraycopy(gheader.name, 0, tmp, 0, tmp.length);
      gheader.name = tmp;
    }

    if(gheader.comment!=null){
      tmp=new byte[gheader.comment.length];
      System.arraycopy(gheader.comment, 0, tmp, 0, tmp.length);
      gheader.comment = tmp;
    }

    return gheader;
  }
}
