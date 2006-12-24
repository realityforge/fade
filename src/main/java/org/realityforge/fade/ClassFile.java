package org.realityforge.fade;

/** Class for representing and parsing class files. */
public class ClassFile
{
  /** The read-only data representing class. */
  private final byte[] data;

  /** The read-only method of accessing the constant pool. */
  private final ConstantPool constantPool;

  /** Offsets for all the method_infos. */
  private final int[] methodOffsets;

  /** Offsets for all the field_infos. */
  private final int[] fieldOffsets;

  /** Offsets for top level attributes. */
  private final int attributeOffset;

  private ClassFile( final byte[] data,
                     final ConstantPool constantPool,
                     final int[] methodOffsets,
                     final int[] fieldOffsets,
                     final int attributeOffset )
  {
    this.data = data;
    this.constantPool = constantPool;
    this.methodOffsets = methodOffsets;
    this.fieldOffsets = fieldOffsets;
    this.attributeOffset = attributeOffset;
  }

  /**
   * Return the access flags for the class.
   *
   * @return the access flags for the class.
   */
  public int getAccessFlags()
  {
    return IOUtil.readUnsignedShort( data, constantPool.getClassHeaderOffset() );
  }

  /**
   * Return the name of the class.
   *
   * @return the name of the class.
   */
  public String getClassName()
  {
    final int offset = constantPool.getClassHeaderOffset() + 2;
    final int index = IOUtil.readUnsignedShort( data, offset );
    return constantPool.getClassEntry( index );
  }

  /**
   * Return the name of the super class.
   *
   * @return the name of the super class.
   */
  public String getSuperClassName()
  {
    final int offset = constantPool.getClassHeaderOffset() + 4;
    final int index = IOUtil.readUnsignedShort( data, offset );
    return ( 0 == index ) ? null : constantPool.getClassEntry( index );
  }

  /**
   * Return the number of interfaces.
   *
   * @return the number of interfaces.
   */
  public int getInterfacesCount()
  {
    final int offset = constantPool.getClassHeaderOffset() + 6;
    return IOUtil.readUnsignedShort( data, offset );
  }

  /**
   * Returns an array of interface names in internal format.
   *
   * @return an array of interface names in internal format.
   */
  public String[] getInterfaces()
  {
    int offset = constantPool.getClassHeaderOffset() + 6;
    final int count = IOUtil.readUnsignedShort( data, offset );
    final String[] names = new String[count];
    for( int i = 0; i < names.length; i++ )
    {
      offset += 2;
      final int index = IOUtil.readUnsignedShort( data, offset );
      names[i] = constantPool.getClassEntry( index );
    }
    return names;
  }

  /**
   * Return the access flags for specified method.
   *
   * @param index the method index.
   * @return the access flags for method.
   */
  public int getMethodAccessFlags( final int index )
  {
    checkMethodIndex( index );
    return IOUtil.readUnsignedShort( data, methodOffsets[index] );
  }

  /**
   * Return the name of specified method.
   *
   * @param index the method index.
   * @return the name of specified method.
   */
  public String getMethodName( final int index )
  {
    checkMethodIndex( index );
    final int utfIndex = IOUtil.readUnsignedShort( data, methodOffsets[index] + 2 );
    return constantPool.getUtfEntry( utfIndex );
  }

  /**
   * Return the descriptor for method in internal format.
   *
   * @param index the method index.
   * @return the method descriptor in internal format.
   */
  public String getMethodDescriptor( final int index )
  {
    checkMethodIndex( index );
    final int utfIndex = IOUtil.readUnsignedShort( data, methodOffsets[index] + 4 );
    return constantPool.getUtfEntry( utfIndex );
  }

  /**
   * Check that access to specified method index is valid.
   *
   * @param index the index.
   */
  private void checkMethodIndex( final int index )
  {
    if( index < 0 || index > methodOffsets.length )
    {
      final String message =
        "Requested invalid method index " + index + " when there is only " + methodOffsets.length + " methods";
      throw new ClassFormatError( message );
    }
  }

