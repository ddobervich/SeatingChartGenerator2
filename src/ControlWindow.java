import processing.core.PApplet;
import processing.core.PVector;

import java.util.HashMap;

public class ControlWindow extends PApplet {
    private static final float LEFT_MARGIN = 10;
    private static final float MARGIN = 10;

    public static HashMap<String, ToggleSwitch> switches = new HashMap<>();

    public ControlWindow() {
        super();
        PApplet.runSketch(new String[] {this.getClass().getSimpleName()}, this);
    }

    public void settings() {
        size(400, 600);
    }

    public void setup() {
        switches.put("group numbers", new ToggleSwitch(new PVector(LEFT_MARGIN, 4*MARGIN)).setText("display group (n)umbers"));
        switches.put("mirror", new ToggleSwitch(new PVector(LEFT_MARGIN, 4*MARGIN + ToggleSwitch.h*2)).setText("mirror for (p)rinting"));
        switches.put("display conflicts", new ToggleSwitch(new PVector(LEFT_MARGIN, 4*MARGIN + ToggleSwitch.h*4)).setText("display (c)onflicts"));
        switches.put("exp level", new ToggleSwitch(new PVector(LEFT_MARGIN, 4*MARGIN + ToggleSwitch.h*6)).setText("display (e)xperience level"));
        switches.put("hide errors", new ToggleSwitch(new PVector(LEFT_MARGIN, 4*MARGIN + ToggleSwitch.h*8)).setText("(h)ides errors"));
        switches.put("show sorting time",new ToggleSwitch(new PVector(LEFT_MARGIN, 4*MARGIN + ToggleSwitch.h*10)).setText("in millis (no keybind)"));
    
    }

    public void draw() {
        textSize(24);

        for( ToggleSwitch s : switches.values() ) {
            s.draw(this);
            fill(0);
            text( s.getText(), s.getRight() + MARGIN, s.getCenterY());
        }
    }

    public void mousePressed() {
        for( ToggleSwitch s : switches.values() ) {
            s.handleClick(mouseX, mouseY);
        }
    }

    public void keyReleased() {

    }
}
