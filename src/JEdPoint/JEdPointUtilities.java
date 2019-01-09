package JEdPoint;

import java.util.*;
import java.io.*;
import java.util.zip.*;
/**
 * <p>Title: JEdPoint</p>
 * <p>Description: Java Fidonet Point Software</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: None</p>
 * @author Edward
 * @version 1.0
 */

public class JEdPointUtilities
{
  /**
   * Replaces a key in a java.util.Properties file with a new key.
   *
   * It keeps everything else in the file exactly the same, only the key is replaced.
   * If the key does not exist and create == true, the key is added to the bottom of the file,
   * else nothing.
   */
  public static void keyReplace( String filename, String key, String newValue ) throws JEdPointException
  {
    boolean found = false;
    FileReader fr = null;
    FileWriter fw = null;
    BufferedReader br;
    BufferedWriter bw;
    File tempFile;
    String line;
    String tempString;
    int counter;

    // Open the input file
    try
    {
      fr = new FileReader( filename );
    }
    catch (FileNotFoundException fnfe)
    {
      // The file could not be found, even though we created it just a few milliseconds ago
      throw new JEdPointException( fnfe, JEdPointException.severityError,
        "Could not find the file that we should have create a few seconds ago, namely: " + filename,
        "Write error.",
        "Check that the file can be created at all." );
    }

    // Open the output file
    try
    {
      fw = new FileWriter( filename + ".tmp" );
    }
    catch (IOException ioeFW)
    {
      // The file could not be found, even though we created it just a few milliseconds ago
      throw new JEdPointException( ioeFW, JEdPointException.severityError,
        "replaceKey: Could not create a temp file!",
        "Write error.",
        "Check that the file can be created at all." );
    }

    br = new BufferedReader(fr);
    bw = new BufferedWriter(fw);

    try
    {
      line = br.readLine();
      while (line != null)
      {
        if (line.trim().startsWith(key))
        {
          found = true;
          for (counter=line.indexOf(key)+key.length(); counter<line.length(); counter++)
          {
            if ( Character.isLetterOrDigit( line.charAt(counter) ) )
              break;
          }
          tempString = line.substring(0, counter) + newValue;
          line = tempString;
        }
        bw.write(line + "\n");
        line = br.readLine();
      }

      bw.close();
      br.close();
      fw.close();
      fr.close();
    }
    catch (Exception e)
    {
      throw new JEdPointException( e, JEdPointException.severityError,
        "Error trying to change the key \"" + key + "\" to the value \"" + newValue + "\" in the file " + filename,
        "See stack trace.",
        "See stack trace." );
    }
  }

  /**
   * Checks to see if a directory exists.
   * <br><br>
   * If the directory doesn't exist it will try to create one.
   * If a file exists that has the directory name, it will throw an exception (and terminate).
   */
  public static void checkDirectory(String dirToCheck) throws JEdPointException
  {
    File newDir = new File(dirToCheck);

    if (newDir.exists() == false)
    {
      // Try to create the directory
      if (!newDir.mkdirs())
        throw new JEdPointException(new Exception("Could not create the directory " + dirToCheck),
          JEdPointException.severityFatal,
          "Unable to create the directory: " + dirToCheck,
          "No permission?",
          "Try and create the directory yourself.");
    }
    if (!newDir.isDirectory())
    {
      throw new JEdPointException(new Exception("Directory " + dirToCheck + " is a file."),
          JEdPointException.severityFatal,
          "Unable to create the directory: " + dirToCheck,
          "The directory is a file.",
          "Delete the file.");
    }
  }

  /**
   * Replaces a string in a string with another string.
   * <br><br>
   * Just a quick string replacement method so that you don't have to sit and roll your own.
   * <br>
   * Replaces all occurrences of stringToFind with stringReplaceWith.
   */
  public static String replaceString(String stringToUse, String stringToFind, String stringReplaceWith)
  {
    while (stringToUse.indexOf(stringToFind)!=-1)
    {
      stringToUse = stringToUse.substring(0,stringToUse.indexOf(stringToFind)) + stringReplaceWith + stringToUse.substring(stringToUse.indexOf(stringToFind)+stringToFind.length(), stringToUse.length() );
    }
    return stringToUse;
  }

