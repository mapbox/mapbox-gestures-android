checkstyle:
	./gradlew checkstyle

javadoc:
	# Output is (module)/build/docs/javadoc/release
	./gradlew library:javadocrelease

test:
	./gradlew :library:test -i

release:
	./gradlew :library:assembleRelease

sdkRegistryUpload:
	./gradlew :library:mapboxSDKRegistryUpload

sdkRegistryPublish:
	./gradlew :library:mapboxSDKRegistryPublish
