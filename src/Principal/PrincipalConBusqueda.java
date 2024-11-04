package Principal;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.json.JSONObject;

public class PrincipalConBusqueda {

    private static final String API_KEY = "4282ebb417a5f8afa2595855"; // Replace with your actual API key
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/";

    public static void main(String[] args) throws IOException, InterruptedException {

        Scanner lectura = new Scanner(System.in);
        HttpClient client = HttpClient.newHttpClient();

        // Map to store conversion rates fetched from the API
        Map<String, Double> conversionRates = new HashMap<>();

        int opcion = 0;

        System.out.println("*****************************************");

        String menu = """
                1) Dolar (USD) ==> Peso Colombiano (COP)
                2) Dolar (USD)  ==> Bolivares (VEF)
                3) Dolar (USD)  ==> Euro (EUR)
                4) Euro (EUR) ==> Dolar (USD)
                5) Bolivares (VEF) ==> Dolar (USD)
                6) Peso Colombiano (COP) ==> Dolar (USD)
                7) Salir
                """;

        while (opcion != 7) {
            System.out.println(menu);
            opcion = lectura.nextInt();

            String fromCurrency = "";
            String toCurrency = "";

            switch (opcion) {
                case 1 -> {
                    fromCurrency = "USD";
                    toCurrency = "COP";
                }
                case 2 -> {
                    fromCurrency = "USD";
                    toCurrency = "VES";
                }
                case 3 -> {
                    fromCurrency = "USD";
                    toCurrency = "EUR";
                }
                case 4 -> {
                    fromCurrency = "EUR";
                    toCurrency = "USD";
                }
                case 5 -> {
                    fromCurrency = "VES";
                    toCurrency = "USD";
                }
                case 6 -> {
                    fromCurrency = "COP";
                    toCurrency = "USD";
                }
                case 7 -> System.out.println("¡Saliendo!");
                default -> System.out.println("Opción no válida.");
            }

            if (opcion >= 1 && opcion <= 6) {
                // Get conversion rate from the API if not already cached
                String key = fromCurrency + toCurrency;

                if (!conversionRates.containsKey(key)) {
                    Double conversionRate = fetchConversionRate(client, fromCurrency, toCurrency);
                    if (conversionRate != null) {
                        conversionRates.put(key, conversionRate);
                    } else {
                        System.out.println("No se pudo obtener la tasa de conversión.");
                        continue;
                    }
                }

                // Get amount to convert
                System.out.print("Ingrese la cantidad a convertir: ");
                double amount = lectura.nextDouble();

                // Perform conversion and display result
                double convertedAmount = amount * conversionRates.get(key);
                System.out.printf("%.2f %s equivale a %.2f %s\n", amount, fromCurrency, convertedAmount, toCurrency);
            }
        }
        lectura.close();
    }

    private static Double fetchConversionRate(HttpClient client, String fromCurrency, String toCurrency) {
        String url = BASE_URL + API_KEY + "/latest/" + fromCurrency;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONObject jsonObject = new JSONObject(response.body());
                JSONObject rates = jsonObject.getJSONObject("conversion_rates");

                // Check if the target currency exists in the rates
                if (rates.has(toCurrency)) {
                    return rates.getDouble(toCurrency);
                } else {
                    System.out.println("No se encontró una tasa de conversión para " + fromCurrency + " a " + toCurrency);
                }
            } else {
                System.out.println("Error en la solicitud de API: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error al obtener la tasa de conversión: " + e.getMessage());
        }
        return null;
    }

    private static Map<String, Object> parseJsonObject(String json) {
        Map<String, Object> result = new HashMap<>();
        JSONObject jsonObject = new JSONObject(json);

        // Convertir el objeto JSON de "conversion_rates" a un Map
        JSONObject rates = jsonObject.getJSONObject("conversion_rates");
        Map<String, Double> conversionRates = new HashMap<>();
        for (String key : rates.keySet()) {
            conversionRates.put(key, rates.getDouble(key));
        }

        result.put("conversion_rates", conversionRates);
        return result;
    }
}