  /**
   * Removes a whole line from a text.
   *
   * @param complete The Whole Text.
   * @param lineToRemove The line to be removed has this keyword.
   * @return complete without the line if lineToRemove was found, else complete.
   */
  public static String removeLine(String complete, String lineToRemove)
  {
    String returnString = new String(complete);
    if ( returnString.lastIndexOf(lineToRemove) != -1 )
    {
      // Because lineToRemove could be the first line, add a \n at the beginning
      returnString = "\n" + returnString;

      String beforeLine = returnString.substring( 0, returnString.indexOf(lineToRemove) );
      beforeLine = beforeLine.substring( 0, beforeLine.lastIndexOf("\n") );
      String afterLine = returnString.substring( returnString.indexOf("\n", returnString.indexOf(lineToRemove)) );
      returnString = beforeLine + afterLine;

      // Remove the \n we added, if it's there
      if (returnString.charAt(0) == '\n')
        returnString = returnString.substring(1);
    }

    return returnString;
  }

  /**
   * Removes the last ocurrence of a whole line from a text.
   *
   * @param complete The Whole Text.
   * @param lineToRemove The line to be removed has this keyword.
   * @return complete without the line if lineToRemove was found, else complete.
   */
  public static String removeLastLine(String complete, String lineToRemove)
  {
    int index;
    String returnString = new String(complete);
    if ( returnString.lastIndexOf(lineToRemove) != -1 )
    {
      String beforeLine = returnString.substring( 0, returnString.lastIndexOf(lineToRemove) );
      beforeLine = beforeLine.substring( 0, beforeLine.lastIndexOf("\n") );

      // This might be the end of the string. Be careful, because in that case we
      // won't be seeing any fancy schmancy \n's
      index = returnString.indexOf("\n", returnString.lastIndexOf(lineToRemove));
      if (index == -1)
        index = returnString.length();

      String afterLine = returnString.substring( index );
      returnString = beforeLine + afterLine;
    }
    return returnString;
  }

  /**
   * Extracts a whole line from a text.
   *
   * @param complete The Whole Text.
   * @param lineToFind  The line to be extracted has this keyword.
   * @return The line found or "" if lineToFind wasn't found.
   */
  public static String getLine(String complete, String lineToFind)
  {
    int startIndex, stopIndex;
    String returnString = "";

    if ( complete.indexOf(lineToFind) != -1 )
    {
      startIndex = complete.substring(0, complete.indexOf(lineToFind)).lastIndexOf("\n") + 1;
      stopIndex = complete.substring( complete.indexOf(lineToFind), complete.length()).indexOf("\n") + complete.indexOf(lineToFind);
      returnString = complete.substring( startIndex, stopIndex );
    }

    return returnString;
  }

  /**
   * Extracts the last occurrence of a whole line from a text.
   *
   * @param complete The Whole Text.
   * @param lineToFind  The line to be extracted has this keyword.
   * @return The line found or "" if lineToFind wasn't found.
   */
  public static String getLastLine(String complete, String lineToFind)
  {
    int startIndex, stopIndex;
    String returnString = "";

    if ( complete.indexOf(lineToFind) != -1 )
    {
      startIndex = complete.substring(0, complete.lastIndexOf(lineToFind)).lastIndexOf("\n") + 1;
      if (complete.substring( complete.lastIndexOf(lineToFind), complete.length() ).indexOf("\n") != -1)
        stopIndex = complete.substring( complete.lastIndexOf(lineToFind), complete.length() ).indexOf("\n") + complete.lastIndexOf(lineToFind);
      else
        stopIndex = complete.length();
      returnString = complete.substring( startIndex, stopIndex );
    }

    return returnString;
  }

  /**
   * Parses a string containing three RGB values into a color.
   * @return A Color if successful, else null.
   */
  public static java.awt.Color parseColor( String RGBString )
  {
    java.awt.Color returnColor;
    StringTokenizer st = new StringTokenizer( RGBString );
    int red, green, blue;

    try
    {
      if (st.countTokens() != 3)
        return null;

      red = Integer.parseInt( st.nextToken() );
      green = Integer.parseInt( st.nextToken() );
      blue = Integer.parseInt( st.nextToken() );

      returnColor = new java.awt.Color( red, green, blue );
    }
    catch (Exception e)
    {
      return null;
    }
    return returnColor;
  }

