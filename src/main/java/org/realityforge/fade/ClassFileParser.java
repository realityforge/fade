package org.realityforge.fade;

/** Class for parsing class file. */
public abstract class ClassFileParser
{
  private static enum AttributeType
  {
    CLASS, METHOD, FIELD, CODE
  }

  final void parseClassAttributes( final byte[] data,
                                             final int offset,
                                             final ConstantPool constantPool )
  {
    parseAttributes( AttributeType.CLASS, data, offset, constantPool );
  }

  final void parseField( final byte[] data,
                                   final int offset,
                                   final ConstantPool constantPool )
  {
    final int accessFlags = IOUtil.readUnsignedShort( data, offset );
    final int nameIndex = IOUtil.readUnsignedShort( data, offset + 2 );
    final int descriptorIndex = IOUtil.readUnsignedShort( data, offset + 4 );
    final String name = constantPool.getUtfEntry( nameIndex );
    final String descriptor = constantPool.getUtfEntry( descriptorIndex );
    handleField( name, descriptor, accessFlags );
    parseAttributes( AttributeType.FIELD, data, offset + 6, constantPool );
  }

  final void parseMethod( final byte[] data,
                                    final int offset,
                                    final ConstantPool constantPool )
  {
    final int accessFlags = IOUtil.readUnsignedShort( data, offset );
    final int nameIndex = IOUtil.readUnsignedShort( data, offset + 2 );
    final int descriptorIndex = IOUtil.readUnsignedShort( data, offset + 4 );
    final String name = constantPool.getUtfEntry( nameIndex );
    final String descriptor = constantPool.getUtfEntry( descriptorIndex );
    handleMethod( name, descriptor, accessFlags );
    parseAttributes( AttributeType.METHOD, data, offset + 6, constantPool );
  }

  private void parseAttributes( final AttributeType type,
                                final byte[] data,
                                final int baseOffset,
                                final ConstantPool constantPool )
  {
    final int count = IOUtil.readUnsignedShort( data, baseOffset );
    int offset = baseOffset + 2;
    for( int i = 0; i < count; i++ )
    {
      final int nameIndex = IOUtil.readUnsignedShort( data, offset );
      final String name = constantPool.getUtfEntry( nameIndex );
      final long length = IOUtil.readUnsignedInteger( data, offset + 2 );
      offset += 6;
      switch( type )
      {
        case CLASS:
          handleClassAttribute( name, data, offset, length, constantPool );
          break;
        case METHOD:
          handleMethodAttribute( name, data, offset, length, constantPool );
          break;
        case FIELD:
          handleFieldAttribute( name, data, offset, length, constantPool );
          break;
        case CODE:
          handleCodeAttribute( name, data, offset, length, constantPool );
          break;
      }
      offset += length;
    }
  }

  protected final void parseCode( final byte[] data,
                                  final int offset,
                                  final ConstantPool constantPool )
  {
    final int maxStack = IOUtil.readUnsignedShort( data, offset );
    final int maxLocals = IOUtil.readUnsignedShort( data, offset + 2 );
    final long codeLength = IOUtil.readUnsignedInteger( data, offset + 4 );
    startCode( maxStack, maxLocals, data, offset + 8, codeLength );
    int location = (int)( offset + codeLength + 8 );
    final int exceptionHandlerCount = IOUtil.readUnsignedShort( data, location );
    location += 2;
    for( int i = 0; i < exceptionHandlerCount; i++ )
    {
      final int startPC = IOUtil.readUnsignedShort( data, location );
      final int endPC = IOUtil.readUnsignedShort( data, location + 2 );
      final int handlerPC = IOUtil.readUnsignedShort( data, location + 4 );
      final int catchTypeIndex = IOUtil.readUnsignedShort( data, location + 6 );
      final String catchType;
      if( 0 != catchTypeIndex )
      {
        catchType = constantPool.getClassEntry( catchTypeIndex );
      }
      else
      {
        catchType = null;
      }
      handleExceptionHandler( startPC, endPC, handlerPC, catchType );
      location += 8;
    }

    final int attributeCount = IOUtil.readUnsignedShort( data, location );
    location += 2;
    for( int i = 0; i < attributeCount; i++ )
    {
      final int nameIndex = IOUtil.readUnsignedShort( data, location );
      final String name = constantPool.getUtfEntry( nameIndex );
      final long length = IOUtil.readUnsignedInteger( data, location + 2 );
      location += 6;
      handleCodeAttribute( name, data, location, length, constantPool );
      location += length;
    }
    endCode();
  }

