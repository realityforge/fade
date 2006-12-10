package org.realityforge.fade;

/** Class that parses the constant pool portion of class file. */
public final class ConstantPool
{
  /** The read-only data representing class. */
  final byte[] data;

  /**
   * The offsets for item in constant pool.
   * The real offsets start at index 1 while offset 0 contains
   * the offset for the class header data.
   */
  final int[] offsets;

  /** Cached copys of strings. Parsing CONSTANT_Utf8 is expensieve. */
  final String[] strings;

  private ConstantPool( final byte[] data, final int[] offsets )
  {
    this.data = data;
    this.offsets = offsets;
    this.strings = new String[offsets.length];
  }

  /**
   * Return the number of constants in pool.
   *
   * @return the number of constants in pool.
   */
  public int getConstantCount()
  {
    return offsets.length - 1;
  }

  /**
   * Returns the offset of class file header.
   *
   * @return the offset of class file header.
   */
  public int getClassHeaderOffset()
  {
    return offsets[0];
  }

  /**
   * Return the type of constant pool entry specified by index.
   * The type must be equal to one of the CONSTANT_* values in {@link ClassFileFormat}.
   *
   * @param index the index of constant pool entry.
   * @return the type.
   * @throws InvalidClassFileException if entry format is invalid.
   */
  public byte getEntryType( final int index )
    throws InvalidClassFileException
  {
    checkRange( index );
    return data[offsets[index]];
  }

  /**
   * Return the name of class identified by specified entry.
   * Note: this caches the name of the class.
   *
   * @param index the index of entry.
   * @return the name of class.
   * @throws InvalidClassFileException if entry format is invalid.
   */
  public String getClassEntry( final int index )
    throws InvalidClassFileException
  {
    checkType( index, ClassFileFormat.CONSTANT_Class );
    if( null == strings[index] )
    {
      final int utfEntry = IOUtil.readUnsignedShort( data, offsets[index] + 1 );
      strings[index] = getUtfEntry( utfEntry );
    }
    return strings[index];
  }

  /**
   * Return the string identified by specified entry.
   * Note: this caches the string.
   *
   * @param index the index of entry.
   * @return the string.
   * @throws InvalidClassFileException if entry format is invalid.
   */
  public String getStringEntry( final int index )
    throws InvalidClassFileException
  {
    checkType( index, ClassFileFormat.CONSTANT_String );
    if( null == strings[index] )
    {
      final int utfEntry = IOUtil.readUnsignedShort( data, offsets[index] + 1 );
      strings[index] = getUtfEntry( utfEntry );
    }
    return strings[index];
  }

  /**
   * Return the float in entry.
   *
   * @param index the index of entry.
   * @return the float.
   * @throws InvalidClassFileException if entry format is invalid.
   */
  public float getFloatEntry( final int index )
    throws InvalidClassFileException
  {
    checkType( index, ClassFileFormat.CONSTANT_Float );
    return Float.intBitsToFloat( IOUtil.readInteger( data, offsets[index] + 1 ) );
  }

  /**
   * Return the double in entry.
   *
   * @param index the index of entry.
   * @return the double.
   * @throws InvalidClassFileException if entry format is invalid.
   */
  public double getDoubleEntry( final int index )
    throws InvalidClassFileException
  {
    checkType( index, ClassFileFormat.CONSTANT_Double );
    return Double.longBitsToDouble( IOUtil.readLong( data, offsets[index] + 1 ) );
  }

  /**
   * Return the integer in entry.
   *
   * @param index the index of entry.
   * @return the integer.
   * @throws InvalidClassFileException if entry format is invalid.
   */
  public int getIntegerEntry( final int index )
    throws InvalidClassFileException
  {
    checkType( index, ClassFileFormat.CONSTANT_Integer );
    return IOUtil.readInteger( data, offsets[index] + 1 );
  }

  /**
   * Return the long in entry.
   *
   * @param index the index of entry.
   * @return the long.
   * @throws InvalidClassFileException if entry format is invalid.
   */
  public long getLongEntry( final int index )
    throws InvalidClassFileException
  {
    checkType( index, ClassFileFormat.CONSTANT_Long );
    return IOUtil.readLong( data, offsets[index] + 1 );
  }

  /**
   * Return the name part from name and type entry.
   *
   * @param index the index of entry.
   * @return the name.
   * @throws InvalidClassFileException if entry format is invalid.
   */
  public String getNameFromNameAndType( final int index )
    throws InvalidClassFileException
  {
    checkType( index, ClassFileFormat.CONSTANT_NameAndType );
    final int utfEntry = IOUtil.readUnsignedShort( data, offsets[index] + 1 );
    return getUtfEntry( utfEntry );
  }

