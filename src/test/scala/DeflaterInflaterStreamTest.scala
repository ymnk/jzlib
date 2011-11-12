/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
package com.jcraft.jzlib

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.{Test, Before}
import org.junit.Assert._
import org.hamcrest.CoreMatchers._

import java.io._

import JZlib._

@RunWith(classOf[JUnit4])
class DeflaterInflaterStreamTest {

  @Before
  def setUp = {
  }

  @Test
  def one_by_one = {
    val data1 = randombuf(1024)

    val baos = new ByteArrayOutputStream
    val gos = new DeflaterOutputStream(baos)

    for(d <- data1){
      gos.write(d&0xff)
    } 
    gos.close

    val bais = new ByteArrayInputStream(baos.toByteArray)
    val gis = new InflaterInputStream(bais)

    val data2 = Stream.continually(gis.read).
                takeWhile(-1 !=).map(_.toByte).toArray 

    assertThat(data2.length, is(data1.length))
    assertThat(data2, is(data1))
  }

  @Test
  def read_write_with_buf = {

    List(true, false).foreach { nowrap =>
    (1 to 100 by 3).foreach { i =>
    List(randombuf(10240), 
         "{\"color\":2,\"id\":\"EvLd4UG.CXjnk35o1e8LrYYQfHu0h.d*SqVJPoqmzXM::Ly::Snaps::Store::Commit\"}".getBytes).foreach { data1 =>
                              
      val buf = new Array[Byte](i)

      val baos = new ByteArrayOutputStream
      val gos = new DeflaterOutputStream(baos,
                                         new Deflater(JZlib.Z_DEFAULT_COMPRESSION,
                                                      JZlib.MAX_WBITS,
                                                      nowrap))

      val datai = new ByteArrayInputStream(data1)
      Stream.continually(datai.read(buf)).
                        takeWhile(-1 !=).foreach(i => gos.write(buf, 0, i))
      gos.close
      datai.close

      val bais = new ByteArrayInputStream(baos.toByteArray)
      val gis = new InflaterInputStream(bais, new Inflater(JZlib.MAX_WBITS, nowrap))

      val baos2 = new ByteArrayOutputStream

      Stream.continually(gis.read(buf)).
                        takeWhile(-1 !=).foreach(i => baos2.write(buf, 0, i))

      val data2 = baos2.toByteArray

      assertThat(data2.length, is(data1.length))
      assertThat(data2, is(data1))
    }
    }
    }
  }

  private def randombuf(n: Int) = (0 to n).map{_ =>
    scala.util.Random.nextLong.asInstanceOf[Byte] 
  }.toArray
}
