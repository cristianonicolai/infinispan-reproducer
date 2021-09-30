package org.acme;

import java.time.Duration;
import java.util.function.Consumer;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class InfinispanContainer extends GenericContainer {

    public static final int PORT = 11222;

    public InfinispanContainer() {
        super(DockerImageName.parse("infinispan/server:12.1.7.Final"));
        waitingFor(Wait.forHttp("/").withStartupTimeout(Duration.ofMinutes(1)));
        withExposedPorts(PORT);
        addFixedExposedPort(PORT, PORT);
        withEnv("USER", "admin");
        withEnv("PASS", "admin");
        withLogConsumer(logConsumer());
    }
    
    protected Consumer<OutputFrame> logConsumer(){
        return f -> System.out.print(f.getUtf8String());
    }
}
