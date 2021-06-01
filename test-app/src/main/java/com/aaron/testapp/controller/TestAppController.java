package com.aaron.testapp.controller;

import com.aaron.framework.FrameworkInject;
import com.aaron.testapp.model.TestAppModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestAppController {

    private static Logger log = LoggerFactory.getLogger(TestAppController.class);
    private TestAppModel testAppModel;

    public void setNames(String first, String last) {
        log.info("invoking setNames() on TestAppController: " + this.toString());
        testAppModel.setFirstName(first);
        testAppModel.setLastName(last);
    }

    public String getFirstName() {
        return testAppModel.getFirstName();
    }

    public String getLastName() {
        return testAppModel.getLastName();
    }

    @FrameworkInject
    public void setTestAppModel(TestAppModel testAppModel) {
        this.testAppModel = testAppModel;
        log.info("set TestAppModel on TestAppController: " + this.toString() + ", " + testAppModel.toString());
    }

}
