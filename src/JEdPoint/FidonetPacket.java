package JEdPoint;

/**
 * Fidonet Packet Reader / Writer / Storage.
 * @author Edward Hevlund
 * Copyright 2001.
 * Released under the GNU General Public License.
*/

import java.io.*;
import java.util.*;
public class FidonetPacket
{
  public static final int Header_OriginNode       =  0;
  public static final int Header_DestinationNode  =  1;
  public static final int Header_Year             =  2;
  public static final int Header_Month            =  3;
  public static final int Header_Day              =  4;
  public static final int Header_Hour             =  5;
  public static final int Header_Minute           =  6;
  public static final int Header_Second           =  7;
  public static final int Header_Baud             =  8;
  public static final int Header_OriginNet        =  9;
  public static final int Header_DestinationNet   = 10;
  public static final int Header_ProductCodeHigh  = 11;
  public static final int Header_PVMajor          = 12;
  public static final int Header_Password         = 13;
  public static final int Header_QOriginZone      = 14;
  public static final int Header_QDestinationZone = 15;
  public static final int Header_Filler           = 16;
  public static final int Header_CapValid         = 17;
  public static final int Header_ProductCodeLow   = 18;
  public static final int Header_PVMinor          = 19;
  public static final int Header_CapWord          = 20;
  public static final int Header_OriginZone       = 21;
  public static final int Header_DestinationZone  = 22;
  public static final int Header_OriginPoint      = 23;
  public static final int Header_DestinationPoint = 24;
  public static final int Header_ProdData         = 25;
  private final int Header_Maximum         = 26;

  public static final int Message_OriginNode      =  0;
  public static final int Message_DestinationNode =  1;
  public static final int Message_OriginNet       =  2;
  public static final int Message_DestinationNet  =  3;
  public static final int Message_Attribute       =  4;
  public static final int Message_Cost            =  5;
  public static final int Message_DateTime        =  6;
  public static final int Message_ToUsername      =  7;
  public static final int Message_FromUsername    =  8;
  public static final int Message_Subject         =  9;
  public static final int Message_Text            = 10;
  private final int Message_Maximum         = 10;

  private java.util.Hashtable Header;

  private java.util.Hashtable Message;
  private java.util.Vector Messages;

  //----------------------------------------------------------------------------
  // FidonetPacketReader
  //----------------------------------------------------------------------------
  private class FidonetPacketReader
  {
    public FidonetPacket readPacket(String Filename) throws java.lang.Exception
    {
      FidonetPacket PacketRead;
      byte Buffer[];
      try
      {
        PacketRead = new FidonetPacket();
        BufferedInputStream PacketStream = new BufferedInputStream( new java.io.FileInputStream(Filename) );
        Buffer = new byte[ (int) new File(Filename).length() ];

        // Read the whole file into memory
        PacketStream.read(Buffer, 0, Buffer.length);
        PacketStream.close();

        // Now translate the information into a Packet
        PacketRead = ByteBufferToPacket(Buffer);
        // Help the garbage collector by nulling the buffer
        Buffer = null;
        System.gc();

        return PacketRead;
      }
      catch (Exception E)
      {
        throw E;
      }
    }

    private FidonetPacket ByteBufferToPacket(byte ByteBuffer[]) throws java.lang.Exception
    {
      FidonetPacket ReturnPacket = new FidonetPacket();

      // First check that this is a packet. Done quite primitively by checking that bytes 18 and 19 are 0x2 and 0x0
      if ( (ByteBuffer[18]!=0x2) && (ByteBuffer[19]!=0x0) ) throw new Exception("Not a v2.0 Packet file!");

      // Now read the header
      ReturnPacket = readHeader(ByteBuffer, ReturnPacket);

      // And finally read all the messages
      ReturnPacket = readMessages(ByteBuffer, ReturnPacket);

      return ReturnPacket;
    }

    // Convert two bytes to an int
    private int ConvertBytesToInt(byte BufferToUse[], int where)
    {
      int ReturnValue = 0;

      // Can you see the ugly signed/unsigned conversions?
      // Terrible, isn't it?
      int HighValue = BufferToUse[where+1];
      if (HighValue < 0) HighValue += 256;
      int LowValue =  BufferToUse[where];
      if (LowValue < 0) LowValue += 256;

      ReturnValue = (HighValue << 8) + LowValue;

      return ReturnValue;
    }

