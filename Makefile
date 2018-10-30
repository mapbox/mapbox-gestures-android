checkstyle:
	./gradlew checkstyle

javadoc:
	# Output is (module)/build/docs/javadoc/release
	./gradlew javadocrelease

test:
	./gradlew :library:test -i

release:
	./gradlew :library:assembleRelease

publish:
	export IS_LOCAL_DEVELOPMENT=false; ./gradlew :library:uploadArchives

publish-local:
	# This publishes to ~/.m2/repository/com/mapbox/mapboxsdk
	export IS_LOCAL_DEVELOPMENT=true; ./gradlew :library:uploadArchives