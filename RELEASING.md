To release:

* Edit [CHANGELOG.md](CHANGELOG.md)
* Edit [gradle.properties](gradle.properties)
* Commit everything:

```bash
gcam "vx.y.z Some description"
```

* Tidy commits:

```bash
grb -i --root
```

* Add a tag:

```bash
gta v1.1.1 -m "Bug fix"
```

* Push:

```bash
gpot
```
