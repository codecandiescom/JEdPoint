package JEdPoint;

import java.util.HashMap;

/**
 * A Message Area.
 *
 * Contains the information for a specific area.
 * The data is stored as a HashMap full of objects, so you'll have to be careful
 * as to what you set/get. Other than that it's straighforward.
 *
 * There are two types of keys. R/W keys that the MessageBase can use to change
 * the area settings, and then Read Only keys that the MessageBase generates.
 *
 * <pre>
 *  Key (String)                    Type              Version     Notes
 *    Descrption
 * -----------------------------------------------------------------------------
 * Read / Write keys
 * -----------------
 * id                             String              1
 *    The area's ID / tag
 * description                    String              1
 *    A short description of the area
 * tearline                       String              1
 *    The tagline (or tagline file) to be used
 * origin                         String              1
 *    The origin (or origin file) to be used
 * readonly                       Boolean             1
 *    Is the user allowed to start new threads?
 * replyin                        String              1
 *    In which other area (if any) should replies automatically be placed?
 *
 * Read Only Keys Start Here
 * -------------------------------
 * totalmessages                  Long                1
 *    Total # of messages in the area
 * totalmessagesunread            Long                1
 *    Total # of unread messages
 * personalmessagestotal          Long                1
 *    Personal messages
 * personalmessagesunread         Long                1
 *    Unread personal messages
 * newmessagestotal               Long                1
 *    Newly imported messages
 * newmessagesunread              Long                1
 *    Newly imported messages not yet read
 * newpersonalmessagestotal       Long                1
 *    Newly imported personal messages (total)
 * newpersonalmessagesunread      Long                1
 *    Newly imported personal messages not yet read
 * lastread                       Long                1
 *    The last message read by the user
 * exportqueue                    Long                1
 *    Messages that are scheduled to be exported
 * deletequeue                    Long                1
 *    Messages that are be deleted at the next pack
 *
 *
 * Notes
 * -----
 * </pre>
 */
public class MessageAreaData implements java.io.Serializable
{
  private HashMap messageAreaData;

  /**
   * Complete list of fields as of this version of MessageAreaData
   */
  public static final String fields[][] = {
    {
      // This is a place holder
    },
    { // VERSION 1
      // RW
      "id",
      "description",
      "tearline",
      "origin",
      "readonly",
      "replyin",
      // RO
      "lastread",
      "deletequeue",
      "exportqueue",
      "totalmessages",
      "totalmessageunread",
      "personalmessagestotal",
      "personalmessagesunread",
      "newmessagestotal",
      "newmessagesunread",
      "newpersonalmessagestotal",
      "newpersonalmessagesunread"
    }
  };

  /**
   * List of read/write fields as of this version of MessageAreaData
   */
  public static final String fieldsrw[][] = {
    {
      // This is a place holder
    },
    { // VERSION 1
      "id",
      "description",
      "tearline",
      "origin",
      "readonly",
      "replyin",
    }
  };

  /**
   * List of read only fields as of this version of MessageAreaData
   */
  public static final String fieldsro[][] = {
    {
      // This is a place holder
    },
    { // VERSION 1
      "lastread",
      "deletequeue",
      "exportqueue",
      "totalmessages",
      "totalmessageunread",
      "personalmessagestotal",
      "personalmessagesunread",
      "newmessagestotal",
      "newmessagesunread",
      "newpersonalmessagestotal",
      "newpersonalmessagesunread"
    }
  };

  public static final int version = 1;

  public MessageAreaData()
  {
    messageAreaData = new HashMap( fields[version].length );
    setMessageAreaData("id", "");
    setMessageAreaData("description", "");
    setMessageAreaData("tearline", "");
    setMessageAreaData("origin", "");
    setMessageAreaData("replyin", "");
    setMessageAreaData("readonly", new Boolean(false));

    setMessageAreaData("lastread", -1L );
    setMessageAreaData("deletequeue", 0L );
    setMessageAreaData("exportqueue", 0L );
    setMessageAreaData("totalmessages", 0L );
    setMessageAreaData("totalmessagesunread", 0L );
    setMessageAreaData("personalmessagestotal", 0L );
    setMessageAreaData("personalmessagesunread", 0L );
    setMessageAreaData("newmessagestotal", 0L );
    setMessageAreaData("newmessagesunread", 0L );
    setMessageAreaData("newpersonalmessagestotal", 0L );
    setMessageAreaData("newpersonalmessagesunread", 0L );
  }

  public MessageAreaData( MessageAreaData dataToClone )
  {
    int counter;

    messageAreaData = new HashMap( fields[version].length );

    for (counter=0; counter<fields[version].length; counter++)
      this.setMessageAreaData( fields[version][counter], dataToClone.getMessageAreaData(fields[version][counter]) );
  }

  public Object getMessageAreaData(String key)
  {
    return this.messageAreaData.get(key);
  }

  public String getMessageAreaDataString(String key)
  {
    return (String)this.messageAreaData.get(key);
  }

  public int getMessageAreaDataInt(String key)
  {
    Integer tempInt = (Integer) this.messageAreaData.get(key);
    return tempInt.intValue();
  }

  public long getMessageAreaDataLong(String key)
  {
    Long tempLong = (Long) this.messageAreaData.get(key);
    return tempLong.longValue();
  }

  public boolean getMessageAreaDataBoolean(String key)
  {
    Boolean returnBool = (Boolean) this.messageAreaData.get(key);
    return returnBool.booleanValue();
  }

  public void setMessageAreaData(String newKey, Object newValue)
  {
    this.messageAreaData.put(newKey, newValue);
  }

  public void setMessageAreaData(String newKey, String newValue)
  {
    this.messageAreaData.put(newKey, newValue);
  }

  public void setMessageAreaData(String newKey, boolean newValue)
  {
    this.messageAreaData.put(newKey, new Boolean(newValue));
  }

  public void setMessageAreaData(String newKey, int newValue)
  {
    this.messageAreaData.put(newKey, new Integer(newValue));
  }

  public void setMessageAreaData(String newKey, long newValue)
  {
    this.messageAreaData.put(newKey, new Long(newValue));
  }

  public void clearAll()
  {
    this.messageAreaData.clear();
  }

  public void clear(String key)
  {
    this.messageAreaData.remove(key);
  }
}
