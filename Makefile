checkstyle:
	./gradlew checkstyle

test:
	./gradlew :library:test

release:
	./gradlew clean :library:assembleRelease

publish:
	export IS_LOCAL_DEVELOPMENT=false; ./gradlew :library:uploadArchives

publish-local:
	# This publishes to ~/.m2/repository/com/mapbox/mapboxsdk
	export IS_LOCAL_DEVELOPMENT=true; ./gradlew :library:uploadArchives