package com.myorg;

import org.junit.jupiter.api.Test;

public class NothingTest {

    @Test
    public void test() {
        String x = "t1.micro";
        String[] y = x.split("\\.");
        System.out.println(y);
    }

}
