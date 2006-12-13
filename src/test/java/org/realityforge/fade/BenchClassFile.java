package org.realityforge.fade;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class BenchClassFile
{
  private static final boolean DEBUG = false;
  private static int cfParsed = 0;

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

    for( int i = 0; i < 4; i++ )
    {
      cfParsed = 0;
      long duration = 0;
      System.out.println( "Starting to process jar." );
      displayMemory();
      for( int j = 0; j < 30; j++ )
      {
        duration += processJar( jar );
      }
      final double rate = ( (double)cfParsed / duration * 1000 * 1000 );
      final String message =
        "Processed " + cfParsed + " class files in " +
        duration + "ns at a rate of " + rate;
      System.out.println( message );
      displayMemory();
      if( true )
      {
        System.out.println( "GC." );
        System.gc();
        System.gc();
        displayMemory();
      }
    }
  }

  private static void displayMemory()
  {
    final Runtime runtime = Runtime.getRuntime();
    final long freeMemory = runtime.freeMemory();
    final long totalMemory = runtime.totalMemory();
    System.out.println( "Memory Used/Total = " + (totalMemory-freeMemory) + "/" + totalMemory );
  }

  private static long processJar( final File jar )
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
        processEntry( entry, jarFile );
      }
    }
    jarFile.close();
    final long end = System.nanoTime();
    return end - start;
  }

  private static void processEntry( final JarEntry entry, final JarFile jarFile )
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
    ClassFile.parseClassFile( bytes );
    cfParsed++;
    if( DEBUG )
    {
      System.out.println( "Completed " + entry.getName() );
    }
  }
}
