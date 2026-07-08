package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.ExcelTestCaseReader;
import utils.TestCase;

import java.util.List;

public class TestCatalogTest extends BaseTest {

    @Test(groups = "catalog")
    public void verifyExcelTestCaseCatalogLoads() {
        List<TestCase> positiveCases = ExcelTestCaseReader.readPositiveCases();
        List<TestCase> negativeCases = ExcelTestCaseReader.readNegativeCases();

        Assert.assertTrue(positiveCases.size() >= 300, "Expected at least 300 positive cases but found " + positiveCases.size());
        Assert.assertTrue(negativeCases.size() >= 100, "Expected at least 100 negative cases but found " + negativeCases.size());

        Assert.assertTrue(
                ExcelTestCaseReader.findByTcId("Positive", "TC01").isPresent(),
                "TC01 should exist in the positive Excel catalog");
        Assert.assertTrue(
                ExcelTestCaseReader.findByTcId("Negative", "TC03").isPresent(),
                "TC03 should exist in the negative Excel catalog");

        System.out.println("Loaded positive cases: " + positiveCases.size());
        System.out.println("Loaded negative cases: " + negativeCases.size());
        System.out.println("Sample positive case: " + positiveCases.get(0));
        System.out.println("Sample negative case: " + negativeCases.get(0));
    }
}
