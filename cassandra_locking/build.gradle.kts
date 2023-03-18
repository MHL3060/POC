plugins {
	java
	id("org.springframework.boot") version "3.0.4"
	 id("io.spring.dependency-management") version "1.1.0"
}

group = "local.tux"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// implementation("org.springframework.boot:spring-boot-starter")
	// testImplementation("org.springframework.boot:spring-boot-starter-test")
	/*implementation("org.springframework.boot:spring-boot-starter-data-cassandra-reactive")
	implementation("org.springframework.boot:spring-boot-starter-graphql")
	implementation("org.springframework.boot:spring-boot-starter-integration")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.apache.kafka:kafka-streams")
	implementation("org.springframework.integration:spring-integration-kafka")
	implementation("org.springframework.integration:spring-integration-webflux")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.springframework.graphql:spring-graphql-test")
	testImplementation("org.springframework.integration:spring-integration-test")*/
}

tasks.withType<Test> {
	useJUnitPlatform()
}
