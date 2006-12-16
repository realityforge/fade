package org.realityforge.fade;

/** Class for handling attributes. */
public interface AttributeHandler
{
  /**
   * Handle specified attribute.
   *
   * @param name   the name of the attribute.
   * @param data   the data for attribute.
   * @param offset the offset into the data.
   * @return true if the next attribute should be processed.
   */
  boolean handleAttribute( ClassFile classFile,
                           String name,
                           byte[] data,
                           int offset,
                           long length );
}
