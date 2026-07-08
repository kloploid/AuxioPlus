# Auxio+ contribution guidelines

**This repository is a fork of [Auxio](https://github.com/OxygenCobalt/Auxio) by
[OxygenCobalt](https://github.com/OxygenCobalt).** It adds a small set of features that are
out of scope upstream. Where an issue belongs depends on what it's about — please read this
first.

## Where do I report?

| What | Where |
| --- | --- |
| Bugs in fork-specific features (ex. the sleep timer) | **This repository's** [Issues](../../issues) |
| Bugs in the core player (playback, library, UI, widgets, Android Auto, …) | [Upstream Issues](https://github.com/OxygenCobalt/Auxio/issues) — reproduce on official Auxio first |
| Feature requests for the core player | [Upstream Issues](https://github.com/OxygenCobalt/Auxio/issues), after reading [Why Are These Features Missing?](https://github.com/OxygenCobalt/Auxio/wiki/Why-Are-These-Features-Missing%3F) |
| Feature requests already declined upstream | This repository's [Issues](../../issues) — they may fit this fork |
| Translations of the core app | The upstream [Weblate project](https://hosted.weblate.org/engage/auxio/) |

**Important:** if you hit a player bug while using this fork, please reproduce it on
[official Auxio](https://github.com/OxygenCobalt/Auxio/releases) before reporting it upstream.
The original author should not have to debug this fork's changes.

## Crashes & Bugs

When reporting an issue here, make sure that:
- **It hasn't been reported already** in this repository or upstream.
- **It's still relevant** in the latest version of this fork.

Provide:
- A description of the bug/crash
- A summary of the steps to reproduce it
- A stack trace/logcat if possible, the longer the better

## Code Contributions

Pull requests are welcome, both for fork features and for keeping the fork healthy. The
original project's rules apply here as well:

- If you want to add something, open an issue first so it can be discussed before you invest
  time into it.
- Do not bring non-free software into the project, such as binary blobs.
- Stick to the [F-Droid Inclusion Guidelines](https://f-droid.org/wiki/page/Inclusion_Policy).
- Make sure you stick to Auxio's styling, which should be auto-formatted on every build.
- Please ***FULLY TEST*** your changes before creating a PR. Untested code will not be merged.
- Only **Kotlin** will be accepted, except for the case that a UI component must be vendored
  in the project.
- Keep your branch up to date with this repository's `dev` while the PR is open.

If your change improves the *core player* rather than a fork feature, consider contributing it
[upstream](https://github.com/OxygenCobalt/Auxio) instead — everyone benefits, including this
fork. Reading about [Auxio's Architecture](https://github.com/OxygenCobalt/Auxio/wiki/Architecture)
is recommended before making changes.
