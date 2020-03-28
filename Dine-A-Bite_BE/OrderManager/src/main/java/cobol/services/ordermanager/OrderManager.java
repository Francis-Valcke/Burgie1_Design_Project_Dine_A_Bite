package cobol.services.ordermanager;
import  cobol.services.ordermanager.dbmenu.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Het maken van lokale database als je deze functionaliteit lokaal wilt testen:
 *
 * CREATE DATABASE `menu`;
 *
 * en dan in dit schema:
 *
 *
 * CREATE TABLE `food_category` (
 *   `category_number` int NOT NULL AUTO_INCREMENT,
 *   `food_id` int NOT NULL,
 *   `category` varchar(50) DEFAULT NULL,
 *   PRIMARY KEY (`category_number`,`food_id`),
 *   KEY `id_idx` (`food_id`),
 *   CONSTRAINT `id` FOREIGN KEY (`food_id`) REFERENCES `food_price` (`id`)
 * ) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=latin1;
 *
 * CREATE TABLE `food_price` (
 *   `id` int NOT NULL AUTO_INCREMENT,
 *   `preptime` int DEFAULT NULL,
 *   `price` float DEFAULT NULL,
 *   `name` varchar(50) NOT NULL,
 *   `description` varchar(500) DEFAULT NULL,
 *   PRIMARY KEY (`id`,`name`)
 * ) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=latin1;
 *
 * CREATE TABLE `stand` (
 *   `id` int NOT NULL AUTO_INCREMENT,
 *   `full_name` varchar(50) NOT NULL,
 *   `location_lat` decimal(10,8) NOT NULL,
 *   `location_lon` decimal(11,8) NOT NULL,
 *   `manager_code` int DEFAULT NULL,
 *   PRIMARY KEY (`id`)
 * ) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=latin1;
 *
 * CREATE TABLE `stock` (
 *   `stand_id` int NOT NULL,
 *   `food_id` int NOT NULL,
 *   `stock_count` int DEFAULT NULL,
 *   `count` int NOT NULL,
 *   PRIMARY KEY (`stand_id`,`food_id`),
 *   KEY `stock_food_id_idx` (`food_id`),
 *   CONSTRAINT `stock_food_id` FOREIGN KEY (`food_id`) REFERENCES `food_price` (`id`),
 *   CONSTRAINT `Stock_ibfk_1` FOREIGN KEY (`stand_id`) REFERENCES `stand` (`id`)
 * ) ENGINE=InnoDB DEFAULT CHARSET=latin1;
 */
@SpringBootApplication
public class OrderManager {

    public static boolean test= true;

    public static String ACURL;
    public static String SMURL;
    public static String OMURL;
    public static String ECURL;

    public static void main(String[] args) {
        ACURL= test ? "http://localhost:8080" : "http://cobol.idlab.ugent.be:8090";
        OMURL= test ? "http://localhost:8081" : "http://cobol.idlab.ugent.be:8091";
        SMURL= test ? "http://localhost:8082" : "http://cobol.idlab.ugent.be:8092";
        ECURL= test ? "http://localhost:8083" : "http://cobol.idlab.ugent.be:8093";

        SpringApplication.run(OrderManager.class, args);

    }

}