  protected final void parseExceptions( final byte[] data,
                                        final int offset,
                                        final ConstantPool constantPool )
  {
    final int count = IOUtil.readUnsignedShort( data, offset );
    final String[] exceptions = new String[count];
    int location = offset + 2;
    for( int i = 0; i < exceptions.length; i++ )
    {
      final int index = IOUtil.readUnsignedShort( data, location );
      exceptions[i] = constantPool.getClassEntry( index );
      location += 2;
    }
    handleExceptions( exceptions );
  }

  protected final void parseDeprecated( final byte[] data,
                                        final int offset,
                                        final long length )
  {
    if( 0 != length )
    {
      final String message =
        "Deprecated attribute with non-zero length at position " + offset;
      throw new ClassFormatError( message );
    }
    handleDeprecated();
  }

  protected final void parseSynthetic( final byte[] data,
                                       final int offset,
                                       final long length )
  {
    if( 0 != length )
    {
      final String message =
        "Synthetic attribute with non-zero length at position " + offset;
      throw new ClassFormatError( message );
    }
    handleSynthetic();
  }

  protected final void parseConstantValue( final byte[] data,
                                           final int offset,
                                           final ConstantPool constantPool )
  {
    final int index = IOUtil.readUnsignedShort( data, offset );
    final byte type = constantPool.getEntryType( index );
    final Object value;
    switch( type )
    {
      case ClassFileFormat.CONSTANT_Long:
        value = constantPool.getLongEntry( index );
        break;
      case ClassFileFormat.CONSTANT_Float:
        value = constantPool.getFloatEntry( index );
        break;
      case ClassFileFormat.CONSTANT_Double:
        value = constantPool.getDoubleEntry( index );
        break;
      case ClassFileFormat.CONSTANT_Integer:
        value = constantPool.getIntegerEntry( index );
        break;
      case ClassFileFormat.CONSTANT_String:
        value = constantPool.getStringEntry( index );
        break;
      default:
        final String message =
          "ConstantValue attribute at " + offset + " references constant pool value of type " + type;
        throw new ClassFormatError( message );
    }
    handleConstantValue( value );
  }

  protected final void parseSourceDebug( final byte[] data,
                                         final int offset,
                                         final long length )
  {
    final String value = IOUtil.parseUtfString( data, offset, -1, (int)length );
    handleSourceDebug( value );
  }

  protected final void parseSourceFile( final byte[] data,
                                        final int offset,
                                        final ConstantPool constantPool )
  {
    final int index = IOUtil.readUnsignedShort( data, offset );
    final String utfEntry = constantPool.getUtfEntry( index );
    handleSourceFile( utfEntry );
  }

  protected final void parseAnnotations( final String type,
                                         final byte[] data,
                                         final int offset,
                                         final ConstantPool constantPool )
  {
    handleAnnotationGroup( type );
    final int count = IOUtil.readUnsignedShort( data, offset );
    int location = offset + 2;
    try
    {
      for( int i = 0; i < count; i++ )
      {
        location = parseAnnotation( data, location, constantPool );
      }
    }
    catch( IllegalStateException e )
    {
      System.err.println( "Skipping array due to " + e.getMessage() );
    }
  }

