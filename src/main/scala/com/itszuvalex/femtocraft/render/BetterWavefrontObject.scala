package com.itszuvalex.femtocraft.render

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util
import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.resources.IResource
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.IModelCustom
import net.minecraftforge.client.model.ModelFormatException
import net.minecraftforge.client.model.obj.Face
import net.minecraftforge.client.model.obj.GroupObject
import net.minecraftforge.client.model.obj.TextureCoordinate
import net.minecraftforge.client.model.obj.Vertex
import org.lwjgl.opengl.GL11
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly

/**
 * Wavefront Object importer
 * Based heavily off of the specifications found at http://en.wikipedia.org/wiki/Wavefront_.obj_file
 */
object BetterWavefrontObject {
  private val vertexPattern: Pattern = Pattern.compile("(v( (\\-){0,1}\\d+\\.\\d+){3,4} *\\n)|(v( (\\-){0,1}\\d+\\.\\d+){3,4} *$)")
  private val vertexNormalPattern: Pattern = Pattern.compile("(vn( (\\-){0,1}\\d+\\.\\d+){3,4} *\\n)|(vn( (\\-){0,1}\\d+\\.\\d+){3,4} *$)")
  private val textureCoordinatePattern: Pattern = Pattern.compile("(vt( (\\-){0,1}\\d+\\.\\d+){2,3} *\\n)|(vt( (\\-){0,1}\\d+\\.\\d+){2,3} *$)")
  private val face_V_VT_VN_Pattern: Pattern = Pattern.compile("(f( \\d+/\\d+/\\d+){3,4} *\\n)|(f( \\d+/\\d+/\\d+){3,4} *$)")
  private val face_V_VT_Pattern: Pattern = Pattern.compile("(f( \\d+/\\d+){3,4} *\\n)|(f( \\d+/\\d+){3,4} *$)")
  private val face_V_VN_Pattern: Pattern = Pattern.compile("(f( \\d+//\\d+){3,4} *\\n)|(f( \\d+//\\d+){3,4} *$)")
  private val face_V_Pattern: Pattern = Pattern.compile("(f( \\d+){3,4} *\\n)|(f( \\d+){3,4} *$)")
  private val groupObjectPattern: Pattern = Pattern.compile("([go]( [\\w\\d]+) *\\n)|([go]( [\\w\\d]+) *$)")
  private var vertexMatcher: Matcher = null
  private var vertexNormalMatcher: Matcher = null
  private var textureCoordinateMatcher: Matcher = null
  private var face_V_VT_VN_Matcher: Matcher = null
  private var face_V_VT_Matcher: Matcher = null
  private var face_V_VN_Matcher: Matcher = null
  private var face_V_Matcher: Matcher = null
  private var groupObjectMatcher: Matcher = null

  /** *
    * Verifies that the given line from the model file is a valid vertex
    * @param line the line being validated
    * @return true if the line is a valid vertex, false otherwise
    */
  private def isValidVertexLine(line: String): Boolean = {
    if (vertexMatcher != null) {
      vertexMatcher.reset
    }
    vertexMatcher = vertexPattern.matcher(line)
    vertexMatcher.matches
  }

  /** *
    * Verifies that the given line from the model file is a valid vertex normal
    * @param line the line being validated
    * @return true if the line is a valid vertex normal, false otherwise
    */
  private def isValidVertexNormalLine(line: String): Boolean = {
    if (vertexNormalMatcher != null) {
      vertexNormalMatcher.reset
    }
    vertexNormalMatcher = vertexNormalPattern.matcher(line)
    vertexNormalMatcher.matches
  }

  /** *
    * Verifies that the given line from the model file is a valid texture coordinate
    * @param line the line being validated
    * @return true if the line is a valid texture coordinate, false otherwise
    */
  private def isValidTextureCoordinateLine(line: String): Boolean = {
    if (textureCoordinateMatcher != null) {
      textureCoordinateMatcher.reset
    }
    textureCoordinateMatcher = textureCoordinatePattern.matcher(line)
    textureCoordinateMatcher.matches
  }

  /** *
    * Verifies that the given line from the model file is a valid face that is described by vertices, texture coordinates, and vertex normals
    * @param line the line being validated
    * @return true if the line is a valid face that matches the format "f v1/vt1/vn1 ..." (with a minimum of 3 points in the face, and a maximum of 4), false otherwise
    */
  private def isValidFace_V_VT_VN_Line(line: String): Boolean = {
    if (face_V_VT_VN_Matcher != null) {
      face_V_VT_VN_Matcher.reset
    }
    face_V_VT_VN_Matcher = face_V_VT_VN_Pattern.matcher(line)
    face_V_VT_VN_Matcher.matches
  }

