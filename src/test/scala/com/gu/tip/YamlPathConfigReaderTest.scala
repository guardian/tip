import java.io.FileNotFoundException

import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import com.gu.tip.{EnrichedPath, Path, Paths, YamlPathConfigReader}

class YamlPathConfigReaderTest extends FlatSpec {

  "YamlPathConfigReader" should "convert tip.yaml definition to Paths type" in {
    YamlPathConfigReader("tip.yaml") shouldBe a [Paths]
  }

  it should "read correct number of paths from tip.yaml" in {
    YamlPathConfigReader("tip.yaml").paths.size shouldEqual 2
  }

  it should "produce FileNotFoundException if tip.yaml does not exist" in {
      assertThrows[FileNotFoundException](YamlPathConfigReader("tipppp.yaml"))
  }

  "Paths" should "have no verified paths initially" in {
    YamlPathConfigReader("tip.yaml") should not be 'allVerified
  }

  it should "indicate when all paths are verified" in {
    val paths = YamlPathConfigReader("tip.yaml")
    paths.verify("Name A")
    paths.verify("Name B")
    paths shouldBe 'allVerified
  }

  it should "produce NoSuchElementException when a wrong path name is specified" in {
    assertThrows[NoSuchElementException] {
      YamlPathConfigReader("tip.yaml").verify("non-existant path name")
    }
  }

  it should "verify an EnrichedPath" in {
    val enrichedPath = new EnrichedPath(Path("Path Name", "Path Description"))
    val paths = new Paths(Map("Path Name" -> enrichedPath))
    paths.verify("Path Name")
    assert(paths.allVerified)
  }

  it should "verify a path only if it has not been verified before" in {
    val enrichedPathA = new EnrichedPath(Path("Path Name A", "Path Description A"))
    val enrichedPathB = new EnrichedPath(Path("Path Name B", "Path Description B"))
    val paths = new Paths(Map("Path Name A" -> enrichedPathA, "Path Name B" -> enrichedPathB))
    paths.verify("Path Name A")
    paths.verify("Path Name A")
    assert(!paths.allVerified)
  }
}