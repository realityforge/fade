package org.realityforge.fade;

import java.io.IOException;
import java.io.InputStream;
import junit.framework.TestCase;

public class TestConstantPool
  extends TestCase
{
  public void test_readClassData_initVars_EmptyClass()
    throws Exception
  {
    final byte[] bytes = loadTestData( "EmptyClass.class.dat" );
    final ConstantPool constantPool = ConstantPool.parseConstantPool( bytes, 0, bytes.length );
    assertEquals( "data", bytes, constantPool.data );
    final int count = 16;
    assertEquals( "offsets.length", constantPool.offsets.length, count );
    assertEquals( "strings.length", constantPool.strings.length, count );
    for( int i = 0; i < count; i++ )
    {
      assertNull( "strings[" + i + "]", constantPool.strings[i] );
    }
  }

  public void test_getEntryType_tooLow_EmptyClass()
    throws Exception
  {
    try
    {
      getEmptyClassData().getEntryType( 0 );
      fail( "Expected to get an IllegalArgumentException when accessing 0th entry" );
    }
    catch( final InvalidClassFileException icfe )
    {
      final String message =
        "Can not access constant pool element 0 as it is not in the range [1-16)";
      assertEquals( "getMessage()", message, icfe.getMessage() );
      assertEquals( "getOffset()", 0, icfe.getOffset() );
    }
  }

  public void test_getEntryType_tooHigh_EmptyClass()
    throws Exception
  {
    try
    {
      getEmptyClassData().getEntryType( 16 );
      fail( "Expected to get an IllegalArgumentException when accessing 16th entry" );
    }
    catch( final InvalidClassFileException icfe )
    {
      final String message =
        "Can not access constant pool element 16 as it is not in the range [1-16)";
      assertEquals( "getMessage()", message, icfe.getMessage() );
      assertEquals( "getOffset()", 0, icfe.getOffset() );
    }
  }

  public void test_getClassHeaderOffset_EmptyClass()
    throws Exception
  {
    assertEquals( "getClassHeaderOffset()", 226, getEmptyClassData().getClassHeaderOffset() );
  }

  public void test_getConstantCount_EmptyClass()
    throws Exception
  {
    assertEquals( "getConstantCount()", 15, getEmptyClassData().getConstantCount() );
  }

  public void test_getUtfEntry_EmptyClass()
    throws Exception
  {
    final ConstantPool constantPool = getEmptyClassData();
    final String value = "Code";
    assertEquals( "getUtfEntry( 6 )", value, constantPool.getUtfEntry( 6 ) );
    assertEquals( "strings[6] post get", value, constantPool.strings[6] );
  }

  public void test_getClassEntry_EmptyClass()
    throws Exception
  {
    final ConstantPool constantPool = getEmptyClassData();
    final String value = "org/realityforge/fade/data/EmptyClass";
    assertEquals( "getClassEntry( 2 )", value, constantPool.getClassEntry( 2 ) );
    assertEquals( "strings[2] post get", value, constantPool.strings[2] );
    assertEquals( "strings[14] post get", value, constantPool.strings[14] );
  }

  public void test_getEntryType_EmptyClass()
    throws Exception
  {
    assertEquals( "getUtfEntry( 13 )", getEmptyClassData().getEntryType( 13 ), ClassFileFormat.CONSTANT_NameAndType );
  }

  public void test_getNameFromNameAndType_MethodRef_EmptyClass()
    throws Exception
  {
    final ConstantPool constantPool = getEmptyClassData();
    final String value = "<init>";
    assertEquals( "getNameFromNameAndType( 13 )", constantPool.getNameFromNameAndType( 13 ), value );
    assertEquals( "strings[4] post get", value, constantPool.strings[4] );

  }

  public void test_getTypeFromNameAndType_MethodRef_EmptyClass()
    throws Exception
  {
    final ConstantPool constantPool = getEmptyClassData();
    final String value = "()V";
    assertEquals( "getTypeFromNameAndType( 13 )", constantPool.getTypeFromNameAndType( 13 ), value );
    assertEquals( "strings[5] post get", value, constantPool.strings[5] );
  }

  public void test_getClassFromRef_MethodRef_EmptyClass()
    throws Exception
  {
    final ConstantPool constantPool = getEmptyClassData();
    final String value = "java/lang/Object";
    assertEquals( "getClassFromRef( 1 )", constantPool.getClassFromRef( 1 ), value );
    assertEquals( "strings[15] post get", value, constantPool.strings[15] );
  }

  public void test_getNameFromRef_EmptyClass()
    throws Exception
  {
    final ConstantPool constantPool = getEmptyClassData();
    final String value = "<init>";
    assertEquals( "getNameFromRef( 1 )", constantPool.getNameFromRef( 1 ), value );
    assertEquals( "strings[4] post get", value, constantPool.strings[4] );
  }

  public void test_getTypeFromRef_EmptyClass()
    throws Exception
  {
    final ConstantPool constantPool = getEmptyClassData();
    final String value = "()V";
    assertEquals( "getTypeFromRef( 1 )", constantPool.getTypeFromRef( 1 ), value );
    assertEquals( "strings[5] post get", value, constantPool.strings[5] );
  }

  public void test_getFloatEntry_NonEmptyClass()
    throws Exception
  {
    assertEquals( "getFloatEntry( 4 )", 32.5F, getNonEmptyClassData().getFloatEntry( 4 ), 0.0 );
  }

  public void test_getDoubleEntry_NonEmptyClass()
    throws Exception
  {
    assertEquals( "getDoubleEntry( 7 )", 55.0D, getNonEmptyClassData().getDoubleEntry( 7 ), 0.0 );
  }

  public void test_getIntegerEntry_NonEmptyClass()
    throws Exception
  {
    assertEquals( "getIntegerEntry( 22 )", 42, getNonEmptyClassData().getIntegerEntry( 22 ) );
  }

  public void test_getLongEntry_NonEmptyClass()
    throws Exception
  {
    assertEquals( "getLongEntry( 5 )", 3L, getNonEmptyClassData().getLongEntry( 5 ) );
  }

  public void test_getStringEntry_NonEmptyClass()
    throws Exception
  {
    assertEquals( "getStringEntry( 2 )", "What is 6 x 9 in base 13?", getNonEmptyClassData().getStringEntry( 2 ) );
  }

  public void test_getNameFromRef_FieldRef_NonEmptyClass()
    throws Exception
  {
    final ConstantPool constantPool = getNonEmptyClassData();
    final String value = "m_question";
    assertEquals( "getNameFromRef( 3 )", constantPool.getNameFromRef( 3 ), value );
    assertEquals( "strings[23] post get", value, constantPool.strings[23] );

  }

  public void test_getTypeFromRef_FieldRef_NonEmptyClass()
    throws Exception
  {
    final ConstantPool constantPool = getNonEmptyClassData();
    final String value = "Ljava/lang/String;";
    assertEquals( "getTypeFromRef( 3 )", value, constantPool.getTypeFromRef( 3 ) );
    assertEquals( "strings[24] post get", value, constantPool.strings[24] );
  }

  public void test_getClassFromRef_FieldRef_NonEmptyClass()
    throws Exception
  {
    final ConstantPool constantPool = getNonEmptyClassData();
    final String value = "org/realityforge/fade/data/NonEmptyClass";
    assertEquals( "getClassFromRef( 3 )", value, constantPool.getClassFromRef( 3 ) );
    assertEquals( "strings[16] post get", value, constantPool.strings[16] );
  }

  public void test_getNameFromRef_InterfaceMethodRef_NonEmptyClass()
    throws Exception
  {
    final ConstantPool constantPool = getNonEmptyClassData();
    final String value = "runJumpSkipAndPlay";
    assertEquals( "getNameFromRef( 9 )", value, constantPool.getNameFromRef( 9 ) );
    assertEquals( "strings[56] post get", value, constantPool.strings[56] );
  }

  public void test_getTypeFromRef_InterfaceMethodRef_NonEmptyClass()
    throws Exception
  {
    final ConstantPool constantPool = getNonEmptyClassData();
    final String value = "()V";
    assertEquals( "getTypeFromRef( 9 )", value, constantPool.getTypeFromRef( 9 ) );
    assertEquals( "strings[57] post get", value, constantPool.strings[57] );
  }

  public void test_getClassFromRef_InterfaceMethod_NonEmptyClass()
    throws Exception
  {
    final ConstantPool constantPool = getNonEmptyClassData();
    final String value = "org/realityforge/fade/data/MyInterface";
    assertEquals( "getClassFromRef( 9 )", value, constantPool.getClassFromRef( 9 ) );
    assertEquals( "strings[18] post get", value, constantPool.strings[18] );
  }

  public void test_attempt_getTypeFromNameAndType_onNonRef_NonEmptyClass()
    throws Exception
  {
    try
    {
      getNonEmptyClassData().getTypeFromRef( 1 );
    }
    catch( final InvalidClassFileException icfe )
    {
      final String message =
        "Unexpected type for constant pool element 1. Expected a ref type but got 10";
      assertEquals( "getMessage()", message, icfe.getMessage() );
      assertEquals( "getOffset()", 0, icfe.getOffset() );
    }
  }

  public void test_attempt_getStringEntry_onNonString_NonEmptyClass()
    throws Exception
  {
    try
    {
      getNonEmptyClassData().getStringEntry( 1 );
    }
    catch( final InvalidClassFileException icfe )
    {
      final String message =
        "Unexpected type for constant pool element 1. Expected: 8 Actual: 10";
      assertEquals( "getMessage()", message, icfe.getMessage() );
      assertEquals( "getOffset()", 10, icfe.getOffset() );
    }
  }

  private ConstantPool getEmptyClassData()
    throws Exception
  {
    final byte[] bytes = loadTestData( "EmptyClass.class.dat" );
    return ConstantPool.parseConstantPool( bytes, 0, bytes.length );
  }

  private ConstantPool getNonEmptyClassData()
    throws Exception
  {
    final byte[] bytes = loadTestData( "NonEmptyClass.class.dat" );
    return ConstantPool.parseConstantPool( bytes, 0, bytes.length );
  }

  private byte[] loadTestData( final String resource )
    throws IOException
  {
    final InputStream input = TestConstantPool.class.getResourceAsStream( resource );
    assertNotNull( "Seemingly missing test data: " + resource, input );
    final int size = input.available();
    final byte[] bytes = new byte[size];
    final int count = input.read( bytes );
    assertEquals( "Unable to fully read testdata for: " + resource, count, size );
    return bytes;
  }
}
