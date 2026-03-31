<<<<<<<< HEAD:deal/src/main/java/com/credithandler/deal/config/ErrorProperties.java
package com.credithandler.deal.config;
========
package com.credithandler.calculator.config;
>>>>>>>> dev:calculator/src/test/java/com/credithandler/calculator/config/ErrorPropertiesITest.java

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "error")
<<<<<<<< HEAD:deal/src/main/java/com/credithandler/deal/config/ErrorProperties.java
public class ErrorProperties {
========
public class ErrorPropertiesITest extends ErrorProperties {
>>>>>>>> dev:calculator/src/test/java/com/credithandler/calculator/config/ErrorPropertiesITest.java

    private Integer businessCode;
    private Integer validationCode;
    private Integer serverCode;
}