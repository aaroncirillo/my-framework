package com.aaron.testapp;

import com.aaron.framework.FrameworkApp;
import com.aaron.framework.FrameworkAppFactory;
import com.aaron.framework.FrameworkBean;
import com.aaron.framework.FrameworkInject;
import com.aaron.testapp.controller.TestAppController;
import com.aaron.testapp.model.TestAppModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FrameworkApp
public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);
    private TestAppController testAppController;

    public static void main(String args[]) {
        Main m = (Main) FrameworkAppFactory.initialize("com.aaron");
        m.invoke();
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
        log.info("set TestAppController on Main: " + this.toString() + ", " + controller.toString());
    }

    private void invoke() {
        log.info("invoking invoke on Main: " + this.toString());
        testAppController.setNames("aaron", "cirillo");
        log.info("Name: " + testAppController.getFirstName() + " " + testAppController.getLastName());
    }

}
