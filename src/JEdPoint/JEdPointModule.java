package JEdPoint;

/**
 * Base class which all modules extend.
 * <br><br>
 * Handles message sending and processing, the latter of which is abstract and needs to be written by the
 * developer.
 * <br><br>
 * The two important parts of this module class is <b>message sending</b> and <b>message processing.</b>
 * <br><br>
 * <b>Message Sending</b>
 * <br>
 * There are various ways of sending messages, depending on what one's needs are.
 * <br>
 * First, there's the <b>Send now - Receive now</b> method, which is what sendMessage() does.
 * <br>
 * Second, there's the <b>Send now - Receive later</b> method, which is what queueMessage() does.
 * <br><br>
 * <b>Message Processing</b>
 * When a module receives a message, it should be processed. Processing is done by:
 * <br>
 * <ul>
 *  <li>Extracting the message type from the message object.</li>
 *  <li>Handling the message</li>
 *  <li>Returning the message</li>
 * </ul>
 * Preferrably, the following piece of example code illustrated how processMessage should be implemented...
 * <br>
 * <pre>
 *   private JEdPointMessage processMessage(int messageType, JEdPointMessage JPM) throws JEdPointException
 *   {
 *     switch (messageType)
 *     {
 *        case JEdPointMessage.moduleInit:
 *        break;
 *        case JEdPointMessage.moduleGetInformation:
 *          JPM.setResponse("author", this.infoAuthor);
 *          JPM.setResponse("modulename", this.infoModuleName);
 *          JPM.setResponse("modulenameversion", this.infoModuleNameVersion);
 *          JPM.setResponse("shortdescription", this.infoShortDescription);
 *          JPM.setResponse("longdescription", this.infoLongDescription);
 *          JPM.setResponse("type", this.infoType);
 *          JPM.setResponse("versionhigh", this.infoVersionHigh);
 *          JPM.setResponse("versionlow", this.infoVersionLow);
 *          JPM.setResponse("apiversion", this.infoApiVersion);
 *        break;
 *     }
 *     return JPM;
 *   }
 * </pre>
 * <br><br>
 * Note: Queue methods are untested.
 *
 * @author Edward Hevlund
 * Copyright 2001.
 * Released under the GNU General Public License.
*/
public abstract class JEdPointModule implements Runnable
{
  // ---------------------------------------------------------------------------
  // MODULE TYPES
  // ---------------------------------------------------------------------------
  /** Number of module types that exist */
  public static final int moduleTypes       =  10;
  /** JEdPoint Microkernel */
  public static final int moduleMicrokernel =  0;
  /** Log */
  public static final int moduleLog         =  1;
  /** User Interface */
  public static final int moduleUI          =  2;
  /** MessageBase */
  public static final int moduleMessageBase =  3;
  /** Importer */
  public static final int moduleImport      =  4;
  /** Exporter */
  public static final int moduleExport      =  5;
  /** Poller */
  public static final int modulePoll        =  6;
  /** Tearline */
  public static final int moduleTearline    =  7;
  /** Origin */
  public static final int moduleOrigin      =  8;
  /** Message Editor */
  public static final int moduleMessageEditor =  9;

  // Variables
  private JEdPointMicrokernel jp;             // The main jedpoint class
  private JEdPointMessage messageQueue[];     // The message queue

  /** Simple constructor that does nothing important */
  public JEdPointModule()
  {
    // initialize the message queue
    messageQueue = new JEdPointMessage[0];
  }


  //----------------------------------------------------------------------------
  // MESSAGE PROCESSING
  //----------------------------------------------------------------------------
  /** Called by the MK when the module receives something to do */
  public abstract JEdPointMessage processMessage(int messageType, JEdPointMessage JPM) throws JEdPointException;


  //----------------------------------------------------------------------------
  // MESSAGE SENDING AND RECEIVING
  //----------------------------------------------------------------------------

  /**
   * Sends the message to the MK.
   * Control will not be returned until the message itself is returned.
   * The answer message will be the value returned by this method.
  */
  public final JEdPointMessage sendMessage(int moduleToSendTo, int messageType, JEdPointMessage JPM)
  {
    return jp.routeMessage(moduleToSendTo, messageType, JPM);
  }

