package org.realityforge.fade;

/**
 * Interface containing constants defining class file format.
 */
public interface ClassFileFormat {
  ///Magic number at start of file
  int MAGIC = 0xCAFEBABE;

  /* Major version numbers for class file format. */
  int MAJOR_VERSION_1 = 45;
  int MAJOR_VERSION_2 = 46;
  int MAJOR_VERSION_3 = 47;
  int MAJOR_VERSION_4 = 48;
  int MAJOR_VERSION_5 = 49;
  int MAJOR_VERSION_6 = 50;

  /*
    Access modifiers and what structures can apply to.
    Note: Some of the modifiers have the same value but apply
    to different structures. i.e.

    Class.ACC_SUPER == Method.ACC_SYNCHRONIZED
    Field.ACC_VOLATILE == Method.ACC_BRIDGE
    Field.ACC_TRANSIENT == Method.ACC_VARARGS
  */
  int ACC_PUBLIC = 0x0001; //Class or Field or Method
  int ACC_PRIVATE = 0x0002; //Class or Field or Method
  int ACC_PROTECTED = 0x0004; //Class or Field or Method
  int ACC_STATIC = 0x0008; //Field or Method
  int ACC_FINAL = 0x0010; //Class or Field or Method
  int ACC_SUPER = 0x0020; //Class
  int ACC_SYNCHRONIZED = 0x0020; //Method (== ACC_SUPER)
  int ACC_VOLATILE = 0x0040; //Field
  int ACC_BRIDGE = 0x0040; //Method
  int ACC_VARARGS = 0x0080; //Method
  int ACC_TRANSIENT = 0x0080; //Field
  int ACC_NATIVE = 0x0100; //Method
  int ACC_INTERFACE = 0x0200; //Class
  int ACC_ABSTRACT = 0x0400; //Class or Method
  int ACC_STRICT = 0x0800; //Method
  int ACC_SYNTHETIC = 0x1000; //Class or Field or Method
  int ACC_ANNOTATION = 0x2000; //Class
  int ACC_ENUM = 0x4000; //Class

  /* Types in the constant pool. */
  int CONSTANT_Utf8 = 1;
  int CONSTANT_Integer = 3;
  int CONSTANT_Float = 4;
  int CONSTANT_Long = 5;
  int CONSTANT_Double = 6;
  int CONSTANT_Class = 7;
  int CONSTANT_String = 8;
  int CONSTANT_Fieldref = 9;
  int CONSTANT_Methodref = 10;
  int CONSTANT_InterfaceMethodref = 11;
  int CONSTANT_NameAndType = 12;

  /* Constants for attributes */

  String ATTR_Deprecated = "Deprecated"; //Method + Field ==> access |= ACC_DEPRECATED
  String ATTR_Synthetic = "Synthetic"; //Method + Field ==> access |= ACC_SYNTHETIC
  String ATTR_ConstantValue = "ConstantValue"; //Field
  String ATTR_Exceptions = "Exceptions"; //Method
  String ATTR_Code = "Code"; //Method
  String ATTR_SourceFile = "SourceFile"; //Class
  String ATTR_InnerClasses = "InnerClasses"; //Class
  String ATTR_EnclosingMethod = "EnclosingMethod"; //Class
  String ATTR_SourceDebugExtension = "SourceDebugExtension"; //Class
  String ATTR_LineNumberTable = "LineNumberTable"; //Code
  String ATTR_LocalVariableTable = "LocalVariableTable"; //Code
  String ATTR_LocalVariableTypeTable = "LocalVariableTypeTable"; //Code
  String ATTR_StackMapTable = "StackMapTable"; //Code
  String ATTR_Signature = "Signature"; //???
  String ATTR_AnnotationDefault = "AnnotationDefault"; //Method
  String ATTR_RuntimeVisibleAnnotations = "RuntimeVisibleAnnotations"; //Class, Method, Field
  String ATTR_RuntimeInvisibleAnnotations = "RuntimeInvisibleAnnotations"; //Class, Method, Field (We should ignore)
  String ATTR_RuntimeVisibleParameterAnnotations = "RuntimeVisibleParameterAnnotations"; //Method
  String ATTR_RuntimeInvisibleParameterAnnotations = "RuntimeInvisibleParameterAnnotations"; //Method (We should ignore)

  /* Tags used when parsing element value pairs of annotation. */
  byte ANN_TAG_INTEGER = 'I';
  byte ANN_TAG_LONG = 'J';
  byte ANN_TAG_FLOAT = 'F';
  byte ANN_TAG_DOUBLE = 'D';
  byte ANN_TAG_BYTE = 'B';
  byte ANN_TAG_BOOLEAN = 'Z';
  byte ANN_TAG_SHORT = 'S';
  byte ANN_TAG_CHAR = 'C';
  byte ANN_TAG_UTF8 = 's';
  byte ANN_TAG_ENUM = 'e';
  byte ANN_TAG_CLASS = 'c';
  byte ANN_TAG_ANNOTATION = '@';
  byte ANN_TAG_ARRAY = '[';

  //Types for newarray instruction
  byte T_BOOLEAN = 4;
  byte T_CHAR = 5;
  byte T_FLOAT = 6;
  byte T_DOUBLE = 7;
  byte T_BYTE = 8;
  byte T_SHORT = 9;
  byte T_INT = 10;
  byte T_LONG = 11;
}
