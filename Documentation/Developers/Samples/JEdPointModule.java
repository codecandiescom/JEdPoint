package JEdPoint;

// Replace all XXX with proper information

/**
 * @author XXX
 * Copyright XXX.
 * Released under the GNU General Public License.
 */
public class XXX extends JEdPointModule;
{
  private static final Integer infoVersionHigh  = new Integer( 1 );
  private static final Integer infoVersionLow   = new Integer( 0 );
  private static final Integer infoApiVersion   = new Integer( 1 );
  private static final Integer infoType         = new Integer( JEdPointModule.XXX );
  private static final String infoAuthor            = "XXX";
  private static final String infoModuleName        = "XXX";
  private static final String infoModuleNameVersion = "XXX";
  private static final String infoShortDescription  = "XXX";
  private static final String infoLongDescription   = "XXX";

  private JEdPointMessage processMessage(int messageType, JEdPointMessage JPM) throws JEdPointException
  {
    switch (messageType)
    {
      case JEdPointMessage.moduleInit:
      break;
      case JEdPointMessage.moduleGetInformation:
        JPM.setResponse("author", this.infoAuthor);
        JPM.setResponse("modulename", this.infoModuleName);
        JPM.setResponse("modulenameversion", this.infoModuleNameVersion);
        JPM.setResponse("shortdescription", this.infoShortDescription);
        JPM.setResponse("longdescription", this.infoLongDescription);
        JPM.setResponse("type", this.infoType);
        JPM.setResponse("versionhigh", this.infoVersionHigh);
        JPM.setResponse("versionlow", this.infoVersionLow);
        JPM.setResponse("apiversion", this.infoApiVersion);
      break;
    }
    return JPM;
  }

}