import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class City extends Circle {
    private double posX;
    private double posY;
    private String name;

    private boolean isSelected = false;


    public City(String name, double x, double y, double radius){
        super(x,y, radius, Color.BLUE);
        this.posX = x;
        this.posY = y;
        this.name = name;
        this.setFill(Color.BLUE);
    }

    public double getPosX(){
        return this.posX;
    }

    public double getPosY(){
        return this.posY;
    }

    public String getName(){
        return this.name;
    }

    public boolean isSelected(){
        return isSelected;
    }

    public void setSelected(){
        this.isSelected = true;
        this.setFill(Color.RED);
    }

    public void setUnselected(){
        this.isSelected = false;
        this.setFill(Color.BLUE);
    }


    @Override
    public String toString(){
        return getName();
    }
}
