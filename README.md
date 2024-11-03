# Currency Exchange and Discount Calculation Application

## Overview
This Spring Boot app connects to a currency exchange API to get real-time exchange rates. Then it calculates how much you need to pay for a bill in a different currency after applying any discounts. You can submit a bill in one currency and get the amount due in another.

## Features
- Connects to a currency exchange API (like Open Exchange Rates)
- Calculates discounts based on user type and tenure
- Converts total amounts to different currencies
- Uses Basic Authentication for API access
- Uml Class diagram is inside resources/diagrams/
- Access jococo report this url: http://localhost:8080/site/jacoco/index.html

## Requirements
- JDK 21 
- Maven 3.9.2
- IDE (IntelliJ IDEA)


## Build and Run
**To build the project:**

```bash
mvn clean install
mvn spring-boot:run

mvn test jacoco:report
mvn sonar:sonar

## To access the calculate API, use Basic Authentication. Here’s a  curl command to authenticate and request the /api/discount/calculate endpoint:


curl --location --request POST 'http://localhost:8080/api/discount/calculate' \
--header 'Content-Type: application/json' \
--header 'Cookie: rwerwe' \
--header 'Authorization: Basic YWRtaW46YWRtaW4=' \
--data-raw '{
  "items": [
    {
      "name": "item1",
      "category": "electronics",
      "price": 100.0
    },
    {
      "name": "item2",
      "category": "groceries",
      "price": 50.0
    }
  ],
  "totalAmount": 150.0,
  "originalCurrency": "AED",
  "targetCurrency": "USD",
  "user": {
    "userType": "EMPLOYEE",
    "tenure": 3
  }
}'
