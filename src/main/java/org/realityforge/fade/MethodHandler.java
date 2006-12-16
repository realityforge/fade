package org.realityforge.fade;

/** Class for handling methods. */
public interface MethodHandler
{
  /**
   * Handle specified method.
   *
   * @param classFile   the associated class file.
   * @param name        the name of the method.
   * @param type        the type of the method.
   * @param accessFlags the access flags for the method.
   */
  void handleMethod( ClassFile classFile,
                     String name,
                     String type,
                     int accessFlags );
}
