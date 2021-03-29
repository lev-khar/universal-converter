package ru.levkharitonov.kontur.intern;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Converter {
    public static void main(String[] args){
        Graph conversions = new Graph();
        readConversions(conversions, "src/test/test.csv");

        System.out.println("км" + " мм " +  convert("км", "мм", conversions));

        System.out.println("\nчас*час " + " мин*мин " + convert("час * час","мин * мин",conversions));
        System.out.println("мин *мин*мин " + " с*с*с " + convert("мин * мин*мин","с * с*с",conversions));
        System.out.println("м" + " км * с / час " +  convert("м","км * с / час", conversions));

        System.out.println("\n м/с " + " км/час " + convert("м/с","км/час",conversions));
        System.out.println("км/м" + " " +  convert("км / м","",conversions));
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

    public static Double convert(String from, String to, Graph conversions) {
        Double rate;
        String[] fromDivided = from.split("/");
        String[] toDivided = to.split("/");
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
                                 Graph conversions, Boolean multiply) {
        Double res = 1.;
        Double temp;    // variable to get and check conversion rate;
        Stack<String> fromDelete = new Stack<>();

        for (String fromUnit:fromUnits) {
            String toDelete = null;
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
