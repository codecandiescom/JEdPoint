package JEdPoint.modules.module_ui_Default;

import java.util.Vector;
import JEdPoint.*;

/**
 * Maintains a user-sorted list of areas.
 */
public class Areas extends Vector
{
  public Areas()
  {
    super();
  }

  public void setVector( Vector areas )
  {
    MessageAreaData currentMAD, newMAD;
    int counter;

    // Are the new areas different in any way from the areas we have?
    if ( areas.hashCode() != super.hashCode() )
    {
      // Remove any areas from our vector that don't exist in the new one
      counter = 0;
      while (counter<super.size())
      {
        if (areas.contains( super.elementAt(counter) ))
          counter++;
        else
          super.removeElementAt(counter);
      }

      // Add any new areas to our vector
      for (counter=0; counter<areas.size(); counter++)
      {
        if (!super.contains( areas.elementAt(counter) ))
          sorted_add(areas.elementAt(counter));
      }
    }
  }

  /**
   * Adds the newElement sorted roundabout where it should be.
   */
  private void sorted_add( Object newElement )
  {
    int counter;

    // Try to find a place in the already existing vector for the new element.
    for (counter=0; counter<super.size()-1; counter++)
    {
      if ( (newElement.toString().compareTo(super.elementAt(counter)) > 0)
        && (newElement.toString().compareTo(super.elementAt(counter+1)) < 0) )
        {
          super.insertElementAt(newElement, counter+1);
          break;
        }
    }

    // Check to see whether we managed to find a place for the new element.
    // If we didn't, add it to the end
    if (!super.contains(newElement))
      super.add(newElement);
  }

  /**
   * Moves an area up (-1) one place in the vector.
   */
  public void moveUp( int indexToMoveUp )
  {
    swap(indexToMoveUp-1, indexToMoveUp);
  }

  /**
   * Moves an area down (+1) one place in the vector.
   */
  public void moveDown( int indexToMoveUp )
  {
    swap(indexToMoveUp, indexToMoveUp+1);
  }

  private void swap( int first, int second )
  {
    try
    {
      Object tempObject;
      tempObject = super.elementAt(first);
      super.setElementAt(super.elementAt(second), first);
      super.setElementAt(tempObject, second);
    }
    catch (Exception e)
    {
    }
  }

  public void sort()
  {
    int counter;
    int bigcounter;

    for (bigcounter=0; bigcounter<super.size()-1; bigcounter++)
      for (counter=0; counter<super.size()-1; counter++)
        if ( super.elementAt(counter).toString().compareTo( super.elementAt(counter+1)) > 0)
          swap(counter, counter+1);
  }

}