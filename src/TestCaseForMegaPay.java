import org.testng.annotations.*;
import atu.testrecorder.ATUTestRecorder;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.android.AndroidKeyCode;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TestCaseForMegaPay {
    String CurrentTestMethodName = "Sanjar_Baike";
    private String reportDirectory = "reports";
    private String reportFormat = "html";
    private String testName = "MegaPay Test-Cases";
    protected AndroidDriver<AndroidElement> driver = null;
    DesiredCapabilities dc = new DesiredCapabilities();
    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
    Date date = new Date();

    //Подготовка среды к запуску
    @BeforeTest
    public void setting() throws Exception {
        dc.setCapability("reportDirectory", reportDirectory);
        dc.setCapability("reportFormat", reportFormat);
        dc.setCapability("testName", testName);
        dc.setCapability(MobileCapabilityType.UDID, "f4506817");
        System.out.println("Список тестов: Авторизация, оплата, справка из бюро находок, оформление карты оплаты");
        Reporter.log("Список тестов: Авторизация, оплата, справка из бюро находок, оформление карты оплаты");
        dc.setCapability(AndroidMobileCapabilityType.APP_PACKAGE, "com.kp.megapay.kg");
        dc.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY, ".MainActivity");
        driver = new AndroidDriver<>(new URL("http://localhost:4723/wd/hub"), dc);
        driver.resetApp();
        driver.closeApp();
        //Удаление сообщений
        try {
            if (driver.findElement(By.xpath("//*[@class='android.widget.FrameLayout' and ./*[@id='content']]")).isDisplayed()) {
                driver.findElement(By.xpath("//*[@id='rl_quick_msg_close']")).click();
            }
        } catch (Exception e) {
        }
        System.out.println("Подготовка среды к запуску...");


    }

    @BeforeMethod
    public void setUp(Method method) throws MalformedURLException, Exception {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        driver.startActivity(new Activity("com.kp.megapay.kg", ".MainActivity"));
        System.out.println("Дата запуска теста: " + dtf.format(now));
        Reporter.log("Дата запуска теста: " + dtf.format(now));
        CurrentTestMethodName = method.getName();
        ScreenRecorder.StartScreenRecording(CurrentTestMethodName);
    }

    @Test(description = "Авторизация в приложении", priority = 1)
    public void Login() throws Exception {
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

    //Справка из бюро находок
    @Test(description = "Запрос справки в бюро находок", priority = 2, dependsOnMethods = "Login")
    public void Lost_and_Found() throws Exception {
        System.out.println("Тест-кейс №2: запрос справки из бюро находок");
        new WebDriverWait(driver, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath("(//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*[@text and @class='android.view.View'])[1]")));
        driver.findElement(By.xpath("//*[@text='0']")).click();
        driver.findElement(By.xpath("//*[@text='0']")).click();
        driver.findElement(By.xpath("//*[@text='0']")).click();
        driver.findElement(By.xpath("//*[@text='0']")).click();
        new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='Гос. услуги']")));
        driver.findElement(By.xpath("//*[@text='Гос. услуги']")).click();
        new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='Наличие документа в бюро находок']")));
        Thread.sleep(5000);
        driver.findElement(By.xpath("//*[@text='Наличие документа в бюро находок']")).click();
        new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='android.widget.EditText']")));
        driver.findElement(By.xpath("//*[@class='android.widget.EditText']")).click();
        driver.getKeyboard().sendKeys("23333333333333");
        driver.findElement(By.xpath("//*[@text='ПРОДОЛЖИТЬ']")).click();
        System.out.println("Запрос справки из бюро находок");
        Reporter.log("Запрос справки из бюро находок...");
        new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='ЗАГРУЗИТЬ']")));
        new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='android.widget.ImageView' and ./parent::*[@class='android.view.View']]")));
        try {
            if (driver.findElement(By.xpath("//*[@class='android.widget.ImageView' and ./parent::*[@class='android.view.View']]")).isDisplayed()) {
                System.out.println("Справка получена");
                Reporter.log("Справка получена");
            }
        } catch (Exception e) {
            System.out.println("Справка не отобразилась");
            Reporter.log("Справка не отображена");
        }
        new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='Назад']")));
        driver.pressKeyCode(AndroidKeyCode.BACK);
        driver.pressKeyCode(AndroidKeyCode.BACK);
        driver.pressKeyCode(AndroidKeyCode.BACK);
        driver.pressKeyCode(AndroidKeyCode.BACK);
    }

    //Carcheck
    @Test(description = "CarCheck", priority = 3, dependsOnMethods = "Login")
    public void CarCheck() throws Exception {
        System.out.println("Тест-кейс №3: Carcheck");
        System.out.println("Запрос информации об автомобиле");
        new WebDriverWait(driver, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath("(//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*[@text and @class='android.view.View'])[1]")));
        driver.findElement(By.xpath("//*[@text='0']")).click();
        driver.findElement(By.xpath("//*[@text='0']")).click();
        driver.findElement(By.xpath("//*[@text='0']")).click();
        driver.findElement(By.xpath("//*[@text='0']")).click();
        new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='Гос. услуги']")));
        driver.findElement(By.xpath("//*[@text='Гос. услуги']")).click();
        new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='CarCheck']")));
        driver.findElement(By.xpath("//*[@text='CarCheck']")).click();
        new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='android.widget.EditText']")));
        driver.findElement(By.xpath("//*[@class='android.widget.EditText']")).click();
        driver.getKeyboard().sendKeys("B7747V");
        driver.findElement(By.xpath("//*[@text='ПРОДОЛЖИТЬ']")).click();
        Thread.sleep(15000);
        new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.xpath("(//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View'] and (./preceding-sibling::* | ./following-sibling::*)[@class='android.view.View']]]/*[@text and @class='android.view.View'])[1]")));
        try {
            if (driver.findElement(By.xpath("(//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View'] and (./preceding-sibling::* | ./following-sibling::*)[@class='android.view.View']]]/*[@text and @class='android.view.View'])[1]")).isDisplayed()) {
                System.out.println("Информация об автомобиле отображена корректно");
                Reporter.log("Информация об автомобиле отображена корректно");
                driver.findElement(By.xpath("//*[@text='Назад']")).click();
                driver.findElement(By.xpath("//*[@text='Назад']")).click();

            }

        } catch (Exception e) {
            System.out.println("Информация не отобразилась");
            Reporter.log("Информация не отобразилась");
            driver.findElement(By.xpath("//*[@text='Назад']")).click();
            driver.findElement(By.xpath("//*[@text='Назад']")).click();

        }
    }

    //Личный кабинет
    @Test(description = "Личный кабинет", priority = 4, dependsOnMethods = "Login")
    public void Private_Office() throws Exception {
        new WebDriverWait(driver, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath("(//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*[@text and @class='android.view.View'])[1]")));
        driver.findElement(By.xpath("//*[@text='0']")).click();
        driver.findElement(By.xpath("//*[@text='0']")).click();
        driver.findElement(By.xpath("//*[@text='0']")).click();
        driver.findElement(By.xpath("//*[@text='0']")).click();
        System.out.println("Тест-кейс №4:Личный кабинет");
        new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='Личный кабинет']")));
        driver.findElement(By.xpath("//*[@text='Личный кабинет']")).click();
        new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.xpath("(//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View'] and (./preceding-sibling::* | ./following-sibling::*)[@class='android.view.View']]]/*[@text and @class='android.widget.ImageView'])[1]")));
        String code7 = driver.findElement(By.xpath("(//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View'] and (./preceding-sibling::* | ./following-sibling::*)[@class='android.view.View']]]/*[@text and @class='android.widget.ImageView'])[1]")).getText();
        System.out.println("Информация о профиле абонента: " + code7);
        driver.findElement(By.xpath("//*[@class='android.widget.Button']")).click();
    }

    //Получение карты оплаты
    @Test(description = "Получение карты оплаты", priority = 5, enabled = false, dependsOnMethods = "Login")
    public void Payment_Card() throws Exception {
        System.out.println("Тест-кейс №5:Получение карты оплаты");
        new WebDriverWait(driver, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath("(//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*[@text and @class='android.view.View'])[1]")));
        driver.findElement(By.xpath("//*[@text='0']")).click();
        driver.findElement(By.xpath("//*[@text='0']")).click();
        driver.findElement(By.xpath("//*[@text='0']")).click();
        driver.findElement(By.xpath("//*[@text='0']")).click();
        new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.xpath("(//*[@class='android.view.View' and ./parent::*[@class='android.view.View']]/*[@text and @class='android.view.View'])[2]")));
        //driver.findElement(By.xpath("(//*[@class='android.view.View' and ./parent::*[@class='android.view.View']]/*[@text and @class='android.view.View'])[2]")).click();
        new TouchAction(driver).press(PointOption.point(446, 635)).waitAction(WaitOptions.waitOptions(Duration.ofMillis(181))).moveTo(PointOption.point(-217, 624)).release().perform();
        Thread.sleep(2000);
        new TouchAction(driver).press(PointOption.point(446, 635)).waitAction(WaitOptions.waitOptions(Duration.ofMillis(181))).moveTo(PointOption.point(-217, 624)).release().perform();
        new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.xpath("(//*[@class='android.view.View' and ./parent::*[@class='android.view.View']]/*[@text and @class='android.view.View'])[3]")));
        driver.findElement(By.xpath("(//*[@class='android.view.View' and ./parent::*[@class='android.view.View']]/*[@text and @class='android.view.View'])[3]")).click();
        new WebDriverWait(driver, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='Заказать карту']")));
        driver.findElement(By.xpath("//*[@text='Заказать карту']")).click();
        new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='ЗАКАЗАТЬ КАРТУ']")));
        driver.findElement(By.xpath("//*[@text='ЗАКАЗАТЬ КАРТУ']")).click();
        new WebDriverWait(driver, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='android.widget.CheckBox']")));
        driver.findElement(By.xpath("//*[@class='android.widget.CheckBox']")).click();
        new WebDriverWait(driver, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='ПРОДОЛЖИТЬ']")));
        driver.findElement(By.xpath("//*[@text='ПРОДОЛЖИТЬ']")).click();

        //ПЕРСОНАЛЬНАЯ ИНФОРМАЦИЯ
        new WebDriverWait(driver, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='Персональная информация']")));
        driver.findElement(By.xpath("//*[@text='Персональная информация']")).click();

        //Лицевая сторона паспорта IO
        new WebDriverWait(driver, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath("((//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*/*[@class='android.view.View' and ./parent::*[@class='android.view.View']])[2]/*/*[@text and @class='android.widget.ImageView'])[1]")));
        driver.findElement(By.xpath("((//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*/*[@class='android.view.View' and ./parent::*[@class='android.view.View']])[2]/*/*[@text and @class='android.widget.ImageView'])[1]")).click();

        //Разрешение доступа к камере
        new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='dialog_container']")));
        driver.findElement(By.xpath("//*[@text='РАЗРЕШИТЬ']")).click();

        //Разрешение доступа к аудио
        new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='dialog_container']")));
        driver.findElement(By.xpath("//*[@text='РАЗРЕШИТЬ']")).click();

        //Сфотографировать паспорт
        new WebDriverWait(driver, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='android.widget.Button' and (./preceding-sibling::* | ./following-sibling::*)[./*[@text='Назад']]]")));
        driver.findElement(By.xpath("//*[@class='android.widget.Button' and (./preceding-sibling::* | ./following-sibling::*)[./*[@text='Назад']]]")).click();

        // Нажать на галочку
        new WebDriverWait(driver, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='android.widget.ImageView' and ./parent::*[@class='android.view.View']]")));
        driver.findElement(By.xpath("(//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*[@class='android.widget.Button'])[2]")).click();

        //Раздел "обратная сторона паспорта ID"
        new WebDriverWait(driver, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath("((//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*/*[@class='android.view.View' and ./parent::*[@class='android.view.View']])[2]/*/*[@text and @class='android.widget.ImageView'])[2]")));
        driver.findElement(By.xpath("((//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*/*[@class='android.view.View' and ./parent::*[@class='android.view.View']])[2]/*/*[@text and @class='android.widget.ImageView'])[2]")).click();

        //Сфотографировать обратную сторону паспорта ID
        new WebDriverWait(driver, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='android.widget.Button' and (./preceding-sibling::* | ./following-sibling::*)[./*[@text='Назад']]]")));
        driver.findElement(By.xpath("//*[@class='android.widget.Button' and (./preceding-sibling::* | ./following-sibling::*)[./*[@text='Назад']]]")).click();

        // Нажать на галочку
        new WebDriverWait(driver, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='android.widget.ImageView' and ./parent::*[@class='android.view.View']]")));
        driver.findElement(By.xpath("(//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*[@class='android.widget.Button'])[2]")).click();

        //поле "Имя"
        new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.xpath("((//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*/*[@class='android.view.View' and ./parent::*[@class='android.view.View']])[2]/*[./*[@text='Осталось 64 символа']])[1]")));
        driver.findElement(By.xpath("((//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*/*[@class='android.view.View' and ./parent::*[@class='android.view.View']])[2]/*[./*[@text='Осталось 64 символа']])[1]")).click();
        driver.getKeyboard().sendKeys("Sanjar");
        driver.hideKeyboard();

        //поле "Фамилия"
        new WebDriverWait(driver, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath("((//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*/*[@class='android.view.View' and ./parent::*[@class='android.view.View']])[2]/*[@class='android.widget.EditText'])[2]")));
        driver.findElement(By.xpath("((//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*/*[@class='android.view.View' and ./parent::*[@class='android.view.View']])[2]/*[@class='android.widget.EditText'])[2]")).click();
        driver.getKeyboard().sendKeys("Baike");
        driver.hideKeyboard();

        //Отчество
        new WebDriverWait(driver, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath("((//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*/*[@class='android.view.View' and ./parent::*[@class='android.view.View']])[2]/*[@class='android.widget.EditText'])[3]")));
        driver.findElement(By.xpath("((//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*/*[@class='android.view.View' and ./parent::*[@class='android.view.View']])[2]/*[@class='android.widget.EditText'])[3]")).click();
        driver.getKeyboard().sendKeys("Мощь");
        driver.hideKeyboard();

        //ИНН
        driver.findElement(By.xpath("//*[@class='android.widget.EditText' and ./*[@text='Осталось 14 символов']]")).click();
        driver.getKeyboard().sendKeys("10000000000000");

        //Сохранить
        driver.findElement(By.xpath("//*[@text='СОХРАНИТЬ']")).click();

        //Контактная информация
        driver.findElement(By.xpath("//*[@text='Контактная информация']")).click();

        //Адрес прописки/Город или село
        driver.findElement(By.xpath("((//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*/*[@class='android.view.View' and ./parent::*[@class='android.view.View']])[2]/*[./*[@text='Осталось 64 символа']])[1]")).click();
        driver.getKeyboard().sendKeys("Санкт-Петербург");
        driver.hideKeyboard();

        //Улица
        driver.findElement(By.xpath("//*[@class='android.widget.EditText' and ./*[@text='Осталось 128 символов']]")).click();
        driver.getKeyboard().sendKeys("3-я улица Строителей");
        driver.hideKeyboard();

        //Дом, квартира
        driver.findElement(By.xpath("((//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*/*[@class='android.view.View' and ./parent::*[@class='android.view.View']])[2]/*[./*[@text='Осталось 64 символа']])[2]")).click();
        driver.getKeyboard().sendKeys("дом 25, квартира 12");
        driver.hideKeyboard();

        //Электронная почта
        driver.findElement(By.xpath("((//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*/*[@class='android.view.View' and ./parent::*[@class='android.view.View']])[2]/*[./*[@text='Осталось 64 символа']])[3]")).click();
        driver.getKeyboard().sendKeys("ИвановИванИванович@gmail.com");
        driver.hideKeyboard();

        //Сохранить данные
        driver.findElement(By.xpath("//*[@text='СОХРАНИТЬ']")).click();

        //Дополнительная информация
        //Кодовое слово
        driver.findElement(By.xpath("((//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*/*[@class='android.view.View' and ./parent::*[@class='android.view.View']])[2]/*[@class='android.widget.EditText'])[1]")).click();
        driver.getKeyboard().sendKeys("Просак");
        driver.hideKeyboard();

        //Место работы
        driver.findElement(By.xpath("((//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*/*[@class='android.view.View' and ./parent::*[@class='android.view.View']])[2]/*[@class='android.widget.EditText'])[2]")).click();
        driver.getKeyboard().sendKeys("ЗАО РОСКОСМОС");
        driver.hideKeyboard();

        //Должность
        driver.findElement(By.xpath("//*[@text='Должность*']")).click();
        driver.findElement(By.xpath("//*[@text='Руководитель/заместитель руководителя']")).click();
        driver.hideKeyboard();

        //Сохранить
        driver.findElement(By.xpath("//*[@text='СОХРАНИТЬ']")).click();

        //Место получения карты
        driver.findElement(By.xpath("//*[@text='Адрес']")).click();
        driver.findElement(By.xpath("//*[@text='г. Бишкек, Головной банк, г.Бишкек, ул. Московская, 118']")).click();

        //Снятие скриншота
        TakesScreenshot scrShot = ((TakesScreenshot) driver);
        File SrcFile = scrShot.getScreenshotAs(OutputType.FILE);
        File DestFile = new File("D:\\Users\\skozhobekov\\Desktop\\Eclipse Environment\\workspace\\errorScreenshots\\" + "-"
                + ".jpg");
        FileUtils.copyFile(SrcFile, DestFile);
    }

    //Произведение оплаты
    @Test(description = "Произведение оплаты", priority = 5, dependsOnMethods = "Login")
    public void PAYMENT() throws Exception {
        System.out.println("Тест-кейс №6:Произведение оплаты");
        new WebDriverWait(driver, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath("(//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*[@text and @class='android.view.View'])[1]")));
        driver.findElement(By.xpath("//*[@text='0']")).click();
        driver.findElement(By.xpath("//*[@text='0']")).click();
        driver.findElement(By.xpath("//*[@text='0']")).click();
        driver.findElement(By.xpath("//*[@text='0']")).click();
        new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='android.widget.FrameLayout' and ./*[@class='android.widget.LinearLayout' and ./*[@id='content']]]")));
        new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.xpath("(//*[@class='android.view.View' and ./parent::*[@class='android.view.View']]/*[@text and @class='android.view.View'])[2]")));
        String code6 = driver.findElement(By.xpath("(//*[@class='android.view.View' and ./parent::*[@class='android.view.View']]/*[@text and @class='android.view.View'])[2]")).getText();
        String code5 = code6.replaceAll("\\D+", "");
        int i = Integer.parseInt(code5);
        double c = i;
        c /= 100;
        System.out.println("Первоначальный баланс: " + c + "сом");
        Reporter.log("Первоначальный баланс: " + c + "сом");
        driver.findElement(By.xpath("//*[@text='Оплата услуг']")).click();
        new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='Сотовые операторы']")));
        driver.findElement(By.xpath("//*[@text='Сотовые операторы']")).click();
        new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='MegaCom']")));
        driver.findElement(By.xpath("//*[@text='MegaCom']")).click();
        new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.xpath("((//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*/*[@class='android.view.View' and ./parent::*[@class='android.view.View']])[2]/*[@class='android.widget.EditText'])[1]")));
        driver.findElement(By.xpath("((//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*/*[@class='android.view.View' and ./parent::*[@class='android.view.View']])[2]/*[@class='android.widget.EditText'])[1]")).click();
        driver.getKeyboard().sendKeys("990394512");
        driver.findElement(By.xpath("((//*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View' and ./parent::*[@class='android.view.View']]]]/*/*[@class='android.view.View' and ./parent::*[@class='android.view.View']])[2]/*[@class='android.widget.EditText'])[2]")).click();
        driver.getKeyboard().sendKeys("5");
        driver.hideKeyboard();
        driver.findElement(By.xpath("//*[@text='ОПЛАТИТЬ']")).click();
        new WebDriverWait(driver, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='ПОДТВЕРДИТЬ ПЛАТЕЖ']")));
        try {
            new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='android.widget.FrameLayout' and ./*[@class='android.widget.FrameLayout' and ./*[@id='content']]]")));
            if (driver.findElement(By.xpath("//*[@class='android.widget.FrameLayout' and ./*[@class='android.widget.FrameLayout' and ./*[@id='content']]]")).isDisplayed()) {
                String code11 = driver.findElement(By.xpath("//*[@id='messageTextView']")).getText();
                String code12 = code11.replaceAll("[^0-9]", "");
                driver.findElement(By.xpath("//*[@id='rl_quick_msg_close']")).click();
                new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='android.widget.EditText']")));
                driver.findElement(By.xpath("//*[@class='android.widget.EditText']")).click();
                Reporter.log("Авторизация...");
                driver.getKeyboard().sendKeys(code12);
                driver.findElement(By.xpath("//*[@text='ПОДТВЕРДИТЬ ПЛАТЕЖ']")).click();
                System.out.println("Парсинг SMS №2");
                Reporter.log("Парсинг SMS №2");
            }

            try {
                if (driver.findElement(By.xpath("//*[@text='Проблемы со списанием, просьба обратиться по короткому номеру *500']")).isDisplayed() |
                        driver.findElement(By.xpath("//*[@text='Недостаточно денежных средств']")).isDisplayed() |
                        driver.findElement(By.xpath("//*[@text='Баланса недостаточно для проведения платежа']")).isDisplayed()) {
                    System.out.println("Проблемы со списанием");
                    Reporter.log("Проблемы со списанием");
                }//else if (driver.findElement(By.xpath("//*[@text='Недостаточно денежных средств']")).isDisplayed()) {
                //System.out.println("Недостаточно денежных стредств на балансе");
                //Reporter.log("Недостаточно денежных средств на балансе");
                //else if (driver.findElement(By.xpath("//*[@text='Баланса недостаточно для проведения платежа']")).isDisplayed()) {
                //System.out.println("У Вас недостаточно средств для проведения оплаты");
                //Reporter.log("У Вас недостаточно средств для проведения оплаты");
            } catch (Exception e) {
                System.out.println("пополнение баланса...");
            }


            driver.findElement(By.xpath("//*[@text='ПРОДОЛЖИТЬ']")).click();
            new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.xpath("(//*[@class='android.view.View' and ./parent::*[@class='android.view.View']]/*[@text and @class='android.view.View'])[2]")));
            double balanceLeft = (c - 5);
            System.out.println("Остаток средств на балансе: " + balanceLeft + "сом");
            Reporter.log("Остаток средств на балансе: " + balanceLeft + "сом");

            driver.pressKeyCode(AndroidKeyCode.BACK);
            driver.pressKeyCode(AndroidKeyCode.BACK);
            driver.pressKeyCode(AndroidKeyCode.BACK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Завершение тестирования
    @AfterMethod
    public void tearDown(ITestResult result) throws IOException, InterruptedException {
        if (result.getStatus() == ITestResult.SUCCESS) {
            System.out.println("Тестирование завершено успешно");
        } else {
            System.out.println("Тестирование завершено неудачно");
        }
        driver.closeApp();
        ScreenRecorder.StopScreenRecording(CurrentTestMethodName, "D:\\Users\\skozhobekov\\IdeaProjects\\TestCaseForMegaPay\\VIDEO", true);
        driver.close();
    }

    @AfterTest
    public void aftertestSetting() throws Exception {
        //запись видео
    }

    @AfterClass
    public void Finishing() throws Exception {
        driver.closeApp();
        driver.close();
    }
}
    	 /*File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
   	String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
   	File screenShotName = new File("D:\\Users\\skozhobekov\\Desktop\\Eclipse Environment\\workspace\\Payment_LostAndFound\\test-output\\screenshots\\"+timeStamp+".png");
   	FileUtils.copyFile(scrFile, screenShotName);
   	String pathToScreen = "D:\\Users\\skozhobekov\\Desktop\\Eclipse Environment\\workspace\\Payment_LostAndFound\\test-output\\screenshots\\";
   	Reporter.log("<br> <png src=.test-output\\screenshots\\" + "/> <br>");
       Reporter.log("Тестирование завершено!");
	   driver.findElement(By.xpath("//*[@text='Настройки\n" +
            		"Вкладка 4 из 4']")).click();
            driver.findElement(By.xpath("//*[@text='Выход']")).click();
            driver.findElement(By.xpath("//*[@text='Выйти']")).click();
   	driver.openNotifications();
   	driver.findElement(By.xpath("//*[@id='btn_stop']")).click();
   	new WebDriverWait(driver, 12).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='iv_close']")));
   	driver.findElement(By.xpath("//*[@id='iv_close']")).click();
		driver.resetApp();
		System.out.println("Тестирование завершено!");
		*/


