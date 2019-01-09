package JEdPoint;

import java.util.*;

/**
 * A collection of util methods used in conjuntion with the FidonetMessage class.
 * @author Edward Hevlund
 * Copyright 2001.
 * Released under the GNU General Public License.
 */

public class FidonetMessageUtils
{
  /**
   * The Static
   *
   * MSGIDs are supposed to be unique for three years. How do we ensure that they
   * are unique?
   *
   * 1) We could use a global counter that is incremented each time we need to
   * generate a msgid. That's possible, unless this counter were to be reset by
   * reinstalling the program or whatever. That's bad, m'kay?
   *
   * 2) We could count the number of seconds since a specific date. That would be
   * ok, unless one were to generate two msgids within a second. That would make
   * both msgids equal to each other.
   *
   * 3) We could count the number of milliseconds since a specific date. That would
   * make the most sense and would almost guarantee you a unique msgid. Unfortunately
   * for us, we only have a 32bit hex code for this field. And the # of milliseconds
   * since date xx easily get over 32 bit.
   *
   * What are we to do?
   *
   * 1 - If the counter gets reset, we're fucked.
   * 3 - We don't have a 64bit hex field. We're fucked.
   * 2 - We can only generate one ID per second.
   *
   * Well, since 1 and 3 are out, we only have 2 left. In order to remember when the
   * last time was that one generated a msgid, we make this variable static (meaning
   * that it always exists, as long as this class exists, in exactly the same space
   * in memory). Then we new() this class somewhere in the main program so that it
   * will exist for the duration of this program.
   *
   * That's it. Now take a look at generateMSGID to see how I worked out how long
   * I must wait before generating a new msgid.
   */
  public static GregorianCalendar msgIDLastCalled = new GregorianCalendar();

  /**
   * The Processor ID
   */
  private static String PID;

  public void setPID( String PID )
  {
    this.PID = PID;
  }

  public String getPID()
  {
    return this.PID;
  }

  /**
   * Generates a completely new message.
   *
   * @param usernameFrom Name of the user who has sent this message
   * @param pointFrom PointNumber of the user who has sent this message
   * @param usernameTo Name of the user who will receive this message
   * @param pointTo PointNumber of the user who will receive this message
   * @param tearline Tearline to use
   * @param origin Origin to use
   */
  public static FidonetMessage newMessage(String usernameFrom,
                                          PointNumber pointFrom,
                                          String usernameTo,
                                          PointNumber pointTo,
                                          String subject,
                                          String tearline,
                                          String origin)
  {
    Vector tempVector;
    FidonetMessage returnFM = new FidonetMessage();

    returnFM.setMessageData("usernamefrom", usernameFrom);
    returnFM.setMessageData("usernameto", usernameTo );
    returnFM.setMessageData("pointfrom", pointFrom );
    returnFM.setMessageData("pointto", pointTo );
    returnFM.setMessageData("msgid", generateMSGID() );

    returnFM = generateAttributes(returnFM);

    returnFM.setMessageData( "datetime", new GregorianCalendar() );
    returnFM.setMessageData( "subject", subject );

    tempVector = new Vector();
    // Add the msgid to the prekludges
    tempVector.add( "\u263AMSGID: " + pointFrom.toString() + " " + returnFM.getMessageDataString("msgid") );

    // Add a PID to the prekludges
    tempVector.add( generatePID() );

    returnFM.setMessageData( "prekludges", tempVector );

    returnFM.setMessageData( "message", "" );
    returnFM.setMessageData( "tearline", tearline );
    returnFM.setMessageData( "origin",origin );

    tempVector = new Vector();
    returnFM.setMessageData( "postkludges", tempVector );

    return returnFM;
  }

  /**
   * Generates a complete reply message.
   *
   * @param fm The original FidonetMessage
   * @param replyFrom Name of the user who has sent this reply
   * @param pointFrom PointNumber of the user who has sent this reply
   * @param replyTo Name of the user who will receive this reply
   * @param pointTo PointNumber of the user who will receive this reply
   * @param tearline Tearline to use
   * @param origin Origin to use
   */
  public static FidonetMessage reply(FidonetMessage fm,
                                      String replyFrom,
                                      PointNumber pointFrom,
                                      String replyTo,
                                      PointNumber pointTo,
                                      String tearline,
                                      String origin)
  {
    Vector tempVector;
    String replyString;

    // Generate a reply kludge
    replyString = "\u263AREPLY: " + pointTo.toString() + " " + fm.getMessageDataString("msgid");

    FidonetMessage returnFM = fm;

    returnFM.setMessageData( "usernamefrom", replyFrom );
    returnFM.setMessageData( "pointfrom", pointFrom );
    returnFM.setMessageData( "usernameto", replyTo );
    returnFM.setMessageData( "pointto", pointTo );

    returnFM.setMessageData( "msgid", generateMSGID() );

    returnFM = generateAttributes(returnFM);

    returnFM.setMessageData( "datetime", new GregorianCalendar() );
    returnFM.setMessageData( "subject", fm.getMessageData("subject") );

    tempVector = new Vector();
    // Add the msgid to the prekludges
    tempVector.add( "\u263AMSGID: " + pointFrom.toString() + " " + returnFM.getMessageDataString("msgid") );
    // Add the reply kludge to it
    tempVector.add(replyString);

    // Add a PID to the prekludges
    tempVector.add( generatePID() );
    returnFM.setMessageData( "prekludges", tempVector );

    returnFM.setMessageData( "message", quote((String)fm.getMessageData("message"), replyTo, replyFrom) );
    returnFM.setMessageData( "tearline", tearline );
    returnFM.setMessageData( "origin",origin );

    tempVector = new Vector();
    returnFM.setMessageData( "postkludges", tempVector );

    return returnFM;
  }