  private int parseAnnotation( final byte[] data,
                               final int offset,
                               final ConstantPool constantPool )
  {
    final int typeIndex = IOUtil.readUnsignedShort( data, offset );
    final String type = constantPool.getUtfEntry( typeIndex );
    startAnnotation( type );

    final int count = IOUtil.readUnsignedShort( data, offset + 2 );
    int location = offset + 4;
    for( int i = 0; i < count; i++ )
    {
      final int nameIndex = IOUtil.readUnsignedShort( data, location );
      final String name = constantPool.getUtfEntry( nameIndex );
      location += 2;
      location = parseElementValue( name, data, location, constantPool );
    }
    endAnnotation();
    return offset;
  }

  private int parseElementValue( final String name,
                                 final byte[] data,
                                 final int offset,
                                 final ConstantPool constantPool )
  {
    final byte tag = data[offset];
    int location = 1 + offset;
    switch( tag )
    {
      //TODO: Sort by frequency
      case ClassFileFormat.ANN_TAG_INTEGER:
      {
        final int index = IOUtil.readUnsignedShort( data, location );
        location += 2;
        final Object value = constantPool.getIntegerEntry( index );
        handleAnnotationValue( name, value );
      }
      break;
      case ClassFileFormat.ANN_TAG_LONG:
      {
        final int index = IOUtil.readUnsignedShort( data, location );
        location += 2;
        final Object value = constantPool.getLongEntry( index );
        handleAnnotationValue( name, value );
        break;
      }
      case ClassFileFormat.ANN_TAG_FLOAT:
      {
        final int index = IOUtil.readUnsignedShort( data, location );
        location += 2;
        final Object value = constantPool.getFloatEntry( index );
        handleAnnotationValue( name, value );
        break;
      }
      case ClassFileFormat.ANN_TAG_DOUBLE:
      {
        final int index = IOUtil.readUnsignedShort( data, location );
        location += 2;
        final Object value = constantPool.getDoubleEntry( index );
        handleAnnotationValue( name, value );
        break;
      }
      case ClassFileFormat.ANN_TAG_BYTE:
      {
        final int index = IOUtil.readUnsignedShort( data, location );
        location += 2;
        final Object value = (byte)constantPool.getIntegerEntry( index );
        handleAnnotationValue( name, value );
        break;
      }
      case ClassFileFormat.ANN_TAG_BOOLEAN:
      {
        final int index = IOUtil.readUnsignedShort( data, location );
        location += 2;
        final Object value =
          ( 0 != constantPool.getIntegerEntry( index ) ) ? Boolean.TRUE : Boolean.FALSE;
        handleAnnotationValue( name, value );
        break;
      }
      case ClassFileFormat.ANN_TAG_SHORT:
      {
        final int index = IOUtil.readUnsignedShort( data, location );
        location += 2;
        final Object value = (short)constantPool.getIntegerEntry( index );
        handleAnnotationValue( name, value );
        break;
      }
      case ClassFileFormat.ANN_TAG_CHAR:
      {
        final int index = IOUtil.readUnsignedShort( data, location );
        location += 2;
        final Object value = (char)constantPool.getIntegerEntry( index );
        handleAnnotationValue( name, value );
        break;
      }
      case ClassFileFormat.ANN_TAG_UTF8:
      {
        final int index = IOUtil.readUnsignedShort( data, location );
        location += 2;
        final Object value = constantPool.getUtfEntry( index );
        handleAnnotationValue( name, value );
        break;
      }
      case ClassFileFormat.ANN_TAG_ENUM: // enum_const_value
      {
        final int keyIndex = IOUtil.readUnsignedShort( data, location );
        location += 2;
        final int valueIndex = IOUtil.readUnsignedShort( data, location );
        location += 2;
        final String key = constantPool.getUtfEntry( keyIndex );
        final String value = constantPool.getUtfEntry( valueIndex );
        handleAnnotationEnumValue( name, key, value );
        break;
      }
      case ClassFileFormat.ANN_TAG_CLASS:
      {
        final int keyIndex = IOUtil.readUnsignedShort( data, location );
        location += 2;
        final Object value = constantPool.getUtfEntry( keyIndex );
        handleAnnotationValue( name, value );
        break;
      }
      case ClassFileFormat.ANN_TAG_ANNOTATION:
      {
        parseAnnotation( data, location, constantPool );
        break;
      }
      case ClassFileFormat.ANN_TAG_ARRAY:
      {
        final int size = IOUtil.readUnsignedShort( data, location );
        location += 2;
        if( size == 0 )
        {
          break;
        }
        final byte arrayTag = data[location];
        switch( arrayTag )
        {
          //TODO: Sort by frequency
          case ClassFileFormat.ANN_TAG_BYTE:
          {
            final byte[] value = new byte[size];
            for( int j = 0; j < size; j++ )
            {
              checkTag( arrayTag, data, location );
              final int index = IOUtil.readUnsignedShort( data, location + 1 );
              value[j] = (byte)constantPool.getIntegerEntry( index );
              location += 3;
            }
            handleAnnotationValue( name, value );
            break;
          }
          case ClassFileFormat.ANN_TAG_SHORT:
          {
            final short[] value = new short[size];
            for( int j = 0; j < size; j++ )
            {
              checkTag( arrayTag, data, location );
              final int index = IOUtil.readUnsignedShort( data, location + 1 );
              value[j] = (short)constantPool.getIntegerEntry( index );
              location += 3;
            }
            handleAnnotationValue( name, value );
            break;
          }
          case ClassFileFormat.ANN_TAG_BOOLEAN:
          {
            final boolean[] value = new boolean[size];
            for( int j = 0; j < size; j++ )
            {
              checkTag( arrayTag, data, location );
              final int index = IOUtil.readUnsignedShort( data, location + 1 );
              value[j] = constantPool.getIntegerEntry( index ) != 0;
              location += 3;
            }
            handleAnnotationValue( name, value );
            break;
          }
          case ClassFileFormat.ANN_TAG_CHAR:
          {
            final char[] value = new char[size];
            for( int j = 0; j < size; j++ )
            {
              checkTag( arrayTag, data, location );
              final int index = IOUtil.readUnsignedShort( data, location + 1 );
              value[j] = (char)constantPool.getIntegerEntry( index );
              location += 3;
            }
            handleAnnotationValue( name, value );
            break;
          }
          case ClassFileFormat.ANN_TAG_INTEGER:
          {
            final int[] value = new int[size];
            for( int j = 0; j < size; j++ )
            {
              checkTag( arrayTag, data, location );
              final int index = IOUtil.readUnsignedShort( data, location + 1 );
              value[j] = constantPool.getIntegerEntry( index );
              location += 3;
            }
            handleAnnotationValue( name, value );
            break;
          }
          case ClassFileFormat.ANN_TAG_LONG:
          {
            final long[] value = new long[size];
            for( int j = 0; j < size; j++ )
            {
              checkTag( arrayTag, data, location );
              final int index = IOUtil.readUnsignedShort( data, location + 1 );
              value[j] = constantPool.getLongEntry( index );
              location += 3;
            }
            handleAnnotationValue( name, value );
            break;
          }
          case ClassFileFormat.ANN_TAG_FLOAT:
          {
            final float[] value = new float[size];
            for( int j = 0; j < size; j++ )
            {
              checkTag( arrayTag, data, location );
              final int index = IOUtil.readUnsignedShort( data, location + 1 );
              value[j] = constantPool.getFloatEntry( index );
              location += 3;
            }
            handleAnnotationValue( name, value );
            break;
          }
          case ClassFileFormat.ANN_TAG_DOUBLE:
          {
            final double[] value = new double[size];
            for( int j = 0; j < size; j++ )
            {
              checkTag( arrayTag, data, location );
              final int index = IOUtil.readUnsignedShort( data, location + 1 );
              value[j] = constantPool.getDoubleEntry( index );
              location += 3;
            }
            handleAnnotationValue( name, value );
            break;
          }
          case ClassFileFormat.ANN_TAG_CLASS:
          {
            final String[] value = new String[size];
            for( int j = 0; j < size; j++ )
            {
              checkTag( arrayTag, data, location );
              final int index = IOUtil.readUnsignedShort( data, location + 1 );
              value[j] = constantPool.getUtfEntry( index );
              location += 3;
            }
            handleAnnotationValue( name, value );
            break;
          }
          case ClassFileFormat.ANN_TAG_UTF8:
          {
            final String[] value = new String[size];
            for( int j = 0; j < size; j++ )
            {
              checkTag( arrayTag, data, location );
              final int index = IOUtil.readUnsignedShort( data, location + 1 );
              value[j] = constantPool.getUtfEntry( index );
              location += 3;
            }
            handleAnnotationValue( name, value );
            break;
          }
          case ClassFileFormat.ANN_TAG_ANNOTATION:
          case ClassFileFormat.ANN_TAG_ENUM:
          case ClassFileFormat.ANN_TAG_ARRAY:
          {
            startAnnotationValueArray( name, size );
            for( int j = 0; j < size; j++ )
            {
              checkTag( arrayTag, data, location );
              location = parseElementValue( null, data, location, constantPool );
            }
            endAnnotationValueArray();
            break;
          }

          default:
            throw new IllegalStateException( "Unhandled array type: " + (char)arrayTag );
        }
        break;
      }

      default:
        final String message =
          "Unknown attribute tag (" + (char)tag + ") at position " + location;
        throw new ClassFormatError( message );
    }
    return location;
  }

