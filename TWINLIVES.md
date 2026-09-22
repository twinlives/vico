# Twin Lives fork of Vico

A fork of [patrykandpatrick/vico](https://github.com/patrykandpatrick/vico), published as
`com.twinlives.vico:compose` and consumed by [oraahi](https://github.com/twinlives/oraahi)'s
`core:ui:designsystem`.

It exists to carry one change upstream does not have while still taking upstream's updates. It is
deliberately tiny — **30 lines across two files** — because the smaller the delta, the cheaper
every future merge is. Keep it that way: before adding a patch here, look hard for a stock
extension point that does the same job.

Upstream files are left alone wherever possible, which is why this document is its own file rather
than a section in `README.md` or `AGENTS.md`. Anything written into a file upstream also edits
conflicts on every sync.

## Branches

| Branch | Role |
|---|---|
| `master` | A pure mirror of `upstream/master`. Only ever fast-forwarded. Never commit here. |
| `twinlives` | The integration branch, and the repo default. Our patches live here. |

Both are protected by rulesets (`protect-master-mirror`, `protect-twinlives`) against force pushes
and deletion, with **no bypass actors** — the protection applies to repository admins too. Ordinary
pushes and fast-forwards are unaffected; only history rewrites are blocked.

**Never use GitHub's "Sync fork" button.** It targets the default branch, and when it cannot
fast-forward it offers to *discard commits* — which would delete the patches. Sync with the
commands below instead. The rulesets are the backstop if someone clicks it anyway.

Renovate is disabled in `renovate.json` on this branch. Its config targets `master` with
`automerge` on, so if the app were ever installed it would merge dependency PRs into the mirror and
break the fast-forward guarantee. Upstream's dependency bumps still reach us through merges.

## What we patch

Both changes are in `vico/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/compose/cartesian/`.

- **`VicoScrollState.visibleXRange`** — the _x_ range currently in view, as snapshot state,
  computed at the end of `update()`. The library already computes this internally for marker
  lookup and the horizontal axis but never exposes it. oraahi reads it to re-publish a downsampled
  window as the chart scrolls.
- **`CartesianChartHost`, marker retention** — a drag that briefly finds no target keeps the last
  marker instead of blinking it out for a frame. Sparse data makes this visible.

Four further patches were removed in favour of stock APIs, and are recorded here so nobody
reintroduces them: fixing a layer's ranges is `CartesianLayerRangeProvider`; overriding the x step
is `CartesianChart(getXStep = ...)`; changing marker lock behaviour is a custom
`CartesianMarkerController`; and `getRepeating` is three lines worth copying into the consumer.

## Versioning

The artifact version is `"${Versions.VICO}-${Versions.TWINLIVES}"`, following KSP's convention, so
it always names the upstream release it was built from.

- `VICO` is upstream's own version name. It arrives by merge — never edit it.
- `TWINLIVES` is ours, a continuous counter. **Bump it for every release**, including a release
  that only takes upstream changes.

A released version is immutable in GitHub Packages, so republishing without a bump cannot work.
Ordering holds within our own line: `3.3.1-1.0.1` supersedes `3.3.1-1.0.0`, and `3.3.2-1.0.0`
supersedes both.

Note the version can lag reality: `master` is often some commits past upstream's last tag while
upstream still calls itself the old version. That is upstream's own naming and we follow it.

## Publishing

The **Publish to GitHub Packages** workflow, manually triggered:

```bash
gh workflow run publish-twinlives.yml --repo twinlives/vico --ref twinlives
```

It runs on an Ubuntu runner — including for the iOS klibs, because
`kotlin.native.enableKlibsCrossCompilation` is set — and authenticates with the built-in
`GITHUB_TOKEN`, so there is no secret to manage. A pre-flight check refuses in seconds if the
version is already published, and a post-check confirms all seven target variants landed.

It is manual on purpose. A `push` trigger on `Versions.kt` was tried and removed: it fired on
upstream's schedule rather than ours, and raced a manual run into a 409.

Signing is off unless `-PtwinlivesSigning=true` is passed. It is gated on a property of our own
rather than the ambient `signing.*` / `signingInMemoryKey*` ones, because those are commonly set
in a developer's global Gradle properties for other projects — and a key mismatch fails while the
signing task is being *created*, which breaks configuration, not just publishing. GitHub Packages
does not want signatures anyway.

## Consuming

GitHub Packages requires credentials even for a public package. One-time, per machine:

```bash
gh auth refresh -s read:packages
```

oraahi's `settings.gradle.kts` then finds a token from, in order: `github.packages_read_token`
in its `secret.properties`, the `GITHUB_TOKEN` environment variable (how CI supplies it), then
`gh auth token`. The last means the command above is usually all a developer needs; the
`secret.properties` key is there for machines that would rather not depend on the CLI.

In CI, the job needs `permissions: { contents: read, packages: read }`. Listing any permission
makes the token restrictive, so `contents` must be named alongside `packages` or checkout loses its
access. No secret is required: both repositories belong to the same account and the packages are
public, so each repository's own `GITHUB_TOKEN` suffices. No package Actions-access grant is
needed either — that is for private packages.

## Syncing from upstream

```bash
git fetch upstream

# Will the merge conflict? If this diff is empty, no.
git diff --stat master..upstream/master -- \
  vico/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/compose/cartesian/VicoScrollState.kt \
  vico/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/compose/cartesian/CartesianChartHost.kt

git checkout master && git merge --ff-only upstream/master && git push origin master
git checkout twinlives && git merge master
# bump TWINLIVES in buildSrc/src/main/kotlin/Versions.kt, commit, push
gh workflow run publish-twinlives.yml --repo twinlives/vico --ref twinlives
# then bump `vico` in oraahi's gradle/libs.versions.toml
```

That diff is the cheap way to know where a conflict will be before hitting it. Conflicts can only
arise in those two files.

Build and test locally with JDK 17: `./gradlew compileDebugSources test`.

## Gotchas

- **Probing the registry needs `curl -sL`.** GitHub Packages answers `302` and redirects to blob
  storage, so an unfollowed probe reports 302 for a file that is present. Both workflow checks were
  written against `200` and silently did the wrong thing until fixed.
- **`VicoScrollState.kt` is as churned as `CartesianChartHost.kt`.** Putting a patch there is not
  quieter ground. It is preferred because a property plus one statement survives churn better than
  a parameter threaded through public overload signatures, which upstream keeps adding to.
- **Repo settings need the `twinlives` account.** A collaborator with push access gets `404`, not
  `403`, from admin-only endpoints — rulesets, default branch, package settings.
