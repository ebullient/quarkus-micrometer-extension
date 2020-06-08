package dev.ebullient.it.micrometer.mpmetrics;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

@Path("/prime")
public class PrimeResource {
    private long highestPrimeSoFar = 2;

    @GET
    @Path("/{number}")
    @Produces("text/plain")
    public String checkIfPrime(@PathParam long number) {
        String result = checkPrime(number);
        if (result.length() > 0) {
            return result;
        }
        if (number > highestPrimeSoFar) {
            highestPrimeSoFar = number;
        }
        return number + " is prime.";
    }

    @Counted(name = "performedChecks", description = "How many primality checks have been performed.")
    @Timed(name = "checksTimer", description = "A measure how long it takes to perform the primality test.", unit = MetricUnits.MILLISECONDS)
    private String checkPrime(long number) {
        if (number < 1) {
            return "Only natural numbers can be prime numbers.";
        }
        if (number == 1) {
            return "1 is not prime.";
        }
        if (number == 2) {
            return "2 is prime.";
        }
        if (number % 2 == 0) {
            return number + " is not prime, it is divisible by 2.";
        }
        for (int i = 3; i < Math.floor(Math.sqrt(number)) + 1; i = i + 2) {
            if (number % i == 0) {
                return number + " is not prime, is divisible by " + i + ".";
            }
        }
        return "";
    }

    @Gauge(name = "highestPrimeNumberSoFar", unit = MetricUnits.NONE, description = "Highest prime number so far.")
    public Long highestPrimeNumberSoFar() {
        return highestPrimeSoFar;
    }
}
