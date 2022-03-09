package tests;

import environment.EnvironmentManager;
import environment.RunEnvironment;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class DemoTest extends TestReporter{
    @Before
    public void startBrowser() {
        EnvironmentManager.initWebDriver();
    }

    public void fillUserLogin(WebDriver driver, String userName, String pass){
        //Navigate to the Paylocity login page
        driver.get("https://wmxrwq14uc.execute-api.us-east-1.amazonaws.com/Prod/Account/Login");
        //Add an explicit wait of 30 seconds until element is visible
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("Username")));
        //Find and assign the element to a variable
        WebElement username=driver.findElement(By.id("Username"));
        //Add an explicit wait of 30 seconds until element is visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("Password")));
        //Find and assign the element to a variable
        WebElement password=driver.findElement(By.id("Password"));
        //Introduce input in user and password
        username.sendKeys(userName);
        password.sendKeys(pass);
    }

    public void addEmployee(WebDriver driver, String name, String lastName, String dependants){
        WebDriverWait wait = new WebDriverWait(driver, 30);
        WebElement addRecordBtn = driver.findElement(By.id("add"));
        addRecordBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName")));
        WebElement firstNameInput = driver.findElement(By.id("firstName"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("lastName")));
        WebElement lastNameInput = driver.findElement(By.id("lastName"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dependants")));
        WebElement dependantsInput = driver.findElement(By.id("dependants"));
        WebElement addEmployeeBtn = driver.findElement(By.id("addEmployee"));
        firstNameInput.sendKeys(name);
        lastNameInput.sendKeys(lastName);
        dependantsInput.sendKeys(dependants);
        addEmployeeBtn.click();
    }

    public String getEmployeeFromTable(WebDriver driver, String firstName, String lastName, String dependants){
        WebDriverWait wait = new WebDriverWait(driver, 30);
        String xPath = "//tr[td[position()=2 and text()=\"" + firstName + "\"] and td[position()=3 and text()=\""
                + lastName + "\"] and td[position()=4 and text()=\"" + dependants + "\"]]";
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("employeesTable")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
        WebElement employeeRow = driver.findElement(By.xpath(xPath));
        String id = employeeRow.findElement(By.xpath("td[position()=1]")).getText();
        String salary = employeeRow.findElement(By.xpath("td[position()=5]")).getText();
        String grossPay = employeeRow.findElement(By.xpath("td[position()=6]")).getText();
        String benefitsCost = employeeRow.findElement(By.xpath("td[position()=7]")).getText();
        String netPay = employeeRow.findElement(By.xpath("td[position()=8]")).getText();
        return id + "," + salary + "," + grossPay + "," + benefitsCost + "," + netPay;
    }

    public String getBenefitCalculations(String dependants, String grossPay){
        //Yearly cost of benefits is 1000 per individual, 500 per dependant.
        //There are 26 paychecks in one year.
        int grossPaycheckValue = Integer.parseInt(grossPay);
        int yearlyCostOfDependants = 500 * Integer.parseInt(dependants);
        double perPayCostOfBenefits = ((1000.00 + yearlyCostOfDependants)/26);
        DecimalFormat twoPlaces = new DecimalFormat("#.##");
        twoPlaces.setRoundingMode(RoundingMode.HALF_UP);
        double perPayCostOfBenefitsResult = Double.parseDouble(twoPlaces.format(perPayCostOfBenefits));
        double netPay = grossPaycheckValue - perPayCostOfBenefitsResult;
        return perPayCostOfBenefitsResult + "," + twoPlaces.format(netPay);
    }

    @Test
    public void NavigateToDashboardOnValidLogin() {
        //Open the browser
        WebDriver driver = RunEnvironment.getWebDriver();
        //Fill in user inputs
        fillUserLogin(driver, "TestUser173", "];(uOeaW8U]I");
        //Find the login button and assign to variable
        WebElement login=driver.findElement(By.className("btn-primary"));
        //Submit user and password
        login.click();
        //Verify the dashboard page is displayed
        String dashboardTitle = driver.getTitle();
        Assert.assertEquals("Employees - Paylocity Benefits Dashboard", dashboardTitle);
    }

    @Test
    public void ShowNotificationOnInvalidLogin() {
        //Open the browser
        WebDriver driver = RunEnvironment.getWebDriver();
        //Introduce input in user and password
        fillUserLogin(driver, "TestUser173", "XXXXXXX");
        //Find the login button and assign to variable
        WebElement login=driver.findElement(By.className("btn-primary"));
        //Submit user and password
        login.click();
        //Verify error notification is displayed
        String xPath = "//div[@class='validation-summary-errors text-danger']//li";
        String expectedError = "The specified username or password is incorrect.";
        WebElement errorNotification = driver.findElement(By.xpath(xPath));
        String errorString = errorNotification.getText();
        Assert.assertEquals(expectedError, errorString);
    }

    @Test
    public void VerifyAddedEmployee() {
        //Adding explicit wait -- (Crashing every other run with what seemed to me session overlapping)
        try {Thread.sleep(3000);}catch (Exception e){System.out.println(e.getMessage());}
        //Open the browser
        WebDriver driver = RunEnvironment.getWebDriver();
        //Fill in user inputs
        fillUserLogin(driver, "TestUser173", "];(uOeaW8U]I");
        //Find the login button and assign to variable
        WebElement login=driver.findElement(By.className("btn-primary"));
        //Submit user and password
        login.click();
        //Add employee
        addEmployee(driver,"Jolly", "Rancher", "2");
        //Refresh page
        driver.navigate().refresh();
        String employee = getEmployeeFromTable(driver, "John", "Smith", "2");
        System.out.println(employee);
        String expectedCalculationsString = getBenefitCalculations("2", "2000");
        String expectedBenefitCost = expectedCalculationsString.split(",")[0];
        String expectedNetPay = expectedCalculationsString.split(",")[1];
        String actualBenefitCost = employee.split(",")[3];
        String actualNetPay = employee.split(",")[4];
        //Assertions to verify employee data
        Assert.assertFalse("Employee does not exist in table.",employee.isEmpty());
        Assert.assertEquals("Benefit cost does not match.", expectedBenefitCost, actualBenefitCost);
        Assert.assertEquals("Net pay does not match. ", expectedNetPay, actualNetPay);
    }

    @After
    public void tearDown() {
       EnvironmentManager.shutDownDriver();
    }
}
