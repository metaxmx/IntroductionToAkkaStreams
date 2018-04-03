resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sbtPluginRepo("releases")
)

addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.0")
