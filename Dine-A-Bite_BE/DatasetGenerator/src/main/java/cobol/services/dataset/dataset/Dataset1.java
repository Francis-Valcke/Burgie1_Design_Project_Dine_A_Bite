package cobol.services.dataset.dataset;

import cobol.services.dataset.domain.entity.*;
import cobol.services.dataset.domain.repository.BrandRepository;
import cobol.services.dataset.domain.repository.CategoryRepository;
import cobol.services.dataset.domain.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Dataset1 {

    //@Autowired
    //BrandRepository brandRepository;
    //
    //@Autowired
    //StockRepository stockRepository;
    //
    //@Autowired
    //CategoryRepository categoryRepository;
    //
    //private static Category SNACK = new Category("SNACK");
    //private static Category VEGGIE = new Category("VEGGIE");
    //private static Category PIZZA = new Category("PIZZA");
    //private static Category BURGER = new Category("BURGER");
    //private static Category FRIES = new Category("FRIES");
    //private static Category PREPARATION = new Category("PREPARATION");
    //
    //private static final double LAT_START = 51.031652;
    //private static final double LON_START = 3.782850;
    //private static final double SIZE = 0.006;
    //
    //private static final Random random = new Random();
    //
    //public void load() {
    //
    //    //Create and persist food categories
    //    categoryRepository.saveAll(Arrays.asList(SNACK, VEGGIE, PIZZA, BURGER, FRIES, PREPARATION));
    //
    //    //Create brands
    //    List<Brand> brands = Arrays.asList(
    //            getBallsAndGlory(),
    //            getBurgerKing(),
    //            getMcDonalds(),
    //            getPizzaHut()
    //    );
    //    //Link food and stands to brand from the other side
    //    for (Brand brand : brands) {
    //
    //        for (Food food : brand.getFood()) {
    //            food.setBrand(brand);
    //        }
    //
    //        for (Stand stand : brand.getStand()) {
    //            stand.setBrand(brand);
    //        }
    //
    //        brandRepository.saveAndFlush(brand);
    //    }
    //
    //    //Get brands back from the db with updated ID's
    //    brands = brandRepository.findAll();
    //
    //    //Create stock with those ID's
    //    for (Brand brand : brands) {
    //        for (Stand stand : brand.getStand()) {
    //            for (Food food : brand.getFood()) {
    //                Stock stock = new Stock(stand.getId(), food.getId(), 25);
    //                stockRepository.saveAndFlush(stock);
    //            }
    //        }
    //    }
    //
    //}
    //
    //public void clear() {
    //    brandRepository.deleteAll();
    //    stockRepository.deleteAll();
    //    categoryRepository.deleteAll();
    //}
    //
    //private double getRandomLatitude(){
    //    return LAT_START + SIZE * random.nextDouble();
    //}
    //
    //private double getRandomLongitude(){
    //    return  LON_START + SIZE * random.nextDouble();
    //}
    //
    //
    //private Brand getBallsAndGlory(){
    //
    //    return Brand.builder()
    //            .name("Balls & Glory")
    //            .stand(
    //                   Arrays.asList(
    //                           Stand.builder().name("Balls & Glory " + 1).latitude(getRandomLatitude()).longitude(getRandomLongitude()).build(),
    //                           Stand.builder().name("Balls & Glory " + 2).latitude(getRandomLatitude()).longitude(getRandomLongitude()).build(),
    //                           Stand.builder().name("Balls & Glory " + 3).latitude(getRandomLatitude()).longitude(getRandomLongitude()).build()
    //                   )
    //            )
    //            .food(
    //                    Arrays.asList(
    //                            Food.builder().name("Classic Pork").description("A ball of delicious pork").preparationTime(300).price(3.33f).stock(25).category(Collections.singletonList(SNACK)).build(),
    //                            Food.builder().name("Delicious Veggie").description("Veggie ball").preparationTime(300).price(3.33f).stock(25).category(Collections.singletonList(VEGGIE)).build(),
    //                            Food.builder().name("Original Stoemp").description("Stoemp like grandma used to make").preparationTime(1200).price(5f).stock(25).category(Collections.singletonList(PREPARATION)).build(),
    //                            Food.builder().name("Tasty Salad").description("Healthy green salad").preparationTime(1200).price(5f).stock(25).category(Collections.singletonList(SNACK)).build(),
    //                            Food.builder().name("Sweet Cake").description("Delicious cake").preparationTime(300).price(5f).stock(25).category(Collections.singletonList(VEGGIE)).build()
    //                    )
    //            )
    //            .build();
    //}
    //
    //private Brand getMcDonalds(){
    //
    //    return Brand.builder()
    //            .name("McDonalds")
    //            .stand(
    //                    Arrays.asList(
    //                            Stand.builder().name("McDonalds " + 1).latitude(getRandomLatitude()).longitude(getRandomLongitude()).build(),
    //                            Stand.builder().name("McDonalds " + 2).latitude(getRandomLatitude()).longitude(getRandomLongitude()).build(),
    //                            Stand.builder().name("McDonalds " + 3).latitude(getRandomLatitude()).longitude(getRandomLongitude()).build(),
    //                            Stand.builder().name("McDonalds " + 4).latitude(getRandomLatitude()).longitude(getRandomLongitude()).build()
    //                    )
    //            )
    //            .food(
    //                    Arrays.asList(
    //                            Food.builder().name("Big Mac").description("The classic big mac!").preparationTime(600).price(5.99f).stock(25).category(Arrays.asList(SNACK, BURGER)).build(),
    //                            Food.builder().name("Cheeseburger").description("A delicious cheeseburger.").preparationTime(600).price(3.33f).stock(25).category(Arrays.asList(SNACK, BURGER)).build(),
    //                            Food.builder().name("Maestro Generous Jack").description("Big burger with 3 patties.").preparationTime(600).price(6.98f).stock(25).category(Arrays.asList(SNACK, BURGER)).build(),
    //                            Food.builder().name("CBO burger").description("Chicken bacon onion").preparationTime(600).price(5.5f).stock(25).category(Arrays.asList(SNACK, BURGER)).build(),
    //                            Food.builder().name("Large fries").description("Large fries").preparationTime(300).price(3f).stock(25).category(Arrays.asList(SNACK, FRIES)).build(),
    //                            Food.builder().name("Medium fries").description("Medium fries").preparationTime(300).price(2.5f).stock(25).category(Arrays.asList(SNACK, FRIES)).build(),
    //                            Food.builder().name("Small fries").description("Small fries").preparationTime(300).price(2f).stock(25).category(Arrays.asList(SNACK, FRIES)).build()
    //                    )
    //            )
    //            .build();
    //
    //}
    //
    //private Brand getBurgerKing(){
    //
    //    return Brand.builder()
    //            .name("BurgerKing")
    //            .stand(
    //                    Arrays.asList(
    //                            Stand.builder().name("BurgerKing " + 1).latitude(getRandomLatitude()).longitude(getRandomLongitude()).build(),
    //                            Stand.builder().name("BurgerKing " + 2).latitude(getRandomLatitude()).longitude(getRandomLongitude()).build(),
    //                            Stand.builder().name("BurgerKing " + 3).latitude(getRandomLatitude()).longitude(getRandomLongitude()).build(),
    //                            Stand.builder().name("BurgerKing " + 4).latitude(getRandomLatitude()).longitude(getRandomLongitude()).build()
    //                    )
    //            )
    //            .food(
    //                    Arrays.asList(
    //                            Food.builder().name("Pepper King").description("Delicious patty with peppered cheese").preparationTime(600).price(5.99f).stock(25).category(Arrays.asList(SNACK, BURGER)).build(),
    //                            Food.builder().name("Chicken Pepper King").description("Pepper king with chicken").preparationTime(600).price(3.33f).stock(25).category(Arrays.asList(SNACK, BURGER)).build(),
    //                            Food.builder().name("Rebel Whopper").description("Veggie burger").preparationTime(600).price(6.98f).stock(25).category(Arrays.asList(SNACK, BURGER, VEGGIE)).build(),
    //                            Food.builder().name("Ultimate Chicken Bacon King").description("The final evolution of the the Chicken Pepper King burger!").preparationTime(600).price(5.5f).stock(25).category(Arrays.asList(SNACK, BURGER)).build(),
    //                            Food.builder().name("Large fries").description("Large fries").preparationTime(300).price(3f).stock(25).category(Arrays.asList(SNACK, FRIES)).build(),
    //                            Food.builder().name("Medium fries").description("Medium fries").preparationTime(300).price(2.5f).stock(25).category(Arrays.asList(SNACK, FRIES)).build(),
    //                            Food.builder().name("Small fries").description("Small fries").preparationTime(300).price(2f).stock(25).category(Arrays.asList(SNACK, FRIES)).build()
    //                    )
    //            )
    //            .build();
    //
    //}
    //
    //private Brand getPizzaHut(){
    //
    //    return Brand.builder()
    //            .name("PizzaHut")
    //            .stand(
    //                    Arrays.asList(
    //                            Stand.builder().name("PizzaHut " + 1).latitude(getRandomLatitude()).longitude(getRandomLongitude()).build(),
    //                            Stand.builder().name("PizzaHut " + 2).latitude(getRandomLatitude()).longitude(getRandomLongitude()).build(),
    //                            Stand.builder().name("PizzaHut " + 3).latitude(getRandomLatitude()).longitude(getRandomLongitude()).build(),
    //                            Stand.builder().name("PizzaHut " + 4).latitude(getRandomLatitude()).longitude(getRandomLongitude()).build()
    //                    )
    //            )
    //            .food(
    //                    Arrays.asList(
    //                            Food.builder().name("PEPPER").description("double portion of beef, diced tomatoes, red onion, pepper sauce, tomato sauce and mozzarella").preparationTime(1200).price(10.5f).stock(25).category(Arrays.asList(SNACK, PIZZA)).build(),
    //                            Food.builder().name("Margharita").description("the original, oven-fresh basic pizza with tomato sauce and mozzarella.").preparationTime(1200).price(7.7f).stock(25).category(Arrays.asList(SNACK, PIZZA)).build(),
    //                            Food.builder().name("Cheesam").description("with extra mozzarella, thin strips of ham and tomato sauce").preparationTime(1200).price(8.8f).stock(25).category(Arrays.asList(SNACK, PIZZA)).build(),
    //                            Food.builder().name("Forestiere").description("strips of ham, mushrooms, tomato cubes, tomato sauce and mozzarella").preparationTime(1200).price(9.9f).stock(25).category(Arrays.asList(SNACK, PIZZA)).build(),
    //                            Food.builder().name("Hot n spicy").description("spicy beef, chili peppers, red onion, diced tomatoes, tomato sauce and mozzarella").preparationTime(1200).price(10.5f).stock(25).category(Arrays.asList(SNACK, PIZZA)).build(),
    //                            Food.builder().name("Barbecue chicken").description("pieces of grilled chicken, mushrooms, red onion, green pepper, mozzarella and barbecue sauce").preparationTime(1200).price(10.5f).stock(25).category(Arrays.asList(SNACK, PIZZA)).build(),
    //                            Food.builder().name("alcace").description("smoked bacon cubes, red onion, mushrooms, mild garlic sauce, tomato sauce and mozzarella").preparationTime(1200).price(10.5f).stock(25).category(Arrays.asList(SNACK, PIZZA)).build()
    //                    )
    //            )
    //            .build();
    //
    //}

}
