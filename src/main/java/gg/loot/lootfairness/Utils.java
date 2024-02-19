package gg.loot.lootfairness;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class Utils {

    private Utils() {
        // Utility class
    }


    public static String generateSeed(String publicSeed, String privateSeed, String clientSeed) {
        return publicSeed + "-" + privateSeed + "-" + clientSeed;
    }

    public static SecureRandom getSecureRandom(String publicSeed, String privateSeed, String clientSeed) {
        try {
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(generateSeed(publicSeed, privateSeed, clientSeed).getBytes(StandardCharsets.UTF_8));
            return secureRandom;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static GamePriceView.ItemDetail.Item generateItemWon(SecureRandom secureRandom, List<GamePriceView.ItemDetail> boxItems) {
        boxItems.sort(Comparator.comparing(o -> o.getItem().getPrice()));

        List<Pair<Double, GamePriceView.ItemDetail.Item>> itemsRanges = new ArrayList<>();
        double currentMax = 0;
        for (GamePriceView.ItemDetail boxItem : boxItems) {
            currentMax += boxItem.getFrequency();
            itemsRanges.add(Pair.of(currentMax, boxItem.getItem()));
        }

        double random = secureRandom.nextDouble() * 100;

        return itemsRanges.stream()
                .filter(pair -> random < pair.getLeft())
                .findFirst()
                .map(Pair::getRight)
                .orElseThrow();
    }

    private static final String BOX_API_URL = "https://api.loot.gg/v1/api/boxes/public/";

    public static GamePriceView.Box getBoxById(String id) {
        RestTemplate restTemplate = new RestTemplate();

        ClientHttpRequestInterceptor interceptor = (request, body, execution) -> {
            request.getHeaders().set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
            request.getHeaders().set("Content-Type", "application/json");
            request.getHeaders().set("Host", "api.loot.gg");
            return execution.execute(request, body);
        };
        restTemplate.setRequestFactory(new InterceptingClientHttpRequestFactory(new SimpleClientHttpRequestFactory(), Collections.singletonList(interceptor)));

        ResponseEntity<GamePriceView.Box> responseEntity;
        responseEntity = restTemplate.getForEntity(URI.create(BOX_API_URL + id), GamePriceView.Box.class);
        return responseEntity.getBody();
    }
}
