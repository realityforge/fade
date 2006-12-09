package org.realityforge.fade.data;

public abstract class NonEmptyClass
  implements MyInterface
{
  private static final int ANSWER = 42;
  protected String m_question = "What is 6 x 9 in base 13?";

  {
    float f = 32.5F;
    long l = 3;
    char c = 'x';
    boolean b = true;
    char[] chs = {'x', 'y'};
  }

  {
    double d = 55.0;
    short s = 2;
  }

  public NonEmptyClass( final String question )
  {
    m_question = question;
  }

  public void setQuestion( final String question )
  {
    m_question = question;
  }

  protected void doMagic( final MyInterface myInterface )
  {
    myInterface.runJumpSkipAndPlay();
  }

  public static int getAnswer()
  {
    return ANSWER;
  }

  public final String toString()
  {
    return "Foo " + super.toString();
  }
}
