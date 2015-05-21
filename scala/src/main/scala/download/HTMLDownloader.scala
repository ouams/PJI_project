
import sys.process._
import java.net.URL
import java.io.File
import java.io.OutputStreamWriter
import java.io.FileOutputStream
import java.io.BufferedWriter
import java.io.InputStreamReader
import org.mozilla.universalchardet.UniversalDetector;
import java.util.Scanner


/**
 * Object to download a Html from the french National Assembly website :
 * http://archives.assemblee-nationale.fr/X/cri
 * (with X the legislature number between 1 and 11)
 *
 * @author Ouamar SAis
 */
 object HTMLDownloader {

  // List of the URLs sorted and grouped by 100
  private val groupedURLs =
  ((URLManager.htmlURLs sortWith (_ < _)) grouped 100).toList

  // Gives the local path of the Html from its URL
  private def htmlFilePath(url: String): String = {
  	val parts: List[String] = (url split '/').toList
  	val legislature = parts(3)
  	val part : List[String] =  (parts(5) split '-').toList
  	var kind = "ordinaire"
  	
  	if(part.length >2){
  		kind = part(2)
  	}
  	
  	val years = part(0) + "-" + part(1);
  	
  	"./cri/" + legislature + "/" + years + "/" + kind + "/fichiers/"
  }


/**
   * Downloads a Html from the given URL
   *
   * @param url
   *          URL of the Html
   */
   def downloadHTML(url: String): Unit = {
    val htmlURL = new URL(url)
    var encoding = getEncoding(htmlURL)
    //Verification de l'encoding
   	val name = url.substring((url lastIndexOf '/') + 1, (url indexOf ".asp")) +"_"+encoding+ ".html"  
   	val path = this.htmlFilePath(url)
   	val pathDirs = new File(path)
   	val check = new File(path + name)
    val inStream =  htmlURL.openStream()
    
    

    if(htmlURL.openConnection.getContentLength != -1){
      if(!check.exists){
        if (!pathDirs.exists){
          pathDirs.mkdirs
        }

        val sc = new Scanner(inStream, encoding)
        val out =  new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + name), encoding))

        while(sc.hasNextLine()){
          out.write(sc.nextLine()+"\n")
        }

        inStream.close()
       sc.close
        out.close()
      }
    } 
    
  }

  private def getEncoding(url: URL): String= {
    val buf = new Array[Byte](1024) 
    val detector = new UniversalDetector(null)
    val inStream =  url.openStream()

    var nread = inStream.read(buf)
    while (nread > 0 && !detector.isDone()) {
      detector.handleData(buf, 0, nread);
      nread = inStream.read(buf)
    }
    inStream.close()
    detector.dataEnd();

    val encoding = detector.getDetectedCharset();
    if (encoding != null) {
      encoding
      } else{
        "UTF-8"
      }

    }



  // Downloads the Html pointed bu the URL and print a trace
  private def printAndDownload(htmlURL: String): Unit = {
   this.downloadHTML(htmlURL)
   println("Downloaded : " + htmlURL)
 }

   /**
   * Dowloads all the Html
   */
   def downloadAll: Unit = URLManager.htmlURLs foreach printAndDownload

  /**
   * Downloads the nth group of 100 Html
   *
   * @param n
   *          number of the group of Html to download
   */
   def downloadGroupNb(n: Int): Unit =
   this.groupedURLs(n) foreach printAndDownload



 }