  /**
   * Converts a string from the specified codepage to / from unicode.
   * <br><br>
   * Currently, the following codepages are accepted:
   *  CP***
   *  LATIN-1
   *  IBMPC
   *  PC-8
   * <br>
   * @param codepage The codepage the string is encoded in
   * @param stringToConvert String to be converted
   * @param toUnicode Convert TO unicode? (false = FROM)
   * @return A Unicode encoded string
   */
  public static String unicodeConvert( String codepage, String stringToConvert, boolean toUnicode )
  {
    String returnValue = new String( stringToConvert );
    byte buffer[];
    int counter;

    // A common table, which all texts are put through anyways and
    String tablePrimaryConversion[] = { "\r", "\n" };

    // This if to convert all the smiley faces to unicode smiley faces (which aren't 0001)
    // We can't convert them at the same time as tablePrimaryConversion beacuse the unicode converter
    // doesn't know what a smiley face is.
    String tableFinalConversion[] = { "\u0001", "\u263A" };

    // Convert the codepage names from Fidonet standard to WORLD standard.
    // Yes, Fidonet even has its own terms for set standard.
    if ( codepage.compareToIgnoreCase("IBMPC8") ==0)
      codepage = "CP437";
    if ( codepage.compareToIgnoreCase("PC-8") ==0)
      codepage = "ISO8859_1";
    if ( codepage.compareToIgnoreCase("IBMPC") ==0)
      codepage = "CP437";
    if ( codepage.compareToIgnoreCase("LATIN-1") ==0)
      codepage = "ISO8859_1";

    if (toUnicode)
    {
      // Convert it TO unicode

      // Change \r to \n
      for (counter=0; counter<tablePrimaryConversion.length/2; counter++)
        returnValue = JEdPointUtilities.replaceString(returnValue, tablePrimaryConversion[(counter*2)], tablePrimaryConversion[(counter*2)+1] );

      // After having converted the stringToUse's \r to \n, we can now put it in the buffer
      buffer = returnValue.getBytes();

      // And now do some lovely high-ansi conversions
      // If the original byte is > 127 ansi (meaning >0xFF00 unicode)
      // Fix the buffer byte by assigning it charAt() - 0xFF00
      for (counter=0; counter<returnValue.length(); counter++)
        if (returnValue.charAt(counter) >= 0xFF00)
          buffer[counter] = (byte)( returnValue.charAt(counter)-0xFF00 ) ;

      try
      {
        returnValue = new String( buffer, codepage );
      }
      catch (UnsupportedEncodingException uee)
      {
      }

      // Do final conversion (smileys)
      for (counter=0; counter<tableFinalConversion.length/2; counter++)
        returnValue = JEdPointUtilities.replaceString(returnValue, tableFinalConversion[(counter*2)], tableFinalConversion[(counter*2)+1] );

    } // if toUnicode
    else
    {
      // Replace the \n with \r
      for (counter=0; counter<tablePrimaryConversion.length/2; counter++)
        returnValue = JEdPointUtilities.replaceString(returnValue, tablePrimaryConversion[(counter*2)+1], tablePrimaryConversion[(counter*2)] );

      // Replace the unicode smiley faces with 0x01 smiley faces.
      for (counter=0; counter<tableFinalConversion.length/2; counter++)
        // Replace all occurrences of the string with another...
        returnValue = JEdPointUtilities.replaceString(returnValue, tableFinalConversion[(counter*2)+1], tableFinalConversion[(counter*2)] );

      // And now convert it from unicode.
      try
      {
        buffer = returnValue.getBytes(codepage);
        returnValue = "";
        for (counter=0; counter<buffer.length; counter++)
          returnValue += (char)buffer[counter];
      }
      catch (Exception e)
      {
      }
    }
    return returnValue;
  }

