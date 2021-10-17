package local.tux;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Hello world!
 */

@Slf4j
public class App {
    public static void main(String[] args) throws IOException {
        Server.start();
    }
}