  /**
   * Return the access flags for specified field.
   *
   * @param index the field index.
   * @return the access flags for field.
   */
  public int getFieldAccessFlags( final int index )
  {
    checkFieldIndex( index );
    return IOUtil.readUnsignedShort( data, fieldOffsets[index] );
  }

  /**
   * Return the name of specified field.
   *
   * @param index the field index.
   * @return the name of specified field.
   */
  public String getFieldName( final int index )
  {
    checkFieldIndex( index );
    final int utfIndex = IOUtil.readUnsignedShort( data, fieldOffsets[index] + 2 );
    return constantPool.getUtfEntry( utfIndex );
  }

  /**
   * Return the descriptor for field in internal format.
   *
   * @param index the field index.
   * @return the field descriptor in internal format.
   */
  public String getFieldDescriptor( final int index )
  {
    checkFieldIndex( index );
    final int utfIndex = IOUtil.readUnsignedShort( data, fieldOffsets[index] + 4 );
    return constantPool.getUtfEntry( utfIndex );
  }

  /**
   * Check that access to specified field index is valid.
   *
   * @param index the index.
   */
  private void checkFieldIndex( final int index )
  {
    if( index < 0 || index > fieldOffsets.length )
    {
      final String message =
        "Requested invalid field index " + index + " when there is only " + fieldOffsets.length + " fields";
      throw new ClassFormatError( message );
    }
  }

  /**
   * Return the minor version of the class file.
   *
   * @return the minor version of the class file.
   */
  public int getMinorVersion()
  {
    return IOUtil.readUnsignedShort( data, 4 );
  }

  /**
   * Return the major version of the class file.
   *
   * @return the major version of the class file.
   */
  public int getMajorVersion()
  {
    return IOUtil.readUnsignedShort( data, 6 );
  }

  /**
   * Method that used to process Class.
   * Processing will use the supplied parser to parse the class file. The fields,
   * methods and class sections will only be parsed if specified flag is true.
   *
   * @param parser the user supplied parser.
   * @param parseFields true to parse field section, false otherwise.
   * @param parseMethods true to parse method section, false otherwise.
   * @param parseClassSection true to parse class section, false otherwise.
   */
  public void processClass( final ClassFileParser parser,
                            final boolean parseFields,
                            final boolean parseMethods,
                            final boolean parseClassSection )
  {
    if( parseFields )
    {
      for( final int offset : fieldOffsets )
      {
        parser.parseField( data, offset, constantPool );
      }
    }
    if( parseMethods )
    {
      for( final int offset : methodOffsets )
      {
        parser.parseMethod( data, offset, constantPool );
      }
    }
    if( parseClassSection )
    {
      parser.parseClassAttributes( data, attributeOffset, constantPool );
    }
  }

