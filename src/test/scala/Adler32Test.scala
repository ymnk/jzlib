/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
package com.jcraft.jzlib

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.{Test, Before}
import org.junit.Assert._
import org.hamcrest.CoreMatchers._
import java.util.zip.{Adler32 => juzAdler32}

@RunWith(classOf[JUnit4])
class Adler32Test {
  private var adler: Adler32 = _

  @Before
  def setUp = {
    adler = new Adler32
  }

  @Test
  def comat = {
    val buf1 = randombuf(1024)
    val juza = new juzAdler32
    val expected = {
      juza.update(buf1, 0, buf1.length)
      juza.getValue
    }
    val actual = getValue(List(buf1));

    assertThat(actual, is(expected)) 
  }

  @Test
  def copy = {
    val buf1 = randombuf(1024)
    val buf2 = randombuf(1024)

    val adler1 = new Adler32
    
    adler1.update(buf1, 0, buf1.length);

    val adler2 = adler1.copy

    adler1.update(buf2, 0, buf1.length);
    adler2.update(buf2, 0, buf1.length);

    val expected = adler1.getValue
    val actual = adler2.getValue

    assertThat(actual, is(expected)) 
  }

  @Test
  def combine = {

    val buf1 = randombuf(1024)
    val buf2 = randombuf(1024)

    val adler1 = getValue(List(buf1));
    val adler2 = getValue(List(buf2));
    val expected = getValue(List(buf1, buf2));

    val actual = Adler32.combine(adler1, adler2, buf2.length)

    assertThat(actual, is(expected)) 
  }

  private def getValue(buf:Seq[Array[Byte]]) = synchronized {
    adler.reset
    buf.foreach { b => adler.update(b, 0, b.length) }
    adler.getValue
  }
}
