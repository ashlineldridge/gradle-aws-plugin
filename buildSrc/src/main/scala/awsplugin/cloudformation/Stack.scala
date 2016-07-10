package awsplugin.cloudformation

import java.io.File

import com.amazonaws.regions.Region

case class Stack(name: String,
                 qualifiedName: String,
                 region: Region,
                 params: Map[String, String],
                 tags: Map[String, String],
                 template: File)
