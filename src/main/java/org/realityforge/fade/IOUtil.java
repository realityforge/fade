package org.realityforge.fade;

/**
 * Utility class with helpers for doing IO on classfiles.
 */
class IOUtil
{
  static int readInteger( final byte[] data, final int offset )
  {
    return ( ( data[offset] ) << 24 ) +
           ( ( data[offset + 1] & 0xFF ) << 16 ) +
           ( ( data[offset + 2] & 0xFF ) << 8 ) +
           ( data[offset + 3] & 0xFF );
  }

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

  static int readUnsignedShort( final byte[] data, final int offset )
  {
    return ( ( data[offset] & 0xFF ) << 8 ) |
           ( ( data[offset + 1] & 0xFF ) );
  }
}
