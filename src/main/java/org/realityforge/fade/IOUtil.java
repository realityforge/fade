package org.realityforge.fade;

/** Utility class with helpers for doing IO on classfiles. */
class IOUtil
{
  /**
   * Read in integer in big-endian order.
   *
   * @param data   the data.
   * @param offset the offset.
   * @return the value.
   */
  static int readInteger( final byte[] data, final int offset )
  {
    return ( ( data[offset] ) << 24 ) +
           ( ( data[offset + 1] & 0xFF ) << 16 ) +
           ( ( data[offset + 2] & 0xFF ) << 8 ) +
           ( data[offset + 3] & 0xFF );
  }

  /**
   * Read in unsigned integer in big-endian order.
   *
   * @param data   the data.
   * @param offset the offset.
   * @return the value.
   */
  static long readUnsignedInteger( final byte[] data, final int offset )
  {
    return (
      ( (long)( data[offset] & 0xff ) << 24 ) +
      ( (long)( data[offset + 1] & 0xff ) << 16 ) +
      ( (long)( data[offset + 2] & 0xff ) << 8 ) +
      ( (long)( data[offset + 3] & 0xff ) ) );
  }

  /**
   * Read in long in big-endian order.
   *
   * @param data   the data.
   * @param offset the offset.
   * @return the value.
   */
  static long readLong( final byte[] data, final int offset )
  {
    return (
      ( (long)( data[offset] & 0xff ) << 56 ) +
      ( (long)( data[offset + 1] & 0xff ) << 48 ) +
      ( (long)( data[offset + 2] & 0xff ) << 40 ) +
      ( (long)( data[offset + 3] & 0xff ) << 32 ) +
      ( (long)( data[offset + 4] & 0xff ) << 24 ) +
      ( (long)( data[offset + 5] & 0xff ) << 16 ) +
      ( (long)( data[offset + 6] & 0xff ) << 8 ) +
      ( (long)( data[offset + 7] & 0xff ) ) );
  }

  /**
   * Read in an unsigned short in big-endian order.
   *
   * @param data   the data.
   * @param offset the offset.
   * @return the value.
   */
  static int readUnsignedShort( final byte[] data, final int offset )
  {
    return ( ( data[offset] & 0xFF ) << 8 ) |
           ( ( data[offset + 1] & 0xFF ) );
  }

  /**
   * Throw an exception if there is not enough data left.
   *
   * @param data     the data.
   * @param offset   the current offset.
   * @param required the amount required.
   */
  static void checkLength( final byte[] data, final int offset, final long required )
  {
    if( data.length < offset + required )
    {
      throwTruncatedException( data, offset, required );
    }
  }

  /**
   * Throw class file truncated exception.
   * Method extracted so above method will be inlined but exception
   * creation will not be and thus will not bloat code.
   *
   * @param data     the data.
   * @param offset   the current offset.
   * @param required the amount required.
   */
  private static void throwTruncatedException( final byte[] data,
                                               final int offset,
                                               final long required )
  {
    final String message =
      "Class file is truncated. Require " + required + " bytes at position " +
      offset + " when class file is only " + data.length + " bytes long.";
    throw new ClassFormatError( message );
  }

  /**
   * Parse a UTF string from data.
   * Format specified in < a href="http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html">ClassFile
   * Spec</a>. Don't really understand it just mechanically translated.
   *
   * @param data       the data.
   * @param baseOffset the offset to start reading utf from.
   * @param index      the constant pool index. if -1 then not parsed from constant pool. Used in reporting exception.
   * @param count      the number of characters in string.
   * @return the string.
   */
  static String parseUtfString( final byte[] data,
                                final int baseOffset,
                                final int index,
                                final int count )
  {
    int offset = baseOffset;
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
   * Create an ClassFormatError for invalid utf content.
   *
   * @param offset the offset.
   * @param index  the index.
   * @return the exception.
   */
  private static ClassFormatError invalidClassFileException( final int offset,
                                                             final int index )
  {
    final String message =
      "Constant pool entry " + index + " has invalid utf8 at " + offset;
    return new ClassFormatError( message );
  }
}
