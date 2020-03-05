package cobol.services.recommender;

public class Food {
    private String type;
    private int preptime;

    public Food(String type, int preptime) {
        this.type = type;
        this.preptime = preptime;
    }

    public int getTime() {
        return preptime;
    }

    public String getType(){
        return type;
    }
}