  /** *
    * Verifies that the given line from the model file is a valid face that is described by vertices and texture coordinates
    * @param line the line being validated
    * @return true if the line is a valid face that matches the format "f v1/vt1 ..." (with a minimum of 3 points in the face, and a maximum of 4), false otherwise
    */
  private def isValidFace_V_VT_Line(line: String): Boolean = {
    if (face_V_VT_Matcher != null) {
      face_V_VT_Matcher.reset
    }
    face_V_VT_Matcher = face_V_VT_Pattern.matcher(line)
    face_V_VT_Matcher.matches
  }

  /** *
    * Verifies that the given line from the model file is a valid face that is described by vertices and vertex normals
    * @param line the line being validated
    * @return true if the line is a valid face that matches the format "f v1//vn1 ..." (with a minimum of 3 points in the face, and a maximum of 4), false otherwise
    */
  private def isValidFace_V_VN_Line(line: String): Boolean = {
    if (face_V_VN_Matcher != null) {
      face_V_VN_Matcher.reset
    }
    face_V_VN_Matcher = face_V_VN_Pattern.matcher(line)
    face_V_VN_Matcher.matches
  }

  /** *
    * Verifies that the given line from the model file is a valid face that is described by only vertices
    * @param line the line being validated
    * @return true if the line is a valid face that matches the format "f v1 ..." (with a minimum of 3 points in the face, and a maximum of 4), false otherwise
    */
  private def isValidFace_V_Line(line: String): Boolean = {
    if (face_V_Matcher != null) {
      face_V_Matcher.reset
    }
    face_V_Matcher = face_V_Pattern.matcher(line)
    face_V_Matcher.matches
  }

  /** *
    * Verifies that the given line from the model file is a valid face of any of the possible face formats
    * @param line the line being validated
    * @return true if the line is a valid face that matches any of the valid face formats, false otherwise
    */
  private def isValidFaceLine(line: String): Boolean = {
    isValidFace_V_VT_VN_Line(line) || isValidFace_V_VT_Line(line) || isValidFace_V_VN_Line(line) || isValidFace_V_Line(line)
  }

  /** *
    * Verifies that the given line from the model file is a valid group (or object)
    * @param line the line being validated
    * @return true if the line is a valid group (or object), false otherwise
    */
  private def isValidGroupObjectLine(line: String): Boolean = {
    if (groupObjectMatcher != null) {
      groupObjectMatcher.reset
    }
    groupObjectMatcher = groupObjectPattern.matcher(line)
    groupObjectMatcher.matches
  }
}

class BetterWavefrontObject extends IModelCustom {
  var vertices: ArrayList[Vertex] = new ArrayList[Vertex]
  var vertexNormals: ArrayList[Vertex] = new ArrayList[Vertex]
  var textureCoordinates: ArrayList[TextureCoordinate] = new ArrayList[TextureCoordinate]
  var groupObjects: Map[String, GroupObject] = Map()
  private var currentGroupObject: GroupObject = null
  private var fileName: String = null

  @throws(classOf[ModelFormatException])
  def this(resource: ResourceLocation) {
    this()
    this.fileName = resource.toString
    try {
      val res: IResource = Minecraft.getMinecraft.getResourceManager.getResource(resource)
      loadObjModel(res.getInputStream)
    }
    catch {
      case e: IOException => {
        throw new ModelFormatException("IO Exception reading model format", e)
      }
    }
  }

  @throws(classOf[ModelFormatException])
  def this(filename: String, inputStream: InputStream) {
    this()
    this.fileName = filename
    loadObjModel(inputStream)
  }

