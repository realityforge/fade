package org.realityforge.fade;

/** Class for representing and parsing class files. */
public class ClassFile
{
  /** The read-only data representing class. */
  final byte[] data;

  /** The read-only method of accessing the constant pool. */
  final ConstantPool constantPool;

  /** Offsets for all the method_infos. */
  final int[] methodOffsets;

  /** Offsets for all the field_infos. */
  final int[] fieldOffsets;

  /** Offsets for top level attributes. */
  final int attributeOffset;

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
    throws InvalidClassFileException
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
    throws InvalidClassFileException
  {
    final int offset = constantPool.getClassHeaderOffset() + 4;
    final int index = IOUtil.readUnsignedShort( data, offset );
    return constantPool.getClassEntry( index );
  }

  /**
   * Return the number of interfaces.
   *
   * @return the number of interfaces.
   */
  public int getInterfacesCount()
    throws InvalidClassFileException
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
    throws InvalidClassFileException
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
   * @throws InvalidClassFileException if index is invalid.
   */
  public int getMethodAccessFlags( final int index )
    throws InvalidClassFileException
  {
    checkMethodIndex( index );
    return IOUtil.readUnsignedShort( data, methodOffsets[index] );
  }

  /**
   * Return the name of specified method.
   *
   * @param index the method index.
   * @return the name of specified method.
   * @throws InvalidClassFileException if index is invalid.
   */
  public String getMethodName( final int index )
    throws InvalidClassFileException
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
   * @throws InvalidClassFileException if index is invalid.
   */
  public String getMethodDescriptor( final int index )
    throws InvalidClassFileException
  {
    checkMethodIndex( index );
    final int utfIndex = IOUtil.readUnsignedShort( data, methodOffsets[index] + 4 );
    return constantPool.getUtfEntry( utfIndex );
  }

  /**
   * Check that access to specified method index is valid.
   *
   * @param index the index.
   * @throws InvalidClassFileException if index is invalid.
   */
  private void checkMethodIndex( final int index )
    throws InvalidClassFileException
  {
    if( index < 0 || index > methodOffsets.length )
    {
      final String message =
        "Requested invalid method index. Available: " + methodOffsets.length + " Actual: " + index;
      throw new InvalidClassFileException( 0, message );
    }
  }

  /**
   * Return the access flags for specified field.
   *
   * @param index the field index.
   * @return the access flags for field.
   * @throws InvalidClassFileException if index is invalid.
   */
  public int getFieldAccessFlags( final int index )
    throws InvalidClassFileException
  {
    checkFieldIndex( index );
    return IOUtil.readUnsignedShort( data, fieldOffsets[index] );
  }

  /**
   * Return the name of specified field.
   *
   * @param index the field index.
   * @return the name of specified field.
   * @throws InvalidClassFileException if index is invalid.
   */
  public String getFieldName( final int index )
    throws InvalidClassFileException
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
   * @throws InvalidClassFileException if index is invalid.
   */
  public String getFieldDescriptor( final int index )
    throws InvalidClassFileException
  {
    checkFieldIndex( index );
    final int utfIndex = IOUtil.readUnsignedShort( data, fieldOffsets[index] + 4 );
    return constantPool.getUtfEntry( utfIndex );
  }

