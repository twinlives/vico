/*
 * Copyright 2026 by Patryk Goworowski and Patrick Michalik.
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

object Versions {
  const val COMPILE_SDK = 37
  const val MIN_SDK = 23
  const val VICO = "3.3.1"

  /**
   * This fork’s revision of [VICO], in the manner of KSP’s versioning: the artifact version is
   * `"$VICO-$TWINLIVES"`, so it always names the upstream release it is built from. [VICO] is
   * upstream’s and arrives by merge; bump this one, and only this one, for our own releases.
   */
  const val TWINLIVES = "1.0.1"
}
