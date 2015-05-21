import scala.io.Source

/**
 * Object that manage the generation of the Html URLs to download.
 *
 * @author Ouamar Sais
 */
 object URLManager {

 // Prefix of all the URLs
 private val prefix = "http://www.assemblee-nationale.fr"


  // URLs of the sessions pages
  private val indexes = for (leg <- 12 to 12) yield (this.prefix + "/" + leg + "/debats/index.asp")


  // Loads the HTML page pointed by the URL in a string
  private def urlContentToString(url: String): String = {
    val html = Source.fromURL(url, "iso-8859-1")
    html.mkString
  }

// Higher order function that catch an URL from a HTML line
private def catchURL(
  from: String,
  linesFilter: (String) => (Boolean),
  urlMapping: (String) => (String)): List[String] = {
  val html = urlContentToString(from)
  val linesToKeep = (html split '\n') filter linesFilter

  (linesToKeep map urlMapping).toList
}

  // Isolates a session URL
  private def catchSessionURL(ann: Int, line: String): String ={
    var res = ""
    if(ann == 14){
      res = line.substring((line indexOf "href=\"") + 6, (line indexOf "Voir tous les") - 2)
      }else if(!line.contains("plf")){
        //Ne prend pas en compte les commissions élargies
          res = line.substring((line indexOf "href=\"") + 6, (line indexOf "Compte rendu int&eacute;gral") - 2)
      }
      res
    }



// Generates all the session URLs from a index page 
private def sessionsURL(index: String): List[String] = {
  val parts = index split '/'
  var ann = 14
  if(parts(3).toInt != 14){
    ann = parts(3).toInt
  }

  this.catchURL(
    index,
    {x: String => (x contains "/\">Compte rendu int&eacute;gral") || (x contains "\">Voir tous les")}, 
    {x: String => this.prefix + this.catchSessionURL(ann, x)})
}


// Isolates a Html URLs
private def catchHTMLURL(line: String): String =
  line.substring((line indexOf "href=\"") + 6, (line indexOf ".asp") + 4)


  // Generates all the Html URLs from a session page
private def htmlURL(session: String): List[String] = {
    this.catchURL(
      session,
      {x: String => x contains "<h1 class=\"seance\">"},
      {x: String => {
        val splitSession = session split '/'
        val leg = splitSession(3)

        this.prefix + "/" +leg +"/cri/" + splitSession(5)+"/"+this.catchHTMLURL(x)
        }})
}



/**
   * List of all the HTML URLs to download
   */
   val htmlURLs = for {
    index <- this.indexes
    session <- this sessionsURL index
    html <- this htmlURL session 
    } yield html


    def test: Unit = println(htmlURLs mkString "\n")


  }