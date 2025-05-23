package com.freshworks.ex.scenarios;

import java.util.Arrays;
import java.util.List;

import static com.freshworks.ex.scenarios.Testcase.newCase;

public class TestcaseRepository {

    private static List<Testcase> testcases = Arrays.asList(
            newCase(1, """
                    Generate a unique dynamic random email address using yopmail domain
                    and then fetch the created contact
                    """, Category.Requester),
            newCase(2, """
                    Generate a unique dynamic random email address using yopmail domain 
                    and then delete the created contact
                    """, Category.Requester),
            newCase(3, """
                    Generate a unique dynamic random email address using yopmail domain 
                    and then forget the created contact
                    """, Category.Requester)
    );


    public static List<Testcase> load() {
        return testcases;
    }
}
