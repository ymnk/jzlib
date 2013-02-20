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
class WrapperTypeTest {
  val data = "hello, hello!".getBytes

  val comprLen = 40000
  val uncomprLen = comprLen
  var compr:Array[Byte] = _
  var uncompr:Array[Byte] = _
  var err: Int = _

  val cases =      /* success */        /* fail */ 
    List((W_ZLIB, (List(W_ZLIB, W_ANY), List(W_GZIP, W_NONE))),
         (W_GZIP, (List(W_GZIP, W_ANY), List(W_ZLIB, W_NONE))),
         (W_NONE, (List(W_NONE, W_ANY), List(W_ZLIB, W_GZIP))))

  @Before
  def setUp = {
    compr = new Array[Byte](comprLen)
    uncompr = new Array[Byte](uncomprLen)

    err = Z_OK
  }

  @Test
  def DeflterInflaterStream = {
    implicit val buf = compr

    cases foreach { case (iflag, (good, bad)) => 
      val baos = new BAOS
      val deflater = new Deflater(Z_DEFAULT_COMPRESSION, DEF_WBITS, 9, iflag)
      val gos = new DeflaterOutputStream(baos, deflater)
      data -> gos
      gos.close

      val deflated = baos.toByteArray

      good map { w =>
        val baos2 = new BAOS
        val inflater = new Inflater(w)
        new InflaterInputStream(new BAIS(deflated), inflater) -> baos2
        val data1 = baos2.toByteArray
        assertThat(data1.length, is(data.length))
        assertThat(data1, is(data))
        import inflater._
        (avail_in, avail_out, total_in, total_out)
      } reduceLeft { (x, y) => assertThat(x, is(y)); x }

      bad foreach { w =>
        val baos2 = new BAOS
        val inflater = new Inflater(w)
        try {
          new InflaterInputStream(new BAIS(deflated), inflater) -> baos2
          fail("unreachable")
        }
        catch {
          case e:java.io.IOException  =>
        }
      } 
    }
  } 

  @Test
  def ZStream = {
    cases foreach { case (iflag, (good, bad)) => 
      val deflater = new ZStream

      err = deflater.deflateInit(Z_BEST_SPEED, DEF_WBITS, 9, iflag)
      assertThat(err, is(Z_OK))

      deflate(deflater, data, compr)

      good foreach { w =>
        val inflater = inflate(compr, uncompr, w)
        val total_out = inflater.total_out.asInstanceOf[Int]
        assertThat(new String(uncompr, 0, total_out), is(new String(data)))
      }

      bad foreach { w =>
        inflate_fail(compr, uncompr, w)
      }
    }
  }

  @Test
  def wbits_plus_32 = {

    var deflater = new Deflater
    err = deflater.init(Z_BEST_SPEED, DEF_WBITS, 9)
    assertThat(err, is(Z_OK))

    deflate(deflater, data, compr)

    var inflater = new Inflater
    err = inflater.init(DEF_WBITS + 32)
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

    var total_out = inflater.total_out.asInstanceOf[Int]
    assertThat(new String(uncompr, 0, total_out), is(new String(data)))

    deflater = new Deflater
    err = deflater.init(Z_BEST_SPEED, DEF_WBITS + 16, 9)
    assertThat(err, is(Z_OK))

    deflate(deflater, data, compr)

    inflater = new Inflater
    err = inflater.init(DEF_WBITS + 32)
    assertThat(err, is(Z_OK))

    inflater.setInput(compr)

    loop = true
    while(loop) {
      inflater.setOutput(uncompr)
      err = inflater.inflate(Z_NO_FLUSH)
      if(err == Z_STREAM_END) loop = false
      else assertThat(err, is(Z_OK))
    }
    err = inflater.end
    assertThat(err, is(Z_OK))

    total_out = inflater.total_out.asInstanceOf[Int]
    assertThat(new String(uncompr, 0, total_out), is(new String(data)))
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
                      w: WrapperType) = {
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
                           w: WrapperType) = {
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
