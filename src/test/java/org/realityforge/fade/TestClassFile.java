package org.realityforge.fade;

import java.io.IOException;
import java.io.InputStream;
import junit.framework.TestCase;

public class TestClassFile
  extends TestCase
{
  public void test_parseClassFile_with_bag_magic_on_EmptyClass()
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
    catch( final ClassFormatError cfe )
    {
      final String message = "Bad magic number 0";
      assertEquals( "getMessage()", message, cfe.getMessage() );
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
    catch( final ClassFormatError cfe )
    {
      final String message = "Bad class file version 0.3";
      assertEquals( "getMessage()", message, cfe.getMessage() );
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
    catch( final ClassFormatError icfe )
    {
      final String message =
        "Class definition ends at position 309 when the class data is 310 bytes long.";
      assertEquals( "getMessage()", message, icfe.getMessage() );
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
    catch( final ClassFormatError cfe )
    {
      final String message =
        "Class file is truncated. Require 2 bytes at position " +
        "307 when class file is only 308 bytes long.";
      assertEquals( "getMessage()", message, cfe.getMessage() );
    }
  }

  public void test_parseClassFile_inited_fields_on_EmptyClass()
    throws Exception
  {
    final byte[] bytes = loadTestData( "EmptyClass.class.dat" );
    final ClassFile classFile = ClassFile.parseClassFile( bytes );

    assertEquals( "data", bytes, classFile.getData() );
    assertEquals( "fieldOffsets.length", 0, classFile.getFieldOffsets().length );
    assertEquals( "methodOffsets.length", 1, classFile.getMethodOffsets().length );
    assertEquals( "methodOffsets[0]", 238, classFile.getMethodOffsets()[0] );
    assertEquals( "attributeOffset", 299, classFile.getAttributeOffset() );
    assertNotNull( "constantPool", classFile.getConstantPool() );
    assertEquals( "constantPool.getConstantCount()", 15, classFile.getConstantPool().getConstantCount() );
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
    catch( final ClassFormatError cfe )
    {
      final String message =
        "Requested invalid method index -1 when there is only 6 methods";
      assertEquals( "getMessage()", message, cfe.getMessage() );
    }
  }

  public void test_getMethodX_too_high_index_on_NonEmptyClass()
    throws Exception
  {
    try
    {
      getNonEmptyClassData().getMethodAccessFlags( 1000 );
      fail( "Expected an exception" );
    }
    catch( final ClassFormatError cfe )
    {
      final String message =
        "Requested invalid method index 1000 when there is only 6 methods";
      assertEquals( "getMessage()", message, cfe.getMessage() );
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
    catch( final ClassFormatError cfe )
    {
      final String message =
        "Requested invalid field index -1 when there is only 2 fields";
      assertEquals( "getMessage()", message, cfe.getMessage() );
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
    catch( final ClassFormatError cfe )
    {
      final String message =
        "Requested invalid field index 1000 when there is only 2 fields";
      assertEquals( "getMessage()", message, cfe.getMessage() );
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

  ///silly "test" to get coverage up. Real testing occurs in {@link TestClassFileParse}
  public void test_processClass_parsing_all_sections()
      throws Exception
  {
    getNonEmptyClassData().processClass( new NoopClassFileParser(), true, true, true );
  }

  ///silly "test" to get coverage up. Real testing occurs in {@link TestClassFileParse}
  public void test_processClass_parsing_no_sections()
      throws Exception
  {
    getNonEmptyClassData().processClass( new NoopClassFileParser(), false, false, false );
  }
  
  static class NoopClassFileParser
    extends ClassFileParser
  {
    protected void startAnnotationValueAnnotation( final String name )
    {
    }

    protected void endAnnotationValueAnnotation()
    {
    }

    protected void handleField( final String name, final String descriptor, final int accessFlags )
    {
    }

    protected void handleFieldAttribute( final String name, final byte[] data, final int offset, final long length, final ConstantPool constantPool )
    {
    }

    protected void handleMethodAttribute( final String name, final byte[] data, final int offset, final long length, final ConstantPool constantPool )
    {
    }

    protected void handleClassAttribute( final String name, final byte[] data, final int offset, final long length, final ConstantPool constantPool )
    {
    }

    protected void handleMethod( final String name, final String descriptor, final int accessFlags )
    {
    }

    protected void endCode()
    {
    }

    protected void handleCodeAttribute( final String name, final byte[] data, final int offset, final long length, final ConstantPool constantPool )
    {
    }

    protected void handleExceptionHandler( final int startPC, final int endPC, final int handlerPC, final String catchType )
    {
    }

    protected void startCode(final int maxStack, final int maxLocals, final byte[] data, final int offset, final long codeLength, final ConstantPool constantPool)
    {
    }

    protected void handleExceptions( final String[] exceptions )
    {
    }

    protected void handleConstantValue( final Object value )
    {
    }

    protected void startAnnotationValueArray( final String name, final int length )
    {
    }

    protected void endAnnotationValueArray()
    {
    }

    protected void handleAnnotationEnumValue( final String name, final String key, final String value )
    {
    }

    protected void handleAnnotationValue( final String name, final Object value )
    {
    }

    protected void endAnnotation()
    {
    }

    protected void startAnnotation( final String type )
    {
    }

    protected void handleAnnotationGroup( final String type )
    {
    }

    protected void handleSourceDebug( final String value )
    {
    }

    protected void handleSourceFile( final String filename )
    {
    }

    protected void handleEnclosingMethod( final String klass, final String methodName, final String methodType )
    {
    }

    protected void handleSignature( final String signature )
    {
    }

    protected void handleInnerClass( final String innerClass, final String outerClass, final String innerName, final int innerClassAccessFlags )
    {
    }

    protected void handleDeprecated()
    {
    }

    protected void handleSynthetic()
    {
    }
  }
}
