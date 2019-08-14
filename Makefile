checkstyle:
	./gradlew checkstyle

javadoc:
	# Output is (module)/build/docs/javadoc/release
	./gradlew library:javadocrelease

test:
	./gradlew :library:test -i

release:
	./gradlew :library:assembleRelease

publish-stable:
	./gradlew :library:bintrayUpload

publish-snapshot-to-artifactory:
	./gradlew :library:artifactoryPublish
