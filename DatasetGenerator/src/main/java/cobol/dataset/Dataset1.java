package cobol.dataset;

import cobol.dataset.domain.entity.Food;
import cobol.dataset.domain.entity.Stand;

import java.util.*;

public class Dataset1 {

    public static List<Stand> getDataset1() {
        List<Stand> stands = new ArrayList<>();
        stands.addAll(getBallsAndGlory(3));
        stands.addAll(getMcDonalds(4));
        stands.addAll(getBurgerKing(2));
        stands.addAll(getPizzaHut(3));
        return stands;
    }




    private static List<Location> getRandomLocations(int amount){
        Random random = new Random();
        List<Location> locations = new ArrayList<>();
        double latStart = 51.031652;
        double lonStart = 3.782850;
        double size = 0.006;
        for (int i = 0; i < amount; i++) {
            locations.add(new Location(
                    latStart+size* random.nextDouble(),
                    lonStart+size*random.nextDouble()
            ));
        }
        return locations;
    }

    private static Location getRandomLocation(){
        return getRandomLocations(1).get(0);
    }

    private static List<Stand> getBallsAndGlory(int amount){
        List<Stand> stands = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            stands.add(Stand.builder()
                    .name("Balls & Glory " + i + 1)
                    .brandName("Balls & Glory")
                    .latitude(getRandomLocation().getLat())
                    .longitude(getRandomLocation().getLon())
                    .food(Arrays.asList(
                            Food.builder().name("Classic Pork").description("A ball of delicious pork").preparationTime(300000).price(3.33f).stock(25).category(Collections.singletonList("SNACK")).build(),
                            Food.builder().name("Delicious Veggie").description("Veggie ball").preparationTime(300000).price(3.33f).stock(25).category(Collections.singletonList("VEGGIE")).build(),
                            Food.builder().name("Original Stoemp").description("Stoemp like grandma used to make").preparationTime(1200000).price(5f).stock(25).category(Collections.singletonList("PREPARATION")).build(),
                            Food.builder().name("Tasty Salad").description("Healthy green salad").preparationTime(1200000).price(5f).stock(25).category(Collections.singletonList("SNACK")).build(),
                            Food.builder().name("Sweet Cake").description("Delicious cake").preparationTime(300000).price(5f).stock(25).category(Collections.singletonList("VEGGIE")).build()
                    ))
                    .build());
        }
        return stands;
    }

    private static List<Stand> getMcDonalds(int amount){
        List<Stand> stands = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            stands.add(Stand.builder()
                    .name("McDonalds " + i + 1)
                    .brandName("McDonalds")
                    .latitude(getRandomLocation().getLat())
                    .longitude(getRandomLocation().getLon())
                    .food(Arrays.asList(
                            Food.builder().name("Big Mac").description("The classic big mac!").preparationTime(600000).price(5.99f).stock(25).category(Arrays.asList("SNACK", "BURGER")).build(),
                            Food.builder().name("Cheeseburger").description("A delicious cheeseburger.").preparationTime(600000).price(3.33f).stock(25).category(Arrays.asList("SNACK", "BURGER")).build(),
                            Food.builder().name("Maestro Generous Jack").description("Big burger with 3 patties.").preparationTime(600000).price(6.98f).stock(25).category(Arrays.asList("SNACK", "BURGER")).build(),
                            Food.builder().name("CBO burger").description("Chicken bacon onion").preparationTime(600000).price(5.5f).stock(25).category(Arrays.asList("SNACK", "BURGER")).build(),
                            Food.builder().name("Large fries").description("Large fries").preparationTime(300000).price(3f).stock(25).category(Arrays.asList("SNACK", "FRIES")).build(),
                            Food.builder().name("Medium fries").description("Medium fries").preparationTime(300000).price(2.5f).stock(25).category(Arrays.asList("SNACK", "FRIES")).build(),
                            Food.builder().name("Small fries").description("Small fries").preparationTime(300000).price(2f).stock(25).category(Arrays.asList("SNACK", "FRIES")).build()

                    ))
                    .build());
        }
        return stands;
    }

    private static List<Stand> getBurgerKing(int amount){
        List<Stand> stands = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            stands.add(Stand.builder()
                    .name("BurgerKing " + i + 1)
                    .brandName("BurgerKing")
                    .latitude(getRandomLocation().getLat())
                    .longitude(getRandomLocation().getLon())
                    .food(Arrays.asList(
                            Food.builder().name("Pepper King").description("Delicious patty with peppered cheese").preparationTime(600000).price(5.99f).stock(25).category(Arrays.asList("SNACK", "BURGER")).build(),
                            Food.builder().name("Chicken Pepper King").description("Pepper king with chicken").preparationTime(600000).price(3.33f).stock(25).category(Arrays.asList("SNACK", "BURGER")).build(),
                            Food.builder().name("Rebel Whopper").description("Veggie burger").preparationTime(600000).price(6.98f).stock(25).category(Arrays.asList("SNACK", "BURGER", "VEGGIE")).build(),
                            Food.builder().name("Ultimate Chicken Bacon King").description("The final evolution of the the Chicken Pepper King burger!").preparationTime(600000).price(5.5f).stock(25).category(Arrays.asList("SNACK", "BURGER")).build(),
                            Food.builder().name("Large fries").description("Large fries").preparationTime(300000).price(3f).stock(25).category(Arrays.asList("SNACK", "FRIES")).build(),
                            Food.builder().name("Medium fries").description("Medium fries").preparationTime(300000).price(2.5f).stock(25).category(Arrays.asList("SNACK", "FRIES")).build(),
                            Food.builder().name("Small fries").description("Small fries").preparationTime(300000).price(2f).stock(25).category(Arrays.asList("SNACK", "FRIES")).build()

                    ))
                    .build());
        }
        return stands;
    }

    private static List<Stand> getPizzaHut(int amount){
        List<Stand> stands = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            stands.add(Stand.builder()
                    .name("PizzaHut " + i + 1)
                    .brandName("PizzaHut")
                    .latitude(getRandomLocation().getLat())
                    .longitude(getRandomLocation().getLon())
                    .food(Arrays.asList(
                            Food.builder().name("PEPPER").description("double portion of beef, diced tomatoes, red onion, pepper sauce, tomato sauce and mozzarella").preparationTime(1200000).price(10.5f).stock(25).category(Arrays.asList("SNACK", "PIZZA")).build(),
                            Food.builder().name("Margharita").description("the original, oven-fresh basic pizza with tomato sauce and mozzarella.").preparationTime(1200000).price(7.7f).stock(25).category(Arrays.asList("SNACK", "PIZZA")).build(),
                            Food.builder().name("Cheesam").description("with extra mozzarella, thin strips of ham and tomato sauce").preparationTime(1200000).price(8.8f).stock(25).category(Arrays.asList("SNACK", "PIZZA")).build(),
                            Food.builder().name("Forestiere").description("strips of ham, mushrooms, tomato cubes, tomato sauce and mozzarella").preparationTime(1200000).price(9.9f).stock(25).category(Arrays.asList("SNACK", "PIZZA")).build(),
                            Food.builder().name("Hot n spicy").description("spicy beef, chili peppers, red onion, diced tomatoes, tomato sauce and mozzarella").preparationTime(1200000).price(10.5f).stock(25).category(Arrays.asList("SNACK", "PIZZA")).build(),
                            Food.builder().name("Barbecue chicken").description("pieces of grilled chicken, mushrooms, red onion, green pepper, mozzarella and barbecue sauce").preparationTime(1200000).price(10.5f).stock(25).category(Arrays.asList("SNACK", "PIZZA")).build(),
                            Food.builder().name("alcace").description("smoked bacon cubes, red onion, mushrooms, mild garlic sauce, tomato sauce and mozzarella").preparationTime(1200000).price(10.5f).stock(25).category(Arrays.asList("SNACK", "PIZZA")).build()

                    ))
                    .build());
        }
        return stands;
    }

    private static class Location{
        private double lon;
        private double lat;

        public Location( double lat, double lon) {
            this.lon = lon;
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public double getLat() {
            return lat;
        }
    }

}
