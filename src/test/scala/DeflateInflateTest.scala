/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
package com.jcraft.jzlib

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.{Test, Before}
import org.junit.Assert._
import org.hamcrest.CoreMatchers._

import JZlib._

@RunWith(classOf[JUnit4])
class DeflateInflateTest {
  val comprLen = 40000
  val uncomprLen = comprLen
  var compr:Array[Byte] = _
  var uncompr:Array[Byte] = _

  var deflater: Deflater = _
  var inflater: Inflater = _
  var err: Int = _
  @Before
  def setUp = {
    compr = new Array[Byte](comprLen)
    uncompr = new Array[Byte](uncomprLen)

    deflater = new Deflater
    inflater = new Inflater

    err = Z_OK
  }

  @Test
  def large = {
    val data = "hello, hello!".getBytes

    err = deflater.init(Z_BEST_SPEED)
    assertThat(err, is(Z_OK))

    deflater.setInput(uncompr)
    deflater.setOutput(compr)

    err = deflater.deflate(Z_NO_FLUSH)
    assertThat(err, is(Z_OK))

    assertThat(deflater.avail_in, is(0))

    deflater.params(Z_NO_COMPRESSION, Z_DEFAULT_STRATEGY)
    deflater.setInput(compr)
    deflater.avail_in = comprLen/2 

    err = deflater.deflate(Z_NO_FLUSH)
    assertThat(err, is(Z_OK))

    deflater.params(Z_BEST_COMPRESSION, Z_FILTERED)
    deflater.setInput(uncompr)
    deflater.avail_in = uncomprLen

    err = deflater.deflate(Z_NO_FLUSH)
    assertThat(err, is(Z_OK))

    err = deflater.deflate(JZlib.Z_FINISH);
    assertThat(err, is(Z_STREAM_END))

    err = deflater.end
    assertThat(err, is(Z_OK))

    inflater.setInput(compr)

    err = inflater.init
    assertThat(err, is(Z_OK))

    var loop = true
    while(loop) {
      inflater.setOutput(uncompr)
      err = inflater.inflate(Z_NO_FLUSH)
      if(err == Z_STREAM_END) loop = false
      else assertThat(err, is(Z_OK))
    }

    err = inflater.end
    assertThat(err, is(Z_OK))

    val total_out = inflater.total_out.asInstanceOf[Int]

    assertThat(total_out, is(2*uncomprLen + comprLen/2))
  }

  @Test
  def small_buffers = {
    val data = "hello, hello!".getBytes

    err = deflater.init(Z_DEFAULT_COMPRESSION)
    assertThat(err, is(Z_OK))

    deflater.setInput(data)
    deflater.setOutput(compr)

    while(deflater.total_in < data.length &&
          deflater.total_out < comprLen){
      deflater.avail_in = 1
      deflater.avail_out = 1
      err = deflater.deflate(Z_NO_FLUSH)
      assertThat(err, is(Z_OK))
    }

    do {
      deflater.avail_out = 1
      err = deflater.deflate(Z_FINISH)
    }
    while(err != Z_STREAM_END);

    err = deflater.end
    assertThat(err, is(Z_OK))

    inflater.setInput(compr)
    inflater.setOutput(uncompr)

    err = inflater.init
    assertThat(err, is(Z_OK))

    var loop = true
    while(inflater.total_out<uncomprLen &&
          inflater.total_in<comprLen && 
          loop) {
      inflater.avail_in = 1; // force small buffers
      inflater.avail_out = 1; // force small buffers
      err = inflater.inflate(Z_NO_FLUSH)
      if(err == Z_STREAM_END) loop = false
      else assertThat(err, is(Z_OK))
    }

    err = inflater.end
    assertThat(err, is(Z_OK))

    val total_out = inflater.total_out.asInstanceOf[Int]
    val actual = new Array[Byte](total_out)
    System.arraycopy(uncompr, 0, actual, 0, total_out)

    assertThat(actual, is(data)) 
  }

  @Test
  def dictionary = {
    val hello = "hello".getBytes
    val dictionary = "hello, hello!".getBytes

    err = deflater.init(Z_DEFAULT_COMPRESSION)
    assertThat(err, is(Z_OK))

    deflater.setDictionary(dictionary, dictionary.length)
    assertThat(err, is(Z_OK))

    val dictID = deflater.getAdler

    deflater.setInput(hello)
    deflater.setOutput(compr)

    err = deflater.deflate(Z_FINISH)
    assertThat(err, is(Z_STREAM_END))

    err = deflater.end
    assertThat(err, is(Z_OK))

    err = inflater.init
    assertThat(err, is(Z_OK))

    inflater.setInput(compr)
    inflater.setOutput(uncompr)

    var loop = true
    do {
      err = inflater.inflate(JZlib.Z_NO_FLUSH)
      err match {
        case Z_STREAM_END =>
          loop = false
        case Z_NEED_DICT =>
          assertThat(dictID, is(inflater.getAdler))
          err = inflater.setDictionary(dictionary, dictionary.length);
          assertThat(err, is(Z_OK))
        case _ =>
          assertThat(err, is(Z_OK))
      }
    }
    while(loop)

    err = inflater.end
    assertThat(err, is(Z_OK))

    val total_out = inflater.total_out.asInstanceOf[Int]
    val actual = new Array[Byte](total_out)
    System.arraycopy(uncompr, 0, actual, 0, total_out)

    assertThat(actual, is(hello)) 
  }

