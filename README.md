### Publishing to Maven

By default, this repo can be published to Bintray as Readdle.  For other publishing schemes, add a file X-publish.gradle to the root of the project.  Then add a key to local.properties, or pass a project setting to gradle with the name `publishing_designator`.  Thus, to define a publishing scheme in `hf-publish.gradle`, add this to your local.properties:
```
publishing_designator=hf
```

#### Publish as GitHub maven packages
Add the following to your local.properties

```
gpr.user=your github access token username
gpr.key=your github access token password
publishing_designator=hf
```

To publish to a github organization other than heliumfoot add this additional key:

```
github.organization=your organization
```

Publish with these commands:

```
./gradlew compiler:publish
./gradlew library:publish
```
