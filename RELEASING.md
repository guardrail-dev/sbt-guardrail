1. Grab the associated release text from [twilio/guardrail/releases](https://github.com/twilio/guardrail/releases)

2. Create a release tag: [link](https://github.com/twilio/sbt-guardrail/releases)

3. Checkout the tag. This will cause `sbt version` to print out a non-SNAPSHOT version number

4. `sbt publishSigned` to stage artifacts

5. `sbt bintrayRelease` to release binary artifacts
