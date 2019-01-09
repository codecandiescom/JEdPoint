package JEdPoint;

/**
 * Error handling for JEdPoint.
 * <br><br>
 * Error handling in JEdPoint is done with the following philisophy:<br>
 * <ul>
 *  <li>
 *   Modules should handle all minor errors by themselves. For example:
 *   Missing settings files should be generated from scratch, missing message bases should also be generated from scratch.
 *   Weird values in settings files should be corrected/guessed.
 *  </li>
 *  <li>
 *   If the module is able to function otherwise, but cannot handle just this message, throw a severityError.
 *  </li>
 *  <li>
 *   If the module is unable to function at all, throw a severityFatal, at which point JEdPoint will terminate.
 *  </li>
 * <br><br>
 * @author Edward Hevlund
 * Copyright 2001.
 * Released under the GNU General Public License.
 */
public class JEdPointException extends Exception
{
  /**
   * Fatal - Terminate JEdPoint
   */
  public static final int severityFatal       = 0;
  /**
   * High - Message could not be changed
   */
  public static final int severityError       = 1;

  // Assume that the severity is at least high, since lower severity exceptions
  // should have been handled by the module
  private int severity = this.severityError;

  // The String variables that give us loads of information
  private String whatWentWrong;
  private String possibleCauses;
  private String possibleSolutions;

  Exception baseException;    // The exception we inherit

  /**
   * The constructor for this exception class
   *
   * @param newSeverity How severe is the exception?
   * @param whatWentWrong A description of what you were trying to do when the exception occurred
   * @param possibleCauses Some ideas about what could have caused the exception
   * @param possibleSolutions Suggest some solutions for solving the problem
   */
  public JEdPointException(Exception e,
                            int severity,
                            String newWhatWentWrong,
                            String newPossibleCauses,
                            String newPossibleSolutions)
  {
    this.severity = severity;
    this.baseException = e;
    this.whatWentWrong = newWhatWentWrong;
    this.possibleCauses = newPossibleCauses;
    this.possibleSolutions = newPossibleSolutions;
  }

  /**
   * Returns the severity of this exception
   */
  public int getSeverity()
  {
    return this.severity;
  }

  /**
   * Returns a full block of information about the exception
   */
   public String toString()
   {
      StringBuffer sb = new StringBuffer();
      sb.append("What went wrong:    " + this.whatWentWrong + "\n");
      sb.append("Possible Causes:    " + this.possibleCauses + "\n");
      sb.append("Possible Solutions: " + this.possibleSolutions + "\n");

      // Append the stack trace
      java.io.StringWriter sw = new java.io.StringWriter();
      java.io.BufferedWriter bw = new java.io.BufferedWriter( sw );
      java.io.PrintWriter pw = new java.io.PrintWriter(bw);
      baseException.printStackTrace(pw);
      try
      {
        bw.flush();
        sw.flush();
      }
      catch (Exception e)
      {
      }

      sb.append("Exception:          " + sw.getBuffer().toString() );

      return sb.toString();
   }
}