  private void checkTag( final byte arrayTag, final byte[] data, final int location )
  {
    if( arrayTag != data[location] )
    {
      final String message =
        "Bad type for array tag in annotation at position " + location +
        ". Expected: " + arrayTag + " Actual: " + data[location];
      throw new ClassFormatError( message );
    }
  }

  protected final void parseInnerClasses( final byte[] data, final int offset, final ConstantPool constantPool )
  {
    final int count = IOUtil.readUnsignedShort( data, offset );
    int location = offset + 2;
    for( int i = 0; i < count; i++ )
    {
      parseInnerClassElement( data, location, constantPool );
      location += 8;
    }
  }

  protected final void parseSignature( final byte[] data, final int offset, final ConstantPool constantPool )
  {
    final int index = IOUtil.readUnsignedShort( data, offset );
    final String utfEntry = constantPool.getUtfEntry( index );
    handleSignature( utfEntry );
  }

  protected final void parseEnclosingMethod( final byte[] data, final int offset, final ConstantPool constantPool )
  {
    final int classIndex = IOUtil.readUnsignedShort( data, offset );
    final int methodIndex = IOUtil.readUnsignedShort( data, offset + 2 );
    final String klass = constantPool.getClassEntry( classIndex );
    final String methodName;
    final String methodType;
    if( 0 != methodIndex )
    {
      methodName = constantPool.getNameFromNameAndType( methodIndex );
      methodType = constantPool.getTypeFromNameAndType( methodIndex );
    }
    else
    {
      methodName = null;
      methodType = null;
    }
    handleEnclosingMethod( klass, methodName, methodType );
  }

