package org.realityforge.fade;

import java.io.IOException;
import java.io.InputStream;
import junit.framework.TestCase;

public class TestClassFile
  extends TestCase
{
  public void test_parseClassFile_withbagmagic_EmptyClass()
    throws Exception
  {
    final byte[] bytes = loadTestData( "EmptyClass.class.dat" );
    bytes[0] = 0;
    bytes[1] = 0;
    bytes[2] = 0;
    bytes[3] = 0;
    try
    {
      ClassFile.parseClassFile( bytes );
      fail( "Expected an exception" );
    }
    catch( final InvalidClassFileException icfe )
    {
      final String message = "Bad magic number 0";
      assertEquals( "getMessage()", message, icfe.getMessage() );
      assertEquals( "getOffset()", 0, icfe.getOffset() );
    }
  }

  public void test_parseClassFile_with_bad_version_on_EmptyClass()
    throws Exception
  {
    final byte[] bytes = loadTestData( "EmptyClass.class.dat" );
    bytes[6] = 0;
    bytes[7] = 0;
    try
    {
      ClassFile.parseClassFile( bytes );
      fail( "Expected an exception" );
    }
    catch( final InvalidClassFileException icfe )
    {
      final String message = "Bad class file version 0.3";
      assertEquals( "getMessage()", message, icfe.getMessage() );
      assertEquals( "getOffset()", 6, icfe.getOffset() );
    }
  }

  public void test_parseClassFile_with_excess_data_on_EmptyClass()
    throws Exception
  {
    final byte[] bytes = loadTestData( "EmptyClass.class.dat" );
    final byte[] badBytes = new byte[bytes.length + 1];
    System.arraycopy( bytes, 0, badBytes, 0, bytes.length );
    try
    {
      ClassFile.parseClassFile( badBytes );
      fail( "Expected an exception" );
    }
    catch( final InvalidClassFileException icfe )
    {
      final String message = "Data past end of class definition.";
      assertEquals( "getMessage()", message, icfe.getMessage() );
      assertEquals( "getOffset()", 309, icfe.getOffset() );
    }
  }

  public void test_parseClassFile_with_truncated_data_on_EmptyClass()
    throws Exception
  {
    final byte[] bytes = loadTestData( "EmptyClass.class.dat" );
    final byte[] badBytes = new byte[bytes.length - 1];
    System.arraycopy( bytes, 0, badBytes, 0, badBytes.length );
    try
    {
      ClassFile.parseClassFile( badBytes );
      fail( "Expected an exception" );
    }
    catch( final InvalidClassFileException icfe )
    {
      final String message =
        "Class file truncated. data.length (308) < offset (307) + required (2)";
      assertEquals( "getMessage()", message, icfe.getMessage() );
      assertEquals( "getOffset()", 307, icfe.getOffset() );
    }
  }

  public void test_parseClassFile_inited_fields_on_EmptyClass()
    throws Exception
  {
    final byte[] bytes = loadTestData( "EmptyClass.class.dat" );
    final ClassFile classFile = ClassFile.parseClassFile( bytes );

    assertEquals( "data", bytes, classFile.data );
    assertEquals( "fieldOffsets.length", 0, classFile.fieldOffsets.length );
    assertEquals( "methodOffsets.length", 1, classFile.methodOffsets.length );
    assertEquals( "methodOffsets[0]", 238, classFile.methodOffsets[0] );
    assertEquals( "attributeOffset", 299, classFile.attributeOffset );
    assertNotNull( "constantPool", classFile.constantPool );
    assertEquals( "constantPool.getConstantCount()", 15, classFile.constantPool.getConstantCount() );
  }

  public void test_getMinorVersion_on_EmptyClass()
    throws Exception
  {
    assertEquals( "getMinorVersion", 3, getEmptyClassData().getMinorVersion() );
  }

  public void test_getMajorVersion_on_EmptyClass()
    throws Exception
  {
    assertEquals( "getMajorVersion", ClassFileFormat.MAJOR_VERSION_1, getEmptyClassData().getMajorVersion() );
  }

  public void test_getAccessFlags_on_EmptyClass()
    throws Exception
  {
    assertEquals( "getAccessFlags",
                  ClassFileFormat.ACC_PUBLIC + ClassFileFormat.ACC_SUPER,
                  getEmptyClassData().getAccessFlags() );
  }

  public void test_getInterfaces_on_EmptyClass()
    throws Exception
  {
    assertEquals( "getInterfaces().length",
                  0,
                  getEmptyClassData().getInterfaces().length );
  }

  public void test_getInterfaces_NonEmptyClass()
    throws Exception
  {
    final String[] interfaces = getNonEmptyClassData().getInterfaces();
    assertEquals( "getInterfaces().length", 1, interfaces.length );
    assertEquals( "getInterfaces()[0]", "org/realityforge/fade/data/MyInterface", interfaces[0] );
  }

  public void test_getClassName_on_EmptyClass()
    throws Exception
  {
    assertEquals( "getClassName",
                  "org/realityforge/fade/data/EmptyClass",
                  getEmptyClassData().getClassName() );
  }

  public void test_getMethodAccessFlags_on_EmptyClass()
    throws Exception
  {
    assertEquals( "getMethodAccessFlags",
                  ClassFileFormat.ACC_PUBLIC,
                  getEmptyClassData().getMethodAccessFlags( 0 ) );
  }

  public void test_getMethodName_on_EmptyClass()
    throws Exception
  {
    assertEquals( "getMethodName",
                  "<init>",
                  getEmptyClassData().getMethodName( 0 ) );
  }

  public void test_getMethodDescriptor_on_EmptyClass()
    throws Exception
  {
    assertEquals( "getMethodDescriptor",
                  "()V",
                  getEmptyClassData().getMethodDescriptor( 0 ) );
  }

  public void test_getMethodX_toolowindex_on_NonEmptyClass()
    throws Exception
  {
    try
    {
      getNonEmptyClassData().getMethodAccessFlags( -1 );
      fail( "Expected an exception" );
    }
    catch( final InvalidClassFileException icfe )
    {
      final String message =
        "Requested invalid method index. Available: 6 Actual: -1";
      assertEquals( "getMessage()", message, icfe.getMessage() );
      assertEquals( "getOffset()", 0, icfe.getOffset() );
    }
  }

  public void test_getMethodX_toohighindex_on_NonEmptyClass()
    throws Exception
  {
    try
    {
      getNonEmptyClassData().getMethodAccessFlags( 1000 );
      fail( "Expected an exception" );
    }
    catch( final InvalidClassFileException icfe )
    {
      final String message =
        "Requested invalid method index. Available: 6 Actual: 1000";
      assertEquals( "getMessage()", message, icfe.getMessage() );
      assertEquals( "getOffset()", 0, icfe.getOffset() );
    }
  }

  public void test_getSuperClassName_on_EmptyClass()
    throws Exception
  {
    assertEquals( "getSuperClassName",
                  "java/lang/Object",
                  getEmptyClassData().getSuperClassName() );
  }

  public void test_getInterfacesCount_on_EmptyClass()
    throws Exception
  {
    assertEquals( "getInterfacesCount",
                  0,
                  getEmptyClassData().getInterfacesCount() );
  }

  public void test_getFieldAccessFlags_on_NonEmptyClass()
    throws Exception
  {
    assertEquals( "getFieldAccessFlags",
                  ClassFileFormat.ACC_PRIVATE + ClassFileFormat.ACC_STATIC + ClassFileFormat.ACC_FINAL,
                  getNonEmptyClassData().getFieldAccessFlags( 0 ) );
  }

  public void test_getFieldName_on_NonEmptyClass()
    throws Exception
  {
    assertEquals( "getFieldName",
                  "ANSWER",
                  getNonEmptyClassData().getFieldName( 0 ) );
  }

  public void test_getFieldDescriptor_on_NonEmptyClass()
    throws Exception
  {
    assertEquals( "getFieldDescriptor",
                  "I",
                  getNonEmptyClassData().getFieldDescriptor( 0 ) );
  }

  public void test_getFieldX_with_too_low_index_on_NonEmptyClass()
    throws Exception
  {
    try
    {
      getNonEmptyClassData().getFieldAccessFlags( -1 );
      fail( "Expected an exception" );
    }
    catch( final InvalidClassFileException icfe )
    {
      final String message =
        "Requested invalid field index. Available: 2 Actual: -1";
      assertEquals( "getMessage()", message, icfe.getMessage() );
      assertEquals( "getOffset()", 0, icfe.getOffset() );
    }
  }

  public void test_getFieldX_with_too_high_index_on_NonEmptyClass()
    throws Exception
  {
    try
    {
      getNonEmptyClassData().getFieldAccessFlags( 1000 );
      fail( "Expected an exception" );
    }
    catch( final InvalidClassFileException icfe )
    {
      final String message =
        "Requested invalid field index. Available: 2 Actual: 1000";
      assertEquals( "getMessage()", message, icfe.getMessage() );
      assertEquals( "getOffset()", 0, icfe.getOffset() );
    }
  }

  private ClassFile getEmptyClassData()
    throws Exception
  {
    final byte[] bytes = loadTestData( "EmptyClass.class.dat" );
    return ClassFile.parseClassFile( bytes );
  }

  private ClassFile getNonEmptyClassData()
    throws Exception
  {
    final byte[] bytes = loadTestData( "NonEmptyClass.class.dat" );
    return ClassFile.parseClassFile( bytes );
  }

  private byte[] loadTestData( final String resource )
    throws IOException
  {
    final InputStream input = TestClassFile.class.getResourceAsStream( resource );
    assertNotNull( "Seemingly missing test data: " + resource, input );
    final int size = input.available();
    final byte[] bytes = new byte[size];
    final int count = input.read( bytes );
    assertEquals( "Unable to fully read testdata for: " + resource, count, size );
    return bytes;
  }
}