  /**
   * Return the type part from name and type entry.
   *
   * @param index the index of entry.
   * @return the type.
   * @throws InvalidClassFileException if entry format is invalid.
   */
  public String getTypeFromNameAndType( final int index )
    throws InvalidClassFileException
  {
    checkType( index, ClassFileFormat.CONSTANT_NameAndType );
    final int utfEntry = IOUtil.readUnsignedShort( data, offsets[index] + 3 );
    return getUtfEntry( utfEntry );
  }

  /**
   * Return the class part from a ref entry.
   *
   * @param index the index of entry.
   * @return the class.
   * @throws InvalidClassFileException if entry format is invalid.
   */
  public String getClassFromRef( final int index )
    throws InvalidClassFileException
  {
    checkIsRefType( index );
    final int entry = IOUtil.readUnsignedShort( data, offsets[index] + 1 );
    return getClassEntry( entry );
  }

  /**
   * Return the name part from a ref entry.
   *
   * @param index the index of entry.
   * @return the name.
   * @throws InvalidClassFileException if entry format is invalid.
   */
  public String getNameFromRef( final int index )
    throws InvalidClassFileException
  {
    checkIsRefType( index );
    final int entry = IOUtil.readUnsignedShort( data, offsets[index] + 3 );
    return getNameFromNameAndType( entry );
  }

  /**
   * Return the type part from a ref entry.
   *
   * @param index the index of entry.
   * @return the type.
   * @throws InvalidClassFileException if entry format is invalid.
   */
  public String getTypeFromRef( final int index )
    throws InvalidClassFileException
  {
    checkIsRefType( index );
    final int entry = IOUtil.readUnsignedShort( data, offsets[index] + 3 );
    return getTypeFromNameAndType( entry );
  }

  /**
   * Return a parsed UTF8 string at specified entry.
   * Note: this caches the parsed string.
   *
   * @param index the index of entry.
   * @return the string.
   * @throws InvalidClassFileException if entry format is invalid.
   */
  public String getUtfEntry( final int index )
    throws InvalidClassFileException
  {
    checkType( index, ClassFileFormat.CONSTANT_Utf8 );
    if( null == strings[index] )
    {
      strings[index] = parseUtfString( data, offsets[index] + 1, index );
    }
    return strings[index];
  }

  /**
   * Parse a UTF string from data.
   * Format specified in < a href="http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html">ClassFile
   * Spec</a>. Don't really understand it just mechanically translated.
   *
   * @param data       the data.
   * @param baseOffset the offset to start reading utf from.
   * @param index      the constant pool entry. Used in reporting exception.
   * @return the string.
   * @throws InvalidClassFileException if string format is invalid.
   */
  static String parseUtfString( final byte[] data, final int baseOffset, final int index )
    throws InvalidClassFileException
  {
    final int count = IOUtil.readUnsignedShort( data, baseOffset );
    int offset = baseOffset + 2;

    int size = 0;

    //We may over-allocate but this is probably faster
    final char[] chars = new char[count];

    final int end = count + offset;

    while( offset < end )
    {
      final byte ch = data[offset];
      //Single char utf character - 0xxxxxxx
      if( 0 == ( ch & 0x80 ) )
      {
        if( ch == 0 )
        {
          throw invalidClassFileException( offset, index );
        }
        chars[size++] = (char)ch;
        offset += 1;
      }
      //Double char utf character - 110x xxxx 10xx xxxx
      else if( 0xC0 == ( ch & 0xE0 ) )
      {
        if( offset + 1 >= end )
        {
          throw invalidClassFileException( offset, index );
        }
        final byte ch2 = data[offset + 1];
        if( 0x80 != ( ch2 & 0xC0 ) )
        {
          throw invalidClassFileException( offset, index );
        }
        chars[size++] = (char)( ( ( ch & 0x1F ) << 6 ) + ( ch2 & 0x3F ) );
        offset += 2;
      }
      //Triple char utf character - 1110 xxxx 10xx xxxx 10xx xxxx
      else if( 0xE0 == ( ch & 0xF0 ) )
      {
        if( offset + 2 >= end )
        {
          throw invalidClassFileException( offset, index );
        }
        final byte ch2 = data[offset + 1];
        final byte ch3 = data[offset + 2];
        if( 0x80 != ( ch2 & 0xC0 ) || 0x80 != ( ch3 & 0xC0 ) )
        {
          throw invalidClassFileException( offset, index );
        }
        chars[size++] =
          (char)( ( ( ch & 0x0F ) << 12 ) + ( ( ch2 & 0x3F ) << 6 ) + ( ch3 & 0x3F ) );
        offset += 3;
      }
      else
      {
        throw invalidClassFileException( offset, index );
      }
    }
    return new String( chars, 0, size );
  }

