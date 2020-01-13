import atu.testrecorder.ATUTestRecorder;
import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.android.AndroidKeyCode;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DemoTest {
    String CurrentTestMethodName="Sanjar_Baike";
    private String reportDirectory = "reports";
    private String reportFormat = "html";
    private String testName = "MegaPay Test-Cases";
    protected AndroidDriver<AndroidElement> driver = null;
    DesiredCapabilities dc = new DesiredCapabilities();
    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
    Date date = new Date();
    ATUTestRecorder recorder;

    //Подготовка среды к запуску
    @BeforeTest
    public void setting() throws Exception {
        dc.setCapability("reportDirectory", reportDirectory);
        dc.setCapability("reportFormat", reportFormat);
        dc.setCapability("testName", testName);
        dc.setCapability(MobileCapabilityType.UDID, "f4506817");
        System.out.println("Тест-кейс: Авторизация, оплата, справка из бюро находок, оформление карты оплаты");
        Reporter.log("Тест-кейс: Авторизация, оплата, справка из бюро находок, оформление карты оплаты");
        dc.setCapability(AndroidMobileCapabilityType.APP_PACKAGE, "com.kp.megapay.kg");
        dc.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY, ".MainActivity");
        driver = new AndroidDriver<>(new URL("http://localhost:4723/wd/hub"), dc);
        driver.resetApp();
        driver.closeApp();
        //Удаление сообщений
        try {
            if(driver.findElement(By.xpath("//*[@class='android.widget.FrameLayout' and ./*[@id='content']]")).isDisplayed()) {
                driver.findElement(By.xpath("//*[@id='rl_quick_msg_close']")).click();
            }
        }
        catch (Exception e) {
        }
        System.out.println("Подготовка среды к запуску...");


    }

    @BeforeMethod
    public void setUp(Method method) throws MalformedURLException, Exception {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Дата запуска теста: " + dtf.format(now));
        Reporter.log("Дата запуска теста: " + dtf.format(now));
        CurrentTestMethodName=method.getName();
        ScreenRecorder.StartScreenRecording(CurrentTestMethodName);
    }

    @Test(description = "Авторизация в приложении", priority = 1)
    public void Authorization() throws Exception {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        //Запуск
        driver.startActivity(new Activity("com.kp.megapay.kg", ".MainActivity"));
        Reporter.log("Время запуска: " + dtf.format(now));
        new WebDriverWait(driver, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='android.widget.EditText']")));
        driver.findElement(By.xpath("//*[@class='android.widget.EditText']")).click();
        driver.getKeyboard().sendKeys("990394512");
        driver.hideKeyboard();
        driver.findElement(By.xpath("//*[@text='ВОЙТИ']")).click();
        try {
            if (driver.findElement(By.xpath("//*[@text='Подключение к интернет отсутствует']")).isDisplayed()) {
                System.out.println("Проверьте соединение с интернетом");
                Reporter.log("Проблемы с доступом к интернету");
            }
        } catch (Exception e) {
            System.out.println("Соединение с интернетом стабильное");
            Reporter.log("Ожидание смс-подтверждения");
        }

        // Парсинг смс
        try {
            new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='android.widget.FrameLayout' and ./*[@class='android.widget.FrameLayout' and ./*[@id='content']]]")));
            if (driver.findElement(By.xpath("//*[@class='android.widget.FrameLayout' and ./*[@class='android.widget.FrameLayout' and ./*[@id='content']]]")).isDisplayed()) {
                String code1 = driver.findElement(By.xpath("//*[@id='messageTextView']")).getText();
                String code2 = code1.replaceAll("[^0-9]", "");
                driver.findElement(By.xpath("//*[@id='rl_quick_msg_close']")).click();
                Reporter.log("Авторизация...");
                driver.findElement(By.xpath("//*[@class='android.widget.EditText']")).click();
                driver.getKeyboard().sendKeys(code2);
                System.out.println("Парсинг SMS");
                driver.findElement(By.xpath("//*[@text='ПРОДОЛЖИТЬ']")).click();
            }
        } catch (Exception e) {
            Thread.sleep(7000);
            driver.openNotifications();
            new WebDriverWait(driver, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath("(//*[@id='notification_children_container']/*/*/*/*[@id='notification_text' and ./parent::*[@class='android.widget.LinearLayout']])[1]")));
            String code1 = driver.findElement(By.xpath("(//*[@id='notification_children_container']/*/*/*/*[@id='notification_text' and ./parent::*[@class='android.widget.LinearLayout']])[1]")).getText();
            String code2 = code1.replaceAll("[^0-9]", "");
            driver.pressKeyCode(AndroidKeyCode.BACK);
            driver.findElement(By.xpath("//*[@class='android.widget.EditText']")).click();
            Reporter.log("Авторизация...");
            driver.getKeyboard().sendKeys(code2);
            System.out.println("Запасной метод парсинга SMS");
            driver.findElement(By.xpath("//*[@text='ПРОДОЛЖИТЬ']")).click();
            try {
                if (driver.findElement(By.xpath("//*[@text='Неверный код активации']")).isDisplayed()) {
                    System.out.println("Вы ввели НЕправильный код активации");
                    Reporter.log("Введён неверный код активации");
                }
            } catch (Exception ex) {
                System.out.println("Код подтверждения введён корректно");
                Reporter.log("Авторизация проведена успешно");
            }
        }
        new WebDriverWait(driver, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='Придумайте ваш код доступа']")));
        try {
            if (driver.findElement(By.xpath("//*[@text='Придумайте ваш код доступа']")).isDisplayed()) {
                System.out.println("Авторизация проведена успешно");
            }
        } catch (Exception e) {
            System.out.println("Авторизация провалилась");
        }
        Reporter.log("Генерация кода доступа");
        driver.findElement(By.xpath("//*[@text='0']")).click();
        driver.findElement(By.xpath("//*[@text='0']")).click();
        driver.findElement(By.xpath("//*[@text='0']")).click();
        driver.findElement(By.xpath("//*[@text='0']")).click();
        Reporter.log("Подтверждение кода доступа");
        driver.findElement(By.xpath("//*[@text='0']")).click();
        driver.findElement(By.xpath("//*[@text='0']")).click();
        driver.findElement(By.xpath("//*[@text='0']")).click();
        driver.findElement(By.xpath("//*[@text='0']")).click();
        Reporter.log("Код доступа задан(по умолчанию 0000)");
        new WebDriverWait(driver, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='НЕТ, СПАСИБО']")));
        driver.findElement(By.xpath("//*[@text='НЕТ, СПАСИБО']")).click();
        new WebDriverWait(driver, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='СОГЛАСЕН']")));
        driver.findElement(By.xpath("//*[@text='СОГЛАСЕН']")).click();
        new WebDriverWait(driver, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='Пропустить']")));
        driver.findElement(By.xpath("//*[@text='Пропустить']")).click();
    }

    @AfterMethod
    public void tearDown(ITestResult result) throws IOException, InterruptedException {
        if (result.getStatus() == ITestResult.SUCCESS) {
            System.out.println("Тестирование завершено успешно");
        } else {
            System.out.println("Тестирование завершено неудачно");
        }
        ScreenRecorder.StopScreenRecording(CurrentTestMethodName, "D:\\Users\\skozhobekov\\IdeaProjects\\TestCaseForMegaPay\\VIDEO", true);
        driver.close();
    }
}
