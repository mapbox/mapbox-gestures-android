checkstyle:
	./gradlew checkstyle

test:
	./gradlew :library:test
	./gradlew :library:test

release:
	./gradlew :library:assembleRelease
	./gradlew :library:assembleRelease