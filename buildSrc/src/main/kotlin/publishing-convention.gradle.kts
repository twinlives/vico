/*
 * Copyright 2025 by Patryk Goworowski and Patrick Michalik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins { id("com.vanniktech.maven.publish") }

publishing {
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/twinlives/vico")
      credentials {
        username =
          providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
        password =
          providers.gradleProperty("gpr.token").orNull ?: System.getenv("GITHUB_TOKEN")
      }
    }
  }
}

mavenPublishing {
  publishToMavenCentral(automaticRelease = true)
  // Signing is off unless explicitly asked for, and the switch is a property of our own rather
  // than one of the ambient `signing.*` / `signingInMemoryKey` ones. Those are commonly set in a
  // developer's global Gradle properties for unrelated projects, and keying off them turns
  // signing on for a key this build has no reason to hold: the signatory is resolved while the
  // task is being created, so a mismatch fails configuration, not just publication. GitHub
  // Packages does not want signatures anyway. For a signed release, pass `-PtwinlivesSigning=true`
  // together with the usual `ORG_GRADLE_PROJECT_signingInMemoryKey*` values.
  if (providers.gradleProperty("twinlivesSigning").getOrElse("false").toBoolean()) {
    signAllPublications()
  }
  pom {
    name = "Vico"
    description =
      "A powerful and extensible multiplatform chart library. Twin Lives fork; see " +
        "github.com/patrykandpatrick/vico for the original."
    url = "https://github.com/twinlives/vico"
    licenses {
      license {
        name = "The Apache License, Version 2.0"
        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
      }
    }
    scm {
      connection = "scm:git:git://github.com/twinlives/vico.git"
      developerConnection = "scm:git:ssh://github.com/twinlives/vico.git"
      url = "https://github.com/twinlives/vico"
    }
    // Upstream's authors stay listed: this is their work, republished under a different group
    // with a small patch on top, and the Apache-2.0 licence asks that attribution be kept.
    developers {
      developer {
        id = "patrykgoworowski"
        name = "Patryk Goworowski"
        email = "contact@patrykgoworowski.pl"
      }
      developer {
        id = "patrickmichalik"
        name = "Patrick Michalik"
        email = "contact@patrickmichalik.com"
      }
    }
  }
}
