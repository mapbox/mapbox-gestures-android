check:
	./gradlew checkstyle && ./gradlew lint

test:
	./gradlew :library:test

release:
	./gradlew :library:assembleRelease

testAppRelease:
	./gradlew :app:assembleRelease

publish:
	export IS_LOCAL_DEVELOPMENT=false; ./gradlew :library:uploadArchives

publish-local:
	# This publishes to ~/.m2/repository/com/mapbox/mapboxsdk
	export IS_LOCAL_DEVELOPMENT=true; ./gradlew :library:uploadArchives