    // Convert four bytes to an int
    private int ConvertFourBytesToInt(byte BufferToUse[], int where)
    {
      int ReturnValue = 0;
      int Value3, Value2, Value1, Value0;

      // Can you see the ugly signed/unsigned conversions?
      // Terrible, isn't it?
      Value3 = BufferToUse[where+3];
      if (Value3 < 0) Value3 += 256;
      Value2 = BufferToUse[where+2];
      if (Value2 < 0) Value2 += 256;
      Value1 = BufferToUse[where+1];
      if (Value1 < 0) Value1 += 256;
      Value0 =  BufferToUse[where];
      if (Value0 < 0) Value0 += 256;

      ReturnValue = (Value3 << 24) + (Value2 << 16)+ (Value1 << 8) + Value0;

      return ReturnValue;
    }

    // Convert one byte to an int
    private int ConvertByteToInt(byte BufferToUse[], int where)
    {
      int ReturnValue = BufferToUse[where];
      if (ReturnValue < 0) ReturnValue += 256;
      return ReturnValue;
    }

    // Converts a 0x0 terminated string in a buffer to a ... string!
    private String ConvertBytesToZString(byte BufferToUse[], int where)
    {
      StringBuffer ReturnStringBuffer = new StringBuffer();
      int counter = where;
      while (BufferToUse[counter]!=0x0)
      {
        ReturnStringBuffer.append( (char)BufferToUse[counter] );
        counter++;
      }
      return ReturnStringBuffer.toString();
    }

    // Converts a string of specific length in a buffer to a ... string!
    private String ConvertBytesToString(byte BufferToUse[], int where, int length)
    {
      StringBuffer ReturnStringBuffer = new StringBuffer();
      int counter = where;
      while (counter != (where+length))
      {
        ReturnStringBuffer.append( (char)BufferToUse[counter] );
        counter++;
      }
      return ReturnStringBuffer.toString();
    }

