package org.realityforge.fade;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class BenchClassFile
{
  private static final boolean DEBUG = "true".equals( System.getProperty( "DEBUG", "false" ) );
  private static int cfParsed = 0;

  private static final class MyHandler
    extends ClassFileParser
  {
    @Override
    protected void handleMethod( final String name,
                                 final String descriptor,
                                 final int accessFlags )
    {
      if( DEBUG )
      {
        System.out.println( "handleMethod(" + name + "," + descriptor + "," + accessFlags + ")" );
      }
    }

    @Override
    protected void handleClassAttribute( final String name,
                                         final byte[] data,
                                         final int offset,
                                         final long length,
                                         final ConstantPool constantPool )
    {
      if( ClassFileFormat.ATTR_SourceFile.equals( name ) )
      {
        parseSourceFile( data, offset, constantPool );
      }
      else if( ClassFileFormat.ATTR_InnerClasses.equals( name ) )
      {
        parseInnerClasses( data, offset, constantPool );
      }
      else if( ClassFileFormat.ATTR_EnclosingMethod.equals( name ) )
      {
        parseEnclosingMethod( data, offset, constantPool );
      }
      else if( ClassFileFormat.ATTR_Signature.equals( name ) )
      {
        parseSignature( data, offset, constantPool );
      }
      else if( ClassFileFormat.ATTR_RuntimeVisibleAnnotations.equals( name ) )
      {
        parseAnnotations( ClassFileFormat.ATTR_RuntimeVisibleAnnotations,
                          data, offset, constantPool );
      }
      else if( ClassFileFormat.ATTR_Deprecated.equals( name ) )
      {
        parseDeprecated( data, offset, length );
      }
      else if( ClassFileFormat.ATTR_Synthetic.equals( name ) )
      {
        parseSynthetic( data, offset, length );
      }
      else if( ClassFileFormat.ATTR_SourceDebugExtension.equals( name ) )
      {
        parseSourceDebug( data, offset, length );
      }
      else if( ClassFileFormat.ATTR_RuntimeInvisibleAnnotations.equals( name ) )
      {
        parseAnnotations( ClassFileFormat.ATTR_RuntimeInvisibleAnnotations,
                          data, offset, constantPool );
      }
      else
      {
        System.err.println( "Unhandled class attribute: " + name );
      }
    }

    @Override
    protected void handleMethodAttribute( final String name,
                                          final byte[] data,
                                          final int offset,
                                          final long length,
                                          final ConstantPool constantPool )
    {
      if( ClassFileFormat.ATTR_RuntimeVisibleAnnotations.equals( name ) )
      {
        parseAnnotations( ClassFileFormat.ATTR_RuntimeVisibleAnnotations,
                          data, offset, constantPool );
      }
      else if( ClassFileFormat.ATTR_Signature.equals( name ) )
      {
        parseSignature( data, offset, constantPool );
      }
      else if( ClassFileFormat.ATTR_Deprecated.equals( name ) )
      {
        parseDeprecated( data, offset, length );
      }
      else if( ClassFileFormat.ATTR_Synthetic.equals( name ) )
      {
        parseSynthetic( data, offset, length );
      }
      else if( ClassFileFormat.ATTR_RuntimeInvisibleAnnotations.equals( name ) )
      {
        parseAnnotations( ClassFileFormat.ATTR_RuntimeInvisibleAnnotations,
                          data, offset, constantPool );
      }
      else if( ClassFileFormat.ATTR_Exceptions.equals( name ) )
      {
        parseExceptions( data, offset, constantPool );
      }
      else if( ClassFileFormat.ATTR_Code.equals( name ) )
      {
        parseCode( data, offset, constantPool );
      }
      else
      {
        System.err.println( "Unhandled method attribute: " + name );
      }
    }

    @Override
    protected void handleFieldAttribute( final String name,
                                         final byte[] data,
                                         final int offset,
                                         final long length,
                                         final ConstantPool constantPool )
    {
      if( ClassFileFormat.ATTR_RuntimeVisibleAnnotations.equals( name ) )
      {
        parseAnnotations( ClassFileFormat.ATTR_RuntimeVisibleAnnotations,
                          data, offset, constantPool );
      }
      else if( ClassFileFormat.ATTR_Signature.equals( name ) )
      {
        parseSignature( data, offset, constantPool );
      }
      else if( ClassFileFormat.ATTR_Deprecated.equals( name ) )
      {
        parseDeprecated( data, offset, length );
      }
      else if( ClassFileFormat.ATTR_Synthetic.equals( name ) )
      {
        parseSynthetic( data, offset, length );
      }
      else if( ClassFileFormat.ATTR_RuntimeInvisibleAnnotations.equals( name ) )
      {
        parseAnnotations( ClassFileFormat.ATTR_RuntimeInvisibleAnnotations,
                          data, offset, constantPool );
      }
      else if( ClassFileFormat.ATTR_ConstantValue.equals( name ) )
      {
        parseConstantValue( data, offset, constantPool );
      }
      else
      {
        System.err.println( "Unhandled field attribute: " + name );
      }
    }

    @Override
    protected void handleField( final String name, final String descriptor, final int accessFlags )
    {
      if( DEBUG )
      {
        System.out.println( "handleField(" + name + "," + descriptor + "," + accessFlags + ")" );
      }
    }

    @Override
    protected void startCode(final int maxStack,
                             final int maxLocals,
                             final byte[] data,
                             final int offset,
                             final long codeLength, final ConstantPool constantPool)
    {
      if( DEBUG )
      {
        System.out.println( "startCode(" + maxStack + "," + maxLocals + "," +
                            data + "," + offset + "," + codeLength + ")" );
      }
    }

    @Override
    protected void handleExceptionHandler( final int startPC,
                                           final int endPC,
                                           final int handlerPC,
                                           final String catchType )
    {
      if( DEBUG )
      {
        System.out.println( "handleExceptionHandler(" + startPC + "," + endPC + "," +
                            handlerPC + "," + catchType + ")" );
      }
    }

    @Override
    protected void handleCodeAttribute( final String name, final byte[] data, final int location, final long length,
                                        final ConstantPool constantPool )
    {

      if( ClassFileFormat.ATTR_LineNumberTable.equals( name ) )
      {
        //
      }
      else if( ClassFileFormat.ATTR_LocalVariableTable.equals( name ) )
      {
        //
      }
      else if( ClassFileFormat.ATTR_LocalVariableTypeTable.equals( name ) )
      {
        //
      }
      else
      {
        System.err.println( "Unhandled attribute: " + name );
      }
    }

    protected void endCode()
    {
      if( DEBUG )
      {
        System.out.println( "endCode()" );
      }
    }

    @Override
    protected void handleExceptions( final String[] exceptions )
    {
      if( DEBUG )
      {
        System.out.println( "Exceptions: " + exceptions.length );
        for( final String exception : exceptions )
        {
          System.out.println( "\t" + exception );
        }
      }
    }

    @Override
    protected void handleConstantValue( final Object value )
    {
      if( DEBUG )
      {
        System.out.println( "handleConstantValue(" + value + ")" );
      }
    }

    @Override
    protected void handleSynthetic()
    {
      if( DEBUG )
      {
        System.out.println( "handleSynthetic()" );
      }
    }

    @Override
    protected void handleDeprecated()
    {
      if( DEBUG )
      {
        System.out.println( "handleDeprecated()" );
      }
    }

    @Override
    protected void handleInnerClass( final String innerClass,
                                     final String outerClass,
                                     final String innerName,
                                     final int innerClassAccessFlags )
    {
      if( DEBUG )
      {
        System.out.println( "handleInnerClass(" +
                            innerClass + "," + outerClass + "," +
                            innerName +
                            "," +
                            innerClassAccessFlags + ")" );
      }
    }

    @Override
    protected void handleSignature( final String signature )
    {
      if( DEBUG )
      {
        System.out.println( "handleSignature(" + signature + ")" );
      }
    }

    @Override
    protected void handleEnclosingMethod( final String klass, final String methodName, final String methodType )
    {
      if( DEBUG )
      {
        System.out.println( "handleEnclosingMethod(" + klass + "," + methodName + "," + methodType + ")" );
      }
    }

    @Override
    protected void handleSourceFile( final String utfEntry )
    {
      if( DEBUG )
      {
        System.out.println( "handleSourceFile(" + utfEntry + ")" );
      }
    }

    @Override
    protected void handleSourceDebug( final String value )
    {
      if( DEBUG )
      {
        System.out.println( "handleSourceDebug(" + value + ")" );
      }
    }

    @Override
    protected void handleAnnotationGroup( final String type )
    {
      if( DEBUG )
      {
        System.out.println( "handleAnnotationGroup(" + type + ")" );
      }
    }

    @Override
    protected void startAnnotation( final String type )
    {
      if( DEBUG )
      {
        System.out.println( "startAnnotation(" + type + ")" );
      }
    }

    @Override
    protected void endAnnotation()
    {
      if( DEBUG )
      {
        System.out.println( "endAnnotation()" );
      }
    }

    @Override
    protected void handleAnnotationValue( final String name, final Object value )
    {
      if( DEBUG )
      {
        System.out.println( "handleAnnotationValue(" + name + "," + value + ")" );
      }
    }

    @Override
    protected void handleAnnotationEnumValue( final String name, final String key, final String value )
    {
      if( DEBUG )
      {
        System.out.println( "handleAnnotationEnumValue(" + name + "," + key + "," + value + ")" );
      }
    }

    @Override
    protected void endAnnotationValueArray()
    {
      if( DEBUG )
      {
        System.out.println( "endAnnotationValueArray()" );
      }
    }

    @Override
    protected void startAnnotationValueArray( final String name, final int length )
    {
      if( DEBUG )
      {
        System.out.println( "startAnnotationValueArray(" + name + "," + length + ")" );
      }
    }
  }

  public static void main( final String[] args )
    throws IOException
  {
    final File jar;
    if( args.length > 0 )
    {
      jar = new File( args[0] );
    }
    else
    {
      final String javaHome = System.getProperty( "java.home" );
      final File home = new File( javaHome );
      final String location = "lib" + File.separator + "rt.jar";
      jar = new File( home, location );
    }
    if( !jar.exists() )
    {
      System.err.println( "Unable to locate file " + jar );
      System.exit( 0 );
    }

    //Garbage collect before we start
    System.gc();
    System.gc();

    final MyHandler handler = new MyHandler();

    for( int i = 0; i < 10; i++ )
    {
      cfParsed = 0;
      long duration = 0;
      System.out.println( "Starting to process jar: " + jar.getCanonicalFile() );
      displayMemory();
      for( int j = 0; j < 30; j++ )
      {
        duration += processJar( jar, handler );
      }
      final double rate = ( (double)cfParsed / duration * 1000 * 1000 );
      final String message =
        "Processed " + cfParsed + " class files in " +
        duration + "ns at a rate of " + rate;
      System.out.println( message );
      displayMemory();
      System.out.println( "GC." );
      System.gc();
      System.gc();
      displayMemory();
    }
  }

  private static void displayMemory()
  {
    final Runtime runtime = Runtime.getRuntime();
    final long freeMemory = runtime.freeMemory();
    final long totalMemory = runtime.totalMemory();
    System.out.println( "Memory Used/Total = " + ( totalMemory - freeMemory ) + "/" + totalMemory );
  }

  private static long processJar( final File jar, final MyHandler handler )
    throws IOException
  {
    final long start = System.nanoTime();
    final JarFile jarFile = new JarFile( jar );
    final Enumeration<JarEntry> enumeration = jarFile.entries();
    while( enumeration.hasMoreElements() )
    {
      final JarEntry entry = enumeration.nextElement();
      final String name = entry.getName();
      if( name.endsWith( ".class" ) )
      {
        processEntry( entry, jarFile, handler );
      }
    }
    jarFile.close();
    final long end = System.nanoTime();
    return end - start;
  }

  private static void processEntry( final JarEntry entry,
                                    final JarFile jarFile,
                                    final MyHandler handler )
    throws IOException
  {
    if( DEBUG )
    {
      System.out.println( "Reading " + entry.getName() );
    }
    final InputStream stream = jarFile.getInputStream( entry );
    final int size = (int)entry.getSize();
    final byte[] bytes = new byte[size];
    int count = 0;
    while( count < size )
    {
      count += stream.read( bytes, count, size - count );
    }
    stream.close();
    if( DEBUG )
    {
      System.out.println( "Parsing " + entry.getName() );
    }
    try
    {
      final ClassFile classFile = ClassFile.parseClassFile( bytes );
      classFile.processClass( handler, true, true, true );
    }
    catch( final Throwable e )
    {
      System.err.println( "Error parsing " + entry.getName() );
      e.printStackTrace( System.err );
      System.exit( 0 );
    }
    cfParsed++;
    if( DEBUG )
    {
      System.out.println( "Completed " + entry.getName() );
    }
  }
}
