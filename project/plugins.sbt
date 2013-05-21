resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "Local Maven Repository" at "file:///"+Path.userHome+"/.m2/repository"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.4.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.8.8")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.6.2")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.3")

addSbtPlugin("de.djini" % "xsbt-reflect" % "0.0.3")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.2.0")
