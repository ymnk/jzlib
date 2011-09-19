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
class ZIOStreamTest {

  @Before
  def setUp = {
  }

  @Test
  def deflate_inflate = {
    val hello = "Hello World!"

    val out = new ByteArrayOutputStream()
    val zOut = new ZOutputStream(out, Z_BEST_COMPRESSION)
    val objOut = new ObjectOutputStream(zOut)
    objOut.writeObject(hello)
    zOut.close

    val in = new ByteArrayInputStream(out.toByteArray())
    val zIn = new ZInputStream(in)
    val objIn = new ObjectInputStream(zIn)

    assertThat(objIn.readObject.toString, is(hello))
  }
}
