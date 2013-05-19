/* -*-mode:scala; c-basic-offset:2; indent-tabs-mode:nil -*- */
package com.jcraft.jzlib

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

import java.io._

import JZlib._

class GZIPIOStreamTest extends FlatSpec with BeforeAndAfter with ShouldMatchers {

  before {
  }

  after {
  }

  behavior of "GZipOutputStream and GZipInputStream"

  it can "deflate and infate data." in {

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

    content.length should equal(i)
    (0 until i) foreach { i =>
      content(i).asInstanceOf[Byte] should equal(buf(i).asInstanceOf[Byte])
    }

    comment should equal(gis.getComment)
    name should equal(gis.getName)

    val crc32 = new CRC32
    crc32.update(content, 0, content.length)

    crc32.getValue should equal(gis.getCRC.asInstanceOf[Long])
  }
}
