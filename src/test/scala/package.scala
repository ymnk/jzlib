/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
package com.jcraft

import java.io._

package object jzlib {
  implicit def readIS(is: InputStream) = new {
      def ->(out: OutputStream)(implicit buf: Array[Byte]) = {
      Stream.continually(is.read(buf)).
                         takeWhile(-1 !=).foreach(i => out.write(buf, 0, i))
      is.close
    }
  }

  implicit def readArray(is: Array[Byte]) = new {
      def ->(out: OutputStream)(implicit buf: Array[Byte]) = {
        new ByteArrayInputStream(is) -> (out)
    }
  }

  def randombuf(n: Int) = (0 to n).map{ _ =>
    util.Random.nextLong.asInstanceOf[Byte] 
  }.toArray
}
