# Fork Information

This BloomReach fork builds on [paultuckey/urlrewritefilter](https://github.com/paultuckey/urlrewritefilter).

## Branching and Versioning

The `master` branch is not be committed to so it can always easily be merged with Tuckey's master branch.

The maintenance branch, like `urlrewriterfilter-4.0.x` contains BloomReach-specific commits and can be merged with `master`.

A BloomReach-specific urlrewritefilter version adds a -h1 (-h2, -h3 etc.) postfix to the version it extends.
For example, version `4.0.4-h9` extends urlrewritefilter `4.0.4`.

## Releases

### Release 4.0.4-h9
_Release Date: 13 February 2020_

- [HIPPLUG-1646](https://issues.onehippo.com/browse/HIPPLUG-1646): 
    in RequestProxy, do not recreate the URL object with the default port, as it can break SSL offloading.
    
### Release 4.0.4-h8
_Release Date: 18 October 2019_

- [HIPPLUG-1606](https://issues.onehippo.com/browse/HIPPLUG-1606): 
    in RequestProxy settings, add followRedirects and useSystemProperties.

### Release 4.0.4-h7
_Release Date: 17 July 2019_

- [HIPPLUG-1595](https://issues.onehippo.com/browse/HIPPLUG-1587): 
    fix NullPointerException in RequestProxy in case response entity is empty.

### Release 4.0.4-h5
_Release Date: 22 March 2019_

- [HIPPLUG-1587](https://issues.onehippo.com/browse/HIPPLUG-1587): in XML rule, accept CDATA values in elements, 
  so escaping is not needed in advanced values. E.g. advanced RegExp's or a parameterized URL: ``<to><![CDATA[to-url&param=1]]></to>``. 

### Release 4.0.4-h4
_Release Date: 25 January 2019_

- [HIPPLUG-1570](https://issues.onehippo.com/browse/HIPPLUG-1570): add a facility to ``UrlRewriteWrappedResponse`` to 
  inject disallowed duplicate header names. Method ``addHeader`` checks this to avoid the headers to be added twice.
- Using javax servlet-api version 3.0.1

### Release 4.0.4-h3
_Release Date: 3 December 2018_

- [HIPPLUG-1565](https://issues.onehippo.com/browse/HIPPLUG-1565): remove dependency on Guava

### Release 4.0.4-h2
_Release Date: 30 March 2017_

- Validate for [CWE 113 attack](https://cwe.mitre.org/data/definitions/113.html)
- Update to httpclient 4.5.5

### Release 4.0.4-h1
_Release Date: 23 March 2017_

Based on master 4.0.5-SNAPSHOT from March 2017, already including:
- [Issue 116: qsappend fix](https://github.com/paultuckey/urlrewritefilter/issues/116)

Then applied:
- [Issue 143: Fix any url pattern host replacement](https://github.com/paultuckey/urlrewritefilter/issues/143)
- [Issue 182: Bad VariableReplacer regex](https://github.com/paultuckey/urlrewritefilter/issues/182)
- [Issue 202: Enable optional cookie forward mechanism in proxy mode from pull request](https://github.com/paultuckey/urlrewritefilter/pull/202)
- [Issue 207: Condition type "request-filename" returns path with double context-path](https://github.com/paultuckey/urlrewritefilter/issues/207)

Additionally:
- Upgrade to Java 8
- Use `org.onehippo.cms7:hippo-cms7-project` as parent pom, for plugin version management 
- [HIPPLUG-1419](https://issues.onehippo.com/browse/HIPPLUG-1419): fix escaping in case of non-ASCII characters, e.g. Cyrillic.