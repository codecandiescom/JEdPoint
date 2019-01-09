package JEdPoint;

import java.util.*;

/**
 * A Fidonet message.
 *
 * Contains all the information that comes out of the .pkt.
 * The data is stored as a HashMap full of objects, so you'll have to be careful
 * as to what you set/get. Other than that it's straighforward.
 * <pre>
 *  Key (String)                    Type              Version     Notes
 * -----------------------------------------------------------------------------
 * Message Header starts here --------------------------------------------------
 * usernamefrom                   String              1
 * usernameto                     String              1
 * pointfrom                      PointNumber         1
 * pointto                        PointNumber         1
 * msgid                          String              1
 *
 * attributeexport                Boolean             1           5
 *  True    Message is marked to be exported
 *  False   It has already been exported
 *
 * attributepersonal              Boolean             1           5
 *  True    This message is for the current user (the username and to-name match)
 *  False   The opposite of true.
 *
 * attributenewlyimported         Boolean             1           5
 *  True    This message was in the last batch of messages to be imported.
 *  False   There are messages that have been imported later than this one.
 *
 * attributeread                  Boolean             1           5
 *  True    The user has read this message in the viewer.
 *  False   The opposite of true.
 *
 * attributedelete                Boolean             1           5
 *  True    This message is marked for deletion
 *  False   The opposite of true.
 *
 * datetime                       GregorianCalendar   1
 * datetimeimported               GregorianCalendar   1           5
 * subject                        String              1
 *
 * End of Message Header
 *
 * Message Body Starts Here
 *
 * prekludges                     Vector              1           1, 4
 * message                        String              1
 * tearline                       String              1           2
 * origin                         String              1           2
 * postkludges                    Vector              1           3, 4
 *
 * End of Message Body----------------------------------------------------------
 *
 * Notes
 * -----
 * 1) All the kludges that are put before the message text are put here
 * 2) Contain only the text, not the --- or * or point number.
 * 3) All the kludges that are put after the message text are put here
 * 4) A Vector of Strings
 * 5) This attribute is maintained by the messagebase.
 * </pre>
 */
public class FidonetMessage implements java.io.Serializable
{
  // A list of all valid fields.
  // This is used when creating new FidonetMessages from other FidonetMessages
  public static final String fields[][] = {
    {
      // This is a place holder
    },
    { // VERSION 1
      "usernamefrom",
      "usernameto",
      "pointfrom",
      "pointto",
      "msgid",
      "attributeexport",
      "attributepersonal",
      "attributenewlyimported",
      "attributeread",
      "attributedelete",
      "datetime",
      "datetimeimported",
      "subject",
      "prekludges",
      "message",
      "tearline",
      "origin",
      "postkludges"
    }
  };

  public static final String fieldsHeader[][] = {
    {
      // This is a place holder
    },
    { // VERSION 1
      "usernamefrom",
      "usernameto",
      "pointfrom",
      "pointto",
      "msgid",
      "attributeexport",
      "attributepersonal",
      "attributenewlyimported",
      "attributeread",
      "attributedelete",
      "datetime",
      "datetimeimported",
      "subject",
    }
  };

  public static final String fieldsBody[][] = {
    {
      // This is a place holder
    },
    { // VERSION 1
      "prekludges",
      "message",
      "tearline",
      "origin",
      "postkludges"
    }
  };

  public static final int version = 1;

  private HashMap messageData = new HashMap(fields.length);

  // Some constants
  public static final String preKludges[] = {
    "\u263ACHRS:",
    "\u263ACODEPAGE:",
    "\u263ACHARSET:",
    "\u263AFLAGS ",
    "\u263AFMPT ",
    "\u263AGATEWAY:",
    "\u263AGMD:",    // Dunno what this is
    "\u263AINTL",
    "\u263AMSGID:",
    "\u263APID:",
    "\u263AREPLY:",
    "\u263AREPLYADDR ",
    "\u263AREPLYTO ",
    "\u263ARFC:",
    "\u263ATID:",
    "\u263ATOPT ",
    "\u263ATZUTC:"
    };

  public static final String postKludges[] = {
    "SEEN-BY:",
    "\u263APATH:",
    "Via "
    };

  /**
   * Generates a completely empty FidonetMessage. It DOES have all the fields initialized.
   */
  public FidonetMessage()
  {
    setMessageData("usernamefrom", "" );
    setMessageData("usernameto", "" );
    setMessageData("pointfrom", new PointNumber(0,0,0,0) );
    setMessageData("pointto", new PointNumber(0,0,0,0) );
    setMessageData("msgid", "" );

    setMessageData("datetime", new GregorianCalendar() );
    setMessageData("datetimeimported", new GregorianCalendar() );
    setMessageData("subject", "" );
    setMessageData("prekludges", new Vector() );
    setMessageData("message", "" );
    setMessageData("tearline", "" );
    setMessageData("origin", "" );
    setMessageData("postkludges", new Vector() );

    setMessageData("attributeexport", false );
    setMessageData("attributepersonal", false );
    setMessageData("attributenewlyimported", false );
    setMessageData("attributeread", false );
    setMessageData("attributedelete", false );
  }

  /**
   * Create a FidonetMessage from another FidonetMessage.
   */
  public FidonetMessage(FidonetMessage newFM)
  {
    int counter;

    for (counter=0; counter<fields[version].length; counter++)
    {
      messageData.put( fields[version][counter], newFM.getMessageData(fields[version][counter]));
    }
  }

  // ---------------------------------------------------------------------------
  // GET
  // ---------------------------------------------------------------------------
  public Object getMessageData(String key)
  {
    return this.messageData.get(key);
  }

  public boolean getMessageDataBoolean(String key)
  {
    Boolean bool = (Boolean)messageData.get(key);
    return bool.booleanValue();
  }

  public int getMessageDataInteger(String key)
  {
    Integer integer = (Integer)messageData.get(key);
    return integer.intValue();
  }

  public long getMessageDataLong(String key)
  {
    Long tempLong = (Long)messageData.get(key);
    return tempLong.longValue();
  }

  public String getMessageDataString(String key)
  {
    return (String)messageData.get(key);
  }

  // ---------------------------------------------------------------------------
  // SET
  // ---------------------------------------------------------------------------
  public void setMessageData(String newKey, Object newValue)
  {
    this.messageData.put(newKey, newValue);
  }

  public void setMessageData(String newKey, boolean newBoolean)
  {
    this.messageData.put(newKey, new Boolean(newBoolean));
  }

  public void setMessageData(String newKey, int newInteger)
  {
    this.messageData.put(newKey, new Integer(newInteger));
  }

  public void setMessageData(String newKey, long newLong)
  {
    this.messageData.put(newKey, new Long(newLong));
  }
}