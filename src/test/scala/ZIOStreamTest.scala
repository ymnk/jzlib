/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
package com.jcraft.jzlib

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.{Test, Before}
import org.junit.Assert._
import org.hamcrest.CoreMatchers._

import java.io.{ByteArrayOutputStream => BAOS, ByteArrayInputStream => BAIS}
import java.io.{ObjectOutputStream => OOS, ObjectInputStream => OIS}
import java.io._

import JZlib._

@RunWith(classOf[JUnit4])
class ZIOStreamTest {

  @Before
  def setUp = {
  }

  @Test
  def deflate_inflate = {
    val hello = "Hello World!"

    val out = new BAOS()
    val zOut = new ZOutputStream(out, Z_BEST_COMPRESSION)
    val objOut = new OOS(zOut)
    objOut.writeObject(hello)
    zOut.close

    val in = new BAIS(out.toByteArray())
    val zIn = new ZInputStream(in)
    val objIn = new OIS(zIn)

    assertThat(objIn.readObject.toString, is(hello))
  }

  @Test
  def nowrap = {

    implicit val buf = new Array[Byte](100)

    val hello = "Hello World!".getBytes

    val baos = new BAOS
    val zos = new ZOutputStream(baos, Z_DEFAULT_COMPRESSION, true)
    hello -> zos
    zos.close

    val baos2 = new BAOS
    val zis = new ZInputStream(new BAIS(baos.toByteArray), true)
    zis -> baos2
    val data2 = baos2.toByteArray

    assertThat(data2.length, is(hello.length))
    assertThat(data2, is(hello))
  }
}