  @throws(classOf[ModelFormatException])
  private def loadObjModel(inputStream: InputStream) {
    var reader: BufferedReader = null
    var currentLine: String = null
    var lineCount: Int = 0
    try {
      reader = new BufferedReader(new InputStreamReader(inputStream))
      while ({
        currentLine = reader.readLine; currentLine
      } != null) {
        lineCount += 1
        currentLine = currentLine.replaceAll("\\s+", " ").trim
        if (!(currentLine.startsWith("#") || currentLine.length == 0)) {
          if (currentLine.startsWith("v ")) {
            val vertex: Vertex = parseVertex(currentLine, lineCount)
            if (vertex != null) {
              vertices.add(vertex)
            }
          }
          else if (currentLine.startsWith("vn ")) {
            val vertex: Vertex = parseVertexNormal(currentLine, lineCount)
            if (vertex != null) {
              vertexNormals.add(vertex)
            }
          }
          else if (currentLine.startsWith("vt ")) {
            val textureCoordinate: TextureCoordinate = parseTextureCoordinate(currentLine, lineCount)
            if (textureCoordinate != null) {
              textureCoordinates.add(textureCoordinate)
            }
          }
          else if (currentLine.startsWith("f ")) {
            if (currentGroupObject == null) {
              currentGroupObject = new GroupObject("Default")
            }
            val face: Face = parseFace(currentLine, lineCount)
            if (face != null) {
              currentGroupObject.faces.add(face)
            }
          }
          else if (currentLine.startsWith("g ") | currentLine.startsWith("o ")) {
            val group: GroupObject = parseGroupObject(currentLine, lineCount)
            if (group != null) {
              if (currentGroupObject != null) {
                groupObjects = groupObjects ++ Map(currentGroupObject.name -> currentGroupObject)
              }
            }
            currentGroupObject = group
          }
        }
      }
      groupObjects = groupObjects ++ Map(currentGroupObject.name -> currentGroupObject)
    }
    catch {
      case e: IOException =>
        throw new ModelFormatException("IO Exception reading model format", e)
    } finally {
      try {
        reader.close
      }
      catch {
        case e: IOException =>
      }
      try {
        inputStream.close
      }
      catch {
        case e: IOException =>
      }
    }
  }

  @SideOnly(Side.CLIENT) def renderAll {
    val tessellator: Tessellator = Tessellator.instance
    if (currentGroupObject != null) {
      tessellator.startDrawing(currentGroupObject.glDrawingMode)
    }
    else {
      tessellator.startDrawing(GL11.GL_TRIANGLES)
    }
    tessellateAll(tessellator)
    tessellator.draw
  }

  @SideOnly(Side.CLIENT) def tessellateAll(tessellator: Tessellator) {
    groupObjects.foreach( groupObject => { groupObject._2.render(tessellator) } )
  }

  @SideOnly(Side.CLIENT) def renderOnly(groupNames: String*) {
    for (groupName <- groupNames) {
      groupObjects(groupName).render()
    }
  }

  @SideOnly(Side.CLIENT) def tessellateOnly(tessellator: Tessellator, groupNames: String*) {
    for (groupName <- groupNames) {
      groupObjects(groupName).render(tessellator)
    }
  }

  @SideOnly(Side.CLIENT) def renderPart(partName: String) {
    groupObjects(partName).render()
  }

  @SideOnly(Side.CLIENT) def tessellatePart(tessellator: Tessellator, partName: String) {
    groupObjects(partName).render(tessellator)
  }

  @SideOnly(Side.CLIENT) def renderAllExcept(excludedGroupNames: String*) {
    groupObjects.foreach { groupObject =>
      if (!excludedGroupNames.contains(groupObject._1)) groupObject._2.render()
    }
  }

  @SideOnly(Side.CLIENT) def tessellateAllExcept(tessellator: Tessellator, excludedGroupNames: String*) {
    groupObjects.foreach { groupObject =>
      if (!excludedGroupNames.contains(groupObject._1)) groupObject._2.render(tessellator)
    }
  }

  @throws(classOf[ModelFormatException])
  private def parseVertex(inputLine: String, lineCount: Int): Vertex = {
    val vertex: Vertex = null
    var line = inputLine
    if (BetterWavefrontObject.isValidVertexLine(line)) {
      line = line.substring(line.indexOf(" ") + 1)
      val tokens: Array[String] = line.split(" ")
      try {
        if (tokens.length == 2) {
          return new Vertex(tokens{0}.toFloat, tokens{1}.toFloat)
        }
        else if (tokens.length == 3) {
          return new Vertex(tokens{0}.toFloat, tokens{1}.toFloat, tokens{2}.toFloat)
        }
      }
      catch {
        case e: NumberFormatException =>
          throw new ModelFormatException("Number formatting error at line " + lineCount, e)
      }
    }
    else {
      throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format")
    }
    vertex
  }

  @throws(classOf[ModelFormatException])
  private def parseVertexNormal(inputLine: String, lineCount: Int): Vertex = {
    val vertexNormal: Vertex = null
    var line = inputLine
    if (BetterWavefrontObject.isValidVertexNormalLine(line)) {
      line = line.substring(line.indexOf(" ") + 1)
      val tokens: Array[String] = line.split(" ")
      try {
        if (tokens.length == 3) return new Vertex(tokens{0}.toFloat, tokens{1}.toFloat, tokens{2}.toFloat)
      }
      catch {
        case e: NumberFormatException =>
          throw new ModelFormatException("Number formatting error at line " + lineCount, e)
      }
    }
    else {
      throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format")
    }
    vertexNormal
  }