    private FidonetPacket readHeader(byte BufferToRead[], FidonetPacket PacketObject)
    {
      PacketObject.setHeaderData( PacketObject.Header_OriginNode,      new Integer(ConvertBytesToInt(BufferToRead, 0)) );
      PacketObject.setHeaderData( PacketObject.Header_DestinationNode, new Integer(ConvertBytesToInt(BufferToRead, 2)) );
      PacketObject.setHeaderData( PacketObject.Header_Year,            new Integer(ConvertBytesToInt(BufferToRead, 4)) );
      PacketObject.setHeaderData( PacketObject.Header_Month,           new Integer(ConvertBytesToInt(BufferToRead, 6)) );
      PacketObject.setHeaderData( PacketObject.Header_Day,             new Integer(ConvertBytesToInt(BufferToRead, 8)) );
      PacketObject.setHeaderData( PacketObject.Header_Hour,            new Integer(ConvertBytesToInt(BufferToRead, 10)) );
      PacketObject.setHeaderData( PacketObject.Header_Minute,          new Integer(ConvertBytesToInt(BufferToRead, 12)) );
      PacketObject.setHeaderData( PacketObject.Header_Second,          new Integer(ConvertBytesToInt(BufferToRead, 14)) );
      PacketObject.setHeaderData( PacketObject.Header_Baud,            new Integer(ConvertBytesToInt(BufferToRead, 16)) );
      // PACKET VERSION = 18
      PacketObject.setHeaderData( PacketObject.Header_OriginNet,       new Integer(ConvertBytesToInt(BufferToRead, 20)) );
      PacketObject.setHeaderData( PacketObject.Header_DestinationNet,  new Integer(ConvertBytesToInt(BufferToRead, 22)) );
      PacketObject.setHeaderData( PacketObject.Header_ProductCodeLow,  new Integer(ConvertByteToInt(BufferToRead, 24)) );
      PacketObject.setHeaderData( PacketObject.Header_PVMajor,         new Integer(ConvertByteToInt(BufferToRead, 25)) );
      PacketObject.setHeaderData( PacketObject.Header_Password,        new String(ConvertBytesToZString(BufferToRead, 26)) );
      PacketObject.setHeaderData( PacketObject.Header_QOriginZone,     new Integer(ConvertBytesToInt(BufferToRead, 34)) );
      PacketObject.setHeaderData( PacketObject.Header_QDestinationZone,new Integer(ConvertBytesToInt(BufferToRead, 36)) );
      PacketObject.setHeaderData( PacketObject.Header_Filler,          new Integer(ConvertBytesToInt(BufferToRead, 38)) );
      PacketObject.setHeaderData( PacketObject.Header_CapValid,        new Integer(ConvertBytesToInt(BufferToRead, 40)) );
      PacketObject.setHeaderData( PacketObject.Header_ProductCodeHigh, new Integer(ConvertByteToInt(BufferToRead, 42)) );
      PacketObject.setHeaderData( PacketObject.Header_PVMinor,         new Integer(ConvertByteToInt(BufferToRead, 43)) );
      PacketObject.setHeaderData( PacketObject.Header_CapWord,         new Integer(ConvertBytesToInt(BufferToRead, 44)) );
      PacketObject.setHeaderData( PacketObject.Header_OriginZone,      new Integer(ConvertBytesToInt(BufferToRead, 46)) );
      PacketObject.setHeaderData( PacketObject.Header_DestinationZone, new Integer(ConvertBytesToInt(BufferToRead, 48)) );
      PacketObject.setHeaderData( PacketObject.Header_OriginPoint,     new Integer(ConvertBytesToInt(BufferToRead, 50)) );
      PacketObject.setHeaderData( PacketObject.Header_DestinationPoint,new Integer(ConvertBytesToInt(BufferToRead, 52)) );
      PacketObject.setHeaderData( PacketObject.Header_ProdData,        new Integer(ConvertFourBytesToInt(BufferToRead, 54)) );
      return PacketObject;
    }

    private FidonetPacket readMessages(byte BufferToRead[], FidonetPacket PacketObject)
    {
      int counter = 58;      // Counts the file position
                             // 58 is where the first position should be
      String tempstring;

      while (ConvertBytesToInt(BufferToRead, counter) == 2)
      {
        // The first two bytes weren't 0, which means we haven't reached EOF
        PacketObject.setMessageData( PacketObject.Message_OriginNode,      new Integer(ConvertBytesToInt(BufferToRead, counter + 2)) );
        PacketObject.setMessageData( PacketObject.Message_DestinationNode, new Integer(ConvertBytesToInt(BufferToRead, counter + 4)) );
        PacketObject.setMessageData( PacketObject.Message_OriginNet,       new Integer(ConvertBytesToInt(BufferToRead, counter + 6)) );
        PacketObject.setMessageData( PacketObject.Message_DestinationNet,  new Integer(ConvertBytesToInt(BufferToRead, counter + 8)) );
        PacketObject.setMessageData( PacketObject.Message_Attribute,       new Integer(ConvertBytesToInt(BufferToRead, counter +10)) );
        PacketObject.setMessageData( PacketObject.Message_Cost,            new Integer(ConvertBytesToInt(BufferToRead, counter +12)) );
        PacketObject.setMessageData( PacketObject.Message_DateTime,        new String(ConvertBytesToString(BufferToRead, counter +14, 20)) );

        // Position counter at the beginning of the zstrings
        counter += 34;

        // Read the ToUsername
        tempstring = ConvertBytesToZString(BufferToRead, counter);
        counter += tempstring.length() + 1;              // Here we try not to forget that the zstrings end with a 0, hence the +1 length
        PacketObject.setMessageData( PacketObject.Message_ToUsername, tempstring );

        // Read the FromUsername
        tempstring = ConvertBytesToZString(BufferToRead, counter);
        counter += tempstring.length() + 1;
        PacketObject.setMessageData( PacketObject.Message_FromUsername, tempstring );

        // Read the Subject
        tempstring = ConvertBytesToZString(BufferToRead, counter);
        counter += tempstring.length() + 1;
        PacketObject.setMessageData( PacketObject.Message_Subject, tempstring );

        // Read the Text
        tempstring = ConvertBytesToZString(BufferToRead, counter);
        counter += tempstring.length() + 1;
        PacketObject.setMessageData( PacketObject.Message_Text, tempstring );

        // And store the message
        PacketObject.storeMessage( PacketObject.getMessageCount() );
      }

      return PacketObject;
    }
  }

