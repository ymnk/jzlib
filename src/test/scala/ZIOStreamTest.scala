/* -*-mode:scala; c-basic-offset:2; indent-tabs-mode:nil -*- */
package com.jcraft.jzlib

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

import java.io.{ByteArrayOutputStream => BAOS, ByteArrayInputStream => BAIS}
import java.io.{ObjectOutputStream => OOS, ObjectInputStream => OIS}
import java.io._

import JZlib._

class ZIOStreamTest extends FlatSpec with BeforeAndAfter with ShouldMatchers {

  before {
  }

  after {
  }

  behavior of "ZOutputStream and ZInputStream"

  it can "deflate and inflate data." in {
    val hello = "Hello World!"

    val out = new BAOS()
    val zOut = new ZOutputStream(out, Z_BEST_COMPRESSION)
    val objOut = new OOS(zOut)
    objOut.writeObject(hello)
    zOut.close

    val in = new BAIS(out.toByteArray())
    val zIn = new ZInputStream(in)
    val objIn = new OIS(zIn)

    objIn.readObject.toString should equal (hello)
  }

  behavior of "ZOutputStream and ZInputStream"

  it can "support nowrap data." in {

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

    data2.length should equal (hello.length)
    data2 should equal (hello)
  }
}
