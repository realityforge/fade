package org.realityforge.fade;

import junit.framework.TestCase;

public class TestIOUtil
  extends TestCase
{
  public void test_readInteger()
  {
    final byte[] data = new byte[4];
    data[0] = 53;
    data[1] = 89;
    data[2] = 62;
    data[3] = 77;
    assertEquals( 895041101, IOUtil.readInteger( data, 0 ) );
  }

  public void test_readUnsignedShort()
  {
    final byte[] data = new byte[2];
    data[0] = 53;
    data[1] = 89;
    assertEquals( 13657, IOUtil.readUnsignedShort( data, 0 ) );
  }

  public void test_readLong()
  {
    final byte[] data = new byte[8];
    data[0] = -123;
    data[1] = 89;
    data[2] = 62;
    data[3] = 77;
    data[4] = 23;
    data[5] = 119;
    data[6] = 32;
    data[7] = -127;
    assertEquals( -8837964292910800767L, IOUtil.readLong( data, 0 ) );
  }
}
