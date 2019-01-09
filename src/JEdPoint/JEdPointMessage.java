package JEdPoint;

import java.util.HashMap;

/**
  * Message class used by modules to transfer data back and forth.
  * <br><br>
  * JEdPoint modules communicate with each other using this class.
  * Since this class uses hashmaps to store data internally, you could view this class as a hashmap, using keys and values.
  * The keys are Strings (always) and the values can be anything. Hashmaps can store any form of data in any amount.
  * <br><br>
  * <b>How messaging works</b><br>
  * <br>
  * A message consists of two parts: the <b>request</b> and the <b>response</b>.
  * <br><br>
  * When sending a message, first the messager sender will fill in any necessary data in the request part of the message.
  * This is done using setRequest(). Some messages require more request settings than others, while some messages require no request settings at all.
  * The message is then routed away to the destination module.
  * <br><br>
  * Having received the message, the destination module will read the request data (if required) using getRequest(). It then does whatever
  * it is supposed to do (as dictated by the message type, which is stored in the request) and then fills in the response data if required.
  * The message is then routed (or returned, rather) back to the source module.
  * <br><br>
  * Now, depending on what the destination module was instructed to do, the source module could read the response data using getResponse().
  * If there was an error in the processing module, a <b>null</b> message will be received.
  *
  * @author Edward Hevlund
  * Copyright 2001.
  * Released under the GNU General Public License.
*/
public class JEdPointMessage
{
  // ---------------------------------------------------------------------------
  // MESSAGE TYPES
  // ---------------------------------------------------------------------------
  // COMMON
  public static final int moduleGetInformation              =     1;
  public static final int moduleInit                        =     2;
  public static final int moduleDeInit                      =     3;
  // MICROKERNEL
  public static final int mkGetSettings                     = 10001;
  public static final int mkParseSetting                    = 10002;
  // LOG
  public static final int logSetLogfile                     = 11001;
  public static final int logDebug                          = 11002;
  public static final int logInfo                           = 11003;
  public static final int logWarning                        = 11004;
  public static final int logError                          = 11005;
  public static final int logFatal                          = 11006;
  // MESSAGEBASE
  public static final int mbAreaExists                      = 12001;
  public static final int mbAddArea                         = 12002;
  public static final int mbChangeArea                      = 12003;
  public static final int mbGetAreaInformation              = 12004;
  public static final int mbRemoveArea                      = 12005;
  public static final int mbListAreas                       = 12006;
  public static final int mbAddMessage                      = 12007;
  public static final int mbGetMessage                      = 12008;
  public static final int mbGetMessageHeader                = 12009;
  public static final int mbInsertMessage                   = 12010;
  public static final int mbReadMessage                     = 12011;
  public static final int mbChangeMessage                   = 12012;
  public static final int mbReplaceMessage                  = 12013;
  public static final int mbReplaceMessageHeader            = 12014;
  public static final int mbSetMessageAttributes            = 12015;
  public static final int mbGetMessageHeaders               = 12016;
  public static final int mbImportMessage                   = 12017;
  public static final int mbExportMessage                   = 12018;
  public static final int mbCatchUpArea                     = 12019;
  public static final int mbWriteMessage                    = 12020;
  public static final int mbClearImported                   = 12021;
  public static final int mbDeleteMessage                   = 12022;
  public static final int mbPackArea                        = 12023;
  public static final int mbListMessagesDelete              = 12024;
  public static final int mbListMessagesExport              = 12025;
  public static final int mbListMessagesNew                 = 12026;
  public static final int mbListMessagesNewUnread           = 12027;
  public static final int mbListMessagesPersonal            = 12028;
  public static final int mbListMessagesPersonalUnread      = 12029;
  public static final int mbListMessagesPersonalNew         = 12030;
  public static final int mbListMessagesPersonalNewUnread   = 12031;
  public static final int mbListMessagesUnread              = 12032;
  // TEARLINE
  public static final int tlGetTearline                     = 13001;
  public static final int tlAssemble                        = 13002;
  // ORIGIN
  public static final int originGetOrigin                   = 14001;
  public static final int originAssemble                    = 14002;
  // POLL
  public static final int pollSendMail                      = 15001;
  public static final int pollGetMail                       = 15002;
  public static final int pollFullPoll                      = 15003;
  public static final int pollGetStatus                     = 15004;
  public static final int pollCancel                        = 15005;
  // UI
  public static final int uiGetStatus                       = 16001;
  public static final int uiStartUI                         = 16002;
  public static final int uiDisplayJOptionPane              = 16003;
//  public static final int uiDisplayError                    = 16003;
  // MESSAGEEDITOR
  public static final int meGetStatus                       = 17001;
  public static final int meEditMessage                     = 17002;
  // IMPORT
  public static final int importStart                       = 18001;
  public static final int importCancel                      = 18002;
  public static final int importGetStatus                   = 18003;
  // EXPORT
  public static final int exportStart                       = 19001;
  public static final int exportCancel                      = 19002;
  public static final int exportGetStatus                   = 19003;


