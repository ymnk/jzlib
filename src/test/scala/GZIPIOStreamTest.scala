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
class GZIPIOStreamTest {

  @Before
  def setUp = {
  }

  @Test
  def outstream = {

    val comment = "hi"
    val name = "/tmp/foo"

    val content = "hello".getBytes

    val baos = new ByteArrayOutputStream
    val gos = new GZIPOutputStream(baos)

    gos.setComment(comment)
    gos.setName(name)
 
    gos.write(content)
    gos.close

    val bais = new ByteArrayInputStream(baos.toByteArray)
    val gis = new GZIPInputStream(bais)

    val buf = new Array[Byte](1024)
    val i = gis.read(buf)

    assertThat(content.length, is(i))
    (0 until i) foreach { i =>
      assertThat(content(i).asInstanceOf[Byte], is(buf(i).asInstanceOf[Byte]))
    }

    assertThat(comment, is(gis.getComment))
    assertThat(name, is(gis.getName))

    val crc32 = new CRC32
    crc32.update(content, 0, content.length)

    assertThat(crc32.getValue, is(gis.getCRC.asInstanceOf[Long]))
  }
}
