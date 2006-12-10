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
}
