package org.realityforge.fade;

/** Class for parsing class file. */
public abstract class ClassFileParser
{
  /** Thw type of attribute being parsed */
  static enum AttributeType
  {
    CLASS, METHOD, FIELD, CODE
  }

  /**
   * Method to parse class attributes.
   *
   * @param data         the data.
   * @param offset       the offset into data where element starts.
   * @param constantPool the associated constant pool.
   */
  final void parseClassAttributes( final byte[] data,
                                   final int offset,
                                   final ConstantPool constantPool )
  {
    parseAttributes( AttributeType.CLASS, data, offset, constantPool );
  }

  /**
   * Method that parses field data.
   *
   * @param data         the data.
   * @param offset       the offset into data where element starts.
   * @param constantPool the associated constant pool.
   */
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

  /**
   * Method that parses method data.
   *
   * @param data         the data.
   * @param offset       the offset into data where element starts.
   * @param constantPool the associated constant pool.
   */
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

  /**
   * Method to parse attributes.
   *
   * @param type         the element associated with attributes.
   * @param data         the data.
   * @param baseOffset   the offset into data where element starts.
   * @param constantPool the associated constant pool.
   */
  final void parseAttributes( final AttributeType type,
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

  /**
   * Method that parses Code attribute.
   *
   * @param data   the data.
   * @param offset the offset into data where element starts.
   * @param constantPool the associated constant pool.
   */
  protected final void parseCode( final byte[] data,
                                  final int offset,
                                  final ConstantPool constantPool )
  {
    final int maxStack = IOUtil.readUnsignedShort( data, offset );
    final int maxLocals = IOUtil.readUnsignedShort( data, offset + 2 );
    final long codeLength = IOUtil.readUnsignedInteger( data, offset + 4 );
    startCode( maxStack, maxLocals, data, offset + 8, codeLength, constantPool );
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

  /**
   * Method that parses Exceptions attribute.
   *
   * @param data   the data.
   * @param offset the offset into data where element starts.
   * @param constantPool the associated constant pool.
   */
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

  /**
   * Method that parses Deprecated attribute.
   *
   * @param data   the data.
   * @param offset the offset into data where element starts.
   * @param length the length of attribute.
   */
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

  /**
   * Method that parses Synthetic attribute.
   *
   * @param data   the data.
   * @param offset the offset into data where element starts.
   * @param length the length of attribute.
   */
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

  /**
   * Method that parses ConstantValue attribute.
   *
   * @param data   the data.
   * @param offset the offset into data where element starts.
   * @param constantPool the associated constant pool.
   */
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

  /**
   * Method that parses SourceDebug attribute.
   *
   * @param data   the data.
   * @param offset the offset into data where element starts.
   * @param length the length of attribute.
   */
  protected final void parseSourceDebug( final byte[] data,
                                         final int offset,
                                         final long length )
  {
    final String value = IOUtil.parseUtfString( data, offset, -1, (int)length );
    handleSourceDebug( value );
  }

  /**
   * Method that parses SourceDebug attribute.
   *
   * @param data   the data.
   * @param offset the offset into data where element starts.
   * @param constantPool the associated constant pool.
   */
  protected final void parseSourceFile( final byte[] data,
                                        final int offset,
                                        final ConstantPool constantPool )
  {
    final int index = IOUtil.readUnsignedShort( data, offset );
    final String utfEntry = constantPool.getUtfEntry( index );
    handleSourceFile( utfEntry );
  }

  /**
   * Method that parses a group of annotations.
   *
   * @param type   the name of attribute annotations parsed from. i.e. "RuntimeVisibleAnnotations"
   * @param data   the data.
   * @param offset the offset into data where element starts.
   * @param constantPool the associated constant pool.
   */
  protected final void parseAnnotations( final String type,
                                         final byte[] data,
                                         final int offset,
                                         final ConstantPool constantPool )
  {
    handleAnnotationGroup( type );
    final int count = IOUtil.readUnsignedShort( data, offset );
    int location = offset + 2;
    for( int i = 0; i < count; i++ )
    {
      location = parseAnnotation( data, location, constantPool );
    }
  }

  /**
   * Method that parses an annotation.
   *
   * @param data   the data.
   * @param offset the offset into data where element starts.
   * @param constantPool the associated constant pool.
   * @return the offset of next element
   */
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

  /**
   * Parse element value for annotation.
   *
   * @param name   the name of the element or null if nested in array.
   * @param data   the data.
   * @param offset the offset into data where element starts.
   * @param constantPool the associated constant pool.
   * @return the number of bytes parsed.
   */
  final int parseElementValue( final String name,
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
        startAnnotationValueAnnotation( name );
        parseAnnotation( data, location, constantPool );
        endAnnotationValueAnnotation();
        break;
      }
      case ClassFileFormat.ANN_TAG_ARRAY:
      {
        final int size = IOUtil.readUnsignedShort( data, location );
        location += 2;
        if( size == 0 )
        {
          startAnnotationValueArray( name, 0 );
          endAnnotationValueArray();
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
          //TODO: collect array tags so arrays of arrays of primitives will be specially handled
          case ClassFileFormat.ANN_TAG_ARRAY:                                                                         
          case ClassFileFormat.ANN_TAG_ANNOTATION:
          case ClassFileFormat.ANN_TAG_ENUM:
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
            final String message = "Unexpected array type " + (char)arrayTag + " at position " + location;
            throw new ClassFormatError( message );
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

  /**
   * Check type for array tag and if not the same then throw an exception.
   *
   * @param arrayTag the expected array type.
   * @param data the data array.
   * @param offset the offset of next tag.
   */
  final void checkTag( final byte arrayTag, final byte[] data, final int offset )
  {
    if( arrayTag != data[offset] )
    {
      final String message =
        "Bad type for array tag in annotation at position " + offset +
        ". Expected: " + arrayTag + " Actual: " + data[offset];
      throw new ClassFormatError( message );
    }
  }

  /**
   * Parse innerClasses attribute.
   *
   * @param data   the data.
   * @param offset the offset into data where element starts.
   * @param constantPool the associated constant pool.
   */
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

  /**
   * Parse Signature attribute.
   *
   * @param data   the data.
   * @param offset the offset into data where element starts.
   * @param constantPool the associated constant pool.
   */
  protected final void parseSignature( final byte[] data, final int offset, final ConstantPool constantPool )
  {
    final int index = IOUtil.readUnsignedShort( data, offset );
    final String utfEntry = constantPool.getUtfEntry( index );
    handleSignature( utfEntry );
  }

  /**
   * Parse EnclosingMethod attribute.
   *
   * @param data   the data.
   * @param offset the offset into data where element starts.
   * @param constantPool the associated constant pool.
   */
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

  /**
   * Parse InnerClass element in InnerClasses attribute.
   *
   * @param data   the data.
   * @param offset the offset into data where element starts.
   * @param constantPool the associated constant pool.
   */
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
      IOUtil.readUnsignedShort( data, offset + 6 );
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

  /** Subclasses override method to handle synthetic attribute. */
  protected void handleSynthetic()
  {
    throw newUnimplementedException();
  }

  /** Subclasses override method to handle deprecated attribute. */
  protected void handleDeprecated()
  {
    throw newUnimplementedException();
  }

  /**
   * Subclasses override to handle InnerClass attribute.
   *
   * @param innerClass            the inner class.
   * @param outerClass            the outer class.
   * @param innerName             the name used in source. (For anonymous types).
   * @param innerClassAccessFlags the access flags for inner class.
   */
  protected void handleInnerClass( final String innerClass,
                                   final String outerClass,
                                   final String innerName,
                                   final int innerClassAccessFlags )
  {
    throw newUnimplementedException();
  }

  /**
   * Subclasses override to handle Signature attribute.
   *
   * @param signature the signature.
   */
  protected void handleSignature( final String signature )
  {
    throw newUnimplementedException();
  }

  /**
   * Subclasses overide to handle EnclosingMethod attribute.
   *
   * @param klass      the outer type.
   * @param methodName the method name.
   * @param methodType the inner type.
   */
  protected void handleEnclosingMethod( final String klass,
                                        final String methodName,
                                        final String methodType )
  {
    throw newUnimplementedException();
  }

  /**
   * Subclasses override to handle SourceFile attribute.
   *
   * @param filename the filename.
   */
  protected void handleSourceFile( final String filename )
  {
    throw newUnimplementedException();
  }

  /**
   * Subclasses override to handle SourceDebug attribute.
   *
   * @param value the debug value.
   */
  protected void handleSourceDebug( final String value )
  {
    throw newUnimplementedException();
  }

  /**
   * Subclasses override to handle start parsing annotation attribute.
   *
   * @param type the name of attribute.
   */
  protected void handleAnnotationGroup( final String type )
  {
    throw newUnimplementedException();
  }

  /**
   * Subclasses override to handle start of annotation parsing.
   *
   * @param type the name of annotation.
   */
  protected void startAnnotation( final String type )
  {
    throw newUnimplementedException();
  }

  /** Subclasses override method to receive notification when annotation parsing completes. */
  protected void endAnnotation()
  {
    throw newUnimplementedException();
  }

  /**
   * Subclasses override to key-value in annotation.
   *
   * @param name  the key.
   * @param value the value.
   */
  protected void handleAnnotationValue( final String name, final Object value )
  {
    throw newUnimplementedException();
  }

  /**
   * Subclasses override to key-value in annotation where value is an enum.
   *
   * @param name  the key.
   * @param key   the type of enum.
   * @param value the value.
   */
  protected void handleAnnotationEnumValue( final String name,
                                            final String key,
                                            final String value )
  {
    throw newUnimplementedException();
  }

  /** Subclasses override method to receive notification when end of annotation value array is parsed. */
  protected void endAnnotationValueArray()
  {
    throw newUnimplementedException();
  }

  /**
   * Subclasses override to receive notification when array value for annotation starts to be parsed.
   *
   * @param name   the name of key or null if nested array.
   * @param length the number of elements in array.
   */
  protected void startAnnotationValueArray( final String name, final int length )
  {
    throw newUnimplementedException();
  }

  /**
   * Subclasses override to handle ConstantValue attribute.
   *
   * @param value the value.
   */
  protected void handleConstantValue( final Object value )
  {
    throw newUnimplementedException();
  }

  /**
   * Subclasses override to handle exceptions attribute.
   *
   * @param exceptions an array of exception class names.
   */
  protected void handleExceptions( final String[] exceptions )
  {
    throw newUnimplementedException();
  }

  /**
   * Subclasses overide to handle code attribute.
   *
   * @param maxStack   the max stack size in words.
   * @param maxLocals  the max local count.
   * @param data       the data containing bytecode.
   * @param offset     the offset into data when bytecode starts.
   * @param codeLength the length of bytecode in bytes.
   * @param constantPool
   */
  protected void startCode(final int maxStack,
                           final int maxLocals,
                           final byte[] data,
                           final int offset,
                           final long codeLength,
                           final ConstantPool constantPool)
  {
    throw newUnimplementedException();
  }

  /**
   * Subclasses override to handle exception handlers.
   *
   * @param startPC   the start of range exception handler covers.
   * @param endPC     the end of range exception handler covers.
   * @param handlerPC the location of handler.
   * @param catchType null if finally else classname of exception caught.
   */
  protected void handleExceptionHandler( final int startPC,
                                         final int endPC,
                                         final int handlerPC,
                                         final String catchType )
  {
    throw newUnimplementedException();
  }

  /**
   * Subclasses override method to handle attributes associated with a code attribute.
   *
   * @param name         the attributes name.
   * @param data         the data.
   * @param offset       the offset into data where attribute data starts sans tag and length fields.
   * @param length       the length of attribute.
   * @param constantPool the constant pool associated with class.
   */
  protected void handleCodeAttribute( final String name,
                                      final byte[] data,
                                      final int offset,
                                      final long length,
                                      final ConstantPool constantPool )
  {
    throw newUnimplementedException();
  }

  /** Subclasses override method to receive notification when code attribute has completed parsing. */
  protected void endCode()
  {
    throw newUnimplementedException();
  }

  /**
   * Subclasses override method to handle methods.
   *
   * @param name        the methods name.
   * @param descriptor  the methods descriptor.
   * @param accessFlags the methods access flags.
   */
  protected void handleMethod( final String name,
                               final String descriptor,
                               final int accessFlags )
  {
    throw newUnimplementedException();
  }

  /**
   * Subclasses override method to handle attributes associated with a class.
   *
   * @param name         the attributes name.
   * @param data         the data.
   * @param offset       the offset into data where attribute data starts sans tag and length fields.
   * @param length       the length of attribute.
   * @param constantPool the constant pool associated with class.
   */
  protected void handleClassAttribute( final String name,
                                       final byte[] data,
                                       final int offset,
                                       final long length,
                                       final ConstantPool constantPool )
  {
    throw newUnimplementedException();
  }

  /**
   * Subclasses override method to handle attributes associated with a method.
   *
   * @param name         the attributes name.
   * @param data         the data.
   * @param offset       the offset into data where attribute data starts sans tag and length fields.
   * @param length       the length of attribute.
   * @param constantPool the constant pool associated with class.
   */
  protected void handleMethodAttribute( final String name,
                                        final byte[] data,
                                        final int offset,
                                        final long length,
                                        final ConstantPool constantPool )
  {
    throw newUnimplementedException();
  }

  /**
   * Subclasses override method to handle attributes associated with a field.
   *
   * @param name         the attributes name.
   * @param data         the data.
   * @param offset       the offset into data where attribute data starts sans tag and length fields.
   * @param length       the length of attribute.
   * @param constantPool the constant pool associated with class.
   */
  protected void handleFieldAttribute( final String name,
                                       final byte[] data,
                                       final int offset,
                                       final long length,
                                       final ConstantPool constantPool )
  {
    throw newUnimplementedException();
  }

  /**
   * Subclasses override method to handle fields.
   *
   * @param name        the fields name.
   * @param descriptor  the fields descriptor.
   * @param accessFlags the fields access flags.
   */
  protected void handleField( final String name,
                              final String descriptor,
                              final int accessFlags )
  {
    throw newUnimplementedException();
  }

  /**
   * Subclasses override method to handle nested annotations.
   */
  protected void endAnnotationValueAnnotation()
  {
    throw newUnimplementedException();
  }

  /**
   * Subclasses override method to handle nested annotations.
   *
   * @param name the name of value.
   */
  protected void startAnnotationValueAnnotation( final String name )
  {
    throw newUnimplementedException();
  }

  /**
   * Helper to create exception to throw when handler not implemented.
   *
   * @return the exception.
   */
  private IllegalStateException newUnimplementedException()
  {
    return new IllegalStateException( "Unimplemented handler invoked." );
  }
}