  /**
   * Generates a msgID.
   */
  public static String generateMSGID()
  {
    long timeNow = new GregorianCalendar().getInstance().getTime().getTime();
    long timeLast = msgIDLastCalled.getTime().getTime();

    long timeToSleep = Math.max( 0, 1000 - Math.abs((timeLast - timeNow)) );

    try
    {
      Thread.sleep(timeToSleep);
    }
    catch (Exception e)
    {
    }

    msgIDLastCalled = new GregorianCalendar();
    String returnString = Long.toHexString( msgIDLastCalled.getTime().getTime() / 1000 );
    while (returnString.length() < 8)
      returnString = "0" + returnString;
    return returnString;
  }

  private static String generatePID()
  {
    return "\u263APID: " + PID;
  }

  /**
   * Generates all the attributes as specified in the spec.
   *
   * Will generate only the attributes and they won't have any valid information.
   */
  private static FidonetMessage generateAttributes(FidonetMessage fm)
  {
    // Currently there aren't any attributes that aren't handled by the message base.
    return fm;
  }

  private static String quote(String toQuote, String fromName, String toName)
  {
    StringBuffer sb = new StringBuffer();
    String initials = "";
    String tempstring = "";
    String originalText = new String(toQuote);
    StringTokenizer st;

    // Get the initials first
    st = new StringTokenizer(fromName);
    while (st.hasMoreTokens())
      initials = initials + st.nextToken().substring(0, 1);

    // Take one line at a time
    while (originalText.length() != 0)
    {
      // Add "> " to the already quoted text.
      if (originalText.charAt( originalText.length()-1 ) != '\n' )
        originalText = originalText + '\n';

      tempstring = originalText.substring(0, originalText.indexOf("\n") );
      originalText = originalText.substring( originalText.indexOf("\n")+1, originalText.length() );

      if (tempstring.trim().length() != 0)
      {
        if ( tempstring.indexOf("> ") != -1 )
        {
          sb.append( tempstring.substring(0, tempstring.indexOf("> "))
            + ">"
            + tempstring.substring( tempstring.indexOf("> "), tempstring.length() )
            );
        }
        else
        {
          // Initials + "> " + the line
          sb.append( initials + "> " + tempstring );
        }
      }
      else
        sb.append("");

      sb.append("\n");
    }

    return sb.toString();
  }

  /**
   * Extracts all the prekludges from a string.
   *
   * @return The codepage from the CHRS kludge
   */
  public static String extractCHRS(String text, String defaultCodepage)
  {
    int index = -1;
    int counter;
    String CHRSKludges[] = {
      "\u263ACHRS: ", "\u0001CHRS: ",
      "\u263ACODEPAGE: ", "\u0001CODEPAGE: ",
      "\u263ACHARSET: ", "\u0001CHARSET: " };

    try
    {
      String codepage;

      for (counter=0; counter<CHRSKludges.length; counter++)
      {
        if ( text.indexOf( CHRSKludges[counter] ) != -1)
          index = text.indexOf( CHRSKludges[counter] ) + CHRSKludges[counter].length();
      }

      if ( index != -1 )
      {
        // Extract the codepage.
        codepage = text.substring( index );
        if (codepage.indexOf(" ") != -1)
          codepage = codepage.substring( 0, codepage.indexOf(" ") );
        if (codepage.indexOf("\n") != -1)
          codepage = codepage.substring( 0, codepage.indexOf("\n") );
        if (codepage.indexOf("\r") != -1)
          codepage = codepage.substring( 0, codepage.indexOf("\r") );
        return codepage;
      }
    }
    catch (Exception e)
    {
    }

    return defaultCodepage;
  }

  /**
   * Extracts all the prekludges from a string.
   *
   * @return A vector full of strings (kludges).
   */
  public static Vector extractPreKludges(String text)
  {
    Vector returnVector = new Vector();
    String tempstring = new String(text);
    int counter;
    for (counter=0; counter<FidonetMessage.preKludges.length; counter++)
    {
      while ( JEdPointUtilities.getLine(tempstring, FidonetMessage.preKludges[counter]) != "" )
      {
        returnVector.add( JEdPointUtilities.getLine(tempstring, FidonetMessage.preKludges[counter]) );
        tempstring = JEdPointUtilities.removeLine(tempstring, FidonetMessage.preKludges[counter]);
      }
    }
    return returnVector;
  }

