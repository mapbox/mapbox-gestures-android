checkstyle:
	./gradlew checkstyle

test:
	./gradlew :library:test

release:
	./gradlew :library:assembleRelease