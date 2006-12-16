package org.realityforge.fade;

/** Class for handling fields. */
public interface FieldHandler
{
  /**
   * Handle specified field.
   *
   * @param classFile   the associated class file.
   * @param name        the name of the method.
   * @param type        the type of the method.
   * @param accessFlags the access flags for the method.
   */
  void handleField( ClassFile classFile,
                    String name,
                    String type,
                    int accessFlags );
}
