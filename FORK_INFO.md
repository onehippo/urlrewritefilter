# Fork Information

This Hippo fork builds on [paultuckey/urlrewritefilter](https://github.com/paultuckey/urlrewritefilter).

## Branching and Versioning

The `master` branch is not be committed to so it can always easily be merged with Tuckey's master branch.

The maintenance branch, like `urlrewriterfilter-4.0.x` contains Hippo specific commits and can be merged with `master`.

A Hippo-specific urlrewritefilter version adds a -h1 (-h2, -h3 etc.) postfix to the version it extends.
For example, version `4.0.4-h1` extends urlrewritefilter `4.0.4`.

## Releases

### Release 4.0.4-h1
_Release Date: 21 March 2017_

Based on master 4.0.5-SNAPSHOT from March 2017, already including:
- [Issue 116: qsappend fix](https://github.com/paultuckey/urlrewritefilter/issues/116)

Then applied:
- [Issue 143: Fix any url pattern host replacement](https://github.com/paultuckey/urlrewritefilter/issues/143)
- [Issue 182: Bad VariableReplacer regex](https://github.com/paultuckey/urlrewritefilter/issues/182)
- [Issue 202: Enable optional cookie forward mechanism in proxy mode from pull request](https://github.com/paultuckey/urlrewritefilter/pull/202)
- [Issue 207: Condition type "request-filename" returns path with double context-path](https://github.com/paultuckey/urlrewritefilter/issues/207)

Additionally:
- Upgraded to Java 8
- Update versions of dependencies
- [HIPPLUG-1419](https://issues.onehippo.com/browse/HIPPLUG-1419): fix escaping in case of non-ASCII characters
