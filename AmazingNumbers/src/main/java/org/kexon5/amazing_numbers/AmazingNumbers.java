package org.kexon5.amazing_numbers;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AmazingNumbers {
    private final Scanner sc = new Scanner(System.in);
    private final Map<String, Function<Long, Boolean>> propertiesMap = new HashMap<>(); //remove support BigInteger for speed up app
    private final List<Consumer<List<String>>> strategies = new ArrayList<>();
    private final List<List<String>> mutuallyExclusivePropertiesList = new ArrayList<>();

    private boolean signalToStop = false;

    AmazingNumbers() {
        strategies.add(this::runRequestWithOneParameter);
        strategies.add(this::runRequestWithTwoAndMoreParameters);

        mutuallyExclusivePropertiesList.add(new ArrayList<>(List.of("even", "odd")));
        mutuallyExclusivePropertiesList.add(new ArrayList<>(List.of("duck", "spy")));
        mutuallyExclusivePropertiesList.add(new ArrayList<>(List.of("sunny", "square")));
        mutuallyExclusivePropertiesList.add(new ArrayList<>(List.of("happy", "sad")));
        mutuallyExclusivePropertiesList.add(new ArrayList<>(List.of("-even", "-odd")));
        mutuallyExclusivePropertiesList.add(new ArrayList<>(List.of("-duck", "-spy")));
        mutuallyExclusivePropertiesList.add(new ArrayList<>(List.of("-happy", "-sad")));

        propertiesMap.put("even", x -> x % 2 == 0);
        propertiesMap.put("odd", x -> x % 2 == 1);
        propertiesMap.put("buzz", x -> x % 10 == 7 || x % 7 == 0);
        propertiesMap.put("duck", x -> String.valueOf(x).contains("0"));
        propertiesMap.put("palindromic", x -> String.valueOf(x).contains(new StringBuilder(String.valueOf(x)).reverse()));
        propertiesMap.put("gapful", x -> {
            String str = String.valueOf(x);
            String div = str.charAt(0) + str.substring(str.length() - 1);
            return x % Long.parseLong(div) == 0 && str.length() >= 3;
        });
        propertiesMap.put("spy", x-> {
           List<Long> digits = Arrays.stream(String.valueOf(x).split("")).map(Long::valueOf).collect(Collectors.toList());
           long sum = 0;
           long product = 1;
           for (long digit: digits) {
               sum += digit;
               product *= digit;
           }
           return sum == product;
        });
        propertiesMap.put("square", x -> {
            BigInteger[] sqrtAndRemainder = new BigInteger(String.valueOf(x)).sqrtAndRemainder();
            return sqrtAndRemainder[1].compareTo(BigInteger.ZERO) == 0;
        });
        propertiesMap.put("sunny", x -> propertiesMap.get("square").apply(x + 1));
        propertiesMap.put("jumping", x -> {
            char[] digits = String.valueOf(x).toCharArray();
            for (int i = 1; i < digits.length; i++) {
                if (Math.abs(digits[i - 1] - digits[i]) != 1)
                    return false;
            }
            return true;
        });
        propertiesMap.put("happy", x -> {
            long n = x;
            long sum;
            while (n / 10 != 0) {
                List<Long> digits = Arrays.stream(String.valueOf(n).split("")).map(Long::valueOf).collect(Collectors.toList());
                sum = 0;
                for (long digit: digits) {
                    sum += digit * digit;
                }
                n = sum;
            }
            return n == 1 || n == 7;
        });
        propertiesMap.put("sad", x -> !propertiesMap.get("happy").apply(x));
        Map<String, Function<Long, Boolean>> propertiesMapReverse = new HashMap<>();
        for (Map.Entry<String, Function<Long, Boolean>> properties: propertiesMap.entrySet()) {
            propertiesMapReverse.put("-" + properties.getKey(), x -> !properties.getValue().apply(x));
        }
        propertiesMap.putAll(propertiesMapReverse);
    }

    public void start() {
        greetings();
        requestsInfo();
        startMainCycle();
        ends();
    }

    private void greetings() {
        System.out.println("Welcome to Amazing Numbers!\n");
    }

    private void ends() {
        System.out.println("\nGoodbye!");
    }

    private void requestsInfo() {
        System.out.println("Supported requests:\n" +
                "- enter a natural number to know its properties;\n" +
                "- enter two natural numbers to obtain the properties of the list:\n" +
                "  * the first parameter represents a starting number;\n" +
                "  * the second parameter shows how many consecutive numbers are to be printed;\n" +
                "- two natural numbers and properties to search for;\n" +
                "- a property preceded by minus must not be present in numbers;\n" +
                "- separate the parameters with one space;\n" +
                "- enter 0 to exit.\n");
    }

    private void startMainCycle() {
        while (!signalToStop) {
            System.out.print("Enter a request: ");
            List<String> stringList = List.of(sc.nextLine().split(" "));
            if (!isCorrectRequest(stringList))
                continue;
            strategies.get(stringList.size() > 1 ? 1 : 0).accept(stringList);
        }
    }

    private List<String> isMutuallyExclusiveProperties(List<String> properties) {
        Map<String, Integer> countProperties = new HashMap<>();
        for (String nameProperty: propertiesMap.keySet()) {
            countProperties.put(nameProperty, 0);
        }
        properties.forEach(x -> countProperties.put(x.toLowerCase(Locale.ROOT), countProperties.get(x.toLowerCase(Locale.ROOT)) + 1));
        for (Map.Entry<String, Integer> property: countProperties.entrySet()) {
            if (property.getKey().charAt(0) != '-') {
                if (property.getValue() > 0 && countProperties.get("-" + property.getKey()) > 0) {
                    return new ArrayList<>(List.of(property.getKey(), "-" + property.getKey()));
                }
            }
        }
        for (List<String> mutuallyExclusiveProperties: mutuallyExclusivePropertiesList) {
            if (countProperties.get(mutuallyExclusiveProperties.get(0)) > 0 &&
                    countProperties.get(mutuallyExclusiveProperties.get(1)) > 0) {
                return mutuallyExclusiveProperties;
            }
        }
        return null;
    }

    private boolean isCorrectProperty(String propertyName) {
        return propertiesMap.containsKey(propertyName.toLowerCase()) || propertiesMap.containsKey(propertyName.toLowerCase().substring(1));
    }

    private boolean isCorrectRequest(List<String> stringList) {
        if (stringList.size() == 1) {
            if (!stringList.get(0).matches("\\d+")) {
                System.out.println("\nThe first parameter should be a natural number or zero.\n");
                return false;
            }
            return true;
        } else if (stringList.size() == 2) {
            if (isCorrectRequest(stringList.subList(0, 1)) && !stringList.get(1).matches("\\d+")) {
                System.out.println("\nThe second parameter should be a natural number.\n");
                return false;
            }
            return true;
        } else if (stringList.size() >= 3)  {
            List<String> checkProperties = new ArrayList<>(stringList.subList(2, stringList.size()));
            List<String> incorrectProperties = new ArrayList<>();
            if (!isCorrectRequest(stringList.subList(0, 2)))
                return false;
            for (String property: checkProperties) {
                if (!isCorrectProperty(property))
                    incorrectProperties.add(property);
            }
            if (incorrectProperties.size() > 0) {
                System.out.printf("\nThe propert%s %s %s wrong.\n", incorrectProperties.size() >= 2 ? "ies" : "y", incorrectProperties.toString().toUpperCase(Locale.ROOT), incorrectProperties.size() >= 2 ? "are" : "is");
                System.out.printf("Available properties: %s%n%n", propertiesMap.keySet().toString().toUpperCase(Locale.ROOT));
                return false;
            }
            List<String> mutuallyExclusiveProperties = isMutuallyExclusiveProperties(checkProperties);
            if (mutuallyExclusiveProperties != null) {
                System.out.printf("\nThe request contains mutually exclusive properties: %s\n", mutuallyExclusiveProperties.toString().toUpperCase(Locale.ROOT));
                System.out.println("There are no numbers with these properties.\n");
                return false;
            }
            return true;
        }
        requestsInfo();
        return false;
    }


    private void runRequestWithOneParameter(List<String> stringList) {
        long n = Long.parseLong(stringList.get(0));
        if (n == 0) {
            signalToStop = true;
            return;
        }
        System.out.println("\nProperties of " + n);
        propertiesMap.forEach((key, func) -> {
            if (key.charAt(0) != '-')
                System.out.printf("%s: %b\n", key, func.apply(n));
        });
        System.out.println();
    }

    private boolean checkNeededNumberProperties(List<String> propertiesList, long n) {
        for (String property: propertiesList) {
            if (!propertiesMap.get(property.toLowerCase(Locale.ROOT)).apply(n)) {
                return true;
            }
        }
        return false;
    }

    private void runRequestWithTwoAndMoreParameters(List<String> stringList) {
        long n = Long.parseLong(stringList.get(0));
        long times = Long.parseLong(stringList.get(1));
        boolean moreParametersFlag = stringList.size() >= 3;

        System.out.println();
        for (long i = 0; i < times; n++) {
            StringBuilder output = new StringBuilder(n + " is ");
            StringBuilder propertyString = new StringBuilder();

            if (checkNeededNumberProperties(stringList.subList(2, stringList.size()), n))
                continue;

            for (Map.Entry<String, Function<Long, Boolean>> entry: propertiesMap.entrySet()) {
                if (entry.getValue().apply(n)) {
                    if (entry.getKey().charAt(0) != '-')
                        output.append(entry.getKey()).append(", ");
                    propertyString.append(entry.getKey(), 0, 3); // this string shows all conditions for which number matches - needs to distinguish mirror and original properties
                }
            }
            output.delete(output.length() - 2, output.length());
            boolean isAllProperties = true;
            for (String property: stringList.subList(2, stringList.size())) {
                isAllProperties &= propertyString.toString().contains(property.substring(0, 3).toLowerCase(Locale.ROOT));
            }
            if (!moreParametersFlag || isAllProperties) {
                i++;
                System.out.println(output);
            }
        }
        System.out.println();
    }
}
