/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
package com.jcraft.jzlib

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.{Test, Before}
import org.junit.Assert._
import org.hamcrest.CoreMatchers._
import java.io.{ByteArrayOutputStream => BAOS, ByteArrayInputStream => BAIS}

import JZlib._

@RunWith(classOf[JUnit4])
class HeaderTypeTest {
  val data = "hello, hello!".getBytes

  val comprLen = 40000
  val uncomprLen = comprLen
  var compr:Array[Byte] = _
  var uncompr:Array[Byte] = _
  var err: Int = _

  @Before
  def setUp = {
    compr = new Array[Byte](comprLen)
    uncompr = new Array[Byte](uncomprLen)

    err = Z_OK
  }

  @Test
  def w_zlib = {
    val deflater = new ZStream

    err = deflater.deflateInit(Z_BEST_SPEED, JZlib.DEF_WBITS, 9, JZlib.W_ZLIB)
    assertThat(err, is(Z_OK))

    deflate(deflater, data, compr)

    List(JZlib.W_ZLIB, JZlib.W_ANY) foreach { w =>
      val inflater = inflate(compr, uncompr, w)
      val total_out = inflater.total_out.asInstanceOf[Int]
      assertThat(new String(uncompr, 0, total_out), is(new String(data)))
    }

    List(JZlib.W_GZIP, JZlib.W_NONE) foreach { w =>
      inflate_fail(compr, uncompr, w)
    }
  }

  @Test
  def w_none = {
    val deflater = new ZStream

    err = deflater.deflateInit(Z_BEST_SPEED, JZlib.DEF_WBITS, 9, JZlib.W_NONE)
    assertThat(err, is(Z_OK))

    deflate(deflater, data, compr)

    List(JZlib.W_NONE, JZlib.W_ANY) foreach { w =>
      val inflater = inflate(compr, uncompr, w)
      val total_out = inflater.total_out.asInstanceOf[Int]
      assertThat(new String(uncompr, 0, total_out), is(new String(data)))
    }

    List(JZlib.W_GZIP, JZlib.W_ZLIB) foreach { w =>
      inflate_fail(compr, uncompr, w)
    }
  }

  @Test
  def w_gzip = {

    val deflater = new ZStream

    err = deflater.deflateInit(Z_BEST_SPEED, JZlib.DEF_WBITS, 9, JZlib.W_GZIP)
    assertThat(err, is(Z_OK))

    deflate(deflater, data, compr)

    List(JZlib.W_GZIP, JZlib.W_ANY) foreach { w =>
      val inflater = inflate(compr, uncompr, w)
      val total_out = inflater.total_out.asInstanceOf[Int]
      assertThat(new String(uncompr, 0, total_out), is(new String(data)))
    }

    List(JZlib.W_ZLIB, JZlib.W_NONE) foreach { w =>
      inflate_fail(compr, uncompr, w)
    }
  }

  private def deflate(deflater: ZStream,
                      data: Array[Byte], compr: Array[Byte]) = {
    deflater.setInput(data)
    deflater.setOutput(compr)

    err = deflater.deflate(JZlib.Z_FINISH)
    assertThat(err, is(Z_STREAM_END))

    err = deflater.end
    assertThat(err, is(Z_OK))
  }    

  private def inflate(compr: Array[Byte],
                      uncompr: Array[Byte],
                      w: HeaderType) = {
    val inflater = new ZStream
    err = inflater.inflateInit(w)
    assertThat(err, is(Z_OK))

    inflater.setInput(compr)

    var loop = true
    while(loop) {
      inflater.setOutput(uncompr)
      err = inflater.inflate(Z_NO_FLUSH)
      if(err == Z_STREAM_END) loop = false
      else assertThat(err, is(Z_OK))
    }
    err = inflater.end
    assertThat(err, is(Z_OK))

    inflater
  }

  private def inflate_fail(compr: Array[Byte],
                           uncompr: Array[Byte],
                           w: HeaderType) = {
    val inflater = new ZStream

    err = inflater.inflateInit(w)
    assertThat(err, is(Z_OK))

    inflater.setInput(compr)

    var loop = true
    while(loop) {
      inflater.setOutput(uncompr)
      err = inflater.inflate(Z_NO_FLUSH)
      if(err == Z_STREAM_END) loop = false
      else {
        assertThat(err, is(Z_DATA_ERROR))
        loop = false
      }
    }
  }
}