  /**
   * Sends the message to the MK.
   * Control will not be returned until the message itself is returned.
   * The answer message will be the value returned by this method.
   * This method prepares a message with a key+value pair in the request before sending it away
  */
  public final JEdPointMessage sendMessage(int moduleToSendTo, int messageType, String newKey, Object newValue)
  {
    return this.sendMessage(moduleToSendTo, messageType, new JEdPointMessage(newKey, newValue));
  }

  /**
   * Sends the message to the MK.
   * Control will not be returned until the message itself is returned.
   * The answer message will be the value returned by this method.
   * This method prepares a message with two key+value pairs in the request before sending it away
  */
  public final JEdPointMessage sendMessage(int moduleToSendTo, int messageType, String newKey1, Object newValue1, String newKey2, Object newValue2)
  {
    return this.sendMessage( moduleToSendTo, messageType, new JEdPointMessage(newKey1, newValue1, newKey2, newValue2) );
  }

  /**
   * Queues the message for the MK.
   * Control will be returned as soon as the message is queued.
   * The response to this message (if there is one) will be received and processed (processMessage) by this module.
   *
   * Adds two keys to the message, "moduleToSendTo" and "messageType", but these are removed by JEdPoint before being
   * routed to the destination module.
  */
  public final void queueMessage(int moduleToSendTo, int messageType, JEdPointMessage JPM)
  {
    JPM.setRequest("moduleToSendTo", new Integer(moduleToSendTo));
    JPM.setRequest("messageType", new Integer(messageType));
    JPM.setRequest("moduleSentFrom", this);
    this.insertAtEndOfQueue(JPM);
    return;
  }

  //----------------------------------------------------------------------------
  // INTERNAL METHODS
  //----------------------------------------------------------------------------
  // From here on there are just a bunch of methods for
  //
  // - Message Queue handling (resizing, emptying, etc)
  // - Interaction with JEdPoint

  /** Internal method to check for available messages */
  public final int messagesAvailable()
  {
    return this.messageQueue.length;
  }

  /** Internal method to fetch and remove the first message from the queue */
  public final JEdPointMessage fetchMessage()
  {
    JEdPointMessage tempMessage;

    if (this.messageQueue.length>0)           // error checking!
    {
      tempMessage = this.messageQueue[0];
      this.removeMessage(0);                  // moves all the messages one place down
      return tempMessage;
    }
    else
    {
      return null;
    }
  }

  /**
   * Internal method setting the jp variable to newjp.
   * This is called by Jedpoint so that the module can call the main JEdPoint class when necessary
  */
  public final void setJEdPointClass(JEdPointMicrokernel newjp)
  {
    this.jp = newjp;
  }

  /** Puts another message in the message queue */
  private final void insertAtEndOfQueue(JEdPointMessage JPM)
  {
    int tempint = this.messageQueue.length;
    this.resizeQueue(tempint+1);

    messageQueue[tempint] = JPM;

    // If the message queue _was_ empty, inform JEdPoint that is has some messages to pick up
    jp.messageReadyForPickup(this);
  }

  /** Removes a message from the message queue */
  private final void removeMessage(int messageNumber)
  {
    int counter;

    for (counter=0; counter<messageNumber; counter++)
    {
      this.messageQueue[counter] = this.messageQueue[counter+1];
    }
    for (counter=messageNumber+1; counter<this.messageQueue.length; counter++)
    {
      this.messageQueue[counter] = this.messageQueue[counter+1];
    }
    this.resizeQueue( this.messageQueue.length-1 );
  }

  /** Resizes the message queue, preserving the messages */
  private final void resizeQueue(int newSize)
  {
    JEdPointMessage tempQueue[] = new JEdPointMessage[newSize];
    int counter;
    int maximum = Math.min(newSize, this.messageQueue.length);

    for (counter=0; counter<maximum; counter++)
    {
      tempQueue[counter] = this.messageQueue[counter];
    }

    this.messageQueue = tempQueue;
  }

  public final void run()
  {
  }
}