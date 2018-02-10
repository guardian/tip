sonatypeProfileName := "com.gu"
publishMavenStyle := true
licenses := Seq("GPL3" -> url("https://www.gnu.org/licenses/gpl-3.0.en.html"))
homepage := Some(url("https://github.com/guardian/tip"))
scmInfo := Some(ScmInfo(url("https://github.com/guardian/tip"), "scm:git@github.com:guardian/tip.git"))
developers := List(
  Developer(id="mario-galic", name="Mario Galic", email="", url=url("https://github.com/mario-galic")),
  Developer(id="jacobwinch", name="Jacob Winch", email="", url=url("https://github.com/jacobwinch"))
)