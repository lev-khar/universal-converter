package ru.levkharitonov.kontur.intern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Converter {
    public static void main(String[] args){
        Graph conversions = new Graph();
        try {
            readConversions(conversions, args[0]);
            HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
            server.createContext("/convert", new ConvertHandler(conversions));
            server.setExecutor(null);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static class ConvertHandler implements HttpHandler {
        Graph conversions;
        public ConvertHandler(Graph conversions) {
            this.conversions = conversions;
        }

        @Override
        public void handle(HttpExchange he) throws IOException {
            InputStreamReader isr = new InputStreamReader(he.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder query = new StringBuilder();
            String from;
            String to;
            String line;
            String response = "";
            while ((line = br.readLine()) != null) {
                query.append(line);
            }
            try {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(query.toString());
                from = jsonObject.get("from").toString();
                to = jsonObject.get("to").toString();
                response = formatDouble(convert(from, to, conversions));
                he.sendResponseHeaders(200, response.length());
            } catch (IllegalArgumentException iae) {
                he.sendResponseHeaders(400, response.length());
            } catch (ArithmeticException ae) {
                he.sendResponseHeaders(404, response.length());
            } catch (ParseException e) {
                he.sendResponseHeaders(520, 0);
            } finally {
                OutputStream os = he.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    public static String formatDouble(Double d) {
        BigDecimal num = new BigDecimal(d);
        return num.round(new MathContext(15, RoundingMode.HALF_UP))
                .stripTrailingZeros().toPlainString();
    }

    public static void readConversions(Graph conversions, String path) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            String from = null;
            String to = null;
            Scanner scanner;
            int pos;
            double rate = 0;

            while ((line = reader.readLine()) != null) {
                scanner = new Scanner(line);
                scanner.useDelimiter(",");
                pos = 0;
                while (scanner.hasNext()) {
                    String data = scanner.next();
                    switch (pos) {
                    case 0:
                        from = data;
                        break;
                    case 1:
                        to = data;
                        break;
                    case 2:
                        rate = Double.parseDouble(data);
                        break;
                    default:
                        throw new IOException("Error parsing CSV");
                    }
                    pos++;
                }
                conversions.addRecord(from, to, rate);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Double convert(String from, String to, Graph conversions)
                throws ArithmeticException, IllegalArgumentException {
        Double rate;
        String[] fromDivided = from.split("/");
        String[] toDivided = to.split("/");

        if ((fromDivided.length > 2) || (toDivided.length > 2)) {
            throw new ArithmeticException();
        }

        List<String> fromNumUnits = mulSplit(fromDivided[0]);        //measure units in numerator
        List<String> fromDenUnits = new ArrayList<>(0);  //measure units in denominator
        List<String> toNumUnits = mulSplit(toDivided[0]);
        List<String> toDenUnits = new ArrayList<>(0);

        if (fromDivided.length > 1) {
           fromDenUnits = mulSplit(fromDivided[1]);
        }
        if (toDivided.length > 1) {
            toDenUnits = mulSplit(toDivided[1]);
        }

        rate = reduce(fromNumUnits, toNumUnits, conversions, true);
        rate /= reduce(fromDenUnits, toDenUnits, conversions, true);
        rate /= reduce(fromNumUnits, fromDenUnits, conversions, false);
        rate *= reduce(toNumUnits, toDenUnits, conversions, false);

        if (!fromNumUnits.isEmpty() || !fromDenUnits.isEmpty()
                || !toNumUnits.isEmpty() || !toDenUnits.isEmpty()) {
            throw new ArithmeticException();
        }
        return rate;
    }

    private static Double reduce(List<String> fromUnits, List<String> toUnits,
                Graph conversions, Boolean multiply) throws IllegalArgumentException{
        Double res = 1.;
        Double temp;    // variable to get and check conversion rate;
        Stack<String> fromDelete = new Stack<>();
        String toDelete = null;

        for (String fromUnit:fromUnits) {
            if (fromUnit.equals("1") || fromUnit.equals("")) {           /* special case */
                res *= 1.;
                fromDelete.push(fromUnit);
                continue;
            }
            for (String toUnit:toUnits) {
                if (toUnit.equals("1") || toUnit.equals("")) {           /* special case */
                    res *= 1.;
                    toDelete = toUnit;
                    continue;
                }
                temp = conversions.getRate(fromUnit, toUnit);
                if (temp != null) {
                    if(multiply) {
                        res *= temp;
                    }
                    else {
                        res /= temp;
                    }
                    toDelete = toUnit;
                    fromDelete.push(fromUnit);
                    break;
                }
            }
            if(toDelete != null) {
                toUnits.remove(toDelete);
            }
        }
        for(String fromDeleteElement: fromDelete) {
            fromUnits.remove(fromDeleteElement);
        }
        return res;
    }

    public static List<String> mulSplit(String str){
        return new ArrayList<>(Arrays.asList(str.replaceAll(" ", "").split("\\*")));
    }

}
