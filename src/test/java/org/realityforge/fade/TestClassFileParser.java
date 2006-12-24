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

  boolean started;
  boolean done;

  public void test_parseAnnotations()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        1, 0, 1, 'a',
        1, 0, 1, 'b',
        1, 0, 1, 'c',
        1, 0, 1, 'd',
        1, 0, 1, 'e',
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0,
        4,
        8, 
        12,
        16,
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        0, 1, // 1 annotation in group
        0, 1, // annotation name index
        0, 1, // number of element-value pairs in annotation
           0, 2, // name index for for pair
          '[', 0, 1, //array of size 1
             '@',  // Annotation tag
             0, 3, // annotation name index
             0, 1, // count of element-value pairs
                0, 4, // name index for first in nested annotation
                's', 0, 5, // value index of str value
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleAnnotationGroup( final String type )
      {
        assertEquals( "name", ClassFileFormat.ATTR_RuntimeVisibleAnnotations, type );
      }

      int depth;

      protected void startAnnotation( final String type )
      {
        depth++;
        if( 1 == depth )
        {
          assertEquals( "type", "a", type );
        }
        else
        {
          assertEquals( "type", "c", type );
        }
      }

      protected void endAnnotationValueAnnotation()
      {
      }

      protected void startAnnotationValueAnnotation( final String name )
      {
        assertEquals( "name", null, name );
      }

      protected void endAnnotation()
      {
      }

      protected void startAnnotationValueArray( final String name, final int length )
      {
        assertEquals( "name", "b", name );
        assertEquals( "length", 1, length );
      }

      protected void endAnnotationValueArray()
      {         
      }

      protected void handleAnnotationValue( final String name, final Object value )
      {
        assertEquals( "name", "d", name );
        assertEquals( "value", "e", value );
      }
    };

    parser.parseAnnotations( ClassFileFormat.ATTR_RuntimeVisibleAnnotations, data, 0, constantPool );
  }

  public void test_parseElementValue_with_bad_array()
  {
    final ConstantPool constantPool = new ConstantPool( new byte[0], new int[0] );
    final byte[] data = new byte[]
      {
        '[', 0, 1, //array of size 1
        42, //bad type!
      };

    try
    {
      new ConcreteParser().parseElementValue( "han", data, 0, constantPool );
      fail( "expected an exception" );
    }
    catch( final ClassFormatError cfe )
    {
      final String message = "Unexpected array type " + (char) 42 + " at position " + 3;
      assertEquals( "cfe.getMessage()", message, cfe.getMessage() );
    }
  }

  public void test_parseElementValue_expecting_Enum_Array()
  {
    final byte[] cpData = new byte[]
      {
        1, 0, 1, 'a', //key
        1, 0, 1, 'b', //value
        1, 0, 1, 'c', //key
        1, 0, 1, 'd', //value
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //key 1
        4, //value 1
        8, //key 2
        12, //value 2
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        '[', 0, 2, //array of size 2
        //Enum 1
        'e',
        0, 1, //key index
        0, 2, //value index
        //Enum 2
        'e',
        0, 3, //key index
        0, 4, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void startAnnotationValueArray( final String name, final int length )
      {
        assertFalse( "started in startAnnotationValueArray", started );
        started = true;
        assertEquals( "name", "han", name );
        assertEquals( "length", 2, length );
        done = true;
      }

      protected void endAnnotationValueArray()
      {
        assertTrue( "started in endAnnotationValueArray", started );
        started = false;
      }

      int count;
      protected void handleAnnotationEnumValue( final String name, final String key, final String value )
      {
        assertEquals( "name", null, name );
        count++;
        if( 1 == count )
        {
          assertEquals( "value", "a", key );
          assertEquals( "value", "b", value );
        }
        else
        {
          assertEquals( "value", "c", key );
          assertEquals( "value", "d", value );
        }
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
  }

  public void test_parseElementValue_expecting_Zero_Sized_array()
  {
    final ConstantPool constantPool = new ConstantPool( new byte[0], new int[0] );
    final byte[] data = new byte[]
      {
        '[', 0, 0, //array of size 0
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void startAnnotationValueArray( final String name, final int length )
      {
        assertFalse( "started in startAnnotationValueArray", started );
        started = true;
        assertEquals( "name", "han", name );
        assertEquals( "length", 0, length );
        done = true;
      }

      protected void endAnnotationValueArray()
      {
        assertTrue( "started in endAnnotationValueArray", started );
        started = false;
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
    assertEquals( "done", true, done );
    assertEquals( "started", false, started );
  }

  public void test_parseElementValue_expecting_Boolean_Array_in_Array()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        3, 0, 0, 0, 1, //value
        3, 0, 0, 0, 0, //value
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //value
        5, //value
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        '[', 0, 1, //array of size 2
        '[', 0, 2, //array of size 2
          'Z', 0, 1, //value index
          'Z', 0, 2, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void startAnnotationValueArray( final String name, final int length )
      {
        assertFalse( "started in startAnnotationValueArray", started );
        started = true;
        assertEquals( "name", "han", name );
        assertEquals( "length", 1, length );
        done = true;
      }

      protected void endAnnotationValueArray()
      {
        assertTrue( "started in endAnnotationValueArray", started );
        started = false;
      }

      protected void handleAnnotationValue( final String name, final Object value )
      {
        assertTrue( "started in endAnnotationValueArray", started );
        assertEquals( "name", null, name );
        assertTrue( "value instanceof", value instanceof boolean[] );
        assertEquals( "value.length", 2, ((boolean[]) value).length );
        assertEquals( "value[0]", (boolean)Boolean.TRUE, ((boolean[]) value)[0] );
        assertEquals( "value[1]", (boolean)Boolean.FALSE, ((boolean[]) value)[1] );
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
    assertEquals( "done", true, done );
    assertEquals( "started", false, started );
  }

  public void test_parseElementValue_expecting_Boolean_Array()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        3, 0, 0, 0, 1, //value
        3, 0, 0, 0, 0, //value
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //value
        5, //value
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        '[', 0, 2, //array of size 2
          'Z', 0, 1, //value index
          'Z', 0, 2, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleAnnotationValue( final String name, final Object value )
      {
        assertEquals( "name", "han", name );
        assertTrue( "value instanceof", value instanceof boolean[] );
        assertEquals( "value.length", 2, ((boolean[]) value).length );
        assertEquals( "value[0]", (boolean)Boolean.TRUE, ((boolean[]) value)[0] );
        assertEquals( "value[1]", (boolean)Boolean.FALSE, ((boolean[]) value)[1] );
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
  }

  public void test_parseElementValue_expecting_Byte_Array()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        3, 0, 0, 0, 1, //value
        3, 0, 0, 0, 0, //value
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //value
        5, //value
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        '[', 0, 2, //array of size 2
          'B', 0, 1, //value
          'B', 0, 2, //value
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleAnnotationValue( final String name, final Object value )
      {
        assertEquals( "name", "han", name );
        assertTrue( "value instanceof", value instanceof byte[] );
        assertEquals( "value.length", 2, ((byte[]) value).length );
        assertEquals( "value[0]", (byte)1, ((byte[]) value)[0] );
        assertEquals( "value[1]", (byte)0, ((byte[]) value)[1] );
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
  }

  public void test_parseElementValue_expecting_Char_Array()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        3, 0, 0, 0, 'a', //value
        3, 0, 0, 0, 'b', //value
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //value
        5, //value
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        '[', 0, 2, //array of size 2
          'C', 0, 1, //value index
          'C', 0, 2, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleAnnotationValue( final String name, final Object value )
      {
        assertEquals( "name", "han", name );
        assertTrue( "value instanceof", value instanceof char[] );
        assertEquals( "value.length", 2, ((char[]) value).length );
        assertEquals( "value[0]", 'a', ((char[]) value)[0] );
        assertEquals( "value[1]", 'b', ((char[]) value)[1] );
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
  }

  public void test_parseElementValue_expecting_Short_Array()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        3, 0, 0, 0, 1, //value
        3, 0, 0, 0, 0, //value
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //value
        5, //value
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        '[', 0, 2, //array of size 2
          'S', 0, 1, //value index
          'S', 0, 2, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleAnnotationValue( final String name, final Object value )
      {
        assertEquals( "name", "han", name );
        assertTrue( "value instanceof", value instanceof short[] );
        assertEquals( "value.length", 2, ((short[]) value).length );
        assertEquals( "value[0]", (short)1, ((short[]) value)[0] );
        assertEquals( "value[1]", (short)0, ((short[]) value)[1] );
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
  }

  public void test_parseElementValue_expecting_Integer_Array()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        3, 0, 0, 0, 1, //value
        3, 0, 0, 0, 0, //value
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //value
        5, //value
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        '[', 0, 2, //array of size 2
          'I', 0, 1, //value index
          'I', 0, 2, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleAnnotationValue( final String name, final Object value )
      {
        assertEquals( "name", "han", name );
        assertTrue( "value instanceof", value instanceof int[] );
        assertEquals( "value.length", 2, ((int[]) value).length );
        assertEquals( "value[0]", 1, ((int[]) value)[0] );
        assertEquals( "value[1]", 0, ((int[]) value)[1] );
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
  }

  public void test_parseElementValue_expecting_Float_Array()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        4, 0, 0, 0, 1, //value
        4, 0, 0, 0, 0, //value
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //value
        5, //value
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        '[', 0, 2, //array of size 2
          'F', 0, 1, //value index
          'F', 0, 2, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleAnnotationValue( final String name, final Object value )
      {
        assertEquals( "name", "han", name );
        assertTrue( "value instanceof", value instanceof float[] );
        assertEquals( "value.length", 2, ((float[]) value).length );
        assertEquals( "value[0]", Float.intBitsToFloat( 1 ), ((float[]) value)[0] );
        assertEquals( "value[1]", Float.intBitsToFloat( 0 ), ((float[]) value)[1] );
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
  }

  public void test_parseElementValue_expecting_Long_Array()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        5, 0, 0, 0, 0, 0, 0, 0, 42, //value
        5, 0, 0, 0, 0, 0, 0, 0, 16, //value
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //value
        9, //value
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        '[', 0, 2, //array of size 2
          'J', 0, 1, //value index
          'J', 0, 2, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleAnnotationValue( final String name, final Object value )
      {
        assertEquals( "name", "han", name );
        assertTrue( "value instanceof", value instanceof long[] );
        assertEquals( "value.length", 2, ((long[]) value).length );
        assertEquals( "value[0]", 42L, ((long[]) value)[0] );
        assertEquals( "value[1]", 16L, ((long[]) value)[1] );
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
  }

  public void test_parseElementValue_expecting_Double_Array()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        6, 0, 0, 0, 0, 0, 0, 0, 42, //value
        6, 0, 0, 0, 0, 0, 0, 0, 16, //value
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //value
        9, //value
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        '[', 0, 2, //array of size 2
          'D', 0, 1, //value index
          'D', 0, 2, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleAnnotationValue( final String name, final Object value )
      {
        assertEquals( "name", "han", name );
        assertTrue( "value instanceof", value instanceof double[] );
        assertEquals( "value.length", 2, ((double[]) value).length );
        assertEquals( "value[0]", Double.longBitsToDouble( 42L ), ((double[]) value)[0] );
        assertEquals( "value[1]", Double.longBitsToDouble( 16L ), ((double[]) value)[1] );
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
  }

  public void test_parseElementValue_expecting_String_Array()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        1, 0, 1, 'a', //value
        1, 0, 1, 'b', //value
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //value
        4, //value
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        '[', 0, 2, //array of size 2
          's', 0, 1, //value index
          's', 0, 2, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleAnnotationValue( final String name, final Object value )
      {
        assertEquals( "name", "han", name );
        assertTrue( "value instanceof", value instanceof String[] );
        assertEquals( "value.length", 2, ((String[]) value).length );
        assertEquals( "value[0]", "a", ((String[]) value)[0] );
        assertEquals( "value[1]", "b", ((String[]) value)[1] );
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
  }

  public void test_parseElementValue_expecting_Class_Array()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        1, 0, 1, 'a', //value
        1, 0, 1, 'b', //value
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //value 1
        4, //value 2
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        '[', 0, 2, //array of size 2
          'c', 0, 1, //value index
          'c', 0, 2, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleAnnotationValue( final String name, final Object value )
      {
        assertEquals( "name", "han", name );
        assertTrue( "value instanceof", value instanceof String[] );
        assertEquals( "value.length", 2, ((String[]) value).length );
        assertEquals( "value[0]", "a", ((String[]) value)[0] );
        assertEquals( "value[1]", "b", ((String[]) value)[1] );
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
  }

  public void test_parseElementValue_expecting_Boolean()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        3, 0, 0, 0, 1 //value
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //value
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        'Z', 0, 1, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleAnnotationValue( final String name, final Object value )
      {
        assertEquals( "name", "han", name );
        assertEquals( "value", Boolean.TRUE, value );
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
  }

  public void test_parseElementValue_expecting_Boolean_False()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        3, 0, 0, 0, 0 //value
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //value
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        'Z', 0, 1, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleAnnotationValue( final String name, final Object value )
      {
        assertEquals( "name", "han", name );
        assertEquals( "value", Boolean.FALSE, value );
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
  }

  public void test_parseElementValue_expecting_byte()
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
        'B', 0, 1, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleAnnotationValue( final String name, final Object value )
      {
        assertEquals( "name", "han", name );
        assertEquals( "value", ( (byte)42 ), value );
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
  }

  public void test_parseElementValue_expecting_Char()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        3, 0, 0, 0, 'a' //value
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //value
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        'C', 0, 1, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleAnnotationValue( final String name, final Object value )
      {
        assertEquals( "name", "han", name );
        assertEquals( "value", 'a', value );
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
  }

  public void test_parseElementValue_expecting_Short()
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
        'S', 0, 1, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleAnnotationValue( final String name, final Object value )
      {
        assertEquals( "name", "han", name );
        assertEquals( "value", ( (short)42 ), value );
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
  }

  public void test_parseElementValue_expecting_Integer()
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
        'I', 0, 1, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleAnnotationValue( final String name, final Object value )
      {
        assertEquals( "name", "han", name );
        assertEquals( "value", 42, value );
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
  }

  public void test_parseElementValue_expecting_Float()
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
        'F', 0, 1, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleAnnotationValue( final String name, final Object value )
      {
        assertEquals( "name", "han", name );
        assertEquals( "value", Float.intBitsToFloat( 42 ), value );
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
  }

  public void test_parseElementValue_expecting_Long()
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
        'J', 0, 1, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleAnnotationValue( final String name, final Object value )
      {
        assertEquals( "name", "han", name );
        assertEquals( "value", 42L, value );
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
  }

  public void test_parseElementValue_expecting_Double()
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
        'D', 0, 1, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleAnnotationValue( final String name, final Object value )
      {
        assertEquals( "name", "han", name );
        assertEquals( "value", Double.longBitsToDouble( 42L ), value );
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
  }

  public void test_parseElementValue_expecting_UTF()
  {
    final byte[] cpData = new byte[]
      {
        1, 0, 1, 'a', //exception 1
        8, 0, 1, //string to exception 1
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //value
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        's', 0, 1, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleAnnotationValue( final String name, final Object value )
      {
        assertEquals( "name", "han", name );
        assertEquals( "value", "a", value );
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
  }

  public void test_parseElementValue_expecting_Class()
  {
    final byte[] cpData = new byte[]
      {
        1, 0, 1, 'a', //exception 1
        7, 0, 1, //class to exception 1
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //value
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        'c', 0, 1, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleAnnotationValue( final String name, final Object value )
      {
        assertEquals( "name", "han", name );
        assertEquals( "value", "a", value );
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
  }

  public void test_parseElementValue_expecting_Enum()
  {
    final byte[] cpData = new byte[]
      {
        1, 0, 1, 'a', //key
        1, 0, 1, 'b', //value
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //value
        4, //value
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        'e',
        0, 1, //key index
        0, 2, //value index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleAnnotationEnumValue( final String name, final String key, final String value )
      {
        assertEquals( "name", "han", name );
        assertEquals( "value", "a", key );
        assertEquals( "value", "b", value );
      }
    };

    final int location = parser.parseElementValue( "han", data, 0, constantPool );
    assertEquals( "location", data.length, location );
  }

  public void test_parseElementValue_with_bad_tag()
  {
    final ConstantPool constantPool = new ConstantPool( new byte[0], new int[0] );
    final byte[] data = new byte[]{42};

    try
    {
      new ConcreteParser().parseElementValue( "han", data, 0, constantPool );
      fail( "Expected exception" );
    }
    catch( final ClassFormatError cfe )
    {
      final String message =
        "Unknown attribute tag (" + (char)42 + ") at position 1";
      assertEquals( "cfe.getMessage()", message, cfe.getMessage() );
    }
  }

  public void test_checkTag_bad_throws_exception()
  {
    try
    {
      new ConcreteParser().checkTag( (byte)0, new byte[]{1}, 0 );
      fail( "expected to throw an exception" );
    }
    catch( final ClassFormatError cfe )
    {
      final String message =
        "Bad type for array tag in annotation at position 0. Expected: 0 Actual: 1";
      assertEquals( "cfe.getMessage()", message, cfe.getMessage() );
    }
  }

  public void test_checkTag_ok()
  {
    new ConcreteParser().checkTag( (byte)'a', new byte[]{'a'}, 0 );
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

  public void test_parseCode()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        1, 0, 1, 'a', //exception 1
        7, 0, 1, //class to exception 1
        1, 0, 1, 'b', //attribute name
        1, 0, 1, 'c', //attribute name
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //exception 1
        4, //exception 2
        7, //attribute name
        11, //attribute name
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        0, 2, //maxStack
        0, 3, //maxLocals
        0, 0, 0, 1, //codeLength
        0, //code
        0, 2, //exception handler count
        //exception handler 1
        0, 1, //start pc
        0, 2, //end pc
        0, 3, //handler pc
        0, 0, //catchTypeIndex
        //exception handler 2
        0, 1, //start pc
        0, 2, //end pc
        0, 3, //handler pc
        0, 2, //catchTypeIndex
        0, 2, //attribute count
        0, 3, //attribute name index
        0, 0, 0, 1, //attribute length
        0, //attribute data
        0, 4, //attribute name index
        0, 0, 0, 0, //attribute length
      };

    /*
final int nameIndex = IOUtil.readUnsignedShort( data, location );
      final String name = constantPool.getUtfEntry( nameIndex );
      final long length = IOUtil.readUnsignedInteger( data, location + 2 );
      location += 6;
      handleCodeAttribute( name, data, location, length, constantPool );

    */

    final ConcreteParser parser = new ConcreteParser()
    {
      boolean codeStarted;
      int exception;
      int attribute;

      protected void startCode(final int maxStack,
                               final int maxLocals,
                               final byte[] p_data,
                               final int offset,
                               final long codeLength,
                               final ConstantPool constantPool)
      {
        assertEquals( "maxStack", 2, maxStack );
        assertEquals( "maxLocals", 3, maxLocals );
        assertEquals( "data", data, p_data );
        assertEquals( "offset", 8, offset );
        assertEquals( "codeLength", 1, codeLength );
        assertFalse( "codeStarted in startCode", codeStarted );
        codeStarted = true;
      }

      protected void handleExceptionHandler( final int startPC,
                                             final int endPC,
                                             final int handlerPC,
                                             final String catchType )
      {
        exception ++;
        assertTrue( "codeStarted in handleExceptionHandler " + exception, codeStarted );
        if( 1 == exception )
        {
          assertNull( "catchType", catchType );
        }
        else
        {
          assertNotNull( "catchType", catchType );
        }
        assertEquals( "startPC", 1, startPC );
        assertEquals( "endPC", 2, endPC );
        assertEquals( "handlerPC", 3, handlerPC );
      }

      protected void handleCodeAttribute( final String name,
                                          final byte[] p_data,
                                          final int offset,
                                          final long length,
                                          final ConstantPool p_constantPool )
      {
        attribute++;
        assertTrue( "codeStarted in handleCodeAttribute " + attribute, codeStarted );
        if( 1 == attribute )
        {
          assertEquals( "name", "b", name );
          assertEquals( "data", data, p_data );
          assertEquals( "offset", 35, offset );
          assertEquals( "length", 1, length );
          assertEquals( "constantPool", constantPool, p_constantPool );
        }
        else
        {
          assertEquals( "name", "c", name );
          assertEquals( "data", data, p_data );
          assertEquals( "offset", 42, offset );
          assertEquals( "length", 0, length );
          assertEquals( "constantPool", constantPool, p_constantPool );
        }
      }

      protected void endCode()
      {
        assertTrue( "codeStarted in endCode", codeStarted );
        codeStarted = false;
      }
    };

    parser.parseCode( data, 0, constantPool );
  }

  public void test_parseExceptions()
  {
    final byte[] cpData = new byte[]
      {
        //1b tag, Nb data
        1, 0, 1, 'a', //exception 1
        1, 0, 1, 'b', //exception 2
        7, 0, 1, //class to exception 1
        7, 0, 2, //class to exception 2
      };
    final int[] offsets = new int[]
      {
        0, //ignored
        0, //exception 1
        4, //exception 2
        8, //class to exception 1
        11, //class to exception 2
      };
    final ConstantPool constantPool = new ConstantPool( cpData, offsets );
    final byte[] data = new byte[]
      {
        0, 2, //count
        0, 3, //exception 1 index
        0, 4, //exception 2 index
      };

    final ConcreteParser parser = new ConcreteParser()
    {
      protected void handleExceptions( final String[] exceptions )
      {
        assertEquals( "exceptions.length", 2, exceptions.length );
        assertEquals( "exceptions[0]", "a", exceptions[0] );
        assertEquals( "exceptions[1]", "b", exceptions[1] );
      }
    };

    parser.parseExceptions( data, 0, constantPool );
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
      new Class[]{Integer.TYPE, Integer.TYPE, byte[].class, Integer.TYPE, Long.TYPE,ConstantPool.class};
    final Object[] args = new Object[]{0, 0, new byte[0], 0, 0L,null};
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

  public void test_startAnnotationValueAnnotation_throws_UnimplementedException()
  {
    verifyHandleStringArg( "startAnnotationValueAnnotation" );
  }                                                

  private void verifyHandleStringArg( final String type )
  {
    verifyUnimplementedMethod( type, new Class[]{String.class}, new Object[]{""} );
  }

  public void test_endAnnotationValueAnnotation_throws_UnimplementedException()
  {
    verifyHandleNoArg( "endAnnotationValueAnnotation" );
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