  /**
   * Create an InvalidClassFileException for invalid utf content.
   *
   * @param offset the offset.
   * @param index  the index.
   * @return the exception.
   */
  private static InvalidClassFileException invalidClassFileException( final int offset,
                                                                      final int index )
  {
    final String message =
      "Constant pool entry " + index + " has invalid utf8 at " + offset;
    return new InvalidClassFileException( offset, message );
  }

  /**
   * Throw an IllegalArgumentException if entry at index is not a reference.
   *
   * @param index the index to check.
   * @throws InvalidClassFileException if entry at index is not a ref type.
   */
  private void checkIsRefType( final int index )
    throws InvalidClassFileException
  {
    final byte entryType = getEntryType( index );
    if( entryType != ClassFileFormat.CONSTANT_Fieldref &&
        entryType != ClassFileFormat.CONSTANT_Methodref &&
        entryType != ClassFileFormat.CONSTANT_InterfaceMethodref )
    {
      final String message =
        "Unexpected type for constant pool element " +
        index + ". Expected a ref type but got " + entryType;
      throw new InvalidClassFileException( offsets[index], message );
    }
  }

  /**
   * Throw an IllegalArgumentException if entry at index is not specified type.
   *
   * @param index the index to check.
   * @param type  the expected type.
   * @throws InvalidClassFileException if entry at index is not of specified type.
   */
  private void checkType( final int index, final int type )
    throws InvalidClassFileException
  {
    final byte entryType = getEntryType( index );
    if( entryType != type )
    {
      final String message =
        "Unexpected type for constant pool element " +
        index + ". Expected: " + type + " Actual: " + entryType;
      throw new InvalidClassFileException( offsets[index], message );
    }
  }

  /**
   * Throw an IllegalArgumentException if index is out of range.
   * i.e. (index &lt; 1 || index >= offsets.length).
   *
   * @param index the index to check.
   * @throws InvalidClassFileException if index is not in valid range.
   */
  private void checkRange( final int index )
    throws InvalidClassFileException
  {
    if( index < 1 || index >= offsets.length )
    {
      final String message =
        "Can not access constant pool element " +
        index + " as it is not in the range [1-" +
        offsets .length + ")";
      throw new InvalidClassFileException( 0, message );
    }
  }

  /**
   * Parse Constant Pool.
   *
   * @param data the data array.
   * @return the newly created ConstantPool.
   * @throws InvalidClassFileException if data not valid.
   */
  public static ConstantPool parseConstantPool( final byte[] data )
    throws InvalidClassFileException
  {
    final int constantCount = IOUtil.readUnsignedShort( data, 8 );
    final int[] elements = new int[constantCount];

    int index = 10;
    for( int i = 1; i < constantCount; i++ )
    {
      elements[i] = index;

      if( index >= data.length )
      {
        throw new InvalidClassFileException( index, "Class file truncated when parsing constant pool." );
      }
      switch( data[index] )
      {
        case ClassFileFormat.CONSTANT_Utf8:
          index += 3 + IOUtil.readUnsignedShort( data, index + 1 );
          break;
        case ClassFileFormat.CONSTANT_Integer:
        case ClassFileFormat.CONSTANT_Float:
        case ClassFileFormat.CONSTANT_Fieldref:
        case ClassFileFormat.CONSTANT_Methodref:
        case ClassFileFormat.CONSTANT_InterfaceMethodref:
        case ClassFileFormat.CONSTANT_NameAndType:
          index += 5;
          break;
        case ClassFileFormat.CONSTANT_Long:
        case ClassFileFormat.CONSTANT_Double:
          index += 9;
          //For some crazy reason longs and doubles take up two cp entrys
          i++;
          break;

        case ClassFileFormat.CONSTANT_Class:
        case ClassFileFormat.CONSTANT_String:
          index += 3;
          break;
        default:
          throw new InvalidClassFileException( index, "Bad constant pool tag " + data[index] );
      }
    }

    //Setup the classheader offset at index 0
    elements[0] = index;

    return new ConstantPool( data, elements );
  }
}
