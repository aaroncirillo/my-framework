package com.aaron.testapp;

import com.aaron.framework.*;
import com.aaron.testapp.controller.TestAppController;
import com.aaron.testapp.model.TestAppModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FrameworkApp
public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);
    private TestAppController testAppController;

    public static void main(String args[]) {
        App app = FrameworkAppFactory.initialize("com.aaron");
        Main m = app.getMain(Main.class);
        m.invoke();
        try {
            TestAppController testAppController = app.getBean(TestAppController.class);
            testAppController.setNames("other", "name");
            log.info("Name: " + testAppController.getFirstName() + " " + testAppController.getLastName());
        } catch (NoSuchBeanException e) {
            log.error("", e);
        }
    }

    @FrameworkBean
    public TestAppModel testAppModel() {
        return new TestAppModel();
    }

    @FrameworkBean
    public TestAppController testAppController() {
        return new TestAppController();
    }

    @FrameworkInject
    public void setTestAppController(TestAppController controller) {
        this.testAppController = controller;
    }

    private void invoke() {
        testAppController.setNames("aaron", "cirillo");
        log.info("Name: " + testAppController.getFirstName() + " " + testAppController.getLastName());
    }

}
