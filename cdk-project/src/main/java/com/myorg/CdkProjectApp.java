package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public final class CdkProjectApp {
    public static void main(final String[] args) {
        App app = new App();

        Environment env = makeEnv(System.getenv("WEEK_0_ENV_ACC"), "us-east-1");

        new Week0Stack(app, "SimpleEC2Stack", StackProps.builder().env(env).build());

        app.synth();
    }

    static Environment makeEnv(String account, String region) {
        return Environment.builder()
                .account(account)
                .region(region)
                .build();
    }
}