  //----------------------------------------------------------------------------
  // FidonetPacketWriter
  //----------------------------------------------------------------------------
  private class FidonetPacketWriter
  {
    // Very simple. Just give it a filename and a Packet and then wait for an exception.
    // If the file cannot be created, you'll know about it.
    public void writePacket(String Filename, FidonetPacket PacketToWrite) throws Exception
    {
      int counter;
      try
      {
        BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream(Filename) );

        bos.write( ReturnByteHeader(PacketToWrite) );

        for (counter=0; counter< PacketToWrite.getMessageCount(); counter++)
        {
          PacketToWrite.loadMessage(counter);
          bos.write( ReturnByteMessage(PacketToWrite) );
        }

        // Terminate the file by writing two nulls
        bos.write( 0x0 );
        bos.write( 0x0 );

        bos.close();
      }
      catch (Exception E)
      {
        throw E;
      }
    }

    // Returns a byte array which contains the header in byte array format
    // Ready to be written to a file.
    private byte[] ReturnByteHeader(FidonetPacket PacketToWrite)
    {
      byte Header[] = new byte[58];
      Integer tempinteger;
      int counter;
      String tempstring;

      WriteLittleEndianToBuffer( Header, 0, PacketToWrite.getHeaderDataInt(PacketToWrite.Header_OriginNode) );
      WriteLittleEndianToBuffer( Header, 2, PacketToWrite.getHeaderDataInt(PacketToWrite.Header_DestinationNode) );
      WriteLittleEndianToBuffer( Header, 4, PacketToWrite.getHeaderDataInt(PacketToWrite.Header_Year) );
      WriteLittleEndianToBuffer( Header, 6, PacketToWrite.getHeaderDataInt(PacketToWrite.Header_Month) );
      WriteLittleEndianToBuffer( Header, 8, PacketToWrite.getHeaderDataInt(PacketToWrite.Header_Day) );
      WriteLittleEndianToBuffer( Header, 10, PacketToWrite.getHeaderDataInt(PacketToWrite.Header_Hour) );
      WriteLittleEndianToBuffer( Header, 12, PacketToWrite.getHeaderDataInt(PacketToWrite.Header_Minute) );
      WriteLittleEndianToBuffer( Header, 14, PacketToWrite.getHeaderDataInt(PacketToWrite.Header_Second) );
      WriteLittleEndianToBuffer( Header, 16, PacketToWrite.getHeaderDataInt(PacketToWrite.Header_Baud) );

      // the packet version is 2.
      WriteLittleEndianToBuffer( Header, 18, 2 );

      WriteLittleEndianToBuffer( Header, 20, PacketToWrite.getHeaderDataInt(PacketToWrite.Header_OriginNet) );
      WriteLittleEndianToBuffer( Header, 22, PacketToWrite.getHeaderDataInt(PacketToWrite.Header_DestinationNet) );

      tempinteger  = (Integer)PacketToWrite.getHeaderData(PacketToWrite.Header_ProductCodeLow);
      if (tempinteger.intValue() > 127) Header[24] = (byte)(tempinteger.intValue()-256);
      else Header[24] = (byte)(tempinteger.intValue() & 0x0F);
      tempinteger  = (Integer)PacketToWrite.getHeaderData(PacketToWrite.Header_PVMajor);
      if (tempinteger.intValue() > 127) Header[25] = (byte)(tempinteger.intValue()-256);
      else Header[25] = (byte)(tempinteger.intValue() & 0x0F);

      WriteStringToBuffer( Header, 26, PacketToWrite.getHeaderDataString( PacketToWrite.Header_Password ), 8 );

      WriteLittleEndianToBuffer( Header, 34, PacketToWrite.getHeaderDataInt(PacketToWrite.Header_QOriginZone) );
      WriteLittleEndianToBuffer( Header, 36, PacketToWrite.getHeaderDataInt(PacketToWrite.Header_QDestinationZone) );

      WriteLittleEndianToBuffer( Header, 38, PacketToWrite.getHeaderDataInt(PacketToWrite.Header_Filler) );

//      WriteLittleEndianToBuffer( Header, 40, PacketToWrite.getHeaderDataInt(PacketToWrite.Header_CapValid) );
      WriteLittleEndianToBuffer( Header, 40, 256 );

      // The Product Code High and PV Minor
      tempinteger  = (Integer)PacketToWrite.getHeaderData(PacketToWrite.Header_ProductCodeHigh);
      if (tempinteger.intValue() > 127) Header[24] = (byte)(tempinteger.intValue()-256);
      else Header[42] = (byte)(tempinteger.intValue() & 0x0F);
      tempinteger  = (Integer)PacketToWrite.getHeaderData(PacketToWrite.Header_PVMinor);
      if (tempinteger.intValue() > 127) Header[25] = (byte)(tempinteger.intValue()-256);
      else Header[43] = (byte)(tempinteger.intValue() & 0x0F);

//      WriteLittleEndianToBuffer( Header, 44, PacketToWrite.getHeaderDataInt(PacketToWrite.Header_CapWord) );
      WriteLittleEndianToBuffer( Header, 44, 1 );

      WriteLittleEndianToBuffer( Header, 46, PacketToWrite.getHeaderDataInt(PacketToWrite.Header_OriginZone) );
      WriteLittleEndianToBuffer( Header, 48, PacketToWrite.getHeaderDataInt(PacketToWrite.Header_DestinationZone) );

      WriteLittleEndianToBuffer( Header, 50, PacketToWrite.getHeaderDataInt(PacketToWrite.Header_OriginPoint) );
      WriteLittleEndianToBuffer( Header, 52, PacketToWrite.getHeaderDataInt(PacketToWrite.Header_DestinationPoint) );

      // Was ist das? "ProdData" can be "Whatever"? Well, then that's what I'll make it.
      WriteLittleEndianToBuffer( Header, 54, 0 );
      WriteLittleEndianToBuffer( Header, 56, 0 );

      return Header;
    }

