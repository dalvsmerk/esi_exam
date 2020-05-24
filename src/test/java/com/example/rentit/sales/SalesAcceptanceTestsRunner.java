package com.example.rentit.sales;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin={"pretty","html:target/cucumber"},
        features="classpath:features/sales",
        glue="com.example.demo.sales")
public class SalesAcceptanceTestsRunner {
}