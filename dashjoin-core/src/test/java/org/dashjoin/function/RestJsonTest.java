package org.dashjoin.function;

import static com.google.common.collect.ImmutableMap.of;

public class RestJsonTest {

  public static void main(String[] args) throws Exception {

    RestJson http;

    http = new RestJson();
    http.url = "https://api.deepai.org/api/sentiment-analysis";
    http.headers = of("api-key", "quickstart-QUdJIGlzIGNvbWluZy4uLi4K");
    http.contentType = "application/x-www-form-urlencoded";
    http.method = "POST";
    Object api = http.run(of("text", "not sure what to make of this"));
    System.out.println(api.getClass());
    System.out.println(api);

    http = new RestJson();
    http.url = "https://api.weather.gov/openapi.json";
    api = http.run(null);
    System.out.println(api.getClass());
    System.out.println(api);

    http = new RestJson();
    http.url = "https://demo.dashjoin.org/rest/manage/getDrivers";
    http.username = "admin";
    http.password = "dj";
    Object drivers = http.run(null);
    System.out.println(drivers.getClass());
    System.out.println(drivers);

    http = new RestJson();
    http.url = "https://demo.dashjoin.org/rest/database/all/config/dj-role";
    http.contentType = "application/json";
    http.method = "POST";
    http.username = "admin";
    http.password = "dj";
    Object query = http.run(of("ID", "admin"));
    System.out.println(query.getClass());
    System.out.println(query);
  }
}
