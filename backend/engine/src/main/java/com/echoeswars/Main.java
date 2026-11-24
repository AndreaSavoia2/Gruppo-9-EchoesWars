package com.echoeswars;

public class Main {

    public static void main(String[] args) {

        // Start the application.
        EchoesWarsApplication application =
                new EchoesWarsApplication();

        int exitCode = application.run(args);

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}