  /**
   * Converts a GregorianCalendar to the format "YYYY-MM-DD  HH:MM:SS".
   *
   * @param gc A gregorian calendar.
   */
  public static String GregorianCalendarToString( GregorianCalendar gc )
  {
    GregorianCalendar tempgc = new GregorianCalendar();
    int valuesInCalendar[] =  { Calendar.YEAR,  Calendar.MONTH, Calendar.DAY_OF_MONTH,  Calendar.HOUR_OF_DAY, Calendar.MINUTE,  Calendar.SECOND };
    int preferredLengths[] =  { 4,              2,              2,                      2,                    2,                2 };
    int counter;
    String values[] = new String[ valuesInCalendar.length ];

    // We have to copy gc to another GregorianCalendar variable because otherwise things can turn ugly
    // Java can't decide whether it's passing variables by reference or by value.
    for (counter=0; counter<tempgc.FIELD_COUNT; counter++)
      tempgc.set( counter, gc.get(counter) );

    for (counter=0; counter<valuesInCalendar.length; counter++)
    {
      values[counter] = String.valueOf(  tempgc.get(valuesInCalendar[counter]) );
      if ( valuesInCalendar[counter] == Calendar.MONTH )
        values[counter] = String.valueOf(  Integer.parseInt(values[counter]) +1 );
      while (values[counter].length() < preferredLengths[counter] )
        values[counter] = "0" + values[counter];
    }

    return values[0] + "-" + values[1] + "-" + values[2] + "  " + values[3] + ":" + values[4] + ":" + values[5];
  }

  /**
   * Compresses/Decompresses an array of bytes.
   * <br><br>
   * There are several compression levels.
   * They vary from 9 = max compression to 0 = no compression.
   * <br>
   * If compress == false (set to uncompress) then compressionLevel can be set to anything.
   */
  public static byte[] compressByteArray(byte ArrayToUse[], boolean compress, int compressionLevel)
  {
    if (compress)
    {
      // COMPRESS
      Deflater deflater = new Deflater(compressionLevel);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DeflaterOutputStream dos = new DeflaterOutputStream( baos, deflater );

      try
      {
        dos.write(ArrayToUse);
        dos.close();
        baos.close();
      }
      catch (IOException ioe)
      {
      }

      return baos.toByteArray();
    }
    else
    {
      // DECOMPRESS
      ByteArrayInputStream bais = new ByteArrayInputStream( ArrayToUse, 0, ArrayToUse.length );
      InflaterInputStream iis = new InflaterInputStream( bais );
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      try
      {
        while (iis.available()==1)
          baos.write(iis.read());
        iis.close();
        bais.close();
      }
      catch (ZipException ze)
      {
        // This wasn't compressed
        // Return the original value
        return ArrayToUse;
      }
      catch (IOException ioe)
      {
      }
      return baos.toByteArray();
    }
  }

  /**
   * Converts an object to an array of bytes;
   */
  public static byte[] objectToByteArray(Object ObjectToSave)
  {
    byte returnValue[];
    ObjectOutputStream oos;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try
    {
      oos = new ObjectOutputStream( baos );
      oos.writeObject(ObjectToSave);
      baos.close();
      oos.flush();
      oos.close();
    }
    catch (Exception e)
    {
      return null;
    }
    return baos.toByteArray();
  }

  /**
   * Converts an array of bytes to an object.
   */
  public static Object byteArrayToObject(byte array[])
  {
    Object ReturnObject = null;
    ByteArrayInputStream bais;
    ObjectInputStream ois;
    try
    {
      bais = new ByteArrayInputStream(array);
      ois = new ObjectInputStream( bais );
      ReturnObject = ois.readObject();
      ois.close();
      bais.close();
    }
    catch (Exception e)
    {
      return null;
    }
    return ReturnObject;
  }

  /**
   * Saves a whole object to disk.
   * <br><br>
   * Use this method to save a complete object (class, String, Vector, whatever) to a specified location on disk.
   * You can then use loadObject to load it.
   */
  public static boolean saveObject(String Filename, Object ObjectToSave)
  {
    ObjectOutputStream oos;
    FileOutputStream fos;
    try
    {
      fos = new FileOutputStream(Filename);
      oos = new ObjectOutputStream( fos );
      oos.writeObject(ObjectToSave);
      fos.close();
      oos.flush();
      oos.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * Loads a specific object from disk.
   * <br><br>
   * @return null if it fails
   * @return Object OK
   */
  public static Object loadObject(String Filename)
  {
    Object ReturnObject = null;
    FileInputStream fis;
    ObjectInputStream ois;
    try
    {
      fis = new FileInputStream(Filename);
      ois = new ObjectInputStream( fis );
      ReturnObject = ois.readObject();
      ois.close();
      fis.close();
    }
    catch (Exception e)
    {
      return null;
    }
    return ReturnObject;
  }

}