  @Test
  def sync = {
    val hello = "hello".getBytes

    err = deflater.init(Z_DEFAULT_COMPRESSION)
    assertThat(err, is(Z_OK))

    deflater.setInput(hello)
    deflater.avail_in = 3;
    deflater.setOutput(compr)

    err = deflater.deflate(Z_FULL_FLUSH)
    assertThat(err, is(Z_OK))

    compr(3) = (compr(3) + 1).asInstanceOf[Byte]
    deflater.avail_in = hello.length - 3;

    err = deflater.deflate(Z_FINISH)
    assertThat(err, is(Z_STREAM_END))
    val comprLen= deflater.total_out.asInstanceOf[Int]

    err = deflater.end
    assertThat(err, is(Z_OK))

    err = inflater.init
    assertThat(err, is(Z_OK))

    inflater.setInput(compr)
    inflater.avail_in = 2

    inflater.setOutput(uncompr)

    err = inflater.inflate(JZlib.Z_NO_FLUSH)
    assertThat(err, is(Z_OK))

    inflater.avail_in = comprLen-2
    err = inflater.sync

    err = inflater.inflate(Z_FINISH)
    assertThat(err, is(Z_DATA_ERROR))

    err = inflater.end
    assertThat(err, is(Z_OK))

    val total_out = inflater.total_out.asInstanceOf[Int]
    val actual = new Array[Byte](total_out)
    System.arraycopy(uncompr, 0, actual, 0, total_out)

    assertThat("hel"+new String(actual), is(new String(hello))) 
  }

  @Test
  def gzip_inflater = {
    val hello = "foo".getBytes
    val data = List(0x1f, 0x8b, 0x08, 0x18, 0x08, 0xeb, 0x7a, 0x0b, 0x00, 0x0b,
                    0x58, 0x00, 0x59, 0x00, 0x4b, 0xcb, 0xcf, 0x07, 0x00, 0x21,
                    0x65, 0x73, 0x8c, 0x03, 0x00, 0x00, 0x00).
                    map(_.asInstanceOf[Byte]).
                    toArray

    err = inflater.init(15 + 32)
    assertThat(err, is(Z_OK))

    inflater.setInput(data)
    inflater.setOutput(uncompr)

    val comprLen = data.length

    var loop = true
    while(inflater.total_out<uncomprLen &&
          inflater.total_in<comprLen && 
          loop) {
      err = inflater.inflate(Z_NO_FLUSH)
      if(err == Z_STREAM_END) loop = false
      else assertThat(err, is(Z_OK))
    }

    err = inflater.end
    assertThat(err, is(Z_OK))

    val total_out = inflater.total_out.asInstanceOf[Int]
    val actual = new Array[Byte](total_out)
    System.arraycopy(uncompr, 0, actual, 0, total_out)

    assertThat(actual, is(hello)) 
  }

  @Test
  def gzip_deflate_inflate = {
    val data = "hello, hello!".getBytes

    err = deflater.init(Z_DEFAULT_COMPRESSION, 15+16)
    assertThat(err, is(Z_OK))

    deflater.setInput(data)
    deflater.setOutput(compr)

    while(deflater.total_in < data.length &&
          deflater.total_out < comprLen){
      deflater.avail_in = 1
      deflater.avail_out = 1
      err = deflater.deflate(Z_NO_FLUSH)
      assertThat(err, is(Z_OK))
    }

    do {
      deflater.avail_out = 1
      err = deflater.deflate(Z_FINISH)
    }
    while(err != Z_STREAM_END);

    err = deflater.end
    assertThat(err, is(Z_OK))

    inflater.setInput(compr)
    inflater.setOutput(uncompr)

    err = inflater.init(15 + 32)
    assertThat(err, is(Z_OK))

    var loop = true
    while(inflater.total_out<uncomprLen &&
          inflater.total_in<comprLen && 
          loop) {
      inflater.avail_in = 1; // force small buffers
      inflater.avail_out = 1; // force small buffers
      err = inflater.inflate(Z_NO_FLUSH)
      if(err == Z_STREAM_END) loop = false
      else assertThat(err, is(Z_OK))
    }

    err = inflater.end
    assertThat(err, is(Z_OK))

    val total_out = inflater.total_out.asInstanceOf[Int]
    val actual = new Array[Byte](total_out)
    System.arraycopy(uncompr, 0, actual, 0, total_out)

    assertThat(actual, is(data)) 
  }
}