    // Returns a whole message as a byte array
    // The message to be written is selected by loading a message prior to calling this method
    private byte[] ReturnByteMessage( FidonetPacket PacketToWrite )
    {
      int counter = 34;
      byte Buffer[] = new byte[34];
      Integer tempinteger;
      String tempstring;

      Buffer[0] = 2;
      Buffer[1] = 0;

      tempinteger = (Integer)PacketToWrite.getMessageData(PacketToWrite.Message_OriginNode);
      WriteLittleEndianToBuffer( Buffer, 2, tempinteger.intValue() );
      tempinteger = (Integer)PacketToWrite.getMessageData(PacketToWrite.Message_DestinationNode);
      WriteLittleEndianToBuffer( Buffer, 4, tempinteger.intValue() );
      tempinteger = (Integer)PacketToWrite.getMessageData(PacketToWrite.Message_OriginNet);
      WriteLittleEndianToBuffer( Buffer, 6, tempinteger.intValue() );
      tempinteger = (Integer)PacketToWrite.getMessageData(PacketToWrite.Message_DestinationNet);
      WriteLittleEndianToBuffer( Buffer, 8, tempinteger.intValue() );
      tempinteger = (Integer)PacketToWrite.getMessageData(PacketToWrite.Message_Attribute);
      WriteLittleEndianToBuffer( Buffer, 10, tempinteger.intValue() );
      tempinteger = (Integer)PacketToWrite.getMessageData(PacketToWrite.Message_Cost);
      WriteLittleEndianToBuffer( Buffer, 12, tempinteger.intValue() );

      tempstring = (String)PacketToWrite.getMessageData( PacketToWrite.Message_DateTime );
      WriteStringToBuffer( Buffer, 14, tempstring, 20 );

      tempstring = (String)PacketToWrite.getMessageData( PacketToWrite.Message_ToUsername );
      Buffer = IncreaseArray( Buffer, tempstring.length() + 1); // +1 is the zero
      WriteZStringToBuffer( Buffer, counter , tempstring);
      counter += tempstring.length() + 1;

      tempstring = (String)PacketToWrite.getMessageData( PacketToWrite.Message_FromUsername );
      Buffer = IncreaseArray( Buffer, tempstring.length() + 1); // +1 is the zero
      WriteZStringToBuffer( Buffer, counter , tempstring);
      counter += tempstring.length() + 1;

      tempstring = (String)PacketToWrite.getMessageData( PacketToWrite.Message_Subject );
      Buffer = IncreaseArray( Buffer, tempstring.length() + 1); // +1 is the zero
      WriteZStringToBuffer( Buffer, counter , tempstring);
      counter += tempstring.length() + 1;

      tempstring = (String)PacketToWrite.getMessageData( PacketToWrite.Message_Text );
      Buffer = IncreaseArray( Buffer, tempstring.length() + 1); // +1 is the zero
      WriteZStringToBuffer( Buffer, counter , tempstring);

      return Buffer;
    }