  protected final void parseInnerClassElement( final byte[] data,
                                               final int offset,
                                               final ConstantPool constantPool )
  {
    final int innerClassInfoIndex =
      IOUtil.readUnsignedShort( data, offset );
    final int outerClassInfoIndex =
      IOUtil.readUnsignedShort( data, offset + 2 );
    final int innerNameIndex =
      IOUtil.readUnsignedShort( data, offset + 4 );
    final int innerClassAccessFlags =
      IOUtil.readUnsignedShort( data, offset + 4 );
    final String innerClass;
    if( 0 != innerClassInfoIndex )
    {
      innerClass = constantPool.getClassEntry( innerClassInfoIndex );
    }
    else
    {
      innerClass = null;
    }
    final String outerClass;
    if( 0 != outerClassInfoIndex )
    {
      outerClass = constantPool.getClassEntry( outerClassInfoIndex );
    }
    else
    {
      outerClass = null;
    }
    final String innerName;
    if( 0 != innerNameIndex )
    {
      innerName = constantPool.getUtfEntry( innerNameIndex );
    }
    else
    {
      innerName = null;
    }
    handleInnerClass( innerClass, outerClass, innerName, innerClassAccessFlags );
  }

  protected void handleSynthetic()
  {
    throw newUnimplementedException();
  }