  @throws(classOf[ModelFormatException])
  private def parseTextureCoordinate(inputLine: String, lineCount: Int): TextureCoordinate = {
    val textureCoordinate: TextureCoordinate = null
    var line = inputLine
    if (BetterWavefrontObject.isValidTextureCoordinateLine(line)) {
      line = line.substring(line.indexOf(" ") + 1)
      val tokens: Array[String] = line.split(" ")
      try {
        if (tokens.length == 2) return new TextureCoordinate(tokens{0}.toFloat, 1 - tokens{1}.toFloat)
        else if (tokens.length == 3) return new TextureCoordinate(tokens{0}.toFloat, 1 - tokens{1}.toFloat, tokens{2}.toFloat)
      }
      catch {
        case e: NumberFormatException =>
          throw new ModelFormatException("Number formatting error at line " + lineCount, e)
      }
    }
    else {
      throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format")
    }
    textureCoordinate
  }

  @throws(classOf[ModelFormatException])
  private def parseFace(line: String, lineCount: Int): Face = {
    var face: Face = null
    if (BetterWavefrontObject.isValidFaceLine(line)) {
      face = new Face
      val trimmedLine: String = line.substring(line.indexOf(" ") + 1)
      val tokens: Array[String] = trimmedLine.split(" ")
      var subTokens: Array[String] = null
      if (tokens.length == 3) {
        if (currentGroupObject.glDrawingMode == -1) {
          currentGroupObject.glDrawingMode = GL11.GL_TRIANGLES
        }
        else if (currentGroupObject.glDrawingMode != GL11.GL_TRIANGLES) {
          throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Invalid number of points for face (expected 4, found " + tokens.length + ")")
        }
      }
      else if (tokens.length == 4) {
        if (currentGroupObject.glDrawingMode == -1) {
          currentGroupObject.glDrawingMode = GL11.GL_QUADS
        }
        else if (currentGroupObject.glDrawingMode != GL11.GL_QUADS) {
          throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Invalid number of points for face (expected 3, found " + tokens.length + ")")
        }
      }
      if (BetterWavefrontObject.isValidFace_V_VT_VN_Line(line)) {
        face.vertices = new Array[Vertex](tokens.length)
        face.textureCoordinates = new Array[TextureCoordinate](tokens.length)
        face.vertexNormals = new Array[Vertex](tokens.length)

        for (i <- 0 to tokens.length - 1) {
          subTokens = tokens(i).split("/")
          face.vertices(i) = vertices.get(subTokens(0).toInt - 1)
          face.textureCoordinates(i) = textureCoordinates.get(subTokens(1).toInt - 1)
          face.vertexNormals(i) = vertexNormals.get(subTokens(2).toInt - 1)
        }

        face.faceNormal = face.calculateFaceNormal
      }
      else if (BetterWavefrontObject.isValidFace_V_VT_Line(line)) {
        face.vertices = new Array[Vertex](tokens.length)
        face.textureCoordinates = new Array[TextureCoordinate](tokens.length)

        for (i <- 0 to tokens.length - 1) {
          subTokens = tokens(i).split("/")
          face.vertices(i) = vertices.get(subTokens(0).toInt - 1)
          face.textureCoordinates(i) = textureCoordinates.get(subTokens(1).toInt - 1)
        }

      face.faceNormal = face.calculateFaceNormal
      }
      else if (BetterWavefrontObject.isValidFace_V_VN_Line(line)) {
        face.vertices = new Array[Vertex](tokens.length)
        face.vertexNormals = new Array[Vertex](tokens.length)

        for (i <- 0 to tokens.length - 1) {
          subTokens = tokens(i).split("//")
          face.vertices(i) = vertices.get(subTokens(0).toInt - 1)
          face.vertexNormals(i) = vertexNormals.get(subTokens(1).toInt - 1)
        }

        face.faceNormal = face.calculateFaceNormal
      }
      else if (BetterWavefrontObject.isValidFace_V_Line(line)) {
        face.vertices = new Array[Vertex](tokens.length)
        
        for (i <- 0 to tokens.length - 1) face.vertices(i) = vertices.get(tokens(i).toInt - 1)
        
        face.faceNormal = face.calculateFaceNormal
      }
      else {
        throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format")
      }
    }
    else {
      throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format")
    }
    face
  }

  @throws(classOf[ModelFormatException])
  private def parseGroupObject(line: String, lineCount: Int): GroupObject = {
    var group: GroupObject = null
    if (BetterWavefrontObject.isValidGroupObjectLine(line)) {
      val trimmedLine: String = line.substring(line.indexOf(" ") + 1)
      if (trimmedLine.length > 0) {
        group = new GroupObject(trimmedLine)
      }
    }
    else {
      throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format")
    }
    group
  }

  def getType: String = {
    "obj"
  }
}