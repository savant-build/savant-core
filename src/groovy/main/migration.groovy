import org.savantbuild.io.FileTools
import org.savantbuild.io.MD5

/**
 * Migrates a 1.0 savant repository to 2.0.  The primary migration
 * is to reverse the old artifact:group format to the new 2.0 format
 *
 * This script requires the savant-core jar
 */

validateArgs()

def validateArgs() {
  if (args.size() != 1) {
    println("""Usage: migration.groovy <dir>

  dir - This is the directory that is the root of the Savant 2.0 repository
""")
    System.exit(-1)
  }
}

File dir = new File(args[0])

// find amd files
dir.eachDirRecurse { d ->
  d.eachFileRecurse { file ->
    migrateAmd(file)
  }
}

/**
 * Migrate the amd and MD5 files.
 *
 * @param amdFile the amd file
 * @return void
 */
def migrateAmd(File amdFile) {
  if (amdFile.isFile() && amdFile.getName().endsWith(".amd")) {
    println("Migrating ${amdFile.getAbsolutePath()}")

    // save old
    String xml = amdFile.getText()

//    println("\nOld XML:")
//    println(xml)

    xml = fixCompatTypeBug(xml)
    xml = fixCloseDependenciesBug(xml)
    xml = changeArtifactGroupElements(xml)
    xml = flipGroups(xml)

//    println("\nFixed XML")
//    println(xml)

//    println("Updating ${amdFile.getAbsolutePath()} with ${xml}")
    amdFile.write(xml)

    File amdMd5File = new File("${amdFile.absolutePath}.md5")
    MD5 md5 = FileTools.md5(amdFile)
    amdMd5File.write(md5.sum)
  }
}

/**
 * Parses the old xml, finds the artifacts, and modifies the groups to be the new format
 *
 * @param xml the xml from the old amd file
 * @return the new, properly formatted xml
 */
def flipGroups(String xml) {
  return xml.replaceAll(~"group=\"(.+?)\"") {
    "group=\"" + it[1].tokenize(".").reverse().join(".") + "\""
  }
}

/**
 * Some old files contained this:
 *
 * <artifact-meta-data> compatType="minor">
 *
 * This method fixes it
 *
 * @param node the root xml node
 * @return
 */
def fixCompatTypeBug(String xml) {
  return xml.replace("> compatType=", " compatType=")
}

/**
 * Some old files contained this:
 *
 * </depedencies>
 *
 * This method fixes it
 *
 * @param node the root xml node
 * @return
 */
def fixCloseDependenciesBug(String xml) {
  return xml.replace("</depedencies>", "</dependencies>")
}

/**
 * Some old files contained this:
 *
 * <artifactGroup>
 *
 * This method changes to the new name
 *
 * @param node the root xml node
 * @return The fixed XML
 */
def changeArtifactGroupElements(String xml) {
  return xml.replace("artifactGroup", "artifact-group")
}