  protected void handleDeprecated()
  {
    throw newUnimplementedException();
  }

  protected void handleInnerClass( final String innerClass,
                                   final String outerClass,
                                   final String innerName,
                                   final int innerClassAccessFlags )
  {
    throw newUnimplementedException();
  }

  protected void handleSignature( final String signature )
  {
    throw newUnimplementedException();
  }

  protected void handleEnclosingMethod( final String klass,
                                        final String methodName,
                                        final String methodType )
  {
    throw newUnimplementedException();
  }

  protected void handleSourceFile( final String utfEntry )
  {
    throw newUnimplementedException();
  }

  protected void handleSourceDebug( final String value )
  {
    throw newUnimplementedException();
  }

  protected void handleAnnotationGroup( final String type )
  {
    throw newUnimplementedException();
  }

  protected void startAnnotation( final String type )
  {
    throw newUnimplementedException();
  }

  protected void endAnnotation()
  {
    throw newUnimplementedException();
  }

  protected void handleAnnotationValue( final String name, final Object value )
  {
    throw newUnimplementedException();
  }

  protected void handleAnnotationEnumValue( final String name,
                                            final String key,
                                            final String value )
  {
    throw newUnimplementedException();
  }

  protected void endAnnotationValueArray()
  {
    throw newUnimplementedException();
  }

  protected void startAnnotationValueArray( final String name, final int length )
  {
    throw newUnimplementedException();
  }

  protected void handleConstantValue( final Object value )
  {
    throw newUnimplementedException();
  }

  protected void handleExceptions( final String[] exceptions )
  {
    throw newUnimplementedException();
  }

  protected void startCode( final int maxStack,
                            final int maxLocals,
                            final byte[] data,
                            final int offset,
                            final long codeLength )
  {
    throw newUnimplementedException();
  }

  protected void handleExceptionHandler( final int startPC,
                                         final int endPC,
                                         final int handlerPC,
                                         final String catchType )
  {
    throw newUnimplementedException();
  }

  protected void handleCodeAttribute( final String name,
                                      final byte[] data,
                                      final int location,
                                      final long length,
                                      final ConstantPool constantPool )
  {
    throw newUnimplementedException();
  }

  protected void endCode()
  {
    throw newUnimplementedException();
  }

  protected void handleMethod( final String name,
                               final String descriptor,
                               final int accessFlags )
  {
    throw newUnimplementedException();
  }

  protected void handleClassAttribute( final String name,
                                       final byte[] data,
                                       final int offset,
                                       final long length,
                                       final ConstantPool constantPool )
  {
    throw newUnimplementedException();
  }

  protected void handleMethodAttribute( final String name,
                                        final byte[] data,
                                        final int offset,
                                        final long length,
                                        final ConstantPool constantPool )
  {
    throw newUnimplementedException();
  }

  protected void handleFieldAttribute( final String name,
                                       final byte[] data,
                                       final int offset,
                                       final long length,
                                       final ConstantPool constantPool )
  {
    throw newUnimplementedException();
  }

  protected void handleField( final String name,
                              final String descriptor,
                              final int accessFlags )
  {
    throw newUnimplementedException();
  }

  private IllegalStateException newUnimplementedException()
  {
    return new IllegalStateException( "Unimplemented handler invoked." );
  }
}