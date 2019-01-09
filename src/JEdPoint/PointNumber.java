package JEdPoint;

import java.util.StringTokenizer;

/**
 * A point number class used to store / handle point numbers.
 * <br><br>
 * Used mainly to store all four point number ints in one place. Can also make a PointNumber out of a string and a PointNumber into a string.
 *
 * @author Edward Hevlund
 * Copyright 2001.
 * Released under the GNU General Public License.
 */

public class PointNumber implements java.io.Serializable
{
  private int zone;
  private int net;
  private int node;
  private int point;

  public PointNumber()
  {
    this.zone = 0;
    this.net = 0;
    this.node = 0;
    this.point = 0;
  }

  /**
   * Creates a PointNumber with the specified zone, net, node and point numbers.
   */
  public PointNumber(int newZone, int newNet, int newNode, int newPoint)
  {
    this.zone = newZone;
    this.net = newNet;
    this.node = newNode;
    this.point = newPoint;
  }

  public int getZone()
  {
    return this.zone;
  }

  public int getNet()
  {
    return this.net;
  }

  public int getNode()
  {
    return this.node;
  }

  public int getPoint()
  {
    return this.point;
  }

  public void setZone(int newZone)
  {
    this.zone = newZone;
  }

  public void setNet(int newNet)
  {
    this.net = newNet;
  }

  public void setNode(int newNode)
  {
    this.node = newNode;
  }

  public void setPoint(int newPoint)
  {
    this.point = newPoint;
  }

  /**
   * Converts a String to a PointNumber.
   * <br><br>
   * The String should be in either "zone:net/node" or "zone:net/node.point" format.
   * Not including the point number will make the point 0.
   */
  public static PointNumber parsePointNumber(String stringToParse) throws NumberFormatException
  {
    StringTokenizer st = new StringTokenizer(stringToParse);

    if (stringToParse.indexOf(".") != -1)
      return new PointNumber(
        new Integer( st.nextToken(":") ).intValue(),
        new Integer( st.nextToken("/").substring(1) ).intValue(),
        new Integer( st.nextToken(".").substring(1) ).intValue(),
        new Integer( st.nextToken() ).intValue() );
    else
      return new PointNumber(
        new Integer( st.nextToken(":") ).intValue(),
        new Integer( st.nextToken("/").substring(1) ).intValue(),
        new Integer( st.nextToken(".").substring(1) ).intValue(),
        0 );
  }

  /**
   * Converts the point number to a String.
   */
  public String toString()
  {
    if (this.point != 0)
      return this.zone + ":" + this.net + "/" + this.node + "." + this.point;
    else
      return this.zone + ":" + this.net + "/" + this.node;
  }
}