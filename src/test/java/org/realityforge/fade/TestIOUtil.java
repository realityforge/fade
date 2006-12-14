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

  public void test_readUnsignedInteger()
  {
    final byte[] data = new byte[4];
    data[0] = -123;
    data[1] = 89;
    data[2] = 62;
    data[3] = -122;
    assertEquals( 2237218438L, IOUtil.readUnsignedInteger( data, 0 ) );
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

  public void test_checkLength()
  {
    IOUtil.checkLength( new byte[10], 0, 1 );
  }

  public void test_checkLength_when_not_enough_length()
  {
    try
    {
      IOUtil.checkLength( new byte[10], 0, 11 );
      fail( "Expected an exception" );
    }
    catch( final ClassFormatError cfe )
    {
      final String message =
        "Class file is truncated. Require 11 bytes at position " +
        "0 when class file is only 10 bytes long.";
      assertEquals( "getMessage()", message, cfe.getMessage() );
    }
  }

  public void test_parseUtfString()
    throws Exception
  {
    final byte[] data = new byte[]{'a', (byte)0xCF, (byte)0x8F, (byte)0xEF, (byte)0x8F, (byte)0x80};
    final String value = IOUtil.parseUtfString( data, 0, 0, 6 );
    assertEquals( "value.length", 3, value.length() );
    assertEquals( "value.charAt( 0 )", 'a', value.charAt( 0 ) );
    assertEquals( "value.charAt( 1 )", 975, value.charAt( 1 ) );
    assertEquals( "value.charAt( 2 )", 62400, value.charAt( 2 ) );
  }

  public void test_parseUtfString_invalid_singlechar()
    throws Exception
  {
    final byte[] data = new byte[]{0};
    try
    {
      IOUtil.parseUtfString( data, 0, 0, 1 );
      fail( "Expected exception" );
    }
    catch( final ClassFormatError cfe )
    {
      final String message =
        "Constant pool entry 0 has invalid utf8 at 0";
      assertEquals( "getMessage()", message, cfe.getMessage() );
    }
  }

  public void test_parseUtfString_invalid_doublechar_tooshort()
    throws Exception
  {
    final byte[] data = new byte[]{'a', (byte)0xCF};
    try
    {
      IOUtil.parseUtfString( data, 0, 0, 2 );
      fail( "Expected exception" );
    }
    catch( final ClassFormatError cfe )
    {
      final String message =
        "Constant pool entry 0 has invalid utf8 at 1";
      assertEquals( "getMessage()", message, cfe.getMessage() );
    }
  }

  public void test_parseUtfString_invalid_doublechar()
    throws Exception
  {
    final byte[] data = new byte[]{'a', (byte)0xCF, (byte)0xFF};
    try
    {
      IOUtil.parseUtfString( data, 0, 0, 3 );
      fail( "Expected exception" );
    }
    catch( final ClassFormatError cfe )
    {
      final String message =
        "Constant pool entry 0 has invalid utf8 at 1";
      assertEquals( "getMessage()", message, cfe.getMessage() );
    }
  }

  public void test_parseUtfString_invalid_triplechar_tooshort()
    throws Exception
  {
    final byte[] data = new byte[]{'a', (byte)0xCF, (byte)0x8F, (byte)0xEF, (byte)0x8F};
    try
    {
      IOUtil.parseUtfString( data, 0, 0, 5 );
      fail( "Expected exception" );
    }
    catch( final ClassFormatError cfe )
    {
      final String message =
        "Constant pool entry 0 has invalid utf8 at 3";
      assertEquals( "getMessage()", message, cfe.getMessage() );
    }
  }

  public void test_parseUtfString_invalid_triplechar()
    throws Exception
  {
    final byte[] data = new byte[]{'a', (byte)0xCF, (byte)0x8F, (byte)0xEF, (byte)0x8F, (byte)0xF0};
    try
    {
      IOUtil.parseUtfString( data, 0, 0, 6 );
      fail( "Expected exception" );
    }
    catch( final ClassFormatError cfe )
    {
      final String message =
        "Constant pool entry 0 has invalid utf8 at 3";
      assertEquals( "getMessage()", message, cfe.getMessage() );
    }
  }

  public void test_parseUtfString_psuedo_quadchar()
    throws Exception
  {
    final byte[] data = new byte[]{'a', (byte)0xCF, (byte)0x8F, (byte)0xFF, (byte)0x8F, (byte)0x80, (byte)0x80};
    try
    {
      IOUtil.parseUtfString( data, 0, 0, 7 );
      fail( "Expected exception" );
    }
    catch( final ClassFormatError cfe )
    {
      final String message =
        "Constant pool entry 0 has invalid utf8 at 3";
      assertEquals( "getMessage()", message, cfe.getMessage() );
    }
  }
}