  /**
   * Check that access to specified field index is valid.
   *
   * @param index the index.
   * @throws InvalidClassFileException if index is invalid.
   */
  private void checkFieldIndex( final int index )
    throws InvalidClassFileException
  {
    if( index < 0 || index > fieldOffsets.length )
    {
      final String message =
        "Requested invalid field index. Available: " + fieldOffsets.length + " Actual: " + index;
      throw new InvalidClassFileException( 0, message );
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
   * Parse Constant Pool.
   * The array is now owned by ConstantPool object and should not be modified
   * within the range betweem offset and offset+length.
   *
   * @param data the data array.
   * @return the newly created ConstantPool.
   * @throws InvalidClassFileException if data not valid.
   */
  public static ClassFile parseClassFile( final byte[] data )
    throws InvalidClassFileException
  {
    checkLength( data, 0, 10 );
    final int magic = IOUtil.readInteger( data, 0 );
    if( ClassFileFormat.MAGIC != magic )
    {
      throw new InvalidClassFileException( 0, "Bad magic number " + magic );
    }
    final int majorVersion = IOUtil.readUnsignedShort( data, 6 );
    final int minorVersion = IOUtil.readUnsignedShort( data, 4 );

    if( majorVersion < ClassFileFormat.MAJOR_VERSION_1 ||
        majorVersion > ClassFileFormat.MAJOR_VERSION_5 )
    {
      final String message = "Bad class file version " + majorVersion + "." + minorVersion;
      throw new InvalidClassFileException( 6, message );
    }
    final ConstantPool constantPool = ConstantPool.parseConstantPool( data );
    int offset = constantPool.getClassHeaderOffset();
    checkLength( data, offset, 8 );
    offset += 6;
    final int interfaceCount = IOUtil.readUnsignedShort( data, offset );
    offset += 2;
    checkLength( data, offset, interfaceCount * 2 );
    //Skip over interfaces.
    offset += interfaceCount * 2;

    checkLength( data, offset, 2 );
    final int fieldCount = IOUtil.readUnsignedShort( data, offset );
    offset += 2;
    final int[] fieldOffsets = new int[fieldCount];
    offset = parseFields( data, offset, fieldOffsets );

    checkLength( data, offset, 2 );
    final int methodCount = IOUtil.readUnsignedShort( data, offset );
    final int[] methodOffsets = new int[methodCount];
    offset = parseMethods( data, offset + 2, methodOffsets );

    final int attributeoffset = offset;
    checkLength( data, offset, 2 );
    final int attributeCount = IOUtil.readUnsignedShort( data, offset );
    offset = parseAttributes( data, offset + 2, attributeCount );

    if( offset != data.length )
    {
      final String message = "Data past end of class definition.";
      throw new InvalidClassFileException( offset, message );
    }
    return new ClassFile( data, constantPool, methodOffsets, fieldOffsets, attributeoffset );
  }

  private static int parseFields( final byte[] data,
                                  final int baseOffset,
                                  final int[] fieldOffsets )
    throws InvalidClassFileException
  {
    int offset = baseOffset;
    for( int i = 0; i < fieldOffsets.length; i++ )
    {
      fieldOffsets[i] = offset;
      checkLength( data, offset, 8 );
      final int count = IOUtil.readUnsignedShort( data, offset + 6 );
      offset = parseAttributes( data, offset + 8, count );
    }
    return offset;
  }

  private static int parseMethods( final byte[] data,
                                   final int baseOffset,
                                   final int[] methodOffsets )
    throws InvalidClassFileException
  {
    int offset = baseOffset;
    for( int i = 0; i < methodOffsets.length; i++ )
    {
      methodOffsets[i] = offset;
      checkLength( data, offset, 8 );
      final int count = IOUtil.readUnsignedShort( data, offset + 6 );
      offset = parseAttributes( data, offset + 8, count );
    }
    return offset;
  }

  private static int parseAttributes( final byte[] data, int offset, int count )
    throws InvalidClassFileException
  {
    for( int i = 0; i < count; i++ )
    {
      checkLength( data, offset, 6 );
      final long size = IOUtil.readUnsignedInteger( data, offset + 2 );
      offset += 6;
      checkLength( data, offset, size );
      offset += size;
    }
    return offset;
  }

  /**
   * Throw an exception if there is not enough data left.
   *
   * @param data     the data.
   * @param offset   the current offset.
   * @param required the amount required.
   * @throws InvalidClassFileException if not enough data left.
   */
  private static void checkLength( final byte[] data, final int offset, final long required )
    throws InvalidClassFileException
  {
    if( data.length < offset + required )
    {
      final String message =
        "Class file truncated. data.length (" + data.length + ") < offset (" +
        offset + ") + required (" + required + ")";
      throw new InvalidClassFileException( offset, message );
    }
  }
}
