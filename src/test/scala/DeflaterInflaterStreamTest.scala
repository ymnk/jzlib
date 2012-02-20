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
class DeflaterInflaterStreamTest {

  @Before
  def setUp = {
  }

  @Test
  def one_by_one = {
    val data1 = randombuf(1024)
    implicit val buf = new Array[Byte](1)

    val baos = new BAOS
    val gos = new DeflaterOutputStream(baos)
    data1 -> gos
    gos.close

    val baos2 = new BAOS
    new InflaterInputStream(new BAIS(baos.toByteArray)) -> baos2
    val data2 = baos2.toByteArray 

    assertThat(data2.length, is(data1.length))
    assertThat(data2, is(data1))
  }

  @Test
  def read_write_with_buf = {

    (1 to 100 by 3).foreach { i =>

      implicit val buf = new Array[Byte](i)

      val data1 = randombuf(10240)

      val baos = new BAOS
      val gos = new DeflaterOutputStream(baos)
      data1 -> gos
      gos.close

      val baos2 = new BAOS
      new InflaterInputStream(new BAIS(baos.toByteArray)) -> baos2
      val data2 = baos2.toByteArray

      assertThat(data2.length, is(data1.length))
      assertThat(data2, is(data1))
    }
  }

  @Test
  def read_write_with_buf_nowrap = {

    (1 to 100 by 3).foreach { i =>

      implicit val buf = new Array[Byte](i)

      val data1 = randombuf(10240)

      val baos = new BAOS
      val deflater = new Deflater(JZlib.Z_DEFAULT_COMPRESSION,
                                 JZlib.DEF_WBITS,
                                 true)
      val gos = new DeflaterOutputStream(baos, deflater)
      data1 -> gos
      gos.close

      val baos2 = new BAOS
      val inflater = new Inflater(JZlib.DEF_WBITS, true)
      new InflaterInputStream(new BAIS(baos.toByteArray), inflater) -> baos2
      val data2 = baos2.toByteArray

      assertThat(data2.length, is(data1.length))
      assertThat(data2, is(data1))
    }
  }

  @Test
  def read_write_with_nowrap = {
    implicit val buf = new Array[Byte](100)

    List(randombuf(10240),
         """{"color":2,"id":"EvLd4UG.CXjnk35o1e8LrYYQfHu0h.d*SqVJPoqmzXM::Ly::Snaps::Store::Commit"}""".getBytes) foreach { data1 =>

      val deflater = new Deflater(JZlib.Z_DEFAULT_COMPRESSION,
                                  JZlib.MAX_WBITS,
                                  true)

      val inflater = new Inflater(JZlib.MAX_WBITS, true)

      val baos = new BAOS
      val gos = new DeflaterOutputStream(baos, deflater)
      data1 -> gos
      gos.close

      val baos2 = new BAOS
      new InflaterInputStream(new BAIS(baos.toByteArray), inflater) -> baos2
      val data2 = baos2.toByteArray

      assertThat(data2.length, is(data1.length))
      assertThat(data2, is(data1))
    }
  }
}
