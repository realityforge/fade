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

    final String[] r_name = new String[1];
    final String[] r_descriptor = new String[1];
    final int[] r_accessFlags = new int[1];

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleField( final String name,
                                  final String descriptor,
                                  final int accessFlags )
      {
        r_name[0] = name;
        r_descriptor[0] = descriptor;
        r_accessFlags[0] = accessFlags;
      }
    };

    parser.parseField( data, 0, constantPool );
    assertEquals( "name", "a", r_name[0] );
    assertEquals( "descriptor", "b", r_descriptor[0] );
    assertEquals( "accessFlags", 1, r_accessFlags[0] );
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

    final String[] r_name = new String[1];
    final String[] r_descriptor = new String[1];
    final int[] r_accessFlags = new int[1];

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleMethod( final String name,
                                   final String descriptor,
                                   final int accessFlags )
      {
        r_name[0] = name;
        r_descriptor[0] = descriptor;
        r_accessFlags[0] = accessFlags;
      }
    };

    parser.parseMethod( data, 0, constantPool );
    assertEquals( "name", "a", r_name[0] );
    assertEquals( "descriptor", "b", r_descriptor[0] );
    assertEquals( "accessFlags", 1, r_accessFlags[0] );
  }

  public void test_handleInnerClass_throws_UnimplementedException()
  {
    final Class[] types = new Class[]{String.class,String.class,String.class,Integer.TYPE};
    final Object[] args = new Object[]{"","","",0};
    verifyUnimplementedMethod( "handleInnerClass", types, args );
  }

  public void test_handleEnclosingMethod_throws_UnimplementedException()
  {
    final Class[] types = new Class[]{String.class,String.class,String.class};
    final Object[] args = new Object[]{"","",""};
    verifyUnimplementedMethod( "handleEnclosingMethod", types, args );
  }

  public void test_handleAnnotationEnumValue_throws_UnimplementedException()
  {
    final Class[] types = new Class[]{String.class,String.class,String.class};
    final Object[] args = new Object[]{"","",""};
    verifyUnimplementedMethod( "handleAnnotationEnumValue", types, args );
  }

  public void test_handleAnnotationValue_throws_UnimplementedException()
  {
    final Class[] types = new Class[]{String.class,Object.class};
    final Object[] args = new Object[]{"",0};
    verifyUnimplementedMethod( "handleAnnotationValue", types, args );
  }

  public void test_startAnnotationValueArray_throws_UnimplementedException()
  {
    final Class[] types = new Class[]{String.class,Integer.TYPE};
    final Object[] args = new Object[]{"",0};
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
