package org.realityforge.fade;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import junit.framework.TestCase;

public class TestClassFileParser
  extends TestCase
{
  static private class ConcreteParser
    extends ClassFileParser
  {
  }

  public void test_parseConstantValue_with_invalid_type()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        42, 0, 2, //value
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //value
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        0, 1, //value index
      };

    final ConcreteParser parser = new ConcreteParser();
    try
    {
      parser.parseConstantValue( data, 0, constantPool );
    }
    catch( final ClassFormatError cfe )
    {
      final String message =
        "ConstantValue attribute at 0 references constant pool value of type 42";
      assertEquals( "cfe.getMessage()", message, cfe.getMessage() );

    }
  }

  public void test_parseConstantValue_expecting_String()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        8, 0, 2, //value
        1, 0, 1, 'a' //string
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //value
        3, //string
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        0, 1, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleConstantValue( final Object value )
      {
        assertEquals( "value", "a", value );
      }
    };

    parser.parseConstantValue( data, 0, constantPool );
  }

  public void test_parseConstantValue_expecting_Double()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        6, 0, 0, 0, 0, 0, 0, 0, 42 //value
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //value
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        0, 1, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleConstantValue( final Object value )
      {
        assertEquals( "value", Double.longBitsToDouble( 42L ), value );
      }
    };

    parser.parseConstantValue( data, 0, constantPool );
  }

  public void test_parseConstantValue_expecting_Long()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        5, 0, 0, 0, 0, 0, 0, 0, 42 //value
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //value
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        0, 1, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleConstantValue( final Object value )
      {
        assertEquals( "value", 42L, value );
      }
    };

    parser.parseConstantValue( data, 0, constantPool );
  }

  public void test_parseConstantValue_expecting_Float()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        4, 0, 0, 0, 42 //value
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //value
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        0, 1, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleConstantValue( final Object value )
      {
        assertEquals( "value", Float.intBitsToFloat( 42 ), value );
      }
    };

    parser.parseConstantValue( data, 0, constantPool );
  }

  public void test_parseConstantValue_expecting_Integer()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        3, 0, 0, 0, 42 //value
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //value
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        0, 1, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleConstantValue( final Object value )
      {
        assertEquals( "value", 42, value );
      }
    };

    parser.parseConstantValue( data, 0, constantPool );
  }

  public void test_parseSourceFile()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        1, 0, 1, 'a', //klass
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //filename
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        0, 1, //filename
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleSourceFile( final String filename )
      {
        assertEquals( "filename", "a", filename );
      }
    };

    parser.parseSourceFile( data, 0, constantPool );
  }

  public void test_parseSourceDebug()
  {
    final byte[] data = new byte[]
      {
        'a'
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleSourceDebug( final String value )
      {
        assertEquals( "value", "a", value );
      }
    };

    parser.parseSourceDebug( data, 0, 1 );
  }

  public void test_parseEnclosingMethod()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        1, 0, 1, 'a', //klass
        1, 0, 1, 'b', //methodName
        1, 0, 1, 'c', //methodType
        7, 0, 1,//class to klass
        12, 0, 2, 0, 3,//NameType
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //klass
        4, //methodName
        8, //methodType
        12, //class to klass
        15, //NameType
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        0, 4, //klass
        0, 5, //NameType
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleEnclosingMethod( final String klass, final String methodName, final String methodType )
      {
        assertEquals( "klass", "a", klass );
        assertEquals( "methodName", "b", methodName );
        assertEquals( "methodType", "c", methodType );
      }
    };

    parser.parseEnclosingMethod( data, 0, constantPool );
  }

  public void test_parseEnclosingMethod_with_null_method()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        1, 0, 1, 'a', //klass
        7, 0, 1,//class to klass
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //klass
        4, //methodName
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        0, 2, //klass
        0, 0, //NameType
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleEnclosingMethod( final String klass, final String methodName, final String methodType )
      {
        assertEquals( "klass", "a", klass );
        assertEquals( "methodName", null, methodName );
        assertEquals( "methodType", null, methodType );
      }
    };

    parser.parseEnclosingMethod( data, 0, constantPool );
  }

  public void test_parseSignature()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        1, 0, 1, 'a', //innerClass

      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //signature
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        0, 1, //signature index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleSignature( final String signature )
      {
        assertEquals( "signature", "a", signature );
      }
    };

    parser.parseSignature( data, 0, constantPool );
  }

  public void test_parseInnerClassElement()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        1, 0, 1, 'a', //innerClass
        1, 0, 1, 'b', //outerClass
        1, 0, 1, 'c', //innerName
        7, 0, 1, //class to innerClass
        7, 0, 2, //class to outerClass

      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //innerClass
        4, //outerClass
        8, //innerName
        12, //class to innerClass
        15, //class to outerClass
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        0, 1, //count of inner classes
        0, 4, //innerClass index
        0, 5, //outerClass index
        0, 3, //name index
        0, 2, //access flags
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleInnerClass( final String innerClass,
                                       final String outerClass,
                                       final String innerName,
                                       final int innerClassAccessFlags )
      {
        assertEquals( "innerName", "c", innerName );
        assertEquals( "innerClassAccessFlags", 2, innerClassAccessFlags );
        assertEquals( "outerClass", "b", outerClass );
        assertEquals( "innerClass", "a", innerClass );
      }
    };

    parser.parseInnerClasses( data, 0, constantPool );
  }

  public void test_parseInnerClassElement_with_null_inner_class()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        1, 0, 1, 'a', //innerClass
        1, 0, 1, 'b', //outerClass
        1, 0, 1, 'c', //innerName
        7, 0, 1, //class to innerClass
        7, 0, 2, //class to outerClass

      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //innerClass
        4, //outerClass
        8, //innerName
        12, //class to innerClass
        15, //class to outerClass
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        0, 1, //count of inner classes
        0, 0, //innerClass index
        0, 5, //outerClass index
        0, 3, //name index
        0, 2, //access flags
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleInnerClass( final String innerClass,
                                       final String outerClass,
                                       final String innerName,
                                       final int innerClassAccessFlags )
      {
        assertEquals( "innerName", "c", innerName );
        assertEquals( "innerClassAccessFlags", 2, innerClassAccessFlags );
        assertEquals( "outerClass", "b", outerClass );
        assertEquals( "innerClass", null, innerClass );
      }
    };

    parser.parseInnerClasses( data, 0, constantPool );
  }

  public void test_parseInnerClassElement_with_null_outer_class()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        1, 0, 1, 'a', //innerClass
        1, 0, 1, 'b', //outerClass
        1, 0, 1, 'c', //innerName
        7, 0, 1, //class to innerClass
        7, 0, 2, //class to outerClass

      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //innerClass
        0, //outerClass
        8, //innerName
        12, //class to innerClass
        15, //class to outerClass
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        0, 1, //count of inner classes
        0, 4, //innerClass index
        0, 0, //outerClass index
        0, 3, //name index
        0, 2, //access flags
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleInnerClass( final String innerClass,
                                       final String outerClass,
                                       final String innerName,
                                       final int innerClassAccessFlags )
      {
        assertEquals( "innerName", "c", innerName );
        assertEquals( "innerClassAccessFlags", 2, innerClassAccessFlags );
        assertEquals( "outerClass", null, outerClass );
        assertEquals( "innerClass", "a", innerClass );
      }
    };

    parser.parseInnerClasses( data, 0, constantPool );
  }

  public void test_parseInnerClassElement_with_null_inner_name()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        1, 0, 1, 'a', //innerClass
        1, 0, 1, 'b', //outerClass
        1, 0, 1, 'c', //innerName
        7, 0, 1, //class to innerClass
        7, 0, 2, //class to outerClass

      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //innerClass
        4, //outerClass
        8, //innerName
        12, //class to innerClass
        15, //class to outerClass
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        0, 1, //count of inner classes
        0, 4, //innerClass index
        0, 5, //outerClass index
        0, 0, //name index
        0, 2, //access flags
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleInnerClass( final String innerClass,
                                       final String outerClass,
                                       final String innerName,
                                       final int innerClassAccessFlags )
      {
        assertEquals( "innerName", null, innerName );
        assertEquals( "innerClassAccessFlags", 2, innerClassAccessFlags );
        assertEquals( "outerClass", "b", outerClass );
        assertEquals( "innerClass", "a", innerClass );
      }
    };

    parser.parseInnerClasses( data, 0, constantPool );
  }

  public void test_parseDeprecated_withBadLength()
  {
    try
    {
      new ConcreteParser().parseDeprecated( new byte[0], 0, 1 );
      fail( "Expected to get throw an exception" );
    }
    catch( final ClassFormatError cfe )
    {
      final String message =
        "Deprecated attribute with non-zero length at position 0";
      assertEquals( "cfe.getMessage()", message, cfe.getMessage() );
    }
  }

  public void test_parseDeprecated()
  {
    final boolean[] success = new boolean[1];
    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleDeprecated()
      {
        success[0] = true;
      }
    };

    parser.parseDeprecated( new byte[0], 0, 0 );
    assertTrue( "deprecated", success[0] );
  }

  public void test_parseSynthetic_withBadLength()
  {
    try
    {
      new ConcreteParser().parseSynthetic( new byte[0], 0, 1 );
      fail( "Expected to get throw an exception" );
    }
    catch( final ClassFormatError cfe )
    {
      final String message =
        "Synthetic attribute with non-zero length at position 0";
      assertEquals( "cfe.getMessage()", message, cfe.getMessage() );
    }
  }

  public void test_parseSynthetic()
  {
    final boolean[] success = new boolean[1];
    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleSynthetic()
      {
        success[0] = true;
      }
    };

    parser.parseSynthetic( new byte[0], 0, 0 );
    assertTrue( "Synthetic", success[0] );
  }

  public void test_parseClassAttributes()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, 2b length, Nb data
        1, 0, 1, 'a', //name
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //name
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        0, 1, //count of attributes
        0, 1, //nameIndex
        0, 0, 0, 0, //length
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleClassAttribute( final String name,
                                           final byte[] p_data,
                                           final int offset,
                                           final long length,
                                           final ConstantPool p_constantPool )
      {
        assertEquals( "name", "a", name );
        assertEquals( "data", data, p_data );
        assertEquals( "offset", 8, offset );
        assertEquals( "length", 0L, length );
        assertEquals( "constantPool", constantPool, p_constantPool );
      }

    };

    parser.parseClassAttributes( data, 0, constantPool );
  }

  public void test_parseAttribute_Code()
  {
    verifyParseAttribute( ClassFileParser.AttributeType.CODE );
  }

  public void test_parseAttribute_Method()
  {
    verifyParseAttribute( ClassFileParser.AttributeType.METHOD );
  }

  public void test_parseAttribute_Field()
  {
    verifyParseAttribute( ClassFileParser.AttributeType.FIELD );
  }

  public void test_parseAttribute_Class()
  {
    verifyParseAttribute( ClassFileParser.AttributeType.CLASS );
  }

  private void verifyParseAttribute( final ClassFileParser.AttributeType type )
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, 2b length, Nb data
        1, 0, 1, 'a', //name
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //name
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        0, 1, //count of attributes
        0, 1, //nameIndex
        0, 0, 0, 0, //length
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleMethodAttribute( final String name,
                                            final byte[] p_data,
                                            final int offset,
                                            final long length,
                                            final ConstantPool p_constantPool )
      {
        verify( name, p_data, offset, length, p_constantPool );
      }

      protected void handleCodeAttribute( final String name,
                                          final byte[] p_data,
                                          final int offset,
                                          final long length,
                                          final ConstantPool p_constantPool )
      {
        verify( name, p_data, offset, length, p_constantPool );
      }

      protected void handleFieldAttribute( final String name,
                                           final byte[] p_data,
                                           final int offset,
                                           final long length,
                                           final ConstantPool p_constantPool )
      {
        verify( name, p_data, offset, length, p_constantPool );
      }

      protected void handleClassAttribute( final String name,
                                           final byte[] p_data,
                                           final int offset,
                                           final long length,
                                           final ConstantPool p_constantPool )
      {
        verify( name, p_data, offset, length, p_constantPool );
      }

      private void verify( final String name,
                           final byte[] p_data,
                           final int offset,
                           final long length,
                           final ConstantPool p_constantPool )
      {
        assertEquals( "name", "a", name );
        assertEquals( "data", data, p_data );
        assertEquals( "offset", 8, offset );
        assertEquals( "length", 0L, length );
        assertEquals( "constantPool", constantPool, p_constantPool );
      }
    };

    parser.parseAttributes( type, data, 0, constantPool );
  }

  public void test_parseField()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, 2b length, Nb data
        1, 0, 1, 'a', //name
        1, 0, 1, 'b', //descriptor

      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //name
        4, //descriptor
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        0, 1, //accessFlags
        0, 1, //nameIndex
        0, 2, //descriptorIndex
        0, 0, //attribute count
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleField( final String name,
                                  final String descriptor,
                                  final int accessFlags )
      {
        assertEquals( "name", "a", name );
        assertEquals( "descriptor", "b", descriptor );
        assertEquals( "accessFlags", 1, accessFlags );
      }
    };

    parser.parseField( data, 0, constantPool );
  }

  public void test_parseMethod()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, 2b length, Nb data
        1, 0, 1, 'a', //name
        1, 0, 1, 'b', //descriptor

      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //name
        4, //descriptor
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        0, 1, //accessFlags
        0, 1, //nameIndex
        0, 2, //descriptorIndex
        0, 0, //attribute count
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleMethod( final String name,
                                   final String descriptor,
                                   final int accessFlags )
      {
        assertEquals( "name", "a", name );
        assertEquals( "descriptor", "b", descriptor );
        assertEquals( "accessFlags", 1, accessFlags );
      }
    };

    parser.parseMethod( data, 0, constantPool );
  }

  public void test_handleInnerClass_throws_UnimplementedException()
  {
    final Class[] types = new Class[]{String.class, String.class, String.class, Integer.TYPE};
    final Object[] args = new Object[]{"", "", "", 0};
    verifyUnimplementedMethod( "handleInnerClass", types, args );
  }

  public void test_handleEnclosingMethod_throws_UnimplementedException()
  {
    final Class[] types = new Class[]{String.class, String.class, String.class};
    final Object[] args = new Object[]{"", "", ""};
    verifyUnimplementedMethod( "handleEnclosingMethod", types, args );
  }

  public void test_handleAnnotationEnumValue_throws_UnimplementedException()
  {
    final Class[] types = new Class[]{String.class, String.class, String.class};
    final Object[] args = new Object[]{"", "", ""};
    verifyUnimplementedMethod( "handleAnnotationEnumValue", types, args );
  }

  public void test_handleAnnotationValue_throws_UnimplementedException()
  {
    final Class[] types = new Class[]{String.class, Object.class};
    final Object[] args = new Object[]{"", 0};
    verifyUnimplementedMethod( "handleAnnotationValue", types, args );
  }

  public void test_startAnnotationValueArray_throws_UnimplementedException()
  {
    final Class[] types = new Class[]{String.class, Integer.TYPE};
    final Object[] args = new Object[]{"", 0};
    verifyUnimplementedMethod( "startAnnotationValueArray", types, args );
  }

  public void test_handleConstantValue_throws_UnimplementedException()
  {
    final Class[] types = new Class[]{Object.class};
    final Object[] args = new Object[]{""};
    verifyUnimplementedMethod( "handleConstantValue", types, args );
  }

  public void test_handleExceptions_throws_UnimplementedException()
  {
    final Class[] types = new Class[]{String[].class};
    final Object[] args = new Object[]{new String[0]};
    verifyUnimplementedMethod( "handleExceptions", types, args );
  }

  public void test_startCode_throws_UnimplementedException()
  {
    final Class[] types =
      new Class[]{Integer.TYPE, Integer.TYPE, byte[].class, Integer.TYPE, Long.TYPE};
    final Object[] args = new Object[]{0, 0, new byte[0], 0, 0L};
    verifyUnimplementedMethod( "startCode", types, args );
  }

  public void test_handleExceptionHandler_throws_UnimplementedException()
  {
    final Class[] types = new Class[]{Integer.TYPE, Integer.TYPE, Integer.TYPE, String.class};
    final Object[] args = new Object[]{0, 0, 0, ""};
    verifyUnimplementedMethod( "handleExceptionHandler", types, args );
  }

  public void test_handleSignature_throws_UnimplementedException()
  {
    verifyHandleStringArg( "handleSignature" );
  }

  public void test_handleSourceFile_throws_UnimplementedException()
  {
    verifyHandleStringArg( "handleSourceFile" );
  }

  public void test_handleSourceDebug_throws_UnimplementedException()
  {
    verifyHandleStringArg( "handleSourceDebug" );
  }

  public void test_handleAnnotationGroup_throws_UnimplementedException()
  {
    verifyHandleStringArg( "handleAnnotationGroup" );
  }

  public void test_startAnnotation_throws_UnimplementedException()
  {
    verifyHandleStringArg( "startAnnotation" );
  }

  private void verifyHandleStringArg( final String type )
  {
    verifyUnimplementedMethod( type, new Class[]{String.class}, new Object[]{""} );
  }

  public void test_handleDeprecated_throws_UnimplementedException()
  {
    verifyHandleNoArg( "handleDeprecated" );
  }

  public void test_handleSynthetic_throws_UnimplementedException()
  {
    verifyHandleNoArg( "handleSynthetic" );
  }

  public void test_endAnnotation_throws_UnimplementedException()
  {
    verifyHandleNoArg( "endAnnotation" );
  }

  public void test_endAnnotationValueArray_throws_UnimplementedException()
  {
    verifyHandleNoArg( "endAnnotationValueArray" );
  }

  public void test_endCode_throws_UnimplementedException()
  {
    verifyHandleNoArg( "endCode" );
  }

  private void verifyHandleNoArg( final String type )
  {
    verifyUnimplementedMethod( type, new Class[0], new Object[0] );
  }

  public void test_handleClassAttribute_throws_UnimplementedException()
  {
    verifyHandleAttribute( "Class" );
  }

  public void test_handleCodeAttribute_throws_UnimplementedException()
  {
    verifyHandleAttribute( "Code" );
  }

  public void test_handleMethodAttribute_throws_UnimplementedException()
  {
    verifyHandleAttribute( "Method" );
  }

  public void test_handleFieldAttribute_throws_UnimplementedException()
  {
    verifyHandleAttribute( "Field" );
  }

  private void verifyHandleAttribute( final String type )
  {
    final String name = "handle" + type + "Attribute";
    final Class[] types =
      new Class[]{String.class, byte[].class, Integer.TYPE, Long.TYPE, ConstantPool.class};
    final Object[] args =
      new Object[]{"foo", new byte[0], 0, 0L, new ConstantPool( new byte[0], new int[0] )};
    verifyUnimplementedMethod( name, types, args );
  }

  public void test_handleField_throws_UnimplementedException()
  {
    verifyHandleFieldOrMethod( "handleField" );
  }

  public void test_handleMethod_throws_UnimplementedException()
  {
    verifyHandleFieldOrMethod( "handleMethod" );
  }

  private void verifyHandleFieldOrMethod( final String name )
  {
    final Class[] types = new Class[]{String.class, String.class, Integer.TYPE};
    final Object[] args = new Object[]{"foo", "bar", 3};
    verifyUnimplementedMethod( name, types, args );
  }

  private void verifyUnimplementedMethod( final String name,
                                          final Class[] types,
                                          final Object[] args )
  {
    try
    {
      final Method method = ClassFileParser.class.getDeclaredMethod( name, types );
      method.invoke( new ConcreteParser(), args );
      fail( "Expected to get an exception when invoking " + name );
    }
    catch( final InvocationTargetException ite )
    {
      if( !( ite.getTargetException() instanceof IllegalStateException ) )
      {
        fail( "Expected exception to be of type IllegalStateException when invoking " + name );
      }
      final IllegalStateException ise = (IllegalStateException)ite.getTargetException();
      assertEquals( "ise.getMessage()", "Unimplemented handler invoked.", ise.getMessage() );
    }
    catch( final Throwable t )
    {
      fail( "Unexpected exception thrown when looking up method " + name + " " + t );
    }
  }
}