    // Writes a string + terminating zero to an array
    // The array should have enough space for the new data
    private void WriteZStringToBuffer( byte BufferToWriteTo[], int Position, String StringToWrite)
    {
      int counter;
      for (counter=Position; counter<Position+StringToWrite.length(); counter++)
      {
        BufferToWriteTo[counter] = (byte)StringToWrite.charAt(counter - Position);
      }
      BufferToWriteTo[counter] = 0x0;  // It's zero terminated, right?
    }

    // Increases an array by SizeToIncrease bytes
    private byte[] IncreaseArray( byte ArrayToIncrease[], int SizeToIncrease)
    {
      int counter;
      byte ReturnByte[] = new byte[ ArrayToIncrease.length + SizeToIncrease ];
      for (counter=0; counter<ArrayToIncrease.length; counter++)
      {
        ReturnByte[counter] = ArrayToIncrease[counter];
      }
      return ReturnByte;
    }

    //   I.
    //  Hate.
    // Little.
    // Endian.
    private void WriteLittleEndianToBuffer( byte Buffer[], int where, int data)
    {
      byte LittleByte = (byte)(data % 256);
      byte BigByte = (byte)(data / 256);

      Buffer[where] = LittleByte;
      Buffer[where+1] = BigByte;
    }

    // Writes a string to the byte buffer and, if the string isn't long enough, will pad the
    // end of the buffer with zeros (length = pad)
    private void WriteStringToBuffer(  byte Buffer[], int where, String data, int pad)
    {
      int counter;

      for (counter=0; counter<data.length(); counter++)
      {
        Buffer[where+counter] = (byte)data.charAt(counter);
      }

      for (counter=data.length(); counter<pad; counter++)
      {
        Buffer[where+counter] = (byte)0x0;
      }
    }
  }

  //----------------------------------------------------------------------------
  // PACKET
  //----------------------------------------------------------------------------
  public FidonetPacket()
  {
    // 16 elements (0->15) and a load factor of 1 guarantees that it doesn't rehash.
    Header = new java.util.Hashtable(Header_Maximum, 1);
    Message = new java.util.Hashtable(Message_Maximum, 1);
    Messages = new java.util.Vector(0);
  }

  // Read a packet
  public FidonetPacket readPacket(String Filename) throws java.lang.Exception
  {
    FidonetPacketReader fpr = new FidonetPacketReader();
    return fpr.readPacket(Filename);
  }

  // Write a packet
  public void writePacket(String Filename, FidonetPacket PacketToWrite) throws Exception
  {
    FidonetPacketWriter fpw = new FidonetPacketWriter();
    fpw.writePacket(Filename, PacketToWrite);
  }

  // Packet header managing routines
  // -------------------------------

  // Sets the data for a certain header item
  // Throws an ArrayIndexOutOfBoundsException if the data isn't within limits
  public void setHeaderData(int index, Object newData)
  {
    if ( (index<0) || (index>Header_Maximum) )
       throw new java.lang.ArrayIndexOutOfBoundsException();
    Header.put(new Integer(index), newData);
  }

  public void setHeaderData(int index, int newInteger)
  {
    setHeaderData(index, new Integer(newInteger));
  }

  public void setHeaderData(int index, long newLong)
  {
    setHeaderData(index, new Long(newLong));
  }

  public void setHeaderData(int index, boolean newBoolean)
  {
    setHeaderData(index, new Boolean(newBoolean));
  }

  // Throws an ArrayIndexOutOfBoundsException if the data isn't within limits
  public Object getHeaderData(int index)
  {
    if ( (index>Header_Maximum) || (index<0) )
       throw new java.lang.ArrayIndexOutOfBoundsException();
    return Header.get(new Integer(index));
  }

  public int getHeaderDataInt(int index)
  {
    Integer tempInt = (Integer)getHeaderData(index);
    return tempInt.intValue();
  }

  public boolean getHeaderDataBoolean(int index)
  {
    Boolean tempBoolean = (Boolean)getHeaderData(index);
    return tempBoolean.booleanValue();
  }

  public long getHeaderDataLong(int index)
  {
    Long tempLong = (Long)getHeaderData(index);
    return tempLong.longValue();
  }

  public String getHeaderDataString(int index)
  {
    String tempString = (String)getHeaderData(index);
    return tempString;
  }


  // Messages vector managing routines
  // ---------------------------------
  // Stores(saves) a message
  public void storeMessage(int index)
  {
    try
    {
        if (index<Messages.size()) Messages.removeElementAt(index);
        Messages.insertElementAt((java.util.Hashtable)Message.clone(), index);
    }
    catch (java.lang.ArrayIndexOutOfBoundsException AIOOBE)
    {
      throw AIOOBE;
    }
  }

  // Loads a message
  public void loadMessage(int index)
  {
    try
    {
      Message = (java.util.Hashtable)Messages.elementAt(index);
    }
    catch (java.lang.ArrayIndexOutOfBoundsException AIOOBE)
    {
      throw AIOOBE;
    }
  }

  // Removes message at index, and shifts the rest of them -1 position
  public void removeMessage(int index)
  {
    try
    {
      Messages.removeElementAt(index);
    }
    catch (java.lang.ArrayIndexOutOfBoundsException AIOOBE)
    {
      throw AIOOBE;
    }
  }

  public int getMessageCount()
  {
    return Messages.size();
  }

  public void clearMessages()
  {
    Messages.clear();
  }


  // Message managing routines
  // -------------------------

  // Sets data for the loaded message
  // Throws an ArrayIndexOutOfBoundsException if the data isn't within limits
  public void setMessageData(int index, Object newData)
  {
    if ( (index<0) || (index>Message_Maximum) )
       throw new java.lang.ArrayIndexOutOfBoundsException();
    Message.put(new Integer(index), newData);
  }

  public void setMessageData(int index, int newObject)
  {
    setMessageData(index, new Integer(newObject));
  }

  public void setMessageData(int index, long newObject)
  {
    setMessageData(index, new Long(newObject));
  }

  public void setMessageData(int index, boolean newObject)
  {
    setMessageData(index, new Boolean(newObject));
  }

  // Gets data from the loaded message
  // Throws an ArrayIndexOutOfBoundsException if the data isn't within limits
  public Object getMessageData(int index)
  {
    if ( (index>Message_Maximum) || (index<0) )
       throw new java.lang.ArrayIndexOutOfBoundsException();
    return Message.get(new Integer(index));
  }

  public String getMessageDataString(int index)
  {
    String newObject = (String)getMessageData(index);
    return newObject;
  }

  public int getMessageDataInt(int index)
  {
    Integer newObject = (Integer)getMessageData(index);
    return newObject.intValue();
  }

  public long getMessageDataLong(int index)
  {
    Long newObject = (Long)getMessageData(index);
    return newObject.longValue();
  }

  public boolean getMessageDataBoolean(int index)
  {
    Boolean newObject = (Boolean)getMessageData(index);
    return newObject.booleanValue();
  }

}