  /**
   * Extracts all the postkludges from a string.
   *
   * @return A vector full of strings (kludges).
   */
  public static Vector extractPostKludges(String text)
  {
    Vector returnVector = new Vector();
    int counter;
    String tempString;

    // Because of the fact that "Via xxxxxx" is a kludge that DOESN'T have a smiley
    // in front of it, it could just as well exist in the text itself. To save ourselves
    // from this stupidity (yes, the Fidonet message standards are _all_ stupid) we
    // shall try to extract everything that _after_ the tearline or originline and
    // search through that.

    if (text.lastIndexOf(" * Origin: ") != -1)
      tempString = text.substring( text.lastIndexOf(" * Origin: ") );
    else
      if (text.lastIndexOf("---") != -1)
        tempString = text.substring( text.lastIndexOf("---") );
      else
        tempString = new String(text);

    for (counter=0; counter<FidonetMessage.postKludges.length; counter++)
    {
      while ( JEdPointUtilities.getLine(tempString, FidonetMessage.postKludges[counter]) != "" )
      {
        returnVector.add( JEdPointUtilities.getLine(tempString, FidonetMessage.postKludges[counter]) );
        tempString = JEdPointUtilities.removeLine(tempString, FidonetMessage.postKludges[counter]);
      }
    }
    return returnVector;
  }

  /**
   * Extracts the tearline from a string.
   *
   * @return The tearline, or just "" if there wasn't one
   */
  public static String extractTearline(String text)
  {
    String returnString;

    if ( text.lastIndexOf("---") != -1)
    {
      // Use the Origin to assist us in getting the valid "---" string.
      // Since there could be several, all over the place...
      if ( text.indexOf(" * Origin: ") != -1 )
      {
        returnString = text.substring(0, text.lastIndexOf(" * Origin: ") );
        returnString = returnString.substring( returnString.lastIndexOf("---") + 3, returnString.lastIndexOf("\n") );
      }
      else
      {
        returnString = text.substring( text.lastIndexOf("---") );
        returnString = returnString.substring( returnString.indexOf("---")+3, returnString.indexOf("\n") );
      }
    }
    else
      returnString = "";

    return returnString.trim();
  }

  /**
   * Extracts the origin from a string.
   *
   * @return The origin, or just "" if there wasn't one
   */
  public static String extractOrigin(String text)
  {
    String returnString;

    if ( text.lastIndexOf(" * Origin: ") != -1)
    {
      returnString = JEdPointUtilities.getLastLine(text, " * Origin: ");
      returnString = returnString.substring(11);

      // We don't want the point number there, so check to see if it's there and get rid of it.
      if (returnString.lastIndexOf("(")  != -1)
        returnString = returnString.substring(0, returnString.lastIndexOf("("));
    }
    else
      returnString = "";

    return returnString.trim();
  }

  /**
   * Extracts the message body from a string.
   *
   * @return The message body, or just "" if there wasn't one
   */
  public static String extractMessageBody(String text)
  {
    int counter;
    Vector postKludges;
    String returnString = new String(text);
    String beforeLine, afterLine;

    // Remove all the prekludges
    for (counter=0; counter<FidonetMessage.preKludges.length; counter++)
    {
      while (returnString.indexOf(FidonetMessage.preKludges[counter]) != -1)
        returnString = JEdPointUtilities.removeLine(returnString, FidonetMessage.preKludges[counter]);
    }

    // Get a list of all the postkludges and then remove them from the message text
    postKludges = extractPostKludges(text);
    for (counter=0; counter<postKludges.size(); counter++)
      returnString = JEdPointUtilities.removeLastLine(returnString, (String)postKludges.elementAt(counter));

    // Remove the tearline

    // Now... for some strange reason, some programs seem to think that there is
    // no need to follow standards and have a tearline marker in the message.
    // So I have to make yet ANOTHER exception for stupid programs like that.
    // I'm not gonna mention which piece of shit programs do crap like that...
    // ... except for FDAPX.
    if ( returnString.indexOf("---") != -1)
    {
      if ( returnString.lastIndexOf(" * Origin: ") != -1)
      {
        beforeLine = returnString.substring(0, returnString.lastIndexOf(" * Origin: ") );
        beforeLine = beforeLine.substring(0, beforeLine.lastIndexOf("---") );

        afterLine = returnString.substring( returnString.lastIndexOf(" * Origin: ") );

        returnString = beforeLine + afterLine;
      }
      else
      {
        beforeLine = returnString.substring(0, returnString.lastIndexOf("---") );

        afterLine = returnString.substring( returnString.lastIndexOf("---") );
        afterLine = afterLine.substring( afterLine.indexOf("\n") );

        returnString = beforeLine + afterLine;
      }
    }

    // Remove the origin
    returnString = JEdPointUtilities.removeLastLine(returnString, " * Origin: " );


    return returnString;
  }
}