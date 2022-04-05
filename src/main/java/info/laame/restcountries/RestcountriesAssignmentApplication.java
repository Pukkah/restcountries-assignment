package info.laame.restcountries;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SpringBootApplication
@RestController
public class RestcountriesAssignmentApplication {
    private static List<Country> countries;

    public static void main(String[] args) throws IOException {
        String jsonData = getRestCountriesJsonData();
        countries = parseRestCountriesJsonData(jsonData);
        SpringApplication.run(RestcountriesAssignmentApplication.class, args);
    }

    @GetMapping
    public List<Country> getAllCountries() {
        return countries;
    }

    @GetMapping(path = "top10/population")
    public List<Country> getTop10ByPopulation() {
        return countries.stream()
                        .sorted(Comparator.comparing(Country::getPopulation).reversed())
                        .limit(10L)
                        .toList();
    }

    @GetMapping(path = "top10/area")
    public List<Country> getTop10ByArea() {
        return countries.stream()
                        .filter(country -> country.getArea() != 0)
                        .sorted(Comparator.comparing(Country::getArea).reversed())
                        .limit(10L)
                        .toList();
    }

    @GetMapping(path = "top10/density")
    public List<Country> getTop10ByDensity() {
        return countries.stream()
                        .filter(country -> country.getDensity() != 0.0)
                        .sorted(Comparator.comparing(Country::getDensity).reversed())
                        .limit(10L)
                        .toList();
    }

    private static String getLocalRestCountriesJsonData() throws IOException {
        Path fileName = Path.of(RestcountriesAssignmentApplication.class.getResource("eu-countries.json").getPath());
        return Files.readString(fileName);
    }

    private static String getRestCountriesJsonData() {
        try {
            URL url = new URL("https://restcountries.com/v2/regionalbloc/eu?fields=name,capital,currencies,population,area");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else {
                StringBuilder builder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    builder.append(inputLine);
                }
                return builder.toString();
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static List<Country> parseRestCountriesJsonData(String jsonData) {
        List<Country> countries = new ArrayList<>();
        JSONArray jsonArr = new JSONArray(jsonData);
        for (Object countryObj : jsonArr) {
            JSONObject countryJson = (JSONObject) countryObj;
            Country country = new Country(countryJson.getString("name"));
            country.setCapital(countryJson.getString("capital"));
            country.setPopulation(countryJson.getInt("population"));
            if (countryJson.has("area")) {
                country.setArea(countryJson.getDouble("area"));
            }
            for (Object currencyObj : countryJson.getJSONArray("currencies")) {
                JSONObject currencyJson = (JSONObject) currencyObj;
                String code = currencyJson.getString("code");
                String name = currencyJson.getString("name");
                String symbol = currencyJson.getString("symbol");
                Currency currency = new Currency(code, name, symbol);
                country.addCurrency(currency);
            }
            countries.add(country);
        }
        return countries;
    }

}
