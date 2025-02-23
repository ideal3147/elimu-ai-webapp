package selenium.analytics;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import lombok.extern.slf4j.Slf4j;
import selenium.util.DomainHelper;

@Slf4j
public class VideoLearningEventsPageTest {
    
    private WebDriver driver;

    @BeforeEach
    public void setUp() { 
        log.info("setUp");

        ChromeOptions chromeOptions = new ChromeOptions();

        // Read "headless" property set on the command line: 
        //    mvn clean verify -P regression-test-ui -D headless=true
        String headlessSystemProperty = System.getProperty("headless");
        log.info("headlessSystemProperty: \"" + headlessSystemProperty + "\"");
        if ("true".equals(headlessSystemProperty)) {
            chromeOptions.addArguments("headless");
        }
        
        driver = new ChromeDriver(chromeOptions);

        driver.get(DomainHelper.getBaseUrl() + "/analytics");
    }

    @AfterEach
    public void tearDown() {
        log.info("tearDown");

        driver.quit();
    }

    @Disabled("Disabled until the event-simulator bug has been fixed")
    @Test
    public void testVideoLearningEventsPage() {
        log.info("testVideoLearningEventsPage");

        MainAnalyticsPage mainAnalyticsPage = new MainAnalyticsPage(driver);
        mainAnalyticsPage.pressVideoLearningEventsLink();
        
        VideoLearningEventsPage videoLearningEventsPage = new VideoLearningEventsPage(driver);
    }
}