  /**
   * Parse Constant Pool.
   * The array is now owned by ConstantPool object and should not be modified
   * within the range betweem offset and offset+length.
   *
   * @param data the data array.
   * @return the newly created ConstantPool.
   */
  public static ClassFile parseClassFile( final byte[] data )
  {
    IOUtil.checkLength( data, 0, 10 );
    final int magic = IOUtil.readInteger( data, 0 );
    if( ClassFileFormat.MAGIC != magic )
    {
      throw new ClassFormatError( "Bad magic number " + magic );
    }
    final int majorVersion = IOUtil.readUnsignedShort( data, 6 );
    final int minorVersion = IOUtil.readUnsignedShort( data, 4 );

    if( majorVersion < ClassFileFormat.MAJOR_VERSION_1 ||
        majorVersion > ClassFileFormat.MAJOR_VERSION_5 )
    {
      final String message = "Bad class file version " + majorVersion + "." + minorVersion;
      throw new ClassFormatError( message );
    }
    final ConstantPool constantPool = ConstantPool.parseConstantPool( data );
    int offset = constantPool.getClassHeaderOffset();
    IOUtil.checkLength( data, offset, 8 );
    offset += 6;
    final int interfaceCount = IOUtil.readUnsignedShort( data, offset );
    offset += 2;
    IOUtil.checkLength( data, offset, interfaceCount * 2 );
    //Skip over interfaces.
    offset += interfaceCount * 2;

    IOUtil.checkLength( data, offset, 2 );
    final int fieldCount = IOUtil.readUnsignedShort( data, offset );
    offset += 2;
    final int[] fieldOffsets = new int[fieldCount];
    offset = parseFields( data, offset, fieldOffsets );

    IOUtil.checkLength( data, offset, 2 );
    final int methodCount = IOUtil.readUnsignedShort( data, offset );
    final int[] methodOffsets = new int[methodCount];
    offset = parseMethods( data, offset + 2, methodOffsets );

    final int attributeoffset = offset;
    IOUtil.checkLength( data, offset, 2 );
    final int attributeCount = IOUtil.readUnsignedShort( data, offset );
    offset = parseAttributes( data, offset + 2, attributeCount );

    if( offset != data.length )
    {
      final String message =
        "Class definition ends at position " + offset + " when the class data is " +
        data.length + " bytes long.";
      throw new ClassFormatError( message );
    }
    return new ClassFile( data, constantPool, methodOffsets, fieldOffsets, attributeoffset );
  }

  /**
   * Parse field_info section.
   *
   * @param data         the bytes.
   * @param baseOffset   the offset to start parsing from.
   * @param fieldOffsets the array to store offsets of field_infos.
   * @return the position after last field is parsed.
   */
  private static int parseFields( final byte[] data,
                                  final int baseOffset,
                                  final int[] fieldOffsets )
  {
    int offset = baseOffset;
    for( int i = 0; i < fieldOffsets.length; i++ )
    {
      fieldOffsets[i] = offset;
      IOUtil.checkLength( data, offset, 8 );
      final int count = IOUtil.readUnsignedShort( data, offset + 6 );
      offset = parseAttributes( data, offset + 8, count );
    }
    return offset;
  }

  /**
   * Parse method_info section.
   *
   * @param data          the bytes.
   * @param baseOffset    the offset to start parsing from.
   * @param methodOffsets the array to store offsets of method_infos.
   * @return the position after last method is parsed.
   */
  private static int parseMethods( final byte[] data,
                                   final int baseOffset,
                                   final int[] methodOffsets )
  {
    int offset = baseOffset;
    for( int i = 0; i < methodOffsets.length; i++ )
    {
      methodOffsets[i] = offset;
      IOUtil.checkLength( data, offset, 8 );
      final int count = IOUtil.readUnsignedShort( data, offset + 6 );
      offset = parseAttributes( data, offset + 8, count );
    }
    return offset;
  }

  /**
   * Parse attribute section.
   *
   * @param data   the bytes.
   * @param offset the offset to start parsing from.
   * @param count  the number of attributes expected.
   * @return the position after last attribute is parsed.
   */
  private static int parseAttributes( final byte[] data, int offset, int count )
  {
    for( int i = 0; i < count; i++ )
    {
      IOUtil.checkLength( data, offset, 6 );
      final long size = IOUtil.readUnsignedInteger( data, offset + 2 );
      offset += 6;
      IOUtil.checkLength( data, offset, size );
      offset += size;
    }
    return offset;
  }

  byte[] getData()
  {
    return data;
  }

  ConstantPool getConstantPool()
  {
    return constantPool;
  }

  int[] getMethodOffsets()
  {
    return methodOffsets;
  }

  int[] getFieldOffsets()
  {
    return fieldOffsets;
  }

  int getAttributeOffset()
  {
    return attributeOffset;
  }
}
