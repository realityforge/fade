package org.realityforge.fade;

/** Exception indicating Class file is in invalid format. */
public class InvalidClassFileException
  extends Exception
{
  /**
   * The offset into classfile where invalid data was found.
   * May be 0 if unknown.
   */
  private final int offset;

  /**
   * Create Exception.
   *
   * @param offset the offset if known, else 0.
   * @param message the message describing error.
   */
  public InvalidClassFileException( final int offset, final String message )
  {
    super( message );
    this.offset = offset;
  }

  /**
   * @return the offset into classfile where invalid data was found. May be 0 if unknown.
   */
  public int getOffset()
  {
    return offset;
  }
}