  // So I don't have to change the value everywhere manually.
  private static final int defaultHashMapSize = 5;

  // Modules communicate with requests and responses.
  private HashMap request;
  private HashMap response;

  /** Generates an empty message */
  public JEdPointMessage()
  {
    request = new HashMap(defaultHashMapSize);
    response = new HashMap(defaultHashMapSize);
  }

  /** Generates a message with one Key+Value pair set */
  public JEdPointMessage(String newKey, Object newValue)
  {
    this();
    this.setRequest(newKey, newValue);
  }

  /** Generates a message with two Key+Value pair sets */
  public JEdPointMessage(String newKey1, Object newValue1, String newKey2, Object newValue2)
  {
    this();
    this.setRequest(newKey1, newValue1);
    this.setRequest(newKey2, newValue2);
  }

  /** Sets a key in the request */
  public void setRequest(String newKey, Object newValue)
  {
    this.request.put(newKey, newValue);
  }

  /** Sets a key in the request */
  public void setRequest(String newKey, String newValue)
  {
    this.request.put(newKey, (String)newValue);
  }

  /** Sets a key in the request */
  public void setRequest(String newKey, int newValue)
  {
    this.request.put(newKey, new Integer(newValue));
  }

  /** Sets a key in the request */
  public void setRequest(String newKey, long newValue)
  {
    this.request.put(newKey, new Long(newValue));
  }

  /** Sets a key in the request */
  public void setRequest(String newKey, boolean newValue)
  {
    this.request.put(newKey, new Boolean(newValue));
  }

  /** Retrieves a key from the request */
  public Object getRequest(String newKey)
  {
    return this.request.get(newKey);
  }

  /** Retrieves a key from the request */
  public String getRequestString(String newKey)
  {
    return (String)this.request.get(newKey);
  }

  /** Retrieves a key from the request */
  public int getRequestInt(String newKey)
  {
    Integer i = (Integer)this.request.get(newKey);
    return i.intValue();
  }

  /** Retrieves a key from the request */
  public long getRequestLong(String newKey)
  {
    Long l = (Long)this.request.get(newKey);
    return l.longValue();
  }

  /** Retrieves a key from the request */
  public boolean getRequestBoolean(String newKey)
  {
    Boolean b = (Boolean)this.request.get(newKey);
    return b.booleanValue();
  }

  /** Removes a key from the request */
  public void removeRequest(String keyToRemove)
  {
    this.request.remove(keyToRemove);
  }

  /** Sets a key in the response */
  public void setResponse(String newKey, Object newValue)
  {
    this.response.put(newKey, newValue);
  }

  /** Sets a key in the response */
  public void setResponse(String newKey, String newValue)
  {
    this.response.put(newKey, newValue);
  }

  /** Sets a key in the response */
  public void setResponse(String newKey, int newValue)
  {
    this.response.put(newKey, new Integer(newValue));
  }

  /** Sets a key in the response */
  public void setResponse(String newKey, boolean newValue)
  {
    this.response.put(newKey, new Boolean(newValue));
  }

  /** Sets a key in the response */
  public void setResponse(String newKey, long newValue)
  {
    this.response.put(newKey, new Long(newValue));
  }

  /** Retrieves a key from the response */
  public Object getResponse(String newKey)
  {
    return this.response.get(newKey);
  }

  /** Retrieves a key from the response */
  public String getResponseString(String newKey)
  {
    return (String)this.response.get(newKey);
  }

  /** Retrieves a key from the response */
  public int getResponseInt(String newKey)
  {
    Integer i = (Integer) this.response.get(newKey);
    return i.intValue();
  }

  /** Retrieves a key from the response */
  public boolean getResponseBoolean(String newKey)
  {
    Boolean b = (Boolean) this.response.get(newKey);
    return b.booleanValue();
  }

  /** Retrieves a key from the response */
  public long getResponseLong(String newKey)
  {
    Long l = (Long)this.response.get(newKey);
    return l.longValue();
  }

  /** Removes a key from the response */
  public void removeResponse(String keyToRemove)
  {
    this.response.remove(keyToRemove);
  }

  /** Clears all the request and response keys in this message */
  public void clear()
  {
    request = new HashMap(defaultHashMapSize);
    response = new HashMap(defaultHashMapSize);
  }

  /** Clears a request key */
  public void clearRequest(String keyToClear)
  {
    this.request.remove(keyToClear);
  }

  /** Clears a response key */
  public void clearResponse(String keyToClear)
  {
    this.response.remove(keyToClear);
  }

}